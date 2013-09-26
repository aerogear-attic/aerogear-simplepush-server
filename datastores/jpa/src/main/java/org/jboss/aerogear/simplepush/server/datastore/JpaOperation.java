/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.simplepush.server.datastore;

import javax.persistence.EntityManager;

/**
 * JpaOperation is used to perform any type of operation and is intended to be used
 * by a {@link JpaExecutor}.
 *
 * @param <T> The type of the return value from the operation. Use {@link Void} if
 *            the operation does not return anything.
 *
 * @see JpaExecutor
 */
public interface JpaOperation<T> {

    /**
     * Performs the operation using the passed in EntityManager.
     *
     * @param entityManager the {@link EntityManager} to be used for the operation.
     * @return {@code T} the return type for this operation.
     */
    T perform(EntityManager entityManager);

}
