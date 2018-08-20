package org.groundwork.rs.client;

import org.groundwork.rs.dto.DtoDepthType;
import org.groundwork.rs.dto.DtoDevice;
import org.groundwork.rs.dto.DtoDeviceList;
import org.groundwork.rs.dto.DtoEvent;
import org.groundwork.rs.dto.DtoHost;
import org.groundwork.rs.dto.DtoHostGroup;
import org.groundwork.rs.dto.DtoHostIdentity;
import org.groundwork.rs.dto.DtoHostIdentityList;
import org.groundwork.rs.dto.DtoHostList;
import org.groundwork.rs.dto.DtoName;
import org.groundwork.rs.dto.DtoOperationResult;
import org.groundwork.rs.dto.DtoOperationResults;
import org.groundwork.rs.dto.DtoPropertyDataType;
import org.groundwork.rs.dto.DtoPropertyType;
import org.groundwork.rs.dto.DtoPropertyTypeList;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class HostClientTest extends AbstractHostTest  {


    @Test
    public void testHostsTokens() throws Exception {
        if (serverDown) return;
        HostClient client = new HostClient(getDeploymentURL());
        DtoHost host = client.lookup("localhost");
        List<DtoHost> hosts = client.list();
        hosts = client.list();
        hosts = client.list();
        hosts = client.list();
        client = new HostClient(getDeploymentURL());
        host = client.lookup("localhost");
        host = client.lookup("localhost");
        HostGroupClient client2 = new HostGroupClient(getDeploymentURL());
        List<DtoHostGroup> hostGroups = client2.list(DtoDepthType.Shallow, -1, -1);
        hostGroups = client2.list(DtoDepthType.Shallow, -1, -1);

    }

    @Test
    public void testHostsCount() throws Exception {
        if (serverDown) return;
        HostClient client = new HostClient(getDeploymentURL());
        List<DtoHost> hosts = client.list();
        List<String> names = new ArrayList<String>();
        for (DtoHost host : hosts) {
            names.add(host.getHostName());
        }
        assertEquals(21, hosts.size());
        hosts = client.list(DtoDepthType.Shallow, 5, 7);
        assertEquals(7, hosts.size());

        // wait to ensure propagation and test autocomplete
        Thread.sleep(250);
        List<DtoName> suggestions = client.autocomplete("qa-");
        assertNotNull(suggestions);
        assertEquals(4, suggestions.size());
        assertTrue(suggestions.containsAll(Arrays.asList(new DtoName[]{
                new DtoName("qa-load-xp-1"),
                new DtoName("qa-rh6-64-2"),
                new DtoName("qa-sles-11-64"),
                new DtoName("qa-sles-11-64-2")})));
        suggestions = client.autocomplete("zzz");
        assertNotNull(suggestions);
        assertEquals(0, suggestions.size());
        suggestions = client.autocomplete("qa-", 2);
        assertNotNull(suggestions);
        assertEquals(2, suggestions.size());
        assertTrue(suggestions.containsAll(Arrays.asList(new DtoName[]{
                new DtoName("qa-load-xp-1"),
                new DtoName("qa-rh6-64-2")})));
    }

    @Test
    public void testHostLookupShallow() throws Exception {
        if (serverDown) return;
        HostClient client = new HostClient(getDeploymentURL());
        DtoHost host = client.lookup("localhost");
        assertNotNull(host);
        assertEquals("localhost", host.getHostName());
        assertEquals("127.0.0.1", host.getDeviceIdentification());
        assertEquals("UP", host.getMonitorStatus());
        assertEquals("NAGIOS", host.getAppType());
        String serviceAvailability = host.getServiceAvailability();
        assertNotNull(serviceAvailability);
        assertTrue(Double.parseDouble(serviceAvailability) > 90.0);
        assertEquals("UNSCHEDULED DOWN", host.getBubbleUpStatus());
        assertEquals(19, host.getProperties().size());
        assertEquals("10", host.getProperty("MaxAttempts"));
        assertEquals("false", host.getProperty("isFlapDetectionEnabled"));
        assertTrue(host.getProperty("PerformanceData").startsWith("rta="));
        assertTrue(host.getProperty("LastPluginOutput").startsWith("OK -"));
    }

    @Test
    public void testNegativeBadPathParam() throws Exception {
        if (serverDown) return;
        HostClient client = new HostClient(getDeploymentURL());
        DtoHost host = client.lookup("hostName = 'qwe23'");
        assertNull(host);
    }

    @Test
    public void testHostLookupFull() throws Exception {
        if (serverDown) return;
        HostClient client = new HostClient(getDeploymentURL());
        DtoHost host = client.lookup("localhost", DtoDepthType.Full);
        assertNotNull(host);
        assertEquals("localhost", host.getHostName());
        assertEquals("127.0.0.1", host.getDeviceIdentification());
        assertEquals(2, host.getHostGroups().size());
        assertEquals(7, host.getStatistics().size());
        assertEquals(21, host.getServices().size());
    }

    @Test
    public void testQueryByLike() throws Exception {
        if (serverDown) return;
        HostClient client = new HostClient(getDeploymentURL());
        List<DtoHost> hosts = client.query("property.LastPluginOutput like 'OK%'");
        assertEquals(15, hosts.size());
        for (DtoHost host : hosts) {
            assertTrue(host.getProperty("LastPluginOutput").startsWith("OK"));
        }
    }

    @Test
    public void testExecutionTimeRangeQuery() throws Exception {
        if (serverDown) return;
        HostClient client = new HostClient(getDeploymentURL());
        List<DtoHost> hosts = client.query("(property.ExecutionTime between 10 and 3500 and (monitorStatus <> 'UP')) order by property.ExecutionTime");
        assertEquals(1, hosts.size());
        for (DtoHost host : hosts) {
            assertEquals("3005", host.getProperty("ExecutionTime"));
            assertEquals("UNSCHEDULED DOWN", host.getMonitorStatus());
        }
    }

    @Test
    public void testDateTimeFunctions() throws Exception {
        if (serverDown) return;
        HostClient client = new HostClient(getDeploymentURL());
        List<DtoHost> hosts = client.query("day(lastCheckTime) = 22 and month(lastCheckTime) = 5 and minute(lastCheckTime) > 43 order by lastCheckTime");
        assertEquals(8, hosts.size());
        for (DtoHost host : hosts) {
            Calendar cal = new GregorianCalendar();
            cal.setTime(host.getLastCheckTime());
            assert(cal.get(Calendar.MINUTE) > 43);
            assert(cal.get(Calendar.DAY_OF_MONTH) == 22);
            assert(cal.get(Calendar.MONTH) == Calendar.MAY);
        }
    }

    @Test
    public void testLatencyRange() throws Exception {
        if (serverDown) return;
        HostClient client = new HostClient(getDeploymentURL());
        List<DtoHost> hosts = client.query("(property.ExecutionTime < 10 and property.Latency between 800 and 900) order by property.Latency");
        assertEquals(3, hosts.size());
        for (DtoHost host : hosts) {
            Double execTime = host.getPropertyDouble("ExecutionTime");
            Double latency = host.getPropertyDouble("Latency");
            assert(execTime < 10.0);
            assert(latency >= 800.0 && latency <= 900.0);
        }
    }

    @Test
    public void testCreateAndSingleDeleteHosts() throws Exception {
        if (serverDown) return;

        DtoHostList hostUpdates = buildHostUpdate("-");
        DtoOperationResults results = executePost(hostUpdates);
        assertEquals(2, results.getCount().intValue());
        String hostName = null;
        for (DtoOperationResult result : results.getResults()) {
            assert(result.getStatus().equals(DtoOperationResult.SUCCESS));
        }

        DtoHost host = retrieveSingleHost("host-100", true);
        assertNotNull(host);
        assertHostWritten(host, "-");

        host = retrieveSingleHost("host-101", true);
        assertNotNull(host);
        assertHostWritten(host, "-");


        // reset data for next test
        executeDelete("host-100");
        executeDelete("host-101");

        // test its deleted
        retrieveSingleHost("host-100", false);
        retrieveSingleHost("host-101", false);

        // test warning for missing delete
        HostClient client = new HostClient(getDeploymentURL());
        DtoOperationResults deleteResults = client.delete(Arrays.asList(new String[]{"NotAHost"}));
        assertNotNull(deleteResults);
        assertEquals(new Integer(1), deleteResults.getWarning());
    }

    @Test
    public void testHostWithSpaces() throws Exception {
        if (serverDown) return;

        DtoHostList hostUpdates = buildHostUpdate(" ");
        DtoOperationResults results = executePost(hostUpdates);
        assertEquals(2, results.getCount().intValue());
        String hostName = null;
        for (DtoOperationResult result : results.getResults()) {
            assert(result.getStatus().equals(DtoOperationResult.SUCCESS));
        }

        DtoHost host = retrieveSingleHost("host 100", true);
        assertNotNull(host);
        assertHostWritten(host, " ");

        host = retrieveSingleHost("host 101", true);
        assertNotNull(host);
        assertHostWritten(host, " ");


        // reset data for next test
        executeDelete("host 100");
        executeDelete("host 101");

        // test its deleted
        retrieveSingleHost("host 100", false);
        retrieveSingleHost("host 101", false);
    }

    @Test
    public void testHostWithPlusSign() throws Exception {
        if (serverDown) return;

        DtoHostList hostUpdates = buildHostUpdate("+");
        DtoOperationResults results = executePost(hostUpdates);
        assertEquals(2, results.getCount().intValue());
        String hostName = null;
        for (DtoOperationResult result : results.getResults()) {
            assert(result.getStatus().equals(DtoOperationResult.SUCCESS));
        }

        DtoHost host = retrieveSingleHost("host+100", true);
        assertNotNull(host);
        assertHostWritten(host, "+");

        host = retrieveSingleHost("host+101", true);
        assertNotNull(host);
        assertHostWritten(host, "+");


        // reset data for next test
        executeDelete("host+100");
        executeDelete("host+101");

        // test its deleted
        retrieveSingleHost("host+100", false);
        retrieveSingleHost("host+101", false);
    }

    @Test
    public void testCreateAndSingleDeleteHostsWithDto() throws Exception {
        if (serverDown) return;

        DtoHostList hostUpdates = buildHostUpdate("-");

        /**
         * GWMON-13138 - asserting that HostGroups are not processed on this API
         */
        boolean testHostGroups = false;
        if (testHostGroups) {
            DtoHostGroup group = new DtoHostGroup();
            group.setName("group1");
            List<DtoHostGroup> groups = new ArrayList<>();
            groups.add(group);
            hostUpdates.getHosts().get(0).setHostGroups(groups);
        }
        DtoOperationResults results = executePost(hostUpdates);
        assertEquals(2, results.getCount().intValue());
        String hostName = null;
        for (DtoOperationResult result : results.getResults()) {
            assert(result.getStatus().equals(DtoOperationResult.SUCCESS));
        }

        if (testHostGroups) {
            // GWMON-13138, submit again for update
            results = executePost(hostUpdates);
            assertEquals(2, results.getCount().intValue());
        }

        DtoHost host = retrieveSingleHost("host-100", true);
        assertNotNull(host);
        assertHostWritten(host, "-");

        host = retrieveSingleHost("host-101", true);
        assertNotNull(host);
        assertHostWritten(host, "-");


        // reset data for next test
        executeDeleteWithDto("host-100,host-101");

        // test its deleted
        retrieveSingleHost("host-100", false);
        retrieveSingleHost("host-101", false);
    }

    @Test
    public void testCreateAndMultiDeleteHosts() throws Exception {
        if (serverDown) return;

        DtoHostList hostUpdates = buildHostUpdate("-");
        DtoOperationResults results = executePost(hostUpdates);
        assertEquals(2, results.getCount().intValue());
        for (DtoOperationResult result : results.getResults()) {
            assert(result.getStatus().equals(DtoOperationResult.SUCCESS));
        }

        DtoHost host = retrieveSingleHost("host-100", true);
        assertNotNull(host);
        assertHostWritten(host, "-");

        host = retrieveSingleHost("host-101", true);
        assertNotNull(host);
        assertHostWritten(host, "-");

        // test lookup by agent id
        host = retrieveHostByAgent(AGENT_84);
        assertEquals("host-100", host.getHostName());
        host = retrieveHostByAgent(AGENT_85);
        assertEquals("host-101", host.getHostName());

        // reset data for next test
        executeDelete("host-100,host-101");
        executeDeviceDelete("192.168.5.50,192.168.5.51");

        // test its deleted
        retrieveSingleHost("host-100", false);
        retrieveSingleHost("host-101", false);
    }

    @Test
    public void testDeleteHostWithLastDevice() throws Exception {
        if (serverDown) return;
        // execute updates with two hosts and shared device
        DtoHostList hostUpdates = buildHostDeviceUpdates();
        DtoOperationResults results = executePost(hostUpdates);
        // assert two hosts result status success
        assertEquals(2, results.getCount().intValue());
        for (DtoOperationResult result : results.getResults()) {
            assert(result.getStatus().equals(DtoOperationResult.SUCCESS));
        }
        // assert device was written
        DeviceClient deviceClient = new DeviceClient(getDeploymentURL());
        DtoDevice device = deviceClient.lookup(DEVICE_IDENTIFICATION);
        assertNotNull(device);
        assertEquals("Device-198", device.getDisplayName());
        // delete one host
        HostClient hostClient = new HostClient(getDeploymentURL());
        List<String> hostNames = new ArrayList<String>();
        hostNames.add(HOST_200);
        hostClient.delete(hostNames);
        // assert device still there
        device = deviceClient.lookup(DEVICE_IDENTIFICATION);
        assertNotNull(device);
        // assert host was deleted
        DtoHost host = hostClient.lookup(HOST_200);
        assertNull(host);
        // delete second host
        hostNames = new ArrayList<String>();
        hostNames.add(HOST_201);
        hostClient.delete(hostNames);

        // assert host was deleted
        host = hostClient.lookup(HOST_201);
        assertNull(host);

        // assert device was removed when zero references
        device = deviceClient.lookup(DEVICE_IDENTIFICATION);
        assertNull(device);
    }

    // this test is dependent on Vermont2 inventory of VmWare hosts
    //@Test
    public void testDashboardQueries() throws Exception {
        if (serverDown) return;
        HostClient client = new HostClient(getDeploymentURL());
        List<DtoHost> hosts = client.query("name like 'NET-%'");
        assertEquals(6, hosts.size());
        for (DtoHost host : hosts) {
            assertTrue(host.getHostName().startsWith("NET-"));
        }
        hosts = client.query("name like 'STOR-%'");
        assertEquals(13, hosts.size());
        for (DtoHost host : hosts) {
            assertTrue(host.getHostName().startsWith("STOR-"));
        }
        HostGroupClient hgClient = new HostGroupClient(getDeploymentURL());
        List<DtoHostGroup> groups = hgClient.query("name like 'ESX%' or name like 'VSS%'");
        assertEquals(6, groups.size());
        for (DtoHostGroup group : groups) {
            DtoHost host = client.lookup(group.getName().substring(4));
            assert host != null;
        }
    }

//    ClientRequest request = createClientRequest("http://localhost:8280/basic-auth-test");
//    ClientResponse<String> response1 = request.get(String.class);
//    String value = response1.getEntity(String.class);
//    System.out.println("value = " + value);

    public static final String HOST_NAME = "docker1-host-1";
    public static final String HOST_DESCRIPTION = "docker1-host-1-desc";
    public static final String NEW_HOST_NAME = "docker2-host-1";
    public static final String NEW_HOST_DESCRIPTION = "docker2-host-1-desc";
    public static final String DEVICE_NAME = HOST_NAME;
    public static final String NEW_DEVICE_NAME = NEW_HOST_NAME;
    public static final String DUPE = "docker2-dupe";

    @Test
    public void testRenameHost() throws Exception {

        HostClient hostClient = new HostClient(getDeploymentURL());
        DeviceClient deviceClient = new DeviceClient(getDeploymentURL());

        try {
            DtoHostList hosts = new DtoHostList();
            DtoHost host = new DtoHost();
            host.setHostName(HOST_NAME);
            host.setDescription(HOST_DESCRIPTION);
            host.setDeviceIdentification(DEVICE_NAME);
            host.setDeviceDisplayName(DEVICE_NAME);
            hosts.add(host);

            hostClient.post(hosts);

            DtoHost renamedHost = hostClient.rename(HOST_NAME, NEW_HOST_NAME, NEW_HOST_DESCRIPTION, NEW_DEVICE_NAME);
            assert renamedHost != null;
            assert renamedHost.getHostName().equals(NEW_HOST_NAME);
            assert renamedHost.getDescription().equals(NEW_HOST_DESCRIPTION);
            assert renamedHost.getDeviceIdentification().equals(NEW_DEVICE_NAME);
            assert renamedHost.getDeviceDisplayName().equals(NEW_DEVICE_NAME);

            assert hostClient.lookup(HOST_NAME) == null;

            DtoHost host2 = hostClient.lookup(NEW_HOST_NAME);
            assert host2 != null;
            assert host2.getHostName().equals(NEW_HOST_NAME);
            assert host2.getDescription().equals(NEW_HOST_DESCRIPTION);
            assert host2.getDeviceIdentification().equals(NEW_DEVICE_NAME);
            assert host2.getDeviceDisplayName().equals(NEW_DEVICE_NAME);

            assert deviceClient.lookup(DEVICE_NAME) == null;

            DtoDevice device = deviceClient.lookup(NEW_DEVICE_NAME);
            assert device != null;
            assert device.getIdentification().equals(NEW_DEVICE_NAME);
            assert device.getDisplayName().equals(NEW_DEVICE_NAME);
        } finally {
            deviceClient.delete(NEW_DEVICE_NAME);
            assert hostClient.lookup(NEW_HOST_NAME) == null;
            assert deviceClient.lookup(NEW_DEVICE_NAME) == null;
        }

    }

    @Test
    public void testRenameHostDupeKey() throws Exception {

        HostClient hostClient = new HostClient(getDeploymentURL());
        DeviceClient deviceClient = new DeviceClient(getDeploymentURL());

        try {
            DtoHostList hosts = new DtoHostList();
            DtoHost host = new DtoHost();
            host.setHostName(HOST_NAME);
            host.setDescription(HOST_DESCRIPTION);
            host.setDeviceIdentification(DEVICE_NAME);
            host.setDeviceDisplayName(DEVICE_NAME);
            hosts.add(host);
            DtoHost dupe = new DtoHost();
            dupe.setHostName(DUPE);
            dupe.setDescription(DUPE);
            dupe.setDeviceIdentification(DUPE);
            dupe.setDeviceDisplayName(DUPE);
            hosts.add(dupe);

            hostClient.post(hosts);

            try {
                DtoHost dupedHost = hostClient.rename(HOST_NAME, DUPE, NEW_HOST_DESCRIPTION, DUPE);
            }
            catch (Exception e) {
                assert e.getMessage().contains("400");
            }
        } finally {
            deviceClient.delete(DEVICE_NAME);
            deviceClient.delete(DUPE);
            assert deviceClient.lookup(DEVICE_NAME) == null;
            assert deviceClient.lookup(DUPE) == null;
        }

    }

    @Test
    public void testBulkCreateHosts() throws Exception {
        if (serverDown) return;

        // create clients
        DeviceClient deviceClient = new DeviceClient(getDeploymentURL());
        HostClient client = new HostClient(getDeploymentURL());

        // create hosts
        int numHosts = 1000;
        Set<String> deviceIdentifications = new HashSet<String>();
        List<DtoHost> dtoHosts = new ArrayList<DtoHost>();
        for (int host = 0; (host < numHosts); host++) {
            DtoHost dtoHost = new DtoHost();
            dtoHost.setHostName("bulk-host-" + host);
            dtoHost.setMonitorStatus("PENDING");
            String deviceIdentification = "bulk-device-" + (host % numHosts/2);
            deviceIdentifications.add(deviceIdentification);
            dtoHost.setDeviceIdentification(deviceIdentification);
            dtoHost.setAppType("SEL");
            dtoHost.setAgentId(AGENT_84);
            dtoHosts.add(dtoHost);
        }
        long start = System.currentTimeMillis();
        DtoOperationResults results = client.post(new DtoHostList(dtoHosts));
        long end = System.currentTimeMillis();
        assert results != null;
        assert results.getCount() == numHosts;
        assert results.getSuccessful() == numHosts;
        log.info(String.format("Elapsed time for testBulkCreateHosts create: %d", (end-start)));

        // cleanup
        if (!deviceIdentifications.isEmpty()) {
            DtoDeviceList deleteDevices = new DtoDeviceList();
            for (String ident : deviceIdentifications) {
                DtoDevice device = new DtoDevice();
                device.setIdentification(ident);
                deleteDevices.add(device);
            }
            deviceClient.delete(deleteDevices);
        }

        // create hosts with duplicates
        deviceIdentifications.clear();
        dtoHosts.clear();
        {
            DtoHost dtoHost = new DtoHost();
            dtoHost.setHostName("bulk-host");
            dtoHost.setMonitorStatus("PENDING");
            String deviceIdentification = "bulk-device";
            deviceIdentifications.add(deviceIdentification);
            dtoHost.setDeviceIdentification(deviceIdentification);
            dtoHost.setAppType("SEL");
            dtoHost.setAgentId(AGENT_84);
            dtoHosts.add(dtoHost);
            dtoHost = new DtoHost();
            dtoHost.setHostName("bulk-host");
            dtoHost.setMonitorStatus("OK");
            dtoHost.setDeviceIdentification(deviceIdentification);
            dtoHost.setAppType("SEL");
            dtoHost.setAgentId(AGENT_84);
            dtoHosts.add(dtoHost);
        }
        results = client.post(new DtoHostList(dtoHosts));
        assert results != null;
        assert results.getCount() == 2;
        assert results.getSuccessful() == 2;

        // cleanup
        if (!deviceIdentifications.isEmpty()) {
            deviceClient.delete(new ArrayList<String>(deviceIdentifications));
        }

        // create async hosts
        numHosts = 10;
        deviceIdentifications.clear();
        dtoHosts.clear();
        for (int host = 0; (host < numHosts); host++) {
            DtoHost dtoHost = new DtoHost();
            dtoHost.setHostName("bulk-async-host-" + host);
            dtoHost.setMonitorStatus("PENDING");
            String deviceIdentification = "bulk-async-device-" + (host % numHosts/2);
            deviceIdentifications.add(deviceIdentification);
            dtoHost.setDeviceIdentification(deviceIdentification);
            dtoHost.setAppType("SEL");
            dtoHost.setAgentId(AGENT_84);
            dtoHosts.add(dtoHost);
        }
        results = client.postAsync(new DtoHostList(dtoHosts));
        assert results != null;
        assert results.getCount() == 1;
        assert results.getSuccessful() == 1;

        // wait and verify
        boolean verified = false;
        for (int wait = 0; ((wait < 20) && !verified); wait++) {
            Thread.sleep(500);
            verified = true;
            for (String deviceIdentification : deviceIdentifications) {
                verified = verified && (deviceClient.lookup(deviceIdentification) != null);
            }
        }
        assert verified;

        // cleanup
        if (!deviceIdentifications.isEmpty()) {
            deviceClient.delete(new ArrayList<String>(deviceIdentifications));
        }
    }

    @Test
    public void testSetDynamicProperty() {
        // allocate clients
        PropertyTypeClient propertyTypeClient = new PropertyTypeClient(getDeploymentURL());
        HostClient hostClient = new HostClient(getDeploymentURL());

        // test using XML
        propertyTypeClient.setMediaType(MediaType.APPLICATION_XML_TYPE);
        hostClient.setMediaType(MediaType.APPLICATION_XML_TYPE);
        testSetDynamicProperty(propertyTypeClient, hostClient);

        // test using JSON
        propertyTypeClient.setMediaType(MediaType.APPLICATION_JSON_TYPE);
        hostClient.setMediaType(MediaType.APPLICATION_JSON_TYPE);
        testSetDynamicProperty(propertyTypeClient, hostClient);
    }

    private void testSetDynamicProperty(PropertyTypeClient propertyTypeClient, HostClient hostClient) {
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
        // lookup test host
        DtoHost dtoHost = hostClient.lookup("localhost");
        assert dtoHost != null;
        assert !dtoHost.getProperties().containsKey("TEST_PROPERTY");
        // set dynamic property
        DtoHostList postDtoHostList = new DtoHostList();
        DtoHost postDtoHost = new DtoHost();
        postDtoHost.setHostName("localhost");
        postDtoHost.getProperties().put("TEST_PROPERTY", "TEST_PROPERTY_VALUE");
        postDtoHostList.add(postDtoHost);
        results = hostClient.post(postDtoHostList);
        assert results != null;
        assert results.getSuccessful() == 1;
        // validate dynamic property
        dtoHost = hostClient.lookup("localhost");
        assert dtoHost != null;
        assert "localhost".equals(dtoHost.getHostName());
        assert "TEST_PROPERTY_VALUE".equals(dtoHost.getProperties().get("TEST_PROPERTY"));
        // remove dynamic property
        postDtoHost.getProperties().put("TEST_PROPERTY", null);
        results = hostClient.post(postDtoHostList);
        assert results != null;
        assert results.getSuccessful() == 1;
        // validate dynamic property
        dtoHost = hostClient.lookup("localhost");
        assert dtoHost != null;
        assert "localhost".equals(dtoHost.getHostName());
        assert !dtoHost.getProperties().containsKey("TEST_PROPERTY");
        // set dynamic property
        postDtoHost.getProperties().put("TEST_PROPERTY", "TEST_PROPERTY_VALUE");
        results = hostClient.post(postDtoHostList);
        assert results != null;
        assert results.getSuccessful() == 1;
        // validate dynamic property
        dtoHost = hostClient.lookup("localhost");
        assert dtoHost != null;
        assert "localhost".equals(dtoHost.getHostName());
        assert "TEST_PROPERTY_VALUE".equals(dtoHost.getProperties().get("TEST_PROPERTY"));
        // remove dynamic property
        postDtoHost.getProperties().put("TEST_PROPERTY", "");
        results = hostClient.post(postDtoHostList);
        assert results != null;
        assert results.getSuccessful() == 1;
        // validate dynamic property
        dtoHost = hostClient.lookup("localhost");
        assert dtoHost != null;
        assert "localhost".equals(dtoHost.getHostName());
        assert !dtoHost.getProperties().containsKey("TEST_PROPERTY");
        // delete test property type
        results = propertyTypeClient.delete(Arrays.asList(new String[]{dtoPropertyType.getName()}));
        assert results != null;
        assert results.getSuccessful() == 1;
    }

    @Test
    public void testHostMerge() {
        if (serverDown) return;

        // create clients
        DeviceClient deviceClient = new DeviceClient(getDeploymentURL());
        EventClient eventClient = new EventClient(getDeploymentURL());
        HostClient client = new HostClient(getDeploymentURL());

        // create merge host
        DtoHostList dtoHosts = new DtoHostList();
        DtoHost dtoHost = new DtoHost();
        dtoHost.setHostName("test-merge-host");
        dtoHost.setMonitorStatus("PENDING");
        dtoHost.setDeviceIdentification("test-merge-device");
        dtoHost.setAppType("SEL");
        dtoHost.setAgentId(AGENT_84);
        dtoHosts.add(dtoHost);
        DtoOperationResults results = client.post(dtoHosts);
        assert results != null;
        assert results.getSuccessful() == 1;

        // update merge host, (merge allowed)
        dtoHost.setHostName("TEST-MERGE-HOST");
        dtoHost.setMonitorStatus("UP");
        results = client.post(dtoHosts, true);
        assert results != null;
        assert results.getSuccessful() == 1;

        // attempt blocked update merge host, (merge disallowed)
        dtoHost.setMonitorStatus("UNSCHEDULED DOWN");
        results = client.post(dtoHosts, false);
        assert results != null;
        assert results.getWarning() == 1;

        // validate update and events
        dtoHost = client.lookup("Test-Merge-Host");
        assert dtoHost != null;
        assert dtoHost.getHostName().equals("test-merge-host");
        assert dtoHost.getMonitorStatus().equals("UP");
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

    @Test
    public void testHostIdentityLinkage() {
        if (serverDown) return;

        // create clients
        DeviceClient deviceClient = new DeviceClient(getDeploymentURL());
        HostIdentityClient hostIdentityClient = new HostIdentityClient(getDeploymentURL());
        HostClient client = new HostClient(getDeploymentURL());

        // create host identity
        DtoHostIdentityList dtoHostIdentities = new DtoHostIdentityList();
        DtoHostIdentity dtoHostIdentity = new DtoHostIdentity("test-host-identity-link",
                Arrays.asList(new String[]{"test-host-identity-link-alias"}));
        dtoHostIdentities.add(dtoHostIdentity);
        DtoOperationResults results = hostIdentityClient.post(dtoHostIdentities);
        assert results != null;
        assert results.getSuccessful() == 1;

        // validate host identity
        dtoHostIdentity = hostIdentityClient.lookup("test-host-identity-link");
        assert dtoHostIdentity != null;
        assert dtoHostIdentity.getHostName().equals("test-host-identity-link");
        assert dtoHostIdentity.getHost() == false;

        // create test host using alias for host name
        DtoHostList dtoHosts = new DtoHostList();
        DtoHost dtoHost = new DtoHost();
        dtoHost.setHostName("Test-Host-Identity-Link-Alias");
        dtoHost.setMonitorStatus("PENDING");
        dtoHost.setDeviceIdentification("test-host-identity-link-device");
        dtoHost.setAppType("SEL");
        dtoHost.setAgentId(AGENT_84);
        dtoHosts.add(dtoHost);
        results = client.post(dtoHosts);
        assert results != null;
        assert results.getSuccessful() == 1;

        // validate host and host identity
        dtoHost = client.lookup("TEST-HOST-IDENTITY-LINK-ALIAS");
        assert dtoHost != null;
        assert dtoHost.getHostName().equals("test-host-identity-link");
        dtoHostIdentity = hostIdentityClient.lookup("test-host-identity-link");
        assert dtoHostIdentity != null;
        assert dtoHostIdentity.getHostName().equals("test-host-identity-link");
        assert dtoHostIdentity.getHost() == true;

        // delete test host
        results = deviceClient.delete("test-host-identity-link-device");
        assert results != null;
        assert results.getSuccessful() == 1;

        // validate host identity
        dtoHostIdentity = hostIdentityClient.lookup("test-host-identity-link");
        assert dtoHostIdentity != null;
        assert dtoHostIdentity.getHostName().equals("test-host-identity-link");
        assert dtoHostIdentity.getHost() == false;

        // cleanup
        results = hostIdentityClient.delete("test-host-identity-link");
        assert results != null;
        assert results.getSuccessful() == 1;
    }

    @Test
    public void testBigDelete() throws Exception {
        String deletes = "SQLClust01,MSWDEST01V (10.10.176.77),_MPLISCANFTP01V (10.10.110.24),MSWWCLM02V (10.10.70.171),MTWAUCARE01V (10.10.172.182),MPWDTHC01V (Remove 7%2f3),MDWDINSP01V,MSWABCBSAR01V (10.10.172.x),mpldcst01v (10.10.176.242),mpldpst01v (10.10.CLONE),MPWIKADC04V (10.10.208.240),mpldest01v (10.10.176.84),MTWWUCARE02V,MSWWUPMC01V_old,MSWDUPMC01V (10.10.176.38) (09-01-15),MPWWFMC01V,MPWIKADC6V,MPWDUPMC01V (10.10.180.40),MSLDEST01V,MSWDC03,MPWDCIKA130-02V,IKACLAIMTS40 - Import,mpldpst01v (10.10.121.51 & 10.10.121.),MPWDCTRL01V (10.10.8.201),MPWIKADC130-02V,MPWIBCBSM03V(temp file server),MTWAUPMC01V,MSLDEST04V,MQWWINSP01V (10.10.208.10),Win2K8_Template,MPWAFMC01V (10.10.180.31),mpldpst01v (10.10.184.240) (to change 10.10.121.51),MSLDCST01V,MTWDBCBAR09V,MQWWINSP03V (10.10.208.10),MTWDBCBAR08V (10.10.176.87 - Gateway and Medicare DB),mpldpst01v (10.10.184.240),MPWDBCMA01V,MSWAFMC01V (10.10.172.81),MDLAINSP01V,MSWWUPMC01v,XtremeIO_XMS_30_old (still in use)_6727,MPWIBCBSM01V (10.10.90.25),MTWASTG01V,VIRTSC02L,mpldpst01v (10.10.176.240),IPRDDC699 (Adam),MSWDEST01V,XtremIO_3.0.1_XMS,MPWIKADC03V (10.10.204.240),MPWDEST01V,MPWDBCBSM03V,msldest01v,MSWWTHC01V,MDWAINSP01V,MPWWGHC01V.ikaprod120.com (10.10.120.182),MPWIKADC65-02V,mpldodi01v (10.10.176.243),wickp_test_6727,SQL_2014,MPWWUPMC03V,MPLDEST03V (use this to create new template),MPWDCIKA130-01V,IKACLAIMTS40 (10.10.70.40),Win2K12_Template2,IKACLAIMTS40 (10.10.70.106),MSWDUPMC01V (10.10.176.38),MTWWCLM01V,MSWWUPMC02V,MSWWUPMC01V (10.10.172.174),MTWAUPMC01V (10.10.172.203),MTLDCLMPST01V (10.10.30.68),SQLClusterMgmt,MPLDEST03V,MTWAUPMC01V (10.10.172.147),Win2K12_Template,MQWWINSP02V (10.10.208.11),MTWWUCARE01V,MQWDINSP01V (10.10.208.12),wickp_lin3_zero,MPWDTHC01V,MSWABCBSAR01V (10.10.172.x - IBM MQ),_MPLISCANFTP01V,MTWABCBSAR01V,MTLDUPMC01V (10.10.46.219),MSWD02V,MPWIKADC30-04V (10.10.30.x),MPWIBCBSM01V,MSLDBCBAR03V,MRLDBCBAR02V (10.10.176.112 - Claims DB),MPWDBCBAR03V (10.10.180.170 - ICES DB & Web),MPWControlM-01V (10.10.8.201),SQLCust01 (10.10.176.70),MTLDFMC02V (10.10.176.58 DB - DWH),MTWWUPMC01V,MSWWCLM01V (10.10.70.186),XtremIO_3.0.1_XMS_old (still in use),MPWWTHC01V,MSWAEST01V,MSWD01V  (10.10.176.161),MPLDCST01V,MPWDUPMC05V,MTLDTEST02V (10.10.30.71),MSWWUPMC01V (10.10.172.142),MTWVARONIS01V (10.10.176.60),MSWDUPMC02V,MTLDBCBAR03V (10.10.176.x - 3 Adjudication DB server),MTWAUCARE01V,IKA_XIO_8143_old (still in use),MPWIKADC130-03V,MPWDFMC02V,MSWWUPMC03V (10.10.30.142),MPWPRHUPMC01V,MPWWAFREEDOMV01,MTWACLM01V,MTWICESUPMC01V,MSWWUPMC03V,MPWDUPMC06V,MPWIKADC5V,MTWDUCARE02V (10.10.176.61),MTWDAFFCA01V,ikaclaimts40-NEW,MDLDPHED01V,MSWDFMC02V (10.10.176.83),MTLDCLMCST01V,MPWIKADC65-06V,MTWWBCBAR09V,MTWAUPMC01V (10.10.172.204),wickp_lin2_zero,MPWIKADC130-01V,MTWWUPMC02V,SQLClust01 (10.10.176.70),MPWIKADC65-05V,MTWACLMS4501V,MSLDPST01V (10.10.176.111 - Claims DB & Adjudication),MPWDFMC01V,MTWICESUPMC01V (10.10.176.94),MTWASTG01V (10.10.172.179),MPWControlM-01V (172.16.8.200),MTWDTHC01V,MPWControlM-01V,MPLDBCBAR02V (10.10.184.171 - Claims DB),MSWWCLM02V,MSWAWSUS01V,MPWIBCBSM02V,MTWAUPMC01V (10.10.30.147),wickp_zero,%2fvmfs%2fvolumes%2f55105e79-055924b6-34b0-0025b5a11077%2fIKACLAIMTS40 - Import,TemplateSQL,MPWIKADC03V (,MTWDUPMC01V,MPWWBCMA02V,MPWWUPMC01V (10.10.180.98),MPWIKADC98-02V,MPLDCST01V (10.10.184.56),MSLDBCBAR02V (10.10.184.211 - Claims DB),MTWDBCBAR10V,MSWDBCBSM03V(1.0,MSWDEST01V (10.10.176.15),MSWWBCBAR06V,MPWWUPMC02V,mtwducare01v,MPWIKADC30-03V (10.10.30.x),MPWWFMC01V (10.10.180.34),MSWFS01V,Win2K14_SQL_Template,MPWFS01V (10.10.172.53),MTLDBCBAR01V (10.10.184.210 - Claims DB & Adjudication),MPWDCIKA130-03V,MTWDBCBAR07V,MRWDBCBAR03V (10.10.176.114 - ICES DB & Web),MSWD03V (10.10.176.65),temp-testingDHCP,__mpliscanftp01v,MPWDFMC01V (10.10.184.69),MPWIBCBSM03V,MTWDBCBAR10V (10.10.172.181 - ITS Claims Web),MTLDBCBAR02V (10.10.176.61 - Claims DB),MTWHSSUPMC01V,MSWWUPMC02V (10.10.172.214),MPWDFMC02V (10.10.184.56),MTWHSSUPMC01V (10.10.172.178),MPWAFMC02V,MQLAINSP01V (10.10.208.13),MTLDTEST01V (10.10.30.70),MTWWINSP01V,XtremeIO_XMS_30,MTWWAFFNY02V,MSWWUPMC04V,MPWIKADC98-01V,IKACLAIMTS40.IKACLAIM.COM,mpldodi01v (10.10.176.TBD),mqldodi01v (10.10.176.244),MSWWBCBAR06V (10.10.180.x - ITS2),IKACLAIMTS40 - TEST,MTLDCLMPST01v,MPWIKADC98-03V,MTWWBCBAR08V,MPWWBCMA01V,MPWWGHC01V,MPWDTHC01V (10.10.184.39),MTWWAFFNY01V (10.10.172.41),MTWABCBSAR01V (10.10.172.x - IBM MQ),MPWAFMC01V,MSWWBCBAR05V (10.10.180.145 - ITS1 - Not needed?),MTWDBCBAR08V,MSWDUPMC02V (10.10.30.174),MSWWBCBAR05V (10.10.180.145 - ITS1),MPWHYPERV383,MDWWINSP01V,MSWWBCBAR06V (10.10.180.146 - ITS2 - Not needed?),MQWAINSP02V (10.10.208.14),MTWDUCARE01V (10.10.176.61),MTWDUPMC01V (10.10.176.153),MPWDCIKA130-04V,IPRDDC699,MSWWBCBAR05V,Win2k12_ADAM_Template,MTWWUPMCPRH01V,MSWAFMC02V (10.10.172.82),MPWDBCBSM03V (10.10.90.246),MPWIBCBSM01V (10.10.172.253 %2f 90.25),mswwupmc01v,MSLDEST03V,MRLDBCBAR01V (10.10.176.111 - Claims DB & Adjudication),SQLClust02,MPWAEST02V,MSLDCST01V (10.10.176.91),MTLDBCBAR04V (10.10.176.86 - Claims DB),MPWDCIKA65V.ikaprod65.com,MSLDEST03V (10.10.176.90),MPWDAFFCA01V,Win2k12_DC_Template,MSWWCLM01V,msldest02v,MSWWUPMC04V (10.10.30.145),MPWAEST01V,mtwducare01v (10.10.176.253),MSWDFMC01V (10.10.176.82),MTLDTEST03V (10.10.30.72),msldest01v (10.10.176.81),MTLDUPMC04V,MSWCST01V,MDWWINSP02V,SSWAWSUS01v,MTWDAFFCA01V (10.10.176.95),MSWFS02V,MPWWUPMC04V,IKACLAIMTS40-VM,MPWDUPMC04V (10.10.61.40),MSLDFMC01V (10.10.176.81),wickp_ZEROout,MSWABCBSAR01V (10.10.172.33 - IBM MQ),MSWWFMC01V (10.10.172.83),MTLDBCBAR03V (10.10.176.x - 3 Adjudication engine),MSWWBCBAR06V (10.10.180.146 - ITS2),MSWDBCBSM03V (1.0.ikaprod90.com (10.10.90.247),MSWDBCBSM03V,MSWDUPMC01V (10.10.176.38) (09-10-15),MPWIKADC65-04V,SQLClust03 (10.10.176.70),msldest02v (10.10.176.84),MPWAFMC02V (10.10.180.32),wickp_lin5_zerp,MSWWUPMC01V,XtremeIO_XMS_30_old (still in use),MPWIKADC65-01V,MPWIKADC65-03V,MTWDICESUPMC01V,MTWWBCBAR07V,MPWIKADC98-04V,MQWAINSP01V (10.10.208.14)";
        String deletes2 = "MPWAFMC01V,MSWWBCBAR05V (10.10.180.145 - ITS1 - Not needed?),MTWDBCBAR08V,MSWDUPMC02V (10.10.30.174),MSWWBCBAR05V (10.10.180.145 - ITS1),MPWHYPERV383,MDWWINSP01V,MSWWBCBAR06V (10.10.180.146 - ITS2 - Not needed?)";

        List<String> items = Arrays.asList(deletes2.split("\\s*,\\s*"));
        HostClient client = new HostClient(getDeploymentURL());
        DtoOperationResults results = client.delete(items);
        assert results.getWarning() > 0;
    }

    // testing performance of unindexed metricType column when retrieving hosts
    // see http://jira/browse/GWMON-12883
    //@Test
    public void testQueryByService() throws Exception {
        if (serverDown) return;
        HostClient client = new HostClient(getDeploymentURL()); // appType = 'VEMA' and 
        List<DtoHost> hosts = client.query("serviceStatuses.metricType is not null");
        //List<DtoHost> hosts = client.query("h.serviceStatuses.applicationType.applicationTypeId = 200");
        //List<DtoHost> hosts = client.query("appType = 'VEMA'");
        //assertEquals(15, hosts.size());
        int vema = 0, aws = 0;
        for (DtoHost host : hosts) {
            //assertTrue(host.getAppType().startsWith("VEMA"));
            if (host.getAppType().equals("VEMA")) vema++;
            if (host.getAppType().equals("AWS")) {
                aws++;
                System.out.println("host " + host.getHostName() + ", " + host.getAppType());
            }
        }
        System.out.println("vema = " + vema);
        System.out.println("aws = " + aws);
    }

    //@Test
    public void testQueryByService2() throws Exception {
        if (serverDown) return;
        HostClient client = new HostClient(getDeploymentURL()); // appType = 'VEMA' and
        List<DtoHost> hosts = client.query("serviceStatuses.serviceDescription = 'tcp_http'");
        System.out.println("-- host: " + hosts.get(0).getHostName());
        assert  hosts.size() >= 1;
    }

}
