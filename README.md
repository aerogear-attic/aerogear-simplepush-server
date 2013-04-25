# AeroGear SimplePush Server
__Disclaimer: This is only a proof of concept.__  
This project is a Java implementation of the server side that follows the [SimplePush Protocol](https://wiki.mozilla.org/WebAPI/SimplePush/Protocol)

## Usage
Start the SimplePushServer (will start the Netty version)

    mvn exec:java
    
Open the _src/main/resources/netty/websocket.html_ in a browser that support WebSockets

__Note__ Only the 'Send Hello' part is implemented at the moment.
