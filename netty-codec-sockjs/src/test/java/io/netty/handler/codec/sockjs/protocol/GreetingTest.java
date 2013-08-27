/*
 * Copyright 2013 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License, version
 * 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package io.netty.handler.codec.sockjs.protocol;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;

import java.nio.charset.Charset;

import org.junit.Test;

public class GreetingTest {

    @Test
    public void greeting() throws Exception {
        final FullHttpResponse response = Greeting.response(createHttpRequest("/simplepush"));
        assertThat(response.content().toString(CharsetUtil.UTF_8), equalTo("Welcome to SockJS!\n"));
    }

    private HttpRequest createHttpRequest(final String prefix) {
        return new DefaultFullHttpRequest(HttpVersion.HTTP_1_1,
                HttpMethod.GET, prefix + "/",
                Unpooled.copiedBuffer("", Charset.defaultCharset()));
    }

}
