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

public class FilterOperator implements java.io.Serializable
{
    public static final java.lang.String _AND = "AND";
    public static final java.lang.String _OR = "OR";
    public static final java.lang.String _LT = "LT";
    public static final java.lang.String _LE = "LE";
    public static final java.lang.String _GT = "GT";
    public static final java.lang.String _GE = "GE";    
    public static final java.lang.String _EQ = "EQ";
    public static final java.lang.String _NE = "NE";
    public static final java.lang.String _LIKE = "LIKE";
    public static final java.lang.String _IN = "IN";
    
    private java.lang.String _value_;
    private static java.util.HashMap<String, FilterOperator> _table_ = 
    	new java.util.HashMap<String, FilterOperator>(9);

    // Constructor
    protected FilterOperator(java.lang.String value) {
        _value_ = value;
        _table_.put(_value_, this);
    }

    public static final FilterOperator AND = new FilterOperator(_AND);
    public static final FilterOperator OR = new FilterOperator(_OR);
    public static final FilterOperator LT = new FilterOperator(_LT);
    public static final FilterOperator LE = new FilterOperator(_LE);
    public static final FilterOperator GT = new FilterOperator(_GT);
    public static final FilterOperator GE = new FilterOperator(_GE);
    public static final FilterOperator EQ = new FilterOperator(_EQ);
    public static final FilterOperator NE = new FilterOperator(_NE);
    public static final FilterOperator LIKE = new FilterOperator(_LIKE);
    public static final FilterOperator IN = new FilterOperator(_IN);
    
    public java.lang.String getValue() { return _value_;}
    
    public static FilterOperator fromValue(java.lang.String value)
          throws java.lang.IllegalArgumentException {
    	FilterOperator enumeration = _table_.get(value);
        if (enumeration==null) throw new java.lang.IllegalArgumentException();
        return enumeration;
    }
    
    public static FilterOperator fromString(java.lang.String value)
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
        new org.apache.axis.description.TypeDesc(org.groundwork.foundation.ws.model.impl.FilterOperator.class);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "FilterOperator"));
    }
    
    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

}
