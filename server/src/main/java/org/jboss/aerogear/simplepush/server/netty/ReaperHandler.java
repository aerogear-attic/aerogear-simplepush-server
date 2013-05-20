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
    
    private final Logger logger = LoggerFactory.getLogger(ReaperHandler.class);
    private final Config config;
    private static AtomicBoolean reaperStarted = new AtomicBoolean(false);
    
    public ReaperHandler(final Config config) {
        this.config = config;
    }
    
    @Override
    public void userEventTriggered(final ChannelHandlerContext ctx, final Object evt) throws Exception {
        if (reaperStarted.get()) {
            return;
        }
        
        if (config.hasReaperTimeout()) {
            if (evt instanceof WebSocketServerHandler) {
                final WebSocketServerHandler wsHandler = (WebSocketServerHandler) evt;
                logger.info("ReadperHandler invoked" );
                ctx.executor().scheduleAtFixedRate(
                        new UserAgentReaper(config.reaperTimeout(), wsHandler),
                        config.reaperTimeout(), 
                        config.reaperTimeout(), 
                        TimeUnit.MILLISECONDS);
                reaperStarted.set(true);
                ctx.pipeline().remove(this);
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
