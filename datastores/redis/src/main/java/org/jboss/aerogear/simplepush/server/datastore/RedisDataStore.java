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

import java.nio.charset.Charset;
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
 */
public class RedisDataStore implements DataStore {

    private final static String CHID_LOOKUP_KEY_PREFIX = "chid:lookup:";
    private final static String UAID_LOOKUP_KEY_PREFIX = "uaid:lookup:";
    private final static String TOKEN_LOOKUP_KEY_PREFIX = "token:lookup:";
    private final static String ACK_LOOKUP_KEY_PREFIX = "ack:";
    private final static String ACKS_LOOKUP_KEY_PREFIX = "acks:";
    private final static String TOKEN_KEY = "token";
    private final static String UAID_KEY = "uaid";

    private final Logger logger = LoggerFactory.getLogger(RedisDataStore.class);
    private final static Charset UTF_8 = Charset.forName("UTF-8");
    private final JedisPool jedisPool;

    public RedisDataStore(final String host, final int port) {
        jedisPool = new JedisPool(new JedisPoolConfig(), host, port);
    }

    @Override
    public void savePrivateKeySalt(final byte[] salt) {
        final Jedis jedis = jedisPool.getResource();
        try {
            jedis.set("salt", new String(salt, UTF_8));
        } finally {
            jedisPool.returnResource(jedis);
        }
    }

    @Override
    public byte[] getPrivateKeySalt() {
        final Jedis jedis = jedisPool.getResource();
        try {
            final String salt = jedis.get("salt");
            return salt != null ? salt.getBytes(UTF_8) : new byte[]{};
        } finally {
            jedisPool.returnResource(jedis);
        }
    }

    @Override
    public boolean saveChannel(final Channel channel) {
        final Jedis jedis = jedisPool.getResource();
        try {
            final String uaid = channel.getUAID();
            final String chid = channel.getChannelId();
            if (jedis.sismember(uaidLookupKey(uaid), chid)) {
                return false;
            }
            final String endpointToken = channel.getEndpointToken();
            final Transaction tx = jedis.multi();
            tx.set(endpointToken, Long.toString(channel.getVersion()));
            tx.set(tokenLookupKey(endpointToken), chid);
            tx.hmset(chidLookupKey(chid), mapOf(endpointToken, uaid));
            tx.sadd(uaidLookupKey(uaid), chid);
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
            tx.del(chidLookupKey(channelId));
            tx.del(tokenLookupKey(endpointToken));
            tx.srem(uaidLookupKey(channel.getUAID()), channelId);
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
            final List<String> endpointTokenAndUaid = jedis.hmget(chidLookupKey(channelId), TOKEN_KEY, UAID_KEY);
            if (!endpointTokenAndUaid.isEmpty()) {
                final String endpointToken = endpointTokenAndUaid.get(0);
                final String uaid = endpointTokenAndUaid.get(1);
                if (endpointToken == null || uaid == null) {
                    throw channelNotFoundException(channelId);
                }
                return new DefaultChannel(uaid, channelId, Long.valueOf(jedis.get(endpointToken)), endpointToken);
            }
            throw channelNotFoundException(channelId);
        } finally {
            jedisPool.returnResource(jedis);
        }
    }

    @Override
    public Set<String> getChannelIds(final String uaid) {
        final Jedis jedis = jedisPool.getResource();
        try {
            return jedis.smembers(uaidLookupKey(uaid));
        } finally {
            jedisPool.returnResource(jedis);
        }
    }

    @Override
    public void removeChannels(final String uaid) {
        //TODO: This is not efficient. Can we do the clean up in some other way.
        //      This is only called from the reaper thread, perhaps we can use an expiration
        //      like Mozilla does or something equivalent.
        final Jedis jedis = jedisPool.getResource();
        try {
            for (String channelId : getChannelIds(uaid)) {
                removeChannel(channelId);
            }
            jedis.del(uaidLookupKey(uaid));
        } finally {
            jedisPool.returnResource(jedis);
        }
    }

    @Override
    public String updateVersion(final String endpointToken, final long newVersion) throws VersionException, ChannelNotFoundException {
        final Jedis jedis = jedisPool.getResource();
        try {
            jedis.watch(endpointToken);
            final String versionString = jedis.get(endpointToken);
            if (versionString == null) {
                throw channelNotFoundException(endpointToken);
            }
            final long currentVersion = Long.valueOf(versionString);
            if (newVersion <= currentVersion) {
                throw new VersionException("version [" + newVersion + "] must be greater than the current version [" + currentVersion + "]");
            }
            final Transaction tx = jedis.multi();
            tx.set(endpointToken, String.valueOf(newVersion));
            tx.exec();
            logger.debug(tokenLookupKey(endpointToken));
            return jedis.get(tokenLookupKey(endpointToken));
        } finally {
            jedisPool.returnResource(jedis);
        }
    }

    @Override
    public String saveUnacknowledged(final String channelId, final long version) {
        final Jedis jedis = jedisPool.getResource();
        try {
            jedis.set(ackLookupKey(channelId), Long.toString(version));
            final List<String> hashValues = jedis.hmget(chidLookupKey(channelId), UAID_KEY);
            final String uaid = hashValues.get(0);
            jedis.sadd(acksLookupKey(uaid), channelId);
            return uaid;
        } finally {
            jedisPool.returnResource(jedis);
        }
    }

    @Override
    public Set<Ack> getUnacknowledged(final String uaid) {
        final Jedis jedis = jedisPool.getResource();
        try {
            final Set<String> unacks = jedis.smembers(acksLookupKey(uaid));
            if (unacks.isEmpty()) {
                return Collections.emptySet();
            }
            final Set<Ack> acks = new HashSet<Ack>(unacks.size());
            for (String channelId : unacks) {
                acks.add(new AckImpl(channelId, Long.valueOf(jedis.get(ackLookupKey(channelId)))));
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
                jedis.del(ackLookupKey(ack.getChannelId()));
                jedis.srem(acksLookupKey(uaid), ack.getChannelId());
            }
            return getUnacknowledged(uaid);
        } finally {
            jedisPool.returnResource(jedis);
        }
    }

    private static String chidLookupKey(final String channelId) {
        return CHID_LOOKUP_KEY_PREFIX + channelId;
    }

    private static String tokenLookupKey(final String endpointToken) {
        return TOKEN_LOOKUP_KEY_PREFIX + endpointToken;
    }

    private static String uaidLookupKey(final String uaid) {
        return UAID_LOOKUP_KEY_PREFIX + uaid;
    }

    private static String ackLookupKey(final String channelId) {
        return ACK_LOOKUP_KEY_PREFIX + channelId;
    }

    private static String acksLookupKey(final String uaid) {
        return ACKS_LOOKUP_KEY_PREFIX + uaid;
    }

    private static ChannelNotFoundException channelNotFoundException(final String channelId) {
        return new ChannelNotFoundException("Could not find channel [" + channelId + "]", channelId);
    }

}