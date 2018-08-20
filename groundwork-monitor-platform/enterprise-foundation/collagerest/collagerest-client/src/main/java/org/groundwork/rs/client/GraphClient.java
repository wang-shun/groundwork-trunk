package org.groundwork.rs.client;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.rs.dto.DtoGraph;
import org.groundwork.rs.dto.DtoGraphList;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.util.GenericType;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * The Java REST Client for generating RRD graphs based on a set of input parameters. One or more graphs may be 
 * returned in the byte array of each {@link DtoGraph} in the {@link DtoGraphList} collection.
 * 
 * Operations supported:
 * <ul>
 *     <li>generateGraphs - generate one or more RRD graphs based on the set of input parameters
 * </ul>
 */
public class GraphClient extends BaseRestClient {

    protected static Log log = LogFactory.getLog(GraphClient.class);
    private static final String API_ROOT_SINGLE = "/graphs";
    private static final String API_ROOT = API_ROOT_SINGLE + "/";

    /**
     * Create a Graph REST Client for performing graph generation operations
     * from the Groundwork enterprise foundation database.
     * Deployment URL example:
     * <pre>http://server-name[:port]/foundation-webapp/api</pre>
     *
     * @param deploymentUrl the deployment root URL for all Rest operations
     */
    public GraphClient(String deploymentUrl) {
        super(deploymentUrl);
    }

    /**
     * Create a Graph REST Client for performing graph generation operations
     * from the Groundwork enterprise foundation database.
     * Deployment URL example:
     * <pre>http://server-name[:port]/foundation-webapp/api</pre>
     * <p>Supported Media Types</p>
     * <ul>application/xml {@link javax.ws.rs.core.MediaType#APPLICATION_XML_TYPE}</ul>
     * <ul>application/json {@link javax.ws.rs.core.MediaType#APPLICATION_JSON_TYPE}</ul>
     * <p></p>
     * @param deploymentUrl the deployment root URL for all Rest operations
     * @param mediaType a valid, supported internet media type (MIME) supported
     */
    public GraphClient(String deploymentUrl, MediaType mediaType) {
        this(deploymentUrl);
        this.mediaType = mediaType;
    }

    /**
     * Generate one or more RRD graphs based on the set of input parameters determining which hosts and service
     * types to create graphs for. If service parameter is not provided, defaults to generating a graph for each
     * service type for the given host. The host name is required. If application type is not provided, defaults to NAGIOS.
     *
     * Start and end date are represented in seconds since start of epoch. Both start and end dates are optional. If
     * end date is not provided, defaults to now. If start date is not provided, defaults to 24 hours ago.
     *
     * {@link GraphParameterBuilder#setStartDateInterval(Long)} is a convenience method for setting the
     *      number of seconds prior to 'now' for the start date
     *
     * @param parameterBuilder the required and optional parameters. Host name is required, all other parameters are optional.
     *                         If serviceName is not provided, a graph for each service for the given host name is generated
     * @return a list of one or more {@link org.groundwork.rs.dto.DtoGraph} objects matching the query.
     *         If no records match the query, then an empty list is returned
     * @throws CollageRestException
     */
    public List<DtoGraph> generateGraphs(GraphParameterBuilder parameterBuilder) throws CollageRestException {
        Response.Status status = Response.Status.SERVICE_UNAVAILABLE;
        ClientResponse<DtoGraphList> response = null;
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                String url = buildGraphQuery(API_ROOT, parameterBuilder);
                ClientRequest request = createClientRequest(url);
                request.accept(mediaType);
                response = request.get(new GenericType<DtoGraphList>(){});
                if (response.getResponseStatus() == Response.Status.OK) {
                    DtoGraphList graphs = response.getEntity(new GenericType<DtoGraphList>(){});
                    return graphs.getGraphs();
                }
                else if (response.getResponseStatus() == Response.Status.NOT_FOUND) {
                    // return an empty list for not found exception
                    return new ArrayList<DtoGraph>();
                }
                else if (response.getResponseStatus() == Response.Status.UNAUTHORIZED && retry < 1) {
                    log.info(RETRY_AUTH);
                    status = response.getResponseStatus();
                    response.releaseConnection();
                    tokenSessionManager.removeToken(deploymentUrl);
                    continue;
                }
                status = response.getResponseStatus();
                break;
            }
        }
        catch (Exception e) {
            throw new CollageRestException(e);
        }
        finally {
            if (response != null)
                response.releaseConnection();
        }
        if (status == null)
            status = Response.Status.SERVICE_UNAVAILABLE;
        throw new CollageRestException(String.format("Exception executing query graphs (%s) with status code of %d, reason: %s",
                parameterBuilder.toString(), status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
        
    }

    protected String buildGraphQuery(String apiRoot, GraphParameterBuilder builder)
            throws UnsupportedEncodingException {
        StringBuilder url = new StringBuilder();
        url.append(joinApiPath(deploymentUrl, apiRoot));
        String paramDelim = "?";
        if (builder.getApplicationType() != null) {
            url.append(paramDelim);
            url.append("applicationType=");
            url.append(encode(builder.getApplicationType()));
            paramDelim = "&";
        }
        if (builder.getHostName() != null) {
            url.append(paramDelim);
            url.append("hostName=");
            url.append(encode(builder.getHostName()));
            paramDelim = "&";
        }
        if (builder.getServiceName() != null) {
            url.append(paramDelim);
            url.append("serviceName=");
            url.append(encode(builder.getServiceName()));
            paramDelim = "&";
        }
        if (builder.getStartDate() != null) {
            url.append(paramDelim);
            url.append("startDate=");
            url.append(builder.getStartDate());
            paramDelim = "&";
        }
        if (builder.getEndDate() != null) {
            url.append(paramDelim);
            url.append("endDate=");
            url.append(builder.getEndDate());
            paramDelim = "&";
        }
        if (builder.getGraphWidth() != null) {
            url.append(paramDelim);
            url.append("graphWidth=");
            url.append(builder.getGraphWidth());
        }
        return url.toString();
    }

}
