package org.groundwork.rs.conversion;

import com.groundwork.collage.model.Device;
import com.groundwork.collage.model.MonitorServer;
import org.groundwork.rs.dto.DtoDepthType;
import org.groundwork.rs.dto.DtoMonitorServer;

import java.util.Set;

public class MonitorServerConverter {

    public final static DtoMonitorServer convert(MonitorServer monitorServer, DtoDepthType depthType) {
        DtoMonitorServer dto = new DtoMonitorServer();
        dto.setMonitorServerId(monitorServer.getMonitorServerId());
        dto.setMonitorServerName(monitorServer.getMonitorServerName());
        dto.setIp(monitorServer.getIp());
        dto.setDescription(monitorServer.getDescription());
        if (depthType == DtoDepthType.Deep) {
            Set<Device> deviceSet = monitorServer.getDevices();
            for (Device device : deviceSet) {
                dto.addDevice(DeviceConverter.convert(device, DtoDepthType.Shallow));
            }
        }
        return dto;
    }


}
