package org.jboss.aerogear.simplepush.server.netty;


public final class SimplePushConfig {
    
    public static final String DEFAULT_ENDPOINT_URL = "/endpoint";
    
    private final String endpointUrl;
    private final long reaperTimeout;
    private final long ackInterval;
    
    private SimplePushConfig(final Builder builder) {
        endpointUrl = builder.endpointUrl;
        reaperTimeout = builder.timeout;
        ackInterval = builder.ackInterval;
    }
    
    public String endpointUrl() {
        return endpointUrl;
    }
    
    public long reaperTimeout() {
        return reaperTimeout;
    }
    
    public boolean hasReaperTimeout() {
        return reaperTimeout != -1;
    }
    
    public long ackInterval() {
        return ackInterval;
    }
    
    public String toString() {
        return new StringBuilder("SimplePushConfig[endpointUrl=").append(endpointUrl)
            .append(", reaperTimeout=").append(reaperTimeout)
            .append(", ackInterval=").append(ackInterval)
            .append("]").toString();
    }
    
    public static Builder create() {
        return new SimplePushConfig.Builder();
    }
    
    public static class Builder {
        private String endpointUrl = DEFAULT_ENDPOINT_URL;
        private long timeout = -1;
        private long ackInterval;
        
        public Builder endpointUrl(final String endpointUrl) {
            this.endpointUrl = endpointUrl;
            return this;
        }
        
        public Builder userAgentReaperTimeout(final long ms) {
            this.timeout = ms;
            return this;
        }
        
        public Builder ackInterval(final long ms) {
            this.ackInterval = ms;
            return this;
        }
        
        public SimplePushConfig build() {
            return new SimplePushConfig(this);
        }
    }

}
