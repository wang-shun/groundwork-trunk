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

import com.groundwork.collage.model.PropertyType;

public class PropertyDataType implements java.io.Serializable
{
    public static final java.lang.String _DATE = PropertyType.DATE;
    public static final java.lang.String _BOOLEAN = PropertyType.BOOLEAN;
    public static final java.lang.String _STRING = PropertyType.STRING;
    public static final java.lang.String _INTEGER = PropertyType.INTEGER;
    public static final java.lang.String _LONG = PropertyType.LONG;
    public static final java.lang.String _DOUBLE = PropertyType.DOUBLE;
    
    private java.lang.String _value_;
    private static java.util.HashMap _table_ = new java.util.HashMap();

    // Constructor
    public PropertyDataType(java.lang.String value) {
        _value_ = value;
        _table_.put(_value_,this);
    }

    public static final PropertyDataType DATE = new PropertyDataType(_DATE);
    public static final PropertyDataType BOOLEAN = new PropertyDataType(_BOOLEAN);
    public static final PropertyDataType STRING = new PropertyDataType(_STRING);
    public static final PropertyDataType INTEGER = new PropertyDataType(_INTEGER);
    public static final PropertyDataType LONG = new PropertyDataType(_LONG);
    public static final PropertyDataType DOUBLE = new PropertyDataType(_DOUBLE);
    
    public java.lang.String getValue() { return _value_;}
    public static PropertyDataType fromValue(java.lang.String value)
          throws java.lang.IllegalArgumentException {
        PropertyDataType enumeration = (PropertyDataType)
            _table_.get(value);
        if (enumeration==null) throw new java.lang.IllegalArgumentException();
        return enumeration;
    }
    public static PropertyDataType fromString(java.lang.String value)
          throws java.lang.IllegalArgumentException {
        return fromValue(value);
    }
    public boolean equals(java.lang.Object obj) {return (obj == this);}
    public int hashCode() { return toString().hashCode();}
    public java.lang.String toString() { return _value_;}
    public java.lang.Object readResolve() throws java.io.ObjectStreamException { return fromValue(_value_);}
    public static org.apache.axis.encoding.Serializer getSerializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new org.apache.axis.encoding.ser.EnumSerializer(
            _javaType, _xmlType);
    }
    public static org.apache.axis.encoding.Deserializer getDeserializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) 
    {    	
        return 
          new org.apache.axis.encoding.ser.EnumDeserializer(
            _javaType, _xmlType);
    }
    
    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(org.groundwork.foundation.ws.model.impl.PropertyDataType.class);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "PropertyDataType"));
    }
    
    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

}
