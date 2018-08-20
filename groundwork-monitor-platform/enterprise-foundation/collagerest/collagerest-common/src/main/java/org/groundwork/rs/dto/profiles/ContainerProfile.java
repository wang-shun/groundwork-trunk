package org.groundwork.rs.dto.profiles;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "container-monitoring")
@XmlType(propOrder={"engine", "container", "excludes"})
public class ContainerProfile extends HubProfile {

    private Hypervisor engine;
    private VM container;
    private Excludes excludes;

    public ContainerProfile(ProfileType profileType, String agent) {
        super(profileType, agent);
        engine = new Hypervisor();
        container = new VM();
    }

    public ContainerProfile() {
        super();
        engine = new Hypervisor();
        container = new VM();
    }

    @XmlElement(name="engine")
    public Hypervisor getEngine() {
        return engine;
    }

    public void setEngine(Hypervisor engine) {
        this.engine = engine;
    }

    @XmlElement(name="container")
    public VM getContainer() {
        return container;
    }

    public void setContainer(VM container) {
        this.container = container;
    }

    public Excludes getExcludes() {
        return excludes;
    }

    public void setExcludes(Excludes excludes) {
        this.excludes = excludes;
    }
}

