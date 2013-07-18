/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.simplepush.server.netty;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.when;

import java.util.concurrent.TimeUnit;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.ScheduledFuture;

import org.jboss.aerogear.simplepush.server.DefaultSimplePushConfig;
import org.jboss.aerogear.simplepush.server.DefaultSimplePushServer;
import org.jboss.aerogear.simplepush.server.SimplePushServer;
import org.jboss.aerogear.simplepush.server.SimplePushServerConfig;
import org.jboss.aerogear.simplepush.server.datastore.InMemoryDataStore;
import org.junit.Test;

public class UserAgentReaperHandlerTest {

    @Test
    public void reaperJob() throws Exception {
        final UserAgentReaperHandler enabled = reaper(10L);
        enabled.handlerAdded(channelHandlerContext());
        assertThat(enabled.started(), equalTo(true));
    }

    private UserAgentReaperHandler reaper(final long timeout) {
        UserAgentReaperHandler reaper = new UserAgentReaperHandler(simplePushServer(DefaultSimplePushConfig.create().userAgentReaperTimeout(timeout).build()));
        return reaper;
    }

    private SimplePushServer simplePushServer(final SimplePushServerConfig config) {
        return new DefaultSimplePushServer(new InMemoryDataStore(), config);
    }

    @SuppressWarnings( { "rawtypes", "unchecked" })
    private ChannelHandlerContext channelHandlerContext() {
        final ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        final EventExecutor eventExecutor = mock(EventExecutor.class);
        final ScheduledFuture future = mock(ScheduledFuture.class);
        when(eventExecutor.scheduleAtFixedRate(any(Runnable.class), anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(future);
        when(ctx.executor()).thenReturn(eventExecutor);
        when(ctx.pipeline()).thenReturn(mock(ChannelPipeline.class));
        return ctx;
    }

}
