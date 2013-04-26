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
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.jboss.aerogear.simplepush.protocol.MessageType;
import org.jboss.aerogear.simplepush.protocol.Notification;
import org.jboss.aerogear.simplepush.protocol.Update;
import org.jboss.aerogear.simplepush.protocol.impl.json.JsonUtil;
import org.junit.Test;

public class NotificationImplTest {

    @Test (expected = NullPointerException.class)
    public void constructWithNullUpdates() {
        new NotificationImpl(null);
    }
    
    @Test (expected = IllegalArgumentException.class)
    public void constructWithEmptyUpdates() {
        new NotificationImpl(Collections.<Update>emptySet());
    }
    
    @Test
    public void fromJson() {
        final String json = "{\"messageType\": \"notification\", \"updates\": [{\"channelID\": \"abc123\", \"version\": \"efg456\"}]}";
        final Notification notification = JsonUtil.fromJson(json, NotificationImpl.class);
        assertThat(notification.getMessageType(), is(equalTo(MessageType.Type.NOTIFICATION)));
        assertThat(notification.getUpdates(), hasItem(new UpdateImpl("abc123", "efg456")));
    }
    
    @Test
    public void toJson() {
        final Set<Update> updates = new HashSet<Update>(Arrays.asList(new UpdateImpl("abc123", "efg456")));
        final String json = JsonUtil.toJson(new NotificationImpl(updates));
        final NotificationImpl notification = JsonUtil.fromJson(json, NotificationImpl.class);
        assertThat(notification.getMessageType(), is(equalTo(MessageType.Type.NOTIFICATION)));
        assertThat(notification.getUpdates(), hasItem(new UpdateImpl("abc123", "efg456")));
    }

}
