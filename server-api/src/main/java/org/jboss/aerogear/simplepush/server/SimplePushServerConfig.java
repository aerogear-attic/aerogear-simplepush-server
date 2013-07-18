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
public interface SimplePushServerConfig {

    String DEFAULT_ENDPOINT_URL_PREFIX = "/endpoint";

    /**
     * Returns the endpoint url prefix for this SimplePush server.
     * This have the channelId appended to it.
     * 
     * @return {@code String} the endpoint url prefix.
     */
    String endpointUrlPrefix();

    /**
     * Returns the UserAgent reaper time out.
     * This is the amount of time which a UserAgent can be inactive after
     * which it will be removed from the system.
     * 
     * @return {@code long} the timeout in milliseconds.
     */
    long userAgentReaperTimeout();

    /**
     * Returns the acknowledgement interval.
     * This is the interval time for resending non acknowledged notifications.
     * 
     * @return {@code long} the interval in milliseconds.
     */
    long acknowledmentInterval();

}
