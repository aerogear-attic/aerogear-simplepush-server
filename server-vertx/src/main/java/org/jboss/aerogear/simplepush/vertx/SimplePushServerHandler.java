package org.jboss.aerogear.simplepush.vertx;

import static org.jboss.aerogear.simplepush.protocol.impl.json.JsonUtil.fromJson;
import static org.jboss.aerogear.simplepush.protocol.impl.json.JsonUtil.toJson;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

import org.jboss.aerogear.simplepush.protocol.AckMessage;
import org.jboss.aerogear.simplepush.protocol.HandshakeMessage;
import org.jboss.aerogear.simplepush.protocol.HandshakeResponse;
import org.jboss.aerogear.simplepush.protocol.MessageType;
import org.jboss.aerogear.simplepush.protocol.RegisterResponse;
import org.jboss.aerogear.simplepush.protocol.UnregisterMessage;
import org.jboss.aerogear.simplepush.protocol.UnregisterResponse;
import org.jboss.aerogear.simplepush.protocol.Update;
import org.jboss.aerogear.simplepush.protocol.impl.AckMessageImpl;
import org.jboss.aerogear.simplepush.protocol.impl.HandshakeMessageImpl;
import org.jboss.aerogear.simplepush.protocol.impl.NotificationMessageImpl;
import org.jboss.aerogear.simplepush.protocol.impl.RegisterMessageImpl;
import org.jboss.aerogear.simplepush.protocol.impl.UnregisterMessageImpl;
import org.jboss.aerogear.simplepush.protocol.impl.json.JsonUtil;
import org.jboss.aerogear.simplepush.server.SimplePushServer;
import org.jboss.aerogear.simplepush.util.UUIDUtil;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.sockjs.SockJSSocket;
import org.vertx.java.platform.Container;

public class SimplePushServerHandler implements Handler<SockJSSocket>{
    
    private final SimplePushServer simplePushServer;
    private final Logger logger;
    private final ConcurrentMap<String, String> writeHandlerMap;
    private final ConcurrentMap<String, Long> lastAccessedMap;
    private final Vertx vertx;
    private final Container container;
    private UUID uaid;

    public SimplePushServerHandler(final SimplePushServer simplePushServer, final Vertx vertx, final Container container) {
        this.simplePushServer = simplePushServer;
        this.container = container;
        logger = container.logger();
        this.vertx = vertx;
        writeHandlerMap = vertx.sharedData().getMap(VertxSimplePushServer.WRITE_HANDLER_MAP);
        lastAccessedMap = vertx.sharedData().getMap(VertxSimplePushServer.LAST_ACCESSED_MAP);
    }

    @Override
    public void handle(final SockJSSocket sock) {
        sock.dataHandler(new Handler<Buffer>() {
            @Override
            public void handle(final Buffer buffer) {
                final MessageType messageType = JsonUtil.parseFrame(buffer.toString());
                switch (messageType.getMessageType()) {
                    case HELLO: 
                        HandshakeMessage handshakeMessage = fromJson(buffer.toString(), HandshakeMessageImpl.class);
                        if (!writeHandlerMap.containsKey(handshakeMessage.getUAID().toString())) {
                            handshakeMessage = new HandshakeMessageImpl(UUIDUtil.newUAID().toString());
                        } 
                        final HandshakeResponse helloResponse = simplePushServer.handleHandshake(handshakeMessage);
                        sock.write(new Buffer(toJson(helloResponse)));
                        uaid = helloResponse.getUAID();
                        writeHandlerMap.put(uaid.toString(), sock.writeHandlerID());
                        lastAccessedMap.put(uaid.toString(), System.currentTimeMillis());
                        logger.info("UserAgent [" + uaid + "] handshake done");
                        break;
                    case REGISTER:
                        if (checkHandshakeCompleted(uaid)) {
                            final RegisterResponse response = simplePushServer.handleRegister(fromJson(buffer.toString(), RegisterMessageImpl.class), uaid);
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
                            processUnacked(uaid);
                        }
                        break;
                default:
                    break;
                }
                updateAccessedTime(uaid);
            }
        });
    }
    
    private boolean checkHandshakeCompleted(final UUID uaid) {
        if (uaid == null) {
            logger.debug("Hello frame has not been sent");
            return false;
        }
        return true;
    }
    
    private void updateAccessedTime(final UUID uaid) {
        if (uaid != null) {
            lastAccessedMap.put(uaid.toString(), System.currentTimeMillis());
        }
    }
    
    private void processUnacked(final UUID uaid) {
        final Set<Update> unacked = simplePushServer.getUnacknowledged(uaid);
        if (!unacked.isEmpty()) {
            final Long interval = container.config().getLong("ackInterval", 60000);
            vertx.setPeriodic(interval, new Handler<Long>() {
                public void handle(final Long timerID) {
                    final Set<Update> unacked = simplePushServer.getUnacknowledged(uaid);
                    if (unacked.isEmpty()) {
                        logger.info("Nothing to ack. Stopping periodic task");
                        vertx.cancelTimer(timerID);
                    } else {
                        logger.info("Resending " + unacked);
                        final Buffer buf = new Buffer(toJson(new NotificationMessageImpl(unacked)));
                        vertx.eventBus().send(writeHandlerMap.get(uaid.toString()), buf);
                    }
                }
            });
        } 
    }

}
