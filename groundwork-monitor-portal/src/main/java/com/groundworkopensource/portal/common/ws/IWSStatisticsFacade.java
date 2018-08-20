/*
 * 
 * Copyright 2007 GroundWork Open Source, Inc. ("GroundWork") All rights
 * reserved. This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package com.groundworkopensource.portal.common.ws;

import java.util.Map;

import org.groundwork.foundation.ws.model.impl.Filter;
import org.groundwork.foundation.ws.model.impl.NagiosStatisticProperty;
import org.groundwork.foundation.ws.model.impl.NagiosStatisticQueryType;
import org.groundwork.foundation.ws.model.impl.StateStatistics;
import org.groundwork.foundation.ws.model.impl.StatisticProperty;
import org.groundwork.foundation.ws.model.impl.StatisticQueryType;

import com.groundworkopensource.portal.common.exception.GWPortalException;
import com.groundworkopensource.portal.common.exception.WSDataUnavailableException;

/**
 * Interface defining methods for "statistics" web service
 * 
 * @author swapnil_gujrathi
 */
public interface IWSStatisticsFacade {

    /**
     * Returns the statistical data by invoking WSStatistics web service. TBD
     * 
     * @param serviceGroupName
     * 
     * @return StatisticProperty Array: service statistics of passed
     *         service-group name
     */
    StatisticProperty[] getServiceStatisticsForServiceGroup(
            String serviceGroupName);

    /**
     * Returns the statistical data by invoking WSStatistics web service.
     * 
     * @param statisticQueryType
     * @param filter
     * @param name
     * @param applicationType
     * @return Map of Statistics
     * @throws WSDataUnavailableException
     * @throws GWPortalException
     */
    Map<String, Long> getFilteredServiceStatistics(
            StatisticQueryType statisticQueryType, Filter filter, String name,
            String applicationType) throws WSDataUnavailableException,
            GWPortalException;

    /**
     * Returns the statistical data by invoking WSStatistics web service. TBD
     * 
     * @return Map of Statistics
     */
    Map<ServiceState, Long> getServiceGroupStatisticsForEntireNetwork();

    /**
     * Returns hostgroup statistics data for selected hostgroup name.
     * 
     * @param hostGroup
     * 
     * @return StatisticProperty Array: host statistics of passed host-group
     *         name
     * @throws WSDataUnavailableException
     */
    StatisticProperty[] getHostStatisticsForHostGroup(String hostGroup)
            throws WSDataUnavailableException;

    /**
     * Returns service statistics data for selected hostgroup name.
     * 
     * @param hostGroup
     * 
     * @return StatisticProperty Array: service statistics of passed host-group
     *         name
     * @throws WSDataUnavailableException
     */
    StatisticProperty[] getServiceStatisticsForHostGroup(String hostGroup)
            throws WSDataUnavailableException;

    /**
     * return the map which contain services group status and Statistics Example
     * :-map<"OK" 20>
     * 
     * @return map
     * @throws WSDataUnavailableException
     * @throws GWPortalException
     */
    Map<String, Long> getEntireNetworkServiceGroupStatistics()
            throws WSDataUnavailableException, GWPortalException;

    /**
     * return the map which contain host group status and Statistics. Example
     * :-map<"DOWN" 20>
     * 
     * @return Map
     * @throws WSDataUnavailableException
     */
    Map<String, Long> getEntireNetworkHostGroupStatistics()
            throws WSDataUnavailableException;

    /**
     * return the map which contain Host status and Statistics. Example
     * :-map<"UP" 20>
     * 
     * @return Map
     * @throws GWPortalException
     * @throws WSDataUnavailableException
     */
    Map<String, Long> getEntireNetworkHostStatistics()
            throws GWPortalException, WSDataUnavailableException;

    /**
     * return the map which contain services status and Statistics. Example
     * :-map<"OK" 20>
     * 
     * @return Map
     * @throws GWPortalException
     * @throws WSDataUnavailableException
     */
    Map<String, Long> getEntireNetworkServiceStatistics()
            throws GWPortalException, WSDataUnavailableException;

    /**
     * @param queryType
     * 
     * @param queryValue
     * @return NagiosStatisticProperty Array
     * @throws GWPortalException
     * @throws WSDataUnavailableException
     */
    NagiosStatisticProperty[] getNagiosStatisticsForHostGroup(String queryValue)
            throws GWPortalException, WSDataUnavailableException;

    /**
     * @param queryValue
     * @return NagiosStatisticProperty Array
     * @throws GWPortalException
     * @throws WSDataUnavailableException
     */
    NagiosStatisticProperty[] getNagiosStatisticsForServiceGroup(
            String queryValue) throws GWPortalException,
            WSDataUnavailableException;

    /**
     * @return NagiosStatisticProperty Array
     * @throws GWPortalException
     * @throws WSDataUnavailableException
     */
    NagiosStatisticProperty[] getNagiosStatisticsForNetwork()
            throws GWPortalException, WSDataUnavailableException;

    /**
     * Retrieves NagiosStatistics as per NagiosStatisticQueryType.
     * 
     * @param queryType
     * @param qyeryType
     * @param queryValue
     * @return NagiosStatisticProperty Array
     * @throws GWPortalException
     * @throws WSDataUnavailableException
     */
    NagiosStatisticProperty[] getNagiosStatistics(
            NagiosStatisticQueryType queryType, String queryValue)
            throws GWPortalException, WSDataUnavailableException;

    /**
     * Returns host availability for the Host Group. In case of any exception
     * (or if not able to contact server) returns 0.
     * 
     * @param hostGroupName
     * @return Host Availability for Host Group.
     */
    double getHostAvailabilityForHostgroup(String hostGroupName);

    /**
     * Returns service availability for the Host Group. In case of any exception
     * (or if not able to contact server) returns 0.
     * 
     * @param hostGroupName
     * @return Service Availability for Host Group.
     */
    double getServiceAvailabilityForHostgroup(String hostGroupName);

    /**
     * Returns service availability for the Service Group. In case of any
     * exception (or if not able to contact server) throw exception.
     * 
     * @param serviceGroupName
     * @return service availability for the Service Group
     * @throws WSDataUnavailableException
     * @throws GWPortalException
     */
    double getServiceAvailabilityForServiceGroup(String serviceGroupName)
            throws WSDataUnavailableException, GWPortalException;

    /**
     * return map which contain statistics for host group by hostgroup ID.
     * 
     * @param hostGroupId
     * @return map
     * @throws WSDataUnavailableException
     */
    Map<String, Long> getHostStatisticsForHostGroupByHostgroupId(
            final String hostGroupId) throws WSDataUnavailableException;

    /**
     * return statistics depending on Applied filter .
     * 
     * @param statisticQueryType
     * 
     * @param filter
     * @param name
     * @param applicationType
     * @return StatisticProperty
     * @throws WSDataUnavailableException
     */
    Map<String, Long> getGroupStatisticsForHostGroup(
            StatisticQueryType statisticQueryType, Filter filter, String name,
            String applicationType) throws WSDataUnavailableException;

    /**
     * return the Filtered Host statistics
     * 
     * @param hostNames
     * @return map
     * @throws GWPortalException
     * @throws WSDataUnavailableException
     */
    Map<String, Long> getFilteredStatisticsForHost(String hostNames)
            throws GWPortalException, WSDataUnavailableException;

    /**
     * @param statisticQueryType
     * @param filter
     * @param name
     * @param applicationType
     * @return map
     * @throws GWPortalException
     * @throws WSDataUnavailableException
     */
    Map<String, Long> getGroupStatisticsForServicegGroup(
            StatisticQueryType statisticQueryType, Filter filter, String name,
            String applicationType) throws GWPortalException,
            WSDataUnavailableException;

    /**
     * return service statistics by Host Group Id.
     * 
     * @param hostGroupId
     * @return map
     * @throws GWPortalException
     * @throws WSDataUnavailableException
     */
    Map<String, Long> getServiceStatisticsByHostGroupId(final String hostGroupId)
            throws GWPortalException, WSDataUnavailableException;

    /**
     * return service statistics by host Name
     * 
     * @param hostName
     * @return Map
     * @throws GWPortalException
     * @throws WSDataUnavailableException
     */
    Map<String, Long> getServiceStatisticsByHostName(String hostName)
            throws GWPortalException, WSDataUnavailableException;

    /**
     * return service statistics by Service group name
     * 
     * @param serviceGroupName
     * @return map
     * @throws GWPortalException
     * @throws WSDataUnavailableException
     */
    Map<String, Long> getServiceStatisticsByServiceGroupName(
            String serviceGroupName) throws GWPortalException,
            WSDataUnavailableException;

    /**
     * return service statistics by comma separated service id's
     * 
     * @param serviceids
     * @return map
     * @throws WSDataUnavailableException
     */
    Map<String, Long> getServiceStatisticsByServiceIds(final String serviceids)
            throws WSDataUnavailableException;

    /**
     * Returns service statistics by Host Group Name.
     * 
     * @param hostGroupName
     * @return statistics map
     * @throws GWPortalException
     * @throws WSDataUnavailableException
     */
    Map<String, Long> getServiceStatisticsByHostGroupName(String hostGroupName)
            throws GWPortalException, WSDataUnavailableException;

    /**
     * Returns host statistics by Host Group Name.
     * 
     * @param hostGroupName
     * @return statistics map
     * @throws WSDataUnavailableException
     */
    Map<String, Long> getHostStatisticsForHostGroupByHostgroupName(
            String hostGroupName) throws WSDataUnavailableException;

    /**
     * return Filtered Host group name
     * 
     * @param statisticQueryType
     * @param filter
     * @param name
     * @param applicationType
     * @return StateStatistics
     * @throws WSDataUnavailableException
     */
    StateStatistics[] getFilteredHostGroupName(
            StatisticQueryType statisticQueryType, Filter filter, String name,
            String applicationType) throws WSDataUnavailableException;

}