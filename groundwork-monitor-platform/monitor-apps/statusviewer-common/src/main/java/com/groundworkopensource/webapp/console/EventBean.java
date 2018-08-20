/*
 * Copyright (C) 2009 GroundWork Open Source, Inc. (GroundWork) All rights
 * reserved. Use is subject to GroundWork commercial license terms.
 */

package com.groundworkopensource.webapp.console;

/*
 * EventBean_delete.java
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
    private static final long serialVersionUID = 2L;
    private String reportDate;
    private int msgCount;
    private String device;
    private StatusBean monitorStatus;
    private SeverityBean severity;
    private String applicationType;
    private String textMessage;
    private String textMessageFull;
    private String lastInsertDate;
    private String firstInsertDate;
    private boolean selected = false;
    private Map<String, Object> dynamicProperty;
    private int logMessageID;
    private int totalCount;
    private String operationStatus = null;
    private String host = null;
    private String serviceDescription = null;

    public String getServiceDescriptionShort() {
        return serviceDescriptionShort;
    }

    public void setServiceDescriptionShort(String serviceDescriptionShort) {
        this.serviceDescriptionShort = serviceDescriptionShort;
    }

    private String serviceDescriptionShort = null;
    private String ticketID = null;
    private String commentsShort = null;

    public String getServiceLinkVisible() {
        if ((serviceDescription != null && !serviceDescription.equalsIgnoreCase("") && ConsoleHelper.isLinksEnabled())) {
            return "display:inline;float:left;";
        }
        return "display:none";
    }

    public String getServiceTextVisible() {
        if ((serviceDescription != null && !serviceDescription.equalsIgnoreCase("") && !ConsoleHelper.isLinksEnabled())) {
            return "display:inline";
        }
        return "display:none";
    }


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

    public SeverityBean getSeverity() {
        return severity;
    }

    public void setSeverity(SeverityBean severity) {
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
    
    public String getTextMessageFull() {
        return textMessageFull;
    }

    public void setTextMessageFull(String textMessageFull) {
        this.textMessageFull = textMessageFull;
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

    public Map<String, Object> getDynamicProperty() {
        return dynamicProperty;
    }

    public void setDynamicProperty(Map<String, Object> dynamicProperty) {
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

    public String getDeviceUrl() {
        return ConsoleHelper
                .buildStatusviewerURL(ConsoleConstants.HOST, host);
    }

    public String getServiceUrl() {
        String serviceUrl = "";
        if (null != serviceDescription) {
            serviceUrl = ConsoleHelper.buildStatusviewerURL(
                    ConsoleConstants.SERVICE, serviceDescription, ConsoleConstants.HOST, host);
        }
        return serviceUrl;
    }

    /**
     * Returns the linkVisible.
     * 
     * @return the linkVisible string value
     */
    public String getDeviceLinkVisible() {
        if (ConsoleHelper.isLinksEnabled()) {
            return "display:inline;float:left;";
        }
        return "display:none";
    }

    /**
     * Returns the linkVisible.
     * 
     * @return the linkVisible string value
     */
    public String getDeviceTextVisible() {
        if (!ConsoleHelper.isLinksEnabled()) {
            return "display:inline";
        }
        return "display:none";
    }

	public String getOperationStatus() {
		return operationStatus;
	}

	public void setOperationStatus(String operationStatus) {
		this.operationStatus = operationStatus;
	}
	
	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}
	
	public String getServiceDescription() {
		return serviceDescription;
	}

	public void setServiceDescription(String serviceDescription) {
		this.serviceDescription = serviceDescription;
	}
	
	public String getTicketID() {
		return ticketID;
	}

	public void setTicketID(String ticketID) {
		this.ticketID = ticketID;
	}
	
	public String getCommentsShort() {
		return commentsShort;
	}

	public void setCommentsShort(String commentsShort) {
		this.commentsShort = commentsShort;
	}

}
