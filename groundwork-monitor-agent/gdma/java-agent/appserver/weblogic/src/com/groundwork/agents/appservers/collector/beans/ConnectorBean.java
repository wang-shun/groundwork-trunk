package com.groundwork.agents.appservers.collector.beans;

public class ConnectorBean {
	
	
	private String userName = "admin";
	private String password = null;	
	private String protocol="t3";
	private String hostName="localhost";
	private String port = "7001";
	private String nagiosHostname =null;
	private String nagiosPort ="5667";
	private String nagiosEncryption= "1";
	private String nagiosPassword = null;
	private String passiveCheckInterval = "600";
	private String objectFilter = "*:*";
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
	public String getProtocol() {
		return protocol;
	}
	public void setProtocol(String protocol) {
		this.protocol = protocol;
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
	
	public String getObjectFilter() {
		return objectFilter;
	}
	public void setObjectFilter(String objectFilter) {
		this.objectFilter = objectFilter;
	}
	
	

}
