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

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.DefaultEventExecutorGroup;

import org.jboss.aerogear.simplepush.server.datastore.DataStore;
import org.jboss.aerogear.simplepush.server.datastore.InMemoryDataStore;

public class NettyWebSocketServer {

    private final String host;
    private final int port;
    private final Config config;

    public NettyWebSocketServer(final Config config , final String host, final int port) {
        this.port = port;
        this.config = config;
        this.host = host;
    }

    public void run() throws Exception {
        final DataStore datastore = new InMemoryDataStore();
        final EventLoopGroup bossGroup = new NioEventLoopGroup();
        final EventLoopGroup workerGroup = new NioEventLoopGroup();
        final DefaultEventExecutorGroup reaperExcutorGroup = new DefaultEventExecutorGroup(1);
        try {
            final ServerBootstrap sb = new ServerBootstrap();
            sb.group(bossGroup, workerGroup)
            .channel(NioServerSocketChannel.class)
            .childHandler(new WebSocketChannelInitializer(config, datastore, reaperExcutorGroup));
            final Channel ch = sb.bind(host, port).sync().channel();
            System.out.println("Web socket server started on " + host + ":" + port + " " + config);
            ch.closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(final String[] args) throws Exception {
        final String host =  args.length > 0 ? args[0] : "localhost";
        final int port =  args.length > 1 ? Integer.parseInt(args[1]) : 7777;
        final Config config = Config.path("/simplepush")
                .subprotocol("push-notification")
                .endpointUrl("/endpoint")
                .tls(args.length > 2 ? Boolean.parseBoolean(args[2]) : true)
                .userAgentReaperTimeout(args.length > 3 ? Long.parseLong(args[3]) : -1)
                .ackInterval(args.length > 4 ? Long.parseLong(args[4]) : 60000)
                .build();
        new NettyWebSocketServer(config, host, port).run();
    }

}
