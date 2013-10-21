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
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.jboss.aerogear.simplepush.protocol.Ack;
import org.jboss.aerogear.simplepush.protocol.AckMessage;
import org.jboss.aerogear.simplepush.protocol.MessageType;
import org.jboss.aerogear.simplepush.protocol.impl.json.JsonUtil;
import org.junit.Test;

public class AckMessageImplTest {

    @Test
    public void constructNullUpdates() {
        final AckMessage ack = new AckMessageImpl(null);
        assertThat(ack.getMessageType(), is(equalTo(MessageType.Type.ACK)));
        assertThat(ack.getAcks().isEmpty(), is(true));
    }

    @Test
    public void constructWithUpdates() {
        final Set<Ack> acks = acks(ack("abc123", 1L), ack("efg456", 20L));
        final AckMessage ack = new AckMessageImpl(acks);
        assertThat(ack.getAcks(), hasItems(ack("abc123", 1L), ack("efg456", 20L)));
    }

    @Test
    public void fromJson() {
        final String json = "{\"messageType\": \"ack\", \"updates\": [{\"channelID\": \"abc123\", \"version\": 20}, {\"channelID\": \"efg456\", \"version\": 10}]}";
        final AckMessage ack = JsonUtil.fromJson(json, AckMessageImpl.class);
        assertThat(ack.getMessageType(), is(equalTo(MessageType.Type.ACK)));
        assertThat(ack.getAcks(), hasItems(ack("abc123", 20L), ack("efg456", 10L)));
    }

    @Test
    public void toJson() {
        final Set<Ack> acks = acks(ack("abc123", 1L), ack("efg456", 20L));
        final String json = JsonUtil.toJson(new AckMessageImpl(acks));
        final AckMessage ack = JsonUtil.fromJson(json, AckMessageImpl.class);
        assertThat(ack.getMessageType(), is(equalTo(MessageType.Type.ACK)));
        assertThat(ack.getAcks(), hasItems(ack("abc123", 1L), ack("efg456", 20L)));
    }

    private Ack ack(final String channelId, final Long version) {
        return new AckImpl(channelId, version);
    }

    private Set<Ack> acks(final Ack... acks) {
        return new HashSet<Ack>(Arrays.asList(acks));
    }

}
