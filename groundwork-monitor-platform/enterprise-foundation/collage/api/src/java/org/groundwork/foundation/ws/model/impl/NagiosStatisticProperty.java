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

public class NagiosStatisticProperty implements java.io.Serializable 
{
    private java.lang.String propertyName;

    private long hostStatisticEnabled;

    private long hostStatisticDisabled;

    private long serviceStatisticEnabled;

    private long serviceStatisticDisabled;

    public NagiosStatisticProperty() {
    }

    public NagiosStatisticProperty(
           java.lang.String propertyName,
           int hostStatisticEnabled,
           int hostStatisticDisabled,
           int serviceStatisticEnabled,
           int serviceStatisticDisabled) {
           this.propertyName = propertyName;
           this.hostStatisticEnabled = hostStatisticEnabled;
           this.hostStatisticDisabled = hostStatisticDisabled;
           this.serviceStatisticEnabled = serviceStatisticEnabled;
           this.serviceStatisticDisabled = serviceStatisticDisabled;
    }


    /**
     * Gets the propertyName value for this NagiosStatisticProperty.
     * 
     * @return propertyName
     */
    public java.lang.String getPropertyName() {
        return propertyName;
    }


    /**
     * Sets the propertyName value for this NagiosStatisticProperty.
     * 
     * @param propertyName
     */
    public void setPropertyName(java.lang.String propertyName) {
        this.propertyName = propertyName;
    }


    /**
     * Gets the hostStatisticEnabled value for this NagiosStatisticProperty.
     * 
     * @return hostStatisticEnabled
     */
    public long getHostStatisticEnabled() {
        return hostStatisticEnabled;
    }


    /**
     * Sets the hostStatisticEnabled value for this NagiosStatisticProperty.
     * 
     * @param hostStatisticEnabled
     */
    public void setHostStatisticEnabled(long hostStatisticEnabled) {
        this.hostStatisticEnabled = hostStatisticEnabled;
    }


    /**
     * Gets the hostStatisticDisabled value for this NagiosStatisticProperty.
     * 
     * @return hostStatisticDisabled
     */
    public long getHostStatisticDisabled() {
        return hostStatisticDisabled;
    }


    /**
     * Sets the hostStatisticDisabled value for this NagiosStatisticProperty.
     * 
     * @param hostStatisticDisabled
     */
    public void setHostStatisticDisabled(long hostStatisticDisabled) {
        this.hostStatisticDisabled = hostStatisticDisabled;
    }


    /**
     * Gets the serviceStatisticEnabled value for this NagiosStatisticProperty.
     * 
     * @return serviceStatisticEnabled
     */
    public long getServiceStatisticEnabled() {
        return serviceStatisticEnabled;
    }


    /**
     * Sets the serviceStatisticEnabled value for this NagiosStatisticProperty.
     * 
     * @param serviceStatisticEnabled
     */
    public void setServiceStatisticEnabled(long serviceStatisticEnabled) {
        this.serviceStatisticEnabled = serviceStatisticEnabled;
    }


    /**
     * Gets the serviceStatisticDisabled value for this NagiosStatisticProperty.
     * 
     * @return serviceStatisticDisabled
     */
    public long getServiceStatisticDisabled() {
        return serviceStatisticDisabled;
    }


    /**
     * Sets the serviceStatisticDisabled value for this NagiosStatisticProperty.
     * 
     * @param serviceStatisticDisabled
     */
    public void setServiceStatisticDisabled(long serviceStatisticDisabled) {
        this.serviceStatisticDisabled = serviceStatisticDisabled;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof NagiosStatisticProperty)) return false;
        NagiosStatisticProperty other = (NagiosStatisticProperty) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.propertyName==null && other.getPropertyName()==null) || 
             (this.propertyName!=null &&
              this.propertyName.equals(other.getPropertyName()))) &&
            this.hostStatisticEnabled == other.getHostStatisticEnabled() &&
            this.hostStatisticDisabled == other.getHostStatisticDisabled() &&
            this.serviceStatisticEnabled == other.getServiceStatisticEnabled() &&
            this.serviceStatisticDisabled == other.getServiceStatisticDisabled();
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
        if (getPropertyName() != null) {
            _hashCode += getPropertyName().hashCode();
        }
        _hashCode += getHostStatisticEnabled();
        _hashCode += getHostStatisticDisabled();
        _hashCode += getServiceStatisticEnabled();
        _hashCode += getServiceStatisticDisabled();
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(NagiosStatisticProperty.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "NagiosStatisticProperty"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("propertyName");
        elemField.setXmlName(new javax.xml.namespace.QName("", "PropertyName"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("hostStatisticEnabled");
        elemField.setXmlName(new javax.xml.namespace.QName("", "HostStatisticEnabled"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "long"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("hostStatisticDisabled");
        elemField.setXmlName(new javax.xml.namespace.QName("", "HostStatisticDisabled"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "long"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("serviceStatisticEnabled");
        elemField.setXmlName(new javax.xml.namespace.QName("", "ServiceStatisticEnabled"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "long"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("serviceStatisticDisabled");
        elemField.setXmlName(new javax.xml.namespace.QName("", "ServiceStatisticDisabled"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "long"));
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
