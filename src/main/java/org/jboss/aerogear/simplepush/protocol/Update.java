package org.jboss.aerogear.simplepush.protocol;

/**
 * Represents an update entry in the Notification message, 'notification' message type, in the 
 * <a href="https://wiki.mozilla.org/WebAPI/SimplePush/Protocol">SimplePush specification protocol</a>.
 *
 */
public interface Update {
    
    /**
     * Returns the channel id for this update.
     * 
     * @return {@code String} the channel id for this update.
     */
    String getChannelId();
    
    /**
     * Returns the version for this update.
     * 
     * @return {@code String} the version for this update.
     */
    String getVersion();

}
