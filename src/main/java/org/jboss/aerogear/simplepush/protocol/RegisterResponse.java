package org.jboss.aerogear.simplepush.protocol;

/**
 * Represents the Register response message, 'register' message type, in the 
 * <a href="https://wiki.mozilla.org/WebAPI/SimplePush/Protocol">SimplePush specification protocol</a>
 * 
 * This message is sent from the PushServer to the UserAgent with the result of a registration attempt
 *
 */
public interface RegisterResponse extends Register {
    
    String STATUS_FIELD = "status";
    String PUSH_ENDPOINT__FIELD = "pushEndpoint";
    
    /**
     * Returns the result of the Register call
     * 
     * @return {@code String} the channelId.
     */
    Status getStatus();
    
    /**
     * Returns the push endpoint for this channel. 
     * 
     * This is the endpoint URL that is passed back to the UserAgent upon registering a channel. The UserAgent 
     * will then update the server side application of this endpoint, which the server side application will 
     * then use when it wants to trigger a notification.
     * 
     * @return {@code String} the endpoint which can be used to trigger notifications.
     */
    String getPushEndpoint();

}
