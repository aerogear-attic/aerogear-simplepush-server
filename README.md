# AeroGear SimplePush Server
__Disclaimer: This is only a proof of concept.__  
This project is a Java implementation of the server side that follows the [SimplePush Protocol](https://wiki.mozilla.org/WebAPI/SimplePush/Protocol)

## Usage

### Build the SimplePush Server

    mvn install

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
   
   
## Protocol

### Hello Handshake
Is sent by the UserAgent to the SimplePush Server:

![Hello Message](https://raw.github.com/danbev/aerogear-simplepush-server/master/src/etc/images/hello-message.png)

#### Message format

    {
       "messageType": "hello",
       "uaid": "fd52438f-1c49-41e0-a2e4-98e49833cc9c",
       "channelIDs": ["431b4391-c78f-429a-a134-f890b5adc0bb", "a7695fa0-9623-4890-9c08-cce0231e4b36"]
    }
    
```uaid```  
The UserAgent Identifier is optional and if not specified a UAID will be created on by the SimplePush Server. This can 
be used as a way of reseting.

```channel_IDs```  
The channelIds passed in are identifiers created on the client side and will be associated with the ```UAID```. In the case
of a Hello Message the _channelIDs_ represent channels that the UserAgent want to have registered.

The SimplePush Server will ignore any additional Hello Messages after the first one on the web socket connection. 


