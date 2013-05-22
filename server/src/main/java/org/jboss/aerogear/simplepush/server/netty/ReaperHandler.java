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
import io.netty.channel.ChannelStateHandlerAdapter;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Sharable
public class ReaperHandler extends ChannelStateHandlerAdapter {
    
    private final Config config;
    private static AtomicBoolean reaperStarted = new AtomicBoolean(false);
    
    public ReaperHandler(final Config config) {
        this.config = config;
    }
    
    @Override
    public void userEventTriggered(final ChannelHandlerContext ctx, final Object evt) throws Exception {
        if (!reaperStarted.get()) {
            if (config.hasReaperTimeout()) {
                if (evt instanceof WebSocketServerHandler) {
                    final WebSocketServerHandler wsHandler = (WebSocketServerHandler) evt;
                    ctx.executor().scheduleAtFixedRate(new UserAgentReaper(config.reaperTimeout(), wsHandler),
                        config.reaperTimeout(), 
                        config.reaperTimeout(), 
                        TimeUnit.MILLISECONDS);
                        reaperStarted.set(true);
                        ctx.pipeline().remove(this);
                }
            }
        }
    }
    
    private static class UserAgentReaper implements Runnable {
        
        private final Logger logger = LoggerFactory.getLogger(UserAgentReaper.class);
        private final long timeout;
        private final WebSocketServerHandler wsHandler;

        public UserAgentReaper(final long timeout, final WebSocketServerHandler wsHandler) {
            this.timeout = timeout;
            this.wsHandler = wsHandler;
        }
        
        @Override
        public void run() {
            logger.info("Running reaper at interval of " + timeout);
            wsHandler.cleanupUserAgents();
        }
    }

    @Override
    public void inboundBufferUpdated(ChannelHandlerContext ctx) throws Exception {
        ctx.fireInboundBufferUpdated();
    }

}
