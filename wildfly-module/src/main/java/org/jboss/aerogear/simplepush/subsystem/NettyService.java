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
    private final String factoryClass;
    private final String tokenKey;
    private Channel channel;


    public NettyService(final String name, final String factoryClass, final String tokenKey) {
        this.name = name;
        this.factoryClass = factoryClass;
        this.tokenKey = tokenKey;
    }

    @Override
    public synchronized  void start(final StartContext context) throws StartException {
        try {
            final ThreadFactory threadFactory = injectedThreadFactory.getOptionalValue();
            final SocketBinding socketBinding = injectedSocketBinding.getValue();
            final ServerBootstrap serverBootstrap = createServerBootstrap(factoryClass, socketBinding, threadFactory);
            logger.info("NettyService [" + name + "] binding to port [" + socketBinding.getPort() + "]");
            channel = serverBootstrap.bind(socketBinding.getAddress(), socketBinding.getPort()).sync().channel();
        } catch (final InterruptedException e) {
            throw new StartException(e);
        }
    }

    private ServerBootstrap createServerBootstrap(final String factoryClass,
            final SocketBinding socketBinding,
            final ThreadFactory threadFactory) throws StartException {
        try {
            final Class<?> type = Class.forName(factoryClass);
            final ServerBootstrapFactory factory = (ServerBootstrapFactory) type.newInstance();
            return factory.createServerBootstrap(socketBinding, threadFactory, tokenKey);
        } catch (final Exception e) {
            throw new StartException(e.getMessage(), e);
        }
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
