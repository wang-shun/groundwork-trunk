
package com.groundworkopensource.portal.common.ws.impl.test;

import junit.framework.TestCase;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.groundwork.foundation.ws.model.impl.Filter;
import org.groundwork.foundation.ws.model.impl.FilterOperator;
import org.groundwork.foundation.ws.model.impl.Host;
import org.groundwork.foundation.ws.model.impl.ServiceStatus;
import org.groundwork.foundation.ws.model.impl.StateTransition;
import org.groundwork.foundation.ws.model.impl.WSFoundationCollection;

import com.groundworkopensource.portal.common.FilterConstants;
import com.groundworkopensource.portal.common.exception.GWPortalException;
import com.groundworkopensource.portal.common.exception.WSDataUnavailableException;
import com.groundworkopensource.portal.common.ws.IWSFacade;
import com.groundworkopensource.portal.common.ws.WebServiceFactory;
import com.groundworkopensource.portal.common.ws.WebServiceFactory.WebServiceType;
import com.groundworkopensource.portal.common.ws.impl.FoundationWSFacade;
import com.groundworkopensource.portal.common.ws.impl.HostWSFacade;
import com.groundworkopensource.portal.common.ws.impl.ServiceWSFacade;

/**
 * Test class for EventWSFacade
 * 
 * @author shivangi_walvekar
 * 
 */
public class EventWSFacadeTest extends TestCase {

    /**
     * logger
     */
    private static final Logger LOGGER = FoundationWSFacade.getLogger();
    /**
     * open Events
     */
    private static final String OPEN = "OPEN";
    /**
     * start index
     */
    private static final int ZERO = 0;
    /**
     * end index
     */
    private static final int TEN = 10;
    /**
     * foundationWSFacade Object to call web services.
     */
    private IWSFacade foundationWSFacade;

    /**
     * Initializes required resources.
     */
    @Override
    public final void setUp() throws Exception {
        foundationWSFacade = new WebServiceFactory()
                .getWebServiceInstance(WebServiceType.FOUNDATION_WEBSERVICE);
    }

    /**
     * This method cleans up any used resources
     * 
     * @throws java.lang.Exception
     */
    @Override
    public final void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * This test case tests getHostStateTransitions() API of wsEvent.
     */
    public final void testGetHostStateTransitions() {
        HostWSFacade hostWSFacade = new HostWSFacade();
        Host[] hosts = null;
        try {
            hosts = hostWSFacade.getAllHosts();
        } catch (WSDataUnavailableException e) {
            fail("Failed to retrieve hosts from HostWSFacade.getAllHosts(). WSDataUnavailableException details : "
                    + e);
        } catch (GWPortalException e) {
            fail("Failed to retrieve hosts from HostWSFacade.getAllHosts(). GWPortalException details : "
                    + e);
        }
        if ((hosts == null) || (hosts.length == 0) || (hosts[0] == null)) {
            System.out
                    .println("No hosts retrieved from HostWSFacade.getAllHosts()");
            return;
        } else {
            String hostName = hosts[0].getName();
            // String hostName = "florida";

            String startDateString = "01/01/2008";
            String endDateString = "03/31/2010";

            WSFoundationCollection foundationCollection = null;
            try {
                foundationCollection = foundationWSFacade
                        .getHostStateTransitions(hostName, startDateString,
                                endDateString);
            } catch (GWPortalException e) {
                fail("getHostStateTransitions() failed. GWPortalException details : "
                        + e);
            } catch (WSDataUnavailableException e) {
                fail("getHostStateTransitions() failed. WSDataUnavailableException details : "
                        + e);
            }
            if (foundationCollection == null) {
                fail("No host state transitions retrieved from EventWSFacade.getHostStateTransitions(). foundationCollection is null");
            } else {
                StateTransition[] stateTransitions = foundationCollection
                        .getStateTransition();
                if (stateTransitions == null || stateTransitions.length == 0) {
                    fail("No state tranisitions retrieved from foundationCollection");
                } else {
                    assertEquals(
                            "Successfully retrieved state transitions for host "
                                    + hostName, true,
                            stateTransitions.length > 0);
                }
            }
        }
    }

    /**
     * Negative test case for getHostStateTransitions()
     */
    public final void testGetHostStateTransitionsNegative() {

        WSFoundationCollection foundationCollection = null;
        try {
            foundationCollection = foundationWSFacade.getHostStateTransitions(
                    "", "", "");
        } catch (GWPortalException e) {
            assert (true);
            return;
        } catch (WSDataUnavailableException e) {
            assert (true);
            return;
        }
        if (foundationCollection == null) {
            assert (true);
            return;
        } else {
            fail("testGetHostStateTransitionsNegative() failed. Must throw exception on passing incorrect [Empty] parameters to retrieve state transitions.");
        }
    }

    /**
     * This test case tests getServiceStateTransitions() API of wsEvent.
     */
    public final void testGetSerivceStateTransitions() {
        HostWSFacade hostWSFacade = new HostWSFacade();
        Host[] hosts = null;
        try {
            hosts = hostWSFacade.getAllHosts();
        } catch (WSDataUnavailableException e) {
            fail("testGetSerivceStateTransitions(): Failed to retrieve hosts from HostWSFacade.getAllHosts(). WSDataUnavailableException details : "
                    + e);
        } catch (GWPortalException e) {
            fail("testGetSerivceStateTransitions(): Failed to retrieve hosts from HostWSFacade.getAllHosts(). GWPortalException details : "
                    + e);
        }
        if (hosts == null || hosts.length == 0) {
            fail("testGetSerivceStateTransitions(): No hosts retrieved from HostWSFacade.getAllHosts()");
        } else {
            String hostName = hosts[0].getName();
            ServiceWSFacade serviceWSFacade = new ServiceWSFacade();
            ServiceStatus[] services = null;
            try {
                services = serviceWSFacade.getServicesByHostName(hostName);
            } catch (GWPortalException e) {
                fail("testGetSerivceStateTransitions(): No service retrieved from serviceWSFacade.getServicesByHostName(). GWPortalException details : "
                        + e);
            } catch (WSDataUnavailableException e) {
                fail("testGetSerivceStateTransitions(): No service retrieved from serviceWSFacade.getServicesByHostName(). WSDataUnavailableException details : "
                        + e);
            }
            if (services == null || services.length == 0) {
                fail("testGetSerivceStateTransitions(): No service retrieved from serviceWSFacade.getServicesByHostName() for host '"
                        + hostName + "'");
            } else {
                String startDateString = "01/01/2008";
                String endDateString = "01/09/2010";
                try {
                    for (ServiceStatus service : services) {
                        WSFoundationCollection foundationCollection;
                        foundationCollection = foundationWSFacade
                                .getServiceStateTransitions(hostName, service
                                        .getDescription(), startDateString,
                                        endDateString);
                        if (foundationCollection == null) {
                            fail("Failed to retrieve service state transitions from EventWSFacade.getServiceStateTransitions()");
                        } else {
                            StateTransition[] stateTransitions = foundationCollection
                                    .getStateTransition();
                            if (stateTransitions == null
                                    || stateTransitions.length == 0) {
                                fail("No state tranisitions found.");
                            } else {
                                assertEquals(
                                        "Successfully retrieved state transitions for service "
                                                + service.getDescription(),
                                        true, stateTransitions.length > 0);
                            }
                        }
                    }
                } catch (WSDataUnavailableException ex) {
                    LOGGER.log(Level.ERROR, ex.getMessage());
                    fail();
                } catch (GWPortalException ex) {
                    LOGGER.log(Level.ERROR, ex.getMessage());
                    fail();
                }
            }
        }
    }

    /**
     * Negative test case for getServiceStateTransitions()
     */
    public final void testGetServiceStateTransitionsNegative() {

        WSFoundationCollection foundationCollection = null;
        try {
            foundationCollection = foundationWSFacade
                    .getServiceStateTransitions("", "", "", "");
        } catch (GWPortalException e) {
            assert (true);
            return;
        } catch (WSDataUnavailableException e) {
            assert (true);
            return;
        }
        if (foundationCollection == null) {
            assert (true);
            return;
        } else {
            fail("testGetServiceStateTransitionsNegative() failed. Must throw exception on passing incorrect [Empty] parameters.");
        }
    }

    /**
     * This method test getEventsByCriteria() for open event.
     */
    public final void testGetEventsByCriteria() {
        WSFoundationCollection foundationCollection = null;
        Filter filter = new Filter(FilterConstants.OPERATION_STATUS_NAME,
                FilterOperator.EQ, OPEN);
        try {
            foundationCollection = foundationWSFacade.getEventsByCriteria(
                    filter, null, ZERO, TEN);
        } catch (GWPortalException e) {
            fail("Fail to get open event (Log message) to check getEventsByCriteria() method. GWPortalException details : "
                    + e);
        } catch (WSDataUnavailableException e) {
            fail("Fail to get open event (Log message) to check getEventsByCriteria() method. WSDataUnavailableException details : "
                    + e);
        }
        if (foundationCollection == null) {
            fail("No open event (Log message) returned for getEventsByCriteria() method");
        }
        assert (true);
    }

    /**
     * This method test getEventsByCriteria() for open event.
     */
    public final void testGetEventsByCriteriaNegative() {
        WSFoundationCollection foundationCollection = null;
        Filter filter = new Filter();
        try {
            foundationCollection = foundationWSFacade.getEventsByCriteria(
                    filter, null, ZERO, TEN);
        } catch (GWPortalException e) {
            assert (true);
            return;
        } catch (WSDataUnavailableException e) {
            assert (true);
            return;
        }
        if (foundationCollection == null) {
            assert (true);
            return;
        }
        fail("testGetEventsByCriteriaNegative failed. Must throw exception on passing empty filter.");
    }
}
