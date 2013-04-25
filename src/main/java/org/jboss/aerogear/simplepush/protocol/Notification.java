package org.jboss.aerogear.simplepush.protocol;

import java.util.Set;

/**
 * Represents the Notification message, 'notification' message type, in the 
 * <a href="https://wiki.mozilla.org/WebAPI/SimplePush/Protocol">SimplePush specification protocol</a>.
 * 
 * A notification message is sent from the SimplePush Server to the UserAgent and contains the channels that
 * have had their versions updated.
 * 
 */
public interface Notification extends MessageType {
    
    String UPDATES_FIELD = "updates";
    String VERSION_FIELD = "version";
    
    /**
     * Returns the channels that have been updated for a UserAgent
     * 
     * @return {@code Set<Channel>} the channels that have been updated.
     */
    Set<Update> getUpdates();

}
