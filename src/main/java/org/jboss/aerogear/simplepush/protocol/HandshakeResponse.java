package org.jboss.aerogear.simplepush.protocol;

import java.util.UUID;


/**
 * Represents the Handshake response message, 'hello' message type, in the 
 * <a href="https://wiki.mozilla.org/WebAPI/SimplePush/Protocol">SimplePush specification protocol</a>
 */
public interface HandshakeResponse extends MessageType {
    
    String UAID_FIELD = "uaid";
    
    /**
     * A globally unique identifier for a UserAgent created by the SimplePush Server.
     * 
     * @return {@code UUID} a globally unique id for a UserAgent, or an empty String if the UserAgent has not
     * been assigned a UAID yet or wants to reset it, which will create a new one.
     */
    UUID getUAID();

}
