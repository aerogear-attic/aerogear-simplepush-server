package org.jboss.aerogear.simplepush.protocol;

import java.util.Set;

/**
 * Represents the acknowledgement message, 'ack' message type, in the 
 * <a href="https://wiki.mozilla.org/WebAPI/SimplePush/Protocol">SimplePush specification protocol</a>.
 * 
 * A ack message is sent from the UserAgent to the SimplePush contains the channels that the UserAgent has 
 * processed and is hence acknowledging. TODO: verify this as I'm note 100% sure I'm reading the spec correctly.
 * 
 */
public interface Ack extends MessageType {
    
    String UPDATES_FIELD = "updates";
    
    /**
     * Returns the channel ids that have been acknowledged by UserAgent
     * 
     * @return {@code Set<Channel>} the channels that have been acknowledged.
     */
    Set<String> getUpdates();

}
