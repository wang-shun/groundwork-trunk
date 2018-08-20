package org.groundwork.rs.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.rs.dto.DtoApplicationType;
import org.groundwork.rs.dto.DtoApplicationTypeList;
import org.groundwork.rs.dto.DtoDepthType;
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
 * on Application Types in the Groundwork enterprise foundation server.
 * <p>
 * Operations supported are of the categories:
 * <ul>
 *     <li>lookup operations - lookup application types by their primary key 'name'</li>
 *     <li>list operations - list all application types in the system, with optional depth and paging parameters</li>
 *     <li>query operations - query for application types using an object query language with optional depth and paging parameters</li>
 *     <li>post operations - administrative batch operations to add or update application types. Works with lists of one or more application types</li>
 *     <li>delete operations - administrative batch operations to delete application types. Works with lists of one or more application types</li>
 * </ul>
 * <p>
 * Note that post and delete operations are not transactional. If a list of 10 application types are passed in to be added, and
 * if, for example, two application types fail to update, the other eight application types will still be persisted. The results for all
 * post and delete operations return the same {@link org.groundwork.rs.dto.DtoOperationResults} list of
 * {@link org.groundwork.rs.dto.DtoOperationResult}, holding
 * the result (success, failure, warning) of each sub-operation.
 */
public class ApplicationTypeClient extends BaseRestClient {

    protected static Log log = LogFactory.getLog(ApplicationTypeClient.class);
    private static final String API_ROOT_SINGLE = "/applicationtypes";
    private static final String API_ROOT = API_ROOT_SINGLE + "/";

    /**
     * Create a ApplicationType REST Client for performing query and administrative operations
     * on the application types in the Groundwork enterprise foundation database.
     * Deployment URL example:
     * <pre>http://server-name[:port]/foundation-webapp/api</pre>
     *
     * @param deploymentUrl the deployment root URL for all Rest operations
     */
    public ApplicationTypeClient(String deploymentUrl) {
        super(deploymentUrl);
    }

    /**
     * Create a ApplicationType REST Client for performing query and administrative operations
     * on the application types in the Groundwork enterprise foundation database.
     * Deployment URL example:
     * <pre>http://server-name[:port]/foundation-webapp/api</pre>
     * <p>Supported Media Types</p>
     * <ul>application/xml {@link javax.ws.rs.core.MediaType#APPLICATION_XML_TYPE}</ul>
     * <ul>application/json {@link javax.ws.rs.core.MediaType#APPLICATION_JSON_TYPE}</ul>
     * <p></p>
     * @param deploymentUrl the deployment root URL for all Rest operations
     * @param mediaType a valid, supported internet media type (MIME) supported
     */
    public ApplicationTypeClient(String deploymentUrl, MediaType mediaType) {
        this(deploymentUrl);
        this.mediaType = mediaType;
    }

    /**
     * Lookup a single application type by its primary, unique key 'applicationTypeName' returning a ApplicationType transfer
     * object, containing all attributes
     *
     * @param applicationTypeName the unique name of the ApplicationType to lookup
     * @return a ApplicationType object or null if not found
     * @throws CollageRestException
     */
    public DtoApplicationType lookup(String applicationTypeName) throws CollageRestException {
        return lookup(applicationTypeName, DtoDepthType.Shallow);
    }

    /**
     * Lookup a single application type by its primary, unique key 'applicationTypeName' returning a ApplicationType transfer
     * object, containing all attributes
     *
     * @param applicationTypeName the unique name of the ApplicationType to lookup
     * @param depthType specify either {@link DtoDepthType#Shallow} to retrieve basic attributes plus properties,
     *                  {@link DtoDepthType#Simple} to retrieve name and descriptions only,
     *                  or {@link DtoDepthType#Deep} to retrieve the objects plus its associated (shallow) objects
     * @return a ApplicationType object or null if not found
     * @throws CollageRestException
     */
    public DtoApplicationType lookup(String applicationTypeName, DtoDepthType depthType) throws CollageRestException {
        Response.Status status = Response.Status.SERVICE_UNAVAILABLE;
        ClientResponse<DtoApplicationType> response = null;
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                String url = buildLookupWithDepth(API_ROOT, applicationTypeName, depthType);
                ClientRequest request = createClientRequest(url);
                request.accept(mediaType);
                response = request.get(new GenericType<DtoApplicationType>(){});
                if (response.getResponseStatus() == Response.Status.OK) {
                    DtoApplicationType applicationType = response.getEntity(new GenericType<DtoApplicationType>() {});
                    return applicationType;
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
        throw new CollageRestException(String.format("Exception executing lookup ApplicationType (%s) with status code of %d, reason: %s",
                applicationTypeName, status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }

    /**
     * Return a list of all application types with paging parameters.
     * <p>Paging parameters <code>first</code> and <code>count</code> are used together to retrieve a subset of
     * entities, paging from the <code>first</code> record, for a <code>count</code> of n records</p>
     *
     * @param first the zero-based first record to retrieve to page over result set. Set to -1 to ignore this parameter
     * @param count the number of records to retrieve offset from the first parameter. Set to -1 to ignore this parameter
     * @return a list of application types as the specified depth. If no records are found, then an empty list is returned
     * @throws org.groundwork.rs.client.CollageRestException
     */
    public List<DtoApplicationType> list(int first, int count) throws CollageRestException {
        return query(null, first, count);
    }

    /**
     * Return a list of all application types.
     * <p>Paging parameters <code>first</code> and <code>count</code> are used together to retrieve a subset of
     * entities, paging from the <code>first</code> record, for a <code>count</code> of n records</p>
     *
     * @return a list of application types. If no records are found, then an empty list is returned
     * @throws org.groundwork.rs.client.CollageRestException
     */
    public List<DtoApplicationType> list() throws CollageRestException {
        return query(null, -1, -1);
    }

    /**
     * Query for applicationTypes with an HQL query string returning a list of matching applicationTypes for the query.
     * Queries are only valid against the model represented by the ApplicationType data transfer object {@link org.groundwork.rs.dto.DtoApplicationType}.
     * Queries are read only operations and
     * are limited to where and order by HQL expressions. Example query:
     * <pre>name like 'RRD%' order by name</pre>
     * <p>The names of the fields queried should match the names in the {@link org.groundwork.rs.dto.DtoApplicationType} model.
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
     * @return a list of one or more {@link org.groundwork.rs.dto.DtoApplicationType} objects matching the query. If no records match the query,
     *         then an empty list is returned
     * @throws org.groundwork.rs.client.CollageRestException
     */
    public List<DtoApplicationType> query(String query) throws CollageRestException {
        return query(query, -1, -1);
    }

    /**
     * Query for applicationTypes with an HQL query string returning a list of matching applicationTypes for the query.
     * Queries are only valid against the model represented by the ApplicationType data transfer object {@link org.groundwork.rs.dto.DtoApplicationType}.
     * Queries are read only operations and
     * are limited to where and order by HQL expressions. Example query:
     * <pre>name like 'RRD%' order by name</pre>
     * <p>The names of the fields queried should match the names in the {@link org.groundwork.rs.dto.DtoApplicationType} model.
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
     * @return a list of one or more {@link org.groundwork.rs.dto.DtoApplicationType} objects matching the query. If no records match the query,
     *         then an empty list is returned
     * @throws org.groundwork.rs.client.CollageRestException
     */
    public List<DtoApplicationType> query(String query, int first, int count) throws CollageRestException {
        Response.Status status = Response.Status.SERVICE_UNAVAILABLE;
        ClientResponse <DtoApplicationTypeList> response = null;
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                String url = buildEncodedQuery(API_ROOT, query, first, count);
                ClientRequest request = createClientRequest(url);
                request.accept(mediaType);
                response = request.get(new GenericType<DtoApplicationTypeList>(){});
                if (response.getResponseStatus() == Response.Status.OK) {
                    DtoApplicationTypeList applicationTypes = response.getEntity(new GenericType<DtoApplicationTypeList>(){});
                    return applicationTypes.getApplicationTypes();
                }
                else if (response.getResponseStatus() == Response.Status.NOT_FOUND) {
                    // return an empty list for not found exception
                    return new ArrayList<DtoApplicationType>();
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
        throw new CollageRestException(String.format("Exception executing query applicationTypes (%s) with status code of %d, reason: %s",
                query, status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }

    /**
     * Administrative batch operations to add or update application types. {@link org.groundwork.rs.dto.DtoApplicationTypeList} is a list of one or
     * more {@link org.groundwork.rs.dto.DtoApplicationType} objects. Each of these objects represent either a new application type, or a application type to be
     * updated. Any field that needs to be updated or added should be set on the DtoApplicationType.
     * The web service will determine if an update or insert is required by looking up the
     * application type's primary key (the field <code>name</code>) from the provided DtoApplicationType objects.
     * <p>
     * The post operation is not transactional. If a list of ten application types are passed in to be added, and
     * if, for example, two application types fail to update, the other eight application types will still be persisted. The results for all
     * post operations return a {@link org.groundwork.rs.dto.DtoOperationResults} list of {@link org.groundwork.rs.dto.DtoOperationResult}, holding
     * the status (success, failure, warning) of each sub-operation, see {@link org.groundwork.rs.dto.DtoOperationResult#getStatus()}.
     * The {@link org.groundwork.rs.dto.DtoOperationResult#getLocation()} method provides the exact URL of the persisted application type.
     * </p>
     * @param updates  a list of one or more {@link org.groundwork.rs.dto.DtoApplicationType} objects. Each object will either be updated
     *                 or inserted based on existence of the application type's <code>name</code> primary key.
     * @return a {@link org.groundwork.rs.dto.DtoOperationResults} set of {@link org.groundwork.rs.dto.DtoOperationResult}, holding the status of each application type operation
     * @throws org.groundwork.rs.client.CollageRestException
     */
    public DtoOperationResults post(DtoApplicationTypeList updates) throws CollageRestException {
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
        throw new CollageRestException(String.format("Exception executing post to application types with status code of %d, reason: %s",
                status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }

    /**
     * Administrative batch operations to delete application types. Takes a list of one or more application type primary key strings,
     * and deletes each application type by primary key (name).
     * This 'application type name' corresponds with the {@link org.groundwork.rs.dto.DtoApplicationType#getName()} field.
     * <p>
     * The delete operation is not transactional. If a list of five application types are passed in to be deleted, and
     * if, for example, two application types fail to delete, the other three application types will still be deleted. The results for all
     * delete operations return a {@link org.groundwork.rs.dto.DtoOperationResults} list of {@link org.groundwork.rs.dto.DtoOperationResult}, , holding
     * the status (success, failure, warning) of each sub-operation, see {@link org.groundwork.rs.dto.DtoOperationResult#getStatus()}.
     * </p>
     * @param applicationTypeNames a list of strings of unique application type name field primary keys
     * @return a {@link org.groundwork.rs.dto.DtoOperationResults} set of {@link org.groundwork.rs.dto.DtoOperationResult}, holding the status of each application type operation
     * @throws org.groundwork.rs.client.CollageRestException
     */
    public DtoOperationResults delete(List<String> applicationTypeNames) throws CollageRestException {
        ClientResponse<DtoOperationResults> response = null;
        Response.Status status = Response.Status.SERVICE_UNAVAILABLE;
        String typeNames = "";
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                typeNames = makeCommaSeparatedParamFromList(applicationTypeNames);
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
        throw new CollageRestException(String.format("Exception executing delete to application types (%s) with status code of %d, reason: %s",
                typeNames, status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }


}
