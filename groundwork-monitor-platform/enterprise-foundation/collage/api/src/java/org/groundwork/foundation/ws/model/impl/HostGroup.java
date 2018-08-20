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

public class HostGroup  implements java.io.Serializable 
{
    private int hostGroupID;

    private int applicationTypeID;
    
    private String applicationName;

    private java.lang.String name;
    
    private java.lang.String description;
    
    private java.lang.String alias;

    private Host[] hosts;

    public HostGroup() {
    }

    public HostGroup(
           int hostGroupID,
           int applicationTypeID,
           String applicationName,
           java.lang.String name,
           java.lang.String description,
           Host[] hosts,
           java.lang.String alias) 
    {
           this.hostGroupID = hostGroupID;
           this.applicationTypeID = applicationTypeID;
           this.applicationName = applicationName;
           this.name = name;
           this.description = description;
           this.hosts = hosts;
           this.alias = alias;
    }

    /**
     * Gets the hostGroupID value for this HostGroup.
     * 
     * @return hostGroupID
     */
    public int getHostGroupID() {
        return hostGroupID;
    }


    /**
     * Sets the hostGroupID value for this HostGroup.
     * 
     * @param hostGroupID
     */
    public void setHostGroupID(int hostGroupID) {
        this.hostGroupID = hostGroupID;
    }


    /**
     * Gets the applicationTypeID value for this HostGroup.
     * 
     * @return applicationTypeID
     */
    public int getApplicationTypeID() {
        return applicationTypeID;
    }


    /**
     * Sets the applicationTypeID value for this HostGroup.
     * 
     * @param applicationTypeID
     */
    public void setApplicationTypeID(int applicationTypeID) {
        this.applicationTypeID = applicationTypeID;
    }

    /**
     * Gets the applicationName value for this HostGroup.
     * 
     * @return applicationName
     */
    public String getApplicationName() {
        return applicationName;
    }


    /**
     * Sets the applicationName value for this HostGroup.
     * 
     * @param applicationName
     */
    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }
    

    /**
     * Gets the name value for this HostGroup.
     * 
     * @return name
     */
    public java.lang.String getName() {
        return name;
    }


    /**
     * Sets the name value for this HostGroup.
     * 
     * @param name
     */
    public void setName(java.lang.String name) {
        this.name = name;
    }
    
    /**
     * Gets the description value for this HostGroup.
     * 
     * @return name
     */
    public java.lang.String getDescription() {
        return description;
    }


    /**
     * Sets the description value for this HostGroup.
     * 
     * @param name
     */
    public void setDescription(java.lang.String description) {
        this.description = description;
    }
    
    /**
     * Gets the alias value for this HostGroup.
     * 
     * @return alias
     */
    public java.lang.String getAlias() {
        return alias;
    }


    /**
     * Sets the alias value for this HostGroup.
     * 
     * @param alias
     */
    public void setAlias(java.lang.String alias) {
        this.alias = alias;
    }

    /**
     * Gets the hosts value for this HostGroup.
     * 
     * @return hosts
     */
    public Host[] getHosts() {
        return hosts;
    }


    /**
     * Sets the hosts value for this HostGroup.
     * 
     * @param hosts
     */
    public void setHosts(Host[] hosts) {
        this.hosts = hosts;
    }

    public Host getHosts(int i) {
        return this.hosts[i];
    }

    public void setHosts(int i, Host _value) {
        this.hosts[i] = _value;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof HostGroup)) return false;
        HostGroup other = (HostGroup) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            this.hostGroupID == other.getHostGroupID() &&
            this.applicationTypeID == other.getApplicationTypeID() &&
            ((this.applicationName==null && other.getApplicationName()==null) || 
                    (this.applicationName!=null &&
                     this.applicationName.equals(other.getApplicationName()))) &&
            ((this.name==null && other.getName()==null) || 
             (this.name!=null &&
              this.name.equals(other.getName()))) &&
            ((this.hosts==null && other.getHosts()==null) || 
             (this.hosts!=null &&
              java.util.Arrays.equals(this.hosts, other.getHosts())));
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
        _hashCode += getHostGroupID();
        _hashCode += getApplicationTypeID();
        if (getApplicationName() != null) {
            _hashCode += getApplicationName().hashCode();
        }        
        if (getName() != null) {
            _hashCode += getName().hashCode();
        }
        if (getHosts() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getHosts());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getHosts(), i);
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
        new org.apache.axis.description.TypeDesc(HostGroup.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "HostGroup"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("hostGroupID");
        elemField.setXmlName(new javax.xml.namespace.QName("", "HostGroupID"));
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
        elemField.setFieldName("name");
        elemField.setXmlName(new javax.xml.namespace.QName("", "Name"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("description");
        elemField.setXmlName(new javax.xml.namespace.QName("", "Description"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("hosts");
        elemField.setXmlName(new javax.xml.namespace.QName("", "Hosts"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "Host"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setMaxOccursUnbounded(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("alias");
        elemField.setXmlName(new javax.xml.namespace.QName("", "Alias"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
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

}
