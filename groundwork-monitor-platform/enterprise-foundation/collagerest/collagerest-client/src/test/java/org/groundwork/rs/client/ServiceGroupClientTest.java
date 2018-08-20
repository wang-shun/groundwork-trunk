package org.groundwork.rs.client;


import org.groundwork.rs.dto.DtoName;
import org.groundwork.rs.dto.DtoOperationResults;
import org.groundwork.rs.dto.DtoService;
import org.groundwork.rs.dto.DtoServiceGroup;
import org.groundwork.rs.dto.DtoServiceGroupMemberUpdate;
import org.groundwork.rs.dto.DtoServiceGroupUpdate;
import org.groundwork.rs.dto.DtoServiceGroupUpdateList;
import org.groundwork.rs.dto.DtoServiceKey;
import org.groundwork.rs.dto.DtoServiceList;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class ServiceGroupClientTest extends AbstractClientTest  {


    String[] SG_NAMES = { "SG-200", "SG-201", "SG-202", "SG-203"};
    String[] SG_DESC = { "Service Group 200", "Service Group 201", "Service Group 202", null};
    String[] SG_APP_TYPES = { "VEMA", "VEMA", "NAGIOS", null};
    String[] SG_AGENTS = { "Agent-007", "Agent-008", "Agent-008", null};
    String[][] SG_SERVICES = {
            { "local_cpu_java", "local_cpu_perl" },
            { "local_cpu_java", "local_cpu_perl" },
            { "local_cpu_java", "local_cpu_perl" },
            { "local_cpu_java", "local_cpu_perl" }
    };
    String[][] SG_HOSTS = {
            { "localhost", "localhost" },
            { "localhost", "localhost" },
            { "localhost", "localhost" },
            { "localhost", "localhost" }
    };

    @Test
    public void testAllServiceGroups() throws Exception {
        if (serverDown) return;
        ServiceGroupClient client = new ServiceGroupClient(getDeploymentURL());

        client.setMediaType(MediaType.APPLICATION_JSON_TYPE);
        testAllServiceGroups(client);
        client.setMediaType(MediaType.APPLICATION_XML_TYPE);
        testAllServiceGroups(client);
    }

    private void testAllServiceGroups(ServiceGroupClient client) throws Exception {

        // Test inserts
        int index = 0;
        for (String name : SG_NAMES) {
            DtoServiceGroupUpdateList updates = new DtoServiceGroupUpdateList();
            DtoServiceGroupUpdate update = create(name, SG_DESC[index], SG_APP_TYPES[index], SG_AGENTS[index], SG_SERVICES[index], SG_HOSTS[index]);
            updates.add(update);
            DtoOperationResults results = client.post(updates);
            assert results.getSuccessful() == 1;
            index++;
        }

        // test single reads
        index = 0;
        for (String name : SG_NAMES) {
            DtoServiceGroup group = client.lookup(name);
            assert group != null;
            assert group.getName().equals(name);
            if (index == 3) {
                assert group.getDescription() == null;
                assert group.getAppType() == null;
                assert group.getAgentId() == null;
            }
            else {
                assert group.getDescription().equals(SG_DESC[index]);
                assert group.getAppType().equals(SG_APP_TYPES[index]);
                assert group.getAgentId().equals(SG_AGENTS[index]);
            }
            assert group.getServices() != null;
            assert group.getServices().size() == 2;
            assertServices(group);
            index++;
        }

        // test all queries
        List<DtoServiceGroup> all = client.list();
        assert all != null;
        assert all.size() == 6;
        for (DtoServiceGroup group : all) {
            if (group.getName().startsWith("SG-")) {
                assert group.getServices() != null;
                assert group.getServices().size() == 2;
                assertServices(group);
            }
        }

        // test query by appType
        List<DtoServiceGroup> nagios = client.list("NAGIOS", null);
        assert nagios != null;
        assert nagios.size() == 2;
        index = 2;
        for (DtoServiceGroup group : nagios) {
            if (!group.getName().equals("SG1")) {
                assert group.getName().equals(SG_NAMES[index]);
                assert group.getDescription().equals(SG_DESC[index]);
                assert group.getAppType().equals(SG_APP_TYPES[index]);
                assert group.getAgentId().equals(SG_AGENTS[index]);
                assert group.getServices() != null;
                assert group.getServices().size() == 2;
                assertServices(group);
                index++;
            }
        }
        List<DtoServiceGroup> vema = client.list("VEMA", null);
        assert vema != null;
        assert vema.size() == 2;
        int increment = 1;
        if (vema.get(0).getName().equals(SG_NAMES[0])) {
            index = 0;
        }
        else {
            index = 1;
            increment = -1;
        }
        for (DtoServiceGroup group : vema) {
            assert group.getName().equals(SG_NAMES[index]);
            assert group.getDescription().equals(SG_DESC[index]);
            assert group.getAppType().equals(SG_APP_TYPES[index]);
            assert group.getAgentId().equals(SG_AGENTS[index]);
            assert group.getServices() != null;
            assert group.getServices().size() == 2;
            assertServices(group);
            index = index + increment;
        }

        // test query by agentId
        List<DtoServiceGroup> groups = client.list(null, "Agent-007");
        assert groups != null;
        assert groups.size() == 1;
        index = 0;
        for (DtoServiceGroup group : groups) {
            assert group.getName().equals(SG_NAMES[index]);
            assert group.getDescription().equals(SG_DESC[index]);
            assert group.getAppType().equals(SG_APP_TYPES[index]);
            assert group.getAgentId().equals(SG_AGENTS[index]);
            assert group.getServices() != null;
            assert group.getServices().size() == 2;
            assertServices(group);
            index++;
        }
        groups = client.list(null, "Agent-008");
        assert groups != null;
        assert groups.size() == 2;
        increment = 1;
        if (groups.get(0).getName().equals(SG_NAMES[1])) {
            index = 1;
        }
        else {
            index = 2;
            increment = -1;
        }
        for (DtoServiceGroup group : groups) {
            assert group.getName().equals(SG_NAMES[index]);
            assert group.getDescription().equals(SG_DESC[index]);
            assert group.getAppType().equals(SG_APP_TYPES[index]);
            assert group.getAgentId().equals(SG_AGENTS[index]);
            assert group.getServices() != null;
            assert group.getServices().size() == 2;
            assertServices(group);
            index = index + increment;
        }

        // test query by appType and Agent
        groups = client.list("VEMA", "Agent-008");
        assert groups != null;
        assert groups.size() == 1;
        index = 1;
        for (DtoServiceGroup group : groups) {
            assert group.getName().equals(SG_NAMES[index]);
            assert group.getDescription().equals(SG_DESC[index]);
            assert group.getAppType().equals(SG_APP_TYPES[index]);
            assert group.getAgentId().equals(SG_AGENTS[index]);
            assert group.getServices() != null;
            assert group.getServices().size() == 2;
            assertServices(group);
            index++;
        }

        // wait to ensure propagation and test autocomplete
        Thread.sleep(250);
        List<DtoName> suggestions = client.autocomplete("sg");
        assertNotNull(suggestions);
        assertEquals(5, suggestions.size());
        assertTrue(suggestions.containsAll(Arrays.asList(new DtoName[]{
                new DtoName("SG-200"),
                new DtoName("SG-201"),
                new DtoName("SG-202"),
                new DtoName("SG-203"),
                new DtoName("SG1")})));
        suggestions = client.autocomplete("zzz");
        assertNotNull(suggestions);
        assertEquals(0, suggestions.size());
        suggestions = client.autocomplete("sg", 3);
        assertNotNull(suggestions);
        assertEquals(3, suggestions.size());
        assertTrue(suggestions.containsAll(Arrays.asList(new DtoName[]{
                new DtoName("SG-200"),
                new DtoName("SG-201"),
                new DtoName("SG-202")})));

        // cleanup
        DtoServiceGroupUpdateList deletes = new DtoServiceGroupUpdateList();
        for (String name : SG_NAMES) {
            deletes.add(new DtoServiceGroupUpdate(name));
        }
        client.delete(deletes);

        // verify cleanup
        for (String name : SG_NAMES) {
            assert client.lookup(name) == null;
        }

        // test warning for missing delete
        DtoOperationResults deleteResults = client.delete(Arrays.asList(new String[]{"NotAServiceGroup"}));
        assert deleteResults != null;
        assert deleteResults.getWarning() == 1;
    }

    @Test
    public void testUpdateGroups() throws Exception {
        if (serverDown) return;
        ServiceGroupClient client = new ServiceGroupClient(getDeploymentURL());

        client.setMediaType(MediaType.APPLICATION_JSON_TYPE);
        testUpdateGroups(client);
        client.setMediaType(MediaType.APPLICATION_XML_TYPE);
        testUpdateGroups(client);
    }

    private void testUpdateGroups(ServiceGroupClient client) {
        int index = 0;

        // create it
        DtoServiceGroupUpdateList updates = new DtoServiceGroupUpdateList();
        DtoServiceGroupUpdate update = create(SG_NAMES[index], SG_DESC[index], SG_APP_TYPES[index], SG_AGENTS[index], SG_SERVICES[index], SG_HOSTS[index]);
        updates.add(update);
        DtoOperationResults results = client.post(updates);
        assert results.getSuccessful() == 1;

        // update it
        String [] updatedServices = { "local_load", "local_users", "tcp_http" };
        String [] updatedHosts = { "localhost", "localhost", "localhost" };

        update.setAppType("CACTI");
        update.setDescription("Updated");
        update.setAgentId("SECRET");
        update.getServices().clear();
        index = 0;
        for (String service : updatedServices) {
            update.addService(new DtoServiceKey(service, updatedHosts[index]));
            index++;
        }
        updates.getServiceGroups().clear();
        updates.add(update);
        results = client.post(updates);
        assert results.getSuccessful() == 1;

        DtoServiceGroup group = client.lookup(SG_NAMES[0]);
        assert group != null;
        assert group.getName().equals(SG_NAMES[0]);
        assert group.getDescription().equals("Updated");
        assert group.getAppType().equals("CACTI");
        assert group.getAgentId().equals("SECRET");
        assert group.getServices() != null;
        assert group.getServices().size() == 3;

        int count = 0;
        for (DtoService service : group.getServices()) {
            if (service.getDescription().equals("local_load")) {
                assert service.getHostName().equals("localhost");
                count++;
            }
            else if (service.getDescription().equals("local_users")) {
                assert service.getHostName().equals("localhost");
                count++;
            }
            else if (service.getDescription().equals("tcp_http")) {
                assert service.getHostName().equals("localhost");
                count++;
            }
            else {
                fail("service name not expected: " + service.getDescription());
            }
        }
        assert count == 3;

        // cleanup
        client.delete(SG_NAMES[0]);

        // verify cleanup
        assert client.lookup(SG_NAMES[0]) == null;
    }

    @Test
    public void testCreateBadData() throws Exception {
        if (serverDown) return;
        ServiceGroupClient client = new ServiceGroupClient(getDeploymentURL());

        client.setMediaType(MediaType.APPLICATION_JSON_TYPE);
        testCreateBadData(client);
        client.setMediaType(MediaType.APPLICATION_XML_TYPE);
        testCreateBadData(client);
    }

    private void testCreateBadData(ServiceGroupClient client) throws Exception {
        // bad apptype
        DtoServiceGroupUpdateList updates = new DtoServiceGroupUpdateList();
        DtoServiceGroupUpdate update = create("BadGroup", "Bad Service Group", "BadAppType", null, SG_SERVICES[0], SG_HOSTS[0]);
        updates.add(update);
        DtoOperationResults results = client.post(updates);
        assert results.getFailed() == 1;
        assert results.getResults().get(0).getMessage().contains("Failed to find app type");

        // bad service
        String [] badServices = { "bad-service" };
        String [] okServices = { "local_cpu_java"};
        update = create(SG_NAMES[0], SG_DESC[0], SG_APP_TYPES[0], SG_AGENTS[0], badServices, SG_HOSTS[0]);
        updates.getServiceGroups().clear();
        updates.add(update);
        results = client.post(updates);
        assert results.getFailed() == 1;
        assert results.getResults().get(0).getMessage().contains("Failed to find service");

        // bad host
        String [] badHosts = { "bad-host" };
        update = create(SG_NAMES[0], SG_DESC[0], SG_APP_TYPES[0], SG_AGENTS[0], okServices, badHosts);
        updates.getServiceGroups().clear();
        updates.add(update);
        results = client.post(updates);
        assert results.getFailed() == 1;
        assert results.getResults().get(0).getMessage().contains("Failed to find service");

    }

    @Test
    public void testAddAndDeleteMembers() throws Exception {
        if (serverDown) return;
        ServiceGroupClient client = new ServiceGroupClient(getDeploymentURL());

        client.setMediaType(MediaType.APPLICATION_JSON_TYPE);
        testAddAndDeleteMembers(client);
        client.setMediaType(MediaType.APPLICATION_XML_TYPE);
        testAddAndDeleteMembers(client);
    }

    private void testAddAndDeleteMembers(ServiceGroupClient client) {
        int index = 0;

        // create a one service group with 2 services
        DtoServiceGroupUpdate update = create(SG_NAMES[index], SG_DESC[index], SG_APP_TYPES[index], SG_AGENTS[index], SG_SERVICES[index], SG_HOSTS[index]);
        DtoServiceGroupUpdateList updates = new DtoServiceGroupUpdateList();
        updates.add(update);
        DtoOperationResults results = client.post(updates);
        assert results.getSuccessful() == 1;

        DtoServiceGroup group = client.lookup(SG_NAMES[index]);
        assert group != null;
        assert group.getServices().size() == 2;

        // test add members
        DtoServiceGroupMemberUpdate memberUpdate = new DtoServiceGroupMemberUpdate();
        memberUpdate.setName(SG_NAMES[index]);
        memberUpdate.addService(new DtoServiceKey("local_load", "localhost"));
        memberUpdate.addService(new DtoServiceKey("local_users", "localhost"));
        memberUpdate.addService(new DtoServiceKey("tcp_http", "localhost"));
        client.addMembers(memberUpdate);

        group = client.lookup(SG_NAMES[index]);
        assert group != null;
        assert group.getServices().size() == 5;
        assert hasService(group.getServices(),"local_load", "localhost" );
        assert hasService(group.getServices(),"local_users", "localhost" );
        assert hasService(group.getServices(),"tcp_http", "localhost" );
        assert hasService(group.getServices(),"local_cpu_java", "localhost" );
        assert hasService(group.getServices(),"local_cpu_perl", "localhost" );

        // test delete members
        memberUpdate.getServices().clear();
        memberUpdate.addService(new DtoServiceKey("local_load", "localhost"));
        memberUpdate.addService(new DtoServiceKey("local_cpu_java", "localhost"));
        memberUpdate.addService(new DtoServiceKey("local_users", "localhost"));
        client.deleteMembers(memberUpdate);

        group = client.lookup(SG_NAMES[index]);
        assert group != null;
        assert group.getServices().size() == 2;
        assert hasService(group.getServices(),"tcp_http", "localhost" );
        assert hasService(group.getServices(),"local_cpu_perl", "localhost" );

        // test warning for missing member delete
        memberUpdate.getServices().clear();
        memberUpdate.addService(new DtoServiceKey("local_load", "localhost"));
        DtoOperationResults deleteResults = client.deleteMembers(memberUpdate);
        assert deleteResults != null;
        assert deleteResults.getWarning() == 1;

        // cleanup
        client.delete(SG_NAMES[0]);
        // verify cleanup
        assert client.lookup(SG_NAMES[0]) == null;
    }

    public boolean hasService(List<DtoService> update, String service, String host) {
        for (DtoService key : update) {
            if (key.getDescription().equals(service) && key.getHostName().equals(host)) {
                return true;
            }
        }
        return false;
    }

    public DtoServiceGroupUpdate create(String name, String desc, String appType, String agentId, String[] services, String[] hosts) {
        DtoServiceGroupUpdate update = new DtoServiceGroupUpdate();
        update.setName(name);
        update.setDescription(desc);
        update.setAppType(appType);
        update.setAgentId(agentId);
        int index = 0;
        for (String service : services) {
            update.addService(new DtoServiceKey(service, hosts[index]));
            index++;
        }
        return update;
    }

    public void assertServices(DtoServiceGroup group) {
        for (DtoService service : group.getServices()) {
            if (service.getDescription().equals("local_cpu_java")) {
                assert service.getHostName().equals("localhost");
            }
            else if (service.getDescription().equals("local_cpu_perl")) {
                assert service.getHostName().equals("localhost");
            }
            else {
                fail("service name not expected: " + service.getDescription());
            }
        }
    }

    // @Test
    public void quickTest() {
        // test query by appType
        if (serverDown) return;
        ServiceGroupClient client = new ServiceGroupClient(getDeploymentURL());

        List<DtoServiceGroup> nagios = client.list("NAGIOS", null);
        assert nagios != null;

    }

    @Test
    public void testServiceDeleteFromServiceGroup() throws Exception {
        if (serverDown) return;
        // get clients
        ServiceClient serviceClient = new ServiceClient(deploymentUrl);
        ServiceGroupClient client = new ServiceGroupClient(getDeploymentURL());

        // create test service
        DtoServiceList services = new DtoServiceList();
        DtoService service = new DtoService();
        service.setHostName("localhost");
        service.setDescription("test-delete-from-service-group");
        service.setDeviceIdentification("127.0.0.1");
        service.setMonitorStatus("PENDING");
        service.setLastHardState("PENDING");
        service.setLastPlugInOutput("testing delete from service group");
        service.setAppType("SEL");
        service.setAgentId(AGENT_84);
        services.add(service);
        DtoOperationResults results = serviceClient.post(services);
        assert results.getSuccessful() == 1;

        // create test service group with service member
        DtoServiceGroupUpdateList serviceGroups = new DtoServiceGroupUpdateList();
        DtoServiceGroupUpdate serviceGroup = new DtoServiceGroupUpdate();
        serviceGroup.setName("test-delete-from-service-group");
        serviceGroup.addService(new DtoServiceKey("test-delete-from-service-group", "localhost"));
        serviceGroups.add(serviceGroup);
        results = client.post(serviceGroups);
        assert results.getSuccessful() == 1;
        DtoServiceGroup verifyServiceGroup = client.lookup("test-delete-from-service-group");
        assert verifyServiceGroup != null;
        assert (verifyServiceGroup.getServices() != null) && !verifyServiceGroup.getServices().isEmpty();

        // delete service
        results = serviceClient.delete("test-delete-from-service-group", "localhost");
        assert results.getSuccessful() == 1;
        DtoService verifyService = serviceClient.lookup("test-delete-from-service-group", "localhost");
        assert verifyService == null;

        // verify service deleted from service group
        verifyServiceGroup = client.lookup("test-delete-from-service-group");
        assert verifyServiceGroup != null;
        assert (verifyServiceGroup.getServices() == null) || verifyServiceGroup.getServices().isEmpty();

        // cleanup service group
        client.delete("test-delete-from-service-group");
        verifyServiceGroup = client.lookup("test-delete-from-service-group");
        assert verifyServiceGroup == null;
    }
}
