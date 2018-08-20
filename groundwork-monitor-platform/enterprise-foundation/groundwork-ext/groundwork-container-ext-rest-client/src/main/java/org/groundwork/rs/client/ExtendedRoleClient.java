package org.groundwork.rs.client;


import com.groundworkopensource.portal.model.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.ws.impl.WSClientConfiguration;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import javax.ws.rs.core.Response.Status;
import org.jboss.resteasy.util.GenericType;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.util.ArrayList;
import java.util.List;


public class ExtendedRoleClient extends BaseRestClient {

    static final String API_ROOT_SINGLE = "/extendedrole";
    static final String API_ROOT = API_ROOT_SINGLE + "/";

    protected static Log log = LogFactory.getLog(ExtendedRoleClient.class);


    public ExtendedRoleClient(String deploymentUrl) {
        this(deploymentUrl,MediaType.APPLICATION_XML_TYPE);
    }

    public ExtendedRoleClient(String deploymentUrl, MediaType mediaType) {
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
     * Finds Extended UI Roles by user
     * @param userName
     * @return
     * @throws CollageRestException
     */
    public ExtendedRoleList findRolesByUser(String userName) throws CollageRestException {
        Status status = javax.ws.rs.core.Response.Status.SERVICE_UNAVAILABLE;
        String[] names = {"userName"};
        String[] values = {userName};
        ClientResponse<ExtendedRoleList> response = null;
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                String url = buildUrlWithPathAndQueryParams(API_ROOT, "findrolesbyuser", buildEncodedQueryParams(names, values));
                ClientRequest request = createClientRequest(url);
                request.accept(mediaType);
                response = request.get(new GenericType<ExtendedRoleList>() {
                });
                if (response.getResponseStatus() == javax.ws.rs.core.Response.Status.OK) {
                    ExtendedRoleList roleList = response.getEntity(new GenericType<ExtendedRoleList>() {
                    });
                    return roleList;
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
            status = Status.SERVICE_UNAVAILABLE;
        throw new CollageRestException(String.format("Exception executing lookup ExtendedRoles (%s) with status code of %d, reason: %s",
                userName, status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }

    /**
     * Find role by name
     * @param roleName
     * @return
     * @throws CollageRestException
     */
    public ExtendedUIRole findRoleByName(String roleName) throws CollageRestException {
        javax.ws.rs.core.Response.Status status = javax.ws.rs.core.Response.Status.SERVICE_UNAVAILABLE;
        String[] names = {"roleName"};
        String[] values = {roleName};
        ClientResponse<ExtendedUIRole> response = null;
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                String url = buildUrlWithPathAndQueryParams(API_ROOT, "findrolebyname", buildEncodedQueryParams(names, values));
                ClientRequest request = createClientRequest(url);
                request.accept(mediaType);
                response = request.get(new GenericType<ExtendedUIRole>() {
                });
                if (response.getResponseStatus() == javax.ws.rs.core.Response.Status.OK) {
                    ExtendedUIRole role = response.getEntity(new GenericType<ExtendedUIRole>() {
                    });
                    return role;
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
        throw new CollageRestException(String.format("Exception executing lookup ExtendedRoles (%s) with status code of %d, reason: %s",
                roleName, status.getStatusCode(), status.getReasonPhrase()));
    }

    /**
     * Creates new Extended Rol
     * @return
     * @throws CollageRestException
     */
    public javax.ws.rs.core.Response.Status createRole(ExtendedUIRole uiRole) throws CollageRestException {
        javax.ws.rs.core.Response.Status status = javax.ws.rs.core.Response.Status.SERVICE_UNAVAILABLE;
        ClientResponse response = null;
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                String url = buildUrlWithPath(API_ROOT, "create");
                ClientRequest request = createClientRequest(url);

                request.accept(mediaType);
                request.body(mediaType,uiRole);
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
        throw new CollageRestException(String.format("Exception creating ExtendedRoles (%s) with status code of %d, reason: %s",
                uiRole.getRoleName(), status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }

    /**
     * Updates the extended role
     * @param roleId
     * @param name
     * @param isDashboardLinksDisabled
     * @param hgList
     * @param sgList
     * @param restrictionType
     * @param defaultHG
     * @param defaultSG
     * @param isActionsEnabled
     * @return
     * @throws CollageRestException
     */
    public javax.ws.rs.core.Response.Status updateRole(ExtendedUIRole uiRole) throws CollageRestException {
        javax.ws.rs.core.Response.Status status = javax.ws.rs.core.Response.Status.SERVICE_UNAVAILABLE;
        ClientResponse response = null;
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                String url = buildUrlWithPath(API_ROOT, "update");
                ClientRequest request = createClientRequest(url);

                request.accept(mediaType);
                request.body(mediaType,uiRole);
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
        throw new CollageRestException(String.format("Exception updating ExtendedRoles (%s) with status code of %d, reason: %s",
                uiRole.getRoleName(), status.getStatusCode(), status.getReasonPhrase()));
    }

    /**
     * Deletes the extendedRole
     * @param roleName
     * @return
     * @throws CollageRestException
     */
    public javax.ws.rs.core.Response.Status deleteRole(String roleName) throws CollageRestException {
        javax.ws.rs.core.Response.Status status = javax.ws.rs.core.Response.Status.SERVICE_UNAVAILABLE;
        ClientResponse response = null;
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                String url = buildUrlWithPath(API_ROOT, "delete");
                ClientRequest request = createClientRequest(url);
                final MultivaluedMap<String, String> formParameters = request.getFormParameters();
                formParameters.putSingle("roleName", roleName);
                request.accept(mediaType);
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
        throw new CollageRestException(String.format("Exception deleting ExtendedRoles (%s) with status code of %d, reason: %s",
                roleName, status.getStatusCode(), status.getReasonPhrase()));
    }

    /**
     * Updates the Actions for the role
     * @param name
     * @param isActionsEnabled
     * @return
     * @throws CollageRestException
     */
    public javax.ws.rs.core.Response.Status updateActionsEnabled(String name, boolean isActionsEnabled) throws CollageRestException {
        javax.ws.rs.core.Response.Status status = javax.ws.rs.core.Response.Status.SERVICE_UNAVAILABLE;
        String[] names = {"name","isActionsEnabled"};
        String[] values = {name,String.valueOf(isActionsEnabled)};
        ClientResponse response = null;
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                String url = buildUrlWithPath(API_ROOT, "updateactions");
                ClientRequest request = createClientRequest(url);
                request.accept(mediaType);
                final MultivaluedMap<String, String> formParameters = request.getFormParameters();
                formParameters.putSingle("name", name);
                formParameters.putSingle("isActionsEnabled", String.valueOf(isActionsEnabled));
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
        throw new CollageRestException(String.format("Exception updating actions for ExtendedRole (%s) with status code of %d, reason: %s",
                name, status.getStatusCode(), status.getReasonPhrase()));
    }


    /**
     * Gets all resources
     * @return
     * @throws CollageRestException
     */
    public ExtendedUIResourceList getResources() throws CollageRestException {
        javax.ws.rs.core.Response.Status status = javax.ws.rs.core.Response.Status.SERVICE_UNAVAILABLE;
        ClientResponse<ExtendedUIResourceList> response = null;
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                String url = buildUrlWithPath(API_ROOT, "getResources");
                ClientRequest request = createClientRequest(url);
                request.accept(mediaType);
                response = request.get(new GenericType<ExtendedUIResourceList>() {
                });
                if (response.getResponseStatus() == javax.ws.rs.core.Response.Status.OK) {
                    ExtendedUIResourceList resourceList = response.getEntity(new GenericType<ExtendedUIResourceList>() {
                    });
                    return resourceList;
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
        throw new CollageRestException(String.format("Exception getResources (%s) with status code of %d, reason: %s",
                "", status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }


}
