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
package org.jboss.aerogear.simplepush.protocol;

import java.util.Set;

/**
 * Represents the Notification message, 'notification' messageType, in the
 * <a href="https://wiki.mozilla.org/WebAPI/SimplePush/Protocol">SimplePush specification protocol</a>.
 * </p>
 * A notification message is sent from the SimplePush Server to the UserAgent, and contains the channel ids that
 * have had their versions updated.
 *
 */
public interface NotificationMessage extends MessageType {

    /**
     * The name of the updates JSON field.
     */
    String UPDATES_FIELD = "updates";

    /**
     * The name of the version JSON field.
     */
    String VERSION_FIELD = "version";

    /**
     * Returns the set channel ids that have been updated for a UserAgent
     *
     * @return {@code Set<Channel>} the channels that have been updated.
     */
    Set<Ack> getAcks();

}
