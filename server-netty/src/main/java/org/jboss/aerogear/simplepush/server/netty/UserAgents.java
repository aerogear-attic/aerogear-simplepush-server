/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class UserAgents {
    
    private UserAgents() {
    }
    
    private static final ConcurrentMap<UUID, UserAgent<SessionContext>> userAgents = new ConcurrentHashMap<UUID, UserAgent<SessionContext>>();
    private static final UserAgents INSTANCE = new UserAgents();
    
    public static UserAgents getInstance() {
        return INSTANCE;
    }
    
    public void add(final UUID uaid, final SessionContext session) {
        userAgents.put(uaid, new UserAgent<SessionContext>(uaid, session, System.currentTimeMillis()));
    }
    
    public UserAgent<SessionContext> get(final UUID uaid) {
        final UserAgent<SessionContext> userAgent = userAgents.get(uaid);
        if (userAgent == null) {
            throw new IllegalStateException("Cound not find UserAgent [" + uaid.toString() + "]");
        }
        return userAgent;
    }
    
    public Collection<UserAgent<SessionContext>> all() {
        return userAgents.values();
    }
    
    public boolean contains(final UUID uaid) {
        return userAgents.containsKey(uaid);
    }

}
