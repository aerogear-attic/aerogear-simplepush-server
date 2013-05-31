package org.jboss.aerogear.simplepush.vertx;

import java.util.UUID;

import org.jboss.aerogear.simplepush.server.DefaultSimplePushServer;
import org.jboss.aerogear.simplepush.server.SimplePushServer;
import org.jboss.aerogear.simplepush.server.datastore.InMemoryDataStore;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.sockjs.SockJSServer;
import org.vertx.java.platform.Verticle;

public class VertxSimplePushServer extends Verticle {
    
    public static final String WRITE_HANDLER_MAP = "simplepush.writehandler.map";
    public static final String LAST_ACCESSED_MAP = "simplepush.lastaccessed.map";
    public static final String USER_AGENT_REMOVER = "simplepush.useragent.remover";
    public static final String DEFAULT_HOST = "localhost";
    public static final int DEFAULT_PORT = 7777;
        
    @Override
    public void start() {
        final SimplePushServer simplePushServer = new DefaultSimplePushServer(new InMemoryDataStore());
        final HttpServer httpServer = vertx.createHttpServer();
        setupHttpNotificationHandler(httpServer, simplePushServer);
        setupSimplePushSockJSServer(httpServer, simplePushServer);
        startHttpServer(httpServer);
        setupUserAgentReaperJob(simplePushServer);
    }

    private void startHttpServer(final HttpServer httpServer) {
        final String host = container.config().getString("host", DEFAULT_HOST);
        final int port = container.config().getInteger("port", DEFAULT_PORT);
        httpServer.listen(port, host);
        container.logger().info("Started VertxSimplePushServer on host [" + host + "] port [" + port + "]");
    }

    private void setupHttpNotificationHandler(final HttpServer httpServer, final SimplePushServer simplePushServer) {
        httpServer.requestHandler(new HttpNotificationHandler(simplePushServer, vertx, container));
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
                simplePushServer.removeAllChannels(UUID.fromString(uaid));
                logger.info("Removed all channels for [" + uaid + "] due to inactivity");
            }
        });
        container.deployWorkerVerticle(UserAgentReaper.class.getName(), container.config());
    }

}
