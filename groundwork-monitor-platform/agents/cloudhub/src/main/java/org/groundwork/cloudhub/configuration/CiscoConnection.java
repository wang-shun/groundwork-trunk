package org.groundwork.cloudhub.configuration;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "cisco")
@XmlType(propOrder = {"server", "uri", "url", "username", "password", "sslEnabled"})
public class CiscoConnection extends BaseSecureMonitorConnection implements SecureMonitorConnection {

    private static boolean DEFAULT_CISCO_SSL_ENABLED = false;
    private static String DEFAULT_CISCO_VSS_SERVER = "";
    private static String DEFAULT_CISCO_URI = "";
    private static String DEFAULT_CISCO_URL = "";
    private static String DEFAULT_CISCO_USER = "";
    private static String DEFAULT_CISCO_PASSWORD = "";

    //@NotBlank (message="Server URI cannot be empty.")
    private String uri;
    private String url;

    public CiscoConnection() {
        setSslEnabled(DEFAULT_CISCO_SSL_ENABLED);
        setServer(DEFAULT_CISCO_VSS_SERVER);
        setUri(DEFAULT_CISCO_URI);
        setUrl(DEFAULT_CISCO_URL);
        setUsername(DEFAULT_CISCO_USER);
        setPassword(DEFAULT_CISCO_PASSWORD);
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
