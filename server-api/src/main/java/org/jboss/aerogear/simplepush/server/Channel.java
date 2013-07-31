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
package org.jboss.aerogear.simplepush.server;


/**
 * A Channel instance represents the server side information of a channel in the
 * <a href="https://wiki.mozilla.org/WebAPI/SimplePush/Protocol">SimplePush specification protocol</a>
 */
public interface Channel {

    /**
     * A unique identifier for a UserAgent created by the SimplePush Server.
     *
     * @return {@code String} a unique id for a UserAgent, or an empty String if the UserAgent has not
     * been assigned a uaid yet, or wants to reset it, which will create a new one.
     */
    String getUAID();

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
     * Updates the {@code version} for this channel.
     *
     * @param version the version to update this channel to.
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
