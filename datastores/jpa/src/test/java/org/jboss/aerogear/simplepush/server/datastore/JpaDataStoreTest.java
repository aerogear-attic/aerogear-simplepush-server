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
        assertThat(channel.getEndpointToken(), equalTo("endpointToken"));
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
    public void removeChannelSingleChannel() {
        final String channelId = UUID.randomUUID().toString();
        jpaDataStore.saveChannel(newChannel(UUIDUtil.newUAID(), channelId, 10L));
        assertThat(jpaDataStore.removeChannel(channelId), is(true));
    }

    @Test (expected = ChannelNotFoundException.class)
    public void removeNotExistingChannel() throws ChannelNotFoundException {
        final String channelId = "DoesNotExistChannelId";
        jpaDataStore.removeChannels(new HashSet<String>(Arrays.asList(channelId)));
        jpaDataStore.getChannel(channelId);
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
    public void removeChannelsNoUserAgentStored() {
        final String channelId1 = UUID.randomUUID().toString();
        jpaDataStore.removeChannels(UUIDUtil.newUAID());
        assertThat(channelExists(channelId1, jpaDataStore), is(false));
    }

    @Test
    public void updateVersion() throws VersionException, ChannelNotFoundException {
        final Channel channel = newChannel(UUIDUtil.newUAID(), UUID.randomUUID().toString(), 0);
        jpaDataStore.saveChannel(channel);
        final String channelId = jpaDataStore.updateVersion(channel.getEndpointToken(), 1);
        assertThat(channelId, is(equalTo(channel.getChannelId())));
        final Channel updated = jpaDataStore.getChannel(channelId);
        assertThat(updated.getVersion(), is(1L));
    }

    @Test
    public void updateVersionLarger() throws VersionException, ChannelNotFoundException {
        final Channel channel = newChannel(UUIDUtil.newUAID(), UUID.randomUUID().toString(), 10);
        jpaDataStore.saveChannel(channel);
        final String channelId = jpaDataStore.updateVersion(channel.getEndpointToken(), 121);
        assertThat(channelId, is(equalTo(channel.getChannelId())));
        final Channel updated = jpaDataStore.getChannel(channelId);
        assertThat(updated.getVersion(), is(121L));
    }


    @Test (expected = VersionException.class)
    public void updateVersionSameVersion() throws VersionException, ChannelNotFoundException {
        final Channel channel = newChannel(UUIDUtil.newUAID(), UUID.randomUUID().toString(), 1);
        jpaDataStore.saveChannel(channel);
        final String channelId = jpaDataStore.updateVersion(channel.getEndpointToken(), 1);
        assertThat(channelId, is(equalTo(channel.getChannelId())));
        final Channel updated = jpaDataStore.getChannel(channelId);
        assertThat(updated.getVersion(), is(1L));
    }

    @Test (expected = VersionException.class)
    public void updateVersionLessThanCurrent() throws VersionException, ChannelNotFoundException {
        final Channel channel = newChannel(UUIDUtil.newUAID(), UUID.randomUUID().toString(), 10);
        jpaDataStore.saveChannel(channel);
        jpaDataStore.getChannel(jpaDataStore.updateVersion(channel.getEndpointToken(), 9));
    }

    @Test (expected = VersionException.class)
    public void updateVersionLessNegativeValue() throws VersionException, ChannelNotFoundException {
        final Channel channel = newChannel(UUIDUtil.newUAID(), UUID.randomUUID().toString(), 10);
        jpaDataStore.saveChannel(channel);
        final String channelId = jpaDataStore.updateVersion(channel.getEndpointToken(), -9);
        assertThat(channelId, is(equalTo(channel.getChannelId())));
        final Channel updated = jpaDataStore.getChannel(channelId);
        assertThat(updated.getVersion(), is(10L));
    }

    @Test
    public void saveUnacknowledgement() throws ChannelNotFoundException {
        final String uaid = UUIDUtil.newUAID();
        final Channel channel = newChannel(uaid, UUID.randomUUID().toString(), 10L);
        jpaDataStore .saveChannel(channel);
        jpaDataStore.saveUnacknowledged(channel.getChannelId(), 10);
        assertThat(jpaDataStore.getUnacknowledged(uaid).size(), is(1));
    }

    @Test
    public void getUnacknowledged() throws ChannelNotFoundException {
        final String uaid = UUIDUtil.newUAID();
        final Channel channel1 = newChannel(uaid, UUID.randomUUID().toString(), 1);
        final Channel channel2 = newChannel(uaid, UUID.randomUUID().toString(), 2);
        jpaDataStore.saveChannel(channel1);
        jpaDataStore.saveChannel(channel2);
        jpaDataStore.saveUnacknowledged(channel1.getChannelId(), 10);
        jpaDataStore.saveUnacknowledged(channel2.getChannelId(), 10);
        assertThat(jpaDataStore.getUnacknowledged(uaid).size(), is(2));
    }

    @Test
    public void removeAcknowledged() throws ChannelNotFoundException {
        final String uaid = UUIDUtil.newUAID();
        final Channel channel1 = newChannel(uaid, UUID.randomUUID().toString(), 10);
        final Channel channel2 = newChannel(uaid, UUID.randomUUID().toString(), 2);
        jpaDataStore.saveChannel(channel1);
        jpaDataStore.saveChannel(channel2);
        jpaDataStore.saveUnacknowledged(channel1.getChannelId(), 11);
        jpaDataStore.saveUnacknowledged(channel2.getChannelId(), 3);
        final Set<Ack> storedUpdates = jpaDataStore.getUnacknowledged(uaid);
        assertThat(storedUpdates.size(), is(2));

        jpaDataStore.removeAcknowledged(uaid, acks(new AckImpl(channel1.getChannelId(), 11)));
        assertThat(jpaDataStore.getUnacknowledged(uaid).size(), is(1));
    }

    @Test
    public void getUnacknowledgedNoChannelsSavedYet() {
        final String uaid = UUIDUtil.newUAID();
        final Set<Ack> storedUpdates = jpaDataStore.getUnacknowledged(uaid);
        assertThat(storedUpdates.size(), is(0));
    }

    private Set<Ack> acks(final Ack... updates) {
        final Set<Ack> ups = new HashSet<Ack>();
        ups.addAll(Arrays.asList(updates));
        return ups;
    }

    private Channel newChannel(final String uaid, final String channelId, final long version) {
        return new DefaultChannel(uaid, channelId, version, "endpointToken");
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
