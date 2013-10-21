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

import org.jboss.aerogear.simplepush.protocol.Ack;
import org.jboss.aerogear.simplepush.server.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link DataStore} implementation that stores all information in memory.
 */
public class InMemoryDataStore implements DataStore {

    private final ConcurrentMap<String, Channel> channels = new ConcurrentHashMap<String, Channel>();
    private final ConcurrentMap<String, Set<Ack>> notifiedChannels = new ConcurrentHashMap<String, Set<Ack>>();
    private final Logger logger = LoggerFactory.getLogger(InMemoryDataStore.class);

    @Override
    public boolean saveChannel(final Channel ch) {
        checkNotNull(ch, "ch");
        final Channel previous = channels.putIfAbsent(ch.getChannelId(), ch);
        return previous == null;
    }

    @Override
    public boolean removeChannel(final String channelId) {
        checkNotNull(channelId, "channelId");
        final Channel channel = channels.remove(channelId);
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
                channels.remove(channel.getChannelId());
                logger.info("Removing [" + channel.getChannelId() + "] for UserAgent [" + uaid + "]");
            }
        }
        notifiedChannels.remove(uaid);
    }

    @Override
    public Integer removeChannels(final Set<String> channelIds) {
        checkNotNull(channelIds, "channelIds");
        int removed = 0;
        for (String channelId : channelIds) {
            channels.remove(channelId);
            removed++;
            logger.debug("Removing [" + channelId + "]");
        }
        return removed;
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
    public void saveUnacknowledged(final Set<Ack> acks, final String uaid) {
        checkNotNull(uaid, "uaid");
        checkNotNull(acks, "acks");
        final Set<Ack> newAcks = Collections.newSetFromMap(new ConcurrentHashMap<Ack, Boolean>());
        newAcks.addAll(acks);
        while (true) {
            final Set<Ack> currentAcks = notifiedChannels.get(uaid);
            if (currentAcks == null) {
                final Set<Ack> previous = notifiedChannels.putIfAbsent(uaid, newAcks);
                if (previous != null) {
                    newAcks.addAll(previous);
                    if (notifiedChannels.replace(uaid, previous, newAcks)) {
                        break;
                    }
                }
            } else {
                newAcks.addAll(currentAcks);
                if (notifiedChannels.replace(uaid, currentAcks, newAcks)) {
                    break;
                }
            }
        }
    }

    @Override
    public Set<Ack> getUnacknowledged(final String uaid) {
        checkNotNull(uaid, "uaid");
        final Set<Ack> acks = notifiedChannels.get(uaid);
        if (acks == null) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(acks);
    }

    @Override
    public boolean removeAcknowledged(final Ack ack, final String uaid) {
        checkNotNull(ack, "ack");
        checkNotNull(uaid, "uaid");
        while (true) {
            final Set<Ack> currentAcks = notifiedChannels.get(uaid);
            if (currentAcks == null || currentAcks.isEmpty()) {
                return false;
            }
            final Set<Ack> newAcks = Collections.newSetFromMap(new ConcurrentHashMap<Ack, Boolean>());
            newAcks.addAll(currentAcks);
            if (newAcks.remove(ack)) {
                if (notifiedChannels.replace(uaid, currentAcks, newAcks)) {
                    return true;
                }
            } else {
                return false;
            }
        }
    }

}
