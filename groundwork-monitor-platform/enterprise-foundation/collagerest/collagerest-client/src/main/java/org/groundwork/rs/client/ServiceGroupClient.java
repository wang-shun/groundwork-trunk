package org.groundwork.rs.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.rs.dto.*;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.util.GenericType;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ServiceGroupClient extends BaseRestClient {

    protected static Log log = LogFactory.getLog(ServiceGroupClient.class);
    private static final String API_ROOT_SINGLE = "/servicegroups";
    private static final String API_ROOT = API_ROOT_SINGLE + "/";
    private static final String RPC_AUTOCOMPLETE = "autocomplete";

    public ServiceGroupClient(String deploymentUrl) {
        super(deploymentUrl);
    }

    public ServiceGroupClient(String deploymentUrl, MediaType mediaType) {
        this(deploymentUrl);
        this.mediaType = mediaType;
    }

    public DtoServiceGroup lookup(String serviceGroupName) throws CollageRestException {
        return lookup(serviceGroupName, DtoDepthType.Shallow);
    }

    public DtoServiceGroup lookup(String serviceGroupName, DtoDepthType depthType) throws CollageRestException {
        try {
            String requestUrl = buildLookupWithDepth(API_ROOT, serviceGroupName, depthType);
            String requestDescription = String.format("lookup serviceGroup [%s]", serviceGroupName);
            return clientRequest(requestUrl, requestDescription, new GenericType<DtoServiceGroup>(){});
        }
        catch (Exception e) {
            throw new CollageRestException(e);
        }
    }

    public List<DtoServiceGroup> list() throws CollageRestException {
        return list(null, null);
    }

    public List<DtoServiceGroup> list(String appType, String agentId) throws CollageRestException {
        Response.Status status = Response.Status.SERVICE_UNAVAILABLE;
        ClientResponse<DtoServiceGroupList> response = null;
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                String url = this.build(API_ROOT);
                if (!isEmpty(appType) || !isEmpty(agentId)) {
                    List<String> names = new ArrayList<String>();
                    List<String> values = new ArrayList<String>();
                    if (!isEmpty(appType)) {
                        names.add("appType");
                        values.add(appType);
                    }
                    if (!isEmpty(agentId)) {
                        names.add("agentId");
                        values.add(agentId);
                    }
                    String[] namesArray = names.toArray(new String[names.size()]);
                    String[] valuesArray = values.toArray(new String[values.size()]);
                    url = buildUrlWithPathAndQueryParams(API_ROOT, null, buildEncodedQueryParams(namesArray, valuesArray));
                }
                ClientRequest request = createClientRequest(url);
                request.accept(mediaType);
                response = request.get(new GenericType<DtoServiceGroupList>(){});
                if (response.getResponseStatus() == Response.Status.OK) {
                    DtoServiceGroupList serviceGroupList = response.getEntity(new GenericType<DtoServiceGroupList>(){});
                    return serviceGroupList.getServiceGroups();
                }
                else if (response.getResponseStatus() == Response.Status.NOT_FOUND) {
                    // return an empty list for not found exception
                    return new ArrayList<DtoServiceGroup>();
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
        String message = String.format("AppType: %s, AgentID: %s",
                (appType == null) ? "" : appType,
                (agentId == null) ? "" : agentId);
        throw new CollageRestException(String.format("Exception executing query serviceGroups (%s) with status code of %d, reason: %s",
                message, status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }

    public List<DtoServiceGroup> query(String query) throws CollageRestException {
        return query(query, -1, -1);
    }

    public List<DtoServiceGroup> query(String query, int first, int count) throws CollageRestException {
        Response.Status status = Response.Status.SERVICE_UNAVAILABLE;
        ClientResponse<DtoServiceGroup> response = null;
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                String url = buildEncodedQuery(API_ROOT_SINGLE, query, first, count);
                ClientRequest request = createClientRequest(url);
                request.accept(mediaType);
                response = request.get(new GenericType<DtoServiceGroup>(){});
                if (response.getResponseStatus() == Response.Status.OK) {
                    return response.getEntity(new GenericType<DtoServiceGroupList>(){}).getServiceGroups();
                } else if (response.getResponseStatus() == Response.Status.NOT_FOUND) {
                    return new ArrayList<DtoServiceGroup>();
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
        throw new CollageRestException(String.format("Exception executing query service groups (%s) with status code of %d, reason: %s", query, status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }

    public DtoOperationResults post(DtoServiceGroupUpdateList updates) throws CollageRestException {
        Response.Status status = Response.Status.SERVICE_UNAVAILABLE;
        ClientResponse<DtoOperationResults> response = null;
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                ClientRequest request = createClientRequest(build(API_ROOT_SINGLE));
                request.accept(mediaType);
                request.body(mediaType, updates);
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
        throw new CollageRestException(String.format("Exception executing post to serviceGroups with status code of %d, reason: %s",
                status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }

    public DtoOperationResults delete(String serviceGroupName) throws CollageRestException {
        return delete(Arrays.asList(new String[]{serviceGroupName}));
    }

    public DtoOperationResults delete(List<String> serviceGroupNamesList) throws CollageRestException {
        DtoServiceGroupUpdateList deletes = new DtoServiceGroupUpdateList();
        for (String serviceGroupName : serviceGroupNamesList) {
            deletes.add(new DtoServiceGroupUpdate(serviceGroupName));
        }
        return delete(deletes);
    }

    public DtoOperationResults delete(DtoServiceGroupUpdateList deletes) throws CollageRestException {
        Response.Status status = Response.Status.SERVICE_UNAVAILABLE;
        ClientResponse<DtoOperationResults> response = null;
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                ClientRequest request = createClientRequest(build(API_ROOT_SINGLE));
                request.accept(mediaType);
                request.body(mediaType, deletes);
                response = request.delete();
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
        throw new CollageRestException(String.format("Exception executing delete to serviceGroups with status code of %d, reason: %s",
                status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }


    public DtoOperationResults addMembers(DtoServiceGroupMemberUpdate update) throws CollageRestException {
        Response.Status status = Response.Status.SERVICE_UNAVAILABLE;
        ClientResponse<DtoOperationResults> response = null;
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                ClientRequest request = createClientRequest(buildUrlWithPath(API_ROOT, "addmembers"));
                request.accept(mediaType);
                request.body(mediaType, update);
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
        throw new CollageRestException(String.format("Exception executing add members to serviceGroups with status code of %d, reason: %s",
                status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }

    public DtoOperationResults deleteMembers(DtoServiceGroupMemberUpdate update) throws CollageRestException {
        Response.Status status = Response.Status.SERVICE_UNAVAILABLE;
        ClientResponse<DtoOperationResults> response = null;
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                ClientRequest request = createClientRequest(buildUrlWithPath(API_ROOT, "deletemembers"));
                request.accept(mediaType);
                request.body(mediaType, update);
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
        throw new CollageRestException(String.format("Exception executing delete members to serviceGroups with status code of %d, reason: %s",
                status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }

    /**
     * Lookup service group name autocomplete suggestions for specified prefix.
     * A null, blank, or '*' wildcard prefix matches all names.
     *
     * @param prefix service group name prefix
     * @return list of suggestions strings or empty list
     */
    public List<DtoName> autocomplete(String prefix) {
        prefix = (((prefix != null) && !prefix.isEmpty()) ? prefix : "*");
        String requestUrl = buildUrlWithPaths(API_ROOT, RPC_AUTOCOMPLETE, prefix);
        String requestDescription = String.format("autocomplete prefix [%s]", prefix);
        DtoNamesList dtoNamesList = clientRequest(requestUrl, requestDescription,
                new GenericType<DtoNamesList>(){});
        return ((dtoNamesList != null) ? dtoNamesList.getNames() : Collections.EMPTY_LIST);
    }

    /**
     * Lookup service group name autocomplete suggestions for specified prefix.
     * If a negative limit is specified, no limit will be applied to the
     * returned suggestion strings. Autocomplete suggestions are considered
     * unique based on their canonical names. In this case, the total number
     * of suggestions returned can exceed the limit since it is limiting the
     * number of unique canonical names. A null, blank, or '*' wildcard prefix
     * matches all names.
     *
     * @param prefix service group name prefix
     * @param limit unique suggestions limit, (-1 for unlimited)
     * @return list of suggestions strings or empty list
     */
    public List<DtoName> autocomplete(String prefix, int limit) {
        prefix = (((prefix != null) && !prefix.isEmpty()) ? prefix : "*");
        String requestParams;
        try {
            requestParams = buildEncodedQueryParams(new String[]{"limit"}, new String[]{Integer.toString(limit)});
        } catch (UnsupportedEncodingException uee) {
            throw new RuntimeException(uee);
        }
        String requestUrl = buildUrlWithPathsAndQueryParams(API_ROOT, RPC_AUTOCOMPLETE, prefix, requestParams);
        String requestDescription = String.format("autocomplete prefix [%s]", prefix);
        DtoNamesList dtoNamesList = clientRequest(requestUrl, requestDescription,
                new GenericType<DtoNamesList>(){});
        return ((dtoNamesList != null) ? dtoNamesList.getNames() : Collections.EMPTY_LIST);
    }
}
