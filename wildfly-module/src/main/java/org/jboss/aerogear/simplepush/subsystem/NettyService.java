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

import java.util.concurrent.ThreadFactory;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.jboss.aerogear.io.netty.handler.codec.sockjs.SockJsConfig;
import io.netty.util.concurrent.DefaultEventExecutorGroup;

import org.jboss.aerogear.simplepush.server.DefaultSimplePushConfig;
import org.jboss.aerogear.simplepush.server.SimplePushServerConfig;
import org.jboss.aerogear.simplepush.server.datastore.DataStore;
import org.jboss.aerogear.simplepush.server.datastore.JpaDataStore;
import org.jboss.aerogear.simplepush.server.netty.SockJSChannelInitializer;
import org.jboss.as.network.SocketBinding;
import org.jboss.logging.Logger;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

public class NettyService implements Service<NettyService> {

    private final Logger logger = Logger.getLogger(NettyService.class);

    private final InjectedValue<SocketBinding> injectedSocketBinding = new InjectedValue<SocketBinding>();
    private final InjectedValue<ThreadFactory> injectedThreadFactory = new InjectedValue<ThreadFactory>();
    private final String name;
    private final String tokenKey;
    private final boolean endpointTls;
    private Channel channel;



    public NettyService(final String name, final String tokenKey, final boolean endpointTls) {
        this.name = name;
        this.tokenKey = tokenKey;
        this.endpointTls = endpointTls;
    }

    @Override
    public synchronized void start(final StartContext context) throws StartException {
        try {
            final ThreadFactory threadFactory = injectedThreadFactory.getOptionalValue();
            final SocketBinding socketBinding = injectedSocketBinding.getValue();
            final SimplePushServerConfig simplePushConfig = createConfig(socketBinding, tokenKey, endpointTls);
            final SockJsConfig sockjsConfig = SockJsConfig.withPrefix("/simplepush")
                    .webSocketProtocols("push-notification")
                    .tls(false)
                    .webSocketHeartbeatInterval(180000)
                    .cookiesNeeded()
                    .build();
            final DefaultEventExecutorGroup reaperExcutorGroup = newEventExecutorGroup(1, threadFactory);
            final EventLoopGroup bossGroup = newEventLoopGroup(threadFactory);
            final EventLoopGroup workerGroup = newEventLoopGroup(threadFactory);
            final DataStore datastore = new JpaDataStore("SimplePushPU");
            final SockJSChannelInitializer channelInitializer = new SockJSChannelInitializer(simplePushConfig, datastore, sockjsConfig, reaperExcutorGroup);
            final ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(channelInitializer);
            logger.info("NettyService [" + name + "] binding to port [" + socketBinding.getPort() + "]");
            channel = serverBootstrap.bind(socketBinding.getAddress(), socketBinding.getPort()).sync().channel();
        } catch (final InterruptedException e) {
            throw new StartException(e);
        }
    }

    /*
     * This OpenShift specific code will be removed when the SimplePush subsystem supports configuration
     * options.
     */
    private SimplePushServerConfig createConfig(final SocketBinding socketBinding, final String tokenKey, final boolean endpointTls) {
        final String openShiftAppDNS = System.getenv("OPENSHIFT_APP_DNS");
        final String hostName = openShiftAppDNS == null ? socketBinding.getAddress().getHostName() : openShiftAppDNS;
        final int port = openShiftAppDNS == null ? socketBinding.getPort() : endpointTls ? 8443 : 8000;
        return DefaultSimplePushConfig.create(hostName, port).tokenKey(tokenKey).useTls(endpointTls).build();
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

    @Override
    public synchronized void stop(StopContext context) {
        logger.info("NettyService [" + name + "] shutting down.");
        channel.eventLoop().shutdownGracefully();
    }

    public InjectedValue<SocketBinding> getInjectedSocketBinding() {
        return injectedSocketBinding;
    }

    public InjectedValue<ThreadFactory> getInjectedThreadFactory() {
        return injectedThreadFactory;
    }

    @Override
    public NettyService getValue() throws IllegalStateException, IllegalArgumentException {
        return this;
    }

    public static ServiceName createServiceName(final String name) {
        return ServiceName.JBOSS.append("netty", name);
    }

}
