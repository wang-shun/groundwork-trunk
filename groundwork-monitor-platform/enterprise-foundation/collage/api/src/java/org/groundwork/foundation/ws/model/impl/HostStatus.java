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



public class HostStatus implements java.io.Serializable 
{
    private int hostStatusID;

    private int applicationTypeID;
    
    private Date nextCheckTime;
    
    private StateType stateType;

    private PropertyTypeBinding propertyTypeBinding;

    public HostStatus() {
    }

    public HostStatus(
           int hostStatusID,
           int applicationTypeID,
           Date nextCheckTime,
           StateType stateType,
           PropertyTypeBinding propertyTypeBinding) {
           this.hostStatusID = hostStatusID;
           this.applicationTypeID = applicationTypeID;
           this.nextCheckTime = nextCheckTime;
           this.stateType = stateType;
           this.propertyTypeBinding = propertyTypeBinding;
    }


    /**
     * Gets the hostStatusID value for this HostStatus.
     * 
     * @return hostStatusID
     */
    public int getHostStatusID() {
        return hostStatusID;
    }


    /**
     * Sets the hostStatusID value for this HostStatus.
     * 
     * @param hostStatusID
     */
    public void setHostStatusID(int hostStatusID) {
        this.hostStatusID = hostStatusID;
    }


    /**
     * Gets the applicationTypeID value for this HostStatus.
     * 
     * @return applicationTypeID
     */
    public int getApplicationTypeID() {
        return applicationTypeID;
    }


    /**
     * Sets the applicationTypeID value for this HostStatus.
     * 
     * @param applicationTypeID
     */
    public void setApplicationTypeID(int applicationTypeID) {
        this.applicationTypeID = applicationTypeID;
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
        if (!(obj instanceof HostStatus)) return false;
        HostStatus other = (HostStatus) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            this.hostStatusID == other.getHostStatusID() &&
            this.applicationTypeID == other.getApplicationTypeID() &&
            ((this.nextCheckTime==null && other.getNextCheckTime()==null) || 
                    (this.nextCheckTime!=null &&
                     this.nextCheckTime.equals(other.getNextCheckTime()))) &&
                     ((this.stateType==null && other.getStateType()==null) || 
                             (this.stateType!=null &&
                              this.stateType.equals(other.getStateType()))) &&
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
        _hashCode += getHostStatusID();
        _hashCode += getApplicationTypeID();
        if (getNextCheckTime() != null) {
            _hashCode += getNextCheckTime().hashCode();
        }
        if (getStateType() != null) {
            _hashCode += getStateType().hashCode();
        }
        if (getPropertyTypeBinding() != null) {
            _hashCode += getPropertyTypeBinding().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(HostStatus.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "HostStatus"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("hostStatusID");
        elemField.setXmlName(new javax.xml.namespace.QName("", "HostStatusID"));
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
        elemField.setFieldName("nextCheckTime");
        elemField.setXmlName(new javax.xml.namespace.QName("", "NextCheckTime"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "dateTime"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        
        
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("stateType");
        elemField.setXmlName(new javax.xml.namespace.QName("", "StateType"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "StateType"));
        elemField.setNillable(false);
        elemField.setMinOccurs(0);
        elemField.setMaxOccurs(1);
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

}
