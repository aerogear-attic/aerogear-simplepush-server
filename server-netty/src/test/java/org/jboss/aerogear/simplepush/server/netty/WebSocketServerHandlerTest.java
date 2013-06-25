package org.jboss.aerogear.simplepush.server.netty;

import static io.netty.handler.codec.http.HttpHeaders.Values.WEBSOCKET;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.jboss.aerogear.simplepush.protocol.impl.json.JsonUtil.toJson;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders.Names;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocket13FrameDecoder;
import io.netty.handler.codec.http.websocketx.WebSocket13FrameEncoder;
import io.netty.util.CharsetUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.jboss.aerogear.simplepush.protocol.HandshakeResponse;
import org.jboss.aerogear.simplepush.protocol.MessageType;
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
import org.jboss.aerogear.simplepush.server.SimplePushServer;
import org.jboss.aerogear.simplepush.server.datastore.DataStore;
import org.jboss.aerogear.simplepush.server.datastore.InMemoryDataStore;
import org.jboss.aerogear.simplepush.util.UUIDUtil;
import org.junit.Ignore;
import org.junit.Test;

public class WebSocketServerHandlerTest {
    
    private DataStore inMemoryStore = new InMemoryDataStore();
    
    @Test
    public void websocketUpgradeWithSubProtocol() throws Exception {
        final EmbeddedChannel channel = createHttpChannelWithoutHttpResponseEncoder();
        channel.writeInbound(websocketUpgradeRequest());
        final HttpResponse res = (HttpResponse) channel.readOutbound();
        assertThat(res.headers().get(Names.SEC_WEBSOCKET_PROTOCOL), equalTo("push-notification"));
        assertThat(res.headers().get(Names.SEC_WEBSOCKET_ACCEPT), equalTo("s3pPLMBiTxaQ9kYGzzhZRbK+xOo="));
    }
    
    @Test
    public void hello() throws Exception {
        final UUID uaid = UUIDUtil.newUAID();
        final EmbeddedChannel channel = createWebsocketChannel();
        channel.writeInbound(helloFrame(uaid.toString()));
        final HandshakeResponse response = responseToType(channel.readOutbound(), HandshakeResponseImpl.class);
        assertThat(response.getMessageType(), equalTo(MessageType.Type.HELLO));
        assertThat(response.getUAID(), equalTo(uaid));
    }
    
    @Test
    public void helloWithChannels() throws Exception {
        final EmbeddedChannel channel = createWebsocketChannel();
        final UUID uaid = UUIDUtil.newUAID();
        final String channelId = UUID.randomUUID().toString();
        channel.writeInbound(helloFrame(uaid.toString(), channelId));
        final HandshakeResponse response = responseToType(channel.readOutbound(), HandshakeResponseImpl.class);
        assertThat(response.getUAID(), equalTo(uaid));
        
        channel.writeInbound(notificationRequest(channelId, 1L));
        final NotificationMessageImpl notification = responseToType(channel.readOutbound(), NotificationMessageImpl.class);
        assertThat(notification.getMessageType(), is(MessageType.Type.NOTIFICATION));
        assertThat(notification.getUpdates().size(), is(1));
        assertThat(notification.getUpdates().iterator().next().getChannelId(), equalTo(channelId));
        assertThat(notification.getUpdates().iterator().next().getVersion(), equalTo(1L));
        
        final HttpResponse notificationResponse = (HttpResponse) channel.readOutbound();
        assertThat(notificationResponse.getStatus().code(), equalTo(200));
    }
    
    @Test
    public void register() throws Exception {
        final String channelId = UUID.randomUUID().toString();
        final EmbeddedChannel channel = createWebsocketChannel();
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
        final EmbeddedChannel channel = createWebsocketChannel();
        doHandshake(UUIDUtil.newUAID(), channel);
        doRegister(channelId, channel);
        
        final RegisterResponse response = doRegister(channelId, channel);
        assertThat(response.getMessageType(), equalTo(MessageType.Type.REGISTER));
        assertThat(response.getChannelId(), equalTo(channelId));
        assertThat(response.getStatus().getCode(), equalTo(409));
        assertThat(response.getPushEndpoint(), equalTo("/endpoint/"+ channelId));
    }
    
    @Test
    public void unregisterNonRegistered() throws Exception {
        final String channelId = UUID.randomUUID().toString();
        final EmbeddedChannel channel = createWebsocketChannel();
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
        final EmbeddedChannel channel = createWebsocketChannel();
        doHandshake(UUIDUtil.newUAID(), channel);
        doRegister(channelId, channel);
        
        final UnregisterResponse response = doUnregister(channelId, channel);
        assertThat(response.getMessageType(), equalTo(MessageType.Type.UNREGISTER));
        assertThat(response.getChannelId(), equalTo(channelId));
        assertThat(response.getStatus().getCode(), equalTo(200));
    }
    
    @Test
    public void notification() throws Exception {
        final String channelId = UUID.randomUUID().toString();
        final EmbeddedChannel channel = createWebsocketChannel();
        doHandshake(UUIDUtil.newUAID(), channel);
        doRegister(channelId, channel);
        doNotification(channelId, 1L, channel);
    }
    
    @Test
    public void notificationWithAcknowlegement() throws Exception {
        final String channelId = UUID.randomUUID().toString();
        final EmbeddedChannel channel = createWebsocketChannel();
        doHandshake(UUIDUtil.newUAID(), channel);
        doRegister(channelId, channel);
        doNotification(channelId, 1L, channel);
        
        final Set<Update> unacked = doAcknowledge(channel, update(channelId, 1L));
        assertThat(unacked.isEmpty(), is(true));
    }
    
    @Test
    public void notificationWithMultipleAcks() throws Exception {
        final String channelId1 = UUID.randomUUID().toString();
        final String channelId2 = UUID.randomUUID().toString();
        final EmbeddedChannel channel = createWebsocketChannel();
        doHandshake(UUIDUtil.newUAID(), channel);
        doRegister(channelId1, channel);
        doRegister(channelId2, channel);
        doNotification(channelId1, 1L, channel);
        doNotification(channelId2, 1L, channel);
        
        final Set<Update> unacked = doAcknowledge(channel, update(channelId1, 1L), update(channelId2, 1L));
        assertThat(unacked.isEmpty(), is(true));
    }
    
    @Test @Ignore ("Need to figure out how to run a schedules job with the new EmbeddedChannel")
    // https://groups.google.com/forum/#!topic/netty/Q-_wat_9Odo
    public void notificationWithNoneUnacknowleged() throws Exception {
        final String channelId1 = UUID.randomUUID().toString();
        final String channelId2 = UUID.randomUUID().toString();
        final EmbeddedChannel channel = createWebsocketChannel();
        doHandshake(UUIDUtil.newUAID(), channel);
        doRegister(channelId1, channel);
        doRegister(channelId2, channel);
        doNotification(channelId1, 1L, channel);
        doNotification(channelId2, 1L, channel);
        
        final Set<Update> unacked = doAcknowledge(channel);
        assertThat(unacked.size(), is(2));
        assertThat(unacked, hasItems(update(channelId1, 1L), update(channelId2, 1L)));
    }
    
    @Test @Ignore ("Need to figure out how to run a schedules job with the new EmbeddedChannel")
    public void notificationWithUnacknowleged() throws Exception {
        final String channelId1 = UUID.randomUUID().toString();
        final String channelId2 = UUID.randomUUID().toString();
        final EmbeddedChannel channel = createWebsocketChannel();
        doHandshake(UUIDUtil.newUAID(), channel);
        doRegister(channelId1, channel);
        doRegister(channelId2, channel);
        doNotification(channelId1, 1L, channel);
        doNotification(channelId2, 1L, channel);
        
        final Set<Update> unacked = doAcknowledge(channel, update(channelId1, 1L));
        assertThat(unacked.size(), is(1));
        assertThat(unacked, hasItem(new UpdateImpl(channelId2, 1L)));
    }
    
    @Test
    public void notificationWithVersionEqualToCurrentShouldReturn400() throws Exception {
        final String channelId = UUID.randomUUID().toString();
        final EmbeddedChannel channel = createWebsocketChannel();
        doHandshake(UUIDUtil.newUAID(), channel);
        doRegister(channelId, channel);
        doNotification(channelId, 1L, channel);
        
        final HttpResponse invalidResponse = getNotificationResponse(channelId, 1L, channel);
        assertThat(invalidResponse.getStatus().code(), equalTo(400));
        assertThat(invalidResponse.getStatus().reasonPhrase(), equalTo("Bad Request"));
    }
    
    @Test
    public void notificationWithVersionLessThanCurrentShouldReturn400() throws Exception {
        final String channelId = UUID.randomUUID().toString();
        final EmbeddedChannel channel = createWebsocketChannel();
        doHandshake(UUIDUtil.newUAID(), channel);
        doRegister(channelId, channel);
        doNotification(channelId, 10L, channel);
        
        final HttpResponse invalidResponse = getNotificationResponse(channelId, 1L, channel);
        assertThat(invalidResponse.getStatus().code(), equalTo(400));
        assertThat(invalidResponse.getStatus().reasonPhrase(), equalTo("Bad Request"));
    }
    
    @Test
    public void closeWebSocketShouldNotRemoveChannels() throws Exception {
        final String channelId = UUID.randomUUID().toString();
        final UUID uaid = UUIDUtil.newUAID();
        final EmbeddedChannel channel = doWebSocketUpgradeRequest();
        // Discard http response
        channel.readOutbound();
        
        channel.pipeline().remove(WebSocket13FrameEncoder.class);
        doHandshake(uaid, channel);
        channel.pipeline().remove(WebSocket13FrameDecoder.class);
        doRegister(channelId, channel);
        doClose(channel);
        
        final EmbeddedChannel newChannel = doWebSocketUpgradeRequest();
        // Discard http response
        newChannel.readOutbound();
        newChannel.pipeline().remove(WebSocket13FrameEncoder.class);
        doHandshake(uaid, newChannel);
        final HttpResponse response = doNotification(channelId, 10L, newChannel);
        assertThat(response.getStatus().code(), equalTo(200));
        assertThat(response.getStatus().reasonPhrase(), equalTo("OK"));
    }
    
    private Update update(final String channelId, final Long version) {
        return new UpdateImpl(channelId, version);
    }

    private <T> T responseToType(final Object response, Class<T> type) {
        if (response instanceof TextWebSocketFrame) {
            final TextWebSocketFrame frame = (TextWebSocketFrame) response;
            return JsonUtil.fromJson(frame.text(), type);
        }
        throw new IllegalArgumentException("Response is expected to be of type TextWebSocketFrame was: " + response);
    }

    private TextWebSocketFrame helloFrame(final String uaid, final String... channelIds) {
        final HashSet<String> channels = new HashSet<String>(Arrays.asList(channelIds));
        return new TextWebSocketFrame(toJson(new HandshakeMessageImpl(uaid.toString(), channels)));
    }

    private void doClose(final EmbeddedChannel channel) throws Exception {
        final CloseWebSocketFrame closeFrame = new CloseWebSocketFrame();
        channel.writeInbound(closeFrame);
    }
    
    private EmbeddedChannel doWebSocketUpgradeRequest() throws Exception {
        final FullHttpRequest request = websocketUpgradeRequest();
        final EmbeddedChannel channel = createHttpChannel2();
        channel.writeInbound(request);
        return channel;
    }
    
    private Set<Update> doAcknowledge(final EmbeddedChannel channel, final Update... updates) throws Exception {
        final Set<Update> ups = new HashSet<Update>(Arrays.asList(updates));
        final TextWebSocketFrame ackFrame = ackFrame(ups);
        channel.writeInbound(ackFrame);
        channel.runPendingTasks();
        
        final Object out = channel.readOutbound();
        if (out == null) {
            return Collections.emptySet();
        } 
        
        final NotificationMessageImpl unacked = responseToType(out, NotificationMessageImpl.class);
        return unacked.getUpdates();
    }

    private HttpResponse doNotification(final String channelId, final Long version, final EmbeddedChannel channel) throws Exception {
        channel.writeInbound(notificationRequest(channelId, version));
        //channel.runPendingTasks();
        
        // The notification destined for the connected channel
        final NotificationMessageImpl notification = responseToType(channel.readOutbound(), NotificationMessageImpl.class);
        assertThat(notification.getMessageType(), is(MessageType.Type.NOTIFICATION));
        assertThat(notification.getUpdates().size(), is(1));
        assertThat(notification.getUpdates().iterator().next().getChannelId(), equalTo(channelId));
        assertThat(notification.getUpdates().iterator().next().getVersion(), equalTo(version));
        
        // The response to the client that sent the notification request
        final HttpResponse httpResponse = (HttpResponse) channel.readOutbound();
        assertThat(httpResponse.getStatus().code(), equalTo(200));
        return httpResponse;
    }
    
    private HttpResponse getNotificationResponse(final String channelId, final Long version, final EmbeddedChannel channel) throws Exception {
        channel.writeInbound(notificationRequest(channelId, version));
        return (HttpResponse) channel.readOutbound();
    }
    
    private UnregisterResponse doUnregister(final String channelId, final EmbeddedChannel channel) throws Exception {
        final TextWebSocketFrame unregisterFrame = unregisterFrame(channelId);
        channel.writeInbound(unregisterFrame);
        return responseToType(channel.readOutbound(), UnregisterResponseImpl.class);
    }
    
    private RegisterResponse doRegister(final String channelId, final EmbeddedChannel channel) throws Exception {
        final TextWebSocketFrame registerFrame = registerFrame(channelId);
        channel.writeInbound(registerFrame);
        return responseToType(channel.readOutbound(), RegisterResponseImpl.class);
    }

    private HandshakeResponse doHandshake(final UUID uaid, final EmbeddedChannel channel) throws Exception {
        channel.writeInbound(helloFrame(uaid.toString()));
        return responseToType(channel.readOutbound(), HandshakeResponseImpl.class);
    }
    
    private TextWebSocketFrame registerFrame(final String channelId) {
        final TextWebSocketFrame frame = mock(TextWebSocketFrame.class);
        when(frame.text()).thenReturn(JsonUtil.toJson(new RegisterImpl(channelId)));
        return frame;
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
    
    private EmbeddedChannel createHttpChannel2() throws Exception {
        return new EmbeddedChannel(
                new HttpRequestDecoder(), 
                new HttpResponseEncoder(), 
                newWebSocketServerHandler());
    }
    
    private EmbeddedChannel createHttpChannelWithoutHttpResponseEncoder() throws Exception {
        return new EmbeddedChannel(
                new HttpRequestDecoder(), 
                newWebSocketServerHandler());
    }
    
    private WebSocketServerHandler newWebSocketServerHandler() {
        final Config config = Config.path("simplepush")
                .subprotocol("push-notification")
                .endpointUrl("/endpoint")
                .tls(false)
                .build();
        final SimplePushServer simplePushServer = new DefaultSimplePushServer(inMemoryStore);
        return new WebSocketServerHandler(config, simplePushServer);
    }
    
    private EmbeddedChannel createWebsocketChannel() throws Exception {
        final Config config = Config.path("simplepush")
                .subprotocol("push-notification")
                .endpointUrl("/endpoint")
                .tls(false)
                .build();
        WebSocketServerHandler webSocketServerHandler = new WebSocketServerHandler(config, new DefaultSimplePushServer(new InMemoryDataStore()));
        return new EmbeddedChannel(webSocketServerHandler);
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
        req.headers().set(Names.CONTENT_LENGTH, "0");
        return req;
    }
    
    private FullHttpRequest notificationRequest(final String channelId, final Long version) {
        final FullHttpRequest req = new DefaultFullHttpRequest(HTTP_1_1, HttpMethod.PUT, "/endpoint/" + channelId);
        req.content().writeBytes(Unpooled.copiedBuffer("version=" + version.toString(), CharsetUtil.UTF_8));
        return req;
    }
    
}
