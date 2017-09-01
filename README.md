# Deprecated 

SimplePush is no longer maintained by Mozilla, see:
https://twitter.com/MozillaWebpush/status/896095563942318080

## Aerogear SimplePush Server [![Build Status](https://travis-ci.org/aerogear/aerogear-simplepush-server.png)](https://travis-ci.org/aerogear/aerogear-simplepush-server)
This project is a Java server side implementation of the [SimplePush Protocol](https://wiki.mozilla.org/WebAPI/SimplePush/Protocol) 
specification from Mozilla that describes a JavaScript API and a protocol which allows backend/application developers to 
send notification messages to their web applications. Originally SimplePush was introduced for Firefox OS but there are 
plans to establish the API on the desktop and mobile browsers as well.

Firefox OS v1.1 uses SimplePush as its Push Notification technology. Firefox OS Push Notifications are designed for one 
thing – waking up apps. They do not deal with data, desktop notifications and other features, since there are other 
Web APIs that provide them. From the very beginning SimplePush was designed to explicitly not carry any payload. 
Instead a version number is sent to the client. Based on that version number the client can perfom an action, e.g. refresh a view of data.

Mozilla published a very detailed [article](https://hacks.mozilla.org/2013/07/dont-miss-out-on-the-real-time-fun-use-firefox-os-push-notifications/) 
that explains the API in depth.

|                 | Project Info  |
| --------------- | ------------- |
| License:        | Apache License, Version 2.0  |
| Build:          | Maven  |
| Documentation:  | https://aerogear.org/push/  |
| Issue tracker:  | https://issues.jboss.org/browse/AGPUSH  |
| Mailing lists:  | [aerogear-users](http://aerogear-users.1116366.n5.nabble.com/) ([subscribe](https://lists.jboss.org/mailman/listinfo/aerogear-users))  |
|                 | [aerogear-dev](http://aerogear-dev.1069024.n5.nabble.com/) ([subscribe](https://lists.jboss.org/mailman/listinfo/aerogear-dev))  |

AeroGear SimplePush consists of the following modules:

* [common](https://github.com/aerogear/aerogear-simple-push-server/tree/master/common)  
Just common classes used by multiple modules in the project.

* [datastores](https://github.com/aerogear/aerogear-simple-push-server/tree/master/datastores)  
Contains implementations of various datastores. Please see the specific datastore's readme for further details.

* [protocol](https://github.com/aerogear/aerogear-simple-push-server/tree/master/protocol)  
The SimplePush Server Protocol provides interfaces for the protocol.

* [server-api](https://github.com/aerogear/aerogear-simple-push-server/tree/master/server-api)  
An API for AeroGear SimplePush Server

* [server-core](https://github.com/aerogear/aerogear-simple-push-server/tree/master/server-core)  
An implementation of AeroGear SimplePush Server API.

* [server-netty](https://github.com/aerogear/aerogear-simple-push-server/tree/master/server-netty)  
The SimplePush Server implementation that uses Netty 4.x.

* [server-vertx](https://github.com/aerogear/aerogear-simple-push-server/tree/master/server-vertx)  
The SimplePush Server implementation that uses vert.x.

* [wildfly-module](https://github.com/aerogear/aerogear-simple-push-server/tree/master/wildfly-module)  
A WildFly/AS7 module for the SimplePush Server.

Please refer to the above modules documentation for more information.


## Docker
You can use [Docker](https://www.docker.io) to build and run SimplePush server. Follow the instructions to
 [install docker](https://www.docker.io/gettingstarted/).

The Docker [image]() provided contains CouchDB and Redis which enables the functional tests that use these databases
to be run.

### Build a SimplePush Container
#### Build using github path
```docker build -t simplepush github.com/aerogear/aerogear-simplepush-server```

### Build using cloned project
```docker build -t simplepush .```

### Run integration tests
`docker run -it simplepush`

### Run standalone Netty server
```docker run -p 7777:7777 -w /home/aerogear-simplepush-server/server-netty -it simplepush mvn exec:java```

### Manually running test
You may want to trigger test using a different branch, perhaps to run the integration tests against
that code base. This can be done by starting the image using a shell:

```docker run -it simplepush /bin/bash```

You'll need to start the databases (currently CouchDB and Redis):

    /startdbs.sh

Now, you can clone your fork and checkout a branch. To run all tests including the integration/functional tests:

```mvn install -Pcouchdb,redis```

#### Port forwarding for Mac OS X
You'll need to configure your VirtualBox to support port forwarding for port ```7777```:

```VBoxManage modifyvm "boot2docker-vm" --natpf1 "guestnginx,tcp,,7777,,7777"```

## Documentation

For more details about the current release, please consult [our documentation](https://aerogear.org/push).

## Development

If you would like to help develop AeroGear you can join our [developer's mailing list](https://lists.jboss.org/mailman/listinfo/aerogear-dev), join #aerogear on Freenode, or shout at us on Twitter @aerogears.

Also takes some time and skim the [contributor guide](http://aerogear.org/docs/guides/Contributing/)

## Questions?

Join our [user mailing list](https://lists.jboss.org/mailman/listinfo/aerogear-users) for any questions or help! We really hope you enjoy app development with AeroGear!

## Found a bug?

If you found a bug please create a ticket for us on [Jira](https://issues.jboss.org/browse/AGPUSH) with some steps to reproduce it.
