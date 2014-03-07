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
package org.jboss.aerogear.simplepush.server.datastore;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JpaExecutor executes an {@link JpaOperation} and makes takes care of transaction
 * demarcation and of closing the EntityManager used for the operation.
 */
public final class JpaExecutor {

    private final Logger logger = LoggerFactory.getLogger(JpaExecutor.class);
    private final EntityManagerFactory entityManagerFactory;

    public JpaExecutor(final EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    /**
     * Executes the passed in operation, wrapping it in a transaction.
     *
     * @param operation the {@link JpaOperation} to be performed.
     * @return {@code T} the return type of the operation.
     */
    public <T> T execute(final JpaOperation<T> operation) {
        final EntityManager em = entityManagerFactory.createEntityManager();
        em.getTransaction().begin();
        try {
            final T t = operation.perform(em);
            em.getTransaction().commit();
            return t;
        } catch (final Exception e) {
            logger.debug("Error while performing JpaOperation:", e);
            em.getTransaction().rollback();
            throw new JpaException("Exception while trying to perform JPA operation", e);
        } finally {
            em.close();
        }
    }

}
