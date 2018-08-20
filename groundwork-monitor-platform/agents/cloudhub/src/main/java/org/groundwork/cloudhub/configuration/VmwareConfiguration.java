package org.groundwork.cloudhub.configuration;

import org.groundwork.agents.monitor.VirtualSystem;

import javax.validation.Valid;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


@XmlRootElement(name = "vema")
@XmlType(propOrder={"connection"})
public class VmwareConfiguration extends ConnectionConfiguration {

	@Valid
    private VmwareConnection connection;

    public VmwareConfiguration() {
        super(VirtualSystem.VMWARE);
        connection = new VmwareConnection();
    }

    @XmlElement(name = "vmware")
    public VmwareConnection getConnection() {
        return connection;
    }

    public void setConnection(VmwareConnection connection) {
        this.connection = connection;
    }

}

