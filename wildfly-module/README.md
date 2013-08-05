# Aerogear Simple Push Server WildFly module
This project is a module intended to be used with the [Netty Subsystem](https://github.com/danbev/netty-subsystem)

## Prerequisites 
This project requires that the [Netty Subsystem](https://github.com/danbev/netty-subsystem/tree/master/subsystem#installation) 
be installed on the local system, as this dependency is currently not available in a maven repository. The module produced
by that project needs to be copied to you WildFly installation, please see the instructions in the link above.   
__You do not need to configure anything at this stage, this will be taken care of in the section ```Configuring WildFly``` in this document.__

## Building
From the root folder of this project run the following command:

    mvn package

## Installing
Copy the module produced by ```mvn package``` to the _modules_ directory of the application server.

    cp -r wildfly-module/target/module/org $WILDFLY_HOME/modules/
    
Next, we need to add ```org.jboss.aerogear.simplepush``` as a dependency to the Netty module by editing 
 the Netty subsystem module ```$WILDFLY_HOME/modules/org/jboss/aerogear/netty/main/module.xml```:

    <dependencies>
        ...
        <module name="org.jboss.aerogear.simplepush" services="import">
            <imports>
                <include path="META-INF"/>
            </imports>
        </module>
    </dependencies>
    
Here we are including ```META-INF``` as an directory path imported from the ```simplepush``` module. This is to allow access
to ```META-INF/persistence.xml```.
    
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

    08:56:13,052 INFO  [org.jboss.aerogear.netty.extension.NettyService] (MSC service thread 1-3) NettyService [simplepush-server] binding to port [7777]    

    
    



    