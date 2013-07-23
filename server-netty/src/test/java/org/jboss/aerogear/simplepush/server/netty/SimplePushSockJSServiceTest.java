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

import static io.netty.handler.codec.http.HttpHeaders.Values.WEBSOCKET;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static io.netty.util.CharsetUtil.UTF_8;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpHeaders.Names;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.sockjs.Config;
import io.netty.handler.codec.sockjs.SockJSService;
import io.netty.handler.codec.sockjs.SockJSServiceFactory;
import io.netty.handler.codec.sockjs.handlers.CorsInboundHandler;
import io.netty.handler.codec.sockjs.handlers.CorsOutboundHandler;
import io.netty.handler.codec.sockjs.handlers.SockJSHandler;
import io.netty.handler.codec.sockjs.transports.Transports;

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
import org.jboss.aerogear.simplepush.protocol.impl.HandshakeResponseImpl;
import org.jboss.aerogear.simplepush.protocol.impl.NotificationMessageImpl;
import org.jboss.aerogear.simplepush.protocol.impl.RegisterResponseImpl;
import org.jboss.aerogear.simplepush.protocol.impl.UnregisterResponseImpl;
import org.jboss.aerogear.simplepush.protocol.impl.UpdateImpl;
import org.jboss.aerogear.simplepush.protocol.impl.json.JsonUtil;
import org.jboss.aerogear.simplepush.server.DefaultSimplePushServer;
import org.jboss.aerogear.simplepush.server.DefaultSimplePushConfig;
import org.jboss.aerogear.simplepush.server.SimplePushServer;
import org.jboss.aerogear.simplepush.server.SimplePushServerConfig;
import org.jboss.aerogear.simplepush.server.datastore.ChannelNotFoundException;
import org.jboss.aerogear.simplepush.server.datastore.InMemoryDataStore;
import org.jboss.aerogear.simplepush.util.UUIDUtil;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class SimplePushSockJSServiceTest {

    private SockJSServiceFactory factory;
    private String sessionUrl;

    @Before
    public void setup() {
        factory = defaultFactory();
        sessionUrl = randomSessionIdUrl(factory);
    }

    @Test
    public void xhrPollingOpenFrame() {
        final FullHttpResponse openFrameResponse = sendXhrOpenFrameRequest(factory, sessionUrl);
        assertThat(openFrameResponse.getStatus(), is(HttpResponseStatus.OK));
        assertThat(openFrameResponse.content().toString(UTF_8), equalTo("o\n"));
    }

    @Test
    public void xhrPollingHelloWithChannelId() {
        final String uaid = UUIDUtil.newUAID();
        final String channelId = UUID.randomUUID().toString();
        sendXhrOpenFrameRequest(factory, sessionUrl);

        final FullHttpResponse sendResponse = sendXhrHelloMessageRequest(factory, sessionUrl, uaid, channelId);
        assertThat(sendResponse.getStatus(), is(HttpResponseStatus.NO_CONTENT));
        final HandshakeResponseImpl handshakeResponse = pollXhrHelloMessageResponse(factory, sessionUrl);
        assertThat(handshakeResponse.getUAID().toString(), equalTo(uaid.toString()));
    }

    @Test
    public void xhrPollingRegister() {
        final String channelId = UUID.randomUUID().toString();
        sendXhrOpenFrameRequest(factory, sessionUrl);
        sendXhrHelloMessageRequest(factory, sessionUrl, UUIDUtil.newUAID());
        pollXhrHelloMessageResponse(factory, sessionUrl);

        final FullHttpResponse registerChannelIdRequest = sendXhrRegisterChannelIdRequest(factory, sessionUrl, channelId);
        assertThat(registerChannelIdRequest.getStatus(), is(HttpResponseStatus.NO_CONTENT));

        final RegisterResponseImpl registerChannelIdResponse = pollXhrRegisterChannelIdResponse(factory, sessionUrl);
        assertThat(registerChannelIdResponse.getChannelId(), equalTo(channelId));
        assertThat(registerChannelIdResponse.getStatus().getCode(), equalTo(200));
        assertThat(registerChannelIdResponse.getPushEndpoint(), equalTo("/endpoint/" + channelId));
    }

    @Test
    public void xhrPollingUnregister() {
        final String channelId = UUID.randomUUID().toString();
        sendXhrOpenFrameRequest(factory, sessionUrl);
        sendXhrHelloMessageRequest(factory, sessionUrl, UUIDUtil.newUAID());
        pollXhrHelloMessageResponse(factory, sessionUrl);
        sendXhrRegisterChannelIdRequest(factory, sessionUrl, channelId);
        pollXhrRegisterChannelIdResponse(factory, sessionUrl);

        final FullHttpResponse unregisterChannelIdRequest = unregisterChannelIdRequest(factory, sessionUrl, channelId);
        assertThat(unregisterChannelIdRequest.getStatus(), is(HttpResponseStatus.NO_CONTENT));

        final UnregisterResponseImpl unregisterChannelIdResponse = unregisterChannelIdResponse(factory, sessionUrl);
        assertThat(unregisterChannelIdResponse.getStatus().getCode(), is(200));
        assertThat(unregisterChannelIdResponse.getChannelId(), equalTo(channelId));
    }

    @Test
    public void websocketUpgradeRequest() throws Exception {
        final EmbeddedChannel channel = createChannel(factory);
        final FullHttpResponse response = websocketHttpUpgradeRequest(sessionUrl, channel);
        assertThat(response.getStatus(), is(HttpResponseStatus.SWITCHING_PROTOCOLS));
        assertThat(response.headers().get(HttpHeaders.Names.UPGRADE), equalTo("websocket"));
        assertThat(response.headers().get(HttpHeaders.Names.CONNECTION), equalTo("Upgrade"));
        assertThat(response.headers().get(Names.SEC_WEBSOCKET_ACCEPT), equalTo("s3pPLMBiTxaQ9kYGzzhZRbK+xOo="));
        channel.close();
    }

    @Test
    public void rawWebSocketUpgradeRequest() throws Exception {
        final SimplePushServerConfig simplePushConfig = DefaultSimplePushConfig.defaultConfig();
        final Config sockjsConf = Config.prefix("/simplepush").websocketProtocols("push-notification").build();
        final SimplePushServiceFactory factory = new SimplePushServiceFactory(sockjsConf, new InMemoryDataStore(), simplePushConfig);
        final EmbeddedChannel channel = createChannel(factory);
        final FullHttpRequest request = websocketUpgradeRequest(factory.config().prefix() + Transports.Types.WEBSOCKET.path());
        request.headers().set(Names.SEC_WEBSOCKET_PROTOCOL, "push-notification");
        channel.writeInbound(request);
        final FullHttpResponse response = (FullHttpResponse) channel.readOutbound();
        assertThat(response.getStatus(), is(HttpResponseStatus.SWITCHING_PROTOCOLS));
        assertThat(response.headers().get(HttpHeaders.Names.UPGRADE), equalTo("websocket"));
        assertThat(response.headers().get(HttpHeaders.Names.CONNECTION), equalTo("Upgrade"));
        assertThat(response.headers().get(Names.SEC_WEBSOCKET_PROTOCOL), equalTo("push-notification"));
        assertThat(response.headers().get(Names.SEC_WEBSOCKET_ACCEPT), equalTo("s3pPLMBiTxaQ9kYGzzhZRbK+xOo="));
        channel.close();
    }

    @Test
    public void websocketHello() {
        final EmbeddedChannel channel = createWebSocketChannel(factory);
        final String uaid = UUIDUtil.newUAID();
        sendWebSocketHttpUpgradeRequest(sessionUrl, channel);

        final HandshakeResponse response = sendWebSocketHelloFrame(uaid, channel);
        assertThat(response.getMessageType(), equalTo(MessageType.Type.HELLO));
        assertThat(response.getUAID(), equalTo(uaid));
        channel.close();
    }

    @Test
    public void websocketRegister() {
        final EmbeddedChannel channel = createWebSocketChannel(factory);
        final String channelId = UUID.randomUUID().toString();
        sendWebSocketHttpUpgradeRequest(sessionUrl, channel);
        sendWebSocketHelloFrame(UUIDUtil.newUAID(), channel);

        final RegisterResponse registerResponse = sendWebSocketRegisterFrame(channelId, channel);
        assertThat(registerResponse.getStatus().getCode(), is(200));
        assertThat(registerResponse.getChannelId(), equalTo(channelId));
        channel.close();
    }

    @Test
    public void websocketRegisterDuplicateChannelId() {
        final EmbeddedChannel channel = createWebSocketChannel(factory);
        final String channelId = UUID.randomUUID().toString();
        sendWebSocketHttpUpgradeRequest(sessionUrl, channel);
        sendWebSocketHelloFrame(UUIDUtil.newUAID(), channel);

        assertThat(sendWebSocketRegisterFrame(channelId, channel).getStatus().getCode(), is(200));
        assertThat(sendWebSocketRegisterFrame(channelId, channel).getStatus().getCode(), is(409));
        channel.close();
    }

    @Test
    public void websocketUnregister() {
        final EmbeddedChannel channel = createWebSocketChannel(factory);
        final String channelId = UUID.randomUUID().toString();
        sendWebSocketHttpUpgradeRequest(sessionUrl, channel);
        sendWebSocketHelloFrame(UUIDUtil.newUAID(), channel);
        sendWebSocketRegisterFrame(channelId, channel);

        final UnregisterResponse registerResponse = websocketUnRegisterFrame(channelId, channel);
        assertThat(registerResponse.getStatus().getCode(), is(200));
        channel.close();
    }

    @Test
    public void websocketUnregisterNonRegistered() {
        final EmbeddedChannel channel = createWebSocketChannel(factory);
        sendWebSocketHttpUpgradeRequest(sessionUrl, channel);
        sendWebSocketHelloFrame(UUIDUtil.newUAID(), channel);

        final UnregisterResponse registerResponse = websocketUnRegisterFrame("notRegistered", channel);
        assertThat(registerResponse.getMessageType(), equalTo(MessageType.Type.UNREGISTER));
        assertThat(registerResponse.getChannelId(), equalTo("notRegistered"));
        assertThat(registerResponse.getStatus().getCode(), is(200));
        channel.close();
    }

    @Test
    public void websocketHandleAcknowledgement() throws Exception {
        final SimplePushServer simplePushServer = defaultPushServer();
        final SockJSServiceFactory serviceFactory = defaultFactory(simplePushServer);
        final EmbeddedChannel channel = createWebSocketChannel(serviceFactory);
        final String uaid = UUIDUtil.newUAID();
        final String channelId = UUID.randomUUID().toString();
        sendWebSocketHttpUpgradeRequest(sessionUrl, channel);
        sendWebSocketHelloFrame(uaid, channel);
        sendWebSocketRegisterFrame(channelId, channel);
        sendNotification(channelId, uaid, 1L, simplePushServer);

        final Set<Update> unacked = sendAcknowledge(channel, update(channelId, 1L));
        assertThat(unacked.isEmpty(), is(true));
        channel.close();
    }

    @Test
    public void websocketHandleAcknowledgements() throws Exception {
        final SimplePushServer simplePushServer = defaultPushServer();
        final SockJSServiceFactory serviceFactory = defaultFactory(simplePushServer);
        final EmbeddedChannel channel = createWebSocketChannel(serviceFactory);
        final String uaid = UUIDUtil.newUAID();
        final String channelId1 = UUID.randomUUID().toString();
        final String channelId2 = UUID.randomUUID().toString();
        sendWebSocketHttpUpgradeRequest(sessionUrl, channel);
        sendWebSocketHelloFrame(uaid, channel);
        sendWebSocketRegisterFrame(channelId1, channel);
        sendWebSocketRegisterFrame(channelId2, channel);
        sendNotification(channelId1, uaid, 1L, simplePushServer);
        sendNotification(channelId2, uaid, 1L, simplePushServer);

        final Set<Update> unacked = sendAcknowledge(channel, update(channelId1, 1L), update(channelId2, 1L));
        assertThat(unacked.isEmpty(), is(true));
        channel.close();
    }

    @Test
    @Ignore("Need to figure out how to run a schedules job with the new EmbeddedChannel")
    // https://groups.google.com/forum/#!topic/netty/Q-_wat_9Odo
    public void websocketHandleOneUnacknowledgement() throws Exception {
        final SimplePushServer simplePushServer = defaultPushServer();
        final SockJSServiceFactory serviceFactory = defaultFactory(simplePushServer);
        final EmbeddedChannel channel = createWebSocketChannel(serviceFactory);
        final String uaid = UUIDUtil.newUAID();
        final String channelId1 = UUID.randomUUID().toString();
        final String channelId2 = UUID.randomUUID().toString();
        sendWebSocketHttpUpgradeRequest(sessionUrl, channel);
        sendWebSocketHelloFrame(uaid, channel);
        sendWebSocketRegisterFrame(channelId1, channel);
        sendWebSocketRegisterFrame(channelId2, channel);
        sendNotification(channelId1, uaid, 1L, simplePushServer);
        sendNotification(channelId2, uaid, 1L, simplePushServer);

        final Set<Update> unacked = sendAcknowledge(channel, update(channelId1, 1L));
        assertThat(unacked.size(), is(1));
        assertThat(unacked, hasItem(new UpdateImpl(channelId2, 1L)));
        channel.close();
    }

    @Test
    @Ignore("Need to figure out how to run a schedules job with the new EmbeddedChannel")
    // https://groups.google.com/forum/#!topic/netty/Q-_wat_9Odo
    public void websocketHandleUnacknowledgement() throws Exception {
        final SimplePushServer simplePushServer = defaultPushServer();
        final SockJSServiceFactory serviceFactory = defaultFactory(simplePushServer);
        final EmbeddedChannel channel = createWebSocketChannel(serviceFactory);
        final String uaid = UUIDUtil.newUAID();
        final String channelId1 = UUID.randomUUID().toString();
        final String channelId2 = UUID.randomUUID().toString();
        sendWebSocketHttpUpgradeRequest(sessionUrl, channel);
        sendWebSocketHelloFrame(uaid, channel);
        sendWebSocketRegisterFrame(channelId1, channel);
        sendWebSocketRegisterFrame(channelId2, channel);
        sendNotification(channelId1, uaid, 1L, simplePushServer);
        sendNotification(channelId2, uaid, 1L, simplePushServer);

        final Set<Update> unacked = sendAcknowledge(channel);
        assertThat(unacked.size(), is(1));
        assertThat(unacked, hasItems(update(channelId1, 1L), update(channelId2, 1L)));
        channel.close();
    }

    private SimplePushServer defaultPushServer() {
        return new DefaultSimplePushServer(new InMemoryDataStore(), DefaultSimplePushConfig.defaultConfig());
    }

    private void sendNotification(final String channelId, final String uaid, final long version,
            final SimplePushServer simplePushServer) throws ChannelNotFoundException {
        simplePushServer.handleNotification(channelId, uaid, "version=" + version);
    }

    private Update update(final String channelId, final Long version) {
        return new UpdateImpl(channelId, version);
    }

    private Set<Update> sendAcknowledge(final EmbeddedChannel channel, final Update... updates) throws Exception {
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

    private TextWebSocketFrame ackFrame(final Set<Update> updates) {
        return new TextWebSocketFrame(JsonUtil.toJson(new AckMessageImpl(updates)));
    }

    private RegisterResponseImpl sendWebSocketRegisterFrame(final String channelId, final EmbeddedChannel ch) {
        ch.writeInbound(TestUtil.registerChannelIdWebSocketFrame(channelId));
        return responseToType(ch.readOutbound(), RegisterResponseImpl.class);
    }

    private UnregisterResponse websocketUnRegisterFrame(final String channelId, final EmbeddedChannel ch) {
        ch.writeInbound(TestUtil.unregisterChannelIdWebSocketFrame(channelId));
        return responseToType(ch.readOutbound(), UnregisterResponseImpl.class);
    }

    private FullHttpResponse websocketHttpUpgradeRequest(final String sessionUrl, final EmbeddedChannel ch) {
        ch.writeInbound(websocketUpgradeRequest(sessionUrl + Transports.Types.WEBSOCKET.path()));
        return (FullHttpResponse) ch.readOutbound();
    }

    private void sendWebSocketHttpUpgradeRequest(final String sessionUrl, final EmbeddedChannel ch) {
        ch.writeInbound(websocketUpgradeRequest(sessionUrl + Transports.Types.WEBSOCKET.path()));
        // Discarding the Http upgrade response
        ch.readOutbound();
        // Discard open frame
        ch.readOutbound();
        ch.pipeline().remove("wsencoder");
    }

    private HandshakeResponse sendWebSocketHelloFrame(final String uaid, final EmbeddedChannel ch) {
        ch.writeInbound(TestUtil.helloWebSocketFrame(uaid.toString()));
        return responseToType(ch.readOutbound(), HandshakeResponseImpl.class);
    }

    private <T> T responseToType(final Object response, Class<T> type) {
        if (response instanceof TextWebSocketFrame) {
            final TextWebSocketFrame frame = (TextWebSocketFrame) response;
            String content = frame.text();
            if (content.startsWith("a[")) {
                content = TestUtil.extractJsonFromSockJSMessage(content);
            }
            return JsonUtil.fromJson(content, type);
        }
        throw new IllegalArgumentException("Response is expected to be of type TextWebSocketFrame was: " + response);
    }

    private FullHttpResponse sendXhrOpenFrameRequest(final SockJSServiceFactory factory, final String sessionUrl) {
        final EmbeddedChannel openChannel = createChannel(factory);
        openChannel.writeInbound(httpGetRequest(sessionUrl + Transports.Types.XHR.path()));
        final FullHttpResponse openFrameResponse = (FullHttpResponse) openChannel.readOutbound();
        openChannel.close();
        return openFrameResponse;
    }

    private FullHttpResponse sendXhrHelloMessageRequest(final SockJSServiceFactory factory, final String sessionUrl,
            final String uaid, final String... channelIds) {
        return xhrSend(factory, sessionUrl, TestUtil.helloSockJSFrame(uaid.toString(), channelIds));
    }

    private HandshakeResponseImpl pollXhrHelloMessageResponse(final SockJSServiceFactory factory, final String sessionUrl) {
        final FullHttpResponse pollResponse = xhrPoll(factory, sessionUrl);
        assertThat(pollResponse.getStatus(), is(HttpResponseStatus.OK));

        final String helloJson = TestUtil.extractJsonFromSockJSMessage(pollResponse.content().toString(UTF_8));
        return JsonUtil.fromJson(helloJson, HandshakeResponseImpl.class);
    }

    private FullHttpResponse sendXhrRegisterChannelIdRequest(final SockJSServiceFactory factory, final String sessionUrl,
            final String channelId) {
        return xhrSend(factory, sessionUrl, TestUtil.registerChannelIdMessageSockJSFrame(channelId));
    }

    private RegisterResponseImpl pollXhrRegisterChannelIdResponse(final SockJSServiceFactory factory, final String sessionUrl) {
        final FullHttpResponse pollResponse = xhrPoll(factory, sessionUrl);
        assertThat(pollResponse.getStatus(), is(HttpResponseStatus.OK));

        final String json = TestUtil.extractJsonFromSockJSMessage(pollResponse.content().toString(UTF_8));
        return JsonUtil.fromJson(json, RegisterResponseImpl.class);
    }

    private FullHttpResponse unregisterChannelIdRequest(final SockJSServiceFactory factory, final String sessionUrl,
            final String channelId) {
        return xhrSend(factory, sessionUrl, TestUtil.unregisterChannelIdMessageSockJSFrame(channelId));
    }

    private UnregisterResponseImpl unregisterChannelIdResponse(final SockJSServiceFactory factory, final String sessionUrl) {
        final FullHttpResponse pollResponse = xhrPoll(factory, sessionUrl);
        assertThat(pollResponse.getStatus(), is(HttpResponseStatus.OK));

        final String json = TestUtil.extractJsonFromSockJSMessage(pollResponse.content().toString(UTF_8));
        return JsonUtil.fromJson(json, UnregisterResponseImpl.class);
    }

    private FullHttpResponse xhrSend(final SockJSServiceFactory factory, final String sessionUrl, final String content) {
        final EmbeddedChannel sendChannel = createChannel(factory);
        final FullHttpRequest sendRequest = httpPostRequest(sessionUrl + Transports.Types.XHR_SEND.path());
        sendRequest.content().writeBytes(Unpooled.copiedBuffer(content, UTF_8));
        sendChannel.writeInbound(sendRequest);
        final FullHttpResponse sendResponse = (FullHttpResponse) sendChannel.readOutbound();
        sendChannel.close();
        return sendResponse;

    }

    private FullHttpResponse xhrPoll(final SockJSServiceFactory factory, final String sessionUrl) {
        final EmbeddedChannel pollChannel = createChannel(factory);
        pollChannel.writeInbound(httpGetRequest(sessionUrl + Transports.Types.XHR.path()));
        return (FullHttpResponse) pollChannel.readOutbound();
    }

    private FullHttpRequest httpGetRequest(final String path) {
        return new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, path);
    }

    private FullHttpRequest websocketUpgradeRequest(final String path) {
        final FullHttpRequest req = new DefaultFullHttpRequest(HTTP_1_1, HttpMethod.GET, path);
        req.headers().set(Names.HOST, "server.test.com");
        req.headers().set(Names.UPGRADE, WEBSOCKET.toLowerCase());
        req.headers().set(Names.CONNECTION, "Upgrade");
        req.headers().set(Names.SEC_WEBSOCKET_KEY, "dGhlIHNhbXBsZSBub25jZQ==");
        req.headers().set(Names.SEC_WEBSOCKET_ORIGIN, "http://test.com");
        req.headers().set(Names.SEC_WEBSOCKET_VERSION, "13");
        req.headers().set(Names.CONTENT_LENGTH, "0");
        return req;
    }

    private FullHttpRequest httpPostRequest(final String path) {
        return new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, path);
    }

    private SockJSServiceFactory defaultFactory() {
        final SimplePushServerConfig simplePushConfig = DefaultSimplePushConfig.defaultConfig();
        final Config sockjsConf = Config.prefix("/simplepush").build();
        return new SimplePushServiceFactory(sockjsConf, new InMemoryDataStore(), simplePushConfig);
    }

    private SockJSServiceFactory defaultFactory(final SimplePushServer simplePushServer) {
        final Config sockJSConfig = Config.prefix("/simplepush").build();
        return new SockJSServiceFactory() {
            @Override
            public SockJSService create() {
                return new SimplePushSockJSService(config(), simplePushServer);
            }

            @Override
            public Config config() {
                return sockJSConfig;
            }
        };
    }

    private EmbeddedChannel createChannel(final SockJSServiceFactory factory) {
        final EmbeddedChannel ch = new EmbeddedChannel(
                new CorsInboundHandler(),
                new SockJSHandler(factory),
                new CorsOutboundHandler());
        ch.pipeline().remove("EmbeddedChannel$LastInboundHandler#0");
        return ch;
    }

    private EmbeddedChannel createWebSocketChannel(final SockJSServiceFactory factory) {
        final EmbeddedChannel ch = new EmbeddedChannel(
                new HttpRequestDecoder(),
                new HttpResponseEncoder(),
                new CorsInboundHandler(),
                new SockJSHandler(factory),
                new CorsOutboundHandler());
        ch.pipeline().remove("EmbeddedChannel$LastInboundHandler#0");
        return ch;
    }

    private String randomSessionIdUrl(final SockJSServiceFactory factory) {
        return factory.config().prefix() + "/111/" + UUID.randomUUID().toString();
    }

}
