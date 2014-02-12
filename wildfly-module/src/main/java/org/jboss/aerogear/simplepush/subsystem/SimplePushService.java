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

package org.jboss.aerogear.simplepush.subsystem;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.DefaultEventExecutorGroup;

import org.jboss.aerogear.io.netty.handler.codec.sockjs.SockJsConfig;
import org.jboss.aerogear.simplepush.server.DefaultSimplePushConfig.Builder;
import org.jboss.aerogear.simplepush.server.SimplePushServerConfig;
import org.jboss.aerogear.simplepush.server.datastore.DataStore;
import org.jboss.aerogear.simplepush.server.netty.SockJSChannelInitializer;
import org.jboss.as.network.SocketBinding;
import org.jboss.logging.Logger;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

public class SimplePushService implements Service<SimplePushService> {

    private final Logger logger = Logger.getLogger(SimplePushService.class);

    private final InjectedValue<SocketBinding> injectedSocketBinding = new InjectedValue<SocketBinding>();
    private final InjectedValue<SocketBinding> injectedNotificationSocketBinding = new InjectedValue<SocketBinding>();
    private final InjectedValue<DataStore> injectedDataStore = new InjectedValue<DataStore>();
    private final Builder simplePushConfig;
    private final SockJsConfig sockJsConfig;
    private Channel channel;

    public SimplePushService(final Builder simplePushConfig, final SockJsConfig sockJsConfig) {
        this.simplePushConfig = simplePushConfig;
        this.sockJsConfig = sockJsConfig;
    }

    @Override
    public synchronized void start(final StartContext context) throws StartException {
        try {
            final SocketBinding notificationSocketBinding = injectedNotificationSocketBinding.getOptionalValue();
            if (notificationSocketBinding != null) {
                simplePushConfig.endpointHost(notificationSocketBinding.getSocketAddress().getHostName());
                simplePushConfig.endpointPort(notificationSocketBinding.getPort());
            }

            final DefaultEventExecutorGroup reaperExcutorGroup = new DefaultEventExecutorGroup(1);
            final DataStore datastore = injectedDataStore.getValue();
            final SimplePushServerConfig simplePushServerConfig = simplePushConfig.build();
            final ServerBootstrap serverBootstrap = new ServerBootstrap()
                .group(new NioEventLoopGroup(), new NioEventLoopGroup())
                .channel(NioServerSocketChannel.class)
                .childHandler(new SockJSChannelInitializer(simplePushServerConfig, datastore, sockJsConfig, reaperExcutorGroup));

            final SocketBinding socketBinding = injectedSocketBinding.getValue();
            logger.info("SimplePush Server binding to [" + socketBinding.getAddress() + ":" + socketBinding.getPort() + "]");
            channel = serverBootstrap.bind(socketBinding.getAddress(), socketBinding.getPort()).sync().channel();
        } catch (final Exception e) {
            throw new StartException(e);
        }
    }

    @Override
    public synchronized void stop(StopContext context) {
        logger.info("SimplePush Server shutting down.");
        channel.eventLoop().shutdownGracefully();
    }

    public InjectedValue<SocketBinding> getInjectedSocketBinding() {
        return injectedSocketBinding;
    }

    public InjectedValue<SocketBinding> getInjectedNotificationSocketBinding() {
        return injectedNotificationSocketBinding;
    }

    public InjectedValue<DataStore> getInjectedDataStore() {
        return injectedDataStore;
    }

    @Override
    public SimplePushService getValue() throws IllegalStateException, IllegalArgumentException {
        return this;
    }

    public static ServiceName createServiceName(final String name) {
        return ServiceName.JBOSS.append("aerogear", name);
    }

}
