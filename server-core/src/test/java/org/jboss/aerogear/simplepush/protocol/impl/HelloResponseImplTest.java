/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
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
import org.jboss.aerogear.simplepush.util.UUIDUtil;
import org.junit.Test;

public class HelloResponseImplTest {

    @Test(expected = NullPointerException.class)
    public void constructWithNullUAID() {
        new HelloResponseImpl(null);
    }

    @Test
    public void toJson() {
        final String uaid = UUIDUtil.newUAID();
        final HelloResponseImpl response = new HelloResponseImpl(uaid);
        final String json = JsonUtil.toJson(response);
        assertThat(json, equalTo("{\"messageType\":\"hello\",\"uaid\":\"" + uaid + "\"}"));
    }

}
