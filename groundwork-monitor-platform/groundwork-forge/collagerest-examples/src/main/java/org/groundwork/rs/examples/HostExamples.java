package org.groundwork.rs.examples;

import org.groundwork.rs.client.DeviceClient;
import org.groundwork.rs.client.HostClient;
import org.groundwork.rs.dto.DtoDepthType;
import org.groundwork.rs.dto.DtoHost;
import org.groundwork.rs.dto.DtoHostList;
import org.groundwork.rs.dto.DtoOperationResult;
import org.groundwork.rs.dto.DtoOperationResults;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;

public class HostExamples {

    protected final FoundationConnection connection;

    public HostExamples(FoundationConnection connection) {
        this.connection = connection;
    }

    public void listHosts() {
        HostClient hostClient = new HostClient(connection.getDeploymentUrl());

        // list out all hosts at default depth (Shallow)
        System.out.println("--- Listing hosts ....");
        List<DtoHost> hosts = hostClient.list();
        for (DtoHost host : hosts) {
            System.out.println("\thost = " + host.getHostName());
        }
        // List a subset of hosts, starting at the 5th host, and for a count of 7 records
        System.out.println("--- Listing subset of hosts ....");
        hosts = hostClient.list(DtoDepthType.Shallow, 5, 7);
        for (DtoHost host : hosts) {
            System.out.println("\thost = " + host.getHostName());
        }
    }

    public void queryHosts() {
        HostClient client = new HostClient(connection.getDeploymentUrl());
        List<DtoHost> hosts = client.query("property.LastPluginOutput like 'OK%'");
        if (connection.isEnableAsserts()) assert(15 == hosts.size());
        for (DtoHost host : hosts) {
            if (connection.isEnableAsserts()) assert(host.getProperty("LastPluginOutput").startsWith("OK"));
        }
        hosts = client.query("(property.ExecutionTime between 10 and 3500 and (monitorStatus <> 'UP')) order by property.ExecutionTime");
        if (connection.isEnableAsserts()) assert(1 == hosts.size());
        for (DtoHost host : hosts) {
            if (connection.isEnableAsserts()) assert("3005" == host.getProperty("ExecutionTime"));
            if (connection.isEnableAsserts()) assert("UNSCHEDULED DOWN" == host.getMonitorStatus());
        }
    }

    public void lookupHostDeep() {
        HostClient client = new HostClient(connection.getDeploymentUrl());
        // return more in-depth information
        DtoHost host = client.lookup("localhost", DtoDepthType.Deep);
        if (connection.isEnableAsserts()) {
            assert host != null;
        }
    }


    public void hostMaintenance() {
        HostClient hostClient = new HostClient(connection.getDeploymentUrl());
        DtoHostList hostUpdates = buildHostUpdate();
        DtoOperationResults results = hostClient.post(hostUpdates);
        if (connection.isEnableAsserts()) assert (2 == results.getCount().intValue());
        String hostName = null;
        for (DtoOperationResult result : results.getResults()) {
            if (connection.isEnableAsserts()) assert(result.getStatus().equals(DtoOperationResult.SUCCESS));
        }

        DtoHost host = hostClient.lookup("host-100");
        if (connection.isEnableAsserts()) assert(null != host);
        assertHostWritten(host);

        host = hostClient.lookup("host-101");
        if (connection.isEnableAsserts()) assert(null != host);
        assertHostWritten(host);

        // reset data for next test
        executeHostDelete("host-100,host-101");
        executeDeviceDelete("192.168.5.50,192.168.5.51");

    }

    public DtoOperationResults executeDeviceDelete(String deviceIds) {
        DeviceClient client = new DeviceClient(connection.getDeploymentUrl());
        List<String> ids = new ArrayList<String>();
        Collections.addAll(ids, deviceIds.split(","));
        return client.delete(ids);
    }

    public DtoOperationResults executeHostDelete(String hostIds)  {
        HostClient client = new HostClient(connection.getDeploymentUrl());
        List<String> ids = new ArrayList<String>();
        Collections.addAll(ids, hostIds.split(","));
        return client.delete(ids);
    }

    public DtoHostList buildHostUpdate() {
        DtoHostList hosts = new DtoHostList();
        DtoHost host = new DtoHost();
        host.setHostName("host-100");
        host.setDescription("First of my servers");
        host.setAgentId("5437840f-a908-49fd-88bd-e04543a69e84");
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
        host.setHostName("host-101");
        host.setDescription("Second of my servers");
        host.setAgentId("5437840f-a908-49fd-88bd-e04543a69e85");
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

    public void assertHostWritten(DtoHost host) {
        assert(null != host.getHostName());
        if (host.getHostName().equals("host-100")) {
            assert("UP" == host.getMonitorStatus());
            assert("First of my servers" == host.getDescription());
            assert("5437840f-a908-49fd-88bd-e04543a69e84" == host.getAgentId());
            assert("NAGIOS" == host.getAppType());
            assert("192.168.5.50" == host.getDeviceIdentification());
            assert(new Double(125.31) == (Double) host.getPropertyDouble("Latency"));
            assert("UnitTester" == host.getProperty("UpdatedBy"));
            assert("This is a test." == host.getProperty("Comments"));
        }
        else if (host.getHostName().equals("host-101")) {
            assert("UP" == host.getMonitorStatus());
            assert("Second of my servers" == host.getDescription());
            assert("5437840f-a908-49fd-88bd-e04543a69e85" == host.getAgentId());
            assert("NAGIOS" == host.getAppType());
            assert("192.168.5.51" == host.getDeviceIdentification());
            assert(new Double(126.31) == (Double) host.getPropertyDouble("Latency"));
            assert("Admin" == host.getProperty("UpdatedBy"));
            assert("This is a test." == host.getProperty("Comments"));
        }
        else {
            assert ( null == "host name " + host.getHostName() + " not valid");
        }
    }


}
