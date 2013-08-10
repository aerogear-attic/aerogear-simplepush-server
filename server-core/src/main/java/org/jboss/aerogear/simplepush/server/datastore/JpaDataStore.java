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
package org.jboss.aerogear.simplepush.server.datastore;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.persistence.Query;

import org.jboss.aerogear.simplepush.protocol.Update;
import org.jboss.aerogear.simplepush.protocol.impl.UpdateImpl;
import org.jboss.aerogear.simplepush.server.Channel;
import org.jboss.aerogear.simplepush.server.DefaultChannel;
import org.jboss.aerogear.simplepush.server.datastore.model.ChannelDTO;
import org.jboss.aerogear.simplepush.server.datastore.model.UpdateDTO;
import org.jboss.aerogear.simplepush.server.datastore.model.UserAgentDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A DataStore implementation that use Java Persistence API (JPA) to store data for the SimplePush Server.
 */
public final class JpaDataStore implements DataStore {

    private final Logger logger = LoggerFactory.getLogger(JpaDataStore.class);
    private final JpaExecutor jpaExecutor;

    /**
     * Sole constructor.
     *
     * @param persistenceUnit the name of the persistence unit to be used.
     */
    public JpaDataStore(final String persistenceUnit) {
        jpaExecutor = new JpaExecutor(Persistence.createEntityManagerFactory(persistenceUnit));
    }

    @Override
    public boolean saveChannel(final Channel channel) {
        final JpaOperation<Boolean> saveChannel = new JpaOperation<Boolean>() {
            @Override
            public Boolean perform(final EntityManager em) {
                UserAgentDTO userAgent = em.find(UserAgentDTO.class, channel.getUAID().toString());
                if (userAgent == null) {
                    userAgent = new UserAgentDTO(channel.getUAID().toString());
                }
                userAgent.addChannel(channel.getChannelId(), channel.getVersion(), channel.getPushEndpoint());
                em.merge(userAgent);
                return Boolean.TRUE;
            }
        };
        try {
            return jpaExecutor.execute(saveChannel);
        } catch (final Exception e) {
            logger.error("Could not save channel [" + channel.getChannelId() + "]", e);
            return false;
        }
    }

    @Override
    public Channel getChannel(final String channelId) throws ChannelNotFoundException {
        final JpaOperation<ChannelDTO> findChannel = new JpaOperation<ChannelDTO>() {
            @Override
            public ChannelDTO perform(EntityManager em) {
                return em.find(ChannelDTO.class, channelId);
            }
        };
        final ChannelDTO dto = jpaExecutor.execute(findChannel);
        if (dto == null) {
            throw new ChannelNotFoundException("No Channel for [" + channelId + "] was found", channelId);
        }
        return new DefaultChannel(dto.getUserAgent().getUaid(), dto.getChannelId(), dto.getVersion(), dto.getEndpointUrl());
    }

    @Override
    public boolean removeChannel(final String channelId) {
        final JpaOperation<Boolean> removeChannel = new JpaOperation<Boolean>() {
            @Override
            public Boolean perform(EntityManager em) {
                final Query delete = em.createQuery("DELETE from ChannelDTO c where c.channelId = :channelId");
                delete.setParameter("channelId", channelId);
                final int nr =  delete.executeUpdate();
                return nr == 1 ? Boolean.TRUE : Boolean.FALSE;
            }
        };
        try {
            return jpaExecutor.execute(removeChannel);
        } catch (final Exception e) {
            logger.error("Could not remove channel [" + channelId + "]", e);
            return false;
        }
    }

    @Override
    public void removeChannels(final String uaid) {
        final JpaOperation<Void> removeChannels = new JpaOperation<Void>() {
            @Override
            public Void perform(final EntityManager em) {
                final UserAgentDTO userAgent = em.find(UserAgentDTO.class, uaid);
                if (userAgent != null) {
                    final Set<ChannelDTO> channels = userAgent.getChannels();
                    for (ChannelDTO channelDTO : channels) {
                        em.remove(channelDTO);
                    }
                    channels.clear();
                    userAgent.setChannels(channels);
                }
                return null;
            }
        };
        jpaExecutor.execute(removeChannels);
        logger.debug("Deleted all channels for UserAgent [" + uaid + "]");
    }

    @Override
    public void saveUpdates(final Set<Update> updates, final String uaid) {
        final JpaOperation<Void> saveUpdates = new JpaOperation<Void>() {
            @Override
            public Void perform(final EntityManager em) {
                final UserAgentDTO userAgent = em.find(UserAgentDTO.class, uaid);
                final Set<UpdateDTO> dtos = new HashSet<UpdateDTO>(updates.size());
                for (Update update : updates) {
                    dtos.add(new UpdateDTO(userAgent, update.getChannelId(), update.getVersion()));
                }
                userAgent.setUpdates(dtos);
                em.merge(userAgent);
                return null;
            }
        };
        jpaExecutor.execute(saveUpdates);
    }

    @Override
    public Set<Update> getUpdates(final String uaid) {
        final JpaOperation<Set<Update>> getUpdates = new JpaOperation<Set<Update>>() {
            @Override
            public Set<Update> perform(final EntityManager em) {
                final UserAgentDTO userAgent = em.find(UserAgentDTO.class, uaid);
                if (userAgent == null) {
                    return Collections.emptySet();
                }
                final HashSet<Update> updates = new HashSet<Update>();
                for (UpdateDTO updateDTO : userAgent.getUpdates()) {
                    updates.add(new UpdateImpl(updateDTO.getChannelId(), updateDTO.getVersion()));
                }
                return updates;
            }
        };
        return jpaExecutor.execute(getUpdates);
    }

    @Override
    public boolean removeUpdate(final Update update, final String uaid) {
        final JpaOperation<Boolean> removeUpdate = new JpaOperation<Boolean>() {
            @Override
            public Boolean perform(final EntityManager em) {
                final UserAgentDTO userAgent = em.find(UserAgentDTO.class, uaid);
                final Set<UpdateDTO> updatesDtos = userAgent.getUpdates();
                UpdateDTO toRemove = null;
                for (UpdateDTO updateDTO : updatesDtos) {
                    if (update.getChannelId().equals(updateDTO.getChannelId())) {
                        toRemove = updateDTO;
                        break;
                    }
                }
                if (toRemove == null) {
                    return Boolean.FALSE;
                }
                em.remove(toRemove);
                updatesDtos.remove(toRemove);
                userAgent.setUpdates(updatesDtos);
                return Boolean.TRUE;
            }
        };
        try {
            return jpaExecutor.execute(removeUpdate);
        } catch (final Exception e) {
            logger.error("Could not remove update [" + update + "]", e);
            return false;
        }
    }

}
