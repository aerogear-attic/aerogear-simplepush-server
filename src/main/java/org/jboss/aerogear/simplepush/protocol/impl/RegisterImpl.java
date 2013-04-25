package org.jboss.aerogear.simplepush.protocol.impl;

import static org.jboss.aerogear.simplepush.util.ArgumentUtil.checkNotNull;

import org.jboss.aerogear.simplepush.protocol.Register;

public class RegisterImpl implements Register {
    
    private final String channelId;

    public RegisterImpl(final String channelId) {
        checkNotNull(channelId, "channelId");
        this.channelId = channelId;
    }

    @Override
    public Type getMessageType() {
        return Type.REGISTER;
    }

    @Override
    public String getChannelId() {
        return channelId;
    }
    
    @Override 
    public String toString() {
        return "RegisterMessageImpl[messageType=" + getMessageType() + ", channelId=" + channelId + "]";
    }
    
}
