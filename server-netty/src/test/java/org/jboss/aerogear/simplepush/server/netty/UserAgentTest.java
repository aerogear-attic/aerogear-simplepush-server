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
