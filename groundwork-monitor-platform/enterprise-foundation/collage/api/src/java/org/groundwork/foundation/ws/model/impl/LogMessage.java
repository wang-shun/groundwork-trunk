/*
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2007  GroundWork Open Source Solutions info@groundworkopensource.com

 *     This program is free software; you can redistribute it and/or modify
 *     it under the terms of version 2 of the GNU General Public License
 *     as published by the Free Software Foundation.

 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.

 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.groundwork.foundation.ws.model.impl;

import java.util.Date;

public class LogMessage  implements java.io.Serializable 
{
    private int     logMessageID;
    private int     applicationTypeID;
    private String 	applicationName;
    private String  textMessage;    
    private Date    firstInsertDate;
    private Date    lastInsertDate;
    private Date    reportDate;
    private int     msgCount;
    
    private Device device = null;
    private MonitorStatus monitorStatus = null;
    private OperationStatus opStatus = null;
    private Severity severity = null;
    private TypeRule typeRule = null;
    private Component component = null;
    private Priority priority = null;
    private Host host = null;
    
    private PropertyTypeBinding propertyTypeBinding;
    
    private ServiceStatus serviceStatus = null;

    public LogMessage() {
    }

    public LogMessage(
           int logMessageID,
           int applicationTypeID,
           String applicationName,
           String textMessage,
           int messageCount,
           Date firstInsertDate,
           Date lastInsertDate,
           Date reportDate,
           Device device,
           MonitorStatus monitorStatus,
           OperationStatus opStatus,
           Severity severity,
           TypeRule typeRule,
           Component component,
           Priority priority,
           Host host,
           PropertyTypeBinding propertyTypeBinding) 
    {
           this.logMessageID = logMessageID;
           this.applicationTypeID = applicationTypeID;
           this.applicationName = applicationName;
           this.textMessage = textMessage;
           this.msgCount = messageCount;
           this.firstInsertDate = firstInsertDate;
           this.lastInsertDate = lastInsertDate;
           this.reportDate = reportDate;
           this.device = device;
           this.monitorStatus = monitorStatus;
           this.opStatus = opStatus;
           this.severity = severity;
           this.typeRule = typeRule;
           this.component = component;
           this.priority = priority;
           this.host = host;
           this.propertyTypeBinding = propertyTypeBinding;
    }

    /**
     * Gets the logMessageID value for this LogMessage.
     * 
     * @return logMessageID
     */
    public int getLogMessageID() {
        return logMessageID;
    }


    /**
     * Sets the logMessageID value for this LogMessage.
     * 
     * @param logMessageID
     */
    public void setLogMessageID(int logMessageID) {
        this.logMessageID = logMessageID;
    }


    /**
     * Gets the applicationTypeID value for this LogMessage.
     * 
     * @return applicationTypeID
     */
    public int getApplicationTypeID() {
        return applicationTypeID;
    }


    /**
     * Sets the applicationTypeID value for this LogMessage.
     * 
     * @param applicationTypeID
     */
    public void setApplicationTypeID(int applicationTypeID) {
        this.applicationTypeID = applicationTypeID;
    }

    /**
     * Gets the applicationName value for this LogMessage.
     * 
     * @return applicationName
     */
    public String getApplicationName() {
        return this.applicationName;
    }


    /**
     * Sets the applicationName value for this LogMessage.
     * 
     * @param applicationName
     */
    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }
    
    /**
     * Gets the propertyTypeBinding value for this LogMessage.
     * 
     * @return propertyTypeBinding
     */
    public PropertyTypeBinding getPropertyTypeBinding() {
        return propertyTypeBinding;
    }
    
    /**
	 * @return Returns the textMessage.
	 */
	public String getTextMessage() {
		return textMessage;
	}

	/**
	 * @param textMessage The textMessage to set.
	 */
	public void setTextMessage(String textMessage) {
		this.textMessage = textMessage;
	}

       /**
     * @return Returns the firstInsertDate.
     */
    public Date getFirstInsertDate() {
        return this.firstInsertDate;
    }

    /**
     * @param firstInsertDate The firstInsertDate to set.
     */
    public void setFirstInsertDate(Date firstInsertDate) {
        this.firstInsertDate = firstInsertDate;
    }

    /**
     * @return Returns the lastInsertDate.
     */
    public Date getLastInsertDate() {
        return this.lastInsertDate;
    }

    /**
     * @param lastInsertDate The lastInsertDate to set.
     */
    public void setLastInsertDate(Date lastInsertDate) {
        this.lastInsertDate = lastInsertDate;
    }

    /**
     * @return Returns the msgCount.
     */
    public int getMessageCount() {
        return this.msgCount;
    }

    /**
     * @param msgCount The msgCount to set.
     */
    public void setMessageCount(int msgCount) {
        this.msgCount = msgCount;
    }

    /**
     * @return Returns the reportDate.
     */
    public Date getReportDate() {
        return this.reportDate;
    }

    /**
     * @param reportDate The reportDate to set.
     */
    public void setReportDate(Date reportDate) {
        this.reportDate = reportDate;
    }
    
    public Device getDevice ()
    {
    	return device;
    }
    
    public void setDevice (Device device)
    {
    	this.device = device;
    }
    
    public MonitorStatus getMonitorStatus ()
    {
    	return monitorStatus;
    }
    
    public void setMonitorStatus (MonitorStatus monitorStatus)
    {
    	this.monitorStatus = monitorStatus;
    }
    
    public OperationStatus getOperationStatus ()
    {
    	return opStatus;
    }
    
    public void setOperationStatus (OperationStatus opStatus)
    {
    	this.opStatus = opStatus;
    }
    
    public Severity getSeverity ()
    {
    	return severity;
    }
    
    public void setSeverity (Severity severity)
    {
    	this.severity = severity;
    }    
    
    public TypeRule getTypeRule ()
    {
    	return typeRule;
    }
    
    public void setTypeRule (TypeRule typeRule)
    {
    	this.typeRule = typeRule;
    }  
    
    public Component getComponent ()
    {
    	return component;
    }
    
    public void setComponent (Component component)
    {
    	this.component = component;
    }  
    
    public Priority getPriority ()
    {
    	return priority;
    }
    
    public void setPriority (Priority priority)
    {
    	this.priority = priority;
    }     
    
    public Host getHost ()
    {
    	return host;
    }
    
    public void setHost (Host host)
    {
    	this.host = host;
    }
    
    public ServiceStatus getServiceStatus() {
		return serviceStatus;
	}

	public void setServiceStatus(ServiceStatus serviceStatus) {
		this.serviceStatus = serviceStatus;
	}
    
    /**
     * Sets the propertyTypeBinding value for this LogMessage.
     * 
     * @param propertyTypeBinding
     */
    public void setPropertyTypeBinding(PropertyTypeBinding propertyTypeBinding) {
        this.propertyTypeBinding = propertyTypeBinding;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        LogMessage other = (LogMessage) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            this.logMessageID == other.getLogMessageID() &&
            this.applicationTypeID == other.getApplicationTypeID() &&
            ((this.textMessage==null && other.getTextMessage()==null) || 
                    (this.textMessage!=null &&
                     this.textMessage.equals(other.getTextMessage()))) &&
            ((this.firstInsertDate==null && other.getFirstInsertDate()==null) || 
                    (this.firstInsertDate!=null &&
                     this.firstInsertDate.equals(other.getFirstInsertDate()))) &&
            ((this.lastInsertDate==null && other.getLastInsertDate()==null) || 
                    (this.lastInsertDate!=null &&
                     this.lastInsertDate.equals(other.getLastInsertDate()))) &&
            ((this.reportDate==null && other.getReportDate()==null) || 
                    (this.reportDate!=null &&
                     this.reportDate.equals(other.getReportDate()))) &&
            this.msgCount == other.getMessageCount() &&
            ((this.device==null && other.getDevice()==null) || 
                    (this.device!=null &&
                     this.device.equals(other.getDevice()))) &&
            ((this.monitorStatus==null && other.getMonitorStatus()==null) || 
             (this.monitorStatus!=null &&
              this.monitorStatus.equals(other.getMonitorStatus()))) &&              
            ((this.opStatus==null && other.getOperationStatus()==null) || 
                    (this.opStatus!=null &&
                     this.opStatus.equals(other.getOperationStatus()))) &&
            ((this.severity==null && other.getSeverity()==null) || 
             (this.severity!=null &&
              this.severity.equals(other.getSeverity()))) &&   
            ((this.typeRule==null && other.getTypeRule()==null) || 
             (this.typeRule!=null &&
              this.typeRule.equals(other.getTypeRule()))) &&
            ((this.component==null && other.getComponent()==null) || 
             (this.component!=null &&
              this.component.equals(other.getComponent()))) &&    
            ((this.priority==null && other.getPriority()==null) || 
             (this.priority!=null &&
              this.priority.equals(other.getPriority()))) &&    
            ((this.host==null && other.getHost()==null) || 
             (this.host!=null &&
              this.host.equals(other.getHost()))) && 
              ((this.serviceStatus==null && other.getServiceStatus()==null) || 
               (this.serviceStatus!=null &&
               this.serviceStatus.equals(other.getServiceStatus()))) && 
            ((this.propertyTypeBinding==null && other.getPropertyTypeBinding()==null) || 
             (this.propertyTypeBinding!=null &&
              this.propertyTypeBinding.equals(other.getPropertyTypeBinding())));
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc = false;
    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        _hashCode += getLogMessageID();
        _hashCode += getApplicationTypeID();
        if (getPropertyTypeBinding() != null) {
            _hashCode += getPropertyTypeBinding().hashCode();
        }
        if (getTextMessage() != null) {
            _hashCode += getTextMessage().hashCode();
        }  
        if (getDevice() != null) {
            _hashCode += getDevice().hashCode();
        }
        if (getMonitorStatus() != null) {
            _hashCode += getMonitorStatus().hashCode();
        }           
        if (getOperationStatus() != null) {
            _hashCode += getOperationStatus().hashCode();
        }
        if (getSeverity() != null) {
            _hashCode += getSeverity().hashCode();
        }    
        if (getTypeRule() != null) {
            _hashCode += getTypeRule().hashCode();
        }
        if (getComponent() != null) {
            _hashCode += getComponent().hashCode();
        }
        if (getPriority() != null) {
            _hashCode += getPriority().hashCode();
        }   
        if (getHost() != null) {
            _hashCode += getHost().hashCode();
        }
        if (getServiceStatus() != null) {
            _hashCode += getServiceStatus().hashCode();
        }  
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(LogMessage.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "LogMessage"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("logMessageID");
        elemField.setXmlName(new javax.xml.namespace.QName("", "LogMessageID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("applicationTypeID");
        elemField.setXmlName(new javax.xml.namespace.QName("", "ApplicationTypeID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);

        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("applicationName");
        elemField.setXmlName(new javax.xml.namespace.QName("", "ApplicationName"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("textMessage");
        elemField.setXmlName(new javax.xml.namespace.QName("", "TextMessage"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);

        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("messageCount");
        elemField.setXmlName(new javax.xml.namespace.QName("", "MessageCount"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("firstInsertDate");
        elemField.setXmlName(new javax.xml.namespace.QName("", "FirstInsertDate"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "dateTime"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("lastInsertDate");
        elemField.setXmlName(new javax.xml.namespace.QName("", "LastInsertDate"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "dateTime"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("reportDate");
        elemField.setXmlName(new javax.xml.namespace.QName("", "ReportDate"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "dateTime"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("operationStatus");
        elemField.setXmlName(new javax.xml.namespace.QName("", "OperationStatus"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "OperationStatus"));
        elemField.setNillable(false);
        elemField.setMinOccurs(0);
        elemField.setMaxOccurs(1);
        typeDesc.addFieldDesc(elemField);
        
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("severity");
        elemField.setXmlName(new javax.xml.namespace.QName("", "Severity"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "Severity"));
        elemField.setNillable(false);
        elemField.setMinOccurs(0);
        elemField.setMaxOccurs(1);        
        typeDesc.addFieldDesc(elemField);

        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("monitorStatus");
        elemField.setXmlName(new javax.xml.namespace.QName("", "MonitorStatus"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "MonitorStatus"));
        elemField.setNillable(true);
        elemField.setMinOccurs(0);
        elemField.setMaxOccurs(1);
        typeDesc.addFieldDesc(elemField);

        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("device");
        elemField.setMinOccurs(0);
        elemField.setMaxOccurs(1);
        elemField.setXmlName(new javax.xml.namespace.QName("", "Device"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "Device"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("typeRule");
        elemField.setMinOccurs(0);
        elemField.setMaxOccurs(1);
        elemField.setXmlName(new javax.xml.namespace.QName("", "TypeRule"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "TypeRule"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("component");
        elemField.setMinOccurs(0);
        elemField.setMaxOccurs(1);
        elemField.setXmlName(new javax.xml.namespace.QName("", "Component"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "Component"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("priority");
        elemField.setMinOccurs(0);
        elemField.setMaxOccurs(1);
        elemField.setXmlName(new javax.xml.namespace.QName("", "Priority"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "Priority"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("host");
        elemField.setMinOccurs(0);
        elemField.setMaxOccurs(1);
        elemField.setXmlName(new javax.xml.namespace.QName("", "Host"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "Host"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("serviceDescription");
        elemField.setMinOccurs(0);
        elemField.setMaxOccurs(1);
        elemField.setXmlName(new javax.xml.namespace.QName("", "ServiceDescription"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "ServiceDescription"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("propertyTypeBinding");
        elemField.setXmlName(new javax.xml.namespace.QName("", "PropertyTypeBinding"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "PropertyTypeBinding"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
    }

    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

    /**
     * Get Custom Serializer
     */
    public static org.apache.axis.encoding.Serializer getSerializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanSerializer(
            _javaType, _xmlType, typeDesc);
    }

    /**
     * Get Custom Deserializer
     */
    public static org.apache.axis.encoding.Deserializer getDeserializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanDeserializer(
            _javaType, _xmlType, typeDesc);
    }
}
