# AeroGear SimplePush Server
This project is a Java implementation of the server side that follows the [SimplePush Protocol](https://wiki.mozilla.org/WebAPI/SimplePush/Protocol)
and uses [vert.x](http://vertx.io/).

__Disclaimer__ This is currently only a skeleton impl with a simple http handler. 

## Prerequisites 
This project requires [vert.x 1.3.1.final](http://vertx.io/downloads.html) to be installed locally.
You should have vertx availalbe on your systems path.

## Usage

### Build the SimplePush Server

    mvn install

### Start the SimplePush Server

    cd target
    vertx runmod aerogear~simplepush~0.0.8
    

