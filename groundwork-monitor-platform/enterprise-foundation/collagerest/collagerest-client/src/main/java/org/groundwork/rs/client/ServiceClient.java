package org.groundwork.rs.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.rs.dto.*;
import org.jboss.resteasy.util.GenericType;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * The Java REST Client for performing query and administrative operations
 * on monitored Services in the Groundwork enterprise foundation server.
 * <p>
 * Operations supported are of the categories:
 * <ul>
 *     <li>lookup operations - lookup services by their primary keys serviceName + hostName (NOTE: in the model, service name and description are the same)</li>
 *     <li>list operations - list all services in the system, with optional depth and paging parameters</li>
 *     <li>query operations - query for services using an object query language with optional depth and paging parameters</li>
 *     <li>post operations - administrative batch operations to add or update services. Works with lists of one or more services</li>
 *     <li>delete operations - administrative batch operations to delete services. Works with lists of one or more services</li>
 * </ul>
 * <p>
 * Note that post and delete operations are not transactional. If a list of 10 services are passed in to be added, and
 * if, for example, two services fail to update, the other eight services will still be persisted. The results for all
 * post and delete operations return the same {@link DtoOperationResults} list of
 * {@link org.groundwork.rs.dto.DtoOperationResult}, holding
 * the result (success, failure, warning) of each sub-operation.
 *
 * ServiceClient supports retrieval operations (lookup,list,query) of only one default depth which includes all attributes:
 * <ul>
 *    <li>{@link org.groundwork.rs.dto.DtoDepthType#Shallow}</li>
 * </ul>
 * <p>Dynamic Properties are retrieved with all service lookup, list and query methods. Properties are returned
 * in a name-value pair map, accessible on the Service DTO via {@link org.groundwork.rs.dto.DtoPropertiesBase#getProperties()}.
 * Property access methods are provided for the simple types of String, Boolean, Int, Long, Double, and Date.</p>
 *
 */
public class ServiceClient extends BaseRestClient {

    protected static Log log = LogFactory.getLog(ServiceClient.class);
    private static final String API_ROOT_SINGLE = "/services";
    private static final String API_ROOT = API_ROOT_SINGLE + "/";

    public static final String PROP_IS_ACKNOWLEDGED = "isProblemAcknowledged";
    public static final String PROP_ACKNOWLEDGED_BY = "AcknowledgedBy";
    public static final String PROP_ACKNOWLEDGE_COMMENT = "AcknowledgeComment";

    /**
     * Create a Service REST Client for performing query and administrative operations
     * on the services in the Groundwork enterprise foundation database.
     * Deployment URL example:
     * <pre>http://server-name[:port]/foundation-webapp/api</pre>
     *
     * @param deploymentUrl the deployment root URL for all Rest operations
     */
    public ServiceClient(String deploymentUrl) {
        super(deploymentUrl);
    }

    /**
     * Create a Service REST Client for performing query and administrative operations
     * on the services in the Groundwork enterprise foundation database.
     * Deployment URL example:
     * <pre>http://server-name[:port]/foundation-webapp/api</pre>
     * <p>Supported Media Types</p>
     * <ul>application/xml {@link MediaType#APPLICATION_XML_TYPE}</ul>
     * <ul>application/json {@link MediaType#APPLICATION_JSON_TYPE}</ul>
     * <p></p>
     * @param deploymentUrl the deployment root URL for all Rest operations
     * @param mediaType a valid, supported internet media type (MIME) supported
     */
    public ServiceClient(String deploymentUrl, MediaType mediaType) {
        this(deploymentUrl);
        this.mediaType = mediaType;
    }

    /**
     * Lookup a single service by its id returning a Service transfer object of given depth, containing the all service attributes.
     *
     * @param serviceId the service ID
     * @return a Service object or null if not found
     * @throws CollageRestException
     */
    public DtoService lookup(int serviceId) throws CollageRestException {
        try {
            String requestUrl = buildUrlWithPath(API_ROOT, String.valueOf(serviceId));
            String requestDescription = String.format("lookup service id [%d]", serviceId);
            return clientRequest(requestUrl, requestDescription, new GenericType<DtoService>(){});
        }
        catch (Exception e) {
            throw new CollageRestException(e);
        }
    }

    /**
     * Lookup a single service by its primary, unique key 'serviceName' + 'hostName' returning a Service transfer object
     * of depth shallow, containing the all service attributes. Note that the <code>serviceName</code> maps to the
     * <code>serviceDescription</code> on the data transfer object {@link org.groundwork.rs.dto.DtoService#getDescription()}
     *
     * @param serviceName the unique service name description
     * @param hostName the name of the Host to lookup
     * @param depthType specify either {@link DtoDepthType#Shallow} to retrieve basic attributes plus properties,
     *                  {@link DtoDepthType#Simple} to retrieve name and descriptions only,
     *                  or {@link DtoDepthType#Deep} to retrieve the objects plus its associated (shallow) objects
     * @return a Service object or null if not found
     * @throws CollageRestException
     */
    public DtoService lookup(String serviceName, String hostName, DtoDepthType depthType) throws CollageRestException {
        try {
            String requestUrl = (depthType == null)
                    ? buildUrlWithPath(API_ROOT, encode(serviceName) + "?hostName=" + encode(hostName))
                    : buildLookupWithPathQueryDepth(API_ROOT, serviceName, "?hostName=" + encode(hostName), depthType);
            String requestDescription = String.format("lookup service [%s:%s]", hostName, serviceName);
            return clientRequest(requestUrl, requestDescription, new GenericType<DtoService>(){});
        }
        catch (Exception e) {
            throw new CollageRestException(e);
        }
    }

    public DtoService lookup(String serviceName, String hostName) throws CollageRestException {
        return lookup(serviceName, hostName, null);
    }

    /**
         * Query for services with an HQL query string returning a list of matching services.
         * Queries are only valid against the model represented by the Service data transfer object {@link org.groundwork.rs.dto.DtoService}.
         * Queries are read only operations and are limited to where and order by HQL expressions. Example query:
         * <pre>(property.ExecutionTime between 10 and 3500 and (monitorStatus <> 'UP')) order by property.ExecutionTime</pre>
         * <p>The names of the fields queried should match the names in the {@link org.groundwork.rs.dto.DtoService} model. Any public field
         * can be queried using standard Java Beans naming convention for example a public method named <code>getDescription()</code>
         * would be queried as <code>description</code>. The values of String fields should be single quoted.
         . Services have dynamic properties,
         * which can be queried with the prefix <code>property</code> for example <code>property.ExecutionTime</code>.</p>
         *
         * @param query the HQL query string. This string should not be encoded, as the Rest Client will convert it for you
         * @return a list of one or more {@link org.groundwork.rs.dto.DtoService} objects matching the query. If no records match the query,
         *         then an empty list is returned
         * @throws CollageRestException
         */
    public List<DtoService> query(String query) throws CollageRestException {
        return query(query, -1, -1);
    }

    /**
     * Query for services with an HQL query string returning a list of matching services.
     * Queries are only valid against the model represented by the Service data transfer object {@link org.groundwork.rs.dto.DtoService}.
     * Queries are read only operations and are limited to where and order by HQL expressions. Example query:
     * <pre>(property.ExecutionTime between 10 and 3500 and (monitorStatus <> 'UP')) order by property.ExecutionTime</pre>
     * <p>The names of the fields queried should match the names in the {@link org.groundwork.rs.dto.DtoService} model. Any public field
     * can be queried using standard Java Beans naming convention for example a public method named <code>getDescription()</code>
     * would be queried as <code>description</code>. The values of String fields should be single quoted.
     . Services have dynamic properties,
     * which can be queried with the prefix <code>property</code> for example <code>property.ExecutionTime</code>.</p>
     * <p>Paging parameters <code>first</code> and <code>count</code> are used together to retrieve a subset of
     * entities, paging from the <code>first</code> record, for a <code>count</code> of n records</p>
     *
     * @param query the HQL query string. This string should not be encoded, as the Rest Client will convert it for you
     * @param first the zero-based first record to retrieve to page over result set. Set to -1 to ignore this parameter
     * @param count the number of records to retrieve offset from the first parameter. Set to -1 to ignore this parameter
     * @return a list of one or more {@link org.groundwork.rs.dto.DtoService} objects matching the query. If no records match the query,
     *         then an empty list is returned
     * @throws CollageRestException
     */
    public List<DtoService> query(String query, int first, int count) throws CollageRestException {
        try {
            String requestUrl =  buildEncodedQuery(API_ROOT, query, first, count);
            String requestDescription = String.format("%s hosts [%s]", (query == null) ? "list" : "query",  (query == null) ? "-" : query);
            DtoServiceList dtoServiceList = clientRequest(requestUrl, requestDescription, new GenericType<DtoServiceList>(){});
            return ((dtoServiceList != null) ? dtoServiceList.getServices() : Collections.EMPTY_LIST);
        }
        catch (Exception e) {
            throw new CollageRestException(e);
        }
    }

    public List<DtoService> query(String query, DtoDepthType depthType) throws CollageRestException {
        return query(query, depthType, -1, -1);
    }

    public List<DtoService> query(String query, DtoDepthType depthType, int first, int count) throws CollageRestException {
        try {
            String requestUrl = buildEncodedQuery(API_ROOT, query, depthType, first, count);
            String requestDescription = String.format("%s services [%s]", (query == null) ? "list" : "query",  (query == null) ? "-" : query);
            DtoServiceList dtoServiceList = clientRequest(requestUrl, requestDescription, new GenericType<DtoServiceList>(){});
            return ((dtoServiceList != null) ? dtoServiceList.getServices() : Collections.EMPTY_LIST);
        }
        catch (Exception e) {
            throw new CollageRestException(e);
        }
    }

    /**
     * Return a limited list of all Services in the database with paging parameters.
     * The list contains the basic attributes and dynamic properties.
     * Paging parameters <code>first</code>
     * and <code>count</code> are used together to retrieve a subset of
     * entities, paging from the <code>first</code> record, for a <code>count</code> of n records</p>
     *
     * @param first the zero-based first record to retrieve to page over result set. Set to -1 to ignore this parameter
     * @param count the number of records to retrieve offset from the first parameter. Set to -1 to ignore this parameter
     * @return a list of all Services of type {@link DtoService}
     *         If no records are found, then an empty list is returned
     * @throws CollageRestException
     */
    public List<DtoService> list(int first, int count) throws CollageRestException {
        return query(null, first, count);
    }

    /**
     * Return a list of all Services in the database.
     * The list contains the basic attributes and dynamic properties.
     *
     * @return a list of all Services of type {@link DtoService}
     *         If no records are found, then an empty list is returned
     * @throws CollageRestException
     */
    public List<DtoService> list() throws CollageRestException {
        return query(null, -1, -1);
    }

    /**
     * Return a list of all Services in the database for a given hostname
     * The list contains the basic attributes and dynamic properties.
     *
     * @return a list of all Services of type {@link DtoService}
     *         If no records are found, then an empty list is returned
     * @throws CollageRestException
     */
    public List<DtoService> list(String hostName) throws CollageRestException {
        if (hostName == null) {
            return list();
        }
        String hostQuery = "hostName = '" + hostName + "'";
        return query(hostQuery, -1, -1);
    }

    /**
     * Administrative batch operations to add or update Services. {@link org.groundwork.rs.dto.DtoServiceList} is a list of one or
     * more {@link org.groundwork.rs.dto.DtoService} objects. Each of these objects represent either a new service, or a service to be
     * updated. Any field that needs to be updated or added should be set on the DtoService object.
     * The web service will determine if an update or insert is required by looking up the
     * service's primary keys (the fields <code>description</code> and <code>hostName</code>) from the provided DtoService objects.
     * Both of these fields are required.
     * <p>
     * The post operation is not transactional. If a list of ten services are passed in to be added, and
     * if, for example, two services fail to update, the other eight services will still be persisted. The results for all
     * post operations return a {@link DtoOperationResults} list of {@link org.groundwork.rs.dto.DtoOperationResult}, holding
     * the status (success, failure, warning) of each sub-operation, see {@link org.groundwork.rs.dto.DtoOperationResult#getStatus()}.
     * The {@link org.groundwork.rs.dto.DtoOperationResult#getLocation()} method provides the exact URL of the persisted Service.
     * </p>
     * @param updates  a list of one or more {@link org.groundwork.rs.dto.DtoService} objects. Each object will either be updated
     *                 or inserted based on existence of the service's primary key <code>description</code> plus <code>hostName</code>.
     * @param merge    merge hosts with matching but different names
     * @param async    should this post operation be synchronous or asynchronous
     * @return a {@link DtoOperationResults} set of {@link org.groundwork.rs.dto.DtoOperationResult},
     *         holding the status of each operation
     * @throws CollageRestException
     */
    public DtoOperationResults post(DtoServiceList updates, boolean merge, boolean async) throws CollageRestException {
        String requestUrl = buildPostAsyncMergeURL(API_ROOT, API_ROOT_SINGLE, merge, async);
        String requestDescription = "posting services";
        DtoOperationResults results = clientRequest(HttpMethod.POST, requestUrl, updates, requestDescription, new GenericType<DtoOperationResults>() {});
        return (results != null) ? results : new DtoOperationResults();
    }

    public DtoOperationResults post(DtoServiceList updates, boolean merge) throws CollageRestException {
        return post(updates, merge, false);
    }

    public DtoOperationResults post(DtoServiceList updates) throws CollageRestException {
        return post(updates, true);
    }

    public DtoOperationResults postAsync(DtoServiceList updates, boolean merge) throws CollageRestException {
        return post(updates, merge, true);
    }

    public DtoOperationResults postAsync(DtoServiceList updates) throws CollageRestException {
        return postAsync(updates, true);
    }

    /**
     * Administrative batch operations to delete a list of services. {@link org.groundwork.rs.dto.DtoServiceList} is a list of one or
     * more {@link org.groundwork.rs.dto.DtoService} objects. Each of these objects should only have the
     * {@link DtoService#setDescription(String)} and {@link DtoService#setHostName(String)} primary key fields set.
     * These fields are used to delete each Service by primary key.
     * All other fields will be ignored.
     * <p>
     * The delete operation is not transactional. If a list of five services are passed in to be deleted, and
     * if, for example, two services fail to delete, the other three services will still be deleted. The results for all
     * delete operations return a {@link DtoOperationResults} list of {@link org.groundwork.rs.dto.DtoOperationResult}, , holding
     * the status (success, failure, warning) of each sub-operation, see {@link org.groundwork.rs.dto.DtoOperationResult#getStatus()}.
     * </p>
     * @param deletes   a list of one or more {@link org.groundwork.rs.dto.DtoHost} objects. Only the
     *                  service's <code>description</code> plus <code>hostName</code> primary key will be considered when deleting.
     * @return a {@link DtoOperationResults} set of {@link org.groundwork.rs.dto.DtoOperationResult},
     *          holding the status of each operation
     * @throws CollageRestException
     */
    public DtoOperationResults delete(DtoServiceList deletes) throws CollageRestException {
        String requestUrl = build(API_ROOT_SINGLE);
        String requestDescription = "deleting services";
        DtoOperationResults results = clientRequest(HttpMethod.DELETE, requestUrl, deletes, requestDescription, new GenericType<DtoOperationResults>(){});
        return (results != null) ? results : new DtoOperationResults();
    }

    /**
     * Administrative operation to delete a single Service. Takes the combined primary key <code>serviceName</code> and
     * <code>hostName</code> and deletes a single Service by this combined primary key.
     * This 'serviceName' corresponds with the {@link org.groundwork.rs.dto.DtoService#getDescription()} field.
     * This 'hostName' corresponds with the {@link org.groundwork.rs.dto.DtoService#getHostName()} field.
     *
     * @param serviceName the unique, primary name of services {@link DtoService#getDescription}
     * @param hostName the secondary primary field of the services
     * @return a {@link DtoOperationResults} set of {@link org.groundwork.rs.dto.DtoOperationResult},
     *              holding the status of each operation
     * @throws CollageRestException
     */
    public DtoOperationResults delete(String serviceName, String hostName) throws CollageRestException {
        List<String> names = new ArrayList<String>();
        names.add(serviceName);
        return delete(names, hostName);
    }

    /**
     * Administrative batch operations to delete a list of Services by primary keys serviceName (description) and hostName.
     * <p>
     * The delete operation is not transactional. If a list of five services are passed in to be deleted, and
     * if, for example, two services fail to delete, the other three services will still be deleted. The results for all
     * delete operations return a {@link DtoOperationResults} list of {@link org.groundwork.rs.dto.DtoOperationResult}, , holding
     * the status (success, failure, warning) of each sub-operation, see {@link org.groundwork.rs.dto.DtoOperationResult#getStatus()}.
     * </p>
     * @param serviceNamesList   a list of one or more service names primary keys.
     * @param hostName the secondary primary field of the services
     * @return a {@link DtoOperationResults} set of {@link org.groundwork.rs.dto.DtoOperationResult},
     *          holding the status of each operation
     * @throws CollageRestException
     */
    public DtoOperationResults delete(List<String> serviceNamesList, String hostName) throws CollageRestException {
        try {
            String serviceNames = makeCommaSeparatedParamFromList(serviceNamesList);
            String requestUrl = buildUrlWithPath(API_ROOT, encode(serviceNames) + "?hostName=" + encode(hostName));
            String requestDescription = "deleting services";
            DtoOperationResults results = clientRequest(HttpMethod.DELETE, requestUrl, requestDescription, new GenericType<DtoOperationResults>() {
            });
            return (results != null) ? results : new DtoOperationResults();
        }
        catch (UnsupportedEncodingException e) {
            throw new CollageRestException(e);
        }
    }

    /**
     * Lookup service description autocomplete suggestions for specified prefix.
     * A null, blank, or '*' wildcard prefix matches all names.
     *
     * @param prefix service description prefix
     * @return list of suggestions strings or empty list
     */
    public List<DtoName> autocomplete(String prefix) {
        return autoComplete(prefix, API_ROOT);
    }

    /**
     * Lookup service description autocomplete suggestions for specified prefix.
     * If a negative limit is specified, no limit will be applied to the
     * returned suggestion strings. Autocomplete suggestions are considered
     * unique based on their canonical names. In this case, the total number
     * of suggestions returned can exceed the limit since it is limiting the
     * number of unique canonical names. A null, blank, or '*' wildcard prefix
     * matches all names.
     *
     * @param prefix service description prefix
     * @param limit unique suggestions limit, (-1 for unlimited)
     * @return list of suggestions strings or empty list
     */
    public List<DtoName> autocomplete(String prefix, int limit) {
        return autoComplete(prefix, API_ROOT, limit);
    }

    /**
     *
     * This is a helper method that sets the required properties for acknowledging a service
     *
     * @param service service to acknowledge
     * @param user name of person doing the acknowledge
     * @param comment comment for the acknowledgement
     */
    public void acknowledge(DtoService service, String user, String comment) {
        Map<String, String> properties = service.getProperties();
        properties.put(PROP_IS_ACKNOWLEDGED, String.valueOf(true));
        properties.put(PROP_ACKNOWLEDGED_BY, user);
        properties.put(PROP_ACKNOWLEDGE_COMMENT, comment);
        service.setProperties(properties);

        post(new DtoServiceList(Collections.singletonList(service)));


    }

}
