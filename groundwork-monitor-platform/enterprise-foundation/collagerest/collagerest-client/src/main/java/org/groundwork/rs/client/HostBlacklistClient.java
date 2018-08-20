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

import com.groundwork.collage.util.RegexList;
import com.groundwork.collage.util.RegexListListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.rs.dto.DtoHostBlacklist;
import org.groundwork.rs.dto.DtoHostBlacklistList;
import org.groundwork.rs.dto.DtoOperationResults;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.util.GenericType;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * HostBlacklistClient
 *
 * The Java REST Client for performing query and administrative operations
 * on HostBlacklists in the Groundwork enterprise foundation server.
 * <p>
 * Operations supported are of the categories:
 * <ul>
 *     <li>lookup operations - lookup host blacklists by host name</li>
 *     <li>list operations - list all host blacklists in the system with optional paging parameters</li>
 *     <li>query operations - query for host blacklists using an object query language with optional paging parameters</li>
 *     <li>post operations - administrative batch operations to add or update host blacklists. Works with lists of one or more host blacklists</li>
 *     <li>delete operations - administrative batch operations to delete host blacklists. Works with lists of one or more host blacklists</li>
 * </ul>
 * <p>
 * Note that post and delete operations are not transactional. If a list of 10 host blacklists are passed in to be added,
 * and if, for example, two host blacklists fail to update, the other eight host blacklists will still be persisted. The
 * results for all post and delete operations return the same {@link org.groundwork.rs.dto.DtoOperationResults} list of
 * {@link org.groundwork.rs.dto.DtoOperationResult}, holding the result (success, failure, warning) of each sub-operation.
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public class HostBlacklistClient extends BaseRestClient implements RegexListListener {

    private static Log log = LogFactory.getLog(HostBlacklistClient.class);

    private static final String API_ROOT_SINGLE = "/hostblacklists";
    private static final String API_ROOT = API_ROOT_SINGLE + "/";

    private static final int MAX_COUNT = 1024;

    /** RegexList blacklist host names timeout */
    private static final long BLACKLIST_REGEXLIST_TIMEOUT = 300000;

    /** RegexList used to match blacklist host names */
    private RegexList blacklistRegexList;

    /**
     * Create a HostBlacklist REST Client for performing query and administrative operations
     * on host blacklists in the Groundwork enterprise foundation server.
     * <p></p>
     * Deployment URL example:
     * <pre>http://server-name[:port]/foundation-webapp/api</pre>
     * <p></p>
     * @param deploymentUrl the deployment root URL for all Rest operations
     */
    public HostBlacklistClient(String deploymentUrl) {
        super(deploymentUrl);
    }

    /**
     * Create a HostBlacklist REST Client for performing query and administrative operations
     * on host blacklists in the Groundwork enterprise foundation server.
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
    public HostBlacklistClient(String deploymentUrl, MediaType mediaType) {
        this(deploymentUrl);
        this.mediaType = mediaType;
    }

    /**
     * Lookup a single HostBlacklist by its host names returning a
     * HostBlacklist transfer object.
     *
     * @param hostName the host name of the HostBlacklist to lookup
     * @return the returned HostBlacklist transfer object or null if not found
     * @throws CollageRestException
     */
    public DtoHostBlacklist lookup(String hostName) throws CollageRestException {
        Response.Status status = Response.Status.SERVICE_UNAVAILABLE;
        ClientResponse<DtoHostBlacklist> response = null;
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                String url = buildUrlWithPath(API_ROOT, encode(hostName));
                ClientRequest request = createClientRequest(url);
                request.accept(mediaType);
                response = request.get(new GenericType<DtoHostBlacklist>(){});
                if (response.getResponseStatus() == Response.Status.OK) {
                    return response.getEntity(new GenericType<DtoHostBlacklist>() {});
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
        throw new CollageRestException(String.format("Exception executing lookup hostblacklists (%s) with status code of %d, reason: %s", hostName, status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }

    /**
     * Match host name against blacklist host names taken as case-insensitive
     * match patterns. Pattern matches are made against a locally cached set
     * of host names. Periodic refreshes are made to keep the cache current.
     *
     * @param hostName host name to match
     * @return match result
     */
    public boolean matchHostNameAgainstHostNames(String hostName) {
        // safely allocate blacklist RegexList if necessary
        if (blacklistRegexList == null) {
            synchronized (this) {
                if (blacklistRegexList == null) {
                    blacklistRegexList = new RegexList(this, true, BLACKLIST_REGEXLIST_TIMEOUT);
                }
            }
        }
        // match host name against blacklist RegexList
        return blacklistRegexList.match(hostName);
    }

    /**
     * Return a list of all HostBlacklists.
     *
     * @return a list of all host blacklists or an empty list.
     * @throws CollageRestException
     */
    public List<DtoHostBlacklist> list() throws CollageRestException {
        return list(-1, -1);
    }

    /**
     * Return a list of all HostBlacklists.
     * <p>Paging parameters <code>first</code> and <code>count</code> are used together to retrieve a subset of
     * entities, paging from the <code>first</code> record, for a <code>count</code> of n records.</p>
     *
     * @param first the zero-based first record to retrieve to page over result set. Set to -1 to ignore this parameter
     * @param count the number of records to retrieve offset from the first parameter. Set to -1 to ignore this parameter
     * @return a list of all host blacklists or an empty list.
     * @throws CollageRestException
     */
    public List<DtoHostBlacklist> list(int first, int count) throws CollageRestException {
        return query(null, first, count);
    }

    /**
     * Query for HostBlacklists with an HQL query string returning a list of HostBlacklist transfer objects.
     * Queries are only valid against the model represented by the HostBlacklist data transfer object {@link org.groundwork.rs.dto.DtoHostBlacklist}.
     * Queries are read only operations and are limited to where and order by HQL expressions.
     * <p>The names of the fields queried should match the names in the {@link org.groundwork.rs.dto.DtoHostBlacklist} model.
     * Any public field can be queried using standard Java Beans naming convention for example a public method named
     * <code>getHostName()</code> would be queried as <code>hostName</code>. The values of String fields should be single quoted.
     * <p>HostBlacklists are returned in the order specified by the query order by expression if specified, otherwise the order
     * is undefined.</p>
     *
     * @param query the HQL query string. This string should not be encoded, as the Rest Client will convert it for you
     * @return a list of one or more {@link org.groundwork.rs.dto.DtoHostBlacklist} objects matching the query. If no records match the query,
     *         then an empty list is returned
     * @throws CollageRestException
     */
    public List<DtoHostBlacklist> query(String query) throws CollageRestException {
        return query(query, -1, -1);
    }

    /**
     * Query for HostBlacklists with an HQL query string returning a list of HostBlacklist transfer objects.
     * Queries are only valid against the model represented by the HostBlacklist data transfer object {@link org.groundwork.rs.dto.DtoHostBlacklist}.
     * Queries are read only operations and are limited to where and order by HQL expressions.
     * <p>The names of the fields queried should match the names in the {@link org.groundwork.rs.dto.DtoHostBlacklist} model.
     * Any public field can be queried using standard Java Beans naming convention for example a public method named
     * <code>getHostName()</code> would be queried as <code>hostName</code>. The values of String fields should be single quoted.
     * <p>Paging parameters <code>first</code> and <code>count</code> are used together to retrieve a subset of
     * entities, paging from the <code>first</code> record, for a <code>count</code> of n records.</p>
     * <p>HostBlacklists are returned in the order specified by the query order by expression if specified, otherwise the order
     * is undefined.</p>
     *
     * @param query the HQL query string. This string should not be encoded, as the Rest Client will convert it for you
     * @param first the zero-based first record to retrieve to page over result set. Set to -1 to ignore this parameter
     * @param count the number of records to retrieve offset from the first parameter. Set to -1 to ignore this parameter
     * @return a list of one or more {@link org.groundwork.rs.dto.DtoHostBlacklist} objects matching the query. If no records match the query,
     *         then an empty list is returned
     * @throws CollageRestException
     */
    public List<DtoHostBlacklist> query(String query, int first, int count) throws CollageRestException {
        Response.Status status = Response.Status.SERVICE_UNAVAILABLE;
        ClientResponse<DtoHostBlacklistList> response = null;
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                String url = buildEncodedQuery(API_ROOT_SINGLE, query, first, count);
                ClientRequest request = createClientRequest(url);
                request.accept(mediaType);
                response = request.get(new GenericType<DtoHostBlacklistList>(){});
                if (response.getResponseStatus() == Response.Status.OK) {
                    return response.getEntity(new GenericType<DtoHostBlacklistList>(){}).getHostBlacklists();
                } else if (response.getResponseStatus() == Response.Status.NOT_FOUND) {
                    return new ArrayList<DtoHostBlacklist>();
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
        throw new CollageRestException(String.format("Exception executing query hostblacklists (%s) with status code of %d, reason: %s", query, status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }

    /**
     * Administrative batch operation to add or update HostBlacklists. {@link org.groundwork.rs.dto.DtoHostBlacklistList}
     * is a list of one or more {@link org.groundwork.rs.dto.DtoHostBlacklist} objects. Each of these objects represent either a new
     * host blacklist or a host blacklist to be updated. Any field that needs to be updated or added should be set on the DtoHostBlacklist
     * object. The web service will determine if an update or insert is required by looking up the  primary keys (the
     * <code>hostBlacklistId</code> and/or <code>hostName</code> fields), from the provided DtoHostBlacklist objects.
     * <p>The post operation is not transactional. If a list of ten host blacklists are passed in to be added, and
     * if, for example, two fail to update, the other eight will still be persisted. The results for all post operations return
     * a {@link DtoOperationResults} list of {@link org.groundwork.rs.dto.DtoOperationResult}, holding the status (success,
     * failure, warning) of each sub-operation, see {@link org.groundwork.rs.dto.DtoOperationResult#getStatus()}.
     * The {@link org.groundwork.rs.dto.DtoOperationResult#getLocation()} method provides the exact URL of the persisted host
     * blacklist.</p>
     *
     * @param hostBlacklists a list of one or more {@link org.groundwork.rs.dto.DtoHostBlacklist} objects to update or insert
     *                       based on the existence of the <code>id</code> or <code>hostName</code> primary keys.
     * @return a {@link org.groundwork.rs.dto.DtoOperationResults} holding the status of each host blacklist operation
     * @throws CollageRestException
     */
    public DtoOperationResults post(DtoHostBlacklistList hostBlacklists) throws CollageRestException {
        Response.Status status = Response.Status.SERVICE_UNAVAILABLE;
        ClientResponse<DtoOperationResults> response = null;
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                String url = build(API_ROOT_SINGLE);
                ClientRequest request = createClientRequest(url);
                request.accept(mediaType);
                request.body(mediaType, hostBlacklists);
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
        throw new CollageRestException(String.format("Exception executing post to hostblacklists with status code of %d, reason: %s", status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }

    /**
     * Administrative operation to delete a single HostBlacklist. Takes a host name
     * as the primary key.
     *
     * @param hostName the host name of the HostBlacklist to delete
     * @return a {@link DtoOperationResults} set of {@link org.groundwork.rs.dto.DtoOperationResult},
     *              holding the status of each host blacklist operation
     * @throws CollageRestException
     */
    public DtoOperationResults delete(String hostName) throws CollageRestException {
        return delete(Arrays.asList(new String[]{hostName}));
    }

    /**
     * Administrative batch operation to delete multiple HostBlacklists specified by their
     * primary key host names.
     * <p>The delete operation is not transactional. If a list of five host blacklists are passed
     * in to be deleted, and if, for example, two fail to delete, the other three will still be
     * deleted. The results for all delete operations return a {@link DtoOperationResults} list of
     * {@link org.groundwork.rs.dto.DtoOperationResult}, holding the status (success, failure,
     * warning) of each sub-operation, see {@link org.groundwork.rs.dto.DtoOperationResult#getStatus()}.</p>
     *
     * @param hostNamesList a list of host names of the HostBlacklists to delete
     * @return a {@link DtoOperationResults} set of {@link org.groundwork.rs.dto.DtoOperationResult},
     *          holding the status of each host blacklist operation
     * @throws CollageRestException
     */
    public DtoOperationResults delete(List<String> hostNamesList) throws CollageRestException {
        Response.Status status = Response.Status.SERVICE_UNAVAILABLE;
        ClientResponse<DtoOperationResults> response = null;
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                String hostNames = makeCommaSeparatedParamFromList(hostNamesList);
                String url = buildUrlWithPath(API_ROOT, encode(hostNames));
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
        throw new CollageRestException(String.format("Exception executing delete hostblacklists (%s) with status code of %d, reason: %s", hostNamesList, status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }

    /**
     * Administrative batch operation to delete a list of HostBlacklists. {@link org.groundwork.rs.dto.DtoHostBlacklistList}
     * is a list of one or more {@link org.groundwork.rs.dto.DtoHostBlacklist} objects. The <code>hostBlacklistId</code>
     * and <code>hostName</code> members of these objects are used to select these HostBlacklists to delete.
     * <p>The delete operation is not transactional. If a list of five host blacklists are passed
     * in to be deleted, and if, for example, two fail to delete, the other three will still be
     * deleted. The results for all delete operations return a {@link DtoOperationResults} list of
     * {@link org.groundwork.rs.dto.DtoOperationResult}, holding the status (success, failure,
     * warning) of each sub-operation, see {@link org.groundwork.rs.dto.DtoOperationResult#getStatus()}.</p>
     *
     * @param dtoHostBlacklists a list of one or more {@link org.groundwork.rs.dto.DtoHostBlacklist} objects to delete.
     * @return a {@link DtoOperationResults} set of {@link org.groundwork.rs.dto.DtoOperationResult},
     *          holding the status of each host blacklist operation
     * @throws CollageRestException
     */
    public DtoOperationResults delete(DtoHostBlacklistList dtoHostBlacklists) throws CollageRestException {
        Response.Status status = Response.Status.SERVICE_UNAVAILABLE;
        ClientResponse<DtoOperationResults> response = null;
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                String url = build(API_ROOT_SINGLE);
                ClientRequest request = createClientRequest(url);
                request.accept(mediaType);
                request.body(mediaType, dtoHostBlacklists);
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
        throw new CollageRestException(String.format("Exception executing delete hostblacklists with status code of %d, reason: %s", status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }

    @Override
    public List<Object> getPatterns(boolean caseInsensitive) {
        // return list of blacklist pattern strings
        List<Object> patterns = new ArrayList<Object>();
        for (DtoHostBlacklist blacklist : list()) {
            patterns.add(blacklist.getHostName());
        }
        return patterns;
    }

    @Override
    public void exception(Exception e) {
        log.error("Error getting blacklist RegexList patterns: "+e, e);
    }
}
