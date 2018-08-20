
package com.groundworkopensource.portal.common.ws.impl.test;

import junit.framework.TestCase;

import org.groundwork.foundation.ws.model.impl.Host;

import com.groundworkopensource.portal.common.exception.GWPortalException;
import com.groundworkopensource.portal.common.exception.WSDataUnavailableException;
import com.groundworkopensource.portal.common.ws.IWSFacade;
import com.groundworkopensource.portal.common.ws.WebServiceFactory;
import com.groundworkopensource.portal.common.ws.WebServiceFactory.WebServiceType;

/**
 * This is the TestCase for testing WebServiceFactory.
 * 
 * @author swapnil_gujrathi
 */
public class WebServiceFactoryTest extends TestCase {

    /**
     * WebServiceFactory instance.
     */
    private WebServiceFactory webServiceFactory;

    /**
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected final void setUp() throws Exception {
        webServiceFactory = new WebServiceFactory();
    }

    /**
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected final void tearDown() throws Exception {
        webServiceFactory = null;
    }

    /**
     * Test case for testing getWebServiceInstance().
     */
    public final void testGetWebServiceInstance() {
        IWSFacade foundationWSFacade = webServiceFactory
                .getWebServiceInstance(WebServiceType.FOUNDATION_WEBSERVICE);
        assertNotNull(foundationWSFacade);

        // test by calling any method in the web service factory
        try {
            Host[] hosts = foundationWSFacade.getAllHosts();
            if (hosts.length == 0) {
                fail("Sample call of getAllHosts() method returns 0 hosts");
            }
            for (Host host : hosts) {
                System.out.println(host.getName() + " - "
                        + host.getMonitorStatus().getDescription());
                System.out.println("---------------");
            }
            assertTrue(true);
        } catch (WSDataUnavailableException e) {
            assertFalse(e.getMessage(), false);
        } catch (GWPortalException e) {
            assertFalse(e.getMessage(), false);
        }

    }

}
