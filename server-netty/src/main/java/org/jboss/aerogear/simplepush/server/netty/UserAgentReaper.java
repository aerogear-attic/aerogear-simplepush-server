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

import io.netty.channel.Channel;
import io.netty.handler.codec.sockjs.SessionContext;

import java.util.Iterator;

import org.jboss.aerogear.simplepush.server.SimplePushServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserAgentReaper implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(UserAgentReaper.class);
    private final SimplePushServer simplePushServer;
    private final UserAgents userAgents = UserAgents.getInstance();

    public UserAgentReaper(final SimplePushServer simplePushServer) {
        this.simplePushServer = simplePushServer;
    }

    @Override
    public void run() {
        logger.info("Running reaper at interval of " + simplePushServer.config().userAgentReaperTimeout());
        for (Iterator<UserAgent<SessionContext>> it = userAgents.all().iterator(); it.hasNext();) {
            final UserAgent<SessionContext> userAgent = it.next();
            final long now = System.currentTimeMillis();
            if (isChannelInactive(userAgent) && userAgent.timestamp() + simplePushServer.config().userAgentReaperTimeout() < now) {
                logger.info("Removing inactive UserAgent [" + userAgent.uaid().toString() + "]");
                /* TODO: update this when persistence is in place so that the logic to remove is in one place
                         and not spread out among the UserAgents class and the SimplePushServer.
                */
                
                // remove from user agents map
                it.remove();
                
                // remove from database
                simplePushServer.removeAllChannels(userAgent.uaid());
                
                // close the user agent context
                userAgent.context().close();
            }
        }
    }
    
    private boolean isChannelInactive(final UserAgent<SessionContext> userAgent) {
        final Channel ch = userAgent.context().getContext().channel();
        return !ch.isActive() && !ch.isRegistered();
    }
}
