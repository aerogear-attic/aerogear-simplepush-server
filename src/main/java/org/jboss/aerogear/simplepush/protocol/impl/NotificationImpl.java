package org.jboss.aerogear.simplepush.protocol.impl;

import static org.jboss.aerogear.simplepush.util.ArgumentUtil.checkNotNullAndNotEmpty;

import java.util.Collections;
import java.util.Set;

import org.jboss.aerogear.simplepush.protocol.Notification;
import org.jboss.aerogear.simplepush.protocol.Update;

public class NotificationImpl implements Notification {
    
    private final Set<Update> updates;

    public NotificationImpl(final Set<Update> updates) {
        checkNotNullAndNotEmpty(updates, "updates");
        this.updates = updates;
    }

    @Override
    public Type getMessageType() {
        return Type.NOTIFICATION;
    }

    @Override
    public Set<Update> getUpdates() {
        return Collections.<Update>unmodifiableSet(updates);
    }
    
    @Override
    public String toString() {
        return "NotificationImpl[messageType=" + getMessageType() + ", updates=" + updates + "]";
    }

}
