# AeroGear SimplePush Server
__Disclaimer: This is only a proof of concept.__  
This project is a Java implementation of the server side that follows the [SimplePush Protocol](https://wiki.mozilla.org/WebAPI/SimplePush/Protocol)

## Usage

### Start the SimplePush Server

    mvn exec:java
    
### Register a channel 
Open the _src/main/resources/netty/websocket.html_ in a browser that support WebSockets.  

1. Click on the _Send Hello_ button to send the hello handshake message.  
2. Type in a channelId and click on the _Register Channel_ button to register a channel.   
2. Type in a channelId and click on the _Unregister Channel_ button to unregister a channel.   

The response JSON messages from the above requests will be displayed in the textaera.

### Send a notification

     curl -i --header "Accept: application/x-www-form-urlencodedl" -X PUT -d "version=1" "http://localhost:8080/endpoint/testChannel"
     
This notification will be displayed in the the textaera of the _websocket.html_ page that has registerd for that 
channel. It would look like this for the above PUT request:

    
   {"messageType":"notification","updates":[{"channelID":"testChannel","version":"1"}]} 