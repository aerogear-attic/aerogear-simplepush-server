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

import static io.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static io.netty.handler.codec.http.HttpHeaders.setContentLength;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaders.Names.HOST;
import static io.netty.handler.codec.http.HttpMethod.PUT;
import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static org.jboss.aerogear.simplepush.protocol.impl.json.JsonUtil.fromJson;
import static org.jboss.aerogear.simplepush.protocol.impl.json.JsonUtil.toJson;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

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
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.util.CharsetUtil;

import org.jboss.aerogear.simplepush.protocol.Ack;
import org.jboss.aerogear.simplepush.protocol.HandshakeResponse;
import org.jboss.aerogear.simplepush.protocol.MessageType;
import org.jboss.aerogear.simplepush.protocol.Notification;
import org.jboss.aerogear.simplepush.protocol.RegisterResponse;
import org.jboss.aerogear.simplepush.protocol.Unregister;
import org.jboss.aerogear.simplepush.protocol.UnregisterResponse;
import org.jboss.aerogear.simplepush.protocol.Update;
import org.jboss.aerogear.simplepush.protocol.impl.AckImpl;
import org.jboss.aerogear.simplepush.protocol.impl.HandshakeImpl;
import org.jboss.aerogear.simplepush.protocol.impl.NotificationImpl;
import org.jboss.aerogear.simplepush.protocol.impl.RegisterImpl;
import org.jboss.aerogear.simplepush.protocol.impl.StatusImpl;
import org.jboss.aerogear.simplepush.protocol.impl.UnregisterImpl;
import org.jboss.aerogear.simplepush.protocol.impl.UnregisterResponseImpl;
import org.jboss.aerogear.simplepush.protocol.impl.UpdateImpl;
import org.jboss.aerogear.simplepush.protocol.impl.json.JsonUtil;
import org.jboss.aerogear.simplepush.server.SimplePushServer;
import org.jboss.aerogear.simplepush.util.VersionExtractor;

public class WebSocketServerHandler extends ChannelInboundMessageHandlerAdapter<Object> {
    
    private static final SimplePushServer simplePushServer = new SimplePushServer();
    private static final Map<UUID, Channel> userAgents = new ConcurrentHashMap<UUID, Channel>();
    
    private final String path;
    private final String subprotocol;
    private final String endpointPath;
    private UUID userAgent;
    private WebSocketServerHandshaker handshaker;
    
    public WebSocketServerHandler(final String path, final String subprotocol, final String endpointPath) {
        this.path = path;
        this.subprotocol = subprotocol;
        this.endpointPath = endpointPath;
    }

    @Override
    public void messageReceived(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        if (msg instanceof FullHttpRequest) {
            handleHttpRequest(ctx, (FullHttpRequest) msg);
        } else if (msg instanceof WebSocketFrame) {
            handleWebSocketFrame(ctx, (WebSocketFrame) msg);
        }
    }

    private void handleHttpRequest(final ChannelHandlerContext ctx, final FullHttpRequest req) throws Exception {
        if (!req.getDecoderResult().isSuccess()) {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST));
            return;
        }

        if (req.getMethod() != PUT && req.getMethod() != GET) {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN));
            return;
        }

        final String requestUri = req.getUri();
        if (requestUri.startsWith(endpointPath)) {
            final String channelId = requestUri.substring(requestUri.lastIndexOf('/')+1);
            
            final NotificationEvent notificationEvent = new NotificationEvent(channelId, req.data().toString(CharsetUtil.UTF_8));
            //TODO: make the handling async.
            ctx.fireUserEventTriggered(notificationEvent);
            handleNotification(notificationEvent);
            
            ByteBuf content = Unpooled.copiedBuffer("", CharsetUtil.UTF_8);
            FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, OK);

            res.headers().set(CONTENT_TYPE, "text/html; charset=UTF-8");
            setContentLength(res, content.readableBytes());

            sendHttpResponse(ctx, req, res);
            return;
        }
        if ("/favicon.ico".equals(req.getUri())) {
            FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, NOT_FOUND);
            sendHttpResponse(ctx, req, res);
            return;
        }

        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(getWebSocketLocation(req, path), subprotocol, false);
        handshaker = wsFactory.newHandshaker(req);
        if (handshaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedWebSocketVersionResponse(ctx.channel());
        } else {
            handshaker.handshake(ctx.channel(), req);
        }
    }
    
    @Override
    //TODO: make the triggering of notification events async
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof NotificationEvent) {
            handleNotification((NotificationEvent) evt);
        }
    }
    
    private void handleNotification(final NotificationEvent event) {
        try {
            final UpdateImpl updateImpl = new UpdateImpl(event.channelId, VersionExtractor.extractVersion(event.body));
            final Notification notification = new NotificationImpl(new HashSet<Update>(Arrays.asList(updateImpl)));
            final String json = JsonUtil.toJson(notification);
            final UUID uaid = simplePushServer.getChannel(event.channelId).getUAID();
            final Channel nettyChannel = userAgents.get(uaid);
            writeJsonResponse(json, nettyChannel);
        } catch (final Exception e) {
            //TODO: error handling should be implemented here.
            e.printStackTrace();
        }
    }

    private void handleWebSocketFrame(final ChannelHandlerContext ctx, final WebSocketFrame frame) throws Exception { 

        // Check for closing frame
        if (frame instanceof CloseWebSocketFrame) {
            frame.retain();
            handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame);
            return;
        }
        if (frame instanceof PingWebSocketFrame) {
            frame.data().retain();
            ctx.channel().write(new PongWebSocketFrame(frame.data()));
            return;
        }
        if (!(frame instanceof TextWebSocketFrame)) {
            throw new UnsupportedOperationException(String.format("%s frame types not supported", frame.getClass() .getName()));
        }

        handleSimplePushMessage(ctx, (TextWebSocketFrame) frame);
    }
    
    public void handleSimplePushMessage(final ChannelHandlerContext ctx, final TextWebSocketFrame frame) throws Exception {
        final MessageType messageType = JsonUtil.parseFrame(frame.text());
        switch (messageType.getMessageType()) {
        case HELLO:
            if (userAgent == null) {
                final HandshakeResponse response = simplePushServer.handleHandshake(fromJson(frame.text(), HandshakeImpl.class));
                writeJsonResponse(toJson(response), ctx.channel());
                userAgent = response.getUAID();
                userAgents.put(userAgent, ctx.channel());
            }
            break;
        case REGISTER:
            if (checkHandshakeCompleted(ctx)) {
                final RegisterResponse response = simplePushServer.handleRegister(fromJson(frame.text(), RegisterImpl.class), userAgent);
                writeJsonResponse(toJson(response), ctx.channel());
            }
            break;
        case UNREGISTER:
            if (checkHandshakeCompleted(ctx)) {
                final Unregister unregister = fromJson(frame.text(), UnregisterImpl.class);
                final String channelId = unregister.getChannelId();
                final boolean removed = simplePushServer.removeChannel(channelId);
                final UnregisterResponse response = removed ? 
                        new UnregisterResponseImpl(channelId, new StatusImpl(200, "OK")):
                        new UnregisterResponseImpl(channelId, new StatusImpl(500, "Could not remove the channel"));
                writeJsonResponse(toJson(response), ctx.channel());
            }
            break;
        case ACK:
            if (checkHandshakeCompleted(ctx)) {
                final Ack ack = fromJson(frame.text(), AckImpl.class);
                final Set<String> updates = ack.getUpdates();
                for (String channelId : updates) {
                    System.out.println("Acked: " + channelId);
                }
                //TODO: resend unacknowledged notifications.
            }
            break;
        default:
            break;
        }
    }
    
    private boolean checkHandshakeCompleted(final ChannelHandlerContext ctx) {
        if (userAgent == null) {
            ctx.channel().write(new TextWebSocketFrame("Hello message has not been sent"));
        }
        return true;
    }
    
    private ChannelFuture writeJsonResponse(final String json, final Channel channel) {
        return channel.write(new TextWebSocketFrame(json));
    }

    private static void sendHttpResponse(final ChannelHandlerContext ctx, final FullHttpRequest req, final FullHttpResponse res) {
        if (res.getStatus().code() != 200) {
            res.data().writeBytes(Unpooled.copiedBuffer(res.getStatus().toString(), CharsetUtil.UTF_8));
            setContentLength(res, res.data().readableBytes());
        }

        ChannelFuture f = ctx.channel().write(res);
        if (!isKeepAlive(req) || res.getStatus().code() != 200) {
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) throws Exception {
        ctx.channel().write(new TextWebSocketFrame(new StatusImpl(400, cause.getMessage()).toString()));
        super.exceptionCaught(ctx, cause);
        cause.printStackTrace();
    }

    private static String getWebSocketLocation(final FullHttpRequest req, final String path) {
        return "ws://" + req.headers().get(HOST) + path;
    }
    
    private static class NotificationEvent {
        
        private String channelId;
        private String body;

        public NotificationEvent(final String channelId, final String body) {
            this.channelId = channelId;
            this.body = body;
        }
        
        @Override
        public String toString() {
            return "NotificationEvent[channelId=" + channelId + ", body=" + body + "]";
        }
    }
}
