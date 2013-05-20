package org.jboss.aerogear.simplepush.server.netty;

import io.netty.channel.ChannelHandlerContext;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class UserAgent {
    
    private final UUID uaid;
    private final ChannelHandlerContext ctx;
    private AtomicLong timestamp;

    public UserAgent(final UUID uaid, final ChannelHandlerContext ctx, final long timestamp) {
        this.uaid = uaid;
        this.ctx = ctx;
        this.timestamp = new AtomicLong(timestamp);
    }
    
    public UUID uaid() {
        return uaid;
    }
    
    public ChannelHandlerContext context() {
        return ctx;
    }
    
    public long timestamp() {
        return timestamp.get();
    }
    
    public void timestamp(final long timestamp) {
        this.timestamp.set(timestamp);
    }
    
    @Override
    public String toString() {
        return "UserAgent[uaid=" + uaid + ", ctx=" + ctx + ", timestamp=" + timestamp() + "]";
    }

}
