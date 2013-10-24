package org.jboss.aerogear.simplepush.subsystem;

import org.jboss.aerogear.simplepush.server.datastore.DataStore;
import org.jboss.aerogear.simplepush.server.datastore.InMemoryDataStore;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;

public class InMemoryDataStoreService extends DataStoreService {

    @Override
    public synchronized void start(StartContext context) throws StartException {
    }

    @Override
    public synchronized DataStore getValue() throws IllegalStateException, IllegalArgumentException {
        return new InMemoryDataStore();
    }

}
