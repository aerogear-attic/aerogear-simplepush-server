/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.simplepush.server.netty;

import org.jboss.aerogear.simplepush.server.DefaultSimplePushServer;
import org.jboss.aerogear.simplepush.server.datastore.DataStore;

import io.netty.handler.codec.sockjs.AbstractServiceFactory;
import io.netty.handler.codec.sockjs.Config;
import io.netty.handler.codec.sockjs.SockJSService;

public class SimplePushServiceFactory extends AbstractServiceFactory {

    private final DataStore datastore;
    private final SimplePushConfig simplePushConfig;
    
    public SimplePushServiceFactory(final Config sockjsConfig, final DataStore datastore,
            final SimplePushConfig simplePushConfig) {
        super(sockjsConfig);
        this.datastore = datastore;
        this.simplePushConfig = simplePushConfig;
    }

    @Override
    public SockJSService create() {
        return new SimplePushSockJSService(config(), new DefaultSimplePushServer(datastore), simplePushConfig);
    }

}
