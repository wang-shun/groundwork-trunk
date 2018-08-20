package org.groundwork.rs.client;

import org.groundwork.rs.dto.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class SeleniumUseCaseTest extends AbstractClientTest {

    private final String APP_TYPE_NAME = "SEL";
    private final String HOSTGROUP_NAME = "TEST_SELENIUM_HOSTGROUP";
    private final String HOST_NAME = "TEST_SELENIUM_HOST";
    private final String SERVICE_NAME = "TEST_SELENIUM_SERVICE";

    // This method must be kept in sync with all the above methods.  It needs to ensure that anything that could be
    // created is deleted here so that in the case of a premature exit everything will get deleted.  In addition,
    // it should attempt to delete blindly and ignore any status codes so that it doesn't exit prematurely itself.
    @Before
    @After
    public void cleanupAnyExistingHostFromFailedRun() throws Exception {
        HostGroupClient hostGroupClient = new HostGroupClient(deploymentUrl);
        hostGroupClient.delete(HOSTGROUP_NAME);

        HostClient hostClient = new HostClient(deploymentUrl);
        hostClient.delete(HOST_NAME);

        ServiceClient serviceClient = new ServiceClient(deploymentUrl);
        serviceClient.delete(SERVICE_NAME, HOST_NAME);
    }

    @Test
    public void SimpleTest() {
        // Check if a host group exists (HostGroupClient.lookup)
        lookupHostGroup();

        // Add a host group (HostGroupClient.post)
        addHostGroup();

        // Check if a host exists (HostGroupClient.lookup)
        lookupHost();

        // Add a host
        addHost();

        // Add host to a group (BizClient.createOrUpdateHost)
        addToHostGroup();

        // Lookup a service (ServiceClient.lookup)
        lookupService();

        // Add a service to the host (BizClient.createOrUpdateService)
        addService();

        // Update service status (BizClient.createOrUpdateService)
        updateService();

        // Delete a service (ServiceClient.delete)
        deleteService();

        // Delete a host (HostClient.delete)
        deleteHost();

        // Delete a host group (HostGroupClient.delete)
        deleteHostGroup();
    }

    private void lookupHostGroup() {
        HostGroupClient hostGroupClient = new HostGroupClient(deploymentUrl);
        assertNull("Unable to find host group", hostGroupClient.lookup(HOSTGROUP_NAME));
    }

    private void addHostGroup() {
        DtoHostGroup dtoHostGroup = new DtoHostGroup();
        dtoHostGroup.setName(HOSTGROUP_NAME);
        DtoHostGroupList dtoHostGroupList = new DtoHostGroupList();
        dtoHostGroupList.add(dtoHostGroup);
        HostGroupClient hostGroupClient = new HostGroupClient(deploymentUrl);
        assertResultsSuccessful(hostGroupClient.post(dtoHostGroupList));
    }

    private void lookupHost() {
        HostClient hostClient = new HostClient(deploymentUrl);
        assertNull("Found unexpected host", hostClient.lookup(HOST_NAME));
    }

    private void addHost() {
        DtoHostList dtoHostList = new DtoHostList();
        dtoHostList.add(constructHost());
        HostClient hostClient = new HostClient(deploymentUrl);
        assertResultsSuccessful(hostClient.post(dtoHostList));
    }

    private void addToHostGroup() {
        HostClient hostClient = new HostClient(deploymentUrl);
        DtoHost dtoHost = hostClient.lookup(HOST_NAME);
        assertNotNull("Unable to find host", dtoHost);

        DtoHostList dtoHostList = new DtoHostList();
        dtoHostList.add(dtoHost);
        HostGroupClient hostGroupClient = new HostGroupClient(deploymentUrl);
        DtoHostGroup dtoHostGroup = hostGroupClient.lookup(HOSTGROUP_NAME);
        assertNotNull("Unable to find host group", dtoHostGroup);

        dtoHostGroup.addHost(dtoHost);
        DtoHostGroupList dtoHostGroupList = new DtoHostGroupList();
        dtoHostGroupList.add(dtoHostGroup);
        assertResultsSuccessful(hostGroupClient.post(dtoHostGroupList));
    }

    private void lookupService() {
        ServiceClient serviceClient = new ServiceClient(deploymentUrl);
        assertNull("Found unexpected service", serviceClient.lookup(SERVICE_NAME, HOST_NAME));
    }

    private void addService() {
        DtoServiceList dtoServiceList = new DtoServiceList();
        dtoServiceList.add(constructService());
        ServiceClient serviceClient = new ServiceClient(deploymentUrl);
        assertResultsSuccessful(serviceClient.post(dtoServiceList));
        assertNotNull("No services found for host", serviceClient.list(HOST_NAME));
    }

    private void updateService() {
        ServiceClient serviceClient = new ServiceClient(deploymentUrl);
        DtoService dtoService = serviceClient.lookup(SERVICE_NAME, HOST_NAME);
        assertNotNull("Unable to find service", dtoService);

        dtoService.setMonitorStatus("DOWN");
        DtoServiceList dtoServiceList = new DtoServiceList();
        dtoServiceList.add(dtoService);
        assertResultsSuccessful(serviceClient.post(dtoServiceList));

        dtoService.setMonitorStatus("UP");
        assertResultsSuccessful(serviceClient.post(dtoServiceList));
    }

    private void deleteService() {
        ServiceClient serviceClient = new ServiceClient(deploymentUrl);
        assertResultsSuccessful(serviceClient.delete(SERVICE_NAME, HOST_NAME));
        assertNull("Found unexpected service", serviceClient.lookup(SERVICE_NAME, HOST_NAME));
        serviceClient.list(HOST_NAME);
        assertTrue("Found unexpected service for host", serviceClient.list(HOST_NAME).size() == 0);
    }

    private void deleteHost() {
        HostClient hostClient = new HostClient(deploymentUrl);
        assertResultsSuccessful(hostClient.delete(HOST_NAME));
    }

    private void deleteHostGroup() {
        HostGroupClient hostGroupClient = new HostGroupClient(deploymentUrl);
        assertResultsSuccessful(hostGroupClient.delete(HOSTGROUP_NAME));
    }

    private void assertResultsSuccessful(DtoOperationResults results) {
        assertNotNull("Results are null", results);
        assertTrue("Results are empty", results.getCount() > 0);
        for (DtoOperationResult result : results.getResults()) {
            assertEquals("Unsuccessful result", DtoOperationResult.SUCCESS, result.getStatus());
        }
    }

    private DtoHost constructHost() {
        DtoHost host = new DtoHost();
        host.setHostName(HOST_NAME);
        host.setDescription(HOST_NAME + "_DESCRIPTION");
        host.setAppType(APP_TYPE_NAME);
        host.setDeviceIdentification(HOST_NAME + "_IDENTIFICATION");
        host.setMonitorServer("localhost");
        host.setDeviceDisplayName(HOST_NAME + "_DEVICE");
        host.setMonitorStatus("UP");
        return host;
    }

    private DtoService constructService() {
        DtoService service = new DtoService();
        service.setHostName(HOST_NAME);
        service.setDescription(SERVICE_NAME);
        service.setAppType(APP_TYPE_NAME);
        service.setMonitorStatus("UP");
        service.setLastHardState("PENDING");
        return service;
    }

}
