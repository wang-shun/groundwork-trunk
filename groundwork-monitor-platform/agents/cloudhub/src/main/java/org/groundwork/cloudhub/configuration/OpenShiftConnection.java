package org.groundwork.cloudhub.configuration;

import org.hibernate.validator.constraints.NotBlank;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "openshift")
@XmlType(propOrder = {"server", "uri", "url", "username", "password", "sslEnabled"})
public class OpenShiftConnection extends BaseSecureMonitorConnection implements SecureMonitorConnection {

    private static boolean DEFAULT_OPENSHIFT_SSL_ENABLED = false;
    private static String DEFAULT_OPENSHIFT_VSS_SERVER = "";
    private static String DEFAULT_OPENSHIFT_URI = "";
    private static String DEFAULT_OPENSHIFT_URL = "";
    private static String DEFAULT_OPENSHIFT_USER = "";
    private static String DEFAULT_OPENSHIFT_PASSWORD = "";

    @NotBlank (message="Server URI cannot be empty.")
    private String uri;
    private String url;

    public OpenShiftConnection() {
        setSslEnabled(DEFAULT_OPENSHIFT_SSL_ENABLED);
        setServer(DEFAULT_OPENSHIFT_VSS_SERVER);
        setUri(DEFAULT_OPENSHIFT_URI);
        setUrl(DEFAULT_OPENSHIFT_URL);
        setUsername(DEFAULT_OPENSHIFT_USER);
        setPassword(DEFAULT_OPENSHIFT_PASSWORD);
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
