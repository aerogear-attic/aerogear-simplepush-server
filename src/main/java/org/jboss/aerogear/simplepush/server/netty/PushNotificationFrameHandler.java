/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.jboss.aerogear.simplepush.server.netty;

import static org.jboss.aerogear.simplepush.protocol.impl.json.JsonUtil.fromJson;
import static org.jboss.aerogear.simplepush.protocol.impl.json.JsonUtil.toJson;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import org.jboss.aerogear.simplepush.protocol.HandshakeResponse;
import org.jboss.aerogear.simplepush.protocol.MessageType;
import org.jboss.aerogear.simplepush.protocol.Status;
import org.jboss.aerogear.simplepush.protocol.impl.HandshakeImpl;
import org.jboss.aerogear.simplepush.protocol.impl.json.JsonUtil;
import org.jboss.aerogear.simplepush.server.SimplePushServer;

public class PushNotificationFrameHandler extends ChannelInboundMessageHandlerAdapter<TextWebSocketFrame> {
    
    private SimplePushServer simplePushServer = new SimplePushServer();
    
    @Override
    public void messageReceived(ChannelHandlerContext ctx, TextWebSocketFrame frame) throws Exception {
        final MessageType messageType = JsonUtil.parseFrame(frame.text());
        switch (messageType.getMessageType()) {
        case HELLO:
            final HandshakeResponse response = simplePushServer.handleHandshake(fromJson(frame.text(), HandshakeImpl.class));
            writeJsonResponse(toJson(response), ctx);
            break;
        case REGISTER:
            ctx.channel().write(new TextWebSocketFrame("register!"));
            break;
        case UNREGISTER:
            ctx.channel().write(new TextWebSocketFrame("unregister!"));
            break;
        case ACK:
            ctx.channel().write(new TextWebSocketFrame("ack!"));
            break;
        case NOTIFICATION:
            ctx.channel().write(new TextWebSocketFrame("notification!"));
            break;
        default:
            break;
        }
    }
    
    private void writeJsonResponse(final String json, final ChannelHandlerContext ctx) {
        ctx.channel().write(new TextWebSocketFrame(json));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.channel().write(new TextWebSocketFrame(Status.badRequest(cause.getMessage()).toString()));
        super.exceptionCaught(ctx, cause);
    }
    
    

}
