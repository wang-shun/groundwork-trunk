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

public interface NagiosStatisticQueryType
{
    public static final java.lang.String _HOSTGROUPID = "HOSTGROUPID";
    public static final java.lang.String _HOSTGROUPNAME = "HOSTGROUPNAME";
    public static final java.lang.String _HOSTNAME = "HOSTNAME";
    public static final java.lang.String _HOSTID = "HOSTID";
    public static final java.lang.String _SYSTEM = "SYSTEM";
    public static final java.lang.String _HOSTLIST = "HOSTLIST";
    public static final java.lang.String _SERVICEGROUPNAME = "SERVICEGROUPNAME";
    
    public String getValue();

}
