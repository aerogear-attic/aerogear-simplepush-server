# Aerogear Simple Push Server WildFly module

## Building
From the root folder of this project run the following command:

    mvn package

## Installing
Copy the module produced by ```mvn package``` to the _modules_ directory of the application server.

    cp -r wildfly-module/target/module/org $WILDFLY_HOME/modules/
    
## Configuring WildFly

### Enabling Transport Layer Security (TLS/SSL)
If you want to enable TLS/SSL support for the server then you must provide a keystore for the server to use. This keystore
must be accessible on the server classpath and a system variable named ```simplepush.keystore.path``` and ```simplepush.keystore.password```
must be provided so that the server can find them. On WildFly you can add these properties using any of the administration
interfaces. An example can be found in ```src/main/resources/wildfly-config.cli```.

### Adding the Mysql module
AeroGear Simple Push server uses MySql datasource for persistence when deployed in WildFly and the database needs
to be configured as well as the application server.

#### Create a database and database user

    $ mysql -u <user-name>
    mysql> create database simplepush;
    mysql> create user 'simplepush'@'localhost' identified by 'simplepush';
    mysql> GRANT SELECT,INSERT,UPDATE,ALTER,DELETE,CREATE,DROP ON simplepush.* TO 'simplepush'@'localhost';
    
    
#### Add a datasource for the SimplePush database
The module for mysql can be found in ```src/main/resources/modules/com/mysql```. Copy this module to WildFlys modules directory:

    cp -r src/main/resources/modules/com $WILDFLY_HOME/modules/
    
We also need the mysql driver copied to this module:

    mvn dependency:copy -Dartifact=mysql:mysql-connector-java:5.1.18 -DoutputDirectory=/$WILDFLY_HOME/modules/com/mysql/jdbc/main/
    
Next, start your server :

    ./standalone.sh

And run the follwing WildFly commane line interface script:

    $WILDFLY_HOME/bin/jboss-cli.sh --file=src/main/resources/wildfly-config.cli
    
The above script will add the mysql driver, a datasource, the Netty extension/subsystem, and lastly add a server entry
for the SimplePush server.
 
If you inspect the server console output you'll see the following message:

    08:56:13,052 INFO  [org.jboss.aerogear.simplepush.subsystem.NettyService] (MSC service thread 1-3) NettyService [simplepush] binding to port [7777]    


## Configuration options
The wildfly-config.cli script will add the configuration elements to the running server. But you might want to configure
things differently then what is provided by default. This section goes through the configuration options available.  

__Note__ This section will change quite dramatically as it was simple moved from a generic Netty Subsystem. For the 1.0.0
this configuration will be more specific to SimplePush and some configuration options will be removed and others added
to make it more flexible.

        <subsystem xmlns="urn:org.jboss.aerogear.simplepush:1.0">
            <server name="my-server" socket-binding="my-socket-binding"
                thread-factory="my-thread-factory" datasource-jndi-name="MyServerDS"
                token-key="c88da833ee33" endpointTls="false"/>
            ...
        </subsystem>
    </profile>    
    
One or more _server_ elements can be added enabling different types of servers to be run.  

__name__  
This is a simple name to identify the server in logs etc.

__socket-binding__  
The socket-binding to be used for this Netty server instance. 

__thread-factory__  
Thread factory that will be passed along to Netty when creating.

__datasource-jndi-name__  
An optional datasource JNDI name that this service depends on.
    
__token_key__  
This should be a random token which will be used by the server for encryption/decryption of the endpoint URLs that are
returned to clients upon successful channel registration.

__endpointTls__  
This optional determines whether the endpoint urls that are returned when a channel is registered should use
http or https. If this setting is true https will be used and if false http will be used.
    

    