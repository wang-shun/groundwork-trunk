package org.groundwork.cloudhub.configuration;

import org.hibernate.validator.constraints.NotBlank;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "redhat")
@XmlType(propOrder = {"server", "uri", "url", "realm", "username", "password", "sslEnabled", "port", "protocol", "certificatePassword", "certificateStore"})
public class RedhatConnection extends BaseSecureMonitorConnection implements SecureMonitorConnection {

    private static boolean DEFAULT_RHEV_SSL_ENABLED = true;
    private static String DEFAULT_RHEV_VSS_SERVER = "";
    private static String DEFAULT_RHEV_URI = "api";
    private static String DEFAULT_RHEV_USER = "admin";
    private static String DEFAULT_RHEV_PASSWORD = "";
    private static String DEFAULT_RHEV_REALM = "internal";
    private static String DEFAULT_RHEV_PORT = "443";
    private static String DEFAULT_RHEV_PROTOCOL = "https";

    @NotBlank(message="Server URI cannot be empty.")
    private String uri;
    
    private String url;
    
    @NotBlank(message="Realm cannot be empty.")
    private String realm;

    private String port;
    
    private String protocol;

    @NotBlank(message="Certificate password cannot be empty.")
    private String certificatePassword;
    
    @NotBlank(message="Certificate store cannot be empty.")
    private String certificateStore;

    public RedhatConnection() {
        setSslEnabled(DEFAULT_RHEV_SSL_ENABLED);
        setServer(DEFAULT_RHEV_VSS_SERVER);
        setUri(DEFAULT_RHEV_URI);
        setUsername(DEFAULT_RHEV_USER);
        setPassword(DEFAULT_RHEV_PASSWORD);
        setRealm(DEFAULT_RHEV_REALM);
        setPort(DEFAULT_RHEV_PORT);
        setProtocol(DEFAULT_RHEV_PROTOCOL);
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

    public String getRealm() {
        return realm;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public void setSslEnabled(boolean sslEnabled) {
        this.sslEnabled = sslEnabled;

        //Protocol and port need to be set on the basis of what is the status of ssl;
        //If ssl enabled:  port = 443, protocol = https
        //If ssl disabled: port = 80, protocol = http
        
        if(!this.sslEnabled) {
        	setPort("80");
        	setProtocol("http");
        }
    }

    public String getCertificatePassword() {
        return certificatePassword;
    }

    public void setCertificatePassword(String certificatePassword) {
        this.certificatePassword = certificatePassword;
    }

    public String getCertificateStore() {
        return certificateStore;
    }

    public void setCertificateStore(String certificateStore) {
        this.certificateStore = certificateStore;
    }
}
