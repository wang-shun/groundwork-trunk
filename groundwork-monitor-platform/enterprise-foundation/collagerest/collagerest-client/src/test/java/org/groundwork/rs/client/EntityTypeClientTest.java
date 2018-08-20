package org.groundwork.rs.client;

import org.groundwork.rs.dto.DtoEntityType;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class EntityTypeClientTest extends AbstractClientTest {

    @Test
    public void testLookupEntityType() {
        if (serverDown) return;
        EntityTypeClient client = new EntityTypeClient(getDeploymentURL());
        DtoEntityType entityType = client.lookup("DEVICE");
        assertNotNull(entityType);
        assertEquals("DEVICE", entityType.getName());
        assertEquals("com.groundwork.collage.model.impl.Device", entityType.getDescription());
        assert entityType.isApplicationTypeSupported() == false;
        assert entityType.isLogicalEntity() == false;

        entityType = client.lookup("SERVICE_GROUP");
        assertNotNull(entityType);
        assertEquals("SERVICE_GROUP", entityType.getName());
        assertEquals("com.groundwork.collage.model.impl.ServiceGroup", entityType.getDescription());
        assert entityType.isApplicationTypeSupported() == false;
        assert entityType.isLogicalEntity() == true;
    }

    @Test
    public void testList() throws Exception {
        if (serverDown) return;
        EntityTypeClient client = new EntityTypeClient(getDeploymentURL());
        List<DtoEntityType> types = client.list();
        assertNotNull(types);
        assert types.size() > 22;
        for (DtoEntityType type : types) {
            assertNotNull(type.getName());
            assertNotNull(type.getDescription());
        }
    }

    @Test
    public void testQuery() throws Exception {
        if (serverDown) return;
        EntityTypeClient client = new EntityTypeClient(getDeploymentURL());
        List<DtoEntityType> types = client.query("name like 'C%'");
        assert types.size() == 4;
        for (DtoEntityType type : types) {
            assert type.getName().startsWith("C");
        }
    }


}
