package org.jboss.aerogear.simplepush.vertx;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import org.vertx.java.core.Handler;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.platform.Verticle;

public class UserAgentReaper extends Verticle {

    @Override
    public void start() {
        final Logger logger = container.logger();
        final Long reaperTimeout = container.config().getLong("reaperTimeout", 300000);
        logger.info("Started UserAgent Reaper with timeout of [" + reaperTimeout + "]");
        final ConcurrentMap<String, Long> lastAccessedMap = vertx.sharedData().getMap(VertxSimplePushServer.LAST_ACCESSED_MAP);
        final ConcurrentMap<String, Long> writeHandlerMap = vertx.sharedData().getMap(VertxSimplePushServer.WRITE_HANDLER_MAP);
        
        vertx.setPeriodic(reaperTimeout, new Handler<Long>() {
            @Override
            public void handle(final Long timerId) {
                logger.info("UserAgentReaper reaping....");
                final Set<String> markedForRemoval = new HashSet<String>();
                final Set<Entry<String, Long>> entrySet = lastAccessedMap.entrySet();
                for (Entry<String, Long> entry : entrySet) {
                    final String uaid = entry.getKey();
                    final Long timestamp = entry.getValue();
                    final long now = System.currentTimeMillis();
                    if (timestamp + reaperTimeout < now) {
                        markedForRemoval.add(uaid);
                        vertx.eventBus().send(VertxSimplePushServer.USER_AGENT_REMOVER, uaid);
                    }
                }
                for (String uaid : markedForRemoval) {
                    lastAccessedMap.remove(uaid);
                    writeHandlerMap.remove(uaid);
                }
            }
        });
    }
    
}
