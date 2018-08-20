package org.groundwork.rs.client;

import com.groundwork.collage.CollageAdminInfrastructure;
import com.groundwork.collage.util.MonitorStatusBubbleUp;
import org.groundwork.rs.dto.DtoBizAuthorization;
import org.groundwork.rs.dto.DtoBizAuthorizedServices;
import org.groundwork.rs.dto.DtoBizHost;
import org.groundwork.rs.dto.DtoBizHostList;
import org.groundwork.rs.dto.DtoBizHostServiceInDowntime;
import org.groundwork.rs.dto.DtoBizHostServiceInDowntimeList;
import org.groundwork.rs.dto.DtoBizHostsAndServices;
import org.groundwork.rs.dto.DtoBizService;
import org.groundwork.rs.dto.DtoBizServiceList;
import org.groundwork.rs.dto.DtoCategory;
import org.groundwork.rs.dto.DtoCategoryList;
import org.groundwork.rs.dto.DtoDepthType;
import org.groundwork.rs.dto.DtoDevice;
import org.groundwork.rs.dto.DtoEvent;
import org.groundwork.rs.dto.DtoHost;
import org.groundwork.rs.dto.DtoHostGroup;
import org.groundwork.rs.dto.DtoHostList;
import org.groundwork.rs.dto.DtoOperationResults;
import org.groundwork.rs.dto.DtoService;
import org.groundwork.rs.dto.DtoServiceGroup;
import org.groundwork.rs.dto.DtoServiceList;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class BizClientTest  extends AbstractClientTest {

    private static final String SERVICE_ENTITY_TYPE = CategoryClient.ENTITY_TYPE_CODE_SERVICESTATUS;
    private static final String SERVICE_CATEGORY_ENTITY_TYPE = CategoryClient.ENTITY_TYPE_CODE_SERVICECATEGORY;
    private static final String HOST_ENTITY_TYPE = CategoryClient.ENTITY_TYPE_CODE_HOST;
    private static final String HOST_CATEGORY_ENTITY_TYPE = CategoryClient.ENTITY_TYPE_CODE_HOSTCATEGORY;
    private static final String HOST_GROUP_ENTITY_TYPE = CategoryClient.ENTITY_TYPE_CODE_HOSTGROUP;

    @Test
    public void testCreateHost() {
        if (serverDown) return;
        BizClient client = new BizClient(getDeploymentURL());
        DtoHost host = client.createOrUpdateHost("biz-host-1", "PENDING", "welcome home", "biz-group-1", "biz-host-category-1",
                "biz-device-1", "SEL", "biz");
        assertNotNull(host);
        assertEquals("biz-host-1", host.getHostName());
        assertEquals("PENDING", host.getMonitorStatus());
        assertEquals("welcome home", host.getProperty("LastPluginOutput"));
        assertEquals("biz-device-1", host.getDeviceIdentification());
        assertEquals("SEL", host.getAppType());
        assertEquals("biz", host.getAgentId());

        /**
         * GWMON-13138 - asserting that HostGroups are not processed on this API
         */
        boolean isTestUpdate = true;
        if (isTestUpdate) {
            DtoHost host2 = client.createOrUpdateHost("biz-host-1", "PENDING", "welcome home", "biz-group-1", "biz-host-category-1",
                    "biz-device-1", "SEL", "biz");
            assertNotNull(host2);
        }
        HostGroupClient hostGroupClient = new HostGroupClient(getDeploymentURL());
        DtoHostGroup hg = hostGroupClient.lookup("biz-group-1");
        assert hg.getHosts().size() == 1;
        assert hg.getHosts().get(0).getHostName().equals("biz-host-1");

        CategoryClient categoryClient = new CategoryClient(getDeploymentURL());
        DtoCategory hc = categoryClient.lookup("biz-host-category-1", HOST_CATEGORY_ENTITY_TYPE, DtoDepthType.Deep);
        assert hc.getEntityTypeName().equals(HOST_CATEGORY_ENTITY_TYPE);
        assert hc.getEntities().size() == 1;
        assert hc.getEntities().get(0).getEntityTypeName().equals(HOST_ENTITY_TYPE);
        assert hc.getEntities().get(0).getObjectID().equals(host.getId());

        // Clean up
        DeviceClient deviceClient = new DeviceClient(getDeploymentURL());
        deviceClient.delete("biz-device-1"); // cascade deletes of hosts
        hostGroupClient.delete("biz-group-1");
        categoryClient.delete("biz-host-category-1", HOST_CATEGORY_ENTITY_TYPE);
    }

    @Test
    public void testCreateService() {
        if (serverDown) return;
        BizClient client = new BizClient(getDeploymentURL());
        DtoService service = client.createOrUpdateService("biz-host-1", "biz-service-1", "PENDING", "welcome home",
                "biz-group-1", "biz-service-category-1", "biz-group-1", "biz-host-category-1", "biz-device-1", "SEL", "biz");
        assertNotNull(service);
        assertEquals("biz-host-1", service.getHostName());
        assertEquals("biz-service-1", service.getDescription());
        assertEquals("PENDING", service.getMonitorStatus());
        assertEquals("welcome home", service.getProperty("LastPluginOutput"));
        assertEquals("biz-device-1", service.getDeviceIdentification());
        assertEquals("SEL", service.getAppType());
        assertEquals("biz", service.getAgentId());
        HostClient hostClient = new HostClient(getDeploymentURL());
        DtoHost host = hostClient.lookup("biz-host-1");
        assertNotNull(host);
        assertEquals("PENDING", host.getMonitorStatus());

        HostGroupClient hostGroupClient = new HostGroupClient(getDeploymentURL());
        DtoHostGroup hg = hostGroupClient.lookup("biz-group-1");
        assert hg.getHosts().size() == 1;
        assert hg.getHosts().get(0).getHostName().equals("biz-host-1");

        CategoryClient categoryClient = new CategoryClient(getDeploymentURL());
        DtoCategory hc = categoryClient.lookup("biz-host-category-1", HOST_CATEGORY_ENTITY_TYPE, DtoDepthType.Deep);
        assert hc.getEntityTypeName().equals(HOST_CATEGORY_ENTITY_TYPE);
        assert hc.getEntities().size() == 1;
        assert hc.getEntities().get(0).getEntityTypeName().equals(HOST_ENTITY_TYPE);
        assert hc.getEntities().get(0).getObjectID().equals(host.getId());

        ServiceGroupClient serviceGroupClient = new ServiceGroupClient(getDeploymentURL());
        DtoServiceGroup sg = serviceGroupClient.lookup("biz-group-1");
        assert sg.getServices().size() == 1;
        assert sg.getServices().get(0).getHostName().equals("biz-host-1");
        assert sg.getServices().get(0).getDescription().equals("biz-service-1");

        DtoCategory sc = categoryClient.lookup("biz-service-category-1", SERVICE_CATEGORY_ENTITY_TYPE, DtoDepthType.Deep);
        assert sc.getEntityTypeName().equals(SERVICE_CATEGORY_ENTITY_TYPE);
        assert sc.getEntities().size() == 1;
        assert sc.getEntities().get(0).getEntityTypeName().equals(SERVICE_ENTITY_TYPE);
        assert sc.getEntities().get(0).getObjectID().equals(service.getId());

        // create a service with existing host
        service = client.createOrUpdateService("biz-host-1", "biz-service-2", "PENDING", "welcome home", "biz-group-1",
                "biz-service-category-1", "biz-group-1", "biz-host-category-1", "biz-device-1", "SEL", "biz");
        assertNotNull(service);
        assertEquals("biz-host-1", service.getHostName());
        assertEquals("biz-service-2", service.getDescription());

        /**
         * GWMON-13138 - asserting that HostGroups are not processed on this API
         */
        DtoService service3 = client.createOrUpdateService("biz-host-1", "biz-service-1", "PENDING", "welcome home",
                "biz-group-1", "biz-service-category-1", "biz-group-1", "biz-host-category-1", "biz-device-1", "SEL", "biz");
        assertNotNull(service3);

        // Clean up
        DeviceClient deviceClient = new DeviceClient(getDeploymentURL());
        deviceClient.delete("biz-device-1"); // cascade deletes of hosts
        hostGroupClient.delete("biz-group-1");
        serviceGroupClient.delete("biz-group-1");
        categoryClient.delete("biz-service-category-1", SERVICE_CATEGORY_ENTITY_TYPE);
        categoryClient.delete("biz-host-category-1", HOST_CATEGORY_ENTITY_TYPE);
    }

    @Test
    public void testCreateHostService() {
        if (serverDown) return;

        // create host with services
        BizClient client = new BizClient(getDeploymentURL());
        DtoBizHost bizHost = new DtoBizHost();
        bizHost.setHost("biz-host-1");
        bizHost.setStatus("PENDING");
        bizHost.setMessage("welcome home");
        bizHost.setHostGroup("biz-group-1");
        bizHost.setHostCategory("biz-host-category-1");
        bizHost.setDevice("biz-device-1");
        bizHost.setAppType("SEL");
        bizHost.setAgentId("biz");
        bizHost.getProperties().put("ExecutionTime", "400");
        DtoBizService bizService1 = new DtoBizService();
        bizService1.setService("biz-service-1");
        bizService1.setStatus("PENDING");
        bizService1.setServiceGroup("biz-group-1");
        bizService1.setServiceCategory("biz-service-category-1");
        bizService1.getProperties().put("Latency", "500");
        bizHost.add(bizService1);
        DtoBizService bizService2 = new DtoBizService();
        bizService2.setService("biz-service-2");
        bizService2.setStatus("PENDING");
        bizService2.setMessage("welcome home again");
        bizService2.setServiceGroup("biz-group-1");
        bizService2.setServiceCategory("biz-service-category-1");
        bizService2.getProperties().put("Latency", "501");
        bizHost.add(bizService2);
        DtoOperationResults results = client.postHosts(new DtoBizHostList(Arrays.asList(new DtoBizHost[]{bizHost})));
        assert results != null;
        assert results.getCount() == 3;
        assert results.getSuccessful() == 3;
        assert "biz-host-1".equals(results.getResults().get(0).getEntity());
        assert results.getResults().get(0).getLocation() != null;
        assert "Insert".equals(results.getResults().get(0).getMessage());
        assert "biz-host-1:biz-service-1".equals(results.getResults().get(1).getEntity());
        assert results.getResults().get(1).getLocation() != null;
        assert "Insert".equals(results.getResults().get(1).getMessage());
        assert "biz-host-1:biz-service-2".equals(results.getResults().get(2).getEntity());
        assert results.getResults().get(2).getLocation() != null;
        assert "Insert".equals(results.getResults().get(2).getMessage());

        // validate host
        HostClient hostClient = new HostClient(getDeploymentURL());
        DtoHost host = hostClient.lookup("biz-host-1");
        assert host != null;
        assert "biz-host-1".equals(host.getHostName());
        assert "PENDING".equals(host.getMonitorStatus());
        assert "welcome home".equals(host.getProperty("LastPluginOutput"));
        assert "biz-device-1".equals(host.getDeviceIdentification());
        assert "SEL".equals(host.getAppType());
        assert "biz".equals(host.getAgentId());
        assert "400".equals(host.getProperty("ExecutionTime"));

        // validate host group
        HostGroupClient hostGroupClient = new HostGroupClient(getDeploymentURL());
        DtoHostGroup hg = hostGroupClient.lookup("biz-group-1");
        assert hg.getHosts().size() == 1;
        assert hg.getHosts().get(0).getHostName().equals("biz-host-1");

        // validate host category
        CategoryClient categoryClient = new CategoryClient(getDeploymentURL());
        DtoCategory hc = categoryClient.lookup("biz-host-category-1", HOST_CATEGORY_ENTITY_TYPE, DtoDepthType.Deep);
        assert hc.getEntityTypeName().equals(HOST_CATEGORY_ENTITY_TYPE);
        assert hc.getEntities().size() == 1;
        assert hc.getEntities().get(0).getEntityTypeName().equals(HOST_ENTITY_TYPE);
        assert hc.getEntities().get(0).getObjectID().equals(host.getId());

        // validate services
        ServiceClient serviceClient = new ServiceClient(getDeploymentURL());
        DtoService service1 = serviceClient.lookup("biz-service-1", "biz-host-1");
        assert service1 != null;
        assert "biz-host-1".equals(service1.getHostName());
        assert "biz-service-1".equals(service1.getDescription());
        assert "PENDING".equals(service1.getMonitorStatus());
        assert "welcome home".equals(service1.getProperty("LastPluginOutput"));
        assert "biz-device-1".equals(service1.getDeviceIdentification());
        assert "SEL".equals(service1.getAppType());
        assert "biz".equals(service1.getAgentId());
        assert "500".equals(service1.getProperty("Latency"));

        DtoService service2 = serviceClient.lookup("biz-service-2", "biz-host-1");
        assert service2 != null;
        assert "biz-host-1".equals(service2.getHostName());
        assert "biz-service-2".equals(service2.getDescription());
        assert "PENDING".equals(service2.getMonitorStatus());
        assert "welcome home again".equals(service2.getProperty("LastPluginOutput"));
        assert "biz-device-1".equals(service2.getDeviceIdentification());
        assert "SEL".equals(service2.getAppType());
        assert "biz".equals(service2.getAgentId());
        assert "501".equals(service2.getProperty("Latency"));

        // validate service group
        ServiceGroupClient serviceGroupClient = new ServiceGroupClient(getDeploymentURL());
        DtoServiceGroup sg = serviceGroupClient.lookup("biz-group-1");
        assert sg.getServices().size() == 2;
        assert sg.getServices().get(0).getHostName().equals("biz-host-1");
        assert sg.getServices().get(1).getHostName().equals("biz-host-1");
        Set<String> serviceDescriptions = new HashSet<String>();
        serviceDescriptions.add(sg.getServices().get(0).getDescription());
        serviceDescriptions.add(sg.getServices().get(1).getDescription());
        assert serviceDescriptions.contains("biz-service-1");
        assert serviceDescriptions.contains("biz-service-2");

        // validate service category
        DtoCategory sc = categoryClient.lookup("biz-service-category-1", SERVICE_CATEGORY_ENTITY_TYPE, DtoDepthType.Deep);
        assert sc.getEntityTypeName().equals(SERVICE_CATEGORY_ENTITY_TYPE);
        assert sc.getEntities().size() == 2;
        assert sc.getEntities().get(0).getEntityTypeName().equals(SERVICE_ENTITY_TYPE);
        assert sc.getEntities().get(1).getEntityTypeName().equals(SERVICE_ENTITY_TYPE);
        Set<Integer> serviceIds = new HashSet<Integer>();
        serviceIds.add(sc.getEntities().get(0).getObjectID());
        serviceIds.add(sc.getEntities().get(1).getObjectID());
        assert serviceIds.contains(service1.getId());
        assert serviceIds.contains(service2.getId());

        // Clean up
        DeviceClient deviceClient = new DeviceClient(getDeploymentURL());
        deviceClient.delete("biz-device-1"); // cascade deletes of hosts and services
        hostGroupClient.delete("biz-group-1");
        serviceGroupClient.delete("biz-group-1");
        categoryClient.delete("biz-service-category-1", SERVICE_CATEGORY_ENTITY_TYPE);
        categoryClient.delete("biz-host-category-1", HOST_CATEGORY_ENTITY_TYPE);
    }

    @Test
    public void testBulkCreateHostsAndServices() {
        if (serverDown) return;

        // create clients
        HostGroupClient hostGroupClient = new HostGroupClient(getDeploymentURL());
        ServiceGroupClient serviceGroupClient = new ServiceGroupClient(getDeploymentURL());
        DeviceClient deviceClient = new DeviceClient(getDeploymentURL());
        CategoryClient categoryClient = new CategoryClient(getDeploymentURL());
        BizClient client = new BizClient(getDeploymentURL());
        HostClient hostClient = new HostClient(getDeploymentURL());

        // create hosts and services
        int numHosts = 100;
        int numServicesPerHost = 10;
        Set<String> deviceIdentifications = new HashSet<String>();
        Set<String> hostGroupNames = new HashSet<String>();
        Set<String> hostCategoryNames = new HashSet<String>();
        Set<String> serviceGroupNames = new HashSet<String>();
        Set<String> serviceCategoryNames = new HashSet<String>();
        List<DtoBizHost> dtoBizHosts = new ArrayList<DtoBizHost>();
        for (int host = 0; (host < numHosts); host++) {
            DtoBizHost bizHost = new DtoBizHost();
            bizHost.setHost("bulk-biz-host-" + host);
            bizHost.setStatus("PENDING");
            bizHost.setMessage("testing bulk create " + host);
            String hostGroupName = "bulk-biz-group-" + (host % 5);
            hostGroupNames.add(hostGroupName);
            bizHost.setHostGroup(hostGroupName);
            String hostCategoryName = "bulk-biz-host-category-" + (host % 5);
            hostCategoryNames.add(hostCategoryName);
            bizHost.setHostCategory(hostCategoryName);
            String deviceIdentification = "bulk-biz-device-" + (host % numHosts/2);
            deviceIdentifications.add(deviceIdentification);
            bizHost.setDevice(deviceIdentification);
            bizHost.setAppType("SEL");
            bizHost.setAgentId("biz");
            for (int service = 0; (service < numServicesPerHost); service++) {
                DtoBizService bizService = new DtoBizService();
                bizService.setService("bulk-biz-service-" + service);
                bizService.setStatus("PENDING");
                bizService.setMessage("testing bulk create " + service);
                String serviceGroupName = "bulk-biz-group-" + (service % 5);
                serviceGroupNames.add(serviceGroupName);
                bizService.setServiceGroup(serviceGroupName);
                String serviceCategoryName = "bulk-biz-service-category-" + (service % 5);
                serviceCategoryNames.add(serviceCategoryName);
                bizService.setServiceCategory(serviceCategoryName);
                bizHost.add(bizService);
            }
            dtoBizHosts.add(bizHost);
        }
        long start = System.currentTimeMillis();
        DtoOperationResults results = client.postHosts(new DtoBizHostList(dtoBizHosts));
        long end = System.currentTimeMillis();
        assert results != null;
        assert results.getCount() == numHosts+(numHosts*numServicesPerHost);
        assert results.getSuccessful() == numHosts+(numHosts*numServicesPerHost);
        log.info(String.format("Elapsed time for testBulkCreateHostsAndServices create: %d", (end-start)));

        // create hosts and services with duplicates
        dtoBizHosts.clear();
        {
            DtoBizHost bizHost = new DtoBizHost();
            bizHost.setHost("bulk-biz-host");
            bizHost.setStatus("PENDING");
            bizHost.setMessage("testing bulk create");
            bizHost.setHostGroup("bulk-biz-group-0");
            bizHost.setHostCategory("bulk-biz-host-category-0");
            String deviceIdentification = "bulk-biz-device";
            deviceIdentifications.add(deviceIdentification);
            bizHost.setDevice(deviceIdentification);
            bizHost.setAppType("SEL");
            bizHost.setAgentId("biz");
            {
                DtoBizService bizService = new DtoBizService();
                bizService.setService("bulk-biz-service");
                bizService.setStatus("PENDING");
                bizService.setMessage("testing bulk create");
                bizService.setServiceGroup("bulk-biz-group-0");
                bizService.setServiceCategory("bulk-biz-service-category-0");
                bizHost.add(bizService);
            }
            dtoBizHosts.add(bizHost);
            bizHost = new DtoBizHost();
            bizHost.setHost("bulk-biz-host");
            bizHost.setStatus("OK");
            bizHost.setMessage("testing bulk create duplicate");
            bizHost.setDevice("bulk-biz-device");
            bizHost.setAppType("SEL");
            bizHost.setAgentId("biz");
            {
                DtoBizService bizService = new DtoBizService();
                bizService.setService("bulk-biz-service");
                bizService.setStatus("OK");
                bizService.setMessage("testing bulk create duplicate");
                bizHost.add(bizService);
            }
            dtoBizHosts.add(bizHost);
        }
        results = client.postHosts(new DtoBizHostList(dtoBizHosts));
        assert results != null;
        assert results.getCount() == 4;
        assert results.getSuccessful() == 4;

        // load bulk hosts and services for synchronization
        start = System.currentTimeMillis();
        hostClient.setMediaType(MediaType.APPLICATION_XML_TYPE);
        List<DtoHost> synchronizationHosts = hostClient.list(DtoDepthType.Sync);
        end = System.currentTimeMillis();
        assert synchronizationHosts != null;
        assert synchronizationHosts.size() > 100;
        log.info(String.format("Elapsed time for testBulkCreateHostsAndServices XML sync load: %d", (end-start)));
        start = System.currentTimeMillis();
        hostClient.setMediaType(MediaType.APPLICATION_JSON_TYPE);
        synchronizationHosts = hostClient.list(DtoDepthType.Sync);
        end = System.currentTimeMillis();
        assert synchronizationHosts != null;
        assert synchronizationHosts.size() > 100;
        log.info(String.format("Elapsed time for testBulkCreateHostsAndServices JSON sync load: %d", (end-start)));

        // cleanup
        if (!deviceIdentifications.isEmpty()) {
            deviceClient.delete(new ArrayList<String>(deviceIdentifications));
        }
        if (!hostGroupNames.isEmpty()) {
            hostGroupClient.delete(new ArrayList<String>(hostGroupNames));
        }
        if (!hostCategoryNames.isEmpty()) {
            deleteCategories(categoryClient, hostCategoryNames, HOST_CATEGORY_ENTITY_TYPE);
        }
        if (!serviceGroupNames.isEmpty()) {
            serviceGroupClient.delete(new ArrayList<String>(serviceGroupNames));
        }
        if (!serviceCategoryNames.isEmpty()) {
            deleteCategories(categoryClient, serviceCategoryNames, SERVICE_CATEGORY_ENTITY_TYPE);
        }
    }

    /**
     * Delete categories by name.
     *
     * @param categoryClient category client
     * @param categoryNames category names collection
     * @param entityTypeName entity type name
     */
    private void deleteCategories(CategoryClient categoryClient, Collection<String> categoryNames, String entityTypeName) {
        DtoCategoryList deletes = new DtoCategoryList();
        for (String categoryName : categoryNames) {
            DtoCategory dtoCategory = new DtoCategory();
            dtoCategory.setName(categoryName);
            dtoCategory.setEntityTypeName(entityTypeName);
            deletes.getCategories().add(dtoCategory);
        }
        categoryClient.delete(deletes);
    }

    @Test
    public void testBulkCreateServices() {
        if (serverDown) return;

        // create clients
        BizClient client = new BizClient(getDeploymentURL());
        DeviceClient deviceClient = new DeviceClient(getDeploymentURL());
        HostGroupClient hostGroupClient = new HostGroupClient(getDeploymentURL());
        ServiceGroupClient serviceGroupClient = new ServiceGroupClient(getDeploymentURL());
        CategoryClient categoryClient = new CategoryClient(getDeploymentURL());

        // explicitly create host, (other is created dynamically)
        DtoHost host = client.createOrUpdateHost("bulk-biz-host-0", "PENDING", "welcome home", null, null,
                "bulk-biz-device-0", "SEL", "biz");
        assert host != null;

        // create services
        int numServices = 1000;
        List<DtoBizService> dtoBizServices = new ArrayList<DtoBizService>();
        for (int service = 0; (service < numServices); service++) {
            DtoBizService dtoBizService = new DtoBizService();
            dtoBizService.setHost("bulk-biz-host-" + (service % 2));
            dtoBizService.setDevice("bulk-biz-device-" + (service % 2));
            dtoBizService.setHostGroup("bulk-biz-group-" + (service % 2));
            dtoBizService.setHostCategory("bulk-biz-host-category-" + (service % 2));
            dtoBizService.setService("bulk-biz-service-" + service);
            dtoBizService.setStatus("PENDING");
            dtoBizService.setMessage("testing bulk create " + service);
            dtoBizService.setServiceGroup("bulk-biz-group-" + (service % 2));
            dtoBizService.setServiceCategory("bulk-biz-service-category-" + (service % 2));
            dtoBizService.setAppType("SEL");
            dtoBizService.setAgentId("biz");
            dtoBizServices.add(dtoBizService);
        }
        long start = System.currentTimeMillis();
        DtoOperationResults results = client.postServices(new DtoBizServiceList(dtoBizServices));
        long end = System.currentTimeMillis();
        assert results != null;
        assert results.getCount() == numServices + 2;
        assert results.getSuccessful() == numServices + 2;
        log.info(String.format("Elapsed time for testBulkCreateServices create: %d", (end-start)));

        // create services with duplicates
        dtoBizServices.clear();
        {
            DtoBizService dtoBizService = new DtoBizService();
            dtoBizService.setHost("bulk-biz-host-0");
            dtoBizService.setDevice("bulk-biz-device-0");
            dtoBizService.setHostGroup("bulk-biz-group-0");
            dtoBizService.setHostCategory("bulk-biz-host-category-0");
            dtoBizService.setService("bulk-biz-service");
            dtoBizService.setStatus("PENDING");
            dtoBizService.setMessage("testing bulk create");
            dtoBizService.setServiceGroup("bulk-biz-group-0");
            dtoBizService.setServiceCategory("bulk-biz-service-category-0");
            dtoBizService.setAppType("SEL");
            dtoBizService.setAgentId("biz");
            dtoBizServices.add(dtoBizService);
            dtoBizService = new DtoBizService();
            dtoBizService.setHost("bulk-biz-host-0");
            dtoBizService.setService("bulk-biz-service");
            dtoBizService.setStatus("OK");
            dtoBizService.setMessage("testing bulk create duplicate");
            dtoBizService.setAppType("SEL");
            dtoBizService.setAgentId("biz");
            dtoBizServices.add(dtoBizService);
        }
        results = client.postServices(new DtoBizServiceList(dtoBizServices));
        assert results != null;
        assert results.getCount() == 3;
        assert results.getSuccessful() == 3;

        // cleanup
        deviceClient.delete("bulk-biz-device-0");
        deviceClient.delete("bulk-biz-device-1");
        hostGroupClient.delete("bulk-biz-group-0");
        hostGroupClient.delete("bulk-biz-group-1");
        categoryClient.delete("bulk-biz-host-category-0", HOST_CATEGORY_ENTITY_TYPE);
        categoryClient.delete("bulk-biz-host-category-1", HOST_CATEGORY_ENTITY_TYPE);
        serviceGroupClient.delete("bulk-biz-group-0");
        serviceGroupClient.delete("bulk-biz-group-1");
        categoryClient.delete("bulk-biz-service-category-0", SERVICE_CATEGORY_ENTITY_TYPE);
        categoryClient.delete("bulk-biz-service-category-1", SERVICE_CATEGORY_ENTITY_TYPE);
    }

    @Test
    public void testHostMerge() {
        if (serverDown) return;

        // create clients
        DeviceClient deviceClient = new DeviceClient(getDeploymentURL());
        HostClient hostClient = new HostClient(getDeploymentURL());
        ServiceClient serviceClient = new ServiceClient(getDeploymentURL());
        EventClient eventClient = new EventClient(getDeploymentURL());
        BizClient client = new BizClient(getDeploymentURL());

        // count initial merge events
        int initialEventCount = countMergeBizEvents(eventClient, "merge-biz-host");

        // create merge host and service
        DtoBizHostList dtoBizHosts = new DtoBizHostList();
        DtoBizHost bizHost = new DtoBizHost();
        bizHost.setHost("merge-biz-host");
        bizHost.setStatus("PENDING");
        bizHost.setMessage("testing merge");
        bizHost.setDevice("merge-biz-device");
        bizHost.setAppType("SEL");
        bizHost.setAgentId("biz");
        bizHost.setMergeHosts(false);
        DtoBizService bizService = new DtoBizService();
        bizService.setService("merge-biz-service");
        bizService.setStatus("PENDING");
        bizService.setMessage("testing merge");
        bizHost.add(bizService);
        dtoBizHosts.add(bizHost);
        DtoOperationResults results = client.postHosts(dtoBizHosts);
        assert results != null;
        assert results.getCount() == 2;
        assert results.getSuccessful() == 2;
        assert "merge-biz-host".equals(results.getResults().get(0).getEntity());
        assert "Insert".equals(results.getResults().get(0).getMessage());
        assert "merge-biz-host:merge-biz-service".equals(results.getResults().get(1).getEntity());
        assert "Insert".equals(results.getResults().get(1).getMessage());

        // update merge host and service
        bizHost.setStatus("UP");
        bizService.setStatus("OK");
        results = client.postHosts(dtoBizHosts);
        assert results != null;
        assert results.getCount() == 2;
        assert results.getSuccessful() == 2;
        assert "merge-biz-host".equals(results.getResults().get(0).getEntity());
        assert "Update".equals(results.getResults().get(0).getMessage());
        assert "merge-biz-host:merge-biz-service".equals(results.getResults().get(1).getEntity());
        assert "Update".equals(results.getResults().get(1).getMessage());

        // attempt blocked update merge host and service
        bizHost.setHost("MERGE-BIZ-HOST");
        bizHost.setStatus("UNSCHEDULED DOWN");
        bizService.setStatus("UNSCHEDULED CRITICAL");
        results = client.postHosts(dtoBizHosts);
        assert results != null;
        assert results.getCount() == 1;
        assert results.getWarning() == 1;
        assert "MERGE-BIZ-HOST".equals(results.getResults().get(0).getEntity());

        // update merge service
        DtoBizServiceList dtoBizServices = new DtoBizServiceList();
        bizService = new DtoBizService();
        bizService.setHost("merge-biz-host");
        bizService.setDevice("merge-biz-device");
        bizService.setService("merge-biz-service");
        bizService.setStatus("OK");
        bizService.setMessage("testing merge");
        bizService.setAppType("SEL");
        bizService.setAgentId("biz");
        bizService.setMergeHosts(false);
        dtoBizServices.add(bizService);
        results = client.postServices(dtoBizServices);
        assert results != null;
        assert results.getCount() == 2;
        assert results.getSuccessful() == 2;
        assert "merge-biz-host".equals(results.getResults().get(0).getEntity());
        assert "Update".equals(results.getResults().get(0).getMessage());
        assert "merge-biz-host:merge-biz-service".equals(results.getResults().get(1).getEntity());
        assert "Update".equals(results.getResults().get(1).getMessage());

        // attempt blocked update merge service
        bizService.setHost("MERGE-BIZ-HOST");
        bizService.setStatus("UNSCHEDULED CRITICAL");
        results = client.postServices(dtoBizServices);
        assert results != null;
        assert results.getCount() == 1;
        assert results.getWarning() == 1;
        assert "MERGE-BIZ-HOST:merge-biz-service".equals(results.getResults().get(0).getEntity());

        // validate updates and consolidated merge hosts events
        DtoHost dtoHost = hostClient.lookup("merge-biz-host");
        assert dtoHost != null;
        assert dtoHost.getHostName().equals("merge-biz-host");
        assert dtoHost.getMonitorStatus().equals("UP");
        DtoService dtoService = serviceClient.lookup("merge-biz-service", "merge-biz-host");
        assert dtoService != null;
        assert dtoService.getHostName().equals("merge-biz-host");
        assert dtoService.getDescription().equals("merge-biz-service");
        assert dtoService.getMonitorStatus().equals("OK");
        List<DtoEvent> dtoEvents = eventClient.query("hostStatus.host.hostName = 'merge-biz-host'");
        assert dtoEvents != null;
        int eventCount = countMergeBizEvents(eventClient, "merge-biz-host");
        assert eventCount-initialEventCount == 2;

        // cleanup
        deviceClient.delete("merge-biz-device");
    }

    @Test
    public void testHostMergeInTxn() {
        if (serverDown) return;

        // create clients
        DeviceClient deviceClient = new DeviceClient(getDeploymentURL());
        HostClient hostClient = new HostClient(getDeploymentURL());
        ServiceClient serviceClient = new ServiceClient(getDeploymentURL());
        EventClient eventClient = new EventClient(getDeploymentURL());
        BizClient client = new BizClient(getDeploymentURL());

        // count initial merge events
        int initialEventCount = countMergeBizEvents(eventClient, "merge-biz-host");

        // create and merge hosts and services
        DtoBizHostList dtoBizHosts = new DtoBizHostList();
        DtoBizHost bizHost = new DtoBizHost();
        bizHost.setHost("merge-biz-host");
        bizHost.setStatus("PENDING");
        bizHost.setMessage("testing merge");
        bizHost.setDevice("merge-biz-device");
        bizHost.setAppType("SEL");
        bizHost.setAgentId("biz");
        bizHost.setMergeHosts(false);
        DtoBizService bizService = new DtoBizService();
        bizService.setService("merge-biz-service");
        bizService.setStatus("PENDING");
        bizService.setMessage("testing merge");
        bizHost.add(bizService);
        dtoBizHosts.add(bizHost);
        bizHost = new DtoBizHost();
        bizHost.setHost("MERGE-BIZ-HOST");
        bizHost.setStatus("PENDING");
        bizHost.setMessage("testing merge");
        bizHost.setDevice("MERGE-BIZ-DEVICE");
        bizHost.setAppType("SEL");
        bizHost.setAgentId("biz");
        bizHost.setMergeHosts(false);
        bizService = new DtoBizService();
        bizService.setService("MERGE-BIZ-SERVICE");
        bizService.setStatus("PENDING");
        bizService.setMessage("testing merge");
        bizHost.add(bizService);
        dtoBizHosts.add(bizHost);
        bizHost = new DtoBizHost();
        bizHost.setHost("MERGE-BIZ-HOST");
        bizHost.setStatus("PENDING");
        bizHost.setMessage("testing merge");
        bizHost.setDevice("MERGE-BIZ-DEVICE");
        bizHost.setAppType("SEL");
        bizHost.setAgentId("biz");
        bizHost.setMergeHosts(false);
        bizService = new DtoBizService();
        bizService.setService("MERGE-BIZ-SERVICE");
        bizService.setStatus("PENDING");
        bizService.setMessage("testing merge");
        bizHost.add(bizService);
        dtoBizHosts.add(bizHost);
        DtoOperationResults results = client.postHosts(dtoBizHosts);
        assert results != null;
        assert results.getCount() == 4;
        assert results.getSuccessful() == 2;
        assert results.getWarning() == 2;

        // validate updates and consolidated merge hosts events
        DtoHost dtoHost = hostClient.lookup("merge-biz-host");
        assert dtoHost != null;
        assert dtoHost.getHostName().equals("merge-biz-host");
        DtoDevice dtoDevice = deviceClient.lookup("merge-biz-device");
        assert dtoDevice != null;
        assert dtoDevice.getIdentification().equals("merge-biz-device");
        dtoDevice = deviceClient.lookup("MERGE-BIZ-DEVICE");
        assert dtoDevice == null;
        DtoService dtoService = serviceClient.lookup("merge-biz-service", "merge-biz-host");
        assert dtoService != null;
        assert dtoService.getHostName().equals("merge-biz-host");
        assert dtoService.getDescription().equals("merge-biz-service");
        dtoService = serviceClient.lookup("MERGE-BIZ-SERVICE", "merge-biz-host");
        assert dtoService == null;
        int eventCount = countMergeBizEvents(eventClient, "merge-biz-host");
        assert eventCount-initialEventCount == 2;

        // cleanup
        deviceClient.delete("merge-biz-device");
    }

    /**
     * Count merge events for host.
     *
     * @param eventClient evet client
     * @param hostName host name
     * @return events count
     */
    private int countMergeBizEvents(EventClient eventClient, String hostName) {
        int eventCount = 0;
        List<DtoEvent> dtoEvents = eventClient.query("hostStatus.host.hostName = '"+hostName+"'");
        if ((dtoEvents != null) && !dtoEvents.isEmpty()) {
            for (DtoEvent dtoEvent : dtoEvents) {
                if (dtoEvent.getHost().equals(hostName) &&
                        dtoEvent.getTextMessage().startsWith("Cannot update/merge hosts with matching names")) {
                    eventCount = dtoEvent.getMsgCount();
                }
            }
        }
        return eventCount;
    }

    protected static final String SELENIUM_TEST_SERVER = "http://172.28.111.204/api";

    protected String getSeleniumDeploymentURL() {
        return getDeploymentURL();
        //return SELENIUM_TEST_SERVER;
    }

    @Test
    public void testSeleniumCase() {
        if (serverDown) return;
        BizClient bizClient = new BizClient(getSeleniumDeploymentURL());
        ServiceClient serviceClient = new ServiceClient(getSeleniumDeploymentURL());
        DeviceClient deviceClient = new DeviceClient(getSeleniumDeploymentURL());

        // delete if it exists
        if (serviceClient.lookup("latestupgrade", "TestingHost3") != null) {
            serviceClient.delete("latestupgrade", "TestingHost3");

        }
        // first add it
        DtoService service = bizClient.createOrUpdateService("TestingHost3", "latestupgrade", "OK",
                " Last Check Time Mon Aug 04 10:52:10 IST 2014 Next Check Time Mon Aug 04 10:53:10 IST 2014 ",
                null, null, "BSM:Business Objects", null, "TestingHost3", "SEL", "f22b7bf3-f317-4846-8bea-ae39f7175f89");
        assert service != null;
        assert serviceClient.lookup("latestupgrade", "TestingHost3") != null;

        // then update it
        service = bizClient.createOrUpdateService("TestingHost3", "latestupgrade", "OK",
                " Last Check Time Mon Aug 04 10:52:10 IST 2014 Next Check Time Mon Aug 04 10:53:10 IST 2014 ",
                null, null, "BSM:Business Objects", null, "TestingHost3", "SEL", "f22b7bf3-f317-4846-8bea-ae39f7175f89");
        assert service != null;
        assert serviceClient.lookup("latestupgrade", "TestingHost3") != null;

        // cleanup
        serviceClient.delete("latestupgrade", "TestingHost3");
        assert serviceClient.lookup("latestupgrade", "TestingHost3") == null;
        deviceClient.delete("TestingHost3");
    }

    //@Test
    public void testResetSeleniumCase() {
        if (serverDown) return;
        ServiceClient  serviceClient = new ServiceClient(getSeleniumDeploymentURL());
        DeviceClient deviceClient = new DeviceClient(getSeleniumDeploymentURL());
        assert serviceClient.lookup("latestupgrade", "TestingHost3") != null;
        serviceClient.delete("latestupgrade", "TestingHost3");
        assert serviceClient.lookup("latestupgrade", "TestingHost3") == null;
        deviceClient.delete("TestingHost3");
    }

    //@Test
    public void testSeleniumCaseWithoutDelete() {
        if (serverDown) return;
        BizClient bizClient = new BizClient(getSeleniumDeploymentURL());
        DeviceClient deviceClient = new DeviceClient(getSeleniumDeploymentURL());
        DtoService service = bizClient.createOrUpdateService("TestingHost3", "latestupgrade", "OK",
                " Last Check Time Mon Aug 04 10:52:10 IST 2014 Next Check Time Mon Aug 04 10:53:10 IST 2014 ",
                null, null, "BSM:Business Objects", null, "TestingHost3", "SEL", "f22b7bf3-f317-4846-8bea-ae39f7175f89");
        System.out.println("service = " + service);

        ServiceClient  serviceClient = new ServiceClient(getSeleniumDeploymentURL());
        assert serviceClient.lookup("latestupgrade", "TestingHost3") != null;
        deviceClient.delete("TestingHost3");
    }

    @Test
    public void testHostsAndServicesInDowntime() {
        if (serverDown) return;
        BizClient bizClient = new BizClient(getDeploymentURL());
        EventClient eventClient = new EventClient(getDeploymentURL());

        // run test using JSON and XML
        bizClient.setMediaType(MediaType.APPLICATION_JSON_TYPE);
        eventClient.setMediaType(MediaType.APPLICATION_JSON_TYPE);
        testHostsAndServicesInDowntime(bizClient, eventClient);
        bizClient.setMediaType(MediaType.APPLICATION_XML_TYPE);
        eventClient.setMediaType(MediaType.APPLICATION_XML_TYPE);
        testHostsAndServicesInDowntime(bizClient, eventClient);
    }

    public void testHostsAndServicesInDowntime(BizClient bizClient, EventClient eventClient) {
        // set service in downtime
        DtoBizHostServiceInDowntimeList serviceInDowntime = bizClient.setInDowntime(Arrays.asList(new String[]{"localhost"}), Arrays.asList(new String[]{"local_memory"}), null, null, false, true);
        assertServiceInDowntime(serviceInDowntime, 1);

        // get service in downtime
        serviceInDowntime = bizClient.getInDowntime(serviceInDowntime);
        assertServiceInDowntime(serviceInDowntime, 1);

        // clear service in downtime
        serviceInDowntime = bizClient.clearInDowntime(serviceInDowntime);
        assertServiceInDowntime(serviceInDowntime, 0);

        // set host group in downtime
        DtoBizHostsAndServices hostGroup = new DtoBizHostsAndServices();
        hostGroup.setHostGroupNames(Arrays.asList(new String[]{"Linux Servers"}));
        hostGroup.setSetHosts(true);
        DtoBizHostServiceInDowntimeList hostGroupInDowntime = bizClient.setInDowntime(hostGroup);
        assertHostGroupInDowntime(hostGroupInDowntime, 1);

        // get host group in downtime
        hostGroupInDowntime = bizClient.getInDowntime(hostGroupInDowntime);
        assertHostGroupInDowntime(hostGroupInDowntime, 1);

        // clear host group in downtime
        hostGroupInDowntime = bizClient.clearInDowntime(hostGroupInDowntime);
        assertHostGroupInDowntime(hostGroupInDowntime, 0);

        // clear downtime event log messages
        List<DtoEvent> downtimeEvents = eventClient.query("applicationType.name = 'DOWNTIME'");
        assert downtimeEvents != null;
        assert downtimeEvents.size() == 22;
        List<String> downtimeEventIds = new ArrayList<String>(downtimeEvents.size());
        for (DtoEvent downtimeEvent : downtimeEvents) {
            downtimeEventIds.add(downtimeEvent.getId().toString());
        }
        DtoOperationResults results = eventClient.delete(downtimeEventIds);
        assert results != null;
        assert results.getCount() == downtimeEvents.size();
        assert results.getFailed() == 0;
        assert results.getSuccessful() == downtimeEvents.size();
    }

    @Test
    public void testCreateHostServiceWithDowntime() {
        if (serverDown) return;

        // create host with services with downtime
        BizClient client = new BizClient(getDeploymentURL());
        DtoBizHost bizHost = new DtoBizHost();
        bizHost.setHost("biz-host-1");
        bizHost.setStatus(MonitorStatusBubbleUp.SCHEDULED_DOWN);
        bizHost.setMessage("welcome home");
        bizHost.setDevice("biz-device-1");
        bizHost.setAppType("VEMA");
        bizHost.setAgentId("biz");
        bizHost.getProperties().put(CollageAdminInfrastructure.PROP_SCHEDULED_DOWNTIME_DEPTH, "1");
        DtoBizService bizService1 = new DtoBizService();
        bizService1.setService("biz-service-1");
        bizService1.setStatus(MonitorStatusBubbleUp.SCHEDULED_CRITICAL);
        bizService1.getProperties().put(CollageAdminInfrastructure.PROP_SCHEDULED_DOWNTIME_DEPTH, "1");
        bizHost.add(bizService1);
        DtoBizService bizService2 = new DtoBizService();
        bizService2.setService("biz-service-2");
        bizService2.setStatus(MonitorStatusBubbleUp.SCHEDULED_CRITICAL);
        bizService2.setMessage("welcome home again");
        bizService2.getProperties().put(CollageAdminInfrastructure.PROP_SCHEDULED_DOWNTIME_DEPTH, "1");
        bizHost.add(bizService2);
        DtoOperationResults results = client.postHosts(new DtoBizHostList(Arrays.asList(new DtoBizHost[]{bizHost})));
        assert results != null;
        assert results.getCount() == 3;
        assert results.getSuccessful() == 3;
        assert "biz-host-1".equals(results.getResults().get(0).getEntity());
        assert results.getResults().get(0).getLocation() != null;
        assert "Insert".equals(results.getResults().get(0).getMessage());
        assert "biz-host-1:biz-service-1".equals(results.getResults().get(1).getEntity());
        assert results.getResults().get(1).getLocation() != null;
        assert "Insert".equals(results.getResults().get(1).getMessage());
        assert "biz-host-1:biz-service-2".equals(results.getResults().get(2).getEntity());
        assert results.getResults().get(2).getLocation() != null;
        assert "Insert".equals(results.getResults().get(2).getMessage());

        HostClient hostClient = new HostClient(getDeploymentURL());
        DtoHost host = hostClient.lookup("biz-host-1");
        assert host != null;
        host.setMonitorStatus(MonitorStatusBubbleUp.SCHEDULED_DOWN);
        results = hostClient.post(new DtoHostList(Arrays.asList(new DtoHost[]{host})));
        assert results != null;

        // validate host
        host = hostClient.lookup("biz-host-1");
        assert "biz-host-1".equals(host.getHostName());
        assert MonitorStatusBubbleUp.SCHEDULED_DOWN.equals(host.getMonitorStatus());
        assert "1".equals(host.getProperty(CollageAdminInfrastructure.PROP_SCHEDULED_DOWNTIME_DEPTH));

        // set services monitor status
        ServiceClient serviceClient = new ServiceClient(getDeploymentURL());
        DtoService service1 = serviceClient.lookup("biz-service-1", "biz-host-1");
        assert service1 != null;
        service1.setMonitorStatus(MonitorStatusBubbleUp.SCHEDULED_CRITICAL);
        serviceClient.post(new DtoServiceList(Arrays.asList(new DtoService[]{service1})));
        DtoService service2 = serviceClient.lookup("biz-service-2", "biz-host-1");
        assert service2 != null;
        service2.setMonitorStatus(MonitorStatusBubbleUp.SCHEDULED_CRITICAL);
        serviceClient.post(new DtoServiceList(Arrays.asList(new DtoService[]{service2})));

        service1 = serviceClient.lookup("biz-service-1", "biz-host-1");
        assert "biz-host-1".equals(service1.getHostName());
        assert "biz-service-1".equals(service1.getDescription());
        assert "biz-device-1".equals(service1.getDeviceIdentification());
        assert MonitorStatusBubbleUp.SCHEDULED_CRITICAL.equals(service1.getMonitorStatus());
        assert "1".equals(service1.getProperty(CollageAdminInfrastructure.PROP_SCHEDULED_DOWNTIME_DEPTH));
        service2 = serviceClient.lookup("biz-service-2", "biz-host-1");
        assert service2 != null;
        assert "biz-host-1".equals(service2.getHostName());
        assert "biz-service-2".equals(service2.getDescription());
        assert "biz-device-1".equals(service2.getDeviceIdentification());
        assert MonitorStatusBubbleUp.SCHEDULED_CRITICAL.equals(service2.getMonitorStatus());
        assert "1".equals(service2.getProperty(CollageAdminInfrastructure.PROP_SCHEDULED_DOWNTIME_DEPTH));

        // attempt monitor status override with a scheduled downtime host and service....
        bizHost = new DtoBizHost();
        bizHost.setHost("biz-host-1");
        bizHost.setStatus(MonitorStatusBubbleUp.UNSCHEDULED_DOWN); // should not update
        bizHost.setMessage("welcome home TEST"); // should update
        bizHost.setDevice("biz-device-1");
        bizHost.setAppType("VEMA");
        bizHost.setAgentId("biz");
        bizService1 = new DtoBizService();
        bizService1.setService("biz-service-1");
        bizService1.setStatus(MonitorStatusBubbleUp.UNSCHEDULED_CRITICAL);
        bizService1.getProperties().put("LastPluginOutput", "(syn.vm.cpu.maxUsed) Status = UNSCHEDULED CRITICAL, value 3%\n[WC=0.0] Thu Sep 03 02:24:42 PDT 2015");
        bizHost.add(bizService1);
        results = client.postHosts(new DtoBizHostList(Arrays.asList(new DtoBizHost[]{bizHost})));
        assert results != null;
        assert results.getCount() == 2;
        assert results.getSuccessful() == 2;
        assert "biz-host-1".equals(results.getResults().get(0).getEntity());
        assert results.getResults().get(0).getLocation() != null;
        assert "Update".equals(results.getResults().get(0).getMessage());
        assert "biz-host-1:biz-service-1".equals(results.getResults().get(1).getEntity());
        assert results.getResults().get(1).getLocation() != null;
        assert "Update".equals(results.getResults().get(1).getMessage());

        // validate status did not update
        host = hostClient.lookup("biz-host-1");
        assert host != null;
        assert "biz-host-1".equals(host.getHostName());
        assert "welcome home TEST".equals(host.getProperty("LastPluginOutput"));
        assert MonitorStatusBubbleUp.SCHEDULED_DOWN.equals(host.getMonitorStatus());
        assert "1".equals(host.getProperty(CollageAdminInfrastructure.PROP_SCHEDULED_DOWNTIME_DEPTH));

        service1 = serviceClient.lookup("biz-service-1", "biz-host-1");
        assert service1 != null;
        assert "biz-host-1".equals(service1.getHostName());
        assert "biz-service-1".equals(service1.getDescription());
        assert "biz-device-1".equals(service1.getDeviceIdentification());
        assert MonitorStatusBubbleUp.SCHEDULED_CRITICAL.equals(service1.getMonitorStatus());
        assert service1.getProperty("LastPluginOutput").equals("(syn.vm.cpu.maxUsed) Status = SCHEDULED CRITICAL, value 3%\n[WC=0.0] Thu Sep 03 02:24:42 PDT 2015");
        assert "1".equals(service1.getProperty(CollageAdminInfrastructure.PROP_SCHEDULED_DOWNTIME_DEPTH));

        // Test inverse by clearing downtime
        host = hostClient.lookup("biz-host-1");
        assert host != null;
        host.getProperties().put(CollageAdminInfrastructure.PROP_SCHEDULED_DOWNTIME_DEPTH, "0");
        results = hostClient.post(new DtoHostList(Arrays.asList(new DtoHost[]{host})));
        assert results != null;
        service1 = serviceClient.lookup("biz-service-1", "biz-host-1");
        assert service1 != null;
        service1.getProperties().put(CollageAdminInfrastructure.PROP_SCHEDULED_DOWNTIME_DEPTH, "0");
        serviceClient.post(new DtoServiceList(Arrays.asList(new DtoService[]{service1})));
        service2 = serviceClient.lookup("biz-service-2", "biz-host-1");
        assert service2 != null;
        service2.getProperties().put(CollageAdminInfrastructure.PROP_SCHEDULED_DOWNTIME_DEPTH, "0");
        serviceClient.post(new DtoServiceList(Arrays.asList(new DtoService[]{service2})));

        // attempt to override again, should pass this time
        bizHost = new DtoBizHost();
        bizHost.setHost("biz-host-1");
        bizHost.setStatus(MonitorStatusBubbleUp.UNSCHEDULED_DOWN); // should not update
        bizHost.setMessage("welcome home TEST"); // should update
        bizHost.setDevice("biz-device-1");
        bizHost.setAppType("VEMA");
        bizHost.setAgentId("biz");
        bizService1 = new DtoBizService();
        bizService1.setService("biz-service-1");
        bizService1.setStatus(MonitorStatusBubbleUp.UNSCHEDULED_CRITICAL);
        bizHost.add(bizService1);
        bizService2 = new DtoBizService();
        bizService2.setService("biz-service-2");
        bizService2.setStatus(MonitorStatusBubbleUp.UNSCHEDULED_CRITICAL);
        bizHost.add(bizService2);
        results = client.postHosts(new DtoBizHostList(Arrays.asList(new DtoBizHost[]{bizHost})));
        assert results != null;
        assert results.getCount() == 3;
        assert results.getSuccessful() == 3;
        assert "biz-host-1".equals(results.getResults().get(0).getEntity());
        assert results.getResults().get(0).getLocation() != null;
        assert "Update".equals(results.getResults().get(0).getMessage());
        assert "biz-host-1:biz-service-1".equals(results.getResults().get(1).getEntity());
        assert results.getResults().get(1).getLocation() != null;
        assert "Update".equals(results.getResults().get(1).getMessage());
        assert "biz-host-1:biz-service-2".equals(results.getResults().get(2).getEntity());
        assert results.getResults().get(2).getLocation() != null;
        assert "Update".equals(results.getResults().get(2).getMessage());

        host = hostClient.lookup("biz-host-1");
        assert "biz-host-1".equals(host.getHostName());
        assert MonitorStatusBubbleUp.UNSCHEDULED_DOWN.equals(host.getMonitorStatus());
        assert "0".equals(host.getProperty(CollageAdminInfrastructure.PROP_SCHEDULED_DOWNTIME_DEPTH));

        service1 = serviceClient.lookup("biz-service-1", "biz-host-1");
        assert service1 != null;
        assert MonitorStatusBubbleUp.UNSCHEDULED_CRITICAL.equals(service1.getMonitorStatus());
        assert "0".equals(service1.getProperty(CollageAdminInfrastructure.PROP_SCHEDULED_DOWNTIME_DEPTH));
        service2 = serviceClient.lookup("biz-service-2", "biz-host-1");
        assert service2 != null;
        assert MonitorStatusBubbleUp.UNSCHEDULED_CRITICAL.equals(service2.getMonitorStatus());
        assert "0".equals(service2.getProperty(CollageAdminInfrastructure.PROP_SCHEDULED_DOWNTIME_DEPTH));

        // Clean up
        DeviceClient deviceClient = new DeviceClient(getDeploymentURL());
        deviceClient.delete("biz-device-1"); // cascade deletes of hosts and services
    }

    private void assertServiceInDowntime(DtoBizHostServiceInDowntimeList serviceInDowntime, int scheduledDowntimeDepth) {
        assert serviceInDowntime.size() == 1;
        DtoBizHostServiceInDowntime serviceInDowntime0 = serviceInDowntime.getBizHostServiceInDowntimes().get(0);
        assert "localhost".equals(serviceInDowntime0.getHostName());
        assert "local_memory".equals(serviceInDowntime0.getServiceDescription());
        assert serviceInDowntime0.getScheduledDowntimeDepth() != null;
        assert serviceInDowntime0.getScheduledDowntimeDepth().intValue() == scheduledDowntimeDepth;
        assert SERVICE_ENTITY_TYPE.equals(serviceInDowntime0.getEntityType());
        assert "localhost:local_memory".equals(serviceInDowntime0.getEntityName());
    }

    private void assertHostGroupInDowntime(DtoBizHostServiceInDowntimeList hostGroupInDowntime, int scheduledDowntimeDepth) {
        assert hostGroupInDowntime.size() > 0;
        DtoBizHostServiceInDowntime localhostInDowntime = null;
        for (DtoBizHostServiceInDowntime hostInDowntime : hostGroupInDowntime.getBizHostServiceInDowntimes()) {
            if ("localhost".equals(hostInDowntime.getHostName())) {
                localhostInDowntime = hostInDowntime;
                break;
            }
        }
        assert localhostInDowntime != null;
        assert localhostInDowntime.getServiceDescription() == null;
        assert localhostInDowntime.getScheduledDowntimeDepth() != null;
        assert localhostInDowntime.getScheduledDowntimeDepth().intValue() == scheduledDowntimeDepth;
        assert HOST_GROUP_ENTITY_TYPE.equals(localhostInDowntime.getEntityType());
        assert "Linux Servers".equals(localhostInDowntime.getEntityName());
    }

    //@Test
    public void setupGoingIntoDowntime() {

        HostClient hostClient = new HostClient(getDeploymentURL());
        DtoHost host = hostClient.lookup("eng-opentsdb-1.groundwork.groundworkopensource.com");
        assert host != null;
        host.setMonitorStatus(MonitorStatusBubbleUp.SCHEDULED_DOWN);
        host.getProperties().put(CollageAdminInfrastructure.PROP_SCHEDULED_DOWNTIME_DEPTH, "1");
        hostClient.post(new DtoHostList(Arrays.asList(new DtoHost[]{host})));

        // set services monitor status
        ServiceClient serviceClient = new ServiceClient(getDeploymentURL());
        DtoService service1 = serviceClient.lookup("syn.vm.cpu.cpuToMax.used", "eng-opentsdb-1.groundwork.groundworkopensource.com");
        assert service1 != null;
        service1.setMonitorStatus(MonitorStatusBubbleUp.SCHEDULED_CRITICAL);
        service1.getProperties().put(CollageAdminInfrastructure.PROP_SCHEDULED_DOWNTIME_DEPTH, "1");
        serviceClient.post(new DtoServiceList(Arrays.asList(new DtoService[]{service1})));

        host = hostClient.lookup("eng-opentsdb-1.groundwork.groundworkopensource.com");
        assert "1".equals(host.getProperty(CollageAdminInfrastructure.PROP_SCHEDULED_DOWNTIME_DEPTH));
        assert MonitorStatusBubbleUp.SCHEDULED_DOWN.equals(host.getMonitorStatus());

        service1 = serviceClient.lookup("syn.vm.cpu.cpuToMax.used", "eng-opentsdb-1.groundwork.groundworkopensource.com");
        assert MonitorStatusBubbleUp.SCHEDULED_CRITICAL.equals(service1.getMonitorStatus());
        assert "1".equals(service1.getProperty(CollageAdminInfrastructure.PROP_SCHEDULED_DOWNTIME_DEPTH));

    }

    //@Test
    public void setupGoingExitingDowntime() {

        HostClient hostClient = new HostClient(getDeploymentURL());
        DtoHost host = hostClient.lookup("eng-opentsdb-1.groundwork.groundworkopensource.com");
        assert host != null;
        host.getProperties().put(CollageAdminInfrastructure.PROP_SCHEDULED_DOWNTIME_DEPTH, "0");
        hostClient.post(new DtoHostList(Arrays.asList(new DtoHost[]{host})));

        // set services monitor status
        ServiceClient serviceClient = new ServiceClient(getDeploymentURL());
        DtoService service1 = serviceClient.lookup("syn.vm.cpu.cpuToMax.used", "eng-opentsdb-1.groundwork.groundworkopensource.com");
        assert service1 != null;
        //service1.setMonitorStatus(MonitorStatusBubbleUp.SCHEDULED_CRITICAL);
        service1.getProperties().put(CollageAdminInfrastructure.PROP_SCHEDULED_DOWNTIME_DEPTH, "0");
        serviceClient.post(new DtoServiceList(Arrays.asList(new DtoService[]{service1})));

        host = hostClient.lookup("eng-opentsdb-1.groundwork.groundworkopensource.com");
        assert "0".equals(host.getProperty(CollageAdminInfrastructure.PROP_SCHEDULED_DOWNTIME_DEPTH));
        assert MonitorStatusBubbleUp.SCHEDULED_DOWN.equals(host.getMonitorStatus());

        service1 = serviceClient.lookup("syn.vm.cpu.cpuToMax.used", "eng-opentsdb-1.groundwork.groundworkopensource.com");
        //assert MonitorStatusBubbleUp.SCHEDULED_CRITICAL.equals(service1.getMonitorStatus());
        assert "0".equals(service1.getProperty(CollageAdminInfrastructure.PROP_SCHEDULED_DOWNTIME_DEPTH));

    }

    @Test
    public void testAuthorizationServices() {
        if (serverDown) return;

        // test authorization
        BizClient client = new BizClient(getDeploymentURL());
        List<String> authorizedHostGroups = Arrays.asList(new String[]{"Linux Servers"});
        DtoBizAuthorization authorization = new DtoBizAuthorization(authorizedHostGroups, null);
        DtoBizAuthorizedServices authorizedServices = client.getAuthorizedServices(authorization);
        assert authorizedServices != null;
        assert authorizedServices.getHostNames() != null;
        assert !authorizedServices.getHostNames().isEmpty();
        assert authorizedServices.getHostNames().contains("localhost");
        assert authorizedServices.getServiceHostNames() != null;
        assert !authorizedServices.getServiceHostNames().isEmpty();
        assert authorizedServices.getServiceHostNames().containsKey("local_cpu_httpd");
        assert authorizedServices.getServiceHostNames().get("local_cpu_httpd") != null;
        assert !authorizedServices.getServiceHostNames().get("local_cpu_httpd").isEmpty();
        assert authorizedServices.getServiceHostNames().get("local_cpu_httpd").contains("localhost");
        authorization = new DtoBizAuthorization();
        authorizedServices = client.getAuthorizedServices(authorization);
        assert authorizedServices == null;
        authorizedServices = client.getAuthorizedServices();
        assert authorizedServices != null;
        assert authorizedServices.getHostNames() != null;
        assert !authorizedServices.getHostNames().isEmpty();
        assert authorizedServices.getServiceHostNames() != null;
        assert !authorizedServices.getServiceHostNames().isEmpty();
    }
}
