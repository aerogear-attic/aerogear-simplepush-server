package org.jboss.aerogear.simplepush.server;

import java.util.Set;
import java.util.UUID;

import org.jboss.aerogear.simplepush.protocol.Ack;
import org.jboss.aerogear.simplepush.protocol.Handshake;
import org.jboss.aerogear.simplepush.protocol.HandshakeResponse;
import org.jboss.aerogear.simplepush.protocol.Notification;
import org.jboss.aerogear.simplepush.protocol.Register;
import org.jboss.aerogear.simplepush.protocol.RegisterResponse;
import org.jboss.aerogear.simplepush.protocol.Unregister;
import org.jboss.aerogear.simplepush.protocol.UnregisterResponse;
import org.jboss.aerogear.simplepush.protocol.Update;

public interface SimplePushServer {
    
    HandshakeResponse handleHandshake(Handshake handshake);
    
    RegisterResponse handleRegister(Register register, UUID uaid);
    
    UnregisterResponse handleUnregister(Unregister unregister, UUID uaid);
    
    Notification handleNotification(String channelId, UUID uaid, String payload);
    
    Set<Update> handleAcknowledgement(Ack ack, UUID uaid);
    
    UUID fromChannel(final String channelId);

}
