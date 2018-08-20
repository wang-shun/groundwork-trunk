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
import org.groundwork.rs.dto.DtoHostIdentity;
import org.groundwork.rs.dto.DtoHostIdentityList;
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
 * HostIdentityClient
 *
 * The Java REST Client for performing query and administrative operations
 * on HostIdentities in the Groundwork enterprise foundation server.
 * <p>
 * Operations supported are of the categories:
 * <ul>
 *     <li>lookup operations - lookup host identities by id or host names</li>
 *     <li>list operations - list all host identities in the system with optional paging parameters</li>
 *     <li>query operations - query for host identities using an object query language with optional paging parameters</li>
 *     <li>post operations - administrative batch operations to add or update host identities. Works with lists of one or more host identities</li>
 *     <li>clear operations - administrative batch operations to clear host identity host names. Works with lists of one or more host identities</li>
 *     <li>delete operations - administrative batch operations to delete host identities. Works with lists of one or more host identities</li>
 * </ul>
 * <p>
 * Note that post, clear, and delete operations are not transactional. If a list of 10 host identities are passed in to be
 * added, and if, for example, two host identities fail to update, the other eight host identities will still be persisted.
 * The results for all post, clear, and delete operations return the same {@link org.groundwork.rs.dto.DtoOperationResults}
 * list of {@link org.groundwork.rs.dto.DtoOperationResult}, holding the result (success, failure, warning) of each sub-operation.
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public class HostIdentityClient extends BaseRestClient {

    private static Log log = LogFactory.getLog(HostIdentityClient.class);

    private static final String API_ROOT_SINGLE = "/hostidentities";
    private static final String API_ROOT = API_ROOT_SINGLE + "/";
    private static final String RPC_AUTOCOMPLETE = "autocomplete";

    private static final int MAX_COUNT = 1024;

    private static final String [] CLEAR_NAMES = new String[]{"clear"};
    private static final String [] CLEAR_VALUES = new String[]{"true"};

    /**
     * Create a HostIdentity REST Client for performing query and administrative operations
     * on host identities in the Groundwork enterprise foundation server.
     * <p></p>
     * Deployment URL example:
     * <pre>http://server-name[:port]/foundation-webapp/api</pre>
     * <p></p>
     * @param deploymentUrl the deployment root URL for all Rest operations
     */
    public HostIdentityClient(String deploymentUrl) {
        super(deploymentUrl);
    }

    /**
     * Create a HostIdentity REST Client for performing query and administrative operations
     * on host identities in the Groundwork enterprise foundation server.
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
    public HostIdentityClient(String deploymentUrl, MediaType mediaType) {
        this(deploymentUrl);
        this.mediaType = mediaType;
    }

    /**
     * Lookup a single HostIdentity by its primary key id or any of its host names returning a
     * HostIdentity transfer object.
     *
     * @param idOrHostName the id or host name of the HostIdentity to lookup
     * @return the returned HostIdentity transfer object or null if not found
     * @throws CollageRestException
     */
    public DtoHostIdentity lookup(String idOrHostName) throws CollageRestException {
        Response.Status status = Response.Status.SERVICE_UNAVAILABLE;
        ClientResponse<DtoHostIdentity> response = null;
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                String url = buildUrlWithPath(API_ROOT, idOrHostName);
                ClientRequest request = createClientRequest(url);
                request.accept(mediaType);
                response = request.get(new GenericType<DtoHostIdentity>(){});
                if (response.getResponseStatus() == Response.Status.OK) {
                    return response.getEntity(new GenericType<DtoHostIdentity>() {});
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
        throw new CollageRestException(String.format("Exception executing lookup hostidentities (%s) with status code of %d, reason: %s", idOrHostName, status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }

    /**
     * Return a list of all HostIdentities.
     *
     * @return a list of all host identities or an empty list.
     * @throws CollageRestException
     */
    public List<DtoHostIdentity> list() throws CollageRestException {
        return list(-1, -1);
    }

    /**
     * Return a list of all HostIdentities.
     * <p>Paging parameters <code>first</code> and <code>count</code> are used together to retrieve a subset of
     * entities, paging from the <code>first</code> record, for a <code>count</code> of n records.</p>
     *
     * @param first the zero-based first record to retrieve to page over result set. Set to -1 to ignore this parameter
     * @param count the number of records to retrieve offset from the first parameter. Set to -1 to ignore this parameter
     * @return a list of all host identities or an empty list.
     * @throws CollageRestException
     */
    public List<DtoHostIdentity> list(int first, int count) throws CollageRestException {
        return query(null, first, count);
    }

    /**
     * Query for HostIdentities with an HQL query string returning a list of HostIdentity transfer objects.
     * Queries are only valid against the model represented by the HostIdentity data transfer object {@link org.groundwork.rs.dto.DtoHostIdentity}.
     * Queries are read only operations and are limited to where and order by HQL expressions.
     * <p>The names of the fields queried should match the names in the {@link org.groundwork.rs.dto.DtoHostIdentity} model.
     * Any public field can be queried using standard Java Beans naming convention for example a public method named
     * <code>getHostName()</code> would be queried as <code>hostName</code>. The values of String fields should be single quoted.
     * <p>HostIdentities are returned in the order specified by the query order by expression if specified, otherwise the order
     * is undefined.</p>
     *
     * @param query the HQL query string. This string should not be encoded, as the Rest Client will convert it for you
     * @return a list of one or more {@link org.groundwork.rs.dto.DtoHostIdentity} objects matching the query. If no records match the query,
     *         then an empty list is returned
     * @throws CollageRestException
     */
    public List<DtoHostIdentity> query(String query) throws CollageRestException {
        return query(query, -1, -1);
    }

    /**
     * Query for HostIdentities with an HQL query string returning a list of HostIdentity transfer objects.
     * Queries are only valid against the model represented by the HostIdentity data transfer object {@link org.groundwork.rs.dto.DtoHostIdentity}.
     * Queries are read only operations and are limited to where and order by HQL expressions.
     * <p>The names of the fields queried should match the names in the {@link org.groundwork.rs.dto.DtoHostIdentity} model.
     * Any public field can be queried using standard Java Beans naming convention for example a public method named
     * <code>getHostName()</code> would be queried as <code>hostName</code>. The values of String fields should be single quoted.
     * <p>Paging parameters <code>first</code> and <code>count</code> are used together to retrieve a subset of
     * entities, paging from the <code>first</code> record, for a <code>count</code> of n records.</p>
     * <p>HostIdentities are returned in the order specified by the query order by expression if specified, otherwise the order
     * is undefined.</p>
     *
     * @param query the HQL query string. This string should not be encoded, as the Rest Client will convert it for you
     * @param first the zero-based first record to retrieve to page over result set. Set to -1 to ignore this parameter
     * @param count the number of records to retrieve offset from the first parameter. Set to -1 to ignore this parameter
     * @return a list of one or more {@link org.groundwork.rs.dto.DtoHostIdentity} objects matching the query. If no records match the query,
     *         then an empty list is returned
     * @throws CollageRestException
     */
    public List<DtoHostIdentity> query(String query, int first, int count) throws CollageRestException {
        Response.Status status = Response.Status.SERVICE_UNAVAILABLE;
        ClientResponse<DtoHostIdentityList> response = null;
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                String url = buildEncodedQuery(API_ROOT_SINGLE, query, first, count);
                ClientRequest request = createClientRequest(url);
                request.accept(mediaType);
                response = request.get(new GenericType<DtoHostIdentityList>(){});
                if (response.getResponseStatus() == Response.Status.OK) {
                    return response.getEntity(new GenericType<DtoHostIdentityList>(){}).getHostIdentities();
                } else if (response.getResponseStatus() == Response.Status.NOT_FOUND) {
                    return new ArrayList<DtoHostIdentity>();
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
        throw new CollageRestException(String.format("Exception executing query hostidentities (%s) with status code of %d, reason: %s", query, status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }

    /**
     * Administrative batch operation to add or update HostIdentities synchronously. {@link org.groundwork.rs.dto.DtoHostIdentityList}
     * is a list of one or more {@link org.groundwork.rs.dto.DtoHostIdentity} objects. Each of these objects represent either a new
     * host identity or a host identity to be updated. Any field that needs to be updated or added should be set on the DtoHostIdentity
     * object. The web service will determine if an update or insert is required by looking up the  primary keys (the
     * <code>hostIdentityId</code> and/or <code>hostName</code> fields), from the provided DtoHostIdentity objects.
     * <p>The post operation is not transactional. If a list of ten host identities are passed in to be added, and
     * if, for example, two fail to update, the other eight will still be persisted. The results for all post operations return
     * a {@link DtoOperationResults} list of {@link org.groundwork.rs.dto.DtoOperationResult}, holding the status (success,
     * failure, warning) of each sub-operation, see {@link org.groundwork.rs.dto.DtoOperationResult#getStatus()}.
     * The {@link org.groundwork.rs.dto.DtoOperationResult#getLocation()} method provides the exact URL of the persisted host
     * identity.</p>
     *
     * @param hostIdentities a list of one or more {@link org.groundwork.rs.dto.DtoHostIdentity} objects to update or insert
     *                       based on the existence of the <code>id</code> or <code>hostName</code> primary keys.
     * @return a {@link org.groundwork.rs.dto.DtoOperationResults} holding the status of each host identity operation
     * @throws CollageRestException
     */
    public DtoOperationResults post(DtoHostIdentityList hostIdentities) throws CollageRestException {
        return post(hostIdentities, false);
    }

    /**
     * Administrative batch operation to add or update HostIdentities. {@link org.groundwork.rs.dto.DtoHostIdentityList} is a
     * list of one or more {@link org.groundwork.rs.dto.DtoHostIdentity} objects. Each of these objects represent either a new
     * host identity or a host identity to be updated. Any field that needs to be updated or added should be set on the DtoHostIdentity
     * object. The web service will determine if an update or insert is required by looking up the  primary keys (the
     * <code>hostIdentityId</code> and/or <code>hostName</code> fields), from the provided DtoHostIdentity objects.
     * <p>The post operation is not transactional. If a list of ten host identities are passed in to be added, and
     * if, for example, two fail to update, the other eight will still be persisted. The results for all post operations return
     * a {@link DtoOperationResults} list of {@link org.groundwork.rs.dto.DtoOperationResult}, holding the status (success,
     * failure, warning) of each sub-operation, see {@link org.groundwork.rs.dto.DtoOperationResult#getStatus()}.
     * The {@link org.groundwork.rs.dto.DtoOperationResult#getLocation()} method provides the exact URL of the persisted host
     * identity. Asynchronous requests return a single invocation status for the background update task.</p>
     *
     * @param hostIdentities a list of one or more {@link org.groundwork.rs.dto.DtoHostIdentity} objects to update or insert
     *                       based on the existence of the <code>id</code> or <code>hostName</code> primary keys.
     * @param async should this post operation be synchronous or asynchronous
     * @return a {@link org.groundwork.rs.dto.DtoOperationResults} holding the status of each host identity operation or a
     * {@link org.groundwork.rs.dto.DtoOperationResult} with the invocation status of a background update task
     * @throws CollageRestException
     */
    public DtoOperationResults post(DtoHostIdentityList hostIdentities, boolean async) throws CollageRestException {
        Response.Status status = Response.Status.SERVICE_UNAVAILABLE;
        ClientResponse<DtoOperationResults> response = null;
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                String url = (async ? buildUrlWithQueryParams(API_ROOT_SINGLE, buildEncodedQueryParams(ASYNC_NAMES, ASYNC_VALUES)) : build(API_ROOT_SINGLE));
                ClientRequest request = createClientRequest(url);
                request.accept(mediaType);
                request.body(mediaType, hostIdentities);
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
        throw new CollageRestException(String.format("Exception executing post to hostidentities with status code of %d, reason: %s", status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }

    /**
     * Administrative operation to clear host names for a single HostIdentity. Takes a primary key
     * id or any of its host names.
     *
     * @param idOrHostName the id or host name of the HostIdentity to clear
     * @return a {@link DtoOperationResults} set of {@link org.groundwork.rs.dto.DtoOperationResult},
     *              holding the status of each Host operation
     * @throws CollageRestException
     */
    public DtoOperationResults clear(String idOrHostName) throws CollageRestException {
        return clear(Arrays.asList(new String[]{idOrHostName}));
    }

    /**
     * Administrative batch operation to clear host names of multiple HostIdentities specified by
     * their primary key ids or any of their host names.
     * <p>The clear operation is not transactional. If a list of five host identities are passed
     * in to be cleared, and if, for example, two fail to clear, the other three will still be
     * cleared. The results for all clear operations return a {@link DtoOperationResults} list of
     * {@link org.groundwork.rs.dto.DtoOperationResult}, holding the status (success, failure,
     * warning) of each sub-operation, see {@link org.groundwork.rs.dto.DtoOperationResult#getStatus()}.</p>
     *
     * @param idOrHostNamesList a list of ids or host names of the HostIdentities to clear
     * @return a {@link DtoOperationResults} set of {@link org.groundwork.rs.dto.DtoOperationResult},
     *          holding the status of each host identity operation
     * @throws CollageRestException
     */
    public DtoOperationResults clear(List<String> idOrHostNamesList) throws CollageRestException {
        return deleteOrClear(idOrHostNamesList, true);
    }

    /**
     * Administrative batch operation to clear host names for a list of HostIdentities.
     * {@link org.groundwork.rs.dto.DtoHostIdentityList} is a list of one or more
     * {@link org.groundwork.rs.dto.DtoHostIdentity} objects. The <code>hostIdentityId</code>
     * and <code>hostName</code> members of these objects are used to select these HostIdentities to clear.
     * <p>The clear operation is not transactional. If a list of five host identities are passed
     * in to be cleared, and if, for example, two fail to clear, the other three will still be
     * cleared. The results for all clear operations return a {@link DtoOperationResults} list of
     * {@link org.groundwork.rs.dto.DtoOperationResult}, holding the status (success, failure,
     * warning) of each sub-operation, see {@link org.groundwork.rs.dto.DtoOperationResult#getStatus()}.</p>
     *
     * @param dtoHostIdentities a list of one or more {@link org.groundwork.rs.dto.DtoHostIdentity} objects to clear.
     * @return a {@link DtoOperationResults} set of {@link org.groundwork.rs.dto.DtoOperationResult},
     *          holding the status of each host identity operation
     * @throws CollageRestException
     */
    public DtoOperationResults clear(DtoHostIdentityList dtoHostIdentities) throws CollageRestException {
        return deleteOrClear(dtoHostIdentities, true);
    }

    /**
     * Administrative operation to delete a single HostIdentity. Takes a primary key id or any
     * of its host names.
     *
     * @param idOrHostName the id or host name of the HostIdentity to delete
     * @return a {@link DtoOperationResults} set of {@link org.groundwork.rs.dto.DtoOperationResult},
     *              holding the status of each Host operation
     * @throws CollageRestException
     */
    public DtoOperationResults delete(String idOrHostName) throws CollageRestException {
        return delete(Arrays.asList(new String[]{idOrHostName}));
    }

    /**
     * Administrative batch operation to delete multiple HostIdentities specified by their
     * primary key ids or any of their host names.
     * <p>The delete operation is not transactional. If a list of five host identities are passed
     * in to be deleted, and if, for example, two fail to delete, the other three will still be
     * deleted. The results for all delete operations return a {@link DtoOperationResults} list of
     * {@link org.groundwork.rs.dto.DtoOperationResult}, holding the status (success, failure,
     * warning) of each sub-operation, see {@link org.groundwork.rs.dto.DtoOperationResult#getStatus()}.</p>
     *
     * @param idOrHostNamesList a list of ids or host names of the HostIdentities to delete
     * @return a {@link DtoOperationResults} set of {@link org.groundwork.rs.dto.DtoOperationResult},
     *          holding the status of each host identity operation
     * @throws CollageRestException
     */
    public DtoOperationResults delete(List<String> idOrHostNamesList) throws CollageRestException {
        return deleteOrClear(idOrHostNamesList, false);
    }

    /**
     * Administrative batch operation to delete a list of HostIdentities. {@link org.groundwork.rs.dto.DtoHostIdentityList}
     * is a list of one or more {@link org.groundwork.rs.dto.DtoHostIdentity} objects. The <code>hostIdentityId</code>
     * and <code>hostName</code> members of these objects are used to select these HostIdentities to delete.
     * <p>The delete operation is not transactional. If a list of five host identities are passed
     * in to be deleted, and if, for example, two fail to delete, the other three will still be
     * deleted. The results for all delete operations return a {@link DtoOperationResults} list of
     * {@link org.groundwork.rs.dto.DtoOperationResult}, holding the status (success, failure,
     * warning) of each sub-operation, see {@link org.groundwork.rs.dto.DtoOperationResult#getStatus()}.</p>
     *
     * @param dtoHostIdentities a list of one or more {@link org.groundwork.rs.dto.DtoHostIdentity} objects to delete.
     * @return a {@link DtoOperationResults} set of {@link org.groundwork.rs.dto.DtoOperationResult},
     *          holding the status of each host identity operation
     * @throws CollageRestException
     */
    public DtoOperationResults delete(DtoHostIdentityList dtoHostIdentities) throws CollageRestException {
        return deleteOrClear(dtoHostIdentities, false);
    }

    public DtoOperationResults deleteOrClear(List<String> idOrHostNamesList, boolean clear) throws CollageRestException {
        Response.Status status = Response.Status.SERVICE_UNAVAILABLE;
        ClientResponse<DtoOperationResults> response = null;
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                String idOrHostNames = makeCommaSeparatedParamFromList(idOrHostNamesList);
                String url = (clear ? buildUrlWithPathAndQueryParams(API_ROOT, idOrHostNames, buildEncodedQueryParams(CLEAR_NAMES, CLEAR_VALUES)) : buildUrlWithPath(API_ROOT, idOrHostNames));
                ClientRequest request = createClientRequest(url);
                request.accept(mediaType);
                request.body(mediaType, "");
                response = request.delete();
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
        throw new CollageRestException(String.format("Exception executing delete/clear hostidentities (%s) with status code of %d, reason: %s", idOrHostNamesList, status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }

    private DtoOperationResults deleteOrClear(DtoHostIdentityList dtoHostIdentities, boolean clear) throws CollageRestException {
        Response.Status status = Response.Status.SERVICE_UNAVAILABLE;
        ClientResponse<DtoOperationResults> response = null;
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                String url = (clear ? buildUrlWithQueryParams(API_ROOT_SINGLE, buildEncodedQueryParams(CLEAR_NAMES, CLEAR_VALUES)) : build(API_ROOT_SINGLE));
                ClientRequest request = createClientRequest(url);
                request.accept(mediaType);
                request.body(mediaType, dtoHostIdentities);
                response = request.delete();
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
        throw new CollageRestException(String.format("Exception executing delete/clear hostidentities with status code of %d, reason: %s", status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }

    /**
     * Lookup host/host identity name autocomplete suggestions for specified prefix.
     * A null, blank, or '*' wildcard prefix matches all names.
     *
     * @param prefix host/host identity name prefix
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
     * Lookup host/host identity name autocomplete suggestions for specified prefix.
     * If a negative limit is specified, no limit will be applied to the
     * returned suggestion strings. Autocomplete suggestions are considered
     * unique based on their canonical names. In this case, the total number
     * of suggestions returned can exceed the limit since it is limiting the
     * number of unique canonical names. A null, blank, or '*' wildcard prefix
     * matches all names.
     *
     * @param prefix host/host identity name prefix
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
