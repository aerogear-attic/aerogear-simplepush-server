package org.jboss.aerogear.simplepush.vertx;

import org.jboss.aerogear.simplepush.server.DefaultSimplePushServer;
import org.jboss.aerogear.simplepush.server.SimplePushServer;
import org.jboss.aerogear.simplepush.server.datastore.InMemoryDataStore;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.sockjs.SockJSServer;
import org.vertx.java.platform.Verticle;

public class VertxSimplePushServer extends Verticle {
    
    public static final String WRITE_HANDLER_MAP = "simplepush.writehandler.map";
    public static final String LAST_ACCESSED_MAP = "simplepush.lastaccessed.map";
        
    @Override
    public void start() {
        final SimplePushServer simplePushServer = new DefaultSimplePushServer(new InMemoryDataStore());
        final HttpServer httpServer = vertx.createHttpServer();
        setupHttpNotificationHandler(httpServer, simplePushServer);
        setupSimplePushSockJSServer(httpServer, simplePushServer);
        httpServer.listen(7777);
    }
    
    private void setupHttpNotificationHandler(final HttpServer httpServer, final SimplePushServer simplePushServer) {
        httpServer.requestHandler(new HttpNotificationHandler(simplePushServer, vertx, container));
    }
    
    private void setupSimplePushSockJSServer(final HttpServer httpServer, final SimplePushServer simplePushServer) {
        final JsonObject appConfig = new JsonObject().putString("prefix", "/simplepush");
        final SockJSServer sockJSServer = vertx.createSockJSServer(httpServer);
        sockJSServer.installApp(appConfig, new SimplePushServerHandler(simplePushServer, vertx, container));
    }

}
