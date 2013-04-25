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
