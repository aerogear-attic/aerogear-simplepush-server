package org.jboss.aerogear.simplepush.server;

import java.util.HashSet;
import java.util.Set;

import org.jboss.aerogear.simplepush.protocol.Handshake;
import org.jboss.aerogear.simplepush.protocol.HandshakeResponse;
import org.jboss.aerogear.simplepush.protocol.Register;
import org.jboss.aerogear.simplepush.protocol.RegisterResponse;
import org.jboss.aerogear.simplepush.protocol.Status;
import org.jboss.aerogear.simplepush.protocol.impl.HandshakeResponseImpl;
import org.jboss.aerogear.simplepush.protocol.impl.RegisterResponseImpl;
import org.jboss.aerogear.simplepush.protocol.impl.StatusImpl;

public class SimplePushServer {
    
    private final Set<Channel> channels = new HashSet<Channel>();
    private boolean handshakeCompleted;
    
    public HandshakeResponse handleHandshake(final Handshake handshake) {
        if (!isHandShakeCompleted()) {
            final Set<Channel> channels = new HashSet<Channel>();
            for (String channelId : handshake.getChannelIds()) {
                channels.add(new DefaultChannel(channelId, defaultEndpoint(channelId)));
            }
        }
        handshakeCompleted = true;
        return new HandshakeResponseImpl(handshake.getUAID());
    }
    
    public RegisterResponse handleRegister(final Register register) {
        final String channelId = register.getChannelId();
        final String pushEndpoint = defaultEndpoint(channelId);
        boolean added = channels.add(new DefaultChannel(channelId, pushEndpoint));
        final Status status = added ? new StatusImpl(200, "OK") : new StatusImpl(409, "Conflict: channeld [" + channelId + " is already in use");
        return new RegisterResponseImpl(channelId, status, pushEndpoint);
    }
    
    public boolean isHandShakeCompleted() {
        return handshakeCompleted;
    }
    
    private String defaultEndpoint(final String channelId) {
        return "/endpoint/" + channelId;
    }


}
