package org.groundwork.rs.client;

import org.groundwork.rs.client.clientdatamodel.IndependentGeneralProperties;
import org.groundwork.rs.dto.*;
import org.junit.After;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.*;

import static org.groundwork.rs.client.clientdatamodel.IndependentGeneralProperties._hostsToGenerate;
import static org.junit.Assert.*;

/**
 * This class contains all Methods that will run testing for hosts.  The only components used outside of this classfile
 * are properties that reside in IndependentGeneralProperties.java inside of the clientdatamodel folder.
 */

public class independentHostTest extends IndependentClientTestBase {
    /**
     * This method will run, in order all current testing on this page
     * @throws Exception
     */
    @Test
    //@Category(org.groundwork.rs.client.IndependentClientTestBase.class)
    public void RunHostTesting() throws Exception{
        System.out.println("****************** Hosts Testing Begin ***********************");
        System.out.println("-----------------------------------------------------------------");

        //Prelim step to clean database of any dups or existing hosts or agents that currently exist.
        cleanupAnyExistingHostFromFailedRun();

        //TESTING STEPS:

        //Step 1: Generate Apptype for Hosts
        GenerateAppType();
        //Step 2: Generate Hosts
        CreateHosts();
        //Step 3: Validate Hosts Exists and the Values are correct
        ValidateAndCompareCreatedHostsExists();
        //Step 4: Rename and validate hosts, and rename them back to original
        renameHosts();
        //Step 5: Validate Hosts Exists and the Values are correct after rename
        ValidateAndCompareCreatedHostsExists();
        //Step 6: Update Existing Hosts
        UpdateHosts();
        //Step 7: Validate Hosts still exists, and changed values are correct.
        ValidateAndCompareUpdatedHostsExists();
        //Step 8: Delete Hosts.
        DeleteHosts();
        //Step 9: Delete Apptype.
        DeleteAppType("UNITTESTAPP");


        System.out.println("-----------------------------------------------------------------");
        System.out.println("****************** Hosts Testing Complete ***********************");
    }

    public void CreateHosts() throws Exception
    {
        System.out.println("**** Begin Unit Test Create Hosts: " + baseUrl + "... ");
        System.out.println("-----------------------------------------------------------------");
        DtoHostList hostCreate = AutoGenerateHost();
        DtoOperationResults results = executePost(hostCreate);
        assertEquals(_hostsToGenerate + 0, results.getCount().intValue());

        for (DtoOperationResult result : results.getResults()) {
            assert(result.getStatus().equals(DtoOperationResult.SUCCESS));
        }

        System.out.println("-----------------------------------------------------------------");
        System.out.println("**** Finished Creating Hosts URL: " + baseUrl + "... ");
    }

    public void ValidateAndCompareCreatedHostsExists() throws Exception {

        System.out.println("**** Begin Unit Test Validate Created Hosts: " + baseUrl + "... ");
        System.out.println("-----------------------------------------------------------------");
        String AgentID = "";
        for(int x=0; x < _hostsToGenerate; x++ ){
            System.out.println("**** Retrieve Host by Host Name: " + "Test-Server-Dev-" + x);
            DtoHost host = retrieveSingleHost("Test-Server-Dev-" + x, true);
            System.out.println("**** Found Host by Host Name: " + "Test-Server-Dev-" + x);
            assertNotNull(host);
            compareCreatedHosts(host, "Test-Server-Dev-" + x, x);
            AgentID = "5437840f-a908-49fd-88bd-e04543a69e" + x;
            System.out.println("**** Retrieve Host by AgentID: " + AgentID);
            DtoHost host1 = retrieveHostByAgent(AgentID);
            System.out.println("**** Found Host by AgentID: " + AgentID);
            assertNotNull(host1);
        }

        System.out.println("-----------------------------------------------------------------");
        System.out.println("**** Finished Validating Created Hosts URL: " + baseUrl + "... ");
    }



    public void renameHosts() throws Exception
    {
        System.out.println("**** Begin Unit Test Rename Created Hosts: " + baseUrl + "... ");
        System.out.println("-----------------------------------------------------------------");
        for(int x=0; x < _hostsToGenerate; x++ ){
            HostClient client = new HostClient(IndependentGeneralProperties._baseUrl);
            DtoHost renamedHost = client.rename("Test-Server-Dev-" + x, "Test-Server-Dev-" + x + "_" + x,"Server" + x + 100, "000.000.000." + x + 100);
            assert renamedHost != null;
            assert renamedHost.getHostName().equals("Test-Server-Dev-" + x + "_" + x);
            System.out.println("Successfully Renamed Host From: " + "Test-Server-Dev-" + x + " To: " + "Test-Server-Dev-" + x + "_" + x);
        }

        for(int x=0; x < _hostsToGenerate; x++ ){
            HostClient client1 = new HostClient(IndependentGeneralProperties._baseUrl);

            DtoHost renamedHost1 = client1.rename("Test-Server-Dev-" + x + "_" + x, "Test-Server-Dev-" + x,"Server" + x, "000.000.000." + x);
            assert renamedHost1 != null;
            assert renamedHost1.getHostName().equals("Test-Server-Dev-" + x);
            System.out.println("Successfully Renamed Host Back To: " + "Test-Server-Dev-" + x + " From: " + "Test-Server-Dev-" + x + "_" + x);
        }

        System.out.println("-----------------------------------------------------------------");
        System.out.println("**** End Unit Test Rename Created Hosts: " + baseUrl + "... ");



    }

    public void ValidateAndCompareUpdatedHostsExists() throws Exception {

        System.out.println("**** Begin Unit Test Validate Updated Hosts: " + baseUrl + "... ");
        System.out.println("-----------------------------------------------------------------");

        for(int x=0; x < _hostsToGenerate; x++ ){
            DtoHost host = retrieveSingleHost("Test-Server-Dev-" + x, true);
            assertNotNull(host);
            compareUpdatedHosts(host, "Test-Server-Dev-" + x, x);
        }



        System.out.println("-----------------------------------------------------------------");
        System.out.println("**** Finished Validating Updated Hosts URL: " + baseUrl + "... ");
    }



    public void DeleteHosts() throws Exception {
        System.out.println("**** Begin Unit Test Delete Hosts: " + baseUrl + "... ");
        System.out.println("-----------------------------------------------------------------");

        for(int x=0; x < _hostsToGenerate; x++ ){
            String hostIds = "Test-Server-Dev-" + x;
            System.out.println("**** @Test Unit Test executeDelete URL: " + baseUrl + "... ");
            System.out.println("-----------------------------------------------------------------");
            System.out.println("**** Start Delete of Host: " + hostIds);
            HostClient client = new HostClient(IndependentGeneralProperties._baseUrl);
            List<String> ids = new ArrayList<String>();
            Collections.addAll(ids, hostIds.split(","));
            client.delete(ids);

            System.out.println("**** Complete deletion of Host: " + "Test-Server-Dev-" + x);
        }

        for(int x=0; x < _hostsToGenerate; x++ ){
            retrieveSingleHost("Test-Server-Dev-" + x, false);
        }

        HostClient client = new HostClient(IndependentGeneralProperties._baseUrl);
        DtoOperationResults deleteResults = client.delete(Arrays.asList(new String[]{"NotAHost"}));
        assertNotNull(deleteResults);
        assertEquals(new Integer(1), deleteResults.getWarning());

        System.out.println("**** Finish Unit Test Delete Hosts: " + baseUrl + "... ");
        System.out.println("-----------------------------------------------------------------");
    }

    public void DeleteAppType(String AppTypeId) throws Exception {
        System.out.println("**** Begin Unit Test Delete Application Type: " + baseUrl + "... ");
        System.out.println("-----------------------------------------------------------------");

        System.out.println("**** Start Delete of Application Type: " + AppTypeId);

        ApplicationTypeClient client = new ApplicationTypeClient(IndependentGeneralProperties._baseUrl);

        List<String> ids = new ArrayList<String>();
        Collections.addAll(ids, AppTypeId.split(","));
        client.delete(ids);


        DtoOperationResults deleteResults = client.delete(Arrays.asList(new String[]{"NotAHost"}));
        assertNotNull(deleteResults);
        assertEquals(new Integer(1), deleteResults.getWarning());

        System.out.println("**** Complete deletion of Application Type: " + AppTypeId);

    }

    public DtoHostList AutoGenerateHost() throws Exception
    {

        DtoHostList hosts = new DtoHostList();
        for(int x=0; x < _hostsToGenerate; x++ ){

            DtoHost host = new DtoHost();
            host.setHostName("Test-Server-Dev-" + x);
            host.setDescription("Server" + x);
            host.setAgentId("5437840f-a908-49fd-88bd-e04543a69e" + x);
            host.setMonitorStatus("UP");
            host.setAppType("UNITTESTAPP");
            host.setDeviceIdentification("000.000.000." + x);
            host.setMonitorServer("localhost");
            host.setDeviceDisplayName("Device" + x);
            host.putProperty("Latency", new Double(125.1 + x));
            host.putProperty("UpdatedBy", "UnitTester" + x);
            host.putProperty("Comments", "This is a test." + x);
            Calendar last = new GregorianCalendar(2013, Calendar.SEPTEMBER, 1, 0, 0);
            host.putProperty("LastStateChange", last);
            hosts.add(host);

            System.out.println("**** Generate Host: " + "Test-Server-Dev-" + x);


        }

        return hosts;
    }

    public void GenerateAppType() throws Exception
    {
        System.out.println("**** Begin Unit Test Create App Type: " + baseUrl + "... ");
        System.out.println("-----------------------------------------------------------------");
        DtoApplicationType apptype = new DtoApplicationType();
        DtoEntityProperty prop = new DtoEntityProperty("isAcknowledged","HOST_STATUS",70);
        DtoEntityProperty prop1 = new DtoEntityProperty("LastPluginOutput","HOST_STATUS",71);
        DtoEntityProperty prop2 = new DtoEntityProperty("LastPluginOutput","SERVICE_STATUS",72);
        DtoEntityProperty prop3 = new DtoEntityProperty("PerformanceData","SERVICE_STATUS",73);
        apptype.setDescription("Testing application system.");
        apptype.setDisplayName("UNIT TEST APP");
        apptype.setId(000);
        apptype.setName("UNITTESTAPP");
        apptype.setStateTransitionCriteria("Device;Host;ServiceDescription");
        apptype.addEntityProperty(prop);
        apptype.addEntityProperty(prop1);
        apptype.addEntityProperty(prop2);
        apptype.addEntityProperty(prop3);

        DtoApplicationTypeList apptypelist = new DtoApplicationTypeList();
        apptypelist.add(apptype);

        ApplicationTypeClient appclient = new ApplicationTypeClient(IndependentGeneralProperties._baseUrl);

        appclient.post(apptypelist);

        System.out.println("**** End Unit Test Create App Type: " + baseUrl + "... ");
        System.out.println("-----------------------------------------------------------------");


    }



    protected DtoHost retrieveHostByAgent(String agentId) throws Exception {
        HostClient client = new HostClient(IndependentGeneralProperties._baseUrl);
        List<DtoHost> hosts = client.query("agentId = '" + agentId + "'");
        if (hosts.size() > 0)
            return hosts.get(0);
        return null;
    }

    protected DtoHost retrieveSingleHost(String hostName, boolean expectToBeFound) throws Exception {
        DtoHost host = this.lookupHost(hostName);
        if (expectToBeFound)
            assertNotNull(host);
        else
            assertNull(host);
        return host;
    }

    protected DtoOperationResults executePost(DtoHostList hostUpdates) throws Exception {
        HostClient client = new HostClient(IndependentGeneralProperties._baseUrl);
        return client.post(hostUpdates);
    }


    protected void UpdateHosts() throws Exception{
        System.out.println("**** Begin Unit Test Update Hosts: " + baseUrl + "... ");
        System.out.println("-----------------------------------------------------------------");
        DtoHostList hostUpdates = AutoUpdateHost();
        DtoOperationResults results = executePost(hostUpdates);
        assertEquals(_hostsToGenerate + 0, results.getCount().intValue());

        for (DtoOperationResult result : results.getResults()) {
            assert(result.getStatus().equals(DtoOperationResult.SUCCESS));
        }

        System.out.println("-----------------------------------------------------------------");
        System.out.println("**** Finished Updating Hosts URL: " + baseUrl + "... ");
    }

    public DtoHostList AutoUpdateHost() throws Exception
    {
        DtoHostList hosts = new DtoHostList();
        for(int x=0; x < _hostsToGenerate; x++ ){

            DtoHost host = new DtoHost();
            host.setHostName("Test-Server-Dev-" + x);
            host.setDescription("Server" + x + 20);
            host.setMonitorStatus("DOWN");
            host.setAppType("UNITTESTAPP");
            host.setDeviceIdentification("000.000.000." + x);
            host.setMonitorServer("localhost");
            host.setDeviceDisplayName("Device" + x + 20);
            host.putProperty("Latency", new Double(125.1 + x  + 20));
            host.putProperty("UpdatedBy", "UnitTester" + x + 20);
            host.putProperty("Comments", "This is a test." + x + 20);
            Calendar last = new GregorianCalendar(2013, Calendar.SEPTEMBER, 1, 0, 0);
            host.putProperty("LastStateChange", last);
            hosts.add(host);
            System.out.println("**** Update Host: " + "Test-Server-Dev-" + x);

        }

        return hosts;
    }

    protected void compareCreatedHosts(DtoHost host, String hostname, int x) {

        assertNotNull(host.getHostName());

        if (host.getHostName().equals(hostname)) {
            assertEquals("UP", host.getMonitorStatus());
            assertEquals("Server" + x, host.getDescription());
            assertEquals("5437840f-a908-49fd-88bd-e04543a69e" + x, host.getAgentId());
            assertEquals("UNITTESTAPP", host.getAppType());
            assertEquals("000.000.000." + x, host.getDeviceIdentification());
            assertEquals(new Double(125.1 + x), (Double) host.getPropertyDouble("Latency"));
            assertEquals( "UnitTester" + x, host.getProperty("UpdatedBy"));
            assertEquals("This is a test." + x, host.getProperty("Comments"));
            Calendar last = new GregorianCalendar(2013, Calendar.SEPTEMBER, 1, 0, 0);
            Date actual = host.getPropertyDate("LastStateChange");
            assertEquals(last.getTime(), actual);
            System.out.println("**** Successfully Validated Host: " + hostname);
        }
        else {
            fail("host name " + host.getHostName() + " not valid");
        }
    }

    protected void compareUpdatedHosts(DtoHost host, String hostname, int x) {

        assertNotNull(host.getHostName());

        if (host.getHostName().equals(hostname)) {

            assertEquals("UNSCHEDULED DOWN", host.getMonitorStatus());
            assertEquals("Server" + x + 20, host.getDescription());
            assertEquals("5437840f-a908-49fd-88bd-e04543a69e" + x, host.getAgentId());
            assertEquals("UNITTESTAPP", host.getAppType());
            assertEquals("000.000.000." + x, host.getDeviceIdentification());
            assertEquals(new Double(125.1 + x + 20), (Double) host.getPropertyDouble("Latency"));
            assertEquals( "UnitTester" + x + 20, host.getProperty("UpdatedBy"));
            assertEquals("This is a test." + x + 20, host.getProperty("Comments"));
            Calendar last = new GregorianCalendar(2013, Calendar.SEPTEMBER, 1, 0, 0);
            Date actual = host.getPropertyDate("LastStateChange");
            assertEquals(last.getTime(), actual);
            System.out.println("**** Successfully Validated Updated Host: " + hostname);
        }
        else {
            fail("host name " + host.getHostName() + " not valid");
        }
    }

    protected DtoHost lookupHost(String hostName) throws Exception {
        HostClient client = new HostClient(baseUrl);
        return client.lookup(hostName);
    }
}

