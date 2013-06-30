package org.jboss.aerogear.simplepush.server.netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.sockjs.Config;
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
    private final SimplePushConfig simplePushConfig;
    private final EventExecutorGroup backgroundGroup;
    private final Config sockjsConfig;
    
    public SockJSChannelInitializer(final SimplePushConfig simplePushConfig, 
            final DataStore datastore,
            final Config sockjsConfig,
            final EventExecutorGroup backgroundGroup) {
        this.simplePushConfig = simplePushConfig;
        this.datastore = datastore;
        this.sockjsConfig = sockjsConfig;
        this.backgroundGroup = backgroundGroup;
    }

    @Override
    protected void initChannel(final SocketChannel socketChannel) throws Exception {
        final ChannelPipeline pipeline = socketChannel.pipeline();
        if (sockjsConfig.tls()) {
            final SSLEngine engine = WebSocketSslServerSslContext.getInstance().serverContext().createSSLEngine();
            engine.setUseClientMode(false);
            pipeline.addLast(new SslHandler(engine));
        }
        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new HttpObjectAggregator(65536));
        
        final DefaultSimplePushServer simplePushServer = new DefaultSimplePushServer(datastore);
        pipeline.addLast(new NotificationHandler(simplePushConfig, simplePushServer));
        pipeline.addLast(new CorsInboundHandler());
        pipeline.addLast(new SockJSHandler(new SimplePushServiceFactory(sockjsConfig, datastore, simplePushConfig)));
        pipeline.addLast(backgroundGroup, new ReaperHandler(simplePushConfig));
        pipeline.addLast(new CorsOutboundHandler());
    }
    
}
