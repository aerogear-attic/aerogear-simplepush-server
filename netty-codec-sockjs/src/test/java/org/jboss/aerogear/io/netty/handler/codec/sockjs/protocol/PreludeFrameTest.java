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
package org.jboss.aerogear.io.netty.handler.codec.sockjs.protocol;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.jboss.aerogear.io.netty.handler.codec.sockjs.protocol.PreludeFrame;
import org.junit.Test;

public class PreludeFrameTest {

    @Test
    public void content() {
        final PreludeFrame preludeFrame = new PreludeFrame();
        assertThat(preludeFrame.content().capacity(), is(PreludeFrame.CONTENT_SIZE));
        assertThat(getContent(preludeFrame), equalTo(expectedContent(PreludeFrame.CONTENT_SIZE)));
    }

    private static byte[] getContent(final PreludeFrame preludeFrame) {
        final byte[] actualContent = new byte[PreludeFrame.CONTENT_SIZE];
        preludeFrame.content().readBytes(actualContent);
        return actualContent;
    }

    private static byte[] expectedContent(final int size) {
        final byte[] content = new byte[size];
        for (int i = 0; i < content.length; i++) {
            content[i] = 'h';
        }
        return content;
    }

}
