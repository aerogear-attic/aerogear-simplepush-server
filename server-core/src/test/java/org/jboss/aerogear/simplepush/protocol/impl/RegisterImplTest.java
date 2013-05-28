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
