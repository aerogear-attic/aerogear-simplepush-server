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
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.jboss.aerogear.simplepush.protocol.Ack;
import org.jboss.aerogear.simplepush.protocol.impl.AckImpl;
import org.jboss.aerogear.simplepush.server.Channel;
import org.jboss.aerogear.simplepush.server.DefaultChannel;
import org.jboss.aerogear.simplepush.util.UUIDUtil;
import org.junit.Before;
import org.junit.Test;

public class JpaDataStoreTest {

    private JpaDataStore jpaDataStore;

    @Before
    public void createJpaStore() {
        jpaDataStore = new JpaDataStore("SimplePush");
    }

    @Test
    public void saveChannel() {
        final boolean saved = jpaDataStore.saveChannel(newChannel(UUIDUtil.newUAID(), UUID.randomUUID().toString(), 10L));
        assertThat(saved, is(true));
    }

    @Test
    public void saveChannels() {
        final String uaid = UUIDUtil.newUAID();
        final String channelId = UUID.randomUUID().toString();
        assertThat(jpaDataStore.saveChannel(newChannel(uaid, channelId, 1L)), is(true));
        assertThat(jpaDataStore.saveChannel(newChannel(uaid, channelId, 10L)), is(true));
    }

    @Test
    public void getChannel() throws ChannelNotFoundException {
        final String uaid = UUIDUtil.newUAID();
        final String channelId = UUID.randomUUID().toString();
        jpaDataStore.saveChannel(newChannel(uaid, channelId, 10L));
        final Channel channel = jpaDataStore.getChannel(channelId);
        assertThat(channel.getChannelId(), equalTo(channelId));
        assertThat(channel.getUAID(), equalTo(uaid));
        assertThat(channel.getVersion(), equalTo(10L));
        assertThat(channel.getPushEndpoint(), equalTo("/endpoint/" + channelId));
    }

    @Test
    public void getChannels() throws ChannelNotFoundException {
        final String uaid = UUIDUtil.newUAID();
        final String channelId1 = UUID.randomUUID().toString();
        final String channelId2 = UUID.randomUUID().toString();
        jpaDataStore.saveChannel(newChannel(uaid, channelId1, 10L));
        jpaDataStore.saveChannel(newChannel(uaid, channelId2, 10L));
        final Set<String> channels = jpaDataStore.getChannelIds(uaid);
        assertThat(channels.size(), is(2));
        assertThat(channels, hasItems(channelId1, channelId2));
    }

    @Test
    public void getChannelsForNonExistingUserAgent() throws ChannelNotFoundException {
        final Set<String> channels = jpaDataStore.getChannelIds(UUIDUtil.newUAID());
        assertThat(channels.isEmpty(), is(true));
    }

    @Test (expected = ChannelNotFoundException.class)
    public void shouldThrowIfChannelIdNotFound() throws ChannelNotFoundException {
        jpaDataStore.getChannel("doesNotExistId");
    }

    @Test
    public void removeChannel() {
        final String channelId = UUID.randomUUID().toString();
        jpaDataStore.saveChannel(newChannel(UUIDUtil.newUAID(), channelId, 10L));
        assertThat(jpaDataStore.removeChannel(channelId), is(true));
    }

    @Test
    public void removeNotExistingChannel() {
        final boolean removed = jpaDataStore.removeChannel("DoesNotExistChannelId");
        assertThat(removed, is(false));
    }

    @Test
    public void removeChannels() {
        final String uaid = UUIDUtil.newUAID();
        final String channelId1 = UUID.randomUUID().toString();
        final String channelId2 = UUID.randomUUID().toString();
        jpaDataStore.saveChannel(newChannel(uaid, channelId1, 10L));
        jpaDataStore.saveChannel(newChannel(uaid, channelId2, 10L));
        jpaDataStore.removeChannels(uaid);
        assertThat(channelExists(channelId1, jpaDataStore), is(false));
    }

    @Test
    public void removeChannelsNoChannelsRegistered() {
        final Integer removed = jpaDataStore.removeChannels(Collections.<String>emptySet());
        assertThat(removed, is(0));
    }

    @Test
    public void removeChannelsNullChannels() {
        final Integer removed = jpaDataStore.removeChannels((Set<String>)null);
        assertThat(removed, is(0));
    }

    @Test
    public void removeChannelsNoUserAgentStored() {
        final String channelId1 = UUID.randomUUID().toString();
        jpaDataStore.removeChannels(UUIDUtil.newUAID());
        assertThat(channelExists(channelId1, jpaDataStore), is(false));
    }

    @Test
    public void saveEmptyUnacknowledged() {
        final String uaid = UUIDUtil.newUAID();
        jpaDataStore.saveChannel(newChannel(uaid, UUID.randomUUID().toString(), 10L));
        jpaDataStore.saveUnacknowledged(Collections.<Ack>emptySet(), uaid);
        assertThat(jpaDataStore.getUnacknowledged(uaid).size(), is(0));
    }

    @Test
    public void saveAndGetUnacknowledged() {
        final String uaid = UUIDUtil.newUAID();
        jpaDataStore.saveChannel(newChannel(uaid, UUID.randomUUID().toString(), 10L));
        jpaDataStore.saveUnacknowledged(acks(newAck(1L), newAck(2L)), uaid);
        assertThat(jpaDataStore.getUnacknowledged(uaid).size(), is(2));
    }

    @Test
    public void removeAcknowledged() {
        final String uaid = UUIDUtil.newUAID();
        jpaDataStore.saveChannel(newChannel(uaid, UUID.randomUUID().toString(), 10L));
        jpaDataStore.saveUnacknowledged(acks(newAck(1L), newAck(2L)), uaid);
        final Set<Ack> storedUpdates = jpaDataStore.getUnacknowledged(uaid);
        assertThat(storedUpdates.size(), is(2));

        jpaDataStore.removeAcknowledged(storedUpdates.iterator().next(), uaid);
        assertThat(jpaDataStore.getUnacknowledged(uaid).size(), is(1));
    }

    @Test
    public void getUnacknowledgedNoChannelsSavedYet() {
        final String uaid = UUIDUtil.newUAID();
        final Set<Ack> storedUpdates = jpaDataStore.getUnacknowledged(uaid);
        assertThat(storedUpdates.size(), is(0));
    }

    private Ack newAck(final long version) {
        return new AckImpl(UUID.randomUUID().toString(), version);
    }

    private Set<Ack> acks(final Ack... updates) {
        final Set<Ack> ups = new HashSet<Ack>();
        ups.addAll(Arrays.asList(updates));
        return ups;
    }

    private Channel newChannel(final String uaid, final String channelId, final long version) {
        return new DefaultChannel(uaid, channelId, version, "/endpoint/" + channelId);
    }

    private boolean channelExists(final String channelId, final JpaDataStore store) {
        try {
            store.getChannel(channelId);
            return true;
        } catch (final ChannelNotFoundException e) {
            return false;
        }
    }

}
