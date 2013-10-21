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
import org.jboss.aerogear.simplepush.server.Channel;
import org.jboss.aerogear.simplepush.util.UUIDUtil;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class InMemoryDataStoreTest {

    @Test
    public void saveChannel() {
        final InMemoryDataStore store = new InMemoryDataStore();
        final Channel channel = mockChannel(UUIDUtil.newUAID(), "channel-1", "endpoint/1");
        final boolean saved = store.saveChannel(channel);
        assertThat(saved, is(true));
    }

    @Test
    public void getChannel() throws ChannelNotFoundException {
        final InMemoryDataStore store = new InMemoryDataStore();
        store.saveChannel(mockChannel(UUIDUtil.newUAID(), "channel-1", "endpoint/1"));
        assertThat(store.getChannel("channel-1"), is(notNullValue()));
        assertThat(store.getChannel("channel-1").getChannelId(), equalTo("channel-1"));
        assertThat(store.getChannel("channel-1").getPushEndpoint(), equalTo("endpoint/1"));
    }

    @Test
    public void getChannels() throws ChannelNotFoundException {
        final InMemoryDataStore store = new InMemoryDataStore();
        final String uaid = UUIDUtil.newUAID();
        final String channelId1 = UUID.randomUUID().toString();
        final String channelId2 = UUID.randomUUID().toString();
        store.saveChannel(mockChannel(uaid, channelId1, "endpoint/" + channelId1));
        store.saveChannel(mockChannel(uaid, channelId2, "endpoint/" + channelId2));
        final Set<String> channels = store.getChannelIds(uaid);
        assertThat(channels.size(), is(2));
        assertThat(channels, hasItems(channelId1, channelId2));
    }

    @Test
    public void removeChannel() {
        final InMemoryDataStore store = new InMemoryDataStore();
        store.saveChannel(mockChannel(UUIDUtil.newUAID(), "channel-1", "endpoint/1"));
        assertThat(store.removeChannel("channel-1"), is(true));
        assertThat(store.removeChannel("channel-1"), is(false));
    }

    @Test
    public void removeChannels() throws ChannelNotFoundException {
        final InMemoryDataStore store = new InMemoryDataStore();
        final String uaid1 = UUIDUtil.newUAID();
        final String uaid2 = UUIDUtil.newUAID();
        store.saveChannel(mockChannel(uaid1, "channel-1", "endpoint/1"));
        store.saveChannel(mockChannel(uaid2, "channel-2", "endpoint/2"));
        store.saveChannel(mockChannel(uaid1, "channel-3", "endpoint/3"));
        store.saveChannel(mockChannel(uaid2, "channel-4", "endpoint/4"));
        store.removeChannels(uaid2);
        assertThat(hasChannel("channel-1", store), is(true));
        assertThat(hasChannel("channel-2", store), is(false));
        assertThat(hasChannel("channel-3", store), is(true));
        assertThat(hasChannel("channel-4", store), is(false));
    }

    @Test
    public void storeUpdates() {
        final InMemoryDataStore store = new InMemoryDataStore();
        final String uaid = UUIDUtil.newUAID();
        final String channelId1 = UUID.randomUUID().toString();
        store.saveUnacknowledged(acks(ack(channelId1, 10L)), uaid);
        final Set<Ack> updates = store.getUnacknowledged(uaid);
        assertThat(updates, hasItem(ack(channelId1, 10L)));
    }

    @Test
    public void storeUpdateWithGreatVersion() {
        final InMemoryDataStore store = new InMemoryDataStore();
        final String uaid = UUIDUtil.newUAID();
        final String channelId1 = UUID.randomUUID().toString();
        store.saveUnacknowledged(acks(ack(channelId1, 10L)), uaid);
        store.saveUnacknowledged(acks(ack(channelId1, 11L)), uaid);
        final Set<Ack> updates = store.getUnacknowledged(uaid);
        assertThat(updates, hasItem(ack(channelId1, 11L)));
        assertThat(updates.size(), is(1));
    }

    @Test
    public void removeUpdate() {
        final InMemoryDataStore store = new InMemoryDataStore();
        final String uaid = UUIDUtil.newUAID();
        final String channelId1 = UUID.randomUUID().toString();
        store.saveUnacknowledged(acks(ack(channelId1, 10L)), uaid);
        assertThat(store.removeAcknowledged(ack(channelId1, 10L), uaid), is(true));
        assertThat(store.removeAcknowledged(ack(channelId1, 10L), uaid), is(false));
        assertThat(store.removeAcknowledged(ack(channelId1, 11L), uaid), is(false));
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
                            final String channelId = UUID.randomUUID().toString();
                            store.saveUnacknowledged(acks(ack(channelId, 10L)), uaid);
                            store.saveUnacknowledged(acks(ack(channelId, 11L)), uaid);
                            store.saveUnacknowledged(acks(ack(channelId, 12L)), uaid);
                            store.saveUnacknowledged(acks(ack(channelId, 13L)), uaid);
                            final Set<Ack> updates = store.getUnacknowledged(uaid);
                            assertThat(updates, hasItems(ack(channelId, 13L)));
                            assertThat(store.removeAcknowledged(ack(channelId, 13L), uaid), is(true));
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

    private Channel mockChannel(final String uaid, final String channelId, final String pushEndpoint) {
        final Channel channel = mock(Channel.class);
        when(channel.getUAID()).thenReturn(uaid);
        when(channel.getChannelId()).thenReturn(channelId);
        when(channel.getPushEndpoint()).thenReturn(pushEndpoint);
        return channel;
    }

    private Ack ack(final String channelId, final Long version) {
        return new AckImpl(channelId, version);
    }

    private Set<Ack> acks(final Ack... acks) {
        return new HashSet<Ack>(Arrays.asList(acks));
    }

    private class AckImpl implements Ack {
        private final String channelId;
        private final Long version;

        public AckImpl(final String channelId, final Long version) {
            this.channelId = channelId;
            this.version = version;
        }

        @Override
        public String getChannelId() {
            return channelId;
        }

        @Override
        public Long getVersion() {
            return version;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((channelId == null) ? 0 : channelId.hashCode());
            return result;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof Ack)) {
                return false;
            }
            final AckImpl other = (AckImpl) obj;
            if (channelId == null) {
                if (other.channelId != null) {
                    return false;
                }
            } else if (!channelId.equals(other.channelId)) {
                return false;
            }
            return true;
        }

    }

}
