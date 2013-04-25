package org.jboss.aerogear.simplepush.protocol.impl;

import static org.jboss.aerogear.simplepush.util.ArgumentUtil.checkNotNull;
import org.jboss.aerogear.simplepush.protocol.RegisterResponse;
import org.jboss.aerogear.simplepush.protocol.Status;

public class RegisterResponseImpl extends RegisterImpl implements RegisterResponse {
    
    private Status status;
    private String pushEndpoint;

    public RegisterResponseImpl(final String channelId, final Status status, final String pushEndpoint) {
        super(channelId);
        checkNotNull(status, "status");
        checkNotNull(pushEndpoint, "pushEndpoint");
        this.status = status;
        this.pushEndpoint = pushEndpoint;
    }

    @Override
    public Status getStatus() {
        return status;
    }

    @Override
    public String getPushEndpoint() {
        return pushEndpoint;
    }
    
    @Override
    public String toString() {
        return new StringBuilder("RegisterResponseImpl[")
        .append("messageType=").append(getMessageType())
        .append(", channelId=").append(getChannelId())
        .append(", status=").append(status)
        .append(", pushEndpoint=").append(pushEndpoint)
        .append("]").toString();
    }

}
