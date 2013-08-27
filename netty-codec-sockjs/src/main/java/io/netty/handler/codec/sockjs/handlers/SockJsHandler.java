/*
 * Copyright 2013 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License, version
 * 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package io.netty.handler.codec.sockjs.handlers;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Values.KEEP_ALIVE;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static java.util.UUID.randomUUID;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.sockjs.SockJsServiceFactory;
import io.netty.handler.codec.sockjs.protocol.Greeting;
import io.netty.handler.codec.sockjs.protocol.Iframe;
import io.netty.handler.codec.sockjs.protocol.Info;
import io.netty.handler.codec.sockjs.transports.EventSourceTransport;
import io.netty.handler.codec.sockjs.transports.HtmlFileTransport;
import io.netty.handler.codec.sockjs.transports.JsonpPollingTransport;
import io.netty.handler.codec.sockjs.transports.JsonpSendTransport;
import io.netty.handler.codec.sockjs.transports.RawWebSocketTransport;
import io.netty.handler.codec.sockjs.transports.Transports;
import io.netty.handler.codec.sockjs.transports.Transports.Types;
import io.netty.handler.codec.sockjs.transports.WebSocketTransport;
import io.netty.handler.codec.sockjs.transports.XhrPollingTransport;
import io.netty.handler.codec.sockjs.transports.XhrSendTransport;
import io.netty.handler.codec.sockjs.transports.XhrStreamingTransport;
import io.netty.util.CharsetUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This handler is the main entry point for SockJS HTTP Request.
 *
 * It is responsible for inspecting the request uri and adding ChannelHandlers for
 * different transport protocols that SockJS support. Once this has been done this
 * handler will be removed from the channel pipeline.
 */
public class SockJsHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(SockJsHandler.class);
    private final Map<String, SockJsServiceFactory> factories = new LinkedHashMap<String, SockJsServiceFactory>();
    private static final ConcurrentMap<String, SockJsSession> sessions = new ConcurrentHashMap<String, SockJsSession>();
    private static final PathParams NON_SUPPORTED_PATH = new NonSupportedPath();
    private static final Pattern SERVER_SESSION_PATTERN = Pattern.compile("^/([^/.]+)/([^/.]+)/([^/.]+)");

    /**
     * Sole constructor which takes one or more {@code SockJSServiceFactory}. These factories will
     * later be used by the server to create the SockJS services that will be exposed by this server
     *
     * @param factories one or more {@link SockJsServiceFactory}s.
     */
    public SockJsHandler(final SockJsServiceFactory... factories) {
        for (SockJsServiceFactory factory : factories) {
            this.factories.put(factory.config().prefix(), factory);
        }
    }

    @Override
    public void channelRead0(final ChannelHandlerContext ctx, final FullHttpRequest request) throws Exception {
        final String path = new QueryStringDecoder(request.getUri()).path();
        for (SockJsServiceFactory factory : factories.values()) {
            if (path.startsWith(factory.config().prefix())) {
                handleService(factory, request, ctx);
                return;
            }
        }
        writeNotFoundResponse(request, ctx);
    }

    private void handleService(final SockJsServiceFactory factory,
            final FullHttpRequest request,
            final ChannelHandlerContext ctx) throws Exception {
        logger.debug("RequestUri : [" + request.getUri() + "]");
        final String pathWithoutPrefix = request.getUri().replaceFirst(factory.config().prefix(), "");
        final String path = new QueryStringDecoder(pathWithoutPrefix).path();
        if (Greeting.matches(path)) {
            writeResponse(ctx.channel(), request, Greeting.response(request));
        } else if (Info.matches(path)) {
            writeResponse(ctx.channel(), request, Info.response(factory.config(), request));
        } else if (Iframe.matches(path)) {
            writeResponse(ctx.channel(), request, Iframe.response(factory.config(), request));
        } else if (Transports.Types.WEBSOCKET.path().equals(path)) {
            addTransportHandler(new RawWebSocketTransport(factory.config(), factory.create()), ctx);
            ctx.fireChannelRead(request.retain());
        } else {
            final PathParams sessionPath = matches(path);
            if (sessionPath.matches()) {
                handleSession(ctx, request, factory, sessionPath);
            } else {
                writeNotFoundResponse(request, ctx);
            }
        }
    }

    private void handleSession(final ChannelHandlerContext ctx,
            final FullHttpRequest request,
            final SockJsServiceFactory factory,
            final PathParams pathParams) throws Exception {
        switch (pathParams.transport()) {
        case XHR:
            addTransportHandler(new XhrPollingTransport(factory.config(), request), ctx);
            addSessionHandler(new PollingSessionState(sessions), getSession(factory, pathParams.sessionId()), ctx);
            break;
        case JSONP:
            addTransportHandler(new JsonpPollingTransport(factory.config(), request), ctx);
            addSessionHandler(new PollingSessionState(sessions), getSession(factory, pathParams.sessionId()), ctx);
            break;
        case XHR_SEND:
            checkSessionExists(pathParams.sessionId(), request);
            addTransportHandler(new XhrSendTransport(factory.config()), ctx);
            addSessionHandler(new SendingSessionState(sessions), sessions.get(pathParams.sessionId()), ctx);
            break;
        case XHR_STREAMING:
            addTransportHandler(new XhrStreamingTransport(factory.config(), request), ctx);
            addSessionHandler(new StreamingSessionState(sessions), getSession(factory, pathParams.sessionId()), ctx);
            break;
        case EVENTSOURCE:
            addTransportHandler(new EventSourceTransport(factory.config(), request), ctx);
            addSessionHandler(new StreamingSessionState(sessions), getSession(factory, pathParams.sessionId()), ctx);
            break;
        case HTMLFILE:
            addTransportHandler(new HtmlFileTransport(factory.config(), request), ctx);
            addSessionHandler(new StreamingSessionState(sessions), getSession(factory, pathParams.sessionId()), ctx);
            break;
        case JSONP_SEND:
            checkSessionExists(pathParams.sessionId(), request);
            addTransportHandler(new JsonpSendTransport(factory.config()), ctx);
            addSessionHandler(new SendingSessionState(sessions), sessions.get(pathParams.sessionId()), ctx);
            break;
        case WEBSOCKET:
            addTransportHandler(new WebSocketTransport(factory.config()), ctx);
            addSessionHandler(new WebSocketSessionState(), new SockJsSession(randomUUID().toString(), factory.create()),
                    ctx);
            break;
        }
        ctx.fireChannelRead(request.retain());
    }

    private void addTransportHandler(final ChannelHandler transportHandler, final ChannelHandlerContext ctx) {
        ctx.pipeline().addLast(transportHandler);
    }

    private void addSessionHandler(final SessionState sessionState, final SockJsSession session,
            final ChannelHandlerContext ctx) {
        ctx.pipeline().addLast(new SessionHandler(sessionState, session));
    }

    private void checkSessionExists(final String sessionId, final HttpRequest request) throws SessionNotFoundException {
        if (!sessions.containsKey(sessionId)) {
            throw new SessionNotFoundException(sessionId, request);
        }
    }

    private SockJsSession getSession(final SockJsServiceFactory factory, final String sessionId) {
        SockJsSession session = sessions.get(sessionId);
        if (session == null) {
            final SockJsSession newSession = new SockJsSession(sessionId, factory.create());
            session = sessions.putIfAbsent(sessionId, newSession);
            if (session == null) {
                session = newSession;
            }
            logger.debug("Created new session [" + sessionId + "]");
        } else {
            logger.debug("Using existing session {" + sessionId + "]");
        }
        return session;
    }

    private void writeNotFoundResponse(final HttpRequest request, final ChannelHandlerContext ctx) {
        final FullHttpResponse response = new DefaultFullHttpResponse(request.getProtocolVersion(), NOT_FOUND,
                Unpooled.copiedBuffer("Not found", CharsetUtil.UTF_8));
        writeResponse(ctx.channel(), request, response);
    }

    private void writeResponse(final Channel channel, final HttpRequest request, final FullHttpResponse response) {
        response.headers().set(CONTENT_LENGTH, response.content().readableBytes());
        boolean hasKeepAliveHeader = KEEP_ALIVE.equalsIgnoreCase(request.headers().get(CONNECTION));
        if (!request.getProtocolVersion().isKeepAliveDefault() && hasKeepAliveHeader) {
            response.headers().set(CONNECTION, KEEP_ALIVE);
        }
        final ChannelFuture wf = channel.writeAndFlush(response);
        if (!HttpHeaders.isKeepAlive(request)) {
            wf.addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof SessionNotFoundException) {
            final SessionNotFoundException se = (SessionNotFoundException) cause;
            logger.debug("Could not find session [" + se.sessionId() + "]");
            writeNotFoundResponse(se.httpRequest(), ctx);
        } else {
            logger.error("exception caught:", cause);
            ctx.fireExceptionCaught(cause);
        }
    }

    public static PathParams matches(final String path) {
        final Matcher matcher = SERVER_SESSION_PATTERN.matcher(path);
        if (matcher.find()) {
            final String serverId = matcher.group(1);
            final String sessionId = matcher.group(2);
            final String transport = matcher.group(3);
            return new MatchingSessionPath(serverId, sessionId, transport);
        } else {
            return NON_SUPPORTED_PATH;
        }
    }

    private class SessionNotFoundException extends Exception {
        private static final long serialVersionUID = 1101611486620901143L;
        private final String sessionId;
        private final HttpRequest request;

        public SessionNotFoundException(final String sessionId, final HttpRequest request) {
            this.sessionId = sessionId;
            this.request = request;
        }

        public String sessionId() {
            return sessionId;
        }

        public HttpRequest httpRequest() {
            return request;
        }
    }

    interface PathParams {
        boolean matches();
        String serverId();
        String sessionId();
        Types transport();
    }

    public static class MatchingSessionPath implements PathParams {
        private String serverId;
        private String sessionId;
        private Transports.Types transport;

        public MatchingSessionPath(final String serverId, final String sessionId, final String transport) {
            this.serverId = serverId;
            this.sessionId = sessionId;
            this.transport = Transports.Types.valueOf(transport.toUpperCase());
        }

        @Override
        public boolean matches() {
            return true;
        }

        @Override
        public String serverId() {
            return serverId;
        }

        @Override
        public String sessionId() {
            return sessionId;
        }

        @Override
        public Types transport() {
            return transport;
        }
    }

    public static class NonSupportedPath implements PathParams {

        @Override
        public boolean matches() {
            return false;
        }

        @Override
        public String serverId() {
            throw new UnsupportedOperationException("serverId is not available in path");
        }

        @Override
        public String sessionId() {
            throw new UnsupportedOperationException("sessionId is not available in path");
        }

        @Override
        public Types transport() {
            throw new UnsupportedOperationException("transport is not available in path");
        }
    }

}
