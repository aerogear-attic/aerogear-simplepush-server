/**
 * JBoss, Home of Professional Open Source Copyright Red Hat, Inc., and individual contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package org.jboss.aerogear.simplepush.server;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

public class DefaultSimplePushServerConfigTest {

    @Test
    public void endpointUrl() {
        final SimplePushServerConfig config = DefaultSimplePushConfig.create().endpointPrefix("ep").password("dummy").build();
        assertThat(config.endpointPrefix(), equalTo("/ep"));
        assertThat(config.endpointUrl(), equalTo("http://127.0.0.1:7777/ep"));
    }

    @Test
    public void endpointHost() {
        final SimplePushServerConfig config = DefaultSimplePushConfig.create()
                .endpointPrefix("ep")
                .endpointHost("localhost")
                .password("dummy").build();
        assertThat(config.endpointHost(), equalTo("localhost"));
        assertThat(config.endpointUrl(), equalTo("http://localhost:7777/ep"));
    }

    @Test
    public void endpointPort() {
        final SimplePushServerConfig config = DefaultSimplePushConfig.create()
                .endpointPrefix("ep")
                .endpointPort(8888)
                .password("dummy").build();
        assertThat(config.endpointPort(), is(8888));
        assertThat(config.endpointUrl(), equalTo("http://127.0.0.1:8888/ep"));
    }

    @Test
    public void endpointPortNegative() {
        final SimplePushServerConfig config = DefaultSimplePushConfig.create()
                .endpointPrefix("ep")
                .endpointPort(-8888)
                .password("dummy").build();
        assertThat(config.endpointPort(), is(7777));
        assertThat(config.endpointUrl(), equalTo("http://127.0.0.1:7777/ep"));
    }

    @Test
    public void notifierMaxThreads() {
        final SimplePushServerConfig config = DefaultSimplePushConfig.create()
                .notifierMaxThreads(1)
                .endpointPrefix("ep")
                .password("dummy")
                .build();
        assertThat(config.notifierMaxThreads(), is(1));
    }
}
