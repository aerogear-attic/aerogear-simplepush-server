package org.jboss.aerogear.simplepush.server;

import java.util.Set;
import java.util.UUID;

import org.jboss.aerogear.simplepush.protocol.AckMessage;
import org.jboss.aerogear.simplepush.protocol.HandshakeMessage;
import org.jboss.aerogear.simplepush.protocol.HandshakeResponse;
import org.jboss.aerogear.simplepush.protocol.NotificationMessage;
import org.jboss.aerogear.simplepush.protocol.RegisterMessage;
import org.jboss.aerogear.simplepush.protocol.RegisterResponse;
import org.jboss.aerogear.simplepush.protocol.UnregisterMessage;
import org.jboss.aerogear.simplepush.protocol.UnregisterResponse;
import org.jboss.aerogear.simplepush.protocol.Update;

/**
 * A Java implementation of a <a href="https://wiki.mozilla.org/WebAPI/SimplePush/Protocol">SimplePush</a> Server.
 *
 */
public interface SimplePushServer {
    
    /**
     * Handles the handshake ('hello') message in the SimplePush protocol.
     * 
     * @param handshakeMessage the {@link HandshakeMessage}.
     * @return {@link HandshakeResponse} the handshake response.
     */
    HandshakeResponse handleHandshake(HandshakeMessage handshakeMessage);
    
    /**
     * Handles the 'register' message in the SimplePush protocol which is used to register a channel.
     * 
     * @param registerMessage the {@link RegisterMessage}.
     * @param uaid the UserAgent identifier that this channel will be registered for.
     * @return {@link RegisterResponse} the response for this register message.
     */
    RegisterResponse handleRegister(RegisterMessage register, UUID uaid);
    
    /**
     * Handles the 'unregister' message in the SimplePush protocol which is used to register a channel.
     * 
     * @param unregisterMessage the {@link UnregisterMessage}.
     * @param uaid the UserAgent identifier that this channel will be unregistered for.
     * @return {@link UnregisterResponse} the response for this register message.
     */
    UnregisterResponse handleUnregister(UnregisterMessage unregisterMessage, UUID uaid);
    
    /**
     * Handles the 'unregister' message in the SimplePush protocol which is used to register a channel.
     * 
     * @param ackMessage the {@link UnregisterMessage}.
     * @param uaid the UserAgent identifier that this channel will be unregistered for.
     * @return {@code Set<Update>} a set of un-acknowledged channel ids.
     */
    Set<Update> handleAcknowledgement(AckMessage ackMessage, UUID uaid);
    
    /**
     * Handles the notification fo a sinlge channel
     * 
     * @param channelId the channelId to be notified.
     * @param uaid the UserAgent identifier 'owning' the channel.
     * @param payload the payload which must be in the format "version=N". 
     * @return {@link NotificationMessage} The notification message that should be sent over the network to the
     *         UserAgent. The actual communication is left to the underlying implementation.
     */
    NotificationMessage handleNotification(String channelId, UUID uaid, String payload);
    
    /**
     * Removes all the channels associated with the UserAgent.
     * 
     * @param uaid the UserAgent Identifier for which all associated channels should be removed.
     */
    void removeAllChannels(UUID uaid);
    
    /**
     * Returns the UserAgent identifier that the passed-in channel belongs to.
     * 
     * @param channelId the channelId for which the UserAgent Identifier should be returned
     * @return {@link UUID} the UserAgent identifier.
     */
    UUID fromChannel(final String channelId);

}
