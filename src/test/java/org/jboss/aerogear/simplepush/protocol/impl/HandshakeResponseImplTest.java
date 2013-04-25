package org.jboss.aerogear.simplepush.protocol.impl;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.UUID;

import org.jboss.aerogear.simplepush.protocol.impl.json.JsonUtil;
import org.jboss.aerogear.simplepush.util.UUIDUtil;
import org.junit.Test;

public class HandshakeResponseImplTest {

    @Test (expected = NullPointerException.class)
    public void constructWithNullUAID() {
        new HandshakeResponseImpl(null);
    }
    
    @Test
    public void toJson() {
        final UUID uaid = UUIDUtil.createVersion4Id();
        final HandshakeResponseImpl response = new HandshakeResponseImpl(uaid);
        final String json = JsonUtil.toJson(response);
        assertThat(json, equalTo("{\"messageType\":\"hello\",\"uaid\":\"" + uaid + "\"}"));
    }

}
