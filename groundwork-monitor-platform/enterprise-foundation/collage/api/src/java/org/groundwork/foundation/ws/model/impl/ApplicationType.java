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

public class ApplicationType implements java.io.Serializable 
{
    private int applicationTypeID;

    private java.lang.String name;
    
    private java.lang.String description;

    public ApplicationType() {
    }

    public ApplicationType(
           int applicationTypeID,
           java.lang.String name,
           java.lang.String description) 
    {
           this.applicationTypeID = applicationTypeID;
           this.description = description;
           this.name = name;
    }

    /**
     * Gets the entity type id
     * 
     * @return entityTypeID
     */
    public int getApplicationTypeID() {
        return applicationTypeID;
    }

    /**
     * Sets the applicationTypeID
     * 
     * @param applicationTypeID
     */
    public void setApplicationTypeID(int applicationTypeID) 
    {
        this.applicationTypeID = applicationTypeID;
    }

    /**
     * Gets the name value for this Entity Type.
     * 
     * @return name
     */
    public java.lang.String getName() {
        return name;
    }

    /**
     * Sets the name value for this Entity Type.
     * 
     * @param name
     */
    public void setName(java.lang.String name) {
        this.name = name;
    }
    
    /**
     * Gets the description value for this Entity Type.
     * 
     * @return description
     */
    public java.lang.String getDescription() 
    {
        return description;
    }

    /**
     * Sets the description value for this Entity Type.
     * 
     * @param name
     */
    public void setDescription(java.lang.String description) {
        this.description = description;
    }
    
    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ApplicationType)) return false;
        ApplicationType other = (ApplicationType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            this.applicationTypeID == other.getApplicationTypeID() &&
            ((this.name==null && other.getName()==null) || 
             (this.name!=null && this.name.equals(other.getName())) )&&
            ((this.description==null && other.getDescription()==null) || 
             (this.description!=null && this.name.equals(other.getDescription())));              
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
        _hashCode += getApplicationTypeID();

        if (getName() != null) {
            _hashCode += getName().hashCode();
        }

        if (getDescription() != null) {
            _hashCode += getDescription().hashCode();
        }
        
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(ApplicationType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "ApplicationType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("applicationTypeID");
        elemField.setXmlName(new javax.xml.namespace.QName("", "ApplicationTypeID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("name");
        elemField.setXmlName(new javax.xml.namespace.QName("", "Name"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("description");
        elemField.setXmlName(new javax.xml.namespace.QName("", "Description"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
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
