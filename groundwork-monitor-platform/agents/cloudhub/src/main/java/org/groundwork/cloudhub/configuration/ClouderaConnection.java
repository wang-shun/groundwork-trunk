package org.groundwork.cloudhub.configuration;

import com.fasterxml.jackson.annotation.JsonInclude;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "cloudera")
@XmlType(propOrder = { "server", "username", "password", "sslEnabled", "port", "timeoutMs" })
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClouderaConnection extends BaseSecureMonitorConnection implements SecureMonitorConnection {

    private static boolean DEFAULT_SSL_ENABLED = false;
    private static String DEFAULT_SERVER = "";
    private static String DEFAULT_USERNAME = "";
    private static String DEFAULT_PASSWORD = "";

    private Integer port = 7180;

    private Long timeoutMs = 5000L;

    private Boolean prefixServiceNames = false;

    public ClouderaConnection() {
        setSslEnabled(DEFAULT_SSL_ENABLED);
        setServer(DEFAULT_SERVER);
        setUsername(DEFAULT_USERNAME);
        setPassword(DEFAULT_PASSWORD);
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Long getTimeoutMs() {
        return timeoutMs;
    }

    public void setTimeoutMs(Long timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    @XmlTransient
    public Boolean getPrefixServiceNames() {
        return prefixServiceNames;
    }

    public void setPrefixServiceNames(Boolean prefixServiceNames) {
        this.prefixServiceNames = prefixServiceNames;
    }
}
