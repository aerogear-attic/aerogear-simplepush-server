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

import org.jboss.aerogear.simplepush.server.datastore.DataStore;
import org.jboss.aerogear.simplepush.server.datastore.InMemoryDataStore;

public class NettyWebSocketServer {

    private final int port;
    private final Config config;

    public NettyWebSocketServer(final Config config , final int port) {
        this.port = port;
        this.config = config;
    }

    public void run() throws Exception {
        final DataStore datastore = new InMemoryDataStore();
        final EventLoopGroup bossGroup = new NioEventLoopGroup();
        final EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            final ServerBootstrap sb = new ServerBootstrap();
            sb.group(bossGroup, workerGroup)
            .channel(NioServerSocketChannel.class)
            .childHandler(new WebSocketChannelInitializer(config, datastore));
            final Channel ch = sb.bind(port).sync().channel();
            System.out.println("Web socket server started at port " + port);
            ch.closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(final String[] args) throws Exception {
        final int port =  args.length > 0 ? Integer.parseInt(args[0]) : 7777;
        final boolean transportLayerSecurity =  args.length > 1 ? Boolean.parseBoolean(args[1]) : true;
        final Config config = Config.path("simplepush").subprotocol("push-notification").endpointUrl("/endpoint").tls(transportLayerSecurity).build();
        new NettyWebSocketServer(config, port).run();
    }

}
