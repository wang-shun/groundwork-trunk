package org.groundwork.rs.dto.profiles;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "vema-monitoring")
@XmlType(propOrder={"hypervisor", "vm", "custom", "excludes"})
public class CloudHubProfile extends HubProfile {

    private Hypervisor hypervisor;
    private VM vm;
    private CustomMetrics custom;
    private Excludes excludes;

    public CloudHubProfile(ProfileType profileType, String agent) {
        super(profileType, agent);
        hypervisor = new Hypervisor();
        vm = new VM();
        custom = new CustomMetrics();
    }
    public CloudHubProfile() {
        super();
        hypervisor = new Hypervisor();
        vm = new VM();
        custom = new CustomMetrics();
    }

    @XmlElement(name="hypervisor")
    public Hypervisor getHypervisor() {
        return hypervisor;
    }

    public void setHypervisor(Hypervisor hypervisor) {
        this.hypervisor = hypervisor;
    }

    @XmlElement(name="vm")
    public VM getVm() {
        return vm;
    }

    public void setVm(VM vm) {
        this.vm = vm;
    }


    @XmlElement(name="custom")
    public CustomMetrics getCustom() {
        return custom;
    }

    public void setCustom(CustomMetrics custom) {
        this.custom = custom;
    }

    public Excludes getExcludes() {
        return excludes;
    }

    public void setExcludes(Excludes excludes) {
        this.excludes = excludes;
    }
}

