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

/**
 * Configuration settings for SimplePush server
 */
public final class DefaultSimplePushConfig implements SimplePushServerConfig {

    private final String endpointUrl;
    private final long reaperTimeout;
    private final long ackInterval;

    private DefaultSimplePushConfig(final Builder builder) {
        endpointUrl = builder.endpointUrl;
        reaperTimeout = builder.timeout;
        ackInterval = builder.ackInterval;
    }

    public String endpointUrlPrefix() {
        return endpointUrl;
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
        return new StringBuilder("SimplePushConfig[endpointUrl=").append(endpointUrl)
                .append(", reaperTimeout=").append(reaperTimeout)
                .append(", ackInterval=").append(ackInterval)
                .append("]").toString();
    }

    public static Builder create() {
        return new DefaultSimplePushConfig.Builder();
    }

    public static SimplePushServerConfig defaultConfig() {
        return new DefaultSimplePushConfig.Builder().build();
    }

    public static class Builder {
        private String endpointUrl = DEFAULT_ENDPOINT_URL_PREFIX;
        private long timeout = 604800000L;
        private long ackInterval = 60000;

        public Builder endpointUrl(final String endpointUrl) {
            if (endpointUrl != null) {
                this.endpointUrl = endpointUrl;
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

        public DefaultSimplePushConfig build() {
            return new DefaultSimplePushConfig(this);
        }
    }

}
