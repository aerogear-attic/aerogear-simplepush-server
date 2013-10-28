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

import static org.jboss.aerogear.simplepush.util.ArgumentUtil.checkNotNull;

import org.jboss.aerogear.simplepush.protocol.Ack;

/**
 * Concrete {@link Ack} implementation.
 *
 * This implementation considers only the channelId for hashcode/equals contract. If required
 * to take the version into consideration an manual call of getVersion is required.
 */
public class AckImpl implements Ack {

    private final String channelId;
    private final long version;

    public AckImpl(final String channelId, final long version) {
        checkNotNull(channelId, "channelId");
        checkNotNull(version, "version");
        this.channelId = channelId;
        this.version = version;
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
    public String toString() {
        return "AckImpl[channelId=" + channelId + ", version=" + version + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((channelId == null) ? 0 : channelId.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Ack)) {
            return false;
        }
        final AckImpl other = (AckImpl) obj;
        return channelId == null ? other.channelId == null : channelId.equals(other.channelId);
    }

}
