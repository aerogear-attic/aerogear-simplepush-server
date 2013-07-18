package org.jboss.aerogear.simplepush.server.datastore;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jboss.aerogear.simplepush.protocol.Update;
import org.jboss.aerogear.simplepush.protocol.impl.UpdateImpl;
import org.jboss.aerogear.simplepush.server.DefaultChannel;
import org.jboss.aerogear.simplepush.util.UUIDUtil;
import org.junit.Assert;
import org.junit.Test;

public class InMemoryDataStoreTest {

    @Test
    public void saveChannel() {
        final InMemoryDataStore store = new InMemoryDataStore();
        final boolean saved = store.saveChannel(new DefaultChannel(UUIDUtil.newUAID(), "channel-1", "endpoint/1"));
        assertThat(saved, is(true));
    }

    @Test
    public void getChannel() {
        final InMemoryDataStore store = new InMemoryDataStore();
        store.saveChannel(new DefaultChannel(UUIDUtil.newUAID(), "channel-1", "endpoint/1"));
        assertThat(store.getChannel("channel-1"), is(notNullValue()));
        assertThat(store.getChannel("channel-1").getChannelId(), equalTo("channel-1"));
        assertThat(store.getChannel("channel-1").getPushEndpoint(), equalTo("endpoint/1"));
    }

    @Test
    public void removeChannel() {
        final InMemoryDataStore store = new InMemoryDataStore();
        store.saveChannel(new DefaultChannel(UUIDUtil.newUAID(), "channel-1", "endpoint/1"));
        assertThat(store.removeChannel("channel-1"), is(true));
        assertThat(store.removeChannel("channel-1"), is(false));
    }

    @Test
    public void removeChannels() {
        final InMemoryDataStore store = new InMemoryDataStore();
        final UUID uaid1 = UUIDUtil.newUAID();
        final UUID uaid2 = UUIDUtil.newUAID();
        store.saveChannel(new DefaultChannel(uaid1, "channel-1", "endpoint/1"));
        store.saveChannel(new DefaultChannel(uaid2, "channel-2", "endpoint/2"));
        store.saveChannel(new DefaultChannel(uaid1, "channel-3", "endpoint/3"));
        store.saveChannel(new DefaultChannel(uaid2, "channel-4", "endpoint/4"));
        store.removeChannels(uaid2);
        assertThat(store.getChannel("channel-1"), is(notNullValue()));
        assertThat(store.getChannel("channel-2"), is(nullValue()));
        assertThat(store.getChannel("channel-3"), is(notNullValue()));
        assertThat(store.getChannel("channel-4"), is(nullValue()));
    }

    @Test
    public void storeUpdates() {
        final InMemoryDataStore store = new InMemoryDataStore();
        final UUID uaid = UUIDUtil.newUAID();
        final String channelId1 = UUID.randomUUID().toString();
        store.storeUpdates(updates(update(channelId1, 10L)), uaid);
        final Set<Update> updates = store.getUpdates(uaid);
        assertThat(updates, hasItem(update(channelId1, 10L)));
    }

    @Test
    public void storeUpdateWithGreatVersion() {
        final InMemoryDataStore store = new InMemoryDataStore();
        final UUID uaid = UUIDUtil.newUAID();
        final String channelId1 = UUID.randomUUID().toString();
        store.storeUpdates(updates(update(channelId1, 10L)), uaid);
        store.storeUpdates(updates(update(channelId1, 11L)), uaid);
        final Set<Update> updates = store.getUpdates(uaid);
        assertThat(updates, hasItem(update(channelId1, 11L)));
        assertThat(updates.size(), is(1));
    }

    @Test
    public void removeUpdate() {
        final InMemoryDataStore store = new InMemoryDataStore();
        final UUID uaid = UUIDUtil.newUAID();
        final String channelId1 = UUID.randomUUID().toString();
        store.storeUpdates(updates(update(channelId1, 10L)), uaid);
        assertThat(store.removeUpdate(update(channelId1, 10L), uaid), is(true));
        assertThat(store.removeUpdate(update(channelId1, 10L), uaid), is(false));
        assertThat(store.removeUpdate(update(channelId1, 11L), uaid), is(false));
    }

    @Test
    public void updatesThreadSafety() throws InterruptedException {
        final InMemoryDataStore store = new InMemoryDataStore();
        final UUID uaid = UUIDUtil.newUAID();
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
                            store.storeUpdates(updates(update(channelId, 10L)), uaid);
                            store.storeUpdates(updates(update(channelId, 11L)), uaid);
                            store.storeUpdates(updates(update(channelId, 12L)), uaid);
                            store.storeUpdates(updates(update(channelId, 13L)), uaid);
                            final Set<Update> updates = store.getUpdates(uaid);
                            assertThat(updates, hasItems(update(channelId, 13L)));
                            assertThat(store.removeUpdate(update(channelId, 13L), uaid), is(true));
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

    private Update update(final String channelId, final Long version) {
        return new UpdateImpl(channelId, version);
    }

    private Set<Update> updates(final Update... updates) {
        return new HashSet<Update>(Arrays.asList(updates));
    }

}
