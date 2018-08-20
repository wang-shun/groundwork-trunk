package org.groundwork.rs.client;

import org.groundwork.rs.dto.DtoHost;
import org.groundwork.rs.dto.DtoHostList;
import org.groundwork.rs.dto.DtoOperationResults;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

public abstract class AbstractHostTest extends AbstractClientTest {

    protected static final String DEVICE_IDENTIFICATION = "192.168.80.198";
    protected static final String HOST_200 = "host-200";
    protected static final String HOST_201 = "host-201";

    protected DtoHost retrieveHostByAgent(String agentId) throws Exception {
        HostClient client = new HostClient(getDeploymentURL());
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
        HostClient client = new HostClient(getDeploymentURL());
        return client.post(hostUpdates);
    }

    protected DtoOperationResults executeDelete(String hostIds) throws Exception {
        HostClient client = new HostClient(getDeploymentURL());
        List<String> ids = new ArrayList<String>();
        Collections.addAll(ids, hostIds.split(","));
        return client.delete(ids);
    }

    protected DtoOperationResults executeDeleteWithDto(String hostIds) throws Exception {
        HostClient client = new HostClient(getDeploymentURL());
        DtoHostList hostList = new DtoHostList();
        List<String> ids = new ArrayList<String>();
        Collections.addAll(ids, hostIds.split(","));
        for (String id : ids) {
            DtoHost host = new DtoHost();
            host.setHostName(id);
            hostList.add(host);
        }
        return client.delete(hostList);
    }

    protected DtoOperationResults executeDeviceDelete(String deviceIds) throws Exception {
        DeviceClient client = new DeviceClient(getDeploymentURL());
        List<String> ids = new ArrayList<String>();
        Collections.addAll(ids, deviceIds.split(","));
        return client.delete(ids);
    }


    protected DtoHostList buildHostUpdate(String delimiter) {
        DtoHostList hosts = new DtoHostList();
        DtoHost host = new DtoHost();
        host.setHostName("host" + delimiter + "100");
        host.setDescription("First of my servers");
        host.setAgentId(AGENT_84);
        host.setMonitorStatus("UP");
        host.setAppType("NAGIOS");
        host.setDeviceIdentification("192.168.5.50");
        host.setMonitorServer("localhost");
        host.setDeviceDisplayName("Device-50");
        host.putProperty("Latency", new Double(125.31));
        host.putProperty("UpdatedBy", "UnitTester");
        host.putProperty("Comments", "This is a test.");
        Calendar last = new GregorianCalendar(2013, Calendar.SEPTEMBER, 1, 0, 0);
        host.putProperty("LastStateChange", last);
        hosts.add(host);

        host = new DtoHost();
        host.setHostName("host" + delimiter +  "101");
        host.setDescription("Second of my servers");
        host.setAgentId(AGENT_85);
        host.setMonitorStatus("UP");
        host.setAppType("NAGIOS");
        host.setDeviceIdentification("192.168.5.51");
        host.setMonitorServer("localhost");
        //host.setDeviceDisplayName("Device-51");
        host.putProperty("Latency", new Double(126.31));
        host.putProperty("UpdatedBy", "Admin");
        host.putProperty("Comments", "This is a test.");
        hosts.add(host);

        return hosts;
    }

    protected void assertHostWritten(DtoHost host, String delimiter) {
        assertNotNull(host.getHostName());
        if (host.getHostName().equals("host" + delimiter + "100")) {
            assertEquals("UP", host.getMonitorStatus());
            assertEquals("First of my servers", host.getDescription());
            assertEquals(AGENT_84, host.getAgentId());
            assertEquals("NAGIOS", host.getAppType());
            assertEquals("192.168.5.50", host.getDeviceIdentification());
            assertEquals(new Double(125.31), (Double) host.getPropertyDouble("Latency"));
            assertEquals("UnitTester", host.getProperty("UpdatedBy"));
            assertEquals("This is a test.", host.getProperty("Comments"));
            Calendar last = new GregorianCalendar(2013, Calendar.SEPTEMBER, 1, 0, 0);
            Date actual = host.getPropertyDate("LastStateChange");
            //assertEquals(last.getTime(), actual);
        }
        else if (host.getHostName().equals("host" + delimiter + "101")) {
            assertEquals("UP", host.getMonitorStatus());
            assertEquals("Second of my servers", host.getDescription());
            assertEquals(AGENT_85, host.getAgentId());
            assertEquals("NAGIOS", host.getAppType());
            assertEquals("192.168.5.51", host.getDeviceIdentification());
            assertEquals(new Double(126.31), (Double) host.getPropertyDouble("Latency"));
            assertEquals("Admin", host.getProperty("UpdatedBy"));
            assertEquals("This is a test.", host.getProperty("Comments"));
        }
        else {
            fail("host name " + host.getHostName() + " not valid");
        }
    }

    protected DtoHostList buildHostDeviceUpdates() {
        DtoHostList hosts = new DtoHostList();
        DtoHost host = new DtoHost();
        host.setHostName(HOST_200);
        host.setDescription("First of my servers");
        host.setMonitorStatus("UP");
        host.setAppType("NAGIOS");
        host.setDeviceIdentification(DEVICE_IDENTIFICATION);
        host.setMonitorServer("localhost");
        host.setDeviceDisplayName("Device-198");
        hosts.add(host);

        host = new DtoHost();
        host.setHostName(HOST_201);
        host.setDescription("Second of my servers");
        host.setMonitorStatus("UP");
        host.setAppType("NAGIOS");
        host.setDeviceIdentification(DEVICE_IDENTIFICATION);
        host.setMonitorServer("localhost");
        host.setDeviceDisplayName("Device-198");
        hosts.add(host);

        return hosts;
    }

}
