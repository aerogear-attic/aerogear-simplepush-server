package org.jboss.aerogear.simplepush.server.netty;

import static io.netty.handler.codec.http.HttpHeaders.Values.WEBSOCKET;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.embedded.EmbeddedByteChannel;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders.Names;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocket13FrameEncoder;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.CharsetUtil;

import java.util.UUID;

import org.jboss.aerogear.simplepush.protocol.MessageType;
import org.jboss.aerogear.simplepush.protocol.impl.HandshakeImpl;
import org.jboss.aerogear.simplepush.protocol.impl.HandshakeResponseImpl;
import org.jboss.aerogear.simplepush.protocol.impl.RegisterImpl;
import org.jboss.aerogear.simplepush.protocol.impl.json.JsonUtil;
import org.jboss.aerogear.simplepush.util.UUIDUtil;
import org.junit.Before;
import org.junit.Test;

public class WebSocketServerHandlerTest {
    
    private WebSocketServerHandler wsHandler;
    
    @Before
    public void setup() {
        wsHandler = new WebSocketServerHandler("simplepush", "push-notification", "/endpoint");
    }

    @Test
    public void websocketUpgradeWithSubProtocol() throws Exception {
        final HttpResponse res = handleHttpUgradeRequest(createHttpChannel());
        assertThat(res.headers().get(Names.SEC_WEBSOCKET_PROTOCOL), equalTo("push-notification"));
        assertThat(res.headers().get(Names.SEC_WEBSOCKET_ACCEPT), equalTo("s3pPLMBiTxaQ9kYGzzhZRbK+xOo="));
    }
    
    @Test
    public void hello() throws Exception {
        final UUID uaid = UUIDUtil.newUAID();
        final HandshakeResponseImpl response = handleWebSocketTextFrame(helloFrame(uaid), HandshakeResponseImpl.class);
        assertThat(response.getMessageType(), equalTo(MessageType.Type.HELLO));
        assertThat(response.getUAID(), equalTo(uaid));
    }
    
    private TextWebSocketFrame helloFrame(final UUID uaid) {
        final TextWebSocketFrame helloFrame = mock(TextWebSocketFrame.class);
        when(helloFrame.text()).thenReturn(JsonUtil.toJson(new HandshakeImpl(uaid.toString())));
        return helloFrame;
    }
    
    private TextWebSocketFrame registerFrame(final String channelId) {
        final TextWebSocketFrame registerFrame = mock(TextWebSocketFrame.class);
        when(registerFrame.text()).thenReturn(JsonUtil.toJson(new RegisterImpl(channelId)));
        return registerFrame;
    }
    
    private <T> T handleWebSocketTextFrame(final TextWebSocketFrame frame,  final Class<T> type) throws Exception {
        final EmbeddedByteChannel channel = createHttpChannel();
        // perform upgrade request. This will add websocket encoder/decoder to the pipeline
        handleHttpUgradeRequest(channel);
        final StubFrameEncoder stubFrameEncoder = new StubFrameEncoder();
        channel.pipeline().replace(WebSocket13FrameEncoder.class, "wsencoder", stubFrameEncoder);
        wsHandler.handleWebSocketFrame(channel, frame);
        return stubFrameEncoder.payloadAsType(type);
    }
    
    private HttpResponse handleHttpUgradeRequest(final EmbeddedByteChannel channel) throws Exception {
        final FullHttpRequest req = websocketUpgradeRequest();
        wsHandler.handleHttpRequest(channel, req);
        final EmbeddedByteChannel responseChannel = new EmbeddedByteChannel(new HttpResponseDecoder());
        responseChannel.writeInbound(channel.readOutbound());
        final HttpResponse res = (HttpResponse) responseChannel.readInbound();
        return res;
    }
    
    private EmbeddedByteChannel createHttpChannel() throws Exception {
        return new EmbeddedByteChannel(
                new HttpObjectAggregator(42), 
                new HttpRequestDecoder(), 
                new HttpResponseEncoder());
    }
    
    private FullHttpRequest websocketUpgradeRequest() {
        final FullHttpRequest req = new DefaultFullHttpRequest(HTTP_1_1, HttpMethod.GET, "/simplepush");
        req.headers().set(Names.HOST, "server.test.com");
        req.headers().set(Names.UPGRADE, WEBSOCKET.toLowerCase());
        req.headers().set(Names.CONNECTION, "Upgrade");
        req.headers().set(Names.SEC_WEBSOCKET_KEY, "dGhlIHNhbXBsZSBub25jZQ==");
        req.headers().set(Names.SEC_WEBSOCKET_ORIGIN, "http://test.com");
        req.headers().set(Names.SEC_WEBSOCKET_PROTOCOL, "push-notification");
        req.headers().set(Names.SEC_WEBSOCKET_VERSION, "13");
        return req;
    }
    
    public static class StubFrameEncoder extends MessageToByteEncoder<WebSocketFrame> {
        
        private WebSocketFrame frame;

        @Override
        protected void encode(ChannelHandlerContext ctx, WebSocketFrame msg, ByteBuf out) throws Exception {
            this.frame = msg;
            frame.retain();
        }
        
        public TextWebSocketFrame getTextFrame() {
            return (TextWebSocketFrame) frame;
        }
        
        public String payload() {
            return getTextFrame().data().toString(CharsetUtil.UTF_8);
        }
        
        public <T> T payloadAsType(final Class<T> type) {
            return JsonUtil.fromJson(payload(), type);
        }
        
    }
    
}
