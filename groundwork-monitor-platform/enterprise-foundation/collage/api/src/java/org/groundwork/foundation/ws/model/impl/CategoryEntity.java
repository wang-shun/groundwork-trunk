package org.groundwork.foundation.ws.model.impl;

import java.io.Serializable;



public class CategoryEntity implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int		objectID;
	private int		categoryEntityID;	// Primary Key
	private EntityType	entityType;
	private Category	category;
	
	 public CategoryEntity()
	 {
	    	
	  }

	
	public CategoryEntity(int objectID, int categoryEntityID,EntityType entityType,Category category)
	{
		this.objectID = objectID;
		this.categoryEntityID = categoryEntityID;
		this.entityType = entityType;
		this.category = category;
	}
	public int getObjectID() {
		return objectID;
	}
	public void setObjectID(int objectID) {
		this.objectID = objectID;
	}
	public int getCategoryEntityID() {
		return categoryEntityID;
	}
	public void setCategoryEntityID(int categoryEntityID) {
		this.categoryEntityID = categoryEntityID;
	}
	public EntityType getEntityType() {
		return entityType;
	}
	public void setEntityType(EntityType entityType) {
		this.entityType = entityType;
	}
	public Category getCategory() {
		return category;
	}
	public void setCategory(Category category) {
		this.category = category;
	}
	
	private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof CategoryEntity)) return false;
        CategoryEntity other = (CategoryEntity) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            this.categoryEntityID == other.getCategoryEntityID() &&
            this.objectID == other.getObjectID() &&
            ((this.entityType==null && other.getEntityType()==null) || 
              (this.entityType!=null && this.entityType.equals(other.getEntityType()))) &&
                ((this.category==null && other.getCategory()==null) || 
                       (this.category!=null && this.category.equals(other.getCategory())));              
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
        _hashCode += getCategoryEntityID();
        _hashCode += getObjectID();
        if (getEntityType() != null) {
            _hashCode += getEntityType().hashCode();
        }
               
        if (getCategory() != null) {
            _hashCode += getCategory().hashCode();
        }
        
        __hashCodeCalc = false;
        return _hashCode;
    }
	
	// Type metadata
	private static org.apache.axis.description.TypeDesc typeDesc = new org.apache.axis.description.TypeDesc(
			CategoryEntity.class, true);

	static {
		typeDesc
				.setXmlType(new javax.xml.namespace.QName(
						"http://model.ws.foundation.groundwork.org",
						"CategoryEntity"));
		org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("categoryEntityID");
        elemField.setXmlName(new javax.xml.namespace.QName("", "CategoryEntityID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("objectID");
        elemField.setXmlName(new javax.xml.namespace.QName("", "ObjectID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("entityType");
        elemField.setXmlName(new javax.xml.namespace.QName("", "EntityType"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "EntityType"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);   
        
      elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("category");
        elemField.setXmlName(new javax.xml.namespace.QName("", "Category"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "category"));
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
			java.lang.String mechType, java.lang.Class _javaType,
			javax.xml.namespace.QName _xmlType) {
		return new org.apache.axis.encoding.ser.BeanSerializer(_javaType,
				_xmlType, typeDesc);
	}

	/**
	 * Get Custom Deserializer
	 */
	public static org.apache.axis.encoding.Deserializer getDeserializer(
			java.lang.String mechType, java.lang.Class _javaType,
			javax.xml.namespace.QName _xmlType) {
		return new org.apache.axis.encoding.ser.BeanDeserializer(_javaType,
				_xmlType, typeDesc);
	}
	
	


}
