Collage Rest Examples
=====================
This project provides basic examples of interacting with the Collage Rest API

Pre-requisites
--------------
1. Java 1.7 installed
2. Groundwork 7.0 or higher installed and running in JBoss 7

Configuration
-------------
Make sure that you have a valid ws_client.properties installed under /usr/local/groundwork/config/ws_client.properties
See the example included in this directory

The assertions in the code may not work, so they are optional as they are dependent on some specific seed data that
comes with the internal test environment

Running From Maven
------------------
```sh
mvn exec:java
```

Running with Fat Jar (Non-Maven developers) - fat jar is available from our Nexus
--------------------
```sh
cd target
java -jar collagerest-examples-7.1.0.jar
```

Building the Fat Jar (Non-Maven developers)
--------------------
```sh
mvn clean install -P fatjar
```

Build Dependencies - if you want to provide all dependencies to non-Maven developers
-------------------
```sh
mvn dependency:copy-dependencies
```

