package org.jboss.aerogear.simplepush.server.netty;

import static org.jboss.aerogear.simplepush.protocol.impl.json.JsonUtil.fromJson;
import static org.jboss.aerogear.simplepush.protocol.impl.json.JsonUtil.toJson;
import io.netty.handler.codec.sockjs.Config;
import io.netty.handler.codec.sockjs.Session;
import io.netty.handler.codec.sockjs.SockJSService;
import io.netty.util.concurrent.ScheduledFuture;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.jboss.aerogear.simplepush.protocol.AckMessage;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SimplePush server implementation using SockJS.
 */
public class SimplePushSockJSService implements SockJSService {
    
    private final Logger logger = LoggerFactory.getLogger(SimplePushSockJSService.class);

    private final UserAgents userAgents = UserAgents.getInstance();
    private final Config sockjsConfig;
    private final SimplePushServer simplePushServer;
    private final SimplePushConfig config;
    private UUID uaid;
    private Session session;
    private ScheduledFuture<?> ackJobFuture;

    public SimplePushSockJSService(final Config sockjsConfig, final SimplePushServer simplePushServer, 
            final SimplePushConfig simplePushConfig) {
        this.sockjsConfig = sockjsConfig;
        this.simplePushServer = simplePushServer;
        this.config = simplePushConfig;
    }

    @Override
    public Config config() {
        return sockjsConfig;
    }

    @Override
    public void onOpen(final Session session) {
        logger.info("SimplePushSockJSServer onOpen");
        this.session = session;
        if (session.getContext().pipeline().get(ReaperHandler.class) != null) {
            session.getContext().fireUserEventTriggered(simplePushServer);
        }
    }

    @Override @SuppressWarnings("incomplete-switch")
    public void onMessage(final String message) throws Exception {
        final MessageType messageType = JsonUtil.parseFrame(message);
        logger.info("messageType: " + messageType.getMessageType());
        switch (messageType.getMessageType()) {
        case HELLO:
            if (!checkHandshakeCompleted(uaid)) {
                final HandshakeResponse response = simplePushServer.handleHandshake(fromJson(message, HandshakeMessageImpl.class));
                session.send(toJson(response));
                uaid = response.getUAID();
                userAgents.add(uaid, session);
                processUnacked(uaid, session, 0);
                logger.info("UserAgent [" + uaid + "] handshake done");
            }
            break;
        case REGISTER:
            if (checkHandshakeCompleted(uaid)) {
                final RegisterResponse response = simplePushServer.handleRegister(fromJson(message, RegisterMessageImpl.class), uaid);
                session.send(toJson(response));
                logger.info("UserAgent [" + uaid + "] Registered[" + response.getChannelId() + "]");
            }
            break;
        case UNREGISTER:
            if (checkHandshakeCompleted(uaid)) {
                final UnregisterMessage unregister = fromJson(message, UnregisterMessageImpl.class);
                final UnregisterResponse response = simplePushServer.handleUnregister(unregister, uaid);
                session.send(toJson(response));
                logger.info("UserAgent [" + uaid + "] Unregistered[" + response.getChannelId() + "]");
            }
            break;
        case ACK:
            if (checkHandshakeCompleted(uaid)) {
                final AckMessage ack = fromJson(message, AckMessageImpl.class);
                simplePushServer.handleAcknowledgement(ack, uaid);
                processUnacked(uaid, session, config.ackInterval());
            }
            break;
        }
        updateAccessedTime(uaid);
    }
    
    private void updateAccessedTime(final UUID uaid) {
        if (uaid != null) {
            final UserAgent<Session> userAgent = userAgents.get(uaid);
            userAgent.timestamp(System.currentTimeMillis());
        }
    }
    
    private void processUnacked(final UUID uaid, final Session session, final long delay) {
        final Set<Update> unacked = simplePushServer.getUnacknowledged(uaid);
        if (unacked.isEmpty()) {
            if (ackJobFuture != null && !ackJobFuture.isCancelled()) {
                ackJobFuture.cancel(false);
                logger.info("Cancelled Re-Acknowledger job");
            }
        } else if (ackJobFuture == null) {
            ackJobFuture = session.getContext().executor().scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    final Set<Update> unacked = simplePushServer.getUnacknowledged(uaid);
                    logger.info("Resending " + unacked);
                    session.send(toJson(new NotificationMessageImpl(unacked)));
                }
            },
            delay,
            config.ackInterval(), 
            TimeUnit.MILLISECONDS);
        }
    }
    
    private boolean checkHandshakeCompleted(final UUID uaid) {
        if (uaid == null) {
            logger.debug("Hello frame has not been sent");
            return false;
        }
        if (!userAgents.contains(uaid)) {
            logger.debug("UserAgent ["+ uaid + "] was cleaned up due to unactivity for " + config.reaperTimeout() + "ms");
            this.uaid = null;
            return false;
        }
        return true;
    }
        
    @Override
    public void onClose() {
        System.out.println("SimplePushSockJSServer onClose");
        if (ackJobFuture != null) {
            ackJobFuture.cancel(true);
        }
    }
    
}
