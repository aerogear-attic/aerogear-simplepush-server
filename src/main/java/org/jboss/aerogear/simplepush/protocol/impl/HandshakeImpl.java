package org.jboss.aerogear.simplepush.protocol.impl;


import static org.jboss.aerogear.simplepush.util.UUIDUtil.createVersion4Id;
import static org.jboss.aerogear.simplepush.util.UUIDUtil.nullOrEmpty;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import org.jboss.aerogear.simplepush.protocol.Handshake;


public class HandshakeImpl implements Handshake {
    
    private final UUID uaid;
    private final Set<String> channelIds;
    
    public HandshakeImpl() {
        this(""); 
    }
    
    public HandshakeImpl(final String uaid) {
        this(uaid, Collections.<String>emptySet());
    }

    public HandshakeImpl(final String uaid, final Set<String> channelIds) {
        this.uaid = nullOrEmpty(uaid) ? createVersion4Id() : UUID.fromString(uaid);
        this.channelIds = channelIds;
    }
    
    @Override
    public UUID getUAID() {
        return uaid;
    }
    
    @Override
    public Set<String> getChannelIds() {
        return Collections.unmodifiableSet(channelIds);
    }

    @Override
    public Type getMessageType() {
        return Type.HELLO;
    }
    
    @Override
    public String toString() {
        return "HandshakeImpl[messageType=" + getMessageType() + ", uaid=" + uaid + ", channelIds=" + channelIds + "]";
    }

}
