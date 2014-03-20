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

import org.jboss.aerogear.io.netty.handler.codec.sockjs.SockJsService;
import org.jboss.aerogear.io.netty.handler.codec.sockjs.SockJsSessionContext;
import org.jboss.aerogear.io.netty.handler.codec.sockjs.handler.SessionState.State;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

public class SockJsSessionTest {

    @Test
    public void setState() throws Exception {
        final SockJsService service = mock(SockJsService.class);
        final SockJsSession session = new SockJsSession("123", service);
        session.setState(State.OPEN);
        assertThat(session.getState(), is(State.OPEN));
    }

    @Test
    public void onOpen() throws Exception {
        final SockJsService service = mock(SockJsService.class);
        final SockJsSession sockJSSession = new SockJsSession("123", service);
        final SockJsSessionContext session = mock(SockJsSessionContext.class);
        sockJSSession.onOpen(session);
        verify(service).onOpen(session);
        assertThat(sockJSSession.getState(), is(State.OPEN));
    }

    @Test
    public void onMessage() throws Exception {
        final SockJsService service = mock(SockJsService.class);
        final SockJsSession sockJSSession = new SockJsSession("123", service);
        sockJSSession.onMessage("testing");
        verify(service).onMessage("testing");
    }

    @Test
    public void onClose() throws Exception {
        final SockJsService service = mock(SockJsService.class);
        final SockJsSession sockJSSession = new SockJsSession("123", service);
        sockJSSession.onClose();
        verify(service).onClose();
    }

    @Test
    public void addMessage() throws Exception {
        final SockJsService service = mock(SockJsService.class);
        final SockJsSession sockJSSession = new SockJsSession("123", service);
        sockJSSession.addMessage("hello");
        assertThat(sockJSSession.getAllMessages().size(), is(1));
    }

    @Test
    public void addMessages() throws Exception {
        final SockJsService service = mock(SockJsService.class);
        final SockJsSession sockJSSession = new SockJsSession("123", service);
        sockJSSession.addMessages(new String[]{"hello", "world"});
        final List<String> messages = sockJSSession.getAllMessages();
        assertThat(messages.size(), is(2));
        assertThat(messages.get(0), equalTo("hello"));
        assertThat(messages.get(1), equalTo("world"));
        assertThat(sockJSSession.getAllMessages().size(), is(0));
    }

}
