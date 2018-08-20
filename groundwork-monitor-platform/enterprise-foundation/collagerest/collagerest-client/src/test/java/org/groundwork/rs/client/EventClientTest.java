package org.groundwork.rs.client;

import org.groundwork.rs.dto.DtoAcknowledge;
import org.groundwork.rs.dto.DtoAcknowledgeList;
import org.groundwork.rs.dto.DtoEvent;
import org.groundwork.rs.dto.DtoEventList;
import org.groundwork.rs.dto.DtoEventProperties;
import org.groundwork.rs.dto.DtoEventPropertiesList;
import org.groundwork.rs.dto.DtoOperationResult;
import org.groundwork.rs.dto.DtoOperationResults;
import org.groundwork.rs.dto.DtoPropertyDataType;
import org.groundwork.rs.dto.DtoPropertyType;
import org.groundwork.rs.dto.DtoPropertyTypeList;
import org.groundwork.rs.dto.DtoServiceKey;
import org.groundwork.rs.dto.DtoStateTransition;
import org.groundwork.rs.dto.DtoUnAcknowledge;
import org.groundwork.rs.dto.DtoUnAcknowledgeList;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public class EventClientTest extends AbstractClientTest {


    @Test
    public void testLookupEvent() throws Exception {
        if (serverDown) return;
        List<String> ids = new LinkedList<String>();
        EventClient client = new EventClient(getDeploymentURL());
        List<DtoEvent> events = client.query("host = 'demo' and service = 'http_alive' and monitorStatus = 'WARNING'");
        assert events.size() == 1;
        ids.add(events.get(0).getId().toString());
        events = client.lookup(ids);
        assert events.size() == 1;
        DtoEvent event = events.get(0);
        assertEquals("WARNING", event.getSeverity());
        assertEquals("http_alive", event.getService());
        assertDatesEqual(2013, Calendar.MAY, 17, 9, 29, event.getLastInsertDate());
        assertEquals("demo:http_alive", event.getProperty("SubComponent"));
        assertEquals("SERVICE ALERT", event.getProperty("ErrorType"));

        events = client.query("host = 'malbec' and service = 'ssh_alive' and monitorStatus = 'CRITICAL'");
        assert events.size() == 1;
        ids.add(events.get(0).getId().toString());
        event = events.get(0);
        assertEquals("CRITICAL", event.getSeverity());
        assertEquals("ssh_alive", event.getService());
        assertDatesEqual(2013, Calendar.MAY, 18, 11, 53, event.getLastInsertDate());
        assertEquals("malbec:ssh_alive", event.getProperty("SubComponent"));
        assertEquals("SERVICE ALERT", event.getProperty("ErrorType"));

        events = client.lookup(ids);
        assertEquals(2, events.size());
        int count = 0;
        for (DtoEvent e : events) {
            if (e.getHost().equals("demo"))
                count++;
            if (e.getHost().equals("malbec"))
                count++;
        }
        assert count == 2;
    }

    @Test
    public void testQueryByLike() throws Exception {
        if (serverDown) return;
        EventClient client = new EventClient(getDeploymentURL());
        List<DtoEvent> events = client.query("message like 'TCP OK%'");
        assertTrue(events.size() >= 2);
        for (DtoEvent event : events) {
            assertTrue(event.getTextMessage().startsWith("TCP OK"));
        }
    }

    @Test
    public void testQueryEvents() {
        if (serverDown) return;
        EventClient client = new EventClient(getDeploymentURL());
        List<DtoEvent> events = client.query("(property.SubComponent like 'demo%' and property.ErrorType = 'SERVICE ALERT')");
        assertEquals(26, events.size());
        for (DtoEvent event : events) {
            assertTrue(event.getProperty("ErrorType").startsWith("SERVICE ALERT"));
            assertTrue(event.getProperty("SubComponent").startsWith("demo"));
        }
    }

    @Test
    public void testQueryByMsgCount() throws Exception {
        if (serverDown) return;
        EventClient client = new EventClient(getDeploymentURL());

        List<DtoEvent> events = client.query("host = 'demo' and service = 'http_alive' and monitorStatus = 'WARNING'");
        assert events.size() == 1;
        DtoEvent event = events.get(0);
        DtoEventPropertiesList updates = new DtoEventPropertiesList();
        DtoEventProperties properties = new DtoEventProperties(event.getId());
        properties.putProperty("MessageCount", new Integer(3));
        updates.addEvent(properties);
        DtoOperationResults results = client.update(updates);
        assert results.getSuccessful() == 1;

        events = client.query("count > 2");
        assert events.size() >= 1;
        for (DtoEvent event1 : events) {
            assertTrue(event1.getMsgCount() > 2);
        }

    }

    @Test
    public void testAppTypeNotNull() throws Exception {
        if (serverDown) return;
        EventClient client = new EventClient(getDeploymentURL());
        List<DtoEvent> events = client.list();
        int initialTotalCount = events.size();
        events = client.query("appType is not null");
        int notNullCount = events.size();
        events = client.query("appType is null");
        int nullCount = events.size();
        assert (notNullCount + nullCount) >= initialTotalCount;
        events = client.list();
        int totalCount = events.size();
        assert (notNullCount + nullCount) <= totalCount;
    }

    @Test
    public void testAttributeJoins() throws Exception {
        if (serverDown) return;
        // retrieve using like with a basic property
        EventClient client = new EventClient(getDeploymentURL());
        List<DtoEvent> events = client.query("device = '172.28.113.156' and severity = 'CRITICAL'");
        assertEquals(3, events.size());
        for (DtoEvent event : events) {
            assertTrue(event.getDevice().equals("172.28.113.156"));
            assertTrue(event.getSeverity().equals("CRITICAL"));
        }
    }

    @Test
    public void testSeverityAndOrderBy() throws Exception {
        if (serverDown) return;
        // test order by on joined attribute
        EventClient client = new EventClient(getDeploymentURL());
        List<DtoEvent> events = client.query("severity > '' order by operationStatus");
        String lastStatus = "";
        for (DtoEvent event : events) {
            assertTrue(lastStatus.compareTo(event.getOperationStatus()) < 0);
        }
    }

    @Test
    public void testBetweenDateQueries() throws Exception {
        if (serverDown) return;
        // in hosts
        EventClient client = new EventClient(getDeploymentURL());
        List<DtoEvent> events = client.query("lastInsertDate between '2013-05-20' and '2013-05-22 23:59:59' order by lastInsertDate");
        assertEquals(6, events.size());
        Calendar begin = new GregorianCalendar(2013, Calendar.MAY, 20, 0, 0);
        Calendar end = new GregorianCalendar(2013, Calendar.MAY, 22, 23, 59);
        for (DtoEvent event : events) {
            Calendar cal = new GregorianCalendar();
            cal.setTime(event.getLastInsertDate());
            assert(cal.after(begin));
            assert(cal.before(end));
        }
    }

    @Test
    public void testInQueries() throws Exception {
        if (serverDown) return;
        // in hosts
        EventClient client = new EventClient(getDeploymentURL());
        List<DtoEvent> events = client.query("host in ('qa-load-xp-1','qa-sles-11-64','do-win7-1')");
        assertEquals(9, events.size());
        for (DtoEvent event : events) {
            String host = event.getHost();
            assertTrue(host.equals("qa-load-xp-1") || host.equals("qa-sles-11-64") || host.equals("do-win7-1"));
        }
        // in host groups
        events = client.query("hostgroup in ('IT','HG1')");
        assertTrue(events.size() >= 30);
    }

//    @Test
//    public void testCategoryName() throws Exception {
//        if (serverDown) return;
//        EventClient client = new EventClient(getDeploymentURL());
//        List<DtoEvent> events = client.query("category = 'SG1'");
//        assert events.size() == 1;
//    }
//
//    @Test
//    public void testServiceGroupIn() throws Exception {
//        if (serverDown) return;
//        EventClient client = new EventClient(getDeploymentURL());
//        List<DtoEvent> events = client.query("(serviceGroup in ('web-svr','SG1'))"); // web-svr(17) SG1(2)
//        assertEquals(2, events.size());
//        for (DtoEvent event : events) {
//            System.out.println("event: " + event.getId());
//        }
//    }

    @Test
    public void testServiceGroupInHostGroup() throws Exception {
        if (serverDown) return;
        EventClient client = new EventClient(getDeploymentURL());
        List<DtoEvent> events = client.query("(serviceGroup in ('web-svr','SG1') and hostgroup in ('IT','HG1'))");
        assertTrue(events.size() >= 2);
    }

    @Test
    public void testPaging() throws Exception {
        if (serverDown) return;
        int first = 0;
        int count = 10;
        int processed = 0;
        do {
            processed = runPaging(first, count);
            first += processed;
        } while (processed == count);
    }

    private int runPaging(int first, int count) throws Exception {
        EventClient client = new EventClient(getDeploymentURL());
        List<DtoEvent> events = client.query("order by id", first, count);
        int last = 0;
        int processed = 0;
        for (DtoEvent event : events) {
            assert(last < event.getId());
            processed++;
        }
        return processed;
    }

    @Test
    public void testPutEventProperties() throws Exception {
        if (serverDown) return;
        EventClient client = new EventClient(getDeploymentURL());
        DtoEvent event = storeTestEvent(client);
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

    @Test
    @Deprecated
    public void testUpdateOpStatus() throws Exception {
        if (serverDown) return;
        boolean testStackTrace = false;
        EventClient client = new EventClient(getDeploymentURL());
        DtoEvent event1 = storeTestEvent(client);
        List<String> ids = new ArrayList<String>();
        ids.add(event1.getId().toString());
        DtoOperationResults results = client.update(ids, "NOTIFIED", "admin", "Updating the status to notified");
        assertEquals(1, results.getCount().intValue());
        for (DtoOperationResult result : results.getResults()) {
            assert(result.getStatus().equals(DtoOperationResult.SUCCESS));
        }
        List<DtoEvent> events = client.lookup(ids);
        for (DtoEvent event : events) {
            assertEquals("NOTIFIED", event.getOperationStatus());
        }

        results = client.update(ids, "OPEN", "admin", "Updating the status to notified");
        assertEquals(1, results.getCount().intValue());
        for (DtoOperationResult result : results.getResults()) {
            assert(result.getStatus().equals(DtoOperationResult.SUCCESS));
        }
        events = client.lookup(ids);
        for (DtoEvent event : events) {
            assertEquals("OPEN", event.getOperationStatus());
        }
        if (testStackTrace) {
            ids.clear();
            ids.add("26543");
            results = client.update(ids, "OPEN", "admin", "Updating the status to notified");
            for (DtoOperationResult result : results.getResults()) {
                assert (result.getStatus().equals(DtoOperationResult.FAILURE));
            }
        }
        client.delete(event1.getId().toString());
        assert null == client.lookup(event1.getId().toString());
    }

    @Test
    public void testPostEventsByConsolidation() throws Exception {
        if (serverDown) return;
        Date reportDate =  parseDate("2013-06-02 10:55:32.943");
        EventClient client = new EventClient(getDeploymentURL());
        DtoOperationResults results = client.post(buildEventsUpdate(1, reportDate, true, "MyError"));
        assertEquals(1, results.getCount().intValue());
        String entity = null;
        for (DtoOperationResult result : results.getResults()) {
            assert(result.getStatus().equals(DtoOperationResult.SUCCESS));
            entity = result.getEntity();
        }
        // assert data written
        DtoEvent event = client.lookup(entity);
        assertEquals(1, event.getMsgCount().intValue());
        assertEventWritten(event, reportDate, "MyError");
 
        // repeat should update same entity
        results = client.post(buildEventsUpdate(1, reportDate, true, "MyError"));
        assertEquals(1, results.getCount().intValue());
        DtoOperationResult result = results.getResults().get(0);
        assertEquals(entity, result.getEntity());
        assertEquals(DtoOperationResult.SUCCESS, result.getStatus());

        // assert data written second time
        event = client.lookup(entity);
        assertEquals(2, event.getMsgCount().intValue());
        assertEventWritten(event, reportDate, "MyError");

        // reset data for next test
        client.delete(entity);

        // test its deleted
        event = client.lookup(entity);
        assertNull(event);
    }

    @Test
    public void testAcknowledge() throws Exception {
        if (serverDown) return;
        simulateNagiosAcknowledge();
        EventClient client = new EventClient(getDeploymentURL());
        DtoAcknowledge ack = new DtoAcknowledge("NAGIOS", "localhost");
        ack.setAcknowledgedBy("admin");
        ack.setAcknowledgeComment("acknowledged ok by admin");
        DtoAcknowledgeList acks = new DtoAcknowledgeList();
        acks.add(ack);
        DtoOperationResults results = client.acknowledge(acks);
        assertEquals(1, results.getCount().intValue());
        for (DtoOperationResult result : results.getResults()) {
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
        EventClient client = new EventClient(getDeploymentURL());
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

//        unack = new DtoUnAcknowledge("VEMA", "STOR-datastore1");
//        unack.setService("summary.freeSpace");
//        unacks = new DtoUnAcknowledgeList();
//        unacks.add(unack);
//        results = client.unacknowledge(unacks);
//        assertEquals(1, results.getCount().intValue());
//        for (DtoOperationResult result : results.getResults()) {
//            assert(result.getStatus().equals(DtoOperationResult.FAILURE));
//        }
//        events = client.query("host = 'STOR-datastore1' and service = 'summary.freeSpace'");
//        assert events.size() > 0;
    }

    @Test
    public void testGetStateTransitions() {
        EventClient client = new EventClient(getDeploymentURL());

        List<DtoStateTransition> dtoStateTransitions = client.getStateTransitions("malbec", "http_alive", "05/10/2013", "05/20/2013");
        assertNotNull(dtoStateTransitions);
        assertEquals(4, dtoStateTransitions.size());
        assertEquals("malbec", dtoStateTransitions.get(0).getHostName());
        assertEquals("http_alive", dtoStateTransitions.get(0).getServiceName());
        assertNotNull(dtoStateTransitions.get(0).getFromStatus());
        assertEquals("OK", dtoStateTransitions.get(0).getFromStatus().getName());
        assertNotNull(dtoStateTransitions.get(0).getFromTransitionDate());
        assertNotNull(dtoStateTransitions.get(0).getToStatus());
        assertEquals("CRITICAL", dtoStateTransitions.get(0).getToStatus().getName());
        assertNotNull(dtoStateTransitions.get(0).getToTransitionDate());
        assertNotNull(dtoStateTransitions.get(0).getDurationInState());

        dtoStateTransitions = client.getStateTransitions("malbec", null, "05/10/2013", "05/20/2013");
        assertNotNull(dtoStateTransitions);
        assertEquals(2, dtoStateTransitions.size());
        assertEquals("malbec", dtoStateTransitions.get(0).getHostName());
        assertNull(dtoStateTransitions.get(0).getServiceName());
        assertNotNull(dtoStateTransitions.get(0).getFromStatus());
        assertEquals("UP", dtoStateTransitions.get(0).getFromStatus().getName());
        assertNotNull(dtoStateTransitions.get(0).getFromTransitionDate());
        assertNotNull(dtoStateTransitions.get(0).getToStatus());
        assertEquals("DOWN", dtoStateTransitions.get(0).getToStatus().getName());
        assertNotNull(dtoStateTransitions.get(0).getToTransitionDate());
        assertNotNull(dtoStateTransitions.get(0).getDurationInState());

        dtoStateTransitions = client.getStateTransitions("localhost", "local_cpu_httpd", "01/01/2010", "01/01/2025");
        assertNotNull(dtoStateTransitions);
        assertEquals(1, dtoStateTransitions.size());
        assertEquals("localhost", dtoStateTransitions.get(0).getHostName());
        assertEquals("local_cpu_httpd", dtoStateTransitions.get(0).getServiceName());
        assertNull(dtoStateTransitions.get(0).getFromStatus());
        assertNull(dtoStateTransitions.get(0).getFromTransitionDate());
        assertNotNull(dtoStateTransitions.get(0).getToStatus());
        assertEquals("PENDING", dtoStateTransitions.get(0).getToStatus().getName());
        assertNotNull(dtoStateTransitions.get(0).getToTransitionDate());
        assertNull(dtoStateTransitions.get(0).getDurationInState());

        DtoServiceKey malbecHttpAliveKey = new DtoServiceKey("http_alive", "malbec");
        DtoServiceKey malbecKey = new DtoServiceKey(null, "malbec");
        List<DtoServiceKey> hostAndServiceKeys = Arrays.asList(malbecHttpAliveKey, malbecKey);
        Map<DtoServiceKey, List<DtoStateTransition>> dtoStateTransitionListsMap =
                client.getStateTransitions(hostAndServiceKeys, "05/10/2013", "05/20/2013");
        assertNotNull(dtoStateTransitionListsMap);
        assertEquals(2, dtoStateTransitionListsMap.size());
        assertTrue(dtoStateTransitionListsMap.containsKey(malbecHttpAliveKey));
        assertEquals(4, dtoStateTransitionListsMap.get(malbecHttpAliveKey).size());
        assertTrue(dtoStateTransitionListsMap.containsKey(malbecKey));
        assertEquals(2, dtoStateTransitionListsMap.get(malbecKey).size());
    }

    @Test
    public void testBulkCreateEvents() {

        // create client
        EventClient client = new EventClient(getDeploymentURL());

        // create events
        int numEvents = 1000;
        DtoEventList events = buildEventsUpdate(numEvents, new Date(), false, "MyOtherError");
        long start = System.currentTimeMillis();
        DtoOperationResults results = client.post(events);
        long end = System.currentTimeMillis();
        assert results != null;
        assert results.getCount() == numEvents;
        assert results.getSuccessful() == numEvents;
        log.info(String.format("Elapsed time for testBulkCreateEvents create: %d", (end-start)));

        // get event ids
        Set<String> eventIds = new HashSet<String>();
        for (DtoOperationResult result : results.getResults()) {
            if (result.getLocation() != null) {
                eventIds.add(result.getEntity());
            }
        }
        if (!eventIds.isEmpty()) {
            // cleanup
            client.delete(new ArrayList<String>(eventIds));
        }
        assert eventIds.size() == results.getSuccessful();
    }

    @Test
    public void testConsolidatedBulkCreateEvents() {

        // create client
        EventClient client = new EventClient(getDeploymentURL());

        // create events
        int numEvents = 100;
        DtoEventList events = buildEventsUpdate(numEvents, new Date(), true, "MyOtherError");
        long start = System.currentTimeMillis();
        DtoOperationResults results = client.post(events);
        long end = System.currentTimeMillis();

        assert results != null;
        assert results.getCount() == numEvents;
        assert results.getSuccessful() == numEvents;
        log.info(String.format("Elapsed time for testConsolidatedBulkCreateEvents create: %d", (end-start)));

        // get event ids
        Set<String> eventIds = new HashSet<String>();
        for (DtoOperationResult result : results.getResults()) {
            if (result.getLocation() != null) {
                eventIds.add(result.getEntity());
            }
        }
        DtoEvent event = null;
        if (!eventIds.isEmpty()) {
            // get consolidated event
            event = client.lookup(eventIds.iterator().next());

            // cleanup
            client.delete(new ArrayList<String>(eventIds));
        }
        assert eventIds.size() == 1;
        assert event.getMsgCount().intValue() == numEvents;
    }

    @Test
    public void testDeleteEvents() {
        // test warning for missing delete
        EventClient client = new EventClient(getDeploymentURL());
        DtoOperationResults deleteResults = client.delete(Arrays.asList(new String[]{Integer.toString(Integer.MAX_VALUE)}));
        assert deleteResults != null;
        assert deleteResults.getWarning() == 1;
    }

    @Test
    public void testSetDynamicProperty() {
        // allocate clients
        PropertyTypeClient propertyTypeClient = new PropertyTypeClient(getDeploymentURL());
        EventClient eventClient = new EventClient(getDeploymentURL());

        // test using XML
        propertyTypeClient.setMediaType(MediaType.APPLICATION_XML_TYPE);
        eventClient.setMediaType(MediaType.APPLICATION_XML_TYPE);
        testSetDynamicProperty(propertyTypeClient, eventClient);

        // test using JSON
        propertyTypeClient.setMediaType(MediaType.APPLICATION_JSON_TYPE);
        eventClient.setMediaType(MediaType.APPLICATION_JSON_TYPE);
        testSetDynamicProperty(propertyTypeClient, eventClient);
    }

    private void testSetDynamicProperty(PropertyTypeClient propertyTypeClient, EventClient eventClient) {
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
        // create test event
        DtoEventList events = buildEventsUpdate(1, new Date(), true, "testSetDynamicProperty");
        results = eventClient.post(events);
        assert results != null;
        assert results.getSuccessful() == 1;
        String eventId = results.getResults().get(0).getEntity();
        assert eventId != null;
        // lookup test event
        DtoEvent dtoEvent = eventClient.lookup(eventId);
        assert dtoEvent != null;
        assert eventId.equals(dtoEvent.getId().toString());
        assert !dtoEvent.getProperties().containsKey("TEST_PROPERTY");
        // set dynamic property
        DtoEventPropertiesList dtoEventPropertiesList = new DtoEventPropertiesList();
        DtoEventProperties dtoEventProperties = new DtoEventProperties(Integer.parseInt(eventId));
        dtoEventProperties.getProperties().put("TEST_PROPERTY", "TEST_PROPERTY_VALUE");
        dtoEventPropertiesList.addEvent(dtoEventProperties);
        results = eventClient.update(dtoEventPropertiesList);
        assert results != null;
        assert results.getSuccessful() == 1;
        // validate dynamic property
        dtoEvent = eventClient.lookup(eventId);
        assert dtoEvent != null;
        assert eventId.equals(dtoEvent.getId().toString());
        assert "TEST_PROPERTY_VALUE".equals(dtoEvent.getProperties().get("TEST_PROPERTY"));
        // remove dynamic property
        dtoEventProperties.getProperties().put("TEST_PROPERTY", null);
        results = eventClient.update(dtoEventPropertiesList);
        assert results != null;
        assert results.getSuccessful() == 1;
        // validate dynamic property
        dtoEvent = eventClient.lookup(eventId);
        assert dtoEvent != null;
        assert eventId.equals(dtoEvent.getId().toString());
        assert !dtoEvent.getProperties().containsKey("TEST_PROPERTY");
        // set dynamic property
        dtoEventProperties.getProperties().put("TEST_PROPERTY", "TEST_PROPERTY_VALUE");
        results = eventClient.update(dtoEventPropertiesList);
        assert results != null;
        assert results.getSuccessful() == 1;
        // validate dynamic property
        dtoEvent = eventClient.lookup(eventId);
        assert dtoEvent != null;
        assert eventId.equals(dtoEvent.getId().toString());
        assert "TEST_PROPERTY_VALUE".equals(dtoEvent.getProperties().get("TEST_PROPERTY"));
        // remove dynamic property
        dtoEventProperties.getProperties().put("TEST_PROPERTY", "");
        results = eventClient.update(dtoEventPropertiesList);
        assert results != null;
        assert results.getSuccessful() == 1;
        // validate dynamic property
        dtoEvent = eventClient.lookup(eventId);
        assert dtoEvent != null;
        assert eventId.equals(dtoEvent.getId().toString());
        assert !dtoEvent.getProperties().containsKey("TEST_PROPERTY");
        // delete test event
        results = eventClient.delete(eventId);
        assert results != null;
        assert results.getSuccessful() == 1;
        // delete test property type
        results = propertyTypeClient.delete(Arrays.asList(new String[]{dtoPropertyType.getName()}));
        assert results != null;
        assert results.getSuccessful() == 1;
    }

    private void simulateNagiosAcknowledge() {
        EventClient client = new EventClient(getDeploymentURL());
        List<DtoEvent> events = client.query("appType = 'NAGIOS' and host = 'localhost' and servicestatusid is null");
        assert events.size() > 0;
        DtoEvent event = events.get(0);
        DtoEventProperties entity1 = new DtoEventProperties(event.getId());
        entity1.putProperty("AcknowledgeComment", "Nagios ACK pending");
        DtoEventPropertiesList entities = new DtoEventPropertiesList();
        entities.addEvent(entity1);
        client.update(entities);
    }

    private DtoEventList buildEventsUpdate(int n, Date reportDate, boolean enableConsolidation, String errorType) {
        DtoEventList events = new DtoEventList();
        for (int i = 0; (i < n); i++) {
            DtoEvent event = new DtoEvent();
            if (enableConsolidation) {
                event.setConsolidationName("NAGIOSEVENT");
            }
            event.setMonitorServer("localhost");
            event.setMonitorStatus("UP");
            event.setService("local_load");
            event.setAppType("NAGIOS");
            event.setDevice("127.0.0.1");
            event.setSeverity("SERIOUS");
            event.setTextMessage("This is a serious Nagios Message on Device 127.0.0.1 - " + i);
            event.setOperationStatus("OPEN");
            event.setHost("localhost");
            event.setReportDate(new Date(reportDate.getTime() - ((n - i - 1) * 1000L)));
            event.putProperty("Latency", new Double(125.31));
            event.putProperty("UpdatedBy", "UnitTester");
            event.putProperty("Comments", "This is a test.");
            event.setErrorType(errorType);
            event.setComponent("MySubComponent");
            events.add(event);
        }
        return events;
    }

    private DtoEvent storeTestEvent(EventClient client) {
        DtoEventList events = new DtoEventList();
        DtoEvent event = new DtoEvent();
        event.setConsolidationName("NAGIOSEVENT");
        event.setMonitorServer("localhost");
        event.setMonitorStatus("UP");
        event.setAppType("NAGIOS");
        event.setDevice("su-win-xp-2");
        event.setSeverity("SERIOUS");
        event.setTextMessage("This is a serious a test");
        event.setOperationStatus("OPEN");
        event.setHost("su-win-xp-2");
        event.setReportDate(parseDate("2013-06-02 10:55:32.943"));
        event.setComponent("MySubComponent");
        events.add(event);
        client.post(events);
        List<DtoEvent> eventList = client.query("host = 'su-win-xp-2' and monitorStatus = 'UP'");
        assert eventList.size() == 1;
        return eventList.get(0);
    }

    private void assertEventWritten(DtoEvent event, Date reportDate, String errorType) {
        assertEquals("UP", event.getMonitorStatus());
        assertEquals("local_load", event.getService());
        assertEquals("NAGIOS", event.getAppType());
        assertEquals("127.0.0.1", event.getDevice());
        assertEquals("SERIOUS", event.getSeverity());
        assertEquals("This is a serious Nagios Message on Device 127.0.0.1 - 0", event.getTextMessage());
        assertEquals("OPEN", event.getOperationStatus());
        assertEquals("localhost", event.getHost());
        assertDatesEqual(reportDate, event.getFirstInsertDate());
        assertEquals("125.31", event.getProperty("Latency"));
        assertEquals("UnitTester", event.getProperty("UpdatedBy"));
        assertEquals("This is a test.", event.getProperty("Comments"));
        assertEquals(errorType, event.getProperty("ErrorType"));
        assertEquals("MySubComponent", event.getProperty("SubComponent"));
    }


}
