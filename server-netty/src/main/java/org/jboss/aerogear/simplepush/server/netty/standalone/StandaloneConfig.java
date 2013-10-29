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
