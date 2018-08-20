/*
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2014  GroundWork Open Source Solutions info@groundworkopensource.com

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
import org.groundwork.rs.dto.DtoAuditLog;
import org.groundwork.rs.dto.DtoAuditLogList;
import org.groundwork.rs.dto.DtoOperationResults;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.util.GenericType;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

/**
 * AuditLogClient
 *
 * The Java REST Client for performing audit query and logging operations
 * the Groundwork enterprise foundation server.
 * <p>
 * Operations supported are of the categories:
 * <ul>
 *     <li>list operations - list audit logs associated with a host/service and optional paging parameters</li>
 *     <li>query operations - query for audit logs using an object query language with optional paging parameters</li>
 *     <li>post operations - batch operation to add audit logs. Works with lists of one or more.</li>
 * </ul>
 * Because the number of AuditLogs returnable by the list and query operations is unbounded, use of paging parameters
 * is required by these operations.
 * The results for post operations return {@link org.groundwork.rs.dto.DtoOperationResults} holding the result of the
 * operation, (success or failure counts for synchronous invocations or a {@link org.groundwork.rs.dto.DtoOperationResult}
 * that contains the status of an asynchronous create task). Individual AuditLog instances are not
 * addressable by resource URI, so this information is not included in the return. AuditLogs must be listed or
 * queried instead.
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public class AuditLogClient extends BaseRestClient {

    private static Log log = LogFactory.getLog(AuditLogClient.class);

    private static final String API_ROOT_SINGLE = "/auditlogs";
    private static final String API_ROOT = API_ROOT_SINGLE + "/";

    private static final int MAX_COUNT = 1024;

    /**
     * Create a AuditLog REST Client for performing audit query and logging operations
     * the Groundwork enterprise foundation server.
     * <p></p>
     * Deployment URL example:
     * <pre>http://server-name[:port]/foundation-webapp/api</pre>
     * <p></p>
     * @param deploymentUrl the deployment root URL for all Rest operations
     */
    public AuditLogClient(String deploymentUrl) {
        super(deploymentUrl);
    }

    /**
     * Create a AuditLog REST Client for performing audit query and logging operations
     * the Groundwork enterprise foundation server.
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
    public AuditLogClient(String deploymentUrl, MediaType mediaType) {
        this(deploymentUrl);
        this.mediaType = mediaType;
    }

    /**
     * List all AuditLogs returning a list of AuditLog transfer objects. Because the number of AuditLogs returnable
     * is unbounded, use of paging parameters is required.
     * <p>Paging parameters <code>first</code> and <code>count</code> are used together to retrieve a subset of
     * entities, paging from the <code>first</code> record, for a <code>count</code> of n records.</p>
     * <p>AuditLogs are returned in the reverse order of their creation, sorted first by descending timestamp and then
     * descending id order.</p>
     *
     * @param first the zero-based first record to retrieve to page over result set
     * @param count the number of records to retrieve offset from the first parameter
     * @return a list of one or more {@link org.groundwork.rs.dto.DtoAuditLog} objects matching the query. If no records match
     * the query, then an empty list is returned
     * @throws CollageRestException
     */
    public List<DtoAuditLog> list(int first, int count) throws CollageRestException {
        return list(null, first, count);
    }

    /**
     * List AuditLogs with matching host name string returning a list of AuditLog transfer objects.
     * Because the number of AuditLogs returnable is unbounded, use of paging parameters is required.
     * <p>Paging parameters <code>first</code> and <code>count</code> are used together to retrieve a subset of
     * entities, paging from the <code>first</code> record, for a <code>count</code> of n records.</p>
     * <p>AuditLogs are returned in the reverse order of their creation, sorted first by descending timestamp and then
     * descending id order.</p>
     *
     * @param hostName unique host name string to match or null for 'all'. This string should not be encoded, as the Rest Client
     *                 will convert it for you
     * @param first the zero-based first record to retrieve to page over result set
     * @param count the number of records to retrieve offset from the first parameter
     * @return a list of one or more {@link org.groundwork.rs.dto.DtoAuditLog} objects matching the query. If no records match
     * the query, then an empty list is returned
     * @throws CollageRestException
     */
    public List<DtoAuditLog> list(String hostName, int first, int count) throws CollageRestException {
        return list(hostName, null, first, count);
    }

    /**
     * List AuditLogs with matching host name and service description strings returning a list of AuditLog transfer objects.
     * Because the number of AuditLogs returnable is unbounded, use of paging parameters is required.
     * <p>Paging parameters <code>first</code> and <code>count</code> are used together to retrieve a subset of
     * entities, paging from the <code>first</code> record, for a <code>count</code> of n records.</p>
     * <p>AuditLogs are returned in the reverse order of their creation, sorted first by descending timestamp and then
     * descending id order.</p>
     *
     * @param hostName unique host name string to match or null for 'all'. This string should not be encoded, as the Rest Client
     *                 will convert it for you
     * @param serviceDescription unique service description string to match or null for 'all'. This string should not be encoded,
     *                           as the Rest Client will convert it for you
     * @param first the zero-based first record to retrieve to page over result set
     * @param count the number of records to retrieve offset from the first parameter
     * @return a list of one or more {@link org.groundwork.rs.dto.DtoAuditLog} objects matching the query. If no records match
     * the query, then an empty list is returned
     * @throws CollageRestException
     */
    public List<DtoAuditLog> list(String hostName, String serviceDescription, int first, int count) throws CollageRestException {
        Response.Status status = Response.Status.SERVICE_UNAVAILABLE;
        ClientResponse<DtoAuditLogList> response = null;
        if ((first >= 0) && (count > 0) && (count <= MAX_COUNT)) {
            try {
                for (int retry = 0; retry < RETRIES; retry++) {
                    String encodedQueryParams = buildEncodedQueryParams(new String[]{PARAM_START_NAME, PARAM_COUNT_NAME}, new String[]{Integer.toString(first), Integer.toString(count)});
                    String url = null;
                    if ((hostName != null) && (hostName.length() > 0)) {
                        if ((serviceDescription != null) && (serviceDescription.length() > 0)) {
                            url = buildUrlWithPathsAndQueryParams(API_ROOT, hostName, serviceDescription, encodedQueryParams);
                        } else {
                            url = buildUrlWithPathAndQueryParams(API_ROOT, hostName, encodedQueryParams);
                        }
                    } else {
                        url = buildUrlWithQueryParams(API_ROOT, encodedQueryParams);
                    }
                    ClientRequest request = createClientRequest(url);
                    request.accept(mediaType);
                    response = request.get(new GenericType<DtoAuditLogList>(){});
                    if (response.getResponseStatus() == Response.Status.OK) {
                        return response.getEntity(new GenericType<DtoAuditLogList>(){}).getAuditLogs();
                    } else if (response.getResponseStatus() == Response.Status.NOT_FOUND) {
                        return new ArrayList<DtoAuditLog>();
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
        } else {
            status = Response.Status.BAD_REQUEST;
        }
        if (status == null) {
            status = Response.Status.SERVICE_UNAVAILABLE;
        }
        throw new CollageRestException(String.format("Exception executing list auditlogs host name %s and service description %s with status code of %d, reason: %s", hostName, serviceDescription, status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }

    /**
     * Query for AuditLogs with an HQL query string returning a list of AuditLog transfer objects.
     * Queries are only valid against the model represented by the AuditLog data transfer object {@link org.groundwork.rs.dto.DtoAuditLog}.
     * Queries are read only operations and are limited to where and order by HQL expressions.
     * Because the number of AuditLogs returnable is unbounded, use of paging parameters is required.
     * <p>The names of the fields queried should match the names in the {@link org.groundwork.rs.dto.DtoAuditLog} model.
     * Any public field can be queried using standard Java Beans naming convention for example a public method named
     * <code>getHostName()</code> would be queried as <code>hostName</code>. The values of String fields should be single quoted.
     * <p>Paging parameters <code>first</code> and <code>count</code> are used together to retrieve a subset of
     * entities, paging from the <code>first</code> record, for a <code>count</code> of n records.</p>
     * <p>AuditLogs are returned in the order specified by the query order by expression if specified, otherwise the order
     * is undefined.</p>
     *
     * @param query the HQL query string. This string should not be encoded, as the Rest Client will convert it for you
     * @param first the zero-based first record to retrieve to page over result set
     * @param count the number of records to retrieve offset from the first parameter
     * @return a list of one or more {@link org.groundwork.rs.dto.DtoAuditLog} objects matching the query. If no records match the query,
     *         then an empty list is returned
     * @throws CollageRestException
     */
    public List<DtoAuditLog> query(String query, int first, int count) throws CollageRestException {
        Response.Status status = Response.Status.SERVICE_UNAVAILABLE;
        if ((first >= 0) && (count > 0) && (count <= MAX_COUNT)) {
            ClientResponse<DtoAuditLogList> response = null;
            try {
                for (int retry = 0; retry < RETRIES; retry++) {
                    String url = buildEncodedQuery(API_ROOT_SINGLE, query, first, count);
                    ClientRequest request = createClientRequest(url);
                    request.accept(mediaType);
                    response = request.get(new GenericType<DtoAuditLogList>(){});
                    if (response.getResponseStatus() == Response.Status.OK) {
                        return response.getEntity(new GenericType<DtoAuditLogList>(){}).getAuditLogs();
                    } else if (response.getResponseStatus() == Response.Status.NOT_FOUND) {
                        return new ArrayList<DtoAuditLog>();
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
        } else {
            status = Response.Status.BAD_REQUEST;
        }
        if (status == null) {
            status = Response.Status.SERVICE_UNAVAILABLE;
        }
        throw new CollageRestException(String.format("Exception executing query auditlogs (%s) with status code of %d, reason: %s", query, status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }

    /**
     * Create and insert AuditLogs asynchronously. {@link org.groundwork.rs.dto.DtoAuditLogList} is a list of one or
     * more {@link org.groundwork.rs.dto.DtoAuditLog} objects. Update operations are not allowed, so
     * each of these objects are used to create new AuditLog instances. Timestamps for the new instances
     * are set on the server.
     * <p>This operation returns a {@link org.groundwork.rs.dto.DtoOperationResults} holding the result of the operation,
     * (a {@link org.groundwork.rs.dto.DtoOperationResult} that contains the invocation status of an asynchronous create
     * task).</p>
     *
     * @param auditLogs a list of one or more {@link org.groundwork.rs.dto.DtoAuditLog} objects to create and insert.
     * @return a {@link org.groundwork.rs.dto.DtoOperationResults} holding a {@link org.groundwork.rs.dto.DtoOperationResult}
     * with the invocation status of a background create task
     * @throws CollageRestException
     */
    public DtoOperationResults post(DtoAuditLogList auditLogs) throws CollageRestException {
        return post(auditLogs, true);
    }

    /**
     * Create and insert AuditLogs. {@link org.groundwork.rs.dto.DtoAuditLogList} is a list of one or
     * more {@link org.groundwork.rs.dto.DtoAuditLog} objects. Update operations are not allowed, so
     * each of these objects are used to create new AuditLog instances. Timestamps for the new instances
     * are set on the server.
     * <p>This operation returns a {@link org.groundwork.rs.dto.DtoOperationResults} holding the result of the operation,
     * (success or failure counts for synchronous invocations or a {@link org.groundwork.rs.dto.DtoOperationResult}
     * that contains the invocation status of an asynchronous create task). Individual AuditLog instances are not
     * addressable by resource URI, so this information is not included in the return. AuditLogs must be listed or
     * queried instead.</p>
     *
     * @param auditLogs a list of one or more {@link org.groundwork.rs.dto.DtoAuditLog} objects to create and insert.
     * @param async should this post operation be synchronous or asynchronous
     * @return a {@link org.groundwork.rs.dto.DtoOperationResults} holding success and failure counts or a
     * {@link org.groundwork.rs.dto.DtoOperationResult} with the invocation status of a background create task
     * @throws CollageRestException
     */
    public DtoOperationResults post(DtoAuditLogList auditLogs, boolean async) throws CollageRestException {
        Response.Status status = Response.Status.SERVICE_UNAVAILABLE;
        ClientResponse<DtoOperationResults> response = null;
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                String url = (!async ? buildUrlWithQueryParams(API_ROOT_SINGLE, buildEncodedQueryParams(ASYNC_NAMES, NOT_ASYNC_VALUES)) : build(API_ROOT_SINGLE));
                ClientRequest request = createClientRequest(url);
                request.accept(mediaType);
                request.body(mediaType, auditLogs);
                response = request.post();
                if (response.getResponseStatus() == Response.Status.OK) {
                    return response.getEntity(DtoOperationResults.class);
                } else if (response.getStatus() == TOO_MANY_REQUESTS) {
                    throw new CollageRestException(response.getEntity(String.class), response.getStatus());
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
        } catch (CollageRestException cre) {
            throw cre;
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
        throw new CollageRestException(String.format("Exception executing post to auditlogs with status code of %d, reason: %s", status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }
}
