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
package org.jboss.aerogear.simplepush.vertx;

import org.jboss.aerogear.simplepush.server.DefaultSimplePushConfig;
import org.jboss.aerogear.simplepush.server.DefaultSimplePushServer;
import org.jboss.aerogear.simplepush.server.SimplePushServer;
import org.jboss.aerogear.simplepush.server.SimplePushServerConfig;
import org.jboss.aerogear.simplepush.server.datastore.DataStore;
import org.jboss.aerogear.simplepush.server.datastore.InMemoryDataStore;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.sockjs.SockJSServer;
import org.vertx.java.platform.Verticle;

/**
 * Verticle for the SimplePush Server.
 */
public class VertxSimplePushServer extends Verticle {

    public static final String WRITE_HANDLER_MAP = "simplepush.writehandler.map";
    public static final String LAST_ACCESSED_MAP = "simplepush.lastaccessed.map";
    public static final String USER_AGENT_REMOVER = "simplepush.useragent.remover";
    public static final String DEFAULT_HOST = "localhost";
    public static final int DEFAULT_PORT = 7777;

    @Override
    public void start() {
        final SimplePushServerConfig config = fromConfig(container.config());
        final DataStore datastore = new InMemoryDataStore();
        final byte[] privateKey = DefaultSimplePushServer.generateAndStorePrivateKey(datastore, config);
        final SimplePushServer simplePushServer = new DefaultSimplePushServer(datastore, config, privateKey);
        final HttpServer httpServer = vertx.createHttpServer();
        setupHttpNotificationHandler(httpServer, simplePushServer);
        setupSimplePushSockJSServer(httpServer, simplePushServer);
        startHttpServer(httpServer);
        setupUserAgentReaperJob(simplePushServer);
    }

    private SimplePushServerConfig fromConfig(JsonObject config) {
        return DefaultSimplePushConfig.create()
                .ackInterval(config.getLong("ackInterval"))
                .endpointPrefix(config.getString("endpointUrlPrefix"))
                .password(config.getString("password", "changeme!!!"))
                .userAgentReaperTimeout(config.getLong("userAgentReaperTimeout")).build();
    }

    private void startHttpServer(final HttpServer httpServer) {
        final String host = container.config().getString("host", DEFAULT_HOST);
        final int port = container.config().getInteger("port", DEFAULT_PORT);
        httpServer.listen(port, host);
        container.logger().info("Started VertxSimplePushServer on host [" + host + "] port [" + port + "]");
    }

    private void setupHttpNotificationHandler(final HttpServer httpServer, final SimplePushServer simplePushServer) {
        final RouteMatcher rm = new RouteMatcher();
        final String endpointUrlPrefix = simplePushServer.config().endpointPrefix();
        rm.put(endpointUrlPrefix + "/:endpoint", new HttpNotificationHandler(simplePushServer, vertx, container));
        httpServer.requestHandler(rm);
    }

    private void setupSimplePushSockJSServer(final HttpServer httpServer, final SimplePushServer simplePushServer) {
        final JsonObject appConfig = new JsonObject().putString("prefix", "/simplepush");
        final SockJSServer sockJSServer = vertx.createSockJSServer(httpServer);
        sockJSServer.installApp(appConfig, new SimplePushServerHandler(simplePushServer, vertx, container));
    }

    private void setupUserAgentReaperJob(final SimplePushServer simplePushServer) {
        final Logger logger = container.logger();
        vertx.eventBus().registerHandler(USER_AGENT_REMOVER, new Handler<Message<String>>() {
            @Override
            public void handle(final Message<String> msg) {
                final String uaid = msg.body();
                simplePushServer.removeAllChannels(uaid);
                logger.info("Removed all channels for [" + uaid + "] due to inactivity");
            }
        });
        container.deployWorkerVerticle(UserAgentReaper.class.getName(), container.config());
    }

}
