package org.groundwork.cloudhub.configuration;

import org.groundwork.agents.monitor.VirtualSystem;

import javax.validation.Valid;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


@XmlRootElement(name = "vema")
@XmlType(propOrder={"connection"})
public class NSXConfiguration extends ConnectionConfiguration {

	@Valid
    private NSXConnection connection;

    public NSXConfiguration() {
        super(VirtualSystem.NSX);
        connection = new NSXConnection();
    }

    @XmlElement(name = "nsx")
    public NSXConnection getConnection() {
        return connection;
    }

    public void setConnection(NSXConnection connection) {
        this.connection = connection;
    }

}

