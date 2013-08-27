# AeroGear SimplePush Server
This project is a Java implementation of the server side that follows the [SimplePush Protocol](https://wiki.mozilla.org/WebAPI/SimplePush/Protocol).  

## Usage

### Build the SimplePush Server

    mvn install

### Start the SimplePush Server

    mvn exec:java
    
This will start the server listening localhost using port 7777. To toggle these arguments you can
specify overrides on the command line:  

    mvn exec:java -Dexec.args="-host=localhost -port=7777 -tls=false -ack_interval=10000 -useragent_reaper_timeout=60000 -token_key=yourRandomToken"
    
__host__  
The host that the server will bind to.

__port__  
The port that the server will bind to.

__tls__  
Whether to use transport layer security or not.
The server will use a system property named ```simplepush.keystore.path``` which should point to 
a keystore available on the servers classpath. If the keystore is password protected then the system property 
```simplepush.keystore.password``` can be used to specify the password for the keystore.

When running the ```mvn exec:java``` command a sample keystore is used that contains a self signed certificate for testing. 
The above mentioned system variables are set in the pom.xml file.

__ack_interval__ 
How often the acknowledge job will run to re-send unacknowledged notifications.

__useragent_reaper_timeout__ 
How often the UserAgent reaper job will run to clean up inactive user agents.

__token_key__  
This should be a random token which will be used by the server for encryption/decryption of the endpoint URLs that are
returned to clients upon successful channel registration.
    
### Access the demo html page

#### Setting up TLS/SSL
This SimplePush Server uses SockJS with transport layer security and therefore requires a certificate to be accepted by 
the client. The server can be enabled with TLS by changing the _tls_ setting in pom.xml, but the browser also needs to 
import the certificate.  

For some broswers is will be enough to access ```https://localhost:7777``` once, and then accept the certificate.  For other
systems it might be required to import the certificate through the browser preferences/settings page. For this case we
have exported the certificate and it can be found in ```src/test/resources/simplepush.crt```.

#### Mac WebServer

Serve _src/test/resources/sockjs-client.html_ from a local webserver. One way to do this is to create a symbolic link
to _src/main/resources_, for example:

    cd /Library/WebServer/Documents/
    sudo ln -s /path/to/push/aerogear-simplepush-server/server-netty/src/test/resources/ netty
    
Now you should be able to point your browser to ```http://localhost/netty/sockjs-client.html```
The path to your documents directory and the port that the web server is listening to might differ. For httpd the look
in _/etc/apache2/httpd.conf_ for this information.

#### Python WebServer

In case you are not running a mac, there is a simple HTTP server, that comes with Python. Simple navigate to ```src/test/resources``` and execute:

    python -m SimpleHTTPServer 5555

Now you should be able to point your browser to ```http://localhost:5555/sockjs-client.html```

### Register a channel
You have to register yourself by executing two commands in the text areas in ```sockjs-client.html``` page. Try 
to execute the first one of message type _hello_. When executing the second one, do not forget to add the channel you 
want to get registered, as the _channelId_, enter e.g. _mail_.

### Send a notification

Use one of the above mentioned IDs in the following ```curl``` command:

    curl -i --header "Accept: application/x-www-form-urlencoded" -X PUT -d "version=1" "https://localhost:7777/update/{ChannelID}"

A push notification stating the version will be displayed in the textarea of the _websocket.html_ page that has registerd for that channel.

    
    
