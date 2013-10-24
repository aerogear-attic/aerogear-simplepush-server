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
package org.jboss.aerogear.simplepush.server;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.jboss.aerogear.simplepush.protocol.Ack;
import org.jboss.aerogear.simplepush.protocol.AckMessage;
import org.jboss.aerogear.simplepush.protocol.HelloMessage;
import org.jboss.aerogear.simplepush.protocol.HelloResponse;
import org.jboss.aerogear.simplepush.protocol.RegisterMessage;
import org.jboss.aerogear.simplepush.protocol.RegisterResponse;
import org.jboss.aerogear.simplepush.protocol.Status;
import org.jboss.aerogear.simplepush.protocol.UnregisterMessage;
import org.jboss.aerogear.simplepush.protocol.UnregisterResponse;
import org.jboss.aerogear.simplepush.protocol.impl.HelloResponseImpl;
import org.jboss.aerogear.simplepush.protocol.impl.RegisterResponseImpl;
import org.jboss.aerogear.simplepush.protocol.impl.StatusImpl;
import org.jboss.aerogear.simplepush.protocol.impl.UnregisterResponseImpl;
import org.jboss.aerogear.simplepush.protocol.impl.AckImpl;
import org.jboss.aerogear.simplepush.server.datastore.ChannelNotFoundException;
import org.jboss.aerogear.simplepush.server.datastore.DataStore;
import org.jboss.aerogear.simplepush.util.CryptoUtil;
import org.jboss.aerogear.simplepush.util.VersionExtractor;

/**
 * Concrete implementation of {@link SimplePushServer} that uses a {@link DataStore} to
 * store information about UserAgents and channels.
 */
public class DefaultSimplePushServer implements SimplePushServer {

    private final DataStore store;
    private final SimplePushServerConfig config;

    /**
     * Sole constructor.
     *
     * @param store the {@link DataStore} that this server should use.
     * @param config the {@link SimplePushServerConfig} for this server.
     */
    public DefaultSimplePushServer(final DataStore store, final SimplePushServerConfig config) {
        this.store = store;
        this.config = config;
    }

    @Override
    public HelloResponse handleHandshake(final HelloMessage handshake) {
        final Set<String> oldChannels = store.getChannelIds(handshake.getUAID());
        for (String channelId : handshake.getChannelIds()) {
            if (!oldChannels.contains(channelId)) {
                store.saveChannel(new DefaultChannel(handshake.getUAID(), channelId, generateEndpointToken(handshake.getUAID(), channelId)));
            } else {
                oldChannels.remove(channelId);
            }
        }
        store.removeChannels(oldChannels);
        return new HelloResponseImpl(handshake.getUAID());
    }

    private String generateEndpointToken(final String uaid, final String channelId) {
        return CryptoUtil.endpointToken(uaid, channelId, config.tokenKey());
    }

    @Override
    public RegisterResponse handleRegister(final RegisterMessage register, final String uaid) {
        final String channelId = register.getChannelId();
        final String endpointToken = generateEndpointToken(uaid, channelId);
        final boolean saved = store.saveChannel(new DefaultChannel(uaid, channelId, endpointToken));
        final Status status = saved ? new StatusImpl(200, "OK") : new StatusImpl(409, "Conflict: channeld [" + channelId + " is already in use");
        return new RegisterResponseImpl(channelId, status, makeEndpointUrl(endpointToken));
    }

    @Override
    public Notification handleNotification(final String endpointToken, final String body) throws ChannelNotFoundException {
        final Long version = Long.valueOf(VersionExtractor.extractVersion(body));
        final String channelId = store.updateVersion(endpointToken, version);
        if (channelId == null) {
            throw new ChannelNotFoundException("Could not find channel for endpoint [" + endpointToken + "]", null);
        }
        final Ack ack = new AckImpl(channelId, version);
        final String uaid = store.saveUnacknowledged(channelId, ack.getVersion());
        return new Notification(uaid, ack);
    }

    @Override
    public UnregisterResponse handleUnregister(final UnregisterMessage unregister, final String uaid) {
        final String channelId = unregister.getChannelId();
        try {
            removeChannel(channelId, uaid);
            return new UnregisterResponseImpl(channelId, new StatusImpl(200, "OK"));
        } catch (final Exception e) {
            return new UnregisterResponseImpl(channelId, new StatusImpl(500, "Could not remove the channel"));
        }
    }

    @Override
    public Set<Ack> handleAcknowledgement(final AckMessage ackMessage, final String uaid) {
        return store.removeAcknowledged(uaid, ackMessage.getAcks());
    }

    @Override
    public Set<Ack> getUnacknowledged(final String uaid) {
        return store.getUnacknowledged(uaid);
    }

    public String getUAID(final String channelId) throws ChannelNotFoundException {
        return getChannel(channelId).getUAID();
    }

    public Channel getChannel(final String channelId) throws ChannelNotFoundException {
        return store.getChannel(channelId);
    }

    public boolean hasChannel(final String uaid, final String channelId) {
        try {
            final Channel channel = store.getChannel(channelId);
            return channel.getUAID().equals(uaid);
        } catch (final ChannelNotFoundException e) {
            return false;
        }
    }

    public boolean removeChannel(final String channnelId, final String uaid) {
        try {
            final Channel channel = store.getChannel(channnelId);
            if (channel.getUAID().equals(uaid)) {
                store.removeChannels(new HashSet<String>(Arrays.asList(channnelId)));
                return true;
            }
        } catch (final ChannelNotFoundException ignored) {
        }
        return false;
    }

    private String makeEndpointUrl(final String endpointToken) {
        return config.endpointUrl() + "/" + endpointToken;
    }

    @Override
    public void removeAllChannels(final String uaid) {
        store.removeChannels(uaid);
    }

    @Override
    public SimplePushServerConfig config() {
        return config;
    }

}
