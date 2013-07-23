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

import io.netty.handler.codec.sockjs.SessionContext;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class UserAgents {

    private UserAgents() {
    }

    private static final ConcurrentMap<String, UserAgent<SessionContext>> userAgents = new ConcurrentHashMap<String, UserAgent<SessionContext>>();
    private static final UserAgents INSTANCE = new UserAgents();

    public static UserAgents getInstance() {
        return INSTANCE;
    }

    public void add(final String uaid, final SessionContext session) {
        userAgents.put(uaid, new UserAgent<SessionContext>(uaid, session, System.currentTimeMillis()));
    }

    public UserAgent<SessionContext> get(final String uaid) {
        final UserAgent<SessionContext> userAgent = userAgents.get(uaid);
        if (userAgent == null) {
            throw new IllegalStateException("Cound not find UserAgent [" + uaid.toString() + "]");
        }
        return userAgent;
    }

    public Collection<UserAgent<SessionContext>> all() {
        return userAgents.values();
    }

    public boolean contains(final String uaid) {
        return userAgents.containsKey(uaid);
    }

    public void updateAccessedTime(final String uaid) {
        if (uaid != null) {
            final UserAgent<SessionContext> userAgent = userAgents.get(uaid);
            userAgent.timestamp(System.currentTimeMillis());
        }
    }

}
