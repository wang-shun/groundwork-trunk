
package com.groundworkopensource.portal.common.ws.impl.test;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.groundwork.foundation.ws.model.impl.BooleanProperty;
import org.groundwork.foundation.ws.model.impl.Filter;
import org.groundwork.foundation.ws.model.impl.FilterOperator;
import org.groundwork.foundation.ws.model.impl.Host;
import org.groundwork.foundation.ws.model.impl.HostGroup;
import org.groundwork.foundation.ws.model.impl.ServiceStatus;
import org.groundwork.foundation.ws.model.impl.SimpleHost;
import org.groundwork.foundation.ws.model.impl.SimpleServiceStatus;
import org.groundwork.foundation.ws.model.impl.Sort;
import org.groundwork.foundation.ws.model.impl.StringProperty;
import org.groundwork.foundation.ws.model.impl.WSFoundationCollection;

import com.groundworkopensource.portal.common.exception.GWPortalException;
import com.groundworkopensource.portal.common.exception.WSDataUnavailableException;
import com.groundworkopensource.portal.common.ws.IWSFacade;
import com.groundworkopensource.portal.common.ws.WebServiceFactory;
import com.groundworkopensource.portal.common.ws.WebServiceFactory.WebServiceType;

/**
 * Tests class for ServiceWSFacade.
 * 
 * @author nitin_jadhav
 * 
 */
public class ServiceWSFacadeTest extends TestCase {

    /**
     * Logger.
     */
    private final Logger logger = Logger.getLogger(this.getClass().getName());
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
     * Method tests getSimpleServicesByHostName()
     */
    public final void testGetSimpleServicesByHostName() {

        SimpleServiceStatus[] simpleServices = null;
        HostGroup[] allHostGroups = null;
        SimpleHost[] simpleHosts = null;

        // get all host groups
        try {
            allHostGroups = foundationWSFacade.getAllHostGroups();
        } catch (GWPortalException e) {
            fail("getAllHostGroups failed. GWPortalException details : " + e);
        } catch (WSDataUnavailableException e) {
            fail("getAllHostGroups failed. WSDataUnavailableException details : "
                    + e);
        }

        if (allHostGroups == null || allHostGroups.length == 0) {
            fail("getAllHostGroups returned null or empty results.");
        } else {
            // get simple hosts in host group by name
            try {
                simpleHosts = foundationWSFacade.getHostsUnderHostGroup(
                        allHostGroups[0].getName(), false);

            } catch (WSDataUnavailableException e) {
                fail("getHostsUnderHostGroupById() failed. WSDataUnavailableException details : "
                        + e);
            } catch (GWPortalException e) {
                fail("getHostsUnderHostGroupById() failed. GWPortalException details : "
                        + e);
            }
        }

        if (simpleHosts == null || simpleHosts.length == 0) {
            fail("Failed to retrieve simple hosts under host group.");
            return;
        }
        try {
            // get the simpleServices for host
            simpleServices = foundationWSFacade
                    .getSimpleServicesByHostName(simpleHosts[0].getName());

        } catch (GWPortalException e) {
            fail("getServicesByHostName() failed. GWPortalException details : "
                    + e);
        } catch (WSDataUnavailableException e) {
            fail("getServicesByHostName() failed. WSDataUnavailableException details :  "
                    + e);
        }

        if (simpleServices == null || simpleServices.length == 0) {
            System.out
                    .println("Null services returned for getServicesByHostName()");
        }
    }

    /**
     * Test method for
     * {@link com.groundworkopensource.portal.common.ws.impl.ServiceWSFacade#getServices()}
     * .
     */
    public final void testGetServices() {
        ServiceStatus[] services = null;
        try {
            services = foundationWSFacade.getServices();
        } catch (GWPortalException e) {
            fail("ServiceWSFacade.getServices() failed. GWPortalException details : "
                    + e);
        } catch (WSDataUnavailableException e) {
            fail("ServiceWSFacade.getServices() failed. WSDataUnavailableException details : "
                    + e);
        }
        if (services == null || services.length == 0) {
            System.out.println("getServices() returned empty result.");
        }
    }

    /**
     * Test method for
     * {@link com.groundworkopensource.portal.common.ws.impl.ServiceWSFacade#getTroubledServices()}
     * .
     */
    public final void testGetTroubledServices() {
        ServiceStatus[] services = null;
        try {
            services = foundationWSFacade.getTroubledServices();
        } catch (GWPortalException e) {
            fail("getTroubledServices() failed. GWPortalException details : "
                    + e);

        } catch (WSDataUnavailableException e) {
            fail("getTroubledServices() failed. WSDataUnavailableException details : "
                    + e);
        }
        if (services == null || services.length == 0) {
            System.out
                    .println("No troubled services returned for getTroubledServices()");
        }
    }

    /**
     * Method tests getServicesByHostName()
     */
    public final void testGetServicesByHostName() {

        ServiceStatus[] services = null;
        HostGroup[] allHostGroups = null;
        Host[] hostsUnderHostGroupById = null;

        // get all host groups
        try {
            allHostGroups = foundationWSFacade.getAllHostGroups();
        } catch (GWPortalException e) {
            fail("getAllHostGroups failed. GWPortalException details : " + e);
        } catch (WSDataUnavailableException e) {
            fail("getAllHostGroups failed. WSDataUnavailableException details : "
                    + e);
        }

        if (allHostGroups == null || allHostGroups.length == 0) {
            fail("getAllHostGroups returned null or empty results.");
        } else {
            // get hosts in host group by ID
            try {
                hostsUnderHostGroupById = foundationWSFacade
                        .getHostsUnderHostGroupById(allHostGroups[0]
                                .getHostGroupID()
                                + "");
            } catch (WSDataUnavailableException e) {
                fail("getHostsUnderHostGroupById() failed. WSDataUnavailableException details : "
                        + e);
            } catch (GWPortalException e) {
                fail("getHostsUnderHostGroupById() failed. GWPortalException details : "
                        + e);
            }
        }

        if (hostsUnderHostGroupById == null
                || hostsUnderHostGroupById.length == 0) {
            fail("Failed to retrieve hosts under host group.");
            return;
        }
        try {
            services = foundationWSFacade
                    .getServicesByHostName(hostsUnderHostGroupById[0].getName());
        } catch (GWPortalException e) {
            fail("getServicesByHostName() failed. GWPortalException details : "
                    + e);
        } catch (WSDataUnavailableException e) {
            fail("getServicesByHostName() failed. WSDataUnavailableException details :  "
                    + e);
        }

        if (services == null || services.length == 0) {
            System.out
                    .println("Null services returned for getServicesByHostName()");
        }
    }

    /**
     * Method tests getServicesByHostName()
     */
    public final void testGetServicesByHostNameNegative() {
        ServiceStatus[] services = null;
        try {
            services = foundationWSFacade.getServicesByHostName("");
        } catch (GWPortalException e) {
            assert (true);
            return;
        } catch (WSDataUnavailableException e) {
            assert (true);
            return;
        }

        if (services == null || services.length == 0) {
            System.out
                    .println("Null services returned for getServicesByHostName()");
        }

        fail("getServicesByHostName() failed. Method must fail on passing incorrect host name.");
    }

    /**
     * Method tests getServicesByHostId()
     */
    public final void testGetServicesByHostId() {

        // Get all host groups
        HostGroup[] allHostGroups = null;
        try {
            allHostGroups = foundationWSFacade.getAllHostGroups();
        } catch (GWPortalException e) {
            fail("getAllHostGroups() failed. GWPortalException details : " + e);
        } catch (WSDataUnavailableException e) {
            fail("getAllHostGroups() failed. WSDataUnavailableException details : "
                    + e);
        }

        if (allHostGroups == null || allHostGroups.length == 0) {
            fail("getAllHostGroups() returned null or empty results");
        } else {
            Host[] hostsUnderHostGroupById = null;
            try {
                hostsUnderHostGroupById = foundationWSFacade
                        .getHostsUnderHostGroupById(allHostGroups[0]
                                .getHostGroupID()
                                + "");
            } catch (WSDataUnavailableException e) {
                fail("getHostsUnderHostGroupById() failed. WSDataUnavailableException details : "
                        + e);
            } catch (GWPortalException e) {
                fail("getHostsUnderHostGroupById() failed. GWPortalException details : "
                        + e);
            }

            if (hostsUnderHostGroupById == null
                    || hostsUnderHostGroupById.length == 0) {
                fail("Failed to retrieve hosts under host group.");
                return;
            }

            // Test get services by host ID
            ServiceStatus[] servicesUnderHost = null;
            try {

                servicesUnderHost = foundationWSFacade
                        .getServicesByHostId(hostsUnderHostGroupById[0]
                                .getHostID());

            } catch (GWPortalException e) {
                fail("getServicesByHostId failed. GWPortalException details : "
                        + e);
            } catch (WSDataUnavailableException e) {
                fail("getServicesByHostId failed. WSDataUnavailableException details : "
                        + e);
            }
            if (servicesUnderHost == null) {
                fail("getServicesByHostId returned no services");
            } else if (servicesUnderHost.length == 0) {
                fail("testGetServicesByHostId returned empty.");
            }
        }
        assert (true);
    }

    /**
     * Method tests getServicesByHostId()
     */
    public final void testGetServicesByHostIdNegative() {

        // Test get services by host ID
        ServiceStatus[] servicesUnderHost = null;
        try {

            servicesUnderHost = foundationWSFacade.getServicesByHostId(-1);

        } catch (GWPortalException e) {
            assert (true);
            return;
        } catch (WSDataUnavailableException e) {
            assert (true);
            return;
        }
        if (servicesUnderHost == null) {
            assert (true);
            return;
        } else if (servicesUnderHost.length == 0) {
            assert (true);
            return;
        }
        fail("testGetServicesByHostIdNegative() failed. Method must fail on incorrect [Negative] ID parameter.");
    }

    /**
     * Method tests getServicesByCriteria(filter)
     */
    public final void testGetServicesByCriteria() {

        // Filter objects
        Filter filter = new Filter();
        Filter leftFilter = new Filter();
        Filter rightFilter = new Filter();

        // Create the left filter
        leftFilter.setStringProperty(new StringProperty("propertyValues.name",
                "isEventHandlersEnabled"));
        leftFilter.setOperator(FilterOperator.EQ);

        // Create the right filter
        rightFilter.setBooleanProperty(new BooleanProperty(
                "propertyValues.valueBoolean", true));
        rightFilter.setOperator(FilterOperator.EQ);

        // left filter AND right filter
        filter.setOperator(FilterOperator.AND);
        filter.setLeftFilter(leftFilter);
        filter.setRightFilter(rightFilter);
        ServiceStatus[] serviceStatusArray = null;

        // Call getServicesbyCriteria with filter as only parameter
        try {
            serviceStatusArray = foundationWSFacade
                    .getServicesbyCriteria(filter);
        } catch (GWPortalException e) {
            fail("getServicesbyCriteria() failed. GWPortalException details : ");
        } catch (WSDataUnavailableException e) {
            fail("getServicesbyCriteria() failed. WSDataUnavailableException details : "
                    + e);
        }
        if (serviceStatusArray == null || serviceStatusArray.length == 0) {
            fail("getServicesbyCriteria() failed to retrieve services with given criteria.");
            return;
        }
        this.logger.info("Length of service status array = "
                + serviceStatusArray.length);
    }

    /**
     * Method tests getServicesByCriteria(filter)
     */
    public final void testGetServicesByCriteriaNegative() {

        // Filter objects
        Filter filter = new Filter();
        ServiceStatus[] serviceStatusArray = null;

        // Call getServicesbyCriteria with filter as only parameter
        try {
            serviceStatusArray = foundationWSFacade
                    .getServicesbyCriteria(filter);
        } catch (GWPortalException e) {
            assert (true);
            return;
        } catch (WSDataUnavailableException e) {
            assert (true);
            return;
        }
        if (serviceStatusArray == null || serviceStatusArray.length == 0) {
            System.out.println("No services of this criteria returned.");
            assert (true);
            return;
        }
        fail("Could not retrieve services; getServicesbyCriteria() with only filter as parameter failed");
    }

    /**
     * Method tests getServiceByHostAndServiceName()
     */
    public final void testGetServiceByHostAndServiceName() {

        // Get all services
        ServiceStatus[] services = null;
        try {
            services = foundationWSFacade.getServices();
        } catch (GWPortalException e1) {
            fail("getServices() failed. GWPortalException details : " + e1);
        } catch (WSDataUnavailableException e1) {
            fail("getServices() failed. WSDataUnavailableException details : "
                    + e1);
        }

        // Get a particular service from list of all services
        ServiceStatus serviceStatus = services[0];
        ServiceStatus uniqueService = null;

        if (serviceStatus == null) {
            fail("Could not retrieve services; getServices() returns no service list!");
            return;
        }

        // Test if same service retrieved on call by service and host name
        try {
            uniqueService = foundationWSFacade.getServiceByHostAndServiceName(
                    serviceStatus.getHost().getName(), serviceStatus
                            .getDescription());
        } catch (GWPortalException e) {
            fail("getServiceByHostAndServiceName() failed. GWPortalException details : "
                    + e);
        } catch (WSDataUnavailableException e) {
            fail("getServiceByHostAndServiceName() failed. WSDataUnavailableException details : "
                    + e);
        }
        if (uniqueService == null) {
            System.out
                    .println("Null service returned for given host and service name");
        }
    }

    /**
     * Negative test case for getServiceByHostAndServiceName()
     */
    public final void testGetServiceByHostAndServiceNameNegative() {

        ServiceStatus uniqueService = null;

        // Test if same service retrieved on call by service and host name
        try {
            uniqueService = foundationWSFacade.getServiceByHostAndServiceName(
                    " ", " ");
        } catch (GWPortalException e) {
            assert (true);
            return;
        } catch (WSDataUnavailableException e) {
            assert (true);
            return;
        }
        if (uniqueService == null) {
            System.out
                    .println("Null service returned for given host and service name");
        }
        fail("testGetServiceByHostAndServiceNameNegative() failed");
    }

    /**
     * Method tests getServicesById()
     */
    public final void testGetServicesById() {

        // Retrieve all services
        ServiceStatus[] services = null;
        try {
            services = foundationWSFacade.getServices();
        } catch (GWPortalException e1) {
            fail("getServices() failed. GWPortalException details : " + e1);
            return;
        } catch (WSDataUnavailableException e1) {
            fail("getServices() failed. WSDataUnavailableException details : "
                    + e1);
            return;
        }

        // Get a particular service from list of all services
        ServiceStatus serviceStatus = services[0];

        ServiceStatus status = null;
        // Test if same service retrieved on call by service ID
        try {
            status = foundationWSFacade.getServicesById(serviceStatus
                    .getServiceStatusID());
        } catch (GWPortalException e) {
            fail("getServicesById() failed. GWPortalException details : " + e);
        } catch (WSDataUnavailableException e) {
            fail("getServicesById() failed. WSDataUnavailableException details : "
                    + e);
        }
        if (status == null) {
            System.out.println("getServicesById(): Null services returned");
        }
    }

    /**
     * Method tests getServicesById()
     */
    public final void testGetServicesByIdNegative() {

        ServiceStatus status = null;
        // Test if same service retrieved on call by service ID
        try {
            status = foundationWSFacade.getServicesById(-1);
        } catch (GWPortalException e) {
            assert (true);
            return;
        } catch (WSDataUnavailableException e) {
            assert (true);
            return;
        }
        if (status == null) {
            System.out.println("Null services returned");
        }
        fail("testGetServicesByIdNegative() failed");
    }

    /**
     * Method tests getServicesbyCriteriaAllParameters()
     */
    public final void testGetServicesbyCriteriaAllParameters() {

        // Filter objects
        Filter filter = new Filter();
        Filter leftFilter = new Filter();
        Filter rightFilter = new Filter();
        Sort sort = null;

        // Create the left filter
        leftFilter.setStringProperty(new StringProperty("propertyValues.name",
                "isEventHandlersEnabled"));
        leftFilter.setOperator(FilterOperator.EQ);

        // Create the right filter
        rightFilter.setBooleanProperty(new BooleanProperty(
                "propertyValues.valueBoolean", true));
        rightFilter.setOperator(FilterOperator.EQ);

        // left filter AND right filter
        filter.setOperator(FilterOperator.AND);
        filter.setLeftFilter(leftFilter);
        filter.setRightFilter(rightFilter);
        WSFoundationCollection serviceStatusArray = null;

        // Call getServicesbyCriteria with all parameters
        try {
            serviceStatusArray = foundationWSFacade.getServicesbyCriteria(
                    filter, sort, -1, -1);
        } catch (GWPortalException e) {
            fail("getServicesbyCriteria() failed. GWPortalException details : "
                    + e);
        } catch (WSDataUnavailableException e) {
            fail("getServicesbyCriteria() failed. WSDataUnavailableException details : "
                    + e);
        }

        if (serviceStatusArray == null) {
            System.out.println("Null services returned");
        }
    }
}
