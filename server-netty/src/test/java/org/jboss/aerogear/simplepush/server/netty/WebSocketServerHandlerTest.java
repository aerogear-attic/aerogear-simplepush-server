package org.jboss.aerogear.simplepush.server.netty;

import static io.netty.handler.codec.http.HttpHeaders.Values.WEBSOCKET;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;
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
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocket13FrameDecoder;
import io.netty.handler.codec.http.websocketx.WebSocket13FrameEncoder;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.AbstractEventExecutor;
import io.netty.util.concurrent.ScheduledFuture;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.jboss.aerogear.simplepush.protocol.HandshakeResponse;
import org.jboss.aerogear.simplepush.protocol.MessageType;
import org.jboss.aerogear.simplepush.protocol.NotificationMessage;
import org.jboss.aerogear.simplepush.protocol.RegisterResponse;
import org.jboss.aerogear.simplepush.protocol.UnregisterResponse;
import org.jboss.aerogear.simplepush.protocol.Update;
import org.jboss.aerogear.simplepush.protocol.impl.AckMessageImpl;
import org.jboss.aerogear.simplepush.protocol.impl.HandshakeMessageImpl;
import org.jboss.aerogear.simplepush.protocol.impl.HandshakeResponseImpl;
import org.jboss.aerogear.simplepush.protocol.impl.NotificationMessageImpl;
import org.jboss.aerogear.simplepush.protocol.impl.RegisterImpl;
import org.jboss.aerogear.simplepush.protocol.impl.RegisterResponseImpl;
import org.jboss.aerogear.simplepush.protocol.impl.UnregisterMessageImpl;
import org.jboss.aerogear.simplepush.protocol.impl.UnregisterResponseImpl;
import org.jboss.aerogear.simplepush.protocol.impl.UpdateImpl;
import org.jboss.aerogear.simplepush.protocol.impl.json.JsonUtil;
import org.jboss.aerogear.simplepush.server.DefaultSimplePushServer;
import org.jboss.aerogear.simplepush.server.datastore.InMemoryDataStore;
import org.jboss.aerogear.simplepush.util.UUIDUtil;
import org.junit.Before;
import org.junit.Test;

public class WebSocketServerHandlerTest {
    
    private WebSocketServerHandler wsHandler;
    
    @Before
    public void setup() {
        final Config config = Config.path("simplepush")
                .subprotocol("push-notification")
                .endpointUrl("/endpoint")
                .tls(false)
                .build();
        wsHandler = new WebSocketServerHandler(config, new DefaultSimplePushServer(new InMemoryDataStore()));
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
        final EmbeddedByteChannel channel = createWebsocketChannel();
        final HandshakeResponse response = doHandshake(uaid, channel);
        assertThat(response.getMessageType(), equalTo(MessageType.Type.HELLO));
        assertThat(response.getUAID(), equalTo(uaid));
    }
    
    @Test
    public void helloWithChannels() throws Exception {
        final UUID uaid = UUIDUtil.newUAID();
        final EmbeddedByteChannel channel = createWebsocketChannel();
        final String channelId = UUID.randomUUID().toString();
        final HandshakeResponse response = doHandshake(uaid, channel, channelId);
        assertThat(response.getUAID(), equalTo(uaid));
        final HttpResponse notificationResponse = doNotification(channelId, 1L);
        assertThat(notificationResponse.getStatus().code(), equalTo(200));
    }
    
    @Test
    public void register() throws Exception {
        final String channelId = UUID.randomUUID().toString();
        final EmbeddedByteChannel channel = createWebsocketChannel();
        doHandshake(UUIDUtil.newUAID(), channel);
        
        final RegisterResponse response = doRegister(channelId, channel);
        assertThat(response.getMessageType(), equalTo(MessageType.Type.REGISTER));
        assertThat(response.getChannelId(), equalTo(channelId));
        assertThat(response.getStatus().getCode(), equalTo(200));
        assertThat(response.getPushEndpoint(), equalTo("/endpoint/" + channelId));
    }
    
    @Test
    public void registerDuplicateChannelId() throws Exception {
        final String channelId = UUID.randomUUID().toString();
        doHandshake(UUIDUtil.newUAID());
        doRegister(channelId);
        
        final RegisterResponse response = doRegister(channelId);
        assertThat(response.getMessageType(), equalTo(MessageType.Type.REGISTER));
        assertThat(response.getChannelId(), equalTo(channelId));
        assertThat(response.getStatus().getCode(), equalTo(409));
        assertThat(response.getPushEndpoint(), equalTo("/endpoint/"+ channelId));
    }
    
    @Test
    public void unregisterNonRegistered() throws Exception {
        final String channelId = UUID.randomUUID().toString();
        final EmbeddedByteChannel channel = createWebsocketChannel();
        doHandshake(UUIDUtil.newUAID(), channel);
        doRegister(channelId, channel);
        
        final UnregisterResponse response = doUnregister("notRegistered", channel);
        assertThat(response.getMessageType(), equalTo(MessageType.Type.UNREGISTER));
        assertThat(response.getChannelId(), equalTo("notRegistered"));
        assertThat(response.getStatus().getCode(), equalTo(200));
    }
    
    @Test
    public void unregister() throws Exception {
        final String channelId = UUID.randomUUID().toString();
        doHandshake(UUIDUtil.newUAID());
        doRegister(channelId);
        
        final UnregisterResponse response = doUnregister(channelId);
        assertThat(response.getMessageType(), equalTo(MessageType.Type.UNREGISTER));
        assertThat(response.getChannelId(), equalTo(channelId));
        assertThat(response.getStatus().getCode(), equalTo(200));
    }
    
    @Test
    public void notification() throws Exception {
        final String channelId = UUID.randomUUID().toString();
        doHandshake(UUIDUtil.newUAID());
        doRegister(channelId);
        
        final HttpResponse response = doNotification(channelId, 1L);
        assertThat(response.getStatus().code(), equalTo(200));
    }
    
    @Test
    public void notificationWithAcknowlegement() throws Exception {
        final String channelId = UUID.randomUUID().toString();
        final EmbeddedByteChannel channel = createWebsocketChannel();
        doHandshake(UUIDUtil.newUAID(), channel);
        doRegister(channelId, channel);
        doNotification(channelId, 1L);
        
        final Set<Update> unacked = doAcknowledge(channel, update(channelId, 1L));
        assertThat(unacked.isEmpty(), is(true));
    }
    
    @Test
    public void notificationWithMultipleAcks() throws Exception {
        final String channelId1 = UUID.randomUUID().toString();
        final String channelId2 = UUID.randomUUID().toString();
        final EmbeddedByteChannel channel = createWebsocketChannel();
        doHandshake(UUIDUtil.newUAID(), channel);
        doRegister(channel, channelId1, channelId2);
        doNotification(channelId1, 1L);
        doNotification(channelId2, 1L);
        
        final Set<Update> unacked = doAcknowledge(channel, update(channelId1, 1L), update(channelId2, 1L));
        assertThat(unacked.isEmpty(), is(true));
    }
    
    @Test
    public void notificationWithNoneUnacknowleged() throws Exception {
        final String channelId1 = UUID.randomUUID().toString();
        final String channelId2 = UUID.randomUUID().toString();
        final EmbeddedByteChannel channel = createWebsocketChannel();
        doHandshake(UUIDUtil.newUAID(), channel);
        doRegister(channel, channelId1, channelId2);
        doNotification(channelId1, 1L);
        doNotification(channelId2, 1L);
        
        final Set<Update> unacked = doAcknowledge(channel);
        assertThat(unacked.size(), is(2));
        assertThat(unacked, hasItems(update(channelId1, 1L), update(channelId2, 1L)));
    }
    
    @Test
    public void notificationWithUnacknowleged() throws Exception {
        final String channelId1 = UUID.randomUUID().toString();
        final String channelId2 = UUID.randomUUID().toString();
        final EmbeddedByteChannel channel = createWebsocketChannel();
        doHandshake(UUIDUtil.newUAID(), channel);
        doRegister(channel, channelId1, channelId2);
        doNotification(channelId1, 1L);
        doNotification(channelId2, 1L);
        
        final Set<Update> unacked = doAcknowledge(channel, update(channelId1, 1L));
        assertThat(unacked.size(), is(1));
        assertThat(unacked, hasItem(new UpdateImpl(channelId2, 1L)));
    }
    
    private Update update(final String channelId, final Long version) {
        return new UpdateImpl(channelId, version);
    }
    
    @Test
    public void notificationWithVersionEqualToCurrentShouldReturn400() throws Exception {
        final String channelId = UUID.randomUUID().toString();
        doHandshake(UUIDUtil.newUAID());
        doRegister(channelId);
        doNotification(channelId, 1L);
        
        final HttpResponse invalidResponse = doNotification(channelId, 1L);
        assertThat(invalidResponse.getStatus().code(), equalTo(400));
        assertThat(invalidResponse.getStatus().reasonPhrase(), equalTo("Bad Request"));
    }
    
    @Test
    public void notificationWithVersionLessThanCurrentShouldReturn400() throws Exception {
        final String channelId = UUID.randomUUID().toString();
        doHandshake(UUIDUtil.newUAID());
        doRegister(channelId);
        doNotification(channelId, 10L);
        
        final HttpResponse invalidResponse = doNotification(channelId, 9L);
        assertThat(invalidResponse.getStatus().code(), equalTo(400));
        assertThat(invalidResponse.getStatus().reasonPhrase(), equalTo("Bad Request"));
    }
    
    @Test
    public void closeWebSocketShouldNotRemoveChannels() throws Exception {
        final String channelId = UUID.randomUUID().toString();
        final EmbeddedByteChannel channel = createWebsocketChannel();
        doWebSocketUpgradeRequest();
        doHandshake(UUIDUtil.newUAID(), channel);
        doRegister(channelId, channel);
        doClose(channel);
        
        final HttpResponse response = doNotification(channelId, 10L);
        assertThat(response.getStatus().code(), equalTo(200));
        assertThat(response.getStatus().reasonPhrase(), equalTo("OK"));
    }
    
    private void doClose(final EmbeddedByteChannel channel) throws Exception {
        final ChannelHandlerContext context = mock(ChannelHandlerContext.class);
        when(context.channel()).thenReturn(channel);
        wsHandler.messageReceived(context, closeFrame());
        channel.runPendingTasks();
    }
    
    private HttpResponse doWebSocketUpgradeRequest() throws Exception {
        return handleHttpRequest(createHttpChannel(), websocketUpgradeRequest());
    }
    
    private Set<Update> doAcknowledge(final EmbeddedByteChannel channel, final Update... updates) throws Exception {
        final Set<Update> ups = new HashSet<Update>(Arrays.asList(updates));
        final NotificationMessage unackedNotification = handleWebSocketTextFrame(ackFrame(ups), NotificationMessageImpl.class, channel);
        if (unackedNotification == null) {
            return Collections.emptySet();
        } 
        return unackedNotification.getUpdates();
    }

    private HttpResponse doNotification(final String channelId, final Long version) throws Exception {
        return handleHttpRequest(createHttpChannel(), notification(channelId, version));
    }
    
    private UnregisterResponse doUnregister(final String channelId) throws Exception {
        return handleWebSocketTextFrame(unregisterFrame(channelId), UnregisterResponseImpl.class);
    }
    
    private UnregisterResponse doUnregister(final String channelId, final EmbeddedByteChannel channel) throws Exception {
        return handleWebSocketTextFrame(unregisterFrame(channelId), UnregisterResponseImpl.class, channel);
    }
    
    private void doRegister(final EmbeddedByteChannel channel, final String... channelIds) throws Exception {
        for (String channelId : channelIds) {
            handleWebSocketTextFrame(registerFrame(channelId), RegisterResponseImpl.class, channel);
        }
    }
    
    private RegisterResponse doRegister(final String channelId, final EmbeddedByteChannel channel) throws Exception {
        return handleWebSocketTextFrame(registerFrame(channelId), RegisterResponseImpl.class, channel);
    }

    private RegisterResponse doRegister(final String channelId) throws Exception {
        return handleWebSocketTextFrame(registerFrame(channelId), RegisterResponseImpl.class);
    }

    private HandshakeResponse doHandshake(final UUID uaid) throws Exception {
        return handleWebSocketTextFrame(helloFrame(uaid), HandshakeResponseImpl.class);
    }
    
    private HandshakeResponse doHandshake(final UUID uaid, final EmbeddedByteChannel channel) throws Exception {
        return handleWebSocketTextFrame(helloFrame(uaid), HandshakeResponseImpl.class, channel);
    }
    
    private HandshakeResponse doHandshake(final UUID uaid, final EmbeddedByteChannel channel, String... channelIds) throws Exception {
        return handleWebSocketTextFrame(helloFrame(uaid, channelIds), HandshakeResponseImpl.class, channel);
    }

    private TextWebSocketFrame helloFrame(final UUID uaid) {
        final TextWebSocketFrame frame = mock(TextWebSocketFrame.class);
        when(frame.text()).thenReturn(JsonUtil.toJson(new HandshakeMessageImpl(uaid.toString())));
        return frame;
    }
    
    private TextWebSocketFrame helloFrame(final UUID uaid, final String... channelIds) {
        final TextWebSocketFrame frame = mock(TextWebSocketFrame.class);
        when(frame.text()).thenReturn(JsonUtil.toJson(new HandshakeMessageImpl(uaid.toString(), new HashSet<String>(Arrays.asList(channelIds)))));
        return frame;
    }
    
    private TextWebSocketFrame registerFrame(final String channelId) {
        final TextWebSocketFrame frame = mock(TextWebSocketFrame.class);
        when(frame.text()).thenReturn(JsonUtil.toJson(new RegisterImpl(channelId)));
        return frame;
    }
    
    private CloseWebSocketFrame closeFrame() {
        return new CloseWebSocketFrame();
    }
    
    private TextWebSocketFrame unregisterFrame(final String channelId) {
        final TextWebSocketFrame frame = mock(TextWebSocketFrame.class);
        when(frame.text()).thenReturn(JsonUtil.toJson(new UnregisterMessageImpl(channelId)));
        return frame;
    }
    
    private TextWebSocketFrame ackFrame(final Set<Update> updates) {
        final TextWebSocketFrame frame = mock(TextWebSocketFrame.class);
        when(frame.text()).thenReturn(JsonUtil.toJson(new AckMessageImpl(updates)));
        return frame;
    }
    
    private <T> T handleWebSocketTextFrame(final TextWebSocketFrame frame, final Class<T> type) throws Exception {
        final EmbeddedByteChannel channel = createWebsocketChannel();
        return handleWebSocketTextFrame(frame, type, channel);
    }
    
    private <T> T handleWebSocketTextFrame(final TextWebSocketFrame frame, final Class<T> type, final EmbeddedByteChannel channel) throws Exception {
        StubFrameEncoder stubFrameEncoder = new StubFrameEncoder();
        if (channel.pipeline().get("stubEncoder") == null) {
            channel.pipeline().addLast("stubEncoder", stubFrameEncoder);
        } else {
            channel.pipeline().replace(StubFrameEncoder.class, "stubEncoder", stubFrameEncoder);
        }
        
        final ChannelHandlerContext context = mock(ChannelHandlerContext.class);
        when(context.channel()).thenReturn(channel);
        when(context.pipeline()).thenReturn(mock(ChannelPipeline.class));
        when(context.executor()).thenReturn(new EmbeddedEventLoop());
        wsHandler.messageReceived(context, frame);
        //wsHandler.proces);
        channel.runPendingTasks();
        return stubFrameEncoder.payloadAsType(type);
    }
    
    private HttpResponse handleHttpRequest(final EmbeddedByteChannel channel, final FullHttpRequest req) throws Exception {
        final ChannelHandlerContext context = mock(ChannelHandlerContext.class);
        when(context.channel()).thenReturn(channel);
        when(context.pipeline()).thenReturn(mock(ChannelPipeline.class));
        wsHandler.messageReceived(context, req);
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
    
    private EmbeddedByteChannel createWebsocketChannel() throws Exception {
        return new EmbeddedByteChannel(
                new WebSocket13FrameEncoder(true), 
                new WebSocket13FrameDecoder(true, false, 2048));
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
        req.content().writeBytes(Unpooled.copiedBuffer("version=" + version.toString(), CharsetUtil.UTF_8));
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
            if (frame.content() != null) {
                return getTextFrame().content().toString(CharsetUtil.UTF_8);
            }
            return null;
        }
        
        public <T> T payloadAsType(final Class<T> type) {
            if (frame != null) {
                return JsonUtil.fromJson(payload(), type);
            }
            return null;
        }
        
        public void clearFrame() {
            frame = null;
        }
        
        @Override
        public String toString() {
            return "StubFrameEncoder[" + hashCode() + "]";
        }
        
    }
    
    final class EmbeddedEventLoop extends AbstractEventExecutor implements EventLoop {

        private final Queue<Runnable> tasks = new ArrayDeque<Runnable>(2);

        @Override
        public void execute(Runnable command) {
            if (command == null) {
                throw new NullPointerException("command");
            }
            tasks.add(command);
        }

        void runTasks() {
            for (;;) {
                Runnable task = tasks.poll();
                if (task == null) {
                    break;
                }

                task.run();
            }
        }

        @Override
        public void shutdownGracefully(long quietPeriod, long timeout, TimeUnit unit) { }

        @Override
        @Deprecated
        public void shutdown() { }

        @Override
        public boolean isShuttingDown() {
            return false;
        }

        @Override
        public boolean isShutdown() {
            return false;
        }

        @Override
        public boolean isTerminated() {
            return false;
        }

        @Override
        public boolean awaitTermination(long timeout, TimeUnit unit)
                throws InterruptedException {
            Thread.sleep(unit.toMillis(timeout));
            return false;
        }

        @Override
        public ChannelFuture register(Channel channel) {
            return register(channel, channel.newPromise());
        }

        @Override
        public ChannelFuture register(Channel channel, ChannelPromise promise) {
            channel.unsafe().register(this, promise);
            return promise;
        }

        @Override
        public boolean inEventLoop() {
            return true;
        }

        @Override
        public boolean inEventLoop(Thread thread) {
            return true;
        }

        @Override
        public EventLoop next() {
            return this;
        }

        @Override
        public EventLoopGroup parent() {
            return this;
        }
        
        @Override
        public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
            command.run();
            final ScheduledFuture<?> future = mock(ScheduledFuture.class);
            when(future.isSuccess()).thenReturn(true);
            when(future.isDone()).thenReturn(true);
            return future;
        }
    }

    
}
