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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;
import java.util.HashSet;

import org.jboss.aerogear.simplepush.protocol.Handshake;
import org.jboss.aerogear.simplepush.protocol.HandshakeResponse;
import org.jboss.aerogear.simplepush.protocol.MessageType;
import org.jboss.aerogear.simplepush.protocol.RegisterResponse;
import org.jboss.aerogear.simplepush.protocol.impl.HandshakeImpl;
import org.jboss.aerogear.simplepush.protocol.impl.RegisterImpl;
import org.jboss.aerogear.simplepush.util.UUIDUtil;
import org.junit.Test;

public class SimplePushServerTest {

    @Test
    public void handleHandshake() {
        final SimplePushServer server = new SimplePushServer();
        final HandshakeResponse response = server.handleHandshake(new HandshakeImpl());
        assertThat(response.getUAID(), is(notNullValue()));
    }
    
    @Test
    public void handleHandshakeWithChannels() {
        final SimplePushServer server = new SimplePushServer();
        final HashSet<String> channelIds = new HashSet<String>(Arrays.asList("channel1", "channel2"));
        final Handshake handshakeImpl = new HandshakeImpl(UUIDUtil.newUAID().toString(), channelIds);
        final HandshakeResponse response = server.handleHandshake(handshakeImpl);
        assertThat(response.getUAID(), is(notNullValue()));
        assertThat(server.getChannel("channel1").getChannelId(), is(equalTo("channel1")));
    }
    
    @Test
    public void handeRegister() {
        final SimplePushServer server = new SimplePushServer();
        final RegisterResponse response = server.handleRegister(new RegisterImpl("someChannelId"), UUIDUtil.newUAID());
        assertThat(response.getChannelId(), equalTo("someChannelId"));
        assertThat(response.getMessageType(), equalTo(MessageType.Type.REGISTER));
        assertThat(response.getStatus().getCode(), equalTo(200));
        assertThat(response.getStatus().getMessage(), equalTo("OK"));
        assertThat(response.getPushEndpoint(), equalTo("/endpoint/someChannelId"));
    }

}
