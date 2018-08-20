package org.groundwork.rs.client;

import org.groundwork.rs.dto.DtoApplicationType;
import org.groundwork.rs.dto.DtoApplicationTypeList;
import org.groundwork.rs.dto.DtoDepthType;
import org.groundwork.rs.dto.DtoEntityProperty;
import org.groundwork.rs.dto.DtoOperationResult;
import org.groundwork.rs.dto.DtoOperationResults;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ApplicationTypeClientTest extends AbstractClientTest {

    @Test
    public void testLookupApplicationType() {
        if (serverDown) return;
        ApplicationTypeClient client = new ApplicationTypeClient(getDeploymentURL());
        DtoApplicationType applicationType = client.lookup("NAGIOS", DtoDepthType.Deep);
        assertNotNull(applicationType);
        assertEquals("NAGIOS", applicationType.getName());
        assert applicationType.getProperties().size() > 10;
        assert applicationType.getEntityProperties().size() > 10;
        assert applicationType.getEntityTypes().size() > 1;
    }

    @Test
    public void testList() throws Exception {
        if (serverDown) return;
        ApplicationTypeClient client = new ApplicationTypeClient(getDeploymentURL());
        List<DtoApplicationType> types = client.list();
        assertNotNull(types);
        assert types.size() >= 10;
        for (DtoApplicationType type : types) {
            assertNotNull(type.getName());
        }
    }

    @Test
    public void testQuery() throws Exception {
        if (serverDown) return;
        ApplicationTypeClient client = new ApplicationTypeClient(getDeploymentURL());
        List<DtoApplicationType> types = client.query("name in ('SYSTEM','SNMPTRAP','SYSLOG')");
        assert types.size() == 3;
        for (DtoApplicationType type : types) {
            assert type.getName().startsWith("S");
        }
    }

    @Test
    public void testCreateAndDeleteApplicationTypes() throws Exception {
        if (serverDown) return;
        DtoApplicationTypeList updates = buildApplicationTypeUpdate();
        ApplicationTypeClient client = new ApplicationTypeClient(getDeploymentURL());

        DtoOperationResults results = client.post(updates);
        assert 2 == results.getCount();
        for (DtoOperationResult result : results.getResults()) {
            assert(result.getStatus().equals(DtoOperationResult.SUCCESS));
        }
        DtoApplicationType type = retrieveApplicationType("NewApplicationType", DtoDepthType.Deep);
        assertNotNull(type);
        assert type.getName().equals("NewApplicationType");
        assert type.getDescription().equals("This is my new application type");
        assert type.getEntityProperties().size() == 2;

        type = retrieveApplicationType("NewerApplicationType", DtoDepthType.Deep);
        assertNotNull(type);
        assert type.getName().equals("NewerApplicationType");
        assert type.getDescription().equals("This is my newer application type");
        assert type.getEntityProperties().size() == 3;

        List<String> names = new ArrayList<>();
        names.add("NewApplicationType");
        names.add("NewerApplicationType");
        client.delete(names);

        type = retrieveApplicationType("NewApplicationType");
        assert type == null;
        type = retrieveApplicationType("NewerApplicationType");
        assert type == null;

        // test warning for missing delete
        DtoOperationResults deleteResults = client.delete(Arrays.asList(new String[]{"NotAnApplicationType"}));
        assert deleteResults != null;
        assert deleteResults.getWarning() == 1;
    }

    @Test
    public void deleteTestData() throws Exception {
        if (serverDown) return;
        ApplicationTypeClient client = new ApplicationTypeClient(getDeploymentURL());
        List<String> names = new ArrayList<>();
        names.add("DAVE");
        names.add("NEWONE");
        client.delete(names);
    }

    private DtoApplicationType retrieveApplicationType(String name) throws Exception {
        return retrieveApplicationType(name, DtoDepthType.Shallow);
    }

    private DtoApplicationType retrieveApplicationType(String name, DtoDepthType depthType) throws Exception {
        ApplicationTypeClient client = new ApplicationTypeClient(getDeploymentURL());
        return client.lookup(name, depthType);
    }

    private DtoApplicationTypeList buildApplicationTypeUpdate() throws Exception {
        DtoApplicationTypeList appTypes = new DtoApplicationTypeList();
        DtoApplicationType type = new DtoApplicationType();
        type.setName("NewApplicationType");
        type.setDescription("This is my new application type");
        type.addEntityProperty(new DtoEntityProperty("ContactPerson", "HOST_STATUS"));
        type.addEntityProperty(new DtoEntityProperty("PerformanceData", "LOG_MESSAGE"));
        appTypes.add(type);
        type = new DtoApplicationType();
        type.setName("NewerApplicationType");
        type.setDescription("This is my newer application type");
        type.addEntityProperty(new DtoEntityProperty("LastPluginOutput", "LOG_MESSAGE"));
        type.addEntityProperty(new DtoEntityProperty("isAcknowledged", "HOST_STATUS"));
        type.addEntityProperty(new DtoEntityProperty("ContactPerson", "HOST_STATUS"));
        appTypes.add(type);
        return appTypes;
    }


}
