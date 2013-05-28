# AeroGear SimplePush Server
This project is a Java implementation of the server side that follows the [SimplePush Protocol](https://wiki.mozilla.org/WebAPI/SimplePush/Protocol)
The project contains a base implementation of AeroGear SimplePush Server API and is intended to be used by subprojects that
use this implementation to create a concrete server, for example using Netty or Vert.x as the framework that hosts the 
server.

Please refer to [server-netty](https://github.com/danbev/aerogear-simplepush-server/tree/master/server-netty) for an 
example of using this base implementation.


## Usage

### Build the SimplePush Server

    mvn install

## Protocol

### Hello Handshake
Is sent by the UserAgent to the SimplePush Server:

![Hello Message](https://raw.github.com/danbev/aerogear-simplepush-server/master/server/src/etc/images/hello-message.png)  

The SimplePush Server will ignore any additional Hello Messages after the first one on the web socket connection. 

#### Request format

    {
       "messageType": "hello",
       "uaid": "fd52438f-1c49-41e0-a2e4-98e49833cc9c",
       "channelIDs": ["431b4391-c78f-429a-a134-f890b5adc0bb", "a7695fa0-9623-4890-9c08-cce0231e4b36"]
    } 

__uaid__  
The UserAgent Identifier is optional and if not specified a UAID will be created on by the SimplePush Server. This can 
be used as a way of reseting. The ```channeldIDs``` are also optional and when specified these channels will be registered
during the handshake. This can be useful if the UserAgent has stored the ```uaid``` and for some reason has disconnected, and
then reconnected. By passing both a ```uaid``` and the ```channelIDs``` it can restore the channels that were registered at 
the point when the UserAgent disconnected.

__channelIDs__  
The optional channelIds passed in are identifiers created on the client side and will be associated with the ```UAID```. In the case
of a Hello Message the _channelIDs_ represent channels that the UserAgent want to have registered.


#### Response format

    {
       "messageType": "hello",
       "uaid": "fd52438f-1c49-41e0-a2e4-98e49833cc9c",
    } 


### Register
Register is used to register a ```channelId``` with the SimplePush server and enables the the client to be notified when the version 
for this channel is updated.

![Register Channel](https://raw.github.com/danbev/aerogear-simplepush-server/master/server/src/etc/images/register-channel.png)  
Notice that the ```UAID``` is absent from this message. This is because we have already performed hello message handshake and the current 
web socket connection is for the current UserAgent (identified by the UAID).

#### Request format

    {
      "messageType": "register",
      "channelID": "d9b74644-4f97-46aa-b8fa-9393985cd6cd"
    }  
    
#### Response format

    {
      "messageType": "register",
      "channelID": "d9b74644-4f97-46aa-b8fa-9393985cd6cd",
      "status": 200,
      "pushEndpoint": "/endpoint/d9b74644-4f97-46aa-b8fa-9393985cd6cd"
    }  
    
__status__  

* 200 "OK"   
* 409 "Conflict"
The chosen channelId is already in use and not associated with this user agent. UserAgent should retry with a different
channelId.
* 500 "Internal Server error"
 
### Notification
A notification is triggered by sending a ```PUT``` request to the ```pushEndpoint```.

![Notification](https://raw.github.com/danbev/aerogear-simplepush-server/master/server/src/etc/images/notification.png)  

#### Request PUT format

    PUT http://server/simplepush-server//endpoint/d9b74644-4f97-46aa-b8fa-9393985cd6cd
    ContentType: application/x-www-form-urlencoded
    
    version=N
    
#### Response (PUT) format

    HTTP/1.1 200 OK
    
#### Notification Request format
    
    {
      "messageType": "notification",
      "updates": [{"channelID": "d9b74644-4f97-46aa-b8fa-9393985cd6cd", "version" 2}, {"channeID": "a9b74644-4f97-46aa-b8fa-9393985dd688", "version": 10}]"
    }  
    
#### Notification Response format

    {
      "messageType": "ack",
      "updates": [{"channelID": "d9b74644-4f97-46aa-b8fa-9393985cd6cd", "version" 2}]"
    }  
The ```updates``` are the channels that the UserAgent acknowledges that it has processed.   
The SimplePush server will try to will resend the the un-acknowledged notifications every 60 seconds. 

### Unregister

![Unregister](https://raw.github.com/danbev/aerogear-simplepush-server/master/server/src/etc/images/unregister-channel.png)  

#### Request format

    {
      "messageType": "unregister",
      "channelID": "d9b74644-4f97-46aa-b8fa-9393985cd6cd"
    }  
    
#### Response format

    {
      "messageType": "unregister",
      "channelID": "d9b74644-4f97-46aa-b8fa-9393985cd6cd"
      "status": 200
    }  
