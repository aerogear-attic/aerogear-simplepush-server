/**
 * JBoss, Home of Professional Open Source Copyright Red Hat, Inc., and individual contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package org.jboss.aerogear.simplepush.server.datastore;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
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
import org.jboss.aerogear.simplepush.util.CryptoUtil;
import org.jboss.aerogear.simplepush.util.UUIDUtil;
import org.junit.BeforeClass;
import org.junit.Test;

public class CouchDBDataStoreTest {

    private static CouchDBDataStore datastore;

    @BeforeClass
    public static void createDataStore() {
        datastore = new CouchDBDataStore("http://127.0.0.1:5984", "simplepush-test");
    }

    @Test
    public void saveChannel() {
        final Channel channel = newChannel(UUIDUtil.newUAID(), UUID.randomUUID().toString());
        final boolean saved = datastore.saveChannel(channel);
        assertThat(saved, is(true));
    }

    @Test
    public void saveChannelWithIdContainingUnderscore() {
        final String uaid = UUIDUtil.newUAID();
        final String channelId = UUID.randomUUID().toString();
        final String endpointToken = CryptoUtil.endpointToken(uaid, channelId, CryptoUtil.secretKey("testKey"));
        final Channel channel = new DefaultChannel(uaid, channelId, "_" + endpointToken);
        final boolean saved = datastore.saveChannel(channel);
        assertThat(saved, is(true));
    }

    @Test (expected = ChannelNotFoundException.class)
    public void getChannelNonExisting() throws ChannelNotFoundException {
        datastore.getChannel(UUID.randomUUID().toString());
    }

    @Test (expected = IllegalStateException.class)
    public void getChannelMultipleResults() throws ChannelNotFoundException {
        final String uaid = UUIDUtil.newUAID();
        final String channelId = UUID.randomUUID().toString();
        final Channel channel1 = newChannel(uaid, channelId);
        datastore.saveChannel(channel1);
        final String uaid2 = UUIDUtil.newUAID();
        final Channel channel2 = newChannel(uaid2, channelId);
        datastore.saveChannel(channel2);
        datastore.getChannel(channelId);
    }

    @Test
    public void getChannel() throws ChannelNotFoundException {
        final String uaid = UUIDUtil.newUAID();
        final String channelId = UUID.randomUUID().toString();
        final Channel channel = newChannel(uaid, channelId);
        datastore.saveChannel(channel);
        final Channel retreived = datastore.getChannel(channelId);
        assertThat(retreived.getUAID(), is(equalTo(channel.getUAID())));
        assertThat(retreived.getChannelId(), is(equalTo(channel.getChannelId())));
        assertThat(retreived.getEndpointToken(), is(equalTo(channel.getEndpointToken())));
        assertThat(retreived.getVersion(), is(channel.getVersion()));
    }

    @Test (expected = ChannelNotFoundException.class)
    public void removeChannelAndTryToRetreive() throws ChannelNotFoundException {
        final Channel channel = newChannel(UUIDUtil.newUAID(), UUID.randomUUID().toString());
        datastore.saveChannel(channel);
        datastore.removeChannels(new HashSet<String>(Arrays.asList(channel.getChannelId())));
        datastore.getChannel(channel.getChannelId());
    }

    @Test (expected = ChannelNotFoundException.class)
    public void removeChannels() throws ChannelNotFoundException {
        final String uaid = UUIDUtil.newUAID();
        final String channelId = UUID.randomUUID().toString();
        datastore.saveChannel(newChannel(uaid, channelId));
        datastore.saveChannel(newChannel(uaid, UUID.randomUUID().toString()));
        datastore.removeChannels(uaid);
        datastore.getChannel(channelId);
    }

    @Test
    public void removeChannelsList() throws ChannelNotFoundException {
        final String uaid = UUIDUtil.newUAID();
        final String channelId1 = UUID.randomUUID().toString();
        final String channelId2 = UUID.randomUUID().toString();
        datastore.saveChannel(newChannel(uaid, channelId1));
        datastore.saveChannel(newChannel(uaid, channelId2));
        datastore.removeChannels(new HashSet<String>(Arrays.asList(channelId1, channelId2)));
    }

    @Test
    public void removeChannelIdsEmpty() throws ChannelNotFoundException {
        final Set<String> channelIds = datastore.getChannelIds(UUIDUtil.newUAID());
        assertThat(channelIds.isEmpty(), is(true));
    }

    @Test
    public void removeChannelIds() throws ChannelNotFoundException {
        final String uaid = UUIDUtil.newUAID();
        final String channelId1 = UUID.randomUUID().toString();
        final String channelId2 = UUID.randomUUID().toString();
        datastore.saveChannel(newChannel(uaid, channelId1));
        datastore.saveChannel(newChannel(uaid, channelId2));
        final Set<String> channelIds = datastore.getChannelIds(uaid);
        assertThat(channelIds, hasItems(channelId1, channelId2));
        assertThat(channelIds.size(), is(2));
    }

    @Test (expected = ChannelNotFoundException.class)
    public void updateVersionNonExistingEndpointToken() throws VersionException, ChannelNotFoundException {
        final Channel channel = newChannel(UUIDUtil.newUAID(), UUID.randomUUID().toString());
        datastore.updateVersion(channel.getEndpointToken(), 2);
    }

    @Test
    public void updateVersion() throws VersionException, ChannelNotFoundException {
        final Channel channel = newChannel(UUIDUtil.newUAID(), UUID.randomUUID().toString());
        datastore.saveChannel(channel);
        final String channelId = datastore.updateVersion(channel.getEndpointToken(), 2);
        assertThat(channel.getChannelId(), is(equalTo(channelId)));
    }

    @Test (expected = VersionException.class)
    public void updateVersionEqualToCurrentVersion() throws VersionException, ChannelNotFoundException {
        final Channel channel = newChannel(UUIDUtil.newUAID(), UUID.randomUUID().toString());
        datastore.saveChannel(channel);
        datastore.updateVersion(channel.getEndpointToken(), 0);
    }

    @Test (expected = VersionException.class)
    public void updateVersionLessThanCurrentVersion() throws VersionException, ChannelNotFoundException {
        final Channel channel = newChannel(UUIDUtil.newUAID(), UUID.randomUUID().toString(), 10);
        datastore.saveChannel(channel);
        datastore.updateVersion(channel.getEndpointToken(), 5);
    }

    @Test
    public void saveUnacknowledged() throws ChannelNotFoundException {
        final String uaid = UUIDUtil.newUAID();
        final Channel channel = newChannel(uaid, UUID.randomUUID().toString(), 10);
        datastore.saveChannel(channel);
        final String savedUaid = datastore.saveUnacknowledged(channel.getChannelId(), channel.getVersion());
        assertThat(savedUaid, is(equalTo(uaid)));
    }

    @Test
    public void getUnacknowledged() throws ChannelNotFoundException {
        final String uaid = UUIDUtil.newUAID();
        final Channel channel = newChannel(uaid, UUID.randomUUID().toString(), 10);
        datastore.saveChannel(channel);
        datastore.saveUnacknowledged(channel.getChannelId(), channel.getVersion());
        final Set<Ack> unacks = datastore.getUnacknowledged(uaid);
        assertThat(unacks, hasItems(ack(channel)));
    }

    @Test
    public void removeAcknowledged() throws ChannelNotFoundException {
        final String uaid = UUIDUtil.newUAID();
        final Channel channel1 = newChannel(uaid, UUID.randomUUID().toString(), 10);
        final Channel channel2 = newChannel(uaid, UUID.randomUUID().toString(), 22);
        datastore.saveChannel(channel1);
        datastore.saveChannel(channel2);
        datastore.saveUnacknowledged(channel1.getChannelId(), channel1.getVersion());
        datastore.saveUnacknowledged(channel2.getChannelId(), channel2.getVersion());
        final Set<Ack> unacks = datastore.removeAcknowledged(uaid, acks(ack(channel1)));
        assertThat(unacks, hasItem(ack(channel2)));
    }

    private Set<Ack> acks(final Ack ...acks) {
        return new HashSet<Ack>(Arrays.asList(acks));
    }

    private Ack ack(final Channel channel) {
        return new AckImpl(channel.getChannelId(), channel.getVersion());
    }

    private Channel newChannel(final String uaid, final String channelId) {
        return newChannel(uaid, channelId, 0);
    }

    private Channel newChannel(final String uaid, final String channelId, final long version) {
        final String endpointToken = CryptoUtil.endpointToken(uaid, channelId, CryptoUtil.secretKey("testKey"));
        return new DefaultChannel(uaid, channelId, version, endpointToken);
    }

}
