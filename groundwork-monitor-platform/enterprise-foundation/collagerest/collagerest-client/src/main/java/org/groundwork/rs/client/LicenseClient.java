package org.groundwork.rs.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.rs.dto.DtoLicenseCheck;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.util.GenericType;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * LicenseClient
 *
 * The Java REST Client for performing license related checks and queries
 * on tje Groundwork enterprise foundation server.
 * <p>
 * Operations supported are of the categories:
 * <ul>
 *     <li>check operations - check for availability of devices given an allocation amount</li>
 * </ul>
 * <p>
 *
 * @author <a href="mailto:david@bluesunrise.com">David S Taylor</a>
 * @version $Id:$
 */
public class LicenseClient extends BaseRestClient {

    private static Log log = LogFactory.getLog(LicenseClient.class);

    private static final String API_ROOT_SINGLE = "/license";
    private static final String API_ROOT = API_ROOT_SINGLE + "/";

    /**
     * Create a License REST Client for performing query and device check operations
     * in the Groundwork enterprise foundation server.
     * <p></p>
     * Deployment URL example:
     * <pre>http://server-name[:port]/foundation-webapp/api</pre>
     * <p></p>
     * @param deploymentUrl the deployment root URL for all Rest operations
     */
    public LicenseClient(String deploymentUrl) {
        super(deploymentUrl);
    }

    /**
     * Create a License REST Client for performing query and device check operations
     * in the Groundwork enterprise foundation server.
     * <p></p>
     * Deployment URL example:
     * <pre>http://server-name[:port]/foundation-webapp/api</pre>
     * <p>Supported Media Types</p>
     * <ul>application/xml {@link javax.ws.rs.core.MediaType#APPLICATION_XML_TYPE}</ul>
     * <ul>application/json {@link javax.ws.rs.core.MediaType#APPLICATION_JSON_TYPE}</ul>
     * <p></p>
     * @param deploymentUrl the deployment root URL for all Rest operations
     * @param mediaType a valid, supported internet media type (MIME) supported
     */
    public LicenseClient(String deploymentUrl, MediaType mediaType) {
        this(deploymentUrl);
        this.mediaType = mediaType;
    }

    /**
     * Check for availability of device allocations against the server's license quotas.
     * Pass in the number of devices you want to allocate, and the service will return
     * a true if you can allocate that many, and false if you cannot not.
     * The DtoLicenseCheck returns a success field set to true or false.
     * Additionally, a message is returned with an detailed information about the result
     * of the license check.
     *
     * @param devicesToAllocate the number of devices you would like to allocate
     * @return the returned {@link DtoLicenseCheck} transfer object
     * @throws CollageRestException
     */
    public DtoLicenseCheck check(int devicesToAllocate) throws CollageRestException {
        Response.Status status = Response.Status.SERVICE_UNAVAILABLE;
        ClientResponse<DtoLicenseCheck> response = null;
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                String encodedQueryParams = buildEncodedQueryParams(new String[]{"allocate"}, new String[]{Integer.toString(devicesToAllocate)});
                String url = buildUrlWithQueryParams(API_ROOT + "check", encodedQueryParams);
                ClientRequest request = createClientRequest(url);
                request.accept(mediaType);
                response = request.get(new GenericType<DtoLicenseCheck>(){});
                if (response.getResponseStatus() == Response.Status.OK) {
                    return response.getEntity(new GenericType<DtoLicenseCheck>() {});
                } else if (response.getResponseStatus() == Response.Status.NOT_FOUND) {
                    return null;
                } else if (response.getResponseStatus() == Response.Status.UNAUTHORIZED && retry < 1) {
                    log.info(RETRY_AUTH);
                    status = response.getResponseStatus();
                    response.releaseConnection();
                    tokenSessionManager.removeToken(deploymentUrl);
                    continue;
                }
                status = response.getResponseStatus();
                break;
            }
        } catch (Exception e) {
            throw new CollageRestException(e);
        } finally {
            if (response != null) {
                response.releaseConnection();
            }
        }
        if (status == null) {
            status = Response.Status.SERVICE_UNAVAILABLE;
        }
        throw new CollageRestException(String.format("Exception executing check license (%d) with status code of %d, reason: %s", devicesToAllocate, status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }

}
