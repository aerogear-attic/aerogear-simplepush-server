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

/**
 * An exception to signal that a channel could not be located in the
 * DataStore in use.
 */
public class ChannelNotFoundException extends Exception {

    private static final long serialVersionUID = 2272895981494748473L;
    private final String channelId;

    /**
     * Sole constructor.
     *
     * @param message a description of when the exception occurred.
     * @param channelId the channel id that could not be located.
     */
    public ChannelNotFoundException(final String message, final String channelId) {
        super(message);
        this.channelId = channelId;
    }

    /**
     * Return the channel id that could not be located.
     *
     * @return {@code String} the channel id.
     */
    public String channelId() {
        return channelId;
    }

}
