package org.groundwork.cloudhub.configuration;

import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.Pattern;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "docker")
@XmlType(propOrder = {"server", "prefix"})
public class DockerConnection extends BaseMonitorConnection implements MonitorConnection {

    private static boolean DEFAULT_DOCKER_SSL_ENABLED = false;
    private static String DEFAULT_DOCKER_VSS_SERVER = "localhost:8080";

    @NotBlank(message="Prefix cannot be empty.") @Pattern(regexp="^[a-zA-Z0-9_\\-\\#\\,\\+]*$", message="Invalid Prefix. Valid values: letters, numbers or _-,+#")
    private String prefix;

    public DockerConnection() {
        setServer(DEFAULT_DOCKER_VSS_SERVER);
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
}
