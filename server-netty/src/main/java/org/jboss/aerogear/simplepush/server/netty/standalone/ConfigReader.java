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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jboss.aerogear.io.netty.handler.codec.sockjs.SockJsConfig;
import org.jboss.aerogear.simplepush.server.DefaultSimplePushConfig;
import org.jboss.aerogear.simplepush.server.DefaultSimplePushConfig.Builder;
import org.jboss.aerogear.simplepush.server.SimplePushServerConfig;
import org.jboss.aerogear.simplepush.server.datastore.CouchDBDataStore;
import org.jboss.aerogear.simplepush.server.datastore.DataStore;
import org.jboss.aerogear.simplepush.server.datastore.InMemoryDataStore;
import org.jboss.aerogear.simplepush.server.datastore.JpaDataStore;
import org.jboss.aerogear.simplepush.server.datastore.RedisDataStore;

/**
 * Utility to read a JSON config files.
 */
public class ConfigReader {

    private static final ObjectMapper OM = new ObjectMapper();

    private ConfigReader() {
    }

    /**
     * Will parse the passed in file, which can either be a file on the file system
     * or a file on the classpath into a {@link StandaloneConfig} instance.
     *
     * @param fileName the name of a file on the file system or on the classpath.
     * @return {@link StandaloneConfig} populated with the values in the JSON configuration file.
     * @throws Exception
     */
    public static StandaloneConfig parse(final String fileName) throws Exception {
        final File configFile = new File(fileName);
        InputStream in = null;
        try {
            in = configFile.exists() ? new FileInputStream(configFile) : ConfigReader.class.getResourceAsStream(fileName);
            return parse(in);
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    /**
     * Will parse the passed InputStream into a {@link StandaloneConfig} instance.
     *
     * @param in the input stream to parse. Should be from a JSON source representing a SimplePush configuration.
     * @return {@link StandaloneConfig} populated with the values in the JSON input stream.
     */
    public static StandaloneConfig parse(final InputStream in) {
        try {
            final JsonNode json = OM.readTree(in);
            return new StandaloneConfig(parseSimplePushProperties(json),
                   parseSockJsProperties(json),
                   createDataStore(json));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static DataStore createDataStore(final JsonNode json) {
        final JsonNode dataStore = json.get("datastore");
        if (dataStore == null) {
            throw new IllegalArgumentException("datastore element must be specified");
        }
        final JsonNode inMemory = dataStore.get("in-memory");
        if (inMemory != null) {
            return new InMemoryDataStore();
        }
        final JsonNode redis = dataStore.get("redis");
        if (redis != null) {
            return new RedisDataStore(redis.get("host").asText(), redis.get("port").asInt());
        }
        final JsonNode couchdb = dataStore.get("couchdb");
        if (couchdb != null) {
            return new CouchDBDataStore(couchdb.get("url").asText(), couchdb.get("dbName").asText());
        }
        final JsonNode jpa = dataStore.get("jpa");
        if (jpa != null) {
            return new JpaDataStore(jpa.get("persistenceUnit").asText());
        }
        throw new IllegalStateException("datastore must be specified");
    }

    private static SockJsConfig parseSockJsProperties(final JsonNode json) {
        final JsonNode prefixNode = json.get("sockjs-prefix");
        final String prefix = prefixNode != null ? prefixNode.asText() : "/simplepush";
        final org.jboss.aerogear.io.netty.handler.codec.sockjs.SockJsConfig.Builder builder = SockJsConfig.withPrefix(prefix);
        final JsonNode cookiesNeeded = json.get("sockjs-cookies-needed");
        if (cookiesNeeded != null && cookiesNeeded.asBoolean()) {
            builder.cookiesNeeded();
        }
        final JsonNode sockjsUrl = json.get("sockjs-url");
        if (sockjsUrl != null) {
            builder.sockJsUrl(sockjsUrl.asText());
        }
        final JsonNode sessionTimeout = json.get("sockjs-session-timeout");
        if (sessionTimeout != null) {
            builder.sessionTimeout(sessionTimeout.asLong());
        }
        final JsonNode heartbeatInterval = json.get("sockjs-heartbeat-interval");
        if (heartbeatInterval != null) {
            builder.heartbeatInterval(heartbeatInterval.asLong());
        }
        final JsonNode maxStreamingBytesSize = json.get("sockjs-max-streaming-bytes-size");
        if (maxStreamingBytesSize != null) {
            builder.maxStreamingBytesSize(maxStreamingBytesSize.asInt());
        }
        final JsonNode keystore = json.get("sockjs-keystore");
        if (keystore != null) {
            builder.keyStore(keystore.asText());
        }
        final JsonNode keystorePassword = json.get("sockjs-keystore-password");
        if (keystorePassword != null) {
            builder.keyStorePassword(keystorePassword.asText());
        }
        final JsonNode tls = json.get("sockjs-tls");
        if (tls != null) {
            builder.tls(tls.asBoolean());
        }
        final JsonNode websocketEnable = json.get("sockjs-websocket-enable");
        if (websocketEnable != null && !websocketEnable.asBoolean()) {
            builder.disableWebSocket();
        }
        final JsonNode websocketHeartbeatInterval = json.get("sockjs-websocket-heartbeat-interval");
        if (websocketHeartbeatInterval != null) {
            builder.webSocketHeartbeatInterval(websocketHeartbeatInterval.asLong());
        }
        final JsonNode websocketProtocols = json.get("sockjs-websocket-protocols");
        if (websocketProtocols != null) {
            builder.webSocketProtocols(websocketProtocols.asText().split(","));
        }
        return builder.build();
    }

    private static SimplePushServerConfig parseSimplePushProperties(final JsonNode json) {
        final JsonNode host = json.get("host");
        final JsonNode port = json.get("port");
        final Builder builder = DefaultSimplePushConfig.create(host.asText(), port.asInt());
        final JsonNode password = json.get("password");
        if (password != null) {
            builder.password(password.asText());
        }
        final JsonNode useragentReaperTimeout = json.get("useragent-reaper-timeout");
        if (useragentReaperTimeout != null) {
            builder.userAgentReaperTimeout(useragentReaperTimeout.asLong());
        }
        final JsonNode endpointHost = json.get("endpoint-host");
        if (endpointHost != null) {
            builder.endpointHost(endpointHost.asText());
        }
        final JsonNode endpointPort = json.get("endpoint-port");
        if (endpointPort != null) {
            builder.endpointPort(endpointPort.asInt());
        }
        final JsonNode endpointTls = json.get("endpoint-tls");
        if (endpointTls != null) {
            builder.endpointTls(endpointTls.asBoolean());
        }
        final JsonNode endpointPrefix = json.get("endpoint-prefix");
        if (endpointPrefix != null) {
            builder.endpointPrefix(endpointPrefix.asText());
        }
        final JsonNode ackInterval = json.get("ack-interval");
        if (ackInterval != null) {
            builder.ackInterval(ackInterval.asLong());
        }
        final JsonNode notifierMaxThreads = json.get("notifier-max-threads");
        if (notifierMaxThreads != null) {
            builder.notifierMaxThreads(notifierMaxThreads.asInt());
        }
        return builder.build();
    }

}
