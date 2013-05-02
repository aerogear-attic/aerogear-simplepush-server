package org.jboss.aerogear.simplepush.server.datastore;

import static org.jboss.aerogear.simplepush.util.ArgumentUtil.checkNotNull;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.jboss.aerogear.simplepush.protocol.Update;
import org.jboss.aerogear.simplepush.server.Channel;
import org.jboss.aerogear.simplepush.server.DefaultChannel;

public class InMemoryDataStore implements DataStore {
    
    private final ConcurrentMap<String, Channel> channels = new ConcurrentHashMap<String, Channel>();
    private final ConcurrentMap<UUID, Set<Update>> notifiedChannels = new ConcurrentHashMap<UUID, Set<Update>>();

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

    @Override
    public void storeUpdates(final Set<Update> updates, final UUID uaid) {
        Set<Update> currentUpdates = notifiedChannels.get(uaid);
        if (currentUpdates == null) {
            currentUpdates = Collections.newSetFromMap(new ConcurrentHashMap<Update, Boolean>());
            final Set<Update> concurrentUpdates = notifiedChannels.putIfAbsent(uaid, currentUpdates);
            if (concurrentUpdates != null) {
                currentUpdates = concurrentUpdates;
            }
        }
        currentUpdates.addAll(updates);
    }

    @Override
    public Set<Update> getUpdates(UUID uaid) {
        final Set<Update> updates = notifiedChannels.get(uaid);
        if (updates == null) {
            return Collections.emptySet();
        }
        return updates;
    }

    @Override
    public boolean removeUpdate(Update update, UUID uaid) {
        final Set<Update> updates = notifiedChannels.get(uaid);
        if (updates != null) {
            return updates.remove(update);
        }
        return false;
    }

}
