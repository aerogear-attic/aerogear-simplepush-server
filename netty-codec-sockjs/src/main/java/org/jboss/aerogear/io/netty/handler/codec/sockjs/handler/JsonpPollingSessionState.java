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

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;
import org.jboss.aerogear.io.netty.handler.codec.sockjs.SockJsConfig;
import org.jboss.aerogear.io.netty.handler.codec.sockjs.protocol.HeartbeatFrame;

import java.util.concurrent.ConcurrentMap;

class JsonpPollingSessionState extends PollingSessionState {

    public JsonpPollingSessionState(ConcurrentMap<String, SockJsSession> sessions, HttpRequest request, SockJsConfig config) {
        super(sessions, request, config);
    }

    @Override
    public void sendNoMessagesResponse(final HttpRequest request, final SockJsConfig config, final ChannelHandlerContext ctx) {
        ctx.writeAndFlush(new HeartbeatFrame()).addListener(ChannelFutureListener.CLOSE);
    }

}
