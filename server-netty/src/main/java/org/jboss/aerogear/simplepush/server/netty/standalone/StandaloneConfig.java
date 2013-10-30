/**
 * JBoss, Home of Professional Open Source Copyright Red Hat, Inc., and individual contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package org.jboss.aerogear.simplepush.server.netty.standalone;

import org.jboss.aerogear.io.netty.handler.codec.sockjs.SockJsConfig;
import org.jboss.aerogear.simplepush.server.SimplePushServerConfig;
import org.jboss.aerogear.simplepush.server.datastore.DataStore;

/**
 * Represents the configuration required for running the standalone Netty
 * version of AeroGear's SimplePush server.
 *
 */
public class StandaloneConfig {

    private final SimplePushServerConfig pushConfig;
    private final SockJsConfig sockJsConfig;
    private final DataStore dataStore;

    public StandaloneConfig(final SimplePushServerConfig pushConfig, final SockJsConfig sockJsConfig,
            final DataStore dataStore) {
        this.pushConfig = pushConfig;
        this.sockJsConfig = sockJsConfig;
        this.dataStore = dataStore;
    }

    public SimplePushServerConfig simplePushServerConfig() {
        return pushConfig;
    }

    public SockJsConfig sockJsConfig() {
        return sockJsConfig;
    }

    public DataStore dataStore() {
        return dataStore;
    }

    @Override
    public String toString() {
        return "StandaloneConfig[simplePushConfig=" + pushConfig + ", sockJsConfig=" + sockJsConfig + ", dataStore=" + dataStore + "]";
    }

}
