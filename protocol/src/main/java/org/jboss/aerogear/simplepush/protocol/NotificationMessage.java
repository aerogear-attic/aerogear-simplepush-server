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
 * Represents the Notification message, 'notification' message type, in the 
 * <a href="https://wiki.mozilla.org/WebAPI/SimplePush/Protocol">SimplePush specification protocol</a>.
 * 
 * A notification message is sent from the SimplePush Server to the UserAgent and contains the channels that
 * have had their versions updated.
 * 
 */
public interface NotificationMessage extends MessageType {

    String UPDATES_FIELD = "updates";
    String VERSION_FIELD = "version";

    /**
     * Returns the channels that have been updated for a UserAgent
     * 
     * @return {@code Set<Channel>} the channels that have been updated.
     */
    Set<Update> getUpdates();

}
