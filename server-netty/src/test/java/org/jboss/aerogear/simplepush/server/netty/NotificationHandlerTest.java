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

import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.sockjs.SessionContext;
import io.netty.util.CharsetUtil;

import java.util.UUID;

import org.jboss.aerogear.simplepush.protocol.MessageType;
import org.jboss.aerogear.simplepush.protocol.RegisterResponse;
import org.jboss.aerogear.simplepush.protocol.impl.NotificationMessageImpl;
import org.jboss.aerogear.simplepush.protocol.impl.RegisterMessageImpl;
import org.jboss.aerogear.simplepush.protocol.impl.json.JsonUtil;
import org.jboss.aerogear.simplepush.server.DefaultSimplePushServer;
import org.jboss.aerogear.simplepush.server.DefaultSimplePushConfig;
import org.jboss.aerogear.simplepush.server.SimplePushServer;
import org.jboss.aerogear.simplepush.server.datastore.InMemoryDataStore;
import org.jboss.aerogear.simplepush.util.UUIDUtil;
import org.junit.Test;

public class NotificationHandlerTest {

    @Test
    public void notification() throws Exception {
        final String uaid = UUIDUtil.newUAID();
        final String channelId = UUID.randomUUID().toString();
        final SimplePushServer simplePushServer = defaultPushServer();
        final EmbeddedChannel channel = createWebsocketChannel(simplePushServer);
        registerUserAgent(uaid, channel);
        doRegister(channelId, uaid, simplePushServer);
        doNotification(channelId, 1L, channel);
        channel.close();
    }

    @Test
    public void notificationVersionEqualToCurrentVersion() throws Exception {
        final String uaid = UUIDUtil.newUAID();
        final String channelId = UUID.randomUUID().toString();
        final SimplePushServer simplePushServer = defaultPushServer();
        final EmbeddedChannel channel = createWebsocketChannel(simplePushServer);
        registerUserAgent(uaid, channel);
        doRegister(channelId, uaid, simplePushServer);
        doNotification(channelId, 1L, channel);

        final HttpResponse response = sendNotification(channelId, 1L, simplePushServer);
        assertThat(response.getStatus(), is(HttpResponseStatus.BAD_REQUEST));
        channel.close();
    }

    @Test
    public void notificationVersionLessThanCurrent() throws Exception {
        final String uaid = UUIDUtil.newUAID();
        final String channelId = UUID.randomUUID().toString();
        final SimplePushServer simplePushServer = defaultPushServer();
        final EmbeddedChannel channel = createWebsocketChannel(simplePushServer);
        registerUserAgent(uaid, channel);
        doRegister(channelId, uaid, simplePushServer);
        doNotification(channelId, 10L, channel);

        final HttpResponse response = sendNotification(channelId, 9L, simplePushServer);
        assertThat(response.getStatus(), is(HttpResponseStatus.BAD_REQUEST));
        channel.close();
    }

    private SimplePushServer defaultPushServer() {
        return new DefaultSimplePushServer(new InMemoryDataStore(), DefaultSimplePushConfig.defaultConfig());
    }

    private HttpResponse sendNotification(final String channelId, final long version, final SimplePushServer simplePushServer) throws Exception {
        final EmbeddedChannel ch = createWebsocketChannel(simplePushServer);
        ch.writeInbound(notificationRequest(channelId, 9L));
        return (HttpResponse) ch.readOutbound();
    }

    private void registerUserAgent(final String uaid, final EmbeddedChannel ch) {
        UserAgents.getInstance().add(uaid, channelSession(ch));
    }

    private RegisterResponse doRegister(final String channelId, final String uaid, final SimplePushServer server) throws Exception {
        return server.handleRegister(new RegisterMessageImpl(channelId), uaid);
    }

    private FullHttpRequest notificationRequest(final String channelId, final Long version) {
        final FullHttpRequest req = new DefaultFullHttpRequest(HTTP_1_1, HttpMethod.PUT, "/endpoint/" + channelId);
        req.content().writeBytes(Unpooled.copiedBuffer("version=" + version.toString(), CharsetUtil.UTF_8));
        return req;
    }

    private HttpResponse doNotification(final String channelId, final Long version, final EmbeddedChannel channel) throws Exception {
        channel.writeInbound(notificationRequest(channelId, version));

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

    private <T> T responseToType(final Object response, Class<T> type) {
        if (response instanceof String) {
            return JsonUtil.fromJson((String) response, type);
        }
        throw new IllegalArgumentException("Response is expected to be of type TextWebSocketFrame was: " + response);
    }

    private EmbeddedChannel createWebsocketChannel(SimplePushServer simplePushServer) throws Exception {
        return new EmbeddedChannel(new NotificationHandler(simplePushServer));
    }

    private SessionContext channelSession(final EmbeddedChannel ch) {
        return new SessionContext() {
            @Override
            public void send(String message) {
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
