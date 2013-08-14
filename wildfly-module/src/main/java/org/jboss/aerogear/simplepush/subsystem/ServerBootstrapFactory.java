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

package org.jboss.aerogear.simplepush.subsystem;

import java.util.concurrent.ThreadFactory;

import org.jboss.as.network.SocketBinding;

import io.netty.bootstrap.ServerBootstrap;

/**
 * A factory for creating {@link ServerBootstrap} instances.
 */
public interface ServerBootstrapFactory {

    /**
     * Sole factory method.
     *
     * @param socketBinding the {@link SocketBinding} provided by WildFly.
     * @param threadFactory the {@link ThreadFactory} provided by WildFly, or null if no thread-factory was configured.
     * @param tokenKey a token key used for encryption/decryption.
     * @param endpointTls if true the https will be used for notification endpoints.
     * @return {@code ServerBootstrap} the {@link ServerBootstrap}.
     */
    ServerBootstrap createServerBootstrap(SocketBinding socketBinding, ThreadFactory threadFactory,
            final String tokenKey,
            final boolean endpointTls);

}
