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
package org.jboss.aerogear.simplepush.server;

import org.jboss.aerogear.simplepush.util.CryptoUtil;

/**
 * Configuration settings for SimplePush server
 */
public final class DefaultSimplePushConfig implements SimplePushServerConfig {


    private final String host;
    private final int port;
    private final boolean tls;
    private final String tokenKey;
    private final String endpointPrefix;
    private final String notificationUrl;
    private final long reaperTimeout;
    private final long ackInterval;

    private DefaultSimplePushConfig(final Builder builder) {
        host = builder.host;
        port = builder.port;
        tls = builder.tls;
        tokenKey = builder.tokenKey;
        endpointPrefix = builder.endpointPrefix;
        notificationUrl = makeNotifyURL(builder.endpointPrefix);
        reaperTimeout = builder.timeout;
        ackInterval = builder.ackInterval;
    }

    private String makeNotifyURL(final String endpointUrl) {
        return new StringBuilder(tls ? "https://" : "http://")
            .append(host).append(":").append(port).append(endpointUrl).toString();
    }

    public String host() {
        return host;
    }

    public int port() {
        return port;
    }

    public String tokenKey() {
        return tokenKey;
    }

    public boolean isUseTls() {
        return tls;
    }

    public String notificationUrl() {
        return notificationUrl;
    }

    public String endpointPrefix() {
        return endpointPrefix;
    }

    public long userAgentReaperTimeout() {
        return reaperTimeout;
    }

    public boolean hasReaperTimeout() {
        return reaperTimeout != -1;
    }

    public long acknowledmentInterval() {
        return ackInterval;
    }

    public String toString() {
        return new StringBuilder("SimplePushConfig[host=").append(host)
                .append(", port=").append(port)
                .append(", tls=").append(tls)
                .append(", endpointUrlPrefix=").append(endpointPrefix)
                .append(", notificationUrl=").append(notificationUrl)
                .append(", reaperTimeout=").append(reaperTimeout)
                .append(", ackInterval=").append(ackInterval)
                .append("]").toString();
    }

    public static Builder create() {
        return new DefaultSimplePushConfig.Builder().host("localhost").port(7777);
    }

    public static Builder create(final String host, final int port) {
        return new DefaultSimplePushConfig.Builder().host(host).port(port);
    }

    public static SimplePushServerConfig defaultConfig() {
        return new DefaultSimplePushConfig.Builder().host("localhost")
                .port(7777)
                .tokenKey(new String(CryptoUtil.randomKey(16)))
                .build();
    }

    public static class Builder {
        private String host;
        private int port;
        private String tokenKey;
        private boolean tls;
        private String endpointPrefix = DEFAULT_ENDPOINT_URL_PREFIX;
        private long timeout = 604800000L;
        private long ackInterval = 60000;

        public Builder host(final String host) {
            if (host != null) {
                this.host = host;
            }
            return this;
        }

        public Builder port(final int port) {
            this.port = port;
            return this;
        }

        public Builder tokenKey(final String tokenKey) {
            this.tokenKey = tokenKey;
            return this;
        }

        public Builder useTls(final boolean tls) {
            this.tls = tls;
            return this;
        }

        public Builder endpointUrlPrefix(final String endpointPrefix) {
            if (endpointPrefix != null) {
                this.endpointPrefix = endpointPrefix;
            }
            return this;
        }

        public Builder userAgentReaperTimeout(final Long ms) {
            if (ms != null) {
                this.timeout = ms;
            }
            return this;
        }

        public Builder ackInterval(final Long ms) {
            if (ms != null) {
                this.ackInterval = ms;
            }
            return this;
        }

        public SimplePushServerConfig build() {
            if (tokenKey == null) {
                throw new IllegalStateException("No 'tokenKey' must be configured!");
            }
            return new DefaultSimplePushConfig(this);
        }
    }

}
