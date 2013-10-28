/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
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
package org.jboss.aerogear.simplepush.server.datastore;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jboss.aerogear.simplepush.protocol.Ack;
import org.jboss.aerogear.simplepush.protocol.impl.AckImpl;
import org.jboss.aerogear.simplepush.server.Channel;
import org.jboss.aerogear.simplepush.server.DefaultChannel;
import org.jboss.aerogear.simplepush.util.CryptoUtil;
import org.jboss.aerogear.simplepush.util.UUIDUtil;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class InMemoryDataStoreTest {

    @Test
    public void saveChannel() {
        final InMemoryDataStore store = new InMemoryDataStore();
        final Channel channel = mockChannel(UUIDUtil.newUAID(), "channel-1", 1, "endpointToken");
        final boolean saved = store.saveChannel(channel);
        assertThat(saved, is(true));
    }

    @Test
    public void getChannel() throws ChannelNotFoundException {
        final InMemoryDataStore store = new InMemoryDataStore();
        store.saveChannel(mockChannel(UUIDUtil.newUAID(), "channel-1", 1, "endpointToken"));
        final Channel channel = store.getChannel("channel-1");
        assertThat(channel, is(notNullValue()));
        assertThat(channel.getChannelId(), equalTo("channel-1"));
        assertThat(channel.getEndpointToken(), equalTo("endpointToken"));
    }

    @Test
    public void getChannels() throws ChannelNotFoundException {
        final InMemoryDataStore store = new InMemoryDataStore();
        final String uaid = UUIDUtil.newUAID();
        final String channelId1 = UUID.randomUUID().toString();
        final String channelId2 = UUID.randomUUID().toString();
        store.saveChannel(mockChannel(uaid, channelId1, 1, "endpointToken"));
        store.saveChannel(mockChannel(uaid, channelId2, 1, "endpointToken"));
        final Set<String> channels = store.getChannelIds(uaid);
        assertThat(channels.size(), is(2));
        assertThat(channels, hasItems(channelId1, channelId2));
    }

    @Test
    public void removeChannel() {
        final InMemoryDataStore store = new InMemoryDataStore();
        store.saveChannel(mockChannel(UUIDUtil.newUAID(), "channel-1", 1, "endpointToken"));
        store.removeChannels(new HashSet<String>(Arrays.asList("channel-1")));
        assertThat(hasChannel("channel-1", store), is(false));
    }

    @Test
    public void removeChannels() throws ChannelNotFoundException {
        final InMemoryDataStore store = new InMemoryDataStore();
        final String uaid1 = UUIDUtil.newUAID();
        final String uaid2 = UUIDUtil.newUAID();
        store.saveChannel(mockChannel(uaid1, "channel-1", 1, "endpointToken1"));
        store.saveChannel(mockChannel(uaid2, "channel-2", 1, "endpointToken2"));
        store.saveChannel(mockChannel(uaid1, "channel-3", 1, "endpointToken3"));
        store.saveChannel(mockChannel(uaid2, "channel-4", 1, "endpointToken4"));
        store.removeChannels(uaid2);
        assertThat(hasChannel("channel-1", store), is(true));
        assertThat(hasChannel("channel-2", store), is(false));
        assertThat(hasChannel("channel-3", store), is(true));
        assertThat(hasChannel("channel-4", store), is(false));
    }

    @Test
    public void saveUnacknowledged() throws ChannelNotFoundException {
        final InMemoryDataStore store = new InMemoryDataStore();
        final String uaid = UUIDUtil.newUAID();
        final String channelId1 = UUID.randomUUID().toString();
        store.saveChannel(mockChannel(uaid, channelId1, 1, "endpointToken"));
        store.saveUnacknowledged(channelId1, 10L);
        final Set<Ack> acks = store.getUnacknowledged(uaid);
        assertThat(acks, hasItem(ack(channelId1, 10L)));
    }

    @Test
    public void saveUnacknowledgedWithGreatVersion() throws ChannelNotFoundException {
        final InMemoryDataStore store = new InMemoryDataStore();
        final String uaid = UUIDUtil.newUAID();
        final String channelId1 = UUID.randomUUID().toString();
        store.saveChannel(mockChannel(uaid, channelId1, 0, "endpointToken"));
        store.saveUnacknowledged(channelId1, 10L);
        store.saveUnacknowledged(channelId1, 11L);
        final Set<Ack> acks = store.getUnacknowledged(uaid);
        assertThat(acks, hasItem(ack(channelId1, 11L)));
        assertThat(acks.size(), is(1));
    }

    @Test
    public void removeUpdate() throws ChannelNotFoundException {
        final InMemoryDataStore store = new InMemoryDataStore();
        final String uaid = UUIDUtil.newUAID();
        final String channelId = UUID.randomUUID().toString();
        store.saveChannel(mockChannel(uaid, channelId, 10, "endpointToken"));
        store.saveUnacknowledged(channelId, 10L);
        assertThat(store.removeAcknowledged(uaid, acks(ack(channelId, 10L))).isEmpty(), is(true));
        assertThat(store.removeAcknowledged(uaid, acks(ack(channelId, 10L))).isEmpty(), is(true));
        assertThat(store.removeAcknowledged(uaid, acks(ack(channelId, 11L))).isEmpty(), is(true));
    }

    @Test @Ignore("Intended to be run manually")
    public void updatesThreadSafety() throws InterruptedException {
        final InMemoryDataStore store = new InMemoryDataStore();
        final String uaid = UUIDUtil.newUAID();
        final AtomicBoolean outcome = new AtomicBoolean(true);
        final int threads = 1000;
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch endLatch = new CountDownLatch(threads);
        for (int i = 0; i < threads; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        startLatch.await();
                        try {
                            final Channel channel = newChannel(uaid, UUID.randomUUID().toString(), 10);
                            store.saveChannel(channel);
                            store.saveUnacknowledged(channel.getChannelId(), 11);
                            store.saveUnacknowledged(channel.getChannelId(), 12);
                            store.saveUnacknowledged(channel.getChannelId(), 13);
                            final Set<Ack> acks = store.getUnacknowledged(uaid);
                            assertThat(acks, hasItems(ack(channel.getChannelId(), 13)));
                            assertThat(store.removeAcknowledged(uaid, acks(ack(channel.getChannelId(), 13))), not(hasItem(ack(channel.getChannelId(), 13))));
                        } catch (final Exception e) {
                            e.printStackTrace();
                            outcome.compareAndSet(true, false);
                        } finally {
                            endLatch.countDown();
                        }
                    } catch (InterruptedException ignored) {
                    }
                }
            }).start();
        }
        startLatch.countDown();
        endLatch.await();
        if (!outcome.get()) {
            Assert.fail("updateThreadSafety test failed. Please check stacktrace(s)");
        }
    }

    private boolean hasChannel(final String channelId, final DataStore store) {
        try {
            store.getChannel(channelId);
            return true;
        } catch (final ChannelNotFoundException e) {
            return false;
        }

    }

    private Channel mockChannel(final String uaid, final String channelId, final long version, final String endpointToken) {
        final Channel channel = mock(Channel.class);
        when(channel.getUAID()).thenReturn(uaid);
        when(channel.getChannelId()).thenReturn(channelId);
        when(channel.getVersion()).thenReturn(version);
        when(channel.getEndpointToken()).thenReturn(endpointToken);
        return channel;
    }

    private Channel newChannel(final String uaid, final String channelId, final long version) {
        final String endpointToken = CryptoUtil.endpointToken(uaid, channelId, CryptoUtil.secretKey("testKey"));
        return new DefaultChannel(uaid, channelId, version, endpointToken);
    }

    private Ack ack(final String channelId, final long version) {
        return new AckImpl(channelId, version);
    }

    private Set<Ack> acks(final Ack... acks) {
        return new HashSet<Ack>(Arrays.asList(acks));
    }

}
