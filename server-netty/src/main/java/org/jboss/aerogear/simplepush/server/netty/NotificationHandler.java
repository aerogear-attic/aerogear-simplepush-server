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
import static io.netty.util.CharsetUtil.UTF_8;
import static org.jboss.aerogear.simplepush.protocol.impl.json.JsonUtil.toJson;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.ReferenceCountUtil;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jboss.aerogear.io.netty.handler.codec.sockjs.SockJsSessionContext;
import org.jboss.aerogear.simplepush.protocol.impl.NotificationMessageImpl;
import org.jboss.aerogear.simplepush.server.Notification;
import org.jboss.aerogear.simplepush.server.SimplePushServer;
import org.jboss.aerogear.simplepush.server.datastore.ChannelNotFoundException;
import org.jboss.aerogear.simplepush.server.datastore.VersionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles HTTP PUT 'notification' request for the SimplePush server.
 */
public class NotificationHandler extends SimpleChannelInboundHandler<Object> {

    private final UserAgents userAgents = UserAgents.getInstance();
    private final Logger logger = LoggerFactory.getLogger(NotificationHandler.class);

    private final SimplePushServer simplePushServer;
    private final ExecutorService executorServer;

    public NotificationHandler(final SimplePushServer simplePushServer) {
        this.simplePushServer = simplePushServer;
        executorServer = Executors.newCachedThreadPool();
    }

    @Override
    public void messageReceived(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        if (msg instanceof FullHttpRequest) {
            final FullHttpRequest request = (FullHttpRequest) msg;
            final String requestUri = request.getUri();
            logger.debug(requestUri);
            if (requestUri.startsWith(simplePushServer.config().endpointPrefix())) {
                handleHttpRequest(ctx, request);
            } else {
                ctx.fireChannelRead(ReferenceCountUtil.retain(msg));
            }
        } else {
            ctx.fireChannelRead(ReferenceCountUtil.retain(msg));
        }
    }

    private void handleHttpRequest(final ChannelHandlerContext ctx, final FullHttpRequest req) throws Exception {
        if (isHttpRequestValid(req, ctx.channel())) {
            executorServer.submit(new Notifier(req.getUri(), req.content()));
            sendHttpResponse(OK, req, ctx.channel());
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

    private void sendHttpResponse(final HttpResponseStatus status, final FullHttpRequest request, final Channel channel) {
        final ByteBuf content = Unpooled.copiedBuffer(status.reasonPhrase(), UTF_8);
        final FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, status, content);
        response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, content.readableBytes());
        response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/html; charset=UTF-8");
        channel.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private class Notifier implements Callable<Void> {

        private final String endpoint;
        private final ByteBuf payload;

        private Notifier(final String requestUri, final ByteBuf payload) {
            this.endpoint = requestUri.substring(requestUri.lastIndexOf('/') + 1);
            this.payload = payload;
            this.payload.retain();
        }

        @Override
        public Void call() throws Exception {
            try {
                final Notification notification = simplePushServer.handleNotification(endpoint, payload.toString(UTF_8));
                final String uaid = notification.uaid();
                final SockJsSessionContext session = userAgents.get(uaid).context();
                session.send(toJson(new NotificationMessageImpl(notification.ack())));
                userAgents.updateAccessedTime(uaid);
            } catch (final ChannelNotFoundException e) {
                logger.debug("Could not find channel for [" + endpoint + "]");
            } catch (final VersionException e) {
                logger.debug(e.getMessage());
            } finally {
                payload.release();
            }
            return null;
        }
    }

}
