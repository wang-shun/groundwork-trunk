package org.groundwork.rs.client;

import org.groundwork.rs.dto.*;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DeviceClientTest extends AbstractClientTest {

    @Test
    public void testLookupDeviceShallow() {
        if (serverDown) return;
        DeviceClient client = new DeviceClient(getDeploymentURL());
        DtoDevice device = client.lookup("172.28.113.161");
        assertNotNull(device);
        assertEquals("172.28.113.161", device.getIdentification());
        assertEquals("mc-cent5-64-5", device.getDisplayName());
    }

    @Test
    public void testLookupDeviceDeep() {
        if (serverDown) return;
        DeviceClient client = new DeviceClient(getDeploymentURL());
        DtoDevice device = client.lookup("127.0.0.1", DtoDepthType.Deep);
        assertNotNull(device);
        assertEquals("127.0.0.1", device.getIdentification());
        assertEquals("127.0.0.1", device.getDisplayName());
        assertEquals("Device localhost", device.getDescription());
        Assert.assertEquals(new Integer(1), device.getId());
        List<DtoMonitorServer> monitorServers = device.getMonitorServers();
        assertEquals("Default Monitor Server", monitorServers.get(0).getDescription());
        List<DtoHost> hosts = device.getHosts();
        assertEquals("localhost", hosts.get(0).getDescription());
    }

    @Test
    public void testDeviceCount() throws Exception {
        if (serverDown) return;
        DeviceClient client = new DeviceClient(getDeploymentURL());
        List<DtoDevice> devices = client.list(DtoDepthType.Shallow, -1, -1);
        Assert.assertEquals(21, devices.size());
        devices = client.list(DtoDepthType.Shallow, 0, 10);
        Assert.assertEquals(10, devices.size());
    }

    @Test
    public void testDeviceJoinedQuery() throws Exception {
        if (serverDown) return;
        DeviceClient client = new DeviceClient(getDeploymentURL());
        List<DtoDevice> devices = client.query(
                "identification like '172.28.113%' and d.hosts.hostName = 'qa-sles-11-64-2' order by identification", DtoDepthType.Deep);
        Assert.assertNotNull(devices);
        Assert.assertEquals(1, devices.size());
        for (DtoDevice device : devices) {
            Assert.assertTrue(device.getIdentification().startsWith("172.28.113"));
            Assert.assertTrue(device.getHosts().get(0).getHostName().equals("qa-sles-11-64-2"));
        }
    }

    @Test
    public void testCreateAndDeleteDevices() throws Exception {
        if (serverDown) return;
        DtoDeviceList updates = buildDeviceUpdate();
        DtoOperationResults results = executePost(updates);
        Assert.assertEquals(2, results.getCount().intValue());
        for (DtoOperationResult result : results.getResults()) {
            assert(result.getStatus().equals(DtoOperationResult.SUCCESS));
        }
        DtoDevice device = retrieveSingleDevice("device-400", Response.Status.OK);
        Assert.assertNotNull(device);
        assertDeviceWritten(device);

        device = retrieveSingleDevice("device-401", Response.Status.OK);
        Assert.assertNotNull(device);
        assertDeviceWritten(device);

        // reset data for next test
        executeDelete("device-400");
        executeDelete("device-401");

        // test its deleted
        retrieveSingleDevice("device-400", Response.Status.NOT_FOUND);
        retrieveSingleDevice("device-401", Response.Status.NOT_FOUND);

        // test warning for missing delete
        DeviceClient client = new DeviceClient(getDeploymentURL());
        DtoOperationResults deleteResults = client.delete(Arrays.asList(new String[]{"NotADevice"}));
        assertNotNull(deleteResults);
        assertEquals(new Integer(1), deleteResults.getWarning());
    }

    private DtoDeviceList buildDeviceUpdate() throws Exception {
        DtoDeviceList devices = new DtoDeviceList();
        DtoDevice device = new DtoDevice();

        device.setIdentification("device-400");
        device.setDisplayName("Device 400");
        device.setDescription("The Device 400");
        device.addHost(lookupHost(createHost("host-a", device.getIdentification())));
        device.addHost(lookupHost(createHost("host-b", device.getIdentification())));
        devices.add(device);

        device = new DtoDevice();
        device.setIdentification("device-401");
        device.setDisplayName("Device 401");
        device.setDescription("The Device 401");
        device.addHost(lookupHost(createHost("host-c", device.getIdentification())));
        devices.add(device);
        return devices;
    }

    protected String createHost(String hostName, String device) throws Exception {
        DtoHostList hosts = new DtoHostList();
        DtoHost host = new DtoHost();
        host.setHostName(hostName);
        host.setDescription(hostName);
        host.setMonitorStatus("UP");
        host.setDeviceIdentification(device);
        hosts.add(host);
        HostClient client = new HostClient(getDeploymentURL());
        DtoOperationResults results = client.post(hosts);
        return results.getResults().get(0).getEntity();
    }

    private DtoDevice retrieveSingleDevice(String deviceName, Response.Status status) throws Exception {
        DeviceClient client = new DeviceClient(getDeploymentURL());
        return client.lookup(deviceName, DtoDepthType.Deep);
    }

    private DtoOperationResults executePost(DtoDeviceList deviceUpdates) throws Exception {
        DeviceClient client = new DeviceClient(getDeploymentURL());
        return client.post(deviceUpdates);
    }

    private DtoOperationResults executeDelete(String deviceId) throws Exception {
        DeviceClient client = new DeviceClient(getDeploymentURL());
        return client.delete(deviceId);
    }

    private void assertDeviceWritten(DtoDevice device) {
        Assert.assertNotNull(device.getIdentification());
        if (device.getIdentification().equals("device-400")) {
            Assert.assertEquals("device-400", device.getIdentification());
            Assert.assertEquals("Device 400", device.getDisplayName());
            Assert.assertEquals("The Device 400", device.getDescription());
            Assert.assertEquals(2, device.getHosts().size());
        }
        else if (device.getIdentification().equals("device-401")) {
            Assert.assertEquals("device-401", device.getIdentification());
            Assert.assertEquals("Device 401", device.getDisplayName());
            Assert.assertEquals("The Device 401", device.getDescription());
            Assert.assertEquals(1, device.getHosts().size());
        }
        else {
            Assert.fail("device name " + device.getIdentification() + " not valid");
        }
    }

    @Test
    public void testJson() throws Exception {
        if (serverDown) return;
        DeviceClient client = new DeviceClient(getDeploymentURL(), MediaType.APPLICATION_JSON_TYPE);
        DtoDevice device = client.lookup("127.0.0.1", DtoDepthType.Shallow);
        assertNotNull(device);
    }

}
