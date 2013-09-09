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
package org.jboss.aerogear.simplepush.server.netty;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.jboss.aerogear.simplepush.server.DefaultSimplePushConfig;
import org.jboss.aerogear.simplepush.server.SimplePushServerConfig;
import org.junit.Test;

public class DefaultSimplePushConfigTest {

    @Test
    public void buildConfig() {
        final SimplePushServerConfig config = DefaultSimplePushConfig.create()
                .userAgentReaperTimeout(1000L)
                .ackInterval(60000L)
                .tokenKey("test")
                .build();
        assertThat(config.notificationUrl(), equalTo("http://127.0.0.1:7777/update"));
        assertThat(config.endpointPrefix(), equalTo("/update"));
        assertThat(config.userAgentReaperTimeout(), is(1000L));
        assertThat(config.acknowledmentInterval(), is(60000L));
    }

    @Test
    public void buildConfigWithNullUserAgentReaperTimeout() {
        final SimplePushServerConfig config = DefaultSimplePushConfig.create().userAgentReaperTimeout(null)
                .tokenKey("test")
                .build();
        assertThat(config.userAgentReaperTimeout(), is(604800000L));
    }

    @Test
    public void buildConfigWithNullAckInterval() {
        final SimplePushServerConfig config = DefaultSimplePushConfig.create().ackInterval(null)
                .tokenKey("test")
                .build();
        assertThat(config.acknowledmentInterval(), is(60000L));
    }

}
