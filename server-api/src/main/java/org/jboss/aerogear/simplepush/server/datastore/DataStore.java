/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.simplepush.server.datastore;

import java.util.Set;

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
    Channel getChannel(String channelId) throws ChannelNotFoundException;

    /**
     * Removes all channels for a certain UserAgent Identifier (uaid).
     *
     * @param uaid the UserAgent Identifier for which all channels that belongs to
     *        that id should be removed.
     */
    void removeChannels(String uaid);

    /**
     * Removes all channels matching the set passed in.
     *
     * @param channelIds the ids of the channels to be removed.
     * @param {@code Integer} the number of entries removed.
     */
    Integer removeChannels(Set<String> channelIds);

    /**
     * Returns registered channel ids for a certain UserAgent Identifier (uaid)
     *
     * @param uaid the UserAgent Identifier for which all channels that belongs to
     *        that id should be removed.
     * @return {@code Set<String>} the registered channels.
     */
    Set<String> getChannelIds(String uaid);

    /**
     * Stores {@code updates/channelIds} so that the notification can be matched against
     * acknowledged channelId from the UserAgent.
     *
     * @param updates the {@link Update}s to store.
     * @param uaid the {@link String} identifiying the UserAgent.
     */
    void saveUpdates(Set<Update> updates, String uaid);

    /**
     * Returns the {@code updates/channelIds} that have been sent to a UserAgent as notifications.
     *
     * @param uaid the {@link String} of the UserAgent
     * @return {@code Set<Update>} the updates waiting for notification.
     */
    Set<Update> getUpdates(String uaid);

    /**
     * Removes the {@code update/channelId} from storage which should be done when a UserAgent
     * has acknowledged a notification.
     *
     * @param update the {@link Update} to remove.
     * @param uaid the {@link String} of the UserAgent
     * @return {@code true} if the {@code update} is removed, false otherwise
     */
    boolean removeUpdate(Update update, String uaid);
}
