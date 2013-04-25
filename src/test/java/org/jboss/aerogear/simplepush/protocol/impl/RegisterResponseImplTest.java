package org.jboss.aerogear.simplepush.protocol.impl;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.jboss.aerogear.simplepush.protocol.impl.json.JsonUtil;
import org.junit.Test;

public class RegisterResponseImplTest {

    @Test
    public void toJson() {
        final RegisterResponseImpl response = new RegisterResponseImpl("someChannel", new StatusImpl(400, "wrong"), "/endpoint/1234");
        final String json = JsonUtil.toJson(response);
        assertThat(json, equalTo("{\"messageType\":\"register\",\"channelID\":\"someChannel\",\"status\":400,\"pushEndpoint\":\"/endpoint/1234\"}"));
    }

}
