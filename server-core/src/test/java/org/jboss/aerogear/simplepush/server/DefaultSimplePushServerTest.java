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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.jboss.aerogear.simplepush.protocol.Ack;
import org.jboss.aerogear.simplepush.protocol.HelloMessage;
import org.jboss.aerogear.simplepush.protocol.HelloResponse;
import org.jboss.aerogear.simplepush.protocol.MessageType;
import org.jboss.aerogear.simplepush.protocol.RegisterResponse;
import org.jboss.aerogear.simplepush.protocol.impl.AckMessageImpl;
import org.jboss.aerogear.simplepush.protocol.impl.HelloMessageImpl;
import org.jboss.aerogear.simplepush.protocol.impl.RegisterMessageImpl;
import org.jboss.aerogear.simplepush.protocol.impl.AckImpl;
import org.jboss.aerogear.simplepush.server.datastore.ChannelNotFoundException;
import org.jboss.aerogear.simplepush.server.datastore.DataStore;
import org.jboss.aerogear.simplepush.server.datastore.InMemoryDataStore;
import org.jboss.aerogear.simplepush.server.datastore.VersionException;
import org.jboss.aerogear.simplepush.util.CryptoUtil;
import org.jboss.aerogear.simplepush.util.UUIDUtil;
import org.junit.Before;
import org.junit.Test;

public class DefaultSimplePushServerTest {

    private DefaultSimplePushServer server;

    @Before
    public void setup() {
        final DataStore dataStore = new InMemoryDataStore();
        server = new DefaultSimplePushServer(dataStore, DefaultSimplePushConfig.create().tokenKey("test").build());
    }

    @Test
    public void handleHandshake() {
        final HelloResponse response = server.handleHandshake(new HelloMessageImpl());
        assertThat(response.getUAID(), is(notNullValue()));
    }

    @Test
    public void handleHandshakeWithNullUaid() {
        final HelloResponse response = server.handleHandshake(new HelloMessageImpl(null));
        assertThat(response.getUAID(), is(notNullValue()));
    }

    @Test
    public void handleHandshakeWithExistingUaid() {
        final String uaid = UUIDUtil.newUAID();
        final HelloResponse response = server.handleHandshake(new HelloMessageImpl(uaid));
        assertThat(response.getUAID(), equalTo(uaid));
    }

    @Test
    public void handleHandshakeWithInvalidUaid() {
        final String uaid = "bajja11122";
        final HelloResponse response = server.handleHandshake(new HelloMessageImpl(uaid));
        assertThat(response.getUAID(), is(notNullValue()));
    }

    @Test
    public void handleHandshakeWithChannels() throws ChannelNotFoundException {
        final String channelId1 = UUID.randomUUID().toString();
        final String channelId2 = UUID.randomUUID().toString();
        final Set<String> channelIds = new HashSet<String>(Arrays.asList(channelId1, channelId2));
        final HelloMessage handshakeImpl = new HelloMessageImpl(UUIDUtil.newUAID(), channelIds);
        final HelloResponse response = server.handleHandshake(handshakeImpl);
        assertThat(response.getUAID(), is(notNullValue()));
        assertThat(server.getChannel(channelId1), is(notNullValue()));
        assertThat(server.getChannel(channelId2), is(notNullValue()));
    }

    @Test
    public void handleHandshakeWithEmptyChannels() throws ChannelNotFoundException {
        final Set<String> channelIds = Collections.emptySet();
        final String uaid = UUIDUtil.newUAID();
        final HelloMessage handshakeImpl = new HelloMessageImpl(uaid, channelIds);
        final HelloResponse response = server.handleHandshake(handshakeImpl);
        assertThat(response.getUAID(), is(notNullValue()));
        assertThat(server.hasChannel(uaid, "channel1"), is(false));
    }

    @Test
    public void handleHandshakeWithExistingAndEmptyChannelIDsInHello() throws ChannelNotFoundException {
        final String channelId1 = UUID.randomUUID().toString();
        final String channelId2 = UUID.randomUUID().toString();
        final Set<String> channelIds = new HashSet<String>(Arrays.asList(channelId1, channelId2));
        final String uaid = UUIDUtil.newUAID();
        final HelloMessage firstHello = new HelloMessageImpl(uaid, channelIds);
        final HelloResponse firstResponse = server.handleHandshake(firstHello);
        assertThat(firstResponse.getUAID(), equalTo(uaid));
        assertThat(server.hasChannel(uaid, channelId1), is(true));
        assertThat(server.hasChannel(uaid, channelId2), is(true));

        final HelloMessage nextHello = new HelloMessageImpl(uaid, Collections.<String>emptySet());
        final HelloResponse secondResponse = server.handleHandshake(nextHello);
        assertThat(secondResponse.getUAID(), equalTo(uaid));
        assertThat(server.hasChannel(secondResponse.getUAID(), channelId1), is(false));
        assertThat(server.hasChannel(secondResponse.getUAID(), channelId2), is(false));
    }

    @Test
    public void handleHandshakeWithExistingAndNewChannels() throws ChannelNotFoundException {
        final String uaid = UUIDUtil.newUAID();
        final Set<String> channelIds = new HashSet<String>(Arrays.asList("channel1", "channel2"));
        final HelloMessage firstHello = new HelloMessageImpl(uaid, channelIds);
        final HelloResponse firstResponse = server.handleHandshake(firstHello);
        assertThat(firstResponse.getUAID(), equalTo(uaid));
        assertThat(server.hasChannel(uaid, "channel1"), is(true));
        assertThat(server.hasChannel(uaid, "channel2"), is(true));

        final Set<String> newChannelIds = new HashSet<String>(Arrays.asList("channel3", "channel4"));
        final HelloMessage nextHello = new HelloMessageImpl(uaid, newChannelIds);
        final HelloResponse secondResponse = server.handleHandshake(nextHello);
        assertThat(secondResponse.getUAID(), equalTo(uaid));
        assertThat(server.hasChannel(uaid, "channel1"), is(false));
        assertThat(server.hasChannel(uaid, "channel2"), is(false));
        assertThat(server.hasChannel(uaid, "channel3"), is(true));
        assertThat(server.hasChannel(uaid, "channel4"), is(true));
    }

    @Test
    public void handleHandshakeWithChannelsButNoUaid() {
        final Set<String> channelIds = new HashSet<String>(Arrays.asList("channel1", "channel2"));
        final HelloMessage handshakeImpl = new HelloMessageImpl(null, channelIds);
        final HelloResponse response = server.handleHandshake(handshakeImpl);
        assertThat(response.getUAID(), is(notNullValue()));
        assertThat(server.hasChannel(handshakeImpl.getUAID(), "channel1"), is(false));
        assertThat(server.hasChannel(handshakeImpl.getUAID(), "channel2"), is(false));
    }

    @Test
    public void handeRegister() {
        final RegisterResponse response = server.handleRegister(new RegisterMessageImpl("someChannelId"), UUIDUtil.newUAID());
        assertThat(response.getChannelId(), equalTo("someChannelId"));
        assertThat(response.getMessageType(), equalTo(MessageType.Type.REGISTER));
        assertThat(response.getStatus().getCode(), equalTo(200));
        assertThat(response.getStatus().getMessage(), equalTo("OK"));
        assertThat(response.getPushEndpoint().startsWith("http://127.0.0.1:7777/update"), is(true));
    }

    @Test
    public void removeChannel() throws ChannelNotFoundException {
        final String channelId = "testChannelId";
        final String uaid = UUIDUtil.newUAID();
        server.handleRegister(new RegisterMessageImpl(channelId), uaid);
        assertThat(server.getChannel(channelId).getChannelId(), is(equalTo(channelId)));
        assertThat(server.removeChannel(channelId, UUIDUtil.newUAID()), is(false));
        assertThat(server.removeChannel(channelId, uaid), is(true));
        assertThat(server.removeChannel(channelId, uaid), is(false));
    }

    @Test
    public void getUAID() throws ChannelNotFoundException {
        final String channelId = UUID.randomUUID().toString();
        final String uaid = UUIDUtil.newUAID();
        server.handleRegister(new RegisterMessageImpl(channelId), uaid);
        assertThat(server.getUAID(channelId), is(equalTo(uaid)));
    }

    @Test
    public void handleNotification() throws ChannelNotFoundException {
        final String channelId = UUID.randomUUID().toString();
        final String uaid = UUIDUtil.newUAID();
        final RegisterResponse registerResponse = server.handleRegister(new RegisterMessageImpl(channelId), uaid);
        final String endpointToken = extractEndpointToken(registerResponse.getPushEndpoint());
        Notification notification = server.handleNotification(endpointToken, "version=1");
        assertThat(notification.ack(), equalTo((Ack)new AckImpl(channelId, 1L)));
        assertThat(server.getChannel(channelId).getVersion(), is(1L));
        notification = server.handleNotification(endpointToken, "version=2");
        assertThat(server.getChannel(channelId).getVersion(), is(2L));
    }

    private String extractEndpointToken(final String pushEndpoint) {
        return pushEndpoint.substring(pushEndpoint.lastIndexOf('/') + 1);
    }

    @Test (expected = VersionException.class)
    public void handleNotificationWithVersionLessThanCurrentVersion() throws ChannelNotFoundException {
        final String channelId = UUID.randomUUID().toString();
        final String uaid = UUIDUtil.newUAID();
        final RegisterResponse registerResponse = server.handleRegister(new RegisterMessageImpl(channelId), uaid);
        final String endpointToken = extractEndpointToken(registerResponse.getPushEndpoint());
        server.handleNotification(endpointToken, "version=10");
        server.handleNotification(endpointToken, "version=2");
    }

    @Test (expected = ChannelNotFoundException.class)
    public void handleNotificationNonExistingChannelId() throws ChannelNotFoundException {
        final String channelId = UUID.randomUUID().toString();
        final String uaid = UUIDUtil.newUAID();
        final String endpointToken = CryptoUtil.endpointToken(channelId, uaid, server.config().tokenKey());
        server.handleNotification(endpointToken, "version=1");
    }

    @Test
    public void handleAck() throws ChannelNotFoundException {
        final String channelId_1 = UUID.randomUUID().toString();
        final String channelId_2 = UUID.randomUUID().toString();
        final String uaid = UUIDUtil.newUAID();
        final RegisterResponse registerResponse1 = server.handleRegister(new RegisterMessageImpl(channelId_1), uaid);
        final String endpointToken1 = extractEndpointToken(registerResponse1.getPushEndpoint());
        final RegisterResponse registerResponse2 = server.handleRegister(new RegisterMessageImpl(channelId_2), uaid);
        final String endpointToken2 = extractEndpointToken(registerResponse2.getPushEndpoint());
        server.handleNotification(endpointToken1, "version=10");
        server.handleNotification(endpointToken2, "version=23");

        final Ack ackChannel_1 = new AckImpl(channelId_1, 10L);
        final Set<Ack> unacked = server.handleAcknowledgement(new AckMessageImpl(asSet(ackChannel_1)), uaid);
        assertThat(unacked, hasItem(new AckImpl(channelId_2, 23L)));
    }

    private Set<Ack> asSet(final Ack... update) {
        return new HashSet<Ack>(Arrays.asList(update));
    }

}
