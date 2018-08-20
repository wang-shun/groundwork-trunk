package org.groundwork.rs.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.rs.dto.DtoEntityType;
import org.groundwork.rs.dto.DtoEntityTypeList;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.util.GenericType;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

/**
 * The Java REST Client for performing query and administrative operations
 * on Entity Types in the Groundwork enterprise foundation server.
 * <p>
 * Operations supported are of the categories:
 * <ul>
 *     <li>lookup operations - lookup entity types by their primary key 'name'</li>
 *     <li>list operations - list all entity types in the system, with optional depth and paging parameters</li>
 *     <li>query operations - query for entity types using an object query language with optional depth and paging parameters</li>
 *     <li>post operations - administrative batch operations to add or update entity types. Works with lists of one or more entity types</li>
 *     <li>delete operations - administrative batch operations to delete entity types. Works with lists of one or more entity types</li>
 * </ul>
 * <p>
 * Note that post and delete operations are not transactional. If a list of 10 entity types are passed in to be added, and
 * if, for example, two entity types fail to update, the other eight entity types will still be persisted. The results for all
 * post and delete operations return the same {@link org.groundwork.rs.dto.DtoOperationResults} list of
 * {@link org.groundwork.rs.dto.DtoOperationResult}, holding
 * the result (success, failure, warning) of each sub-operation.
 */
public class EntityTypeClient extends BaseRestClient {

    protected static Log log = LogFactory.getLog(EntityTypeClient.class);
    private static final String API_ROOT_SINGLE = "/entitytypes";
    private static final String API_ROOT = API_ROOT_SINGLE + "/";

    /**
     * Create a EntityType REST Client for performing query and administrative operations
     * on the entity types in the Groundwork enterprise foundation database.
     * Deployment URL example:
     * <pre>http://server-name[:port]/foundation-webapp/api</pre>
     *
     * @param deploymentUrl the deployment root URL for all Rest operations
     */
    public EntityTypeClient(String deploymentUrl) {
        super(deploymentUrl);
    }

    /**
     * Create a EntityType REST Client for performing query and administrative operations
     * on the entity types in the Groundwork enterprise foundation database.
     * Deployment URL example:
     * <pre>http://server-name[:port]/foundation-webapp/api</pre>
     * <p>Supported Media Types</p>
     * <ul>application/xml {@link javax.ws.rs.core.MediaType#APPLICATION_XML_TYPE}</ul>
     * <ul>application/json {@link javax.ws.rs.core.MediaType#APPLICATION_JSON_TYPE}</ul>
     * <p></p>
     * @param deploymentUrl the deployment root URL for all Rest operations
     * @param mediaType a valid, supported internet media type (MIME) supported
     */
    public EntityTypeClient(String deploymentUrl, MediaType mediaType) {
        this(deploymentUrl);
        this.mediaType = mediaType;
    }

    /**
     * Lookup a single entity type by its primary, unique key 'entityTypeName' returning a EntityType transfer
     * object, containing all attributes
     *
     * @param entityTypeName the unique name of the EntityType to lookup
     * @return a EntityType object or null if not found
     * @throws CollageRestException
     */
    public DtoEntityType lookup(String entityTypeName) throws CollageRestException {
        Response.Status status = Response.Status.SERVICE_UNAVAILABLE;
        ClientResponse<DtoEntityType> response = null;
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                String url = buildUrlWithPath(API_ROOT, entityTypeName);
                ClientRequest request = createClientRequest(url);
                request.accept(mediaType);
                response = request.get(new GenericType<DtoEntityType>(){});
                if (response.getResponseStatus() == Response.Status.OK) {
                    DtoEntityType entityType = response.getEntity(new GenericType<DtoEntityType>() {});
                    return entityType;
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
        throw new CollageRestException(String.format("Exception executing lookup EntityType (%s) with status code of %d, reason: %s",
                entityTypeName, status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }

    /**
     * Return a list of all entity types with paging parameters.
     * <p>Paging parameters <code>first</code> and <code>count</code> are used together to retrieve a subset of
     * entities, paging from the <code>first</code> record, for a <code>count</code> of n records</p>
     *
     * @param first the zero-based first record to retrieve to page over result set. Set to -1 to ignore this parameter
     * @param count the number of records to retrieve offset from the first parameter. Set to -1 to ignore this parameter
     * @return a list of entity types as the specified depth. If no records are found, then an empty list is returned
     * @throws org.groundwork.rs.client.CollageRestException
     */
    public List<DtoEntityType> list(int first, int count) throws CollageRestException {
        return query(null, first, count);
    }

    /**
     * Return a list of all entity types.
     * <p>Paging parameters <code>first</code> and <code>count</code> are used together to retrieve a subset of
     * entities, paging from the <code>first</code> record, for a <code>count</code> of n records</p>
     *
     * @return a list of entity types. If no records are found, then an empty list is returned
     * @throws org.groundwork.rs.client.CollageRestException
     */
    public List<DtoEntityType> list() throws CollageRestException {
        return query(null, -1, -1);
    }

    /**
     * Query for entityTypes with an HQL query string returning a list of matching entityTypes for the query.
     * Queries are only valid against the model represented by the EntityType data transfer object {@link org.groundwork.rs.dto.DtoEntityType}.
     * Queries are read only operations and
     * are limited to where and order by HQL expressions. Example query:
     * <pre>name like 'RRD%' order by name</pre>
     * <p>The names of the fields queried should match the names in the {@link org.groundwork.rs.dto.DtoEntityType} model.
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
     * @return a list of one or more {@link org.groundwork.rs.dto.DtoEntityType} objects matching the query. If no records match the query,
     *         then an empty list is returned
     * @throws org.groundwork.rs.client.CollageRestException
     */
    public List<DtoEntityType> query(String query) throws CollageRestException {
        return query(query, -1, -1);
    }

    /**
     * Query for entityTypes with an HQL query string returning a list of matching entityTypes for the query.
     * Queries are only valid against the model represented by the EntityType data transfer object {@link org.groundwork.rs.dto.DtoEntityType}.
     * Queries are read only operations and
     * are limited to where and order by HQL expressions. Example query:
     * <pre>name like 'RRD%' order by name</pre>
     * <p>The names of the fields queried should match the names in the {@link org.groundwork.rs.dto.DtoEntityType} model.
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
     * @return a list of one or more {@link org.groundwork.rs.dto.DtoEntityType} objects matching the query. If no records match the query,
     *         then an empty list is returned
     * @throws org.groundwork.rs.client.CollageRestException
     */
    public List<DtoEntityType> query(String query, int first, int count) throws CollageRestException {
        Response.Status status = Response.Status.SERVICE_UNAVAILABLE;
        ClientResponse <DtoEntityTypeList> response = null;
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                String url = buildEncodedQuery(API_ROOT, query, first, count);
                ClientRequest request = createClientRequest(url);
                request.accept(mediaType);
                response = request.get(new GenericType<DtoEntityTypeList>(){});
                if (response.getResponseStatus() == Response.Status.OK) {
                    DtoEntityTypeList entityTypes = response.getEntity(new GenericType<DtoEntityTypeList>(){});
                    return entityTypes.getEntityTypes();
                }
                else if (response.getResponseStatus() == Response.Status.NOT_FOUND) {
                    // return an empty list for not found exception
                    return new ArrayList<DtoEntityType>();
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
        throw new CollageRestException(String.format("Exception executing query entityTypes (%s) with status code of %d, reason: %s",
                query, status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }

}
