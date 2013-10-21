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

import org.jboss.aerogear.simplepush.protocol.Ack;
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
     */
    void removeChannels(Set<String> channelIds);

    /**
     * Returns registered channel ids for a certain UserAgent Identifier (uaid)
     *
     * @param uaid the UserAgent Identifier for which all channels that belongs to
     *        that id should be removed.
     * @return {@code Set<String>} the registered channels.
     */
    Set<String> getChannelIds(String uaid);

    /**
     * Updates the version for a channel (identified by the endpointToken)
     *
     * @param endpointToken the unique identifier for the channel/uaid combination.
     * @param version the version to update to.
     * @return {@code String} The channel id of the updated channel
     */
    String updateVersion(final String endpointToken, final long version) throws VersionException, ChannelNotFoundException;

    /**
     *
     * @param channelId the channelId that this update/ack belongs to.
     * @param version the {@link String} the version of the update.
     * @return {@code String} the UserAgent Id for the channel.
     * @throws ChannelNotFoundException
     */
    String saveUnacknowledged(String channelId, final long version) throws ChannelNotFoundException;

    /**
     * Returns the {@code Ack}s that have been sent to a UserAgent as notifications.
     *
     * @param uaid the {@link String} of the UserAgent
     * @return {@code Set<Update>} the updates waiting for notification.
     */
    Set<Ack> getUnacknowledged(String uaid);

    /**
     * Removes the {@code Ack} from storage which should be done when a UserAgent
     * has acknowledged a notification.
     *
     * @param ack the {@link Ack} to remove.
     * @param uaid the {@link String} of the UserAgent
     */
    Set<Ack> removeAcknowledged(String uaid, Set<Ack> acked);
}