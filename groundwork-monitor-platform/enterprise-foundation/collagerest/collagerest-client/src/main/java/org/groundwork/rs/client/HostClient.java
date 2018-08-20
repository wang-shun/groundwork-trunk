package org.groundwork.rs.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.rs.dto.DtoDepthType;
import org.groundwork.rs.dto.DtoHost;
import org.groundwork.rs.dto.DtoHostList;
import org.groundwork.rs.dto.DtoName;
import org.groundwork.rs.dto.DtoOperationResults;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.util.GenericType;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The Java REST Client for performing query and administrative operations
 * on monitored Hosts in the Groundwork enterprise foundation server.
 * <p>
 * Operations supported are of the categories:
 * <ul>
 *     <li>lookup operations - lookup hosts by their primary key host name</li>
 *     <li>list operations - list all hosts in the system, with optional depth and paging parameters</li>
 *     <li>query operations - query for hosts using an object query language with optional depth and paging parameters</li>
 *     <li>post operations - administrative batch operations to add or update hosts. Works with lists of one or more hosts</li>
 *     <li>delete operations - administrative batch operations to delete hosts. Works with lists of one or more hosts</li>
 * </ul>
 * <p>
 * Note that post and delete operations are not transactional. If a list of 10 hosts are passed in to be added, and
 * if, for example, two hosts fail to update, the other eight hosts will still be persisted. The results for all
 * post and delete operations return the same {@link DtoOperationResults} list of
 * {@link org.groundwork.rs.dto.DtoOperationResult}, holding
 * the result (success, failure, warning) of each sub-operation.
 *
 * HostClient supports retrieval operations (lookup,list,query) of three depths:
 * <ul>
 *    <li>{@link DtoDepthType#Shallow}</li>
 *    <li>{@link DtoDepthType#Deep}</li>
 *    <li>{@link DtoDepthType#Simple}</li>
 * </ul>
 * Default depth is shallow, returning all attributes and properties. Simple depth returns names and descriptions only.
 * For deep operations, the following associated objects are retrieved:
 * <ul>
 *   <li>{@link org.groundwork.rs.dto.DtoDevice}</li>
 *   <li>{@link org.groundwork.rs.dto.DtoHostStatus}</li>
 *   <li>{@link org.groundwork.rs.dto.DtoMonitorStatus}</li>
 *   <li>{@link org.groundwork.rs.dto.DtoCheckType}</li>
 *   <li>{@link org.groundwork.rs.dto.DtoStateType}</li>
 *   <li>{@link org.groundwork.rs.dto.DtoApplicationType}</li>
 * </ul>
 * plus the following deep associated collections:
 * <ul>
 *   <li>{@link org.groundwork.rs.dto.DtoService}</li>
 *   <li>{@link org.groundwork.rs.dto.DtoHostGroup}</li>
 *   <li>{@link org.groundwork.rs.dto.DtoStateStatistic}</li>
 * </ul>
 * <p>Dynamic Properties are retrieved at both Deep and Shallow depths, but not at simple depth. Properties are returned
 * in a name-value pair map, accessible on the Host DTO via {@link org.groundwork.rs.dto.DtoPropertiesBase#getProperties()}.
 * Property access methods are provided for the simple types of String, Boolean, Int, Long, Double, and Date.</p>
 */
public class HostClient extends BaseRestClient {

    protected static Log log = LogFactory.getLog(HostClient.class);
    private static final String API_ROOT_SINGLE = "/hosts";
    private static final String API_ROOT = API_ROOT_SINGLE + "/";
    private static final String RPC_RENAME = "rename";
    private static final String OLD_HOST_NAME = "oldHostName";
    private static final String NEW_HOST_NAME = "newHostName";
    private static final String DESCRIPTION = "description";
    private static final String DEVICE_IDENTIFICATION = "deviceIdentification";
    private static final String API_FILTER_BY_HOSTS = API_ROOT + "filter/hostgroups";

    /**
     * Create a Host REST Client for performing query and administrative operations
     * on the hosts in the Groundwork enterprise foundation database.
     * Deployment URL example:
     * <pre>http://server-name[:port]/foundation-webapp/api</pre>
     *
     * @param deploymentUrl the deployment root URL for all Rest operations
     */
    public HostClient(String deploymentUrl) {
        super(deploymentUrl);
    }

    /**
     * Create a Host REST Client for performing query and administrative operations
     * on the hosts in the Groundwork enterprise foundation database.
     * Deployment URL example:
     * <pre>http://server-name[:port]/foundation-webapp/api</pre>
     * <p>Supported Media Types</p>
     * <ul>application/xml {@link MediaType#APPLICATION_XML_TYPE}</ul>
     * <ul>application/json {@link MediaType#APPLICATION_JSON_TYPE}</ul>
     * <p></p>
     * @param deploymentUrl the deployment root URL for all Rest operations
     * @param mediaType a valid, supported internet media type (MIME) supported
     */
    public HostClient(String deploymentUrl, MediaType mediaType) {
        this(deploymentUrl);
        this.mediaType = mediaType;
    }

    /**
     * Lookup a single host by its primary, unique key 'hostName' returning a Host transfer object
     * of depth shallow, containing only the basic attributes, but not deep values like Device, Services, HostGroups
     *
     * @param hostName the unique name of the Host to lookup
     * @return a shallow Host object or null if not found
     * @throws CollageRestException
     */
    public DtoHost lookup(String hostName) throws CollageRestException {
        return lookup(hostName, DtoDepthType.Shallow);
    }

    /**
     * Lookup a single Host by its primary, unique key 'hostName' returning a Host transfer object
     * of the specified depth. Three depths are supported, Shallow (default), Simple, and Deep.
     * Shallow contains only the basic attributes plus properties, Simple contains only
     * names and description attributes, and Deep returns basic attributes
     * plus all associated 1:1 objects, and all associated 1:n objects (lists).
     *
     *  {@link org.groundwork.rs.dto.DtoMonitorServer} collections.
     * @param hostName the unique name of the Host to lookup
     * @param depthType specify either {@link DtoDepthType#Shallow} to retrieve basic attributes plus properties,
     *                  {@link DtoDepthType#Simple} to retrieve name and descriptions only,
     *                  or {@link DtoDepthType#Deep} to retrieve the objects plus its associated (shallow) objects
     * @return a Host object of the specified depth or null if not found
     * @throws CollageRestException
     */
    public DtoHost lookup(String hostName, DtoDepthType depthType) throws CollageRestException {
        try {
            String requestUrl = buildLookupWithDepth(API_ROOT, hostName, depthType);
            String requestDescription = String.format("lookup host [%s]", hostName);
            return clientRequest(requestUrl, requestDescription, new GenericType<DtoHost>(){});
        }
        catch (Exception e) {
            throw new CollageRestException(e);
        }
    }

    /**
     * Query for Hosts with an HQL query string returning a list of Host transfer objects at the shallow depth. Shallow
     * depth includes all attributes and properties of the host, but not associated objects.
     * Queries are only valid against the model represented by the Host data transfer object {@link org.groundwork.rs.dto.DtoHost}.
     * Associated objects can be queried such as the associated {@link org.groundwork.rs.dto.DtoApplicationType}.
     * Queries are read only operations and are limited to where and order by HQL expressions. Example query on a property:
     * <pre>(property.ExecutionTime between 10 and 3500 and (monitorStatus <> 'UP')) order by property.ExecutionTime</pre>
     * <p>The names of the fields queried should match the names in the {@link org.groundwork.rs.dto.DtoHost} model.
     * Any public field can be queried using standard Java Beans naming convention for example a public method named
     * <code>getHostName()</code> would be queried as <code>hostName</code>.
     * The values of String fields should be single quoted. Hosts have dynamic properties,
     * which can be queried with the prefix <code>property</code> for example <code>property.ExecutionTime</code></p>
     *
     * @param query the HQL query string. This string should not be encoded, as the Rest Client will convert it for you
     * @return a list of one or more {@link org.groundwork.rs.dto.DtoHost} objects matching the query. If no records match the query,
     *         then an empty list is returned
     * @throws CollageRestException
     */
    public List<DtoHost> query(String query) throws CollageRestException {
        return query(query, DtoDepthType.Shallow);
    }

    /**
     * Query for Hosts with an HQL query string returning a list of Host transfer objects at the specified depth.
     * Three depths are supported, Shallow (default), Simple, and Deep.
     * Shallow contains only the basic attributes plus properties, Simple contains only
     * names and description attributes, and Deep returns basic attributes
     * plus all associated 1:1 objects, and all associated 1:n objects (lists).
     * Queries are only valid against the model represented by the Host data transfer object {@link org.groundwork.rs.dto.DtoHost}.
     * Associated objects can be queried such as the associated {@link org.groundwork.rs.dto.DtoApplicationType}.
     * Queries are read only operations and are limited to where and order by HQL expressions. Example query on a property:
     * <pre>(property.ExecutionTime between 10 and 3500 and (monitorStatus <> 'UP')) order by property.ExecutionTime</pre>
     * <p>The names of the fields queried should match the names in the {@link org.groundwork.rs.dto.DtoHost} model.
     * Any public field can be queried using standard Java Beans naming convention for example a public method named
     * <code>getHostName()</code> would be queried as <code>hostName</code>.
     * The values of String fields should be single quoted. Hosts have dynamic properties,
     * which can be queried with the prefix <code>property</code> for example <code>property.ExecutionTime</code></p>
     *
     * @param query the HQL query string. This string should not be encoded, as the Rest Client will convert it for you
     * @param depthType specify either {@link DtoDepthType#Shallow} to retrieve basic attributes plus properties,
     *                  {@link DtoDepthType#Simple} to retrieve name and descriptions only,
     *                  or {@link DtoDepthType#Deep} to retrieve the objects plus its associated (shallow) objects
     * @return a list of one or more {@link org.groundwork.rs.dto.DtoHost} objects matching the query. If no records match the query,
     *         then an empty list is returned
     * @throws CollageRestException
     */
    public List<DtoHost> query(String query, DtoDepthType depthType) throws CollageRestException {
        return query(query, depthType, -1, -1);
    }

    /**
     * Query for Hosts with an HQL query string returning a list of Host transfer objects at the shallow depth. Shallow
     * depth includes all attributes and properties of the host, but not associated objects.
     * Queries are only valid against the model represented by the Host data transfer object {@link org.groundwork.rs.dto.DtoHost}.
     * Associated objects can be queried such as the associated {@link org.groundwork.rs.dto.DtoApplicationType}.
     * Queries are read only operations and are limited to where and order by HQL expressions. Example query on a property:
     * <pre>(property.ExecutionTime between 10 and 3500 and (monitorStatus <> 'UP')) order by property.ExecutionTime</pre>
     * <p>The names of the fields queried should match the names in the {@link org.groundwork.rs.dto.DtoHost} model.
     * Any public field can be queried using standard Java Beans naming convention for example a public method named
     * <code>getHostName()</code> would be queried as <code>hostName</code>.
     * The values of String fields should be single quoted. Hosts have dynamic properties,
     * which can be queried with the prefix <code>property</code> for example <code>property.ExecutionTime</code></p>
     * <p>Paging parameters <code>first</code> and <code>count</code> are used together to retrieve a subset of
     * entities, paging from the <code>first</code> record, for a <code>count</code> of n records</p>
     *
     * @param query the HQL query string. This string should not be encoded, as the Rest Client will convert it for you
     * @param first the zero-based first record to retrieve to page over result set. Set to -1 to ignore this parameter
     * @param count the number of records to retrieve offset from the first parameter. Set to -1 to ignore this parameter
     * @return a list of one or more {@link org.groundwork.rs.dto.DtoHost} objects matching the query. If no records match the query,
     *         then an empty list is returned
     * @throws CollageRestException
     */
    public List<DtoHost> query(String query, int first, int count) throws CollageRestException {
        return query(query, DtoDepthType.Shallow, first, count);
    }

    /**
     * Query for Hosts with an HQL query string returning a list of Host transfer objects at the specified depth. Shallow
     * depth includes all attributes and properties of the host, but not associated objects. Simple contains only
     * names and description attributes, and Deep returns basic attributes
     * plus all associated 1:1 objects, and all associated 1:n objects (lists).
     * Queries are only valid against the model represented by the Host data transfer object {@link org.groundwork.rs.dto.DtoHost}.
     * Associated objects can be queried such as the associated {@link org.groundwork.rs.dto.DtoApplicationType}.
     * Queries are read only operations and are limited to where and order by HQL expressions. Example query on a property:
     * <pre>(property.ExecutionTime between 10 and 3500 and (monitorStatus <> 'UP')) order by property.ExecutionTime</pre>
     * <p>The names of the fields queried should match the names in the {@link org.groundwork.rs.dto.DtoHost} model.
     * Any public field can be queried using standard Java Beans naming convention for example a public method named
     * <code>getHostName()</code> would be queried as <code>hostName</code>.
     * The values of String fields should be single quoted. Hosts have dynamic properties,
     * which can be queried with the prefix <code>property</code> for example <code>property.ExecutionTime</code></p>
     * <p>Paging parameters <code>first</code> and <code>count</code> are used together to retrieve a subset of
     * entities, paging from the <code>first</code> record, for a <code>count</code> of n records</p>
     *
     * @param query the HQL query string. This string should not be encoded, as the Rest Client will convert it for you
     * @param depthType specify either {@link DtoDepthType#Shallow} to retrieve basic attributes plus properties,
     *                  {@link DtoDepthType#Simple} to retrieve name and descriptions only,
     *                  or {@link DtoDepthType#Deep} to retrieve the objects plus its associated (shallow) objects
     * @param first the zero-based first record to retrieve to page over result set. Set to -1 to ignore this parameter
     * @param count the number of records to retrieve offset from the first parameter. Set to -1 to ignore this parameter
     * @return a list of one or more {@link org.groundwork.rs.dto.DtoHost} objects matching the query. If no records match the query,
     *         then an empty list is returned
     * @throws CollageRestException
     */
    public List<DtoHost> query(String query, DtoDepthType depthType, int first, int count) throws CollageRestException {
        try {
            String requestUrl = buildEncodedQuery(API_ROOT, query, depthType, first, count);
            String requestDescription = String.format("%s hosts [%s]", (query == null) ? "list" : "query",  (query == null) ? "-" : query);
            DtoHostList dtoHosts = clientRequest(requestUrl, requestDescription, new GenericType<DtoHostList>(){});
            return ((dtoHosts != null) ? dtoHosts.getHosts() : Collections.EMPTY_LIST);
        }
        catch (Exception e) {
            throw new CollageRestException(e);
        }
    }

    /**
     * Return a list of all hosts at the specified depth.
     * Three depths are supported, Shallow (default), Simple, and Deep.
     * Shallow contains only the basic attributes plus properties, Simple contains only
     * names and description attributes, and Deep returns basic attributes
     * plus all associated 1:1 objects, and all associated 1:n objects (lists).
     * <p>Paging parameters <code>first</code> and <code>count</code> are used together to retrieve a subset of
     * entities, paging from the <code>first</code> record, for a <code>count</code> of n records</p>
     *
     * @param depthType specify either {@link DtoDepthType#Shallow} to retrieve basic attributes plus properties,
     *                  {@link DtoDepthType#Simple} to retrieve name and descriptions only,
     *                  or {@link DtoDepthType#Deep} to retrieve the objects plus its associated (shallow) objects
     * @param first the zero-based first record to retrieve to page over result set. Set to -1 to ignore this parameter
     * @param count the number of records to retrieve offset from the first parameter. Set to -1 to ignore this parameter
     * @return a list of all hosts at the {@link DtoDepthType#Shallow} depth.
     *         If no records are found, then an empty list is returned
     * @throws CollageRestException
     */
    public List<DtoHost> list(DtoDepthType depthType, int first, int count) throws CollageRestException {
        return query(null, depthType, first, count);
    }

    /**
     * Return a list of all hosts at the default shallow depth.
     * The shallow list contains only the basic attributes and dynamic properties, but no associated objects.
     *
     * @return a list of all hosts at the {@link DtoDepthType#Shallow} depth.
     *         If no records are found, then an empty list is returned
     * @throws CollageRestException
     */
    public List<DtoHost> list() throws CollageRestException {
        return query(null, DtoDepthType.Shallow, -1, -1);
    }

    /**
     * Return a list of all hosts at the specified depth.
     * Three depths are supported, Shallow (default), Simple, and Deep.
     * Shallow contains only the basic attributes plus properties, Simple contains only
     * names and description attributes, and Deep returns basic attributes
     * plus all associated 1:1 objects, and all associated 1:n objects (lists).
     *
     * @param depthType specify either {@link DtoDepthType#Shallow} to retrieve basic attributes plus properties,
     *                  {@link DtoDepthType#Simple} to retrieve name and descriptions only,
     *                  or {@link DtoDepthType#Deep} to retrieve the objects plus its associated (shallow) objects
     * @return a list of all hosts at the {@link DtoDepthType#Shallow} depth.
     *         If no records are found, then an empty list is returned
     * @throws CollageRestException
     */
    public List<DtoHost> list(DtoDepthType depthType) throws CollageRestException {
        return query(null, depthType, -1, -1);
    }

    /**
     * Administrative batch operations to add or update hosts. {@link org.groundwork.rs.dto.DtoHostList} is a list of one or
     * more {@link org.groundwork.rs.dto.DtoHost} objects. Each of these objects represent either a new host, or a host to be
     * updated. Any field that needs to be updated or added should be set on the DtoHost object.
     * The web service will determine if an update or insert is required by looking up the
     * host's primary key (the field <code>hostName</code>) from the provided DtoHost objects.
     * <p>
     * The post operation is not transactional. If a list of ten hosts are passed in to be added, and
     * if, for example, two hosts fail to update, the other eight hosts will still be persisted. The results for all
     * post operations return a {@link DtoOperationResults} list of {@link org.groundwork.rs.dto.DtoOperationResult}, holding
     * the status (success, failure, warning) of each sub-operation, see {@link org.groundwork.rs.dto.DtoOperationResult#getStatus()}.
     * The {@link org.groundwork.rs.dto.DtoOperationResult#getLocation()} method provides the exact URL of the persisted host.
     * </p>
     * @param updates  a list of one or more {@link org.groundwork.rs.dto.DtoHost} objects. Each object will either be updated
     *                 or inserted based on existence of the host's <code>hostName</code> primary key.
     * @param merge    merge hosts with matching but different names
     * @param async    should this post operation be synchronous or asynchronous
     * @return a {@link DtoOperationResults} set of {@link org.groundwork.rs.dto.DtoOperationResult},
     *         holding the status of each host operation
     * @throws CollageRestException
     */
    public DtoOperationResults post(DtoHostList updates, boolean merge, boolean async) throws CollageRestException {
        String requestUrl = buildPostAsyncMergeURL(API_ROOT, API_ROOT_SINGLE, merge, async);
        String requestDescription = "posting hosts";
        DtoOperationResults results = clientRequest(HttpMethod.POST, requestUrl, updates, requestDescription, new GenericType<DtoOperationResults>() {});
        return (results != null) ? results : new DtoOperationResults();
    }

    public DtoOperationResults post(DtoHostList updates, boolean merge) throws CollageRestException {
        return post(updates, merge, false);
    }

    public DtoOperationResults post(DtoHostList updates) throws CollageRestException {
        return post(updates, true);
    }

    public DtoOperationResults postAsync(DtoHostList updates, boolean merge) throws CollageRestException {
        return post(updates, merge, true);
    }

    public DtoOperationResults postAsync(DtoHostList updates) throws CollageRestException {
        return postAsync(updates, true);
    }

    /**
     * Administrative batch operations to delete a list of hosts. {@link org.groundwork.rs.dto.DtoHostList} is a list of one or
     * more {@link org.groundwork.rs.dto.DtoHost} objects. Each of these objects should only have the
     * {@link org.groundwork.rs.dto.DtoHost#setHostName(String)}
     * hostName field set. The <code>hostName</code> field is used to delete each host by primary key.
     * All other fields will be ignored.
     * <p>
     * The delete operation is not transactional. If a list of five hosts are passed in to be deleted, and
     * if, for example, two hosts fail to delete, the other three hosts will still be deleted. The results for all
     * delete operations return a {@link DtoOperationResults} list of {@link org.groundwork.rs.dto.DtoOperationResult}, , holding
     * the status (success, failure, warning) of each sub-operation, see {@link org.groundwork.rs.dto.DtoOperationResult#getStatus()}.
     * </p>
     * @param deletes   a list of one or more {@link org.groundwork.rs.dto.DtoHost} objects. Only the
     *                  host's <code>hostName</code> primary key will be considered when deleting.
     * @return a {@link DtoOperationResults} set of {@link org.groundwork.rs.dto.DtoOperationResult},
     *          holding the status of each host operation
     * @throws CollageRestException
     */
    public DtoOperationResults delete(DtoHostList deletes) throws CollageRestException {
        String requestUrl = build(API_ROOT_SINGLE);
        String requestDescription = "delete hosts";
        DtoOperationResults results = clientRequest(HttpMethod.DELETE, requestUrl, deletes, requestDescription, new GenericType<DtoOperationResults>(){});
        return (results != null) ? results : new DtoOperationResults();
    }

    /**
     * Administrative operation to delete a single Host. Takes a primary key hostName string,
     * and deletes a single Host by primary key.
     * This 'hostName' corresponds with the {@link org.groundwork.rs.dto.DtoHost#getHostName()} field.
     *
     * @param hostName the unique, primary identification field for this host
     * @return a {@link DtoOperationResults} set of {@link org.groundwork.rs.dto.DtoOperationResult},
     *              holding the status of each Host operation
     * @throws CollageRestException
     */
    public DtoOperationResults delete(String hostName) throws CollageRestException {
        List<String> names = new ArrayList<String>();
        names.add(hostName);
        return delete(names);
    }

    /**
     * Administrative batch operations to delete a list of hosts by primary key hostName.
     * <p>
     * The delete operation is not transactional. If a list of five hosts are passed in to be deleted, and
     * if, for example, two hosts fail to delete, the other three hosts will still be deleted. The results for all
     * delete operations return a {@link DtoOperationResults} list of {@link org.groundwork.rs.dto.DtoOperationResult}, , holding
     * the status (success, failure, warning) of each sub-operation, see {@link org.groundwork.rs.dto.DtoOperationResult#getStatus()}.
     * </p>
     * @param hostNamesList   a list of one or more host names primary keys.
     * @return a {@link DtoOperationResults} set of {@link org.groundwork.rs.dto.DtoOperationResult},
     *          holding the status of each host operation
     * @throws CollageRestException
     */
    public DtoOperationResults delete(List<String> hostNamesList) throws CollageRestException {
        DtoHostList deletes = new DtoHostList();
        for (String id : hostNamesList) {
            deletes.add(new DtoHost(id));
        }
        return delete(deletes);
    }

    /**
     * Administrative operation to rename a host returning a new copy of the renamed host.
     * <p>
     * The old and new host name are required parameters. Description is optional, and can be used to override the
     * host description field. DeviceIdentification is optional, and can be used to override the associated device
     * identification. Use the deviceIdentification parameter when you have matching host/device pairs.
     * </p>
     * <p>
     * This operation can fail with duplicate key errors not only on the host table, but also on the hostIdentity table
     * and the device table. Also, it can return null when the old host name is not found
     * </p>
     * @param oldHostName the current name of the host record
     * @param newHostName the new name of the host record
     * @param description the optional description of this host. Setting to null indicates to not update the description
     * @param deviceIdentification the optional deviceIdentification of the associated device. Setting to null
     *                             indicates to not update the description
     * @return a {@link DtoHost} the updated host record with new fields all set. Can return null for old host name not found
     * @throws CollageRestException
     */
    public DtoHost rename(String oldHostName, String newHostName, String description, String deviceIdentification) throws CollageRestException {
        if (oldHostName == null)
            throw new CollageRestException("Required parameter oldHostname not provided");
        if (newHostName == null)
            throw new CollageRestException("Required parameter newHostname not provided");
        try {
            List<String> names = new ArrayList<>();
            List<String> values = new ArrayList<>();
            names.add(OLD_HOST_NAME);
            values.add(oldHostName);
            names.add(NEW_HOST_NAME);
            values.add(newHostName);
            if (description != null) {
                names.add(DESCRIPTION);
                values.add(description);
            }
            if (deviceIdentification != null) {
                names.add(DEVICE_IDENTIFICATION);
                values.add(deviceIdentification);
            }
            String[] nameArray = new String[names.size()];
            nameArray = names.toArray(nameArray);
            String[] valueArray = new String[values.size()];
            valueArray = values.toArray(valueArray);
            String requestUrl = buildUrlWithPathAndQueryParams(API_ROOT, RPC_RENAME, buildEncodedQueryParams(nameArray, valueArray));
            String requestDescription = String.format("rename host (%s) to (%s)", oldHostName, newHostName);
            return clientRequest(HttpMethod.PUT, requestUrl, requestDescription, new GenericType<DtoHost>() {
            });
        }
        catch (UnsupportedEncodingException e) {
            throw new CollageRestException(e);
        }
    }


    /**
     * Lookup host name autocomplete suggestions for specified prefix.
     * A null, blank, or '*' wildcard prefix matches all names.
     *
     * @param prefix host name prefix
     * @return list of suggestions strings or empty list
     */
    public List<DtoName> autocomplete(String prefix) {
        return autoComplete(prefix, API_ROOT);
    }

    /**
     * Lookup host name autocomplete suggestions for specified prefix.
     * If a negative limit is specified, no limit will be applied to the
     * returned suggestion strings. Autocomplete suggestions are considered
     * unique based on their canonical names. In this case, the total number
     * of suggestions returned can exceed the limit since it is limiting the
     * number of unique canonical names. A null, blank, or '*' wildcard prefix
     * matches all names.
     *
     * @param prefix host name prefix
     * @param limit unique suggestions limit, (-1 for unlimited)
     * @return list of suggestions strings or empty list
     */
    public List<DtoName> autocomplete(String prefix, int limit) {
        return autoComplete(prefix, API_ROOT, limit);
    }

    /**
     * Return a list of all hosts at the specified depth filtered by a list of Host Group Names
     * Three depths are supported, Shallow (default), Simple, and Deep.
     * Shallow contains only the basic attributes plus properties, Simple contains only
     * names and description attributes, and Deep returns basic attributes
     * plus all associated 1:1 objects, and all associated 1:n objects (lists).
     *
     * @param depthType specify either {@link DtoDepthType#Shallow} to retrieve basic attributes plus properties,
     *                  {@link DtoDepthType#Simple} to retrieve name and descriptions only,
     *                  or {@link DtoDepthType#Deep} to retrieve the objects plus its associated (shallow) objects
     * @return a list of all hosts at the {@link DtoDepthType#Shallow} depth.
     *         If no records are found, then an empty list is returned
     * @throws CollageRestException
     */
    public List<DtoHost> filterByHostGroups(List<String> hostGroupNamesList, DtoDepthType depthType) {
        ClientResponse<DtoHostList> response = null;
        String hostGroupNames = "";

        try {
            hostGroupNames = makeCommaSeparatedParamFromList(hostGroupNamesList);
            String params = buildEncodedQueryParamsWithDepth(
                    new String[]{"hostGroupNames"},
                    new String[]{hostGroupNames},
                    depthType);
            String requestUrl = buildUrlWithQueryParams(API_FILTER_BY_HOSTS, params);
            String requestDescription = "filter by hostgroups";
            DtoHostList dtoHosts = clientRequest(requestUrl, requestDescription, new GenericType<DtoHostList>(){});
            return ((dtoHosts != null) ? dtoHosts.getHosts() : Collections.EMPTY_LIST);
        }
        catch (Exception e) {
            throw new CollageRestException(e);
        }

    }
}
