package org.groundwork.rs.dto.profiles;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "network-monitoring")
@XmlType(propOrder={"controller", "switch", "excludes"})
public class NetHubProfile extends HubProfile {

    private Hypervisor networkController;
    private VM networkSwitch;
    private Excludes excludes;

    public NetHubProfile(ProfileType profileType, String agent) {
        super(profileType, agent);
        networkController = new Hypervisor();
        networkSwitch = new VM();
    }

    public NetHubProfile() {
        super();
        networkController = new Hypervisor();
        networkSwitch = new VM();
    }

    @XmlElement(name="controller")
    public Hypervisor getController() {
        return networkController;
    }

    public void setController(Hypervisor networkController) {
        this.networkController = networkController;
    }

    @XmlElement(name="switch")
    public VM getSwitch() {
        return networkSwitch;
    }

    public void setSwitch(VM networkSwitch) {
        this.networkSwitch = networkSwitch;
    }

    public Excludes getExcludes() {
        return excludes;
    }

    public void setExcludes(Excludes excludes) {
        this.excludes = excludes;
    }
}

