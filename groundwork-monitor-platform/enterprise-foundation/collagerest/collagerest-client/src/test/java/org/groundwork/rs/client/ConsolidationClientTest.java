package org.groundwork.rs.client;

import org.groundwork.rs.dto.DtoConsolidation;
import org.groundwork.rs.dto.DtoConsolidationList;
import org.groundwork.rs.dto.DtoOperationResult;
import org.groundwork.rs.dto.DtoOperationResults;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ConsolidationClientTest extends AbstractClientTest {

    @Test
    public void testLookupConsolidation() {
        if (serverDown) return;
        ConsolidationClient client = new ConsolidationClient(getDeploymentURL());
        DtoConsolidation consolidation = client.lookup("SYSTEM");
        assertNotNull(consolidation);
        assertEquals("SYSTEM", consolidation.getName());
        assertEquals("OperationStatus;Device;MonitorStatus;ApplicationType;TextMessage", consolidation.getCriteria());
    }

    @Test
    public void testList() throws Exception {
        if (serverDown) return;
        ConsolidationClient client = new ConsolidationClient(getDeploymentURL());
        List<DtoConsolidation> consolidations = client.list();
        assertNotNull(consolidations);
        assert consolidations.size() > 3;
        for (DtoConsolidation consolidation : consolidations) {
            assertNotNull(consolidation.getName());
            System.out.println(consolidation.toString());
        }
    }

    @Test
    public void testQuery() throws Exception {
        if (serverDown) return;
        ConsolidationClient client = new ConsolidationClient(getDeploymentURL());
        List<DtoConsolidation> consolidations = client.query("name like 'SY%'");
        assert consolidations.size() == 2;
        for (DtoConsolidation consolidation : consolidations) {
            assert consolidation.getName().startsWith("SY");
        }
        consolidations = client.query("criteria like '%OID%'");
        assert consolidations.size() > 0;
        for (DtoConsolidation consolidation : consolidations) {
            assert consolidation.getCriteria().contains("OID");
        }
    }

    @Test
    public void testCreateAndDeleteConsolidations() throws Exception {
        if (serverDown) return;
        DtoConsolidationList updates = buildConsolidationUpdate();
        ConsolidationClient client = new ConsolidationClient(getDeploymentURL());

        DtoOperationResults results = client.post(updates);
        assert 2 == results.getCount();
        for (DtoOperationResult result : results.getResults()) {
            assert(result.getStatus().equals(DtoOperationResult.SUCCESS));
        }
        DtoConsolidation consolidation = retrieveConsolidation("NewConsolidation");
        assertNotNull(consolidation);
        assert consolidation.getName().equals("NewConsolidation");
        assert consolidation.getCriteria().equals("This is my new consolidation");

        consolidation = retrieveConsolidation("NewerConsolidation");
        assertNotNull(consolidation);
        assert consolidation.getName().equals("NewerConsolidation");
        assert consolidation.getCriteria().equals("This is my newer consolidation");

        List<String> names = new ArrayList<>();
        names.add("NewConsolidation");
        names.add("NewerConsolidation");
        client.delete(names);

        consolidation = retrieveConsolidation("NewConsolidation");
        assert consolidation == null;
        consolidation = retrieveConsolidation("NewerConsolidation");
        assert consolidation == null;

        // test warning for missing delete
        DtoOperationResults deleteResults = client.delete(Arrays.asList(new String[]{"NotAConsolidation"}));
        assert deleteResults != null;
        assert deleteResults.getWarning() == 1;
    }

    private DtoConsolidation retrieveConsolidation(String name) throws Exception {
        ConsolidationClient client = new ConsolidationClient(getDeploymentURL());
        return client.lookup(name);
    }

    private DtoConsolidationList buildConsolidationUpdate() throws Exception {
        DtoConsolidationList consolidations = new DtoConsolidationList();
        DtoConsolidation consolidation = new DtoConsolidation();
        consolidation.setName("NewConsolidation");
        consolidation.setCriteria("This is my new consolidation");
        consolidations.add(consolidation);
        consolidation = new DtoConsolidation();
        consolidation.setName("NewerConsolidation");
        consolidation.setCriteria("This is my newer consolidation");
        consolidations.add(consolidation);
        return consolidations;
    }


}
