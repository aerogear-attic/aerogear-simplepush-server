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
import org.jboss.aerogear.simplepush.server.SimplePushServer;

/**
 * Factory class that creates instances of {@link SimplePushSockJSService}.
 */
public class SimplePushServiceFactory extends AbstractSockJsServiceFactory {

    private final SimplePushServer simplePushServer;

    /**
     * Sole constructor.
     *
     * @param sockjsConfig the Netty SockJS configuration.
     * @param simplePushServer the {@link SimplePushServer} to be used by all instances created.
     */
    public SimplePushServiceFactory(final SockJsConfig sockjsConfig, final SimplePushServer simplePushServer) {
        super(sockjsConfig);
        this.simplePushServer = simplePushServer;
    }

    @Override
    public SockJsService create() {
        return new SimplePushSockJSService(config(), simplePushServer);
    }

}
