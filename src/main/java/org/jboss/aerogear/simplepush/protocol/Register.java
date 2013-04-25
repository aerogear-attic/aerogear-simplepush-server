package org.jboss.aerogear.simplepush.protocol;

/**
 * Represents the Register message, 'register' message type, in the 
 * <a href="https://wiki.mozilla.org/WebAPI/SimplePush/Protocol">SimplePush specification protocol</a>
 * 
 * This message is sent from the UserAgent to the PushServer to register for notifications using the 
 * channelId. The channelId is create by the UserAgent.
 *
 */
public interface Register extends MessageType {
    
    String CHANNEL_ID_FIELD = "channelID";
    
    /**
     * Returns the channelId that was sent from the UserAgent.
     * 
     * @return {@code String} the channelId.
     */
    String getChannelId();

}
