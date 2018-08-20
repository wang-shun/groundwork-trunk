package org.groundwork.rs.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;

@XmlRootElement(name = "event")
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoEvent extends DtoPropertiesBase {

    // Shallow attributes
    @XmlAttribute
    protected Integer id;
    @XmlAttribute
    protected String device;
    @XmlAttribute
    protected String host;
    @XmlAttribute
    protected String service;
    @XmlAttribute
    protected String operationStatus;
    @XmlAttribute
    protected String monitorStatus;
    @XmlAttribute
    protected String severity;
    @XmlAttribute
    protected String applicationSeverity;
    @XmlAttribute
    protected String component;
    @XmlAttribute
    protected String priority;
    @XmlAttribute
    protected String typeRule;
    @XmlAttribute
    protected String  textMessage;
    @XmlAttribute
    protected Date firstInsertDate;
    @XmlAttribute
    protected Date lastInsertDate;
    @XmlAttribute
    protected Date reportDate;
    @XmlAttribute
    protected Integer msgCount;
    @XmlAttribute
    protected String appType;
    @XmlAttribute
    protected Boolean stateChanged;

    // Update level attributes (update only)
    @XmlAttribute
    private String monitorServer = "localhost";
    @XmlAttribute
    private String consolidationName;
    @XmlAttribute
    private String logType;
    @XmlAttribute
    private String errorType;
    @XmlAttribute
    private String loggerName;
    @XmlAttribute
    private String applicationName;

    public DtoEvent() {
        super();
    }

    public DtoEvent(Integer id) {
        super();
        this.id = id;
    }

    public DtoEvent(String host, String operationStatus, String monitorStatus, String severity, String textMessage) {
    	this.host = host;
    	this.operationStatus = operationStatus;
    	this.monitorStatus = monitorStatus;
    	this.severity = severity;
    	this.textMessage = textMessage;
    }
    
    public DtoEvent(String host, String monitorStatus, String severity, String monitorServer, String device, Date lastInsertDate, String textMessage, Date reportDate) {
    	this.host = host;
    	this.monitorStatus = monitorStatus;
    	this.severity = severity;
    	this.monitorServer = monitorServer;
    	this.device = device;
    	this.lastInsertDate = lastInsertDate;
    	this.textMessage = textMessage;
    	this.reportDate = reportDate;
    }
    

    // Shallow accessors
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getOperationStatus() {
        return operationStatus;
    }

    public void setOperationStatus(String operationStatus) {
        this.operationStatus = operationStatus;
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

    public String getApplicationSeverity() {
        return applicationSeverity;
    }

    public void setApplicationSeverity(String applicationSeverity) {
        this.applicationSeverity = applicationSeverity;
    }

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getTypeRule() {
        return typeRule;
    }

    public void setTypeRule(String typeRule) {
        this.typeRule = typeRule;
    }

    public String getTextMessage() {
        return textMessage;
    }

    public void setTextMessage(String textMessage) {
        this.textMessage = textMessage;
    }

    public Date getFirstInsertDate() {
        return firstInsertDate;
    }

    public void setFirstInsertDate(Date firstInsertDate) {
        this.firstInsertDate = firstInsertDate;
    }

    public Date getLastInsertDate() {
        return lastInsertDate;
    }

    public void setLastInsertDate(Date lastInsertDate) {
        this.lastInsertDate = lastInsertDate;
    }

    public Date getReportDate() {
        return reportDate;
    }

    public void setReportDate(Date reportDate) {
        this.reportDate = reportDate;
    }

    public Integer getMsgCount() {
        return msgCount;
    }

    public void setMsgCount(Integer msgCount) {
        this.msgCount = msgCount;
    }

    public String getAppType() {
        return appType;
    }

    public void setAppType(String appType) {
        this.appType = appType;
    }

    // Update level accessors
    public String getMonitorServer() {
        return monitorServer;
    }

    public void setMonitorServer(String monitorServer) {
        this.monitorServer = monitorServer;
    }

    public String getConsolidationName() {
        return consolidationName;
    }

    public void setConsolidationName(String consolidation) {
        this.consolidationName = consolidation;
    }

    public String getLogType() {
        return logType;
    }

    public void setLogType(String logType) {
        this.logType = logType;
    }

    public String getErrorType() {
        return errorType;
    }

    public void setErrorType(String errorType) {
        this.errorType = errorType;
    }

    public String getLoggerName() {
        return loggerName;
    }

    public void setLoggerName(String loggerName) {
        this.loggerName = loggerName;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public Boolean getStateChanged() {
        return stateChanged;
    }

    public void setStateChanged(Boolean stateChanged) {
        this.stateChanged = stateChanged;
    }
}
