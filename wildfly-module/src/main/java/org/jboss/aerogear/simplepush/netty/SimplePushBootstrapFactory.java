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
package org.jboss.aerogear.simplepush.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.sockjs.Config;
import io.netty.util.concurrent.DefaultEventExecutorGroup;

import java.util.concurrent.ThreadFactory;

import org.jboss.aerogear.netty.extension.api.ServerBootstrapFactory;
import org.jboss.aerogear.simplepush.server.DefaultSimplePushConfig;
import org.jboss.aerogear.simplepush.server.SimplePushServerConfig;
import org.jboss.aerogear.simplepush.server.datastore.DataStore;
import org.jboss.aerogear.simplepush.server.datastore.JpaDataStore;
import org.jboss.aerogear.simplepush.server.netty.SockJSChannelInitializer;
import org.jboss.as.network.SocketBinding;

public class SimplePushBootstrapFactory implements ServerBootstrapFactory {

    @Override
    public ServerBootstrap createServerBootstrap(final SocketBinding socketBinding, final ThreadFactory threadFactory) {
        final SimplePushServerConfig simplePushConfig = DefaultSimplePushConfig.defaultConfig();
        final Config sockjsConfig = Config.prefix("/simplepush")
                .websocketProtocols("push-notification")
                .tls(false)
                .cookiesNeeded()
                .build();
        final DefaultEventExecutorGroup reaperExcutorGroup = newEventExecutorGroup(1, threadFactory);
        final EventLoopGroup bossGroup = newEventLoopGroup(threadFactory);
        final EventLoopGroup workerGroup = newEventLoopGroup(threadFactory);
        final DataStore datastore = new JpaDataStore("SimplePushPU");
        final SockJSChannelInitializer channelInitializer = new SockJSChannelInitializer(simplePushConfig, datastore, sockjsConfig, reaperExcutorGroup);
        final ServerBootstrap sb = new ServerBootstrap();
        sb.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(channelInitializer);
        return sb;
    }

    private DefaultEventExecutorGroup newEventExecutorGroup(int i, ThreadFactory threadFactory) {
        if (threadFactory != null) {
            return new DefaultEventExecutorGroup(1, threadFactory);
        }
        return new DefaultEventExecutorGroup(1);
    }

    private EventLoopGroup newEventLoopGroup(final ThreadFactory threadFactory) {
        if (threadFactory != null) {
            return new NioEventLoopGroup(0, threadFactory);
        }
        return new NioEventLoopGroup();
    }

}
