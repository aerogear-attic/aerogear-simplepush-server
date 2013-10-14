/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.simplepush.server.datastore;

import static org.jboss.aerogear.simplepush.util.ArgumentUtil.checkNotNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import org.jboss.aerogear.simplepush.protocol.Ack;
import org.jboss.aerogear.simplepush.protocol.impl.AckImpl;
import org.jboss.aerogear.simplepush.server.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link DataStore} implementation that stores all information in memory.
 */
public class InMemoryDataStore implements DataStore {

    private final ConcurrentMap<String, MutableChannel> channels = new ConcurrentHashMap<String, MutableChannel>();
    private final ConcurrentMap<String, MutableChannel> endpoints = new ConcurrentHashMap<String, MutableChannel>();
    private final ConcurrentMap<String, Set<Ack>> unacked = new ConcurrentHashMap<String, Set<Ack>>();
    private final Logger logger = LoggerFactory.getLogger(InMemoryDataStore.class);

    @Override
    public boolean saveChannel(final Channel ch) {
        checkNotNull(ch, "ch");
        final MutableChannel mutableChannel = new MutableChannel(ch);
        final Channel previous = channels.putIfAbsent(ch.getChannelId(), mutableChannel);
        endpoints.put(ch.getEndpointToken(), mutableChannel);
        return previous == null;
    }

    private boolean removeChannel(final String channelId) {
        checkNotNull(channelId, "channelId");
        final Channel channel = channels.remove(channelId);
        if (channel != null) {
            endpoints.remove(endpoints.get(channel.getEndpointToken()));
        }
        return channel != null;
    }

    @Override
    public Channel getChannel(final String channelId) throws ChannelNotFoundException {
        checkNotNull(channelId, "channelId");
        final Channel channel = channels.get(channelId);
        if (channel == null) {
            throw new ChannelNotFoundException("No Channel for [" + channelId + "] was found", channelId);
        }
        return channel;
    }

    @Override
    public void removeChannels(final String uaid) {
        checkNotNull(uaid, "uaid");
        for (Channel channel : channels.values()) {
            if (channel.getUAID().equals(uaid)) {
                removeChannel(channel.getChannelId());
                logger.info("Removing [" + channel.getChannelId() + "] for UserAgent [" + uaid + "]");
            }
        }
        unacked.remove(uaid);
    }

    @Override
    public void removeChannels(final Set<String> channelIds) {
        checkNotNull(channelIds, "channelIds");
        for (String channelId : channelIds) {
            removeChannel(channelId);
            logger.debug("Removing [" + channelId + "]");
        }
    }

    @Override
    public Set<String> getChannelIds(final String uaid) {
        checkNotNull(uaid, "uaid");
        final Set<String> channelIds = new HashSet<String>();
        for (Channel channel : channels.values()) {
            if (channel.getUAID().equals(uaid)) {
                channelIds.add(channel.getChannelId());
            }
        }
        return channelIds;
    }

    @Override
    public String updateVersion(final String endpointToken, final long version) throws VersionException, ChannelNotFoundException {
        final MutableChannel channel = endpoints.get(endpointToken);
        if (channel == null) {
            throw new ChannelNotFoundException("Could not find channel for endpointToken", endpointToken);
        }
        channel.updateVersion(version);
        return channel.getChannelId();
    }

    @Override
    public String saveUnacknowledged(final String channelId, final long version) throws ChannelNotFoundException {
        checkNotNull(channelId, "channelId");
        checkNotNull(version, "version");
        final Channel channel = channels.get(channelId);
        if (channel == null) {
            throw new ChannelNotFoundException("Could not find channel", channelId);
        }
        final String uaid = channel.getUAID();
        final Set<Ack> newAcks = Collections.newSetFromMap(new ConcurrentHashMap<Ack, Boolean>());
        newAcks.add(new AckImpl(channelId, version));
        while (true) {
            final Set<Ack> currentAcks = unacked.get(uaid);
            if (currentAcks == null) {
                final Set<Ack> previous = unacked.putIfAbsent(uaid, newAcks);
                if (previous != null) {
                    newAcks.addAll(previous);
                    if (unacked.replace(uaid, previous, newAcks)) {
                        break;
                    }
                }
            } else {
                newAcks.addAll(currentAcks);
                if (unacked.replace(uaid, currentAcks, newAcks)) {
                    break;
                }
            }
        }
        return uaid;
    }

    @Override
    public Set<Ack> getUnacknowledged(final String uaid) {
        checkNotNull(uaid, "uaid");
        final Set<Ack> acks = unacked.get(uaid);
        if (acks == null) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(acks);
    }

    @Override
    public Set<Ack> removeAcknowledged(final String uaid, final Set<Ack> acked) {
        checkNotNull(uaid, "uaid");
        checkNotNull(acked, "acked");
        while (true) {
            final Set<Ack> currentAcks = unacked.get(uaid);
            if (currentAcks == null || currentAcks.isEmpty()) {
                return Collections.emptySet();
            }
            final Set<Ack> newUpdates = Collections.newSetFromMap(new ConcurrentHashMap<Ack, Boolean>());
            boolean added = newUpdates.addAll(currentAcks);
            if (!added){
                return newUpdates;
            }

            boolean removed = newUpdates.removeAll(acked);
            if (removed) {
                if (unacked.replace(uaid, currentAcks, newUpdates)) {
                    return newUpdates;
                }
            } else {
                return newUpdates;
            }
        }
    }

    private static class MutableChannel implements Channel {

        private final Channel delegate;
        private final AtomicLong version;

        public MutableChannel(final Channel delegate) {
            this.delegate = delegate;
            version = new AtomicLong(delegate.getVersion());
        }

        @Override
        public String getUAID() {
            return delegate.getUAID();
        }

        @Override
        public String getChannelId() {
            return delegate.getChannelId();
        }

        @Override
        public long getVersion() {
            return version.get();
        }

        public void updateVersion(final long newVersion) {
            for (;;) {
                final long currentVersion = version.get();
                if (newVersion <= currentVersion) {
                    throw new VersionException("New version [" + newVersion + "] must be greater than current version [" + currentVersion + "]");
                }
                if (version.compareAndSet(currentVersion, newVersion)) {
                    break;
                }
            }
        }

        @Override
        public String getEndpointToken() {
            return delegate.getEndpointToken();
        }

    }

}
