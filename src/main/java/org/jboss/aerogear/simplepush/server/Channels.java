package org.jboss.aerogear.simplepush.server;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


public class Channels {
    
    private ConcurrentMap<String, Set<Channel>> userAgents = new ConcurrentHashMap<String, Set<Channel>>();
    
    public void addUserAgent(final UUID uaid) {
        userAgents.putIfAbsent(uaid.toString(), new HashSet<Channel>());
    }
    
    public void addUserAgent(final UUID uaid, final Set<String> channelIds) {
        final Set<Channel> channels = new HashSet<Channel>();
        for (String channelId : channelIds) {
            channels.add(new DefaultChannel(channelId, defaultEndpoint(channelId)));
        }
        userAgents.putIfAbsent(uaid.toString(), channels);
    }
    
    private String defaultEndpoint(final String channelId) {
        return "/endpoint/" + channelId;
    }

    public Set<Channel> getChannels(UUID uaid) {
        return userAgents.get(uaid.toString());
    }
    
}
