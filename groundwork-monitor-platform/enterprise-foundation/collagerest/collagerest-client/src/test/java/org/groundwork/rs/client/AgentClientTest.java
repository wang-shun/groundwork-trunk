package org.groundwork.rs.client;

import org.groundwork.rs.dto.DtoHost;
import org.groundwork.rs.dto.DtoHostList;
import org.groundwork.rs.dto.DtoOperationResult;
import org.groundwork.rs.dto.DtoOperationResults;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class AgentClientTest extends AbstractHostTest {

    @Test
    public void testDeleteByAgent() throws Exception {
        if (serverDown) return;

        DtoHostList hostUpdates = buildHostUpdate("-");
        DtoOperationResults results = executePost(hostUpdates);
        assertEquals(2, results.getCount().intValue());
        String hostName = null;
        for (DtoOperationResult result : results.getResults()) {
            assert(result.getStatus().equals(DtoOperationResult.SUCCESS));
        }

        // test lookup by agent id
        DtoHost host = retrieveHostByAgent(AGENT_84);
        assertEquals("host-100", host.getHostName());
        host = retrieveHostByAgent(AGENT_85);
        assertEquals("host-101", host.getHostName());

        AgentClient client = new AgentClient(getDeploymentURL());
        results = client.delete(AGENT_84);
        for (DtoOperationResult result : results.getResults()) {
            assertEquals(DtoOperationResult.SUCCESS, result.getStatus());
        }
        host = retrieveHostByAgent(AGENT_84);
        assertNull(host);

        results = client.delete(AGENT_85);
        for (DtoOperationResult result : results.getResults()) {
            assertEquals(DtoOperationResult.SUCCESS, result.getStatus());
        }
        host = retrieveHostByAgent(AGENT_85);
        assertNull(host);

    }

}
