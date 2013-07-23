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

import java.util.concurrent.atomic.AtomicLong;

public class DefaultChannel implements Channel {

    private final String uaid;
    private final String channelId;
    private final String pushEndpoint;
    private final AtomicLong version;

    public DefaultChannel(final String uaid, final String channelId, final String pushEndpoint) {
        this(uaid, channelId, 0, pushEndpoint);
    }

    public DefaultChannel(final String uaid, final String channelId, final long version, final String pushEndpoint) {
        checkNotNull(uaid, "uaid");
        checkNotNull(channelId, "channelId");
        checkNotNegative(version, "version");
        checkNotNull(pushEndpoint, "pushEndpoint");
        this.uaid = uaid;
        this.channelId = channelId;
        this.version = new AtomicLong(version);
        this.pushEndpoint = pushEndpoint;
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
        return version.get();
    }

    @Override
    public void setVersion(final long newVersion) {
        for (;;) {
            final long currentVersion = version.get();
            if (newVersion <= currentVersion) {
                throw new IllegalArgumentException("New version [" + newVersion + "] must be greater than current version [" + currentVersion + "]");
            }
            if (version.compareAndSet(currentVersion, newVersion)) {
                break;
            }
        }
    }

    @Override
    public String getPushEndpoint() {
        return pushEndpoint;
    }

    @Override
    public String toString() {
        return "DefaultChannel[uaid=" + uaid + ", channelId=" + channelId + ", version=" + version + ", pushEndpoint=" + pushEndpoint + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((channelId == null) ? 0 : channelId.hashCode());
        result = prime * result + ((pushEndpoint == null) ? 0 : pushEndpoint.hashCode());
        result = prime * result + ((version.toString() == null) ? 0 : version.toString().hashCode());
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
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DefaultChannel other = (DefaultChannel) obj;
        if (channelId == null) {
            if (other.channelId != null) {
                return false;
            }
        } else if (!channelId.equals(other.channelId)) {
            return false;
        }
        if (pushEndpoint == null) {
            if (other.pushEndpoint != null) {
                return false;
            }
        } else if (!pushEndpoint.equals(other.pushEndpoint)) {
            return false;
        }
        if (version == null) {
            if (other.version != null) {
                return false;
            }
        } else if (version.longValue() != other.version.longValue()) {
            return false;
        }
        return true;
    }

}
