# Aerogear Simple Push Server WildFly module
This project is a module intended to be used with the [Netty Subsystem](https://github.com/danbev/netty-subsystem)

## Prerequisites 
This project depends on [aergoear-simple-push-server](https://github.com/aerogear/aerogear-simple-push-server) which needs 
to be installed manually as it is currently not available in any public maven repository.  

It also requires that [Netty Subsystem](https://github.com/danbev/netty-subsystem) be installed on the local system, as this
dependency is currently not available in a maven repository.

## Building
From the root folder of this project run the following command:

    mvn install

## Installing
Copy the module produced by ```mvn package``` to the _modules_ directory of the application server.

    cp -r wildfly-module/target/module/org $WILDFLY_HOME/modules
    
Make sure you have installed the [Netty Subsystem](https://github.com/danbev/netty-subsystem), and then add this module as 
a dependency to the Netty subsystem module (_modules/org/jboss/aerogear/netty/main/module.xml_):

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
This involves adding a _server_ element to the Netty subsystem.  
Open up your WildFly server's configuration xml file, for example standalone.xml, and add the following to the _netty_ subsystem:

    <subsystem xmlns="urn:org.jboss.aerogear.netty:1.0">
        <server name="simplepush-server" socket-binding="simplepush" factory-class="org.jboss.aerogear.simplepush.netty.SimplePushBootstrapFactory"/>
    </subsystem>
    
For details regarding the attirbutes of the _server_ element, please refer to [Netty Subsystem](https://github.com/danbev/netty-subsystem) .

### Add a socket-binding    
You need to add a _socket-binding_ for the server configured in the previous step. This is done by adding a _socket-binding_ element
to the _socket-binding-group_ element in _standalone.xml_:

    <socket-binding-group name="standard-sockets" default-interface="public" port-offset="${jboss.socket.binding.port-offset:0}">
        ...
        <socket-binding name="simplepush" port="7777"/>
    </socket-binding-group>  

## Adding a Mysql datasource
AeroGear Simple Push server uses MySql datasource for persistence when deployed in WildFly and the database needs
to be configured as well as the application server.

### Create a database and database user

    $ mysql -u <user-name>
    mysql> create database simplepush;
    mysql> create user 'simplepush'@'localhost' identified by 'simplepush';
    mysql> GRANT SELECT,INSERT,UPDATE,ALTER,DELETE,CREATE,DROP ON simplepush.* TO 'simplepush'@'localhost';
    
    
### Add a datasource for the SimplePush database

    <datasources>
        <datasource jndi-name="java:jboss/datasources/SimplePushDS" pool-name="SimplePushDS" enabled="true" use-java-context="true" use-ccm="true">
            <connection-url>jdbc:mysql://localhost:3306/simplepush</connection-url>
            <driver>mysql</driver>
            <pool>
                <flush-strategy>IdleConnections</flush-strategy>
            </pool>
            <security>
                <user-name>simplepush</user-name>
                <password>simplepush</password>
            </security>
            <validation>
                <check-valid-connection-sql>SELECT 1</check-valid-connection-sql>
                <background-validation>true</background-validation>
            </validation>
        </datasource>
        <drivers>
            ...
            <driver name="mysql" module="com.mysql.jdbc">
                <xa-datasource-class>com.mysql.jdbc.jdbc2.optional.MysqlXADataSource</xa-datasource-class>
            </driver>
        </drivers>
    </datasources>
    
The module for MySql can be found in ```src/main/resources/modules```. Copy this module to WildFlys modules directory:

    cp -r src/main/resources/modules/com $WILDFLY_HOME/modules/
    
We also need the mysql driver copied to this module:

    mvn dependency:copy -Dartifact=mysql:mysql-connector-java:5.1.18 -DoutputDirectory=/$WILDFLY_HOME/modules/com/mysql/jdbc/main/
    
## Start WildFly

    ./standalone.sh

If you inspect the server console output you'll see the following message:

    08:56:13,052 INFO  [org.jboss.aerogear.netty.extension.NettyService] (MSC service thread 1-3) NettyService [simplepush-server] binding to port [7777]    

    
    



    