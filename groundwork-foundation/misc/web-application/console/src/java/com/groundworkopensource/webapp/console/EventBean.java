/*
 * 
 * Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")  
 * All rights reserved. This program is free software; you can redistribute it
 * and/or modify it under the terms of the GNU General Public License version 2
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for 
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51 Franklin
 * Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 */

package com.groundworkopensource.webapp.console;

/*
 * EventBean.java
 * 
 * Created on June 20, 2007, 2:33 PM
 */
import java.io.Serializable;
import java.util.Map;


/**
 * @author Arul Shanmugam
 */
public class EventBean implements Serializable {

	

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String reportDate;
	private int msgCount;
	private String device;
	private StatusBean monitorStatus;
	private String severity;
	private String applicationType;
	private String textMessage;
	private String lastInsertDate;
	private String firstInsertDate;
	private boolean selected = false;
	private Map<String,Object> dynamicProperty;
	private int logMessageID;
	private int totalCount;

	public EventBean() {
		
	}

	public String getReportDate() {
		return reportDate;
	}

	public void setReportDate(String reportDate) {
		this.reportDate = reportDate;
	}

	public int getMsgCount() {
		return msgCount;
	}

	public void setMsgCount(int msgCount) {
		this.msgCount = msgCount;
	}

	public String getDevice() {
		return device;
	}

	public void setDevice(String device) {
		this.device = device;
	}

	public StatusBean getMonitorStatus() {
		return monitorStatus;
	}

	public void setMonitorStatus(StatusBean monitorStatus) {
		this.monitorStatus = monitorStatus;
	}

	public String getSeverity() {
		return severity;
	}

	public void setSeverity(String severity) {
		this.severity = severity;
	}

	public String getApplicationType() {
		return applicationType;
	}

	public void setApplicationType(String applicationType) {
		this.applicationType = applicationType;
	}

	public String getTextMessage() {
		return textMessage;
	}

	public void setTextMessage(String textMessage) {
		this.textMessage = textMessage;
	}

	public String getLastInsertDate() {
		return lastInsertDate;
	}

	public void setLastInsertDate(String lastInsertDate) {
		this.lastInsertDate = lastInsertDate;
	}

	public String getFirstInsertDate() {
		return firstInsertDate;
	}

	public void setFirstInsertDate(String firstInsertDate) {
		this.firstInsertDate = firstInsertDate;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public Map<String,Object> getDynamicProperty() {
		return dynamicProperty;
	}

	public void setDynamicProperty(Map<String,Object> dynamicProperty) {
		this.dynamicProperty = dynamicProperty;
	}

	public int getLogMessageID() {
		return logMessageID;
	}

	public void setLogMessageID(int logMessageID) {
		this.logMessageID = logMessageID;
	}

	public int getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}

	

	

}
