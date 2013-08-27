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
package org.jboss.aerogear.simplepush.server.netty;

import org.jboss.aerogear.io.netty.handler.codec.sockjs.AbstractSockJsServiceFactory;
import org.jboss.aerogear.io.netty.handler.codec.sockjs.SockJsConfig;
import org.jboss.aerogear.io.netty.handler.codec.sockjs.SockJsService;

import org.jboss.aerogear.simplepush.server.DefaultSimplePushServer;
import org.jboss.aerogear.simplepush.server.SimplePushServerConfig;
import org.jboss.aerogear.simplepush.server.datastore.DataStore;

/**
 * Factory class that creates instances of {@link SimplePushSockJSService}.
 */
public class SimplePushServiceFactory extends AbstractSockJsServiceFactory {

    private final DataStore datastore;
    private final SimplePushServerConfig simplePushConfig;

    /**
     * Sole constructor.
     *
     * @param sockjsConfig the Netty SockJS configuration.
     * @param datastore the {@link DataStore} to be used by all instances created.
     * @param simplePushConfig the {@link SimplePushServerConfig} configuration.
     */
    public SimplePushServiceFactory(final SockJsConfig sockjsConfig, final DataStore datastore,
            final SimplePushServerConfig simplePushConfig) {
        super(sockjsConfig);
        this.datastore = datastore;
        this.simplePushConfig = simplePushConfig;
    }

    @Override
    public SockJsService create() {
        return new SimplePushSockJSService(config(), new DefaultSimplePushServer(datastore, simplePushConfig));
    }

}
