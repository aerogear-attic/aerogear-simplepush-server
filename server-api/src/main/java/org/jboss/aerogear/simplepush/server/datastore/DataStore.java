package org.jboss.aerogear.simplepush.server.datastore;

import java.util.Set;
import java.util.UUID;

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
    boolean saveChannel(Channel channel);

    /**
     * Removes the channel with the matching channelId from the underlying storage system.
     * 
     * @param channelId of the channel to be removed
     * @return {@code true} if removal was successful.
     */
    boolean removeChannel(String channelId);

    /**
     * Returns the Channel for the passed-in channelId.
     * 
     * @param channelId of the channel to be retrieved.
     * @return {@code Channel} the matching Channel, or null if no channel with the
     *         channelId was found.
     */
    Channel getChannel(String channelId);

    /**
     * Removes all channels for a certain UserAgent Identifier (uaid).
     * 
     * @param uaid the UserAgent Identifier for which all channels that belongs to 
     *        that id should be removed.
     */
    void removeChannels(UUID uaid);

    /**
     * Stores {@code updates/channelIds} so that the notification can be matched against
     * acknowledged channelId from the UserAgent.
     * 
     * @param updates the {@link Update}s to store.
     * @param uaid the {@link UUID} identifiying the UserAgent.
     */
    void storeUpdates(Set<Update> updates, UUID uaid);

    /**
     * Returns the {@code updates/channelIds} that have been sent to a UserAgent as notifications.
     * 
     * @param uaid the {@link UUID} of the UserAgent
     * @return {@code Set<Update>} the updates waiting for notification.
     */
    Set<Update> getUpdates(UUID uaid);

    /**
     * Removes the {@code update/channelId} from storage which should be done when a UserAgent
     * has acknowledged a notification.
     * 
     * @param update the {@link Update} to remove.
     * @param uaid the {@link UUID} of the UserAgent
     * @return
     */
    boolean removeUpdate(Update update, UUID uaid);
}
