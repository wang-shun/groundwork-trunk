package org.groundwork.rs.client;

import org.groundwork.rs.dto.DtoGraph;
import org.junit.Test;

import java.util.List;

import static junit.framework.Assert.assertNotNull;

public class GraphClientTest extends AbstractClientTest {

    @Test
    public void testGraphs() throws Exception {
        if (serverDown) return;
        GraphClient client = new GraphClient(getDeploymentURL());
        GraphParameterBuilder builder = new GraphParameterBuilder()
                    .setHostName("localhost")
                    .setServiceName("http_alive")
                    .setStartDateInterval(60L * 60L * 168L); // for the last week
        List<DtoGraph> graphs = client.generateGraphs(builder);
        int count = 0;
        for (DtoGraph graph : graphs) {
            byte[] bytes = graph.getGraph();
            assertNotNull(bytes);
            count++;
        }
        assert count > 0;
    }

    @Test
    public void testRetrieveGraphs() throws Exception {
        if (serverDown) return;
        GraphClient client = new GraphClient(getDeploymentURL());
        GraphParameterBuilder builder = new GraphParameterBuilder()
                .setHostName("localhost")
                .setStartDateInterval(60L * 60L * 168L); // for the last week
        List<DtoGraph> graphs = client.generateGraphs(builder);
        int count = 0;
        for (DtoGraph graph : graphs) {
            byte[] bytes = graph.getGraph();
            assertNotNull(bytes);
            count++;
        }
        assert count > 0;
    }

    @Test
    public void testGraphExceptionCases() throws Exception {
        if (serverDown) return;
        GraphClient client = new GraphClient(getDeploymentURL());
        GraphParameterBuilder builder = new GraphParameterBuilder()
                .setHostName("notfound")
                .setStartDateInterval(60L * 60L * 168L); // for the last week
        List<DtoGraph> graphs = client.generateGraphs(builder);
        int count = 0;
        for (DtoGraph graph : graphs) {
            byte[] bytes = graph.getGraph();
            assertNotNull(bytes);
            count++;
        }
        assert count == 0;
    }


}
