
package com.groundworkopensource.portal.common.ws.impl.test;

import junit.framework.TestCase;

import org.groundwork.foundation.ws.model.impl.Filter;
import org.groundwork.foundation.ws.model.impl.FilterOperator;
import org.groundwork.foundation.ws.model.impl.Host;
import org.groundwork.foundation.ws.model.impl.HostGroup;
import org.groundwork.foundation.ws.model.impl.SimpleHost;
import org.groundwork.foundation.ws.model.impl.WSFoundationCollection;

import com.groundworkopensource.portal.common.FilterConstants;
import com.groundworkopensource.portal.common.exception.GWPortalException;
import com.groundworkopensource.portal.common.exception.GWPortalGenericException;
import com.groundworkopensource.portal.common.ws.IWSFacade;
import com.groundworkopensource.portal.common.ws.WebServiceFactory;
import com.groundworkopensource.portal.common.ws.WebServiceFactory.WebServiceType;
import com.groundworkopensource.portal.common.ws.impl.HostGroupWSFacade;

/**
 * Test Class for HostWSFacade.
 * 
 * @author swapnil_gujrathi
 */
public class HostWSFacadeTest extends TestCase {

    /**
     * Host monitor status "UNREACHABLE" constant.
     */
    private static final String STATUS_UNREACHABLE = "UNREACHABLE";
    /**
     * Host monitor status "PENDING" constant.
     */
    private static final String STATUS_PENDING = "PENDING";
    /**
     * Host monitor status "DOWN" constant.
     */
    private static final String STATUS_DOWN = "DOWN";
    /**
     * Host monitor status "UP" constant.
     */
    private static final String STATUS_UP = "UP";
    /**
     * foundationWSFacade Object to call web services.
     */
    private IWSFacade foundationWSFacade;

    /**
     * (non-Javadoc).
     * 
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected final void setUp() throws Exception {
        foundationWSFacade = new WebServiceFactory()
                .getWebServiceInstance(WebServiceType.FOUNDATION_WEBSERVICE);
    }

    /**
     * (non-Javadoc).
     * 
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected final void tearDown() throws Exception {
        foundationWSFacade = null;
    }

    /**
     * Test case for testing getSimpleHostByName()
     */
    public final void testGetSimpleHostByName() {
        Host[] hosts = null;
        try {
            hosts = foundationWSFacade.getAllHosts();
        } catch (GWPortalGenericException e) {
            fail("getAllHosts() failed. GWPortalGenericException details : "
                    + e);
        }
        if (hosts == null || hosts.length == 0) {
            fail("getHostsByHostName(): Failed to get any host from getAllHosts() method");

        } else {
            Host host = hosts[0];
            String hostName = host.getName();
            try {
                SimpleHost simpleHost = foundationWSFacade.getSimpleHostByName(
                        hostName, true);
                assertEquals(hostName, simpleHost.getName());
            } catch (GWPortalGenericException e) {
                fail("getHostsByName() failed. GWPortalGenericException details : "
                        + e);
            }
        }

    }

    /**
     * Test case for testing getAllHosts()
     */
    public final void testGetAllHosts() {
        Host[] hosts = null;
        try {
            hosts = foundationWSFacade.getAllHosts();
        } catch (GWPortalGenericException e) {
            fail("getAllHosts() failed! " + e);
        }
        if (hosts == null || hosts.length == 0) {
            fail("HostWSFacade.getAllHosts() method returns no hosts");
        }
    }

    /**
     * Test case for testing getSimpleHosts()
     */
    public final void testGetSimpleHosts() {
        SimpleHost[] hosts = null;
        try {
            hosts = foundationWSFacade.getSimpleHosts();
        } catch (GWPortalGenericException e) {
            fail("getAllHosts() failed. GWPortalGenericException details : "
                    + e);
        }
        if (hosts == null || hosts.length == 0) {
            fail("HostWSFacade.getSimpleHosts() method returns no hosts");
        }
    }

    /**
     * Test case for testing getHostsByName()
     */
    public final void testGetHostsByName() {
        Host[] hosts = null;
        try {
            hosts = foundationWSFacade.getAllHosts();
        } catch (GWPortalGenericException e) {
            fail("getAllHosts() failed. GWPortalGenericException details : "
                    + e);
        }
        if (hosts == null || hosts.length == 0) {
            fail("getHostsByHostName(): Failed to get any host from getAllHosts() method");

        } else {
            Host host = hosts[0];
            String hostName = host.getName();
            try {
                Host hostsByHostName = foundationWSFacade
                        .getHostsByName(hostName);
                assertEquals(hostName, hostsByHostName.getName());
            } catch (GWPortalGenericException e) {
                fail("getHostsByName() failed. GWPortalGenericException details : "
                        + e);
            }
        }

    }

    /**
     * Negative test case for testing getHostsByName()
     */
    public final void testGetHostsByNameNegative() {
        String hostName = null;
        Host hostsByHostName = null;
        try {
            hostsByHostName = foundationWSFacade.getHostsByName(hostName);
        } catch (GWPortalGenericException e) {
            System.out
                    .println("GWPortalGenericException thrown in getHostsByName() for incorrect[Null] name");
            assert (true);
            return;
        }
        if (hostsByHostName == null) {
            System.out.println("Null hosts returned");
            assert (true);
            return;
        }
        fail("getHostsByName() failed. Method must fail on incorrect host name parameter.");
    }

    /**
     * Test case for testing getHostsById()
     */
    public final void testGetHostsById() {
        try {
            Host[] hosts = null;
            try {
                hosts = foundationWSFacade.getAllHosts();
            } catch (GWPortalException e) {
                fail("getAllHosts() failed. GWPortalException details :  " + e);
            }
            if (hosts == null || hosts.length == 0) {
                fail("testGetHostsByHostId(): Failed to get any host from getAllHosts() method");
                return;
            }
            Host host = hosts[0];
            int hostID = host.getHostID();
            Host hostByID = foundationWSFacade.getHostsById(String
                    .valueOf(hostID));
            System.out.println("Host ID is : " + hostID);
            assertEquals(hostID, hostByID.getHostID());
        } catch (GWPortalGenericException e) {
            assertFalse(
                    "WSDataUnavailableException - Host with specified ID is not available. Exception - "
                            + e.getMessage(), false);
        }
    }

    /**
     * Negative test case for testing getHostsById()
     */
    public final void testGetHostsByIdNegative() {
        String hostId = null;
        Host hostsByHostName = null;
        try {
            hostsByHostName = foundationWSFacade.getHostsById(hostId);
        } catch (GWPortalGenericException e) {
            System.out
                    .println("GWPortalGenericException thrown in getHostsById()");
            assert (true);
            return;
        }
        if (hostsByHostName == null) {
            assert (true);
            return;
        }
        fail("getHostsById() failed. Method must fail on incorrect host ID parameter.");
    }

    /**
     * Test case for testing getHostsUnderHostGroup()
     */
    public final void testGetHostsUnderHostGroup() {
        HostGroupWSFacade hostGroupWSFacade = new HostGroupWSFacade();
        HostGroup[] allHostGroups = null;
        try {
            allHostGroups = hostGroupWSFacade.getAllHostGroups();
        } catch (GWPortalGenericException e) {
            fail("getAllHostGroups() failed. GWPortalGenericException details : "
                    + e);
        }
        if (allHostGroups == null || allHostGroups.length == 0) {
            fail("getHostsUnderHostGroup(): Failed to get any host-groups from getAllHostGroups()");
            return;
        }
        HostGroup hostGroup = allHostGroups[0];
        Host[] hosts = hostGroup.getHosts();
        System.out.println("Host group name to be checked is : "
                + hostGroup.getName());
        int hostsLength = hosts.length;
        SimpleHost[] hostsUnderHostGroup = null;
        try {
            hostsUnderHostGroup = foundationWSFacade.getHostsUnderHostGroup(
                    hostGroup.getName(), false);
        } catch (GWPortalGenericException e) {
            fail("getHostsUnderHostGroup() failed. GWPortalGenericException details : "
                    + e);
        }
        if (hostsUnderHostGroup == null || hostsUnderHostGroup.length == 0) {
            fail("getHostsUnderHostGroup() failed to get any hosts");
            return;
        }
        assertEquals(hostsLength, hostsUnderHostGroup.length);
    }

    /**
     * Negative test case for testing getHostsUnderHostGroupNegative()
     */
    public final void testGetHostsUnderHostGroupNegative() {
        String hostGroup = null;
        SimpleHost[] hosts = null;
        try {
            hosts = foundationWSFacade.getHostsUnderHostGroup(hostGroup, false);
        } catch (GWPortalGenericException e) {
            System.out
                    .println("GWPortalGenericException thrown in getHostsUnderHostGroup() for incorrect[null] host group name");
            assert (true);
            return;
        }

        if (hosts == null || hosts.length == 0) {
            assert (true);
            return;
        }
        fail("testGetHostsUnderHostGroupNegative() failed. Method must fail on incorrect host group parameter.");
    }

    /**
     * Test case for testing getHostsUnderHostGroupById()
     */
    public final void testGetHostsUnderHostGroupById() {

        // Get all Host Groups
        HostGroupWSFacade hostGroupWSFacade = new HostGroupWSFacade();
        HostGroup[] allHostGroups = null;
        try {
            allHostGroups = hostGroupWSFacade.getAllHostGroups();
        } catch (GWPortalGenericException e) {
            fail("getAllHostGroups() failed. GWPortalGenericException details : ");
        }

        if (allHostGroups == null || allHostGroups.length == 0) {
            fail("failed to get any host-groups from getAllHostGroups() method to check getHostsUnderHostGroupById() method");
            return;
        }
        HostGroup hostGroup = allHostGroups[0];
        // Get Hosts under the particular Host Group
        Host[] hosts = hostGroup.getHosts();
        int hostsLength = hosts.length;
        Host[] hostsUnderHostGroup = null;
        try {
            hostsUnderHostGroup = foundationWSFacade
                    .getHostsUnderHostGroupById(String.valueOf(hostGroup
                            .getHostGroupID()));
        } catch (GWPortalGenericException e) {
            fail("getHostsUnderHostGroupById() failed. GWPortalGenericException details :  "
                    + e);
        }
        if (hostsUnderHostGroup == null || hostsUnderHostGroup.length == 0) {
            fail("failed to get any hosts from getHostsUnderHostGroupById() method");
            return;
        }

        // Check number of hosts obtained to be same
        assertEquals(hostsLength, hostsUnderHostGroup.length);
    }

    /**
     * Negative test case for testing getHostsUnderHostGroupNegative()
     */
    public final void testGetHostsUnderHostGroupByIdNegative() {
        String hostGroupId = null;
        Host[] hosts = null;
        try {
            hosts = foundationWSFacade.getHostsUnderHostGroupById(hostGroupId);
        } catch (GWPortalGenericException e) {
            System.out
                    .println("GWPortalGenericException thrown in getHostsUnderHostGroupById() for incorrect ID");
            assert (true);
            return;
        }

        if (hosts == null) {
            assert (true);
            return;
        }
        fail("getHostsUnderHostGroupById() failed! Method must fail on incorrect host group ID parameter.");
    }

    /**
     * Method tests testGetHostsbyCriteria() with all parameters.
     */
    public final void testGetHostsbyCriteriaParameters() {
        WSFoundationCollection hostArr = null;
        Filter filter = null;

        // create filter for Up monitor status
        filter = new Filter(
                FilterConstants.HOST_STATUS_HOST_MONITOR_STATUS_NAME,
                FilterOperator.EQ, STATUS_UP);
        try {
            hostArr = foundationWSFacade.getHostsbyCriteria(filter, null, -1,
                    -1);
        } catch (GWPortalGenericException e) {
            fail("getHostByCriteria() failed. GWPortalGenericException details :  "
                    + e);
        }
        if (hostArr == null) {
            System.out.println("No hosts with monitor status UP");
        }
        assert (true);
    }

    /**
     * Method tests the host for monitor status "UP"
     */
    public final void testGetHostsbyCriteriaForUp() {
        Host[] hostArr = null;

        Filter filter = null;

        // create filter for Up monitor status
        filter = new Filter(
                FilterConstants.HOST_STATUS_HOST_MONITOR_STATUS_NAME,
                FilterOperator.EQ, STATUS_UP);
        try {
            hostArr = foundationWSFacade.getHostsbyCriteria(filter);
        } catch (GWPortalGenericException e) {
            fail("getHostByCriteria() failed. GWPortalGenericException details :  "
                    + e);
        }

        if (hostArr == null) {
            System.out.println("No hosts with monitor status UP");
        }
        assert (true);
    }

    /**
     * Method tests the host for monitor status "DOWN"
     */
    public final void testGetHostsbyCriteriaForDown() {
        Host[] hostArr = null;

        Filter filter = null;

        // create filter for Down monitor status
        filter = new Filter(
                FilterConstants.HOST_STATUS_HOST_MONITOR_STATUS_NAME,
                FilterOperator.EQ, STATUS_DOWN);
        try {
            hostArr = foundationWSFacade.getHostsbyCriteria(filter);
        } catch (GWPortalGenericException e) {
            fail("getHostByCriteria() failed. GWPortalGenericException details : "
                    + e);
        }

        if (hostArr == null) {
            System.out.println("No hosts with monitor status DOWN");
        }
        assert (true);
    }

    /**
     * Method tests the host for monitor status "PENDING"
     */
    public final void testGetHostsbyCriteriaForPending() {
        Host[] hostArr = null;

        Filter filter = null;

        // create filter for Pending monitor status
        filter = new Filter(
                FilterConstants.HOST_STATUS_HOST_MONITOR_STATUS_NAME,
                FilterOperator.EQ, STATUS_PENDING);
        try {
            hostArr = foundationWSFacade.getHostsbyCriteria(filter);
        } catch (GWPortalGenericException e) {
            fail("getHostByCriteria() failed. GWPortalGenericException details :  "
                    + e);
        }

        if (hostArr == null) {
            System.out.println("No hosts with monitor status PENDING");
        }
        assert (true);
    }

    /**
     * Method tests the host for monitor status "UNREACHABLE"
     */
    public final void testGetHostsbyCriteriaForUnreachable() {

        Host[] hostArr = null;
        Filter filter = null;

        // create filter for Unreachable monitor status
        filter = new Filter(
                FilterConstants.HOST_STATUS_HOST_MONITOR_STATUS_NAME,
                FilterOperator.EQ, STATUS_UNREACHABLE);
        try {
            hostArr = foundationWSFacade.getHostsbyCriteria(filter);
        } catch (GWPortalGenericException e) {
            fail("getHostByCriteria() failed. GWPortalGenericException details :  "
                    + e);
        }

        if (hostArr == null || hostArr.length == 0) {
            System.out.println("No hosts with monitor status UNREACHABLE");
        }
        assert (true);
    }

    /**
     * Method tests the host for monitor status "UNREACHABLE"
     */
    public final void testGetHostsbyCriteriaNegative() {

        Host[] hostArr = null;
        Filter filter = new Filter();

        // Pass null filter explicitly
        try {
            hostArr = foundationWSFacade.getHostsbyCriteria(filter);
        } catch (GWPortalGenericException e) {
            assert (true);
            return;
        }
        if (hostArr == null || hostArr.length == 0) {
            assert (true);
            return;
        }
        fail("testGetHostsbyCriteriaNegative failed! Method must fail on passing empty filter");
    }

    /**
     * Method tests getUnscheduledOrScheduledHostCount()
     */
    public final void testGetUnscheduledOrScheduledHostCount() {
        int count = 0;
        // Create Filter
        Filter filter = new Filter(
                FilterConstants.HOST_STATUS_HOST_MONITOR_STATUS_NAME,
                FilterOperator.EQ, STATUS_DOWN);
        try {
            count = foundationWSFacade
                    .getUnscheduledOrScheduledHostCount(filter);
        } catch (GWPortalGenericException e) {
            fail("getHostByCriteria() failed. GWPortalGenericException details : s "
                    + e);
        }
        if (count == 0) {
            System.out
                    .println("Unscheduled/Scheduled Host Count returned as zero.");
        }
    }

    /**
     * Method tests the host for monitor status "UNREACHABLE"
     */
    public final void testGetUnscheduledOrScheduledHostCountNegative() {

        int count;
        Filter filter = new Filter();

        // Pass empty filter explicitly
        try {
            count = foundationWSFacade
                    .getUnscheduledOrScheduledHostCount(filter);
        } catch (GWPortalGenericException e) {
            assert (true);
            return;
        }
        if (count == 0) {
            assert (true);
            return;
        }
        fail("getUnscheduledOrScheduledHostCount failed! Method must fail on passing empty filter");
    }
}
