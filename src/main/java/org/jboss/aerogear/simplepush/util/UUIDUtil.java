package org.jboss.aerogear.simplepush.util;

import java.util.UUID;

public class UUIDUtil {
    
    private UUIDUtil() {
    }
    
    public static boolean nullOrEmpty(final String uuid) {
        return (uuid == null) || uuid.equals("");
    }
    
    public static UUID createVersion4Id() {
        return UUID.randomUUID();
    }

}
