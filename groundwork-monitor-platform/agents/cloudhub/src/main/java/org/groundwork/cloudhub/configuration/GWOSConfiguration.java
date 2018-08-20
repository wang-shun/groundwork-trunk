package org.groundwork.cloudhub.configuration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.format.annotation.NumberFormat;
import org.springframework.format.annotation.NumberFormat.Style;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "gwos")
@XmlType(propOrder={"gwosVersion","gwosPort", "gwosServer", "gwosSSLEnabled", "wsEndPoint", "wsHostName", "wsUsername", "wsPassword",
        "wsPortNumber", "wsHostGroupName", "rsEndPoint", "mergeHosts", "monitor"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GWOSConfiguration {

    public static String DEFAULT_VERSION = "7.1";
	private static String DEFAULT_GWOS_PORT = "4913";
    public static String DEFAULT_GWOS_SERVER = "";
    public static String DEFAULT_WS_USER = "wsuser";
    public static String DEFAULT_WS_71_USER = "RESTAPIACCESS";

    private static String DEFAULT_WS_PORT_NUMBER = "80";
    private static String DEFAULT_WS_ENDPOINT_URI = "/foundation-webapp/services";
    private static String DEFAULT_WS_HOSTGROUP_ENDPOINT = "wshostgroup";
    private static String DEFAULT_WS_HOST_ENDPOINT = "wshost";
    private static String DEFAULT_WS_PASSWORD = "wsuser";

    private static String DEFAULT_RS_ENDPOINT_URI = "/api";

    public static boolean DEFAULT_GWOS_70_MERGE_HOSTS = true;
    public static boolean DEFAULT_GWOS_71_MERGE_HOSTS = false;
    public static boolean DEFAULT_MERGE_HOSTS = DEFAULT_GWOS_71_MERGE_HOSTS;

    private String gwosVersion = DEFAULT_VERSION;

    @Pattern(regexp="\\d+", message="Not a valid number.")
    private String gwosPort = DEFAULT_GWOS_PORT;
  
    @NotBlank(message="Server name cannot be empty.")
    @Pattern(regexp="^[a-zA-Z0-9_\\.\\-\\:]*$",
             message="Invalid characters entered for Groundwork Server Name")
    private String gwosServer = DEFAULT_GWOS_SERVER;

    private boolean gwosSSLEnabled = false;

    @NotBlank(message="Webservices Endpoint cannot be empty.")
    private String wsEndPoint = DEFAULT_WS_ENDPOINT_URI;
    
    @NumberFormat(style = Style.NUMBER) @NotNull
    private String wsPortNumber = DEFAULT_WS_PORT_NUMBER;
    private String wsHostName = DEFAULT_WS_HOST_ENDPOINT;
    
    @NotBlank (message="User name cannot be empty.")
    private String wsUsername = DEFAULT_WS_USER;
    
    @NotBlank (message="Password cannot be empty.")
    private String wsPassword = DEFAULT_WS_PASSWORD;
    private String wsHostGroupName = DEFAULT_WS_HOSTGROUP_ENDPOINT;

    private String rsEndPoint = DEFAULT_RS_ENDPOINT_URI;

    private Boolean mergeHosts = null;
    private Boolean monitor = true;

    public GWOSConfiguration() {
    }

    public String getGwosVersion() {
        return gwosVersion;
    }

    public void setGwosVersion(String gwosVersion) {
        this.gwosVersion = gwosVersion;
    }
    public String getGwosPort() {
        return gwosPort;
    }

    public void setGwosPort(String gwosPort) {
        this.gwosPort = gwosPort;
    }

    public String getGwosServer() {
        return gwosServer;
    }

    public void setGwosServer(String gwosServer) {
        this.gwosServer = gwosServer;
    }

    public boolean isGwosSSLEnabled() {
        return gwosSSLEnabled;
    }

    public void setGwosSSLEnabled(boolean gwosSSLEnabled) {
        this.gwosSSLEnabled = gwosSSLEnabled;
    }

    public String getWsEndPoint() {
        return wsEndPoint;
    }

    public void setWsEndPoint(String wsEndPoint) {
        this.wsEndPoint = wsEndPoint;
    }

    public String getWsHostName() {
        return wsHostName;
    }

    public void setWsHostName(String wsHostName) {
        this.wsHostName = wsHostName;
    }

    public String getWsUsername() {
        return wsUsername;
    }

    public void setWsUsername(String wsUsername) {
        this.wsUsername = wsUsername;
    }

    public String getWsPassword() {
        return wsPassword;
    }

    public void setWsPassword(String wsPassword) {
        this.wsPassword = wsPassword;
    }

    public String getWsHostGroupName() {
        return wsHostGroupName;
    }

    public void setWsHostGroupName(String wsHostGroupName) {
        this.wsHostGroupName = wsHostGroupName;
    }

    public String getRsEndPoint() {
        return rsEndPoint;
    }

    public void setRsEndPoint(String rsEndPoint) {
        this.rsEndPoint = rsEndPoint;
    }

    public String getWsPortNumber() {
        return wsPortNumber;
    }

    public void setWsPortNumber(String wsPortNumber) {
        this.wsPortNumber = wsPortNumber;
    }

    @JsonIgnore
    public boolean isMergeHostsSet() {
        return mergeHosts != null;
    }

    public boolean isMergeHosts() {
        return mergeHosts;
    }

    @JsonProperty
    public boolean getMergeHosts() { return (mergeHosts == null) ? false : mergeHosts; }

    public void setMergeHosts(boolean mergeHosts) {
        this.mergeHosts = mergeHosts;
    }

    public Boolean getMonitor() {
        return monitor;
    }

    public void setMonitor(Boolean monitor) {
        this.monitor = monitor;
    }
}