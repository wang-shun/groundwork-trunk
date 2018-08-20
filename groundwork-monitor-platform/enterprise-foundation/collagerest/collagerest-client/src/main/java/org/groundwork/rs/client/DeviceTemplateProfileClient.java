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
import org.groundwork.rs.dto.DtoDeviceTemplateProfile;
import org.groundwork.rs.dto.DtoDeviceTemplateProfileList;
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
 * DeviceTemplateProfileClient
 *
 * The Java REST Client for performing query and administrative operations
 * on DeviceTemplateProfiles in the Groundwork enterprise foundation server.
 * <p>
 * Operations supported are of the categories:
 * <ul>
 *     <li>lookup operations - lookup device template profiles by device identification</li>
 *     <li>list operations - list all device template profiles in the system with optional paging parameters</li>
 *     <li>query operations - query for device template profiles using an object query language with optional paging parameters</li>
 *     <li>post operations - administrative batch operations to add or update device template profiles. Works with lists of one or more device template profiles</li>
 *     <li>clear operations - administrative batch operations to clear device template profiles. Works with lists of one or more device template profiles</li>
 *     <li>delete operations - administrative batch operations to delete device template profiles. Works with lists of one or more device template profiles</li>
 * </ul>
 * <p>
 * Note that post, clear, and delete operations are not transactional. If a list of 10 device template profiles are passed in to
 * be added, and if, for example, two device template profiles fail to update, the other eight device template profiles
 * will still be persisted. The results for all post, clear, and delete operations return the same {@link org.groundwork.rs.dto.DtoOperationResults}
 * list of {@link org.groundwork.rs.dto.DtoOperationResult}, holding the result (success, failure, warning) of each sub-operation.
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public class DeviceTemplateProfileClient extends BaseRestClient {

    private static Log log = LogFactory.getLog(DeviceTemplateProfileClient.class);

    private static final String API_ROOT_SINGLE = "/devicetemplateprofiles";
    private static final String API_ROOT = API_ROOT_SINGLE + "/";

    private static final int MAX_COUNT = 1024;

    private static final String [] CLEAR_NAMES = new String[]{"clear"};
    private static final String [] CLEAR_VALUES = new String[]{"true"};

    /**
     * Create a DeviceTemplateProfile REST Client for performing query and administrative operations
     * on device template profiles in the Groundwork enterprise foundation server.
     * <p></p>
     * Deployment URL example:
     * <pre>http://server-name[:port]/foundation-webapp/api</pre>
     * <p></p>
     * @param deploymentUrl the deployment root URL for all Rest operations
     */
    public DeviceTemplateProfileClient(String deploymentUrl) {
        super(deploymentUrl);
    }

    /**
     * Create a DeviceTemplateProfile REST Client for performing query and administrative operations
     * on device template profiles in the Groundwork enterprise foundation server.
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
    public DeviceTemplateProfileClient(String deploymentUrl, MediaType mediaType) {
        this(deploymentUrl);
        this.mediaType = mediaType;
    }

    /**
     * Lookup a single DeviceTemplateProfile by its device identifications returning a
     * DeviceTemplateProfile transfer object.
     *
     * @param deviceIdentification the device identification of the DeviceTemplateProfile to lookup
     * @return the returned DeviceTemplateProfile transfer object or null if not found
     * @throws CollageRestException
     */
    public DtoDeviceTemplateProfile lookup(String deviceIdentification) throws CollageRestException {
        Response.Status status = Response.Status.SERVICE_UNAVAILABLE;
        ClientResponse<DtoDeviceTemplateProfile> response = null;
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                String url = buildUrlWithPath(API_ROOT, deviceIdentification);
                ClientRequest request = createClientRequest(url);
                request.accept(mediaType);
                response = request.get(new GenericType<DtoDeviceTemplateProfile>(){});
                if (response.getResponseStatus() == Response.Status.OK) {
                    return response.getEntity(new GenericType<DtoDeviceTemplateProfile>() {});
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
        throw new CollageRestException(String.format("Exception executing lookup devicetemplateprofiles (%s) with status code of %d, reason: %s", deviceIdentification, status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }

    /**
     * Return a list of all DeviceTemplateProfiles.
     *
     * @return a list of all device template profiles or an empty list.
     * @throws CollageRestException
     */
    public List<DtoDeviceTemplateProfile> list() throws CollageRestException {
        return list(-1, -1);
    }

    /**
     * Return a list of all DeviceTemplateProfiles.
     * <p>Paging parameters <code>first</code> and <code>count</code> are used together to retrieve a subset of
     * entities, paging from the <code>first</code> record, for a <code>count</code> of n records.</p>
     *
     * @param first the zero-based first record to retrieve to page over result set. Set to -1 to ignore this parameter
     * @param count the number of records to retrieve offset from the first parameter. Set to -1 to ignore this parameter
     * @return a list of all device template profiles or an empty list.
     * @throws CollageRestException
     */
    public List<DtoDeviceTemplateProfile> list(int first, int count) throws CollageRestException {
        return query(null, first, count);
    }

    /**
     * Query for DeviceTemplateProfiles with an HQL query string returning a list of DeviceTemplateProfile transfer objects.
     * Queries are only valid against the model represented by the DeviceTemplateProfile data transfer object {@link org.groundwork.rs.dto.DtoDeviceTemplateProfile}.
     * Queries are read only operations and are limited to where and order by HQL expressions.
     * <p>The names of the fields queried should match the names in the {@link org.groundwork.rs.dto.DtoDeviceTemplateProfile} model.
     * Any public field can be queried using standard Java Beans naming convention for example a public method named
     * <code>getDeviceIdentification()</code> would be queried as <code>deviceIdentification</code>. The values of String fields should be single quoted.
     * <p>DeviceTemplateProfiles are returned in the order specified by the query order by expression if specified, otherwise the order
     * is undefined.</p>
     *
     * @param query the HQL query string. This string should not be encoded, as the Rest Client will convert it for you
     * @return a list of one or more {@link org.groundwork.rs.dto.DtoDeviceTemplateProfile} objects matching the query. If no records match the query,
     *         then an empty list is returned
     * @throws CollageRestException
     */
    public List<DtoDeviceTemplateProfile> query(String query) throws CollageRestException {
        return query(query, -1, -1);
    }

    /**
     * Query for DeviceTemplateProfiles with an HQL query string returning a list of DeviceTemplateProfile transfer objects.
     * Queries are only valid against the model represented by the DeviceTemplateProfile data transfer object {@link org.groundwork.rs.dto.DtoDeviceTemplateProfile}.
     * Queries are read only operations and are limited to where and order by HQL expressions.
     * <p>The names of the fields queried should match the names in the {@link org.groundwork.rs.dto.DtoDeviceTemplateProfile} model.
     * Any public field can be queried using standard Java Beans naming convention for example a public method named
     * <code>getDeviceIdentification()</code> would be queried as <code>deviceIdentification</code>. The values of String fields should be single quoted.
     * <p>Paging parameters <code>first</code> and <code>count</code> are used together to retrieve a subset of
     * entities, paging from the <code>first</code> record, for a <code>count</code> of n records.</p>
     * <p>DeviceTemplateProfiles are returned in the order specified by the query order by expression if specified, otherwise the order
     * is undefined.</p>
     *
     * @param query the HQL query string. This string should not be encoded, as the Rest Client will convert it for you
     * @param first the zero-based first record to retrieve to page over result set. Set to -1 to ignore this parameter
     * @param count the number of records to retrieve offset from the first parameter. Set to -1 to ignore this parameter
     * @return a list of one or more {@link org.groundwork.rs.dto.DtoDeviceTemplateProfile} objects matching the query. If no records match the query,
     *         then an empty list is returned
     * @throws CollageRestException
     */
    public List<DtoDeviceTemplateProfile> query(String query, int first, int count) throws CollageRestException {
        Response.Status status = Response.Status.SERVICE_UNAVAILABLE;
        ClientResponse<DtoDeviceTemplateProfileList> response = null;
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                String url = buildEncodedQuery(API_ROOT_SINGLE, query, first, count);
                ClientRequest request = createClientRequest(url);
                request.accept(mediaType);
                response = request.get(new GenericType<DtoDeviceTemplateProfileList>(){});
                if (response.getResponseStatus() == Response.Status.OK) {
                    return response.getEntity(new GenericType<DtoDeviceTemplateProfileList>(){}).getDeviceTemplateProfiles();
                } else if (response.getResponseStatus() == Response.Status.NOT_FOUND) {
                    return new ArrayList<DtoDeviceTemplateProfile>();
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
        throw new CollageRestException(String.format("Exception executing query devicetemplateprofiles (%s) with status code of %d, reason: %s", query, status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }

    /**
     * Administrative batch operation to add or update DeviceTemplateProfiles. {@link org.groundwork.rs.dto.DtoDeviceTemplateProfileList}
     * is a list of one or more {@link org.groundwork.rs.dto.DtoDeviceTemplateProfile} objects. Each of these objects represent either a new
     * device template profile or a device template profile to be updated. Any field that needs to be updated or added should be set on the
     * DtoDeviceTemplateProfile object. The web service will determine if an update or insert is required by looking up the  primary keys
     * (the <code>devicetemplateprofileId</code> and/or <code>deviceIdentification</code> fields), from the provided DtoDeviceTemplateProfile objects.
     * <p>The post operation is not transactional. If a list of ten device template profiles are passed in to be added, and
     * if, for example, two fail to update, the other eight will still be persisted. The results for all post operations return
     * a {@link DtoOperationResults} list of {@link org.groundwork.rs.dto.DtoOperationResult}, holding the status (success,
     * failure, warning) of each sub-operation, see {@link org.groundwork.rs.dto.DtoOperationResult#getStatus()}.
     * The {@link org.groundwork.rs.dto.DtoOperationResult#getLocation()} method provides the exact URL of the persisted device
     * template profile.</p>
     *
     * @param devicetemplateprofiles a list of one or more {@link org.groundwork.rs.dto.DtoDeviceTemplateProfile} objects to update or insert
     *                       based on the existence of the <code>id</code> or <code>deviceIdentification</code> primary keys.
     * @return a {@link org.groundwork.rs.dto.DtoOperationResults} holding the status of each device template profile operation
     * @throws CollageRestException
     */
    public DtoOperationResults post(DtoDeviceTemplateProfileList devicetemplateprofiles) throws CollageRestException {
        Response.Status status = Response.Status.SERVICE_UNAVAILABLE;
        ClientResponse<DtoOperationResults> response = null;
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                String url = build(API_ROOT_SINGLE);
                ClientRequest request = createClientRequest(url);
                request.accept(mediaType);
                request.body(mediaType, devicetemplateprofiles);
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
        throw new CollageRestException(String.format("Exception executing post to devicetemplateprofiles with status code of %d, reason: %s", status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }

    /**
     * Administrative operation to clear a single DeviceTemplateProfile. Takes a device identification
     * as the primary key.
     *
     * @param deviceIdentification the device identification of the DeviceTemplateProfile to clear
     * @return a {@link DtoOperationResults} set of {@link org.groundwork.rs.dto.DtoOperationResult},
     *              holding the status of each device template profile operation
     * @throws CollageRestException
     */
    public DtoOperationResults clear(String deviceIdentification) throws CollageRestException {
        return clear(Arrays.asList(new String[]{deviceIdentification}));
    }

    /**
     * Administrative batch operation to clear multiple DeviceTemplateProfiles specified by their
     * primary key device identifications.
     * <p>The clear operation is not transactional. If a list of five device template profiles are passed
     * in to be cleared, and if, for example, two fail to clear, the other three will still be
     * cleared. The results for all clear operations return a {@link DtoOperationResults} list of
     * {@link org.groundwork.rs.dto.DtoOperationResult}, holding the status (success, failure,
     * warning) of each sub-operation, see {@link org.groundwork.rs.dto.DtoOperationResult#getStatus()}.</p>
     *
     * @param deviceIdentificationsList a list of device identifications of the DeviceTemplateProfiles to clear
     * @return a {@link DtoOperationResults} set of {@link org.groundwork.rs.dto.DtoOperationResult},
     *          holding the status of each device template profile operation
     * @throws CollageRestException
     */
    public DtoOperationResults clear(List<String> deviceIdentificationsList) throws CollageRestException {
        return deleteOrClear(deviceIdentificationsList, true);
    }

    /**
     * Administrative batch operation to clear a list of DeviceTemplateProfiles. {@link org.groundwork.rs.dto.DtoDeviceTemplateProfileList}
     * is a list of one or more {@link org.groundwork.rs.dto.DtoDeviceTemplateProfile} objects. The <code>devicetemplateprofileId</code>
     * and <code>deviceIdentification</code> members of these objects are used to select these DeviceTemplateProfiles to clear.
     * <p>The clear operation is not transactional. If a list of five device template profiles are passed
     * in to be cleared, and if, for example, two fail to clear, the other three will still be
     * cleared. The results for all clear operations return a {@link DtoOperationResults} list of
     * {@link org.groundwork.rs.dto.DtoOperationResult}, holding the status (success, failure,
     * warning) of each sub-operation, see {@link org.groundwork.rs.dto.DtoOperationResult#getStatus()}.</p>
     *
     * @param dtoDeviceTemplateProfiles a list of one or more {@link org.groundwork.rs.dto.DtoDeviceTemplateProfile} objects to clear.
     * @return a {@link DtoOperationResults} set of {@link org.groundwork.rs.dto.DtoOperationResult},
     *          holding the status of each device template profile operation
     * @throws CollageRestException
     */
    public DtoOperationResults clear(DtoDeviceTemplateProfileList dtoDeviceTemplateProfiles) throws CollageRestException {
        return deleteOrClear(dtoDeviceTemplateProfiles, true);
    }

    /**
     * Administrative operation to delete a single DeviceTemplateProfile. Takes a device identification
     * as the primary key.
     *
     * @param deviceIdentification the device identification of the DeviceTemplateProfile to delete
     * @return a {@link DtoOperationResults} set of {@link org.groundwork.rs.dto.DtoOperationResult},
     *              holding the status of each device template profile operation
     * @throws CollageRestException
     */
    public DtoOperationResults delete(String deviceIdentification) throws CollageRestException {
        return delete(Arrays.asList(new String[]{deviceIdentification}));
    }

    /**
     * Administrative batch operation to delete multiple DeviceTemplateProfiles specified by their
     * primary key device identifications.
     * <p>The delete operation is not transactional. If a list of five device template profiles are passed
     * in to be deleted, and if, for example, two fail to delete, the other three will still be
     * deleted. The results for all delete operations return a {@link DtoOperationResults} list of
     * {@link org.groundwork.rs.dto.DtoOperationResult}, holding the status (success, failure,
     * warning) of each sub-operation, see {@link org.groundwork.rs.dto.DtoOperationResult#getStatus()}.</p>
     *
     * @param deviceIdentificationsList a list of device identifications of the DeviceTemplateProfiles to delete
     * @return a {@link DtoOperationResults} set of {@link org.groundwork.rs.dto.DtoOperationResult},
     *          holding the status of each device template profile operation
     * @throws CollageRestException
     */
    public DtoOperationResults delete(List<String> deviceIdentificationsList) throws CollageRestException {
        return deleteOrClear(deviceIdentificationsList, false);
    }

    /**
     * Administrative batch operation to delete a list of DeviceTemplateProfiles. {@link org.groundwork.rs.dto.DtoDeviceTemplateProfileList}
     * is a list of one or more {@link org.groundwork.rs.dto.DtoDeviceTemplateProfile} objects. The <code>devicetemplateprofileId</code>
     * and <code>deviceIdentification</code> members of these objects are used to select these DeviceTemplateProfiles to delete.
     * <p>The delete operation is not transactional. If a list of five device template profiles are passed
     * in to be deleted, and if, for example, two fail to delete, the other three will still be
     * deleted. The results for all delete operations return a {@link DtoOperationResults} list of
     * {@link org.groundwork.rs.dto.DtoOperationResult}, holding the status (success, failure,
     * warning) of each sub-operation, see {@link org.groundwork.rs.dto.DtoOperationResult#getStatus()}.</p>
     *
     * @param dtoDeviceTemplateProfiles a list of one or more {@link org.groundwork.rs.dto.DtoDeviceTemplateProfile} objects to delete.
     * @return a {@link DtoOperationResults} set of {@link org.groundwork.rs.dto.DtoOperationResult},
     *          holding the status of each device template profile operation
     * @throws CollageRestException
     */
    public DtoOperationResults delete(DtoDeviceTemplateProfileList dtoDeviceTemplateProfiles) throws CollageRestException {
        return deleteOrClear(dtoDeviceTemplateProfiles, false);
    }

    public DtoOperationResults deleteOrClear(List<String> deviceIdentificationsList, boolean clear) throws CollageRestException {
        Response.Status status = Response.Status.SERVICE_UNAVAILABLE;
        ClientResponse<DtoOperationResults> response = null;
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                String deviceIdentifications = makeCommaSeparatedParamFromList(deviceIdentificationsList);
                String url = (clear ? buildUrlWithPathAndQueryParams(API_ROOT, deviceIdentifications, buildEncodedQueryParams(CLEAR_NAMES, CLEAR_VALUES)) : buildUrlWithPath(API_ROOT, deviceIdentifications));
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
        throw new CollageRestException(String.format("Exception executing delete/clear devicetemplateprofiles (%s) with status code of %d, reason: %s", deviceIdentificationsList, status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }

    public DtoOperationResults deleteOrClear(DtoDeviceTemplateProfileList dtoDeviceTemplateProfiles, boolean clear) throws CollageRestException {
        Response.Status status = Response.Status.SERVICE_UNAVAILABLE;
        ClientResponse<DtoOperationResults> response = null;
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                String url = (clear ? buildUrlWithQueryParams(API_ROOT_SINGLE, buildEncodedQueryParams(CLEAR_NAMES, CLEAR_VALUES)) : build(API_ROOT_SINGLE));
                ClientRequest request = createClientRequest(url);
                request.accept(mediaType);
                request.body(mediaType, dtoDeviceTemplateProfiles);
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
        throw new CollageRestException(String.format("Exception executing delete/clear devicetemplateprofiles with status code of %d, reason: %s", status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }
}
