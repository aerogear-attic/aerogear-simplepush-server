package org.jboss.aerogear.simplepush.protocol;

public class Status {
    
    private final int statusCode;
    private final String message;

    public Status(final int statusCode, final String message) {
        this.statusCode = statusCode;
        this.message = message;
    }
    
    @Override
    public String toString() {
        return "Status Code: " + statusCode + " " + message;
    }
    
    public static Status badRequest(final String message) {
        return new Status(400, message);
    }
    
    
}
