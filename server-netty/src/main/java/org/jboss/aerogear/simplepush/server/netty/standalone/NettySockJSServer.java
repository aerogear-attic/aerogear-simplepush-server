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
package org.jboss.aerogear.simplepush.server.netty.standalone;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.DefaultEventExecutorGroup;

import org.jboss.aerogear.simplepush.server.SimplePushServerConfig;
import org.jboss.aerogear.simplepush.server.netty.SockJSChannelInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A standalone Netty SockJS SimplePush Server.
 */
public class NettySockJSServer {

    private final Logger logger = LoggerFactory.getLogger(NettySockJSServer.class);

    private final StandaloneConfig config;

    public NettySockJSServer(final StandaloneConfig standaloneConfig) {
        this.config = standaloneConfig;
    }

    public void run() throws Exception {
        final EventLoopGroup bossGroup = new NioEventLoopGroup();
        final EventLoopGroup workerGroup = new NioEventLoopGroup();
        final DefaultEventExecutorGroup reaperExcutorGroup = new DefaultEventExecutorGroup(1);
        final SimplePushServerConfig simplePushConfig = config.simplePushServerConfig();
        try {
            final ServerBootstrap sb = new ServerBootstrap();
            sb.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new SockJSChannelInitializer(simplePushConfig, config.dataStore(), config.sockJsConfig(), reaperExcutorGroup));
            final Channel ch = sb.bind(simplePushConfig.host(), simplePushConfig.port()).sync().channel();
            logger.info("Server started");
            logger.debug(config.toString());
            ch.closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    /**
     * Starts the server using an optional passed in configuration file.
     * </p>
     * Options:
     * <pre>
     * path/to/config.json
     * </pre>
     *
     * @param args the command line arguments passed.
     * @throws Exception
     */
    public static void main(final String[] args) throws Exception {
        final String configFile = args.length == 1 ? args[0]: "/simplepush-config.json";
        final StandaloneConfig config = ConfigReader.parse(configFile);
        new NettySockJSServer(config).run();
    }

}
