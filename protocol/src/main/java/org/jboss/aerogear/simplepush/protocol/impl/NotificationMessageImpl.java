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

import static org.jboss.aerogear.simplepush.util.ArgumentUtil.checkNotNullAndNotEmpty;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.jboss.aerogear.simplepush.protocol.Ack;
import org.jboss.aerogear.simplepush.protocol.NotificationMessage;

public class NotificationMessageImpl implements NotificationMessage {

    private final Set<Ack> acks;

    public NotificationMessageImpl(final Ack ack) {
        this(new HashSet<Ack>(Arrays.asList(ack)));
    }

    public NotificationMessageImpl(final Set<Ack> acks) {
        checkNotNullAndNotEmpty(acks, "acks");
        this.acks = acks;
    }

    @Override
    public Type getMessageType() {
        return Type.NOTIFICATION;
    }

    @Override
    public Set<Ack> getAcks() {
        return Collections.unmodifiableSet(acks);
    }

    @Override
    public String toString() {
        return "NotificationImpl[messageType=" + getMessageType() + ", acks=" + acks + "]";
    }

}
