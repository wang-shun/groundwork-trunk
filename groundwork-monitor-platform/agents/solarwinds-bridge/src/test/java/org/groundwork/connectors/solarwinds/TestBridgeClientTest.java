package org.groundwork.connectors.solarwinds;

import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.junit.Test;

import javax.ws.rs.core.MediaType;

public class TestBridgeClientTest extends AbstractBridgeClientTest {

    @Test
    public void testPostText() throws Exception {
        ClientRequest request = new ClientRequest(getApiUrl() + "/hosts/text");
        request.body(MediaType.TEXT_PLAIN_TYPE, "Hello There!");
        ClientResponse response = request.post();
        assert response.getResponseStatus().getStatusCode() == 200;
        assert response.getEntity(String.class).toString().startsWith("OK");
    }

}
