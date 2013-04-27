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
package org.jboss.aerogear.simplepush.protocol.impl;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.jboss.aerogear.simplepush.util.UUIDUtil.newUAID;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.jboss.aerogear.simplepush.protocol.Handshake;
import org.jboss.aerogear.simplepush.protocol.MessageType;
import org.jboss.aerogear.simplepush.protocol.impl.json.JsonUtil;
import org.jboss.aerogear.simplepush.util.UUIDUtil;
import org.junit.Test;

public class HandshakeImplTest {

    @Test
    public void constructWithNullUAID() {
        final HandshakeImpl handshake = new HandshakeImpl(null);
        assertThat(handshake.getUAID(), notNullValue());
        assertThat(handshake.getChannelIds().isEmpty(), is(true));
    }
    
    @Test
    public void constructWithEmptyUAID() {
        final HandshakeImpl handshake = new HandshakeImpl("");
        assertThat(handshake.getUAID(), notNullValue());
        assertThat(handshake.getChannelIds().isEmpty(), is(true));
    }
    
    @Test
    public void constructWithChannelIds() {
        final HandshakeImpl handshake= new HandshakeImpl(newUAID().toString(), channelIds("123abc", "efg456"));
        assertThat(handshake.getUAID(), notNullValue());
        assertThat(handshake.getChannelIds(), hasItems("123abc", "efg456"));
    }
    
    @Test (expected = UnsupportedOperationException.class)
    public void channelIdsUnmodifiable() {
        final HandshakeImpl handshake = new HandshakeImpl(newUAID().toString(), channelIds("123abc", "efg456"));
        handshake.getChannelIds().remove("123abc");
    }
    
    @Test
    public void fromJson() {
        final UUID uaid = UUIDUtil.newUAID();
        final String json = "{\"messageType\": \"hello\", \"uaid\": \"" + uaid + "\", \"channelIDs\": [\"123abc\", \"efg456\"]}";
        final Handshake handshake = JsonUtil.fromJson(json, HandshakeImpl.class);
        assertThat(handshake.getMessageType(), is(equalTo(MessageType.Type.HELLO)));
        assertThat(handshake.getUAID(), is(equalTo(uaid)));
        assertThat(handshake.getChannelIds(), hasItems("123abc", "efg456"));
    }
    
    @Test
    public void fromJsonWithoutUAID() {
        final String json = "{\"messageType\": \"hello\"}";
        final Handshake handshake = JsonUtil.fromJson(json, HandshakeImpl.class);
        assertThat(handshake.getMessageType(), is(equalTo(MessageType.Type.HELLO)));
        assertThat(handshake.getUAID(), is(notNullValue()));
        assertThat(handshake.getChannelIds().isEmpty(), is(true));
    }
    
    @Test
    public void fromJsonWithNullChannelIds() {
        final UUID uaid = UUIDUtil.newUAID();
        final String json = "{\"messageType\": \"hello\", \"uaid\": \"" + uaid + "\"}";
        final Handshake handshake = JsonUtil.fromJson(json, HandshakeImpl.class);
        assertThat(handshake.getMessageType(), is(equalTo(MessageType.Type.HELLO)));
        assertThat(handshake.getUAID(), is(equalTo(uaid)));
        assertThat(handshake.getChannelIds().isEmpty(), is(true));
    }
    
    @Test
    public void toJson() {
        final UUID uaid = UUIDUtil.newUAID();
        final HandshakeImpl handshake = new HandshakeImpl(uaid.toString(), channelIds("123abc", "efg456"));
        final String asJson = JsonUtil.toJson(handshake);
        final Handshake parsed = JsonUtil.fromJson(asJson, HandshakeImpl.class);
        assertThat(parsed.getMessageType(), is(equalTo(MessageType.Type.HELLO)));
        assertThat(parsed.getUAID(), is(equalTo(uaid)));
        assertThat(parsed.getChannelIds(), hasItems("123abc", "efg456"));
    }
    
    
    private Set<String> channelIds(final String... channelIds) {
        return new HashSet<String>(Arrays.asList(channelIds));
    }
    
}
