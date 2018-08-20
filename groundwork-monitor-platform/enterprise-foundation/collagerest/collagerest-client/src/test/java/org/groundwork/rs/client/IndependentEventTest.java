package org.groundwork.rs.client;


import org.groundwork.rs.client.clientdatamodel.IndependentGeneralProperties;
import org.groundwork.rs.dto.*;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import javax.ws.rs.core.MediaType;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import static org.groundwork.rs.client.clientdatamodel.IndependentGeneralProperties._hostsToGenerate;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;



/**
 * Created by rphillips on 3/15/16.
 */
public class IndependentEventTest  extends IndependentClientTestBase  {

    public ArrayList<String> eventidlist = new ArrayList<String>();
    public ArrayList<String> ackeventidlist = new ArrayList<String>();

    @Test
    @Category(org.groundwork.rs.client.IndependentClientTestBase.class)
    public void runEventsTest() throws Exception
    {

        //Step 1: Generate AppType to support Event testing.
        System.out.println("**** Generate AppType to support Event Testing.");
        System.out.println("-----------------------------------------------------------------");
        CreateAppType();
        System.out.println("-----------------------------------------------------------------");
        System.out.println("**** Finished Create AppType.");


        //Step 2: Generate Host to support Event testing.
        System.out.println("**** Generate Host to support Event Testing.");
        System.out.println("-----------------------------------------------------------------");
        CreateHost();
        CreateAckHost();

        System.out.println("-----------------------------------------------------------------");
        System.out.println("**** Finished Create Host.");


        //Step 3: Create Service
        System.out.println("**** Begin Create Service for Events Test");
        System.out.println("-----------------------------------------------------------------");
        CreateService();
        System.out.println("-----------------------------------------------------------------");
        System.out.println("**** Finished Create Service for Events Test");

        //Step 4: Create Events
        System.out.println("**** Begin Unit Test Create Events: " + baseUrl + "... ");
        System.out.println("-----------------------------------------------------------------");
        CreateEvents();
        CreateAckEvent();
        System.out.println("-----------------------------------------------------------------");
        System.out.println("**** Finished Create Events URL: " + baseUrl + "... ");

        //Step 5: Validate Events
        System.out.println("**** Begin Unit Test Validate Events: " + baseUrl + "... ");
        System.out.println("-----------------------------------------------------------------");
        validateEvents();
        System.out.println("-----------------------------------------------------------------");
        System.out.println("**** Finished Validate Events URL: " + baseUrl + "... ");

        //Step 6: Set Dynamic Event Property
        System.out.println("**** Begin Unit Test Set Dynamic Property for Events: " + baseUrl + "... ");
        System.out.println("-----------------------------------------------------------------");
        testSetDynamicProperty();
        System.out.println("-----------------------------------------------------------------");
        System.out.println("**** Finished Set Dynamic Property for Events: " + baseUrl + "... ");
        testPutEventProperties();
        //Step 7: Set Acknowledge
        System.out.println("**** Begin Unit Test Set Acknowledge for Events: " + baseUrl + "... ");
        System.out.println("-----------------------------------------------------------------");
        testAcknowledge();
        System.out.println("-----------------------------------------------------------------");
        System.out.println("**** Finished Set Acknowledge for Events: " + baseUrl + "... ");

        //Step 8: Set Acknowledge
        System.out.println("**** Begin Unit Test Set Event Consolidation: " + baseUrl + "... ");
        System.out.println("-----------------------------------------------------------------");
        EventsConsolidation();
        System.out.println("-----------------------------------------------------------------");
        System.out.println("**** Finished Set Event Consolidation: " + baseUrl + "... ");

        //Step 9: Set Acknowledge
        System.out.println("**** Begin Unit Test Set Dynamic Properties for Events: " + baseUrl + "... ");
        System.out.println("-----------------------------------------------------------------");
        EventProperties();
        System.out.println("-----------------------------------------------------------------");
        System.out.println("**** Finished Set Dynamic Properties for Events: " + baseUrl + "... ");

        //Step 10: Delete Host
        System.out.println("**** Begin Delete Hosts");
        System.out.println("-----------------------------------------------------------------");
        deleteHost();
        System.out.println("-----------------------------------------------------------------");
        System.out.println("**** Finished Delete Hosts");

        //Step 11: Delete Device
        System.out.println("**** Begin Delete Device for Events Test");
        System.out.println("-----------------------------------------------------------------");
        deleteDevice();
        System.out.println("-----------------------------------------------------------------");
        System.out.println("**** Finished Delete Device for Events Test");

        //Step 12: Delete Events
        System.out.println("**** Begin Unit Test Delete Events: " + baseUrl + "... ");
        System.out.println("-----------------------------------------------------------------");
        deleteEvents();
        System.out.println("-----------------------------------------------------------------");
        System.out.println("**** Finished Delete Events URL: " + baseUrl + "... ");

        //Step 13: Delete Service
        System.out.println("**** Begin Delete Service for Events Test");
        System.out.println("-----------------------------------------------------------------");
        deleteService();
        System.out.println("-----------------------------------------------------------------");
        System.out.println("**** Finished Delete Service for Events Test");



    }

    public void CreateService() throws Exception{
        DtoServiceList services = new DtoServiceList();
        DtoService service = new DtoService();


        service.setDescription("serviceEventTest-100");
        service.setHostName("Test-Server-Dev-0");
        service.setMonitorServer("localhost");
        service.setDeviceIdentification("000.000.000.0");
        service.setAgentId("5437840f-a908-49fd-88bd-e04543a69e0");
        service.setMonitorStatus("UP");
        service.setLastHardState("UP");
        service.setAppType("UNITTESTAPP");
        service.setCheckType("ACTIVE");
        service.setStateType("HARD");
        Calendar last = new GregorianCalendar(2016, Calendar.MAY, 20, 0, 0);
        Calendar next = new GregorianCalendar(2016, Calendar.MAY, 27, 0, 0);
        Calendar stateChange = new GregorianCalendar(2016, Calendar.MAY, 25, 0, 0);
        service.setLastStateChange(stateChange.getTime());
        service.setNextCheckTime(next.getTime());
        service.setLastCheckTime(last.getTime());
        service.putProperty("Latency", new Double(175.4));
        service.putProperty("ExecutionTime", new Double(200.5));
        service.putProperty("LastPluginOutput", "1.output");
        services.add(service);
        ServiceClient client = new ServiceClient(baseUrl);
        DtoOperationResults results = client.post(services);
        assertEquals(1, results.getCount().intValue());
        DtoService service1 = new DtoService();

        service1 = client.lookup("serviceEventTest-100", "Test-Server-Dev-0");
        if (service1 == null) {
            assertNull(service1);
        }
        else{
            System.out.println("ServiceName Created: " + service1.getDescription());
        }

    }

    public void deleteService() throws Exception{
        ServiceClient client = new ServiceClient(baseUrl);
        client.delete("serviceEventTest-100", "Test-Server-Dev-0");
    }

    public void CreateAppType() throws Exception
    {
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
        ApplicationTypeClient appclient = new ApplicationTypeClient(baseUrl);
        appclient.post(apptypelist);
    }

    public void CreateHost() throws Exception
    {
        DtoHostList hostCreate = CreateSingleHost();
        DtoOperationResults results = executePost(hostCreate);
        assertEquals(1, results.getCount().intValue());

        for (DtoOperationResult result : results.getResults()) {
            assert(result.getStatus().equals(DtoOperationResult.SUCCESS));
        }

    }



    public void CreateAckHost() throws Exception
    {
        DtoHostList hostCreate = CreateAckHosts();
        DtoOperationResults results = executePost(hostCreate);
        assertEquals(1, results.getCount().intValue());

        for (DtoOperationResult result : results.getResults()) {
            assert(result.getStatus().equals(DtoOperationResult.SUCCESS));
        }

    }


    public void ViewCurrentEvents() throws Exception{

        EventClient client1 = new EventClient(baseUrl);
        List<DtoEvent> events = client1.query("host = 'Test-Server-Dev-0'");

        for (DtoEvent event : events) {
            System.out.println("Viewing Events: " + event.getId());
        }

    }

    public void ViewCurrentServices() throws Exception{
        ServiceClient client1 = new ServiceClient(baseUrl);
        List<DtoService> services = client1.query("");
        for (DtoService service : services) {
            System.out.println("Viewing Services: " + service.getDescription());
        }

    }

    public DtoHostList CreateSingleHost() throws Exception
    {
        DtoHostList hosts = new DtoHostList();
        for(int x=0; x < 1; x++ ){
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
            Calendar last = new GregorianCalendar(2016, Calendar.SEPTEMBER, 1, 0, 0);
            host.putProperty("LastStateChange", last);
            hosts.add(host);
            System.out.println("**** Generate Host: " + "Test-Server-Dev-" + x);
        }
        return hosts;
    }

    protected DtoOperationResults executePost(DtoHostList hostUpdates) throws Exception {
        HostClient client = new HostClient(IndependentGeneralProperties._baseUrl);
        return client.post(hostUpdates);
    }


    public void deleteDevice()
    {
        for(int x=0; x < 1; x++ ){
            DeviceClient deviceClient = new DeviceClient(IndependentGeneralProperties._baseUrl);
            DtoDevice device = deviceClient.lookup("000.000.000." + x + 100);
            assertNull(device);
            deviceClient.delete("000.000.000." + x + 100);
            System.out.println("Cleaning up device for Event Testing: " + "000.000.000." + x + 100);
        }
    }

    public void deleteHost()
    {
        for(int x=0; x < 1; x++ ){
            HostClient client = new HostClient(IndependentGeneralProperties._baseUrl);
            DtoHost host = client.lookup("Test-Server-Dev-" + x);
            if (host != null) {
                String hostIds = "Test-Server-Dev-" + x;
                System.out.println("**** CleanUp Hosts for Event Testing: " + hostIds);
                List<String> ids = new ArrayList<String>();
                Collections.addAll(ids, hostIds.split(","));
                client.delete(ids);
            }
        }

    }

    public void CreateEvents() throws Exception {

        Date reportDate =  parseDate("2016-06-02 10:55:32.943");
        // create client
        EventClient client = new EventClient(baseUrl);

        // create events
        DtoEventList events = AutoCreateEvents(reportDate, false);
        DtoOperationResults results = client.post(events);

        for (int x = 0; (x < IndependentGeneralProperties._eventsToGenerate); x++) {
            System.out.println("Successfully Generated Event: " + results.getResults().get(x).getEntity() + "\r\n");

            eventidlist.add(results.getResults().get(x).getEntity());
        }

        assert results != null;
        assert results.getCount() == IndependentGeneralProperties._eventsToGenerate;
        assert results.getSuccessful() == IndependentGeneralProperties._eventsToGenerate;


    }

    public void CreateAckEvent() throws Exception {

        Date reportDate =  parseDate("2016-06-02 10:55:32.943");
        // create client
        EventClient client = new EventClient(baseUrl);

        // create events
        DtoEventList events = CreateAckEvents();
        DtoOperationResults results = client.post(events);


    }

    private DtoEventList AutoCreateEvents(Date reportDate, boolean enableConsolidation) {
        DtoEventList events = new DtoEventList();
        for (int x = 0; (x < IndependentGeneralProperties._eventsToGenerate); x++) {
            DtoEvent event = new DtoEvent();
            if (enableConsolidation) {
                event.setConsolidationName("UNITTESTAPPEVENT");
            }
            event.setMonitorServer("localhost");
            event.setMonitorStatus("UP");
            event.setService("serviceEventTest-100");
            event.setAppType("UNITTESTAPP");
            event.setDevice("000.000.000.0");
            event.setSeverity("SERIOUS");
            event.setTextMessage("This is a serious UNITTESTAPP Message on Device 000.000.000.1 - " + x);
            event.setOperationStatus("OPEN");
            event.setHost("Test-Server-Dev-0");
            event.setReportDate(reportDate);
            event.putProperty("Latency", new Double(125.31));
            event.putProperty("UpdatedBy", "UnitTester");
            event.putProperty("Comments", "This is a test.");
            event.setErrorType("ErrorType");
            event.setComponent("MySubComponent");
            events.add(event);

        }
        return events;
    }

    public void deleteEvents() throws Exception{
        EventClient client1 = new EventClient(baseUrl);

        List<DtoEvent> events2 = client1.query("host = 'Test-Server-Dev-0'");

        for (DtoEvent event : events2) {
            client1.delete(event.getId().toString());
            System.out.println("Deleted Event: " + event.getId());
        }

        EventClient client2 = new EventClient(baseUrl);

        List<DtoEvent> events3 = client2.query("host = 'Test-Server-Dev-Ack'");

        for (DtoEvent event : events3) {
            client2.delete(event.getId().toString());
            System.out.println("Deleted Event: " + event.getId());
        }

        EventClient client3 = new EventClient(baseUrl);

        List<DtoEvent> events4 = client3.query("host = 'localhost'");

        for (DtoEvent event : events4) {
            client3.delete(event.getId().toString());
            System.out.println("Deleted Event: " + event.getId());
        }
    }


    protected Date parseDate(String date) throws Exception{
        try {
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            format.setTimeZone(TimeZone.getTimeZone("PST8PDT"));
            return format.parse(date);
        }
        catch (Exception ex){
            return null;
        }
    }

    private void validateEvents() throws Exception{

        Date reportDate =  parseDate("2016-06-02 10:55:32.943");
        EventClient client1 = new EventClient(baseUrl);
        int x = 0;
        List<DtoEvent> events = client1.query("(host = 'Test-Server-Dev-0') order by id");

        for (DtoEvent event : events) {
            assert events.size()  == IndependentGeneralProperties._eventsToGenerate;

            if (event == null)
            {
                System.out.println("**** Failed Lookup of event: " + event.getId());
                assertNull(event);
            }

            assertEquals("UP", event.getMonitorStatus());
            assertEquals("serviceEventTest-100", event.getService());
            assertEquals("UNITTESTAPP", event.getAppType());
            assertEquals("000.000.000.0", event.getDevice());
            assertEquals("SERIOUS", event.getSeverity());
            assertEquals("This is a serious UNITTESTAPP Message on Device 000.000.000.1 - " + x, event.getTextMessage());
            assertEquals("OPEN", event.getOperationStatus());
            assertEquals("Test-Server-Dev-0", event.getHost());
            assertDatesEqual(reportDate, event.getFirstInsertDate());
            assertEquals("125.31", event.getProperty("Latency"));
            assertEquals("UnitTester", event.getProperty("UpdatedBy"));
            assertEquals("This is a test.", event.getProperty("Comments"));
            assertEquals("ErrorType", event.getProperty("ErrorType"));
            assertEquals("MySubComponent", event.getProperty("SubComponent"));
            System.out.println("**** Successfully Validated Event: " + event.getId());
            x++;
        }

    }


    protected void assertDatesEqual(Date test, Date actual) {
        Calendar calendarTest = Calendar.getInstance();
        calendarTest.setTime(test);
        Calendar calendarActual = Calendar.getInstance();
        calendarActual.setTime(actual);
        assertEquals(calendarTest.get(Calendar.YEAR), calendarActual.get(Calendar.YEAR));
        assertEquals(calendarTest.get(Calendar.MONTH), calendarActual.get(Calendar.MONTH));
        assertEquals(calendarTest.get(Calendar.DAY_OF_MONTH), calendarActual.get(Calendar.DAY_OF_MONTH));
        assertEquals(calendarTest.get(Calendar.HOUR), calendarActual.get(Calendar.HOUR));
        assertEquals(calendarTest.get(Calendar.MINUTE), calendarActual.get(Calendar.MINUTE));
        assertEquals(calendarTest.get(Calendar.SECOND), calendarActual.get(Calendar.SECOND));
    }



    public void testSetDynamicProperty() throws Exception {
        // allocate clients
        PropertyTypeClient propertyTypeClient = new PropertyTypeClient(baseUrl);
        EventClient eventClient = new EventClient(baseUrl);

        // test using XML
        propertyTypeClient.setMediaType(MediaType.APPLICATION_XML_TYPE);
        eventClient.setMediaType(MediaType.APPLICATION_XML_TYPE);
        testSetDynamicProperty(propertyTypeClient, eventClient);

        // test using JSON
        propertyTypeClient.setMediaType(MediaType.APPLICATION_JSON_TYPE);
        eventClient.setMediaType(MediaType.APPLICATION_JSON_TYPE);
        testSetDynamicProperty(propertyTypeClient, eventClient);
    }

    private void testSetDynamicProperty(PropertyTypeClient propertyTypeClient, EventClient eventClient){

        DtoPropertyTypeList dtoPropertyTypeList = new DtoPropertyTypeList();
        DtoPropertyType dtoPropertyType = new DtoPropertyType();
        dtoPropertyType.setName("TEST_PROPERTY");
        dtoPropertyType.setDescription("ErrorType");
        dtoPropertyType.setDataType(DtoPropertyDataType.STRING);
        dtoPropertyTypeList.add(dtoPropertyType);
        DtoOperationResults results = propertyTypeClient.post(dtoPropertyTypeList);
        assert results != null;
        assert results.getSuccessful() == 1;

        List<DtoEvent> events = eventClient.query("(host = 'Test-Server-Dev-0') order by id");
        String eventId = events.get(0).getId().toString();
        System.out.println("Dynamic Event Set for EventID: " + eventId);
        assert eventId != null;

        DtoEvent dtoEvent = events.get(0);
        System.out.println("Dynamic EventID for event selected: " + dtoEvent.getId().toString());
        assert dtoEvent != null;

        assert !dtoEvent.getProperties().containsKey("TEST_PROPERTY");

        DtoEventPropertiesList dtoEventPropertiesList = new DtoEventPropertiesList();
        DtoEventProperties dtoEventProperties = new DtoEventProperties(Integer.parseInt(eventId));
        dtoEventProperties.getProperties().put("TEST_PROPERTY", "TEST_PROPERTY_VALUE");
        dtoEventPropertiesList.addEvent(dtoEventProperties);
        results = eventClient.update(dtoEventPropertiesList);
        assert results != null;
        assert results.getSuccessful() == 1;

        dtoEvent = eventClient.lookup(eventId);
        assert dtoEvent != null;
        assert eventId.equals(dtoEvent.getId().toString());
        assert "TEST_PROPERTY_VALUE".equals(dtoEvent.getProperties().get("TEST_PROPERTY"));

        dtoEventProperties.getProperties().put("TEST_PROPERTY", null);
        results = eventClient.update(dtoEventPropertiesList);
        assert results != null;
        assert results.getSuccessful() == 1;

        dtoEvent = eventClient.lookup(eventId);
        assert dtoEvent != null;
        assert eventId.equals(dtoEvent.getId().toString());
        assert !dtoEvent.getProperties().containsKey("TEST_PROPERTY");

        dtoEventProperties.getProperties().put("TEST_PROPERTY", "TEST_PROPERTY_VALUE");
        results = eventClient.update(dtoEventPropertiesList);
        assert results != null;
        assert results.getSuccessful() == 1;

        dtoEvent = eventClient.lookup(eventId);
        assert dtoEvent != null;
        assert eventId.equals(dtoEvent.getId().toString());
        assert "TEST_PROPERTY_VALUE".equals(dtoEvent.getProperties().get("TEST_PROPERTY"));

        dtoEventProperties.getProperties().put("TEST_PROPERTY", "");
        results = eventClient.update(dtoEventPropertiesList);
        assert results != null;
        assert results.getSuccessful() == 1;

        dtoEvent = eventClient.lookup(eventId);
        assert dtoEvent != null;
        assert eventId.equals(dtoEvent.getId().toString());
        assert !dtoEvent.getProperties().containsKey("TEST_PROPERTY");

        results = eventClient.delete(eventId);
        assert results != null;
        assert results.getSuccessful() == 1;

        results = propertyTypeClient.delete(Arrays.asList(new String[]{dtoPropertyType.getName()}));
        assert results != null;
        assert results.getSuccessful() == 1;
    }



    public DtoHostList CreateAckHosts() throws Exception
    {
        DtoHostList hosts = new DtoHostList();

            DtoHost host = new DtoHost();
            host.setHostName("Test-Server-Dev-Ack");
            host.setDescription("Server" + "Ack");
            host.setAgentId("5437840f-a908-49fd-88bd-e04543a69e" + "Ack");
            host.setMonitorStatus("UP");
            host.setAppType("UNITTESTAPP");
            host.setDeviceIdentification("000.000.000." + "Ack");
            host.setMonitorServer("localhost");
            host.setDeviceDisplayName("Device" + "Ack");
            host.putProperty("Latency", new Double(125.1 + 222));
            host.putProperty("UpdatedBy", "UnitTester" + "Ack");
            host.putProperty("Comments", "This is a test." + "Ack");
            Calendar last = new GregorianCalendar(2016, Calendar.SEPTEMBER, 1, 0, 0);
            host.putProperty("LastStateChange", last);
            hosts.add(host);
            System.out.println("**** Generate Host: " + "Test-Server-Dev-Ack");

        return hosts;
    }

    private DtoEventList CreateAckEvents() throws Exception{
        Date reportDate =  parseDate("2016-06-02 10:55:32.943");
        DtoEventList events = new DtoEventList();

            DtoEvent event = new DtoEvent();

            event.setMonitorServer("localhost");
            event.setMonitorStatus("UP");
            event.setService("serviceEventTest-100");
            event.setAppType("NAGIOS");
            event.setDevice("000.000.000.0");
            event.setSeverity("SERIOUS");
            event.setTextMessage("This is a serious NAGIOS Message on Device 000.000.000.1 - ");
            event.setOperationStatus("OPEN");
            event.setHost("localhost");
            event.setReportDate(reportDate);
            event.putProperty("Latency", new Double(125.31));
            event.putProperty("UpdatedBy", "UnitTester");
            event.putProperty("Comments", "This is a test.");
            event.setErrorType("ErrorType");
            event.setComponent("MySubComponent");
            events.add(event);


        return events;
    }
    int globalevent = 0;

    public void testPutEventProperties() throws Exception {

        EventClient client = new EventClient(baseUrl);
        DtoEvent event = client.query("appType = 'NAGIOS' and host = 'localhost'").get(0);
        DtoEventProperties entity = new DtoEventProperties(event.getId());
        entity.putProperty("AcknowledgedBy", "707");
        entity.putProperty("LastPluginOutput", "output output");
        DtoEventPropertiesList entities = new DtoEventPropertiesList();
        entities.addEvent(entity);
        DtoOperationResults results = client.update(entities);
        for (DtoOperationResult result : results.getResults()) {
            assert(result.getStatus().equals(DtoOperationResult.SUCCESS));
        }
        List<DtoEvent> events = client.query("id = " + event.getId());
        globalevent = event.getId();
        assert events.size()  == 1;
        for (DtoEvent e : events) {
            assert e.getProperty("AcknowledgedBy").equals("707");
            assert e.getProperty("LastPluginOutput").equals("output output");
        }
        client.delete(event.getId().toString());
        assert null == client.lookup(event.getId().toString());
    }

    public void testAcknowledge() throws Exception {
        simulateNagiosAcknowledge();
        EventClient client = new EventClient(baseUrl);
        DtoAcknowledge ack = new DtoAcknowledge("NAGIOS", "localhost");
        ack.setAcknowledgedBy("admin");
        ack.setAcknowledgeComment("acknowledged ok by admin");
        DtoAcknowledgeList acks = new DtoAcknowledgeList();
        acks.add(ack);
        DtoOperationResults results = client.acknowledge(acks);
        assertEquals(1, results.getCount().intValue());
        for (DtoOperationResult result : results.getResults()) {
            System.out.println("NAGIOS ACK Results: " + result.getStatus());
            assert(result.getStatus().equals(DtoOperationResult.SUCCESS));
        }
        List<DtoEvent> events = client.query("appType = 'NAGIOS' and host = 'localhost'");
        assert events.size() > 0;
        int count = 0;
        for (DtoEvent event : events) {
            String opStatus = event.getOperationStatus();
            if (opStatus.equals("ACKNOWLEDGED")) {
                assert event.getProperty("AcknowledgedBy").equals("admin");
                assert event.getProperty("AcknowledgeComment").equals("acknowledged ok by admin");
                assert event.getOperationStatus().equals("ACKNOWLEDGED");
                count++;
            }
        }
        assert count > 0;
        testUnacknowledge();
    }

    public void testUnacknowledge() throws Exception {
        EventClient client = new EventClient(baseUrl);
        DtoUnAcknowledge unack = new DtoUnAcknowledge("NAGIOS", "localhost");
        DtoUnAcknowledgeList unacks = new DtoUnAcknowledgeList();
        unacks.add(unack);
        DtoOperationResults results = client.unacknowledge(unacks);
        assertEquals(1, results.getCount().intValue());
        for (DtoOperationResult result : results.getResults()) {
            assert(result.getStatus().equals(DtoOperationResult.SUCCESS));
        }
        List<DtoEvent> events = client.query("appType = 'NAGIOS' and host = 'localhost'");
        assert events.size() > 0;
        int count = 0;
        for (DtoEvent event : events) {
            String opStatus = event.getOperationStatus();
            if (opStatus.equals("OPEN")) {
                String ackBy = event.getProperty("AcknowledgedBy");
                String comment = event.getProperty("AcknowledgeComment");
                assert ackBy == null || ackBy.equals("");
                assert comment == null || comment.equals("");
                assert event.getOperationStatus().equals("OPEN");
                count++;
            }
        }
        assert count > 0;

    }

    private void simulateNagiosAcknowledge() {
        EventClient client = new EventClient(baseUrl);
        List<DtoEvent> events = client.query("host = 'localhost'");
        assert events.size() > 0;
        DtoEvent event = events.get(0);
        DtoEventProperties entity1 = new DtoEventProperties(event.getId());
        entity1.putProperty("AcknowledgeComment", "localhost ACK pending");
        DtoEventPropertiesList entities = new DtoEventPropertiesList();
        entities.addEvent(entity1);
        client.update(entities);
    }


    public void EventProperties() throws Exception {

        EventClient client = new EventClient(baseUrl);
        DtoEvent event = client.query("(host = 'Test-Server-Dev-0') order by id").get(0);
        DtoEventProperties entity = new DtoEventProperties(event.getId());
        entity.putProperty("AcknowledgedBy", "707");
        entity.putProperty("LastPluginOutput", "output output");
        DtoEventPropertiesList entities = new DtoEventPropertiesList();
        entities.addEvent(entity);
        DtoOperationResults results = client.update(entities);
        for (DtoOperationResult result : results.getResults()) {
            assert(result.getStatus().equals(DtoOperationResult.SUCCESS));
        }
        List<DtoEvent> events = client.query("id = " + event.getId());
        assert events.size()  == 1;
        for (DtoEvent e : events) {
            assert e.getProperty("AcknowledgedBy").equals("707");
            assert e.getProperty("LastPluginOutput").equals("output output");
        }
        client.delete(event.getId().toString());
        assert null == client.lookup(event.getId().toString());
    }

    public void EventsConsolidation() throws Exception {

        Date reportDate =  parseDate("2013-06-02 10:55:32.943");
        EventClient client = new EventClient(baseUrl);
        DtoOperationResults results = client.post(AutoCreateEvents(reportDate, true));

        String entity = null;
        DtoOperationResult result = results.getResults().get(0);
            assert(result.getStatus().equals(DtoOperationResult.SUCCESS));
            entity = result.getEntity();

        // assert data written
        DtoEvent event = client.lookup(entity);

        assertEquals(IndependentGeneralProperties._eventsToGenerate + 1, event.getMsgCount().intValue());



    }



}
