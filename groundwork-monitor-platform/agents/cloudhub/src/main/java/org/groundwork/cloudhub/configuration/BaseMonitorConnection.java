package org.groundwork.cloudhub.configuration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.Pattern;
import javax.xml.bind.annotation.XmlTransient;

@XmlTransient
public abstract class BaseMonitorConnection implements MonitorConnection {

    @NotBlank(message="Server name cannot be empty.")
    @Pattern(regexp="^[a-zA-Z0-9_\\.\\-\\:\\/]*$", message="Invalid characters entered for Server Name")
    protected String server;

    public BaseMonitorConnection() {
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    @JsonIgnore
    public String getHostName() {
        if (server == null)
            return server;
        String hostName = server;
        if (hostName.startsWith("http://")) {
            hostName = hostName.substring("http://".length());
        }
        return hostName;
    }

}
