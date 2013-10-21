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


import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.jboss.aerogear.simplepush.server.datastore.model.ChannelDTO;
import org.jboss.aerogear.simplepush.server.datastore.model.AckDTO;
import org.jboss.aerogear.simplepush.server.datastore.model.UserAgentDTO;
import org.jboss.aerogear.simplepush.util.UUIDUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class JpaEntitiesTest {

    private Connection connection;
    private EntityManagerFactory entityManagerFactory;
    private EntityManager entityManager;

    @Before
    public void setupDatabase() throws Exception{
        Class.forName("org.h2.Driver");
        connection = DriverManager.getConnection("jdbc:h2:mem:simplepush", "sa", "");
        entityManagerFactory = Persistence.createEntityManagerFactory("SimplePush");
        entityManager = entityManagerFactory.createEntityManager();
    }

    @After
    public void teardownDatabase() throws Exception {
        if (entityManager != null) {
            entityManager.close();
            entityManagerFactory.close();
            connection.createStatement().execute("SHUTDOWN");
        }
    }

    @Test
    public void persistUserAgent() {
        final String channelId = UUID.randomUUID().toString();
        final String uaid = UUIDUtil.newUAID();
        final UserAgentDTO userAgent = persist(uaid, channelId, 1, "/endpoint/" + channelId);
        assertThat(entityManager.contains(userAgent), is(true));

        entityManager.getTransaction().begin();
        final UserAgentDTO ua = entityManager.find(UserAgentDTO.class, userAgent.getUaid());
        assertThat(ua.getUaid(), equalTo(uaid.toString()));
        assertThat(ua.getChannels().size(), is(1));
        assertThat(ua.getChannels().iterator().next().getChannelId(), equalTo(channelId));
        assertThat(ua.getChannels().iterator().next().getVersion(), equalTo(1L));
        entityManager.getTransaction().commit();
    }

    @Test
    public void getChannel() {
        final String channelId = UUID.randomUUID().toString();
        final String uaid = UUIDUtil.newUAID();
        persist(uaid, channelId, 10, "/endpoint/" + channelId);

        entityManager.getTransaction().begin();
        final ChannelDTO channel = entityManager.find(ChannelDTO.class, channelId);
        assertThat(channel.getChannelId(), equalTo(channelId));
        assertThat(channel.getVersion(), equalTo(10L));
        entityManager.getTransaction().commit();
    }

    @Test
    public void removeUserAgent() {
        final String channelId = UUID.randomUUID().toString();
        final String uaid = UUIDUtil.newUAID();
        final UserAgentDTO userAgent = persist(uaid, channelId, 1, "/endpoint/" + channelId);
        assertThat(entityManager.contains(userAgent), is(true));
        entityManager.getTransaction().begin();
        entityManager.remove(userAgent);
        entityManager.getTransaction().commit();
        final UserAgentDTO ua = entityManager.find(UserAgentDTO.class, userAgent.getUaid());
        assertThat(ua, is(nullValue()));
        final ChannelDTO channel = entityManager.find(ChannelDTO.class, channelId);
        assertThat(channel, is(nullValue()));
    }

    @Test
    public void persistUpdate() {
        final String channelId = UUID.randomUUID().toString();
        final String uaid = UUIDUtil.newUAID();
        final UserAgentDTO userAgent = persist(uaid, channelId, 10, "/endpoint/" + channelId);

        entityManager.getTransaction().begin();
        final AckDTO update = new AckDTO(userAgent, channelId, 10);
        entityManager.persist(update);
        entityManager.getTransaction().commit();

        entityManager.getTransaction().begin();
        final AckDTO up = entityManager.find(AckDTO.class, channelId);
        assertThat(up.getUserAgent(), equalTo(userAgent));
        assertThat(up.getChannelId(), equalTo(channelId));
        assertThat(up.getVersion(), equalTo(10L));
        entityManager.getTransaction().commit();
    }

    @Test
    public void persistUpdateToSameChannel() {
        final String channelId = UUID.randomUUID().toString();
        final String uaid = UUIDUtil.newUAID();
        final UserAgentDTO userAgent = persist(uaid, channelId, 10, "/endpoint/" + channelId);

        entityManager.getTransaction().begin();
        final AckDTO update = new AckDTO(userAgent, channelId, 10);
        entityManager.persist(update);
        entityManager.getTransaction().commit();

        entityManager.getTransaction().begin();
        final UserAgentDTO userAgentToUpdate = entityManager.find(UserAgentDTO.class, uaid);
        final AckDTO newUpdate = new AckDTO(userAgent, channelId, 11);
        userAgentToUpdate.setAcks(new HashSet<AckDTO>(Arrays.asList(newUpdate)));
        entityManager.merge(userAgentToUpdate);
        entityManager.getTransaction().commit();

        entityManager.getTransaction().begin();
        final AckDTO updated = entityManager.find(AckDTO.class, channelId);
        assertThat(updated.getVersion(), is(11L));
    }

    private UserAgentDTO persist(final String uaid, final String channelId, final long version, final String endpointUrl) {
        entityManager.getTransaction().begin();
        final UserAgentDTO userAgent = new UserAgentDTO(uaid.toString());
        userAgent.addChannel(channelId, version, endpointUrl);

        entityManager.persist(userAgent);
        assertThat(entityManager.contains(userAgent), is(true));
        entityManager.getTransaction().commit();
        return userAgent;
    }

}
