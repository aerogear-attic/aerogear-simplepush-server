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

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.jboss.aerogear.simplepush.protocol.Ack;
import org.jboss.aerogear.simplepush.protocol.impl.AckImpl;
import org.jboss.aerogear.simplepush.server.Channel;
import org.jboss.aerogear.simplepush.server.DefaultChannel;
import org.jboss.aerogear.simplepush.server.datastore.model.AckDTO;
import org.jboss.aerogear.simplepush.server.datastore.model.ChannelDTO;
import org.jboss.aerogear.simplepush.server.datastore.model.Server;
import org.jboss.aerogear.simplepush.server.datastore.model.UserAgentDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A DataStore implementation that use Java Persistence API (JPA) to store data for the SimplePush Server.
 */
public final class JpaDataStore implements DataStore {

    private final Logger logger = LoggerFactory.getLogger(JpaDataStore.class);
    private final JpaExecutor jpaExecutor;
    private final static Charset UTF_8 = Charset.forName("UTF-8");

    /**
     * Sole constructor.
     *
     * @param persistenceUnit the name of the persistence unit to be used.
     */
    public JpaDataStore(final String persistenceUnit) {
        jpaExecutor = new JpaExecutor(Persistence.createEntityManagerFactory(persistenceUnit));
    }

    @Override
    public void savePrivateKeySalt(final byte[] salt) {
        final byte[] privateKeySalt = getPrivateKeySalt();
        if (privateKeySalt.length != 0) {
            return;
        }

        final JpaOperation<Void> saveSalt = new JpaOperation<Void>() {
            @Override
            public Void perform(final EntityManager em) {
                em.persist(new Server(new String(salt, UTF_8)));
                return null;
            }
        };
        jpaExecutor.execute(saveSalt);
    }

    @Override
    public byte[] getPrivateKeySalt() {
        final JpaOperation<byte[]> saveChannel = new JpaOperation<byte[]>() {
            @Override
            public byte[] perform(final EntityManager em) {
                final Query query = em.createQuery("SELECT s FROM Server s");
                final Server server = (Server) query.getSingleResult();
                return server.getSalt().getBytes(UTF_8);
            }
        };
        try {
            return jpaExecutor.execute(saveChannel);
        } catch (final Exception e) {
            if (! (e instanceof NoResultException)) {
                logger.debug("Exception while trying to find a the servers salt");
            }
            return new byte[]{};
        }
    }

    @Override
    public boolean saveChannel(final Channel channel) {
        final JpaOperation<Boolean> saveChannel = new JpaOperation<Boolean>() {
            @Override
            public Boolean perform(final EntityManager em) {
                UserAgentDTO userAgent = em.find(UserAgentDTO.class, channel.getUAID());
                if (userAgent == null) {
                    userAgent = new UserAgentDTO(channel.getUAID());
                }
                userAgent.addChannel(channel.getChannelId(), channel.getVersion(), channel.getEndpointToken());
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
        return new DefaultChannel(dto.getUserAgent().getUaid(), dto.getChannelId(), dto.getVersion(), dto.getEndpointToken());
    }

    @Override
    public void removeChannels(final Set<String> channelIds) {
        if (channelIds == null || channelIds.isEmpty()) {
            return;
        }
        final JpaOperation<Integer> removeChannel = new JpaOperation<Integer>() {
            @Override
            public Integer perform(EntityManager em) {
                final Query delete = em.createQuery("DELETE from ChannelDTO c where c.channelId in (:channelIds)");
                delete.setParameter("channelIds", channelIds);
                return delete.executeUpdate();
            }
        };
        jpaExecutor.execute(removeChannel);
    }

    @Override
    public Set<String> getChannelIds(final String uaid) {
        final JpaOperation<Set<String>> getChannelIds = new JpaOperation<Set<String>>() {
            @Override
            public Set<String> perform(final EntityManager em) {
                final Set<String> channels = new HashSet<String>();
                final UserAgentDTO userAgent = em.find(UserAgentDTO.class, uaid);
                if (userAgent != null) {
                    for (ChannelDTO dto : userAgent.getChannels()) {
                        channels.add(dto.getChannelId());
                    }
                }
                return channels;
            }
        };
        return jpaExecutor.execute(getChannelIds);
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
    public String updateVersion(final String endpointToken, final long version) throws VersionException, ChannelNotFoundException {
        final JpaOperation<ChannelDTO> updateVersion = new JpaOperation<ChannelDTO>() {
            @Override
            public ChannelDTO perform(final EntityManager em) {
                final TypedQuery<ChannelDTO> select = em.createQuery("SELECT c FROM ChannelDTO c where c.endpointToken = :endpointToken", ChannelDTO.class);
                select.setParameter("endpointToken", endpointToken);
                final List<ChannelDTO> resultList = select.getResultList();
                if (resultList.isEmpty()) {
                    return null;
                }
                final ChannelDTO channelDTO = resultList.get(0);
                if (channelDTO != null) {
                    if (version > channelDTO.getVersion()) {
                        channelDTO.setVersion(version);
                        em.merge(channelDTO);
                    } else {
                        throw new VersionException("New version [" + version + "] must be greater than current version [" + channelDTO.getVersion() + "]");
                    }
                }
                return channelDTO;
            }
        };
        try {
            final ChannelDTO channelDto = jpaExecutor.execute(updateVersion);
            if (channelDto == null) {
                throw new ChannelNotFoundException("No Channel for endpointToken [" + endpointToken + "] was found", endpointToken);
            }
            return channelDto.getChannelId();
        } catch (final JpaException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof VersionException) {
                throw (VersionException) cause;
            }
            throw e;
        }
    }

    @Override
    public String saveUnacknowledged(final String channelId, final long version) throws ChannelNotFoundException {
        final JpaOperation<String> saveAcks = new JpaOperation<String>() {
            @Override
            public String perform(final EntityManager em) {
                final ChannelDTO channel = em.find(ChannelDTO.class, channelId);
                final UserAgentDTO userAgent = channel.getUserAgent();
                final Set<AckDTO> dtos = new HashSet<AckDTO>();
                dtos.add(new AckDTO(userAgent, channel.getChannelId(), version));
                userAgent.setAcks(dtos);
                em.merge(userAgent);
                return userAgent.getUaid();
            }
        };
        return jpaExecutor.execute(saveAcks);
    }

    @Override
    public Set<Ack> getUnacknowledged(final String uaid) {
        final JpaOperation<Set<Ack>> getUnacks = new JpaOperation<Set<Ack>>() {
            @Override
            public Set<Ack> perform(final EntityManager em) {
                final UserAgentDTO userAgent = em.find(UserAgentDTO.class, uaid);
                if (userAgent == null) {
                    return Collections.emptySet();
                }
                final HashSet<Ack> acks = new HashSet<Ack>();
                for (AckDTO ackDTO : userAgent.getAcks()) {
                    acks.add(new AckImpl(ackDTO.getChannelId(), ackDTO.getVersion()));
                }
                return acks;
            }
        };
        return jpaExecutor.execute(getUnacks);
    }

    @Override
    public Set<Ack> removeAcknowledged(final String uaid, final Set<Ack> acked) {
        final JpaOperation<Set<Ack>> removeAck = new JpaOperation<Set<Ack>>() {
            @Override
            public Set<Ack> perform(final EntityManager em) {
                final List<String> channelIds = new ArrayList<String>(acked.size());
                for (Ack ack : acked) {
                    channelIds.add(ack.getChannelId());
                }
                final Query delete = em.createQuery("DELETE from AckDTO c where c.channelId in (:channelIds)");
                delete.setParameter("channelIds", channelIds);
                delete.executeUpdate();
                final UserAgentDTO userAgent = em.find(UserAgentDTO.class, uaid);
                final Set<AckDTO> acks = userAgent.getAcks();
                final Set<Ack> unacked = new HashSet<Ack>(acks.size());
                for (AckDTO ackDto : acks) {
                    unacked.add(new AckImpl(ackDto.getChannelId(), ackDto.getVersion()));
                }
                return unacked;
            }
        };
        return jpaExecutor.execute(removeAck);
    }

}
