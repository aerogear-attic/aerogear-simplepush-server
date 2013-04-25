package org.jboss.aerogear.simplepush.util;

import java.util.Collection;

public class ArgumentUtil {
    
    private ArgumentUtil() {
    }
    
    public static <T> void checkNotNull(final T ref, final String name) {
        if (ref == null) {
            throw new NullPointerException("[" + name + "] must not be null");
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
