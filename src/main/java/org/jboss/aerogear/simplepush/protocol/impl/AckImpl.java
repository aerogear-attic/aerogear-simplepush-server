package org.jboss.aerogear.simplepush.protocol.impl;

import java.util.Collections;
import java.util.Set;

import org.jboss.aerogear.simplepush.protocol.Ack;

public class AckImpl implements Ack {
    
    private final Set<String> updates;
    
    public AckImpl(final Set<String> updates) {
        this.updates = updates == null ? Collections.<String>emptySet() : updates;
    }

    @Override
    public Type getMessageType() {
        return Type.ACK;
    }

    @Override
    public Set<String> getUpdates() {
        return Collections.unmodifiableSet(updates);
    }
    
    @Override
    public String toString() {
        return "AckImpl[messageType=" + getMessageType() + ",update=" + updates + "]";
    }

}
