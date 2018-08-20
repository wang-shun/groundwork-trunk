/*
 * Collage - The ultimate data integration framework. Copyright (C) 2004-2007
 * GroundWork Open Source Solutions info@groundworkopensource.com
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of version 2 of the GNU General Public License as published by the
 * Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
 */
package org.groundwork.foundation.bs;

public class ServiceNotifyAction implements java.io.Serializable {
    private static final long serialVersionUID = 1;

    private java.lang.String _value_;

    private static java.util.HashMap<String, ServiceNotifyAction> _table_ = new java.util.HashMap<String, ServiceNotifyAction>(
            3);

    // Constructor
    protected ServiceNotifyAction(java.lang.String value) {
        _value_ = value;
        _table_.put(_value_, this);
    }

    public static final ServiceNotifyAction CREATE = new ServiceNotifyAction(
            "CREATE");
    public static final ServiceNotifyAction UPDATE = new ServiceNotifyAction(
            "UPDATE");
    public static final ServiceNotifyAction RENAME = new ServiceNotifyAction(
            "RENAME");
    public static final ServiceNotifyAction DELETE = new ServiceNotifyAction(
            "DELETE");
    public static final ServiceNotifyAction UPDATE_ACKNOWLEDGE = new ServiceNotifyAction(
            "UPDATE_ACKNOWLEDGE");

    public java.lang.String getValue() {
        return _value_;
    }

    public static ServiceNotifyAction fromValue(java.lang.String value)
            throws java.lang.IllegalArgumentException {
        ServiceNotifyAction enumeration = _table_.get(value);
        if (enumeration == null)
            throw new java.lang.IllegalArgumentException();
        return enumeration;
    }

    public static ServiceNotifyAction fromString(java.lang.String value)
            throws java.lang.IllegalArgumentException {
        return fromValue(value);
    }

    @Override
    public boolean equals(java.lang.Object obj) {
        return (obj == this);
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public java.lang.String toString() {
        return _value_;
    }
}