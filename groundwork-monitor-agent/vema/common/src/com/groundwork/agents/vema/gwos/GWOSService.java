/*
 * Copyright 2012 GroundWork , Inc. ("GroundWork") 
 * All rights reserved. 
*/
package com.groundwork.agents.vema.gwos;

import com.groundwork.agents.vema.api.GWOSEntity;

/**
 * @author rruttimann@gwos.com
 * Created: Jul 18, 2012
 */
public class GWOSService extends GWOSEntity
{
	static final String	XML_HEAD	    =	"<Service ";

	// Local members
	private String hostName				=	null;
	private String serviceDescription	=	null;
	private String checkType			=	"ACTIVE";
	private String stateType			= 	"HARD";
	private String monitorStatus		= 	"PENDING";
	private String lastStateChange		=	null;
	private String lastHardState		=	"PENDING";
	
	// Monitoring attributes
	private String	lastCheckTime		=	null;
	private String	lastPluginOutput	=	null;
	private String	nextCheckTime		=	null;
	private String	performanceData		=	null;
	
	public GWOSService(String hostName, String ServiceDescription, String lastStateChange, String monitorStatus, String lastHardState)
	{
		this.hostName	        = hostName;
		this.serviceDescription	= ServiceDescription;
		this.lastStateChange    = lastStateChange;
		this.lastHardState      = lastHardState;
		
		if (monitorStatus != null) 
			this.monitorStatus  = monitorStatus;

		setXmlHead(XML_HEAD);
		
		// Add attributes for Service
		addAttribute("Host",               this.hostName);
		addAttribute("ServiceDescription", this.serviceDescription);
		addAttribute("CheckType",          this.checkType);
		addAttribute("StateType",          this.stateType);
		addAttribute("MonitorStatus",      this.monitorStatus);
		addAttribute("LastStateChange",    this.lastStateChange);
		addAttribute("LastHardState" ,     this.lastHardState);
		
	}

	public void setLastCheckTime(String lastCheckTime) 
	{
		if (lastCheckTime != null) {
			this.lastCheckTime = lastCheckTime;
			addAttribute("LastCheckTime" , this.lastCheckTime);
		}
	}

	public void setLastPluginOutput(String lastPluginOutput) 
	{
		if (lastPluginOutput != null) {
			this.lastPluginOutput = lastPluginOutput;
			addAttribute("LastPluginOutput", this.lastPluginOutput);
			
		}
	}

	public void setNextCheckTime(String nextCheckTime) {
		if (nextCheckTime != null) {
			this.nextCheckTime = nextCheckTime;
			addAttribute("NextCheckTime", this.nextCheckTime);
		}
	}

	public void setPerformanceData(String performanceData) {
		if (performanceData !=null) {
			this.performanceData = performanceData;
			addAttribute("PerformanceData", this.performanceData);
		}
	}
}
