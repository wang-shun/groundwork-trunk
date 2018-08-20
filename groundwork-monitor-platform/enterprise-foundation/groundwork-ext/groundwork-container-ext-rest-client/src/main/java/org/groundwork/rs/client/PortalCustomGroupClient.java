package org.groundwork.rs.client;


import com.groundworkopensource.portal.model.CustomGroupList;
import com.groundworkopensource.portal.model.EntityTypeList;
import com.groundworkopensource.portal.model.ExtendedRoleList;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.ws.impl.WSClientConfiguration;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.util.GenericType;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;


public class PortalCustomGroupClient extends BaseRestClient {

    static final String API_ROOT_SINGLE = "/customgroup";
    static final String API_ROOT = API_ROOT_SINGLE + "/";

    protected static Log log = LogFactory.getLog(PortalCustomGroupClient.class);


    public PortalCustomGroupClient(String deploymentUrl) {
        this(deploymentUrl,MediaType.APPLICATION_XML_TYPE);
    }

    public PortalCustomGroupClient(String deploymentUrl, MediaType mediaType) {
        super(deploymentUrl, WSClientConfiguration.getProperty(FOUNDATION_REST_URL));
        this.mediaType = mediaType;
    }

    public class Response {
        private final String token;
        private final javax.ws.rs.core.Response.Status status;

        public Response(String token, javax.ws.rs.core.Response.Status status) {
            this.token = token;
            this.status = status;
        }

        public String getToken() {
            return token;
        }

        public javax.ws.rs.core.Response.Status getStatus() {
            return status;
        }

        public boolean success() {
            return status == javax.ws.rs.core.Response.Status.OK;
        }

        public boolean authFailure() {
            return status == javax.ws.rs.core.Response.Status.NOT_FOUND;
        }

        public boolean error() {
            return status != javax.ws.rs.core.Response.Status.NOT_FOUND &&
                    status != javax.ws.rs.core.Response.Status.OK;
        }

    }


    /**
     * Finds customgroups
     * @return
     * @throws CollageRestException
     */
    public CustomGroupList findCustomGroups() throws CollageRestException {
        javax.ws.rs.core.Response.Status status = javax.ws.rs.core.Response.Status.SERVICE_UNAVAILABLE;
        ClientResponse<ExtendedRoleList> response = null;
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                String url = buildUrlWithPath(API_ROOT, "findcustomgroups");
                ClientRequest request = createClientRequest(url);
                request.accept(mediaType);
                response = request.get(new GenericType<CustomGroupList>() {
                });
                if (response.getResponseStatus() == javax.ws.rs.core.Response.Status.OK) {
                    CustomGroupList groupList = response.getEntity(new GenericType<CustomGroupList>() {
                    });
                    return groupList;
                } else if (response.getResponseStatus() == javax.ws.rs.core.Response.Status.NOT_FOUND) {
                    return null;
                } else if (response.getResponseStatus() == javax.ws.rs.core.Response.Status.UNAUTHORIZED && retry < 1) {
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
            if (response != null)
                response.releaseConnection();
        }
        if (status == null)
            status = javax.ws.rs.core.Response.Status.SERVICE_UNAVAILABLE;
        throw new CollageRestException(String.format("Exception findCustomGroups (%s) with status code of %d, reason: %s",
                "", status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }


    /**
     * Finds customgroups
     * @return
     * @throws CollageRestException
     */
    public EntityTypeList findEntityTypes() throws CollageRestException {
        javax.ws.rs.core.Response.Status status = javax.ws.rs.core.Response.Status.SERVICE_UNAVAILABLE;
        ClientResponse<ExtendedRoleList> response = null;
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                String url = buildUrlWithPath(API_ROOT, "findEntityTypes");
                ClientRequest request = createClientRequest(url);
                request.accept(mediaType);
                response = request.get(new GenericType<EntityTypeList>() {
                });
                if (response.getResponseStatus() == javax.ws.rs.core.Response.Status.OK) {
                    EntityTypeList typeList = response.getEntity(new GenericType<EntityTypeList>() {
                    });
                    return typeList;
                } else if (response.getResponseStatus() == javax.ws.rs.core.Response.Status.NOT_FOUND) {
                    return null;
                } else if (response.getResponseStatus() == javax.ws.rs.core.Response.Status.UNAUTHORIZED && retry < 1) {
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
            if (response != null)
                response.releaseConnection();
        }
        if (status == null)
            status = javax.ws.rs.core.Response.Status.SERVICE_UNAVAILABLE;
        throw new CollageRestException(String.format("Exception findEntityTypes customgroup (%s) with status code of %d, reason: %s",
                "", status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }

    /**
     * Creates customgroup
     * @param groupName
     * @param entityTypeId
     * @param parents
     * @param groupState
     * @param createdBy
     * @param children
     * @return
     * @throws CollageRestException
     */
    public javax.ws.rs.core.Response.Status createCustomGroup( String groupName,
                                             int entityTypeId,
                                             String parents,
                                             String groupState,
                                             String createdBy,
                                             String children) throws CollageRestException {
        javax.ws.rs.core.Response.Status status = javax.ws.rs.core.Response.Status.SERVICE_UNAVAILABLE;
        ClientResponse response = null;
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                String url = buildUrlWithPath(API_ROOT, "createCustomGroup");
                ClientRequest request = createClientRequest(url);
                request.accept(mediaType);
                final MultivaluedMap<String, String> formParameters = request.getFormParameters();
                formParameters.putSingle("groupName", groupName);
                formParameters.putSingle("entityTypeId", String.valueOf(entityTypeId));
                formParameters.putSingle("parents", parents);
                formParameters.putSingle("groupState", groupState);
                formParameters.putSingle("createdBy", createdBy);
                formParameters.putSingle("children", children);
                response = request.post();
                if (response.getResponseStatus() == javax.ws.rs.core.Response.Status.OK) {
                    return response.getResponseStatus();
                } else if (response.getResponseStatus() == javax.ws.rs.core.Response.Status.NOT_FOUND) {
                    return null;
                } else if (response.getResponseStatus() == javax.ws.rs.core.Response.Status.UNAUTHORIZED && retry < 1) {
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
            if (response != null)
                response.releaseConnection();
        }
        if (status == null)
            status = javax.ws.rs.core.Response.Status.SERVICE_UNAVAILABLE;
        throw new CollageRestException(String.format("Exception creating customgroup (%s) with status code of %d, reason: %s",
                groupName, status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }

    /**
     * Updates customgroup
     * @param groupName
     * @param entityTypeId
     * @param parents
     * @param groupState
     * @param createdBy
     * @param children
     * @return
     * @throws CollageRestException
     */
    public javax.ws.rs.core.Response.Status updateCustomGroup(String groupName,
                                            int entityTypeId,
                                            String parents,
                                            String groupState,
                                            String createdBy,
                                            String children) throws CollageRestException {
        javax.ws.rs.core.Response.Status status = javax.ws.rs.core.Response.Status.SERVICE_UNAVAILABLE;
        ClientResponse response = null;
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                String url = buildUrlWithPath(API_ROOT, "updateCustomGroup");
                ClientRequest request = createClientRequest(url);
                request.accept(mediaType);
                final MultivaluedMap<String, String> formParameters = request.getFormParameters();
                formParameters.putSingle("groupName", groupName);
                formParameters.putSingle("entityTypeId", String.valueOf(entityTypeId));
                formParameters.putSingle("parents", parents);
                formParameters.putSingle("groupState", groupState);
                formParameters.putSingle("createdBy", createdBy);
                formParameters.putSingle("children", children);
                response = request.post();
                if (response.getResponseStatus() == javax.ws.rs.core.Response.Status.OK) {
                    return response.getResponseStatus();
                } else if (response.getResponseStatus() == javax.ws.rs.core.Response.Status.NOT_FOUND) {
                    return null;
                } else if (response.getResponseStatus() == javax.ws.rs.core.Response.Status.UNAUTHORIZED && retry < 1) {
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
            if (response != null)
                response.releaseConnection();
        }
        if (status == null)
            status = javax.ws.rs.core.Response.Status.SERVICE_UNAVAILABLE;
        throw new CollageRestException(String.format("Exception updating customgroups (%s) with status code of %d, reason: %s",
                groupName, status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }

    /**
     * Removes the customgroup
     * @param groupId
     * @return
     * @throws CollageRestException
     */
    public javax.ws.rs.core.Response.Status removeCustomGroup(String groupId) throws CollageRestException {
        javax.ws.rs.core.Response.Status status = javax.ws.rs.core.Response.Status.SERVICE_UNAVAILABLE;
        ClientResponse response = null;
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                String url = buildUrlWithPath(API_ROOT, "removeCustomGroup");
                ClientRequest request = createClientRequest(url);
                request.accept(mediaType);
                final MultivaluedMap<String, String> formParameters = request.getFormParameters();
                formParameters.putSingle("groupId", groupId);
                response = request.post();
                if (response.getResponseStatus() == javax.ws.rs.core.Response.Status.OK) {
                    return response.getResponseStatus();
                } else if (response.getResponseStatus() == javax.ws.rs.core.Response.Status.NOT_FOUND) {
                    return null;
                } else if (response.getResponseStatus() == javax.ws.rs.core.Response.Status.UNAUTHORIZED && retry < 1) {
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
            if (response != null)
                response.releaseConnection();
        }
        if (status == null)
            status = javax.ws.rs.core.Response.Status.SERVICE_UNAVAILABLE;
        throw new CollageRestException(String.format("Exception removing customgroups (%s) with status code of %d, reason: %s",
                groupId, status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }

    /**
     * Removes orphaned children
     * @param elementId
     * @param entityTypeId
     * @return
     * @throws CollageRestException
     */
    public javax.ws.rs.core.Response.Status removeOrphanedChildren(Long elementId,
                                                 int entityTypeId) throws CollageRestException {
        javax.ws.rs.core.Response.Status status = javax.ws.rs.core.Response.Status.SERVICE_UNAVAILABLE;
        ClientResponse response = null;
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                String url = buildUrlWithPath(API_ROOT, "removeOrphanedChildren");
                ClientRequest request = createClientRequest(url);
                request.accept(mediaType);
                final MultivaluedMap<String, String> formParameters = request.getFormParameters();
                formParameters.putSingle("elementId", String.valueOf(elementId));
                formParameters.putSingle("entityTypeId", String.valueOf(entityTypeId));
                response = request.post();
                if (response.getResponseStatus() == javax.ws.rs.core.Response.Status.OK) {
                    return response.getResponseStatus();
                } else if (response.getResponseStatus() == javax.ws.rs.core.Response.Status.NOT_FOUND) {
                    return null;
                } else if (response.getResponseStatus() == javax.ws.rs.core.Response.Status.UNAUTHORIZED && retry < 1) {
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
            if (response != null)
                response.releaseConnection();
        }
        if (status == null)
            status = javax.ws.rs.core.Response.Status.SERVICE_UNAVAILABLE;
        throw new CollageRestException(String.format("Exception removing orphaned children (%s) with status code of %d, reason: %s",
                elementId, status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }


}
