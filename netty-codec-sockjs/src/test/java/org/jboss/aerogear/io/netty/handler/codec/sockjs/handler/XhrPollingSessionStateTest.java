/*
 * Copyright 2013 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License, version
 * 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.jboss.aerogear.io.netty.handler.codec.sockjs.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.*;
import org.jboss.aerogear.io.netty.handler.codec.sockjs.SockJsConfig;
import org.jboss.aerogear.io.netty.handler.codec.sockjs.SockJsService;
import org.junit.Test;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.mockito.Mockito.*;

public class XhrPollingSessionStateTest {

    @Test
    public void flushMessagedRepsondNoContent() {
        final EmbeddedChannel channel = new EmbeddedChannel();
        final PollingSessionState pollingSessionState = createPollingSessionState();
        final ChannelHandlerContext ctx = contextForChannel(channel);
        final SockJsSession sockJsSession = new SockJsSession("1234", mock(SockJsService.class));
        pollingSessionState.onOpen(sockJsSession, ctx);
        final HttpResponse response = channel.readOutbound();
        assertThat(response.getStatus(), is(OK));
    }

    private PollingSessionState createPollingSessionState() {
        final ConcurrentMap<String, SockJsSession> sessions = new ConcurrentHashMap<String, SockJsSession>();
        final HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "/123/456/xhr");
        final SockJsConfig config = SockJsConfig.withPrefix("serviceName").build();
        final PollingSessionState pollingSessionState = new XhrPollingSessionState(sessions, request, config);
        return pollingSessionState;
    }

    private ChannelHandlerContext contextForChannel(final EmbeddedChannel ch) {
        final ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        when(ctx.channel()).thenReturn(ch);
        return ctx;
    }

}
