/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.simplepush.server.netty;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaders.Names.HOST;
import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpMethod.PUT;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static org.jboss.aerogear.simplepush.protocol.impl.json.JsonUtil.fromJson;
import static org.jboss.aerogear.simplepush.protocol.impl.json.JsonUtil.toJson;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.aerogear.simplepush.protocol.AckMessage;
import org.jboss.aerogear.simplepush.protocol.HandshakeResponse;
import org.jboss.aerogear.simplepush.protocol.MessageType;
import org.jboss.aerogear.simplepush.protocol.NotificationMessage;
import org.jboss.aerogear.simplepush.protocol.RegisterResponse;
import org.jboss.aerogear.simplepush.protocol.UnregisterMessage;
import org.jboss.aerogear.simplepush.protocol.UnregisterResponse;
import org.jboss.aerogear.simplepush.protocol.Update;
import org.jboss.aerogear.simplepush.protocol.impl.AckMessageImpl;
import org.jboss.aerogear.simplepush.protocol.impl.HandshakeMessageImpl;
import org.jboss.aerogear.simplepush.protocol.impl.NotificationMessageImpl;
import org.jboss.aerogear.simplepush.protocol.impl.RegisterImpl;
import org.jboss.aerogear.simplepush.protocol.impl.StatusImpl;
import org.jboss.aerogear.simplepush.protocol.impl.UnregisterMessageImpl;
import org.jboss.aerogear.simplepush.protocol.impl.json.JsonUtil;
import org.jboss.aerogear.simplepush.server.SimplePushServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebSocketServerHandler extends ChannelInboundMessageHandlerAdapter<Object> {
    
    private static final Map<UUID, Channel> userAgents = new ConcurrentHashMap<UUID, Channel>();
    private final Logger logger = LoggerFactory.getLogger(WebSocketServerHandler.class);
    
    private final String path;
    private final boolean tls;
    private final String subprotocol;
    private final String endpointPath;
    private final SimplePushServer simplePushServer;
    private UUID userAgent;
    private WebSocketServerHandshaker handshaker;
    
    public WebSocketServerHandler(final String path, 
            final boolean transportLayerSecurity,
            final String subprotocol, 
            final String endpointPath, 
            final SimplePushServer simplePushServer) {
        this.path = path;
        this.tls = transportLayerSecurity;
        this.subprotocol = subprotocol;
        this.endpointPath = endpointPath;
        this.simplePushServer = simplePushServer;
    }

    @Override
    public void messageReceived(final ChannelHandlerContext ctx , final Object msg) throws Exception {
        if (msg instanceof FullHttpRequest) {
            handleHttpRequest(ctx.channel(), (FullHttpRequest) msg);
        } else if (msg instanceof WebSocketFrame) {
            handleWebSocketFrame(ctx.channel(), (WebSocketFrame) msg);
        }
    }

    public void handleHttpRequest(final Channel channel, final FullHttpRequest req) throws Exception {
        if (!isHttpRequestValid(req, channel)) {
            return;
        }

        final String requestUri = req.getUri();
        if (requestUri.startsWith(endpointPath)) {
            final String channelId = requestUri.substring(requestUri.lastIndexOf('/') + 1);
            final Future<Void> future = channel.eventLoop().submit(new Notifier(channelId, req.content()));
            future.addListener(new NotificationFutureListener(channel, req));
        } else {
            final String wsUrl = getWebSocketLocation(tls, req, path);
            logger.info("WebSocket location: " + wsUrl);
            final WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(wsUrl, subprotocol, false);
            handshaker = wsFactory.newHandshaker(req);
            if (handshaker == null) {
                WebSocketServerHandshakerFactory.sendUnsupportedWebSocketVersionResponse(channel);
            } else {
                handshaker.handshake(channel, req);
            }
        }
    }
    
    private boolean isHttpRequestValid(final FullHttpRequest request, final Channel channel) {
        if (!request.getDecoderResult().isSuccess()) {
            sendHttpResponse(BAD_REQUEST, request, channel);
            return false;
        }
        if (request.getMethod() != PUT && request.getMethod() != GET) {
            sendHttpResponse(FORBIDDEN, request, channel);
            return false;
        }
        return true;
    }
    
    protected void handleWebSocketFrame(final Channel channel, final WebSocketFrame frame) throws Exception { 
        if (frame instanceof CloseWebSocketFrame) {
            frame.retain();
            logger.info("Closing WebSocket for UserAgent [" + userAgent + "]");
            final ChannelFuture future = handshaker.close(channel, (CloseWebSocketFrame) frame);
            future.addListener(new GenericFutureListener<Future<Void>>() {
                @Override
                public void operationComplete(Future<Void> future) throws Exception {
                    simplePushServer.removeAllChannels(userAgent);
                    userAgents.remove(userAgent);
                }
            });
            return;
        }
        if (frame instanceof PingWebSocketFrame) {
            frame.content().retain();
            channel.write(new PongWebSocketFrame(frame.content()));
            return;
        }
        if (!(frame instanceof TextWebSocketFrame)) {
            throw new UnsupportedOperationException(String.format("%s frame types not supported", frame.getClass() .getName()));
        }
        handleSimplePushMessage(channel, (TextWebSocketFrame) frame);
    }
    
    private void handleSimplePushMessage(final Channel channel, final TextWebSocketFrame frame) throws Exception {
        final MessageType messageType = JsonUtil.parseFrame(frame.text());
        switch (messageType.getMessageType()) {
        case HELLO:
            if (userAgent == null) {
                final HandshakeResponse response = simplePushServer.handleHandshake(fromJson(frame.text(), HandshakeMessageImpl.class));
                writeJsonResponse(toJson(response), channel);
                userAgent = response.getUAID();
                userAgents.put(userAgent, channel);
                logger.info("UserAgent [" + userAgent + " handshake done");
            }
            break;
        case REGISTER:
            if (checkHandshakeCompleted(channel)) {
                final RegisterResponse response = simplePushServer.handleRegister(fromJson(frame.text(), RegisterImpl.class), userAgent);
                logger.info("UserAgent [" + userAgent + "] Registered[" + response.getChannelId() + "]");
                writeJsonResponse(toJson(response), channel);
            }
            break;
        case UNREGISTER:
            if (checkHandshakeCompleted(channel)) {
                final UnregisterMessage unregister = fromJson(frame.text(), UnregisterMessageImpl.class);
                final UnregisterResponse response = simplePushServer.handleUnregister(unregister, userAgent);
                logger.info("UserAgent [" + userAgent + "] Unregistered[" + response.getChannelId() + "]");
                writeJsonResponse(toJson(response), channel);
            }
            break;
        case ACK:
            if (checkHandshakeCompleted(channel)) {
                final AckMessage ack = fromJson(frame.text(), AckMessageImpl.class);
                final Set<Update> unacked = simplePushServer.handleAcknowledgement(ack, userAgent);
                if (!unacked.isEmpty()) {
                    channel.eventLoop().submit(new Acker(unacked));
                }
            }
            break;
        default:
            break;
        }
    }
    
    private boolean checkHandshakeCompleted(final Channel channel) {
        if (userAgent == null) {
            logger.debug("Hello message has not been sent, ignoring the frame");
            return false;
        }
        return true;
    }
    
    private ChannelFuture writeJsonResponse(final String json, final Channel channel) {
        return channel.write(new TextWebSocketFrame(json));
    }
    
    private void sendHttpResponse(final HttpResponseStatus status, final FullHttpRequest request, final Channel channel) {
        sendHttpResponse("", status, request, channel);
    }
    
    private void sendHttpResponse(final String body,final HttpResponseStatus status, final FullHttpRequest request, final Channel channel) {
        final FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, status);
        res.headers().set(CONTENT_TYPE, "text/html; charset=UTF-8");
        final ByteBuf content = Unpooled.copiedBuffer(body, CharsetUtil.UTF_8);
        HttpHeaders.setContentLength(res, content.readableBytes());
        sendHttpResponse(channel, request, res);
    }

    private static void sendHttpResponse(final Channel channel, final FullHttpRequest req, final FullHttpResponse res) {
        res.content().writeBytes(Unpooled.copiedBuffer(res.getStatus().toString(), CharsetUtil.UTF_8));
        HttpHeaders.setContentLength(res, res.content().readableBytes());

        ChannelFuture f = channel.write(res);
        if (!HttpHeaders.isKeepAlive(req) || res.getStatus().code() != 200) {
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) throws Exception {
        ctx.channel().write(new TextWebSocketFrame(new StatusImpl(400, cause.getMessage()).toString()));
        super.exceptionCaught(ctx, cause);
    }
    
    @Override
    public void channelUnregistered(final ChannelHandlerContext ctx) throws Exception {
        if (userAgent != null) {
            userAgents.remove(userAgent);
            logger.info("UserAgent [" + userAgent + "] unregistered");
            userAgent = null;
        }
        super.channelUnregistered(ctx);
    }

    private static String getWebSocketLocation(final boolean tls, final FullHttpRequest req, final String path) {
        final String protocol = tls ? "wss://" : "ws://";
        return protocol + req.headers().get(HOST) + path;
    }

    private class Notifier implements Callable<Void> {
        
        private final String channelId;
        private final ByteBuf content;
    
        private Notifier(final String channelId, final ByteBuf content) {
            this.channelId = channelId;
            this.content = content;
            this.content.retain();
        }
    
        @Override
        public Void call() throws Exception {
            try {
                final UUID uaid = simplePushServer.fromChannel(channelId);
                final String payload = content.toString(CharsetUtil.UTF_8);
                logger.info("UserAgent [" + uaid + "] Notification [" + channelId + ", " + payload + "]");
                final NotificationMessage notification = simplePushServer.handleNotification(channelId, uaid, payload);
                writeJsonResponse(toJson(notification), userAgents.get(uaid)); 
                return null;
            } finally {
                content.release();
            }
        }
    }
    
    private class Acker implements Callable<Void> {
        
        private Set<Update> updates;
    
        private Acker(final Set<Update> updates) {
            this.updates = updates;
        }
    
        @Override
        public Void call() throws Exception {
            writeJsonResponse(toJson(new NotificationMessageImpl(updates)), userAgents.get(userAgent)); 
            return null;
        }
        
    }

    private class NotificationFutureListener implements GenericFutureListener<Future<Void>> {
        
        private Channel channel;
        private FullHttpRequest request;
    
        private NotificationFutureListener(final Channel channel, final FullHttpRequest request) {
            this.channel = channel;
            this.request = request;
        }
    
        @Override
        public void operationComplete(Future<Void> future) throws Exception {
            if (future.cause() != null) {
                logger.debug("Notification failed: ", future.cause());
                final String body = future.cause().getMessage() != null ? future.cause().getMessage() : "";
                sendHttpResponse(body, BAD_REQUEST, request, channel);
            } else {
                sendHttpResponse(OK, request, channel);
            }
        }
        
    }
    
}
