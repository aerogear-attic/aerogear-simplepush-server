package org.jboss.aerogear.simplepush.protocol.impl;

import static org.jboss.aerogear.simplepush.util.ArgumentUtil.checkNotNull;
import org.jboss.aerogear.simplepush.protocol.Unregister;

public class UnregisterImpl implements Unregister {
    
    private String channelId;

    public UnregisterImpl(final String channelId) {
        checkNotNull(channelId, "channelId");
        this.channelId = channelId;
    }

    @Override
    public Type getMessageType() {
        return Type.UNREGISTER;
    }

    @Override
    public String getChannelId() {
        return channelId;
    }
    
    @Override
    public String toString() {
        return "UnregisterImpl[messageType=" + getMessageType() + ", channelId=" + channelId + "]";
    }

}
