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

public class HostQueryType implements org.groundwork.foundation.ws.model.HostQueryType ,java.io.Serializable {
    private java.lang.String _value_;
    private static java.util.HashMap _table_ = new java.util.HashMap();

    // Constructor
    protected HostQueryType(java.lang.String value) {
        _value_ = value;
        _table_.put(_value_,this);
    }

    public static final java.lang.String _ALL = "ALL";
    public static final java.lang.String _HOSTGROUPID = "HOSTGROUPID";
    public static final java.lang.String _HOSTGROUPNAME = "HOSTGROUPNAME";
    public static final java.lang.String _SERVICEDESCRIPTION = "SERVICEDESCRIPTION";
    public static final java.lang.String _MONITORSERVERNAME = "MONITORSERVERNAME";
    public static final java.lang.String _HOSTNAME = "HOSTNAME";
    public static final java.lang.String _HOSTID = "HOSTID";
    public static final java.lang.String _DEVICEIDENTIFICATION = "DEVICEIDENTIFICATION";
    public static final java.lang.String _DEVICEID = "DEVICEID";
    public static final HostQueryType ALL = new HostQueryType(_ALL);
    public static final HostQueryType HOSTGROUPID = new HostQueryType(_HOSTGROUPID);
    public static final HostQueryType HOSTGROUPNAME = new HostQueryType(_HOSTGROUPNAME);
    public static final HostQueryType SERVICEDESCRIPTION = new HostQueryType(_SERVICEDESCRIPTION);
    public static final HostQueryType MONITORSERVERNAME = new HostQueryType(_MONITORSERVERNAME);
    public static final HostQueryType HOSTNAME = new HostQueryType(_HOSTNAME);
    public static final HostQueryType HOSTID = new HostQueryType(_HOSTID);
    public static final HostQueryType DEVICEIDENTIFICATION = new HostQueryType(_DEVICEIDENTIFICATION);
    public static final HostQueryType DEVICEID = new HostQueryType(_DEVICEID);
    public java.lang.String getValue() { return _value_;}
    public static HostQueryType fromValue(java.lang.String value)
          throws java.lang.IllegalArgumentException {
        HostQueryType enumeration = (HostQueryType)
            _table_.get(value);
        if (enumeration==null) throw new java.lang.IllegalArgumentException();
        return enumeration;
    }
    public static HostQueryType fromString(java.lang.String value)
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
        new org.apache.axis.description.TypeDesc(HostQueryType.class);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "HostQueryType"));
    }
    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

}
