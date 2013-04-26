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
