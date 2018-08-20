/*
 *  Copyright (C) 2009 GroundWork Open Source, Inc. (GroundWork)
 *  All rights reserved. Use is subject to GroundWork commercial license terms.
 */

package com.groundworkopensource.webapp.console;

import org.groundwork.foundation.ws.model.impl.ServiceStatus;

public class HostDetailBean {
	
	private String hostName = ConsoleConstants.NOT_AVAILABLE;
	private String status= ConsoleConstants.NOT_AVAILABLE;
	private ServiceStatus[] serviceStatus;
	private String lastCheckTime = ConsoleConstants.NOT_AVAILABLE;
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public ServiceStatus[] getServiceStatus() {
		return serviceStatus;
	}
	public void setServiceStatus(ServiceStatus[] serviceStatus) {
		this.serviceStatus = serviceStatus;
	}
	public String getLastCheckTime() {
		return lastCheckTime;
	}
	public void setLastCheckTime(String lastCheckTime) {
		this.lastCheckTime = lastCheckTime;
	}
	public String getHostName() {
		return hostName;
	}
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}
	
	public String getBaseURL(){
	    return ConsoleHelper.getBaseURL();
	}
	
	/**
	 * TEMPORARY METHOD! returns "localhost" when "127.0.0.1" encountered.
	 */
	public String getCorrectHostName(){
	    if("127.0.0.1".equalsIgnoreCase(hostName)){
	        return "localhost";
	    }else{
	        return hostName;
	    }
	}

}
