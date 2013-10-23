package org.jboss.aerogear.simplepush.subsystem;

import org.jboss.aerogear.simplepush.server.datastore.DataStore;
import org.jboss.aerogear.simplepush.server.datastore.JpaDataStore;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;

public class JpaDataStoreService extends DataStoreService {

    private final String persistenceUnit;
    private DataStore dataStore;

    public JpaDataStoreService(final String persistenceUnit) {
        this.persistenceUnit = persistenceUnit;
    }

    @Override
    public synchronized void start(StartContext context) throws StartException {
        dataStore = new JpaDataStore(persistenceUnit);
    }

    @Override
    public synchronized DataStore getValue() throws IllegalStateException, IllegalArgumentException {
        return dataStore;
    }

}
