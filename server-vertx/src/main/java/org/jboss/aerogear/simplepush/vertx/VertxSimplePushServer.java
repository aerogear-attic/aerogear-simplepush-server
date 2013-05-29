package org.jboss.aerogear.simplepush.vertx;

import static org.jboss.aerogear.simplepush.protocol.impl.json.JsonUtil.fromJson;
import static org.jboss.aerogear.simplepush.protocol.impl.json.JsonUtil.toJson;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.aerogear.simplepush.protocol.AckMessage;
import org.jboss.aerogear.simplepush.protocol.HandshakeResponse;
import org.jboss.aerogear.simplepush.protocol.MessageType;
import org.jboss.aerogear.simplepush.protocol.NotificationMessage;
import org.jboss.aerogear.simplepush.protocol.RegisterResponse;
import org.jboss.aerogear.simplepush.protocol.UnregisterMessage;
import org.jboss.aerogear.simplepush.protocol.UnregisterResponse;
import org.jboss.aerogear.simplepush.protocol.impl.AckMessageImpl;
import org.jboss.aerogear.simplepush.protocol.impl.HandshakeMessageImpl;
import org.jboss.aerogear.simplepush.protocol.impl.RegisterImpl;
import org.jboss.aerogear.simplepush.protocol.impl.UnregisterMessageImpl;
import org.jboss.aerogear.simplepush.protocol.impl.json.JsonUtil;
import org.jboss.aerogear.simplepush.server.DefaultSimplePushServer;
import org.jboss.aerogear.simplepush.server.SimplePushServer;
import org.jboss.aerogear.simplepush.server.datastore.InMemoryDataStore;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.sockjs.SockJSServer;
import org.vertx.java.core.sockjs.SockJSSocket;
import org.vertx.java.platform.Verticle;

public class VertxSimplePushServer extends Verticle {
        
    @Override
    public void start() {
        final HttpServer httpServer = vertx.createHttpServer();
        final SimplePushServer simplePushServer = new DefaultSimplePushServer(new InMemoryDataStore());
        final Map<UUID, UserAgent> userAgents = new ConcurrentHashMap<UUID, UserAgent>();
        final Logger logger = container.logger();
        final EventBus eventBus = vertx.eventBus();
        
        httpServer.requestHandler(new Handler<HttpServerRequest>() {
            @Override
            public void handle(final HttpServerRequest request) {
                request.bodyHandler(new Handler<Buffer>() {
                    @Override
                    public void handle(final Buffer buffer) {
                        logger.info("Notification Request " + request.uri());
                        final String channelId = request.uri().substring(request.uri().lastIndexOf('/') + 1);
                        logger.info("channelId = " + channelId);
                        final UUID uaid = simplePushServer.fromChannel(channelId);
                        final UserAgent userAgent = userAgents.get(uaid);
                        final String payload = buffer.toString();
                        logger.info("payload = " + payload);
                        final NotificationMessage notification = simplePushServer.handleNotification(channelId, userAgent.uaid(), payload);
                        eventBus.send(userAgent.writeHandlerId(), new Buffer(toJson(notification)));
                    }
                });
                request.response().setStatusCode(200);
                request.response().end();;
            }
        });
        
        final JsonObject appConfig = new JsonObject().putString("prefix", "/simplepush");
        final SockJSServer sockJSServer = vertx.createSockJSServer(httpServer);
        sockJSServer.installApp(appConfig, new Handler<SockJSSocket>() {
            private UUID uaid;
            
            @Override
            public void handle(final SockJSSocket sock) {
                logger.info("Sock: writeHandlerID=" + sock.writeHandlerID());
                sock.dataHandler(new Handler<Buffer>() {
                    @Override
                    public void handle(final Buffer buffer) {
                        final MessageType messageType = JsonUtil.parseFrame(buffer.toString());
                        switch (messageType.getMessageType()) {
                            case HELLO: 
                                final HandshakeResponse helloResponse = simplePushServer.handleHandshake(fromJson(buffer.toString(), HandshakeMessageImpl.class));
                                sock.write(new Buffer(toJson(helloResponse)));
                                uaid = helloResponse.getUAID();
                                userAgents.put(uaid, new UserAgent(uaid, sock.writeHandlerID(), System.currentTimeMillis()));
                                logger.info("UserAgent [" + uaid + "] handshake done");
                                break;
                            case REGISTER:
                                if (checkHandshakeCompleted(uaid)) {
                                    final RegisterResponse response = simplePushServer.handleRegister(fromJson(buffer.toString(), RegisterImpl.class), uaid);
                                    sock.write(new Buffer(toJson(response)));
                                    logger.info("UserAgent [" + uaid + "] Registered[" + response.getChannelId() + "]");
                                }
                                break;
                            case UNREGISTER:
                                if (checkHandshakeCompleted(uaid)) {
                                    final UnregisterMessage unregister = fromJson(buffer.toString(), UnregisterMessageImpl.class);
                                    final UnregisterResponse response = simplePushServer.handleUnregister(unregister, uaid);
                                    sock.write(new Buffer(toJson(response)));
                                    logger.info("UserAgent [" + uaid + "] Unregistered[" + response.getChannelId() + "]");
                                }
                                break;
                            case ACK:
                                if (checkHandshakeCompleted(uaid)) {
                                    final AckMessage ack = fromJson(buffer.toString(), AckMessageImpl.class);
                                    simplePushServer.handleAcknowledgement(ack, uaid);
                                    //processUnacked(uaid, ctx, config.ackInterval());
                                }
                                break;
                        default:
                            break;
                        }
                        updateAccessedTime(uaid);
                    }
                });
                //Pump.createPump(sock, sock).start();
            }
            
            private boolean checkHandshakeCompleted(final UUID uaid) {
                if (uaid == null) {
                    logger.debug("Hello frame has not been sent");
                    return false;
                }
                /*
                if (!userAgents.containsKey(uaid)) {
                    logger.debug("UserAgent ["+ uaid + "] was cleaned up due to unactivity for " + config.reaperTimeout() + "ms");
                    this.uaid = null;
                    return false;
                }
                */
                return true;
            }
            
            private void updateAccessedTime(final UUID uaid) {
                if (uaid != null) {
                    final UserAgent userAgent = userAgents.get(uaid);
                    userAgent.timestamp(System.currentTimeMillis());
                }
            }
        });
        
        httpServer.listen(7777);
    }

}
