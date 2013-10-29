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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.jboss.aerogear.io.netty.handler.codec.sockjs.SockJsConfig;
import org.jboss.aerogear.simplepush.server.SimplePushServerConfig;
import org.jboss.aerogear.simplepush.server.datastore.CouchDBDataStore;
import org.jboss.aerogear.simplepush.server.datastore.InMemoryDataStore;
import org.jboss.aerogear.simplepush.server.datastore.JpaDataStore;
import org.jboss.aerogear.simplepush.server.datastore.RedisDataStore;
import org.junit.BeforeClass;
import org.junit.Test;

public class ConfigReaderTest {

    private static StandaloneConfig standaloneConfig;
    private static SimplePushServerConfig simplePushServerConfig;
    private static SockJsConfig sockJsConfig;

    static {
        System.setProperty("org.jboss.logging.provider", "slf4j");
    }

    @BeforeClass
    public static void parseConfigFile() {
        standaloneConfig = ConfigReader.parse(ConfigReaderTest.class.getResourceAsStream("/simplepush-test-config.json"));
        simplePushServerConfig = standaloneConfig.simplePushServerConfig();
        sockJsConfig = standaloneConfig.sockJsConfig();
    }

    @Test
    public void host() {
        assertThat(simplePushServerConfig.host(), equalTo("localhost"));
    }

    @Test
    public void port() {
        assertThat(simplePushServerConfig.port(), is(9999));
        assertThat(simplePushServerConfig.tokenKey(), is(notNullValue()));
    }

    @Test
    public void tokenKey() {
        assertThat(simplePushServerConfig.tokenKey(), is(notNullValue()));
    }

    @Test
    public void useragentReaperTimeout() {
        assertThat(simplePushServerConfig.userAgentReaperTimeout(), is(16000L));
    }

    @Test
    public void endpointPrefix() {
        assertThat(simplePushServerConfig.endpointPrefix(), equalTo("/endpoint"));
    }

    @Test
    public void endpointTls() {
        assertThat(simplePushServerConfig.useEndpointTls(), is(true));
    }

    @Test
    public void acknowledgementInterval() {
        assertThat(simplePushServerConfig.acknowledmentInterval(), is(80000L));
    }

    @Test
    public void sockjsPrefix() {
        assertThat(sockJsConfig.prefix(), equalTo("/mysimplepush"));
    }

    @Test
    public void sockjsCookiesNeeded() {
        assertThat(sockJsConfig.areCookiesNeeded(), is(true));
    }

    @Test
    public void sockjsUrl() {
        assertThat(sockJsConfig.sockJsUrl(), equalTo("http://someurl/sockjs.js"));
    }

    @Test
    public void sockjsSessionTimeout() {
        assertThat(sockJsConfig.sessionTimeout(), is(25000L));
    }

    @Test
    public void sockjsHeartbeatInterval() {
        assertThat(sockJsConfig.heartbeatInterval(), is(40000L));
    }

    @Test
    public void sockjsMaxStreamingBytesSize() {
        assertThat(sockJsConfig.maxStreamingBytesSize(), is(65356));
    }

    @Test
    public void sockjsKeystore() {
        assertThat(sockJsConfig.keyStore(), equalTo("/simplepush-sample.keystore"));
    }

    @Test
    public void sockjsKeystorePassword() {
        assertThat(sockJsConfig.keyStorePassword(), equalTo("simplepush"));
    }

    @Test
    public void sockjsTls() {
        assertThat(sockJsConfig.isTls(), is(true));
    }

    @Test
    public void sockjsWebSocketEnable() {
        assertThat(sockJsConfig.isWebSocketEnabled(), is(false));
    }

    @Test
    public void sockjsWebSocketHeartbeatInterval() {
        assertThat(sockJsConfig.webSocketHeartbeatInterval(), is(180000L));
    }

    @Test
    public void sockjsWebSocketProtocols() {
        assertThat(sockJsConfig.webSocketProtocol(), hasItems("push-notification", "myproto"));
    }

    @Test
    public void inMemoryDataStore() {
        assertThat(standaloneConfig.dataStore(), is(instanceOf(InMemoryDataStore.class)));
    }

    @Test
    public void redisDataStore() {
        final StandaloneConfig config = ConfigReader.parse(ConfigReaderTest.class.getResourceAsStream("/simplepush-redis-config.json"));
        assertThat(config.dataStore(), is(instanceOf(RedisDataStore.class)));
    }

    @Test
    public void couchDBDataStore() {
        final StandaloneConfig config = ConfigReader.parse(ConfigReaderTest.class.getResourceAsStream("/simplepush-couchdb-config.json"));
        assertThat(config.dataStore(), is(instanceOf(CouchDBDataStore.class)));
    }

    @Test
    public void jpaDataStore() {
        final StandaloneConfig config = ConfigReader.parse(ConfigReaderTest.class.getResourceAsStream("/simplepush-jpa-config.json"));
        assertThat(config.dataStore(), is(instanceOf(JpaDataStore.class)));
    }

    @Test
    public void sampleConfig() {
        final StandaloneConfig config = ConfigReader.parse(ConfigReaderTest.class.getResourceAsStream("/simplepush-config.json"));
        assertThat(config.simplePushServerConfig().host(), equalTo("localhost"));
        assertThat(config.simplePushServerConfig().port(), is(7777));
        assertThat(config.simplePushServerConfig().tokenKey(), is(notNullValue()));
        assertThat(config.dataStore(), is(instanceOf(InMemoryDataStore.class)));
    }

}
