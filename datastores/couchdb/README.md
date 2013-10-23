# CouchDB DataStore for AeroGear SimplePush
This project implements a data store for [Apache CouchDB](http://couchdb.apache.org/) using the Java client library 
[Ektorp](http://www.ektorp.org).

For information about installing Apache CouchDB please refer to the [Apache CouchDB Documentation](http://docs.couchdb.org/en/latest/install/index.html)


## Building and testing
To build this project run the following command:

    mvn install
    
### Testing
To run the tests for this project a local Apache CouchDB server must running on localhost and listening to port 5984.
After starting the Apache CouchDB server execute the following command to run the tests:

    mvn install -Pcouchdb

