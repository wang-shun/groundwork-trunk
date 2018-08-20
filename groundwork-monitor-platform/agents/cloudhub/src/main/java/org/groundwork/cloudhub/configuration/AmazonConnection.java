package org.groundwork.cloudhub.configuration;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * 
 * AmazonConnection class stores the connection parameters required for building
 * the connection with Amazon
 */
@XmlRootElement(name = "amazon")
@XmlType(propOrder = { "server", "username", "password", "sslEnabled", "enableIAMRoles" })
public class AmazonConnection extends BaseSecureMonitorConnection implements
        SecureMonitorConnection {

    private static boolean DEFAULT_AMAZON_SSL_ENABLED = true;
    private static String DEFAULT_AMAZON_DOMAIN = "us-west-2.amazonaws.com";
    private static String DEFAULT_AMAZON_ACCESS_KEY = "";
    private static String DEFAULT_AMAZON_SECRET_KEY = "";

    private Boolean enableIAMRoles = false;

    public AmazonConnection() {
        setSslEnabled(DEFAULT_AMAZON_SSL_ENABLED);
        setServer(DEFAULT_AMAZON_DOMAIN);
        setUsername(DEFAULT_AMAZON_ACCESS_KEY);
        setPassword(DEFAULT_AMAZON_SECRET_KEY);
        enableIAMRoles = false;
    }

    public Boolean getEnableIAMRoles() {
        return enableIAMRoles;
    }

    public void setEnableIAMRoles(Boolean enableIAMRoles) {
        this.enableIAMRoles = enableIAMRoles;
    }
}
