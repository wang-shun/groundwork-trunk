package org.groundwork.cloudhub.configuration;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "nsx")
@XmlType(propOrder = {"server", "uri", "url", "username", "password", "sslEnabled"})
public class NSXConnection extends BaseSecureMonitorConnection implements SecureMonitorConnection {

    private static boolean DEFAULT_OPENDAYLIGHT_SSL_ENABLED = false;
    private static String DEFAULT_OPENDAYLIGHT_VSS_SERVER = "";
    private static String DEFAULT_OPENDAYLIGHT_URI = "";
    private static String DEFAULT_OPENDAYLIGHT_URL = "";
    private static String DEFAULT_OPENDAYLIGHT_USER = "";
    private static String DEFAULT_OPENDAYLIGHT_PASSWORD = "";

    //@NotBlank (message="Server URI cannot be empty.")
    private String uri;
    private String url;

    public NSXConnection() {
        setSslEnabled(DEFAULT_OPENDAYLIGHT_SSL_ENABLED);
        setServer(DEFAULT_OPENDAYLIGHT_VSS_SERVER);
        setUri(DEFAULT_OPENDAYLIGHT_URI);
        setUrl(DEFAULT_OPENDAYLIGHT_URL);
        setUsername(DEFAULT_OPENDAYLIGHT_USER);
        setPassword(DEFAULT_OPENDAYLIGHT_PASSWORD);
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
