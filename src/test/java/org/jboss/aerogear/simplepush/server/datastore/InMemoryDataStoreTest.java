package org.jboss.aerogear.simplepush.server.datastore;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.UUID;

import org.jboss.aerogear.simplepush.server.DefaultChannel;
import org.jboss.aerogear.simplepush.util.UUIDUtil;
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

}
