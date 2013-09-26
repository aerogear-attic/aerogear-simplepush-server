/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.simplepush.server.datastore.model;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class ChannelDTOTest {

    private final String uaid = "1234";
    private final String channelId = "abcd1234";
    private final long version = 1;
    private final String endpointUrl = "/endpoint/" + channelId;

    @Test
    public void equalsContractReflexive() {
        final ChannelDTO x = new ChannelDTO(new UserAgentDTO(uaid), channelId, version, endpointUrl);
        assertThat(x.equals(x), is(true));
    }

    @Test
    public void equalsContractSymmetric() {
        final ChannelDTO x = new ChannelDTO(new UserAgentDTO(uaid), channelId, version, endpointUrl);
        final ChannelDTO y = new ChannelDTO(new UserAgentDTO(uaid), channelId, version, endpointUrl);
        assertThat(x.equals(y), is(true));
        assertThat(y.equals(x), is(true));
    }

    @Test
    public void equalsContractTransative() {
        final ChannelDTO x = new ChannelDTO(new UserAgentDTO(uaid), channelId, version, endpointUrl);
        final ChannelDTO y = new ChannelDTO(new UserAgentDTO(uaid), channelId, version, endpointUrl);
        final ChannelDTO z = new ChannelDTO(new UserAgentDTO(uaid), channelId, version, endpointUrl);
        assertThat(x.equals(y), is(true));
        assertThat(y.equals(z), is(true));
        assertThat(x.equals(z), is(true));
    }

    @Test
    public void equalsContractConsistency() {
        final ChannelDTO x = new ChannelDTO(new UserAgentDTO(uaid), channelId, version, endpointUrl);
        final ChannelDTO y = new ChannelDTO(new UserAgentDTO(uaid), channelId, version, endpointUrl);
        assertThat(x.equals(y), is(true));
        assertThat(y.equals(x), is(true));
        x.getUserAgent().addChannel("9999", 11L, "/endpoint/9999");
        assertThat(x.equals(y), is(true));
    }

    @Test
    public void equalsContractNull() {
        final UpdateDTO x = new UpdateDTO(new UserAgentDTO(uaid), channelId, version);
        assertThat(x.equals(null), is(false));
    }
}
