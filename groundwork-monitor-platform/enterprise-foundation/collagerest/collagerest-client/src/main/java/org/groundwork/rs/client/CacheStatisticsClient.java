package org.groundwork.rs.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.rs.dto.DtoCacheState;
import org.groundwork.rs.dto.DtoCacheStateList;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.util.GenericType;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

/**
 * The Java REST Client for reading and manipulating Hibernate caches
 * Operations supported:
 * <ul>
 *     <li>list - list all caches
 * </ul>
 */
public class CacheStatisticsClient extends BaseRestClient {

    protected static Log log = LogFactory.getLog(CacheStatisticsClient.class);
    private static final String API_ROOT_SINGLE = "/cache";
    private static final String API_ROOT = API_ROOT_SINGLE + "/";

    /**
     * Create a Cache Statistics REST Client
     *
     * Deployment URL example:
     * <pre>http://server-name[:port]/foundation-webapp/api</pre>
     *
     * @param deploymentUrl the deployment root URL for all Rest operations
     */
    public CacheStatisticsClient(String deploymentUrl) {
        super(deploymentUrl);
    }

    /**
     * Create a Cache Statistics REST Client
     *
     * Deployment URL example:
     * <pre>http://server-name[:port]/foundation-webapp/api</pre>
     * <p>Supported Media Types</p>
     * <ul>application/xml {@link javax.ws.rs.core.MediaType#APPLICATION_XML_TYPE}</ul>
     * <ul>application/json {@link javax.ws.rs.core.MediaType#APPLICATION_JSON_TYPE}</ul>
     * <p></p>
     * @param deploymentUrl the deployment root URL for all Rest operations
     * @param mediaType a valid, supported internet media type (MIME) supported
     */
    public CacheStatisticsClient(String deploymentUrl, MediaType mediaType) {
        this(deploymentUrl);
        this.mediaType = mediaType;
    }

    public List<DtoCacheState> list() throws CollageRestException {
        ClientResponse<DtoCacheState> response = null;
        Response.Status status = Response.Status.SERVICE_UNAVAILABLE;
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                String url = build(API_ROOT_SINGLE);
                ClientRequest request = createClientRequest(url);
                request.accept(mediaType);
                response = request.get(new GenericType<DtoCacheStateList>(){});
                if (response.getResponseStatus() == Response.Status.OK) {
                    DtoCacheStateList cacheStateList = response.getEntity(new GenericType<DtoCacheStateList>(){});
                    return cacheStateList.getCacheStates();
                }
                else if (response.getResponseStatus() == Response.Status.NOT_FOUND) {
                    // return an empty list for not found exception
                    return new ArrayList<DtoCacheState>();
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
        throw new CollageRestException(String.format("Exception executing list cache stats with status code of %d, reason: %s",
                status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }

}
