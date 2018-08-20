package org.groundwork.cloudhub.configuration;

import org.groundwork.agents.monitor.VirtualSystem;

import javax.validation.Valid;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


@XmlRootElement(name = "vema")
@XmlType(propOrder={"connection"})
public class OpenShiftConfiguration extends ConnectionConfiguration {

	@Valid
    private OpenShiftConnection connection;

    public OpenShiftConfiguration() {
        super(VirtualSystem.OPENSHIFT);
        connection = new OpenShiftConnection();
    }

    @XmlElement(name = "openshift")
    public OpenShiftConnection getConnection() {
        return connection;
    }

    public void setConnection(OpenShiftConnection connection) {
        this.connection = connection;
    }

}

