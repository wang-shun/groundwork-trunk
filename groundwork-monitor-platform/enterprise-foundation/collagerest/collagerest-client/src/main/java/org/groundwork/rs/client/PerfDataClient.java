package org.groundwork.rs.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.rs.dto.DtoPerfDataList;
import org.groundwork.rs.dto.DtoOperationResults;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * The Java REST Client for PerfData in the Groundwork enterprise foundation server.
 * <p>
 * Operations supported are of the categories:
 * <ul>
 *     <li>post operation - post a set of one or more PerfData records</li>
 * </ul>
 * <p>
 * The results for all post and delete operations return
 * the same {@link org.groundwork.rs.dto.DtoOperationResults} list of
 * {@link org.groundwork.rs.dto.DtoOperationResult}, holding
 * the result (success, failure, warning) of each sub-operation.
 */
public class PerfDataClient extends BaseRestClient {

    protected static Log log = LogFactory.getLog(PerfDataClient.class);
    private static final String API_ROOT_SINGLE = "/perfdata";
    private static final String API_ROOT = API_ROOT_SINGLE + "/";

    /**
     * Create a PerfData REST Client for performing PerfData operations
     *  in the Groundwork enterprise foundation database.
     * Deployment URL example:
     * <pre>http://server-name[:port]/foundation-webapp/api</pre>
     *
     * @param deploymentUrl the deployment root URL for all Rest operations
     */
    public PerfDataClient(String deploymentUrl) {
        super(deploymentUrl);
    }

    /**
     * Create a PerfData REST Client for performing PerfData operations
     *  in the Groundwork enterprise foundation database.
     * Deployment URL example:
     * <pre>http://server-name[:port]/foundation-webapp/api</pre>
     * <p>Supported Media Types</p>
     * <ul>application/xml {@link javax.ws.rs.core.MediaType#APPLICATION_XML_TYPE}</ul>
     * <ul>application/json {@link javax.ws.rs.core.MediaType#APPLICATION_JSON_TYPE}</ul>
     * <p></p>
     * @param deploymentUrl the deployment root URL for all Rest operations
     * @param mediaType a valid, supported internet media type (MIME) supported
     */
    public PerfDataClient(String deploymentUrl, MediaType mediaType) {
        this(deploymentUrl);
        this.mediaType = mediaType;
    }


    /**
     * Post one or more PerfData records. {@link org.groundwork.rs.dto.DtoPerfDataList} is a list of one or
     * more {@link org.groundwork.rs.dto.DtoPerfData} objects. Each of these objects represents a single PerfData record.
     * Any field that needs to be updated or added should be set on the DtoPerfData. Unset fields are ignored.
     * <p>
     * The post operation is not transactional. If a list of ten PerfData records are passed in to be added, and
     * if, for example, two records fail to update, the other eight records will still be sent. The results for all
     *  operations return a {@link org.groundwork.rs.dto.DtoOperationResults} list of {@link org.groundwork.rs.dto.DtoOperationResult}, holding
     * the status (success, failure, warning) of each sub-operation, see {@link org.groundwork.rs.dto.DtoOperationResult#getStatus()}.
     * </p>
     * @param perfDataList  a list of one or more {@link org.groundwork.rs.dto.DtoPerfData} objects.
     * @return a {@link org.groundwork.rs.dto.DtoOperationResults} set of {@link org.groundwork.rs.dto.DtoOperationResult},
     *         holding the status of each PerfData record
     * @throws org.groundwork.rs.client.CollageRestException
     */
    public DtoOperationResults post(DtoPerfDataList perfDataList) throws CollageRestException {
        ClientResponse<DtoOperationResults> response = null;
        Response.Status status = Response.Status.SERVICE_UNAVAILABLE;
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                ClientRequest request = createClientRequest(build(API_ROOT_SINGLE));
                request.accept(mediaType);
                request.body(mediaType, perfDataList);
                response = request.post();
                if (response.getResponseStatus() == Response.Status.OK) {
                    DtoOperationResults results = response.getEntity(DtoOperationResults.class);
                    return results;
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
        throw new CollageRestException(String.format("Exception executing PerfData post with status code of %d, reason: %s",
                status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }

}
