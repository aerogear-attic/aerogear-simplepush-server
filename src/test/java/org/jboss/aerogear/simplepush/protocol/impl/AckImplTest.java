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
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.jboss.aerogear.simplepush.protocol.Ack;
import org.jboss.aerogear.simplepush.protocol.MessageType;
import org.jboss.aerogear.simplepush.protocol.impl.json.JsonUtil;
import org.junit.Test;

public class AckImplTest {

    @Test
    public void constructNullUpdates() {
        final Ack ack = new AckImpl(null);
        assertThat(ack.getMessageType(), is(equalTo(MessageType.Type.ACK)));
        assertThat(ack.getUpdates().isEmpty(), is(true));
    }
    
    @Test
    public void constructWithUpdates() {
        final Set<String> updates = new HashSet<String>(Arrays.asList("abc123", "efg456"));
        final Ack ack = new AckImpl(updates);
        assertThat(ack.getUpdates(), hasItems("abc123", "efg456"));
    }
    
    @Test
    public void fromJson() {
        final String json = "{\"messageType\": \"ack\", \"updates\": [\"abc123\", \"efg456\"]}";
        final Ack ack = JsonUtil.fromJson(json, AckImpl.class);
        assertThat(ack.getMessageType(), is(equalTo(MessageType.Type.ACK)));
        assertThat(ack.getUpdates(), hasItems("abc123", "efg456"));
    }
    
    @Test 
    public void toJson() {
        final Set<String> updates = new HashSet<String>(Arrays.asList("abc123", "efg456"));
        final String json = JsonUtil.toJson(new AckImpl(updates));
        final Ack ack = JsonUtil.fromJson(json, AckImpl.class);
        assertThat(ack.getMessageType(), is(equalTo(MessageType.Type.ACK)));
        assertThat(ack.getUpdates(), hasItems("abc123", "efg456"));
    }

}
