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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.util.concurrent.DefaultEventExecutorGroup;

import java.net.URI;
import java.util.UUID;

import org.jboss.aerogear.io.netty.handler.codec.sockjs.SockJsConfig;
import org.jboss.aerogear.simplepush.protocol.HelloResponse;
import org.jboss.aerogear.simplepush.protocol.MessageType;
import org.jboss.aerogear.simplepush.protocol.impl.HelloMessageImpl;
import org.jboss.aerogear.simplepush.protocol.impl.HelloResponseImpl;
import org.jboss.aerogear.simplepush.protocol.impl.RegisterMessageImpl;
import org.jboss.aerogear.simplepush.protocol.impl.RegisterResponseImpl;
import org.jboss.aerogear.simplepush.protocol.impl.json.JsonUtil;
import org.jboss.aerogear.simplepush.server.DefaultSimplePushConfig;
import org.jboss.aerogear.simplepush.server.SimplePushServerConfig;
import org.jboss.aerogear.simplepush.server.datastore.DataStore;
import org.jboss.aerogear.simplepush.server.datastore.InMemoryDataStore;
import org.jboss.aerogear.simplepush.util.UUIDUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class NettySimplePushSockJSServerTest {

    private static final int port = 1111;
    private static Channel channel;
    private static final EventLoopGroup bossGroup = new NioEventLoopGroup();
    private static final EventLoopGroup workerGroup = new NioEventLoopGroup();
    private static final DefaultEventExecutorGroup eventExecutorGroup = new DefaultEventExecutorGroup(1);

    @BeforeClass
    public static void startSimplePushServer() throws Exception {
        final SockJsConfig sockJSConfig = SockJsConfig.withPrefix("/simplepush").cookiesNeeded().build();
        final DataStore datastore = new InMemoryDataStore();
        final ServerBootstrap sb = new ServerBootstrap();
        final SimplePushServerConfig simplePushConfig = DefaultSimplePushConfig.create()
                .userAgentReaperTimeout(2000L)
                .password("test")
                .build();
        sb.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new SockJSChannelInitializer(simplePushConfig, datastore, sockJSConfig, eventExecutorGroup));
        channel = sb.bind(port).sync().channel();
    }

    @AfterClass
    public static void stopSimplePushServer() throws InterruptedException {
        final ChannelFuture disconnect = channel.disconnect();
        disconnect.await(1000);
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
        eventExecutorGroup.shutdownGracefully();
    }

    @Test
    public void withoutTLS() throws Exception {
        final URI uri = new URI("ws://127.0.0.1:" + port + "/simplepush/websocket");
        final EventLoopGroup group = new NioEventLoopGroup();
        try {
            final Bootstrap b = new Bootstrap();
            final HttpHeaders customHeaders = new DefaultHttpHeaders();
            final WebSocketClientHandler handler = new WebSocketClientHandler(
                            WebSocketClientHandshakerFactory.newHandshaker(
                                    uri, WebSocketVersion.V13, null, false, customHeaders));
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast("http-codec", new HttpClientCodec());
                            pipeline.addLast("aggregator", new HttpObjectAggregator(8192));
                            pipeline.addLast("ws-handler", handler);
                        }
                    });

            final Channel ch = b.connect(uri.getHost(), uri.getPort()).sync().channel();
            handler.handshakeFuture().sync();

            final String uaid = UUIDUtil.newUAID();
            final String json = JsonUtil.toJson(new HelloMessageImpl(uaid.toString()));
            final ChannelFuture future = ch.writeAndFlush(new TextWebSocketFrame(json));
            future.sync();
            final TextWebSocketFrame textFrame = handler.getTextFrame();
            final HelloResponse fromJson = JsonUtil.fromJson(textFrame.text(), HelloResponseImpl.class);
            assertThat(fromJson.getMessageType(), equalTo(MessageType.Type.HELLO));
            assertThat(fromJson.getUAID(), equalTo(uaid));
            textFrame.release();

            final String channelId = UUID.randomUUID().toString();
            final String register = JsonUtil.toJson(new RegisterMessageImpl(channelId));
            final ChannelFuture registerFuture = ch.writeAndFlush(new TextWebSocketFrame(register));
            registerFuture.sync();
            final TextWebSocketFrame registerFrame = handler.getTextFrame();
            final RegisterResponseImpl registerResponse = JsonUtil.fromJson(registerFrame.text(), RegisterResponseImpl.class);
            assertThat(registerResponse.getMessageType(), equalTo(MessageType.Type.REGISTER));
            assertThat(registerResponse.getChannelId(), equalTo(channelId));

            ch.writeAndFlush(new CloseWebSocketFrame());

            ch.closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }

    @Test
    public void userAgentReaper() throws Exception {
        final URI uri = new URI("ws://127.0.0.1:" + port + "/simplepush/websocket");
        final EventLoopGroup group = new NioEventLoopGroup();
        try {
            final Bootstrap b = new Bootstrap();
            final HttpHeaders customHeaders = new DefaultHttpHeaders();
            final WebSocketClientHandler handler = new WebSocketClientHandler(
                            WebSocketClientHandshakerFactory.newHandshaker(
                                    uri, WebSocketVersion.V13, null, false, customHeaders));
            b.group(group).channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ChannelPipeline pipeline = ch.pipeline();
                    pipeline.addLast("http-codec", new HttpClientCodec());
                    pipeline.addLast("aggregator", new HttpObjectAggregator(8192));
                    pipeline.addLast("ws-handler", handler);
                }
            });

            final Channel ch = b.connect(uri.getHost(), uri.getPort()).sync().channel();
            handler.handshakeFuture().sync();

            final String uaid = UUIDUtil.newUAID();
            final String json = JsonUtil.toJson(new HelloMessageImpl(uaid.toString()));
            final ChannelFuture future = ch.writeAndFlush(new TextWebSocketFrame(json));
            future.sync();
            final TextWebSocketFrame textFrame = handler.getTextFrame();
            final HelloResponse fromJson = JsonUtil.fromJson(textFrame.text(), HelloResponseImpl.class);
            assertThat(fromJson.getMessageType(), equalTo(MessageType.Type.HELLO));
            assertThat(fromJson.getUAID(), equalTo(uaid));
            textFrame.release();

            Thread.sleep(3000);
            final String channelId = UUID.randomUUID().toString();
            final String register = JsonUtil.toJson(new RegisterMessageImpl(channelId));
            final ChannelFuture registerFuture = ch.writeAndFlush(new TextWebSocketFrame(register));
            registerFuture.sync();
            ch.writeAndFlush(new CloseWebSocketFrame());
            ch.closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }

}
