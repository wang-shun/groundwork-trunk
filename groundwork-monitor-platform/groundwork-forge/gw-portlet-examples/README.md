Collage Rest Examples
=====================
This project provides basic examples of writing Groundwork Portlets

Pre-requisites
--------------
1. Java 1.7 installed
2. Groundwork 7.0 or higher installed and running in JBoss 7

Configuration
-------------
Make sure you have successfully installed Groundwork on your development machine or somewhere on your network.
Locally, you will minimally need to have a valid ws_client.properties installed under /usr/local/groundwork/config/ws_client.properties


Building From Maven
------------------
```sh
mvn clean install
```

Deploying (requires previous step)
--------------------
```sh
cd gw-portlet-examples
cp target/gw-portlet-examples.war /usr/local/groundwork/jpp/standalone/deployments
touch /usr/local/groundwork/jpp/standalone/deployments/gw-portlet-examples.war.dodeploy
```



