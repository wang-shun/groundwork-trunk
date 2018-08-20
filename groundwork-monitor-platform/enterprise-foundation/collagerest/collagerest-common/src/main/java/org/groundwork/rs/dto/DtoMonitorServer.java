package org.groundwork.rs.dto;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "monitorServer")
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoMonitorServer {

    @XmlAttribute
    private Integer monitorServerId;

    @XmlAttribute
    private String monitorServerName;

    @XmlAttribute
    private String ip;

    @XmlAttribute
    private String description;

    @XmlElementWrapper(name="devices")
    @XmlElement(name="device")
    private List<DtoDevice> devices;

    public DtoMonitorServer() {}

    public Integer getMonitorServerId() {
        return monitorServerId;
    }

    public void setMonitorServerId(Integer monitorServerId) {
        this.monitorServerId = monitorServerId;
    }

    public String getMonitorServerName() {
        return monitorServerName;
    }

    public void setMonitorServerName(String monitorServerName) {
        this.monitorServerName = monitorServerName;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<DtoDevice> getDevices() {
        return devices;
    }

    public void setDevices(List<DtoDevice> devices) {
        this.devices = devices;
    }

    public void addDevice(DtoDevice device) {
        if (getDevices() == null) {
            devices = new ArrayList<DtoDevice>();
        }
        devices.add(device);
    }
}
