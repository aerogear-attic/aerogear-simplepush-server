# AeroGear SimplePush Server
This project is a Java implementation of the server side that follows the [SimplePush Protocol](https://wiki.mozilla.org/WebAPI/SimplePush/Protocol)
and uses [vert.x](http://vertx.io/).

__Disclaimer__ This is currently only a skeleton impl with a simple http handler. 

## Prerequisites 
This project requires [vert.x 2.0.0-beta4-SNAPSHOT](https://github.com/vert-x/vert.x) to be installed locally.
You should have vertx availalbe on your systems path.

Vert.x requires Java 7.

### Clone vert.x
    git clone https://github.com/vert-x/vert.x
    cd vert.x

#### Install
Running the following target will install vert.x into you local maven repository.

    ./gradlew install
    
If you are on window you use:

    ./gradlew.bat install
    
To build a distribution which enable you to access the ```vertx``` command line tool:

    ./gradlew dist
    
If you are on window you use:

    ./gradlew.bat dist
    
The above command will produce a distribution with the version of vert.x that was built, in the ```build``` directory. Add 
the ```bin``` directory to your PATH and you should be good to go.

## Usage

### Build the SimplePush Server

    mvn install

### Start the SimplePush Server

    cd target
    vertx runmod aerogear~simplepush~0.0.8
    
You can also start the server with a different configuration:

    vertx runmod aerogear~simplepush~0.0.8 -c classes/config.json

The config file can be anything file you like but the ```config.json``` file above contains an example of the configuration 
options and their default values.    

## Configuration
The SimplePush Server vert.x module can be configured using a json configuration file. The following configuration options
are available:

    {
      "port" : 7777,
      "reaperTimeout" : 300000,
      "ackInterval" : 60000
    }
    
#### port
The port that the server should listen to.

#### reaperTimeout
This is a scheduled job that will clean up UserAgent that have been inactive for the specified amount of time.

#### ackInterval
The time, in milliseconds, that a scheduled job will try to resend unacknowledge notifications.    


