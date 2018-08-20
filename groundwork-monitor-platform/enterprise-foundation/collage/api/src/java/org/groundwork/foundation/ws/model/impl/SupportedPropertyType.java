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

public class SupportedPropertyType implements java.io.Serializable, org.groundwork.foundation.ws.model.SupportedPropertyType {
    private java.lang.String _value_;
    private static java.util.HashMap _table_ = new java.util.HashMap();

    // Constructor
    protected SupportedPropertyType(java.lang.String value) {
        _value_ = value;
        _table_.put(_value_,this);
    }

    public static final SupportedPropertyType OPERATIONSTATUS = new SupportedPropertyType(_OPERATIONSTATUS);
    public static final SupportedPropertyType SEVERITY = new SupportedPropertyType(_SEVERITY);
    public static final SupportedPropertyType COMPONENT = new SupportedPropertyType(_COMPONENT);
    public static final SupportedPropertyType TYPERULE = new SupportedPropertyType(_TYPERULE);
    public static final SupportedPropertyType PRIORITY = new SupportedPropertyType(_PRIORITY);
    public static final SupportedPropertyType HOSTSTATUS = new SupportedPropertyType(_HOSTSTATUS);
    public static final SupportedPropertyType MONITORSTATUS = new SupportedPropertyType(_MONITORSTATUS);
    public static final SupportedPropertyType SERVICESTATUS = new SupportedPropertyType(_SERVICESTATUS);
    public static final SupportedPropertyType STATETYPE = new SupportedPropertyType(_STATETYPE);
    public static final SupportedPropertyType CHECKTYPE = new SupportedPropertyType(_CHECKTYPE);
    public static final SupportedPropertyType HOST = new SupportedPropertyType(_HOST);
    public static final SupportedPropertyType HOSTGROUP = new SupportedPropertyType(_HOSTGROUP);
    public static final SupportedPropertyType DEVICE = new SupportedPropertyType(_DEVICE);
    public static final SupportedPropertyType INT = new SupportedPropertyType(_INT);
    public static final SupportedPropertyType STRING = new SupportedPropertyType(_STRING);
    public static final SupportedPropertyType LONG = new SupportedPropertyType(_LONG);
    public static final SupportedPropertyType BOOLEAN = new SupportedPropertyType(_BOOLEAN);
    public static final SupportedPropertyType DOUBLE = new SupportedPropertyType(_DOUBLE);
    public static final SupportedPropertyType DATE = new SupportedPropertyType(_DATE);
    public static final SupportedPropertyType TIME = new SupportedPropertyType(_TIME);
    public java.lang.String getValue() { return _value_;}
    public static SupportedPropertyType fromValue(java.lang.String value)
          throws java.lang.IllegalArgumentException {
        SupportedPropertyType enumeration = (SupportedPropertyType)
            _table_.get(value);
        if (enumeration==null) throw new java.lang.IllegalArgumentException();
        return enumeration;
    }
    public static SupportedPropertyType fromString(java.lang.String value)
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
           javax.xml.namespace.QName _xmlType) {
        return 
          new org.apache.axis.encoding.ser.EnumDeserializer(
            _javaType, _xmlType);
    }
    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(SupportedPropertyType.class);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "SupportedPropertyType"));
    }
    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

}
