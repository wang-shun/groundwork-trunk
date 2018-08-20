
package com.groundworkopensource.portal.common.ws.impl.test;

import junit.framework.TestCase;

import org.groundwork.foundation.ws.model.impl.Filter;
import org.groundwork.foundation.ws.model.impl.FilterOperator;
import org.groundwork.foundation.ws.model.impl.HostGroup;

import com.groundworkopensource.portal.common.FilterConstants;
import com.groundworkopensource.portal.common.exception.GWPortalException;
import com.groundworkopensource.portal.common.exception.GWPortalGenericException;
import com.groundworkopensource.portal.common.exception.WSDataUnavailableException;
import com.groundworkopensource.portal.common.ws.IWSFacade;
import com.groundworkopensource.portal.common.ws.WebServiceFactory;
import com.groundworkopensource.portal.common.ws.WebServiceFactory.WebServiceType;

/**
 * 
 * Test Case for HostGroupWSFacade.
 * 
 * @author nitin_jadhav
 * 
 */
public class HostGroupWSFacadeTest extends TestCase {

    /**
     * MESSAGE_FAILED_TO_GET_HOST_GROUPS
     */
    private static final String MESSAGE_FAILED_TO_GET_HOST_GROUPS = "failed to get host groups from getAllHostGroups()";
    /**
     * Host Group monitor status "UNREACHABLE" constant.
     */
    private static final String STATUS_UNREACHABLE = "UNREACHABLE";
    /**
     * Host Group monitor status "PENDING" constant.
     */
    private static final String STATUS_PENDING = "PENDING";
    /**
     * Host Group monitor status "DOWN" constant.
     */
    private static final String STATUS_DOWN = "DOWN";
    /**
     * Host Group monitor status "UP" constant.
     */
    private static final String STATUS_UP = "UP";

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
     * {@link com.groundworkopensource.portal.common.ws.impl.HostGroupWSFacade#getAllHostGroups()}
     * .
     */
    public final void testGetAllHostGroups() {
        HostGroup[] group = null;
        try {
            group = foundationWSFacade.getAllHostGroups();
        } catch (GWPortalGenericException e) {
            fail("getAllHostGroups() failed. GWPortalGenericException details : "
                    + e);
        }
        if (group != null) {
            if (group.length == 0) {
                fail(MESSAGE_FAILED_TO_GET_HOST_GROUPS);
            }
        } else {
            fail(" getAllHostGroups() returned null");
        }
    }

    /**
     * Test method for
     * {@link com.groundworkopensource.portal.common.ws.impl.HostGroupWSFacade#getAllHostGroups()}
     * .
     */
    public final void testGetAllHostGroupsWithDeep() {
        HostGroup[] group = null;
        try {
            group = foundationWSFacade.getAllHostGroups(true);
        } catch (GWPortalGenericException e) {
            fail("getAllHostGroups() failed. GWPortalGenericException details : "
                    + e);
        }
        if (group != null) {
            if (group.length == 0) {
                fail(MESSAGE_FAILED_TO_GET_HOST_GROUPS);
            }
        } else {
            fail(" getAllHostGroups() returned null");
        }
    }

    /**
     * This Testcase test getHostGroupsbyCriteria method for Host group Monitor
     * status UP
     */
    public final void testgetHostGroupsbyCriteriaForUP() {
        Filter filter = new Filter(
                FilterConstants.HOSTGROUP_MONITORSTATUS_NAME,
                FilterOperator.EQ, STATUS_UP);
        HostGroup[] group = null;
        try {
            group = foundationWSFacade.getHostGroupsbyCriteria(filter, null,
                    -1, -1, true);
        } catch (GWPortalGenericException e) {
            fail("getHostGroupsbyCriteria() failed. GWPortalGenericException details : "
                    + e);
        }
        if (group == null || group.length == 0) {
            System.out
                    .println("No host groups exist currently with the monitor status UP");
        }
        assert (true);
    }

    /**
     * This Testcase test getHostGroupsbyCriteria method for Host group Monitor
     * status UP
     */
    public final void testGetHostGroupsbyCriteriaForDown() {
        Filter filter = new Filter(
                FilterConstants.HOSTGROUP_MONITORSTATUS_NAME,
                FilterOperator.EQ, STATUS_DOWN);
        HostGroup[] group = null;
        try {
            group = foundationWSFacade.getHostGroupsbyCriteria(filter, null,
                    -1, -1, true);
        } catch (GWPortalGenericException e) {
            fail("getHostGroupsbyCriteria() failed. GWPortalGenericException details : ");
        }
        if (group == null || group.length == 0) {
            System.out
                    .println("No host groups exist currently with the monitor status DOWN");
        }
        assert (true);
    }

    /**
     * This Testcase test getHostGroupsbyCriteria method for Host group Monitor
     * status UP
     */
    public final void testGetHostGroupsbyCriteriaForPending() {
        Filter filter = new Filter(
                FilterConstants.HOSTGROUP_MONITORSTATUS_NAME,
                FilterOperator.EQ, STATUS_PENDING);
        HostGroup[] group = null;
        try {
            group = foundationWSFacade.getHostGroupsbyCriteria(filter, null,
                    -1, -1, true);
        } catch (GWPortalGenericException e) {
            fail("getHostGroupsbyCriteria() failed. GWPortalGenericException details : "
                    + e);
        }
        if (group == null || group.length == 0) {
            System.out
                    .println("No host groups exist currently with the monitor status PENDING");
        }
        assert (true);
    }

    /**
     * This Testcase test getHostGroupsbyCriteria method for Host group Monitor
     * status UP
     */
    public final void testGetHostGroupsbyCriteriaForUnreachable() {
        Filter filter = new Filter(
                FilterConstants.HOSTGROUP_MONITORSTATUS_NAME,
                FilterOperator.EQ, STATUS_UNREACHABLE);
        HostGroup[] group = null;
        try {
            group = foundationWSFacade.getHostGroupsbyCriteria(filter, null,
                    -1, -1, true);
        } catch (GWPortalGenericException e) {
            fail("getHostGroupsbyCriteria() failed.  GWPortalGenericException details : "
                    + e);
        }
        if (group == null || group.length == 0) {
            System.out
                    .println("No host groups exist currently with the monitor status UNREACHABLE");
        }
        assert (true);
    }

    /**
     * This Testcase test getHostGroupsbyCriteria method for Host group Monitor
     * status UP
     */
    public final void testgetHostGroupsbyCriteriaNegative() {
        Filter filter = new Filter();
        HostGroup[] group = null;
        try {
            group = foundationWSFacade.getHostGroupsbyCriteria(filter, null,
                    -1, -1, true);
        } catch (GWPortalGenericException e) {
            assert (true);
            return;
        }
        if (group == null || group.length == 0) {
            assert (true);
            return;
        }
        fail("testgetHostGroupsbyCriteriaNegative() failed. Method must throw exception for incorrect parameters");
    }

    /**
     * Test case for getHostGroupsById()
     */
    public final void testGetHostGroupsById() {

        HostGroup[] group = null;
        try {
            group = foundationWSFacade.getAllHostGroups();
        } catch (GWPortalGenericException e) {
            fail(" getAllHostGroups() failed. GWPortalGenericException details : "
                    + e);
        }

        if (group == null || group.length == 0) {
            fail(" getAllHostGroups() failed to retrieve any host groups ");
            return;
        }
        HostGroup hostGroup1 = group[0];
        HostGroup hostGroup2 = null;
        try {
            hostGroup2 = foundationWSFacade.getHostGroupsById(hostGroup1
                    .getHostGroupID());
        } catch (WSDataUnavailableException e) {
            fail("getHostGroupsById() failed. WSDataUnavailableException details : "
                    + e);
        } catch (GWPortalException e) {
            fail("getHostGroupsById() failed. GWPortalException details : " + e);
        }
        if (hostGroup2 == null) {
            System.out.println("Null host group returned ");
        }
    }

    /**
     * Negative test case for getHostGroupsById()
     */
    public final void testGetHostGroupsByIdNegative() {

        HostGroup hostGroup2 = null;
        try {
            hostGroup2 = foundationWSFacade.getHostGroupsById(-1);
        } catch (WSDataUnavailableException e) {
            assert (true);
            return;
        } catch (GWPortalException e) {
            assert (true);
            return;
        }
        if (hostGroup2 == null) {
            System.out
                    .println("Null host group returned for getHostGroupsById()");
            return;
        }
        fail("testGetHostGroupsByIdNegative() failed. Method must fail on passing incorrect [negative] ID");
    }

    /**
     * Test case for getHostGroupsByName()
     */
    public final void testGetHostGroupsByName() {
        HostGroup[] group = null;
        try {
            group = foundationWSFacade.getAllHostGroups();
        } catch (GWPortalGenericException e) {
            fail("getAllHostGroups() failed. GWPortalGenericException details : ");
        }

        if (group == null || group.length == 0) {
            fail(" getAllHostGroups() failed to retrieve any host groups ");
            return;
        }
        HostGroup hostGroup1 = group[0];
        HostGroup hostGroup2 = null;

        try {
            hostGroup2 = foundationWSFacade.getHostGroupsByName(hostGroup1
                    .getName());
        } catch (WSDataUnavailableException e) {
            fail("getHostGroupsByName() failed. WSDataUnavailableException details : ");
        } catch (GWPortalException e) {
            fail("getHostGroupsByName() failed. GWPortalGenericException details : ");
        }
        if (hostGroup2 == null) {
            System.out
                    .println("Null host group returned for getHostGroupsByName().");
        }
    }

    /**
     * Negative Test case for getHostGroupsByName()
     */
    public final void testGetHostGroupsByNameNegative() {

        HostGroup hostGroup2 = null;
        try {
            hostGroup2 = foundationWSFacade.getHostGroupsByName("");
        } catch (WSDataUnavailableException e) {
            assert (true);
            return;
        } catch (GWPortalException e) {
            assert (true);
            return;
        }
        if (hostGroup2 == null) {
            assert (true);
            System.out
                    .println("Null host group returned for getHostGroupsByName()");
            return;
        }
        fail("testGetHostGroupsByNameNegative() failed. Method must throw exception on passing incorrect [Empty]  HostGroup name parameter");
    }

    /**
     * Test Case for getEntireNetworkStatisticsbyCriteria()
     */
    public final void testGetEntireNetworkStatisticsbyCriteria() {

        int count = 0;

        Filter filter = new Filter(
                FilterConstants.HOSTGROUP_MONITORSTATUS_NAME,
                FilterOperator.EQ, STATUS_UP);
        try {
            count = foundationWSFacade
                    .getEntireNetworkStatisticsbyCriteria(filter);
        } catch (GWPortalException e) {
            fail("getEntireNetworkStatisticsbyCriteria() failed. GWPortalException details : ");
        } catch (WSDataUnavailableException e) {
            fail("getEntireNetworkStatisticsbyCriteria() failed. WSDataUnavailableException details : ");
        }
        if (count == 0) {
            System.out
                    .println("Entire network statistics by given criteria returns count as zero.");
        }
    }

    /**
     * Negative test case for getEntireNetworkStatisticsbyCriteria()
     */
    public final void testGetEntireNetworkStatisticsbyCriteriaNegative() {
        Filter filter = new Filter();
        try {
            foundationWSFacade.getEntireNetworkStatisticsbyCriteria(filter);
        } catch (GWPortalException e) {
            assert (true);
            return;
        } catch (WSDataUnavailableException e) {
            assert (true);
            return;
        }
        fail("testGetEntireNetworkStatisticsbyCriteriaNegative() failed. Method must throw exception on passing incorrect [Empty] filter parameter");
    }
}
