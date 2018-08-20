package org.groundwork.rs.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.rs.dto.DtoComment;
import org.groundwork.rs.dto.DtoOperationResults;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class CommentClient extends BaseRestClient {

    protected static Log log = LogFactory.getLog(CommentClient.class);
    private static final String API_ROOT_SINGLE = "/comments";
    private static final String API_ROOT = API_ROOT_SINGLE + "/";

    public CommentClient(String deploymentUrl) {
        super(deploymentUrl);
    }

    public CommentClient(String deploymentUrl, MediaType mediaType) {
        this(deploymentUrl);
        this.mediaType = mediaType;
    }

    public DtoOperationResults addHostComment(Integer hostId, String notes, String author) throws CollageRestException {
        return addComment("host", hostId, notes, author);
    }

    public DtoOperationResults addServiceComment(Integer serviceId, String notes, String author) throws CollageRestException {
        return addComment("service", serviceId, notes, author);
    }

    private DtoOperationResults addComment(String entityType, Integer entityId, String notes, String author) throws CollageRestException {
        Response.Status status = Response.Status.SERVICE_UNAVAILABLE;
        ClientResponse<DtoOperationResults> response = null;
        DtoComment comment = new DtoComment();
        comment.setNotes(notes);
        comment.setAuthor(author);
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                ClientRequest request = createClientRequest(build(API_ROOT + entityType + "/" + entityId));
                request.accept(mediaType);
                request.body(mediaType, comment);
                response = request.post();
                if (response.getResponseStatus() == Response.Status.OK) {
                    return response.getEntity(DtoOperationResults.class);
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
        throw new CollageRestException(String.format("Exception adding comment with status code of %d, reason: %s",
                status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }

    public DtoOperationResults deleteHostComment(Integer hostId, Integer commentId) throws CollageRestException {
       return delete("host", hostId, commentId);
    }

    public DtoOperationResults deleteServiceComment(Integer serviceId, Integer commentId) throws CollageRestException {
        return delete("service", serviceId, commentId);
    }

    private DtoOperationResults delete(String entityType, Integer entityId, Integer commentId) throws CollageRestException {
        Response.Status status = Response.Status.SERVICE_UNAVAILABLE;
        ClientResponse response = null;
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                ClientRequest request = createClientRequest(build(API_ROOT + entityType + "/" + entityId + "/" + commentId));
                request.accept(mediaType);
                response = request.delete();
                if (response.getResponseStatus() == Response.Status.OK) {
                    return (DtoOperationResults) response.getEntity(DtoOperationResults.class);
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
            if (response != null) response.releaseConnection();
        }
        if (status == null) status = Response.Status.SERVICE_UNAVAILABLE;
        throw new CollageRestException(String.format("Exception executing delete to comments with status code of %d, reason: %s",
                status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }
}
