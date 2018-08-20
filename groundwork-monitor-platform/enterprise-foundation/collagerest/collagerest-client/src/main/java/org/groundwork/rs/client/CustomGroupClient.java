/*
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2015  GroundWork Open Source Solutions info@groundworkopensource.com

 *     This program is free software; you can redistribute it and/or modify
 *     it under the terms of version 2 of the GNU General Public License
 *     as published by the Free Software Foundation.

 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.

 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package org.groundwork.rs.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.rs.dto.DtoCustomGroup;
import org.groundwork.rs.dto.DtoCustomGroupList;
import org.groundwork.rs.dto.DtoCustomGroupMemberUpdate;
import org.groundwork.rs.dto.DtoCustomGroupUpdate;
import org.groundwork.rs.dto.DtoCustomGroupUpdateList;
import org.groundwork.rs.dto.DtoName;
import org.groundwork.rs.dto.DtoNamesList;
import org.groundwork.rs.dto.DtoOperationResults;
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

/**
 * CustomGroupClient
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public class CustomGroupClient extends BaseRestClient {

    protected static Log log = LogFactory.getLog(CustomGroupClient.class);

    private static final String API_ROOT_SINGLE = "/customgroups";
    private static final String API_ROOT = API_ROOT_SINGLE + "/";
    private static final String RPC_AUTOCOMPLETE = "autocomplete";

    /**
     * Constructor for client taking deployment url.
     *
     * @param deploymentUrl deployment url
     */
    public CustomGroupClient(String deploymentUrl) {
        super(deploymentUrl);
    }

    /**
     * Constructor for client taking deployment url and media type.
     *
     * @param deploymentUrl deployment url
     * @param mediaType JSON/XML media type
     */
    public CustomGroupClient(String deploymentUrl, MediaType mediaType) {
        this(deploymentUrl);
        this.mediaType = mediaType;
    }

    /**
     * Lookup a custom group by name.
     *
     * @param customGroupName custom group name
     * @return custom group
     * @throws CollageRestException
     */
    public DtoCustomGroup lookup(String customGroupName) throws CollageRestException {
        Response.Status status = Response.Status.SERVICE_UNAVAILABLE;
        ClientResponse<DtoCustomGroup> response = null;
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                String url = buildUrlWithPath(API_ROOT, customGroupName);
                ClientRequest request = createClientRequest(url);
                request.accept(mediaType);
                response = request.get(new GenericType<DtoCustomGroup>(){});
                if (response.getResponseStatus() == Response.Status.OK) {
                    DtoCustomGroup customGroup = response.getEntity(new GenericType<DtoCustomGroup>() {});
                    return customGroup;
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
        throw new CollageRestException(String.format("Exception executing lookup customgroups (%s) with status code of %d, reason: %s",
                customGroupName, status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }

    /**
     * Get all custom groups.
     *
     * @return custom groups
     * @throws CollageRestException
     */
    public List<DtoCustomGroup> list() throws CollageRestException {
        return list(null, null);
    }

    /**
     * Get all custom groups filtered by application type and agent id.
     *
     * @param appType application type name filter
     * @param agentId agent id filter
     * @return custom groups
     * @throws CollageRestException
     */
    public List<DtoCustomGroup> list(String appType, String agentId) throws CollageRestException {
        Response.Status status = Response.Status.SERVICE_UNAVAILABLE;
        ClientResponse<DtoCustomGroupList> response = null;
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
                response = request.get(new GenericType<DtoCustomGroupList>(){});
                if (response.getResponseStatus() == Response.Status.OK) {
                    DtoCustomGroupList customGroupList = response.getEntity(new GenericType<DtoCustomGroupList>(){});
                    return customGroupList.getCustomGroups();
                } else if (response.getResponseStatus() == Response.Status.NOT_FOUND) {
                    // return an empty list for not found exception
                    return new ArrayList<DtoCustomGroup>();
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
            if (response != null) {
                response.releaseConnection();
            }
        }
        if (status == null) {
            status = Response.Status.SERVICE_UNAVAILABLE;
        }
        String message = String.format("AppType: %s, AgentID: %s", ((appType == null) ? "" : appType),
                ((agentId == null) ? "" : agentId));
        throw new CollageRestException(String.format("Exception executing query customgroups (%s) with status code of %d, reason: %s",
                message, status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }

    /**
     * Query custom groups.
     *
     * @param query query string
     * @return custom groups
     * @throws CollageRestException
     */
    public List<DtoCustomGroup> query(String query) throws CollageRestException {
        return query(query, -1, -1);
    }

    /**
     * Query custom groups with paging control.
     *
     * @param query query string
     * @param first index of first custom group
     * @param count limit of the number of custom groups
     * @return page of custom groups
     * @throws CollageRestException
     */
    public List<DtoCustomGroup> query(String query, int first, int count) throws CollageRestException {
        Response.Status status = Response.Status.SERVICE_UNAVAILABLE;
        ClientResponse<DtoCustomGroup> response = null;
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                String url = buildEncodedQuery(API_ROOT_SINGLE, query, first, count);
                ClientRequest request = createClientRequest(url);
                request.accept(mediaType);
                response = request.get(new GenericType<DtoCustomGroup>(){});
                if (response.getResponseStatus() == Response.Status.OK) {
                    return response.getEntity(new GenericType<DtoCustomGroupList>(){}).getCustomGroups();
                } else if (response.getResponseStatus() == Response.Status.NOT_FOUND) {
                    return new ArrayList<DtoCustomGroup>();
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
        throw new CollageRestException(String.format("Exception executing query customgroups (%s) with status code of %d, reason: %s", query, status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }

    /**
     * Update custom groups. Updates existing custom groups or creates
     * new custom groups. Host and service groups of existing custom
     * groups are replaced with update groups.
     *
     * @param updates list of custom group updates
     * @return update operation results
     * @throws CollageRestException
     */
    public DtoOperationResults post(DtoCustomGroupUpdateList updates) throws CollageRestException {
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
            if (response != null) {
                response.releaseConnection();
            }
        }
        if (status == null) {
            status = Response.Status.SERVICE_UNAVAILABLE;
        }
        throw new CollageRestException(String.format("Exception executing post to customgroups with status code of %d, reason: %s",
                status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }

    /**
     * Delete custom group by name.
     *
     * @param customGroupName custom group name
     * @return delete operation results
     * @throws CollageRestException
     */
    public DtoOperationResults delete(String customGroupName) throws CollageRestException {
        return delete(Arrays.asList(new String[]{customGroupName}));
    }

    /**
     * Delete custom groups by name.
     *
     * @param customGroupNamesList list of custom group name
     * @return delete operation results
     * @throws CollageRestException
     */
    public DtoOperationResults delete(List<String> customGroupNamesList) throws CollageRestException {
        DtoCustomGroupUpdateList deletes = new DtoCustomGroupUpdateList();
        for (String customGroupName : customGroupNamesList) {
            deletes.add(new DtoCustomGroupUpdate(customGroupName));
        }
        return delete(deletes);
    }

    /**
     * Delete custom groups by update. Only the custom group name is
     * needs to be set in each custom group update.
     *
     * @param deletes list of custom group updates
     * @return delete operation results
     * @throws CollageRestException
     */
    public DtoOperationResults delete(DtoCustomGroupUpdateList deletes) throws CollageRestException {
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
            if (response != null) {
                response.releaseConnection();
            }
        }
        if (status == null) {
            status = Response.Status.SERVICE_UNAVAILABLE;
        }
        throw new CollageRestException(String.format("Exception executing delete to customgroups with status code of %d, reason: %s",
                status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }

    /**
     * Add host and service group members to a category group.
     *
     * @param update custom group member update
     * @return update operation results
     * @throws CollageRestException
     */
    public DtoOperationResults addMembers(DtoCustomGroupMemberUpdate update) throws CollageRestException {
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
            if (response != null) {
                response.releaseConnection();
            }
        }
        if (status == null) {
            status = Response.Status.SERVICE_UNAVAILABLE;
        }
        throw new CollageRestException(String.format("Exception executing add members to customgroups with status code of %d, reason: %s",
                status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }

    /**
     * Delete host and service group members from a category group.
     *
     * @param update custom group member update
     * @return update operation results
     * @throws CollageRestException
     */
    public DtoOperationResults deleteMembers(DtoCustomGroupMemberUpdate update) throws CollageRestException {
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
            if (response != null) {
                response.releaseConnection();
            }
        }
        if (status == null) {
            status = Response.Status.SERVICE_UNAVAILABLE;
        }
        throw new CollageRestException(String.format("Exception executing delete members to customgroups with status code of %d, reason: %s",
                status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }

    /**
     * Lookup custom group name autocomplete suggestions for specified prefix.
     * A null, blank, or '*' wildcard prefix matches all names.
     *
     * @param prefix custom group name prefix
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
     * Lookup custom group name autocomplete suggestions for specified prefix.
     * If a negative limit is specified, no limit will be applied to the
     * returned suggestion strings. Autocomplete suggestions are considered
     * unique based on their canonical names. In this case, the total number
     * of suggestions returned can exceed the limit since it is limiting the
     * number of unique canonical names. A null, blank, or '*' wildcard prefix
     * matches all names.
     *
     * @param prefix custom group name prefix
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
