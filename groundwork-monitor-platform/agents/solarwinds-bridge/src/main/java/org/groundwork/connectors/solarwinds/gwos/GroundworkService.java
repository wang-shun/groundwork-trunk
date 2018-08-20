package org.groundwork.connectors.solarwinds.gwos;

import org.groundwork.connectors.solarwinds.SolarWindsConfiguration;
import org.groundwork.rs.client.ApplicationTypeClient;
import org.groundwork.rs.client.DeviceClient;
import org.groundwork.rs.client.EventClient;
import org.groundwork.rs.client.HostClient;
import org.groundwork.rs.client.HostGroupClient;
import org.groundwork.rs.client.NotificationClient;
import org.groundwork.rs.client.PerfDataClient;
import org.groundwork.rs.client.ServiceClient;

public class GroundworkService {

//    private static HostClient hostClient = null;
//    private static HostGroupClient hostGroupClient = null;
//    private static ServiceClient serviceClient = null;
//    private static EventClient eventClient = null;
//    private static PerfDataClient perfDataClient = null;
//    private static NotificationClient notificationClient = null;
//    private static DeviceClient deviceClient = null;
//    private static ApplicationTypeClient applicationTypeClient = null;

    public static HostClient getHostClient() {
        SolarWindsConfiguration configuration = SolarWindsConfiguration.instance();
        return new HostClient(configuration.getRestApiEndpoint());
    }

    public static HostGroupClient getHostGroupClient() {
        SolarWindsConfiguration configuration = SolarWindsConfiguration.instance();
        return new HostGroupClient(configuration.getRestApiEndpoint());
    }

    public static ServiceClient getServiceClient() {
        SolarWindsConfiguration configuration = SolarWindsConfiguration.instance();
        return new ServiceClient(configuration.getRestApiEndpoint());
    }

    public static EventClient getEventClient() {
        SolarWindsConfiguration configuration = SolarWindsConfiguration.instance();
        return new EventClient(configuration.getRestApiEndpoint());
    }

    public static PerfDataClient getPerfDataClient() {
        SolarWindsConfiguration configuration = SolarWindsConfiguration.instance();
        return new PerfDataClient(configuration.getRestApiEndpoint());
    }

    public static NotificationClient getNotificationClient() {
        SolarWindsConfiguration configuration = SolarWindsConfiguration.instance();
        return new NotificationClient(configuration.getRestApiEndpoint());
    }

    public static DeviceClient getDeviceClient() {
        SolarWindsConfiguration configuration = SolarWindsConfiguration.instance();
        return new DeviceClient(configuration.getRestApiEndpoint());
    }

    public static ApplicationTypeClient getApplicationTypeClient() {
        SolarWindsConfiguration configuration = SolarWindsConfiguration.instance();
        return new ApplicationTypeClient(configuration.getRestApiEndpoint());
    }

}
