
package com.groundworkopensource.portal.common.ws.impl.test;

import junit.framework.TestCase;

import org.groundwork.foundation.ws.model.impl.Category;
import org.groundwork.foundation.ws.model.impl.CategoryEntity;
import org.groundwork.foundation.ws.model.impl.Filter;
import org.groundwork.foundation.ws.model.impl.FilterOperator;
import org.groundwork.foundation.ws.model.impl.ServiceStatus;

import com.groundworkopensource.portal.common.FilterConstants;
import com.groundworkopensource.portal.common.exception.GWPortalException;
import com.groundworkopensource.portal.common.exception.WSDataUnavailableException;
import com.groundworkopensource.portal.common.ws.IWSFacade;
import com.groundworkopensource.portal.common.ws.WebServiceFactory;
import com.groundworkopensource.portal.common.ws.WebServiceFactory.WebServiceType;

/**
 * Test class for CategoryWSFacade
 * 
 * @author nitin_jadhav
 * 
 */
public class CategoryWSFacadeTest extends TestCase {

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
     * {@link com.groundworkopensource.portal.common.ws.impl.CategoryWSFacade#getAllServiceGroups()}
     * .
     */
    public void testGetAllServiceGroups() {
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
        // If no service groups exist in the data-set
        if (serviceGroups == null || serviceGroups.length == 0) {
            System.out
                    .println("No service groups returned for getAllServiceGroups()");
        }
    }

    /**
     * Test method for
     * {@link com.groundworkopensource.portal.common.ws.impl.CategoryWSFacade#getCategoryEntities(java.lang.String)}
     * .
     */
    public void testGetCategoryEntities() {
        Category[] serviceGroups = null;
        try {
            // Get all service groups
            serviceGroups = foundationWSFacade.getAllServiceGroups();
        } catch (WSDataUnavailableException e) {
            fail("testGetCategoryEntities(): getAllServiceGroups() failed. WSDataUnavailableException details : "
                    + e);
        } catch (GWPortalException e) {
            fail("testGetCategoryEntities(): getAllServiceGroups() failed. GWPortalException details : "
                    + e);
        }
        // Get category entities with service group name
        if (null == serviceGroups || serviceGroups.length == 0) {
            System.out
                    .println("testGetCategoryEntities(): getAllServiceGroups() returns no service groups.");
            return;
        } else {
            CategoryEntity[] categoryEntities = null;
            try {
                categoryEntities = foundationWSFacade
                        .getCategoryEntities(serviceGroups[0].getName());
            } catch (WSDataUnavailableException e) {
                fail("getCategoryEntities() failed. WSDataUnavailableException details : "
                        + e);
            } catch (GWPortalException e) {
                fail("getCategoryEntities() failed. GWPortalException details : "
                        + e);
            }
            if (categoryEntities == null || categoryEntities.length == 0) {
                System.out
                        .println("No category entities returned for getCategoryEntities() with service group name parameter - "
                                + serviceGroups[0].getName());
            }
        }
    }

    /**
     * Negative test case for CategoryWSFacade.getCategoryEntities()
     */
    public void testGetCategoryEntitiesNegative() {
        String serviceGroupName = null;
        CategoryEntity[] categoryEntities = null;
        try {

            // Passing null service group name parameter
            categoryEntities = foundationWSFacade
                    .getCategoryEntities(serviceGroupName);
        } catch (WSDataUnavailableException e) {
            assert (true);
            return;
        } catch (GWPortalException e) {
            assert (true);
            return;
        }

        if (categoryEntities == null || categoryEntities.length == 0) {
            System.out
                    .println("No category entities returned for getCategoryEntities() with service group name parameter as null");
            assert (true);
            return;
        }
        fail("getCategoryEntities() failed. Method must fail on passing incorrect [null] service group parameter name.");
    }

    /**
     * Test case for CategoryWSFacade.getCategory()
     */
    public void testGetCategory() {
        Category[] category = null;
        Filter filter = null;
        // Create filters for all service groups which are OK
        try {
            filter = getServiceGroupFilter("OK");
        } catch (GWPortalException e) {
            fail("testGetCategoryByName(): getServiceGroupFilter() failed. Failed to create service group filter. WSDataUnavailableException details : "
                    + e);
        } catch (WSDataUnavailableException e) {
            fail("testGetCategoryByName(): "
                    + "getServiceGroupFilter() failed. Failed to create service group filter. WSDataUnavailableException details : "
                    + e);
        }
        try {
            category = foundationWSFacade.getCategory(filter, -1, -1, null,
                    true, false);
        } catch (WSDataUnavailableException e) {
            fail("getCategory() failed. WSDataUnavailableException details : "
                    + e);
        } catch (GWPortalException e) {
            fail("getCategory() failed. GWPortalException details : " + e);
        }

        if (category == null || category.length == 0) {
            System.out
                    .println("No categories returned for getCategory() with service group filter for all OK services");
        }
    }

    /**
     * Negative test case for CategoryWSFacade.getCategory()
     */
    public void testGetCategoryNegative() {
        Category[] category = null;
        // Create empty filter
        Filter filter = new Filter();
        try {
            category = foundationWSFacade.getCategory(filter, -1, -1, null,
                    true, false);
        } catch (WSDataUnavailableException e) {
            assert (true);
            return;
        } catch (GWPortalException e) {
            assert (true);
            return;
        }

        if (category == null || category.length == 0) {
            System.out
                    .println("No categories returned for getCategory() with empty filter");
            assert (true);
            return;
        }
        fail("getCategory() failed. Method must fail on passing incorrect [empty] filter");
    }

    /**
     * Test case for CategoryWSFacade.getCategoryByID()
     */
    public void testGetCategoryByID() {
        Category[] categoryList = null;
        Filter filter = null;
        try {
            filter = getServiceGroupFilter("OK");
        } catch (GWPortalException e) {
            fail("testGetCategoryByName(): getServiceGroupFilter() failed. Failed to create service group filter. WSDataUnavailableException details : "
                    + e);
        } catch (WSDataUnavailableException e) {
            fail("testGetCategoryByName(): "
                    + "getServiceGroupFilter() failed. Failed to create service group filter. WSDataUnavailableException details : "
                    + e);
        }
        // Retrieve category list by filter for 'OK'
        try {
            categoryList = foundationWSFacade.getCategory(filter, -1, -1, null,
                    true, false);
        } catch (WSDataUnavailableException e) {
            fail("getCategory() failed. WSDataUnavailableException details : "
                    + e);
        } catch (GWPortalException e) {
            fail("getCategory() failed. GWPortalException details : " + e);
        }

        if (null == categoryList || categoryList.length == 0) {
            System.out
                    .println("No categories returned for getCategory() with service group filter for all OK services");
            return;
        }

        // Retrieve a valid category
        Category category1 = categoryList[0];
        int id = category1.getCategoryId();

        Category category2 = null;
        try {
            // Test call
            category2 = foundationWSFacade.getCategoryByID(id);
        } catch (WSDataUnavailableException e) {
            fail("getCategoryByID() failed. WSDataUnavailableException details : "
                    + e);
        } catch (GWPortalException e) {
            fail("getCategoryByID() failed. GWPortalException details : " + e);
        }
        if (category2 == null) {
            fail("getCategoryByID() returned null category");
            return;
        }
        // Assert value
        assertEquals(category1.getName(), category2.getName());
    }

    /**
     * Test case for CategoryWSFacade.getCategoryByName()
     */
    public void testGetCategoryByName() {
        Category[] categoryList = null;
        Filter filter = null;
        try {
            filter = getServiceGroupFilter("OK");
        } catch (GWPortalException e) {
            fail("testGetCategoryByName(): getServiceGroupFilter() failed. Failed to create service group filter. WSDataUnavailableException details : "
                    + e);
        } catch (WSDataUnavailableException e) {
            fail("testGetCategoryByName(): "
                    + "getServiceGroupFilter() failed. Failed to create service group filter. WSDataUnavailableException details : "
                    + e);
        }
        try {
            categoryList = foundationWSFacade.getCategory(filter, -1, -1, null,
                    true, false);
        } catch (WSDataUnavailableException e) {
            fail("testGetCategoryByName(): getCategory() failed. WSDataUnavailableException details : "
                    + e);
        } catch (GWPortalException e) {
            fail("testGetCategoryByName(): getCategory() failed. GWPortalException details : "
                    + e);
        }
        if (null == categoryList || categoryList.length == 0) {
            System.out
                    .println("No categories returned for getCategory() with service group filter for all OK services");
            return;
        }
        Category category1 = categoryList[0];
        String name = category1.getName();
        Category category2 = null;

        try {
            category2 = foundationWSFacade.getCategoryByName(name);
        } catch (WSDataUnavailableException e) {
            fail("getCategoryByName() failed. WSDataUnavailableException details : "
                    + e);
        } catch (GWPortalException e) {
            fail("getCategoryByName() failed. GWPortalException details : " + e);
        }
        // Assert value
        assertEquals(category1.getCategoryId(), category2.getCategoryId());
    }

    /**
     * Negative test case for CategoryWSFacade.getCategoryByName()
     */
    public void testGetCategoryByNameNegative() {
        Category category = null;
        try {
            // Test call with category name as null
            category = foundationWSFacade.getCategoryByName(null);
        } catch (WSDataUnavailableException e) {
            assert (true);
            return;
        } catch (GWPortalException e) {
            assert (true);
            return;
        }

        if (category == null) {
            System.out
                    .println("Null category returned for getCategoryByName with null name parameter.");
            return;
        }
        fail("getCategoryByName() failed. Method must fail on passing incorrect [null] name");
    }

    /**
     * Method to get service Group filter for Service Group name depending
     * monitor status passed.
     * 
     * @param currentStatus
     * @return Filter
     * @throws WSDataUnavailableException
     * @throws GWPortalException
     */

    private Filter getServiceGroupFilter(String currentStatus)
            throws GWPortalException, WSDataUnavailableException {

        // Create String for service status ID
        StringBuffer serviceStatusIdBuilder = new StringBuffer(
                TestConstants.EMPTY_STRING);
        // Create filter to get all service status ID
        Filter serviceFilter = new Filter(FilterConstants.MONITOR_STATUS_NAME,
                FilterOperator.EQ, currentStatus.toUpperCase());
        // Get service status array from foundation web service.
        ServiceStatus[] serviceStatusArr = null;

        serviceStatusArr = foundationWSFacade
                .getServicesbyCriteria(serviceFilter);
        if (null != serviceStatusArr) {
            // Creating comma Separated service Status ID String
            for (ServiceStatus serviceStatus : serviceStatusArr) {
                serviceStatusIdBuilder.append(serviceStatus
                        .getServiceStatusID());
                serviceStatusIdBuilder.append(TestConstants.COMMA);
            }
        }
        int lastcommaindex = serviceStatusIdBuilder
                .lastIndexOf(TestConstants.COMMA);
        // Remove comma at last
        String serviceStatusIds = serviceStatusIdBuilder.substring(0,
                lastcommaindex - 1);
        // Create filter for retrieving service group.
        Filter serviceGroupCategoryidFilter = new Filter(
                FilterConstants.CATEGORY_ENTITIES_OBJECT_I_D,
                FilterOperator.IN, serviceStatusIds);
        Filter serviceGroupCategoryNameFilter = new Filter(
                FilterConstants.CATEGORY_ENTITIES_ENTITY_TYPE_NAME,
                FilterOperator.EQ, FilterConstants.SERVICE_GROUP);
        // Do ANDing of both the filters
        Filter filter = Filter.AND(serviceGroupCategoryidFilter,
                serviceGroupCategoryNameFilter);
        // Return resultant filter
        return filter;
    }
}
