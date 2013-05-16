package org.jboss.aerogear.simplepush.server.netty;

import org.jboss.aerogear.simplepush.util.ArgumentUtil;

public final class Config {
    
    private final String path;
    private final boolean tls;
    private final String subprotocol;
    private final String endpointUrl;
    private final long reaperTimeout;
    
    private Config(final Builder builder) {
        path = builder.path;
        tls = builder.tls;
        subprotocol = builder.subprotocol;
        endpointUrl = builder.endpointUrl;
        reaperTimeout = builder.timeout;
    }
    
    public String path() {
        return path;
    }
    
    public boolean tls() {
        return tls;
    }
    
    public String subprotocol() {
        return subprotocol;
    }
    
    public String endpointUrl() {
        return endpointUrl;
    }
    
    public long reaperTimeout() {
        return reaperTimeout;
    }
    
    public static Builder path(final String path) {
        ArgumentUtil.checkNotNullAndNotEmpty(path, "path");
        return new Config.Builder(path);
    }
    
    public static class Builder {
        private final String path;
        private boolean tls;
        private String subprotocol;
        private String endpointUrl;
        private long timeout = -1;
        
        public Builder(final String path) {
            this.path = path;
        }
        
        public Builder tls(final boolean tls) {
            this.tls = tls;
            return this;
        }
        
        public Builder subprotocol(final String protocol) {
            this.subprotocol = protocol;
            return this;
        }
        
        public Builder endpointUrl(final String endpointUrl) {
            this.endpointUrl = endpointUrl;
            return this;
        }
        
        public Builder userAgentReaperTimeout(final long ms) {
            this.timeout = ms;
            return this;
        }
        
        public Config build() {
            ArgumentUtil.checkNotNullAndNotEmpty(subprotocol, "subprotocol");
            ArgumentUtil.checkNotNullAndNotEmpty(endpointUrl, "endpointUrl");
            return new Config(this);
        }
    }

}
