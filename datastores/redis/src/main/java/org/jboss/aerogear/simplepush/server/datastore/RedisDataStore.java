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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.aerogear.simplepush.protocol.Ack;
import org.jboss.aerogear.simplepush.protocol.impl.AckImpl;
import org.jboss.aerogear.simplepush.server.Channel;
import org.jboss.aerogear.simplepush.server.DefaultChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Transaction;

/**
 * DataStore that uses a Redis database for storage.
 * </p>
 *
 */
public class RedisDataStore implements DataStore {

    private final static String CHID_LOOKUP = "chid:lookup:";
    private final static String UAID_LOOKUP = "uaid:lookup:";
    private final static String TOKEN_LOOKUP = "token:lookup:";
    private final static String ACK_LOOKUP = "ack:";
    private final static String ACKS_LOOKUP = "acks:";
    private final static String TOKEN_KEY = "token";
    private final static String UAID_KEY = "uaid";

    private final Logger logger = LoggerFactory.getLogger(RedisDataStore.class);
    private final JedisPool jedisPool;

    public RedisDataStore(final String host, final int port) {
        jedisPool = new JedisPool(new JedisPoolConfig(), host, port);
    }

    @Override
    public boolean saveChannel(final Channel channel) {
        final Jedis jedis = jedisPool.getResource();
        try {
            final String uaid = channel.getUAID();
            final String chid = channel.getChannelId();
            final String endpointToken = channel.getEndpointToken();
            if (jedis.sismember(UAID_LOOKUP + uaid, chid)) {
                return false;
            }
            final Transaction tx = jedis.multi();
            tx.set(endpointToken, Long.toString(channel.getVersion()));
            tx.set(TOKEN_LOOKUP + endpointToken, chid);
            tx.hmset(CHID_LOOKUP + chid, mapOf(endpointToken, uaid));
            tx.sadd(UAID_LOOKUP + uaid, chid);
            tx.exec();
            return true;
        } finally {
            jedisPool.returnResource(jedis);
        }
    }

    private Map<String, String> mapOf(final String endpointToken, final String uaid) {
        final Map<String, String> map = new HashMap<String, String>(2);
        map.put(TOKEN_KEY, endpointToken);
        map.put(UAID_KEY, uaid);
        return map;
    }

    private void removeChannel(final String channelId) {
        final Jedis jedis = jedisPool.getResource();
        try {
            final Channel channel = getChannel(channelId);
            final String endpointToken = channel.getEndpointToken();
            final Transaction tx = jedis.multi();
            tx.del(endpointToken);
            tx.del(CHID_LOOKUP + channelId);
            tx.del(TOKEN_LOOKUP + endpointToken);
            tx.srem(UAID_LOOKUP + channel.getUAID(), channelId);
            tx.exec();
        } catch (final ChannelNotFoundException e) {
            logger.debug("ChannelId [" + channelId + "] was not found");
        } finally {
            jedisPool.returnResource(jedis);
        }
    }

    @Override
    public void removeChannels(final Set<String> channelIds) {
        for (String channelId : channelIds) {
            removeChannel(channelId);
        }
    }

    @Override
    public Channel getChannel(final String channelId) throws ChannelNotFoundException {
        final Jedis jedis = jedisPool.getResource();
        try {
            final List<String> hmget = jedis.hmget(CHID_LOOKUP + channelId, TOKEN_KEY, UAID_KEY);
            if (!hmget.isEmpty()) {
                final String endpointToken = hmget.get(0);
                final String uaid = hmget.get(1);
                if (endpointToken == null || uaid == null) {
                    throw new ChannelNotFoundException("Could not find channel [" + channelId + "]", channelId);
                }
                return new DefaultChannel(uaid, channelId, Long.valueOf(jedis.get(endpointToken)), endpointToken);
            }
            throw new ChannelNotFoundException("Could not find channel [" + channelId + "]", channelId);
        } finally {
            jedisPool.returnResource(jedis);
        }
    }

    @Override
    public Set<String> getChannelIds(final String uaid) {
        final Jedis jedis = jedisPool.getResource();
        try {
            return jedis.smembers(UAID_LOOKUP + uaid);
        } finally {
            jedisPool.returnResource(jedis);
        }
    }

    @Override
    public void removeChannels(final String uaid) {
        //TODO: This is not efficient. Can we to the clean up in some other way.
        //      This is only called from the reaper thread, perhaps we can use an expiration
        //      or something equivalent.
        final Jedis jedis = jedisPool.getResource();
        try {
            for (String channelId : getChannelIds(uaid)) {
                removeChannel(channelId);
            }
            jedis.del(UAID_LOOKUP + uaid);
        } finally {
            jedisPool.returnResource(jedis);
        }
    }

    @Override
    public String updateVersion(final String endpointToken, final long version) throws VersionException, ChannelNotFoundException {
        final Jedis jedis = jedisPool.getResource();
        try {
            jedis.watch(endpointToken);
            final String versionString = jedis.get(endpointToken);
            if (versionString == null) {
                throw new ChannelNotFoundException("Could not find endpointToken [" + endpointToken + "]", endpointToken);
            }
            final long v = Long.valueOf(versionString);
            if (version <= v) {
                throw new VersionException("version [" + version + "] must be greater than the current version [" + v + "]");
            }
            final Transaction tx = jedis.multi();
            tx.set(endpointToken, String.valueOf(version));
            final List<Object> exec = tx.exec();
            logger.info("Result : " + exec);
            logger.info(TOKEN_LOOKUP + endpointToken);
            return jedis.get(TOKEN_LOOKUP + endpointToken);
        } finally {
            jedisPool.returnResource(jedis);
        }
    }

    @Override
    public String saveUnacknowledged(final String channelId, final long version) {
        final Jedis jedis = jedisPool.getResource();
        try {
            jedis.set(ACK_LOOKUP + channelId, Long.toString(version));
            final List<String> hashValues = jedis.hmget(CHID_LOOKUP + channelId, UAID_KEY);
            final String uaid = hashValues.get(0);
            jedis.sadd(ACKS_LOOKUP + uaid, channelId);
            return uaid;
        } finally {
            jedisPool.returnResource(jedis);
        }
    }

    @Override
    public Set<Ack> getUnacknowledged(final String uaid) {
        final Jedis jedis = jedisPool.getResource();
        try {
            final Set<String> unacks = jedis.smembers(ACKS_LOOKUP + uaid);
            if (unacks.isEmpty()) {
                return Collections.emptySet();
            }
            final Set<Ack> acks = new HashSet<Ack>(unacks.size());
            for (String channelId : unacks) {
                acks.add(new AckImpl(channelId, Long.valueOf(jedis.get(ACK_LOOKUP + channelId))));
            }
            return acks;
        } finally {
            jedisPool.returnResource(jedis);
        }
    }

    @Override
    public Set<Ack> removeAcknowledged(final String uaid, final Set<Ack> acks) {
        final Jedis jedis = jedisPool.getResource();
        try {
            for (Ack ack : acks) {
                jedis.del(ACK_LOOKUP + ack.getChannelId());
                jedis.srem(ACKS_LOOKUP + uaid, ack.getChannelId());
            }
            return getUnacknowledged(uaid);
        } finally {
            jedisPool.returnResource(jedis);
        }
    }

}