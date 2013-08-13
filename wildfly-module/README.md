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

    
    



    