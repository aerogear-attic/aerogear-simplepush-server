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

import java.util.Set;

import org.jboss.aerogear.simplepush.protocol.AckMessage;
import org.jboss.aerogear.simplepush.protocol.HandshakeMessage;
import org.jboss.aerogear.simplepush.protocol.HandshakeResponse;
import org.jboss.aerogear.simplepush.protocol.NotificationMessage;
import org.jboss.aerogear.simplepush.protocol.RegisterMessage;
import org.jboss.aerogear.simplepush.protocol.RegisterResponse;
import org.jboss.aerogear.simplepush.protocol.UnregisterMessage;
import org.jboss.aerogear.simplepush.protocol.UnregisterResponse;
import org.jboss.aerogear.simplepush.protocol.Update;
import org.jboss.aerogear.simplepush.server.datastore.ChannelNotFoundException;

/**
 * A Java implementation of a <a href="https://wiki.mozilla.org/WebAPI/SimplePush/Protocol">SimplePush</a> Server.
 *
 */
public interface SimplePushServer {

    /**
     * Handles the handshake ('hello') message in the SimplePush protocol.
     *
     * @param handshakeMessage the {@link HandshakeMessage}.
     * @return {@link HandshakeResponse} the handshake response.
     */
    HandshakeResponse handleHandshake(HandshakeMessage handshakeMessage);

    /**
     * Handles the 'register' message in the SimplePush protocol which is used to register a channel.
     *
     * @param registerMessage the {@link RegisterMessage}.
     * @param uaid the UserAgent identifier that this channel will be registered for.
     * @return {@link RegisterResponse} the response for this register message.
     */
    RegisterResponse handleRegister(RegisterMessage register, String uaid);

    /**
     * Handles the 'unregister' message in the SimplePush protocol which is used to register a channel.
     *
     * @param unregisterMessage the {@link UnregisterMessage}.
     * @param uaid the UserAgent identifier that this channel will be unregistered for.
     * @return {@link UnregisterResponse} the response for this register message.
     */
    UnregisterResponse handleUnregister(UnregisterMessage unregisterMessage, String uaid);

    /**
     * Handles the 'ack' message in the SimplePush protocol which is acknowledge a notification.
     *
     * @param ackMessage the {@link UnregisterMessage}.
     * @param uaid the UserAgent identifier that this channel will be unregistered for.
     * @return {@code Set<Update>} a set of un-acknowledged channel ids.
     */
    Set<Update> handleAcknowledgement(AckMessage ackMessage, String uaid);

    /**
     * Returns all the un-acknowledged notifications for a specific UserAgent.
     *
     * @param uaid the UserAgent identifier for which unacked notifications should be retrieved.
     * @return {@code Set<Update>} a set of un-acknowledged channel ids.
     */
    Set<Update> getUnacknowledged(String uaid);

    /**
     * Handles the notification for a single channel
     *
     * @param channelId the channelId to be notified.
     * @param uaid the UserAgent identifier 'owning' the channel.
     * @param payload the payload which must be in the format "version=N".
     * @return {@link NotificationMessage} The notification message that should be sent over the network to the
     *         UserAgent. The actual communication is left to the underlying implementation.
     */
    NotificationMessage handleNotification(String channelId, String uaid, String payload) throws ChannelNotFoundException;

    /**
     * Removes all the channels associated with the UserAgent.
     *
     * @param uaid the UserAgent Identifier for which all associated channels should be removed.
     */
    void removeAllChannels(String uaid);

    /**
     * Returns the UserAgent identifier that the passed-in channel belongs to.
     *
     * @param channelId the channelId for which the UserAgent Identifier should be returned
     * @return {@link String} the UserAgent identifier.
     * @throws ChannelNotFoundException if the channel could not be found.
     */
    String fromChannel(final String channelId) throws ChannelNotFoundException;

    /**
     * Returns the configuration for this SimplePush server.
     *
     * @return {@link SimplePushServerConfig} this servers configuration.
     */
    SimplePushServerConfig config();

}
