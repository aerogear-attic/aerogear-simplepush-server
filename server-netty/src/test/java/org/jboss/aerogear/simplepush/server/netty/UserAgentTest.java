package org.jboss.aerogear.simplepush.server.netty;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import io.netty.channel.ChannelHandlerContext;

import java.util.Date;

import org.jboss.aerogear.simplepush.util.UUIDUtil;
import org.junit.Test;

public class UserAgentTest {

    @Test
    public void timestamp() {
        final UserAgent<ChannelHandlerContext> userAgent = new UserAgent<ChannelHandlerContext>(UUIDUtil.newUAID(), mock(ChannelHandlerContext.class), 1368781528407L);
        System.out.println(new Date(userAgent.timestamp()));
        final long addedTimeout = userAgent.timestamp() + 10000;
        System.out.println(new Date(addedTimeout));
        final long now = System.currentTimeMillis();
        System.out.println(new Date(now));
        assertThat(addedTimeout < now, is(true));
    }

}
