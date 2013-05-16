/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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

import java.util.concurrent.ThreadFactory;

import org.jboss.aerogear.netty.extension.api.ServerBootstrapFactory;
import org.jboss.aerogear.simplepush.server.datastore.DataStore;
import org.jboss.aerogear.simplepush.server.datastore.InMemoryDataStore;
import org.jboss.aerogear.simplepush.server.netty.Config;
import org.jboss.aerogear.simplepush.server.netty.WebSocketChannelInitializer;
import org.jboss.as.network.SocketBinding;

public class SimplePushBootstrapFactory implements ServerBootstrapFactory {

    @Override
    public ServerBootstrap createServerBootstrap(final SocketBinding socketBinding, final ThreadFactory threadFactory) {
        final Config config = Config.path("simplepush").subprotocol("push-notification").endpointUrl("/endpoint").tls(false).build();
        final DataStore datastore = new InMemoryDataStore();
        final WebSocketChannelInitializer channelInitializer = new WebSocketChannelInitializer(config, datastore);
        final EventLoopGroup bossGroup = newEventLoopGroup(threadFactory);
        final EventLoopGroup workerGroup = newEventLoopGroup(threadFactory);
        final ServerBootstrap sb = new ServerBootstrap();
        sb.group(bossGroup, workerGroup)
        .channel(NioServerSocketChannel.class)
        .childHandler(channelInitializer);
        return sb;
    }
    
    private EventLoopGroup newEventLoopGroup(final ThreadFactory threadFactory) {
        if (threadFactory == null) {
            return new NioEventLoopGroup(NioEventLoopGroup.DEFAULT_EVENT_LOOP_THREADS, threadFactory);
        } else {
            return new NioEventLoopGroup();
        }
    }

}
