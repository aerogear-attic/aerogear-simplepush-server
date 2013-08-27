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
package org.jboss.aerogear.simplepush.server.netty;

import static org.jboss.aerogear.simplepush.protocol.impl.json.JsonUtil.fromJson;
import static org.jboss.aerogear.simplepush.protocol.impl.json.JsonUtil.toJson;
import org.jboss.aerogear.io.netty.handler.codec.sockjs.SockJsConfig;
import org.jboss.aerogear.io.netty.handler.codec.sockjs.SockJsSessionContext;
import org.jboss.aerogear.io.netty.handler.codec.sockjs.SockJsService;
import io.netty.util.concurrent.ScheduledFuture;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.jboss.aerogear.simplepush.protocol.AckMessage;
import org.jboss.aerogear.simplepush.protocol.HelloResponse;
import org.jboss.aerogear.simplepush.protocol.MessageType;
import org.jboss.aerogear.simplepush.protocol.RegisterResponse;
import org.jboss.aerogear.simplepush.protocol.UnregisterMessage;
import org.jboss.aerogear.simplepush.protocol.UnregisterResponse;
import org.jboss.aerogear.simplepush.protocol.Update;
import org.jboss.aerogear.simplepush.protocol.impl.AckMessageImpl;
import org.jboss.aerogear.simplepush.protocol.impl.HelloMessageImpl;
import org.jboss.aerogear.simplepush.protocol.impl.NotificationMessageImpl;
import org.jboss.aerogear.simplepush.protocol.impl.PingMessageImpl;
import org.jboss.aerogear.simplepush.protocol.impl.RegisterMessageImpl;
import org.jboss.aerogear.simplepush.protocol.impl.UnregisterMessageImpl;
import org.jboss.aerogear.simplepush.protocol.impl.json.JsonUtil;
import org.jboss.aerogear.simplepush.server.SimplePushServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SimplePush server implementation using SockJS.
 */
public class SimplePushSockJSService implements SockJsService {

    private final Logger logger = LoggerFactory.getLogger(SimplePushSockJSService.class);

    private final UserAgents userAgents = UserAgents.getInstance();
    private final SockJsConfig sockjsConfig;
    private final SimplePushServer simplePushServer;
    private String uaid;
    private SockJsSessionContext session;
    private ScheduledFuture<?> ackJobFuture;

    /**
     * Sole constructor.
     *
     * @param sockjsConfig the SockJS {@link Config} for this service.
     * @param simplePushServer the {@link SimplePushServer} that this instance will use.
     */
    public SimplePushSockJSService(final SockJsConfig sockjsConfig, final SimplePushServer simplePushServer) {
        this.sockjsConfig = sockjsConfig;
        this.simplePushServer = simplePushServer;
    }

    @Override
    public SockJsConfig config() {
        return sockjsConfig;
    }

    @Override
    public void onOpen(final SockJsSessionContext session) {
        logger.info("SimplePushSockJSServer onOpen");
        this.session = session;
    }

    @Override
    @SuppressWarnings("incomplete-switch")
    public void onMessage(final String message) throws Exception {
        final MessageType messageType = JsonUtil.parseFrame(message);
        logger.info("messageType: " + messageType.getMessageType());
        switch (messageType.getMessageType()) {
        case HELLO:
            if (!checkHandshakeCompleted(uaid)) {
                final HelloResponse response = simplePushServer.handleHandshake(fromJson(message, HelloMessageImpl.class));
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
                processUnacked(uaid, session, simplePushServer.config().acknowledmentInterval());
            }
            break;
        case PING:
            session.send(PingMessageImpl.JSON);
            break;
        }
        userAgents.updateAccessedTime(uaid);
    }

    private void processUnacked(final String uaid, final SockJsSessionContext session, final long delay) {
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
                    simplePushServer.config().acknowledmentInterval(),
                    TimeUnit.MILLISECONDS);
        }
    }

    private boolean checkHandshakeCompleted(final String uaid) {
        if (uaid == null) {
            logger.debug("Hello frame has not been sent");
            return false;
        }
        if (!userAgents.contains(uaid)) {
            logger.debug("UserAgent [" + uaid + "] was cleaned up due to unactivity for " + simplePushServer.config().userAgentReaperTimeout() + "ms");
            this.uaid = null;
            return false;
        }
        return true;
    }

    @Override
    public void onClose() {
        logger.info("SimplePushSockJSServer onClose");
        if (ackJobFuture != null) {
            ackJobFuture.cancel(true);
        }
    }

}
