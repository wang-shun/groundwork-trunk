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

package org.groundwork.foundation.ws.model;

/**
 * @author glee
 *
 */
public interface SupportedPropertyType {

    public static final java.lang.String _OPERATIONSTATUS = "OPERATIONSTATUS";

    public static final java.lang.String _SEVERITY = "SEVERITY";

    public static final java.lang.String _COMPONENT = "COMPONENT";

    public static final java.lang.String _TYPERULE = "TYPERULE";

    public static final java.lang.String _PRIORITY = "PRIORITY";

    public static final java.lang.String _HOSTSTATUS = "HOSTSTATUS";

    public static final java.lang.String _MONITORSTATUS = "MONITORSTATUS";

    public static final java.lang.String _SERVICESTATUS = "SERVICESTATUS";

    public static final java.lang.String _STATETYPE = "STATETYPE";

    public static final java.lang.String _CHECKTYPE = "CHECKTYPE";

    public static final java.lang.String _HOST = "HOST";

    public static final java.lang.String _HOSTGROUP = "HOSTGROUP";

    public static final java.lang.String _DEVICE = "DEVICE";

    public static final java.lang.String _INT = "INT";

    public static final java.lang.String _STRING = "STRING";

    public static final java.lang.String _LONG = "LONG";

    public static final java.lang.String _BOOLEAN = "BOOLEAN";

    public static final java.lang.String _DOUBLE = "DOUBLE";

    public static final java.lang.String _DATE = "DATE";

    public static final java.lang.String _TIME = "TIME";

    public java.lang.String getValue();

    public boolean equals(java.lang.Object obj);

    public int hashCode();

    public java.lang.String toString();

    public java.lang.Object readResolve()
            throws java.io.ObjectStreamException;

}