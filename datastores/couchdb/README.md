# CouchDB DataStore for AeroGear SimplePush
This project implements a data store for [CouchDB](http://http://couchdb.apache.org/) using the Java client library 
[Ektorp](https://http://http://www.ektorp.org).


## Building and testing
To build this project run the following command:

    mvn install
    
### Testing
To run the tests for this project a local CouchDB server must running on localhost and listening to port 5984.
After starting the CouchDB server execute the following command to run the tests:

    mvn install -Pcouchdb

