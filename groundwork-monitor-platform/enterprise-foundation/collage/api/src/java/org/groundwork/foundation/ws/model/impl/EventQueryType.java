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

public class EventQueryType implements org.groundwork.foundation.ws.model.EventQueryType , java.io.Serializable {
    

    private java.lang.String _value_;
    private static java.util.HashMap _table_ = new java.util.HashMap();

    // Constructor
    protected EventQueryType(java.lang.String value) {
        _value_ = value;
        _table_.put(_value_,this);
    }

    public static final EventQueryType ALL = new EventQueryType(_ALL);
    public static final EventQueryType DEVICEID = new EventQueryType(_DEVICEID);
    public static final EventQueryType DEVICEIDENTIFICATION = new EventQueryType(_DEVICEIDENTIFICATION);
    public static final EventQueryType HOSTGROUPID = new EventQueryType(_HOSTGROUPID);
    public static final EventQueryType HOSTGROUPNAME = new EventQueryType(_HOSTGROUPNAME);
    public static final EventQueryType HOSTID = new EventQueryType(_HOSTID);
    public static final EventQueryType HOSTNAME = new EventQueryType(_HOSTNAME);
    public static final EventQueryType SERVICEDESCRIPTION = new EventQueryType(_SERVICEDESCRIPTION);
    public static final EventQueryType EVENTID = new EventQueryType(_EVENTID);
    public static final EventQueryType FOUNDATION_QUERY_PREPARE = new EventQueryType(_FOUNDATION_QUERY_PREPARE);
    
    public java.lang.String getValue() { return _value_;}
    public static EventQueryType fromValue(java.lang.String value)
          throws java.lang.IllegalArgumentException {
        EventQueryType enumeration = (EventQueryType)
            _table_.get(value);
        if (enumeration==null) throw new java.lang.IllegalArgumentException();
        return enumeration;
    }
    public static EventQueryType fromString(java.lang.String value)
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
        new org.apache.axis.description.TypeDesc(EventQueryType.class);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "EventQueryType"));
    }
    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

}
