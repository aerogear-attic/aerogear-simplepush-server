package org.jboss.aerogear.simplepush.protocol;

/**
 * Represents the Unregister message, 'unregister' message type, in the 
 * <a href="https://wiki.mozilla.org/WebAPI/SimplePush/Protocol">SimplePush specification protocol</a>
 * 
 * This message is sent from the UserAgent to the PushServer to unregister for notifications using the 
 * channelId. 
 *
 */
public interface Unregister extends MessageType {
    
    /**
     * Returns the channelId that was sent from the UserAgent.
     * 
     * @return {@code String} the channelId.
     */
    String getChannelId();

}
