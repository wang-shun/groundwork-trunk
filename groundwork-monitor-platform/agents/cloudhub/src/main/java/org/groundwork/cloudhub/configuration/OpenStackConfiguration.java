package org.groundwork.cloudhub.configuration;

import org.groundwork.agents.monitor.VirtualSystem;

import javax.validation.Valid;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


@XmlRootElement(name = "vema")
@XmlType(propOrder={"connection"})
public class OpenStackConfiguration extends ConnectionConfiguration {

	@Valid
    private OpenStackConnection connection;

    public OpenStackConfiguration() {
        super(VirtualSystem.OPENSTACK);
        connection = new OpenStackConnection();
    }

    @XmlElement(name = "openstack")
    public OpenStackConnection getConnection() {
        return connection;
    }

    public void setConnection(OpenStackConnection connection) {
        this.connection = connection;
    }

}

