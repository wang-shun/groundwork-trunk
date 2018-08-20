CollageRest Integration Testing
================================
** This document is DEPRECATED as of version 7.2.1. **
This document only applies to the OLD integration test procedure, with tests found in org.groundwork.rs.client
The new integration tests starting with 7.2.1 can be found under org.groundwork.rs.it


** Please see the new README.md for 7.2.1 procedure **

This document should be read by QA teams to learn how to setup integration testing in automated test suites

Developer Setup
----------------
This step can be skipped by QA. Its a one time developer setup to generate the json files found in the data directory.
Steps required to setup the integration tests first time. Only should need to run this once to generate the json files here.

1. Restore the database containing test data (Make sure there are no connections to Postgresql including JBoss)

    ```sh
    cd src/test/data
    ./restore.sh
    ```
2. Create the test data (JBoss must be running with foundation-webapp deployed)

    ```sh
    cd ${collagrest-client-home}
    mvn install -P integration-generate-data    
    ```

Connection to GWOS Server
-------------------------
A GWOS Server must be running in order to run Rest Client Integration tests.
   
    
Full Integration Testing
------------------------
To run integration tests, make sure you have a clean 7.1.0 database.

         ```sh
        cd ${collagrest-client-home}
        mvn install -P integration-test -DskipTests=false    
        ```
        
The integration-test profile will 
 
1. Populate the test data in the database
2. run the Integration tests
3. tear down the test data in the database

Additional Configuration 
-------------------------
The following Java System Properties can be configured:

GWOS_REST_API   http://localhost/api

GWOS_REST_USER      RESTAPIACCESS
GWOS_REST_PW        RESTAPIACCESSPASSWORD

Only for Authentication Test 7.0.2

GWOS_REST_USER      wsuser
GWOS_REST_PW        wsuser


Example:
         ```sh
        cd ${collagrest-client-home}
        mvn install -P integration-test -DskipTests=false -DGWOS_REST_API=http://qa-server/api    
        ```


Tear Down Integration DB
-------------------------
Developers can populate the database to 'initial state'.
Supported profiles are 'production' and 'test'. Default is 'test'

         ```sh
        cd ./monitor-platform/enterprise-foundation/collage/database
        # mvn install -P test -DskipTests=false
        mvn install -P production -DskipTests=false
        ```

Test Automation tear down only, without setup or running tests

         ```sh
         cd ${collagrest-client-home}
         mvn install -P integration-teardown
         ```

Setting up Data without testing or tearing down
------------------------------------------------
        
         ```sh
        cd ${collagrest-client-home}
        mvn install -P integration-setup    
        ```
    

       
        
        




