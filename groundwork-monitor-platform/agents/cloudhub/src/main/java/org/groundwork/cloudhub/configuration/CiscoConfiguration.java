package org.groundwork.cloudhub.configuration;

import org.groundwork.agents.monitor.VirtualSystem;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.Valid;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


@XmlRootElement(name = "vema")
@XmlType(propOrder={"connection"})
public class CiscoConfiguration extends ConnectionConfiguration {

	@Valid
    private CiscoConnection connection;

    @NotBlank(message="User name cannot be empty.")
    protected String username;

    @NotBlank (message="Password cannot be empty.")
    protected String password;

    public CiscoConfiguration() {
        super(VirtualSystem.CISCO);
        connection = new CiscoConnection();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @XmlElement(name = "cisco")
    public CiscoConnection getConnection() {
        return connection;
    }

    public void setConnection(CiscoConnection connection) {
        this.connection = connection;
    }

}

