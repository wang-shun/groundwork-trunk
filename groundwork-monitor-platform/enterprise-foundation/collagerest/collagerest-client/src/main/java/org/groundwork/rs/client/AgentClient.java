package org.groundwork.rs.client;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.rs.dto.DtoOperationResults;
import org.jboss.resteasy.util.GenericType;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;

public class AgentClient  extends BaseRestClient {

    protected static Log log = LogFactory.getLog(AgentClient.class);
    private static final String API_ROOT_SINGLE = "/agents";
    private static final String API_ROOT = API_ROOT_SINGLE + "/";

    public AgentClient(String deploymentUrl) {
        super(deploymentUrl);
    }

    public AgentClient(String deploymentUrl, MediaType mediaType) {
        this(deploymentUrl);
        this.mediaType = mediaType;
    }

    public DtoOperationResults delete(String agentId) throws CollageRestException {
        String requestUrl = buildUrlWithPath(API_ROOT, agentId);
        String requestDescription = "delete by agent " + agentId;
        DtoOperationResults results = clientRequest(HttpMethod.DELETE, requestUrl, requestDescription, new GenericType<DtoOperationResults>(){});
        return (results != null) ? results : new DtoOperationResults();
    }

}
