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

public class Host implements java.io.Serializable 
{
    private int hostID;

    private int applicationTypeID;

    private Device device;

    private java.lang.String name;
    
    private MonitorStatus monitorStatus;
    
    private Date lastCheckTime;
    
    private Date nextCheckTime;
    
    private PropertyTypeBinding propertyTypeBinding;
    
    private HostGroup[] hostGroups;
    private double serviceAvailability = 0;
    
    private StateType stateType;
    
    private CheckType checkType;

    public Host() {
    }

    public Host(
           int hostID,
           int applicationTypeID,
           Device device,
           java.lang.String name,
           MonitorStatus monitorStatus,
           Date lastCheckTime,
           Date nextCheckTime,
           PropertyTypeBinding propertyTypeBinding, StateType stateType,  CheckType checkType) 
    {
           this.hostID = hostID;
           this.applicationTypeID = applicationTypeID;
           this.device = device;
           this.name = name;
           this.monitorStatus = monitorStatus;
           this.lastCheckTime = lastCheckTime;
           this.nextCheckTime = nextCheckTime;
           this.propertyTypeBinding = propertyTypeBinding;
           this.stateType = stateType;
           this.checkType = checkType;
    }


    /**
     * Gets the hostID value for this Host.
     * 
     * @return hostID
     */
    public int getHostID() {
        return hostID;
    }


    /**
     * Sets the hostID value for this Host.
     * 
     * @param hostID
     */
    public void setHostID(int hostID) {
        this.hostID = hostID;
    }


    /**
     * Gets the applicationTypeID value for this Host.
     * 
     * @return applicationTypeID
     */
    public int getApplicationTypeID() {
        return applicationTypeID;
    }


    /**
     * Sets the applicationTypeID value for this Host.
     * 
     * @param applicationTypeID
     */
    public void setApplicationTypeID(int applicationTypeID) {
        this.applicationTypeID = applicationTypeID;
    }


    /**
     * Gets the device value for this Host.
     * 
     * @return device
     */
    public Device getDevice() {
        return device;
    }


    /**
     * Sets the device value for this Host.
     * 
     * @param device
     */
    public void setDevice(Device device) {
        this.device = device;
    }


    /**
     * Gets the name value for this Host.
     * 
     * @return name
     */
    public java.lang.String getName() {
        return name;
    }


    /**
     * Sets the name value for this Host.
     * 
     * @param name
     */
    public void setName(java.lang.String name) {
        this.name = name;
    }

    /**
     * Gets the monitorStatus value for this HostStatus.
     * 
     * @return monitorStatus
     */
    public MonitorStatus getMonitorStatus() 
    {
        return monitorStatus;
    }


    /**
     * Sets the monitorStatus value for this HostStatus.
     * 
     * @param monitorStatus
     */
    public void setMonitorStatus(MonitorStatus monitorStatus) {
        this.monitorStatus = monitorStatus;
    }
    
    /**
     * Gets the lastCheckTime value for this HostStatus.
     * 
     * @return lastCheckTime
     */
    public Date getLastCheckTime() 
    {
        return lastCheckTime;
    }


    /**
     * Sets the lastCheckTime value for this HostStatus.
     * 
     * @param lastCheckTime
     */
    public void setLastCheckTime(Date lastCheckTime) {
        this.lastCheckTime = lastCheckTime;
    }    
    
    /**
     * Gets the propertyTypeBinding value for this HostStatus.
     * 
     * @return propertyTypeBinding
     */
    public PropertyTypeBinding getPropertyTypeBinding() {
        return propertyTypeBinding;
    }


    /**
     * Sets the propertyTypeBinding value for this HostStatus.
     * 
     * @param propertyTypeBinding
     */
    public void setPropertyTypeBinding(PropertyTypeBinding propertyTypeBinding) {
        this.propertyTypeBinding = propertyTypeBinding;
    }
    
    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof Host)) return false;
        Host other = (Host) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            this.hostID == other.getHostID() &&
            this.serviceAvailability== other.getServiceAvailability() &&
            this.applicationTypeID == other.getApplicationTypeID() &&
            ((this.device==null && other.getDevice()==null) || 
             (this.device!=null &&
              this.device.equals(other.getDevice()))) &&
            ((this.name==null && other.getName()==null) || 
             (this.name!=null &&
              this.name.equals(other.getName())) )&&
            ((this.monitorStatus==null && other.getMonitorStatus()==null) || 
             (this.monitorStatus!=null &&
              this.monitorStatus.equals(other.getMonitorStatus())))  &&
            ((this.propertyTypeBinding==null && other.getPropertyTypeBinding()== null) || 
             (this.propertyTypeBinding!=null &&
              this.propertyTypeBinding.equals(other.getPropertyTypeBinding())) )  &&              
            ((this.lastCheckTime==null && other.getLastCheckTime()==null) || 
             (this.lastCheckTime!=null && this.lastCheckTime.equals(other.getLastCheckTime()))) &&              
             ((this.nextCheckTime==null && other.getNextCheckTime()==null) || 
                     (this.nextCheckTime!=null && this.nextCheckTime.equals(other.getNextCheckTime()))) &&  
                     ((this.stateType==null && other.getStateType()==null) || 
                             (this.stateType!=null && this.stateType.equals(other.getStateType()))) &&           
                             ((this.checkType==null && other.getCheckType()==null) || 
                                     (this.checkType!=null && this.checkType.equals(other.getCheckType()))) &&
             ((this.hostGroups==null && other.getHostGroups()==null) || 
                     (this.hostGroups!=null &&  java.util.Arrays.equals(this.hostGroups, other.getHostGroups()))) ;                               
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
        _hashCode += getHostID();
        _hashCode += getApplicationTypeID();
        _hashCode += getServiceAvailability();
        if (getDevice() != null) {
            _hashCode += getDevice().hashCode();
        }
        if (getName() != null) {
            _hashCode += getName().hashCode();
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
        if (getPropertyTypeBinding() != null) {
            _hashCode += getPropertyTypeBinding().hashCode();
        }  
        
        if (getStateType() != null) {
            _hashCode += getStateType().hashCode();
        }  
        
        if (getCheckType() != null) {
            _hashCode += getCheckType().hashCode();
        }  
        if (getHostGroups() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getHostGroups());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getHostGroups(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(Host.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "Host"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("hostID");
        elemField.setXmlName(new javax.xml.namespace.QName("", "HostID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("applicationTypeID");
        elemField.setXmlName(new javax.xml.namespace.QName("", "ApplicationTypeID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("device");
        elemField.setXmlName(new javax.xml.namespace.QName("", "Device"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "Device"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("name");
        elemField.setXmlName(new javax.xml.namespace.QName("", "Name"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("monitorStatus");
        elemField.setXmlName(new javax.xml.namespace.QName("", "MonitorStatus"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "MonitorStatus"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);  
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("lastCheckTime");
        elemField.setXmlName(new javax.xml.namespace.QName("", "LastCheckTime"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "dateTime"));
        elemField.setMinOccurs(0);
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
        elemField.setFieldName("propertyTypeBinding");
        elemField.setXmlName(new javax.xml.namespace.QName("", "PropertyTypeBinding"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "PropertyTypeBinding"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField); 
        
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("hostGroups");
        elemField.setXmlName(new javax.xml.namespace.QName("", "HostGroups"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "HostGroup"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        elemField.setMaxOccursUnbounded(true);
        typeDesc.addFieldDesc(elemField);
        
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("serviceAvailability");
        elemField.setXmlName(new javax.xml.namespace.QName("", "ServiceAvailability"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "double"));
        elemField.setNillable(true);
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

	public HostGroup[] getHostGroups() {
		return hostGroups;
	}

	public void setHostGroups(HostGroup[] hostGroups) {
		this.hostGroups = hostGroups;
	}

	public double getServiceAvailability() {
		return serviceAvailability;
	}

	public void setServiceAvailability(double serviceAvailability) {
		this.serviceAvailability = serviceAvailability;
	}

	public Date getNextCheckTime() {
		return nextCheckTime;
	}

	public void setNextCheckTime(Date nextCheckTime) {
		this.nextCheckTime = nextCheckTime;
	}

	public StateType getStateType() {
		return stateType;
	}

	public void setStateType(StateType stateType) {
		this.stateType = stateType;
	}

	public CheckType getCheckType() {
		return checkType;
	}

	public void setCheckType(CheckType checkType) {
		this.checkType = checkType;
	}

}
