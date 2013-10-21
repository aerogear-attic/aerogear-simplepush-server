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
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.jboss.aerogear.simplepush.protocol.MessageType;
import org.jboss.aerogear.simplepush.protocol.NotificationMessage;
import org.jboss.aerogear.simplepush.protocol.Ack;
import org.jboss.aerogear.simplepush.protocol.impl.json.JsonUtil;
import org.junit.Test;

public class NotificationMessageImplTest {

    @Test(expected = NullPointerException.class)
    public void constructWithNullUpdates() {
        new NotificationMessageImpl(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructWithEmptyUpdates() {
        new NotificationMessageImpl(Collections.<Ack> emptySet());
    }

    @Test
    public void fromJson() {
        final String json = "{\"messageType\": \"notification\", \"updates\": [{\"channelID\": \"abc123\", \"version\": 1}]}";
        final NotificationMessage notification = JsonUtil.fromJson(json, NotificationMessageImpl.class);
        assertThat(notification.getMessageType(), is(equalTo(MessageType.Type.NOTIFICATION)));
        assertThat(notification.getAcks(), hasItem(new AckImpl("abc123", 1L)));
    }

    @Test
    public void toJson() {
        final Set<Ack> acks = new HashSet<Ack>(Arrays.asList(new AckImpl("abc123", 2L)));
        final String json = JsonUtil.toJson(new NotificationMessageImpl(acks));
        final NotificationMessageImpl notification = JsonUtil.fromJson(json, NotificationMessageImpl.class);
        assertThat(notification.getMessageType(), is(equalTo(MessageType.Type.NOTIFICATION)));
        assertThat(notification.getAcks(), hasItem(new AckImpl("abc123", 2L)));
    }

}
