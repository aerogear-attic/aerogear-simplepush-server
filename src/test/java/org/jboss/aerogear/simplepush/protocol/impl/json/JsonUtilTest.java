package org.jboss.aerogear.simplepush.protocol.impl.json;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.UUID;

import org.jboss.aerogear.simplepush.protocol.MessageType;
import org.jboss.aerogear.simplepush.util.UUIDUtil;
import org.junit.Test;

public class JsonUtilTest {

    @Test
    public void parseFrame() {
        final UUID uaid = UUIDUtil.createVersion4Id();
        final String json = "{\"messageType\": \"hello\", \"uaid\": \"" + uaid + "\", \"channelIDs\": [\"123abc\", \"efg456\"]}";
        final MessageType messageType = JsonUtil.parseFrame(json);
        assertThat(messageType.getMessageType(), is(equalTo(MessageType.Type.HELLO)));
    }

}
