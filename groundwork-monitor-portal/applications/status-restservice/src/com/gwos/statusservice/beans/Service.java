package com.gwos.statusservice.beans;

import javax.xml.bind.annotation.XmlAttribute;

public class Service extends BaseEntity {
	private String hostname = null;
	private String currentAttempt = "NA"; 
	private String maxAttempts = "NA";
	private String lastCheckTime = "NA";
	private String nextCheckTime = "NA";
	private String lastStateChange = "NA";
	private String alias = "NA";
	
	private String perfData = "NA";

	@XmlAttribute
	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
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
	public String getPerfData() {
		return perfData;
	}

	public void setPerfData(String perfData) {
		this.perfData = perfData;
	}

	@XmlAttribute
	public String getMaxAttempts() {
		return maxAttempts;
	}

	public void setMaxAttempts(String maxAttempts) {
		this.maxAttempts = maxAttempts;
	}

}
