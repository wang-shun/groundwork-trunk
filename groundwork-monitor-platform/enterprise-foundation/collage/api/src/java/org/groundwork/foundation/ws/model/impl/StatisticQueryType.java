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

public class StatisticQueryType implements org.groundwork.foundation.ws.model.StatisticQueryType, java.io.Serializable {
    private java.lang.String _value_;
    private static java.util.HashMap _table_ = new java.util.HashMap();

    // Constructor
    protected StatisticQueryType(java.lang.String value) {
        _value_ = value;
        _table_.put(_value_,this);
    }
   
    public static final StatisticQueryType ALL_HOSTS = new StatisticQueryType(_ALL_HOSTS);
    public static final StatisticQueryType ALL_SERVICES = new StatisticQueryType(_ALL_SERVICES);
    public static final StatisticQueryType HOSTS_BY_HOSTGROUPID = new StatisticQueryType(_HOSTS_FOR_HOSTGROUPID);
    public static final StatisticQueryType HOSTS_BY_HOSTGROUPNAME = new StatisticQueryType(_HOSTS_FOR_HOSTGROUPNAME);
    public static final StatisticQueryType SERVICES_BY_HOSTGROUPID = new StatisticQueryType(_SERVICES_FOR_HOSTGROUPID);
    public static final StatisticQueryType SERVICES_BY_HOSTGROUPNAME = new StatisticQueryType(_SERVICES_FOR_HOSTGROUPNAME);
    public static final StatisticQueryType TOTALS_FOR_SERVICES_BY_HOSTNAME = new StatisticQueryType(_TOTALS_FOR_SERVICES_BY_HOSTNAME);       
    public static final StatisticQueryType TOTALS_BY_HOSTS = new StatisticQueryType(_TOTALS_FOR_HOSTS);
    public static final StatisticQueryType TOTALS_BY_SERVICES = new StatisticQueryType(_TOTALS_FOR_SERVICES);
    public static final StatisticQueryType HOSTGROUP_STATE_COUNTS_HOST = new StatisticQueryType(_HOSTGROUP_STATE_COUNTS_HOST);
    public static final StatisticQueryType HOSTGROUP_STATE_COUNTS_SERVICE = new StatisticQueryType(_HOSTGROUP_STATE_COUNTS_SERVICE);
    public static final StatisticQueryType SERVICEGROUP_STATS_BY_SERVICEGROUPNAME = new StatisticQueryType(_SERVICEGROUP_STATS_BY_SERVICEGROUPNAME);
    
    public static final  StatisticQueryType  SERVICEGROUP_STATS_FOR_ALL_NETWORK = new StatisticQueryType(_SERVICEGROUP_STATS_FOR_ALL_NETWORK);
    public static final  StatisticQueryType  HOSTGROUP_STATISTICS_BY_FILTER =new StatisticQueryType(_HOSTGROUP_STATISTICS_BY_FILTER);
    public static final  StatisticQueryType  SERVICEGROUP_STATISTICS_BY_FILTER = new StatisticQueryType(_SERVICEGROUP_STATISTICS_BY_FILTER);
    public static final  StatisticQueryType  SERVICE_STATISTICS_BY_FILTER = new StatisticQueryType(_SERVICE_STATISTICS_BY_FILTER);
    public static final  StatisticQueryType  HOST_LIST = new StatisticQueryType(_HOST_LIST);
    public static final  StatisticQueryType SERVICE_ID_LIST = new StatisticQueryType(_SERVICE_ID_LIST);
    
    
    public java.lang.String getValue() { return _value_;}
    public static StatisticQueryType fromValue(java.lang.String value)
          throws java.lang.IllegalArgumentException {
        StatisticQueryType enumeration = (StatisticQueryType)
            _table_.get(value);
        if (enumeration==null) throw new java.lang.IllegalArgumentException();
        return enumeration;
    }
    public static StatisticQueryType fromString(java.lang.String value)
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
        new org.apache.axis.description.TypeDesc(StatisticQueryType.class);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "StatisticQueryType"));
    }
    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

}
