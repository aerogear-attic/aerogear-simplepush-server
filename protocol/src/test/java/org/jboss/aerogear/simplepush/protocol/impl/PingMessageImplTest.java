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
package org.jboss.aerogear.simplepush.protocol.impl;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.jboss.aerogear.simplepush.protocol.MessageType;
import org.jboss.aerogear.simplepush.protocol.PingMessage;
import org.jboss.aerogear.simplepush.protocol.impl.json.JsonUtil;
import org.junit.Test;

public class PingMessageImplTest {

    @Test(expected = NullPointerException.class)
    public void constructNullBody() {
        new PingMessageImpl(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructEmptyBody() {
        new PingMessageImpl("");
    }

    @Test
    public void constructWithBody() {
        final PingMessage ping = new PingMessageImpl("{}");
        assertThat(ping.getMessageType(), is(equalTo(MessageType.Type.PING)));
        assertThat(ping.getPingMessage(), equalTo(PingMessage.PING_MESSAGE));
    }

    @Test
    public void fromJson() {
        final String json = "{ }";
        final PingMessage ping = JsonUtil.fromJson(json, PingMessageImpl.class);
        assertThat(ping.getMessageType(), is(equalTo(MessageType.Type.PING)));
        assertThat(ping.getPingMessage(), equalTo(PingMessage.PING_MESSAGE));
    }

    @Test(expected = RuntimeException.class)
    public void fromJsonInvalidPingMessage() {
        final String json = "{\"ping\": \"something\"}";
        JsonUtil.fromJson(json, PingMessageImpl.class);
    }

    @Test
    public void toJson() {
        final String json = JsonUtil.toJson(new PingMessageImpl());
        final PingMessage ping = JsonUtil.fromJson(json, PingMessageImpl.class);
        assertThat(ping.getMessageType(), is(equalTo(MessageType.Type.PING)));
        assertThat(ping.getPingMessage(), equalTo(PingMessage.PING_MESSAGE));
    }

}
