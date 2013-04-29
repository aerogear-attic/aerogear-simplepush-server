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
package org.jboss.aerogear.simplepush.protocol.impl;

import static org.jboss.aerogear.simplepush.util.ArgumentUtil.checkNotNull;

import org.jboss.aerogear.simplepush.protocol.Update;

public class UpdateImpl implements Update {
    
    private final String channelId;
    private final Long version;

    public UpdateImpl(final String channelId, final Long version) {
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
    public Long getVersion() {
        return version;
    }
    
    @Override
    public String toString() {
        return "UpdateImpl[channelId=" + channelId + ", version=" + version + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((channelId == null) ? 0 : channelId.hashCode());
        result = prime * result + ((version == null) ? 0 : version.hashCode());
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
        if (getClass() != obj.getClass()) {
            return false;
        }
        final UpdateImpl other = (UpdateImpl) obj;
        if (channelId == null) {
            if (other.channelId != null) {
                return false;
            }
        } else if (!channelId.equals(other.channelId)) {
            return false;
        }
        if (version == null) {
            if (other.version != null) {
                return false;
            }
        } else if (!version.equals(other.version)) {
            return false;
        }
        return true;
    }

}
