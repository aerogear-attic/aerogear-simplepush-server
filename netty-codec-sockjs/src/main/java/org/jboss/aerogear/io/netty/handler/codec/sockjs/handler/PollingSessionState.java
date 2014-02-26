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
package org.jboss.aerogear.io.netty.handler.codec.sockjs.handler;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import org.jboss.aerogear.io.netty.handler.codec.sockjs.SockJsConfig;
import org.jboss.aerogear.io.netty.handler.codec.sockjs.protocol.MessageFrame;
import io.netty.util.internal.StringUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.util.concurrent.ConcurrentMap;


/**
 * A polling state does not have a persistent connection to the client, instead a client
 * will connect, poll, to request data.
 *
 * The first poll request will open the session and the ChannelHandlerContext for
 * that request will be stored along with the new session. Subsequent request will
 * use the same sessionId and hence use the same session.
 *
 * A polling request will flush any queued up messages in the session and write them
 * out to the current channel. It cannot use the original channel for the session as it
 * most likely will have been closed. Instead it will use current channel effectively
 * writing out the queued up messages to the pipeline to be handled, and eventually returned
 * in the response.
 *
 */
abstract class PollingSessionState extends AbstractTimersSessionState {
    private static final InternalLogger logger = InternalLoggerFactory.getInstance(PollingSessionState.class);
    private final ConcurrentMap<String, SockJsSession> sessions;
    private final HttpRequest request;
    private final SockJsConfig config;

    public PollingSessionState(final ConcurrentMap<String, SockJsSession> sessions,
                               final HttpRequest request,
                               final SockJsConfig config) {
        super(sessions);
        this.sessions = sessions;
        this.request = request;
        this.config = config;
    }

    /**
     * Gives implementations the ability to decide what a response should look like and
     * also how it should be written back to the client.
     *
     * @param request the polling HttpRequest.
     * @param config the SockJsConfig.
     * @param ctx {@code ChannelHandlerContext} the context.
     */
    public abstract void sendNoMessagesResponse(HttpRequest request, SockJsConfig config, ChannelHandlerContext ctx);

    @Override
    public void onOpen(final SockJsSession session, final ChannelHandlerContext ctx) {
        flushMessages(ctx, session);
    }

    @Override
    public ChannelHandlerContext getSendingContext(SockJsSession session) {
        return session.connectionContext();
    }

    private void flushMessages(final ChannelHandlerContext ctx, final SockJsSession session) {
        final String[] allMessages = session.getAllMessages();
        if (allMessages.length == 0) {
            sendNoMessagesResponse(request, config, ctx);
            return;
        }
        final MessageFrame messageFrame = new MessageFrame(allMessages);
        ChannelFuture channelFuture = ctx.channel().writeAndFlush(messageFrame);
        channelFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(final ChannelFuture future) throws Exception {
                if (!future.isSuccess()) {
                    session.addMessages(allMessages);
                }
            }
        });
        channelFuture.addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public boolean isInUse(final SockJsSession session) {
        return session.connectionContext().channel().isActive() || session.inuse();
    }

    @Override
    public void onSockJSServerInitiatedClose(final SockJsSession session) {
        final ChannelHandlerContext context = session.connectionContext();
        if (context != null) { //could be null if the request is aborted, for example due to missing callback.
            if (logger.isDebugEnabled()) {
                logger.debug("Will close session connectionContext {}", session.connectionContext());
            }
            context.close();
        }
        sessions.remove(session.sessionId());
    }

    @Override
    public String toString() {
        return StringUtil.simpleClassName(this);
    }

    @Override
    public void onClose() {
    }

}
