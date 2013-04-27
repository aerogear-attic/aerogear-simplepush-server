# AeroGear SimplePush Server
__Disclaimer: This is only a proof of concept.__
This project is a Java implementation of the server side that follows the [SimplePush Protocol](https://wiki.mozilla.org/WebAPI/SimplePush/Protocol)

## Usage

### Build the SimplePush Server

    mvn install

### Start the SimplePush Server

    mvn exec:java -Dexec.args="7777"
    
### Access the demo html page
Serve ```src/main/resources/netty/socket.html``` from a local webserver. One way to do this is to create a symbolic link
to ```src/main/resources/netty```, for example:

    cd /Library/WebServer/Documents/
    sudo ln -s /path/to/push/aerogear-simplepush-server/src/main/resources/netty/ netty
    
Now you should be able to point your browser to ```http://localhost/netty/websocket.html```
The path to your documents directory and the port that the web server is listening to might differ. For httpd the look
in /etc/apache2/httpd.conf for this information.

### Register a channel
You will automatically be registered to receive push notifications for mail and foo. The endpoint channelID's will be displayed in the results textarea.

### Send a notification

    curl -i --header "Accept: application/x-www-form-urlencoded" -X PUT -d "version=1" "http://localhost:7777/endpoint/testChannel"

A push notification stating the version will be displayed in the textarea of the _websocket.html_ page that has registerd for that channel.

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

__uaid__  
The UserAgent Identifier is optional and if not specified a UAID will be created on by the SimplePush Server. This can 
be used as a way of reseting.

__channelIDs__  
The channelIds passed in are identifiers created on the client side and will be associated with the ```UAID```. In the case
of a Hello Message the _channelIDs_ represent channels that the UserAgent want to have registered.

The SimplePush Server will ignore any additional Hello Messages after the first one on the web socket connection. 
