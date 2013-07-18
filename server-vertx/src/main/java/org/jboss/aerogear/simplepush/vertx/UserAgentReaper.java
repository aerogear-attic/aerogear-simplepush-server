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
        final Long reaperTimeout = container.config().getLong("userAgentReaperTimeout", 300000);
        logger.info("Started UserAgent Reaper with timeout of [" + reaperTimeout + "]");
        final ConcurrentMap<String, Long> lastAccessedMap = vertx.sharedData().getMap(VertxSimplePushServer.LAST_ACCESSED_MAP);
        final ConcurrentMap<String, String> writeHandlerMap = vertx.sharedData().getMap(VertxSimplePushServer.WRITE_HANDLER_MAP);

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
