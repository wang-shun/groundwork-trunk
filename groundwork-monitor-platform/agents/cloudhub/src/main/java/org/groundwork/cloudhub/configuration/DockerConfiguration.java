package org.groundwork.cloudhub.configuration;

import org.groundwork.agents.monitor.VirtualSystem;

import javax.validation.Valid;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


@XmlRootElement(name = "vema")
@XmlType(propOrder={"connection"})
public class DockerConfiguration extends ConnectionConfiguration {

	@Valid
    private DockerConnection connection;

    public DockerConfiguration() {
        super(VirtualSystem.DOCKER);
        connection = new DockerConnection();
    }

    @XmlElement(name = "docker")
    public DockerConnection getConnection() {
        return connection;
    }

    public void setConnection(DockerConnection connection) {
        this.connection = connection;
    }

}

