package org.jboss.aerogear.simplepush.server.netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.sockjs.Config;
import io.netty.handler.codec.sockjs.SockJSServiceFactory;
import io.netty.handler.codec.sockjs.handlers.CorsInboundHandler;
import io.netty.handler.codec.sockjs.handlers.CorsOutboundHandler;
import io.netty.handler.codec.sockjs.handlers.SockJSHandler;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.concurrent.EventExecutorGroup;

import javax.net.ssl.SSLEngine;

import org.jboss.aerogear.simplepush.server.DefaultSimplePushServer;
import org.jboss.aerogear.simplepush.server.datastore.DataStore;

public class SockJSChannelInitializer extends ChannelInitializer<SocketChannel> {
    
    private final DataStore datastore;
    private final SimplePushConfig config;
    private final EventExecutorGroup backgroundGroup;
    private final SockJSServiceFactory serviceFactory;
    
    public SockJSChannelInitializer(final SimplePushConfig simplePushConfig, 
            final DataStore datastore,
            final Config sockJSConfig,
            final EventExecutorGroup backgroundGroup) {
        this.config = simplePushConfig;
        this.datastore = datastore;
        this.backgroundGroup = backgroundGroup;
        this.serviceFactory = new SimplePushServiceFactory(sockJSConfig, datastore, config);
    }

    @Override
    protected void initChannel(final SocketChannel socketChannel) throws Exception {
        final ChannelPipeline pipeline = socketChannel.pipeline();
        if (config.tls()) {
            final SSLEngine engine = WebSocketSslServerSslContext.getInstance().serverContext().createSSLEngine();
            engine.setUseClientMode(false);
            pipeline.addLast(new SslHandler(engine));
        }
        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new HttpObjectAggregator(65536));
        
        final DefaultSimplePushServer simplePushServer = new DefaultSimplePushServer(datastore);
        pipeline.addLast(new NotificationHandler(config, simplePushServer));
        pipeline.addLast(new CorsInboundHandler());
        pipeline.addLast(new SockJSHandler(serviceFactory));
        pipeline.addLast(backgroundGroup, new ReaperHandler(config));
        pipeline.addLast(new CorsOutboundHandler());
    }
    
}
