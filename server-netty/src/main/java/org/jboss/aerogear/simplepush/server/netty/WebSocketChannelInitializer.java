package org.jboss.aerogear.simplepush.server.netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.concurrent.EventExecutorGroup;

import javax.net.ssl.SSLEngine;

import org.jboss.aerogear.simplepush.server.DefaultSimplePushServer;
import org.jboss.aerogear.simplepush.server.datastore.DataStore;

public class WebSocketChannelInitializer extends ChannelInitializer<SocketChannel> {
    
    private final DataStore datastore;
    private final Config config;
    private final EventExecutorGroup backgroundGroup;
    
    public WebSocketChannelInitializer(final Config config, final DataStore datastore, final EventExecutorGroup backgroundGroup) {
        this.config = config;
        this.datastore = datastore;
        this.backgroundGroup = backgroundGroup;
    }

    @Override
    protected void initChannel(final SocketChannel socketChannel) throws Exception {
        final ChannelPipeline pipeline = socketChannel.pipeline();
        if (config.tls()) {
            final SSLEngine engine = WebSocketSslServerSslContext.getInstance().serverContext().createSSLEngine();
            engine.setUseClientMode(false);
            pipeline.addLast("ssl", new SslHandler(engine));
        }
        pipeline.addLast("codec-http", new HttpServerCodec());
        pipeline.addLast("aggregator", new HttpObjectAggregator(65536));
        pipeline.addLast("handler", new WebSocketServerHandler(config, new DefaultSimplePushServer(datastore)));
        pipeline.addLast(backgroundGroup, new ReaperHandler(config));
    }

}
