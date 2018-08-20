
package com.groundworkopensource.portal.common.ws.impl.test;

import com.groundworkopensource.portal.common.exception.GWPortalException;
import com.groundworkopensource.portal.common.exception.WSDataUnavailableException;
import com.groundworkopensource.portal.common.ws.IWSFacade;
import com.groundworkopensource.portal.common.ws.WebServiceFactory;
import com.groundworkopensource.portal.common.ws.WebServiceFactory.WebServiceType;
import junit.framework.TestCase;
import org.groundwork.foundation.ws.model.impl.Action;
import org.groundwork.foundation.ws.model.impl.ActionPerform;
import org.groundwork.foundation.ws.model.impl.Host;
import org.groundwork.foundation.ws.model.impl.StringProperty;
import org.groundwork.foundation.ws.model.impl.WSFoundationCollection;

/**
 * Test class for CommonWSFacade
 * 
 * @author nitin_jadhav
 * 
 */
public class CommonWSFacadeTest extends TestCase {

    /**
     * Event application Type Syslog
     */
    private static final String SYSLOG = "Syslog";
    /**
     * Event application Type SNMPTRAP
     */
    private static final String SNMPTRAP = "SNMPTRAP";
    /**
     * Number of action for Event application Type Nagios.
     */
    private static final int FIVE = 5;
    /**
     * Number Eleven
     */
    private static final int ELEVEN = 11;
    /**
     * Event application Type Nagios
     */
    private static final String NAGIOS = "Nagios";
    /**
     * Number of action for Event application Type System.
     */
    private static final int FOUR = 4;
    /**
     * Event application Type System
     */
    private static final String SYSTEM = "SYSTEM";
    /**
     * Constant - 50 indicating number of results
     */
    private static final int FIFTY = 50;
    /**
     * foundationWSFacade Object to call web services.
     */
    private IWSFacade foundationWSFacade;

    /**
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        foundationWSFacade = new WebServiceFactory()
                .getWebServiceInstance(WebServiceType.FOUNDATION_WEBSERVICE);
    }

    /**
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test method for
     * {@link com.groundworkopensource.portal.common.ws.impl.CommonWSFacade#searchEntity(java.lang.String, int, String, String)}
     * .
     */
    public void testSearchEntity() {
        // Returns list of results for given search entity
        WSFoundationCollection resultCollection;
        try {
            resultCollection = foundationWSFacade.searchEntity("localhost", FIFTY, null, null);
            if (resultCollection == null) {
                System.out
                        .println("searchEntity() call returns null result list");
                fail("Test Failed. For Query \"localhost\" the result is null");
                return;
            }

            int searchResultCount = resultCollection.getTotalCount();
            if (searchResultCount == 0) {
                System.out
                        .println("For Query \"localhost\" to searchEntity() result count is 0");
                return;
            }
            Host[] hosts = resultCollection.getHost();
            if (hosts == null || hosts.length == 0) {
                fail("For Query \"localhost\" to searchEntity() resultant list is null/empty");
                return;
            }
            // Check if localhost is contained in list
            boolean containsLocalHost = false;
            for (Host host : hosts) {
                if (host.getName().contains("localhost")) {
                    containsLocalHost = true;
                }
                assertEquals(containsLocalHost, true);
            }
        } catch (WSDataUnavailableException e) {
            fail("testSearchEntity() failed. WSDataUnavailableException details : "
                    + e);
        } catch (GWPortalException e) {
            fail("testSearchEntity() failed. GWPortalException details : " + e);
        }
    }

    /**
     * Negative test case for performActions()
     */
    public void testPerformActionsNegative() {

        ActionPerform[] actionPerform = new ActionPerform[1];
        int actionID = 0;
        StringProperty[] parameters = new StringProperty[ELEVEN];
        actionPerform[0] = new ActionPerform(actionID, parameters);

        WSFoundationCollection wsfoundationCollection = null;
        try {
            wsfoundationCollection = foundationWSFacade
                    .performActions(actionPerform);
        } catch (GWPortalException e) {
            assert (true);
            return;
        } catch (WSDataUnavailableException e) {
            assert (true);
            return;
        }
        if (wsfoundationCollection == null) {
            System.out
                    .println("Null collection returned for incorrect performActions() parameters.");
            assert (true);
            return;
        }
        fail("testPerformActions() failed. Method must fail on passing incorrect parameters.");
    }

    /**
     * Test case for getActionsByApplicationType for 'all open events' for
     * SYSTEM
     */
    public void testGetActionsByApplicationTypeSystem() {
        WSFoundationCollection wsfoundationCollection = null;
        try {
            wsfoundationCollection = foundationWSFacade
                    .getActionsByApplicationType(SYSTEM, true);
        } catch (WSDataUnavailableException e) {
            fail("getActionsByApplicationType failed for SYSTEM. WSDataUnavailableException details : "
                    + e);
        } catch (GWPortalException e) {
            fail("getActionsByApplicationType failed for SYSTEM. GWPortalException details : "
                    + e);
        }
        if (wsfoundationCollection == null) {
            fail("getActionsByApplicationType failed for SYSTEM returned null results [WSFoundationCollection]");
        }
        Action[] actions = wsfoundationCollection.getAction();
        if (actions == null) {
            fail("getActionsByApplicationType failed for SYSTEM returned null results [Action]");
            return;
        }
        assertEquals(actions.length, FOUR);
    }

    /**
     * Test case for getActionsByApplicationType for 'all open event' for NAGIOS
     */
    public void testGetActionsByApplicationTypeNagios() {
        WSFoundationCollection wsfoundationCollection = null;
        try {
            wsfoundationCollection = foundationWSFacade
                    .getActionsByApplicationType(NAGIOS, true);
        } catch (WSDataUnavailableException e) {
            fail("getActionsByApplicationType failed for NAGIOS. WSDataUnavailableException details : "
                    + e);
        } catch (GWPortalException e) {
            fail("getActionsByApplicationType failed for NAGIOS. GWPortalException details : "
                    + e);
        }
        if (wsfoundationCollection == null) {
            fail("getActionsByApplicationType for NAGIOS returned wsfoundationCollection as null");
        }
        Action[] actions = wsfoundationCollection.getAction();
        if (actions == null) {
            fail("getActionsByApplicationType for NAGIOS returned actions as null");
            return;
        }
        assertEquals(actions.length, FIVE);
    }

    /**
     * Test getActionsByApplicationType for all open event
     */
    public void testGetActionsByApplicationTypeSyslog() {
        WSFoundationCollection wsfoundationCollection = null;
        try {
            wsfoundationCollection = foundationWSFacade
                    .getActionsByApplicationType(SYSLOG, true);
        } catch (WSDataUnavailableException e) {
            fail("getActionsByApplicationType failed for SYSLOG. WSDataUnavailableException details : "
                    + e);
        } catch (GWPortalException e) {
            fail("getActionsByApplicationType failed for SYSLOG. GWPortalException details : "
                    + e);
        }
        if (wsfoundationCollection == null) {
            fail("getActionsByApplicationType for SYSLOG returned wsfoundationCollection as null");
        }
        Action[] actions = wsfoundationCollection.getAction();
        if (actions == null) {
            fail("getActionsByApplicationType for SYSLOG returned actions as null");
            return;
        }
        assertEquals(actions.length, FIVE);
    }

    /**
     * Test getActionsByApplicationType for all open event
     */
    public void testGetActionsByApplicationTypeSnmptrap() {
        WSFoundationCollection wsfoundationCollection = null;
        try {
            wsfoundationCollection = foundationWSFacade
                    .getActionsByApplicationType(SNMPTRAP, true);
        } catch (WSDataUnavailableException e) {
            fail("getActionsByApplicationType failed for SNMPTRAP. WSDataUnavailableException details : "
                    + e);

        } catch (GWPortalException e) {
            fail("getActionsByApplicationType failed for SNMPTRAP. GWPortalException details : "
                    + e);
        }
        if (wsfoundationCollection == null) {
            fail("getActionsByApplicationType for SNMPTRAP returned wsfoundationCollection as null");
        }
        Action[] actions = wsfoundationCollection.getAction();
        if (actions == null) {
            fail("getActionsByApplicationType for SNMPTRAP returned actions as null");
            return;
        }
        assertEquals(actions.length, FIVE);
    }

    /**
     * Test getEntityTypeProperties()
     */
    public void testGetEntityTypeProperties() {

        WSFoundationCollection wsfoundationCollection = null;

        String entityType = "LOG_MESSAGE";
        String appType = "NAGIOS";
        boolean child = false;

        try {
            wsfoundationCollection = foundationWSFacade
                    .getEntityTypeProperties(entityType, appType, child);
        } catch (WSDataUnavailableException e) {
            fail("getEntityTypeProperties failed. WSDataUnavailableException details : "
                    + e);
        } catch (GWPortalException e) {
            fail("getEntityTypeProperties failed. GWPortalException details : "
                    + e);
        }
        if (wsfoundationCollection == null) {
            System.out
                    .println("No entityType Properties exist for LOG_MESSAGE entity type & NAGIOS application type. Null results for wsfoundationCollection returned");
        }
    }

    /**
     * Test getEntityTypeProperties()
     */
    public void testGetEntityTypePropertiesNegative() {

        WSFoundationCollection wsfoundationCollection = null;

        String entityType = "";
        String appType = "";
        boolean child = false;

        try {
            wsfoundationCollection = foundationWSFacade
                    .getEntityTypeProperties(entityType, appType, child);
        } catch (WSDataUnavailableException e) {
            assert (true);
            return;
        } catch (GWPortalException e) {
            assert (true);
            return;
        }
        if (wsfoundationCollection == null) {
            assert (true);
            return;
        }
        fail("getEntityTypePropertiesNegative failed. Method must throw exception on incorrect [Empty] parameters for entity type and application type.");
    }
}
