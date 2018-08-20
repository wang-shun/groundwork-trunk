package org.groundwork.cloudhub.configuration;

import org.groundwork.agents.utils.StringUtils;
import org.hibernate.validator.constraints.NotBlank;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "vmware")
@XmlType(propOrder = {"server", "uri", "url", "username", "password", "sslEnabled"})
public class VmwareConnection extends BaseSecureMonitorConnection implements SecureMonitorConnection {

    private static boolean DEFAULT_VMWARE_SSL_ENABLED = true;
    private static String DEFAULT_VMWARE_VSS_SERVER = "";
    private static String DEFAULT_VMWARE_URI = "sdk";
    private static String DEFAULT_VMWARE_URL = "";
    private static String DEFAULT_VMWARE_USER = "vmware-dev";
    private static String DEFAULT_VMWARE_PASSWORD = "";

    @NotBlank (message="Server URI cannot be empty.")
    private String uri;
    private String url;

    public VmwareConnection() {
        setSslEnabled(DEFAULT_VMWARE_SSL_ENABLED);
        setServer(DEFAULT_VMWARE_VSS_SERVER);
        setUri(DEFAULT_VMWARE_URI);
        setUrl(DEFAULT_VMWARE_URL);
        setUsername(DEFAULT_VMWARE_USER);
        setPassword(DEFAULT_VMWARE_PASSWORD);
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getUrl() {
        StringBuilder builder = new StringBuilder();
        builder.append((sslEnabled) ? "https://" : "http://");
        builder.append(server);
        if (StringUtils.isEmpty(uri)) {
            builder.append("/");
            builder.append(DEFAULT_VMWARE_URI);
        }
        else {
            if (!uri.endsWith("/"))
                builder.append("/");
            builder.append(uri);
        }
        url = builder.toString();
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
