/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors
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
package org.jboss.aerogear.simplepush.server.datastore.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * A simple data transfer object (DTO) for UserAgent information.
 */
@Entity
@Table (name = "useragents")
public final class UserAgentDTO implements Serializable {

    private static final long serialVersionUID = 4196926119759583672L;

    @Id
    private String uaid;

    @OneToMany (mappedBy = "userAgent", cascade = {CascadeType.ALL})
    private Set<ChannelDTO> channels;

    @OneToMany (mappedBy = "userAgent", cascade = {CascadeType.ALL})
    private Set<UpdateDTO> updates;

    /**
     * Only provided as a no-args constructor is required by JPA. Should not be call directly by client
     * code.
     */
    protected UserAgentDTO() {
    }

    public UserAgentDTO(final String uaid) {
        this.uaid = uaid;
    }

    public void addChannel(final String channelId, final long version, final String endpointUrl) {
        if (channels == null) {
            channels = new HashSet<ChannelDTO>();
        }
        channels.add(new ChannelDTO(this, channelId, version, endpointUrl));
    }

    public void setChannels(final Set<ChannelDTO> channels) {
        this.channels = channels;
    }

    public void setUpdates(final Set<UpdateDTO> updates) {
        this.updates = updates;
    }

    public Set<UpdateDTO> getUpdates() {
        return updates;
    }

    public String getUaid() {
        return uaid;
    }

    public Set<ChannelDTO> getChannels() {
        if (channels == null) {
            return Collections.emptySet();
        }
        return channels;
    }

}
