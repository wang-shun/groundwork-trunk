package com.groundwork.collage.biz;

import com.groundwork.collage.CollageAdminInfrastructure;
import com.groundwork.collage.model.Category;
import com.groundwork.collage.model.CategoryEntity;
import com.groundwork.collage.model.CheckType;
import com.groundwork.collage.model.Device;
import com.groundwork.collage.model.EntityType;
import com.groundwork.collage.model.Host;
import com.groundwork.collage.model.HostGroup;
import com.groundwork.collage.model.LogMessage;
import com.groundwork.collage.model.MonitorStatus;
import com.groundwork.collage.model.ServiceStatus;
import com.groundwork.collage.model.StateType;
import com.groundwork.collage.util.Nagios;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.bs.category.CategoryService;
import org.groundwork.foundation.bs.device.DeviceService;
import org.groundwork.foundation.bs.host.HostService;
import org.groundwork.foundation.bs.hostgroup.HostGroupService;
import org.groundwork.foundation.bs.logmessage.LogMessageService;
import org.groundwork.foundation.bs.status.StatusService;
import org.groundwork.foundation.dao.SortCriteria;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BizServicesTest extends AbstractTestBizBase
{
    private static final Log log = LogFactory.getLog(BizServicesTest.class);

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    protected static final String HOST_BIZ_1 = "host-biz-1";
    protected static final String HOST_BIZ_2 = "host-biz-2";
    protected static final String DEVICE_BIZ_1 = "device-biz-1";
    protected static final String DEVICE_BIZ_2 = "device-biz-2";
    protected static final String BIZ_GROUP_1 = "biz-group-1";
    protected static final String BIZ_GROUP_2 = "biz-group-2";
    protected static final String BIZ_HOST_CATEGORY_1 = "biz-host-category-1";
    protected static final String BIZ_HOST_CATEGORY_2 = "biz-host-category-2";
    protected static final String BIZ_SERVICE_CATEGORY_1 = "biz-service-category-1";
    protected static final String BIZ_SERVICE_CATEGORY_2 = "biz-service-category-2";
    protected static final String BIZ_SERVICE_1 = "biz-service-1";
    protected static final String BIZ_SERVICE_2 = "biz-service-2";

    private static final String DOWNTIME_APP_TYPE = BizServicesImpl.DOWNTIME_APP_TYPE;
    private static final String START_DOWNTIME_MONITOR_STATUS = BizServicesImpl.START_DOWNTIME_MONITOR_STATUS;
    private static final String IN_DOWNTIME_MONITOR_STATUS = BizServicesImpl.IN_DOWNTIME_MONITOR_STATUS;
    private static final String END_DOWNTIME_MONITOR_STATUS = BizServicesImpl.END_DOWNTIME_MONITOR_STATUS;

    private static final SortCriteria LOGMESSAGE_SORT_CRITERIA;
    static {
        LOGMESSAGE_SORT_CRITERIA = SortCriteria.desc(LogMessage.HP_LAST_INSERT_DATE);
        LOGMESSAGE_SORT_CRITERIA.addSort(LogMessage.HP_ID, false);
    }

    public BizServicesTest(String x) {
        super(x);
    }

    public static Test suite()
    {
        executeScript(false, "../common/testdata/monitor-data.sql");

        TestSuite suite = new TestSuite();
        suite.addTest(new BizServicesTest("testHostBizServices"));
        suite.addTest(new BizServicesTest("testServiceBizServices"));
        suite.addTest(new BizServicesTest("testHostServiceBizServices"));
        suite.addTest(new BizServicesTest("testDowntimeBizServices"));
        suite.addTest(new BizServicesTest("testAuthorizationBizServices"));
        return suite;
    }

    public void setUp()
    {
        super.setUp();
    }


    public void testHostBizServices() throws Exception {
        System.out.println("testing Host Biz Services...");
        assert collage != null;
        assert collage.getLogMessageService() != null;
        assert collage.getHostService() != null;
        assert collage.getHostGroupService() != null;
        assert collage.getCategoryService() != null;
        BizServices biz = (BizServices) collage.getAPIObject(BizServices.SERVICE);
        assert biz != null;

        // Test adding ....
        beginTransaction();
        Host host = biz.createOrUpdateHost(HOST_BIZ_1, "PENDING", "this is a test", BIZ_GROUP_1, BIZ_HOST_CATEGORY_1,
                DEVICE_BIZ_1, "SEL", "biz", 3, true, false, false, null);
        assertValidHost(host);
        Host host2 = biz.createOrUpdateHost(HOST_BIZ_2, "UP", "this is a test 2", null, null, DEVICE_BIZ_2, "SEL", "biz",
                3, true, false, true, null);
        assertValidHost2(host2);

        host = collage.getHostService().getHostByHostName(HOST_BIZ_1);
        assertValidHost(host);
        host2 = collage.getHostService().getHostByHostName(HOST_BIZ_2);
        assertValidHost2(host2);

        // Test Updating ...
        host = biz.createOrUpdateHost(HOST_BIZ_1, "UP", "this is a test of updating", null, null, null, "SEL", null,
                null, null, null, null, null);
        assertValidUpdatedHost(host);
        assertGroups(HOST_BIZ_1, BIZ_GROUP_1);
        assertHostCategories(host, BIZ_HOST_CATEGORY_1);

        host = biz.createOrUpdateHost(HOST_BIZ_1, "UP", "this is a test of updating", BIZ_GROUP_2, BIZ_HOST_CATEGORY_2,
                null, "SEL", null, 7, false, false, false, null);
        assertValidUpdatedHost(host);
        assertGroups(HOST_BIZ_1, BIZ_GROUP_2);
        assertHostCategories(host, BIZ_HOST_CATEGORY_2);

        commitTransaction();

        // Cleanup
        admin.removeHost(HOST_BIZ_1);
        admin.removeHost(HOST_BIZ_2);
        admin.removeHostGroup(BIZ_GROUP_1);
        admin.removeHostGroup(BIZ_GROUP_2);
        collage.getCategoryService().deleteCategoryByName(BIZ_HOST_CATEGORY_1, CategoryService.ENTITY_TYPE_CODE_HOSTCATEGORY);
        collage.getCategoryService().deleteCategoryByName(BIZ_HOST_CATEGORY_2, CategoryService.ENTITY_TYPE_CODE_HOSTCATEGORY);

        // Validate cleanup
        assert collage.getHostService().getHostByHostName(HOST_BIZ_1) == null;
        assert collage.getHostService().getHostByHostName(HOST_BIZ_2) == null;
        assert collage.getHostGroupService().getHostGroupByName(BIZ_GROUP_1) == null;
        assert collage.getHostGroupService().getHostGroupByName(BIZ_GROUP_2) == null;
        assert collage.getCategoryService().getCategoryByName(BIZ_HOST_CATEGORY_1, CategoryService.ENTITY_TYPE_CODE_HOSTCATEGORY) == null;
        assert collage.getCategoryService().getCategoryByName(BIZ_HOST_CATEGORY_2, CategoryService.ENTITY_TYPE_CODE_HOSTCATEGORY) == null;
        System.out.println("... testing Host Biz Services completed");
    }

    public void testServiceBizServices() throws Exception {
        System.out.println("testing Service Biz Services...");
        assert collage != null;
        assert collage.getLogMessageService() != null;
        assert collage.getHostService() != null;
        assert collage.getHostGroupService() != null;
        assert collage.getStatusService() != null;
        assert collage.getCategoryService() != null;
        BizServices biz = (BizServices) collage.getAPIObject(BizServices.SERVICE);
        assert biz != null;

        // Test adding ....
        beginTransaction();
        ServiceStatus service = biz.createOrUpdateService(HOST_BIZ_1, BIZ_SERVICE_1, "PENDING", "this is a service test",
                BIZ_GROUP_1, BIZ_SERVICE_CATEGORY_1, BIZ_GROUP_1, BIZ_HOST_CATEGORY_1, DEVICE_BIZ_1, "SEL", "biz", 6,
                true, false, false, "10", 20, 30, "vm", null, null, false);
        assertValidService(service);
        ServiceStatus service2 = biz.createOrUpdateService(HOST_BIZ_2, BIZ_SERVICE_2, "OK", "this is a service test 2",
                null, null, null, null, DEVICE_BIZ_2, "SEL", "biz", 6, true, false, true, "10", 20, 30, "vm", null, null, false);
        assertValidService2(service2);
        assertGroups(HOST_BIZ_1, BIZ_GROUP_1);
        Host host = collage.getHostService().getHostByHostName(HOST_BIZ_1);
        assert host != null;
        assert BizServicesImpl.lookupLastMonitorStatus(host).equals("PENDING");
        Host host2 = collage.getHostService().getHostByHostName(HOST_BIZ_2);
        assert host2 != null;
        assert BizServicesImpl.lookupLastMonitorStatus(host2).equals("UP");
        assertHostCategories(host, BIZ_HOST_CATEGORY_1);
        assertServiceGroups(service, BIZ_GROUP_1);
        assertServiceCategories(service, BIZ_SERVICE_CATEGORY_1);
        commitTransaction();

        beginTransaction();
        service = collage.getStatusService().getServiceByDescription(BIZ_SERVICE_1, HOST_BIZ_1);
        assertValidService(service);
        service2 = collage.getStatusService().getServiceByDescription(BIZ_SERVICE_2, HOST_BIZ_2);
        assertValidService2(service2);

        // Test Updating ...
        service = biz.createOrUpdateService(HOST_BIZ_1, BIZ_SERVICE_1, "OK", "this is a service test of updating", null,
                null, null, null, null, "SEL", null, 8, false, false, false, "12", -1, -1, "vm", null, null, false);
        assertValidUpdatedService(service);
        assertGroups(HOST_BIZ_1, BIZ_GROUP_1);
        host = collage.getHostService().getHostByHostName(HOST_BIZ_1);
        assert host != null;
        assertHostCategories(host, BIZ_HOST_CATEGORY_1);
        assertServiceGroups(service, BIZ_GROUP_1);
        assertServiceCategories(service, BIZ_SERVICE_CATEGORY_1);

        service = biz.createOrUpdateService(HOST_BIZ_1, BIZ_SERVICE_1, "OK", "this is a service test of updating",
                BIZ_GROUP_2, BIZ_SERVICE_CATEGORY_2, BIZ_GROUP_2, BIZ_HOST_CATEGORY_2, null, "SEL", null, 10, true,
                false, false, null, -1, -1, "vm", null, null, false);
        assertValidUpdatedService(service);
        assertGroups(HOST_BIZ_1, BIZ_GROUP_2);
        host = collage.getHostService().getHostByHostName(HOST_BIZ_1);
        assert host != null;
        assertHostCategories(host, BIZ_HOST_CATEGORY_2);
        assertServiceGroups(service, BIZ_GROUP_2);
        assertServiceCategories(service, BIZ_SERVICE_CATEGORY_2);
        commitTransaction();

        // Cleanup
        admin.removeHost(HOST_BIZ_1);
        admin.removeHost(HOST_BIZ_2);
        admin.removeHostGroup(BIZ_GROUP_1);
        admin.removeHostGroup(BIZ_GROUP_2);
        collage.getCategoryService().deleteCategoryByName(BIZ_GROUP_1, CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP);
        collage.getCategoryService().deleteCategoryByName(BIZ_GROUP_2, CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP);
        collage.getCategoryService().deleteCategoryByName(BIZ_HOST_CATEGORY_1, CategoryService.ENTITY_TYPE_CODE_HOSTCATEGORY);
        collage.getCategoryService().deleteCategoryByName(BIZ_HOST_CATEGORY_2, CategoryService.ENTITY_TYPE_CODE_HOSTCATEGORY);
        collage.getCategoryService().deleteCategoryByName(BIZ_SERVICE_CATEGORY_1, CategoryService.ENTITY_TYPE_CODE_SERVICECATEGORY);
        collage.getCategoryService().deleteCategoryByName(BIZ_SERVICE_CATEGORY_2, CategoryService.ENTITY_TYPE_CODE_SERVICECATEGORY);

        // Validate cleanup
        assert collage.getStatusService().getServiceByDescription(BIZ_SERVICE_1, HOST_BIZ_1) == null;
        assert collage.getStatusService().getServiceByDescription(BIZ_SERVICE_2, HOST_BIZ_2) == null;
        assert collage.getHostService().getHostByHostName(HOST_BIZ_1) == null;
        assert collage.getHostService().getHostByHostName(HOST_BIZ_2) == null;
        assert collage.getHostGroupService().getHostGroupByName(BIZ_GROUP_1) == null;
        assert collage.getHostGroupService().getHostGroupByName(BIZ_GROUP_2) == null;
        assert collage.getCategoryService().getCategoryByName(BIZ_GROUP_1, CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP) == null;
        assert collage.getCategoryService().getCategoryByName(BIZ_GROUP_2, CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP) == null;
        assert collage.getCategoryService().getCategoryByName(BIZ_HOST_CATEGORY_1, CategoryService.ENTITY_TYPE_CODE_HOSTCATEGORY) == null;
        assert collage.getCategoryService().getCategoryByName(BIZ_HOST_CATEGORY_2, CategoryService.ENTITY_TYPE_CODE_HOSTCATEGORY) == null;
        assert collage.getCategoryService().getCategoryByName(BIZ_SERVICE_CATEGORY_1, CategoryService.ENTITY_TYPE_CODE_SERVICECATEGORY) == null;
        assert collage.getCategoryService().getCategoryByName(BIZ_SERVICE_CATEGORY_2, CategoryService.ENTITY_TYPE_CODE_SERVICECATEGORY) == null;
        System.out.println("... testing Service Biz Services completed");
    }

    public void testHostServiceBizServices() throws Exception {
        System.out.println("testing Host Service Biz Services...");
        assert collage != null;
        assert collage.getLogMessageService() != null;
        assert collage.getHostService() != null;
        assert collage.getHostGroupService() != null;
        assert collage.getStatusService() != null;
        assert collage.getCategoryService() != null;
        BizServices biz = (BizServices) collage.getAPIObject(BizServices.SERVICE);
        assert biz != null;

        // Test adding ....
        beginTransaction();
        {
            Map<String, Host> hosts = new HashMap<String, Host>();
            Map<String, HostGroup> hostGroups = new HashMap<String, HostGroup>();
            Map<String, Category> hostCategories = new HashMap<String, Category>();
            Map<String, Device> devices = new HashMap<String, Device>();
            Map<String, ServiceStatus> services = new HashMap<String, ServiceStatus>();
            Map<String, Category> serviceGroups = new HashMap<String, Category>();
            Map<String, Category> serviceCategories = new HashMap<String, Category>();
            Map<String,String> hostDynamicProperties = new HashMap<>();
            Map<String,String> serviceDynamicProperties = new HashMap<>();
            hostDynamicProperties.put("ExecutionTime", "200");
            serviceDynamicProperties.put("Latency", "100");

            Host host = biz.createOrUpdateHost(HOST_BIZ_1, "PENDING", "this is a test", BIZ_GROUP_1, BIZ_HOST_CATEGORY_1,
                    DEVICE_BIZ_1, "SEL", "biz", 3, true, false, false, hosts, hostGroups, hostCategories, devices,
                    hostDynamicProperties, null);
            assertValidHost(host);
            Double execTime = (Double)host.getHostStatus().getProperty("ExecutionTime");
            assert execTime == 200.0;
            assertGroups(HOST_BIZ_1, BIZ_GROUP_1);
            assertHostCategories(host, BIZ_HOST_CATEGORY_1);
            ServiceStatus service = biz.createOrUpdateHostService(host, HOST_BIZ_1, BIZ_SERVICE_1, "PENDING",
                    "this is a service test", BIZ_GROUP_1, BIZ_SERVICE_CATEGORY_1, BIZ_GROUP_1, DEVICE_BIZ_1, "SEL",
                    "biz", 6, false, false, "10", 20, 30, "vm", services, serviceGroups, serviceCategories,
                    serviceDynamicProperties, null, false);
            assertValidService(service);
            Double latency = (Double)service.getProperty("Latency");
            assert latency == 100;
            assertServiceGroups(service, BIZ_GROUP_1);
            assertServiceCategories(service, BIZ_SERVICE_CATEGORY_1);

            assert hosts.size() == 1;
            assert hostGroups.size() == 1;
            assert hostCategories.size() == 1;
            assert devices.size() == 1;
            assert services.size() == 1;
            assert serviceGroups.size() == 1;
            assert serviceCategories.size() == 1;
        }
        commitTransaction();

        beginTransaction();
        {
            Map<String, Host> hosts = new HashMap<String, Host>();
            Map<String, HostGroup> hostGroups = new HashMap<String, HostGroup>();
            Map<String, Category> hostCategories = new HashMap<String, Category>();
            Map<String, Device> devices = new HashMap<String, Device>();
            Map<String, ServiceStatus> services = new HashMap<String, ServiceStatus>();
            Map<String, Category> serviceGroups = new HashMap<String, Category>();
            Map<String, Category> serviceCategories = new HashMap<String, Category>();

            Host host = collage.getHostService().getHostByHostName(HOST_BIZ_1);
            assertValidHost(host);
            ServiceStatus service = collage.getStatusService().getServiceByDescription(BIZ_SERVICE_1, HOST_BIZ_1);
            assertValidService(service);

            Map<String,String> hostDynamicProperties = new HashMap<>();
            Map<String,String> serviceDynamicProperties = new HashMap<>();
            hostDynamicProperties.put("ExecutionTime", "200");
            serviceDynamicProperties.put("Latency", "100");

            // Test Updating ...
            host = biz.createOrUpdateHost(HOST_BIZ_1, "UP", "this is a test of updating", null, null, null, "SEL", null,
                    null, null, null, null, hosts, hostGroups, hostCategories, devices, hostDynamicProperties, null);
            assertValidUpdatedHost(host);
            Double execTime = (Double)host.getHostStatus().getProperty("ExecutionTime");
            assert execTime == 200.0;
            assertGroups(HOST_BIZ_1, BIZ_GROUP_1);
            assertHostCategories(host, BIZ_HOST_CATEGORY_1);

            service = biz.createOrUpdateHostService(host, HOST_BIZ_1, BIZ_SERVICE_1, "OK",
                    "this is a service test of updating", null, null, BIZ_GROUP_1, DEVICE_BIZ_1, "SEL", null, 8, null,
                    null, "12", -1, -1, "vm", services, serviceGroups, serviceCategories, serviceDynamicProperties, null, false);
            assertValidUpdatedService(service);
            Double latency = (Double)service.getProperty("Latency");
            assert latency == 100;

            assertServiceGroups(service, BIZ_GROUP_1);
            assertServiceCategories(service, BIZ_SERVICE_CATEGORY_1);

            host = biz.createOrUpdateHost(HOST_BIZ_1, "UP", "this is a test of updating", BIZ_GROUP_2, BIZ_HOST_CATEGORY_2,
                    null, "SEL", null, 7, false, false, false, hosts, hostGroups, hostCategories, devices, hostDynamicProperties, null);
            assertValidUpdatedHost(host);
            assertGroups(HOST_BIZ_1, BIZ_GROUP_1);
            assertGroups(HOST_BIZ_1, BIZ_GROUP_2);
            assertHostCategories(host, BIZ_HOST_CATEGORY_1);
            assertHostCategories(host, BIZ_HOST_CATEGORY_2);

            service = biz.createOrUpdateHostService(host, HOST_BIZ_1, BIZ_SERVICE_1, "OK",
                    "this is a service test of updating", BIZ_GROUP_2, BIZ_SERVICE_CATEGORY_2, BIZ_GROUP_2, DEVICE_BIZ_1,
                    "SEL", null, 10, null, null, null, -1, -1, "vm", services, serviceGroups, serviceCategories, serviceDynamicProperties, null, false);
            assertValidUpdatedService(service);
            assertServiceGroups(service, BIZ_GROUP_1);
            assertServiceGroups(service, BIZ_GROUP_2);
            assertServiceCategories(service, BIZ_SERVICE_CATEGORY_1);
            assertServiceCategories(service, BIZ_SERVICE_CATEGORY_2);

            assert hosts.size() == 1;
            assert hostGroups.size() == 1;
            assert hostCategories.size() == 1;
            assert devices.size() == 1;
            assert services.size() == 1;
            assert serviceGroups.size() == 1;
            assert serviceCategories.size() == 1;
        }
        commitTransaction();

        // Cleanup
        admin.removeHost(HOST_BIZ_1);
        admin.removeHostGroup(BIZ_GROUP_1);
        admin.removeHostGroup(BIZ_GROUP_2);
        collage.getCategoryService().deleteCategoryByName(BIZ_GROUP_1, CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP);
        collage.getCategoryService().deleteCategoryByName(BIZ_GROUP_2, CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP);
        collage.getCategoryService().deleteCategoryByName(BIZ_HOST_CATEGORY_1, CategoryService.ENTITY_TYPE_CODE_HOSTCATEGORY);
        collage.getCategoryService().deleteCategoryByName(BIZ_HOST_CATEGORY_2, CategoryService.ENTITY_TYPE_CODE_HOSTCATEGORY);
        collage.getCategoryService().deleteCategoryByName(BIZ_SERVICE_CATEGORY_1, CategoryService.ENTITY_TYPE_CODE_SERVICECATEGORY);
        collage.getCategoryService().deleteCategoryByName(BIZ_SERVICE_CATEGORY_2, CategoryService.ENTITY_TYPE_CODE_SERVICECATEGORY);

        // Validate cleanup
        assert collage.getStatusService().getServiceByDescription(BIZ_SERVICE_1, HOST_BIZ_1) == null;
        assert collage.getHostService().getHostByHostName(HOST_BIZ_1) == null;
        assert collage.getHostGroupService().getHostGroupByName(BIZ_GROUP_1) == null;
        assert collage.getHostGroupService().getHostGroupByName(BIZ_GROUP_2) == null;
        assert collage.getCategoryService().getCategoryByName(BIZ_GROUP_1, CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP) == null;
        assert collage.getCategoryService().getCategoryByName(BIZ_GROUP_2, CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP) == null;
        assert collage.getCategoryService().getCategoryByName(BIZ_HOST_CATEGORY_1, CategoryService.ENTITY_TYPE_CODE_HOSTCATEGORY) == null;
        assert collage.getCategoryService().getCategoryByName(BIZ_HOST_CATEGORY_2, CategoryService.ENTITY_TYPE_CODE_HOSTCATEGORY) == null;
        assert collage.getCategoryService().getCategoryByName(BIZ_SERVICE_CATEGORY_1, CategoryService.ENTITY_TYPE_CODE_SERVICECATEGORY) == null;
        assert collage.getCategoryService().getCategoryByName(BIZ_SERVICE_CATEGORY_2, CategoryService.ENTITY_TYPE_CODE_SERVICECATEGORY) == null;
        System.out.println("... testing Host Service Biz Services completed");
    }

    public void assertValidHost(Host host) {
        assert host != null;
        assert host.getHostName().equals(HOST_BIZ_1);
        assert host.getDescription().equals(HOST_BIZ_1);
        assert host.getDevice().getDisplayName().equals(HOST_BIZ_1);
        assert host.getDevice().getIdentification().equals(DEVICE_BIZ_1);
        assert BizServicesImpl.lookupLastMonitorStatus(host).equals("PENDING");
        assert host.getApplicationType().getName().equals("SEL");
        assert host.getAgentId().equals("biz");
        assert host.getHostStatus().getProperty(CollageAdminInfrastructure.PROP_LAST_PLUGIN_OUTPUT).equals("this is a test");
        assert host.getHostStatus().getLastCheckTime() != null;
        assert host.getHostStatus().getNextCheckTime() != null;
    }

    public void assertValidHost2(Host host2) {
        assert host2 != null;
        assert host2.getHostName().equals(HOST_BIZ_2);
        assert host2.getDescription().equals(HOST_BIZ_2);
        assert host2.getDevice().getDisplayName().equals(HOST_BIZ_2);
        assert host2.getDevice().getIdentification().equals(DEVICE_BIZ_2);
        assert BizServicesImpl.lookupLastMonitorStatus(host2).equals("UP");
        assert host2.getApplicationType().getName().equals("SEL");
        assert host2.getAgentId().equals("biz");
        assert host2.getHostStatus().getProperty(CollageAdminInfrastructure.PROP_LAST_PLUGIN_OUTPUT).equals("this is a test 2");
        assert host2.getHostStatus().getLastCheckTime() != null;
        assert host2.getHostStatus().getNextCheckTime() != null;
    }

    public void assertValidUpdatedHost(Host host) {
        assert host != null;
        assert host.getHostName().equals(HOST_BIZ_1);
        assert host.getDescription().equals(HOST_BIZ_1);
        assert host.getDevice().getDisplayName().equals(HOST_BIZ_1);
        assert host.getDevice().getIdentification().equals(DEVICE_BIZ_1);
        assert BizServicesImpl.lookupLastMonitorStatus(host).equals("UP");
        assert host.getApplicationType().getName().equals("SEL");
        assert host.getAgentId().equals("biz");
        assert host.getHostStatus().getProperty(CollageAdminInfrastructure.PROP_LAST_PLUGIN_OUTPUT).equals("this is a test of updating");
        assert host.getHostStatus().getLastCheckTime() != null;
        assert host.getHostStatus().getNextCheckTime() != null;
    }

    public void assertGroups(String hostName, String hostGroup) {
        HostGroup hg = collage.getHostGroupService().getHostGroupByName(hostGroup);
        assert hg != null;
        assert hg.getName().equals(hostGroup);
        Set<Host> hosts = hg.getHosts();
        assert hosts != null;
        boolean found = false;
        for (Host host : hosts) {
            if (host.getHostName().equals(hostName)) {
                found = true;
                break;
            }
        }
        assert found;
    }

    public void assertHostCategories(Host host, String hostCategory) {
        assertCategories(host.getHostId(), CategoryService.ENTITY_TYPE_CODE_HOST,
                hostCategory, CategoryService.ENTITY_TYPE_CODE_HOSTCATEGORY);
    }

    public void assertServiceGroups(ServiceStatus service, String serviceGroup) {
        assertCategories(service.getServiceStatusId(), CategoryService.ENTITY_TYPE_CODE_SERVICESTATUS,
                serviceGroup, CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP);
    }

    public void assertServiceCategories(ServiceStatus service, String serviceCategory) {
        assertCategories(service.getServiceStatusId(), CategoryService.ENTITY_TYPE_CODE_SERVICESTATUS,
                serviceCategory, CategoryService.ENTITY_TYPE_CODE_SERVICECATEGORY);
    }

    private void assertCategories(Integer id, String entityType, String category, String categoryEntityType) {
        Category c = collage.getCategoryService().getCategoryByName(category, categoryEntityType);
        assert c != null;
        assert c.getName().equals(category);
        Collection<CategoryEntity> entities = c.getCategoryEntities();
        assert entities != null;
        boolean found = false;
        for (CategoryEntity entity : entities) {
            if ((entity.getObjectID() != null) &&
                    entity.getObjectID().equals(id) &&
                    (entity.getEntityType() != null) &&
                    entity.getEntityType().getName().equals(entityType)) {
                found = true;
                break;
            }
        }
        assert found;
    }

    public void assertValidService(ServiceStatus service) {
        assert service != null;
        assert service.getServiceDescription().equals(BIZ_SERVICE_1);
        assert service.getHost().getHostName().equals(HOST_BIZ_1);
        assert service.getHost().getDevice().getDisplayName().equals(HOST_BIZ_1);
        assert service.getHost().getDevice().getIdentification().equals(DEVICE_BIZ_1);
        assert service.getMonitorStatus().getName().equals("PENDING");
        assert service.getApplicationType().getName().equals("SEL");
        assert service.getAgentId().equals("biz");
        assert service.getLastCheckTime() != null;
        assert service.getNextCheckTime() != null;
        assert service.getMetricType().equals("vm");

        assert service.getProperty(CollageAdminInfrastructure.PROP_LAST_PLUGIN_OUTPUT).equals("this is a service test");
        assert service.getProperty(CollageAdminInfrastructure.PROP_STATE_TYPE).equals("HARD");
        assert service.getProperty(CollageAdminInfrastructure.PROP_CHECK_TYPE).equals("ACTIVE");
        assert service.getProperty(CollageAdminInfrastructure.PROP_MONITOR_STATUS).equals("PENDING");
        assert service.getProperty(CollageAdminInfrastructure.PROP_PERFORMANCE_DATA).equals("10");
    }

    public void assertValidService2(ServiceStatus service2) {
        assert service2 != null;
        assert service2.getServiceDescription().equals(BIZ_SERVICE_2);
        assert service2.getHost().getHostName().equals(HOST_BIZ_2);
        assert service2.getHost().getDevice().getDisplayName().equals(HOST_BIZ_2);
        assert service2.getHost().getDevice().getIdentification().equals(DEVICE_BIZ_2);
        assert service2.getMonitorStatus().getName().equals("OK");
        assert service2.getApplicationType().getName().equals("SEL");
        assert service2.getAgentId().equals("biz");
        assert service2.getLastCheckTime() != null;
        assert service2.getNextCheckTime() != null;
        assert service2.getMetricType().equals("vm");

        assert service2.getProperty(CollageAdminInfrastructure.PROP_LAST_PLUGIN_OUTPUT).equals("this is a service test 2");
        assert service2.getProperty(CollageAdminInfrastructure.PROP_STATE_TYPE).equals("HARD");
        assert service2.getProperty(CollageAdminInfrastructure.PROP_CHECK_TYPE).equals("ACTIVE");
        assert service2.getProperty(CollageAdminInfrastructure.PROP_MONITOR_STATUS).equals("OK");
        assert service2.getProperty(CollageAdminInfrastructure.PROP_PERFORMANCE_DATA).equals("10");
    }

    public void assertValidUpdatedService(ServiceStatus service) {
        assert service != null;
        assert service.getServiceDescription().equals(BIZ_SERVICE_1);
        assert service.getHost().getHostName().equals(HOST_BIZ_1);
        assert service.getHost().getDevice().getDisplayName().equals(HOST_BIZ_1);
        assert service.getHost().getDevice().getIdentification().equals(DEVICE_BIZ_1);
        assert service.getMonitorStatus().getName().equals("OK");
        assert service.getApplicationType().getName().equals("SEL");
        assert service.getAgentId().equals("biz");
        assert service.getLastCheckTime() != null;

        assert service.getProperty(CollageAdminInfrastructure.PROP_LAST_PLUGIN_OUTPUT).equals("this is a service test of updating");
        assert service.getProperty(CollageAdminInfrastructure.PROP_STATE_TYPE).equals("HARD");
        assert service.getProperty(CollageAdminInfrastructure.PROP_CHECK_TYPE).equals("ACTIVE");
        assert service.getProperty(CollageAdminInfrastructure.PROP_MONITOR_STATUS).equals("OK");
        assert service.getProperty(CollageAdminInfrastructure.PROP_PERFORMANCE_DATA).equals("12");
    }

    public void testDowntimeBizServices() throws Exception {
        System.out.println("testing Downtime Biz Services...");
        // get general and biz services
        LogMessageService logMessageService = collage.getLogMessageService();
        DeviceService deviceService = collage.getDeviceService();
        HostService hostService = collage.getHostService();
        HostGroupService hostGroupService = collage.getHostGroupService();
        CategoryService categoryService = collage.getCategoryService();
        StatusService statusService = collage.getStatusService();
        BizServices bizServices = (BizServices) collage.getAPIObject(BizServices.SERVICE);

        Device testDevice0 = null;
        Device testDevice1 = null;
        Device testDevice2 = null;
        Host testHost0 = null;
        Host testHost1 = null;
        Host testHost2 = null;
        HostGroup testHostGroup0 = null;
        HostGroup testHostGroup1 = null;
        Category testServiceGroupCategory0 = null;
        Category testServiceGroupCategory1 = null;
        try {
            // setup test devices, hosts, services, host groups, and service groups
            beginTransaction();
            MonitorStatus unknownMonitorStatus = metadataService.getMonitorStatusByName("UNKNOWN");
            StateType unknownStateType = metadataService.getStateTypeByName("UNKNOWN");
            CheckType activeCheckType = metadataService.getCheckTypeByName("ACTIVE");
            testDevice0 = deviceService.createDevice("10.0.0.0", "test-device-0");
            testHost0 = hostService.createHost("test-host-0", testDevice0);
            hostService.createHostStatus(null, testHost0);
            ServiceStatus testServiceStatus0 = statusService.createService("test-service-0", "NAGIOS", testHost0);
            testServiceStatus0.setMonitorStatus(unknownMonitorStatus);
            testServiceStatus0.setLastHardState(unknownMonitorStatus);
            testServiceStatus0.setStateType(unknownStateType);
            testServiceStatus0.setCheckType(activeCheckType);
            ServiceStatus testServiceStatus1 = statusService.createService("test-service-1", "NAGIOS", testHost0);
            testServiceStatus1.setMonitorStatus(unknownMonitorStatus);
            testServiceStatus1.setLastHardState(unknownMonitorStatus);
            testServiceStatus1.setStateType(unknownStateType);
            testServiceStatus1.setCheckType(activeCheckType);
            ServiceStatus testServiceStatus2 = statusService.createService("test-service-2", "NAGIOS", testHost0);
            testServiceStatus2.setMonitorStatus(unknownMonitorStatus);
            testServiceStatus2.setLastHardState(unknownMonitorStatus);
            testServiceStatus2.setStateType(unknownStateType);
            testServiceStatus2.setCheckType(activeCheckType);
            testHost0.getServiceStatuses().addAll(Arrays.asList(new ServiceStatus[]{testServiceStatus0, testServiceStatus1, testServiceStatus2}));
            testDevice1 = deviceService.createDevice("10.0.0.1", "test-device-1");
            testHost1 = hostService.createHost("test-host-1", testDevice1);
            hostService.createHostStatus(null, testHost1);
            ServiceStatus testServiceStatus3 = statusService.createService("test-service-0", "NAGIOS", testHost1);
            testServiceStatus3.setMonitorStatus(unknownMonitorStatus);
            testServiceStatus3.setLastHardState(unknownMonitorStatus);
            testServiceStatus3.setStateType(unknownStateType);
            testServiceStatus3.setCheckType(activeCheckType);
            ServiceStatus testServiceStatus4 = statusService.createService("test-service-1", "NAGIOS", testHost1);
            testServiceStatus4.setMonitorStatus(unknownMonitorStatus);
            testServiceStatus4.setLastHardState(unknownMonitorStatus);
            testServiceStatus4.setStateType(unknownStateType);
            testServiceStatus4.setCheckType(activeCheckType);
            testHost1.getServiceStatuses().addAll(Arrays.asList(new ServiceStatus[]{testServiceStatus3, testServiceStatus4}));
            testDevice2 = deviceService.createDevice("10.0.0.2", "test-device-2");
            testHost2 = hostService.createHost("test-host-2", testDevice2);
            hostService.createHostStatus(null, testHost2);
            ServiceStatus testServiceStatus5 = statusService.createService("test-service-0", "NAGIOS", testHost2);
            testServiceStatus5.setMonitorStatus(unknownMonitorStatus);
            testServiceStatus5.setLastHardState(unknownMonitorStatus);
            testServiceStatus5.setStateType(unknownStateType);
            testServiceStatus5.setCheckType(activeCheckType);
            testHost2.getServiceStatuses().add(testServiceStatus5);
            testHostGroup0 = hostGroupService.createHostGroup("test-host-group-0");
            testHostGroup0.getHosts().addAll(Arrays.asList(new Host[]{testHost0, testHost1, testHost2}));
            testHostGroup1 = hostGroupService.createHostGroup("test-host-group-1");
            testHostGroup1.getHosts().addAll(Arrays.asList(new Host[]{testHost2}));
            testHost0.getHostGroups().add(testHostGroup0);
            testHost1.getHostGroups().add(testHostGroup0);
            testHost2.getHostGroups().add(testHostGroup0);
            testHost2.getHostGroups().add(testHostGroup1);
            statusService.saveService(Arrays.asList(new ServiceStatus[]{testServiceStatus0, testServiceStatus1, testServiceStatus2, testServiceStatus3, testServiceStatus4, testServiceStatus5}));
            hostService.saveHost(Arrays.asList(new Host[]{testHost0, testHost1, testHost2}));
            hostGroupService.saveHostGroup(Arrays.asList(new HostGroup[]{testHostGroup0, testHostGroup1}));
            EntityType serviceGroupEntityType = metadataService.getEntityTypeByName(CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP);
            EntityType serviceStatusEntityType = metadataService.getEntityTypeByName(CategoryService.ENTITY_TYPE_CODE_SERVICESTATUS);
            testServiceGroupCategory0 = categoryService.createCategory("test-service-group-category-0", null, serviceGroupEntityType);
            CategoryEntity testServiceGroupCategoryEntity0 = categoryService.createCategoryEntity();
            testServiceGroupCategoryEntity0.setCategory(testServiceGroupCategory0);
            testServiceGroupCategoryEntity0.setEntityType(serviceStatusEntityType);
            testServiceGroupCategoryEntity0.setObjectID(testServiceStatus2.getServiceStatusId());
            CategoryEntity testServiceGroupCategoryEntity1 = categoryService.createCategoryEntity();
            testServiceGroupCategoryEntity1.setCategory(testServiceGroupCategory0);
            testServiceGroupCategoryEntity1.setEntityType(serviceStatusEntityType);
            testServiceGroupCategoryEntity1.setObjectID(testServiceStatus4.getServiceStatusId());
            CategoryEntity testServiceGroupCategoryEntity2 = categoryService.createCategoryEntity();
            testServiceGroupCategoryEntity2.setCategory(testServiceGroupCategory0);
            testServiceGroupCategoryEntity2.setEntityType(serviceStatusEntityType);
            testServiceGroupCategoryEntity2.setObjectID(testServiceStatus5.getServiceStatusId());
            testServiceGroupCategory0.getCategoryEntities().addAll(Arrays.asList(new CategoryEntity[]{testServiceGroupCategoryEntity0, testServiceGroupCategoryEntity1, testServiceGroupCategoryEntity2}));
            testServiceGroupCategory1 = categoryService.createCategory("test-service-group-category-1", null, serviceGroupEntityType);
            CategoryEntity testServiceGroupCategoryEntity3 = categoryService.createCategoryEntity();
            testServiceGroupCategoryEntity3.setCategory(testServiceGroupCategory1);
            testServiceGroupCategoryEntity3.setEntityType(serviceStatusEntityType);
            testServiceGroupCategoryEntity3.setObjectID(testServiceStatus5.getServiceStatusId());
            testServiceGroupCategory1.getCategoryEntities().add(testServiceGroupCategoryEntity3);
            categoryService.saveCategories(Arrays.asList(new Category[]{testServiceGroupCategory0, testServiceGroupCategory1}));
            commitTransaction();

            if (log.isDebugEnabled()) {
                beginTransaction();
                log.debug("Hosts: " + hostService.getHostList());
                for (Host h : (Collection<Host>) hostService.getHosts(null, null, -1, -1).getResults()) {
                    log.debug("Host: " + h.getHostName() + "/" + h.getDevice().getIdentification() + "/" + h.getDevice().getDisplayName());
                    for (ServiceStatus s : (Collection<ServiceStatus>) h.getServiceStatuses()) {
                        log.debug("Service: " + h.getHostName() + "/" + s.getServiceDescription());
                    }
                }
                List<String> hostGroupNames = new ArrayList<String>();
                for (HostGroup hg : (Collection<HostGroup>) hostGroupService.getHostGroups(null, null, -1, -1).getResults()) {
                    hostGroupNames.add(hg.getName());
                    for (Host h : (Collection<Host>) hg.getHosts()) {
                        log.debug("Host Group Host: " + hg.getName() + "/" + h.getHostName());
                    }
                }
                log.debug("Host Groups: " + hostGroupNames);
                List<String> categoryNames = new ArrayList<String>();
                for (Category c : (Collection<Category>) categoryService.getCategories(null, null, -1, -1).getResults()) {
                    List<ServiceStatus> serviceStatuses = statusService.getServicesByCategoryId(c.getCategoryId());
                    if (!serviceStatuses.isEmpty()) {
                        for (ServiceStatus s : serviceStatuses) {
                            log.debug("Service Group Category Service: " + c.getName() + "/" + s.getHost().getHostName() + "/" + s.getServiceDescription());
                        }
                        categoryNames.add(c.getName());
                    }
                }
                log.debug("Service Group Categories: " + categoryNames);
                rollbackTransaction();
            }

            beginTransaction();
            String setInDowntimeTimestamp = DATE_FORMAT.format(new Date(System.currentTimeMillis()-1000L));
            // set hosts in downtime
            List<BizServices.HostServiceInDowntime> hostsAndServices0 = bizServices.setHostsAndServicesInDowntime(Arrays.asList(new String[]{"test-host-0", "test-host-1"}), null, null, null, true, false);
            assertNotNull(hostsAndServices0);
            assertEquals(2, hostsAndServices0.size());
            assertEquals("test-host-0", hostsAndServices0.get(0).hostName);
            assertEquals(new Integer(1), hostsAndServices0.get(0).scheduledDowntimeDepth);
            assertEquals(Host.ENTITY_TYPE_CODE, hostsAndServices0.get(0).entityType);
            assertEquals("test-host-0", hostsAndServices0.get(0).entityName);
            assertEquals("test-host-1", hostsAndServices0.get(1).hostName);
            assertEquals(new Integer(1), hostsAndServices0.get(1).scheduledDowntimeDepth);
            assertEquals(Host.ENTITY_TYPE_CODE, hostsAndServices0.get(1).entityType);
            assertEquals("test-host-1", hostsAndServices0.get(1).entityName);
            testHost0 = hostService.getHostByHostName("test-host-0");
            assertNotNull(testHost0);
            assertNotNull(testHost0.getHostStatus());
            assertEquals(new Integer(1), testHost0.getHostStatus().getProperty(Nagios.SCHEDULED_DOWNTIME_DEPTH));
            testHost1 = hostService.getHostByHostName("test-host-1");
            assertNotNull(testHost1);
            assertNotNull(testHost1.getHostStatus());
            assertEquals(new Integer(1), testHost1.getHostStatus().getProperty(Nagios.SCHEDULED_DOWNTIME_DEPTH));
            List<LogMessage> logMessages = logMessageService.getLogMessagesByHostName("test-host-0", setInDowntimeTimestamp, null, null, LOGMESSAGE_SORT_CRITERIA, -1, -1).getResults();
            assertFalse(logMessages.isEmpty());
            assertEquals(testHost0.getHostStatus(), logMessages.get(0).getHostStatus());
            assertEquals(DOWNTIME_APP_TYPE, logMessages.get(0).getApplicationType().getName());
            assertEquals(START_DOWNTIME_MONITOR_STATUS, logMessages.get(0).getMonitorStatus().getName());
            assertTrue(logMessages.get(0).getTextMessage().contains("test-host-0"));
            logMessages = logMessageService.getLogMessagesByHostName("test-host-1", setInDowntimeTimestamp, null, null, LOGMESSAGE_SORT_CRITERIA, -1, -1).getResults();
            assertFalse(logMessages.isEmpty());
            assertEquals(testHost1.getHostStatus(), logMessages.get(0).getHostStatus());
            assertEquals(DOWNTIME_APP_TYPE, logMessages.get(0).getApplicationType().getName());
            assertEquals(START_DOWNTIME_MONITOR_STATUS, logMessages.get(0).getMonitorStatus().getName());
            assertTrue(logMessages.get(0).getTextMessage().contains("test-host-1"));
            // set host services in downtime
            List<BizServices.HostServiceInDowntime> hostsAndServices1 = bizServices.setHostsAndServicesInDowntime(Arrays.asList(new String[]{"test-host-2"}), Arrays.asList(new String[]{"*"}), null, null, false, true);
            assertNotNull(hostsAndServices1);
            assertEquals(1, hostsAndServices1.size());
            assertEquals("test-host-2", hostsAndServices1.get(0).hostName);
            assertEquals("test-service-0", hostsAndServices1.get(0).serviceDescription);
            assertEquals(new Integer(1), hostsAndServices1.get(0).scheduledDowntimeDepth);
            assertEquals(Host.ENTITY_TYPE_CODE, hostsAndServices1.get(0).entityType);
            assertEquals("test-host-2", hostsAndServices1.get(0).entityName);
            testHost2 = hostService.getHostByHostName("test-host-2");
            assertNotNull(testHost2);
            assertNotNull(testHost2.getHostStatus());
            assertNull(testHost2.getHostStatus().getProperty(Nagios.SCHEDULED_DOWNTIME_DEPTH));
            testServiceStatus5 = testHost2.getServiceStatus("test-service-0");
            assertNotNull(testServiceStatus5);
            assertEquals(new Integer(1), testServiceStatus5.getProperty(Nagios.SCHEDULED_DOWNTIME_DEPTH));
            logMessages = logMessageService.getLogMessagesByHostName("test-host-2", setInDowntimeTimestamp, null, null, LOGMESSAGE_SORT_CRITERIA, -1, -1).getResults();
            assertFalse(logMessages.isEmpty());
            assertEquals(testHost2.getHostStatus(), logMessages.get(0).getHostStatus());
            assertEquals(testServiceStatus5, logMessages.get(0).getServiceStatus());
            assertEquals(DOWNTIME_APP_TYPE, logMessages.get(0).getApplicationType().getName());
            assertEquals(START_DOWNTIME_MONITOR_STATUS, logMessages.get(0).getMonitorStatus().getName());
            assertTrue(logMessages.get(0).getTextMessage().contains("test-host-2"));
            // set host groups in downtime
            List<BizServices.HostServiceInDowntime> hostsAndServices2 = bizServices.setHostsAndServicesInDowntime(Arrays.asList(new String[]{"*"}), null, Arrays.asList(new String[]{"test-host-group-0", "test-host-group-1"}), null, true, false);
            assertNotNull(hostsAndServices2);
            assertEquals(4, hostsAndServices2.size());
            int testHost0Index = hostsAndServicesIndexOf(hostsAndServices2, "test-host-0", null, 0, 3);
            assertFalse(testHost0Index == -1);
            assertEquals("test-host-0", hostsAndServices2.get(testHost0Index).hostName);
            assertEquals(new Integer(2), hostsAndServices2.get(testHost0Index).scheduledDowntimeDepth);
            assertEquals(HostGroup.ENTITY_TYPE_CODE, hostsAndServices2.get(testHost0Index).entityType);
            assertEquals("test-host-group-0", hostsAndServices2.get(testHost0Index).entityName);
            int testHost1Index = hostsAndServicesIndexOf(hostsAndServices2, "test-host-1", null, 0, 3);
            assertFalse(testHost1Index == -1);
            assertEquals("test-host-1", hostsAndServices2.get(testHost1Index).hostName);
            assertEquals(new Integer(2), hostsAndServices2.get(testHost1Index).scheduledDowntimeDepth);
            assertEquals(HostGroup.ENTITY_TYPE_CODE, hostsAndServices2.get(testHost1Index).entityType);
            assertEquals("test-host-group-0", hostsAndServices2.get(testHost1Index).entityName);
            int testHost2Index = hostsAndServicesIndexOf(hostsAndServices2, "test-host-2", null, 0, 3);
            assertFalse(testHost2Index == -1);
            assertEquals("test-host-2", hostsAndServices2.get(testHost2Index).hostName);
            assertEquals(new Integer(1), hostsAndServices2.get(testHost2Index).scheduledDowntimeDepth);
            assertEquals(HostGroup.ENTITY_TYPE_CODE, hostsAndServices2.get(testHost2Index).entityType);
            assertEquals("test-host-group-0", hostsAndServices2.get(testHost2Index).entityName);
            assertEquals("test-host-2", hostsAndServices2.get(3).hostName);
            assertEquals(new Integer(2), hostsAndServices2.get(3).scheduledDowntimeDepth);
            assertEquals(HostGroup.ENTITY_TYPE_CODE, hostsAndServices2.get(3).entityType);
            assertEquals("test-host-group-1", hostsAndServices2.get(3).entityName);
            testHost0 = hostService.getHostByHostName("test-host-0");
            assertNotNull(testHost0);
            assertNotNull(testHost0.getHostStatus());
            assertEquals(new Integer(2), testHost0.getHostStatus().getProperty(Nagios.SCHEDULED_DOWNTIME_DEPTH));
            testHost1 = hostService.getHostByHostName("test-host-1");
            assertNotNull(testHost1);
            assertNotNull(testHost1.getHostStatus());
            assertEquals(new Integer(2), testHost1.getHostStatus().getProperty(Nagios.SCHEDULED_DOWNTIME_DEPTH));
            testHost2 = hostService.getHostByHostName("test-host-2");
            assertNotNull(testHost2);
            assertNotNull(testHost2.getHostStatus());
            assertEquals(new Integer(2), testHost2.getHostStatus().getProperty(Nagios.SCHEDULED_DOWNTIME_DEPTH));
            logMessages = logMessageService.getLogMessagesByHostName("test-host-0", setInDowntimeTimestamp, null, null, LOGMESSAGE_SORT_CRITERIA, -1, -1).getResults();
            assertFalse(logMessages.isEmpty());
            assertEquals(testHost0.getHostStatus(), logMessages.get(0).getHostStatus());
            assertEquals(DOWNTIME_APP_TYPE, logMessages.get(0).getApplicationType().getName());
            assertEquals(IN_DOWNTIME_MONITOR_STATUS, logMessages.get(0).getMonitorStatus().getName());
            assertTrue(logMessages.get(0).getTextMessage().contains("test-host-group-0"));
            logMessages = logMessageService.getLogMessagesByHostName("test-host-1", setInDowntimeTimestamp, null, null, LOGMESSAGE_SORT_CRITERIA, -1, -1).getResults();
            assertFalse(logMessages.isEmpty());
            assertEquals(testHost1.getHostStatus(), logMessages.get(0).getHostStatus());
            assertEquals(DOWNTIME_APP_TYPE, logMessages.get(0).getApplicationType().getName());
            assertEquals(IN_DOWNTIME_MONITOR_STATUS, logMessages.get(0).getMonitorStatus().getName());
            assertTrue(logMessages.get(0).getTextMessage().contains("test-host-group-0"));
            logMessages = logMessageService.getLogMessagesByHostName("test-host-2", setInDowntimeTimestamp, null, null, LOGMESSAGE_SORT_CRITERIA, -1, -1).getResults();
            assertFalse(logMessages.size() < 2);
            assertEquals(testHost2.getHostStatus(), logMessages.get(1).getHostStatus());
            assertEquals(DOWNTIME_APP_TYPE, logMessages.get(1).getApplicationType().getName());
            assertEquals(START_DOWNTIME_MONITOR_STATUS, logMessages.get(1).getMonitorStatus().getName());
            assertTrue(logMessages.get(1).getTextMessage().contains("test-host-group-0"));
            assertEquals(testHost2.getHostStatus(), logMessages.get(0).getHostStatus());
            assertEquals(DOWNTIME_APP_TYPE, logMessages.get(0).getApplicationType().getName());
            assertEquals(IN_DOWNTIME_MONITOR_STATUS, logMessages.get(0).getMonitorStatus().getName());
            assertTrue(logMessages.get(0).getTextMessage().contains("test-host-group-1"));
            // set service groups in downtime
            List<BizServices.HostServiceInDowntime> hostsAndServices3 = bizServices.setHostsAndServicesInDowntime(null, null, null, Arrays.asList(new String[]{"test-service-group-category-0", "test-service-group-category-1"}), false, true);
            assertNotNull(hostsAndServices3);
            assertEquals(4, hostsAndServices3.size());
            testHost0Index = hostsAndServicesIndexOf(hostsAndServices3, "test-host-0", "test-service-2", 0, 3);
            assertFalse(testHost0Index == -1);
            assertEquals("test-host-0", hostsAndServices3.get(testHost0Index).hostName);
            assertEquals("test-service-2", hostsAndServices3.get(testHost0Index).serviceDescription);
            assertEquals(new Integer(1), hostsAndServices3.get(testHost0Index).scheduledDowntimeDepth);
            assertEquals(CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP, hostsAndServices3.get(testHost0Index).entityType);
            assertEquals("test-service-group-category-0", hostsAndServices3.get(testHost0Index).entityName);
            testHost1Index = hostsAndServicesIndexOf(hostsAndServices3, "test-host-1", "test-service-1", 0, 3);
            assertFalse(testHost1Index == -1);
            assertEquals("test-host-1", hostsAndServices3.get(testHost1Index).hostName);
            assertEquals("test-service-1", hostsAndServices3.get(testHost1Index).serviceDescription);
            assertEquals(new Integer(1), hostsAndServices3.get(testHost1Index).scheduledDowntimeDepth);
            assertEquals(CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP, hostsAndServices3.get(testHost1Index).entityType);
            assertEquals("test-service-group-category-0", hostsAndServices3.get(testHost1Index).entityName);
            testHost2Index = hostsAndServicesIndexOf(hostsAndServices3, "test-host-2", "test-service-0", 0, 3);
            assertFalse(testHost2Index == -1);
            assertEquals("test-host-2", hostsAndServices3.get(testHost2Index).hostName);
            assertEquals("test-service-0", hostsAndServices3.get(testHost2Index).serviceDescription);
            assertEquals(new Integer(2), hostsAndServices3.get(testHost2Index).scheduledDowntimeDepth);
            assertEquals(CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP, hostsAndServices3.get(testHost2Index).entityType);
            assertEquals("test-service-group-category-0", hostsAndServices3.get(testHost2Index).entityName);
            assertEquals("test-host-2", hostsAndServices3.get(3).hostName);
            assertEquals("test-service-0", hostsAndServices3.get(3).serviceDescription);
            assertEquals(new Integer(3), hostsAndServices3.get(3).scheduledDowntimeDepth);
            assertEquals(CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP, hostsAndServices3.get(3).entityType);
            assertEquals("test-service-group-category-1", hostsAndServices3.get(3).entityName);
            testHost0 = hostService.getHostByHostName("test-host-0");
            assertNotNull(testHost0);
            assertNotNull(testHost0.getHostStatus());
            assertEquals(new Integer(2), testHost0.getHostStatus().getProperty(Nagios.SCHEDULED_DOWNTIME_DEPTH));
            assertEquals(new Integer(1), testHost0.getServiceStatus("test-service-2").getProperty(Nagios.SCHEDULED_DOWNTIME_DEPTH));
            testHost1 = hostService.getHostByHostName("test-host-1");
            assertNotNull(testHost1);
            assertNotNull(testHost1.getHostStatus());
            assertEquals(new Integer(2), testHost1.getHostStatus().getProperty(Nagios.SCHEDULED_DOWNTIME_DEPTH));
            assertEquals(new Integer(1), testHost1.getServiceStatus("test-service-1").getProperty(Nagios.SCHEDULED_DOWNTIME_DEPTH));
            testHost2 = hostService.getHostByHostName("test-host-2");
            assertNotNull(testHost2);
            assertNotNull(testHost2.getHostStatus());
            assertEquals(new Integer(2), testHost2.getHostStatus().getProperty(Nagios.SCHEDULED_DOWNTIME_DEPTH));
            assertEquals(new Integer(3), testHost2.getServiceStatus("test-service-0").getProperty(Nagios.SCHEDULED_DOWNTIME_DEPTH));
            logMessages = logMessageService.getLogMessagesByHostName("test-host-0", setInDowntimeTimestamp, null, null, LOGMESSAGE_SORT_CRITERIA, -1, -1).getResults();
            assertFalse(logMessages.isEmpty());
            assertEquals(testHost0.getHostStatus(), logMessages.get(0).getHostStatus());
            assertEquals(testHost0.getServiceStatus("test-service-2"), logMessages.get(0).getServiceStatus());
            assertEquals(DOWNTIME_APP_TYPE, logMessages.get(0).getApplicationType().getName());
            assertEquals(START_DOWNTIME_MONITOR_STATUS, logMessages.get(0).getMonitorStatus().getName());
            assertTrue(logMessages.get(0).getTextMessage().contains("test-service-group-category-0"));
            logMessages = logMessageService.getLogMessagesByHostName("test-host-1", setInDowntimeTimestamp, null, null, LOGMESSAGE_SORT_CRITERIA, -1, -1).getResults();
            assertFalse(logMessages.isEmpty());
            assertEquals(testHost1.getHostStatus(), logMessages.get(0).getHostStatus());
            assertEquals(testHost1.getServiceStatus("test-service-1"), logMessages.get(0).getServiceStatus());
            assertEquals(DOWNTIME_APP_TYPE, logMessages.get(0).getApplicationType().getName());
            assertEquals(START_DOWNTIME_MONITOR_STATUS, logMessages.get(0).getMonitorStatus().getName());
            assertTrue(logMessages.get(0).getTextMessage().contains("test-service-group-category-0"));
            logMessages = logMessageService.getLogMessagesByHostName("test-host-2", setInDowntimeTimestamp, null, null, LOGMESSAGE_SORT_CRITERIA, -1, -1).getResults();
            assertFalse(logMessages.size() < 2);
            assertEquals(testHost2.getHostStatus(), logMessages.get(1).getHostStatus());
            assertEquals(testHost2.getServiceStatus("test-service-0"), logMessages.get(0).getServiceStatus());
            assertEquals(DOWNTIME_APP_TYPE, logMessages.get(1).getApplicationType().getName());
            assertEquals(IN_DOWNTIME_MONITOR_STATUS, logMessages.get(1).getMonitorStatus().getName());
            assertTrue(logMessages.get(1).getTextMessage().contains("test-service-group-category-0"));
            assertEquals(testHost2.getHostStatus(), logMessages.get(0).getHostStatus());
            assertEquals(testHost2.getServiceStatus("test-service-0"), logMessages.get(0).getServiceStatus());
            assertEquals(DOWNTIME_APP_TYPE, logMessages.get(0).getApplicationType().getName());
            assertEquals(IN_DOWNTIME_MONITOR_STATUS, logMessages.get(0).getMonitorStatus().getName());
            assertTrue(logMessages.get(0).getTextMessage().contains("test-service-group-category-1"));
            commitTransaction();

            beginTransaction();
            // get hosts in downtime
            hostsAndServices0 = bizServices.getHostsAndServicesInDowntime(hostsAndServices0);
            assertNotNull(hostsAndServices0);
            assertEquals(2, hostsAndServices0.size());
            assertEquals("test-host-0", hostsAndServices0.get(0).hostName);
            assertEquals(new Integer(2), hostsAndServices0.get(0).scheduledDowntimeDepth);
            assertEquals(Host.ENTITY_TYPE_CODE, hostsAndServices0.get(0).entityType);
            assertEquals("test-host-0", hostsAndServices0.get(0).entityName);
            assertEquals("test-host-1", hostsAndServices0.get(1).hostName);
            assertEquals(new Integer(2), hostsAndServices0.get(1).scheduledDowntimeDepth);
            assertEquals(Host.ENTITY_TYPE_CODE, hostsAndServices0.get(1).entityType);
            assertEquals("test-host-1", hostsAndServices0.get(1).entityName);
            // get host services in downtime
            hostsAndServices1 = bizServices.getHostsAndServicesInDowntime(hostsAndServices1);
            assertNotNull(hostsAndServices1);
            assertEquals(1, hostsAndServices1.size());
            assertEquals("test-host-2", hostsAndServices1.get(0).hostName);
            assertEquals("test-service-0", hostsAndServices1.get(0).serviceDescription);
            assertEquals(new Integer(3), hostsAndServices1.get(0).scheduledDowntimeDepth);
            assertEquals(Host.ENTITY_TYPE_CODE, hostsAndServices1.get(0).entityType);
            assertEquals("test-host-2", hostsAndServices1.get(0).entityName);
            // get host groups in downtime
            hostsAndServices2 = bizServices.getHostsAndServicesInDowntime(hostsAndServices2);
            assertNotNull(hostsAndServices2);
            assertEquals(4, hostsAndServices2.size());
            testHost0Index = hostsAndServicesIndexOf(hostsAndServices2, "test-host-0", null, 0, 3);
            assertFalse(testHost0Index == -1);
            assertEquals("test-host-0", hostsAndServices2.get(testHost0Index).hostName);
            assertEquals(new Integer(2), hostsAndServices2.get(testHost0Index).scheduledDowntimeDepth);
            assertEquals(HostGroup.ENTITY_TYPE_CODE, hostsAndServices2.get(testHost0Index).entityType);
            assertEquals("test-host-group-0", hostsAndServices2.get(testHost0Index).entityName);
            testHost1Index = hostsAndServicesIndexOf(hostsAndServices2, "test-host-1", null, 0, 3);
            assertFalse(testHost1Index == -1);
            assertEquals("test-host-1", hostsAndServices2.get(testHost1Index).hostName);
            assertEquals(new Integer(2), hostsAndServices2.get(testHost1Index).scheduledDowntimeDepth);
            assertEquals(HostGroup.ENTITY_TYPE_CODE, hostsAndServices2.get(testHost1Index).entityType);
            assertEquals("test-host-group-0", hostsAndServices2.get(testHost1Index).entityName);
            testHost2Index = hostsAndServicesIndexOf(hostsAndServices2, "test-host-2", null, 0, 3);
            assertFalse(testHost2Index == -1);
            assertEquals("test-host-2", hostsAndServices2.get(testHost2Index).hostName);
            assertEquals(new Integer(2), hostsAndServices2.get(testHost2Index).scheduledDowntimeDepth);
            assertEquals(HostGroup.ENTITY_TYPE_CODE, hostsAndServices2.get(testHost2Index).entityType);
            assertEquals("test-host-group-0", hostsAndServices2.get(testHost2Index).entityName);
            assertEquals("test-host-2", hostsAndServices2.get(3).hostName);
            assertEquals(new Integer(2), hostsAndServices2.get(3).scheduledDowntimeDepth);
            assertEquals(HostGroup.ENTITY_TYPE_CODE, hostsAndServices2.get(3).entityType);
            assertEquals("test-host-group-1", hostsAndServices2.get(3).entityName);
            // get service groups in downtime
            hostsAndServices3 = bizServices.getHostsAndServicesInDowntime(hostsAndServices3);
            assertNotNull(hostsAndServices3);
            assertEquals(4, hostsAndServices3.size());
            testHost0Index = hostsAndServicesIndexOf(hostsAndServices3, "test-host-0", "test-service-2", 0, 3);
            assertFalse(testHost0Index == -1);
            assertEquals("test-host-0", hostsAndServices3.get(testHost0Index).hostName);
            assertEquals("test-service-2", hostsAndServices3.get(testHost0Index).serviceDescription);
            assertEquals(new Integer(1), hostsAndServices3.get(testHost0Index).scheduledDowntimeDepth);
            assertEquals(CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP, hostsAndServices3.get(testHost0Index).entityType);
            assertEquals("test-service-group-category-0", hostsAndServices3.get(testHost0Index).entityName);
            testHost1Index = hostsAndServicesIndexOf(hostsAndServices3, "test-host-1", "test-service-1", 0, 3);
            assertFalse(testHost1Index == -1);
            assertEquals("test-host-1", hostsAndServices3.get(testHost1Index).hostName);
            assertEquals("test-service-1", hostsAndServices3.get(testHost1Index).serviceDescription);
            assertEquals(new Integer(1), hostsAndServices3.get(testHost1Index).scheduledDowntimeDepth);
            assertEquals(CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP, hostsAndServices3.get(testHost1Index).entityType);
            assertEquals("test-service-group-category-0", hostsAndServices3.get(testHost1Index).entityName);
            testHost2Index = hostsAndServicesIndexOf(hostsAndServices3, "test-host-2", "test-service-0", 0, 3);
            assertFalse(testHost2Index == -1);
            assertEquals("test-host-2", hostsAndServices3.get(testHost2Index).hostName);
            assertEquals("test-service-0", hostsAndServices3.get(testHost2Index).serviceDescription);
            assertEquals(new Integer(3), hostsAndServices3.get(testHost2Index).scheduledDowntimeDepth);
            assertEquals(CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP, hostsAndServices3.get(testHost2Index).entityType);
            assertEquals("test-service-group-category-0", hostsAndServices3.get(testHost2Index).entityName);
            assertEquals("test-host-2", hostsAndServices3.get(3).hostName);
            assertEquals("test-service-0", hostsAndServices3.get(3).serviceDescription);
            assertEquals(new Integer(3), hostsAndServices3.get(3).scheduledDowntimeDepth);
            assertEquals(CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP, hostsAndServices3.get(3).entityType);
            assertEquals("test-service-group-category-1", hostsAndServices3.get(3).entityName);
            rollbackTransaction();

            beginTransaction();
            String clearInDowntimeTimestamp = DATE_FORMAT.format(new Date(System.currentTimeMillis()-1000L));
            // clear service groups in downtime
            hostsAndServices3 = bizServices.clearHostsAndServicesInDowntime(hostsAndServices3);
            assertNotNull(hostsAndServices3);
            assertEquals(4, hostsAndServices3.size());
            assertEquals("test-host-2", hostsAndServices3.get(0).hostName);
            assertEquals("test-service-0", hostsAndServices3.get(0).serviceDescription);
            assertEquals(new Integer(2), hostsAndServices3.get(0).scheduledDowntimeDepth);
            assertEquals(CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP, hostsAndServices3.get(0).entityType);
            assertEquals("test-service-group-category-1", hostsAndServices3.get(0).entityName);
            testHost2Index = hostsAndServicesIndexOf(hostsAndServices3, "test-host-2", "test-service-0", 1);
            assertFalse(testHost2Index == -1);
            assertEquals("test-host-2", hostsAndServices3.get(testHost2Index).hostName);
            assertEquals("test-service-0", hostsAndServices3.get(testHost2Index).serviceDescription);
            assertEquals(new Integer(1), hostsAndServices3.get(testHost2Index).scheduledDowntimeDepth);
            assertEquals(CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP, hostsAndServices3.get(testHost2Index).entityType);
            assertEquals("test-service-group-category-0", hostsAndServices3.get(testHost2Index).entityName);
            testHost1Index = hostsAndServicesIndexOf(hostsAndServices3, "test-host-1", "test-service-1", 1);
            assertFalse(testHost1Index == -1);
            assertEquals("test-host-1", hostsAndServices3.get(testHost1Index).hostName);
            assertEquals("test-service-1", hostsAndServices3.get(testHost1Index).serviceDescription);
            assertEquals(new Integer(0), hostsAndServices3.get(testHost1Index).scheduledDowntimeDepth);
            assertEquals(CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP, hostsAndServices3.get(testHost1Index).entityType);
            assertEquals("test-service-group-category-0", hostsAndServices3.get(testHost1Index).entityName);
            testHost0Index = hostsAndServicesIndexOf(hostsAndServices3, "test-host-0", "test-service-2", 1);
            assertFalse(testHost0Index == -1);
            assertEquals("test-host-0", hostsAndServices3.get(testHost0Index).hostName);
            assertEquals("test-service-2", hostsAndServices3.get(testHost0Index).serviceDescription);
            assertEquals(new Integer(0), hostsAndServices3.get(testHost0Index).scheduledDowntimeDepth);
            assertEquals(CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP, hostsAndServices3.get(testHost0Index).entityType);
            assertEquals("test-service-group-category-0", hostsAndServices3.get(testHost0Index).entityName);
            testHost0 = hostService.getHostByHostName("test-host-0");
            assertNotNull(testHost0);
            assertNotNull(testHost0.getHostStatus());
            assertEquals(new Integer(2), testHost0.getHostStatus().getProperty(Nagios.SCHEDULED_DOWNTIME_DEPTH));
            assertEquals(new Integer(0), testHost0.getServiceStatus("test-service-2").getProperty(Nagios.SCHEDULED_DOWNTIME_DEPTH));
            testHost1 = hostService.getHostByHostName("test-host-1");
            assertNotNull(testHost1);
            assertNotNull(testHost1.getHostStatus());
            assertEquals(new Integer(2), testHost1.getHostStatus().getProperty(Nagios.SCHEDULED_DOWNTIME_DEPTH));
            assertEquals(new Integer(0), testHost1.getServiceStatus("test-service-1").getProperty(Nagios.SCHEDULED_DOWNTIME_DEPTH));
            testHost2 = hostService.getHostByHostName("test-host-2");
            assertNotNull(testHost2);
            assertNotNull(testHost2.getHostStatus());
            assertEquals(new Integer(2), testHost2.getHostStatus().getProperty(Nagios.SCHEDULED_DOWNTIME_DEPTH));
            assertEquals(new Integer(1), testHost2.getServiceStatus("test-service-0").getProperty(Nagios.SCHEDULED_DOWNTIME_DEPTH));
            logMessages = logMessageService.getLogMessagesByHostName("test-host-0", clearInDowntimeTimestamp, null, null, LOGMESSAGE_SORT_CRITERIA, -1, -1).getResults();
            assertFalse(logMessages.isEmpty());
            assertEquals(testHost0.getHostStatus(), logMessages.get(0).getHostStatus());
            assertEquals(testHost0.getServiceStatus("test-service-2"), logMessages.get(0).getServiceStatus());
            assertEquals(DOWNTIME_APP_TYPE, logMessages.get(0).getApplicationType().getName());
            assertEquals(END_DOWNTIME_MONITOR_STATUS, logMessages.get(0).getMonitorStatus().getName());
            assertTrue(logMessages.get(0).getTextMessage().contains("test-service-group-category-0"));
            logMessages = logMessageService.getLogMessagesByHostName("test-host-1", clearInDowntimeTimestamp, null, null, LOGMESSAGE_SORT_CRITERIA, -1, -1).getResults();
            assertFalse(logMessages.isEmpty());
            assertEquals(testHost1.getHostStatus(), logMessages.get(0).getHostStatus());
            assertEquals(testHost1.getServiceStatus("test-service-1"), logMessages.get(0).getServiceStatus());
            assertEquals(DOWNTIME_APP_TYPE, logMessages.get(0).getApplicationType().getName());
            assertEquals(END_DOWNTIME_MONITOR_STATUS, logMessages.get(0).getMonitorStatus().getName());
            assertTrue(logMessages.get(0).getTextMessage().contains("test-service-group-category-0"));
            logMessages = logMessageService.getLogMessagesByHostName("test-host-2", clearInDowntimeTimestamp, null, null, LOGMESSAGE_SORT_CRITERIA, -1, -1).getResults();
            assertFalse(logMessages.size() < 2);
            assertEquals(testHost2.getHostStatus(), logMessages.get(1).getHostStatus());
            assertEquals(testHost2.getServiceStatus("test-service-0"), logMessages.get(0).getServiceStatus());
            assertEquals(DOWNTIME_APP_TYPE, logMessages.get(1).getApplicationType().getName());
            assertEquals(IN_DOWNTIME_MONITOR_STATUS, logMessages.get(1).getMonitorStatus().getName());
            assertTrue(logMessages.get(1).getTextMessage().contains("test-service-group-category-1"));
            assertEquals(testHost2.getHostStatus(), logMessages.get(0).getHostStatus());
            assertEquals(testHost2.getServiceStatus("test-service-0"), logMessages.get(0).getServiceStatus());
            assertEquals(DOWNTIME_APP_TYPE, logMessages.get(0).getApplicationType().getName());
            assertEquals(IN_DOWNTIME_MONITOR_STATUS, logMessages.get(0).getMonitorStatus().getName());
            assertTrue(logMessages.get(0).getTextMessage().contains("test-service-group-category-0"));
            // clear host groups in downtime
            hostsAndServices2 = bizServices.clearHostsAndServicesInDowntime(hostsAndServices2);
            assertNotNull(hostsAndServices2);
            assertEquals(4, hostsAndServices2.size());
            assertEquals("test-host-2", hostsAndServices2.get(0).hostName);
            assertEquals(new Integer(1), hostsAndServices2.get(0).scheduledDowntimeDepth);
            assertEquals(HostGroup.ENTITY_TYPE_CODE, hostsAndServices2.get(0).entityType);
            assertEquals("test-host-group-1", hostsAndServices2.get(0).entityName);
            testHost2Index = hostsAndServicesIndexOf(hostsAndServices2, "test-host-2", null, 1);
            assertFalse(testHost2Index == -1);
            assertEquals("test-host-2", hostsAndServices2.get(testHost2Index).hostName);
            assertEquals(new Integer(0), hostsAndServices2.get(testHost2Index).scheduledDowntimeDepth);
            assertEquals(HostGroup.ENTITY_TYPE_CODE, hostsAndServices2.get(testHost2Index).entityType);
            assertEquals("test-host-group-0", hostsAndServices2.get(testHost2Index).entityName);
            testHost1Index = hostsAndServicesIndexOf(hostsAndServices2, "test-host-1", null, 1);
            assertFalse(testHost1Index == -1);
            assertEquals("test-host-1", hostsAndServices2.get(testHost1Index).hostName);
            assertEquals(new Integer(1), hostsAndServices2.get(testHost1Index).scheduledDowntimeDepth);
            assertEquals(HostGroup.ENTITY_TYPE_CODE, hostsAndServices2.get(testHost1Index).entityType);
            assertEquals("test-host-group-0", hostsAndServices2.get(testHost1Index).entityName);
            testHost0Index = hostsAndServicesIndexOf(hostsAndServices2, "test-host-0", null, 1);
            assertFalse(testHost0Index == -1);
            assertEquals("test-host-0", hostsAndServices2.get(testHost0Index).hostName);
            assertEquals(new Integer(1), hostsAndServices2.get(testHost0Index).scheduledDowntimeDepth);
            assertEquals(HostGroup.ENTITY_TYPE_CODE, hostsAndServices2.get(testHost0Index).entityType);
            assertEquals("test-host-group-0", hostsAndServices2.get(testHost0Index).entityName);
            testHost0 = hostService.getHostByHostName("test-host-0");
            assertNotNull(testHost0);
            assertNotNull(testHost0.getHostStatus());
            assertEquals(new Integer(1), testHost0.getHostStatus().getProperty(Nagios.SCHEDULED_DOWNTIME_DEPTH));
            testHost1 = hostService.getHostByHostName("test-host-1");
            assertNotNull(testHost1);
            assertNotNull(testHost1.getHostStatus());
            assertEquals(new Integer(1), testHost1.getHostStatus().getProperty(Nagios.SCHEDULED_DOWNTIME_DEPTH));
            testHost2 = hostService.getHostByHostName("test-host-2");
            assertNotNull(testHost2);
            assertNotNull(testHost2.getHostStatus());
            assertEquals(new Integer(0), testHost2.getHostStatus().getProperty(Nagios.SCHEDULED_DOWNTIME_DEPTH));
            logMessages = logMessageService.getLogMessagesByHostName("test-host-0", clearInDowntimeTimestamp, null, null, LOGMESSAGE_SORT_CRITERIA, -1, -1).getResults();
            assertFalse(logMessages.isEmpty());
            assertEquals(testHost0.getHostStatus(), logMessages.get(0).getHostStatus());
            assertEquals(DOWNTIME_APP_TYPE, logMessages.get(0).getApplicationType().getName());
            assertEquals(IN_DOWNTIME_MONITOR_STATUS, logMessages.get(0).getMonitorStatus().getName());
            assertTrue(logMessages.get(0).getTextMessage().contains("test-host-group-0"));
            logMessages = logMessageService.getLogMessagesByHostName("test-host-1", clearInDowntimeTimestamp, null, null, LOGMESSAGE_SORT_CRITERIA, -1, -1).getResults();
            assertFalse(logMessages.isEmpty());
            assertEquals(testHost1.getHostStatus(), logMessages.get(0).getHostStatus());
            assertEquals(DOWNTIME_APP_TYPE, logMessages.get(0).getApplicationType().getName());
            assertEquals(IN_DOWNTIME_MONITOR_STATUS, logMessages.get(0).getMonitorStatus().getName());
            assertTrue(logMessages.get(0).getTextMessage().contains("test-host-group-0"));
            logMessages = logMessageService.getLogMessagesByHostName("test-host-2", clearInDowntimeTimestamp, null, null, LOGMESSAGE_SORT_CRITERIA, -1, -1).getResults();
            assertFalse(logMessages.size() < 2);
            assertEquals(testHost2.getHostStatus(), logMessages.get(1).getHostStatus());
            assertEquals(DOWNTIME_APP_TYPE, logMessages.get(1).getApplicationType().getName());
            assertEquals(IN_DOWNTIME_MONITOR_STATUS, logMessages.get(1).getMonitorStatus().getName());
            assertTrue(logMessages.get(1).getTextMessage().contains("test-host-group-1"));
            assertEquals(testHost2.getHostStatus(), logMessages.get(0).getHostStatus());
            assertEquals(DOWNTIME_APP_TYPE, logMessages.get(0).getApplicationType().getName());
            assertEquals(END_DOWNTIME_MONITOR_STATUS, logMessages.get(0).getMonitorStatus().getName());
            assertTrue(logMessages.get(0).getTextMessage().contains("test-host-group-0"));
            // clear host services in downtime
            hostsAndServices1 = bizServices.clearHostsAndServicesInDowntime(hostsAndServices1);
            assertNotNull(hostsAndServices1);
            assertEquals(1, hostsAndServices1.size());
            assertEquals("test-host-2", hostsAndServices1.get(0).hostName);
            assertEquals("test-service-0", hostsAndServices1.get(0).serviceDescription);
            assertEquals(new Integer(0), hostsAndServices1.get(0).scheduledDowntimeDepth);
            assertEquals(Host.ENTITY_TYPE_CODE, hostsAndServices1.get(0).entityType);
            assertEquals("test-host-2", hostsAndServices1.get(0).entityName);
            testHost2 = hostService.getHostByHostName("test-host-2");
            assertNotNull(testHost2);
            assertNotNull(testHost2.getHostStatus());
            assertEquals(new Integer(0), testHost2.getHostStatus().getProperty(Nagios.SCHEDULED_DOWNTIME_DEPTH));
            testServiceStatus5 = testHost2.getServiceStatus("test-service-0");
            assertNotNull(testServiceStatus5);
            assertEquals(new Integer(0), testServiceStatus5.getProperty(Nagios.SCHEDULED_DOWNTIME_DEPTH));
            logMessages = logMessageService.getLogMessagesByHostName("test-host-2", clearInDowntimeTimestamp, null, null, LOGMESSAGE_SORT_CRITERIA, -1, -1).getResults();
            assertFalse(logMessages.isEmpty());
            assertEquals(testHost2.getHostStatus(), logMessages.get(0).getHostStatus());
            assertEquals(testServiceStatus5, logMessages.get(0).getServiceStatus());
            assertEquals(DOWNTIME_APP_TYPE, logMessages.get(0).getApplicationType().getName());
            assertEquals(END_DOWNTIME_MONITOR_STATUS, logMessages.get(0).getMonitorStatus().getName());
            assertTrue(logMessages.get(0).getTextMessage().contains("test-service-0"));
            // clear hosts in downtime
            hostsAndServices0 = bizServices.clearHostsAndServicesInDowntime(hostsAndServices0);
            assertNotNull(hostsAndServices0);
            assertEquals(2, hostsAndServices0.size());
            assertEquals("test-host-1", hostsAndServices0.get(0).hostName);
            assertEquals(new Integer(0), hostsAndServices0.get(0).scheduledDowntimeDepth);
            assertEquals(Host.ENTITY_TYPE_CODE, hostsAndServices0.get(0).entityType);
            assertEquals("test-host-1", hostsAndServices0.get(0).entityName);
            assertEquals("test-host-0", hostsAndServices0.get(1).hostName);
            assertEquals(new Integer(0), hostsAndServices0.get(1).scheduledDowntimeDepth);
            assertEquals(Host.ENTITY_TYPE_CODE, hostsAndServices0.get(1).entityType);
            assertEquals("test-host-0", hostsAndServices0.get(1).entityName);
            testHost0 = hostService.getHostByHostName("test-host-0");
            assertNotNull(testHost0);
            assertNotNull(testHost0.getHostStatus());
            assertEquals(new Integer(0), testHost0.getHostStatus().getProperty(Nagios.SCHEDULED_DOWNTIME_DEPTH));
            testHost1 = hostService.getHostByHostName("test-host-1");
            assertNotNull(testHost1);
            assertNotNull(testHost1.getHostStatus());
            assertEquals(new Integer(0), testHost1.getHostStatus().getProperty(Nagios.SCHEDULED_DOWNTIME_DEPTH));
            logMessages = logMessageService.getLogMessagesByHostName("test-host-0", clearInDowntimeTimestamp, null, null, LOGMESSAGE_SORT_CRITERIA, -1, -1).getResults();
            assertFalse(logMessages.isEmpty());
            assertEquals(testHost0.getHostStatus(), logMessages.get(0).getHostStatus());
            assertEquals(DOWNTIME_APP_TYPE, logMessages.get(0).getApplicationType().getName());
            assertEquals(END_DOWNTIME_MONITOR_STATUS, logMessages.get(0).getMonitorStatus().getName());
            assertTrue(logMessages.get(0).getTextMessage().contains("test-host-0"));
            logMessages = logMessageService.getLogMessagesByHostName("test-host-1", clearInDowntimeTimestamp, null, null, LOGMESSAGE_SORT_CRITERIA, -1, -1).getResults();
            assertFalse(logMessages.isEmpty());
            assertEquals(testHost1.getHostStatus(), logMessages.get(0).getHostStatus());
            assertEquals(DOWNTIME_APP_TYPE, logMessages.get(0).getApplicationType().getName());
            assertEquals(END_DOWNTIME_MONITOR_STATUS, logMessages.get(0).getMonitorStatus().getName());
            assertTrue(logMessages.get(0).getTextMessage().contains("test-host-1"));
            commitTransaction();
        } finally {
            // rollback current transaction if possible
            try {
                rollbackTransaction();
            } catch (Exception e) {
            }
            // tear down test devices, hosts, services, host groups, and service groups
            if ((testDevice0 != null) && (testDevice0.getDeviceId() != null)) {
                try {
                    logMessageService.deleteLogMessagesForDevice(testDevice0.getIdentification());
                } catch (Exception e) {
                }
                try {
                    deviceService.deleteDeviceById(testDevice0.getDeviceId());
                } catch (Exception e) {
                }
            }
            if ((testDevice1 != null) && (testDevice1.getDeviceId() != null)) {
                try {
                    logMessageService.deleteLogMessagesForDevice(testDevice1.getIdentification());
                } catch (Exception e) {
                }
                try {
                    deviceService.deleteDeviceById(testDevice1.getDeviceId());
                } catch (Exception e) {
                }
            }
            if ((testDevice2 != null) && (testDevice2.getDeviceId() != null)) {
                try {
                    logMessageService.deleteLogMessagesForDevice(testDevice2.getIdentification());
                } catch (Exception e) {
                }
                try {
                    deviceService.deleteDeviceById(testDevice2.getDeviceId());
                } catch (Exception e) {
                }
            }
            if ((testHost0 != null) && (testHost0.getHostId() != null)) {
                try {
                    hostService.deleteHostById(testHost0.getHostId());
                } catch (Exception e) {
                }
            }
            if ((testHost1 != null) && (testHost1.getHostId() != null)) {
                try {
                    hostService.deleteHostById(testHost1.getHostId());
                } catch (Exception e) {
                }
            }
            if ((testHost2 != null) && (testHost2.getHostId() != null)) {
                try {
                    hostService.deleteHostById(testHost2.getHostId());
                } catch (Exception e) {
                }
            }
            if ((testHostGroup0 != null) && (testHostGroup0.getHostGroupId() != null)) {
                try {
                    hostGroupService.deleteHostGroupById(testHostGroup0.getHostGroupId());
                } catch (Exception e) {
                }
            }
            if ((testHostGroup1 != null) && (testHostGroup1.getHostGroupId() != null)) {
                try {
                    hostGroupService.deleteHostGroupById(testHostGroup1.getHostGroupId());
                } catch (Exception e) {
                }
            }
            if ((testServiceGroupCategory0 != null) && (testServiceGroupCategory0.getCategoryId() != null)) {
                try {
                    categoryService.deleteCategoryById(testServiceGroupCategory0.getCategoryId());
                } catch (Exception e) {
                }
            }
            if ((testServiceGroupCategory1 != null) && (testServiceGroupCategory1.getCategoryId() != null)) {
                try {
                    categoryService.deleteCategoryById(testServiceGroupCategory1.getCategoryId());
                } catch (Exception e) {
                }
            }
        }
    }

    private int hostsAndServicesIndexOf(List<BizServices.HostServiceInDowntime> hostsAndServices, String hostName, String serviceDescription) {
        return hostsAndServicesIndexOf(hostsAndServices, hostName, serviceDescription, 0, hostsAndServices.size());
    }

    private int hostsAndServicesIndexOf(List<BizServices.HostServiceInDowntime> hostsAndServices, String hostName, String serviceDescription, int fromIndex) {
        return hostsAndServicesIndexOf(hostsAndServices, hostName, serviceDescription, fromIndex, hostsAndServices.size());
    }

    private int hostsAndServicesIndexOf(List<BizServices.HostServiceInDowntime> hostsAndServices, String hostName, String serviceDescription, int fromIndex, int toIndex) {
        for (int index = fromIndex; ((index < toIndex) && (index < hostsAndServices.size())); index++) {
            BizServices.HostServiceInDowntime hostOrService = hostsAndServices.get(index);
            if (hostOrService.hostName.equals(hostName) &&
                    (((hostOrService.serviceDescription != null) && hostOrService.serviceDescription.equals(serviceDescription)) ||
                            ((hostOrService.serviceDescription == null) && (serviceDescription == null)))) {
                return index;
            }
        }
        return -1;
    }

    public void testAuthorizationBizServices() throws Exception {
        System.out.println("testing Authorization Biz Services...");
        // get biz services
        BizServices bizServices = (BizServices) collage.getAPIObject(BizServices.SERVICE);
        // test authorized services
        List<String> authorizedHostGroups = Arrays.asList(new String[]{"Storage"});
        BizServices.AuthorizedServices authorizedServices = bizServices.getAuthorizedServices(authorizedHostGroups, null);
        assertNotNull(authorizedServices);
        assertNotNull(authorizedServices.hostNames);
        assertEquals(1, authorizedServices.hostNames.size());
        assertTrue(authorizedServices.hostNames.contains("gwrk-storage"));
        assertNotNull(authorizedServices.serviceHostNames);
        assertEquals(1, authorizedServices.serviceHostNames.size());
        assertTrue(authorizedServices.serviceHostNames.containsKey("storage"));
        assertNotNull(authorizedServices.serviceHostNames.get("storage"));
        assertEquals(1, authorizedServices.serviceHostNames.get("storage").size());
        assertTrue(authorizedServices.serviceHostNames.get("storage").contains("gwrk-storage"));
        List<String> authorizedServiceGroups = Arrays.asList(new String[]{"SG1", "SG2"});
        authorizedServices = bizServices.getAuthorizedServices(null, authorizedServiceGroups);
        assertNotNull(authorizedServices);
        assertNotNull(authorizedServices);
        assertNotNull(authorizedServices.hostNames);
        assertEquals(0, authorizedServices.hostNames.size());
        assertNotNull(authorizedServices.serviceHostNames);
        assertEquals(2, authorizedServices.serviceHostNames.size());
        assertTrue(authorizedServices.serviceHostNames.containsKey("local_disk"));
        assertNotNull(authorizedServices.serviceHostNames.get("local_disk"));
        assertEquals(1, authorizedServices.serviceHostNames.get("local_disk").size());
        assertTrue(authorizedServices.serviceHostNames.get("local_disk").contains("nagios"));
        assertTrue(authorizedServices.serviceHostNames.containsKey("local_procs"));
        assertNotNull(authorizedServices.serviceHostNames.get("local_procs"));
        assertEquals(1, authorizedServices.serviceHostNames.get("local_procs").size());
        assertTrue(authorizedServices.serviceHostNames.get("local_procs").contains("nagios"));
        authorizedServices = bizServices.getAuthorizedServices(null, null);
        assertNull(authorizedServices);
        authorizedServices = bizServices.getAuthorizedServices();
        assertNotNull(authorizedServices);
        assertNotNull(authorizedServices.hostNames);
        assertFalse(authorizedServices.hostNames.isEmpty());
        assertNotNull(authorizedServices.serviceHostNames);
        assertFalse(authorizedServices.serviceHostNames.isEmpty());
    }
}
