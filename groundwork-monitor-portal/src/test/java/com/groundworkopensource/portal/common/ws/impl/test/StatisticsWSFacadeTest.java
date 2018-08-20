
package com.groundworkopensource.portal.common.ws.impl.test;

import java.util.Map;

import junit.framework.TestCase;

import org.groundwork.foundation.ws.model.impl.Category;
import org.groundwork.foundation.ws.model.impl.Filter;
import org.groundwork.foundation.ws.model.impl.FilterOperator;
import org.groundwork.foundation.ws.model.impl.Host;
import org.groundwork.foundation.ws.model.impl.HostGroup;
import org.groundwork.foundation.ws.model.impl.NagiosStatisticProperty;
import org.groundwork.foundation.ws.model.impl.ServiceStatus;
import org.groundwork.foundation.ws.model.impl.StatisticProperty;
import org.groundwork.foundation.ws.model.impl.StatisticQueryType;

import com.groundworkopensource.portal.common.CommonConstants;
import com.groundworkopensource.portal.common.exception.GWPortalException;
import com.groundworkopensource.portal.common.exception.WSDataUnavailableException;
import com.groundworkopensource.portal.common.ws.IWSFacade;
import com.groundworkopensource.portal.common.ws.WebServiceFactory;
import com.groundworkopensource.portal.common.ws.WebServiceFactory.WebServiceType;
import com.groundworkopensource.portal.common.ws.impl.CategoryWSFacade;
import com.groundworkopensource.portal.common.ws.impl.HostGroupWSFacade;

/**
 * Test class for StatisticsWSFacade.
 * 
 * @author nitin_jadhav
 * 
 */
public class StatisticsWSFacadeTest extends TestCase {

    /**
     * STATISTIC_PROPERTY_IS_NULL
     */
    private static final String STATISTIC_PROPERTY_IS_NULL = "Failed to retrieve service group statistics due to statisticProperty being null  ";
    /**
     * FAILED_TO_RETRIEVE_SERVICE_GROUP_STATISTICS
     */
    private static final String FAILED_TO_RETRIEVE_SERVICE_GROUP_STATISTICS = "Failed to retrieve service group statistics as foundationWSFacade is null  ";

    /**
     * foundationWSFacade Object to call web services.
     */
    private IWSFacade foundationWSFacade;

    /**
     * @throws java.lang.Exception
     */
    @Override
    public final void setUp() throws Exception {
        foundationWSFacade = new WebServiceFactory()
                .getWebServiceInstance(WebServiceType.FOUNDATION_WEBSERVICE);
    }

    /**
     * @throws java.lang.Exception
     */
    @Override
    public final void tearDown() throws Exception {
        foundationWSFacade = null;
    }

    /**
     * Test method for
     * {@link com.groundworkopensource.portal.common.ws.impl.StatisticsWSFacade#getNagiosStatistics(org.groundwork.foundation.ws.model.impl.NagiosStatisticQueryType, java.lang.String)}
     * This method tests StatisticsWSFacade#getNagiosStatistics() API for query
     * type HOSTLIST
     */
    public final void testGetNagiosStatisticsForHostList() {

        Filter leftFilter = new Filter("hostStatus.hostMonitorStatus.name",
                FilterOperator.EQ, "DOWN");
        Filter rightSubFilter1 = new Filter("hostStatus.propertyValues.name",
                FilterOperator.EQ, "isAcknowledged");
        Filter rightSubFilter2 = new Filter(
                "hostStatus.propertyValues.valueBoolean", FilterOperator.EQ,
                true);

        Filter rightFilter = Filter.AND(rightSubFilter1, rightSubFilter2);
        Filter filter = Filter.AND(leftFilter, rightFilter);
        Host[] hosts = null;
        try {
            hosts = foundationWSFacade.getHostsbyCriteria(filter);
        } catch (WSDataUnavailableException e1) {
            fail("getHostsbyCriteria() failed. WSDataUnavailableException details : "
                    + e1);
        } catch (GWPortalException e1) {
            fail("getHostsbyCriteria() failed. GWPortalException details : "
                    + e1);
        }

        if (hosts == null || hosts.length == 0) {
            System.out
                    .println("There are no hosts matching filter criteria. Hence empty host list returned.");
        } else {
            StringBuffer strBuf = new StringBuffer();
            String hostListString = "";

            // Create the comma separated host list string.
            for (Host host : hosts) {
                strBuf.append(host.getName()).append(TestConstants.COMMA);
            }
            // Remove the last comma.
            strBuf = strBuf.deleteCharAt(strBuf.length() - 1);
            hostListString = strBuf.toString();

            NagiosStatisticProperty[] property = null;
            try {
                property = foundationWSFacade
                        .getNagiosStatistics(
                                org.groundwork.foundation.ws.model.impl.NagiosStatisticQueryType.HOSTLIST,
                                hostListString);
            } catch (GWPortalException e) {
                fail("getNagiosStatistics() failed. GWPortalException details : "
                        + e);
            } catch (WSDataUnavailableException e) {
                fail("getNagiosStatistics() failed. WSDataUnavailableException details : "
                        + e);
            }

            if (property == null || property.length == 0) {
                fail("Failed to retrieve Nagios Statistics from getNagiosStatisticsForHostList() method");
            } else {
                assertEquals(
                        "Successfully retrieved nagios statistics for hostList "
                                + hostListString, true, property.length > 0);
            }
        }
    }

    /**
     * This method tests getNagiosStatisticsForHostGroup() API for query type
     * HOSTGROUPID.
     */
    public final void testGetNagiosStatisticsForHostGroup() {

        HostGroup[] hostGroups = null;
        try {
            hostGroups = foundationWSFacade.getAllHostGroups();
        } catch (GWPortalException e1) {
            fail("testGetNagiosStatisticsForHostGroup(): getAllHostGroups() failed. GWPortalException details : "
                    + e1);
        } catch (WSDataUnavailableException e1) {
            fail("testGetNagiosStatisticsForHostGroup(): getAllHostGroups() failed. WSDataUnavailableException details : "
                    + e1);
        }

        if (hostGroups == null || hostGroups.length == 0) {
            fail("getAllHostGroups() failed. No hosts groups retrieved.");
        } else {
            String hostGroupName = String.valueOf(hostGroups[1].getName());
            NagiosStatisticProperty[] property = null;
            try {
                property = foundationWSFacade
                        .getNagiosStatisticsForHostGroup(hostGroupName);
            } catch (GWPortalException e) {
                fail("getNagiosStatisticsForHostGroup() failed. GWPortalException details : "
                        + e);
            } catch (WSDataUnavailableException e) {
                fail("getNagiosStatisticsForHostGroup() failed. WSDataUnavailableException details : "
                        + e);
            }
            if (property == null || property.length == 0) {
                fail("Failed to retrieve Nagios Statistics from getNagiosStatisticsForHostGroup() method for Host Group - "
                        + hostGroupName);
            } else {
                assert (true);
            }
        }
    }

    /**
     * Test method for
     * {@link com.groundworkopensource.portal.common.ws.impl.StatisticsWSFacade#getServiceStatisticsForHostGroup(java.lang.String)}
     * .
     */
    public final void testGetServiceStatisticsForHostGroup() {
        HostGroupWSFacade hostGroupFacade = new HostGroupWSFacade();

        HostGroup[] hostGroups = null;
        try {
            hostGroups = hostGroupFacade.getAllHostGroups();
        } catch (GWPortalException e) {
            fail("testGetServiceStatisticsForHostGroup(): getAllHostGroups() failed. GWPortalException details : "
                    + e);
        } catch (WSDataUnavailableException e) {
            fail("testGetServiceStatisticsForHostGroup(): getAllHostGroups() failed. WSDataUnavailableException details : "
                    + e);
        }
        if (hostGroups == null || hostGroups.length == 0) {
            fail("getAllHostGroups() failed. No hosts groups retrieved.");
            return;
        }

        String name = hostGroups[0].getName();
        StatisticProperty[] property = null;
        try {
            property = foundationWSFacade
                    .getServiceStatisticsForHostGroup(name);
        } catch (WSDataUnavailableException e) {
            fail("getServiceStatisticsForHostGroup() failed. WSDataUnavailableException details : "
                    + e);
        }
        if (property == null || property.length == 0) {
            fail("getServiceStatisticsForHostGroup() failed.");
        }

    }

    /**
     * Test method for getHostAvailabilityForHostgroup(String hostGroupName)
     * method in StatisticsWSFacade.
     */
    public final void testGetHostAvailabilityForHostgroup() {
        // get host group name
        HostGroupWSFacade hostGroupFacade = new HostGroupWSFacade();
        HostGroup[] hostGroups = null;
        try {
            hostGroups = hostGroupFacade.getAllHostGroups();
        } catch (GWPortalException e) {
            fail("testGetHostAvailabilityForHostgroup(): getAllHostGroups() failed. GWPortalException details : "
                    + e);
        } catch (WSDataUnavailableException e) {
            fail("testGetHostAvailabilityForHostgroup(): getAllHostGroups() failed. WSDataUnavailableException details : "
                    + e);
        }
        if (hostGroups == null || hostGroups.length == 0) {
            fail("getAllHostGroups() failed. No hosts groups retrieved.");
            return;
        }

        // test getHostAvailabilityForHostgroup method
        String hostGroupName = hostGroups[0].getName();
        double hostAvailabilityForHostgroup = foundationWSFacade
                .getHostAvailabilityForHostgroup(hostGroupName);
        if (hostAvailabilityForHostgroup == 0D) {
            fail("Failed to retrieve 'Host Availability Statistics for Host Group'.");
        }
    }

    /**
     * Test method for getHostStatisticsForHostgroup(String hostGroupName)
     * method in StatisticsWSFacade.
     */
    public final void testGetHostStatisticsForHostgroup() {
        // get host group name
        HostGroup[] hostGroups = null;
        try {
            hostGroups = foundationWSFacade.getAllHostGroups();
        } catch (GWPortalException e) {
            fail("testGetHostStatisticsForHostgroup(): getAllHostGroups() failed. GWPortalException details : "
                    + e);
        } catch (WSDataUnavailableException e) {
            fail("testGetHostStatisticsForHostgroup(): getAllHostGroups() failed. WSDataUnavailableException details : "
                    + e);
        }

        if (hostGroups == null || hostGroups.length == 0) {
            fail("getAllHostGroups() failed. No hosts groups retrieved.");
            return;
        }

        // test getHostAvailabilityForHostgroup method
        String hostGroupName = hostGroups[0].getName();
        StatisticProperty[] hoststatisticsForHostgroup = null;
        try {
            hoststatisticsForHostgroup = foundationWSFacade
                    .getHostStatisticsForHostGroup(hostGroupName);
        } catch (WSDataUnavailableException e) {
            fail("Failed to retrieve 'Host statistics for Host Group'. WSDataUnavailableException details : "
                    + e);
        }

        if (hoststatisticsForHostgroup == null
                || hoststatisticsForHostgroup.length == 0) {
            fail("Failed to retrieve 'Host statistics for Host Group'.");
        }
    }

    /**
     * Method to test getServiceAvailabilityForServiceGroup()
     */
    public final void testGetServiceAvailabilityForServiceGroup() {

        Category[] serviceGroups = null;
        try {
            serviceGroups = foundationWSFacade.getAllServiceGroups();
        } catch (WSDataUnavailableException e) {
            fail("getAllServiceGroups() failed. WSDataUnavailableException details : "
                    + e);
        } catch (GWPortalException e) {
            fail("getAllServiceGroups() failed. GWPortalException details : "
                    + e);
        }

        if (serviceGroups == null || serviceGroups.length == 0) {
            fail("getAllServiceGroups() failed. No service groups retrieved.");
            return;
        }

        double serviceAvailability = 0;
        try {
            serviceAvailability = foundationWSFacade
                    .getServiceAvailabilityForServiceGroup(serviceGroups[0]
                            .getName());
        } catch (WSDataUnavailableException e) {
            fail("getServiceAvailabilityForServiceGroup() failed. WSDataUnavailableException details : "
                    + e);
        } catch (GWPortalException e) {
            fail("getServiceAvailabilityForServiceGroup() failed. GWPortalException details : "
                    + e);
        }

        if (serviceAvailability == 0) {
            System.out.println("Zero service availability results returned.");
        }
    }

    /**
     * Test method for getServiceAvailabilityForHostgroup(String hostGroupName)
     * method in StatisticsWSFacade.
     */
    public final void testGetServiceAvailabilityForHostgroup() {
        // get host group name
        HostGroup[] hostGroups = null;
        try {
            hostGroups = foundationWSFacade.getAllHostGroups();
        } catch (GWPortalException e) {
            fail("testGetServiceAvailabilityForHostgroup(): getAllHostGroups() failed. GWPortalException details : "
                    + e);
        } catch (WSDataUnavailableException e) {
            fail("testGetServiceAvailabilityForHostgroup(): getAllHostGroups() failed. WSDataUnavailableException details : "
                    + e);
        }
        if (hostGroups == null || hostGroups.length == 0) {
            fail("getAllHostGroups() failed. No hosts groups retrieved.");
            return;
        }

        // test getServiceAvailabilityForHostgroup method
        String hostGroupName = hostGroups[0].getName();
        double serviceAvailabilityForHostgroup = foundationWSFacade
                .getServiceAvailabilityForHostgroup(hostGroupName);
        if (serviceAvailabilityForHostgroup == 0D) {
            fail("Failed to retrieve 'Service Availability Statistics for Host Group'.");
        }
    }

    /**
     * Test method for getServiceGroupStatisticsForEntireNetwork
     */

    public final void testGetServiceGroupStatisticsForEntireNetwork() {
        foundationWSFacade.getServiceGroupStatisticsForEntireNetwork();
        assert (true);
    }

    /**
     * Test method for getServiceStatisticsForServiceGroup
     * 
     */

    public final void testGetServiceStatisticsForServiceGroup() {

        Category[] serviceGroups = null;
        try {
            serviceGroups = foundationWSFacade.getAllServiceGroups();
        } catch (WSDataUnavailableException e) {
            fail("getAllServiceGroups() failed. WSDataUnavailableException details : "
                    + e);
        } catch (GWPortalException e) {
            fail("getAllServiceGroups() failed. GWPortalException details : "
                    + e);
        }

        if (serviceGroups == null) {
            System.out.println("Null service groups returned");
            return;
        }
        this.foundationWSFacade
                .getServiceStatisticsForServiceGroup(serviceGroups[0]
                        .getDescription());
        assert (true);
    }

    /**
     * Test method for getFilteredServiceStatistics()
     */

    public final void testGetFilteredServiceStatistics() {
        Map<String, Long> filteredServiceStatistics = null;

        Filter serviceFilter = new Filter("monitorStatus.name",
                FilterOperator.EQ, "OK");
        try {
            filteredServiceStatistics = foundationWSFacade
                    .getFilteredServiceStatistics(
                            StatisticQueryType.SERVICEGROUP_STATISTICS_BY_FILTER,
                            serviceFilter, null, "NAGIOS");
        } catch (WSDataUnavailableException e) {
            fail("getFilteredServiceStatistics() failed. WSDataUnavailableException details : "
                    + e);
        } catch (GWPortalException e) {
            fail("getFilteredServiceStatistics() failed. GWPortalException details : "
                    + e);
        }
        if (filteredServiceStatistics == null
                || filteredServiceStatistics.size() == 0) {
            System.out.println("Empty filteredServiceStatistics returned");
        }
    }

    /**
     * Negative test method for getFilteredServiceStatistics()
     * 
     */

    public final void testGetFilteredServiceStatisticsNegative() {
        Map<String, Long> filteredServiceStatistics = null;

        Filter serviceFilter = new Filter();
        try {
            filteredServiceStatistics = foundationWSFacade
                    .getFilteredServiceStatistics(
                            StatisticQueryType.SERVICEGROUP_STATISTICS_BY_FILTER,
                            serviceFilter, null, "NAGIOS");
        } catch (WSDataUnavailableException e) {
            assert (true);
            return;
        } catch (GWPortalException e) {
            assert (true);
            return;
        }
        if (filteredServiceStatistics == null
                || filteredServiceStatistics.size() == 0) {
            assert (true);
            return;
        }
        fail("testGetFilteredServiceStatisticsNegative() failed!");
    }

    /**
     * Test method for getNagiosStatisticsForNetwork() in StatisticWSFacade.
     */
    public final void testGetNagiosStatisticsForNetwork() {
        NagiosStatisticProperty[] property = null;
        try {
            property = foundationWSFacade.getNagiosStatisticsForNetwork();
        } catch (GWPortalException e) {
            fail("getNagiosStatisticsForNetwork() failed. GWPortalException details : "
                    + e);
        } catch (WSDataUnavailableException e) {
            fail("getNagiosStatisticsForNetwork() failed. WSDataUnavailableException details : "
                    + e);
        }
        if (property == null || property.length == 0) {
            fail("Failed to retrieve NAGIOS SYSTEM Statistics from getNagiosStatistics() foe Entire network.");
        } else {
            assert (true);
        }
    }

    /**
     * This method tests getNagiosStatisticsForHostGroup() API for query type
     * SERVICEGROUPNAME.
     */
    public final void testGetNagiosStatisticsForServiceGroup() {

        CategoryWSFacade categoryWSFacade = new CategoryWSFacade();
        Category[] categories = new Category[] {};
        try {
            categories = categoryWSFacade.getAllServiceGroups();
        } catch (WSDataUnavailableException e1) {
            fail();
        } catch (GWPortalException e1) {
            fail();
        }

        if (categories == null || categories.length == 0) {
            fail("Failed to retrieve SERVICE GROUPS from hostGroupFacade.getAllHostGroups() for testGetNagiosStatisticsForServiceGroup() method.");
        } else {
            String serviceGroupName = String.valueOf(categories[0].getName());
            NagiosStatisticProperty[] property = null;
            try {
                property = foundationWSFacade
                        .getNagiosStatisticsForServiceGroup(serviceGroupName);
            } catch (GWPortalException e) {
                fail("getNagiosStatisticsForServiceGroup() failed. GWPortalException details : "
                        + e);
            } catch (WSDataUnavailableException e) {
                fail("getNagiosStatisticsForServiceGroup() failed. WSDataUnavailableException details : ");
            }

            if ((property == null) || (property.length == 0)) {
                fail("Failed to retrieve Nagios Statistics from StatisticsWSFacade.getNagiosStatisticsForServiceGroup() method for service group  "
                        + serviceGroupName);
            } else {
                assert (true);
            }
        }
    }

    /**
     * Test case for getHostStatisticsForHostGroupByHostgroupId
     */
    public final void testGetHostStatisticsForHostGroupByHostgroupId() {

        HostGroup[] hostGroups = null;
        try {
            hostGroups = foundationWSFacade.getAllHostGroups();
        } catch (WSDataUnavailableException exc) {
            fail("getAllHostGroups() failed");
        } catch (GWPortalException exc) {
            fail("getAllHostGroups() failed");
        }

        if (hostGroups == null || hostGroups.length == 0) {
            fail("Failed to retrieve HOST GROUPS from getAllHostGroups() for testGetHostStatisticsForHostGroupByHostgroupId() method.");
        } else {
            int id = hostGroups[0].getHostGroupID();
            try {
                foundationWSFacade
                        .getHostStatisticsForHostGroupByHostgroupId(Integer
                                .toString(id));
            } catch (WSDataUnavailableException e) {
                fail("getHostStatisticsForHostGroupByHostgroupId() failed");
            }
        }
    }

    /**
     * Test case for getHostStatisticsForHostGroupByHostgroupName()
     */
    public final void testGetHostStatisticsForHostGroupByHostgroupName() {

        HostGroup[] hostGroups = null;
        try {
            hostGroups = foundationWSFacade.getAllHostGroups();

        } catch (WSDataUnavailableException exc) {
            fail("getAllHostGroups() failed");
        } catch (GWPortalException exc) {
            fail("getAllHostGroups() failed");
        }

        if (hostGroups == null || hostGroups.length == 0) {
            fail("Failed to retrieve HOST GROUPS from getAllHostGroups() for testGetHostStatisticsForHostGroupByHostgroupId() method.");
        } else {
            try {
                foundationWSFacade
                        .getHostStatisticsForHostGroupByHostgroupName(hostGroups[0]
                                .getName());
            } catch (WSDataUnavailableException e) {
                fail("getHostStatisticsForHostGroupByHostgroupName() failed");
            }
        }
    }

    /**
     * This method test the Entire network Host group statistics.if web service
     * map is empty or null i.e. test case if failed other wise pass.
     */
    public final void testGetEntireNetworkHostGroupStatistics() {
        // create map with parameter String and long which hold monitor status
        // as key and statistics as a value.
        Map<String, Long> hostGroupStatisticsMap = null;
        // get host group statistics
        try {
            hostGroupStatisticsMap = foundationWSFacade
                    .getEntireNetworkHostGroupStatistics();
        } catch (WSDataUnavailableException e) {
            fail("Failed to retrieve Host group statistics ");
        }
        if (hostGroupStatisticsMap == null || hostGroupStatisticsMap.isEmpty()) {
            fail("Failed to retrieve Host group statistics ");
        } else {
            assert (true);
        }

    }

    /**
     * This method test the Entire network Host statistics.if web service map is
     * empty or null i.e. test case is failed other wise pass.
     */
    public final void testGetEntireNetworkHostStatistics() {
        // create map with parameter String and long which hold monitor status
        // as key and statistics as a value.
        Map<String, Long> hostStatisticsMap = null;
        // get host statistics
        try {
            hostStatisticsMap = foundationWSFacade
                    .getEntireNetworkHostStatistics();
        } catch (GWPortalException e) {
            fail("Failed to retrieve Host statistics ");

        } catch (WSDataUnavailableException e) {
            fail("Failed to retrieve Host statistics ");

        }

        if (hostStatisticsMap == null || hostStatisticsMap.isEmpty()) {
            fail("Failed to retrieve Host statistics ");
        } else {
            assert (true);
        }

    }

    /**
     * This method test the Entire network Service statistics.if web service map
     * is empty or null i.e. test case is failed other wise pass.
     */
    public final void testGetEntireNetworkServiceStatistics() {
        // create map with parameter String and long which hold monitor status
        // as key and statistics as a value.
        Map<String, Long> serviceStatisticsMap = null;
        // get service statistics
        try {
            serviceStatisticsMap = foundationWSFacade
                    .getEntireNetworkServiceStatistics();
        } catch (GWPortalException e) {
            fail("Failed to retrieve service statistics ");
        } catch (WSDataUnavailableException e) {
            fail("Failed to retrieve service statistics ");
        }

        if (serviceStatisticsMap == null || serviceStatisticsMap.isEmpty()) {
            fail("Failed to retrieve service statistics ");
        } else {
            assert (true);
        }

    }

    /**
     * test get filter host statistics by passing comma separated host name
     */
    public final void testGetFilteredStatisticsForHost() {
        Map<String, Long> filteredStatisticsForHost = null;
        if (foundationWSFacade == null) {
            fail("Failed to retrieve getFilteredStatisticsForHost due to statisticWSFacade is null  ");
        }
        try {
            filteredStatisticsForHost = foundationWSFacade
                    .getFilteredStatisticsForHost("localhost");
        } catch (GWPortalException e) {
            fail("Failed to retrieve getFilteredStatisticsForHost due to GWPortalException  ");
        } catch (WSDataUnavailableException e) {
            fail("Failed to retrieve getFilteredStatisticsForHost due to WSDataUnavailableException  ");
        }
        if (filteredStatisticsForHost == null
                || filteredStatisticsForHost.isEmpty()) {
            fail("Failed to retrieve getFilteredStatisticsForHost due to filtered Statistics For Host is null or empty  ");
        }
    }

    /**
     * test getGroupStatistics if filter applied .
     */
    public final void testGetGroupStatisticsfForHostGroup() {
        Filter filter = new Filter(
                TestConstants.HOSTS_HOST_STATUS_HOST_MONITOR_STATUS_NAME,
                FilterOperator.EQ, TestConstants.UP);
        Map<String, Long> statisticsMap = null;
        if (foundationWSFacade == null) {
            fail(FAILED_TO_RETRIEVE_SERVICE_GROUP_STATISTICS);
        }
        try {
            statisticsMap = foundationWSFacade.getGroupStatisticsForHostGroup(
                    StatisticQueryType.HOSTGROUP_STATISTICS_BY_FILTER, filter,
                    null, TestConstants.NAGIOS);
        } catch (WSDataUnavailableException e) {
            fail(STATISTIC_PROPERTY_IS_NULL);
        }
        if (statisticsMap == null || statisticsMap.isEmpty()) {
            fail(STATISTIC_PROPERTY_IS_NULL);
        }

    }

    /**
     * test getGroupStatistics if filter applied .
     */
    public final void testGetGroupStatisticsForServiceGroup() {
        Filter filter = new Filter(TestConstants.MONITOR_STATUS_NAME,
                FilterOperator.EQ, TestConstants.OK);
        Map<String, Long> statisticsMap = null;
        if (foundationWSFacade == null) {
            fail(FAILED_TO_RETRIEVE_SERVICE_GROUP_STATISTICS);
        }
        try {
            statisticsMap = foundationWSFacade
                    .getGroupStatisticsForServicegGroup(
                            StatisticQueryType.SERVICEGROUP_STATISTICS_BY_FILTER,
                            filter, null, TestConstants.NAGIOS);
        } catch (GWPortalException e) {
            fail(FAILED_TO_RETRIEVE_SERVICE_GROUP_STATISTICS);
        } catch (WSDataUnavailableException e) {
            fail(FAILED_TO_RETRIEVE_SERVICE_GROUP_STATISTICS);
        }
        if (statisticsMap == null || statisticsMap.isEmpty()) {
            fail(STATISTIC_PROPERTY_IS_NULL);
        }

    }

    /**
     * test getGroupStatistics if filter applied .
     */
    public final void testGetEntireNetworkHostStatisticsAllHost() {

        Map<String, Long> statisticsMap = null;
        if (foundationWSFacade == null) {
            fail(FAILED_TO_RETRIEVE_SERVICE_GROUP_STATISTICS);
        }
        try {
            statisticsMap = foundationWSFacade.getEntireNetworkHostStatistics();
        } catch (GWPortalException e) {
            fail(FAILED_TO_RETRIEVE_SERVICE_GROUP_STATISTICS);
        } catch (WSDataUnavailableException e) {
            fail(FAILED_TO_RETRIEVE_SERVICE_GROUP_STATISTICS);
        }
        if (statisticsMap == null || statisticsMap.isEmpty()) {
            fail(STATISTIC_PROPERTY_IS_NULL);
        }

    }

    /**
     * Test method for
     * {@link com.groundworkopensource.portal.common.ws.impl.StatisticsWSFacade#getServiceStatisticsByHostGroupId(java.lang.String)}
     * .
     */
    public final void testGetServiceStatisticsbyHostGroupId() {

        HostGroup[] hostGroups = null;
        try {
            hostGroups = foundationWSFacade.getAllHostGroups();
        } catch (GWPortalException e) {
            fail("Failed to retrieve HOST GROUPS from hostGroupFacade.getAllHostGroups() for statisticWSFacade.getServiceStatisticsByHostGroupId method. See error logs for errors; if any");
        } catch (WSDataUnavailableException e) {
            fail("Failed to retrieve HOST GROUPS from hostGroupFacade.getAllHostGroups() for statisticWSFacade.getServiceStatisticsByHostGroupId method. See error logs for errors; if any");
        }

        if (hostGroups == null || hostGroups.length == 0) {
            fail("Failed to retrieve HOST GROUPS from hostGroupFacade.getAllHostGroups() for statisticWSFacade.getServiceStatisticsByHostGroupId method. See error logs for errors; if any");
            return;
        }

        int hostGroupId = hostGroups[0].getHostGroupID();
        Map<String, Long> serviceStatistics = null;
        try {
            serviceStatistics = foundationWSFacade
                    .getServiceStatisticsByHostGroupId(Integer
                            .toString(hostGroupId));
        } catch (GWPortalException e) {
            fail("Failed to retrieve ALL HOST Statistics from from StatisticsWSFacade.getServiceStatisticsByHostGroupId() method. See error logs for errors; if any");
        } catch (WSDataUnavailableException e) {
            fail("Failed to retrieve ALL HOST Statistics from from StatisticsWSFacade.getServiceStatisticsByHostGroupId() method. See error logs for errors; if any");
        }

        if (serviceStatistics == null || serviceStatistics.isEmpty()) {
            fail("Failed to retrieve ALL HOST Statistics from from StatisticsWSFacade.getServiceStatisticsByHostGroupId() method. See error logs for errors; if any");
        }

    }

    /**
     * 
     */
    public final void testGetServiceStatisticsByHostGroupName() {
        HostGroup[] hostGroups = null;
        try {
            hostGroups = foundationWSFacade.getAllHostGroups();
        } catch (GWPortalException e) {
            fail("Failed to retrieve HOST GROUPS from hostGroupFacade.getAllHostGroups() for statisticWSFacade.getServiceStatisticsByHostGroupId method. See error logs for errors; if any");
        } catch (WSDataUnavailableException e) {
            fail("Failed to retrieve HOST GROUPS from hostGroupFacade.getAllHostGroups() for statisticWSFacade.getServiceStatisticsByHostGroupId method. See error logs for errors; if any");
        }

        if (hostGroups == null || hostGroups.length == 0) {
            fail("Failed to retrieve HOST GROUPS from hostGroupFacade.getAllHostGroups() for statisticWSFacade.getServiceStatisticsByHostGroupId method. See error logs for errors; if any");
            return;
        }

        String hostGroupName = hostGroups[0].getName();
        Map<String, Long> serviceStatistics = null;

        try {
            serviceStatistics = foundationWSFacade
                    .getServiceStatisticsByHostGroupName(hostGroupName);
        } catch (GWPortalException e) {
            fail("Failed to retrieve ALL HOST Statistics from from StatisticsWSFacade.getServiceStatisticsByHostGroupName() method. See error logs for errors; if any");
        } catch (WSDataUnavailableException e) {
            fail("Failed to retrieve ALL HOST Statistics from from StatisticsWSFacade.getServiceStatisticsByHostGroupName() method. See error logs for errors; if any");
        }

        if (serviceStatistics == null || serviceStatistics.isEmpty()) {
            fail("Failed to retrieve ALL HOST Statistics from from StatisticsWSFacade.getServiceStatisticsByHostGroupId() method. See error logs for errors; if any");
        }

    }

    /**
     * Test method for
     * {@link com.groundworkopensource.portal.common.ws.impl.StatisticsWSFacade#getServiceStatisticsByHostName(java.lang.String)}
     * .
     */
    public final void testGetServiceStatisticsbyHostName() {
        Host[] host = null;
        try {
            host = foundationWSFacade.getAllHosts();
        } catch (WSDataUnavailableException e) {
            fail("Failed to retrieve HOST GROUPS from HostWSFacade.getAllHosts() for statisticWSFacade.getServiceStatisticsByHostName method. See error logs for errors; if any");
        } catch (GWPortalException e) {
            fail("Failed to retrieve HOST GROUPS from HostWSFacade.getAllHosts() for statisticWSFacade.getServiceStatisticsByHostName method. See error logs for errors; if any");
        }

        if (host == null || host.length == 0) {
            fail("Failed to retrieve HOST GROUPS from HostWSFacade.getAllHosts() for statisticWSFacade.getServiceStatisticsByHostName method. See error logs for errors; if any");
            return;
        }

        String hostName = host[0].getName();
        Map<String, Long> serviceStatistics = null;
        try {
            serviceStatistics = foundationWSFacade
                    .getServiceStatisticsByHostName(hostName);
        } catch (GWPortalException e) {
            fail("Failed to retrieve ALL HOST Statistics from from StatisticsWSFacade.getServiceStatisticsByHostName() method. See error logs for errors; if any. GWPortalException details : "
                    + e);
        } catch (WSDataUnavailableException e) {
            fail("Failed to retrieve ALL HOST Statistics from from StatisticsWSFacade.getServiceStatisticsByHostName() method. See error logs for errors; if any. WSDataUnavailableException details : "
                    + e);
        }

        if (serviceStatistics == null || serviceStatistics.isEmpty()) {
            fail("Failed to retrieve ALL HOST Statistics from from StatisticsWSFacade.getServiceStatisticsByHostName() method. See error logs for errors; if any. ");
        }

    }

    /**
     * Test method for
     * {@link com.groundworkopensource.portal.common.ws.impl.StatisticsWSFacade#getServiceStatisticsByServiceGroupName(java.lang.String)}
     * .
     */
    public final void testGetServiceStatisticsbyServiceGroupName() {
        Category[] serviceGroupNames = null;
        try {
            serviceGroupNames = foundationWSFacade.getAllServiceGroups();
        } catch (WSDataUnavailableException e1) {
            fail("testGetServiceStatisticsbyServiceGroupName(): getAllServiceGroups failed. WSDataUnavailableException details : "
                    + e1);
        } catch (GWPortalException e1) {
            fail("testGetServiceStatisticsbyServiceGroupName(): getAllServiceGroups failed. GWPortalException details : "
                    + e1);
        }

        if (serviceGroupNames == null || serviceGroupNames.length == 0) {
            fail("No service groups retrieved from getAllServiceGroups.");
            return;
        }

        String serviceGroupName = serviceGroupNames[0].getName();
        Map<String, Long> serviceStatistics = null;
        try {
            serviceStatistics = foundationWSFacade
                    .getServiceStatisticsByServiceGroupName(serviceGroupName);
        } catch (GWPortalException e) {
            fail("getServiceStatisticsByServiceGroupName() failed. GWPortalException details : "
                    + e);
        } catch (WSDataUnavailableException e) {
            fail("getServiceStatisticsByServiceGroupName failed. WSDataUnavailableException details : "
                    + e);
        }
        if (serviceStatistics == null || serviceStatistics.isEmpty()) {
            fail("Failed to retrieve ALL HOST Statistics from from StatisticsWSFacade.getServiceStatisticsByServiceGroupName() method. See error logs for errors; if any");
        }

    }

    /**
     * Test case for getServiceStatisticsByServiceIds
     */
    public final void testGetServiceStatisticsByServiceIds() {
        ServiceStatus[] serviceStatusArr = null;

        HostGroup[] hostGroups = null;
        try {
            hostGroups = foundationWSFacade.getAllHostGroups();
        } catch (GWPortalException e) {
            fail("testGetServiceStatisticsByServiceIds(): getAllHostGroups() failed. GWPortalException details "
                    + e);
        } catch (WSDataUnavailableException e) {
            fail("testGetServiceStatisticsByServiceIds(): getAllHostGroups() failed. WSDataUnavailableException details "
                    + e);
        }

        if (hostGroups == null || hostGroups.length == 0) {
            fail("getAllHostGroups(): No host groups returned.");
            return;
        }

        int hostGroupId = hostGroups[0].getHostGroupID();

        Filter leftfilter = new Filter(
                TestConstants.HOST_HOST_GROUPS_HOST_GROUP_ID,
                FilterOperator.EQ, hostGroupId);
        Filter rightfilter = new Filter(TestConstants.MONITOR_STATUS_NAME,
                FilterOperator.EQ, TestConstants.OK);
        Filter filter = Filter.AND(leftfilter, rightfilter);

        try {
            serviceStatusArr = foundationWSFacade.getServicesbyCriteria(filter);
        } catch (GWPortalException e) {
            fail("getServicesbyCriteria() failed. Failed to retrieve service statistics. GWPortalException details : "
                    + e);
        } catch (WSDataUnavailableException e) {
            fail("getServicesbyCriteria() failed. Failed to retrieve service statistics. WSDataUnavailableException details : "
                    + e);
        }
        if (serviceStatusArr == null) {
            fail("Failed to retrieve service statistics. ServiceStatus returned is null.");
        }
        // building comma separated service id list
        // create String for service status ID
        StringBuffer serviceIdBuilder = new StringBuffer(
                CommonConstants.EMPTY_STRING);
        if (serviceStatusArr != null) {
            // creating comma Separated service Status ID String
            for (int i = 0; i < serviceStatusArr.length; i++) {
                int servicestatusid = serviceStatusArr[i].getServiceStatusID();
                serviceIdBuilder.append(servicestatusid);
                serviceIdBuilder.append(CommonConstants.COMMA);

            }
        }
        int lastcommaindex = serviceIdBuilder
                .lastIndexOf(CommonConstants.COMMA);
        // remove last comma
        String serviceStatusIds = serviceIdBuilder.substring(0,
                lastcommaindex - 1);

        Map<String, Long> statisticsMap = null;
        try {
            statisticsMap = foundationWSFacade
                    .getServiceStatisticsByServiceIds(serviceStatusIds);
        } catch (WSDataUnavailableException e) {
            fail("getServiceStatisticsByServiceIds() failed. WSDataUnavailableException details : "
                    + e);
        }
        if (statisticsMap == null || statisticsMap.isEmpty()) {
            fail(STATISTIC_PROPERTY_IS_NULL);
        }
    }

    /**
     * This method test the Entire network Service Group statistics.if web
     * service map is empty or null i.e. test case is failed other wise pass.
     */
    public final void testGetEntireNetworkServiceGroupStatistics() {
        // create map with parameter String and long which hold monitor status
        // as key and statistics as a value.
        Map<String, Long> serviceGroupStatisticsMap = null;
        if (foundationWSFacade == null) {
            fail(FAILED_TO_RETRIEVE_SERVICE_GROUP_STATISTICS);
        }
        // get service statistics
        try {
            serviceGroupStatisticsMap = foundationWSFacade
                    .getEntireNetworkServiceGroupStatistics();
        } catch (WSDataUnavailableException e) {
            fail("getEntireNetworkServiceGroupStatistics() failed. WSDataUnavailableException details "
                    + e);
        } catch (GWPortalException e) {
            fail("getEntireNetworkServiceGroupStatistics() failed. GWPortalException details : "
                    + e);
        }
        if (serviceGroupStatisticsMap == null
                || serviceGroupStatisticsMap.isEmpty()) {
            fail("Failed to retrieve service group statistics ");
        } else {
            assert (true);
        }
    }

    /**
     * test GetFilteredHostGroupName if filter applied .
     */
    public final void testGetFilteredHostGroupName() {
        Filter filter = new Filter(
                TestConstants.HOSTS_HOST_STATUS_HOST_MONITOR_STATUS_NAME,
                FilterOperator.EQ, TestConstants.UP);

        if (foundationWSFacade == null) {
            fail(FAILED_TO_RETRIEVE_SERVICE_GROUP_STATISTICS);
        }
        try {
            foundationWSFacade.getFilteredHostGroupName(
                    StatisticQueryType.HOSTGROUP_STATISTICS_BY_FILTER, filter,
                    null, TestConstants.NAGIOS);
        } catch (WSDataUnavailableException e) {
            fail(STATISTIC_PROPERTY_IS_NULL);
        }

    }

}
