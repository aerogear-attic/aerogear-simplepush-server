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
import org.junit.Test;

/**
 * This test requires a local Redis installation running on localhost:6379
 *
 */
public class RedisDataStoreTest {
    private static final String HOST = "localhost";
    private static final int PORT = 6379;

    @Test
    public void saveChannel() {
        final Channel channel = newChannel2();
        assertThat(newRedisDataStore().saveChannel(channel), is(true));
    }

    @Test
    public void saveChannelDuplicate() {
        final Channel channel = newChannel2();
        assertThat(newRedisDataStore().saveChannel(channel), is(true));
        assertThat(newRedisDataStore().saveChannel(channel), is(false));
    }

    @Test
    public void getChannel() throws ChannelNotFoundException {
        final RedisDataStore store = newRedisDataStore();
        final Channel channel = newChannel2();
        store.saveChannel(channel);
        final Channel retreived = store.getChannel(channel.getChannelId());
        assertThat(retreived.getChannelId(), equalTo(channel.getChannelId()));
        assertThat(retreived.getEndpointToken(), equalTo(channel.getEndpointToken()));
    }

    @Test (expected = ChannelNotFoundException.class)
    public void getChannelNonExisting() throws ChannelNotFoundException {
        newRedisDataStore().getChannel(UUID.randomUUID().toString());
    }

    @Test
    public void getChannelIds() throws ChannelNotFoundException {
        final RedisDataStore store = newRedisDataStore();
        final String uaid = UUIDUtil.newUAID();
        store.saveChannel(newChannel2(uaid));
        store.saveChannel(newChannel2(uaid));
        final Set<String> channelIds = store.getChannelIds(uaid);
        assertThat(channelIds.size(), is(2));
    }

    @Test
    public void removeChannelIds() throws ChannelNotFoundException {
        final RedisDataStore store = newRedisDataStore();
        final String uaid = UUIDUtil.newUAID();
        store.saveChannel(newChannel2(uaid));
        store.saveChannel(newChannel2(uaid));
        store.removeChannels(uaid);
        assertThat(store.getChannelIds(uaid).size(), is(0));
    }

    @Test
    public void removeChannels() throws ChannelNotFoundException {
        final RedisDataStore store = newRedisDataStore();
        final String uaid = UUIDUtil.newUAID();
        final Channel ch1 = newChannel2(uaid);
        final Channel ch2 = newChannel2(uaid);
        store.saveChannel(ch1);
        store.saveChannel(ch2);
        store.removeChannels(new HashSet<String>(Arrays.asList(ch1.getChannelId(), ch2.getChannelId())));
        assertThat(store.getChannelIds(uaid).size(), is(0));
    }

    @Test
    public void updateVersion() throws VersionException, ChannelNotFoundException {
        final RedisDataStore store = newRedisDataStore();
        final Channel channel = newChannel2();
        store.saveChannel(channel);
        final String channelId = store.updateVersion(channel.getEndpointToken(), 2L);
        assertThat(channelId, equalTo(channel.getChannelId()));
    }

    @Test (expected = VersionException.class)
    public void updateVersionEqualToCurrentVersion() throws VersionException, ChannelNotFoundException {
        final RedisDataStore store = newRedisDataStore();
        final Channel channel = newChannel2();
        store.saveChannel(channel);
        store.updateVersion(channel.getEndpointToken(), 2L);
        store.updateVersion(channel.getEndpointToken(), 2L);
    }

    @Test (expected = VersionException.class)
    public void updateVersionLessThanCurrentVersion() throws VersionException, ChannelNotFoundException {
        final RedisDataStore store = newRedisDataStore();
        final Channel channel = newChannel2();
        store.saveChannel(channel);
        store.updateVersion(channel.getEndpointToken(), 2L);
        store.updateVersion(channel.getEndpointToken(), 1L);
    }

    @Test
    public void saveUnacknowledged() {
        final RedisDataStore store = newRedisDataStore();
        final Channel channel = newChannel2();
        store.saveChannel(channel);
        store.saveUnacknowledged(channel.getChannelId(), channel.getVersion());
        final Set<Ack> unacknowledged = store.getUnacknowledged(channel.getUAID());
        assertThat(unacknowledged.size(), is(1));
    }

    @Test
    public void removeAcknowledged() {
        final RedisDataStore store = newRedisDataStore();
        final String uaid = UUIDUtil.newUAID();
        final Channel channel1 = newChannel2(uaid);
        final Channel channel2 = newChannel2(uaid);
        store.saveChannel(channel1);
        store.saveChannel(channel2);

        store.saveUnacknowledged(channel1.getChannelId(), channel1.getVersion());
        store.saveUnacknowledged(channel2.getChannelId(), channel2.getVersion());

        final Set<Ack> acks = asSet(new AckImpl(channel1.getChannelId(), channel1.getVersion()));
        final Set<Ack> unacknowledged = store.removeAcknowledged(channel1.getUAID(), acks);
        assertThat(unacknowledged.size(), is(1));
        assertThat(unacknowledged, hasItem(new AckImpl(channel2.getChannelId(), channel2.getVersion())));
    }

    private Set<Ack> asSet(final Ack... ack) {
        return new HashSet<Ack>(Arrays.asList(ack));
    }

    private RedisDataStore newRedisDataStore() {
        return new RedisDataStore(HOST, PORT);
    }

    private Channel newChannel2() {
        return newChannel2(UUIDUtil.newUAID());
    }

    private Channel newChannel2(final String uaid) {
        final String channelId = UUID.randomUUID().toString();
        final String endpointToken = CryptoUtil.endpointToken(uaid, channelId, CryptoUtil.secretKey("testKey"));
        return new DefaultChannel(uaid, channelId, endpointToken);
    }

}