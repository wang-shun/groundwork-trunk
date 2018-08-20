package org.groundwork.rs.client;

import org.groundwork.rs.dto.DtoAsyncSettings;
import org.groundwork.rs.dto.DtoDevice;
import org.groundwork.rs.dto.DtoDeviceList;
import org.groundwork.rs.dto.DtoHost;
import org.groundwork.rs.dto.DtoHostList;
import org.groundwork.rs.dto.DtoOperationResult;
import org.groundwork.rs.dto.DtoOperationResults;
import org.junit.Test;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class AsyncHostClientTest extends AbstractHostTest {

    @Test
    public void testCreateAndMultiDeleteHostsAsync() throws Exception {
        if (serverDown) return;

        DtoHostList hostUpdates = buildHostUpdate("-");
        DtoOperationResults results = executePostAsync(hostUpdates);
        assertEquals(1, results.getCount().intValue());
        for (DtoOperationResult result : results.getResults()) {
            assert(result.getStatus().equals(DtoOperationResult.SUCCESS));
        }

        Thread.sleep(3000);

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

    protected DtoOperationResults executePostAsync(DtoHostList hostUpdates) throws Exception {
        HostClient client = new HostClient(getDeploymentURL());
        return client.postAsync(hostUpdates);
    }

    @Test
    public void testOverloadAsync() throws Exception {
        if (serverDown) return;

        SettingsClient settingsClient = new SettingsClient(getDeploymentURL());
        DtoAsyncSettings saveSettings = settingsClient.getAsyncSettings();
        DtoDeviceList devices = new DtoDeviceList();
        devices.add(buildDevice());
        DeviceClient deviceClient = new DeviceClient(getDeploymentURL());
        deviceClient.post(devices);

        // Force queue threshold....
        DtoAsyncSettings newSettings = new DtoAsyncSettings(1, 2, 0, 500);
        DtoOperationResults settingsResults = settingsClient.setAsyncSettings(newSettings);
        assert settingsResults.getSuccessful() == 1;
        List<String> hostNames = new LinkedList<String>();
        boolean failed = false;
        for (int ix = 1; ix <= 40; ix++) {
            DtoHostList hostUpdates = new DtoHostList();
            DtoHost host = buildAsyncHost(ix);
            hostUpdates.add(host);
            try {
                DtoOperationResults results = executePostAsync(hostUpdates);
                assertEquals(1, results.getCount().intValue());
                for (DtoOperationResult result : results.getResults()) {
                    assert (result.getStatus().equals(DtoOperationResult.SUCCESS));
                }
                hostNames.add(host.getHostName());
            }
            catch (CollageRestException e) {
                failed = true;
                System.out.println("Failed for " + ix  );
                System.out.println("Message: " + e.getMessage());
                System.out.println("Status: " + e.getStatus());
                break;
            }
            finally {
            }
        }
        // cleanup / wait for queue to be empty
        long startCleanupWait = System.currentTimeMillis();
        do {
            settingsResults = settingsClient.setAsyncSettings(saveSettings);
            if (settingsResults.getFailed() == 1) {
                Thread.sleep(250);
            }
        } while ((settingsResults.getFailed() == 1) && (System.currentTimeMillis()-startCleanupWait < 2500));
        assert settingsResults.getSuccessful() == 1;
        HostClient client = new HostClient(getDeploymentURL());
        client.delete(hostNames);
    }

    protected DtoHost buildAsyncHost(int number) {
        DtoHost host = new DtoHost();
        host.setHostName(String.format("host-%d", number));
        host.setDescription(String.format("%d of my servers", number));
        host.setAgentId(AGENT_84);
        host.setMonitorStatus("UP");
        host.setAppType("NAGIOS");
        host.setDeviceIdentification("192.168.5.50");
        host.setMonitorServer("localhost");
        host.setDeviceDisplayName(String.format("Device-%d", number));
        host.putProperty("Latency", new Double(125.31));
        host.putProperty("UpdatedBy", "UnitTester");
        host.putProperty("Comments", "This is a test.");
        host.putProperty("LastStateChange", new Date());
        return host;
    }

    protected DtoDevice buildDevice() {
        DtoDevice device = new DtoDevice();
        device.setIdentification("192.168.5.50");
        device.setDisplayName("Device 50");
        device.setDescription("The Device 500");
        return device;
    }
}
