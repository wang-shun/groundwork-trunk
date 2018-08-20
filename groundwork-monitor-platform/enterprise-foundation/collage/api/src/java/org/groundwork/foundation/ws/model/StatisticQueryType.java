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

public interface StatisticQueryType
{
    public static final java.lang.String _ALL_HOSTS = "ALL_HOSTS";
    public static final java.lang.String _ALL_SERVICES = "ALL_SERVICES";
    public static final java.lang.String _HOSTS_FOR_HOSTGROUPID = "HOSTS_FOR_HOSTGROUPID";
    public static final java.lang.String _HOSTS_FOR_HOSTGROUPNAME = "HOSTS_FOR_HOSTGROUPNAME";
    public static final java.lang.String _SERVICES_FOR_HOSTGROUPID = "SERVICES_FOR_HOSTGROUPID";
    public static final java.lang.String _SERVICES_FOR_HOSTGROUPNAME = "SERVICES_FOR_HOSTGROUPNAME";
    public static final java.lang.String _TOTALS_FOR_SERVICES_BY_HOSTNAME = "TOTALS_FOR_SERVICES_BY_HOSTNAME";
    public static final java.lang.String _TOTALS_FOR_HOSTS = "TOTALS_FOR_HOSTS";
    public static final java.lang.String _TOTALS_FOR_SERVICES = "TOTALS_FOR_SERVICES";
    public static final java.lang.String _HOSTGROUP_STATE_COUNTS_HOST = "HOSTGROUP_STATE_COUNTS_HOST";
    public static final java.lang.String _HOSTGROUP_STATE_COUNTS_SERVICE = "HOSTGROUP_STATE_COUNTS_SERVICE";
    public static final java.lang.String _SERVICEGROUP_STATS_BY_SERVICEGROUPNAME = "SERVICEGROUP_STATS_BY_SERVICEGROUPNAME";
    
    public static final java.lang.String _SERVICEGROUP_STATS_FOR_ALL_NETWORK = "SERVICEGROUP_STATS_FOR_ALL_NETWORK";
    public static final java.lang.String _HOSTGROUP_STATISTICS_BY_FILTER = "HOSTGROUP_STATISTICS_BY_FILTER";
    public static final java.lang.String _SERVICE_STATISTICS_BY_FILTER = "SERVICE_STATISTICS_BY_FILTER";
    public static final java.lang.String _SERVICEGROUP_STATISTICS_BY_FILTER = "SERVICEGROUP_STATISTICS_BY_FILTER";
    public static final java.lang.String _HOST_LIST = "HOST_LIST";
    public static final java.lang.String _SERVICE_ID_LIST = "SERVICE_ID_LIST";
    public String getValue();

}
