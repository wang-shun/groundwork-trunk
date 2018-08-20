package com.groundworkopensource.webapp.console;

public class FilterConfigBean {
	private String name = null;
	private String label = null;
	private String appType = null;
	private String hostGroup = null;
	private String monitorStatus = null;
	private String severity = null;
	private FetchConfigBean fetch = null;
	private TimeConfigBean time = null;
	private String opStatus = null;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public String getAppType() {
		return appType;
	}
	public void setAppType(String appType) {
		this.appType = appType;
	}
	
	
	public TimeConfigBean getTime() {
		return time;
	}
	public void setTime(TimeConfigBean time) {
		this.time = time;
	}
	public String getOpStatus() {
		return opStatus;
	}
	public void setOpStatus(String opStatus) {
		this.opStatus = opStatus;
	}
	public FetchConfigBean getFetch() {
		return fetch;
	}
	public void setFetch(FetchConfigBean fetch) {
		this.fetch = fetch;
	}
	public String getMonitorStatus() {
		return monitorStatus;
	}
	public void setMonitorStatus(String monitorStatus) {
		this.monitorStatus = monitorStatus;
	}
	public String getSeverity() {
		return severity;
	}
	public void setSeverity(String severity) {
		this.severity = severity;
	}
	public String getHostGroup() {
		return hostGroup;
	}
	public void setHostGroup(String hostGroup) {
		this.hostGroup = hostGroup;
	}
	

}
