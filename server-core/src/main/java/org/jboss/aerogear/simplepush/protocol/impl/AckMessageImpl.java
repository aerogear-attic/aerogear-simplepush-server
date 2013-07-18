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

import java.util.Collections;
import java.util.Set;

import org.jboss.aerogear.simplepush.protocol.AckMessage;
import org.jboss.aerogear.simplepush.protocol.Update;

public class AckMessageImpl implements AckMessage {

    private final Set<Update> updates;

    public AckMessageImpl(final Set<Update> updates) {
        this.updates = updates == null ? Collections.<Update> emptySet() : updates;
    }

    @Override
    public Type getMessageType() {
        return Type.ACK;
    }

    @Override
    public Set<Update> getUpdates() {
        return Collections.unmodifiableSet(updates);
    }

    @Override
    public String toString() {
        return "AckImpl[messageType=" + getMessageType() + ",update=" + updates + "]";
    }

}
