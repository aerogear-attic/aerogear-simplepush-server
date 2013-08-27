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

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.sockjs.SockJsConfig;
import io.netty.handler.codec.sockjs.handlers.CorsInboundHandler;
import io.netty.handler.codec.sockjs.handlers.CorsOutboundHandler;
import io.netty.handler.codec.sockjs.handlers.SockJsHandler;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.concurrent.EventExecutorGroup;

import javax.net.ssl.SSLEngine;

import org.jboss.aerogear.simplepush.server.DefaultSimplePushServer;
import org.jboss.aerogear.simplepush.server.SimplePushServerConfig;
import org.jboss.aerogear.simplepush.server.datastore.DataStore;

/**
 * The Netty {@link ChannelInitializer} for the SimplePush Server.
 */
public class SockJSChannelInitializer extends ChannelInitializer<SocketChannel> {

    private final DataStore datastore;
    private final SimplePushServerConfig simplePushConfig;
    private final EventExecutorGroup backgroundGroup;
    private final SockJsConfig sockjsConfig;

    /**
     * Sole constructor.
     *
     * @param simplePushConfig the {@link SimplePushServerConfig} configuration.
     * @param datastore the {@link DataStore} to be passed to the {@link SimplePushServiceFactory}.
     * @param sockjsConfig the SockJS {@link Config}.
     * @param backgroundGroup an {@link EventExecutorGroup} to be used for the {@link UserAgentReaperHandler}.
     */
    public SockJSChannelInitializer(final SimplePushServerConfig simplePushConfig,
            final DataStore datastore,
            final SockJsConfig sockjsConfig,
            final EventExecutorGroup backgroundGroup) {
        this.simplePushConfig = simplePushConfig;
        this.datastore = datastore;
        this.sockjsConfig = sockjsConfig;
        this.backgroundGroup = backgroundGroup;
    }

    @Override
    protected void initChannel(final SocketChannel socketChannel) throws Exception {
        final ChannelPipeline pipeline = socketChannel.pipeline();
        if (sockjsConfig.isTls()) {
            final SSLEngine engine = WebSocketSslServerSslContext.getInstance().serverContext().createSSLEngine();
            engine.setUseClientMode(false);
            pipeline.addLast(new SslHandler(engine));
        }
        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new HttpObjectAggregator(65536));

        final DefaultSimplePushServer simplePushServer = new DefaultSimplePushServer(datastore, simplePushConfig);
        pipeline.addLast(new NotificationHandler(simplePushServer));
        pipeline.addLast(new CorsInboundHandler());
        pipeline.addLast(new SockJsHandler(new SimplePushServiceFactory(sockjsConfig, datastore, simplePushConfig)));
        pipeline.addLast(backgroundGroup, new UserAgentReaperHandler(simplePushServer));
        pipeline.addLast(new CorsOutboundHandler());
    }

}
