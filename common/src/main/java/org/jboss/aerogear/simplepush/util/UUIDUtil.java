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
package org.jboss.aerogear.simplepush.util;

import java.util.UUID;

/**
 * Utility class which method for helping with dealing with UUIDs.
 */
public final class UUIDUtil {

    private UUIDUtil() {
    }

    public static boolean nullOrEmpty(final String uuid) {
        return (uuid == null) || uuid.equals("");
    }

    /**
     * Generates a new random UUID and returns the string representation of it.
     *
     * @return {@code String} the String representation of a new random UUID.
     */
    public static String newUAID() {
        return UUID.randomUUID().toString();
    }

    /**
     * Tries to parse the passed-in uaid into a valid UUID. If the string cannot
     * be parsed into a valid UUID a new UUID will be generated.
     *
     * @param uaid the UserAgentID string representation to be parsed.
     * @return {@code String} the UserAgentID in UUID format representing the passed in uaid, or
     *         a new UUID if the uaid was not in a valid UUID format.
     */
    public static String fromString(final String uaid) {
        try {
            return UUID.fromString(uaid).toString();
        } catch (final Exception e) {
            return newUAID();
        }
    }

}
