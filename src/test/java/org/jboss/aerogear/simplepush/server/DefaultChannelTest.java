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
package org.jboss.aerogear.simplepush.server;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.jboss.aerogear.simplepush.util.UUIDUtil;
import org.junit.Test;

public class DefaultChannelTest {

    @Test (expected = IllegalArgumentException.class)
    public void constructWithNegativeVersion() {
        new DefaultChannel(UUIDUtil.createVersion4Id(), "123abc", -1, "http://host/simple-push/endpoint/123abc");
    }
    
    @Test (expected = IllegalArgumentException.class)
    public void setVersionEqualToCurrentVersion() {
        final Channel channel = new DefaultChannel(UUIDUtil.createVersion4Id(), "123abc", 10L, "http://host/simple-push/endpoint/123abc");
        channel.setVersion(10);
    }
    
    @Test (expected = IllegalArgumentException.class)
    public void setVersionToLessThanCurrentVersion() {
        final Channel channel = new DefaultChannel(UUIDUtil.createVersion4Id(), "123abc", 10L, "http://host/simple-push/endpoint/123abc");
        channel.setVersion(2);
    }
    
    @Test 
    public void setVersion() {
        final Channel channel = new DefaultChannel(UUIDUtil.createVersion4Id(), "123abc", 10L, "http://host/simple-push/endpoint/123abc");
        channel.setVersion(11);
        assertThat(channel.getVersion(), is(11L));
    }

}
