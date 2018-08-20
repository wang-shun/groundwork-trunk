package org.groundwork.cloudhub.configuration;

import org.hibernate.validator.constraints.NotBlank;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "nedi")
@XmlType(propOrder = {"server", "port", "username", "password", "database", "nediInterval", "policyHost", "sslEnabled", "monitorDevices", "monitorPolicies", "monitorEvents" })
public class NediConnection extends BaseSecureMonitorConnection implements SecureMonitorConnection {

    private static String DEFAULT_NEDI_SERVER = "";
    private static String DEFAULT_NEDI_SERVER_PORT = "5432";
    private static String DEFAULT_NEDI_USER = "";
    private static String DEFAULT_NEDI_PASSWORD = "";
    private static String DEFAULT_NEDI_DATABASE = "";
    private static String DEFAULT_NEDI_POLICY_HOST = "localhost";

    @NotBlank(message="Database cannot be empty.")
    protected String database;

    protected String port;

    private Long nediInterval = 300L; // in seconds, 5 minutes

    private String policyHost;

    private Boolean monitorDevices = true;
    private Boolean monitorPolicies = true;
    private Boolean monitorEvents = false;

    public NediConnection() {
        setServer(DEFAULT_NEDI_SERVER);
        setPort(DEFAULT_NEDI_SERVER_PORT);
        setUsername(DEFAULT_NEDI_USER);
        setPassword(DEFAULT_NEDI_PASSWORD);
        setDatabase(DEFAULT_NEDI_DATABASE);
        setPolicyHost(DEFAULT_NEDI_POLICY_HOST);
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public Long getNediInterval() {
        return nediInterval;
    }

    public void setNediInterval(Long nediInterval) {
        this.nediInterval = nediInterval;
    }

    public String getPolicyHost() {
        return policyHost;
    }

    public void setPolicyHost(String policyHost) {
        this.policyHost = policyHost;
    }

    public Boolean getMonitorDevices() {
        return monitorDevices;
    }

    public void setMonitorDevices(Boolean monitorDevices) {
        this.monitorDevices = monitorDevices;
    }

    public Boolean getMonitorPolicies() {
        return monitorPolicies;
    }

    public void setMonitorPolicies(Boolean monitorPolicies) {
        this.monitorPolicies = monitorPolicies;
    }

    public Boolean getMonitorEvents() {
        return monitorEvents;
    }

    public void setMonitorEvents(Boolean monitorEvents) {
        this.monitorEvents = monitorEvents;
    }
}
