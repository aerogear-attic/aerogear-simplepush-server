/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.simplepush.server.netty;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

public class ConfigTest {
    
    @Test (expected = NullPointerException.class)
    public void shouldThrowIfPathIsNull() {
        Config.path(null);
    }
    
    @Test (expected = IllegalArgumentException.class)
    public void shouldThrowIfPathIsEmpty() {
        Config.path("");
    }

    @Test
    public void buildConfig() {
        final Config config = Config.path("/simplepush")
                .tls(true)
                .subprotocol("notification")
                .endpointUrl("/endpoint")
                .userAgentReaperTimeout(1000)
                .build();
        assertThat(config.path(), equalTo("/simplepush"));
        assertThat(config.tls(), is(true));
        assertThat(config.subprotocol(), equalTo("notification"));
        assertThat(config.endpointUrl(), equalTo("/endpoint"));
        assertThat(config.reaperTimeout(), is(1000L));
    }

}
