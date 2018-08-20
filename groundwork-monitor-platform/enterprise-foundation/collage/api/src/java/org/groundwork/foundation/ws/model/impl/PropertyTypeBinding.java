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

import java.util.ArrayList;
import java.util.List;

public class PropertyTypeBinding implements java.io.Serializable 
{
    private StringProperty[] stringProperty;

    private IntegerProperty[] integerProperty;

    private LongProperty[] longProperty;

    private DoubleProperty[] doubleProperty;

    private BooleanProperty[] booleanProperty;

    private DateProperty[] dateProperty;

    private TimeProperty[] timeProperty;

    public PropertyTypeBinding() {
    }

    public PropertyTypeBinding(
           StringProperty[] stringProperty,
           IntegerProperty[] integerProperty,
           LongProperty[] longProperty,
           DoubleProperty[] doubleProperty,
           BooleanProperty[] booleanProperty,
           DateProperty[] dateProperty,
           TimeProperty[] timeProperty) {
           this.stringProperty = stringProperty;
           this.integerProperty = integerProperty;
           this.longProperty = longProperty;
           this.doubleProperty = doubleProperty;
           this.booleanProperty = booleanProperty;
           this.dateProperty = dateProperty;
           this.timeProperty = timeProperty;
    }
    
    public Object getPropertyValue (String propName)
    {
    	// Note:  We return the first property match we find.    	
    	StringProperty stringProperty = getStringProperty(propName);
    	if (stringProperty != null)
    		return stringProperty.getValue();
    	
    	IntegerProperty intProperty = getIntegerProperty(propName);
    	if (intProperty != null)
    		return intProperty.getValue();
    	
    	DateProperty dateProperty = getDateProperty(propName);
    	if (dateProperty != null)
    		return dateProperty.getValue();
    	
    	LongProperty longProperty = getLongProperty(propName);
    	if (longProperty != null)
    		return longProperty.getValue();
    	
    	BooleanProperty booleanProperty = getBooleanProperty(propName);
    	if (booleanProperty != null)
    		return booleanProperty.isValue();
    	
    	DoubleProperty doubleProperty = getDoubleProperty(propName);
    	if (doubleProperty != null)
    		return doubleProperty.getValue();

    	return null;
    }
    
    public Object getPropertyValue(String propName, PropertyDataType dataType)
    {
    	if (PropertyDataType.STRING.equals(dataType))
    	{    	
    		StringProperty prop = getStringProperty(propName);
    		if (prop == null)
    			return null;
    		
    		return prop.getValue();
    	}
    	else if (PropertyDataType.INTEGER.equals(dataType))
    	{
    		IntegerProperty prop = getIntegerProperty(propName);
    		if (prop == null)
    			return null;
    		
    		return prop.getValue();    		
    	}
    	else if (PropertyDataType.DATE.equals(dataType))
    	{
    		DateProperty prop = getDateProperty(propName);
    		if (prop == null)
    			return null;
    		
    		return prop.getValue();    
    	}
    	else if (PropertyDataType.DOUBLE.equals(dataType))
    	{
    		DoubleProperty prop = getDoubleProperty(propName);
    		if (prop == null)
    			return null;
    		
    		return prop.getValue();    
    	}
    	else if (PropertyDataType.LONG.equals(dataType))
    	{
    		LongProperty prop = getLongProperty(propName);
    		if (prop == null)
    			return null;
    		
    		return prop.getValue();    
    	}
    	else if (PropertyDataType.BOOLEAN.equals(dataType))
    	{
    		BooleanProperty prop = getBooleanProperty(propName);
    		if (prop == null)
    			return null;
    		
    		return prop.isValue();
    	}
    	
    	return null;
    }
    
    /**
     * Gets the stringProperty value for this PropertyTypeBinding.
     * 
     * @return stringProperty
     */
    public StringProperty[] getStringProperty() {
        return stringProperty;
    }

    /**
     * Sets the stringProperty value for this PropertyTypeBinding.
     * 
     * @param stringProperty
     */
    public void setStringProperty(StringProperty[] stringProperty) {
        this.stringProperty = stringProperty;
    }

    public StringProperty getStringProperty(int i) {
        return this.stringProperty[i];
    }

    public StringProperty getStringProperty(String propName) 
    {
    	if (this.stringProperty == null)
    		return null;
    	
    	for (int i = 0; i < this.stringProperty.length; i++)
    	{
    		if (this.stringProperty[i].getName().equalsIgnoreCase(propName))
    			return this.stringProperty[i];
    	}

    	return null;
    }
    
    public void setStringProperty(int i, StringProperty _value) {
        this.stringProperty[i] = _value;
    }


    /**
     * Gets the integerProperty value for this PropertyTypeBinding.
     * 
     * @return integerProperty
     */
    public IntegerProperty[] getIntegerProperty() {
        return integerProperty;
    }


    /**
     * Sets the integerProperty value for this PropertyTypeBinding.
     * 
     * @param integerProperty
     */
    public void setIntegerProperty(IntegerProperty[] integerProperty) {
        this.integerProperty = integerProperty;
    }

    public IntegerProperty getIntegerProperty(int i) {
        return this.integerProperty[i];
    }

    public IntegerProperty getIntegerProperty(String propName) 
    {
    	if (this.integerProperty == null)
    		return null;
    	
    	for (int i = 0; i < this.integerProperty.length; i++)
    	{
    		if (this.integerProperty[i].getName().equalsIgnoreCase(propName))
    			return this.integerProperty[i];
    	}

    	return null;
    }
    
    public void setIntegerProperty(int i, IntegerProperty _value) {
        this.integerProperty[i] = _value;
    }

    /**
     * Gets the longProperty value for this PropertyTypeBinding.
     * 
     * @return longProperty
     */
    public LongProperty[] getLongProperty() {
        return longProperty;
    }


    /**
     * Sets the longProperty value for this PropertyTypeBinding.
     * 
     * @param longProperty
     */
    public void setLongProperty(LongProperty[] longProperty) {
        this.longProperty = longProperty;
    }

    public LongProperty getLongProperty(int i) {
        return this.longProperty[i];
    }
    
    public LongProperty getLongProperty(String propName) 
    {
    	if (this.longProperty == null)
    		return null;
    	
    	for (int i = 0; i < this.longProperty.length; i++)
    	{
    		if (this.longProperty[i].getName().equalsIgnoreCase(propName))
    			return this.longProperty[i];
    	}

    	return null;
    }    

    public void setLongProperty(int i, LongProperty _value) {
        this.longProperty[i] = _value;
    }


    /**
     * Gets the doubleProperty value for this PropertyTypeBinding.
     * 
     * @return doubleProperty
     */
    public DoubleProperty[] getDoubleProperty() {
        return doubleProperty;
    }


    /**
     * Sets the doubleProperty value for this PropertyTypeBinding.
     * 
     * @param doubleProperty
     */
    public void setDoubleProperty(DoubleProperty[] doubleProperty) {
        this.doubleProperty = doubleProperty;
    }

    public DoubleProperty getDoubleProperty(int i) {
        return this.doubleProperty[i];
    }

    public DoubleProperty getDoubleProperty(String propName) 
    {
    	if (this.doubleProperty == null)
    		return null;
    	
    	for (int i = 0; i < this.doubleProperty.length; i++)
    	{
    		if (this.doubleProperty[i].getName().equalsIgnoreCase(propName))
    			return this.doubleProperty[i];
    	}

    	return null;
    }  
    
    public void setDoubleProperty(int i, DoubleProperty _value) {
        this.doubleProperty[i] = _value;
    }


    /**
     * Gets the booleanProperty value for this PropertyTypeBinding.
     * 
     * @return booleanProperty
     */
    public BooleanProperty[] getBooleanProperty() {
        return booleanProperty;
    }


    /**
     * Sets the booleanProperty value for this PropertyTypeBinding.
     * 
     * @param booleanProperty
     */
    public void setBooleanProperty(BooleanProperty[] booleanProperty) {
        this.booleanProperty = booleanProperty;
    }

    public BooleanProperty getBooleanProperty(int i) {
        return this.booleanProperty[i];
    }

    public BooleanProperty getBooleanProperty(String propName) 
    {
    	if (this.booleanProperty == null)
    		return null;
    	
    	for (int i = 0; i < this.booleanProperty.length; i++)
    	{
    		if (this.booleanProperty[i].getName().equalsIgnoreCase(propName))
    			return this.booleanProperty[i];
    	}

    	return null;
    }  
    
    public void setBooleanProperty(int i, BooleanProperty _value) {
        this.booleanProperty[i] = _value;
    }


    /**
     * Gets the dateProperty value for this PropertyTypeBinding.
     * 
     * @return dateProperty
     */
    public DateProperty[] getDateProperty() {
        return dateProperty;
    }


    /**
     * Sets the dateProperty value for this PropertyTypeBinding.
     * 
     * @param dateProperty
     */
    public void setDateProperty(DateProperty[] dateProperty) {
        this.dateProperty = dateProperty;
    }

    public DateProperty getDateProperty(int i) {
        return this.dateProperty[i];
    }

    public DateProperty getDateProperty(String propName) 
    {
    	if (this.dateProperty == null)
    		return null;
    	
    	for (int i = 0; i < this.dateProperty.length; i++)
    	{
    		if (this.dateProperty[i].getName().equalsIgnoreCase(propName))
    			return this.dateProperty[i];
    	}

    	return null;
    }  
    
    public void setDateProperty(int i, DateProperty _value) {
        this.dateProperty[i] = _value;
    }

    /**
     * Gets the timeProperty value for this PropertyTypeBinding.
     * 
     * @return timeProperty
     */
    public TimeProperty[] getTimeProperty() {
        return timeProperty;
    }


    /**
     * Sets the timeProperty value for this PropertyTypeBinding.
     * 
     * @param timeProperty
     */
    public void setTimeProperty(TimeProperty[] timeProperty) {
        this.timeProperty = timeProperty;
    }

    public TimeProperty getTimeProperty(int i) {
        return this.timeProperty[i];
    }
    
    public TimeProperty getTimeProperty(String propName) 
    {
    	if (this.timeProperty == null)
    		return null;
    	
    	for (int i = 0; i < this.timeProperty.length; i++)
    	{
    		if (this.timeProperty[i].getName().equalsIgnoreCase(propName))
    			return this.timeProperty[i];
    	}

    	return null;
    }  
        
    public void setTimeProperty(int i, TimeProperty _value) {
        this.timeProperty[i] = _value;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof PropertyTypeBinding)) return false;
        PropertyTypeBinding other = (PropertyTypeBinding) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.stringProperty==null && other.getStringProperty()==null) || 
             (this.stringProperty!=null &&
              java.util.Arrays.equals(this.stringProperty, other.getStringProperty()))) &&
            ((this.integerProperty==null && other.getIntegerProperty()==null) || 
             (this.integerProperty!=null &&
              java.util.Arrays.equals(this.integerProperty, other.getIntegerProperty()))) &&
            ((this.longProperty==null && other.getLongProperty()==null) || 
             (this.longProperty!=null &&
              java.util.Arrays.equals(this.longProperty, other.getLongProperty()))) &&
            ((this.doubleProperty==null && other.getDoubleProperty()==null) || 
             (this.doubleProperty!=null &&
              java.util.Arrays.equals(this.doubleProperty, other.getDoubleProperty()))) &&
            ((this.booleanProperty==null && other.getBooleanProperty()==null) || 
             (this.booleanProperty!=null &&
              java.util.Arrays.equals(this.booleanProperty, other.getBooleanProperty()))) &&
            ((this.dateProperty==null && other.getDateProperty()==null) || 
             (this.dateProperty!=null &&
              java.util.Arrays.equals(this.dateProperty, other.getDateProperty()))) &&
            ((this.timeProperty==null && other.getTimeProperty()==null) || 
             (this.timeProperty!=null &&
              java.util.Arrays.equals(this.timeProperty, other.getTimeProperty())));
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
        if (getStringProperty() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getStringProperty());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getStringProperty(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getIntegerProperty() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getIntegerProperty());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getIntegerProperty(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getLongProperty() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getLongProperty());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getLongProperty(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getDoubleProperty() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getDoubleProperty());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getDoubleProperty(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getBooleanProperty() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getBooleanProperty());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getBooleanProperty(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getDateProperty() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getDateProperty());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getDateProperty(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getTimeProperty() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getTimeProperty());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getTimeProperty(), i);
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
        new org.apache.axis.description.TypeDesc(PropertyTypeBinding.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "PropertyTypeBinding"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("stringProperty");
        elemField.setXmlName(new javax.xml.namespace.QName("", "StringProperty"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "StringProperty"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        elemField.setMaxOccursUnbounded(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("integerProperty");
        elemField.setXmlName(new javax.xml.namespace.QName("", "IntegerProperty"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "IntegerProperty"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        elemField.setMaxOccursUnbounded(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("longProperty");
        elemField.setXmlName(new javax.xml.namespace.QName("", "LongProperty"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "LongProperty"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        elemField.setMaxOccursUnbounded(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("doubleProperty");
        elemField.setXmlName(new javax.xml.namespace.QName("", "DoubleProperty"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "DoubleProperty"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        elemField.setMaxOccursUnbounded(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("booleanProperty");
        elemField.setXmlName(new javax.xml.namespace.QName("", "BooleanProperty"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "BooleanProperty"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        elemField.setMaxOccursUnbounded(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("dateProperty");
        elemField.setXmlName(new javax.xml.namespace.QName("", "DateProperty"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "DateProperty"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        elemField.setMaxOccursUnbounded(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("timeProperty");
        elemField.setXmlName(new javax.xml.namespace.QName("", "TimeProperty"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "TimeProperty"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        elemField.setMaxOccursUnbounded(true);
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
