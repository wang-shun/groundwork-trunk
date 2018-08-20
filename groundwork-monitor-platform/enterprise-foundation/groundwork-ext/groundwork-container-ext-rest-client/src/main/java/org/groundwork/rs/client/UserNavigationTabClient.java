package org.groundwork.rs.client;


import com.groundworkopensource.portal.model.ExtendedRoleList;
import com.groundworkopensource.portal.model.NavigationList;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.ws.impl.WSClientConfiguration;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.util.GenericType;

import javax.ws.rs.core.MediaType;



public class UserNavigationTabClient extends BaseRestClient {

    static final String API_ROOT_SINGLE = "/usertabpersistance";
    static final String API_ROOT = API_ROOT_SINGLE + "/";

    protected static Log log = LogFactory.getLog(UserNavigationTabClient.class);


    public UserNavigationTabClient(String deploymentUrl) {
        this(deploymentUrl,MediaType.APPLICATION_XML_TYPE);
    }

    public UserNavigationTabClient(String deploymentUrl, MediaType mediaType) {
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
     *
     * @param userId
     * @return
     * @throws CollageRestException
     */
    public NavigationList getHistoryRecords(String userId,String app_type) throws CollageRestException {
        javax.ws.rs.core.Response.Status status = javax.ws.rs.core.Response.Status.SERVICE_UNAVAILABLE;
        String[] names = {"userId","app_type"};
        String[] values = {userId,app_type};
        ClientResponse<NavigationList> response = null;
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                String url = buildUrlWithPathAndQueryParams(API_ROOT, "gethistory", buildEncodedQueryParams(names, values));
                ClientRequest request = createClientRequest(url);
                request.accept(mediaType);
                response = request.get();
                if (response.getResponseStatus() == javax.ws.rs.core.Response.Status.OK) {
                    NavigationList naviList = response.getEntity(new GenericType<NavigationList>() {
                    });
                    return naviList;
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
        throw new CollageRestException(String.format("Exception executing getHistoryRecords (%s) with status code of %d, reason: %s",
                userId, status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }

    /**
     * Adds history
     *
     * @param userId
     * @param nodeId
     * @param nodeName
     * @param nodeType
     * @param parentInfo
     * @param toolTip
     * @param app_type
     * @return
     * @throws CollageRestException
     */
    public javax.ws.rs.core.Response.Status addHistoryRecord(String userId,
                                           int nodeId,
                                           String nodeName,
                                           String nodeType,
                                           String parentInfo,
                                           String toolTip,
                                           String app_type) throws CollageRestException {
        javax.ws.rs.core.Response.Status status = javax.ws.rs.core.Response.Status.SERVICE_UNAVAILABLE;
        String[] names = {"userId", "nodeId", "nodeName", "nodeType", "parentInfo", "toolTip", "app_type"};
        String[] values = {userId, String.valueOf(nodeId), nodeName, nodeType, parentInfo, toolTip, app_type};
        ClientResponse response = null;
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                String url = buildUrlWithPathAndQueryParams(API_ROOT, "addwithoutlabel", buildEncodedQueryParams(names, values));
                ClientRequest request = createClientRequest(url);
                request.accept(mediaType);
                response = request.get();
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
        throw new CollageRestException(String.format("Exception executing addHistoryRecord (%s) with status code of %d, reason: %s",
                userId, status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }


    /**
     * Add history with label
     *
     * @param userId
     * @param nodeId
     * @param nodeName
     * @param nodeType
     * @param parentInfo
     * @param toolTip
     * @param app_type
     * @param nodeLabel
     * @return
     * @throws CollageRestException
     */
    public javax.ws.rs.core.Response.Status addHistoryRecord(String userId,
                                           int nodeId,
                                           String nodeName,
                                           String nodeType,
                                           String parentInfo,
                                           String toolTip,
                                           String app_type,
                                           String nodeLabel) throws CollageRestException {
        javax.ws.rs.core.Response.Status status = javax.ws.rs.core.Response.Status.SERVICE_UNAVAILABLE;
        String[] names = {"userId", "nodeId", "nodeName", "nodeType", "parentInfo", "toolTip", "app_type", "nodeLabel"};
        String[] values = {userId, String.valueOf(nodeId), nodeName, nodeType, parentInfo, toolTip, app_type, nodeLabel};
        ClientResponse response = null;
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                String url = buildUrlWithPathAndQueryParams(API_ROOT, "addwithlabel", buildEncodedQueryParams(names, values));
                ClientRequest request = createClientRequest(url);
                request.accept(mediaType);
                response = request.get();
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
        throw new CollageRestException(String.format("Exception executing addHistoryRecord (%s) with status code of %d, reason: %s",
                userId, status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }


    /**
     * Update without label
     *
     * @param userId
     * @param nodeId
     * @param nodeName
     * @param nodeType
     * @param app_type
     * @return
     * @throws CollageRestException
     */
    public javax.ws.rs.core.Response.Status updateHistoryRecord(String userId,
                                              int nodeId,
                                              String nodeName,
                                              String nodeType,

                                              String app_type
    ) throws CollageRestException {
        javax.ws.rs.core.Response.Status status = javax.ws.rs.core.Response.Status.SERVICE_UNAVAILABLE;
        String[] names = {"userId", "nodeId", "nodeName", "nodeType", "app_type"};
        String[] values = {userId, String.valueOf(nodeId), nodeName, nodeType, app_type};
        ClientResponse response = null;
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                String url = buildUrlWithPathAndQueryParams(API_ROOT, "updatewithoutlabel", buildEncodedQueryParams(names, values));
                ClientRequest request = createClientRequest(url);
                request.accept(mediaType);
                response = request.get();
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
        throw new CollageRestException(String.format("Exception executing updateHistoryRecords (%s) with status code of %d, reason: %s",
                userId, status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }

    /**
     * Update without label
     *
     * @param userId
     * @param nodeId
     * @param nodeName
     * @param nodeType
     * @param app_type
     * @return
     * @throws CollageRestException
     */
    public javax.ws.rs.core.Response.Status updateHistoryRecord(String userId,
                                              int nodeId,
                                              String nodeName,
                                              String nodeType,
                                              String app_type,
                                              String tabHistory,
                                              String nodeLabel
    ) throws CollageRestException {
        javax.ws.rs.core.Response.Status status = javax.ws.rs.core.Response.Status.SERVICE_UNAVAILABLE;
        String[] names = {"userId", "nodeId", "nodeName", "nodeType", "app_type", "tabHistory", "nodeLabel"};
        String[] values = {userId, String.valueOf(nodeId), nodeName, nodeType, app_type, tabHistory, nodeLabel};
        ClientResponse response = null;
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                String url = buildUrlWithPathAndQueryParams(API_ROOT, "updatewithlabel", buildEncodedQueryParams(names, values));
                ClientRequest request = createClientRequest(url);
                request.accept(mediaType);
                response = request.get();
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
        throw new CollageRestException(String.format("Exception executing lookup updateHistoryRecord (%s) with status code of %d, reason: %s",
                userId, status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }

    /**
     * Update the label
     *
     * @param userId
     * @param nodeId
     * @param app_type
     * @param nodeLabel
     * @return
     * @throws CollageRestException
     */
    public javax.ws.rs.core.Response.Status updateNodeLabelRecord(String userId,
                                                int nodeId,
                                                String app_type,
                                                String nodeLabel

    ) throws CollageRestException {
        javax.ws.rs.core.Response.Status status = javax.ws.rs.core.Response.Status.SERVICE_UNAVAILABLE;
        String[] names = {"userId", "nodeId", "app_type", "nodeLabel"};
        String[] values = {userId, String.valueOf(nodeId), app_type, nodeLabel};
        ClientResponse response = null;
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                String url = buildUrlWithPathAndQueryParams(API_ROOT, "updatenodelabel", buildEncodedQueryParams(names, values));
                ClientRequest request = createClientRequest(url);
                request.accept(mediaType);
                response = request.get();
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
        throw new CollageRestException(String.format("Exception executing lookup updateHistoryRecord (%s) with status code of %d, reason: %s",
                userId, status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }

    /**
     * Gets the max node id
     *
     * @param userId
     * @param app_type
     * @return
     * @throws CollageRestException
     */
    public int getMaxNodeID(String userId,
                            String app_type) throws CollageRestException {
        javax.ws.rs.core.Response.Status status = javax.ws.rs.core.Response.Status.SERVICE_UNAVAILABLE;
        String[] names = {"userId", "app_type"};
        String[] values = {userId, app_type};
        ClientResponse<Integer> response = null;
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                String url = buildUrlWithPathAndQueryParams(API_ROOT, "getmaxnodeid", buildEncodedQueryParams(names, values));
                ClientRequest request = createClientRequest(url);
                request.accept(mediaType);
                response = request.get(Integer.class);
                if (response.getResponseStatus() == javax.ws.rs.core.Response.Status.OK) {
                    return response.getEntity();
                } else if (response.getResponseStatus() == javax.ws.rs.core.Response.Status.NOT_FOUND) {
                    return -1;
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
        throw new CollageRestException(String.format("Exception executing lookup updateHistoryRecord (%s) with status code of %d, reason: %s",
                userId, status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }

    /**
     * Deletes history
     * @param userId
     * @param nodeId
     * @param app_type
     * @return
     * @throws CollageRestException
     */
    public javax.ws.rs.core.Response.Status deleteHistoryRecord(String userId,
                                              int nodeId,
                                              String app_type

    ) throws CollageRestException {
        javax.ws.rs.core.Response.Status status = javax.ws.rs.core.Response.Status.SERVICE_UNAVAILABLE;
        String[] names = {"userId", "nodeId", "app_type"};
        String[] values = {userId, String.valueOf(nodeId), app_type};
        ClientResponse response = null;
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                String url = buildUrlWithPathAndQueryParams(API_ROOT, "deletenode", buildEncodedQueryParams(names, values));
                ClientRequest request = createClientRequest(url);
                request.accept(mediaType);
                response = request.get();
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
        throw new CollageRestException(String.format("Exception executing lookup updateHistoryRecord (%s) with status code of %d, reason: %s",
                userId, status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }

    /**
     * Deletes the history
     * @param userId
     * @param app_type
     * @return
     * @throws CollageRestException
     */
    public javax.ws.rs.core.Response.Status deleteAllHistoryRecords(String userId,
                                                  String app_type
    ) throws CollageRestException {
        javax.ws.rs.core.Response.Status status = javax.ws.rs.core.Response.Status.SERVICE_UNAVAILABLE;
        String[] names = {"userId", "app_type"};
        String[] values = {userId, app_type};
        ClientResponse response = null;
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                String url = buildUrlWithPathAndQueryParams(API_ROOT, "deleteall", buildEncodedQueryParams(names, values));
                ClientRequest request = createClientRequest(url);
                request.accept(mediaType);
                response = request.get();
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
        throw new CollageRestException(String.format("Exception executing lookup updateHistoryRecord (%s) with status code of %d, reason: %s",
                userId, status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }


    /**
     * Delete history
     * @param userId
     * @param nodeId
     * @param nodeType
     * @param app_type
     * @return
     * @throws CollageRestException
     */
    public javax.ws.rs.core.Response.Status deleteHistoryRecord(String userId,
                                              int nodeId,
                                              String nodeType,
                                              String app_type) throws CollageRestException {
        javax.ws.rs.core.Response.Status status = javax.ws.rs.core.Response.Status.SERVICE_UNAVAILABLE;
        String[] names = {"userId", "nodeId", "nodeType", "app_type"};
        String[] values = {userId, String.valueOf(nodeId), nodeType, app_type};
        ClientResponse response = null;
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                String url = buildUrlWithPathAndQueryParams(API_ROOT, "deletenodewithtype", buildEncodedQueryParams(names, values));
                ClientRequest request = createClientRequest(url);
                request.accept(mediaType);
                response = request.get();
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
        throw new CollageRestException(String.format("Exception executing lookup updateHistoryRecord (%s) with status code of %d, reason: %s",
                userId, status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }

    /**
     * Updates the tab history
     * @param userId
     * @param nodeId
     * @param app_type
     * @param tabHistory
     * @return
     * @throws CollageRestException
     */
    public javax.ws.rs.core.Response.Status updateTabHistoryRecord(String userId,
                                                 int nodeId,
                                                 String app_type,
                                                 String tabHistory) throws CollageRestException {
        javax.ws.rs.core.Response.Status status = javax.ws.rs.core.Response.Status.SERVICE_UNAVAILABLE;
        String[] names = {"userId", "nodeId", "app_type", "tabHistory"};
        String[] values = {userId, String.valueOf(nodeId), app_type, tabHistory};
        ClientResponse response = null;
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                String url = buildUrlWithPathAndQueryParams(API_ROOT, "updatetabhistoryfield", buildEncodedQueryParams(names, values));
                ClientRequest request = createClientRequest(url);
                request.accept(mediaType);
                response = request.get(new GenericType<ExtendedRoleList>() {
                });
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
        throw new CollageRestException(String.format("Exception executing lookup updateHistoryRecord (%s) with status code of %d, reason: %s",
                userId, status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }

}
