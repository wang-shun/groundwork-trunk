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

public class Filter implements java.io.Serializable
{
    private StringProperty stringProperty;

    private IntegerProperty integerProperty;

    private LongProperty longProperty;

    private DoubleProperty doubleProperty;

    private BooleanProperty booleanProperty;

    private DateProperty dateProperty;

    private TimeProperty timeProperty;

    private Filter leftFilter;

    private Filter rightFilter;

    private FilterOperator operator;

    public Filter() {
    }

    public Filter(
           StringProperty stringProperty,
           IntegerProperty integerProperty,
           LongProperty longProperty,
           DoubleProperty doubleProperty,
           BooleanProperty booleanProperty,
           DateProperty dateProperty,
           TimeProperty timeProperty,
           Filter leftFilter,
           Filter rightFilter,
           FilterOperator operator)
    {
           this.stringProperty = stringProperty;
           this.integerProperty = integerProperty;
           this.longProperty = longProperty;
           this.doubleProperty = doubleProperty;
           this.booleanProperty = booleanProperty;
           this.dateProperty = dateProperty;
           this.timeProperty = timeProperty;
           this.leftFilter = leftFilter;
           this.rightFilter = rightFilter;
           this.operator = operator;
    }

	public Filter(String propertyName, FilterOperator operator, String value)
	{
		if (propertyName == null || propertyName.length() == 0)
			throw new IllegalArgumentException("Invalid null / empty property name parameter.");

		if (value == null || value.length() == 0)
			throw new IllegalArgumentException("Invalid null / empty string value parameter.");
		
		this.stringProperty = new StringProperty(propertyName, value);
		this.operator = operator;
	}
	
	public Filter(String propertyName, FilterOperator operator, int value)
	{
		if (propertyName == null || propertyName.length() == 0)
			throw new IllegalArgumentException("Invalid null / empty property name parameter.");
		
		this.integerProperty = new IntegerProperty(propertyName,value);
		this.operator = operator;
	}
	
	public Filter(String propertyName, FilterOperator operator, long value)
	{
		if (propertyName == null || propertyName.length() == 0)
			throw new IllegalArgumentException("Invalid null / empty property name parameter.");
		
		this.longProperty = new LongProperty(propertyName, value);
		this.operator = operator;
	}
	
	public Filter(String propertyName, FilterOperator operator, double value)
	{
		if (propertyName == null || propertyName.length() == 0)
			throw new IllegalArgumentException("Invalid null / empty property name parameter.");
		
		this.doubleProperty = new DoubleProperty(propertyName, value);
		this.operator = operator;
	}
	
	public Filter(String propertyName, FilterOperator operator, boolean value)
	{
		if (propertyName == null || propertyName.length() == 0)
			throw new IllegalArgumentException("Invalid null / empty property name parameter.");
		
		this.booleanProperty = new BooleanProperty(propertyName, value);
		this.operator = operator;
	}
	
	public Filter(String propertyName, FilterOperator operator, Date value)
	{
		if (propertyName == null || propertyName.length() == 0)
			throw new IllegalArgumentException("Invalid null / empty property name parameter.");

		if (value == null)
			throw new IllegalArgumentException("Invalid null / empty date value parameter.");
		
		this.dateProperty = new DateProperty(propertyName, value);
		this.operator = operator;
	}
	
	public Filter(String propertyName, FilterOperator operator, org.apache.axis.types.Time value)
	{
		if (propertyName == null || propertyName.length() == 0)
			throw new IllegalArgumentException("Invalid null / empty property name parameter.");

		if (value == null)
			throw new IllegalArgumentException("Invalid null / empty time value parameter.");
		
		this.timeProperty = new TimeProperty(propertyName, value);
		this.operator = operator;
	}	
	
	public Filter(String propertyName, FilterOperator operator, Object value)
	{
		if (propertyName == null || propertyName.length() == 0)
			throw new IllegalArgumentException("Invalid null / empty property name parameter.");

		if (value == null)
			throw new IllegalArgumentException("Invalid null / empty time value parameter.");
		
		if (value instanceof String)
		{
			this.stringProperty = new StringProperty(propertyName, (String)value);
		} 
		else if (value instanceof Integer)
		{
			this.integerProperty = new IntegerProperty(propertyName, ((Integer)value));
		}
		else if (value instanceof Long)
		{
			this.longProperty = new LongProperty(propertyName, (Long)value);
		}
		else if (value instanceof Double)
		{
			this.doubleProperty = new DoubleProperty(propertyName, (Double)value);
		}
		else if (value instanceof Boolean)
		{
			this.booleanProperty = new BooleanProperty(propertyName, (Boolean)value);
		}
		else if (value instanceof Date)
		{
			this.dateProperty = new DateProperty(propertyName, (Date)value);
		}
		else if (value instanceof org.apache.axis.types.Time)
		{
			this.timeProperty = new TimeProperty(propertyName, (org.apache.axis.types.Time)value);
		}
		else 
		{
			throw new IllegalArgumentException("Invalid value data type.  Type not supported - " + value.getClass().getName());
		}
		
		this.operator = operator;
	}	
	
	private Filter(Filter lval, FilterOperator op, Filter rval)
	{
		if (lval == null)
			throw new IllegalArgumentException("Invalid null / empty left filter parameter.");

		if (rval == null)
			throw new IllegalArgumentException("Invalid null / empty right filter parameter.");

		leftFilter = lval;
		rightFilter = rval;
		operator = op;
	}		

	public static Filter AND (Filter filter1, Filter filter2)
	{
		return new Filter(filter1, FilterOperator.AND, filter2);
	}

	public static Filter OR (Filter filter1, Filter filter2)
	{
		return new Filter(filter1, FilterOperator.OR, filter2);
	}
	
	public String getPropertyName ()
	{
		if (stringProperty != null)
			return stringProperty.getName();
		else if (integerProperty != null)
			return integerProperty.getName();
		else if (longProperty != null)
			return longProperty.getName();
		else if (doubleProperty != null)
			return doubleProperty.getName();
		else if (booleanProperty != null)
			return booleanProperty.getName();
		else if (dateProperty != null)
			return dateProperty.getName();
		else if (timeProperty != null)
			return timeProperty.getName();
		else 
			return null;
	}
	
	public Object getValue ()
	{
		if (stringProperty != null)
			return stringProperty.getValue();
		else if (integerProperty != null)
			return integerProperty.getValue();
		else if (longProperty != null)
			return longProperty.getValue();
		else if (doubleProperty != null)
			return doubleProperty.getValue();
		else if (booleanProperty != null)
			return booleanProperty.isValue();
		else if (dateProperty != null)
			return dateProperty.getValue();
		else if (timeProperty != null)
			return timeProperty.getValue();
		else 
			return null;
	}    

    /**
     * Gets the stringProperty value for this Filter.
     * 
     * @return stringProperty
     */
    public StringProperty getStringProperty() {
        return stringProperty;
    }


    /**
     * Sets the stringProperty value for this Filter.
     * 
     * @param stringProperty
     */
    public void setStringProperty(StringProperty stringProperty) {
        this.stringProperty = stringProperty;
    }


    /**
     * Gets the integerProperty value for this Filter.
     * 
     * @return integerProperty
     */
    public IntegerProperty getIntegerProperty() {
        return integerProperty;
    }


    /**
     * Sets the integerProperty value for this Filter.
     * 
     * @param integerProperty
     */
    public void setIntegerProperty(IntegerProperty integerProperty) {
        this.integerProperty = integerProperty;
    }


    /**
     * Gets the longProperty value for this Filter.
     * 
     * @return longProperty
     */
    public LongProperty getLongProperty() {
        return longProperty;
    }


    /**
     * Sets the longProperty value for this Filter.
     * 
     * @param longProperty
     */
    public void setLongProperty(LongProperty longProperty) {
        this.longProperty = longProperty;
    }


    /**
     * Gets the doubleProperty value for this Filter.
     * 
     * @return doubleProperty
     */
    public DoubleProperty getDoubleProperty() {
        return doubleProperty;
    }


    /**
     * Sets the doubleProperty value for this Filter.
     * 
     * @param doubleProperty
     */
    public void setDoubleProperty(DoubleProperty doubleProperty) {
        this.doubleProperty = doubleProperty;
    }


    /**
     * Gets the booleanProperty value for this Filter.
     * 
     * @return booleanProperty
     */
    public BooleanProperty getBooleanProperty() {
        return booleanProperty;
    }


    /**
     * Sets the booleanProperty value for this Filter.
     * 
     * @param booleanProperty
     */
    public void setBooleanProperty(BooleanProperty booleanProperty) {
        this.booleanProperty = booleanProperty;
    }


    /**
     * Gets the dateProperty value for this Filter.
     * 
     * @return dateProperty
     */
    public DateProperty getDateProperty() {
        return dateProperty;
    }


    /**
     * Sets the dateProperty value for this Filter.
     * 
     * @param dateProperty
     */
    public void setDateProperty(DateProperty dateProperty) {
        this.dateProperty = dateProperty;
    }


    /**
     * Gets the timeProperty value for this Filter.
     * 
     * @return timeProperty
     */
    public TimeProperty getTimeProperty() {
        return timeProperty;
    }


    /**
     * Sets the timeProperty value for this Filter.
     * 
     * @param timeProperty
     */
    public void setTimeProperty(TimeProperty timeProperty) {
        this.timeProperty = timeProperty;
    }


    /**
     * Gets the leftFilter value for this Filter.
     * 
     * @return leftFilter
     */
    public Filter getLeftFilter() {
        return leftFilter;
    }


    /**
     * Sets the leftFilter value for this Filter.
     * 
     * @param leftFilter
     */
    public void setLeftFilter(Filter leftFilter) {
        this.leftFilter = leftFilter;
    }


    /**
     * Gets the rightFilter value for this Filter.
     * 
     * @return rightFilter
     */
    public Filter getRightFilter() {
        return rightFilter;
    }


    /**
     * Sets the rightFilter value for this Filter.
     * 
     * @param rightFilter
     */
    public void setRightFilter(Filter rightFilter) {
        this.rightFilter = rightFilter;
    }


    /**
     * Gets the operator value for this Filter.
     * 
     * @return operator
     */
    public FilterOperator getOperator() {
        return operator;
    }


    /**
     * Sets the operator value for this Filter.
     * 
     * @param operator
     */
    public void setOperator(FilterOperator operator) {
        this.operator = operator;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof Filter)) return false;
        Filter other = (Filter) obj;
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
              this.stringProperty.equals(other.getStringProperty()))) &&
            ((this.integerProperty==null && other.getIntegerProperty()==null) || 
             (this.integerProperty!=null &&
              this.integerProperty.equals(other.getIntegerProperty()))) &&
            ((this.longProperty==null && other.getLongProperty()==null) || 
             (this.longProperty!=null &&
              this.longProperty.equals(other.getLongProperty()))) &&
            ((this.doubleProperty==null && other.getDoubleProperty()==null) || 
             (this.doubleProperty!=null &&
              this.doubleProperty.equals(other.getDoubleProperty()))) &&
            ((this.booleanProperty==null && other.getBooleanProperty()==null) || 
             (this.booleanProperty!=null &&
              this.booleanProperty.equals(other.getBooleanProperty()))) &&
            ((this.dateProperty==null && other.getDateProperty()==null) || 
             (this.dateProperty!=null &&
              this.dateProperty.equals(other.getDateProperty()))) &&
            ((this.timeProperty==null && other.getTimeProperty()==null) || 
             (this.timeProperty!=null &&
              this.timeProperty.equals(other.getTimeProperty()))) &&
            ((this.leftFilter==null && other.getLeftFilter()==null) || 
             (this.leftFilter!=null &&
              this.leftFilter.equals(other.getLeftFilter()))) &&
            ((this.rightFilter==null && other.getRightFilter()==null) || 
             (this.rightFilter!=null &&
              this.rightFilter.equals(other.getRightFilter()))) &&
            ((this.operator==null && other.getOperator()==null) || 
             (this.operator!=null &&
              this.operator.equals(other.getOperator())));
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
            _hashCode += getStringProperty().hashCode();
        }
        if (getIntegerProperty() != null) {
            _hashCode += getIntegerProperty().hashCode();
        }
        if (getLongProperty() != null) {
            _hashCode += getLongProperty().hashCode();
        }
        if (getDoubleProperty() != null) {
            _hashCode += getDoubleProperty().hashCode();
        }
        if (getBooleanProperty() != null) {
            _hashCode += getBooleanProperty().hashCode();
        }
        if (getDateProperty() != null) {
            _hashCode += getDateProperty().hashCode();
        }
        if (getTimeProperty() != null) {
            _hashCode += getTimeProperty().hashCode();
        }
        if (getLeftFilter() != null) {
            _hashCode += getLeftFilter().hashCode();
        }
        if (getRightFilter() != null) {
            _hashCode += getRightFilter().hashCode();
        }
        if (getOperator() != null) {
            _hashCode += getOperator().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(Filter.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "Filter"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("stringProperty");
        elemField.setXmlName(new javax.xml.namespace.QName("", "StringProperty"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "StringProperty"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("integerProperty");
        elemField.setXmlName(new javax.xml.namespace.QName("", "IntegerProperty"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "IntegerProperty"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("longProperty");
        elemField.setXmlName(new javax.xml.namespace.QName("", "LongProperty"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "LongProperty"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("doubleProperty");
        elemField.setXmlName(new javax.xml.namespace.QName("", "DoubleProperty"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "DoubleProperty"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("booleanProperty");
        elemField.setXmlName(new javax.xml.namespace.QName("", "BooleanProperty"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "BooleanProperty"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("dateProperty");
        elemField.setXmlName(new javax.xml.namespace.QName("", "DateProperty"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "DateProperty"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("timeProperty");
        elemField.setXmlName(new javax.xml.namespace.QName("", "TimeProperty"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "TimeProperty"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("leftFilter");
        elemField.setXmlName(new javax.xml.namespace.QName("", "LeftFilter"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "Filter"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("rightFilter");
        elemField.setXmlName(new javax.xml.namespace.QName("", "RightFilter"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "Filter"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("operator");
        elemField.setXmlName(new javax.xml.namespace.QName("", "Operator"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "FilterOperator"));
        elemField.setNillable(false);
        elemField.setMinOccurs(1);
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
