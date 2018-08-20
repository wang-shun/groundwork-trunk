package org.groundwork.cloudhub.configuration;

import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.Pattern;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "openstack")
@XmlType(propOrder = {"server", "tenantId", "tenantName", "username", "password", "sslEnabled", "novaPort", "keystonePort", "ceilometerPort", "ceilometerSampleRateMinutes"})
public class OpenStackConnection extends BaseSecureMonitorConnection implements SecureMonitorConnection {

    protected static final String HTTP = "http://";
    protected static final String HTTPS = "https://";

    private static boolean DEFAULT_OPENSTACK_SSL_ENABLED = false;
    private static String DEFAULT_OPENSTACK_VSS_SERVER = "";
    private static String DEFAULT_OPENSTACK_URI = "";
    private static String DEFAULT_OPENSTACK_URL = "";
    private static String DEFAULT_OPENSTACK_USER = "";
    private static String DEFAULT_OPENSTACK_PASSWORD = "";

    @NotBlank (message="Tenant id cannot be empty.")
    private String tenantId;

    @NotBlank (message="Tenant name cannot be empty.")
    private String tenantName;

    @Pattern(regexp="^[0-9]{2,4}$", message="Not a valid port number.")
    private String novaPort = "8774";

    @Pattern(regexp="^[0-9]{2,4}$", message="Not a valid port number.")
    private String keystonePort = "5000";

    @Pattern(regexp="^[0-9]{2,4}$", message="Not a valid port number.")
    private String ceilometerPort = "8777";

    @Pattern(regexp="^[0-9]{1,2}$", message="Not a valid ceilometer sample rate.")
    private String ceilometerSampleRateMinutes = "10";

    public OpenStackConnection() {
        setSslEnabled(DEFAULT_OPENSTACK_SSL_ENABLED);
        setServer(DEFAULT_OPENSTACK_VSS_SERVER);
        setUsername(DEFAULT_OPENSTACK_USER);
        setPassword(DEFAULT_OPENSTACK_PASSWORD);
    }

    public void setServer(String server) {
        if (server != null) {
            server = server.startsWith(HTTP) ? server.substring((HTTP).length()) : server;
            server = server.startsWith(HTTPS) ? server.substring((HTTPS).length()) : server;
        }
        this.server = server;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getTenantName() {
        return tenantName;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    public String getNovaPort() {
        return novaPort;
    }

    public void setNovaPort(String novaPort) {
        this.novaPort = novaPort;
    }

    public String getKeystonePort() {
        return keystonePort;
    }

    public void setKeystonePort(String keystonePort) {
        this.keystonePort = keystonePort;
    }

    public String getCeilometerPort() {
        return ceilometerPort;
    }

    public void setCeilometerPort(String ceilometerPort) {
        this.ceilometerPort = ceilometerPort;
    }

    public String getCeilometerSampleRateMinutes() {
        return ceilometerSampleRateMinutes;
    }

    public void setCeilometerSampleRateMinutes(String ceilometerSampleRateMinutes) {
        this.ceilometerSampleRateMinutes = ceilometerSampleRateMinutes;
    }
}
