package org.jboss.aerogear.simplepush.server;


/**
 * A Channel instance represents the server side information of a channel in the 
 * <a href="https://wiki.mozilla.org/WebAPI/SimplePush/Protocol">SimplePush specification protocol</a>
 * 
 */
public interface Channel {
    
    /**
     * Returns the channelId for this channel. This identifier will be create on by the UserAgent
     * and sent to the SimplePush Server.
     * 
     * @return {@code String} this channels identifier.
     */
    String getChannelId();
    
    /**
     * Returns the version for this channel. The version is maintained and updated by the 
     * server side applications triggering push notifications. 
     * 
     * It is the server side application that will issue a PUT HTTP request with the update version to the 
     * SimplePush Server which in turn will notifiy the channel of the update. 
     * 
     * @return {@code long} the version for this channel.
     */
    long getVersion();
    
    /**
     * Increments the {@code version} for this channel.
     */
    void setVersion(final long version);
    
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
