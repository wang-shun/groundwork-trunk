package org.groundwork.rs.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.rs.dto.DtoOperationResults;
import org.groundwork.rs.dto.DtoPropertyType;
import org.groundwork.rs.dto.DtoPropertyTypeList;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.util.GenericType;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

/**
 * The Java REST Client for performing query and administrative operations
 * on Property Types in the Groundwork enterprise foundation server.
 * <p>
 * Operations supported are of the categories:
 * <ul>
 *     <li>lookup operations - lookup property types by their primary key 'name'</li>
 *     <li>list operations - list all property types in the system, with optional depth and paging parameters</li>
 *     <li>query operations - query for property types using an object query language with optional depth and paging parameters</li>
 *     <li>post operations - administrative batch operations to add or update property types. Works with lists of one or more property types</li>
 *     <li>delete operations - administrative batch operations to delete property types. Works with lists of one or more property types</li>
 * </ul>
 * <p>
 * Note that post and delete operations are not transactional. If a list of 10 property types are passed in to be added, and
 * if, for example, two property types fail to update, the other eight property types will still be persisted. The results for all
 * post and delete operations return the same {@link org.groundwork.rs.dto.DtoOperationResults} list of
 * {@link org.groundwork.rs.dto.DtoOperationResult}, holding
 * the result (success, failure, warning) of each sub-operation.
 */
public class PropertyTypeClient extends BaseRestClient {

    protected static Log log = LogFactory.getLog(PropertyTypeClient.class);
    private static final String API_ROOT_SINGLE = "/propertytypes";
    private static final String API_ROOT = API_ROOT_SINGLE + "/";

    /**
     * Create a PropertyType REST Client for performing query and administrative operations
     * on the property types in the Groundwork enterprise foundation database.
     * Deployment URL example:
     * <pre>http://server-name[:port]/foundation-webapp/api</pre>
     *
     * @param deploymentUrl the deployment root URL for all Rest operations
     */
    public PropertyTypeClient(String deploymentUrl) {
        super(deploymentUrl);
    }

    /**
     * Create a PropertyType REST Client for performing query and administrative operations
     * on the property types in the Groundwork enterprise foundation database.
     * Deployment URL example:
     * <pre>http://server-name[:port]/foundation-webapp/api</pre>
     * <p>Supported Media Types</p>
     * <ul>application/xml {@link javax.ws.rs.core.MediaType#APPLICATION_XML_TYPE}</ul>
     * <ul>application/json {@link javax.ws.rs.core.MediaType#APPLICATION_JSON_TYPE}</ul>
     * <p></p>
     * @param deploymentUrl the deployment root URL for all Rest operations
     * @param mediaType a valid, supported internet media type (MIME) supported
     */
    public PropertyTypeClient(String deploymentUrl, MediaType mediaType) {
        this(deploymentUrl);
        this.mediaType = mediaType;
    }

    /**
     * Lookup a single property type by its primary, unique key 'propertyTypeName' returning a PropertyType transfer
     * object, containing all attributes
     *
     * @param propertyTypeName the unique name of the PropertyType to lookup
     * @return a PropertyType object or null if not found
     * @throws org.groundwork.rs.client.CollageRestException
     */
    public DtoPropertyType lookup(String propertyTypeName) throws CollageRestException {
        Response.Status status = Response.Status.SERVICE_UNAVAILABLE;
        ClientResponse<DtoPropertyType> response = null;
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                String url = buildUrlWithPath(API_ROOT, propertyTypeName);
                ClientRequest request = createClientRequest(url);
                request.accept(mediaType);
                response = request.get(new GenericType<DtoPropertyType>(){});
                if (response.getResponseStatus() == Response.Status.OK) {
                    DtoPropertyType propertyType = response.getEntity(new GenericType<DtoPropertyType>() {});
                    return propertyType;
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
        throw new CollageRestException(String.format("Exception executing lookup PropertyType (%s) with status code of %d, reason: %s",
                propertyTypeName, status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }

    /**
     * Return a list of all property types with paging parameters.
     * <p>Paging parameters <code>first</code> and <code>count</code> are used together to retrieve a subset of
     * entities, paging from the <code>first</code> record, for a <code>count</code> of n records</p>
     *
     * @param first the zero-based first record to retrieve to page over result set. Set to -1 to ignore this parameter
     * @param count the number of records to retrieve offset from the first parameter. Set to -1 to ignore this parameter
     * @return a list of property types as the specified depth. If no records are found, then an empty list is returned
     * @throws CollageRestException
     */
    public List<DtoPropertyType> list(int first, int count) throws CollageRestException {
        return query(null, first, count);
    }

    /**
     * Return a list of all property types.
     * <p>Paging parameters <code>first</code> and <code>count</code> are used together to retrieve a subset of
     * entities, paging from the <code>first</code> record, for a <code>count</code> of n records</p>
     *
     * @return a list of property types. If no records are found, then an empty list is returned
     * @throws CollageRestException
     */
    public List<DtoPropertyType> list() throws CollageRestException {
        return query(null, -1, -1);
    }

    /**
     * Query for propertyTypes with an HQL query string returning a list of matching propertyTypes for the query.
     * Queries are only valid against the model represented by the PropertyType data transfer object {@link org.groundwork.rs.dto.DtoPropertyType}.
     * Queries are read only operations and
     * are limited to where and order by HQL expressions. Example query:
     * <pre>name like 'RRD%' order by name</pre>
     * <p>The names of the fields queried should match the names in the {@link org.groundwork.rs.dto.DtoPropertyType} model.
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
     * @return a list of one or more {@link org.groundwork.rs.dto.DtoPropertyType} objects matching the query. If no records match the query,
     *         then an empty list is returned
     * @throws CollageRestException
     */
    public List<DtoPropertyType> query(String query) throws CollageRestException {
        return query(query, -1, -1);
    }

    /**
     * Query for propertyTypes with an HQL query string returning a list of matching propertyTypes for the query.
     * Queries are only valid against the model represented by the PropertyType data transfer object {@link org.groundwork.rs.dto.DtoPropertyType}.
     * Queries are read only operations and
     * are limited to where and order by HQL expressions. Example query:
     * <pre>name like 'RRD%' order by name</pre>
     * <p>The names of the fields queried should match the names in the {@link org.groundwork.rs.dto.DtoPropertyType} model.
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
     * @return a list of one or more {@link org.groundwork.rs.dto.DtoPropertyType} objects matching the query. If no records match the query,
     *         then an empty list is returned
     * @throws CollageRestException
     */
    public List<DtoPropertyType> query(String query, int first, int count) throws CollageRestException {
        Response.Status status = Response.Status.SERVICE_UNAVAILABLE;
        ClientResponse <DtoPropertyTypeList> response = null;
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                String url = buildEncodedQuery(API_ROOT, query, first, count);
                ClientRequest request = createClientRequest(url);
                request.accept(mediaType);
                response = request.get(new GenericType<DtoPropertyTypeList>(){});
                if (response.getResponseStatus() == Response.Status.OK) {
                    DtoPropertyTypeList propertyTypes = response.getEntity(new GenericType<DtoPropertyTypeList>(){});
                    return propertyTypes.getPropertyTypes();
                }
                else if (response.getResponseStatus() == Response.Status.NOT_FOUND) {
                    // return an empty list for not found exception
                    return new ArrayList<DtoPropertyType>();
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
        throw new CollageRestException(String.format("Exception executing query propertyTypes (%s) with status code of %d, reason: %s",
                query, status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }

    /**
     * Administrative batch operations to add or update property types. {@link org.groundwork.rs.dto.DtoPropertyTypeList} is a list of one or
     * more {@link org.groundwork.rs.dto.DtoPropertyType} objects. Each of these objects represent either a new property type, or a property type to be
     * updated. Any field that needs to be updated or added should be set on the DtoPropertyType.
     * The web service will determine if an update or insert is required by looking up the
     * property type's primary key (the field <code>name</code>) from the provided DtoPropertyType objects.
     * <p>
     * The post operation is not transactional. If a list of ten property types are passed in to be added, and
     * if, for example, two property types fail to update, the other eight property types will still be persisted. The results for all
     * post operations return a {@link org.groundwork.rs.dto.DtoOperationResults} list of {@link org.groundwork.rs.dto.DtoOperationResult}, holding
     * the status (success, failure, warning) of each sub-operation, see {@link org.groundwork.rs.dto.DtoOperationResult#getStatus()}.
     * The {@link org.groundwork.rs.dto.DtoOperationResult#getLocation()} method provides the exact URL of the persisted property type.
     * </p>
     * @param updates  a list of one or more {@link org.groundwork.rs.dto.DtoPropertyType} objects. Each object will either be updated
     *                 or inserted based on existence of the property type's <code>name</code> primary key.
     * @return a {@link org.groundwork.rs.dto.DtoOperationResults} set of {@link org.groundwork.rs.dto.DtoOperationResult}, holding the status of each property type operation
     * @throws CollageRestException
     */
    public DtoOperationResults post(DtoPropertyTypeList updates) throws CollageRestException {
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
        throw new CollageRestException(String.format("Exception executing post to property types with status code of %d, reason: %s",
                status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }

    /**
     * Administrative batch operations to delete property types. Takes a list of one or more property type primary key strings,
     * and deletes each property type by primary key (name).
     * This 'property type name' corresponds with the {@link org.groundwork.rs.dto.DtoPropertyType#getName()} field.
     * <p>
     * The delete operation is not transactional. If a list of five property types are passed in to be deleted, and
     * if, for example, two property types fail to delete, the other three property types will still be deleted. The results for all
     * delete operations return a {@link DtoOperationResults} list of {@link org.groundwork.rs.dto.DtoOperationResult}, , holding
     * the status (success, failure, warning) of each sub-operation, see {@link org.groundwork.rs.dto.DtoOperationResult#getStatus()}.
     * </p>
     * @param propertyTypeNames a list of strings of unique property type name field primary keys
     * @return a {@link DtoOperationResults} set of {@link org.groundwork.rs.dto.DtoOperationResult}, holding the status of each property type operation
     * @throws CollageRestException
     */
    public DtoOperationResults delete(List<String> propertyTypeNames) throws CollageRestException {
        ClientResponse<DtoOperationResults> response = null;
        Response.Status status = Response.Status.SERVICE_UNAVAILABLE;
        String typeNames = "";
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                typeNames = makeCommaSeparatedParamFromList(propertyTypeNames);
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
        throw new CollageRestException(String.format("Exception executing delete to property types (%s) with status code of %d, reason: %s",
                typeNames, status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }


}
