package org.jboss.aerogear.simplepush.protocol;

/**
 * Represents the basis of a message in the 
 * <a href="https://wiki.mozilla.org/WebAPI/SimplePush/Protocol">SimplePush specification protocol</a>
 */
public interface MessageType {
    
    /**
     * The name of the JSON field that identifies a messageType according to the SimplePush protocol.
     */
    String MESSSAGE_TYPE_FIELD = "messageType";
    
    enum Type {HELLO, REGISTER, NOTIFICATION, UNREGISTER, ACK}
    
    /**
     * The value of the 'messageType' field of a JSON SimplePush Protocol message.
     * 
     * @return {@code String} 
     */
    Type getMessageType();
}
