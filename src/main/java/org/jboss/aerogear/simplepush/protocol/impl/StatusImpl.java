package org.jboss.aerogear.simplepush.protocol.impl;

import org.jboss.aerogear.simplepush.protocol.Status;

public class StatusImpl implements Status {
    
    private final int code;
    private final String message;

    public StatusImpl(final int code, final String message) {
        this.code = code;
        this.message = message;
    }
    
    @Override
    public String toString() {
        return "StatusImp[code=" + code + ", message=" + message + "]";
    }
    
    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
    
    
}
