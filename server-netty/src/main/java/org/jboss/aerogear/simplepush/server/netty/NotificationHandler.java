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

import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpMethod.PUT;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static org.jboss.aerogear.simplepush.protocol.impl.json.JsonUtil.toJson;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.sockjs.SessionContext;
import io.netty.handler.codec.sockjs.transports.Transports;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.util.UUID;
import java.util.concurrent.Callable;

import org.jboss.aerogear.simplepush.protocol.NotificationMessage;
import org.jboss.aerogear.simplepush.server.SimplePushServer;
import org.jboss.aerogear.simplepush.server.datastore.ChannelNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles HTTP PUT 'notification' request for the SimplePush server.
 */
public class NotificationHandler extends SimpleChannelInboundHandler<Object> {

    private final UserAgents userAgents = UserAgents.getInstance();
    private final Logger logger = LoggerFactory.getLogger(NotificationHandler.class);

    private final SimplePushServer simplePushServer;

    public NotificationHandler(final SimplePushServer simplePushServer) {
        this.simplePushServer = simplePushServer;
    }

    @Override
    public void channelRead0(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        if (msg instanceof FullHttpRequest) {
            final FullHttpRequest request = (FullHttpRequest) msg;
            final String requestUri = request.getUri();
            if (requestUri.startsWith(simplePushServer.config().endpointUrlPrefix())) {
                handleHttpRequest(ctx, request);
            } else {
                ctx.fireChannelRead(ReferenceCountUtil.retain(msg));
            }
        } else {
            ctx.fireChannelRead(ReferenceCountUtil.retain(msg));
        }
    }

    private void handleHttpRequest(final ChannelHandlerContext ctx, final FullHttpRequest req) throws Exception {
        if (!isHttpRequestValid(req, ctx.channel())) {
            return;
        }
        final String requestUri = req.getUri();
        final String channelId = requestUri.substring(requestUri.lastIndexOf('/') + 1);
        final Future<Void> future = ctx.channel().eventLoop().submit(new Notifier(channelId, req.content()));
        future.addListener(new NotificationFutureListener(ctx.channel(), req));
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

    private void sendHttpResponse(final HttpResponseStatus status, final FullHttpRequest request, final Channel channel) {
        final FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, status);
        Transports.writeContent(response, response.getStatus().toString(), Transports.CONTENT_TYPE_HTML);
        channel.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
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
                final SessionContext session = userAgents.get(uaid).context();
                session.send(toJson(notification));
                userAgents.updateAccessedTime(uaid);
                return null;
            } finally {
                content.release();
            }
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
                if (future.cause() instanceof ChannelNotFoundException) {
                    final ChannelNotFoundException cne = (ChannelNotFoundException) future.cause();
                    logger.warn("Could not find channel [" + cne.channelId() + "]");
                }
                sendHttpResponse(BAD_REQUEST, request, channel);
            } else {
                sendHttpResponse(OK, request, channel);
            }
        }
    }

}
