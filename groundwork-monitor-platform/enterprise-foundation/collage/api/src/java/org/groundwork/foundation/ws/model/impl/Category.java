package org.groundwork.foundation.ws.model.impl;

import java.io.Serializable;
import java.util.Set;

public class Category implements Serializable {
	
	 /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/** identifier field */
    private int categoryId;

    /** nullable persistent field */
    private String name;

    /** nullable persistent field */
    private String description;
    
    private EntityType entityType;
    
    /* unidirectional many to many associations */
    private Category[] parents;
        
    /* unidirectional one to many associations */
    private CategoryEntity[] categoryEntities;
    
    public Category()
    {
    	
    }

	public Category(int categoryId,String name, String description,EntityType entityType,Category[] parents, CategoryEntity[] categoryEntities)
	{
		this.categoryId = categoryId;
		this.name = name;
		this.description = description;
		this.entityType = entityType;
		this.parents = parents;
		this.categoryEntities = categoryEntities;
	}
    
    public int getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(int categoryId) {
		this.categoryId = categoryId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Category[] getParents() {
		return parents;
	}
	
	 public CategoryEntity getCategoryEntities(int i) {
	        return this.categoryEntities[i];
	    }

	public void setParents(Category[] parents) {
		this.parents = parents;
	}

	public CategoryEntity[] getCategoryEntities() {
		return categoryEntities;
	}
	
	public void setCategoryEntities(int i, CategoryEntity _value) {
        this.categoryEntities[i] = _value;
    }
	
	public void setCategoryEntities(CategoryEntity[] categoryEntities) {
		this.categoryEntities = categoryEntities;
	}
	
	public void setParents(int i,Category _value) {
		this.parents[i] = _value;
	}
	
	
	public Category getParents(int i) {
        return this.parents[i];
    }
	
	private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof Category)) return false;
        Category other = (Category) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            this.categoryId == other.getCategoryId() &&
            ((this.name==null && other.getName()==null) || 
             (this.name!=null && this.name.equals(other.getName())) )&&
            ((this.description==null && other.getDescription()==null) || 
             (this.description!=null && this.description.equals(other.getDescription()))) &&
             ((this.entityType==null && other.getEntityType()==null) || 
                     (this.entityType!=null && this.entityType.equals(other.getEntityType()))) &&
                     ((this.parents==null && other.getParents()==null) || 
                             (this.parents!=null && this.parents.equals(other.getParents()))) &&
                             ((this.categoryEntities==null && other.getCategoryEntities()==null) || 
                                     (this.categoryEntities!=null && this.categoryEntities.equals(other.getCategoryEntities())));              
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
        _hashCode += getCategoryId();

        if (getName() != null) {
            _hashCode += getName().hashCode();
        }

        if (getDescription() != null) {
            _hashCode += getDescription().hashCode();
        }
        

        if (getEntityType() != null) {
            _hashCode += getEntityType().hashCode();
        }
        
        if (getParents() != null) {
            _hashCode += getParents().hashCode();
        }
        
        if (getCategoryEntities() != null) {
            _hashCode += getCategoryEntities().hashCode();
        }
        
        __hashCodeCalc = false;
        return _hashCode;
    }
	
	// Type metadata
	private static org.apache.axis.description.TypeDesc typeDesc = new org.apache.axis.description.TypeDesc(
			Category.class, true);

	static {
		typeDesc
				.setXmlType(new javax.xml.namespace.QName(
						"http://model.ws.foundation.groundwork.org",
						"Category"));
		org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("categoryId");
        elemField.setXmlName(new javax.xml.namespace.QName("", "CategoryID"));
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
        elemField.setFieldName("entityType");
        elemField.setXmlName(new javax.xml.namespace.QName("", "EntityType"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "EntityType"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);   
        
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("parents");
        elemField.setXmlName(new javax.xml.namespace.QName("", "Parents"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "Category"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);   
        
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("categoryEntities");
        elemField.setXmlName(new javax.xml.namespace.QName("", "CategoryEntity"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "CategoryEntity"));
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

	public EntityType getEntityType() {
		return entityType;
	}

	public void setEntityType(EntityType entityType) {
		this.entityType = entityType;
	}
	

}
