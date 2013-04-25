package org.jboss.aerogear.simplepush.protocol.impl;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.jboss.aerogear.simplepush.protocol.MessageType;
import org.jboss.aerogear.simplepush.protocol.Unregister;
import org.jboss.aerogear.simplepush.protocol.impl.json.JsonUtil;
import org.junit.Test;

public class UnregisterImplTest {

    @Test (expected = NullPointerException.class)
    public void construct() {
        new UnregisterImpl(null);
    }
    
    @Test
    public void fromJson() {
        final String json = "{\"messageType\": \"unregister\", \"channelID\": \"someChannelName\"}";
        final Unregister unregister = JsonUtil.fromJson(json, UnregisterImpl.class);
        assertThat(unregister.getMessageType(), is(equalTo(MessageType.Type.UNREGISTER)));
        assertThat(unregister.getChannelId(), is(equalTo("someChannelName")));
    }
    
    @Test
    public void toJson() {
        final String json = JsonUtil.toJson(new UnregisterImpl("someChannelName"));
        final Unregister unregister = JsonUtil.fromJson(json, UnregisterImpl.class);
        assertThat(unregister.getMessageType(), is(equalTo(MessageType.Type.UNREGISTER)));
        assertThat(unregister.getChannelId(), is(equalTo("someChannelName")));
    }

}
