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

import static org.jboss.aerogear.simplepush.util.ArgumentUtil.checkNotNull;
import java.util.UUID;

import org.jboss.aerogear.simplepush.protocol.HandshakeResponse;

public class HandshakeResponseImpl implements HandshakeResponse {

    private final UUID uaid;

    public HandshakeResponseImpl(final UUID uaid) {
        checkNotNull(uaid, "uaid");
        this.uaid = uaid;
    }

    @Override
    public UUID getUAID() {
        return uaid;
    }

    @Override
    public Type getMessageType() {
        return Type.HELLO;
    }

    @Override
    public String toString() {
        return "HandshakeResponseImpl[messageType=" + getMessageType() + ", uaid=" + uaid + "]";
    }

}
