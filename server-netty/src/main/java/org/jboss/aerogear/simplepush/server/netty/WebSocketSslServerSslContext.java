/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.jboss.aerogear.simplepush.server.netty;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.Security;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

import org.jboss.aerogear.io.netty.handler.codec.sockjs.SockJsConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates a {@link SSLContext} for just server certificates.
 */
public final class WebSocketSslServerSslContext {

    private static final String PROTOCOL = "TLS";
    private final SockJsConfig sockJsConfig;
    private final Logger logger = LoggerFactory.getLogger(SimplePushSockJSService.class);

    WebSocketSslServerSslContext(final SockJsConfig sockJsConfig) {
        this.sockJsConfig = sockJsConfig;
    }

    /**
     * Creates a new {@link SSLContext}. This is an expensive operation and should only be done
     * once and then the SSL context can be reused.
     *
     * @return {@link SSLContext} the SSLContext.
     */
    public SSLContext sslContext() {
        try {
            final SSLContext serverContext = SSLContext.getInstance(PROTOCOL);
            serverContext.init(keyManagerFactory(loadKeyStore()).getKeyManagers(), null, null);
            return serverContext;
        } catch (final Exception e) {
            throw new RuntimeException("Failed to initialize the server-side SSLContext", e);
        }
    }

    private KeyManagerFactory keyManagerFactory(final KeyStore ks) throws Exception {
        final KeyManagerFactory kmf = KeyManagerFactory.getInstance(getKeyManagerAlgorithm());
        kmf.init(ks, sockJsConfig.keyStorePassword().toCharArray());
        return kmf;
    }

    @SuppressWarnings("resource")
    private KeyStore loadKeyStore() throws Exception {
        InputStream fin = null;
        try {
            fin = this.getClass().getResourceAsStream(sockJsConfig.keyStore());
            if (fin == null) {
                throw new IllegalStateException("Could not locate keystore [" + sockJsConfig.keyStore() + "]");
            }
            final KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(fin, sockJsConfig.keyStorePassword().toCharArray());
            return ks;
        } finally {
            safeClose(fin);
        }

    }

    private String getKeyManagerAlgorithm() {
        String algorithm = Security.getProperty("ssl.KeyManagerFactory.algorithm");
        if (algorithm == null) {
            algorithm = "SunX509";
        }
        return algorithm;
    }

    private void safeClose(final Closeable c) {
        try {
            if (c != null) {
                c.close();
            }
        } catch (final IOException e) {
            logger.error("Error while trying to close closable [" + c + "]", e);
        }
    }

}
