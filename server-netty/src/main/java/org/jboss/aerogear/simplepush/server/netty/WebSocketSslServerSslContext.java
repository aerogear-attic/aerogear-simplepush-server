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

import java.io.InputStream;
import java.security.KeyStore;
import java.security.Security;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

/**
 * Creates a {@link SSLContext} for just server certificates.
 */
public final class WebSocketSslServerSslContext {

    private static final Logger logger = Logger.getLogger(WebSocketSslServerSslContext.class.getName());
    private static final String PROTOCOL = "TLS";
    private final SSLContext serverContext;

    /**
     * Returns the singleton instance for this class
     */
    public static WebSocketSslServerSslContext getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private interface SingletonHolder {
        WebSocketSslServerSslContext INSTANCE = new WebSocketSslServerSslContext();
    }

    /**
     * Constructor for singleton
     */
    private WebSocketSslServerSslContext() {
        SSLContext serverContext = null;
        try {
            String algorithm = Security.getProperty("ssl.KeyManagerFactory.algorithm");
            if (algorithm == null) {
                algorithm = "SunX509";
            }

            InputStream fin = null;
            try {
                String keyStorePassword = System.getProperty("simplepush.keystore.password");

                final KeyStore ks = KeyStore.getInstance("JKS");
                fin = this.getClass().getResourceAsStream("/simplepush.keystore");
                ks.load(fin, keyStorePassword.toCharArray());

                final KeyManagerFactory kmf = KeyManagerFactory.getInstance(algorithm);
                kmf.init(ks, keyStorePassword.toCharArray());

                serverContext = SSLContext.getInstance(PROTOCOL);
                serverContext.init(kmf.getKeyManagers(), null, null);
            } catch (final Exception e) {
                throw new Error("Failed to initialize the server-side SSLContext", e);
            } finally {
                if (fin != null) {
                    fin.close();
                }
            }
        } catch (final Exception ex) {
            logger.log(Level.WARNING, "Error initializing SslContextManager.", ex);
            throw new RuntimeException("Error initializing SslContextManager.", ex);
        } finally {
            this.serverContext = serverContext;
        }
    }

    /**
     * Returns the server context with server side key store
     */
    public SSLContext serverContext() {
        return serverContext;
    }

}
