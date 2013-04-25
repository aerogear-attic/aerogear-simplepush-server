package org.jboss.aerogear.simplepush.protocol;

import java.util.Set;
import java.util.UUID;

/**
 * Represents the Handshake message, 'hello' message type, in the 
 * <a href="https://wiki.mozilla.org/WebAPI/SimplePush/Protocol">SimplePush specification protocol</a>
 * 
 * This message is sent after the initial WebSocket handshake has been completed and is the handshake for 
 * between the UserAgent and the SimplePush Server.
 *
 */
public interface Handshake extends MessageType {
    
    String CHANNEL_IDS_FIELD = "channelIDs";
    String UAID_FIELD = "uaid";
    
    /**
     * A globally unique identifier for a UserAgent created by the SimplePush Server.
     * 
     * @return {@code UUID} a globally unique id for a UserAgent, or an empty String if the UserAgent has not
     * been assigned a UAID yet or wants to reset it, which will create a new one.
     */
    UUID getUAID();
    
    /**
     * Channel identifiers are created on the UserAgent side and are stored by the SimplePush Server
     * and associated with the UserAgent. Every channelId has a version and an endpoint associated 
     * with it.  
     * 
     * @return {@code Set<String>} a set of channelIds sent from the UserAgent, or an empty list if no channel 
     * ids were sent.
     */
    Set<String> getChannelIds();

}
