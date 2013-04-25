package org.jboss.aerogear.simplepush.server;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.jboss.aerogear.simplepush.util.UUIDUtil;
import org.junit.Test;

public class ChannelsTest {

    @Test
    public void addUserAgent() {
        final Channels channels = new Channels();
        final UUID uaid = UUIDUtil.createVersion4Id();
        channels.addUserAgent(uaid);
        assertThat(channels.getChannels(uaid).isEmpty(), is(true));
    }
    
    @Test
    public void addUserAgentWithChannel() {
        final Channels channels = new Channels();
        final UUID uaid = UUIDUtil.createVersion4Id();
        channels.addUserAgent(uaid, new HashSet<String>(Arrays.asList("someChannelName")));
        final Set<Channel> chs = channels.getChannels(uaid);
        assertThat(chs.size(), is(1));
        assertThat(chs, hasItem(new DefaultChannel("someChannelName", "/endpoint/someChannelName")));
    }

}
