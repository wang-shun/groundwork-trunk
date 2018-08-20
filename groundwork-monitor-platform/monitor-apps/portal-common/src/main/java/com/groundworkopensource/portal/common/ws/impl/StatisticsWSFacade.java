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

package com.groundworkopensource.portal.common.ws.impl;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.rpc.ServiceException;

import org.apache.log4j.Logger;
import org.groundwork.foundation.ws.api.WSStatistics;
import org.groundwork.foundation.ws.model.impl.Filter;
import org.groundwork.foundation.ws.model.impl.NagiosStatisticProperty;
import org.groundwork.foundation.ws.model.impl.NagiosStatisticQueryType;
import org.groundwork.foundation.ws.model.impl.StateStatistics;
import org.groundwork.foundation.ws.model.impl.StatisticProperty;
import org.groundwork.foundation.ws.model.impl.StatisticQueryType;
import org.groundwork.foundation.ws.model.impl.WSFoundationCollection;
import org.groundwork.foundation.ws.model.impl.WSFoundationException;

import com.groundworkopensource.portal.common.CommonConstants;
import com.groundworkopensource.portal.common.ResourceUtils;
import com.groundworkopensource.portal.common.exception.GWPortalException;
import com.groundworkopensource.portal.common.exception.WSDataUnavailableException;
import com.groundworkopensource.portal.common.ws.IWSStatisticsFacade;
import com.groundworkopensource.portal.common.ws.ServiceState;

/**
 * This class provides methods to interact with "statistics" foundation web
 * service.
 * 
 * @author swapnil_gujrathi
 * 
 */
public class StatisticsWSFacade implements IWSStatisticsFacade {

    /**
     * logger
     */
    private static final Logger LOGGER = FoundationWSFacade.getLogger();

    /**
     * Host Status Array.
     */
    private final String[] hostStatusArray = {
            CommonConstants.UN_SCHEDULED_DOWN, CommonConstants.SCHEDULED_DOWN,
            CommonConstants.UNREACHABLE, CommonConstants.PENDING,
            CommonConstants.UP };
    /**
     * service Status Array.
     */
    private final String[] serviceStatusArray = {
            CommonConstants.UNSCHEDULED_CRITICAL,
            CommonConstants.SCHEDULED_CRITICAL, CommonConstants.WARNING,
            CommonConstants.UNKNOWN, CommonConstants.PENDING,
            CommonConstants.OK };

    /**
     * Returns Binding object for "statistics" web service
     * 
     * @return WSStatistics Binding
     * @throws GWPortalException
     */
    private WSStatistics getStatisticsBinding() {
        // get the host binding object
        try {
            return WebServiceLocator.getInstance().statisticsLocator()
                    .getstatistics();
        } catch (ServiceException e) {
            LOGGER.fatal(CommonConstants.SERVICE_EXCEPTION_MESSAGE, e);
        }
        return null;
    }

    /**
     * (non-Javadoc)
     * 
     * @throws WSDataUnavailableException
     * 
     * @see com.groundworkopensource.portal.common.ws.IWSStatisticsFacade#getHostStatisticsForHostGroup(java.lang.String)
     */
    public final StatisticProperty[] getHostStatisticsForHostGroup(
            final String hostGroup) throws WSDataUnavailableException {
        StatisticProperty[] propertyArr = new StatisticProperty[] {};
        WSStatistics statisticsBinding = getStatisticsBinding();
        if (null == statisticsBinding) {
            return propertyArr;
        }

        try {
            // get nagios statistics for entire system
            WSFoundationCollection wsfoundationCollectionstatistics = statisticsBinding
                    .getStatistics(StatisticQueryType.HOSTS_BY_HOSTGROUPNAME,
                            hostGroup, CommonConstants.EMPTY_STRING);
            if (wsfoundationCollectionstatistics == null) {

                LOGGER
                        .info("Found null WSFoundationCollection when calling getHostStatisticsForHostGroup() for host group: "
                                + hostGroup);
                throw new WSDataUnavailableException();
            }

            StateStatistics[] stateStatisticCollection = wsfoundationCollectionstatistics
                    .getStateStatisticCollection();

            if ((null == stateStatisticCollection)
                    || (stateStatisticCollection.length == 0)) {
                LOGGER
                        .info("Found null stateStatisticCollection when calling getHostStatisticsForHostGroup() for host group: "
                                + hostGroup);
                throw new WSDataUnavailableException();
            }
            // we have requested the statistics for one hostgroup, so only item
            // is present in the array - fetch 0th element

            propertyArr = stateStatisticCollection[0].getStatisticProperties();
            if (propertyArr == null) {
                LOGGER
                        .info("Found null propertyArr when calling getHostStatisticsForHostGroup() for Host group : "
                                + hostGroup);
                throw new WSDataUnavailableException();
            }

        } catch (WSFoundationException fEx) {
            LOGGER.error(CommonConstants.WSFOUNDATION_EXCEPTION_MESSAGE
                    + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + fEx);
            throw new WSDataUnavailableException();
        } catch (RemoteException rEx) {
            LOGGER.error(CommonConstants.REMOTE_EXCEPTION_MESSAGE
                    + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + rEx);
            throw new WSDataUnavailableException();
        }
        return propertyArr;
    }

    /**
     * (non-Javadoc)
     * 
     * @throws WSDataUnavailableException
     * 
     * @see com.groundworkopensource.portal.common.ws.IWSStatisticsFacade#getServiceStatisticsForHostGroup(java.lang.String)
     */
    public final StatisticProperty[] getServiceStatisticsForHostGroup(
            final String hostGroup) throws WSDataUnavailableException {
        StatisticProperty[] propertyArr = new StatisticProperty[] {};
        WSStatistics statisticsBinding = getStatisticsBinding();
        if (null == statisticsBinding) {
            return propertyArr;
        }

        try {
            // get nagios statistics for entire system
            StateStatistics[] stateStatisticCollection = statisticsBinding
                    .getStatistics(
                            StatisticQueryType.SERVICES_BY_HOSTGROUPNAME,
                            hostGroup, CommonConstants.EMPTY_STRING)
                    .getStateStatisticCollection();
            if ((null == stateStatisticCollection)
                    || (stateStatisticCollection.length == 0)) {
                LOGGER
                        .info("Found null stateStatisticCollection when calling getServiceStatisticsForHostGroup() for host group : "
                                + hostGroup);
                throw new WSDataUnavailableException();
            }
            // we have requested the statistics for one hostgroup, so only item
            // is present in the array - fetch 0th element
            propertyArr = stateStatisticCollection[0].getStatisticProperties();
            if (propertyArr == null) {
                LOGGER
                        .info("Found null propertyArr when calling getServiceStatisticsForHostGroup() for Host group : "
                                + hostGroup);
                throw new WSDataUnavailableException();
            }

        } catch (WSFoundationException fEx) {
            LOGGER.error(CommonConstants.WSFOUNDATION_EXCEPTION_MESSAGE
                    + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + fEx);
            throw new WSDataUnavailableException();
        } catch (RemoteException rEx) {
            LOGGER.error(CommonConstants.REMOTE_EXCEPTION_MESSAGE
                    + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + rEx);
            throw new WSDataUnavailableException();
        }
        return propertyArr;
    }

    /**
     * Returns the statistical data by invoking WSStatistics web service. TBD
     * 
     * @return Map of Statistics
     */

    public final Map<ServiceState, Long> getServiceGroupStatisticsForEntireNetwork() {
        // TODO implement
        return null;
    }

    /**
     * Returns the statistical data by invoking WSStatistics web service.
     * 
     * @param serviceGroupName
     * 
     * @return StatisticProperty Array: service statistics of passed
     *         service-group name
     */
    public final StatisticProperty[] getServiceStatisticsForServiceGroup(
            final String serviceGroupName) {
        StatisticProperty[] propertyArr = new StatisticProperty[] {};
        WSStatistics statisticsBinding = getStatisticsBinding();
        if (null == statisticsBinding) {
            return propertyArr;
        }

        try {
            // get service statistics for particular service group
            StateStatistics[] stateStatisticCollection = statisticsBinding
                    .getStatistics(
                            StatisticQueryType.SERVICEGROUP_STATS_BY_SERVICEGROUPNAME,
                            serviceGroupName, CommonConstants.EMPTY_STRING)
                    .getStateStatisticCollection();
            /*
             * we have requested the statistics for one service group, so only
             * item is present in the array - fetch 0th element
             */
            if (null != stateStatisticCollection
                    && stateStatisticCollection.length > 0
                    && stateStatisticCollection[0] != null) {
                propertyArr = stateStatisticCollection[0]
                        .getStatisticProperties();
                // FIXME Nitin - add null check for propertyArr and throw
                // WSDataUnavailableException
            }
        } catch (WSFoundationException fEx) {
            LOGGER.error(CommonConstants.WSFOUNDATION_EXCEPTION_MESSAGE
                    + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + fEx);
        } catch (RemoteException rEx) {
            LOGGER.error(CommonConstants.REMOTE_EXCEPTION_MESSAGE
                    + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + rEx);
        }
        return propertyArr;
    }

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

    public Map<String, Long> getFilteredServiceStatistics(
            StatisticQueryType statisticQueryType, Filter filter, String name,
            String applicationType) throws WSDataUnavailableException,
            GWPortalException {
        StatisticProperty[] propertyArray = null;
        Map<String, Long> serviceStatisticsMap = new HashMap<String, Long>();
        long total = 0;
        WSStatistics statisticsBinding = getStatisticsBinding();
        if (null == statisticsBinding) {
            throw new GWPortalException();
        }
        try {
            WSFoundationCollection wsFoundationCollection = statisticsBinding
                    .getGroupStatistics(statisticQueryType, filter, name,
                            applicationType);
            if (wsFoundationCollection == null) {
                throw new GWPortalException();
            }

            StateStatistics[] stateStatCol = wsFoundationCollection
                    .getStateStatisticCollection();

            if (stateStatCol != null && stateStatCol.length > 0) {
                propertyArray = stateStatCol[0].getStatisticProperties();
                if (propertyArray != null) {
                    if (propertyArray.length > 0) {
                        for (int i = 0; i < propertyArray.length; i++) {
                            serviceStatisticsMap
                                    .put(propertyArray[i].getName()
                                            .toUpperCase(), propertyArray[i]
                                            .getCount());
                            // calculating total count
                            total = total + propertyArray[i].getCount();
                        }
                        serviceStatisticsMap.put(
                                CommonConstants.TOTAL_COUNT_KEY, total);

                    } else {
                        for (int i = 0; i < serviceStatusArray.length; i++) {
                            serviceStatisticsMap.put(serviceStatusArray[i]
                                    .toUpperCase(), (long) 0);
                        }
                        serviceStatisticsMap.put(
                                CommonConstants.TOTAL_COUNT_KEY, (long) 0);
                    }

                } else {
                    LOGGER
                            .info("Statistic Property is null during access getGroupStatistics");
                }
            } else {
                LOGGER
                        .info("State Statistics is null during access getFilteredServiceStatistics");
            }

        } catch (WSFoundationException fEx) {
            LOGGER
                    .error(
                            "WSFoundationException while getting getGroupStatistics for Host group with ID ().",
                            fEx);
            throw new WSDataUnavailableException();
        } catch (RemoteException rEx) {
            LOGGER.error(CommonConstants.REMOTE_EXCEPTION_MESSAGE
                    + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + rEx);
            throw new WSDataUnavailableException();
        }
        return serviceStatisticsMap;
    }

    /**
     * return the entire network host group statistics
     * 
     * @return map
     * @throws WSDataUnavailableException
     */
    public Map<String, Long> getEntireNetworkHostGroupStatistics()
            throws WSDataUnavailableException {
        StatisticProperty[] statisticCollection = null;
        WSStatistics statisticsBinding = getStatisticsBinding();
        if (null == statisticsBinding) {
            return new HashMap<String, Long>();
        }

        // create map with parameter String and long which hold monitor status
        // as key and statistics as a value.
        Map<String, Long> hostGroupStatisticsMap = new HashMap<String, Long>();
        long total = 0;
        try {
            // get statistics
            WSFoundationCollection statistics = statisticsBinding
                    .getStatistics(
                            StatisticQueryType.HOSTGROUP_STATE_COUNTS_HOST,
                            CommonConstants.EMPTY_STRING,
                            CommonConstants.EMPTY_STRING);
            if (statistics == null) {
                // error occurred. throw exception
                LOGGER
                        .info("WSFoundationCollection is null in getEntireNetworkHostGroupStatistics method");
                throw new WSDataUnavailableException();
            }

            statisticCollection = statistics.getStatisticCollection();
            if (statisticCollection == null) {
                // error occurred. throw exception
                LOGGER
                        .info("StatisticProperty[] is null in getEntireNetworkHostGroupStatistics method");
                throw new WSDataUnavailableException();
            }

            for (int i = 0; i < statisticCollection.length; i++) {
                // get the status and count of hostGroup and put in to
                // map
                hostGroupStatisticsMap.put(statisticCollection[i].getName()
                        .toUpperCase(), statisticCollection[i].getCount());
                if (!CommonConstants.DOWN
                        .equalsIgnoreCase(statisticCollection[i].getName())) {
                    total = total + statisticCollection[i].getCount();
                }
            }
            // put the total count of hostGroup in map
            hostGroupStatisticsMap.put(CommonConstants.TOTAL_COUNT_KEY, total);

        } catch (WSFoundationException fEx) {
            LOGGER.error(CommonConstants.WSFOUNDATION_EXCEPTION_MESSAGE
                    + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + fEx);
            throw new WSDataUnavailableException();
        } catch (RemoteException rEx) {
            LOGGER.error(CommonConstants.REMOTE_EXCEPTION_MESSAGE
                    + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + rEx);
            throw new WSDataUnavailableException();

        }

        return hostGroupStatisticsMap;

    }

    /**
     * (non-Javadoc)
     * 
     * @param hostGroupId
     * @return MAP
     * @throws WSDataUnavailableException
     * 
     * @see com.groundworkopensource.portal.common.ws.IWSStatisticsFacade#getHostStatisticsForHostGroup(java.lang.String)
     */
    public Map<String, Long> getHostStatisticsForHostGroupByHostgroupId(
            final String hostGroupId) throws WSDataUnavailableException {
        return getHostStatisticsForHostGroupByHostgroup(
                StatisticQueryType.HOSTS_BY_HOSTGROUPID, hostGroupId);
    }

    /**
     * (non-Javadoc)
     * 
     * @see com.groundworkopensource.portal.common.ws.IWSStatisticsFacade#getHostStatisticsForHostGroupByHostgroupName(java.lang.String)
     */
    public Map<String, Long> getHostStatisticsForHostGroupByHostgroupName(
            final String hostGroupName) throws WSDataUnavailableException {
        return getHostStatisticsForHostGroupByHostgroup(
                StatisticQueryType.HOSTS_BY_HOSTGROUPNAME, hostGroupName);
    }

    /**
     * @param statisticQueryType
     * @param hostGroup
     * @return
     * @throws WSDataUnavailableException
     */
    private Map<String, Long> getHostStatisticsForHostGroupByHostgroup(
            StatisticQueryType statisticQueryType, final String hostGroup)
            throws WSDataUnavailableException {
        StateStatistics[] statisticCollection = null;
        StatisticProperty[] propertyArr = null;
        // create map with parameter String and long which hold monitor status
        // as key and statistics as a value.
        Map<String, Long> hostGroupStatisticsMap = new HashMap<String, Long>();
        WSStatistics statisticsBinding = getStatisticsBinding();
        if (statisticsBinding == null) {
            LOGGER
                    .info("statisticsBinding is null in getHostStatisticsForHostGroupByHostgroupId,This time not able to call foundation web service. ");
            return hostGroupStatisticsMap;
        }

        try {
            // get statistics
            WSFoundationCollection statistics = statisticsBinding
                    .getStatistics(statisticQueryType, hostGroup,
                            CommonConstants.EMPTY_STRING);
            if (statistics == null) {
                LOGGER
                        .info("WSFoundationCollection is null getHostStatisticsForHostGroupByHostgroupId ");
                throw new WSDataUnavailableException();
            }
            statisticCollection = statistics.getStateStatisticCollection();
            if (statisticCollection == null || statisticCollection.length <= 0) {
                LOGGER
                        .info("StateStatistics[] is null or StateStatistics[] length is less then or equal 0 in  getHostStatisticsForHostGroupByHostgroupId method");
                throw new WSDataUnavailableException();

            }
            propertyArr = statisticCollection[0].getStatisticProperties();
            if (propertyArr == null) {
                LOGGER
                        .info("StatisticProperty[] is null or StatisticProperty[] length is less then or equal 0 in  getHostStatisticsForHostGroupByHostgroupId method");
                throw new WSDataUnavailableException();
            }
            for (int i = 0; i < propertyArr.length; i++) {
                // get the status and count of hostGroup and put in
                // to
                // map
                hostGroupStatisticsMap.put(propertyArr[i].getName()
                        .toUpperCase(), propertyArr[i].getCount());

            }

            // put the total count of hostGroup in map
            hostGroupStatisticsMap.put(CommonConstants.TOTAL_COUNT_KEY,
                    statisticCollection[0].getTotalHosts());

        } catch (WSFoundationException fEx) {
            LOGGER.error(CommonConstants.WSFOUNDATION_EXCEPTION_MESSAGE
                    + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + fEx);
            throw new WSDataUnavailableException();
        } catch (RemoteException rEx) {
            LOGGER.error(CommonConstants.REMOTE_EXCEPTION_MESSAGE
                    + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + rEx);
            throw new WSDataUnavailableException();
        }

        return hostGroupStatisticsMap;
    }

    /**
     * return the entire network service group statistics
     * 
     * @return map
     * @throws WSDataUnavailableException
     * @throws GWPortalException
     */
    public Map<String, Long> getEntireNetworkServiceGroupStatistics()
            throws WSDataUnavailableException, GWPortalException {
        StatisticProperty[] statisticCollection = null;
        // create map with parameter String and long which hold monitor status
        // as key and statistics as a value.
        Map<String, Long> serviceStatisticsMap = new HashMap<String, Long>();
        WSStatistics statisticsBinding = getStatisticsBinding();
        long total = 0;
        try {
            // get statistics
            if (statisticsBinding == null) {
                LOGGER
                        .info("statisticsBinding is null in getEntireNetworkServiceGroupStatistics method");
                throw new GWPortalException();
            }
            WSFoundationCollection statistics = statisticsBinding
                    .getStatistics(
                            StatisticQueryType.SERVICEGROUP_STATS_FOR_ALL_NETWORK,
                            CommonConstants.EMPTY_STRING,
                            CommonConstants.NAGIOS);

            if (statistics != null) {
                StateStatistics[] stateStatisticCollection = statistics
                        .getStateStatisticCollection();
                long ok = 0;
                long pending = 0;
                long unScheduledCritical = 0;
                long scheduledCritical = 0;
                long unknown = 0;
                long warning = 0;
                if (stateStatisticCollection != null
                        && stateStatisticCollection.length > 0) {

                    for (int i = 0; i < stateStatisticCollection.length; i++) {
                        StateStatistics stateStatistics = stateStatisticCollection[i];
                        if (stateStatistics != null) {
                            statisticCollection = stateStatistics
                                    .getStatisticProperties();
                            if (statisticCollection != null
                                    && statisticCollection.length > 0) {
                                for (int j = 0; j < statisticCollection.length; j++) {
                                    if (CommonConstants.UNSCHEDULED_CRITICAL
                                            .equalsIgnoreCase(statisticCollection[j]
                                                    .getName())
                                            && statisticCollection[j]
                                                    .getCount() > 0) {
                                        unScheduledCritical++;
                                    } else if (CommonConstants.SCHEDULED_CRITICAL
                                            .equalsIgnoreCase(statisticCollection[j]
                                                    .getName())
                                            && statisticCollection[j]
                                                    .getCount() > 0) {
                                        scheduledCritical++;
                                    } else if (CommonConstants.WARNING
                                            .equalsIgnoreCase(statisticCollection[j]
                                                    .getName())
                                            && statisticCollection[j]
                                                    .getCount() > 0) {
                                        warning++;
                                    } else if (CommonConstants.UNKNOWN
                                            .equalsIgnoreCase(statisticCollection[j]
                                                    .getName())
                                            && statisticCollection[j]
                                                    .getCount() > 0) {
                                        unknown++;
                                    } else if (CommonConstants.PENDING
                                            .equalsIgnoreCase(statisticCollection[j]
                                                    .getName())
                                            && statisticCollection[j]
                                                    .getCount() > 0) {
                                        pending++;
                                    } else if (CommonConstants.OK
                                            .equalsIgnoreCase(statisticCollection[j]
                                                    .getName())
                                            && statisticCollection[j]
                                                    .getCount() > 0) {
                                        ok++;
                                    }
                                }

                            }
                        }
                    }
                } else {
                    LOGGER
                            .info("stateStatisticCollection is null while getting statistics for entire network, hence no service group is available");

                }

                total = ok + pending + unScheduledCritical + scheduledCritical
                        + unknown + warning;
                serviceStatisticsMap.put(CommonConstants.UNSCHEDULED_CRITICAL
                        .toUpperCase(), unScheduledCritical);
                serviceStatisticsMap.put(CommonConstants.SCHEDULED_CRITICAL
                        .toUpperCase(), scheduledCritical);
                serviceStatisticsMap.put(CommonConstants.WARNING.toUpperCase(),
                        warning);
                serviceStatisticsMap.put(CommonConstants.UNKNOWN.toUpperCase(),
                        unknown);
                serviceStatisticsMap.put(CommonConstants.PENDING.toUpperCase(),
                        pending);
                serviceStatisticsMap.put(CommonConstants.OK.toUpperCase(), ok);
                serviceStatisticsMap
                        .put(CommonConstants.TOTAL_COUNT_KEY, total);

            } else {
                LOGGER
                        .info("WSFoundationCollection is null while getting statistics for entire network ");
                throw new WSDataUnavailableException();
            }
        } catch (WSFoundationException fEx) {
            LOGGER.error(CommonConstants.WSFOUNDATION_EXCEPTION_MESSAGE
                    + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + fEx);
            throw new WSDataUnavailableException();
        } catch (RemoteException rEx) {
            LOGGER.error(CommonConstants.REMOTE_EXCEPTION_MESSAGE
                    + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + rEx);
            throw new WSDataUnavailableException();
        }

        return serviceStatisticsMap;

    }

    /**
     * return the entire network Host statistics
     * 
     * @return map
     * @throws GWPortalException
     * @throws WSDataUnavailableException
     */
    public Map<String, Long> getEntireNetworkHostStatistics()
            throws GWPortalException, WSDataUnavailableException {
        StateStatistics[] stateCollection = null;
        Map<String, Long> hostStatisticsMap = new HashMap<String, Long>();
        WSStatistics statisticsBinding = getStatisticsBinding();
        try {
            // get statistics
            if (statisticsBinding == null) {
                LOGGER
                        .info("statisticsBinding is null in getEntireNetworkHostStatistics method");
                throw new GWPortalException();
            }
            WSFoundationCollection statistics = statisticsBinding
                    .getStatistics(StatisticQueryType.TOTALS_BY_HOSTS,
                            CommonConstants.EMPTY_STRING,
                            CommonConstants.EMPTY_STRING);
            if (statistics == null) {
                LOGGER
                        .info("WSFoundationCollection is null in getEntireNetworkHostStatistics method");
                throw new WSDataUnavailableException();

            }

            stateCollection = statistics.getStateStatisticCollection();
            if (stateCollection == null || stateCollection.length <= 0) {
                LOGGER
                        .info("StateStatistics[] is null or StateStatistics[] length is 0 or less then 0 in getEntireNetworkHostStatistics method");
                throw new WSDataUnavailableException();

            }

            StatisticProperty[] statisticCollection = stateCollection[0]
                    .getStatisticProperties();
            if (statisticCollection == null) {
                LOGGER
                        .info("statisticCollection is null  in getEntireNetworkHostStatistics method");
                throw new WSDataUnavailableException();
            }
            for (int i = 0; i < statisticCollection.length; i++) {
                // get the status and count of hostGroup and put in
                // to
                // map
                hostStatisticsMap.put(statisticCollection[i].getName()
                        .toUpperCase(), statisticCollection[i].getCount());

            }
            // put the total count of hostGroup in map
            hostStatisticsMap.put(CommonConstants.TOTAL_COUNT_KEY,
                    stateCollection[0].getTotalHosts());

        } catch (WSFoundationException fEx) {
            LOGGER.error(CommonConstants.WSFOUNDATION_EXCEPTION_MESSAGE
                    + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + fEx);
            throw new WSDataUnavailableException();
        } catch (RemoteException rEx) {
            LOGGER.error(CommonConstants.REMOTE_EXCEPTION_MESSAGE
                    + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + rEx);
            throw new WSDataUnavailableException();
        }

        return hostStatisticsMap;

    }

    /**
     * return the entire network Host statistics
     * 
     * @return map
     * @throws GWPortalException
     * @throws WSDataUnavailableException
     */
    public Map<String, Long> getEntireNetworkServiceStatistics()
            throws GWPortalException, WSDataUnavailableException {
        StateStatistics[] stateCollection = null;
        Map<String, Long> serviceStatisticsMap = new HashMap<String, Long>();
        WSStatistics statisticsBinding = getStatisticsBinding();
        try {
            if (statisticsBinding == null) {
                LOGGER
                        .info("statisticsBinding is null in getEntireNetworkServiceStatistics method");
                throw new GWPortalException();
            }
            // get statistics
            WSFoundationCollection statistics = statisticsBinding
                    .getStatistics(StatisticQueryType.TOTALS_BY_SERVICES,
                            CommonConstants.EMPTY_STRING,
                            CommonConstants.EMPTY_STRING);
            if (statistics == null) {
                LOGGER
                        .info("WSFoundationCollection is null in getEntireNetworkServiceStatistics method");
                throw new WSDataUnavailableException();
            }
            stateCollection = statistics.getStateStatisticCollection();
            if (stateCollection == null) {
                LOGGER
                        .info("StateStatistics[] is null in getEntireNetworkServiceStatistics method");
                throw new WSDataUnavailableException();
            }
            StatisticProperty[] statisticCollection = stateCollection[0]
                    .getStatisticProperties();
            if (statisticCollection == null) {
                LOGGER
                        .info("StatisticProperty[] is null in getEntireNetworkServiceStatistics method");
                throw new WSDataUnavailableException();
            }
            for (int i = 0; i < statisticCollection.length; i++) {
                // get the status and count of hostGroup and put in
                // to
                // map
                serviceStatisticsMap.put(statisticCollection[i].getName()
                        .toUpperCase(), statisticCollection[i].getCount());

            }
            // put the total count of hostGroup in map
            serviceStatisticsMap.put(CommonConstants.TOTAL_COUNT_KEY,
                    stateCollection[0].getTotalServices());

        } catch (WSFoundationException fEx) {
            LOGGER.error(CommonConstants.WSFOUNDATION_EXCEPTION_MESSAGE
                    + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + fEx);
            throw new WSDataUnavailableException();
        } catch (RemoteException rEx) {
            LOGGER.error(CommonConstants.REMOTE_EXCEPTION_MESSAGE
                    + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + rEx);
            throw new WSDataUnavailableException();
        }

        return serviceStatisticsMap;
    }

    /**
     * (non-Javadoc)
     * 
     * @throws WSDataUnavailableException
     * 
     * @see com.groundworkopensource.portal.common.ws.IWSStatisticsFacade#
     *      getNagiosStatisticsForNetwork()
     */

    public NagiosStatisticProperty[] getNagiosStatisticsForNetwork()
            throws GWPortalException, WSDataUnavailableException {
        NagiosStatisticProperty[] propertyArr = null;
        WSStatistics statisticsBinding = getStatisticsBinding();
        // by default return null propertyArr
        if (statisticsBinding == null) {
            String errorMessage = ResourceUtils
                    .getLocalizedMessage("com_groundwork_portal_statusviewer_error_nullBinding");
            throw new GWPortalException(errorMessage);
        }
        try {
            // get nagios statistics for entire system
            final WSFoundationCollection nagiosStatistics = statisticsBinding
                    .getNagiosStatistics(NagiosStatisticQueryType.SYSTEM,
                            CommonConstants.EMPTY_STRING);
            if (nagiosStatistics == null) {
                LOGGER
                        .info("Error occured when retrieving nagios statistics for NETWORK");
                throw new WSDataUnavailableException();
            }
            propertyArr = nagiosStatistics.getNagiosStatisticCollection();
            return propertyArr;
        } catch (final WSFoundationException fEx) {
            LOGGER
                    .error(
                            "WSFoundationException while getting getNagiosStatistics() for the entire network.",
                            fEx);
        } catch (final RemoteException rEx) {
            LOGGER.error(CommonConstants.REMOTE_EXCEPTION_MESSAGE
                    + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + rEx);
        }
        throw new WSDataUnavailableException();
    }

    /**
     * (non-Javadoc)
     * 
     * @throws GWPortalException
     * @throws WSDataUnavailableException
     * 
     * @see com.groundworkopensource.portal.common.ws.IWSStatisticsFacade#getNagiosStatisticsForServiceGroup(java.lang.String)
     */
    public NagiosStatisticProperty[] getNagiosStatisticsForServiceGroup(
            String queryValue) throws GWPortalException,
            WSDataUnavailableException {
        NagiosStatisticProperty[] propertyArr = null;
        WSStatistics statisticsBinding = getStatisticsBinding();
        // by default return null propertyArr
        if (statisticsBinding == null) {
            String errorMessage = ResourceUtils
                    .getLocalizedMessage("com_groundwork_portal_statusviewer_error_nullBinding");
            throw new GWPortalException(errorMessage);
        }
        try {
            // get nagios statistics for service group.
            WSFoundationCollection nagiosStatistics = statisticsBinding
                    .getNagiosStatistics(
                            NagiosStatisticQueryType.SERVICEGROUPNAME,
                            queryValue);
            if (nagiosStatistics == null) {
                LOGGER
                        .info("Error occured when retrieving nagios statistics for SERVICE GROUP "
                                + queryValue);
                throw new WSDataUnavailableException();
            }
            propertyArr = nagiosStatistics.getNagiosStatisticCollection();
            return propertyArr;
        } catch (WSFoundationException fEx) {
            LOGGER
                    .error("WSFoundationException while getting getNagiosStatistics for service group with name ()."
                            + queryValue
                            + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + fEx);
        } catch (RemoteException rEx) {
            LOGGER.error(CommonConstants.REMOTE_EXCEPTION_MESSAGE
                    + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + rEx);
        }
        throw new WSDataUnavailableException();
    }

    /**
     * This method retrieves nagios statistics for Host Group
     * 
     * @param queryValue
     *            - Host Group Name
     * @return propertyArr
     * @throws GWPortalException
     * @throws WSDataUnavailableException
     * 
     */
    public NagiosStatisticProperty[] getNagiosStatisticsForHostGroup(
            String queryValue) throws GWPortalException,
            WSDataUnavailableException {
        NagiosStatisticProperty[] propertyArr = null;
        WSStatistics statisticsBinding = getStatisticsBinding();
        // by default return null propertyArr
        if (statisticsBinding == null) {
            String errorMessage = ResourceUtils
                    .getLocalizedMessage("com_groundwork_portal_statusviewer_error_nullBinding");
            throw new GWPortalException(errorMessage);
        }
        try {
            // get nagios statistics for host group specified with hostGroupID
            // WSFoundationCollection nagiosStatistics = statisticsBinding
            // .getNagiosStatistics(NagiosStatisticQueryType.HOSTGROUPID,
            // queryValue);

            // get nagios statistics for host group specified with hostGroupID
            WSFoundationCollection nagiosStatistics = statisticsBinding
                    .getNagiosStatistics(
                            NagiosStatisticQueryType.HOSTGROUPNAME, queryValue);

            if (nagiosStatistics == null) {
                LOGGER
                        .info("Error occured when retrieving nagios statistics for HOST GROUP "
                                + queryValue);
                throw new WSDataUnavailableException();
            }
            propertyArr = nagiosStatistics.getNagiosStatisticCollection();
            return propertyArr;
        } catch (WSFoundationException fEx) {
            LOGGER
                    .error("WSFoundationException while getting getNagiosStatistics for Host group with ID ()."
                            + queryValue
                            + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + fEx);
        } catch (RemoteException rEx) {
            LOGGER.error(CommonConstants.REMOTE_EXCEPTION_MESSAGE
                    + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + rEx);
        }
        throw new WSDataUnavailableException();
    }

    /**
     * (non-Javadoc)
     * 
     * @see com.groundworkopensource.portal.common.ws.IWSStatisticsFacade#getHostAvailabilityForHostgroup(java.lang.String)
     */
    public double getHostAvailabilityForHostgroup(String hostGroupName) {
        double hostAvailability = 0;
        WSStatistics statisticsBinding = getStatisticsBinding();
        // by default return host availability as '0'.
        if (null == statisticsBinding) {
            return hostAvailability;
        }
        // getHostAvailabilityForHostgroup by using statistics web service call.
        try {
            hostAvailability = statisticsBinding
                    .getHostAvailabilityForHostgroup(hostGroupName);
        } catch (WSFoundationException fEx) {
            LOGGER
                    .error("WSFoundationException while getting Host Availability for Host group with name ["
                            + hostGroupName
                            + "]"
                            + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + fEx);
        } catch (RemoteException rEx) {
            LOGGER.error(CommonConstants.REMOTE_EXCEPTION_MESSAGE
                    + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + rEx);
        }
        return hostAvailability;
    }

    /**
     * (non-Javadoc)
     * 
     * @see com.groundworkopensource.portal.common.ws.IWSStatisticsFacade#getServiceAvailabilityForHostgroup(java.lang.String)
     */
    public double getServiceAvailabilityForHostgroup(String hostGroupName) {
        double serviceAvailability = 0;
        // by default return service availability as '0'.
        WSStatistics statisticsBinding = getStatisticsBinding();
        if (null == statisticsBinding) {
            return serviceAvailability;
        }
        // getServiceAvailabilityForHostgroup by using statistics web service
        // call.
        try {
            serviceAvailability = statisticsBinding
                    .getServiceAvailabilityForHostgroup(hostGroupName);
        } catch (WSFoundationException fEx) {
            LOGGER
                    .error("WSFoundationException while getting Service Availability for Host group with name ["
                            + hostGroupName
                            + "]"
                            + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + fEx);
        } catch (RemoteException rEx) {
            LOGGER.error(CommonConstants.REMOTE_EXCEPTION_MESSAGE
                    + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + rEx);
        }
        return serviceAvailability;
    }

    /**
     * Returns service availability for the Service Group. In case of any
     * exception (or if not able to contact server) throw exception.
     * 
     * @param serviceGroupName
     * @return double
     * @throws WSDataUnavailableException
     * @throws GWPortalException
     */
    public double getServiceAvailabilityForServiceGroup(String serviceGroupName)
            throws WSDataUnavailableException, GWPortalException {
        double serviceAvailability = 0;
        WSStatistics statisticsBinding = getStatisticsBinding();
        // by default return service availability as '0'.
        if (statisticsBinding == null) {
            String errorMessage = ResourceUtils
                    .getLocalizedMessage("com_groundwork_portal_statusviewer_error_nullBinding");
            throw new GWPortalException(errorMessage);
        }
        // getServiceAvailabilityForServicegroup by using statistics web service
        // call.
        try {
            serviceAvailability = statisticsBinding
                    .getServiceAvailabilityForServiceGroup(serviceGroupName);
            return serviceAvailability;
        } catch (WSFoundationException fEx) {
            LOGGER
                    .error("WSFoundationException while getting Service Availability for Service group with name ["
                            + serviceGroupName
                            + "]"
                            + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + fEx);
        } catch (RemoteException rEx) {
            LOGGER.error(CommonConstants.REMOTE_EXCEPTION_MESSAGE
                    + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + rEx);
        }
        throw new WSDataUnavailableException();
    }

    /**
     * (non-Javadoc)
     * 
     * @throws WSDataUnavailableException
     * 
     * @see com.groundworkopensource.portal.common.ws.IWSStatisticsFacade#
     *      getNagiosStatistics (org. groundwork . foundation .ws. model .impl.
     *      NagiosStatisticQueryType , java. lang. String )
     */
    public NagiosStatisticProperty[] getNagiosStatistics(
            NagiosStatisticQueryType queryType, String queryValue)
            throws GWPortalException, WSDataUnavailableException {
        NagiosStatisticProperty[] propertyArr = new NagiosStatisticProperty[] {};
        WSStatistics statisticsBinding = getStatisticsBinding();
        if (statisticsBinding == null) {
            String errorMessage = ResourceUtils
                    .getLocalizedMessage("com_groundwork_portal_statusviewer_error_nullBinding");
            throw new GWPortalException(errorMessage);
        }
        try {
            WSFoundationCollection nagiosStatistics = statisticsBinding
                    .getNagiosStatistics(queryType, queryValue);
            if (nagiosStatistics == null) {
                LOGGER.info("Error occured when retrieving nagios statistics"
                        + queryValue);
                throw new WSDataUnavailableException();
            }
            propertyArr = nagiosStatistics.getNagiosStatisticCollection();
            return propertyArr;
        } catch (WSFoundationException fEx) {
            LOGGER
                    .error("WSFoundationException while getting getNagiosStatistics for Host group with ID ()."
                            + queryValue
                            + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + fEx);
        } catch (RemoteException rEx) {
            LOGGER.error(CommonConstants.REMOTE_EXCEPTION_MESSAGE
                    + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + rEx);
        }
        throw new WSDataUnavailableException();
    }

    /**
     * (non-Javadoc)
     * 
     * @param statisticQueryType
     * @param filter
     * @param name
     * @param applicationType
     * @return MAP
     * @throws WSDataUnavailableException
     * 
     * @see com.groundworkopensource.portal.common.ws.IWSStatisticsFacade#getGroupStatisticsForHostGroup(org.groundwork.foundation.ws.model.impl.StatisticQueryType,
     *      org.groundwork.foundation.ws.model.impl.Filter, java.lang.String,
     *      java.lang.String)
     */
    public StateStatistics[] getFilteredHostGroupName(
            StatisticQueryType statisticQueryType, Filter filter, String name,
            String applicationType) throws WSDataUnavailableException {
        StateStatistics[] stateStatCol = null;

        WSStatistics statisticsBinding = getStatisticsBinding();
        if (null == statisticsBinding) {
            LOGGER
                    .info("statisticsBinding is null in getFilteredHostGroupName method ");
            return stateStatCol;
        }
        try {

            WSFoundationCollection wsFoundationCollection = statisticsBinding
                    .getGroupStatistics(statisticQueryType, filter, name,
                            applicationType);
            if (wsFoundationCollection == null) {
                LOGGER
                        .info("WSFoundationCollection is null in getFilteredHostGroupName ");
                throw new WSDataUnavailableException();

            }

            stateStatCol = wsFoundationCollection.getStateStatisticCollection();

        } catch (WSFoundationException fEx) {
            LOGGER
                    .error(
                            "WSFoundationException while getting getGroupStatistics for Host group with ID ().",
                            fEx);
            throw new WSDataUnavailableException();

        } catch (RemoteException rEx) {
            LOGGER.error(CommonConstants.REMOTE_EXCEPTION_MESSAGE
                    + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + rEx);
            throw new WSDataUnavailableException();
        }
        return stateStatCol;

    }

    /**
     * (non-Javadoc)
     * 
     * @param statisticQueryType
     * @param filter
     * @param name
     * @param applicationType
     * @return MAP
     * @throws WSDataUnavailableException
     * 
     * @see com.groundworkopensource.portal.common.ws.IWSStatisticsFacade#getGroupStatisticsForHostGroup(org.groundwork.foundation.ws.model.impl.StatisticQueryType,
     *      org.groundwork.foundation.ws.model.impl.Filter, java.lang.String,
     *      java.lang.String)
     */
    public Map<String, Long> getGroupStatisticsForHostGroup(
            StatisticQueryType statisticQueryType, Filter filter, String name,
            String applicationType) throws WSDataUnavailableException {
        StatisticProperty[] propertyArr = null;
        Map<String, Long> statisticsMap = new HashMap<String, Long>();
        long total = 0;
        WSStatistics statisticsBinding = getStatisticsBinding();
        if (null == statisticsBinding) {
            LOGGER
                    .info("statisticsBinding is null in getGroupStatisticsForHostGroup method ");
            return statisticsMap;
        }
        try {

            WSFoundationCollection wsFoundationCollection = statisticsBinding
                    .getGroupStatistics(statisticQueryType, filter, name,
                            applicationType);
            if (wsFoundationCollection == null) {
                LOGGER
                        .info("WSFoundationCollection is null in getGroupStatisticsForHostGroup ");
                throw new WSDataUnavailableException();

            }

            StateStatistics[] stateStatCol = wsFoundationCollection
                    .getStateStatisticCollection();

            if (stateStatCol != null && stateStatCol.length > 0) {
                // statistics for entire network
                if (name == null) {
                    long up = 0;
                    long pending = 0;
                    long unScheduledDown = 0;
                    long scheduledDown = 0;
                    long unreachable = 0;

                    for (int i = 0; i < stateStatCol.length; i++) {
                        propertyArr = stateStatCol[i].getStatisticProperties();
                        if (propertyArr != null && propertyArr.length > 0) {
                            for (int j = 0; j < propertyArr.length; j++) {
                                if (CommonConstants.UN_SCHEDULED_DOWN
                                        .equalsIgnoreCase(propertyArr[j]
                                                .getName())
                                        && propertyArr[j].getCount() > 0) {
                                    unScheduledDown ++;
                                } else if (CommonConstants.SCHEDULED_DOWN
                                        .equalsIgnoreCase(propertyArr[j]
                                                .getName())
                                        && propertyArr[j].getCount() > 0) {
                                   scheduledDown ++;
                                } else if (CommonConstants.UP
                                        .equalsIgnoreCase(propertyArr[j]
                                                .getName())
                                        && propertyArr[j].getCount() > 0) {
                                   up ++ ;
                                } else if (CommonConstants.PENDING
                                        .equalsIgnoreCase(propertyArr[j]
                                                .getName())
                                        && propertyArr[j].getCount() > 0) {
                                    pending ++;
                                } else if (CommonConstants.UNREACHABLE
                                        .equalsIgnoreCase(propertyArr[j]
                                                .getName())
                                        && propertyArr[j].getCount() > 0) {
                                    unreachable++;
                                }
                            }

                        }
                    }
                    total = unScheduledDown + scheduledDown + up + pending
                            + unreachable;
                    statisticsMap.put(CommonConstants.UN_SCHEDULED_DOWN
                            .toUpperCase(), unScheduledDown);
                    statisticsMap.put(CommonConstants.SCHEDULED_DOWN
                            .toUpperCase(), scheduledDown);
                    statisticsMap.put(CommonConstants.UP.toUpperCase(), up);
                    statisticsMap.put(CommonConstants.PENDING.toUpperCase(),
                            pending);
                    statisticsMap.put(
                            CommonConstants.UNREACHABLE.toUpperCase(),
                            unreachable);
                    statisticsMap.put(CommonConstants.TOTAL_COUNT_KEY, total);

                } else {
                    // statistics for host Group
                    propertyArr = stateStatCol[0].getStatisticProperties();
                    if (propertyArr != null && propertyArr.length > 0) {

                        for (int i = 0; i < propertyArr.length; i++) {
                            statisticsMap.put(propertyArr[i].getName()
                                    .toUpperCase(), propertyArr[i].getCount());
                            // calculating total count
                            total = total + propertyArr[i].getCount();
                        }
                        statisticsMap.put(CommonConstants.TOTAL_COUNT_KEY,
                                total);

                    } else {
                        LOGGER
                                .info("Statistic Property is null or empty during access getGroupStatistics");
                        throw new WSDataUnavailableException();
                    }
                }
            } else {
                for (int i = 0; i < hostStatusArray.length; i++) {
                    statisticsMap.put(hostStatusArray[i].toUpperCase(),
                            (long) 0);
                }
                statisticsMap.put(CommonConstants.TOTAL_COUNT_KEY, (long) 0);
            }

        } catch (WSFoundationException fEx) {
            LOGGER
                    .error(
                            "WSFoundationException while getting getGroupStatistics for Host group with ID ().",
                            fEx);
            throw new WSDataUnavailableException();

        } catch (RemoteException rEx) {
            LOGGER.error(CommonConstants.REMOTE_EXCEPTION_MESSAGE
                    + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + rEx);
            throw new WSDataUnavailableException();
        }
        return statisticsMap;

    }

    /**
     * (non-Javadoc)
     * 
     * @param statisticQueryType
     * @param filter
     * @param name
     * @param applicationType
     * @return MAP
     * @throws GWPortalException
     * @throws WSDataUnavailableException
     * 
     * @see com.groundworkopensource.portal.common.ws.IWSStatisticsFacade#getGroupStatisticsForHostGroup(org.groundwork.foundation.ws.model.impl.StatisticQueryType,
     *      org.groundwork.foundation.ws.model.impl.Filter, java.lang.String,
     *      java.lang.String)
     */
    public Map<String, Long> getGroupStatisticsForServicegGroup(
            StatisticQueryType statisticQueryType, Filter filter, String name,
            String applicationType) throws GWPortalException,
            WSDataUnavailableException {
        StatisticProperty[] propertyArr = null;
        Map<String, Long> statisticsMap = new HashMap<String, Long>();
        long total = 0;
        WSStatistics statisticsBinding = getStatisticsBinding();
        if (null == statisticsBinding) {
            LOGGER
                    .info("statisticsBinding is null in getGroupStatisticsForServicegGroup method ");
            throw new GWPortalException();

        }
        try {
            WSFoundationCollection wsFoundationCollection = statisticsBinding
                    .getGroupStatistics(statisticQueryType, filter, name,
                            applicationType);
            if (wsFoundationCollection == null) {
                LOGGER
                        .info("WSFoundationCollection is null in getGroupStatisticsForServicegGroup method ");
                throw new WSDataUnavailableException();
            }
            StateStatistics[] stateStatCol = wsFoundationCollection
                    .getStateStatisticCollection();
            if ((stateStatCol == null) || (stateStatCol.length == 0)) {
                LOGGER
                        .info("Found null stateStatCol when calling getGroupStatisticsForServicegGroup() : "
                                + name);
                throw new WSDataUnavailableException();
            }
            // statistics for service group under entire network
            if (name == null) {
                long ok = 0;
                long pending = 0;
                long unScheduledCritical = 0;
                long scheduledCritical = 0;
                long unknown = 0;
                long warning = 0;

                for (int i = 0; i < stateStatCol.length; i++) {
                    propertyArr = stateStatCol[i].getStatisticProperties();
                    if (propertyArr != null && propertyArr.length > 0) {
                        for (int j = 0; j < propertyArr.length; j++) {
                            if (CommonConstants.UNSCHEDULED_CRITICAL
                                    .equalsIgnoreCase(propertyArr[j].getName())
                                    && propertyArr[j].getCount() > 0) {
                                unScheduledCritical++;
                            } else if (CommonConstants.SCHEDULED_CRITICAL
                                    .equalsIgnoreCase(propertyArr[j].getName())
                                    && propertyArr[j].getCount() > 0) {
                                scheduledCritical++;
                            } else if (CommonConstants.WARNING
                                    .equalsIgnoreCase(propertyArr[j].getName())
                                    && propertyArr[j].getCount() > 0) {
                                warning++;
                            } else if (CommonConstants.UNKNOWN
                                    .equalsIgnoreCase(propertyArr[j].getName())
                                    && propertyArr[j].getCount() > 0) {
                                unknown++;
                            } else if (CommonConstants.PENDING
                                    .equalsIgnoreCase(propertyArr[j].getName())
                                    && propertyArr[j].getCount() > 0) {
                                pending++;
                            } else if (CommonConstants.OK
                                    .equalsIgnoreCase(propertyArr[j].getName())
                                    && propertyArr[j].getCount() > 0) {
                                ok++;
                            }
                        }

                    }
                }
                total = ok + pending + unScheduledCritical + scheduledCritical
                        + unknown + warning;
                statisticsMap.put(CommonConstants.UNSCHEDULED_CRITICAL
                        .toUpperCase(), unScheduledCritical);
                statisticsMap.put(CommonConstants.SCHEDULED_CRITICAL
                        .toUpperCase(), scheduledCritical);
                statisticsMap.put(CommonConstants.WARNING.toUpperCase(),
                        warning);
                statisticsMap.put(CommonConstants.UNKNOWN.toUpperCase(),
                        unknown);
                statisticsMap.put(CommonConstants.PENDING.toUpperCase(),
                        pending);
                statisticsMap.put(CommonConstants.OK.toUpperCase(), ok);
                statisticsMap.put(CommonConstants.TOTAL_COUNT_KEY, total);

            } else {
                propertyArr = stateStatCol[0].getStatisticProperties();
                if (propertyArr != null) {
                    if (propertyArr.length > 0) {
                        for (int i = 0; i < propertyArr.length; i++) {
                            statisticsMap.put(propertyArr[i].getName()
                                    .toUpperCase(), propertyArr[i].getCount());
                            // calculating total count
                            total = total + propertyArr[i].getCount();
                        }
                        statisticsMap.put(CommonConstants.TOTAL_COUNT_KEY,
                                total);

                    } else {
                        for (int i = 0; i < serviceStatusArray.length; i++) {
                            statisticsMap.put(serviceStatusArray[i]
                                    .toUpperCase(), (long) 0);
                        }
                        statisticsMap.put(CommonConstants.TOTAL_COUNT_KEY,
                                (long) 0);
                    }

                } else {
                    LOGGER
                            .info("Statistic Property is null during access getGroupStatistics");
                    throw new WSDataUnavailableException();
                }
            }

        } catch (WSFoundationException fEx) {
            LOGGER
                    .error("WSFoundationException while calling  getGroupStatisticsForServicegGroup() for service group: "
                            + name
                            + CommonConstants.ACTUAL_EXCEPTION_MESSAGE
                            + fEx);
            throw new WSDataUnavailableException();
        } catch (RemoteException rEx) {
            LOGGER.error(CommonConstants.REMOTE_EXCEPTION_MESSAGE
                    + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + rEx);
            throw new WSDataUnavailableException();
        }
        return statisticsMap;

    }

    /**
     * return the Filtered Host statistics
     * 
     * @param hostNames
     * @return map
     * @throws GWPortalException
     * @throws WSDataUnavailableException
     */
    public Map<String, Long> getFilteredStatisticsForHost(String hostNames)
            throws GWPortalException, WSDataUnavailableException {
        StateStatistics[] stateCollection = null;
        Map<String, Long> filteredHostStatisticsMap = new HashMap<String, Long>();
        WSStatistics statisticsBinding = getStatisticsBinding();
        try {

            if (statisticsBinding == null) {
                LOGGER
                        .info("statisticsBinding is null in getFilteredStatisticsForHost metohd");
                throw new GWPortalException();
            }
            // get statistics
            WSFoundationCollection statistics = statisticsBinding
                    .getStatistics(StatisticQueryType.HOST_LIST, hostNames,
                            CommonConstants.NAGIOS);
            if (statistics == null) {
                LOGGER
                        .info("statistics is null in getFilteredStatisticsForHost metohd");
                throw new WSDataUnavailableException();
            }
            stateCollection = statistics.getStateStatisticCollection();
            if (stateCollection == null || stateCollection.length <= 0) {
                LOGGER
                        .info("StateStatistics[] is null or StateStatistics[] length is less then or equal 0 in getFilteredStatisticsForHost method");
                throw new WSDataUnavailableException();

            }
            StatisticProperty[] statisticCollection = stateCollection[0]
                    .getStatisticProperties();
            if (statisticCollection == null) {
                LOGGER
                        .info("StatisticProperty[] is null or StatisticProperty[] length is less then or equal 0 in getFilteredStatisticsForHost method");
                throw new WSDataUnavailableException();
            }
            // Variable for filtered total count
            long total = 0;
            for (int i = 0; i < statisticCollection.length; i++) {
                // get the status and count of hostGroup and put in
                // to
                // map
                filteredHostStatisticsMap.put(statisticCollection[i].getName()
                        .toUpperCase(), statisticCollection[i].getCount());
                total = total + statisticCollection[i].getCount();

            }
            // put the total count of host in map
            filteredHostStatisticsMap.put(CommonConstants.TOTAL_COUNT_KEY,
                    total);

        } catch (WSFoundationException fEx) {
            LOGGER.error(CommonConstants.WSFOUNDATION_EXCEPTION_MESSAGE
                    + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + fEx);
            throw new WSDataUnavailableException();
        } catch (RemoteException rEx) {
            LOGGER.error(CommonConstants.REMOTE_EXCEPTION_MESSAGE
                    + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + rEx);
            throw new WSDataUnavailableException();
        }
        return filteredHostStatisticsMap;

    }

    /**
     * (non-Javadoc)
     * 
     * @param hostGroupId
     * @return map
     * @throws GWPortalException
     * @throws WSDataUnavailableException
     * 
     * @see com.groundworkopensource.portal.common.ws.IWSStatisticsFacade#getServiceStatisticsByHostGroupId(java.lang.String)
     */
    public Map<String, Long> getServiceStatisticsByHostGroupId(
            final String hostGroupId) throws GWPortalException,
            WSDataUnavailableException {
        return getServiceStatisticsByHostGroup(
                StatisticQueryType.SERVICES_BY_HOSTGROUPID, hostGroupId);
    }

    /**
     * (non-Javadoc)
     * 
     * @see com.groundworkopensource.portal.common.ws.IWSStatisticsFacade#getServiceStatisticsByHostGroupName(java.lang.String)
     */
    public Map<String, Long> getServiceStatisticsByHostGroupName(
            final String hostGroupName) throws GWPortalException,
            WSDataUnavailableException {
        return getServiceStatisticsByHostGroup(
                StatisticQueryType.SERVICES_BY_HOSTGROUPNAME, hostGroupName);
    }

    /**
     * @param statisticQueryType
     * @param hostGroupId
     * @return Map of service statistics
     * @throws GWPortalException
     * @throws WSDataUnavailableException
     */
    private Map<String, Long> getServiceStatisticsByHostGroup(
            StatisticQueryType statisticQueryType, final String hostGroup)
            throws GWPortalException, WSDataUnavailableException {
        StatisticProperty[] propertyArr = null;
        StateStatistics[] stateStatisticCollection = null;
        Map<String, Long> serviceStatisticsMap = new HashMap<String, Long>();
        WSStatistics statisticsBinding = getStatisticsBinding();
        if (null == statisticsBinding) {
            LOGGER
                    .info("Statistics binding is null while getting service statistics by host group ["
                            + hostGroup + "]");
            throw new GWPortalException();
        }

        try {
            // get statistics for Host Group
            WSFoundationCollection statistics = statisticsBinding
                    .getStatistics(statisticQueryType, hostGroup,
                            CommonConstants.EMPTY_STRING);
            if (statistics == null) {
                LOGGER
                        .info("WSFoundationCollection is null while getting service statistics by host group ["
                                + hostGroup + "]");
                throw new WSDataUnavailableException();
            }
            stateStatisticCollection = statistics.getStateStatisticCollection();
            if (stateStatisticCollection == null
                    || stateStatisticCollection.length <= 0) {
                LOGGER
                        .info("StateStatistics array is null or Empty while getting service statistics by host group ["
                                + hostGroup + "]");
                throw new WSDataUnavailableException();
            }
            // we have requested the statistics for one hostgroup, so
            // only item
            // is present in the array - fetch 0th element
            long total = 0;
            propertyArr = stateStatisticCollection[0].getStatisticProperties();
            if (propertyArr == null) {
                LOGGER
                        .info("StatisticProperty[] is null while getting service statistics by host group ["
                                + hostGroup + "]");
                throw new WSDataUnavailableException();
            }
            for (int i = 0; i < propertyArr.length; i++) {
                // get the status and count of hostGroup and put in
                // to map
                serviceStatisticsMap.put(
                        propertyArr[i].getName().toUpperCase(), propertyArr[i]
                                .getCount());
                if (!propertyArr[i].getName().equalsIgnoreCase(
                        CommonConstants.CRITICAL)) {
                    total = total + propertyArr[i].getCount();
                }
            }
            // put the total count of hostGroup in map
            serviceStatisticsMap.put(CommonConstants.TOTAL_COUNT_KEY, total);

        } catch (WSFoundationException fEx) {
            LOGGER.error(CommonConstants.WSFOUNDATION_EXCEPTION_MESSAGE
                    + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + fEx);
            throw new WSDataUnavailableException();
        } catch (RemoteException rEx) {
            LOGGER.error(CommonConstants.REMOTE_EXCEPTION_MESSAGE
                    + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + rEx);
            throw new WSDataUnavailableException();
        }
        return serviceStatisticsMap;
    }

    /**
     * (non-Javadoc)
     * 
     * @param hostName
     * @return map
     * @throws GWPortalException
     * @throws WSDataUnavailableException
     * 
     * @see com.groundworkopensource.portal.common.ws.IWSStatisticsFacade#getServiceStatisticsForHostGroup(java.lang.String)
     */
    public Map<String, Long> getServiceStatisticsByHostName(
            final String hostName) throws GWPortalException,
            WSDataUnavailableException {
        StatisticProperty[] propertyArr = null;
        StateStatistics[] stateStatisticCollection = null;
        Map<String, Long> serviceStatisticsMap = new HashMap<String, Long>();
        WSStatistics statisticsBinding = getStatisticsBinding();
        if (null == statisticsBinding) {
            LOGGER
                    .info("Statistics binding is null while getting service statistics by host name");
            throw new GWPortalException();
        }

        try {
            // get statistics for Host Group
            WSFoundationCollection statistics = statisticsBinding
                    .getStatistics(
                            StatisticQueryType.TOTALS_FOR_SERVICES_BY_HOSTNAME,
                            hostName, CommonConstants.EMPTY_STRING);
            if (statistics != null) {
                stateStatisticCollection = statistics
                        .getStateStatisticCollection();
                if (stateStatisticCollection != null
                        && stateStatisticCollection.length > 0) {
                    // we have requested the statistics for one host, so
                    // only item
                    // is present in the array - fetch 0th element
                    long total = 0;
                    propertyArr = stateStatisticCollection[0]
                            .getStatisticProperties();
                    if (propertyArr == null) {
                        LOGGER
                                .info(" StatisticProperty[] array is null  while getting service statistics by host name");
                        throw new WSDataUnavailableException();
                    }
                    for (int i = 0; i < propertyArr.length; i++) {
                        // get the status and count of host and put in
                        // to
                        // map
                        serviceStatisticsMap.put(propertyArr[i].getName()
                                .toUpperCase(), propertyArr[i].getCount());
                        if (!CommonConstants.CRITICAL
                                .equalsIgnoreCase(propertyArr[i].getName())) {
                            total = total + propertyArr[i].getCount();
                        }
                    }
                    // put the total count of host in map
                    serviceStatisticsMap.put(CommonConstants.TOTAL_COUNT_KEY,
                            total);

                } else {
                    LOGGER
                            .info("StateStatistics array is null or Empty while getting service statistics by host name");
                    throw new WSDataUnavailableException();

                }
            } else {
                LOGGER
                        .info("WSFoundationCollection is null while getting service statistics by host name");
                throw new WSDataUnavailableException();
            }

        } catch (WSFoundationException fEx) {
            LOGGER.error(CommonConstants.WSFOUNDATION_EXCEPTION_MESSAGE
                    + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + fEx);
            throw new WSDataUnavailableException();
        } catch (RemoteException rEx) {
            LOGGER.error(CommonConstants.REMOTE_EXCEPTION_MESSAGE
                    + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + rEx);
            throw new WSDataUnavailableException();
        }
        return serviceStatisticsMap;
    }

    /**
     * (non-Javadoc)
     * 
     * @param serviceGroupName
     * @return map
     * @throws GWPortalException
     * @throws WSDataUnavailableException
     * 
     * @see com.groundworkopensource.portal.common.ws.IWSStatisticsFacade#getServiceStatisticsForHostGroup(java.lang.String)
     */
    public Map<String, Long> getServiceStatisticsByServiceGroupName(
            final String serviceGroupName) throws GWPortalException,
            WSDataUnavailableException {
        StatisticProperty[] propertyArr = null;
        StateStatistics[] stateStatisticCollection = null;
        Map<String, Long> serviceStatisticsMap = new HashMap<String, Long>();
        WSStatistics statisticsBinding = getStatisticsBinding();
        if (null == statisticsBinding) {
            LOGGER
                    .info("Statistics binding is null while getting service statistics by service group name");
            throw new GWPortalException();
        }

        try {
            // get statistics for Service group
            WSFoundationCollection statistics = statisticsBinding
                    .getStatistics(
                            StatisticQueryType.SERVICEGROUP_STATS_BY_SERVICEGROUPNAME,
                            serviceGroupName, CommonConstants.EMPTY_STRING);
            if (statistics == null) {
                LOGGER
                        .info("Found null WSFoundationCollection array when calling  getServiceStatisticsByServiceGroupName() for serviceGroup : "
                                + serviceGroupName);
                throw new WSDataUnavailableException();
            }
            stateStatisticCollection = statistics.getStateStatisticCollection();
            if ((stateStatisticCollection == null)
                    || (stateStatisticCollection.length == 0) || stateStatisticCollection[0] == null) {
                LOGGER
                        .info("StateStatistics array is null or Empty while getting service statistics by service group name :"
                                + serviceGroupName);
                throw new WSDataUnavailableException();
            }

            // we have requested the statistics for one Service group,
            // so only item is present in the array - fetch 0th element
            long total = 0;
            propertyArr = stateStatisticCollection[0].getStatisticProperties();
            if (propertyArr == null) {
                LOGGER
                        .info(" StatisticPropertyarray is null or Empty while getting service statistics by service group name");
                throw new WSDataUnavailableException();
            }
            for (int i = 0; i < propertyArr.length; i++) {
                // get the status and count of Service group and put
                // into map
                serviceStatisticsMap.put(
                        propertyArr[i].getName().toUpperCase(), propertyArr[i]
                                .getCount());
                if (!propertyArr[i].getName().equalsIgnoreCase(
                        CommonConstants.CRITICAL)) {
                    total = total + propertyArr[i].getCount();
                }
            }
            // put the total count of Service group in map
            serviceStatisticsMap.put(CommonConstants.TOTAL_COUNT_KEY, total);

        } catch (WSFoundationException fEx) {
            LOGGER.error(CommonConstants.WSFOUNDATION_EXCEPTION_MESSAGE
                    + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + fEx);
            throw new WSDataUnavailableException();
        } catch (RemoteException rEx) {
            LOGGER.error(CommonConstants.REMOTE_EXCEPTION_MESSAGE
                    + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + rEx);
            throw new WSDataUnavailableException();
        }
        return serviceStatisticsMap;
    }

    /**
     * (non-Javadoc)
     * 
     * @param serviceids
     * 
     * 
     * @return map
     * @throws WSDataUnavailableException
     * 
     * @see com.groundworkopensource.portal.common.ws.IWSStatisticsFacade#getServiceStatisticsByServiceIds(java.lang.String)
     */
    public Map<String, Long> getServiceStatisticsByServiceIds(
            final String serviceids) throws WSDataUnavailableException {
        StatisticProperty[] propertyArr = null;
        StateStatistics[] stateStatisticCollection = null;
        Map<String, Long> serviceStatisticsMap = new HashMap<String, Long>();
        WSStatistics statisticsBinding = getStatisticsBinding();
        if (null == statisticsBinding) {
            LOGGER
                    .info("Statistics binding is null while getting service statistics by Service IDs");
            return serviceStatisticsMap;
        }
        try {
            // get statistics
            WSFoundationCollection statistics = statisticsBinding
                    .getStatistics(StatisticQueryType.SERVICE_ID_LIST,
                            serviceids, CommonConstants.EMPTY_STRING);
            if (statistics == null) {
                LOGGER
                        .info("Found null WSFoundationCollection when calling getServiceStatisticsByServiceIds() for service IDs : "
                                + serviceids);
                throw new WSDataUnavailableException();
            }

            stateStatisticCollection = statistics.getStateStatisticCollection();
            if (stateStatisticCollection == null) {
                LOGGER
                        .info("Found null stateStatisticCollection when calling getServiceStatisticsByServiceIds() for service IDs :"
                                + serviceids);
                throw new WSDataUnavailableException();
            }

            long total = 0;
            propertyArr = stateStatisticCollection[0].getStatisticProperties();
            if (propertyArr == null) {
                LOGGER
                        .info("Found null propertyArr when calling getServiceStatisticsByServiceIds() for service IDs :"
                                + serviceids);
                throw new WSDataUnavailableException();
            }

            for (int i = 0; i < propertyArr.length; i++) {
                // get the status and count of hostGroup and put into map
                serviceStatisticsMap.put(
                        propertyArr[i].getName().toUpperCase(), propertyArr[i]
                                .getCount());
                if (!propertyArr[i].getName().equalsIgnoreCase(
                        CommonConstants.CRITICAL)) {
                    total = total + propertyArr[i].getCount();
                }
            }
            serviceStatisticsMap.put(CommonConstants.TOTAL_COUNT_KEY, total);

        } catch (WSFoundationException fEx) {
            LOGGER.error(CommonConstants.WSFOUNDATION_EXCEPTION_MESSAGE
                    + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + fEx);
            throw new WSDataUnavailableException();
        } catch (RemoteException rEx) {
            LOGGER.error(CommonConstants.REMOTE_EXCEPTION_MESSAGE
                    + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + rEx);
            throw new WSDataUnavailableException();
        }
        return serviceStatisticsMap;
    }

}
