# AeroGear SimplePush Server
This project is a Java implementation of the server side that follows the [SimplePush Protocol](https://wiki.mozilla.org/WebAPI/SimplePush/Protocol).  

## Usage

### Build the SimplePush Server

    mvn install

### Start the SimplePush Server

    mvn exec:java
    
This will start the server listening localhost using port 7777. This will use a default configuration which can be found
in ```src/main/resources/simplepush-config.json```

    mvn exec:java

To start the server with a Redis datastore:

    mvn exec:java -Predis
    
To start the server with a Couchdb datastore:

    mvn exec:java -Pcouchdb

The configuration file can either be a path to a file on the file system or to a file on the classpath.

The default sample configuration file can be found in ```src/main/resources``` directory, which also contains sample 
configurations. 

### Configuration
Configuration is done using JSON configuration file.
Example configuration:  

    {
        "host": "localhost",
        "port": 7777,
        "password" :"testing",
        "useragent-reaper-timeout": "604800000",
        "endpoint-host": "external.name",
        "endpoint-port": 8899,
        "endpoint-tls": false,
        "endpoint-prefix": "/update",
        "ack-interval": "60000",
        "notifier-max-threads": "8",
        "sockjs-prefix": "/simplepush",
        "sockjs-cookies-needed": "true",
        "sockjs-url": "http://cdn.jsdelivr.net/sockjs/0.3.4/sockjs.min.js",
        "sockjs-session-timeout": "5000",
        "sockjs-heartbeat-interval": 25000,
        "sockjs-max-streaming-bytes-size": 65356,
        "sockjs-tls": false,
        "sockjs-keystore": "/simplepush-sample.keystore",
        "sockjs-keystore-password": "simplepush",
        "sockjs-websocket-enable": true,
        "sockjs-websocket-heartbeat-interval": -1,
        "sockjs-websocket-protocols": "push-notification",
        "datastore": { "in-memory": {} }
    }

#### host
The host that the server will bind to.

#### port
The port that the server will bind to.

#### password
This should be a password that will be used to generate the server private key which is used for  encryption/decryption
of the endpoint URLs that are returned to clients upon successful channel registration.

#### useragent-reaper-timeout
This is the amount of time which a UserAgent can be inactive after which it will be removed from the system.
Default is 604800000 ms (7 days).

#### endpoint-host
The allows the configuration of the host name that will be exposed for the endpoint that clients use to send notifications.
This enables an externally exposed host name/ip address to be specified which differs from the host that the server 
binds to.

#### endpoint-port
The allows the configuration of the port that will be exposed for the endpoint that clients use to send notifications.
This enables an externally exposed port to be specified which differs from the host that the server binds to.

#### endpoint-tls
Configures Transport Layer Security (TLS) for the notification endpointUrl that is returned when a UserAgent/client registers a channel. 
Setting this to _true_ will return a url with _https_ as the protocol.

#### endpoint-prefix  
The prefix for the the notification endpoint url. This prefix will be included in the endpointUrl returned to the client to enabling them to send notifications.

#### ack-interval  
This is the interval time for resending un-acknowledged notifications. Default is 60000 ms.

#### notifier-max-threads
This is the maxium number of threads that will be used for handling notifications.

#### sockjs-prefix
The prefix/name, of the SockJS service. For example, in the url _http://localhost/simplepush/111/12345/xhr_, _simplepush_ is the prefix. 

#### sockjs-cookies-needed
This is used by some load balancers to enable session stickyness. Default is true.

#### sockjs-url
The url to the sock-js-<version>.json. This is used by the 'iframe' protocol and the url is replaced in the script 
returned to the client. This allows for configuring the version of sockjs used.  
Default is _http://cdn.jsdelivr.net/sockjs/0.3.4/sockjs.min.js_.

#### sockjs-session-timeout
A timeout for inactive sessions. Default is 5000 ms. 

#### sockjs-heartbeat-interval
Specifies a heartbeat interval. Default is 25000 ms.

#### sockjs-max-streaming-bytes-size
The max number of bytes that a streaming transport protocol should allow to be returned before closing the connection, 
forcing the client to reconnect. 
This is done so that the responseText in the XHR Object will not grow and be come an issue for the client. Instead, 
by forcing a reconnect the client will create a new XHR object and this can be see as a form of garbage collection.
Default is 131072 bytes.

#### sockjs-tls
Specified whether Transport Layer Security (TLS) should be used by the SockJS layer.
Default is false.

#### sockjs-keystore
If _tls_ is in use then the value of this property should be a path to keystore available on the classpath of the subystem.

#### sockjs-keystore-password
If _tls_ is in use, then the value of this property should be the password to the keystore specified in _keystore_.

#### sockjs-websocket-enable
Determines whether WebSocket support is enabled on the server.

#### sockjs-websocket-heartbeat-interval
A heartbeat-interval for WebSockets. This interval is separate from the normal SockJS heartbeat-interval and might be 
required in certain environments where idle connection are closed by a proxy. It is a separate value from the hearbeat 
that the streaming protocols use as it is often desirable to have a much larger value for it.

#### sockjs-websocket-protocols
Adds the specified comma separated list of protocols which will be returned to during the HTTP upgrade request as the header 'WebSocket-Protocol'. 
This is only used with raw WebSockets as the SockJS protocol does not support protocols to be specified by the client yet.

#### datastore
Configures the datastore to be used.  

Redis datastore configuration:
    
   "datastore": { "redis": { "host": "localhost", "port": 6379 } } 
   
Using Redis datastore:

    mvn exec:java -Dexec.args="src/main/resources/simplepush-redis-config.json"
   
CouchDB datastore:

    "datastore": { "couchdb": { "url": "http://127.0.0.1:5984", "dbName": "simplepush" } }
    
Using CouchDB datastore:

    mvn exec:java -Dexec.args="src/main/resources/simplepush-couchdb-config.json"
    
JPA datastore:

    "datastore": { "jpa": { "persistenceUnit": "SimplePushTest" } }
    
Using JPA datastore:

    mvn exec:java -Dexec.args="src/main/resources/simplepush-jpa-config.json"
    
InMemory datastore:

    "datastore": { "in-memory": {} }
    
Using InMemory datastore:

    mvn exec:java -Dexec.args="src/main/resources/simplepush-inmem-config.json"

    
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

Another option for trying out the server using WebSocket is to use [wscat](http://einaros.github.io/ws/):

    $ npm install -g ws

Next, use ```wscat``` to connect to the SimplePush Server using the WebSocket protocol:  

    $ wscat -c ws://localhost:7777/simplepush/websocket
    connected (press CTRL+C to quit)

Send the _hello_ message:

    > {"messageType": "hello"}
        < {"messageType":"hello","uaid":"a1863f69-05a1-478b-abb8-d826828410bf"}

Register a channel:

    > > {"messageType": "register", "channelID":"testChannel"}
        < {"messageType":"register","channelID":"testChannel","status":200,"pushEndpoint":"http://localhost:7777/update/I23jYJSX6--3s1BGZ0_kdpE4ria5rY9c-ddssLx5vL-KFbGhgFW6rLM7CN2jaq9U9RKL58tS4LQagYseN0l9Nt6n3c-Cf6jDvb3OWX8EmCY."}

Now, you can copy the ```pushEndpoint``` url and use it to send notifications. See the following section for how to use
curl to send a push notification.

### Send a notification

Use one of the above mentioned IDs in the following ```curl``` command:

    curl -i --header "Accept: application/x-www-form-urlencoded" -X PUT -d "version=1" "{pushEndpoint}"

The ```pushEndpoint``` will be returned from the server when registering the channel. The above command specifies an 
explicit version, but the version can be left out in which case a timestamp will be used instead:

    curl -i --header "Accept: application/x-www-form-urlencoded" -X PUT "{pushEndpoint}"

A push notification stating the version will be displayed in the textarea of the _websocket.html_ page that has registerd for that channel.

    
   
