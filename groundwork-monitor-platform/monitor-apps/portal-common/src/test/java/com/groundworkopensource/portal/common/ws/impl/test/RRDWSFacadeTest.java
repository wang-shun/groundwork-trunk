
package com.groundworkopensource.portal.common.ws.impl.test;

import java.util.Date;

import junit.framework.TestCase;

import org.groundwork.foundation.ws.model.impl.Host;
import org.groundwork.foundation.ws.model.impl.RRDGraph;
import org.groundwork.foundation.ws.model.impl.ServiceStatus;

import com.groundworkopensource.portal.common.exception.GWPortalException;
import com.groundworkopensource.portal.common.exception.WSDataUnavailableException;
import com.groundworkopensource.portal.common.ws.IWSFacade;
import com.groundworkopensource.portal.common.ws.WebServiceFactory;
import com.groundworkopensource.portal.common.ws.WebServiceFactory.WebServiceType;

/**
 * Test class for RRDWSFacade
 * 
 * @author swapnil_gujrathi
 * 
 */
public class RRDWSFacadeTest extends TestCase {

    /**
     * foundationWSFacade Object to call web services.
     */
    private IWSFacade foundationWSFacade;

    /**
     * Number 400
     */
    private static final int FOUR_HUNDRED = 400;

    /**
     * Number 400
     */
    private static final int ONE_THOUSAND = 1000;

    /**
     * Number 400
     */
    private static final int SEVEN_THOUSAND_TWO_HUNDRED = 7200;

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
     * Test case for getRrdGraph()
     */
    public final void testGetRrdGraph() {

        Host[] hosts = null;
        try {
            hosts = foundationWSFacade.getAllHosts();
        } catch (WSDataUnavailableException e) {
            fail("getAllHosts() failed. WSDataUnavailableException details : "
                    + e);
        } catch (GWPortalException e) {
            fail("getAllHosts() failed. GWPortalException details : " + e);
        }

        if (hosts == null || hosts.length == 0) {
            fail("testGetRrdGraph(): getAllHosts failed to retrieve all hosts.");
            return;
        }
        Host host = hosts[0];
        ServiceStatus[] serviceArray = null;
        try {
            serviceArray = foundationWSFacade.getServicesByHostName(host
                    .getName());
        } catch (WSDataUnavailableException e) {
            fail("getServicesByHostName() failed. WSDataUnavailableException details : "
                    + e);
        } catch (GWPortalException e) {
            fail("getServicesByHostName() failed. GWPortalException details : "
                    + e);
        }

        if (serviceArray == null || serviceArray.length == 0) {
            fail("Failed to retrieve services for given host name.");
            return;
        }
        ServiceStatus serviceStatus = serviceArray[0];

        long startDateInSec;
        long endDateInSec;
        int graphWidth = FOUR_HUNDRED;
        Date currentDate = new Date();
        endDateInSec = currentDate.getTime() / ONE_THOUSAND;
        startDateInSec = endDateInSec - SEVEN_THOUSAND_TWO_HUNDRED;

        RRDGraph[] rrdGraphs = null;
        try {
            rrdGraphs = foundationWSFacade.getRrdGraph("localhost",
                    serviceStatus.getDescription(), startDateInSec,
                    endDateInSec, "NAGIOS", graphWidth);
        } catch (WSDataUnavailableException e) {
            fail("getRrdGraph() failed. WSDataUnavailableException thrown - "
                    + e);
        }
        if (rrdGraphs == null || rrdGraphs.length == 0) {
            System.out.println("Null rrd graphs returned");
        }
    }

    /**
     * Negative test case for getRrdGraph()
     */
    public final void testGetRrdGraphNegative() {

        long startDateInSec;
        long endDateInSec;
        int graphWidth = FOUR_HUNDRED;
        Date currentDate = new Date();
        endDateInSec = currentDate.getTime() / ONE_THOUSAND;
        startDateInSec = endDateInSec - SEVEN_THOUSAND_TWO_HUNDRED;
        RRDGraph[] rrdGraphs = null;
        try {
            rrdGraphs = foundationWSFacade.getRrdGraph("", "", startDateInSec,
                    endDateInSec, "NAGIOS", graphWidth);
        } catch (WSDataUnavailableException e) {
            assert (true);
            return;
        }
        if (rrdGraphs == null || rrdGraphs.length == 0) {
            System.out.println("Null rrd graphs returned");
        }
        fail("getRrdGraph() failed. Method must fail on incorrect parameters");
    }
}
