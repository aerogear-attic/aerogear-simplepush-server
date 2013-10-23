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
package org.jboss.aerogear.simplepush.server;

import static org.jboss.aerogear.simplepush.util.ArgumentUtil.checkNotNegative;
import static org.jboss.aerogear.simplepush.util.ArgumentUtil.checkNotNull;

public class DefaultChannel implements Channel {

    private final String uaid;
    private final String channelId;
    private final String endpointToken;
    private final long version;

    public DefaultChannel(final String uaid, final String channelId, final String endpointToken) {
        this(uaid, channelId, 0, endpointToken);
    }

    public DefaultChannel(final String uaid, final String channelId, final long version, final String endpointToken) {
        checkNotNull(uaid, "uaid");
        checkNotNull(channelId, "channelId");
        checkNotNegative(version, "version");
        checkNotNull(endpointToken, "endpointToken");
        this.uaid = uaid;
        this.channelId = channelId;
        this.version = version;
        this.endpointToken = endpointToken;
    }

    @Override
    public String getUAID() {
        return uaid;
    }

    @Override
    public String getChannelId() {
        return channelId;
    }

    @Override
    public long getVersion() {
        return version;
    }

    @Override
    public String getEndpointToken() {
        return endpointToken;
    }

    @Override
    public String toString() {
        return "DefaultChannel[uaid=" + uaid + ", channelId=" + channelId + ", version=" + version + ", pushEndpoint=" + endpointToken + "]";
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + ((channelId == null) ? 0 : channelId.hashCode());
        result = 31 * result + ((endpointToken == null) ? 0 : endpointToken.hashCode());
        result = 31 * result + (int) (version ^ (version >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Channel)) {
            return false;
        }
        final DefaultChannel o = (DefaultChannel) obj;
        return (uaid == null ? o.uaid == null : uaid.equals(o.uaid)) &&
            (channelId == null ? o.channelId == null : channelId.equals(o.channelId)) &&
            (endpointToken == null ? o.endpointToken == null : endpointToken.equals(o.endpointToken)) &&
            (version == o.version);
    }

}
