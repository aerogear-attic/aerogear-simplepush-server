package org.jboss.aerogear.simplepush.server;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

public class DefaultChannelTest {

    @Test (expected = IllegalArgumentException.class)
    public void constructWithNegativeVersion() {
        new DefaultChannel("123abc", -1, "http://host/simple-push/endpoint/123abc");
    }
    
    @Test (expected = IllegalArgumentException.class)
    public void setVersionEqualToCurrentVersion() {
        final Channel channel = new DefaultChannel("123abc", 10L, "http://host/simple-push/endpoint/123abc");
        channel.setVersion(10);
    }
    
    @Test (expected = IllegalArgumentException.class)
    public void setVersionToLessThanCurrentVersion() {
        final Channel channel = new DefaultChannel("123abc", 10L, "http://host/simple-push/endpoint/123abc");
        channel.setVersion(2);
    }
    
    @Test 
    public void setVersion() {
        final Channel channel = new DefaultChannel("123abc", 10L, "http://host/simple-push/endpoint/123abc");
        channel.setVersion(11);
        assertThat(channel.getVersion(), is(11L));
    }

}
