package org.jboss.aerogear.simplepush.protocol.impl;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.jboss.aerogear.simplepush.protocol.MessageType;
import org.jboss.aerogear.simplepush.protocol.impl.json.JsonUtil;
import org.junit.Test;

public class RegisterImplTest {

    @Test
    public void fromJson() {
        final String json = "{\"messageType\": \"register\", \"channelID\": \"2233df8\"}";
        final RegisterImpl register = JsonUtil.fromJson(json, RegisterImpl.class);
        assertThat(register.getMessageType(), is(equalTo(MessageType.Type.REGISTER)));
        assertThat(register.getChannelId(), is(equalTo("2233df8")));
    }
    
    @Test
    public void toJson() {
        final String asJson = JsonUtil.toJson(new RegisterImpl("2344dbc38"));
        final RegisterImpl register= JsonUtil.fromJson(asJson, RegisterImpl.class);
        assertThat(register.getMessageType(), is(equalTo(MessageType.Type.REGISTER)));
        assertThat(register.getChannelId(), is(equalTo("2344dbc38")));
    }

}
