package org.groundwork.connectors.solarwinds;

import org.groundwork.connectors.solarwinds.gwos.GroundworkService;
import org.groundwork.connectors.solarwinds.monitor.BridgeStatusService;
import org.groundwork.rs.client.ApplicationTypeClient;
import org.groundwork.rs.client.DeviceClient;
import org.groundwork.rs.client.HostClient;
import org.groundwork.rs.client.HostGroupClient;
import org.groundwork.rs.client.ServiceClient;
import org.groundwork.rs.dto.DtoDevice;
import org.groundwork.rs.dto.DtoHost;
import org.groundwork.rs.dto.DtoHostGroup;
import org.junit.Test;

public class InitializationTest extends AbstractBridgeClientTest {

    @Test
    public void initTest() throws Exception {
        cleanup();
        BridgeStatusService statusService = new BridgeStatusService();
        statusService.contextInitialized(null);

        SolarWindsConfiguration configuration = SolarWindsConfiguration.instance();
        ApplicationTypeClient applicationTypeClient = GroundworkService.getApplicationTypeClient();
        DeviceClient deviceClient = GroundworkService.getDeviceClient();
        HostGroupClient hostGroupClient = GroundworkService.getHostGroupClient();
        HostClient hostClient = GroundworkService.getHostClient();
        ServiceClient serviceClient = GroundworkService.getServiceClient();

        assert applicationTypeClient.lookup(configuration.getAppType()) != null;

        DtoDevice device = deviceClient.lookup(configuration.getBridgeDevice());
        assert device != null;

        DtoHostGroup defaultHostGroup = hostGroupClient.lookup(configuration.getDefaultHostGroup());
        assert defaultHostGroup != null;
        assert defaultHostGroup.getHosts().size() == 2;

        assert hostClient.lookup(configuration.getBridgeDevice()) != null;
        assert hostClient.lookup(configuration.getUnknownHost()) != null;

        assert serviceClient.lookup(configuration.getUnknownService(), configuration.getUnknownHost()) != null;
        assert serviceClient.lookup(configuration.getSolarWindsService(), configuration.getBridgeDevice()) != null;
        assert serviceClient.lookup(configuration.getBridgeService(), configuration.getBridgeDevice()) != null;
    }

    @Test
    public void testQuick() throws Exception {

        SolarWindsConfiguration configuration = SolarWindsConfiguration.instance();
        ServiceClient serviceClient = GroundworkService.getServiceClient();
        assert serviceClient.lookup(configuration.getUnknownService(), configuration.getUnknownHost()) != null;
        assert serviceClient.lookup("Gi0/1", "382HUM1-rtr-2821") == null;
    }

    public void cleanup() {
        SolarWindsConfiguration configuration = SolarWindsConfiguration.instance();
        ApplicationTypeClient applicationTypeClient = GroundworkService.getApplicationTypeClient();
        DeviceClient deviceClient = GroundworkService.getDeviceClient();
        HostGroupClient hostGroupClient = GroundworkService.getHostGroupClient();
        HostClient hostClient = GroundworkService.getHostClient();
        ServiceClient serviceClient = GroundworkService.getServiceClient();

//        List<String> appTypes = new LinkedList<String>();
//        appTypes.add(configuration.getAppType());
//        applicationTypeClient.delete(appTypes);

        deviceClient.delete(configuration.getBridgeDevice());
        hostGroupClient.delete(configuration.getDefaultHostGroup());
        hostClient.delete(configuration.getUnknownHost());
        hostClient.delete(configuration.getBridgeDevice());
        serviceClient.delete(configuration.getUnknownService(), configuration.getUnknownHost());
        serviceClient.delete(configuration.getSolarWindsService(), configuration.getBridgeDevice());
        serviceClient.delete(configuration.getBridgeService(), configuration.getBridgeDevice());

    }


    @Test
    public void testPing() throws Exception {
        HostClient hostClient = GroundworkService.getHostClient();
        SolarWindsConfiguration configuration = SolarWindsConfiguration.instance();
        boolean thrown = false;
        try {
            DtoHost host = hostClient.lookup(configuration.getBridgeDevice());
        }
        catch (Exception e) {
            thrown = true;
        }
        assert thrown;
    }
}
