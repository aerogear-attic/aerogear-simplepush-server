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

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.concurrent.ScheduledFuture;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jboss.aerogear.simplepush.server.SimplePushServer;
import org.jboss.aerogear.simplepush.server.SimplePushServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * UserAgentReaperHandler is responsible for starting a single scheduled job
 * that will clean up inactive user agents.
 * 
 * @see UserAgentReaper
 */
@Sharable
public class UserAgentReaperHandler extends ChannelInboundHandlerAdapter {

    private final Logger logger = LoggerFactory.getLogger(UserAgentReaperHandler.class);
    private final SimplePushServer simplePushServer;
    private static ScheduledFuture<?> scheduleFuture;
    private static final AtomicBoolean reaperStarted = new AtomicBoolean(false);

    public UserAgentReaperHandler(final SimplePushServer simplePushServer) {
        this.simplePushServer = simplePushServer;
    }

    @Override
    public void handlerAdded(final ChannelHandlerContext ctx) throws Exception {
        if (isReaperStarted()) {
            return;
        }
        final SimplePushServerConfig config = simplePushServer.config();
        logger.info("Creating UserAgentReaper job : " + config.userAgentReaperTimeout());
        scheduleFuture = ctx.executor().scheduleAtFixedRate(new UserAgentReaper(simplePushServer),
                config.userAgentReaperTimeout(),
                config.userAgentReaperTimeout(),
                TimeUnit.MILLISECONDS);
        reaperStarted.set(true);
    }

    public boolean started() {
        return reaperStarted.get();
    }

    public void cancelReaper() {
        if (scheduleFuture != null) {
            if (scheduleFuture.cancel(true)) {
                reaperStarted.set(false);
            }
        }
    }

    private boolean isReaperStarted() {
        return reaperStarted.get();
    }

}
