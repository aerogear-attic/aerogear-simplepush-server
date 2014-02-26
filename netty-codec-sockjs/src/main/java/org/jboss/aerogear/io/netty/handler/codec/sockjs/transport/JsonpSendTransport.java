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
package org.jboss.aerogear.io.netty.handler.codec.sockjs.transport;

import static io.netty.handler.codec.http.HttpResponseStatus.OK;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.jboss.aerogear.io.netty.handler.codec.sockjs.SockJsConfig;
import io.netty.util.internal.StringUtil;
import static org.jboss.aerogear.io.netty.handler.codec.sockjs.transport.Transports.CONTENT_TYPE_PLAIN;
import static org.jboss.aerogear.io.netty.handler.codec.sockjs.transport.Transports.responseWithContent;
import static org.jboss.aerogear.io.netty.handler.codec.sockjs.transport.Transports.setDefaultHeaders;

/**
 * JSON Padding (JSONP) Polling is a transport where there is no open connection between
 * the client and the server. Instead the client will issue a new request for polling from
 * and sending data to the SockJS service.
 *
 * This handler is responsible for handling data destined for the target SockJS service.
 *
 * @see JsonpPollingTransport
 */
public class JsonpSendTransport extends AbstractSendTransport {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(JsonpSendTransport.class);

    public JsonpSendTransport(final SockJsConfig config) {
        super(config);
    }

    @Override
    public void respond(final ChannelHandlerContext ctx, final FullHttpRequest request) throws Exception {
        final FullHttpResponse response = responseWithContent(request.getProtocolVersion(), OK, CONTENT_TYPE_PLAIN, "ok");
        logger.info("Responding=" + response.getStatus() + ", request.uri=" + request.getUri());
        setDefaultHeaders(response, config);
        if (ctx.channel().isActive() && ctx.channel().isRegistered()) {
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public String toString() {
        return StringUtil.simpleClassName(this) + "[config=" + config + ']';
    }
}

