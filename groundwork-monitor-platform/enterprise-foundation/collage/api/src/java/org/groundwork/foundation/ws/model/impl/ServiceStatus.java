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

public class ServiceStatus  implements java.io.Serializable 
{
    private int serviceStatusID;
    private int applicationTypeID;
    private String description;
    private MonitorStatus monitorStatus;
    private Date lastCheckTime;
    private Date nextCheckTime;
    private Date lastStateChange;
    private Host host;
    private String metricType;
    private String domain;
    private StateType stateType;
    private CheckType checkType;
    private MonitorStatus lastHardState;
    
    private PropertyTypeBinding propertyTypeBinding;

    public ServiceStatus() {
    }

    public ServiceStatus(
           int serviceStatusID,
           int applicationTypeID,
           String description,
           Host host,
           MonitorStatus monitorStatus,           
           Date lastCheckTime,
           Date nextCheckTime,
           Date lastStateChange,
           String metricType,
           String domain,
           StateType stateType,
           CheckType checkType,
           MonitorStatus lastHardState,
           PropertyTypeBinding propertyTypeBinding) 
    {
           this.serviceStatusID = serviceStatusID;
           this.applicationTypeID = applicationTypeID;
           this.description = description;
           this.host = host;
           this.monitorStatus = monitorStatus;           
           this.lastCheckTime = lastCheckTime;
           this.nextCheckTime = nextCheckTime;
           this.lastStateChange = lastStateChange;
           this.metricType = metricType;
           this.domain = domain;
           this.stateType = stateType;
           this.checkType = checkType;
           this.propertyTypeBinding = propertyTypeBinding;
    }

    /**
     * Gets the serviceStatusID value for this ServiceStatus.
     * 
     * @return serviceStatusID
     */
    public int getServiceStatusID() {
        return serviceStatusID;
    }


    /**
     * Sets the serviceStatusID value for this ServiceStatus.
     * 
     * @param serviceStatusID
     */
    public void setServiceStatusID(int serviceStatusID) {
        this.serviceStatusID = serviceStatusID;
    }


    /**
     * Gets the applicationTypeID value for this ServiceStatus.
     * 
     * @return applicationTypeID
     */
    public int getApplicationTypeID() {
        return applicationTypeID;
    }


    /**
     * Sets the applicationTypeID value for this ServiceStatus.
     * 
     * @param applicationTypeID
     */
    public void setApplicationTypeID(int applicationTypeID) {
        this.applicationTypeID = applicationTypeID;
    }

    /**
     * Gets the description value for this ServiceStatus.
     * 
     * @return description
     */
    public String getDescription() {
        return description;
    }


    /**
     * Sets the description value for this ServiceStatus.
     * 
     * @param description
     */
    public void setDescription(String description) {
        this.description = description;
    }    

    /**
     * Gets the monitorStatus value for this ServiceStatus.
     * 
     * @return monitorStatus
     */
    public Host getHost() 
    {
        return host;
    }

    /**
     * Sets the monitorStatus value for this ServiceStatus.
     * 
     * @param monitorStatus
     */
    public void setHost(Host host) {
        this.host = host;
    }
    
    /**
     * Gets the monitorStatus value for this ServiceStatus.
     * 
     * @return monitorStatus
     */
    public MonitorStatus getMonitorStatus() 
    {
        return monitorStatus;
    }


    /**
     * Sets the monitorStatus value for this ServiceStatus.
     * 
     * @param monitorStatus
     */
    public void setMonitorStatus(MonitorStatus monitorStatus) {
        this.monitorStatus = monitorStatus;
    }
    
    /**
     * Gets the lastCheckTime value for this ServiceStatus.
     * 
     * @return lastCheckTime
     */
    public Date getLastCheckTime() 
    {
        return lastCheckTime;
    }


    /**
     * Sets the lastCheckTime value for this ServiceStatus.
     * 
     * @param lastCheckTime
     */
    public void setLastCheckTime(Date lastCheckTime) {
        this.lastCheckTime = lastCheckTime;
    }    

    /**
     * Gets the nextCheckTime value for this ServiceStatus.
     * 
     * @return nextCheckTime
     */
    public Date getNextCheckTime() 
    {
        return nextCheckTime;
    }


    /**
     * Sets the nextCheckTime value for this ServiceStatus.
     * 
     * @param lastCheckTime
     */
    public void setNextCheckTime(Date nextCheckTime) {
        this.nextCheckTime = nextCheckTime;
    }  
    
    /**
     * Gets the lastCheckTime value for this ServiceStatus.
     * 
     * @return lastCheckTime
     */
    public Date getLastStateChange() 
    {
        return lastStateChange;
    }

    /**
     * Sets the lastStateChange value for this ServiceStatus.
     * 
     * @param lastStateChange
     */
    public void setLastStateChange(Date lastStateChange) {
        this.lastStateChange = lastStateChange;
    }      
    
    public String getMetricType ()
    {
    	return metricType;
    }
    
    public void setMetricType (String metricType)
    {
    	this.metricType = metricType;
    }
    
    public String getDomain ()
    {
    	return domain;
    }
    
    public void setDomain (String domain)
    {
    	this.domain = domain;
    }
    
    public StateType getStateType ()
    {
    	return stateType;
    }
    
    public void setStateType (StateType stateType)
    {
    	this.stateType = stateType;
    }
    
    public CheckType getCheckType ()
    {
    	return checkType;
    }
    
    public void setCheckType (CheckType checkType)
    {
    	this.checkType = checkType;
    }
    
    public MonitorStatus getLastHardState ()
    {
    	return lastHardState;
    }
    
    public void setLastHardState (MonitorStatus lastHardState)
    {
    	this.lastHardState = lastHardState;
    }    
    
    /**
     * Gets the propertyTypeBinding value for this ServiceStatus.
     * 
     * @return propertyTypeBinding
     */
    public PropertyTypeBinding getPropertyTypeBinding() {
        return propertyTypeBinding;
    }

    /**
     * Sets the propertyTypeBinding value for this ServiceStatus.
     * 
     * @param propertyTypeBinding
     */
    public void setPropertyTypeBinding(PropertyTypeBinding propertyTypeBinding) {
        this.propertyTypeBinding = propertyTypeBinding;
    }
    
    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ServiceStatus)) return false;
        ServiceStatus other = (ServiceStatus) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            this.serviceStatusID == other.getServiceStatusID() &&
            this.applicationTypeID == other.getApplicationTypeID() &&
            ((this.description==null && other.getDescription()==null) || 
                    (this.description!=null && this.description.equals(other.getDescription()))) &&
            ((this.host==null && other.getHost()==null) || 
                    (this.host!=null && this.host.equals(other.getHost()))) &&                   
            ((this.monitorStatus==null && other.getMonitorStatus()==null) || 
             (this.monitorStatus!=null && this.monitorStatus.equals(other.getMonitorStatus()))) &&
            ((this.lastCheckTime==null && other.getLastCheckTime()==null) || 
             (this.lastCheckTime!=null && this.lastCheckTime.equals(other.getLastCheckTime()))) &&
            ((this.nextCheckTime==null && other.getNextCheckTime()==null) || 
             (this.nextCheckTime!=null && this.nextCheckTime.equals(other.getNextCheckTime())))  &&
            ((this.lastStateChange==null && other.getLastStateChange()==null) || 
             (this.lastStateChange!=null && this.lastStateChange.equals(other.getLastStateChange()))) &&
            ((this.metricType==null && other.getMetricType()==null) || 
             (this.metricType!=null && this.metricType.equals(other.getMetricType()))) &&                    
            ((this.domain==null && other.getDomain()==null) || 
             (this.domain!=null && this.domain.equals(other.getDomain()))) &&
            ((this.stateType==null && other.getStateType()==null) || 
             (this.stateType!=null && this.stateType.equals(other.getStateType()))) &&                    
            ((this.checkType==null && other.getCheckType()==null) || 
             (this.checkType!=null && this.checkType.equals(other.getCheckType()))) &&
            ((this.lastHardState==null && other.getLastHardState()==null) || 
             (this.lastHardState!=null && this.lastHardState.equals(other.getLastHardState()))) &&                                                 
            ((this.propertyTypeBinding==null && other.getPropertyTypeBinding()==null) || 
             (this.propertyTypeBinding!=null && this.propertyTypeBinding.equals(other.getPropertyTypeBinding())));
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
        _hashCode += getServiceStatusID();
        _hashCode += getApplicationTypeID();
        if (getDescription() != null) {
            _hashCode += getDescription().hashCode();
        }      
        if (getHost() != null) {
            _hashCode += getHost().hashCode();
        }        
        if (getMonitorStatus() != null) {
            _hashCode += getMonitorStatus().hashCode();
        }
        if (getLastCheckTime() != null) {
            _hashCode += getLastCheckTime().hashCode();
        }
        if (getNextCheckTime() != null) {
            _hashCode += getNextCheckTime().hashCode();
        }
        if (getLastStateChange() != null) {
            _hashCode += getLastStateChange().hashCode();
        }      
        if (getMetricType() != null) {
            _hashCode += getMetricType().hashCode();
        }  
        if (getDomain() != null) {
            _hashCode += getDomain().hashCode();
        }  
        if (getStateType() != null) {
            _hashCode += getStateType().hashCode();
        }  
        if (getCheckType() != null) {
            _hashCode += getCheckType().hashCode();
        }  
        if (getLastHardState() != null) {
            _hashCode += getLastHardState().hashCode();
        }          
        if (getPropertyTypeBinding() != null) {
            _hashCode += getPropertyTypeBinding().hashCode();
        }        
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(ServiceStatus.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "ServiceStatus"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("serviceStatusID");
        elemField.setXmlName(new javax.xml.namespace.QName("", "ServiceStatusID"));
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
        elemField.setFieldName("description");
        elemField.setXmlName(new javax.xml.namespace.QName("", "Description"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("host");
        elemField.setXmlName(new javax.xml.namespace.QName("", "Host"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "Host"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("monitorStatus");
        elemField.setXmlName(new javax.xml.namespace.QName("", "MonitorStatus"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "MonitorStatus"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("lastCheckTime");
        elemField.setXmlName(new javax.xml.namespace.QName("", "LastCheckTime"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "dateTime"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);   
        
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("nextCheckTime");
        elemField.setXmlName(new javax.xml.namespace.QName("", "NextCheckTime"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "dateTime"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);   
        
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("lastStateChange");
        elemField.setXmlName(new javax.xml.namespace.QName("", "LastStateChange"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "dateTime"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);           
        
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("metricType");
        elemField.setXmlName(new javax.xml.namespace.QName("", "MetricType"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);    
        
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("domain");
        elemField.setXmlName(new javax.xml.namespace.QName("", "Domain"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);    
        
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("stateType");
        elemField.setXmlName(new javax.xml.namespace.QName("", "StateType"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "StateType"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);    
        
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("checkType");
        elemField.setXmlName(new javax.xml.namespace.QName("", "CheckType"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "CheckType"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);    
        
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("lastHardState");
        elemField.setXmlName(new javax.xml.namespace.QName("", "LastHardState"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "MonitorStatus"));
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
