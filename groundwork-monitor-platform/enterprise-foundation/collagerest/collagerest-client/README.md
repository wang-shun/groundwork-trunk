CollageRest Integration Testing
================================
The document describes how to work with a new set of integration tests starting with version 7.2.1.
The new integration tests can be found under the package: *org.groundwork.rs.it*
Tests found under the package *org.groundwork.rs.client* and *org.groundwork.rs.integration* are no longer used in 7.2.1.

Connection to GWOS Server
-------------------------
A GWOS Server (foundation-webapp) must be running in order to run Rest Client Integration tests.
This server can have already existing data. The integration tests will create test data from Java, and clean up after itself.   
    
Running Integration Testing
---------------------------
To run integration tests...

```sh
# assuming Groundwork 7.2.x source is installed under a directory monitor-platform
cd monitor-platform/enterprise-foundation/collagerest/collagerest-client

mvn integration-test -P integration
```
        
Additional Configuration 
-------------------------
The following Java System Properties can be configured:

GWOS_REST_API       http://localhost/api

GWOS_REST_USER      RESTAPIACCESS
GWOS_REST_PW        RESTAPIACCESSPASSWORD


Example:

```sh
cd monitor-platform/enterprise-foundation/collagerest/collagerest-client
mvn integration-test -P integration -DGWOS_REST_API=http://qa-server/api -DGWOS_REST_USER=RESTAPIACCESS2 -DGWOS_REST_PW=RESTAPIACCESSPASSWORD2   
```

Code Coverage Reports 
---------------------
To run the integration tests and generate code reports, run:

```sh
# assuming Groundwork 7.2.x source is installed under a directory monitor-platform
cd monitor-platform/enterprise-foundation/collagerest/collagerest-client

mvn verify -P integration
    
```

The reports are written to the output directory: target/site/jacoco-it/index.html 
Note: code coverage is generated for Rest API clients, not resources

Running in Continuous Integration
---------------------------------
To run integration tests in continuous integration, don't use verify or integration-test goals since they won't fail the build on test failures

```sh
# assuming Groundwork 7.2.x source is installed under a directory monitor-platform
cd monitor-platform/enterprise-foundation/collagerest/collagerest-client

mvn install -P integration
```

Running a Single Integration Test 
---------------------------------

````
mvn -Dit.test=HostGroupIT -P integration verify
````
 
