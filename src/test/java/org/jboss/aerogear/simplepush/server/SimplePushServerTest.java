package org.jboss.aerogear.simplepush.server;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.jboss.aerogear.simplepush.protocol.HandshakeResponse;
import org.jboss.aerogear.simplepush.protocol.MessageType;
import org.jboss.aerogear.simplepush.protocol.RegisterResponse;
import org.jboss.aerogear.simplepush.protocol.impl.HandshakeImpl;
import org.jboss.aerogear.simplepush.protocol.impl.RegisterImpl;
import org.junit.Test;

public class SimplePushServerTest {

    @Test
    public void handleHandshake() {
        final SimplePushServer server = new SimplePushServer();
        final HandshakeResponse response = server.handleHandshake(new HandshakeImpl());
        assertThat(response.getUAID(), is(notNullValue()));
    }
    
    @Test
    public void handeRegister() {
        final SimplePushServer server = new SimplePushServer();
        final RegisterResponse response = server.handleRegister(new RegisterImpl("someChannelId"));
        assertThat(response.getChannelId(), equalTo("someChannelId"));
        assertThat(response.getMessageType(), equalTo(MessageType.Type.REGISTER));
        assertThat(response.getStatus().getCode(), equalTo(200));
        assertThat(response.getStatus().getMessage(), equalTo("OK"));
        assertThat(response.getPushEndpoint(), equalTo("/endpoint/someChannelId"));
    }

}
