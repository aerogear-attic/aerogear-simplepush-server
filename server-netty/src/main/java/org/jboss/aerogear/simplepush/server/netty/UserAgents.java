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
package org.jboss.aerogear.simplepush.server.netty;

import io.netty.handler.codec.sockjs.SockJsSessionContext;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Represents a mapping of connected UserAgents in a SimplePush Server.
 */
public class UserAgents {

    private UserAgents() {
    }

    private static final ConcurrentMap<String, UserAgent<SockJsSessionContext>> userAgents = new ConcurrentHashMap<String, UserAgent<SockJsSessionContext>>();
    private static final UserAgents INSTANCE = new UserAgents();

    /**
     * Returns the singleton instance.
     *
     * @return {@link UserAgents} the singleton instance.
     */
    public static UserAgents getInstance() {
        return INSTANCE;
    }

    /**
     * Adds the a new UserAgent "session".
     *
     * @param uaid the unique identifier for the UserAgent.
     * @param session the {@link SessionContext} for the connected UserAgent.
     */
    public void add(final String uaid, final SockJsSessionContext session) {
        userAgents.put(uaid, new UserAgent<SockJsSessionContext>(uaid, session, System.currentTimeMillis()));
    }

    /**
     * Returns the {@link UserAgent} for the specified user agent identifier.
     *
     * @param uaid the UserAgent id.
     * @return {@link UserAgent} matching the passed in user agent identifier.
     */
    public UserAgent<SockJsSessionContext> get(final String uaid) {
        final UserAgent<SockJsSessionContext> userAgent = userAgents.get(uaid);
        if (userAgent == null) {
            throw new IllegalStateException("Cound not find UserAgent [" + uaid.toString() + "]");
        }
        return userAgent;
    }

    /**
     * Returns all the {@link UserAgent}s.
     *
     * @return {@code Collection<UserAgent>} all the {@link UserAgent}.
     */
    public Collection<UserAgent<SockJsSessionContext>> all() {
        return userAgents.values();
    }

    /**
     * Determines if a {@link UserAgent} exists for the passed-in user agent identifier.
     *
     * @param uaid the user agent identifier.
     * @return {@code true} if a {@link UserAgent} exists, or false otherwise.
     */
    public boolean contains(final String uaid) {
        return userAgents.containsKey(uaid);
    }

    /**
     * Updates the timestamp for the UserAgent matching the passed-in user agent identifier.
     * If the {@link UserAgent} does not exist nothing is performed.
     *
     * @param uaid the user agent identifier to update.
     */
    public void updateAccessedTime(final String uaid) {
        if (uaid != null) {
            final UserAgent<SockJsSessionContext> userAgent = userAgents.get(uaid);
            userAgent.timestamp(System.currentTimeMillis());
        }
    }

}
