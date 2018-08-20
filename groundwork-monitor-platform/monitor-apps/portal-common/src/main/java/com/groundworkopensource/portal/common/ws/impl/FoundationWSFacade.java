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

import java.util.Map;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.groundwork.foundation.ws.model.impl.ActionPerform;
import org.groundwork.foundation.ws.model.impl.Category;
import org.groundwork.foundation.ws.model.impl.CategoryEntity;
import org.groundwork.foundation.ws.model.impl.Filter;
import org.groundwork.foundation.ws.model.impl.Host;
import org.groundwork.foundation.ws.model.impl.HostGroup;
import org.groundwork.foundation.ws.model.impl.HostQueryType;
import org.groundwork.foundation.ws.model.impl.NagiosStatisticProperty;
import org.groundwork.foundation.ws.model.impl.NagiosStatisticQueryType;
import org.groundwork.foundation.ws.model.impl.RRDGraph;
import org.groundwork.foundation.ws.model.impl.ServiceStatus;
import org.groundwork.foundation.ws.model.impl.SimpleHost;
import org.groundwork.foundation.ws.model.impl.SimpleServiceStatus;
import org.groundwork.foundation.ws.model.impl.Sort;
import org.groundwork.foundation.ws.model.impl.SortCriteria;
import org.groundwork.foundation.ws.model.impl.StateStatistics;
import org.groundwork.foundation.ws.model.impl.StatisticProperty;
import org.groundwork.foundation.ws.model.impl.StatisticQueryType;
import org.groundwork.foundation.ws.model.impl.WSFoundationCollection;

import com.groundworkopensource.portal.common.exception.GWPortalException;
import com.groundworkopensource.portal.common.exception.WSDataUnavailableException;
import com.groundworkopensource.portal.common.ws.ICategoryWSFacade;
import com.groundworkopensource.portal.common.ws.ICommonWSFacade;
import com.groundworkopensource.portal.common.ws.IEventWSFacade;
import com.groundworkopensource.portal.common.ws.IHostGroupWSFacade;
import com.groundworkopensource.portal.common.ws.IHostWSFacade;
import com.groundworkopensource.portal.common.ws.IWSFacade;
import com.groundworkopensource.portal.common.ws.IWSRRDFacade;
import com.groundworkopensource.portal.common.ws.IWSServiceFacade;
import com.groundworkopensource.portal.common.ws.IWSStatisticsFacade;
import com.groundworkopensource.portal.common.ws.ServiceState;
import com.groundworkopensource.portal.model.CustomGroup;
import com.groundworkopensource.portal.model.EntityType;
import com.groundworkopensource.portal.common.ws.IPortalCustomGroupWSFacade;

/**
 * This class provides methods to interact with foundation web service.
 * 
 * @author rashmi_tambe
 */
public class FoundationWSFacade implements IWSFacade {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -6301950253954497292L;
    /**
     * logger.
     */
    public static final Logger LOGGER = Logger
            .getLogger(FoundationWSFacade.class.getName());

    /**
     * Default Constructor.
     */
    public FoundationWSFacade() {
        // Default Constructor
    }

    /**
     * Returns logger instance.
     * 
     * @return logger
     */
    public static Logger getLogger() {
        return LOGGER;
    }

    /**
     * Returns the list of hosts by calling foundation web service API based on
     * HostQueryType.
     * 
     * @return the list of hosts
     * @throws GWPortalException
     * @throws WSDataUnavailableException
     */
    public final Host[] getAllHosts() throws WSDataUnavailableException,
            GWPortalException {
        return getHostWSFacadeInstance().getAllHosts();
    }

    /**
     * Returns the light weight hosts and services HostQueryType.
     * 
     * @return the list of hosts
     * @throws GWPortalException
     * @throws WSDataUnavailableException
     */
    public final SimpleHost[] getSimpleHosts()
            throws WSDataUnavailableException, GWPortalException {
        return getHostWSFacadeInstance().getSimpleHosts();
    }

    /**
     * Returns the list of host-groups by calling foundation web service API.
     * 
     * @return the list of all host-groups
     * @throws WSDataUnavailableException
     * @throws GWPortalException
     */
    public final HostGroup[] getAllHostGroups() throws GWPortalException,
            WSDataUnavailableException {
        return getHostGroupWSFacadeInstance().getAllHostGroups();
    }

    /**
     * Returns hostgroup statistics data for selected hostgroup name.
     * 
     * @param hostGroup
     * @return StatisticProperty Array: host statistics of passed host-group
     *         name
     * @throws WSDataUnavailableException
     */
    public final StatisticProperty[] getHostStatisticsForHostGroup(
            final String hostGroup) throws WSDataUnavailableException {
        return getStatisticsWSFacadeInstance().getHostStatisticsForHostGroup(
                hostGroup);
    }

    /**
     * Returns service statistics data for selected hostgroup name.
     * 
     * @param hostGroup
     * @return StatisticProperty Array: service statistics of passed host-group
     *         name
     * @throws WSDataUnavailableException
     */
    public final StatisticProperty[] getServiceStatisticsForHostGroup(
            final String hostGroup) throws WSDataUnavailableException {
        return getStatisticsWSFacadeInstance()
                .getServiceStatisticsForHostGroup(hostGroup);
    }

    /**
     * Returns all services.
     * 
     * @return ServiceStatus array of all services
     * @throws WSDataUnavailableException
     * @throws GWPortalException
     */
    public final ServiceStatus[] getServices() throws GWPortalException,
            WSDataUnavailableException {
        return getServiceWSFacadeInstance().getServices();
    }

    /**
     * Returns list of troubled services.
     * 
     * @return ServiceStatus array of troubled services
     * @throws WSDataUnavailableException
     * @throws GWPortalException
     */
    public final ServiceStatus[] getTroubledServices()
            throws GWPortalException, WSDataUnavailableException {
        return getServiceWSFacadeInstance().getTroubledServices();
    }

    /**
     * @return Instance of HostWSFacade
     */
    private IHostWSFacade getHostWSFacadeInstance() {
        return new HostWSFacade();
    }
    
    /**
     * @return Instance of PortalCustomGroupFacade
     */
    private IPortalCustomGroupWSFacade getPortalCustomGroupWSFacadeInstance() {
        return new PortalCustomGroupWSFacade();
    }

    /**
     * @return Instance of HostGroupWSFacade
     */
    private IHostGroupWSFacade getHostGroupWSFacadeInstance() {
        return new HostGroupWSFacade();
    }

    /**
     * @return Instance of StatisticsWSFacade
     */
    private IWSStatisticsFacade getStatisticsWSFacadeInstance() {
        return new StatisticsWSFacade();
    }

    /**
     * @return Instance of ServiceWSFacade
     */
    private IWSServiceFacade getServiceWSFacadeInstance() {
        return new ServiceWSFacade();
    }

    /**
     * @return Instance of HostGroupWSFacade
     */
    private ICategoryWSFacade getCategoryWSFacade() {
        return new CategoryWSFacade();
    }

    /**
     * @return Instance of EventWSFacade
     */
    private IEventWSFacade getEventWSFacadeInstance() {
        return new EventWSFacade();
    }

    /**
     * @return Instance of RRDWSFacade
     */
    private IWSRRDFacade getRRDWSFacadeInstance() {
        return new RRDWSFacade();
    }

    /**
     * (non-Javadoc)
     * 
     * @throws GWPortalException
     * 
     * @see com.groundworkopensource.portal.common.ws.IHostWSFacade#getHostsById(java.lang.String)
     */
    public Host getHostsById(String hostId) throws WSDataUnavailableException,
            GWPortalException {
        return getHostWSFacadeInstance().getHostsById(hostId);
    }

    /**
     * (non-Javadoc)
     * 
     * @throws GWPortalException
     * @throws WSDataUnavailableException
     * 
     * @see com.groundworkopensource.portal.common.ws.IHostWSFacade#getHostsByName(java.
     *      lang.String)
     */
    public final Host getHostsByName(final String hostName)
            throws WSDataUnavailableException, GWPortalException {
        return getHostWSFacadeInstance().getHostsByName(hostName);
    }

    /**
     * (non-Javadoc).
     * 
     * @throws GWPortalException
     * @throws WSDataUnavailableException
     * 
     * @see com.groundworkopensource.portal.common.ws.IHostWSFacade#getHostsUnderHostGroup
     *      (java.lang.String)
     */
    public final SimpleHost[] getHostsUnderHostGroup(
            final String hostGroupName, boolean deep)
            throws WSDataUnavailableException, GWPortalException {
        return getHostWSFacadeInstance().getHostsUnderHostGroup(hostGroupName,
                deep);
    }

    /**
     * (non-Javadoc).
     * 
     * @see com.groundworkopensource.portal.common.ws.IHostWSFacade#getHostsUnderHostGroupById
     *      (java.lang.String)
     * @return Host[]
     * @throws GWPortalException
     * @throws WSDataUnavailableException
     */
    public final Host[] getHostsUnderHostGroupById(final String hostGroupId)
            throws WSDataUnavailableException, GWPortalException {
        return getHostWSFacadeInstance()
                .getHostsUnderHostGroupById(hostGroupId);
    }

    /**
     * getServiceGroupStatisticsForEntireNetwork.
     * 
     * @return map
     */

    public final Map<ServiceState, Long> getServiceGroupStatisticsForEntireNetwork() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * getServiceStatisticsForServiceGroup.
     * 
     * @param String
     *            groupName
     * 
     * @return map
     */

    /**
     * (non-Javadoc)
     * 
     * @see com.groundworkopensource.portal.common.ws.IWSStatisticsFacade#getServiceStatisticsForServiceGroup(java.lang.String)
     */
    public final StatisticProperty[] getServiceStatisticsForServiceGroup(
            final String serviceGroupName) {
        return getStatisticsWSFacadeInstance()
                .getServiceStatisticsForServiceGroup(serviceGroupName);
    }

    /**
     * return the map which contain services group status and Statistics.
     * Example :-map<"OK" 20>
     * 
     * @return map
     * @throws GWPortalException
     * @throws WSDataUnavailableException
     * 
     * @see com.groundworkopensource.portal.common.ws.IWSStatisticsFacade#
     *      getEntireNetworkServiceGroupStatistics ()
     */
    public Map<String, Long> getEntireNetworkServiceGroupStatistics()
            throws WSDataUnavailableException, GWPortalException {
        return getStatisticsWSFacadeInstance()
                .getEntireNetworkServiceGroupStatistics();
    }

    /**
     * return the map which contain host group status and Statistics. Example
     * :-map<"DOWN" 20>
     * 
     * @return Map
     * @throws WSDataUnavailableException
     * @see com.groundworkopensource.portal.common.ws.IWSStatisticsFacade#getEntireNetworkHostGroupStatistics()
     */
    public Map<String, Long> getEntireNetworkHostGroupStatistics()
            throws WSDataUnavailableException {
        return getStatisticsWSFacadeInstance()
                .getEntireNetworkHostGroupStatistics();
    }

    /**
     * return the map which contain Host status and Statistics. Example
     * :-map<"UP" 20>
     * 
     * @return Map
     * @throws WSDataUnavailableException
     * @throws GWPortalException
     */
    public Map<String, Long> getEntireNetworkHostStatistics()
            throws GWPortalException, WSDataUnavailableException {
        return getStatisticsWSFacadeInstance().getEntireNetworkHostStatistics();
    }

    /**
     * return the map which contain services status and Statistics. Example
     * :-map<"OK" 20>
     * 
     * @return Map
     * @throws WSDataUnavailableException
     * @throws GWPortalException
     */
    public Map<String, Long> getEntireNetworkServiceStatistics()
            throws GWPortalException, WSDataUnavailableException {
        return getStatisticsWSFacadeInstance()
                .getEntireNetworkServiceStatistics();
    }

    /**
     * Returns the list of all hosts under filter criteria
     * 
     * @param filter
     * @return the list of all hosts under filter criteria
     * @throws GWPortalException
     * @throws WSDataUnavailableException
     */
    public Host[] getHostsbyCriteria(Filter filter)
            throws WSDataUnavailableException, GWPortalException {
        return getHostWSFacadeInstance().getHostsbyCriteria(filter);
    }

    /**
     * get the service by criteria.
     * 
     * @throws WSDataUnavailableException
     * @throws GWPortalException
     * 
     * @see com.groundworkopensource.portal.common.ws.IWSServiceFacade#getServicesbyCriteria(org.groundwork.foundation.ws.model.impl.Filter)
     */
    public ServiceStatus[] getServicesbyCriteria(Filter filter)
            throws GWPortalException, WSDataUnavailableException {
        return getServiceWSFacadeInstance().getServicesbyCriteria(filter);
    }

    /**
     * Get Services under Host by name
     * 
     * @param hostName
     * @return ServiceStatus[]
     * 
     *         (non-Javadoc)
     * @throws WSDataUnavailableException
     * @throws GWPortalException
     * @see com.groundworkopensource.portal.common.ws.IWSServiceFacade#getServicesByHostName(java.lang.String)
     */
    public ServiceStatus[] getServicesByHostName(String hostName)
            throws GWPortalException, WSDataUnavailableException {
        return getServiceWSFacadeInstance().getServicesByHostName(hostName);
    }

    /**
     * (non-Javadoc)
     * 
     * @throws WSDataUnavailableException
     * @throws GWPortalException
     * 
     * @see com.groundworkopensource.portal.common.ws.IWSServiceFacade#getServicesByHostId(int)
     */
    public ServiceStatus[] getServicesByHostId(int hostId)
            throws GWPortalException, WSDataUnavailableException {
        return getServiceWSFacadeInstance().getServicesByHostId(hostId);
    }

    /**
     * Returns list of Services under a Servicegroup. takes servicegroupid as a
     * parameter.
     * 
     * @param servicegroupid
     * @return List of Services under a servicegroup
     * @throws GWPortalException
     * @throws WSDataUnavailableException
     */
    public ServiceStatus[] getServicesByServiceGroupId(int serviceGroupId)
            throws GWPortalException, WSDataUnavailableException {
        return getServiceWSFacadeInstance().getServicesByServiceGroupId(
                serviceGroupId);
    }

    /**
     * (non-Javadoc)
     * 
     * @throws WSDataUnavailableException
     * @throws GWPortalException
     * 
     * @see com.groundworkopensource.portal.common.ws.IWSStatisticsFacade#
     *      getNagiosStatisticsForHostGroup (java.lang.String)
     */
    public NagiosStatisticProperty[] getNagiosStatisticsForHostGroup(
            String queryValue) throws GWPortalException,
            WSDataUnavailableException {
        return getStatisticsWSFacadeInstance().getNagiosStatisticsForHostGroup(
                queryValue);
    }

    /**
     * (non-Javadoc)
     * 
     * @throws GWPortalException
     * 
     * @see com.groundworkopensource.portal.common.ws.IWSStatisticsFacade#
     *      getNagiosStatisticsForNetwork()
     */
    public NagiosStatisticProperty[] getNagiosStatisticsForNetwork()
            throws WSDataUnavailableException, GWPortalException {
        return getStatisticsWSFacadeInstance().getNagiosStatisticsForNetwork();
    }

    /**
     * @param queryValue
     * @return NagiosStatisticProperty array
     * @throws GWPortalException
     * @throws WSDataUnavailableException
     * @see com.groundworkopensource.portal.common.ws.IWSStatisticsFacade#
     *      getNagiosStatisticsForServiceGroup(String queryValue)
     */
    public NagiosStatisticProperty[] getNagiosStatisticsForServiceGroup(
            String queryValue) throws GWPortalException,
            WSDataUnavailableException {
        return getStatisticsWSFacadeInstance()
                .getNagiosStatisticsForServiceGroup(queryValue);
    }

    /**
     * Returns the list of all host groups under filter criteria
     * 
     * @param filter
     * @param sort
     * @param firstResult
     * @param maxResult
     * @param deep
     * @return the list of all hosts under filter criteria
     * @throws GWPortalException
     * @throws WSDataUnavailableException
     */
    public HostGroup[] getHostGroupsbyCriteria(Filter filter, Sort sort,
            int firstResult, int maxResult, boolean deep)
            throws GWPortalException, WSDataUnavailableException {
        return getHostGroupWSFacadeInstance().getHostGroupsbyCriteria(filter,
                sort, firstResult, maxResult, deep);
    }

    /**
     * returns all available service groups (category).
     * 
     * @return Category[]
     * @throws GWPortalException
     * @throws WSDataUnavailableException
     */
    public Category[] getAllServiceGroups() throws WSDataUnavailableException,
            GWPortalException {
        return getCategoryWSFacade().getAllServiceGroups();
    }

    /**
     * Returns the list of searched ResultSet: containing Host, Host groups,
     * Services etc by calling foundation web service API.
     * 
     * @param searchQuery
     *            , resultsQuantity
     * @param resultsQuantity
     * @param extRoleServiceGroupList
     * @param extRoleHostGroupList
     * @return WSFoundationCollection
     * @throws WSDataUnavailableException
     * @throws GWPortalException
     */
    public WSFoundationCollection searchEntity(String searchQuery,
            int resultsQuantity, String extRoleServiceGroupList,
            String extRoleHostGroupList) throws WSDataUnavailableException,
            GWPortalException {
        return getCommonWSFacade().searchEntity(searchQuery, resultsQuantity,
                extRoleServiceGroupList, extRoleHostGroupList);
    }

    /**
     * returns Common Facade
     * 
     * @return ICommonWSFacade
     */
    private ICommonWSFacade getCommonWSFacade() {
        return new CommonWSFacade();
    }

    /**
     * returns the category array depending on filter applied.
     * 
     * @param filter
     * @param start
     * @param end
     * @param sortCriteria
     * @param retrieveChildren
     * @param namePropertyOnly
     * @return Category
     * @throws GWPortalException
     * @throws WSDataUnavailableException
     */
    public Category[] getCategory(Filter filter, int start, int end,
            SortCriteria sortCriteria, boolean retrieveChildren,
            boolean namePropertyOnly) throws WSDataUnavailableException,
            GWPortalException {
        return getCategoryWSFacade().getCategory(filter, start, end,
                sortCriteria, retrieveChildren, namePropertyOnly);
    }

    /**
     * Returns all host groups
     * 
     * @param deep
     * @return HostGroup[]
     * 
     *         (non-Javadoc)
     * @throws WSDataUnavailableException
     * @throws GWPortalException
     * @see com.groundworkopensource.portal.common.ws.IHostGroupWSFacade#getAllHostGroups(boolean)
     */
    public HostGroup[] getAllHostGroups(boolean deep) throws GWPortalException,
            WSDataUnavailableException {
        return getHostGroupWSFacadeInstance().getAllHostGroups(deep);
    }

    /**
     * (non-Javadoc)
     * 
     * @see com.groundworkopensource.portal.common.ws.IWSStatisticsFacade#getHostAvailabilityForHostgroup(java.lang.String)
     */
    public double getHostAvailabilityForHostgroup(String hostGroupName) {
        return getStatisticsWSFacadeInstance().getHostAvailabilityForHostgroup(
                hostGroupName);
    }

    /**
     * (non-Javadoc)
     * 
     * @see com.groundworkopensource.portal.common.ws.IWSStatisticsFacade#getServiceAvailabilityForHostgroup(java.lang.String)
     */
    public double getServiceAvailabilityForHostgroup(String hostGroupName) {
        return getStatisticsWSFacadeInstance()
                .getServiceAvailabilityForHostgroup(hostGroupName);
    }

    /**
     * Returns All CategoryEntities for ServiceGroup
     * 
     * (non-Javadoc)
     * 
     * @throws GWPortalException
     * @throws WSDataUnavailableException
     * 
     * @see com.groundworkopensource.portal.common.ws.ICategoryWSFacade#getCategoryEntities(java.lang.String)
     */
    public CategoryEntity[] getCategoryEntities(String serviceGroupName)
            throws WSDataUnavailableException, GWPortalException {
        return getCategoryWSFacade().getCategoryEntities(serviceGroupName);
    }

    /**
     * Returns host state transitions for the host-name in the start-date to
     * end-date range. (non-Javadoc)
     * 
     * @throws GWPortalException
     * @throws WSDataUnavailableException
     * 
     * @see com.groundworkopensource.portal.common.ws.IEventWSFacade#
     *      getHostStateTransitions (java.lang. String, java.lang. String,
     *      java.lang. String)
     */
    public WSFoundationCollection getHostStateTransitions(String hostName,
            String startDate, String endDate) throws GWPortalException,
            WSDataUnavailableException {
        return getEventWSFacadeInstance().getHostStateTransitions(hostName,
                startDate, endDate);
    }

    /**
     * Returns service state transitions for the specified service on specified
     * host in the start-date to end-date range. (non-Javadoc)
     * 
     * @throws GWPortalException
     * @throws WSDataUnavailableException
     * 
     * @see com.groundworkopensource.portal.common.ws.IEventWSFacade#getServiceStateTransitions(java.lang.String,
     *      java.lang.String, java.lang.String, java.lang.String)
     */
    public WSFoundationCollection getServiceStateTransitions(String hostName,
            String serviceName, String startDate, String endDate)
            throws GWPortalException, WSDataUnavailableException {
        return getEventWSFacadeInstance().getServiceStateTransitions(hostName,
                serviceName, startDate, endDate);
    }

    /**
     * REturns nagios statistics for the specified query type and value.
     * (non-Javadoc)
     * 
     * @throws WSDataUnavailableException
     * @throws GWPortalException
     * 
     * @see com.groundworkopensource.portal.common.ws.IWSStatisticsFacade#
     *      getNagiosStatistics
     *      (org.groundwork.foundation.ws.model.impl.NagiosStatisticQueryType,
     *      java.lang.String)
     */
    public NagiosStatisticProperty[] getNagiosStatistics(
            NagiosStatisticQueryType queryType, String queryValue)
            throws GWPortalException, WSDataUnavailableException {
        return getStatisticsWSFacadeInstance().getNagiosStatistics(queryType,
                queryValue);
    }

    /**
     * (non-Javadoc)
     * 
     * @throws WSDataUnavailableException
     * @throws GWPortalException
     * 
     * @see com.groundworkopensource.portal.common.ws.IEventWSFacade#getEventsByCriteria(org.groundwork.foundation.ws.model.impl.Filter,
     *      org.groundwork.foundation.ws.model.impl.Sort, int, int)
     */
    public WSFoundationCollection getEventsByCriteria(Filter filter, Sort sort,
            int startIndex, int endIndex) throws GWPortalException,
            WSDataUnavailableException {
        return getEventWSFacadeInstance().getEventsByCriteria(filter, sort,
                startIndex, endIndex);
    }

    /**
     * Returns Category object by its Id (non-Javadoc)
     * 
     * @throws GWPortalException
     * 
     * @see com.groundworkopensource.portal.common.ws.ICategoryWSFacade#getCategoryByID(int)
     */
    public Category getCategoryByID(int categoryId)
            throws WSDataUnavailableException, GWPortalException {
        return getCategoryWSFacade().getCategoryByID(categoryId);
    }

    /**
     * (non-Javadoc)
     * 
     * @throws GWPortalException
     * 
     * @see com.groundworkopensource.portal.common.ws.IWSServiceFacade#getServicesById(int)
     */
    public ServiceStatus getServicesById(int serviceId)
            throws WSDataUnavailableException, GWPortalException {
        return getServiceWSFacadeInstance().getServicesById(serviceId);
    }

    /**
     * (non-Javadoc)
     * 
     * @see com.groundworkopensource.portal.common.ws.IWSStatisticsFacade#getServiceAvailabilityForServiceGroup(java.lang.String)
     */
    public double getServiceAvailabilityForServiceGroup(String serviceGroupName)
            throws WSDataUnavailableException, GWPortalException {
        return getStatisticsWSFacadeInstance()
                .getServiceAvailabilityForServiceGroup(serviceGroupName);
    }

    /**
     * (non-Javadoc)
     * 
     * @throws GWPortalException
     * @throws WSDataUnavailableException
     * 
     * @see com.groundworkopensource.portal.common.ws.ICommonWSFacade#getActionsByApplicationType(java.lang.String,
     *      boolean)
     */
    public WSFoundationCollection getActionsByApplicationType(String appType,
            boolean child) throws WSDataUnavailableException, GWPortalException {
        return getCommonWSFacade().getActionsByApplicationType(appType, child);

    }

    /**
     * (non-Javadoc)
     * 
     * @throws WSDataUnavailableException
     * @throws GWPortalException
     * 
     * @see com.groundworkopensource.portal.common.ws.ICommonWSFacade#performActions(org.groundwork.foundation.ws.model.impl.ActionPerform[])
     */
    public WSFoundationCollection performActions(ActionPerform[] actionPerforms)
            throws GWPortalException, WSDataUnavailableException {
        return getCommonWSFacade().performActions(actionPerforms);
    }

    /**
     * (non-Javadoc)
     * 
     * @throws WSDataUnavailableException
     * @throws GWPortalException
     * 
     * @see com.groundworkopensource.portal.common.ws.IHostGroupWSFacade#getEntireNetworkStatisticsbyCriteria(org.groundwork.foundation.ws.model.impl.Filter)
     */
    public int getEntireNetworkStatisticsbyCriteria(Filter filter)
            throws GWPortalException, WSDataUnavailableException {
        return getHostGroupWSFacadeInstance()
                .getEntireNetworkStatisticsbyCriteria(filter);
    }

    /**
     * (non-Javadoc)
     * 
     * @throws GWPortalException
     * @throws WSDataUnavailableException
     * 
     * @see com.groundworkopensource.portal.common.ws.IHostWSFacade#
     *      getUnscheduledOrScheduledHostCount (org. groundwork .foundation .ws
     *      .model.impl .Filter)
     */
    public int getUnscheduledOrScheduledHostCount(Filter filter)
            throws WSDataUnavailableException, GWPortalException {
        return getHostWSFacadeInstance().getUnscheduledOrScheduledHostCount(
                filter);
    }

    /**
     * (non-Javadoc)
     * 
     * @throws WSDataUnavailableException
     * 
     * @see com.groundworkopensource.portal.common.ws.IWSStatisticsFacade#getHostStatisticsForHostGroupByHostgroupId(java.lang.String)
     */
    public Map<String, Long> getHostStatisticsForHostGroupByHostgroupId(
            String hostGroupId) throws WSDataUnavailableException {
        return getStatisticsWSFacadeInstance()
                .getHostStatisticsForHostGroupByHostgroupId(hostGroupId);

    }

    /**
     * (non-Javadoc)
     * 
     * @throws WSDataUnavailableException
     * 
     * @see com.groundworkopensource.portal.common.ws.IWSStatisticsFacade#getGroupStatisticsForHostGroup(org.groundwork.foundation.ws.model.impl.StatisticQueryType,
     *      org.groundwork.foundation.ws.model.impl.Filter, java.lang.String,
     *      java.lang.String)
     */
    public Map<String, Long> getGroupStatisticsForHostGroup(
            StatisticQueryType statisticQueryType, Filter filter, String name,
            String applicationType) throws WSDataUnavailableException {
        return getStatisticsWSFacadeInstance().getGroupStatisticsForHostGroup(
                statisticQueryType, filter, name, applicationType);
    }

    /**
     * (non-Javadoc)
     * 
     * @throws WSDataUnavailableException
     * @throws GWPortalException
     * 
     * @see com.groundworkopensource.portal.common.ws.IWSStatisticsFacade#getFilteredStatisticsForHost(java.lang.String)
     */
    public Map<String, Long> getFilteredStatisticsForHost(String hostNames)
            throws GWPortalException, WSDataUnavailableException {
        return getStatisticsWSFacadeInstance().getFilteredStatisticsForHost(
                hostNames);

    }

    /**
     * (non-Javadoc)
     * 
     * @throws WSDataUnavailableException
     * @throws GWPortalException
     * 
     * @see com.groundworkopensource.portal.common.ws.IWSStatisticsFacade#getGroupStatisticsForServicegGroup(org.groundwork.foundation.ws.model.impl.StatisticQueryType,
     *      org.groundwork.foundation.ws.model.impl.Filter, java.lang.String,
     *      java.lang.String)
     */
    public Map<String, Long> getGroupStatisticsForServicegGroup(
            StatisticQueryType statisticQueryType, Filter filter, String name,
            String applicationType) throws GWPortalException,
            WSDataUnavailableException {
        return getStatisticsWSFacadeInstance()
                .getGroupStatisticsForServicegGroup(statisticQueryType, filter,
                        name, applicationType);
    }

    /**
     * (non-Javadoc)
     * 
     * @throws GWPortalException
     * @throws WSDataUnavailableException
     * 
     * @see com.groundworkopensource.portal.common.ws.IWSStatisticsFacade#getFilteredServiceStatistics(org.groundwork.foundation.ws.model.impl.StatisticQueryType,
     *      org.groundwork.foundation.ws.model.impl.Filter, java.lang.String,
     *      java.lang.String)
     */
    public Map<String, Long> getFilteredServiceStatistics(
            StatisticQueryType statisticQueryType, Filter filter, String name,
            String applicationType) throws WSDataUnavailableException,
            GWPortalException {
        return getStatisticsWSFacadeInstance().getFilteredServiceStatistics(
                statisticQueryType, filter, name, applicationType);

    }

    /**
     * (non-Javadoc)
     * 
     * @throws WSDataUnavailableException
     * @throws GWPortalException
     * 
     * @see com.groundworkopensource.portal.common.ws.IWSStatisticsFacade#getServiceStatisticsByHostGroupId(java.lang.String)
     */
    public Map<String, Long> getServiceStatisticsByHostGroupId(
            String hostGroupId) throws GWPortalException,
            WSDataUnavailableException {
        return getStatisticsWSFacadeInstance()
                .getServiceStatisticsByHostGroupId(hostGroupId);
    }

    /**
     * Returns service statistics by host name
     * 
     * @throws WSDataUnavailableException
     * @throws GWPortalException
     * 
     * @see com.groundworkopensource.portal.common.ws.IWSStatisticsFacade#getServiceStatisticsByHostName(java.lang.String)
     */
    public Map<String, Long> getServiceStatisticsByHostName(String hostName)
            throws GWPortalException, WSDataUnavailableException {
        return getStatisticsWSFacadeInstance().getServiceStatisticsByHostName(
                hostName);
    }

    /**
     * (non-Javadoc)
     * 
     * @throws WSDataUnavailableException
     * @throws GWPortalException
     * 
     * @see com.groundworkopensource.portal.common.ws.IWSStatisticsFacade#getServiceStatisticsByServiceGroupName(java.lang.String)
     */
    public Map<String, Long> getServiceStatisticsByServiceGroupName(
            String serviceGroupName) throws GWPortalException,
            WSDataUnavailableException {
        return getStatisticsWSFacadeInstance()
                .getServiceStatisticsByServiceGroupName(serviceGroupName);
    }

    /**
     * (non-Javadoc)
     * 
     * @throws WSDataUnavailableException
     * 
     * @see com.groundworkopensource.portal.common.ws.IWSStatisticsFacade#getServiceStatisticsByServiceIds(java.lang.String)
     */
    public Map<String, Long> getServiceStatisticsByServiceIds(String serviceids)
            throws WSDataUnavailableException {
        return getStatisticsWSFacadeInstance()
                .getServiceStatisticsByServiceIds(serviceids);
    }

    /**
     * (non-Javadoc)
     * 
     * @see com.groundworkopensource.portal.common.ws.IWSRRDFacade#getRrdGraph(java.lang.String,
     *      java.lang.String, long, long, java.lang.String, int)
     */
    public RRDGraph[] getRrdGraph(String hostName, String serviceName,
            long startTime, long endTime, String applicationType, int graphWidth)
            throws WSDataUnavailableException {
        return getRRDWSFacadeInstance().getRrdGraph(hostName, serviceName,
                startTime, endTime, applicationType, graphWidth);
    }

    /**
     * Returns services based on given filter, sort type etc
     * 
     * @throws GWPortalException
     * 
     * @see com.groundworkopensource.portal.common.ws.IWSServiceFacade#getServicesbyCriteria(org.groundwork.foundation.ws.model.impl.Filter,
     *      org.groundwork.foundation.ws.model.impl.Sort, int, int)
     */

    public WSFoundationCollection getServicesbyCriteria(Filter filter,
            Sort sort, int startIndex, int pagesize)
            throws WSDataUnavailableException, GWPortalException {
        return getServiceWSFacadeInstance().getServicesbyCriteria(filter, sort,
                startIndex, pagesize);
    }

    /**
     * Get hosts by provided criteria
     * 
     * @throws GWPortalException
     * 
     * @see com.groundworkopensource.portal.common.ws.IHostWSFacade#getHostsbyCriteria(org.groundwork.foundation.ws.model.impl.Filter,
     *      org.groundwork.foundation.ws.model.impl.Sort, int, int)
     */
    public WSFoundationCollection getHostsbyCriteria(Filter filter, Sort sort,
            int startIndex, int pageSize) throws WSDataUnavailableException,
            GWPortalException {
        return getHostWSFacadeInstance().getHostsbyCriteria(filter, sort,
                startIndex, pageSize);
    }

    /**
     * (non-Javadoc)
     * 
     * @throws GWPortalException
     * 
     * @see com.groundworkopensource.portal.common.ws.IHostGroupWSFacade#getHostGroupsById(int)
     */
    public HostGroup getHostGroupsById(int hostGroupId)
            throws WSDataUnavailableException, GWPortalException {
        return getHostGroupWSFacadeInstance().getHostGroupsById(hostGroupId);
    }

    /**
     * (non-Javadoc)
     * 
     * @throws GWPortalException
     * 
     * @see com.groundworkopensource.portal.common.ws.IHostGroupWSFacade#getHostGroupsByName(java.lang.String)
     */
    public HostGroup getHostGroupsByName(String hostGroupName)
            throws WSDataUnavailableException, GWPortalException {
        return getHostGroupWSFacadeInstance()
                .getHostGroupsByName(hostGroupName);
    }

    /**
     * (non-Javadoc)
     * 
     * @see com.groundworkopensource.portal.common.ws.IWSStatisticsFacade#getServiceStatisticsByHostGroupName(java.lang.String)
     */
    public Map<String, Long> getServiceStatisticsByHostGroupName(
            String hostGroupName) throws GWPortalException,
            WSDataUnavailableException {
        return getStatisticsWSFacadeInstance()
                .getServiceStatisticsByHostGroupName(hostGroupName);
    }

    /**
     * (non-Javadoc)
     * 
     * @see com.groundworkopensource.portal.common.ws.IWSStatisticsFacade#getHostStatisticsForHostGroupByHostgroupName(java.lang.String)
     */
    public Map<String, Long> getHostStatisticsForHostGroupByHostgroupName(
            String hostGroupName) throws WSDataUnavailableException {
        return getStatisticsWSFacadeInstance()
                .getHostStatisticsForHostGroupByHostgroupName(hostGroupName);
    }

    /**
     * (non-Javadoc)
     * 
     * @throws GWPortalException
     * 
     * @see com.groundworkopensource.portal.common.ws.ICategoryWSFacade#getCategoryByName(java.lang.String)
     */
    public Category getCategoryByName(String categoryName)
            throws WSDataUnavailableException, GWPortalException {
        return getCategoryWSFacade().getCategoryByName(categoryName);
    }

    /**
     * (non-Javadoc)
     * 
     * @see com.groundworkopensource.portal.common.ws.IWSServiceFacade#getServiceByHostAndServiceName(java.lang.String,
     *      java.lang.String)
     */
    public ServiceStatus getServiceByHostAndServiceName(String hostName,
            String serviceName) throws WSDataUnavailableException,
            GWPortalException {
        return getServiceWSFacadeInstance().getServiceByHostAndServiceName(
                hostName, serviceName);
    }

    /**
     * (non-Javadoc)
     * 
     * @see com.groundworkopensource.portal.common.ws.ICommonWSFacade#getEntityTypeProperties(java.lang.String,
     *      java.lang.String, boolean)
     */
    public WSFoundationCollection getEntityTypeProperties(String entityType,
            String appType, boolean child) throws WSDataUnavailableException,
            GWPortalException {
        return getCommonWSFacade().getEntityTypeProperties(entityType, appType,
                child);
    }

    /**
     * (non-Javadoc)
     * 
     * @see com.groundworkopensource.portal.common.ws.IWSStatisticsFacade#getFilteredHostGroupName(org.groundwork.foundation.ws.model.impl.StatisticQueryType,
     *      org.groundwork.foundation.ws.model.impl.Filter, java.lang.String,
     *      java.lang.String)
     */
    public StateStatistics[] getFilteredHostGroupName(
            StatisticQueryType statisticQueryType, Filter filter, String name,
            String applicationType) throws WSDataUnavailableException {
        return getStatisticsWSFacadeInstance().getFilteredHostGroupName(
                statisticQueryType, filter, name, applicationType);
    }

    /**
     * (non-Javadoc)
     * 
     * @see com.groundworkopensource.portal.common.ws.IHostGroupWSFacade#getHostGroupsById(int,
     *      boolean)
     */
    public HostGroup getHostGroupsById(int hostGroupId, boolean deep)
            throws WSDataUnavailableException, GWPortalException {
        return getHostGroupWSFacadeInstance().getHostGroupsById(hostGroupId,
                deep);
    }

    /**
     * (non-Javadoc)
     * 
     * @see com.groundworkopensource.portal.common.ws.IHostWSFacade#getSimpleHostByName
     *      (java.lang.String, boolean)
     */
    public SimpleHost getSimpleHostByName(String hostName, boolean deep)
            throws WSDataUnavailableException, GWPortalException {
        return getHostWSFacadeInstance().getSimpleHostByName(hostName, deep);
    }

    /**
     * (non-Javadoc)
     * 
     * @see com.groundworkopensource.portal.common.ws.IWSServiceFacade#
     *      getSimpleServicesByHostName(java.lang.String)
     */
    public SimpleServiceStatus[] getSimpleServicesByHostName(String hostName)
            throws GWPortalException, WSDataUnavailableException {
        return getServiceWSFacadeInstance().getSimpleServicesByHostName(
                hostName);
    }

    /**
     * get the Simple Host by criteria.
     * 
     * @param filter
     * @param deep
     * @return Simple Host
     * @throws WSDataUnavailableException
     * @throws GWPortalException
     */
    public WSFoundationCollection getSimpleHostsbyCriteria(Filter filter,
            Sort sort, int firstResult, int maxResults, boolean deep)
            throws WSDataUnavailableException, GWPortalException {
        return getHostWSFacadeInstance().getSimpleHostsbyCriteria(filter, sort,
                firstResult, maxResults, deep);
    }

    /**
     * get the simple service by criteria.
     * 
     * @param filter
     * @param sort
     * @param firstResult
     * @param maxResults
     * @return list of simpleServiceStatus
     * @throws GWPortalException
     * @throws WSDataUnavailableException
     */
    public SimpleServiceStatus[] getSimpleServicesbyCriteria(Filter filter,
            Sort sort, int firstResult, int maxResults)
            throws GWPortalException, WSDataUnavailableException {
        return getServiceWSFacadeInstance().getSimpleServicesbyCriteria(filter,
                sort, firstResult, maxResults);
    }

    /**
     * (non-Javadoc)
     * 
     * @see com.groundworkopensource.portal.common.ws.IWSServiceFacade#getSimpleServiceCollectionbyCriteria(org.groundwork.foundation.ws.model.impl.Filter,
     *      org.groundwork.foundation.ws.model.impl.Sort, int, int)
     */
    public WSFoundationCollection getSimpleServiceCollectionbyCriteria(
            Filter filter, Sort sort, int firstResult, int maxResults)
            throws GWPortalException, WSDataUnavailableException {
        return getServiceWSFacadeInstance()
                .getSimpleServiceCollectionbyCriteria(filter, sort,
                        firstResult, maxResults);
    }

    /**
     * (non-Javadoc)
     * 
     * @see com.groundworkopensource.portal.common.ws.ICategoryWSFacade#getCategoryCollectionbyCriteria(org.groundwork.foundation.ws.model.impl.Filter,
     *      int, int, org.groundwork.foundation.ws.model.impl.SortCriteria,
     *      boolean, boolean)
     */
    public WSFoundationCollection getCategoryCollectionbyCriteria(
            Filter filter, int start, int end, SortCriteria sortCriteria,
            boolean retrieveChildren, boolean namePropertyOnly)
            throws WSDataUnavailableException, GWPortalException {
        return getCategoryWSFacade().getCategoryCollectionbyCriteria(filter,
                start, end, sortCriteria, retrieveChildren, namePropertyOnly);
    }

    /**
     * (non-Javadoc)
     * 
     * @see com.groundworkopensource.portal.common.ws.IHostWSFacade#getHosts(org.groundwork.foundation.ws.model.impl.HostQueryType,
     *      java.lang.String, java.lang.String, int, int,
     *      org.groundwork.foundation.ws.model.impl.SortCriteria)
     */
    public WSFoundationCollection getHosts(HostQueryType hostQueryType,
            String value, String applicationType, int startRange, int endRange,
            SortCriteria sortCriteria) throws WSDataUnavailableException,
            GWPortalException {
        return getHostWSFacadeInstance().getHosts(hostQueryType, value,
                applicationType, startRange, endRange, sortCriteria);
    }
    
    /**
     * Returns the list of hosts by calling foundation web service API based on
     * HostQueryType.
     * 
     * @return the list of hosts
     * @throws GWPortalException
     * @throws WSDataUnavailableException
     */
    public final Collection<CustomGroup> findCustomGroups() throws WSDataUnavailableException {
        return getPortalCustomGroupWSFacadeInstance().findCustomGroups();
    }
    
    /**
     * Create customgroup.
     * 
     * @throws WSDataUnavailableException
     * @throws GWPortalException
     */
    public final void createCustomGroup(String groupName,
			int entityTypeId, String parents, String groupState,
			String createdBy, String children) throws WSDataUnavailableException{
    	getPortalCustomGroupWSFacadeInstance().createCustomGroup(groupName, entityTypeId, parents, groupState,
				createdBy, children);
    }
	
	/**
     * Update customgroup.
     * 
     * @throws WSDataUnavailableException
     * @throws GWPortalException
     */
    public final void updateCustomGroup(String groupName,
			int entityTypeId, String parents, String groupState,
			String createdBy, String children) throws WSDataUnavailableException {
    	getPortalCustomGroupWSFacadeInstance().updateCustomGroup(groupName, entityTypeId, parents, groupState,
				createdBy, children);
    }

	/**
     * Remove customgroup.
     * 
     * @throws WSDataUnavailableException
     * @throws GWPortalException
     */
    public final void removeCustomGroup(Long groupid) throws WSDataUnavailableException {
    	getPortalCustomGroupWSFacadeInstance().removeCustomGroup(groupid);
    }
    
    /**
     * Remove customgroup.
     * 
     * @throws WSDataUnavailableException
     * @throws GWPortalException
     */
    public final void removeOrphanedChildren(Long elementId, int entityTypeId) throws WSDataUnavailableException {
    	getPortalCustomGroupWSFacadeInstance().removeOrphanedChildren(elementId,entityTypeId);
    }
    
    /**
     * Gets all entitytypes customgroup.
     * 
     * @throws WSDataUnavailableException
     * @throws GWPortalException
     */
    public final Collection<EntityType> findEntityTypes() throws WSDataUnavailableException {
    	return getPortalCustomGroupWSFacadeInstance().findEntityTypes();
    }


}
