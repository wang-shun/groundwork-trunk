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

public class EntityTypeProperty implements java.io.Serializable 
{
    private ApplicationType applicationType;

    private EntityType entityType;

    private int propertyTypeID;
    
    private String name;
    
    private String description;
    
    private PropertyDataType dataType;    
    
    private String propertyEntityType;

    public EntityTypeProperty() {
    }

    public EntityTypeProperty(
    		ApplicationType applicationType,
    		EntityType entityType,
            int propertyTypeID,
            java.lang.String name,
            java.lang.String description,
            PropertyDataType dataType,
            String propertyEntityType)
    {
           this.applicationType = applicationType;
           this.entityType = entityType;
           this.propertyTypeID = propertyTypeID;
           this.name = name;
           this.description = description;
           this.dataType = dataType;
           this.propertyEntityType = propertyEntityType;
    }

	public ApplicationType getApplicationType()
	{
		return applicationType;
	}

	public void setApplicationType(ApplicationType applicationType)
	{
		this.applicationType = applicationType;
	}

	public PropertyDataType getDataType()
	{
		return dataType;
	}

	public void setDataType(PropertyDataType dataType)
	{
		this.dataType = dataType;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public EntityType getEntityType()
	{
		return entityType;
	}

	public void setEntityType(EntityType entityType)
	{
		this.entityType = entityType;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public int getPropertyTypeID()
	{
		return propertyTypeID;
	}

	public void setPropertyTypeID(int propertyTypeID)
	{
		this.propertyTypeID = propertyTypeID;
	}

	public String getPropertyEntityType()
	{
		return propertyEntityType;
	}

	public void setPropertyEntityType(String entityTypeName)
	{
		this.propertyEntityType = entityTypeName;
	}
	
    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof EntityTypeProperty)) return false;
        EntityTypeProperty other = (EntityTypeProperty) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            this.propertyTypeID == other.getPropertyTypeID() &&
            ((this.applicationType == null && other.getApplicationType()==null) || 
             (this.applicationType != null && this.applicationType.equals(other.getApplicationType()))) &&
            ((this.entityType == null && other.getEntityType()==null) || 
             (this.entityType != null && this.entityType.equals(other.getEntityType()))) &&
            ((this.name==null && other.getName()==null) || 
             (this.name!=null && this.name.equals(other.getName()))) &&
            ((this.description==null && other.getDescription()==null) || 
             (this.description!=null && this.description.equals(other.getDescription()))) &&
            ((this.dataType == null && other.getDataType()==null) || 
             (this.dataType != null && this.dataType.equals(other.getDataType()))) &&
            ((this.propertyEntityType == null && other.getPropertyEntityType()==null) || 
             (this.propertyEntityType != null && this.dataType.equals(other.getPropertyEntityType())));
        
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
        _hashCode += getPropertyTypeID();
        
        if (getApplicationType() != null) {
            _hashCode += getApplicationType().hashCode();
        }
        if (getEntityType() != null) {
            _hashCode += getEntityType().hashCode();
        }        
        if (getName() != null) {
            _hashCode += getName().hashCode();
        }
        if (getDescription() != null) {
            _hashCode += getDescription().hashCode();
        }   
        if (getDataType() != null) {
            _hashCode += getDataType().hashCode();
        } 
        if (getPropertyEntityType() != null) {
            _hashCode += getPropertyEntityType().hashCode();
        }          
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(EntityTypeProperty.class, true);

    static {
  		    
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "EntityTypeProperty"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("applicationType");
        elemField.setXmlName(new javax.xml.namespace.QName("", "ApplicationType"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "ApplicationType"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("entityType");
        elemField.setXmlName(new javax.xml.namespace.QName("", "EntityType"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "EntityType"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("propertyTypeID");
        elemField.setXmlName(new javax.xml.namespace.QName("", "PropertyTypeID"));
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
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("dataType");
        elemField.setXmlName(new javax.xml.namespace.QName("", "DataType"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "PropertyDataType"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField); 
        
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("propertyEntityType");
        elemField.setXmlName(new javax.xml.namespace.QName("", "PropertyEntityType"));
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
