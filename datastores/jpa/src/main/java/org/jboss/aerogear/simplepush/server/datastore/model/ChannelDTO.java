/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
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

/**
 * A simple data transfer object (DTO) for Channel information.
 */
@Entity
@Table (name = "channels")
public final class ChannelDTO implements Serializable {

    private static final long serialVersionUID = -1092289737919705375L;

    @Id
    private String channelId;
    private long version;
    private String endpointUrl;

    @ManyToOne
    @JoinColumn (name = "useragent_fk")
    private UserAgentDTO userAgent;

    /**
     * Only provided as a no-args constructor is required by JPA.
     * Should not be call directly by client code.
     */
    protected ChannelDTO() {
    }

    public ChannelDTO(final UserAgentDTO userAgent, final String channelId, final long version, final String endpointUrl) {
        this.userAgent = userAgent;
        this.channelId = channelId;
        this.version = version;
        this.endpointUrl = endpointUrl;
    }

    public String getChannelId() {
        return channelId;
    }

    public long getVersion() {
        return version;
    }

    public String getEndpointUrl() {
        return endpointUrl;
    }

    public UserAgentDTO getUserAgent() {
        return userAgent;
    }

    @Override
    public String toString() {
        return "Channel[channelId=" + channelId + ", uaid=" + userAgent.getUaid().toString() + ", version=" + version + ", endpointUrl=" + endpointUrl + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((channelId == null) ? 0 : channelId.hashCode());
        result = prime * result + ((endpointUrl == null) ? 0 : endpointUrl.hashCode());
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

        final ChannelDTO other = (ChannelDTO) obj;
        return channelId == null ? other.channelId == null : channelId.equals(other.channelId) &&
                endpointUrl == null ? other.endpointUrl == null : endpointUrl.equals(other.endpointUrl);
    }

}
