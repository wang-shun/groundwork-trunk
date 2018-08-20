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
package org.groundwork.foundation.bs;

public class ServiceNotifyEntityType implements java.io.Serializable 
{
	private static final long serialVersionUID = 1;
	
    private java.lang.String _value_;
    
    private static java.util.HashMap<String, ServiceNotifyEntityType> _table_ = 
    	new java.util.HashMap<String, ServiceNotifyEntityType>(5);

    // Constructor
    protected ServiceNotifyEntityType(java.lang.String value) {
        _value_ = value;
        _table_.put(_value_,this);
    }

    public static final ServiceNotifyEntityType LOG_MESSAGE = new ServiceNotifyEntityType("LOG_MESSAGE");
    public static final ServiceNotifyEntityType HOST = new ServiceNotifyEntityType("HOST");
    public static final ServiceNotifyEntityType HOSTGROUP = new ServiceNotifyEntityType("HOSTGROUP");
    public static final ServiceNotifyEntityType SERVICEGROUP = new ServiceNotifyEntityType("SERVICEGROUP");
    public static final ServiceNotifyEntityType SERVICESTATUS = new ServiceNotifyEntityType("SERVICESTATUS");
    public static final ServiceNotifyEntityType CUSTOMGROUP = new ServiceNotifyEntityType("CUSTOMGROUP");

    public java.lang.String getValue() { return _value_;}
    
    public static ServiceNotifyEntityType fromValue(java.lang.String value)
          throws java.lang.IllegalArgumentException {
    	ServiceNotifyEntityType enumeration = (ServiceNotifyEntityType)
            _table_.get(value);
        if (enumeration==null) throw new java.lang.IllegalArgumentException();
        return enumeration;
    }
    
    public static ServiceNotifyEntityType fromString(java.lang.String value)
          throws java.lang.IllegalArgumentException {
        return fromValue(value);
    }
    
    public boolean equals(java.lang.Object obj) {return (obj == this);}
    
    public int hashCode() { return toString().hashCode();}
    
    public java.lang.String toString() { return _value_;}
}