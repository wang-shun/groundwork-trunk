package org.groundwork.rs.client;

import org.groundwork.rs.dto.DtoOperationResult;
import org.groundwork.rs.dto.DtoOperationResults;
import org.groundwork.rs.dto.DtoPropertyDataType;
import org.groundwork.rs.dto.DtoPropertyType;
import org.groundwork.rs.dto.DtoPropertyTypeList;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class PropertyTypeClientTest extends AbstractClientTest {

    @Test
    public void testLookupPropertyType() {
        if (serverDown) return;
        PropertyTypeClient client = new PropertyTypeClient(getDeploymentURL());
        DtoPropertyType propertyType = client.lookup("TimeCritical");
        assertNotNull(propertyType);
        assertEquals("TimeCritical", propertyType.getName());
        assertEquals("The amount of time that the entity has had a status of CRITICAL", propertyType.getDescription());
        assertEquals(DtoPropertyDataType.LONG, propertyType.getDataType());
    }

    @Test
    public void testList() throws Exception {
        if (serverDown) return;
        PropertyTypeClient client = new PropertyTypeClient(getDeploymentURL());
        List<DtoPropertyType> types = client.list();
        assertNotNull(types);
        assert types.size() > 20;
        for (DtoPropertyType type : types) {
            assertNotNull(type.getName());
            assertNotNull(type.getDataType());
            //System.out.println(type.toString());
        }
    }

    @Test
    public void testQuery() throws Exception {
        if (serverDown) return;
        PropertyTypeClient client = new PropertyTypeClient(getDeploymentURL());
        List<DtoPropertyType> types = client.query("name like 'RRD%'");
        assert types.size() == 3;
        for (DtoPropertyType type : types) {
            assert type.getName().startsWith("RRD");
        }
        types = client.query("isBoolean = true");
        assert types.size() > 2;
        for (DtoPropertyType type : types) {
            assert type.getDataType() == DtoPropertyDataType.BOOLEAN;
        }
    }

    @Test
    public void testCreateAndDeletePropertyTypes() throws Exception {
        if (serverDown) return;
        DtoPropertyTypeList updates = buildPropertyTypeUpdate();
        PropertyTypeClient client = new PropertyTypeClient(getDeploymentURL());

        DtoOperationResults results = client.post(updates);
        assert 2 == results.getCount();
        for (DtoOperationResult result : results.getResults()) {
            assert(result.getStatus().equals(DtoOperationResult.SUCCESS));
        }
        DtoPropertyType type = retrievePropertyType("NewProperty");
        assertNotNull(type);
        assert type.getName().equals("NewProperty");
        assert type.getDataType() == DtoPropertyDataType.STRING;
        assert type.getDescription().equals("This is my new property type");

        type = retrievePropertyType("NewerProperty");
        assertNotNull(type);
        assert type.getName().equals("NewerProperty");
        assert type.getDataType() == DtoPropertyDataType.INTEGER;
        assert type.getDescription().equals("This is my newer property type");

        List<String> names = new ArrayList<>();
        names.add("NewProperty");
        names.add("NewerProperty");
        client.delete(names);

        type = retrievePropertyType("NewProperty");
        assert type == null;
        type = retrievePropertyType("NewerProperty");
        assert type == null;

        // test warning for missing delete
        DtoOperationResults deleteResults = client.delete(Arrays.asList(new String[]{"NotAPropertyType"}));
        assert deleteResults != null;
        assert deleteResults.getWarning() == 1;
    }

    private DtoPropertyType retrievePropertyType(String name) throws Exception {
        PropertyTypeClient client = new PropertyTypeClient(getDeploymentURL());
        return client.lookup(name);
    }

    private DtoPropertyTypeList buildPropertyTypeUpdate() throws Exception {
        DtoPropertyTypeList properties = new DtoPropertyTypeList();
        DtoPropertyType type = new DtoPropertyType();
        type.setName("NewProperty");
        type.setDataType(DtoPropertyDataType.STRING);
        type.setDescription("This is my new property type");
        properties.add(type);
        type = new DtoPropertyType();
        type.setName("NewerProperty");
        type.setDataType(DtoPropertyDataType.INTEGER);
        type.setDescription("This is my newer property type");
        properties.add(type);
        return properties;
    }


}
