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
package org.jboss.aerogear.simplepush.util;

import java.util.Collection;

/**
 * Utility methods for checking method arguments.
 */
public final class ArgumentUtil {

    private ArgumentUtil() {
    }

    public static <T> void checkNotNull(final T ref, final String name) {
        if (ref == null) {
            throw new NullPointerException("[" + name + "] must not be null");
        }
    }

    public static <T> void checkNotNullAndNotEmpty(final T ref, final String name) {
        checkNotNull(ref, name);
        if ("".equals(ref)) {
            throw new IllegalArgumentException("[" + name + "] must not be empty");
        }
    }

    public static void checkNotNullAndNotEmpty(final Collection<?> c, final String name) {
        checkNotNull(c, name);
        if (c.isEmpty()) {
            throw new IllegalArgumentException("Collection[" + name + "] must not be empty");
        }
    }

    public static void checkNotEmpty(final Collection<?> c, final String name) {
        if (c.isEmpty()) {
            throw new IllegalArgumentException("Collection[" + name + "] must not be empty");
        }
    }

    public static void checkNotNegative(final long value, final String name) {
        if (value < 0) {
            throw new IllegalArgumentException("[" + name + "] must not be a negative number");
        }
    }

}
