package org.groundwork.rs.dto;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "device")
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoDevice {

    // Shallow attributes
    @XmlAttribute
    private Integer id;

    @XmlAttribute
    private String displayName;

    @XmlAttribute
    private String identification;

    @XmlAttribute
    private String description;

    // Deep Attributes
    @XmlElementWrapper(name="hosts")
    @XmlElement(name="host")
    private List<DtoHost> hosts;

    @XmlElementWrapper(name="monitorServers")
    @XmlElement(name="monitorServer")
    private List<DtoMonitorServer> monitorServers;

    public DtoDevice() {
    }

    public DtoDevice(String identification) {
        this.identification = identification;
    }

    // Shallow Accessors
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getIdentification() {
        return identification;
    }

    public void setIdentification(String identification) {
        this.identification = identification;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // Deep Accessors
    public List<DtoHost> getHosts() {
        return hosts;
    }

    public void setHosts(List<DtoHost> hosts) {
        this.hosts = hosts;
    }


    public List<DtoMonitorServer> getMonitorServers() {
        return monitorServers;
    }

    public void setMonitorServers(List<DtoMonitorServer> monitorServers) {
        this.monitorServers = monitorServers;
    }

    public void addHost(DtoHost host) {
        if (getHosts() == null) {
            hosts = new ArrayList<DtoHost>();
        }
        hosts.add(host);
    }

    public void addMonitorServer(DtoMonitorServer server) {
        if (getMonitorServers() == null) {
            monitorServers = new ArrayList<DtoMonitorServer>();
        }
        monitorServers.add(server);
    }

}


