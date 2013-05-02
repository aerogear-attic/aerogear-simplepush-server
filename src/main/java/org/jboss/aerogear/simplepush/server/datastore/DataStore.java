package org.jboss.aerogear.simplepush.server.datastore;

import java.util.Set;
import java.util.UUID;

import org.jboss.aerogear.simplepush.protocol.NotificationMessage;
import org.jboss.aerogear.simplepush.protocol.Update;
import org.jboss.aerogear.simplepush.server.Channel;


/**
 * Handles the storing of channels for a SimplePush Server implementation.
 */
public interface DataStore {
    
    /**
     * Saves the channel to the underlying storage system.
     * 
     * @param channel the channel to be stored.
     * @return {@code true} if storage was successful.
     */
    boolean saveChannel(final Channel channel); 
    
    /**
     * Removes the channel with the matching channelId from the underlying storage system.
     * 
     * @param channelId of the channel to be removed
     * @return {@code true} if removal was successful.
     */
    boolean removeChannel(final String channelId);

    /**
     * Returns the Channel for the passed-in channelId.
     * 
     * @param channelId of the channel to be retrieved.
     * @return {@code Channel} the matching Channel, or null if no channel with the
     *         channelId was found.
     */
    Channel getChannel(final String channelId);
    
    /**
     * Removes all channels for a certain UserAgent Identifier (uaid).
     * 
     * @param uaid the UserAgent Identifier for which all channels that belongs to 
     *        that id should be removed.
     */
    void removeChannels(final UUID uaid);
    
    /**
     * Stores a {@code Notifiation} so that the notification can be matched against
     * acknowledged channelId from the UserAgent.
     * 
     * @param notification the {@link NotificationMessage} to store.
     * @param uaid the {@link UUID} identifiying the UserAgent.
     */
    void storeUpdates(Set<Update> updates, UUID uaid);
    
    boolean removeUpdate(Update update, UUID uaid);
    
    Set<Update> getUpdates(UUID uaid);
}
