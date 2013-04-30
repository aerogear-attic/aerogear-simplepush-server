package org.jboss.aerogear.simplepush.server.netty;

import static io.netty.handler.codec.http.HttpHeaders.Values.WEBSOCKET;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.jboss.aerogear.simplepush.protocol.Ack;
import org.jboss.aerogear.simplepush.protocol.MessageType;
import org.jboss.aerogear.simplepush.protocol.Update;
import org.jboss.aerogear.simplepush.protocol.impl.AckImpl;
import org.jboss.aerogear.simplepush.protocol.impl.HandshakeImpl;
import org.jboss.aerogear.simplepush.protocol.impl.HandshakeResponseImpl;
import org.jboss.aerogear.simplepush.protocol.impl.NotificationImpl;
import org.jboss.aerogear.simplepush.protocol.impl.RegisterImpl;
import org.jboss.aerogear.simplepush.protocol.impl.RegisterResponseImpl;
import org.jboss.aerogear.simplepush.protocol.impl.UnregisterImpl;
import org.jboss.aerogear.simplepush.protocol.impl.UnregisterResponseImpl;
import org.jboss.aerogear.simplepush.protocol.impl.UpdateImpl;
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
        final HttpResponse res = handleHttpRequest(createHttpChannel(), websocketUpgradeRequest());
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
    
    @Test
    public void register() throws Exception {
        handleWebSocketTextFrame(helloFrame(UUIDUtil.newUAID()), HandshakeResponseImpl.class);
        final String channelId = UUID.randomUUID().toString();
        final RegisterResponseImpl response = handleWebSocketTextFrame(registerFrame(channelId), RegisterResponseImpl.class);
        assertThat(response.getMessageType(), equalTo(MessageType.Type.REGISTER));
        assertThat(response.getChannelId(), equalTo(channelId));
        assertThat(response.getStatus().getCode(), equalTo(200));
        assertThat(response.getPushEndpoint(), equalTo("/endpoint/" + channelId));
    }
    
    @Test
    public void registerDuplicateChannelId() throws Exception {
        handleWebSocketTextFrame(helloFrame(UUIDUtil.newUAID()), HandshakeResponseImpl.class);
        final String channelId = UUID.randomUUID().toString();
        handleWebSocketTextFrame(registerFrame(channelId), RegisterResponseImpl.class);
        final RegisterResponseImpl response = handleWebSocketTextFrame(registerFrame(channelId), RegisterResponseImpl.class);
        assertThat(response.getMessageType(), equalTo(MessageType.Type.REGISTER));
        assertThat(response.getChannelId(), equalTo(channelId));
        assertThat(response.getStatus().getCode(), equalTo(409));
        assertThat(response.getPushEndpoint(), equalTo("/endpoint/"+ channelId));
    }
    
    @Test
    public void unregister() throws Exception {
        handleWebSocketTextFrame(helloFrame(UUIDUtil.newUAID()), HandshakeResponseImpl.class);
        final String channelId = UUID.randomUUID().toString();
        handleWebSocketTextFrame(registerFrame(channelId), RegisterResponseImpl.class);
        final UnregisterResponseImpl response = handleWebSocketTextFrame(unregisterFrame(channelId), UnregisterResponseImpl.class);
        assertThat(response.getMessageType(), equalTo(MessageType.Type.UNREGISTER));
        assertThat(response.getChannelId(), equalTo(channelId));
        assertThat(response.getStatus().getCode(), equalTo(200));
    }
    
    @Test
    public void unregisterNonRegistered() throws Exception {
        handleWebSocketTextFrame(helloFrame(UUIDUtil.newUAID()), HandshakeResponseImpl.class);
        final String channelId = UUID.randomUUID().toString();
        handleWebSocketTextFrame(registerFrame(channelId), RegisterResponseImpl.class);
        final UnregisterResponseImpl response = handleWebSocketTextFrame(unregisterFrame("notRegistered"), UnregisterResponseImpl.class);
        assertThat(response.getMessageType(), equalTo(MessageType.Type.UNREGISTER));
        assertThat(response.getChannelId(), equalTo("notRegistered"));
        assertThat(response.getStatus().getCode(), equalTo(200));
    }
    
    @Test
    public void unregisterRegistered() throws Exception {
        handleWebSocketTextFrame(helloFrame(UUIDUtil.newUAID()), HandshakeResponseImpl.class);
        final String channelId = UUID.randomUUID().toString();
        handleWebSocketTextFrame(registerFrame(channelId), RegisterResponseImpl.class);
        final UnregisterResponseImpl response = handleWebSocketTextFrame(unregisterFrame("notRegistered"), UnregisterResponseImpl.class);
        assertThat(response.getMessageType(), equalTo(MessageType.Type.UNREGISTER));
        assertThat(response.getChannelId(), equalTo("notRegistered"));
        assertThat(response.getStatus().getCode(), equalTo(200));
    }
    
    @Test
    public void notification() throws Exception {
        handleWebSocketTextFrame(helloFrame(UUIDUtil.newUAID()), HandshakeResponseImpl.class);
        final String channelId = UUID.randomUUID().toString();
        handleWebSocketTextFrame(registerFrame(channelId), RegisterResponseImpl.class);
        
        final HttpResponse response = handleHttpRequest(createHttpChannel(), notification(channelId, 1L));
        assertThat(response.getStatus().code(), equalTo(200));
    }
    
    @Test
    public void notificationWithAck() throws Exception {
        handleWebSocketTextFrame(helloFrame(UUIDUtil.newUAID()), HandshakeResponseImpl.class);
        final String channelId = UUID.randomUUID().toString();
        handleWebSocketTextFrame(registerFrame(channelId), RegisterResponseImpl.class);
        
        final HttpResponse response = handleHttpRequest(createHttpChannel(), notification(channelId, 1L));
        assertThat(response.getStatus().code(), equalTo(200));
        
        final Set<String> updates = new HashSet<String>(Arrays.asList(channelId));
        handleWebSocketTextFrame(ackFrame(updates), AckImpl.class);
    }
    
    @Test
    public void notificationWithVersionEqualToCurrentShouldReturn400() throws Exception {
        handleWebSocketTextFrame(helloFrame(UUIDUtil.newUAID()), HandshakeResponseImpl.class);
        final String channelId = UUID.randomUUID().toString();
        handleWebSocketTextFrame(registerFrame(channelId), RegisterResponseImpl.class);
        
        final FullHttpRequest notification = notification(channelId, 1L);
        HttpResponse response = handleHttpRequest(createHttpChannel(), notification);
        assertThat(response.getStatus().code(), equalTo(200));
        
        final FullHttpRequest invalidVersion = notification(channelId, 1L);
        response = handleHttpRequest(createHttpChannel(), invalidVersion);
        assertThat(response.getStatus().code(), equalTo(400));
        assertThat(response.getStatus().reasonPhrase(), equalTo("Bad Request"));
    }
    
    @Test
    public void notificationWithVersionLessThanCurrentShouldReturn400() throws Exception {
        handleWebSocketTextFrame(helloFrame(UUIDUtil.newUAID()), HandshakeResponseImpl.class);
        final String channelId = UUID.randomUUID().toString();
        handleWebSocketTextFrame(registerFrame(channelId), RegisterResponseImpl.class);
        
        final FullHttpRequest notification = notification(channelId, 10L);
        HttpResponse response = handleHttpRequest(createHttpChannel(), notification);
        assertThat(response.getStatus().code(), equalTo(200));
        
        final FullHttpRequest invalidVersion = notification(channelId, 9L);
        response = handleHttpRequest(createHttpChannel(), invalidVersion);
        assertThat(response.getStatus().code(), equalTo(400));
        assertThat(response.getStatus().reasonPhrase(), equalTo("Bad Request"));
    }
    
    private TextWebSocketFrame helloFrame(final UUID uaid) {
        final TextWebSocketFrame frame = mock(TextWebSocketFrame.class);
        when(frame.text()).thenReturn(JsonUtil.toJson(new HandshakeImpl(uaid.toString())));
        return frame;
    }
    
    private TextWebSocketFrame registerFrame(final String channelId) {
        final TextWebSocketFrame frame = mock(TextWebSocketFrame.class);
        when(frame.text()).thenReturn(JsonUtil.toJson(new RegisterImpl(channelId)));
        return frame;
    }
    
    private TextWebSocketFrame unregisterFrame(final String channelId) {
        final TextWebSocketFrame frame = mock(TextWebSocketFrame.class);
        when(frame.text()).thenReturn(JsonUtil.toJson(new UnregisterImpl(channelId)));
        return frame;
    }
    
    private TextWebSocketFrame ackFrame(final Set<String> updates) {
        final TextWebSocketFrame frame = mock(TextWebSocketFrame.class);
        when(frame.text()).thenReturn(JsonUtil.toJson(new AckImpl(updates)));
        return frame;
    }
    
    private <T> T handleWebSocketTextFrame(final TextWebSocketFrame frame,  final Class<T> type) throws Exception {
        final EmbeddedByteChannel channel = createHttpChannel();
        // perform upgrade request. This will add websocket encoder/decoder to the pipeline
        handleHttpRequest(channel, websocketUpgradeRequest());
        final StubFrameEncoder stubFrameEncoder = new StubFrameEncoder();
        channel.pipeline().replace(WebSocket13FrameEncoder.class, "wsencoder", stubFrameEncoder);
        wsHandler.handleWebSocketFrame(channel, frame);
        return stubFrameEncoder.payloadAsType(type);
    }
    
    private HttpResponse handleHttpRequest(final EmbeddedByteChannel channel, final FullHttpRequest req) throws Exception {
        wsHandler.handleHttpRequest(channel, req);
        // make the callable notification task run
        channel.runPendingTasks();
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
    
    private FullHttpRequest notification(final String channelId, final Long version) {
        final FullHttpRequest req = new DefaultFullHttpRequest(HTTP_1_1, HttpMethod.PUT, "/endpoint/" + channelId);
        req.data().writeBytes(Unpooled.copiedBuffer("version=" + version.toString(), CharsetUtil.UTF_8));
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
            if (frame.data() != null) {
                return getTextFrame().data().toString(CharsetUtil.UTF_8);
            }
            return null;
        }
        
        public <T> T payloadAsType(final Class<T> type) {
            if (frame != null) {
                return JsonUtil.fromJson(payload(), type);
            }
            return null;
            
        }
        
    }
    
}
