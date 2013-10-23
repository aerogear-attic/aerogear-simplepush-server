package org.jboss.aerogear.simplepush.subsystem;

import org.jboss.aerogear.simplepush.server.datastore.CouchDBDataStore;
import org.jboss.aerogear.simplepush.server.datastore.DataStore;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;

public class CouchDBDataStoreService extends DataStoreService {

    private final String url;
    private final String dbName;

    public CouchDBDataStoreService(final String url, final String dbName ) {
        this.url = url;
        this.dbName = dbName;
    }

    @Override
    public synchronized void start(StartContext context) throws StartException {
    }

    @Override
    public synchronized DataStore getValue() throws IllegalStateException, IllegalArgumentException {
        return new CouchDBDataStore(url, dbName);
    }

}
