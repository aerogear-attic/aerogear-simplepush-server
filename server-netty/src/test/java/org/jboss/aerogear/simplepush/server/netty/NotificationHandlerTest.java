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

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.CharsetUtil;
import org.jboss.aerogear.io.netty.handler.codec.sockjs.SockJsSessionContext;
import org.jboss.aerogear.simplepush.protocol.MessageType;
import org.jboss.aerogear.simplepush.protocol.RegisterResponse;
import org.jboss.aerogear.simplepush.protocol.impl.NotificationMessageImpl;
import org.jboss.aerogear.simplepush.protocol.impl.RegisterMessageImpl;
import org.jboss.aerogear.simplepush.protocol.impl.json.JsonUtil;
import org.jboss.aerogear.simplepush.server.DefaultSimplePushConfig;
import org.jboss.aerogear.simplepush.server.DefaultSimplePushServer;
import org.jboss.aerogear.simplepush.server.SimplePushServer;
import org.jboss.aerogear.simplepush.server.SimplePushServerConfig;
import org.jboss.aerogear.simplepush.server.datastore.DataStore;
import org.jboss.aerogear.simplepush.server.datastore.InMemoryDataStore;
import org.jboss.aerogear.simplepush.util.UUIDUtil;
import org.junit.Test;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class NotificationHandlerTest {

    @Test
    public void notification() throws Exception {
        final String uaid = UUIDUtil.newUAID();
        final String channelId = UUID.randomUUID().toString();
        final SimplePushServer simplePushServer = defaultPushServer();
        final EmbeddedChannel channel = createWebsocketChannel(simplePushServer);
        registerUserAgent(uaid, channel);
        final RegisterResponse registerResponse = doRegister(channelId, uaid, simplePushServer);
        final String endpointToken = extractEndpointToken(registerResponse.getPushEndpoint());
        doNotification(endpointToken, channelId, 1L, simplePushServer, channel);
        channel.close();
    }

    @Test
    public void notificationVersionEqualToCurrentVersion() throws Exception {
        final String uaid = UUIDUtil.newUAID();
        final String channelId = UUID.randomUUID().toString();
        final SimplePushServer simplePushServer = defaultPushServer();
        final EmbeddedChannel channel = createWebsocketChannel(simplePushServer);
        registerUserAgent(uaid, channel);
        final RegisterResponse registerResponse = doRegister(channelId, uaid, simplePushServer);
        final String endpointToken = extractEndpointToken(registerResponse.getPushEndpoint());
        doNotification(endpointToken, channelId, 1L, simplePushServer, channel);

        final HttpResponse response = sendNotification(notificationRequest(endpointToken, 1L), simplePushServer);
        assertThat(response.getStatus(), is(HttpResponseStatus.OK));
        channel.close();
    }

    private String extractEndpointToken(final String pushEndpoint) {
        return pushEndpoint.substring(pushEndpoint.lastIndexOf('/') + 1);
    }

    @Test
    public void notificationVersionLessThanCurrent() throws Exception {
        final String uaid = UUIDUtil.newUAID();
        final String channelId = UUID.randomUUID().toString();
        final SimplePushServer simplePushServer = defaultPushServer();
        final EmbeddedChannel channel = createWebsocketChannel(simplePushServer);
        registerUserAgent(uaid, channel);
        final RegisterResponse registerResponse = doRegister(channelId, uaid, simplePushServer);
        final String endpointToken = extractEndpointToken(registerResponse.getPushEndpoint());
        doNotification(endpointToken, channelId, 10L, simplePushServer, channel);

        final HttpResponse response = sendNotification(notificationRequest(endpointToken, 9L), simplePushServer);
        assertThat(response.getStatus(), is(HttpResponseStatus.OK));
        channel.close();
    }

    @Test
    public void notificationWithNonExistingChannelId() throws Exception {
        final SimplePushServer simplePushServer = defaultPushServer();
        final EmbeddedChannel channel = createWebsocketChannel(simplePushServer);
        channel.writeInbound(notificationRequest("non-existing-channelId", 10L));
        final HttpResponse httpResponse = channel.readOutbound();
        assertThat(httpResponse.getStatus().code(), equalTo(200));
        channel.close();
    }

    @Test
    public void notificationWithoutVersionBody() throws Exception {
        final String uaid = UUIDUtil.newUAID();
        final String channelId = UUID.randomUUID().toString();
        final SimplePushServer simplePushServer = defaultPushServer();
        final EmbeddedChannel channel = createWebsocketChannel(simplePushServer);
        registerUserAgent(uaid, channel);
        final RegisterResponse registerResponse = doRegister(channelId, uaid, simplePushServer);
        final String endpointToken = extractEndpointToken(registerResponse.getPushEndpoint());
        doNotificationWithoutVersion(endpointToken, channelId, simplePushServer, channel);
        channel.close();
    }

    private SimplePushServer defaultPushServer() {
        final DataStore store = new InMemoryDataStore();
        final SimplePushServerConfig config = DefaultSimplePushConfig.create().password("testToken").build();
        final byte[] privateKey = DefaultSimplePushServer.generateAndStorePrivateKey(store, config);
        return new DefaultSimplePushServer(store, config, privateKey);
    }

    private HttpResponse sendNotification(final FullHttpRequest request,
                                          final SimplePushServer simplePushServer) throws Exception {
        final EmbeddedChannel ch = createWebsocketChannel(simplePushServer);
        ch.writeInbound(request);
        return (HttpResponse) ch.readOutbound();
    }

    private void registerUserAgent(final String uaid, final EmbeddedChannel ch) {
        UserAgents.getInstance().add(uaid, channelSession(ch));
    }

    private RegisterResponse doRegister(final String channelId, final String uaid, final SimplePushServer server) {
        return server.handleRegister(new RegisterMessageImpl(channelId), uaid);
    }

    private FullHttpRequest notificationRequest(final String endpointToken, final Long version) {
        final FullHttpRequest req = new DefaultFullHttpRequest(HTTP_1_1, HttpMethod.PUT, "/update/" + endpointToken);
        if (version != null) {
            req.content().writeBytes(Unpooled.copiedBuffer("version=" + version.toString(), CharsetUtil.UTF_8));
        }
        return req;
    }
    private void doNotificationWithoutVersion(final String endpointToken,
                                                      final String channelId,
                                                      final SimplePushServer sps,
                                                      final EmbeddedChannel channel) throws Exception {
        doNotification(endpointToken, channelId, null, sps, channel);
    }

    private void doNotification(final String endpointToken,
                                        final String channelId,
                                        final Long version,
                                        final SimplePushServer sps,
                                        final EmbeddedChannel channel) throws Exception {
        final FullHttpRequest request = notificationRequest(endpointToken, version);
        final HttpResponse notificationResponse = sendNotification(request, sps);
        assertThat(notificationResponse.getStatus(), is(HttpResponseStatus.OK));

        final CountDownLatch countDownLatch = new CountDownLatch(1);
        while (countDownLatch.getCount() != 0) {
            final String str = channel.readOutbound();
            if (str == null) {
                Thread.sleep(200);
            } else {
                // The notification destined for the connected channel
                final NotificationMessageImpl notification = responseToType(str, NotificationMessageImpl.class);
                assertThat(notification.getMessageType(), is(MessageType.Type.NOTIFICATION));
                assertThat(notification.getAcks().size(), is(1));
                assertThat(notification.getAcks().iterator().next().getChannelId(), equalTo(channelId));
                if (version != null) {
                    assertThat(notification.getAcks().iterator().next().getVersion(), equalTo(version));
                } else {
                    final Date date = new Date(notification.getAcks().iterator().next().getVersion());
                    assertThat(date, is(notNullValue()));
                }
                countDownLatch.countDown();
            }
        }
    }

    private <T> T responseToType(final Object response, Class<T> type) {
        if (response instanceof String) {
            return JsonUtil.fromJson((String) response, type);
        }
        throw new IllegalArgumentException("Response is expected to be of type TextWebSocketFrame was: " + response);
    }

    private EmbeddedChannel createWebsocketChannel(SimplePushServer simplePushServer) {
        return new EmbeddedChannel(new NotificationHandler(simplePushServer));
    }

    private SockJsSessionContext channelSession(final EmbeddedChannel ch) {
        return new SockJsSessionContext() {
            @Override
            public void send(final String message) {
                ch.writeOutbound(message);
            }

            @Override
            public void close() {
                ch.close();
            }

            @Override
            public ChannelHandlerContext getContext() {
                return null;
            }

        };
    }

}
