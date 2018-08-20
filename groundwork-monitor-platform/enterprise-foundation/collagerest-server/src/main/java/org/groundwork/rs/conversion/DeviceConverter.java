package org.groundwork.rs.conversion;

import com.groundwork.collage.model.Device;
import com.groundwork.collage.model.Host;
import com.groundwork.collage.model.MonitorServer;
import org.groundwork.rs.dto.DtoDepthType;
import org.groundwork.rs.dto.DtoDevice;

import java.util.Set;

public class DeviceConverter {

    public final static DtoDevice convert(Device device, DtoDepthType depthType) {
        DtoDevice dto = new DtoDevice();
        dto.setId(device.getDeviceId());
        dto.setDisplayName(device.getDisplayName());
        dto.setIdentification(device.getIdentification());
        dto.setDescription(device.getDescription());
        if (depthType.equals(DtoDepthType.Deep)) {
            Set<Host> hostSet = device.getHosts();
            for (Host host : hostSet) {
                dto.addHost(HostConverter.convert(host, DtoDepthType.Simple));
            }
            Set<MonitorServer> serverSet = device.getMonitorServers();
            for (MonitorServer monitorServer : serverSet) {
                dto.addMonitorServer(MonitorServerConverter.convert(monitorServer, DtoDepthType.Shallow));
            }
        }
        return dto;
    }
}
