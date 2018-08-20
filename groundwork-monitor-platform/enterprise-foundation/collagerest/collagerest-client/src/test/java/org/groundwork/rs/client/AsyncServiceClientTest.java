package org.groundwork.rs.client;

import org.groundwork.rs.dto.DtoOperationResult;
import org.groundwork.rs.dto.DtoOperationResults;
import org.groundwork.rs.dto.DtoService;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class AsyncServiceClientTest extends AbstractServiceTest {


    @Test
    public void testPostAndDeleteServicesAsync() throws Exception {
        if (serverDown) return;
        ServiceClient client = new ServiceClient(getDeploymentURL());
        DtoOperationResults results = client.postAsync(buildServiceUpdate());
        assertEquals(1, results.getCount().intValue());
        String hostName = null;
        for (DtoOperationResult result : results.getResults()) {
            assert(result.getStatus().equals(DtoOperationResult.SUCCESS));
        }

        Thread.sleep(3000);

        DtoService service = client.lookup("service-100", "localhost");
        assertNotNull(service);
        assertServiceWritten(service);

        service = client.lookup("service-101", "localhost");
        assertNotNull(service);
        assertServiceWritten(service);

        List<String> serviceNames = new LinkedList<String>();
        serviceNames.add("service-100");
        serviceNames.add("service-101");
        client.delete(serviceNames, "localhost");

        service = client.lookup("service-100", "localhost");
        assertNull(service);
        service = client.lookup("service-101", "localhost");
        assertNull(service);
    }

}
