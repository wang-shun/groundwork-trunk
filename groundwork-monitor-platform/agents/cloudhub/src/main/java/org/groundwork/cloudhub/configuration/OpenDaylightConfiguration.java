package org.groundwork.cloudhub.configuration;

import org.groundwork.agents.monitor.VirtualSystem;

import javax.validation.Valid;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


@XmlRootElement(name = "vema")
@XmlType(propOrder={"connection"})
public class OpenDaylightConfiguration extends ConnectionConfiguration {

	@Valid
    private OpenDaylightConnection connection;

    public OpenDaylightConfiguration() {
        super(VirtualSystem.OPENDAYLIGHT);
        connection = new OpenDaylightConnection();
    }

    @XmlElement(name = "opendaylight")
    public OpenDaylightConnection getConnection() {
        return connection;
    }

    public void setConnection(OpenDaylightConnection connection) {
        this.connection = connection;
    }

}

