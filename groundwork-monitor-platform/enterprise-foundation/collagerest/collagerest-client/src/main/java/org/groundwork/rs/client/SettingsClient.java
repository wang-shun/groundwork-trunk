package org.groundwork.rs.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.rs.dto.DtoAsyncSettings;
import org.groundwork.rs.dto.DtoOperationResults;
import org.groundwork.rs.dto.DtoTokensList;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.util.GenericType;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

public class SettingsClient extends BaseRestClient {

    protected static Log log = LogFactory.getLog(SettingsClient.class);
    private static final String API_ROOT_SINGLE = "/settings";
    private static final String API_ASYNC = "/async";
    private static final String API_TOKENS = "/tokens";
    private static final String API_ROOT = API_ROOT_SINGLE + "/";

    private static final String EXCEPTION_EXECUTING_WITH_STATUS_AND_REASON = "Exception executing AsyncSettings with status code of %d, reason: %s";

    public SettingsClient(String deploymentUrl) {
        super(deploymentUrl);
    }

    public SettingsClient(String deploymentUrl, MediaType mediaType) {
        this(deploymentUrl);
        this.mediaType = mediaType;
    }

    public DtoAsyncSettings getAsyncSettings() {
        ClientResponse<DtoAsyncSettings> response = null;
        Response.Status status = Response.Status.SERVICE_UNAVAILABLE;
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                ClientRequest request = createClientRequest(build(API_ROOT_SINGLE + API_ASYNC));
                request.accept(mediaType);
                response = request.get(new GenericType<DtoAsyncSettings>(){});
                if (response.getResponseStatus() == Response.Status.OK) {
                    DtoAsyncSettings settings = response.getEntity(new GenericType<DtoAsyncSettings>() {});
                    return settings;
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
        throw new CollageRestException(String.format(EXCEPTION_EXECUTING_WITH_STATUS_AND_REASON,
                status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());

    }

    //    TODO: http://jira/browse/GWMON-12908
    //    A Rest API for listing authentication tokens. Authentication tokens can be depleted.
    //    This API is to be used internally in the future. It will be disabled until we add authorization to the Rest APIs.
    public DtoTokensList getTokens() {
        ClientResponse<Map<String,String>> response = null;
        Response.Status status = Response.Status.SERVICE_UNAVAILABLE;
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                ClientRequest request = createClientRequest(build(API_ROOT_SINGLE + API_TOKENS));
                request.accept(mediaType);
                response = request.get(new GenericType<Map<String,String>>(){});
                if (response.getResponseStatus() == Response.Status.OK) {
                    DtoTokensList settings = response.getEntity(new GenericType<DtoTokensList>() {});
                    return settings;
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
        throw new CollageRestException(String.format(EXCEPTION_EXECUTING_WITH_STATUS_AND_REASON,
                status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());

    }

    public DtoOperationResults setAsyncSettings(DtoAsyncSettings settings) {
        Response.Status status = Response.Status.SERVICE_UNAVAILABLE;
        ClientResponse<DtoOperationResults> response = null;
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                ClientRequest request = createClientRequest(build(API_ROOT_SINGLE + API_ASYNC));
                request.accept(mediaType);
                request.body(mediaType, settings);
                response = request.put();
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
        throw new CollageRestException(String.format("Exception executing post to async settings with status code of %d, reason: %s",
                status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }

}


