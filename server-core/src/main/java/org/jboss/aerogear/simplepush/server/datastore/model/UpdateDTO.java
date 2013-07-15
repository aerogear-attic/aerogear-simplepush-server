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
package org.jboss.aerogear.simplepush.server.datastore.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table (name = "updates")
public final class UpdateDTO implements Serializable {

    private static final long serialVersionUID = -3557132969354495839L;

    @Id
    private String channelId;

    @ManyToOne
    @JoinColumn (name = "useragent_fk")
    private UserAgentDTO userAgent;
    private long version;

    protected UpdateDTO() {
    }

    public UpdateDTO(final UserAgentDTO userAgent, final String channelId, final long version) {
        this.userAgent = userAgent;
        this.channelId = channelId;
        this.version = version;
    }

    public UserAgentDTO getUserAgent() {
        return userAgent;
    }

    public String getChannelId() {
        return channelId;
    }

    public long getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return "UpdateDTO[userAgent=" + userAgent + ", channelId=" + channelId + ", version=" + version + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((channelId == null) ? 0 : channelId.hashCode());
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
        final UpdateDTO other = (UpdateDTO) obj;
        return channelId == null ? other.channelId == null : channelId.equals(other.channelId);
    }

}
