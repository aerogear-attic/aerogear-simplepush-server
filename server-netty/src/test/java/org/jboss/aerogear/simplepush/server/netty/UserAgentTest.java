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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import io.netty.channel.ChannelHandlerContext;

import org.jboss.aerogear.simplepush.util.UUIDUtil;
import org.junit.Test;

public class UserAgentTest {

    @Test
    public void timestamp() {
        final UserAgent<ChannelHandlerContext> userAgent = new UserAgent<ChannelHandlerContext>(UUIDUtil.newUAID(), mock(ChannelHandlerContext.class), 1368781528407L);
        final long addedTimeout = userAgent.timestamp() + 10000;
        final long now = System.currentTimeMillis();
        assertThat(addedTimeout < now, is(true));
    }

}
