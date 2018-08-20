package org.groundwork.rs.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.rs.dto.DtoDepthType;
import org.groundwork.rs.dto.DtoDevice;
import org.groundwork.rs.dto.DtoDeviceList;
import org.groundwork.rs.dto.DtoHost;
import org.groundwork.rs.dto.DtoMonitorServer;
import org.groundwork.rs.dto.DtoOperationResult;
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
 * on monitored devices in the Groundwork enterprise foundation server.
 * <p>
 * Operations supported are of the categories:
 * <ul>
 *     <li>lookup operations - lookup devices by their primary key device name</li>
 *     <li>list operations - list all devices in the system, with optional depth and paging parameters</li>
 *     <li>query operations - query for devices using an object query language with optional depth and paging parameters</li>
 *     <li>post operations - administrative batch operations to add or update devices. Works with lists of one or more devices</li>
 *     <li>delete operations - administrative batch operations to delete devices. Works with lists of one or more devices</li>
 * </ul>
 * <p>
 * Note that post and delete operations are not transactional. If a list of 10 devices are passed in to be added, and
 * if, for example, two devices fail to update, the other eight devices will still be persisted. The results for all
 * post and delete operations return the same {@link DtoOperationResults} list of {@link DtoOperationResult}, holding
 * the result (success, failure, warning) of each sub-operation.
 *
 * DeviceClient supports retrieval operations (lookup,list,query) of two depths: {@link DtoDepthType#Shallow} and {@link DtoDepthType#Deep}
 * Default depth is shallow. For deep operations, the following deep associated collections:
 * <ul>
 *   <li>{@link org.groundwork.rs.dto.DtoHost}</li>
 *   <li>{@link org.groundwork.rs.dto.DtoMonitorServer}</li>
 * </ul>
 *
 */
public class DeviceClient extends  BaseRestClient {

    protected static Log log = LogFactory.getLog(DeviceClient.class);
    private static final String API_ROOT_SINGLE = "/devices";
    private static final String API_ROOT = API_ROOT_SINGLE + "/";

    /**
     * Create a Device REST Client for performing query and administrative operations
     * on the devices in the Groundwork enterprise foundation database.
     * Deployment URL example:
     * <pre>http://server-name[:port]/foundation-webapp/api</pre>
     *
     * @param deploymentUrl the deployment root URL for all Rest operations
     */
    public DeviceClient(String deploymentUrl) {
        super(deploymentUrl);
    }

    /**
     * Create a Device REST Client for performing query and administrative operations
     * on the devices in the Groundwork enterprise foundation database.
     * Deployment URL example:
     * <pre>http://server-name[:port]/foundation-webapp/api</pre>
     * <p>Supported Media Types</p>
     * <ul>application/xml {@link MediaType#APPLICATION_XML_TYPE}</ul>
     * <ul>application/json {@link MediaType#APPLICATION_JSON_TYPE}</ul>
     * <p></p>
     * @param deploymentUrl the deployment root URL for all Rest operations
     * @param mediaType a valid, supported internet media type (MIME) supported
     */
    public DeviceClient(String deploymentUrl, MediaType mediaType) {
        this(deploymentUrl);
        this.mediaType = mediaType;
    }

    /**
     * Lookup a single device by its primary, unique key 'deviceName' returning a Device transfer object
     * of depth shallow, containing only the basic attributes, but not hosts or monitor servers.
     *
     * @param identification the unique identifier of the device to lookup
     * @return a shallow Device object or null if not found
     * @throws CollageRestException
     */
    public DtoDevice lookup(String identification) throws CollageRestException {
        return lookup(identification, DtoDepthType.Shallow);
    }

    /**
     * Lookup a single device by its primary, unique key 'identification' returning a device transfer object
     * of the specified depth. Shallow contains only the basic attributes, where as Deep returns basic attributes
     * plus the {@link DtoHost} and {@link DtoMonitorServer} collections.
     *
     * @param identification the unique identifier of the device to lookup
     * @param depthType specify either {@link DtoDepthType#Shallow} to retrieve basic attributes
     *                  or {@link DtoDepthType#Deep} to retrieve the objects plus its associated (shallow) objects
     * @return a device object of the specified depth or null if not found
     * @throws CollageRestException
     */
    public DtoDevice lookup(String identification, DtoDepthType depthType) throws CollageRestException {
        Response.Status status = Response.Status.SERVICE_UNAVAILABLE;
        ClientResponse<DtoDevice> response = null;
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                String url = buildLookupWithDepth(API_ROOT, identification, depthType);
                ClientRequest request = createClientRequest(url);
                request.accept(mediaType);
                response = request.get(new GenericType<DtoDevice>(){});
                if (response.getResponseStatus() == Response.Status.OK) {
                    DtoDevice device = response.getEntity(new GenericType<DtoDevice>() {});
                    return device;
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
        throw new CollageRestException(String.format("Exception executing lookup device (%s) with status code of %d, reason: %s",
                identification, status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }

    /**
     * Query for devices with an HQL query string returning a list of matching devices at the shallow depth.
     * Queries are only valid against the model represented by the Device data transfer object {@link DtoDevice}.
     * Associated objects can be queried such as the associated {@link DtoHost}. Queries are read only operations and
     * are limited to where and order by HQL expressions. Example query:
     * <pre>identification like '172.28.113%' and hosts.hostName = 'qa-sles-11-64-2' order by identification</pre>
     * <p>The names of the fields queried should match the names in the {@link DtoDevice} model. Any public field
     * can be queried using standard Java Beans naming convention for example a public method named
     * <code>getHostName()</code> would be queried as <code>hostName</code>.
     * The values of String fields should be single quoted.</p>
     *
     * @param query the HQL query string. This string should not be encoded, as the Rest Client will convert it for you
     * @return a list of one or more {@link DtoDevice} objects matching the query. If no records match the query,
     *         then an empty list is returned
     * @throws CollageRestException
     */
    public List<DtoDevice> query(String query) throws CollageRestException {
        return query(query, DtoDepthType.Shallow);
    }

    /**
     * Query for devices with an HQL query string returning a list of matching devices at the specified depth.
     * Queries are only valid against the model represented by the Device data transfer object {@link DtoDevice}.
     * Associated objects can be queried such as the associated {@link DtoHost}. Queries are read only operations and
     * are limited to where and order by HQL expressions. Example query:
     * <pre>identification like '172.28.113%' and hosts.hostName = 'qa-sles-11-64-2' order by identification</pre>
     * <p>The names of the fields queried should match the names in the {@link DtoDevice} model. Any public field
     * can be queried using standard Java Beans naming convention for example a public method named
     * <code>getHostName()</code> would be queried as <code>hostName</code>.
     * The values of String fields should be single quoted.</p>
     *
     * @param query the HQL query string. This string should not be encoded, as the Rest Client will convert it for you
     * @param depthType specify either {@link DtoDepthType#Shallow} to retrieve basic attributes
     *                  or {@link DtoDepthType#Deep} to retrieve the objects plus its associated (shallow) objects
     * @return a list of one or more {@link DtoDevice} objects matching the query. If no records match the query,
     *         then an empty list is returned
     * @throws CollageRestException
     */
    public List<DtoDevice> query(String query, DtoDepthType depthType) throws CollageRestException {
        return query(query, depthType, -1, -1);
    }

    /**
     * Query for devices with an HQL query string returning a list of matching devices at the shallow depth.
     * Queries are only valid against the model represented by the Device data transfer object {@link DtoDevice}.
     * Associated objects can be queried such as the associated {@link DtoHost}. Queries are read only operations and
     * are limited to where and order by HQL expressions. Example query:
     * <pre>identification like '172.28.113%' and hosts.hostName = 'qa-sles-11-64-2' order by identification</pre>
     * <p>The names of the fields queried should match the names in the {@link DtoDevice} model. Any public field
     * can be queried using standard Java Beans naming convention for example a public method named
     * <code>getHostName()</code> would be queried as <code>hostName</code>.
     * The values of String fields should be single quoted.</p>
     * <p>Paging parameters <code>first</code> and <code>count</code> are used together to retrieve a subset of
     * entities, paging from the <code>first</code> record, for a <code>count</code> of n records</p>
     *
     * @param query the HQL query string. This string should not be encoded, as the Rest Client will convert it for you
     * @param first the zero-based first record to retrieve to page over result set. Set to -1 to ignore this parameter
     * @param count the number of records to retrieve offset from the first parameter. Set to -1 to ignore this parameter
     * @return a list of one or more {@link DtoDevice} objects matching the query. If no records match the query,
     *         then an empty list is returned
     * @throws CollageRestException
     */
    public List<DtoDevice> query(String query, int first, int count) throws CollageRestException {
        return query(query, DtoDepthType.Shallow, first, count);
    }

    /**
     * Query for devices with an HQL query string returning a list of matching devices at the specified depth.
     * Queries are only valid against the model represented by the Device data transfer object {@link DtoDevice}.
     * Associated objects can be queried such as the associated {@link DtoHost}. Queries are read only operations and
     * are limited to where and order by HQL expressions. Example query:
     * <pre>identification like '172.28.113%' and hosts.hostName = 'qa-sles-11-64-2' order by identification</pre>
     * <p>The names of the fields queried should match the names in the {@link DtoDevice} model. Any public field
     * can be queried using standard Java Beans naming convention for example a public method named
     * <code>getHostName()</code> would be queried as <code>hostName</code>.
     * The values of String fields should be single quoted.</p>
     * <p>Paging parameters <code>first</code> and <code>count</code> are used together to retrieve a subset of
     * entities, paging from the <code>first</code> record, for a <code>count</code> of n records</p>
     *
     * @param query the HQL query string. This string should not be encoded, as the Rest Client will convert it for you
     * @param depthType specify either {@link DtoDepthType#Shallow} to retrieve basic attributes
     *                  or {@link DtoDepthType#Deep} to retrieve the objects plus its associated (shallow) objects
     * @param first the zero-based first record to retrieve to page over result set. Set to -1 to ignore this parameter
     * @param count the number of records to retrieve offset from the first parameter. Set to -1 to ignore this parameter
     * @return a list of one or more {@link DtoDevice} objects matching the query. If no records match the query,
     *         then an empty list is returned
     * @throws CollageRestException
     */
    public List<DtoDevice> query(String query, DtoDepthType depthType, int first, int count) throws CollageRestException {
        ClientResponse<List<DtoDevice>> response = null;
        Response.Status status = Response.Status.SERVICE_UNAVAILABLE;
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                String url = buildEncodedQuery(API_ROOT, query, depthType, first, count);
                ClientRequest request = createClientRequest(url);
                request.accept(mediaType);
                response = request.get(new GenericType<List<DtoDevice>>(){});
                if (response.getResponseStatus() == Response.Status.OK) {
                    DtoDeviceList devices = response.getEntity(new GenericType<DtoDeviceList>(){});
                    return devices.getDevices();
                }
                else if (response.getResponseStatus() == Response.Status.NOT_FOUND) {
                    return new ArrayList<DtoDevice>();
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
        throw new CollageRestException(String.format("Exception executing query devices (%s) with status code of %d, reason: %s",
                query, status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }

    /**
     * Return a list of all devices at the specified depth.
     * Shallow contains only the basic attributes, where as Deep returns basic attributes
     * plus the {@link DtoHost} and {@link DtoMonitorServer} collections.
     * <p>Paging parameters <code>first</code> and <code>count</code> are used together to retrieve a subset of
     * entities, paging from the <code>first</code> record, for a <code>count</code> of n records</p>
     *
     * @param depthType specify either {@link DtoDepthType#Shallow} to retrieve basic attributes
     *                  or {@link DtoDepthType#Deep} to retrieve the objects plus its associated (shallow) objects
     * @param first the zero-based first record to retrieve to page over result set. Set to -1 to ignore this parameter
     * @param count the number of records to retrieve offset from the first parameter. Set to -1 to ignore this parameter
     * @return a list of devices as the specified depth. If no records are found, then an empty list is returned
     * @throws CollageRestException
     */
    public List<DtoDevice> list(DtoDepthType depthType, int first, int count) throws CollageRestException {
        return query(null, depthType, first, count);
    }

    /**
     * Return a list of all devices at the default shallow depth.
     * The shallow list contains only the basic attributes, but not hosts or monitor servers.
     *
     * @return a list of all devices at the {@link DtoDepthType#Shallow} depth.
     *         If no records are found, then an empty list is returned
     * @throws CollageRestException
     */
    public List<DtoDevice> list() throws CollageRestException {
        return query(null, DtoDepthType.Shallow, -1, -1);
    }

    /**
     * Return a list of all devices at the specified depth.
     *
     * @param depthType specify either {@link DtoDepthType#Shallow} to retrieve basic attributes
     *                  or {@link DtoDepthType#Deep} to retrieve the objects plus its associated (shallow) objects
     * @return a list of all devices at the {@link DtoDepthType#Shallow} depth.
     *         If no records are found, then an empty list is returned
     * @throws CollageRestException
     */
    public List<DtoDevice> list(DtoDepthType depthType) throws CollageRestException {
        return query(null, depthType, -1, -1);
    }

    /**
     * Administrative batch operations to add or update devices. {@link org.groundwork.rs.dto.DtoDeviceList} is a list of one or
     * more {@link DtoDevice} objects. Each of these objects represent either a new device, or a device to be
     * updated. Any field that needs to be updated or added should be set on the DtoDevice.
     * The web service will determine if an update or insert is required by looking up the
     * device's primary key (the field <code>identification</code>) from the provided DtoDevice objects.
     * <p>
     * The post operation is not transactional. If a list of ten devices are passed in to be added, and
     * if, for example, two devices fail to update, the other eight devices will still be persisted. The results for all
     * post operations return a {@link DtoOperationResults} list of {@link DtoOperationResult}, holding
     * the status (success, failure, warning) of each sub-operation, see {@link DtoOperationResult#getStatus()}.
     * The {@link DtoOperationResult#getLocation()} method provides the exact URL of the persisted device.
     * </p>
     * @param updates  a list of one or more {@link DtoDevice} objects. Each object will either be updated
     *                 or inserted based on existence of the device's <code>identification</code> primary key.
     * @return a {@link DtoOperationResults} set of {@link DtoOperationResult}, holding the status of each device operation
     * @throws CollageRestException
     */
    public DtoOperationResults post(DtoDeviceList updates) throws CollageRestException {
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
        throw new CollageRestException(String.format("Exception executing post to devices with status code of %d, reason: %s",
                status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }

    /**
     * Administrative batch operations to delete devices. {@link org.groundwork.rs.dto.DtoDeviceList} is a list of one or
     * more {@link DtoDevice} objects. Each of these objects should only have the {@link DtoDevice#setIdentification(String)}
     * identification field set. The <code>identification</code> field is used to delete each device by primary key.
     * All other fields will be ignored.
     * <p>
     * The delete operation is not transactional. If a list of five devices are passed in to be deleted, and
     * if, for example, two devices fail to delete, the other three devices will still be deleted. The results for all
     * delete operations return a {@link DtoOperationResults} list of {@link DtoOperationResult}, , holding
     * the status (success, failure, warning) of each sub-operation, see {@link DtoOperationResult#getStatus()}.
     * </p>
     * @param deletes   a list of one or more {@link DtoDevice} objects. Only the
     *                  device's <code>identification</code> primary key will be considered when deleting.
     * @return a {@link DtoOperationResults} set of {@link DtoOperationResult}, holding the status of each device operation
     * @throws CollageRestException
     */
    public DtoOperationResults delete(DtoDeviceList deletes) throws CollageRestException {
        ClientResponse<DtoOperationResults> response = null;
        Response.Status status = Response.Status.SERVICE_UNAVAILABLE;
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                ClientRequest request = createClientRequest(build(API_ROOT_SINGLE));
                request.accept(mediaType);
                request.body(mediaType, deletes);
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
        throw new CollageRestException(String.format("Exception executing delete to devices with status code of %d, reason: %s",
                status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }

    /**
     * Administrative batch operations to delete devices. Takes a list of one or more device primary key identifier strings,
     * and deletes each device by primary key.
     * This 'device identifier' corresponds with the {@link DtoDevice#getIdentification()} field.
     * <p>
     * The delete operation is not transactional. If a list of five devices are passed in to be deleted, and
     * if, for example, two devices fail to delete, the other three devices will still be deleted. The results for all
     * delete operations return a {@link DtoOperationResults} list of {@link DtoOperationResult}, , holding
     * the status (success, failure, warning) of each sub-operation, see {@link DtoOperationResult#getStatus()}.
     * </p>
     * @param deviceIdentifications a list of strings of unique device identification field primary keys
     * @return a {@link DtoOperationResults} set of {@link DtoOperationResult}, holding the status of each device operation
     * @throws CollageRestException
     */
    public DtoOperationResults delete(List<String> deviceIdentifications) throws CollageRestException {
        if (deviceIdentifications.size() > 50) {
            DtoDeviceList deletes = new DtoDeviceList();
            for (String id : deviceIdentifications) {
                deletes.add(new DtoDevice(id));
            }
            return delete(deletes);
        }
        ClientResponse<DtoOperationResults> response = null;
        Response.Status status = Response.Status.SERVICE_UNAVAILABLE;
        String deviceNames = "";
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                deviceNames = makeCommaSeparatedParamFromList(deviceIdentifications);
                ClientRequest request = createClientRequest(buildUrlWithPath(API_ROOT, deviceNames));
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
        throw new CollageRestException(String.format("Exception executing delete to devices (%s) with status code of %d, reason: %s",
                deviceNames, status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }

    /**
     * Administrative operation to delete a single device. Takes a primary key identifier string,
     * and deletes a single device by primary key.
     * This 'device identifier' corresponds with the {@link DtoDevice#getIdentification()} field.
     *
     * @param identification the unique, primary identification field for this device
     * @return a {@link DtoOperationResults} set of {@link DtoOperationResult}, holding the status of each device operation
     * @throws CollageRestException
     */
    public DtoOperationResults delete(String identification) throws CollageRestException {
        List<String> names = new ArrayList<String>();
        names.add(identification);
        return delete(names);
    }

}
