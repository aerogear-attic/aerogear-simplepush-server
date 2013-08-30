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

import org.jboss.aerogear.simplepush.protocol.HelloMessage;
import org.jboss.aerogear.simplepush.protocol.HelloResponse;
import org.jboss.aerogear.simplepush.protocol.MessageType;
import org.jboss.aerogear.simplepush.protocol.NotificationMessage;
import org.jboss.aerogear.simplepush.protocol.RegisterResponse;
import org.jboss.aerogear.simplepush.protocol.Update;
import org.jboss.aerogear.simplepush.protocol.impl.AckMessageImpl;
import org.jboss.aerogear.simplepush.protocol.impl.HelloMessageImpl;
import org.jboss.aerogear.simplepush.protocol.impl.RegisterMessageImpl;
import org.jboss.aerogear.simplepush.protocol.impl.UpdateImpl;
import org.jboss.aerogear.simplepush.server.datastore.ChannelNotFoundException;
import org.jboss.aerogear.simplepush.server.datastore.InMemoryDataStore;
import org.jboss.aerogear.simplepush.util.UUIDUtil;
import org.junit.Before;
import org.junit.Test;

public class DefaultSimplePushServerTest {

    private DefaultSimplePushServer server;

    @Before
    public void setup() {
        server = new DefaultSimplePushServer(new InMemoryDataStore(), DefaultSimplePushConfig.create().tokenKey("test").build());
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
        final Set<String> channelIds = new HashSet<String>(Arrays.asList("channel1", "channel2"));
        final HelloMessage handshakeImpl = new HelloMessageImpl(UUIDUtil.newUAID().toString(), channelIds);
        final HelloResponse response = server.handleHandshake(handshakeImpl);
        assertThat(response.getUAID(), is(notNullValue()));
        assertThat(server.getChannel("channel1").getChannelId(), is(equalTo("channel1")));
    }

    @Test
    public void handleHandshakeWithEmptyChannels() throws ChannelNotFoundException {
        final Set<String> channelIds = Collections.emptySet();
        final HelloMessage handshakeImpl = new HelloMessageImpl(UUIDUtil.newUAID().toString(), channelIds);
        final HelloResponse response = server.handleHandshake(handshakeImpl);
        assertThat(response.getUAID(), is(notNullValue()));
        assertThat(server.hasChannel("channel1"), is(false));
    }

    @Test
    public void handleHandshakeWithChannelsButNoUaid() {
        final Set<String> channelIds = new HashSet<String>(Arrays.asList("channel1", "channel2"));
        final HelloMessage handshakeImpl = new HelloMessageImpl(null, channelIds);
        final HelloResponse response = server.handleHandshake(handshakeImpl);
        assertThat(response.getUAID(), is(notNullValue()));
        assertThat(server.hasChannel("channel1"), is(false));
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
        final String channelId = "testChannelId";
        final String uaid = UUIDUtil.newUAID();
        server.handleRegister(new RegisterMessageImpl(channelId), uaid);
        assertThat(server.getUAID(channelId), is(equalTo(uaid)));
    }

    @Test
    public void handleNotification() throws ChannelNotFoundException {
        final String channelId = "testChannelId";
        final String uaid = UUIDUtil.newUAID();
        server.handleRegister(new RegisterMessageImpl(channelId), uaid);
        NotificationMessage notification = server.handleNotification(channelId, uaid, "version=1");
        assertThat(notification.getUpdates(), hasItem(new UpdateImpl(channelId, 1L)));
        assertThat(server.getChannel(channelId).getVersion(), is(1L));
        notification = server.handleNotification(channelId, uaid, "version=2");
        assertThat(server.getChannel(channelId).getVersion(), is(2L));
    }

    @Test (expected = IllegalArgumentException.class)
    public void handleNotificationWithVersionLessThanCurrentVersion() throws ChannelNotFoundException {
        final String channelId = "testChannelId";
        final String uaid = UUIDUtil.newUAID();
        server.handleRegister(new RegisterMessageImpl(channelId), uaid);
        server.handleNotification(channelId, uaid, "version=10");
        assertThat(server.getChannel(channelId).getVersion(), is(10L));
        server.handleNotification(channelId, uaid, "version=2");
    }

    @Test (expected = ChannelNotFoundException.class)
    public void handleNotificationNonExistingChannelId() throws ChannelNotFoundException {
        final String channelId = "testChannelId";
        final String uaid = UUIDUtil.newUAID();
        server.handleNotification(channelId, uaid, "version=1");
    }

    @Test
    public void handleAck() throws ChannelNotFoundException {
        final String channelId_1 = "testChannelId_1";
        final String channelId_2 = "testChannelId_2";
        final String uaid = UUIDUtil.newUAID();
        server.handleRegister(new RegisterMessageImpl(channelId_1), uaid);
        server.handleRegister(new RegisterMessageImpl(channelId_2), uaid);
        server.handleNotification(channelId_1, uaid, "version=10");
        server.handleNotification(channelId_2, uaid, "version=23");

        final Update updateChannel_1 = new UpdateImpl(channelId_1, 10L);
        final Set<Update> unacked = server.handleAcknowledgement(new AckMessageImpl(new HashSet<Update>(Arrays.asList(updateChannel_1))), uaid);
        assertThat(unacked, hasItem(new UpdateImpl(channelId_2, 23L)));
    }

}
