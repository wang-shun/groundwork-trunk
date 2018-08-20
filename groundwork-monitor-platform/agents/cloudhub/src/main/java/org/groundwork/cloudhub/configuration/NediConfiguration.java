package org.groundwork.cloudhub.configuration;

import org.groundwork.agents.monitor.VirtualSystem;

import javax.validation.Valid;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


@XmlRootElement(name = "vema")
@XmlType(propOrder={"connection"})
public class NediConfiguration extends ConnectionConfiguration {

	@Valid
    private NediConnection connection;

    public NediConfiguration() {
        super(VirtualSystem.NEDI);
        connection = new NediConnection();
    }

    @XmlElement(name = "nedi")
    public NediConnection getConnection() {
        return connection;
    }

    public void setConnection(NediConnection connection) {
        this.connection = connection;
    }

}

