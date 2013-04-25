package org.jboss.aerogear.simplepush.server;

import org.jboss.aerogear.simplepush.protocol.Handshake;
import org.jboss.aerogear.simplepush.protocol.HandshakeResponse;
import org.jboss.aerogear.simplepush.protocol.impl.HandshakeResponseImpl;

public class SimplePushServer {
    
    private Channels channels = new Channels();
    
    public HandshakeResponse handleHandshake(final Handshake handshake) {
        channels.addUserAgent(handshake.getUAID(), handshake.getChannelIds());
        return new HandshakeResponseImpl(handshake.getUAID());
    }

}
