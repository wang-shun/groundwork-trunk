package com.groundwork.agents.appservers.collector.beans;

public class ConnectorBean {
	
	
	
	private String javaNamingProviderURL="jnp://localhost:1099";
	private String javaNamingFactoryInitial = "org.jnp.interfaces.NamingContextFactory";
	private String javaNamingFactoryURLPackages= "org.jboss.naming:org.jnp.interfaces";
	private String nagiosHostname =null;
	private String nagiosPort ="5667";
	private String nagiosEncryption= "1";
	private String nagiosPassword = null;
	private String passiveCheckInterval = "600";
	private String objectNameFilter = "*:*";
	private String instanceId=null;
	
	public String getJavaNamingProviderURL() {
		return javaNamingProviderURL;
	}
	public void setJavaNamingProviderURL(String javaNamingProviderURL) {
		this.javaNamingProviderURL = javaNamingProviderURL;
	}
	public String getJavaNamingFactoryInitial() {
		return javaNamingFactoryInitial;
	}
	public void setJavaNamingFactoryInitial(String javaNamingFactoryInitial) {
		this.javaNamingFactoryInitial = javaNamingFactoryInitial;
	}
	public String getJavaNamingFactoryURLPackages() {
		return javaNamingFactoryURLPackages;
	}
	public void setJavaNamingFactoryURLPackages(String javaNamingFactoryURLPackages) {
		this.javaNamingFactoryURLPackages = javaNamingFactoryURLPackages;
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
	public String getObjectNameFilter() {
		return objectNameFilter;
	}
	public void setObjectNameFilter(String objectNameFilter) {
		this.objectNameFilter = objectNameFilter;
	}
	
	public String getInstanceId() {
		return instanceId;
	}
	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}
	

}
