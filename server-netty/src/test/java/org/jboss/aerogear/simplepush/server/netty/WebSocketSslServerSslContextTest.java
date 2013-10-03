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
package org.jboss.aerogear.simplepush.server.netty;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import javax.net.ssl.SSLEngine;

import org.jboss.aerogear.io.netty.handler.codec.sockjs.SockJsConfig;
import org.junit.Test;

public class WebSocketSslServerSslContextTest {

    @Test
    public void createSSLEngine() {
        final SockJsConfig sockJsConfig = SockJsConfig.withPrefix("/echo")
                .tls(true)
                .keyStore("/simplepush-sample.keystore")
                .keyStorePassword("simplepush")
                .build();
        final SSLEngine engine = new WebSocketSslServerSslContext(sockJsConfig).sslContext().createSSLEngine();
        assertThat(engine, is(notNullValue()));
    }

    @Test (expected = RuntimeException.class)
    public void createSSLContextKeyStoreNotFound() {
        final SockJsConfig sockJsConfig = SockJsConfig.withPrefix("/echo")
                .tls(true)
                .keyStore("/missing.keystore")
                .keyStorePassword("simplepush")
                .build();
        new WebSocketSslServerSslContext(sockJsConfig).sslContext();
    }

}
