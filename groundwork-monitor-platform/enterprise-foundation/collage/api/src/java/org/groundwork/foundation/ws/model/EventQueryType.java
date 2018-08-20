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

/*Created on: Apr 6, 2006 */

package org.groundwork.foundation.ws.model;

/**
 * enumeration of predefined Objects
 * 
 * The enum has to be in it's own class and not in an inner class otherwise
 * the webservice serialization won't work
 */
public interface EventQueryType
{
    public static final java.lang.String _ALL = "ALL";
    public static final java.lang.String _DEVICEID = "DEVICEID";
    public static final java.lang.String _DEVICEIDENTIFICATION = "DEVICEIDENTIFICATION";
    public static final java.lang.String _HOSTGROUPID = "HOSTGROUPID";
    public static final java.lang.String _HOSTGROUPNAME = "HOSTGROUPNAME";
    public static final java.lang.String _HOSTID = "HOSTID";
    public static final java.lang.String _HOSTNAME = "HOSTNAME";
    public static final java.lang.String _SERVICEDESCRIPTION = "SERVICEDESCRIPTION";
    public static final java.lang.String _EVENTID = "EVENTID";
    public static final java.lang.String _FOUNDATION_QUERY_PREPARE = "FOUNDATION_QUERY_PREPARE";
    
	public String getValue();
    
};
