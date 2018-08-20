package org.groundwork.rs.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.rs.dto.DtoConsolidation;
import org.groundwork.rs.dto.DtoConsolidationList;
import org.groundwork.rs.dto.DtoOperationResults;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.util.GenericType;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

/**
 * The Java REST Client for performing query and administrative operations
 * on Consolidation Criterias in the Groundwork enterprise foundation server.
 * <p>
 * Operations supported are of the categories:
 * <ul>
 *     <li>lookup operations - lookup consolidation criterion by their primary key 'name'</li>
 *     <li>list operations - list all consolidation criterion in the system, with optional depth and paging parameters</li>
 *     <li>query operations - query for consolidation criterion using an object query language with optional depth and paging parameters</li>
 *     <li>post operations - administrative batch operations to add or update consolidation criterion. Works with lists of one or more consolidation criterion</li>
 *     <li>delete operations - administrative batch operations to delete consolidation criterion. Works with lists of one or more consolidation criterion</li>
 * </ul>
 * <p>
 * Note that post and delete operations are not transactional. If a list of 10 consolidation criterion are passed in to be added, and
 * if, for example, two consolidation criterion fail to update, the other eight consolidation criterion will still be persisted. The results for all
 * post and delete operations return the same {@link org.groundwork.rs.dto.DtoOperationResults} list of
 * {@link org.groundwork.rs.dto.DtoOperationResult}, holding
 * the result (success, failure, warning) of each sub-operation.
 */
public class ConsolidationClient extends BaseRestClient {

    protected static Log log = LogFactory.getLog(ConsolidationClient.class);
    private static final String API_ROOT_SINGLE = "/consolidations";
    private static final String API_ROOT = API_ROOT_SINGLE + "/";

    /**
     * Create a Consolidation REST Client for performing query and administrative operations
     * on the consolidation criterion in the Groundwork enterprise foundation database.
     * Deployment URL example:
     * <pre>http://server-name[:port]/foundation-webapp/api</pre>
     *
     * @param deploymentUrl the deployment root URL for all Rest operations
     */
    public ConsolidationClient(String deploymentUrl) {
        super(deploymentUrl);
    }

    /**
     * Create a Consolidation REST Client for performing query and administrative operations
     * on the consolidation criterion in the Groundwork enterprise foundation database.
     * Deployment URL example:
     * <pre>http://server-name[:port]/foundation-webapp/api</pre>
     * <p>Supported Media Types</p>
     * <ul>application/xml {@link javax.ws.rs.core.MediaType#APPLICATION_XML_TYPE}</ul>
     * <ul>application/json {@link javax.ws.rs.core.MediaType#APPLICATION_JSON_TYPE}</ul>
     * <p></p>
     * @param deploymentUrl the deployment root URL for all Rest operations
     * @param mediaType a valid, supported internet media type (MIME) supported
     */
    public ConsolidationClient(String deploymentUrl, MediaType mediaType) {
        this(deploymentUrl);
        this.mediaType = mediaType;
    }

    /**
     * Lookup a single consolidation criterion by its primary, unique key 'ConsolidationName' returning a Consolidation transfer
     * object, containing all attributes
     *
     * @param consolidationName the unique name of the Consolidation to lookup
     * @return a Consolidation object or null if not found
     * @throws CollageRestException
     */
    public DtoConsolidation lookup(String consolidationName) throws CollageRestException {
        Response.Status status = Response.Status.SERVICE_UNAVAILABLE;
        ClientResponse<DtoConsolidation> response = null;
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                String url = buildUrlWithPath(API_ROOT, consolidationName);
                ClientRequest request = createClientRequest(url);
                request.accept(mediaType);
                response = request.get(new GenericType<DtoConsolidation>(){});
                if (response.getResponseStatus() == Response.Status.OK) {
                    DtoConsolidation consolidation = response.getEntity(new GenericType<DtoConsolidation>() {});
                    return consolidation;
                }
                else if (response.getResponseStatus() == Response.Status.NOT_FOUND) {
                    return null;
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
        throw new CollageRestException(String.format("Exception executing lookup Consolidation (%s) with status code of %d, reason: %s",
                consolidationName, status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }

    /**
     * Return a list of all consolidation criterion with paging parameters.
     * <p>Paging parameters <code>first</code> and <code>count</code> are used together to retrieve a subset of
     * entities, paging from the <code>first</code> record, for a <code>count</code> of n records</p>
     *
     * @param first the zero-based first record to retrieve to page over result set. Set to -1 to ignore this parameter
     * @param count the number of records to retrieve offset from the first parameter. Set to -1 to ignore this parameter
     * @return a list of consolidation criterion as the specified depth. If no records are found, then an empty list is returned
     * @throws org.groundwork.rs.client.CollageRestException
     */
    public List<DtoConsolidation> list(int first, int count) throws CollageRestException {
        return query(null, first, count);
    }

    /**
     * Return a list of all consolidation criterion.
     * <p>Paging parameters <code>first</code> and <code>count</code> are used together to retrieve a subset of
     * entities, paging from the <code>first</code> record, for a <code>count</code> of n records</p>
     *
     * @return a list of consolidation criterion. If no records are found, then an empty list is returned
     * @throws org.groundwork.rs.client.CollageRestException
     */
    public List<DtoConsolidation> list() throws CollageRestException {
        return query(null, -1, -1);
    }

    /**
     * Query for consolidations with an HQL query string returning a list of matching consolidations for the query.
     * Queries are only valid against the model represented by the Consolidation data transfer object {@link org.groundwork.rs.dto.DtoConsolidation}.
     * Queries are read only operations and
     * are limited to where and order by HQL expressions. Example query:
     * <pre>name like 'RRD%' order by name</pre>
     * <p>The names of the fields queried should match the names in the {@link org.groundwork.rs.dto.DtoConsolidation} model.
     * Any public field can be queried using standard Java Beans naming convention for example a public method named
     * <code>getDescription()</code> would be queried as <code>description</code>.
     * The values of String fields should be single quoted.</p>
     * <p>The dataType field cannot be queried. Instead, the following query fields are available</p>
     * <pre>isBoolean = true</pre>
     * <pre>isString = false</pre>
     * <pre>isInteger = true</pre>
     * <pre>isLong = false</pre>
     * <pre>isDate = true</pre>
     * <p>Paging parameters <code>first</code> and <code>count</code> are used together to retrieve a subset of
     * entities, paging from the <code>first</code> record, for a <code>count</code> of n records</p>
     *
     * @param query the HQL query string. This string should not be encoded, as the Rest Client will convert it for you
     * @return a list of one or more {@link org.groundwork.rs.dto.DtoConsolidation} objects matching the query. If no records match the query,
     *         then an empty list is returned
     * @throws org.groundwork.rs.client.CollageRestException
     */
    public List<DtoConsolidation> query(String query) throws CollageRestException {
        return query(query, -1, -1);
    }

    /**
     * Query for consolidations with an HQL query string returning a list of matching consolidations for the query.
     * Queries are only valid against the model represented by the Consolidation data transfer object {@link org.groundwork.rs.dto.DtoConsolidation}.
     * Queries are read only operations and
     * are limited to where and order by HQL expressions. Example query:
     * <pre>name like 'RRD%' order by name</pre>
     * <p>The names of the fields queried should match the names in the {@link org.groundwork.rs.dto.DtoConsolidation} model.
     * Any public field can be queried using standard Java Beans naming convention for example a public method named
     * <code>getDescription()</code> would be queried as <code>description</code>.
     * The values of String fields should be single quoted.</p>
     * <p>The dataType field cannot be queried. Instead, the following query fields are available</p>
     * <pre>isBoolean = true</pre>
     * <pre>isString = false</pre>
     * <pre>isInteger = true</pre>
     * <pre>isLong = false</pre>
     * <pre>isDate = true</pre>
     * <p>Paging parameters <code>first</code> and <code>count</code> are used together to retrieve a subset of
     * entities, paging from the <code>first</code> record, for a <code>count</code> of n records</p>
     *
     * @param query the HQL query string. This string should not be encoded, as the Rest Client will convert it for you
     * @param first the zero-based first record to retrieve to page over result set. Set to -1 to ignore this parameter
     * @param count the number of records to retrieve offset from the first parameter. Set to -1 to ignore this parameter
     * @return a list of one or more {@link org.groundwork.rs.dto.DtoConsolidation} objects matching the query. If no records match the query,
     *         then an empty list is returned
     * @throws org.groundwork.rs.client.CollageRestException
     */
    public List<DtoConsolidation> query(String query, int first, int count) throws CollageRestException {
        Response.Status status = Response.Status.SERVICE_UNAVAILABLE;
        ClientResponse <DtoConsolidationList> response = null;
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                String url = buildEncodedQuery(API_ROOT, query, first, count);
                ClientRequest request = createClientRequest(url);
                request.accept(mediaType);
                response = request.get(new GenericType<DtoConsolidationList>(){});
                if (response.getResponseStatus() == Response.Status.OK) {
                    DtoConsolidationList consolidations = response.getEntity(new GenericType<DtoConsolidationList>(){});
                    return consolidations.getConsolidations();
                }
                else if (response.getResponseStatus() == Response.Status.NOT_FOUND) {
                    // return an empty list for not found exception
                    return new ArrayList<DtoConsolidation>();
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
        throw new CollageRestException(String.format("Exception executing query consolidations (%s) with status code of %d, reason: %s",
                query, status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }

    /**
     * Administrative batch operations to add or update consolidation criterion. {@link org.groundwork.rs.dto.DtoConsolidationList} is a list of one or
     * more {@link org.groundwork.rs.dto.DtoConsolidation} objects. Each of these objects represent either a new consolidation criterion, or a consolidation criterion to be
     * updated. Any field that needs to be updated or added should be set on the DtoConsolidation.
     * The web service will determine if an update or insert is required by looking up the
     * consolidation criterion's primary key (the field <code>name</code>) from the provided DtoConsolidation objects.
     * <p>
     * The post operation is not transactional. If a list of ten consolidation criterion are passed in to be added, and
     * if, for example, two consolidation criterion fail to update, the other eight consolidation criterion will still be persisted. The results for all
     * post operations return a {@link org.groundwork.rs.dto.DtoOperationResults} list of {@link org.groundwork.rs.dto.DtoOperationResult}, holding
     * the status (success, failure, warning) of each sub-operation, see {@link org.groundwork.rs.dto.DtoOperationResult#getStatus()}.
     * The {@link org.groundwork.rs.dto.DtoOperationResult#getLocation()} method provides the exact URL of the persisted consolidation criterion.
     * </p>
     * @param updates  a list of one or more {@link org.groundwork.rs.dto.DtoConsolidation} objects. Each object will either be updated
     *                 or inserted based on existence of the consolidation criterion's <code>name</code> primary key.
     * @return a {@link org.groundwork.rs.dto.DtoOperationResults} set of {@link org.groundwork.rs.dto.DtoOperationResult}, holding the status of each consolidation criterion operation
     * @throws org.groundwork.rs.client.CollageRestException
     */
    public DtoOperationResults post(DtoConsolidationList updates) throws CollageRestException {
        ClientResponse<DtoOperationResults> response = null;
        Response.Status status = Response.Status.SERVICE_UNAVAILABLE;
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
        throw new CollageRestException(String.format("Exception executing post to consolidation criterion with status code of %d, reason: %s",
                status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }

    /**
     * Administrative batch operations to delete consolidation criterion. Takes a list of one or more consolidation criterion primary key strings,
     * and deletes each consolidation criterion by primary key (name).
     * This 'consolidation criterion name' corresponds with the {@link org.groundwork.rs.dto.DtoConsolidation#getName()} field.
     * <p>
     * The delete operation is not transactional. If a list of five consolidation criterion are passed in to be deleted, and
     * if, for example, two consolidation criterion fail to delete, the other three consolidation criterion will still be deleted. The results for all
     * delete operations return a {@link org.groundwork.rs.dto.DtoOperationResults} list of {@link org.groundwork.rs.dto.DtoOperationResult}, , holding
     * the status (success, failure, warning) of each sub-operation, see {@link org.groundwork.rs.dto.DtoOperationResult#getStatus()}.
     * </p>
     * @param consolidationNames a list of strings of unique consolidation criterion name field primary keys
     * @return a {@link org.groundwork.rs.dto.DtoOperationResults} set of {@link org.groundwork.rs.dto.DtoOperationResult}, holding the status of each consolidation criterion operation
     * @throws org.groundwork.rs.client.CollageRestException
     */
    public DtoOperationResults delete(List<String> consolidationNames) throws CollageRestException {
        ClientResponse<DtoOperationResults> response = null;
        Response.Status status = Response.Status.SERVICE_UNAVAILABLE;
        String typeNames = "";
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                typeNames = makeCommaSeparatedParamFromList(consolidationNames);
                ClientRequest request = createClientRequest(buildUrlWithPath(API_ROOT, typeNames));
                request.accept(mediaType);
                request.body(mediaType, "");
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
        throw new CollageRestException(String.format("Exception executing delete to consolidation criterion (%s) with status code of %d, reason: %s",
                typeNames, status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }


}
