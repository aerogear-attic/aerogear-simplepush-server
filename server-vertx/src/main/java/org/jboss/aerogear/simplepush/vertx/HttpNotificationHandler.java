package org.jboss.aerogear.simplepush.vertx;

import static org.jboss.aerogear.simplepush.protocol.impl.json.JsonUtil.toJson;

import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

import org.jboss.aerogear.simplepush.protocol.NotificationMessage;
import org.jboss.aerogear.simplepush.server.SimplePushServer;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.platform.Container;

public class HttpNotificationHandler implements Handler<HttpServerRequest> {
    
    private final SimplePushServer simplePushServer;
    private final Vertx vertx;
    private final Logger logger;
    private final ConcurrentMap<String, String> writeHandlerMap;

    public HttpNotificationHandler(final SimplePushServer simplePushServer, final Vertx vertx, final Container container) {
        this.simplePushServer = simplePushServer;
        this.vertx = vertx;
        writeHandlerMap = vertx.sharedData().getMap(VertxSimplePushServer.WRITE_HANDLER_MAP);
        logger = container.logger();
    }

    @Override
    public void handle(final HttpServerRequest request) {
        request.bodyHandler(new Handler<Buffer>() {
            @Override
            public void handle(final Buffer buffer) {
                logger.info("Notification Request " + request.uri());
                final String channelId = request.uri().substring(request.uri().lastIndexOf('/') + 1);
                logger.info("channelId = " + channelId);
                final UUID uaid = simplePushServer.fromChannel(channelId);
                final String payload = buffer.toString();
                logger.info("payload = " + payload);
                final NotificationMessage notification = simplePushServer.handleNotification(channelId, uaid, payload);
                vertx.eventBus().send(writeHandlerMap.get(uaid.toString()), new Buffer(toJson(notification)));
            }
        });
        request.response().setStatusCode(200);
        request.response().end();;
    }

}
