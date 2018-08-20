package org.groundwork.rs.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.rs.dto.DtoHostNotificationList;
import org.groundwork.rs.dto.DtoOperationResults;
import org.groundwork.rs.dto.DtoServiceNotificationList;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * The Java REST Client for Notifications in the Groundwork enterprise foundation server.
 * <p>
 * Operations supported are of the categories:
 * <ul>
 *     <li>host post operations - notify NOMA of a set of host notifications</li>
 *     <li>service post operations - notify NOMA of a set of service notifications</li>
 * </ul>
 * <p>
 * The results for all post and delete operations return
 * the same {@link org.groundwork.rs.dto.DtoOperationResults} list of
 * {@link org.groundwork.rs.dto.DtoOperationResult}, holding
 * the result (success, failure, warning) of each sub-operation.
 */
public class NotificationClient extends BaseRestClient {

    protected static Log log = LogFactory.getLog(NotificationClient.class);
    private static final String API_ROOT_SINGLE = "/notifications";
    private static final String API_ROOT = API_ROOT_SINGLE + "/";

    /**
     * Create a Notification REST Client for performing notifications operations
     *  in the Groundwork enterprise foundation database.
     * Deployment URL example:
     * <pre>http://server-name[:port]/foundation-webapp/api</pre>
     *
     * @param deploymentUrl the deployment root URL for all Rest operations
     */
    public NotificationClient(String deploymentUrl) {
        super(deploymentUrl);
    }

    /**
     * Create a Notification REST Client for performing notifications operations
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
    public NotificationClient(String deploymentUrl, MediaType mediaType) {
        this(deploymentUrl);
        this.mediaType = mediaType;
    }


    /**
     * Notify Hosts of one or more notifications. {@link org.groundwork.rs.dto.DtoHostNotificationList} is a list of one or
     * more {@link org.groundwork.rs.dto.DtoHostNotification} objects. Each of these objects represents a single notification.
     * Any field that needs to be updated or added should be set on the DtoNotification. Unset fields are ignored.
     * <p>
     * The post operation is not transactional. If a list of ten notifications are passed in to be added, and
     * if, for example, two notifications fail to update, the other eight notifications will still be sent. The results for all
     * notification operations return a {@link org.groundwork.rs.dto.DtoOperationResults} list of {@link org.groundwork.rs.dto.DtoOperationResult}, holding
     * the status (success, failure, warning) of each sub-operation, see {@link org.groundwork.rs.dto.DtoOperationResult#getStatus()}.
     * </p>
     * @param notifications  a list of one or more {@link org.groundwork.rs.dto.DtoHostNotification} objects.
     * @return a {@link org.groundwork.rs.dto.DtoOperationResults} set of {@link org.groundwork.rs.dto.DtoOperationResult}, holding the status of each notification
     * @throws CollageRestException
     */
    public DtoOperationResults notifyHosts(DtoHostNotificationList notifications) throws CollageRestException {
        ClientResponse<DtoOperationResults> response = null;
        Response.Status status = Response.Status.SERVICE_UNAVAILABLE;
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                ClientRequest request = createClientRequest(buildUrlWithPath(API_ROOT_SINGLE, "/hosts"));
                request.accept(mediaType);
                request.body(mediaType, notifications);
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
        throw new CollageRestException(String.format("Exception executing notification with status code of %d, reason: %s",
                status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }

    /**
     * Notify Services of one or more notifications. {@link org.groundwork.rs.dto.DtoServiceNotificationList} is a list of one or
     * more {@link org.groundwork.rs.dto.DtoServiceNotification} objects. Each of these objects represents a single notification.
     * Any field that needs to be updated or added should be set on the DtoNotification. Unset fields are ignored.
     * <p>
     * The post operation is not transactional. If a list of ten notifications are passed in to be added, and
     * if, for example, two notifications fail to update, the other eight notifications will still be sent. The results for all
     * notification operations return a {@link org.groundwork.rs.dto.DtoOperationResults} list of {@link org.groundwork.rs.dto.DtoOperationResult}, holding
     * the status (success, failure, warning) of each sub-operation, see {@link org.groundwork.rs.dto.DtoOperationResult#getStatus()}.
     * </p>
     * @param notifications  a list of one or more {@link org.groundwork.rs.dto.DtoServiceNotification} objects.
     * @return a {@link org.groundwork.rs.dto.DtoOperationResults} set of {@link org.groundwork.rs.dto.DtoOperationResult}, holding the status of each notification
     * @throws CollageRestException
     */
    public DtoOperationResults notifyServices(DtoServiceNotificationList notifications) throws CollageRestException {
        ClientResponse<DtoOperationResults> response = null;
        Response.Status status = Response.Status.SERVICE_UNAVAILABLE;
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                ClientRequest request = createClientRequest(buildUrlWithPath(API_ROOT_SINGLE, "/services"));
                request.accept(mediaType);
                request.body(mediaType, notifications);
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
        throw new CollageRestException(String.format("Exception executing notification with status code of %d, reason: %s",
                status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }

}
