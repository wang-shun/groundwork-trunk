package com.gwos.statusservice.beans;

import javax.xml.bind.annotation.XmlAttribute;

public class Host extends BaseEntity {
	
	private Service[] service = null;
	private String currentAttempt = "NA";
	private String maxAttempts = "NA";
	private String lastCheckTime = "NA";
	private String nextCheckTime = "NA";
	private String lastStateChange = "NA";
	private String alias = "NA";
	
	
	public Service[] getService() {
		return service;
	}

	public void setService(Service[] service) {
		this.service = service;
	}

	@XmlAttribute
	public String getCurrentAttempt() {
		return currentAttempt;
	}

	public void setCurrentAttempt(String currentAttempt) {
		this.currentAttempt = currentAttempt;
	}

	@XmlAttribute
	public String getLastCheckTime() {
		return lastCheckTime;
	}
	
	public void setLastCheckTime(String lastCheckTime) {
		this.lastCheckTime = lastCheckTime;
	}

	@XmlAttribute
	public String getNextCheckTime() {
		return nextCheckTime;
	}

	public void setNextCheckTime(String nextCheckTime) {
		this.nextCheckTime = nextCheckTime;
	}

	@XmlAttribute
	public String getLastStateChange() {
		return lastStateChange;
	}

	public void setLastStateChange(String lastStateChange) {
		this.lastStateChange = lastStateChange;
	}

	@XmlAttribute
	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	@XmlAttribute
	public String getMaxAttempts() {
		return maxAttempts;
	}

	public void setMaxAttempts(String maxAttempts) {
		this.maxAttempts = maxAttempts;
	}
	
	

}
