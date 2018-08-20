package org.groundwork.rs.client;

import org.groundwork.rs.dto.*;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.ws.rs.core.MediaType;
import java.util.*;

import static org.junit.Assert.*;

/**
 * Created by rphillips on 3/29/16.
 */
public class IndependentServiceTest  extends IndependentClientTestBase   {

    @Test
    @Category(org.groundwork.rs.client.IndependentClientTestBase.class)
    public void runEventsTest() throws Exception
    {
        //Step 1: Generate AppType to support Event testing.
        System.out.println("**** Generate AppType to support Event Testing.");
        System.out.println("-----------------------------------------------------------------");
        //CreateAppType();
        System.out.println("-----------------------------------------------------------------");
        System.out.println("**** Finished Create AppType.");


        //Step 2: Generate Host to support Event testing.
        System.out.println("**** Generate Host to support Event Testing.");
        System.out.println("-----------------------------------------------------------------");
        //CreateHost();
        System.out.println("-----------------------------------------------------------------");
        System.out.println("**** Finished Create Host.");


        //Step 3: Create Events
        System.out.println("**** Begin Unit Test Create Events: " + baseUrl + "... ");
        System.out.println("-----------------------------------------------------------------");
        //CreateEvents();
        System.out.println("-----------------------------------------------------------------");
        System.out.println("**** Finished Create Events URL: " + baseUrl + "... ");

        //Step 4: Create Service
        System.out.println("**** Begin Create Service for Events Test");
        System.out.println("-----------------------------------------------------------------");
        //CreateService();
        System.out.println("-----------------------------------------------------------------");
        System.out.println("**** Finished Create Service for Events Test");

        //Step 5: Validate Events
        System.out.println("**** Begin Unit Test Validate Events: " + baseUrl + "... ");
        System.out.println("-----------------------------------------------------------------");
        //validateEvents();
        System.out.println("-----------------------------------------------------------------");
        System.out.println("**** Finished Validate Events URL: " + baseUrl + "... ");

        //Step 6: Delete Host
        System.out.println("**** Begin Delete Hosts");
        System.out.println("-----------------------------------------------------------------");
        //deleteHost();
        System.out.println("-----------------------------------------------------------------");
        System.out.println("**** Finished Delete Hosts");

        //Step 7: Delete Device
        System.out.println("**** Begin Delete Device for Events Test");
        System.out.println("-----------------------------------------------------------------");
        //eleteDevice();
        System.out.println("-----------------------------------------------------------------");
        System.out.println("**** Finished Delete Device for Events Test");

        //Step 8: Delete Events
        System.out.println("**** Begin Unit Test Delete Events: " + baseUrl + "... ");
        System.out.println("-----------------------------------------------------------------");
        //deleteEvents();
        System.out.println("-----------------------------------------------------------------");
        System.out.println("**** Finished Delete Events URL: " + baseUrl + "... ");

        //Step 9: Delete Service
        System.out.println("**** Begin Delete Service for Events Test");
        System.out.println("-----------------------------------------------------------------");
        //deleteService();
        System.out.println("-----------------------------------------------------------------");
        System.out.println("**** Finished Delete Service for Events Test");

    }



    public void testLookupService() throws Exception {

        ServiceClient client = new ServiceClient(baseUrl);
        DtoService service = client.lookup("local_load", "localhost");
        assertNotNull(service);
        assertEquals("local_load", service.getDescription());
        assertEquals("localhost", service.getHostName());

        // wait to ensure propagation and test autocomplete
        Thread.sleep(250);
        List<DtoName> suggestions = client.autocomplete("http_");
        assertNotNull(suggestions);
        assertEquals(1, suggestions.size());
        assertTrue(suggestions.containsAll(Arrays.asList(new DtoName[]{new DtoName("http_alive")})));
        suggestions = client.autocomplete("zzz");
        assertNotNull(suggestions);
        assertEquals(0, suggestions.size());
        suggestions = client.autocomplete("http_", 1);
        assertNotNull(suggestions);
        assertEquals(1, suggestions.size());
        assertTrue(suggestions.containsAll(Arrays.asList(new DtoName[]{new DtoName("http_alive")})));
    }


    public void testQueryServices() {

        ServiceClient client = new ServiceClient(baseUrl);
        List<DtoService> services = client.query("(property.ExecutionTime between 10 and 3500 and (monitorStatus <> 'UP')) order by property.ExecutionTime");
        assertTrue(services.size() >= 80);
        for (DtoService service : services) {
            int executionTime = service.getPropertyInteger("ExecutionTime");
            assertTrue(executionTime >= 10 && executionTime <= 3500);
            assertNotSame("UP", service.getMonitorStatus());
        }
        services = client.query("(hostName = 'localhost' and name like 'local_%') or (hostName = 'sql-2008' and name like 'snmp_%') order by hostName, name");
//        for (DtoService service : services) {
//            System.out.println("service = " + service.getDescription() + ", host = " + service.getHostName());
//        }
    }


    public void testQueryServicesWithEncoding() {

        ServiceClient client = new ServiceClient(baseUrl);
        DtoService service = client.lookup("host with+space", "service+with space");
        assertNull(service);
    }


    public void testKevinsUseCase() throws Exception {
        // lastchecktime IS NOT NULL and ApplictionType="VEMA"

        ServiceClient client = new ServiceClient(baseUrl);
        List<DtoService> services = client.query("lastCheckTime is not null and appType = 'VEMA' order by lastCheckTime", 1, 1);
        for (DtoService service : services) {
            assertNotNull(service.getAppType());
            //System.out.format("id = %d event = %s\n", service.getId(), service.getAppType());
        }
        //System.out.println("not null count = " + services.size());
        services = client.query("lastCheckTime is null and appType = 'VEMA' order by lastCheckTime");
        for (DtoService service : services) {
            assertNotNull(service.getAppType());
            //System.out.format("id = %d event = %s\n", service.getId(), service.getAppType());
        }
        //System.out.println("null count = " + services.size());
    }


    public void testQueryById() {

        ServiceClient client = new ServiceClient(baseUrl);
        List<DtoService> services = client.query("id=2");
        assertEquals(1, services.size());
        for (DtoService service : services) {
            assertEquals(service.getId().intValue(), 2);
        }
    }


    public void testPostAndDeleteServices() {

        ServiceClient client = new ServiceClient(baseUrl);
        DtoOperationResults results = client.post(buildServiceUpdate());
        assertEquals(2, results.getCount().intValue());
        String hostName = null;
        for (DtoOperationResult result : results.getResults()) {
            assert(result.getStatus().equals(DtoOperationResult.SUCCESS));
        }

        DtoService service = client.lookup("service-100", "localhost");
        assertNotNull(service);
        assertServiceWritten(service);

        service = client.lookup("service-101", "localhost");
        assertNotNull(service);
        assertServiceWritten(service);

        // test update
        DtoServiceList services = new DtoServiceList();
        service = new DtoService();
        service.setDescription("service-100");
        service.setHostName("localhost");
        service.setMonitorStatus("UNSCHEDULED CRITICAL");
        service.setAgentId("007");
        services.add(service);
        client.post(services);
        DtoService service2 = client.lookup("service-100", "localhost");
        assertNotNull(service2);
        assert service2.getAppType().equals("VEMA");
        assert service2.getMonitorStatus().equals("UNSCHEDULED CRITICAL");
        assert service2.getAgentId().equals("007");

        List<String> serviceNames = new LinkedList<String>();
        serviceNames.add("service-100");
        serviceNames.add("service-101");
        serviceNames.add("service-bad");
        DtoOperationResults result = client.delete(serviceNames, "localhost");

        service = client.lookup("service-100", "localhost");
        assertNull(service);
        service = client.lookup("service-101", "localhost");
        assertNull(service);

        // test warning for missing delete
        DtoOperationResults deleteResults = client.delete("NotAService", "localhost");
        assert deleteResults != null;
        assert deleteResults.getWarning() == 1;
    }

    protected void assertServiceWritten(DtoService service) {

        if (service.getDescription().equals("service-100")) {
            assertEquals("UP", service.getMonitorStatus());
            assertEquals("localhost", service.getHostName());
            assertEquals("5437840f-a908-49fd-88bd-e04543a69e0", service.getAgentId());
            assertEquals("UP", service.getLastHardState());
            assertEquals("VEMA", service.getAppType());
            assertEquals("ACTIVE", service.getCheckType());
            assertEquals("HARD", service.getStateType());
            assertEquals(new Double(175.4), (Double) service.getPropertyDouble("Latency"));
            assertEquals(new Double(200.5), (Double) service.getPropertyDouble("ExecutionTime"));
            assertEquals("1.output", service.getProperty("LastPluginOutput"));
        }
        else if (service.getDescription().equals("service-101"))  {
            assertEquals("UP", service.getMonitorStatus());
            assertEquals("localhost", service.getHostName());
            assertEquals("5437840f-a908-49fd-88bd-e04543a69e0", service.getAgentId());
            assertEquals("UP", service.getLastHardState());
            assertEquals("VEMA", service.getAppType());
            assertEquals("ACTIVE", service.getCheckType());
            assertEquals("HARD", service.getStateType());
            assertEquals(new Double(275.4), (Double) service.getPropertyDouble("Latency"));
            assertEquals(new Double(300.5), (Double) service.getPropertyDouble("ExecutionTime"));
            assertEquals("2.output", service.getProperty("LastPluginOutput"));
        }
        else {
            fail("service name " + service.getDescription() + " not valid");
        }
    }

    protected DtoServiceList buildServiceUpdate() {
        DtoServiceList services = new DtoServiceList();
        DtoService service = new DtoService();
        service.setDescription("service-100");
        service.setHostName("localhost");
        service.setMonitorServer("localhost");
        service.setDeviceIdentification("127.0.0.1");
        service.setAgentId("5437840f-a908-49fd-88bd-e04543a69e0");
        service.setMonitorStatus("UP");
        service.setLastHardState("UP");
        service.setAppType("VEMA");
        service.setCheckType("ACTIVE");
        service.setStateType("HARD");
        Calendar last = new GregorianCalendar(2013, Calendar.MAY, 20, 0, 0);
        Calendar next = new GregorianCalendar(2013, Calendar.MAY, 27, 0, 0);
        Calendar stateChange = new GregorianCalendar(2013, Calendar.MAY, 25, 0, 0);
        service.setLastStateChange(stateChange.getTime());
        service.setNextCheckTime(next.getTime());
        service.setLastCheckTime(last.getTime());
        service.putProperty("Latency", new Double(175.4));
        service.putProperty("ExecutionTime", new Double(200.5));
        service.putProperty("LastPluginOutput", "1.output");
        services.add(service);

        service = new DtoService();
        service.setDescription("service-101");
        service.setHostName("localhost");
        service.setMonitorServer("localhost");
        service.setDeviceIdentification("127.0.0.1");
        service.setAgentId("5437840f-a908-49fd-88bd-e04543a69e0");
        service.setMonitorStatus("UP");
        service.setLastHardState("UP");
        service.setAppType("VEMA");
        service.setCheckType("ACTIVE");
        service.setStateType("HARD");
        last = new GregorianCalendar(2013, Calendar.MAY, 21, 0, 0);
        stateChange = new GregorianCalendar(2013, Calendar.MAY, 26, 0, 0);
        next = new GregorianCalendar(2013, Calendar.MAY, 28, 0, 0);
        service.setLastStateChange(stateChange.getTime());
        service.setNextCheckTime(next.getTime());
        service.setLastCheckTime(last.getTime());
        service.putProperty("Latency", new Double(275.4));
        service.putProperty("ExecutionTime", new Double(300.5));
        service.putProperty("LastPluginOutput", "2.output");
        services.add(service);
        return services;
    }


    public void testDeleteEncode() {

        ServiceClient client = new ServiceClient(baseUrl);
        DtoOperationResults results = client.delete("service 100+100 %", "host + 100 %");
        assert results.getWarning() == 1;
    }



    public void testQueryByHostNameParam() {

        ServiceClient client = new ServiceClient(baseUrl);
        List<DtoService> services = client.list("localhost");
        //assertEquals(94, services.size());
        for (DtoService service : services) {
            //System.out.println("service = " + service.getDescription() + ", host = " + service.getHostName());
            assertEquals("localhost", service.getHostName());
        }
    }


//    @Test
//    public void testWriteWithDevice() {
//        DtoServiceList serviceUpdates = new DtoServiceList();
//        ServiceClient client = new ServiceClient(getDeploymentURL());
//        DtoService serviceStatus = new DtoService();
//        serviceStatus.setDescription("service-2000");
//        serviceStatus.setHostName("TEST");
//        serviceStatus.setDeviceIdentification("127.0.0.1");
//        Calendar last = new GregorianCalendar(2013, Calendar.MAY, 20, 0, 0);
//        serviceStatus.setLastStateChange(last.getTime());
//        serviceStatus.setMonitorStatus("PENDING");
//        serviceStatus.setLastHardState("PENDING");
//        serviceStatus.setAppType("NAGIOS");
//        serviceUpdates.add(serviceStatus);
//        DtoOperationResults results = client.post(serviceUpdates);
//        for (DtoOperationResult result : results.getResults()) {
//            System.out.println("result " + result.getStatus());
//        }
//    }

    // @Test
    public void testMyQueries() {

        ServiceClient client = new ServiceClient(baseUrl);
        List<DtoService> services = client.query("appType = 'ODL' and (description like '%-receiveDrops' or description like '%-transmitErrors')");
        assert services.size() == 54;
        services = client.query("appType = 'DOCK' and description in ('syn.cpu.usage.system','syn.memory.usage','cpu.load')");
        assert services.size() == 8;
    }


    public void testBulkCreateServices() throws Exception {

        // create clients
        ServiceClient serviceClient = new ServiceClient(baseUrl);
        DeviceClient deviceClient = new DeviceClient(baseUrl);
        HostClient hostClient = new HostClient(baseUrl);

        // explicitly create host, (other is created dynamically)
        DtoHost dtoHost = new DtoHost();
        dtoHost.setHostName("bulk-host-0");
        dtoHost.setDeviceIdentification("bulk-device-0");
        dtoHost.setMonitorStatus("PENDING");
        dtoHost.setAppType("SEL");
        dtoHost.setAgentId("5437840f-a908-49fd-88bd-e04543a69e0");
        DtoOperationResults results = hostClient.post(new DtoHostList(Arrays.asList(new DtoHost[]{dtoHost})));
        assert results != null;
        assert results.getCount() == 1;
        assert results.getSuccessful() == 1;

        // create services
        int numServices = 1000;
        List<DtoService> dtoServices = new ArrayList<DtoService>();
        for (int service = 0; (service < numServices); service++) {
            DtoService dtoService = new DtoService();
            dtoService.setHostName("bulk-host-" + (service % 2));
            dtoService.setDeviceIdentification("bulk-device-" + (service % 2));
            dtoService.setDescription("bulk-service-" + service);
            dtoService.setMonitorStatus("PENDING");
            dtoService.setLastHardState("PENDING");
            dtoService.setLastPlugInOutput("testing bulk create " + service);
            dtoService.setAppType("SEL");
            dtoService.setAgentId("5437840f-a908-49fd-88bd-e04543a69e0");
            dtoServices.add(dtoService);
        }
        long start = System.currentTimeMillis();
        results = serviceClient.post(new DtoServiceList(dtoServices));
        long end = System.currentTimeMillis();
        assert results != null;
        assert results.getCount() == numServices;
        assert results.getSuccessful() == numServices;
        log.info(String.format("Elapsed time for testBulkCreateServices create: %d", (end-start)));

        // create services with duplicates
        dtoServices.clear();
        {
            DtoService dtoService = new DtoService();
            dtoService.setHostName("bulk-host-0");
            dtoService.setDeviceIdentification("bulk-device-0");
            dtoService.setDescription("bulk-service");
            dtoService.setMonitorStatus("PENDING");
            dtoService.setLastHardState("PENDING");
            dtoService.setLastPlugInOutput("testing bulk create");
            dtoService.setAppType("SEL");
            dtoService.setAgentId("5437840f-a908-49fd-88bd-e04543a69e0");
            dtoServices.add(dtoService);
            dtoService = new DtoService();
            dtoService.setHostName("bulk-host-0");
            dtoService.setDeviceIdentification("bulk-device-0");
            dtoService.setDescription("bulk-service");
            dtoService.setMonitorStatus("OK");
            dtoService.setLastHardState("OK");
            dtoService.setLastPlugInOutput("testing bulk create duplicate");
            dtoService.setAppType("SEL");
            dtoService.setAgentId("5437840f-a908-49fd-88bd-e04543a69e0");
            dtoServices.add(dtoService);
        }
        results = serviceClient.post(new DtoServiceList(dtoServices));
        assert results != null;
        assert results.getCount() == 2;
        assert results.getSuccessful() == 2;

        // cleanup
        deviceClient.delete("bulk-device-0");
        deviceClient.delete("bulk-device-1");

        // create async services
        numServices = 10;
        dtoServices.clear();
        for (int service = 0; (service < numServices); service++) {
            DtoService dtoService = new DtoService();
            dtoService.setHostName("bulk-async-host-" + (service % 2));
            dtoService.setDeviceIdentification("bulk-async-device-" + (service % 2));
            dtoService.setDescription("bulk-async-service-" + service);
            dtoService.setMonitorStatus("PENDING");
            dtoService.setLastHardState("PENDING");
            dtoService.setLastPlugInOutput("testing bulk async create " + service);
            dtoService.setAppType("SEL");
            dtoService.setAgentId("5437840f-a908-49fd-88bd-e04543a69e0");
            dtoServices.add(dtoService);
        }
        results = serviceClient.postAsync(new DtoServiceList(dtoServices));
        assert results != null;
        assert results.getCount() == 1;
        assert results.getSuccessful() == 1;

        // wait and verify
        boolean verified = false;
        for (int wait = 0; ((wait < 20) && !verified); wait++) {
            Thread.sleep(500);
            verified = (deviceClient.lookup("bulk-async-device-0") != null);
            verified = verified && (deviceClient.lookup("bulk-async-device-1") != null);
        }
        assert verified;

        // cleanup
        deviceClient.delete("bulk-async-device-0");
        deviceClient.delete("bulk-async-device-1");
    }


    public void testSetDynamicProperty() {
        // allocate clients
        PropertyTypeClient propertyTypeClient = new PropertyTypeClient(baseUrl);
        ServiceClient serviceClient = new ServiceClient(baseUrl);

        // test using XML
        propertyTypeClient.setMediaType(MediaType.APPLICATION_XML_TYPE);
        serviceClient.setMediaType(MediaType.APPLICATION_XML_TYPE);
        testSetDynamicProperty(propertyTypeClient, serviceClient);

        // test using JSON
        propertyTypeClient.setMediaType(MediaType.APPLICATION_JSON_TYPE);
        serviceClient.setMediaType(MediaType.APPLICATION_JSON_TYPE);
        testSetDynamicProperty(propertyTypeClient, serviceClient);
    }

    private void testSetDynamicProperty(PropertyTypeClient propertyTypeClient, ServiceClient serviceClient) {
        // define test property type
        DtoPropertyTypeList dtoPropertyTypeList = new DtoPropertyTypeList();
        DtoPropertyType dtoPropertyType = new DtoPropertyType();
        dtoPropertyType.setName("TEST_PROPERTY");
        dtoPropertyType.setDescription("testSetDynamicProperty");
        dtoPropertyType.setDataType(DtoPropertyDataType.STRING);
        dtoPropertyTypeList.add(dtoPropertyType);
        DtoOperationResults results = propertyTypeClient.post(dtoPropertyTypeList);
        assert results != null;
        assert results.getSuccessful() == 1;
        // lookup test service
        DtoService dtoService = serviceClient.lookup("local_load", "localhost");
        assert dtoService != null;
        assert !dtoService.getProperties().containsKey("TEST_PROPERTY");
        // set dynamic property
        DtoServiceList postDtoServiceList = new DtoServiceList();
        DtoService postDtoService = new DtoService();
        postDtoService.setDescription("local_load");
        postDtoService.setHostName("localhost");
        postDtoService.getProperties().put("TEST_PROPERTY", "TEST_PROPERTY_VALUE");
        postDtoServiceList.add(postDtoService);
        results = serviceClient.post(postDtoServiceList);
        assert results != null;
        assert results.getSuccessful() == 1;
        // validate dynamic property
        dtoService = serviceClient.lookup("local_load", "localhost");
        assert dtoService != null;
        assert "local_load".equals(dtoService.getDescription());
        assert "localhost".equals(dtoService.getHostName());
        assert "TEST_PROPERTY_VALUE".equals(dtoService.getProperties().get("TEST_PROPERTY"));
        // remove dynamic property
        postDtoService.getProperties().put("TEST_PROPERTY", null);
        results = serviceClient.post(postDtoServiceList);
        assert results != null;
        assert results.getSuccessful() == 1;
        // validate dynamic property
        dtoService = serviceClient.lookup("local_load", "localhost");
        assert dtoService != null;
        assert "local_load".equals(dtoService.getDescription());
        assert "localhost".equals(dtoService.getHostName());
        assert !dtoService.getProperties().containsKey("TEST_PROPERTY");
        // set dynamic property
        postDtoService.getProperties().put("TEST_PROPERTY", "TEST_PROPERTY_VALUE");
        results = serviceClient.post(postDtoServiceList);
        assert results != null;
        assert results.getSuccessful() == 1;
        // validate dynamic property
        dtoService = serviceClient.lookup("local_load", "localhost");
        assert dtoService != null;
        assert "local_load".equals(dtoService.getDescription());
        assert "localhost".equals(dtoService.getHostName());
        assert "TEST_PROPERTY_VALUE".equals(dtoService.getProperties().get("TEST_PROPERTY"));
        // remove dynamic property
        postDtoService.getProperties().put("TEST_PROPERTY", "");
        results = serviceClient.post(postDtoServiceList);
        assert results != null;
        assert results.getSuccessful() == 1;
        // validate dynamic property
        dtoService = serviceClient.lookup("local_load", "localhost");
        assert dtoService != null;
        assert "local_load".equals(dtoService.getDescription());
        assert "localhost".equals(dtoService.getHostName());
        assert !dtoService.getProperties().containsKey("TEST_PROPERTY");
        // delete test property type
        results = propertyTypeClient.delete(Arrays.asList(new String[]{dtoPropertyType.getName()}));
        assert results != null;
        assert results.getSuccessful() == 1;
    }


    public void testServiceHostMerge() {

        // create clients
        DeviceClient deviceClient = new DeviceClient(baseUrl);
        EventClient eventClient = new EventClient(baseUrl);
        HostClient hostClient = new HostClient(baseUrl);
        ServiceClient client = new ServiceClient(baseUrl);

        // create merge host
        DtoHostList dtoHosts = new DtoHostList();
        DtoHost dtoHost = new DtoHost();
        dtoHost.setHostName("test-merge-host");
        dtoHost.setMonitorStatus("PENDING");
        dtoHost.setDeviceIdentification("test-merge-device");
        dtoHost.setAppType("SEL");
        dtoHost.setAgentId("5437840f-a908-49fd-88bd-e04543a69e0");
        dtoHosts.add(dtoHost);
        DtoOperationResults results = hostClient.post(dtoHosts);
        assert results != null;
        assert results.getSuccessful() == 1;

        // attempt blocked create merge service, (merge disallowed)
        DtoServiceList dtoServices = new DtoServiceList();
        DtoService dtoService = new DtoService();
        dtoService.setHostName("TEST-MERGE-HOST");
        dtoService.setDeviceIdentification("test-merge-device");
        dtoService.setDescription("test-merge-service");
        dtoService.setMonitorStatus("PENDING");
        dtoService.setLastHardState("PENDING");
        dtoService.setLastPlugInOutput("testing service host merge");
        dtoService.setAppType("SEL");
        dtoService.setAgentId("5437840f-a908-49fd-88bd-e04543a69e0");
        dtoServices.add(dtoService);
        results = client.post(dtoServices, false);
        assert results != null;
        assert results.getWarning() == 1;

        // create merge service, (merge disallowed)
        dtoService.setHostName("test-merge-host");
        results = client.post(dtoServices, false);
        assert results != null;
        assert results.getSuccessful() == 1;

        // update merge service, (merge allowed)
        dtoService.setHostName("TEST-MERGE-HOST");
        dtoService.setMonitorStatus("OK");
        results = client.post(dtoServices, true);
        assert results != null;
        assert results.getSuccessful() == 1;

        // attempt blocked update merge service, (merge disallowed)
        dtoService.setMonitorStatus("UNSCHEDULED CRITICAL");
        results = client.post(dtoServices, false);
        assert results != null;
        assert results.getWarning() == 1;

        // validate update and events
        dtoService = client.lookup("test-merge-service", "Test-Merge-Host");
        assert dtoService != null;
        assert dtoService.getHostName().equals("test-merge-host");
        assert dtoService.getDescription().equals("test-merge-service");
        assert dtoService.getMonitorStatus().equals("OK");
        List<DtoEvent> dtoEvents = eventClient.query("hostStatus.host.hostName = 'test-merge-host'");
        assert dtoEvents != null;
        assert dtoEvents.size() == 1;
        assert dtoEvents.get(0).getHost().equals("test-merge-host");
        assert dtoEvents.get(0).getTextMessage().startsWith("Cannot update/merge hosts with matching names");

        // cleanup, (deleting device clears events)
        results = deviceClient.delete("test-merge-device");
        assert results != null;
        assert results.getSuccessful() == 1;
    }

    protected DtoServiceList buildDomServiceUpdate() {
        DtoServiceList services = new DtoServiceList();
        DtoService service = new DtoService();
        service.setDescription(KILLER_SLASH);
        service.setHostName("localhost");
        service.setMonitorServer("localhost");
        service.setDeviceIdentification("127.0.0.1");
        service.setAgentId("5437840f-a908-49fd-88bd-e04543a69e0");
        service.setMonitorStatus("UP");
        service.setLastHardState("UP");
        service.setAppType("VEMA");
        service.setCheckType("ACTIVE");
        service.setStateType("HARD");
        Calendar last = new GregorianCalendar(2013, Calendar.MAY, 20, 0, 0);
        Calendar next = new GregorianCalendar(2013, Calendar.MAY, 27, 0, 0);
        Calendar stateChange = new GregorianCalendar(2013, Calendar.MAY, 25, 0, 0);
        service.setLastStateChange(stateChange.getTime());
        service.setNextCheckTime(next.getTime());
        service.setLastCheckTime(last.getTime());
        service.putProperty("Latency", new Double(175.4));
        service.putProperty("ExecutionTime", new Double(200.5));
        service.putProperty("LastPluginOutput", "1.output");
        services.add(service);
        return services;
    }

    public static final String KILLER_SLASH = "burbank_ns1 [ethernet1/0] Interface Problems [out_discards]";


    public void testDomsUseCase() throws Exception {

        ServiceClient client = new ServiceClient(baseUrl);
        DtoOperationResults results = client.post(buildDomServiceUpdate());
        assertEquals(1, results.getCount().intValue());


        List<DtoService> services = client.list();
        boolean found = false;
        for (DtoService service : services) {
            if (service.getDescription().equals(KILLER_SLASH)) {
                found = true;
                break;
            }
        }
        assert found;

        try {
            DtoService failedBecauseOfSlash = client.lookup(KILLER_SLASH, "localhost");
            found = (failedBecauseOfSlash != null);
        }
        catch (Exception e) {
            found = false;
        }
        assert found;


        String query = String.format("hostname = 'localhost' and description = '%s'", KILLER_SLASH);
        services = client.query(query);
        assert services.size() == 1;
        DtoService serviceToDelete = new DtoService();
        for (DtoService service : services) {
            assertNotNull(service.getAppType());
            serviceToDelete = service;
            assert service.getHostName().equals("localhost");
            assert service.getDescription().equals(KILLER_SLASH);
        }

        boolean exceptioned = false;
        try {
            client.delete(KILLER_SLASH, "localhost");
        }
        catch (Exception e) {
            exceptioned = true;
        }
        assert !exceptioned;

        services = client.query(query);
        assert services.size() == 0;
    }

}
