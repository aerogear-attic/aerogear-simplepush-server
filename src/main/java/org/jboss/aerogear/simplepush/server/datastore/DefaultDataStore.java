package org.jboss.aerogear.simplepush.server.datastore;

import static org.jboss.aerogear.simplepush.util.ArgumentUtil.checkNotNull;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.jboss.aerogear.simplepush.server.Channel;
import org.jboss.aerogear.simplepush.server.DefaultChannel;

public class DefaultDataStore implements DataStore {
    
    private final ConcurrentMap<String, Channel> channels = new ConcurrentHashMap<String, Channel>();

    @Override
    public boolean saveChannel(final Channel ch) {
        checkNotNull(ch, "ch");
        final Channel previous = channels.putIfAbsent(ch.getChannelId(), new DefaultChannel(ch.getUAID(), ch.getChannelId(), ch.getPushEndpoint()));
        return previous == null;
    }

    @Override
    public boolean removeChannel(final String channelId) {
        checkNotNull(channelId, "channelId");
        final Channel channel = channels.remove(channelId);
        return channel != null;
    }

    @Override
    public Channel getChannel(final String channelId) {
        checkNotNull(channelId, "channelId");
        return channels.get(channelId);
    }

    @Override
    public void removeChannels(final UUID uaid) {
        checkNotNull(uaid, "uaid");
        for (Channel channel : channels.values()) {
            if (channel.getUAID().equals(uaid)) {
                channels.remove(channel.getChannelId());
            }
        }
    }

}
