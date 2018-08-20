package org.groundwork.cloudhub.configuration;

import org.groundwork.agents.monitor.VirtualSystem;

import javax.validation.Valid;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "vema")
@XmlType(propOrder = {"connection"})
public class RedhatConfiguration extends ConnectionConfiguration {

	@Valid
    private RedhatConnection connection;

    public RedhatConfiguration() {
        super(VirtualSystem.REDHAT);
        connection = new RedhatConnection();
    }

    @XmlElement(name = "redhat")
    public RedhatConnection getConnection() {
        return connection;
    }

    public void setConnection(RedhatConnection connection) {
        this.connection = connection;
    }
}
