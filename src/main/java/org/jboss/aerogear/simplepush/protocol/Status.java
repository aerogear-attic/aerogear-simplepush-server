package org.jboss.aerogear.simplepush.protocol;

/**
 * Represents a status that may be returned by a Register, Unregister messages in 
 * <a href="https://wiki.mozilla.org/WebAPI/SimplePush/Protocol">SimplePush specification protocol</a>
 */
public interface Status {
    
    int getCode();

    String getMessage();
}
