# AeroGear SimplePush Server
This project is a Java implementation of the server side that follows the [SimplePush Protocol](https://wiki.mozilla.org/WebAPI/SimplePush/Protocol)
and uses [vert.x](http://vertx.io/).

__Disclaimer__ This version uses an in-memory data store and will loose all registrations upon shutdown restart.   

## Prerequisites 
[Vert.x](http://vertx.io/downloads.html) is required to be availble and usable from the command line. 
Note also that Vert.x requires Java 7.

## Usage

### Build the SimplePush Server

    mvn install

### Start the SimplePush Server

    cd target
    vertx runmod aerogear~simplepush~<version>
    
You can also start the server with a different configuration:

    vertx runmod aerogear~simplepush~<version> -conf classes/config.json

The config file can be named anything file you like but the ```classes/config.json``` file above contains an example of the configuration 
options and their default values.    

## Configuration
The SimplePush Server vert.x module can be configured using a json configuration file.  
The following configuration options are available:

    {
      "host" : "localhost",
      "port" : 7777,
      "reaperTimeout" : 300000,
      "ackInterval" : 60000,
      "password" : "yourRandomToken"
    }
    
__host__    
The host that the server should bind to.
    
__port__  
The port that the server should bind to.

__reaperTimeout__  
This is a scheduled job that will clean up UserAgent that have been inactive for the specified amount of time.

__ackInterval__  
The time, in milliseconds, that a scheduled job will try to resend unacknowledged notifications.    

__password__
This should be a password that will be used to generate the server private key which is used for  encryption/decryption
of the endpoint URLs that are returned to clients upon successful channel registration.

