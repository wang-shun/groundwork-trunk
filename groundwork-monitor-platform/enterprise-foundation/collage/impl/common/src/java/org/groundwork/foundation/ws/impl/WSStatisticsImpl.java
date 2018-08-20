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
package org.groundwork.foundation.ws.impl;

import com.groundwork.collage.impl.CollageConvert;
import com.groundwork.collage.model.Category;
import com.groundwork.collage.model.CategoryEntity;
import com.groundwork.collage.model.Host;
import com.groundwork.collage.model.HostGroup;
import com.groundwork.collage.model.ServiceStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.bs.category.CategoryService;
import org.groundwork.foundation.dao.FilterCriteria;
import org.groundwork.foundation.dao.FoundationQueryList;
import org.groundwork.foundation.ws.api.WSFoundationException;
import org.groundwork.foundation.ws.api.WSStatistics;
import org.groundwork.foundation.ws.model.NagiosStatisticQueryType;
import org.groundwork.foundation.ws.model.StatisticQueryType;
import org.groundwork.foundation.ws.model.impl.ExceptionType;
import org.groundwork.foundation.ws.model.impl.Filter;
import org.groundwork.foundation.ws.model.impl.FilterOperator;
import org.groundwork.foundation.ws.model.impl.HostGroupStatisticProperty;
import org.groundwork.foundation.ws.model.impl.StateStatistics;
import org.groundwork.foundation.ws.model.impl.StatisticProperty;
import org.groundwork.foundation.ws.model.impl.WSFoundationCollection;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

public class WSStatisticsImpl extends WebServiceImpl implements WSStatistics {
    /* Enable logging */
    protected static Log log = LogFactory.getLog(WSStatisticsImpl.class);

    public WSStatisticsImpl() {
    }

    public WSFoundationCollection getStatistics(StatisticQueryType statType,
            String value, String applicationType) throws WSFoundationException,
            RemoteException {
        if (log.isInfoEnabled()) {
            log.info("Statistics Web Service -- entering get statistics");
        }

        org.groundwork.foundation.ws.model.impl.StateStatistics[] statistics = null;

        // check first for empty event type
        if (statType == null) {
            throw new WSFoundationException(
                    "StatisticQueryType cannot be null",
                    ExceptionType.WEBSERVICE);
        }

        if (statType.getValue().compareToIgnoreCase(
                StatisticQueryType._TOTALS_FOR_HOSTS) == 0)
            statistics = new StateStatistics[] { getConverter().convert(
                    getStatisticsService().getHostStatisticTotals()) };
        else if (statType.getValue().compareToIgnoreCase(
                StatisticQueryType._TOTALS_FOR_SERVICES) == 0)
            statistics = new StateStatistics[] { getConverter().convert(
                    getStatisticsService().getServiceStatisticTotals()) };
        else if (statType.getValue().compareToIgnoreCase(
                StatisticQueryType._ALL_HOSTS) == 0)
            statistics = getConverter().convertStatistics(
                    getStatisticsService().getAllHostStatistics());
        else if (statType.getValue().equalsIgnoreCase(
                StatisticQueryType._HOST_LIST)) {
            // Convert comma seperated hostNames to Array
            StringTokenizer stkn = new StringTokenizer(value, ",");
            String[] hostNames = new String[stkn.countTokens()];
            int i = 0;
            while (stkn.hasMoreTokens()) {
                hostNames[i] = stkn.nextToken();
                i++;
            }
            statistics = new StateStatistics[] { getConverter().convert(
                    getStatisticsService().getAllHostStatisticsByNames(
                            hostNames)) };
        } else if (statType.getValue().equalsIgnoreCase(
                StatisticQueryType._SERVICE_ID_LIST)) {
            // Convert comma seperated hostNames to Array
            StringTokenizer stkn = new StringTokenizer(value, ",");
            int[] serviceIds = new int[stkn.countTokens()];
            int i = 0;
            while (stkn.hasMoreTokens()) {
                serviceIds[i] = Integer.parseInt(stkn.nextToken());
                i++;
            }
            statistics = new StateStatistics[] { getConverter().convert(
                    getStatisticsService().getServiceStatisticsByServiceIDs(
                            serviceIds)) };
        } else if (statType.getValue().compareToIgnoreCase(
                StatisticQueryType._ALL_SERVICES) == 0)
            statistics = getConverter().convertStatistics(
                    getStatisticsService().getAllServiceStatistics());
        else if (statType.getValue().compareToIgnoreCase(
                StatisticQueryType._HOSTS_FOR_HOSTGROUPID) == 0)
            statistics = new StateStatistics[] { getConverter().convert(
                    getStatisticsService().getHostStatisticsByHostGroupId(
                            Integer.valueOf(value))) };
        else if (statType.getValue().compareToIgnoreCase(
                StatisticQueryType._HOSTS_FOR_HOSTGROUPNAME) == 0)
            statistics = new StateStatistics[] { getConverter().convert(
                    getStatisticsService().getHostStatisticsByHostGroupName(
                            value)) };
        else if (statType.getValue().compareToIgnoreCase(
                StatisticQueryType._SERVICES_FOR_HOSTGROUPID) == 0)
            statistics = new StateStatistics[] { getConverter().convert(
                    getStatisticsService().getServiceStatisticsByHostGroupId(
                            Integer.valueOf(value))) };
        else if (statType.getValue().compareToIgnoreCase(
                StatisticQueryType._SERVICES_FOR_HOSTGROUPNAME) == 0)
            statistics = new StateStatistics[] { getConverter().convert(
                    getStatisticsService().getServiceStatisticsByHostGroupName(
                            value)) };
        else if (statType.getValue().compareToIgnoreCase(
                StatisticQueryType._TOTALS_FOR_SERVICES_BY_HOSTNAME) == 0) {
            com.groundwork.collage.model.impl.StateStatistics stats = getStatisticsService()
                    .getServiceStatisticByHostName(value);
            statistics = new StateStatistics[] { getConverter().convert(stats) };
        } else if (statType.getValue().compareToIgnoreCase(
                StatisticQueryType._HOSTGROUP_STATE_COUNTS_HOST) == 0) {
            return new WSFoundationCollection(getConverter().convertStatisticProperties(
                    getStatisticsService().getHostGroupStateCountHost()));
        } else if (statType.getValue().compareToIgnoreCase(
                StatisticQueryType._HOSTGROUP_STATE_COUNTS_SERVICE) == 0) {
            return new WSFoundationCollection(getConverter().convertStatisticProperties(
                    getStatisticsService().getHostGroupStateCountService()));
        } else if (statType.getValue().compareToIgnoreCase(
                StatisticQueryType._SERVICEGROUP_STATS_BY_SERVICEGROUPNAME) == 0)
            statistics = new StateStatistics[] { getConverter().convert(
                    getStatisticsService()
                            .getServiceStatisticsByServiceGroupName(value)) };
        else if (statType.getValue().compareToIgnoreCase(
                StatisticQueryType._SERVICEGROUP_STATS_FOR_ALL_NETWORK) == 0)
            statistics = getConverter().convertStatistics(
                    getStatisticsService()
                            .getServiceStatisticsForAllServiceGroups());
        else {
            log
                    .error("Invalid StatisticQueryType specified in getStatistics - "
                            + statType);
            throw new WSFoundationException(
                    "Invalid StatisticQueryType specified in getStatistics - "
                            + statType, ExceptionType.WEBSERVICE);
        } // end if/else

        return new WSFoundationCollection(statistics);
    }

    public WSFoundationCollection getGroupStatistics(StatisticQueryType type,
            Filter filter, String groupName, String applicationType)
            throws WSFoundationException, RemoteException {
        log.info("Statistics Web Service -- entering get statistics-- Filter");
        org.groundwork.foundation.ws.model.impl.StateStatistics[] statistics = null;
        CollageConvert converter = getConverter();

        // check first for empty event type
        if (type == null) {
            throw new WSFoundationException(
                    "StatisticQueryType cannot be null",
                    ExceptionType.WEBSERVICE);
        }

        if (type.getValue().compareToIgnoreCase(
                StatisticQueryType._HOSTGROUP_STATISTICS_BY_FILTER) == 0) {
            log.debug("To call getHostGroupStatisticsByFilter");
            FilterCriteria filterCriteria = null;
            if (groupName == null || "".equalsIgnoreCase(groupName)) {
                filterCriteria = converter.convert(filter);
                FoundationQueryList list = getHostGroupService().getHostGroups(
                        filterCriteria, null, -1, -1);
                List<HostGroup> results = list.getResults();
                Collection<String> hostGroupNames = new ArrayList<String>();
                for (int i = 0; i < results.size(); i++) {
                    hostGroupNames.add(results.get(i).getName());
                } // end for

                statistics = getConverter().convertStatistics(
                        getStatisticsService()
                                .getHostStatisticsByHostGroupNames(
                                        hostGroupNames));
            } else {
                // First find hosts that match the filter criteria and then
                // filterout the hosts matching the supplied hostgroup
                // and return the statistics data
                filterCriteria = converter.convert(filter);
                FoundationQueryList list = getHostService().getHosts(
                        filterCriteria, null, -1, -1);
                List<Host> results = list.getResults();
                ArrayList<String> hostNames = new ArrayList<String>();
                for (int i = 0; i < results.size(); i++) {
                    Host host = results.get(i);
                    Set<HostGroup> hostGroups = host.getHostGroups();
                    if (hostGroups != null) {
                        Iterator<HostGroup> iter = hostGroups.iterator();
                        while (iter.hasNext()) {
                            HostGroup hostGroup = iter.next();
                            if (hostGroup.getName().equalsIgnoreCase(groupName)
                                    && host.getHostName() != null)
                                hostNames.add(host.getHostName());
                        } // end while
                    } // end if
                } // end for
                String[] hostArray = new String[hostNames.size()];
                if (hostNames.size() > 0) {
                    statistics = new StateStatistics[1];
                    statistics[0] = getConverter().convert(
                            getStatisticsService().getAllHostStatisticsByNames(
                                    hostNames.toArray(hostArray)));
                    statistics[0].setName(groupName);
                } // end if

            } // end if

        } else if (type.getValue().compareToIgnoreCase(
                StatisticQueryType._SERVICEGROUP_STATISTICS_BY_FILTER) == 0) {
            log.debug("To call getServiceGroupStatisticsByFilter");
            Collection<Category> categories = getCategoryService()
                    .getRootCategories("SERVICE_GROUP");

            // if group name is null or empty AND there are no service-groups
            // (categories) in the system, then return empty statistics
            // collection.
            if ((groupName == null || groupName.equals(""))
                    && (categories == null || categories.size() <= 0)) {
                statistics = new StateStatistics[1];
                StatisticProperty[] stateProps = new StatisticProperty[0];
                statistics[0] = new StateStatistics("NONE", null, 0, 0,
                        stateProps);
                return new WSFoundationCollection(statistics);
            }

            // create the filter criteria
            FilterCriteria filterCriteria = null;
            if ((groupName == null || "".equalsIgnoreCase(groupName))
                    && (categories == null || categories.size() <= 0)) {
                filterCriteria = converter.convert(filter);
            } else {
                filterCriteria = createFilterCriteriaForServiceGroup(groupName,
                        filter, categories);
            } // end if

            return new WSFoundationCollection(getStatisticsForServiceGroups(
                    categories, filterCriteria, filter, groupName));

        } else if (type.getValue().compareToIgnoreCase(
                StatisticQueryType._SERVICE_STATISTICS_BY_FILTER) == 0) {
            log.debug("To call getServiceStatisticsByFilter");
            FilterCriteria filterCriteria = null;
            if (groupName == null || groupName.equalsIgnoreCase("")) {
                filterCriteria = converter.convert(filter);
                return new WSFoundationCollection(
                        getStatisticsForServiceGroups(null, filterCriteria,
                                filter, null));
            } else {
                filterCriteria = createFilterCriteriaForServiceGroup(groupName,
                        filter, null);
                return new WSFoundationCollection(
                        getStatisticsForServiceGroups(null, filterCriteria,
                                filter, groupName));
            }
        } else {
            log
                    .error("Invalid StatisticQueryType specified in getStatistics - "
                            + type);
            throw new WSFoundationException(
                    "Invalid StatisticQueryType specified in getStatistics - "
                            + type, ExceptionType.WEBSERVICE);
        }
        return new WSFoundationCollection(statistics);
    }

    /**
     * String parameter version of getStatistics() This method return a set of
     * HostGroupStatisticProperty instances for each host group specified. The
     * WSFoundationCollection HostGroupStatisticProperty[] is populated.
     */
    public WSFoundationCollection getStatisticsByString(String type,
            String value, String applicationType) throws WSFoundationException,
            RemoteException {
        try {
            // Note: If type is "ALL_HOSTS" and "ALL_SERVICES then we return the
            // statistics for each individual host group and not
            // just one total for all. To get totals then pass, SERVICE_TOTALS,
            // HOST_TOTALS
            Collection<HostGroupStatisticProperty> hgStatistics = null;

            if (type == null
                    || type.length() == 0
                    || type
                            .equalsIgnoreCase(StatisticQueryType._TOTALS_FOR_HOSTS)) {
                // TODO: ByString methods will be completely removed
                // hgStatistics =
                // getConverter().convertStateStatistics(getStatisticsService().getHostStatisticTotals());
            } else if (type
                    .equalsIgnoreCase(StatisticQueryType._TOTALS_FOR_SERVICES)) {
                // TODO: ByString methods will be completely removed
                // hgStatistics =
                // getConverter().convertStateStatistics(getStatisticsService().getServiceStatisticTotals());
            } else if (type.equalsIgnoreCase(StatisticQueryType._ALL_HOSTS)) {
                hgStatistics = getConverter().convertStateStatistics(
                        getStatisticsService().getAllHostStatistics());
            } else if (type.equalsIgnoreCase(StatisticQueryType._ALL_SERVICES)) {
                hgStatistics = getConverter().convertStateStatistics(
                        getStatisticsService().getAllServiceStatistics());
            } else if (type
                    .equalsIgnoreCase(StatisticQueryType._HOSTS_FOR_HOSTGROUPID)) {
                Collection<com.groundwork.collage.model.impl.StateStatistics> list = new ArrayList<com.groundwork.collage.model.impl.StateStatistics>(
                        1);
                list.add(getStatisticsService().getHostStatisticsByHostGroupId(
                        Integer.parseInt(value)));

                hgStatistics = getConverter().convertStateStatistics(list);
            } else if (type
                    .equalsIgnoreCase(StatisticQueryType._HOSTS_FOR_HOSTGROUPNAME)) {
                Collection<com.groundwork.collage.model.impl.StateStatistics> list = new ArrayList<com.groundwork.collage.model.impl.StateStatistics>(
                        1);
                list.add(getStatisticsService()
                        .getHostStatisticsByHostGroupName(value));

                hgStatistics = getConverter().convertStateStatistics(list);
            } else if (type
                    .equalsIgnoreCase(StatisticQueryType._SERVICES_FOR_HOSTGROUPID)) {
                Collection<com.groundwork.collage.model.impl.StateStatistics> list = new ArrayList<com.groundwork.collage.model.impl.StateStatistics>(
                        1);
                list.add(getStatisticsService()
                        .getServiceStatisticsByHostGroupId(
                                Integer.parseInt(value)));

                hgStatistics = getConverter().convertStateStatistics(list);
            } else if (type
                    .equalsIgnoreCase(StatisticQueryType._SERVICES_FOR_HOSTGROUPNAME)) {
                Collection<com.groundwork.collage.model.impl.StateStatistics> list = new ArrayList<com.groundwork.collage.model.impl.StateStatistics>(
                        1);
                list.add(getStatisticsService()
                        .getServiceStatisticsByHostGroupName(value));

                hgStatistics = getConverter().convertStateStatistics(list);
            } else if (type
                    .equalsIgnoreCase(StatisticQueryType._TOTALS_FOR_SERVICES_BY_HOSTNAME)) {
                // Package into an array an convert to a
                // HostGroupStatisticProperty[]
                Collection<com.groundwork.collage.model.impl.StateStatistics> list = new ArrayList<com.groundwork.collage.model.impl.StateStatistics>(
                        1);
                list.add(getStatisticsService().getServiceStatisticByHostName(
                        value));

                hgStatistics = getConverter().convertStateStatistics(list);
            } else {
                throw new WSFoundationException(
                        "Invalid StatisticQueryType specified in getStatistics - "
                                + type, ExceptionType.WEBSERVICE);
            }

            if (hgStatistics == null || hgStatistics.size() == 0) {
                return new WSFoundationCollection();
            }

            return new WSFoundationCollection(hgStatistics
                    .toArray(new HostGroupStatisticProperty[0]));
        } catch (Exception e) {
            log.error("Exception occurred in getStatisticsByString().", e);
            throw new WSFoundationException(
                    "Exception occurred in getStatisticsByString().",
                    ExceptionType.WEBSERVICE);
        }
    }

    public WSFoundationCollection getNagiosStatistics(
            NagiosStatisticQueryType queryType, String value)
            throws WSFoundationException, RemoteException {
        org.groundwork.foundation.ws.model.impl.NagiosStatisticProperty[] nagiosStatistics = null;

        // check for empty event type
        if (queryType == null) {
            throw new WSFoundationException(
                    "StatisticQueryType cannot be null",
                    ExceptionType.WEBSERVICE);
        }

        try {
            // TODO: The application type parameter is currently ignored. That
            // is why its hard-wired.
            if (queryType.getValue().equalsIgnoreCase(
                    NagiosStatisticQueryType._HOSTGROUPID))
                nagiosStatistics = getConverter().convertNagiosStatProps(
                        getStatisticsService().getApplicationStatistics(100,
                                Integer.valueOf(value)));
            else if (queryType.getValue().equalsIgnoreCase(
                    NagiosStatisticQueryType._HOSTGROUPNAME))
                nagiosStatistics = getConverter().convertNagiosStatProps(
                        getStatisticsService().getApplicationStatistics(100,
                                value));
            else if (queryType.getValue().equalsIgnoreCase(
                    NagiosStatisticQueryType._HOSTID))
                nagiosStatistics = getConverter().convertNagiosStatProps(
                        getStatisticsService().getApplicationStatisticsHost(
                                100, Integer.valueOf(value)));
            else if (queryType.getValue().equalsIgnoreCase(
                    NagiosStatisticQueryType._HOSTNAME))
                nagiosStatistics = getConverter().convertNagiosStatProps(
                        getStatisticsService().getApplicationStatisticsHost(
                                100, value));
            else if (queryType.getValue().equalsIgnoreCase(
                    NagiosStatisticQueryType._SYSTEM))
                nagiosStatistics = getConverter().convertNagiosStatProps(
                        getStatisticsService().getApplicationStatisticsTotals(
                                100));
            else if (queryType.getValue().equalsIgnoreCase(
                    NagiosStatisticQueryType._HOSTLIST)) {
                // Convert comma seperated hostNames to Array
                StringTokenizer stkn = new StringTokenizer(value, ",");
                String[] hostNames = new String[stkn.countTokens()];
                int i = 0;
                while (stkn.hasMoreTokens()) {
                    hostNames[i] = stkn.nextToken();
                    i++;
                }
                nagiosStatistics = getConverter().convertNagiosStatProps(
                        getStatisticsService()
                                .getApplicationStatisticsHostList(100,
                                        hostNames));
            } else if (queryType.getValue().equalsIgnoreCase(
                    NagiosStatisticQueryType._SERVICEGROUPNAME))
                nagiosStatistics = getConverter()
                        .convertNagiosStatProps(
                                getStatisticsService()
                                        .getNagiosStatisticsForServiceGroup(
                                                100, value));
            else {
                log
                        .error("Invalid NagiosStatisticQueryType specified in getStatistics");
                throw new WSFoundationException(
                        "Invalid NagiosStatisticQueryType specified in getStatistics",
                        ExceptionType.WEBSERVICE);
            }
        } catch (Exception e) {
            log.error("Exception occurred in getNagiosStatistics()", e);
            throw new WSFoundationException(
                    "Exception occurred in getNagiosStatistics() - " + e,
                    ExceptionType.WEBSERVICE);
        }

        return new WSFoundationCollection(nagiosStatistics);
    }

    /**
     * String parameter version of getStatistics()
     */
    public WSFoundationCollection getNagiosStatisticsByString(String type,
            String value) throws WSFoundationException, RemoteException {
        // Do parameter conversion then delegate
        org.groundwork.foundation.ws.model.impl.NagiosStatisticQueryType queryType = org.groundwork.foundation.ws.model.impl.NagiosStatisticQueryType.HOSTGROUPID;

        if (type != null) {
            queryType = org.groundwork.foundation.ws.model.impl.NagiosStatisticQueryType
                    .fromValue(type);
        }

        return getNagiosStatistics(queryType, value);
    }

    public double getHostAvailabilityForHostgroup(String hostGroupName)
            throws java.rmi.RemoteException, WSFoundationException {
        if (hostGroupName == null || "".equalsIgnoreCase(hostGroupName))
            throw new WSFoundationException("Invalid hostGroup Name supplied",
                    ExceptionType.WEBSERVICE);
        log
                .debug("WSStatisticsImpl.getHostAvailabilityForHostgroup hostGroupName=["
                        + hostGroupName + "]");
        log
                .debug("WSStatisticsImpl.getHostAvailabilityForHostgroup hostGroupName value =["
                        + getStatisticsService()
                                .getHostAvailabilityForHostgroup(hostGroupName)
                        + "]");
        return getStatisticsService().getHostAvailabilityForHostgroup(
                hostGroupName);
    }

    public double getServiceAvailabilityForHostgroup(String hostGroupName)
            throws java.rmi.RemoteException, WSFoundationException {
        if (hostGroupName == null || "".equalsIgnoreCase(hostGroupName))
            throw new WSFoundationException("Invalid hostGroup Name supplied",
                    ExceptionType.WEBSERVICE);
        log
                .debug("WSStatisticsImpl.getServiceAvailabilityForHostgroup hostGroupName=["
                        + hostGroupName + "]");
        log
                .debug("WSStatisticsImpl.getServiceAvailabilityForHostgroup hostGroupName=["
                        + getStatisticsService()
                                .getServiceAvailabilityForHostGroup(
                                        hostGroupName) + "]");
        return getStatisticsService().getServiceAvailabilityForHostGroup(
                hostGroupName);
    }

    /**
     * Method to be invoked to return a statistic: percentage of Services with
     * status OK
     * 
     * @param serviceGroupName
     * @return
     * @throws java.rmi.RemoteException
     * @throws WSFoundationException
     */
    public double getServiceAvailabilityForServiceGroup(
            java.lang.String serviceGroupName) throws java.rmi.RemoteException,
            WSFoundationException {
        if (serviceGroupName == null || "".equalsIgnoreCase(serviceGroupName))
            throw new WSFoundationException(
                    "Invalid serviceGroup Name supplied",
                    ExceptionType.WEBSERVICE);
        log
                .debug("WSStatisticsImpl.getServiceAvailabilityForHostgroup hostGroupName=["
                        + serviceGroupName + "]");
        log
                .debug("WSStatisticsImpl.getServiceAvailabilityForHostgroup hostGroupName=["
                        + getStatisticsService()
                                .getServiceAvailabilityForServiceGroup(
                                        serviceGroupName) + "]");
        return getStatisticsService().getServiceAvailabilityForServiceGroup(
                serviceGroupName);
    }

    /**
     * Helper method to create service groupfilter
     * 
     * @param serviceGroupName
     * @param filter
     * @return
     */
    private FilterCriteria createFilterCriteriaForServiceGroup(
            String serviceGroupName, Filter filter,
            Collection<Category> serviceGroups) {
        FilterCriteria filterCriteria = null;
        Collection<CategoryEntity> services = null;
        if (serviceGroupName != null && !serviceGroupName.equals("")) {
            services = getCategoryService().getCategoryByName(serviceGroupName, CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP)
                    .getCategoryEntities();
        } else {
            if (serviceGroups == null || serviceGroups.size() <= 0)
                return filterCriteria;
            else
                return getConverter().convert(filter);
        } // end if/else
        StringBuffer sb = new StringBuffer();
        Iterator<CategoryEntity> iter = services.iterator();
        while (iter.hasNext()) {
            CategoryEntity catEntity = iter.next();
            int serviceId = catEntity.getObjectID().intValue();
            sb.append(serviceId);
            sb.append(",");
        } // end while
        String serviceIds = sb.substring(0, sb.length() - 1);
        log.info("Service IDs are " + serviceIds);
        Filter serviceIdsFilter = new Filter("serviceStatusId",
                FilterOperator.IN, serviceIds);
        CollageConvert converter = getConverter();
        filterCriteria = converter
                .convert(Filter.AND(serviceIdsFilter, filter));
        return filterCriteria;
    }

    /**
     * Gets the statistics for the given categories
     * 
     * @param categories
     * @return
     */
    private StateStatistics[] getStatisticsForServiceGroups(
            Collection<Category> categories, FilterCriteria filterCriteria,
            Filter filter, String serviceGroupName) {
        StateStatistics[] statistics = null;
        if (serviceGroupName != null && !serviceGroupName.equals("")) {
            statistics = populateStats(filterCriteria, serviceGroupName);
        } else {
            if (categories == null || categories.size() <= 0) {
                statistics = populateStats(filterCriteria, serviceGroupName);
            } else {
                statistics = new StateStatistics[categories.size()];
                int count = 0;
                for (Category category : categories) {
                    filterCriteria = createFilterCriteriaForServiceGroup(
                            category.getName(), filter, categories);
                    FoundationQueryList list = getStatusService().getServices(
                            filterCriteria, null, -1, -1);

                    List<String> serviceStatuslist = this
                            .getStatisticsService().getServiceStatusList();
                    StatisticProperty[] stateProps = new StatisticProperty[serviceStatuslist
                            .size()];
                    for (int i = 0; i < serviceStatuslist.size(); i++) {
                        stateProps[i] = new StatisticProperty(serviceStatuslist
                                .get(i), 0);
                    } // end for

                    List<ServiceStatus> results = list.getResults();

                    for (int i = 0; i < results.size(); i++) {
                        ServiceStatus service = results.get(i);
                        for (int j = 0; j < stateProps.length; j++) {
                            if (service.getMonitorStatus().getName().equals(
                                    stateProps[j].getName())) {
                                stateProps[j]
                                        .setCount(stateProps[j].getCount() + 1);
                            } // end if
                        } // end for
                    } // end for

                    statistics[count] = new StateStatistics(category.getName(),
                            null, 0, list.getResults().size(), stateProps);
                    count++;
                }
            }
        }
        return statistics;
    } // end if

    /**
     * Helper method
     * 
     * @param filterCriteria
     * @param serviceGroupName
     * @return
     */
    private StateStatistics[] populateStats(FilterCriteria filterCriteria,
            String serviceGroupName) {
        StateStatistics[] statistics = null;
        FoundationQueryList list = getStatusService().getServices(
                filterCriteria, null, -1, -1);

        List<String> serviceStatuslist = this.getStatisticsService()
                .getServiceStatusList();
        StatisticProperty[] stateProps = new StatisticProperty[serviceStatuslist
                .size()];
        for (int i = 0; i < serviceStatuslist.size(); i++) {
            stateProps[i] = new StatisticProperty(serviceStatuslist.get(i), 0);
        } // end for

        List<ServiceStatus> results = list.getResults();

        for (int i = 0; i < results.size(); i++) {
            ServiceStatus service = results.get(i);
            for (int j = 0; j < stateProps.length; j++) {
                if (service.getMonitorStatus().getName().equals(
                        stateProps[j].getName())) {
                    stateProps[j].setCount(stateProps[j].getCount() + 1);
                } // end if
            } // end for
        } // end for

        statistics = new StateStatistics[1];
        if (serviceGroupName == null)
            serviceGroupName = "NONE";
        statistics[0] = new StateStatistics(serviceGroupName, null, 0, list
                .getResults().size(), stateProps);
        return statistics;
    }
}
