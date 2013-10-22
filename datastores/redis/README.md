# Redis DataStore for AeroGear SimplePush
This project implements a data store for [Redis](http://redis.io/) using the Java client library 
[Jedis](https://github.com/xetorthio/jedis)


## Building and testing
To build this project run the following command:

    mvn install
    
### Testing
To run the tests for this project a local Redis server must running on localhost and listening to port 6397.
After starting the Redis server execute the following command to run the tests:

    mvn install -Predis

