/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
