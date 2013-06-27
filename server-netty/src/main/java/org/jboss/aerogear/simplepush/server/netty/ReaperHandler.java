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

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.sockjs.Session;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jboss.aerogear.simplepush.server.SimplePushServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Sharable
public class ReaperHandler extends ChannelInboundHandlerAdapter {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationHandler.class);
    private final SimplePushConfig config;
    private static AtomicBoolean reaperStarted = new AtomicBoolean(false);
    
    public ReaperHandler(final SimplePushConfig config) {
        this.config = config;
    }
    
    
    @Override
    public void userEventTriggered(final ChannelHandlerContext ctx, final Object evt) throws Exception {
        if (!reaperStarted.get()) {
            if (config.hasReaperTimeout()) {
                if (evt instanceof SimplePushServer) {
                    final SimplePushServer simplePushServer = (SimplePushServer) evt;
                    ctx.executor().scheduleAtFixedRate(new UserAgentReaper(config.reaperTimeout(), simplePushServer, config),
                        config.reaperTimeout(), 
                        config.reaperTimeout(), 
                        TimeUnit.MILLISECONDS);
                        reaperStarted.set(true);
                        ctx.pipeline().remove(this);
                } else if (evt instanceof SimplePushSockJSService) {
                    
                }
            }
        } else {
            logger.info("Reaper allready started. Do nothing");
        }
    }
    
    private static class UserAgentReaper implements Runnable {
        
        private final Logger logger = LoggerFactory.getLogger(UserAgentReaper.class);
        private final long timeout;
        private final SimplePushServer simplePushServer;
        private final UserAgents userAgents = UserAgents.getInstance();
        private final SimplePushConfig config;

        public UserAgentReaper(final long timeout, final SimplePushServer simplePushServer, final SimplePushConfig config) {
            this.timeout = timeout;
            this.simplePushServer = simplePushServer;
            this.config = config;
        }
        
        @Override
        public void run() {
            logger.info("Running reaper at interval of " + timeout);
            for (Iterator<UserAgent<Session>> it = userAgents.all().iterator(); it.hasNext();) {
                final UserAgent<Session> userAgent = it.next();
                final long now = System.currentTimeMillis();
                if (userAgent.timestamp() + config.reaperTimeout() < now) {
                    logger.info("Removing userAgent=" + userAgent.uaid().toString());
                    it.remove();
                    simplePushServer.removeAllChannels(userAgent.uaid());
                    userAgent.context().close();
                }
            }
        }
    }
    

}
