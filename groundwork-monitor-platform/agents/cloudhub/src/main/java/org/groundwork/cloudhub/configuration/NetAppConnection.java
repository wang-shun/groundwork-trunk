package org.groundwork.cloudhub.configuration;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * 
 * AmazonConnection class stores the connection parameters required for building
 * the connection with Amazon
 */
@XmlRootElement(name = "netapp")
@XmlType(propOrder = { "server", "username", "password", "sslEnabled" })
public class NetAppConnection extends BaseSecureMonitorConnection implements
        SecureMonitorConnection {

    private static boolean DEFAULT_NETAPP_SSL_ENABLED = false;
    private static String DEFAULT_NETAPP_SERVER = "";
    private static String DEFAULT_NETAPP_USERNAME = "";
    private static String DEFAULT_NETAPP_PASSWORD = "";

    public NetAppConnection() {
        setSslEnabled(DEFAULT_NETAPP_SSL_ENABLED);
        setServer(DEFAULT_NETAPP_SERVER);
        setUsername(DEFAULT_NETAPP_USERNAME);
        setPassword(DEFAULT_NETAPP_PASSWORD);
    }
}
