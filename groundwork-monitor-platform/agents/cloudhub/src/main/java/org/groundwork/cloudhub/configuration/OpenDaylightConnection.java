package org.groundwork.cloudhub.configuration;

import org.hibernate.validator.constraints.NotBlank;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "opendaylight")
@XmlType(propOrder = {"server", "container", "username", "password", "sslEnabled"})
public class OpenDaylightConnection  extends BaseSecureMonitorConnection implements SecureMonitorConnection {

    private static boolean DEFAULT_OPENDAYLIGHT_SSL_ENABLED = false;
    private static String DEFAULT_OPENDAYLIGHT_VSS_SERVER = "";
    private static String DEFAULT_OPENDAYLIGHT_CONTAINER = "default";
    private static String DEFAULT_OPENDAYLIGHT_USER = "";
    private static String DEFAULT_OPENDAYLIGHT_PASSWORD = "";

    @NotBlank (message="Container cannot be empty.")
    private String container;

    public OpenDaylightConnection() {
        setSslEnabled(DEFAULT_OPENDAYLIGHT_SSL_ENABLED);
        setServer(DEFAULT_OPENDAYLIGHT_VSS_SERVER);
        setContainer(DEFAULT_OPENDAYLIGHT_CONTAINER);
        setUsername(DEFAULT_OPENDAYLIGHT_USER);
        setPassword(DEFAULT_OPENDAYLIGHT_PASSWORD);
    }

    public String getContainer() {
        return container;
    }

    public void setContainer(String container) {
        this.container = container;
    }

}
