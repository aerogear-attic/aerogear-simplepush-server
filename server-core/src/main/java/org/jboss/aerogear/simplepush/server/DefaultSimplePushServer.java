/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.simplepush.server;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.jboss.aerogear.simplepush.protocol.AckMessage;
import org.jboss.aerogear.simplepush.protocol.HandshakeMessage;
import org.jboss.aerogear.simplepush.protocol.HandshakeResponse;
import org.jboss.aerogear.simplepush.protocol.NotificationMessage;
import org.jboss.aerogear.simplepush.protocol.RegisterMessage;
import org.jboss.aerogear.simplepush.protocol.RegisterResponse;
import org.jboss.aerogear.simplepush.protocol.Status;
import org.jboss.aerogear.simplepush.protocol.UnregisterMessage;
import org.jboss.aerogear.simplepush.protocol.UnregisterResponse;
import org.jboss.aerogear.simplepush.protocol.Update;
import org.jboss.aerogear.simplepush.protocol.impl.HandshakeResponseImpl;
import org.jboss.aerogear.simplepush.protocol.impl.NotificationMessageImpl;
import org.jboss.aerogear.simplepush.protocol.impl.RegisterResponseImpl;
import org.jboss.aerogear.simplepush.protocol.impl.StatusImpl;
import org.jboss.aerogear.simplepush.protocol.impl.UnregisterResponseImpl;
import org.jboss.aerogear.simplepush.protocol.impl.UpdateImpl;
import org.jboss.aerogear.simplepush.server.datastore.ChannelNotFoundException;
import org.jboss.aerogear.simplepush.server.datastore.DataStore;
import org.jboss.aerogear.simplepush.util.VersionExtractor;

public class DefaultSimplePushServer implements SimplePushServer {

    private final DataStore store;
    private final SimplePushServerConfig config;

    public DefaultSimplePushServer(final DataStore store, final SimplePushServerConfig config) {
        this.store = store;
        this.config = config;
    }

    public HandshakeResponse handleHandshake(final HandshakeMessage handshake) {
        for (String channelId : handshake.getChannelIds()) {
            store.saveChannel(new DefaultChannel(handshake.getUAID(), channelId, endpointUrl(channelId)));
        }
        return new HandshakeResponseImpl(handshake.getUAID());
    }

    public RegisterResponse handleRegister(final RegisterMessage register, final UUID uaid) {
        final String channelId = register.getChannelId();
        final String pushEndpoint = endpointUrl(channelId);
        final boolean saved = store.saveChannel(new DefaultChannel(uaid, channelId, pushEndpoint));
        final Status status = saved ? new StatusImpl(200, "OK") : new StatusImpl(409, "Conflict: channeld [" + channelId + " is already in use");
        return new RegisterResponseImpl(channelId, status, pushEndpoint);
    }

    public NotificationMessage handleNotification(final String channelId, final UUID uaid, final String body) throws ChannelNotFoundException {
        final Long version = Long.valueOf(VersionExtractor.extractVersion(body));
        final Channel channel = getChannel(channelId);
        channel.setVersion(version);
        final NotificationMessage notification = new NotificationMessageImpl(new HashSet<Update>(Arrays.asList(new UpdateImpl(channelId, version))));
        store.storeUpdates(notification.getUpdates(), uaid);
        return notification;
    }

    public UnregisterResponse handleUnregister(UnregisterMessage unregister, final UUID uaid) {
        final String channelId = unregister.getChannelId();
        try {
            removeChannel(channelId, uaid);
            return new UnregisterResponseImpl(channelId, new StatusImpl(200, "OK"));
        } catch (final Exception e) {
            return new UnregisterResponseImpl(channelId, new StatusImpl(500, "Could not remove the channel"));
        }
    }

    public Set<Update> handleAcknowledgement(final AckMessage ack, final UUID uaid) {
        final Set<Update> acks = ack.getUpdates();
        final Set<Update> waitingForAcks = store.getUpdates(uaid);
        final Set<Update> unacked = new HashSet<Update>(waitingForAcks);
        for (Update update : waitingForAcks) {
            if (acks.contains(update)) {
                final boolean removed = store.removeUpdate(update, uaid);
                if (removed) {
                    unacked.remove(update);
                }
            }
        }
        return unacked;
    }

    @Override
    public Set<Update> getUnacknowledged(final UUID uaid) {
        return store.getUpdates(uaid);
    }

    public UUID getUAID(final String channelId) throws ChannelNotFoundException {
        return getChannel(channelId).getUAID();
    }

    public Channel getChannel(final String channelId) throws ChannelNotFoundException {
        final Channel channel = store.getChannel(channelId);
        if (channel == null) {
            throw new ChannelNotFoundException("Could not find a channel with id [" + channelId + "]", channelId);
        }
        return channel;
    }

    public boolean hasChannel(final String channelId) {
        final Channel channel = store.getChannel(channelId);
        return channel != null;
    }

    public boolean removeChannel(final String channnelId, final UUID uaid) {
        final Channel channel = store.getChannel(channnelId);
        if (channel != null && channel.getUAID().equals(uaid)) {
            return store.removeChannel(channnelId);
        }
        return false;
    }

    public void removeChannels(final UUID uaid) {
        store.removeChannels(uaid);
    }

    private String endpointUrl(final String channelId) {
        return config.endpointUrlPrefix() + "/" + channelId;
    }

    @Override
    public UUID fromChannel(final String channelId) throws ChannelNotFoundException {
        return getChannel(channelId).getUAID();
    }

    @Override
    public void removeAllChannels(final UUID uaid) {
        store.removeChannels(uaid);
    }

    @Override
    public SimplePushServerConfig config() {
        return config;
    }

}
