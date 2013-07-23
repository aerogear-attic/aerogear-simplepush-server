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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.sockjs.SessionContext;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.jboss.aerogear.simplepush.protocol.impl.HandshakeMessageImpl;
import org.jboss.aerogear.simplepush.protocol.impl.RegisterMessageImpl;
import org.jboss.aerogear.simplepush.server.DefaultSimplePushConfig;
import org.jboss.aerogear.simplepush.server.DefaultSimplePushServer;
import org.jboss.aerogear.simplepush.server.SimplePushServer;
import org.jboss.aerogear.simplepush.server.SimplePushServerConfig;
import org.jboss.aerogear.simplepush.server.datastore.InMemoryDataStore;
import org.jboss.aerogear.simplepush.util.UUIDUtil;
import org.junit.Before;
import org.junit.Test;

public class UserAgentReaperTest {

    @Before
    public void clearUserAgents() {
        UserAgents.getInstance().all().clear();
    }

    @Test
    public void reapActiveUserAgent() throws InterruptedException {
        final String uaid = UUIDUtil.newUAID();
        final SimplePushServer simplePushServer = simplePushServer();
        final SessionContext sessionContext = newSessionContext(true);
        doRegister(uaid, simplePushServer);
        addUserAgent(uaid, sessionContext);

        exceute(new UserAgentReaper(simplePushServer));
        verify(sessionContext, never()).close();
        assertThat(UserAgents.getInstance().get(uaid), is(notNullValue()));
    }

    @Test(expected = IllegalStateException.class)
    public void reapInactiveUserAgent() throws InterruptedException {
        final String uaid = UUIDUtil.newUAID();
        final SimplePushServer simplePushServer = simplePushServer();
        final SessionContext sessionContext = newSessionContext(false);
        doRegister(uaid, simplePushServer);
        addUserAgent(uaid, sessionContext);

        exceute(new UserAgentReaper(simplePushServer));
        verify(sessionContext).close();
        UserAgents.getInstance().get(uaid);
    }

    private SessionContext newSessionContext(final boolean active) {
        final Channel channel = mock(Channel.class);
        when(channel.isActive()).thenReturn(active);
        when(channel.isRegistered()).thenReturn(active);
        final ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        when(ctx.channel()).thenReturn(channel);
        final SessionContext sessionContext = mock(SessionContext.class);
        when(sessionContext.getContext()).thenReturn(ctx);
        return sessionContext;
    }

    private void doRegister(final String uaid, final SimplePushServer server) {
        server.handleHandshake(new HandshakeMessageImpl(uaid.toString()));
        server.handleRegister(new RegisterMessageImpl(uaid.toString()), uaid);

    }

    private void addUserAgent(final String uaid, final SessionContext sessionContext) throws InterruptedException {
        UserAgents.getInstance().add(uaid, sessionContext);
        // When a UserAgent is added a timestap will be added. We need to allow for some time to pass to simulate
        // an inactive client.
        Thread.sleep(1000);
    }

    private void exceute(final UserAgentReaper reaper) throws InterruptedException {
        final ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(reaper);
        executorService.awaitTermination(500, TimeUnit.MILLISECONDS);
    }

    private SimplePushServer simplePushServer() {
        final SimplePushServerConfig config = DefaultSimplePushConfig.create().userAgentReaperTimeout(20L).build();
        return new DefaultSimplePushServer(new InMemoryDataStore(), config);
    }

}
