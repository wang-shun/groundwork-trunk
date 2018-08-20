package com.groundwork.agents.appservers.collector.beans;

public class ConnectorBean {
	
	private boolean connectorSecurityEnabled = true;
	private String userName = "admin";
	private String password = null;
	private String sslTruststorePath = "/opt/IBM/WebSphere/AppServer/profiles/AppSrv01/etc/DummyClientTrustFile.jks";
	private String sslKeystorePath = "/opt/IBM/WebSphere/AppServer/profiles/AppSrv01/etc/DummyClientKeyFile.jks";
	private String sslTruststorePassword = "WebAS";
	private String sslKeystorePassword="WebAS";
	private String hostName="localhost";
	private String port = "8880";
	private String nagiosHostname =null;
	private String nagiosPort ="5667";
	private String nagiosEncryption= "1";
	private String nagiosPassword = null;
	private String passiveCheckInterval = "600";
	public boolean isConnectorSecurityEnabled() {
		return connectorSecurityEnabled;
	}
	public void setConnectorSecurityEnabled(boolean connectorSecurityEnabled) {
		this.connectorSecurityEnabled = connectorSecurityEnabled;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getSslTruststorePath() {
		return sslTruststorePath;
	}
	public void setSslTruststorePath(String sslTruststorePath) {
		this.sslTruststorePath = sslTruststorePath;
	}
	public String getSslKeystorePath() {
		return sslKeystorePath;
	}
	public void setSslKeystorePath(String sslKeystorePath) {
		this.sslKeystorePath = sslKeystorePath;
	}
	public String getSslTruststorePassword() {
		return sslTruststorePassword;
	}
	public void setSslTruststorePassword(String sslTruststorePassword) {
		this.sslTruststorePassword = sslTruststorePassword;
	}
	public String getSslKeystorePassword() {
		return sslKeystorePassword;
	}
	public void setSslKeystorePassword(String sslKeystorePassword) {
		this.sslKeystorePassword = sslKeystorePassword;
	}
	public String getHostName() {
		return hostName;
	}
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}
	public String getPort() {
		return port;
	}
	public void setPort(String port) {
		this.port = port;
	}
	public String getNagiosHostname() {
		return nagiosHostname;
	}
	public void setNagiosHostname(String nagiosHostname) {
		this.nagiosHostname = nagiosHostname;
	}
	public String getNagiosPort() {
		return nagiosPort;
	}
	public void setNagiosPort(String nagiosPort) {
		this.nagiosPort = nagiosPort;
	}
	public String getNagiosEncryption() {
		return nagiosEncryption;
	}
	public void setNagiosEncryption(String nagiosEncryption) {
		this.nagiosEncryption = nagiosEncryption;
	}
	public String getNagiosPassword() {
		return nagiosPassword;
	}
	public void setNagiosPassword(String nagiosPassword) {
		this.nagiosPassword = nagiosPassword;
	}
	public String getPassiveCheckInterval() {
		return passiveCheckInterval;
	}
	public void setPassiveCheckInterval(String passiveCheckInterval) {
		this.passiveCheckInterval = passiveCheckInterval;
	}
	

}
