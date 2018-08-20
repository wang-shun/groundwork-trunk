package org.groundwork.rs.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.rs.common.BizFormParameters;
import org.groundwork.rs.dto.DtoBizAuthorization;
import org.groundwork.rs.dto.DtoBizAuthorizedServices;
import org.groundwork.rs.dto.DtoBizHostList;
import org.groundwork.rs.dto.DtoBizHostServiceInDowntimeList;
import org.groundwork.rs.dto.DtoBizHostsAndServices;
import org.groundwork.rs.dto.DtoBizServiceList;
import org.groundwork.rs.dto.DtoHost;
import org.groundwork.rs.dto.DtoOperationResults;
import org.groundwork.rs.dto.DtoService;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * The Java REST Client for performing high level aggregate Collage Business level operations
 * on monitored Services in the Groundwork enterprise foundation server.
 * <p>
 * Operations supported are of the categories:
 * <ul>
 *     <li>createOrUpdateHost - create or update a host, and optionally add to a host group</li>
 *     <li>createOrUpdateService - create or update a service, host, and optionally add host to a host group</li>
 *     <li>setInDowntime - set hosts and services in scheduled downtime</li>
 *     <li>clearInDowntime - clear scheduled downtime for hosts and services previously set in downtime</li>
 *     <li>getInDowntime - get scheduled downtime for hosts and services</li>
 * </ul>
 * <p>
 * <p>
 * These APIs will also send events and notifications on updates when there is a status change. Mutating in
 * downtime operations are synchronous and transactional.
 * </p>
 */
public class BizClient extends BaseRestClient {

    protected static Log log = LogFactory.getLog(BizClient.class);

    private static final String API_HOST_ROOT_SINGLE = "/biz/host";
    private static final String API_SERVICE_ROOT_SINGLE = "/biz/service";
    private static final String API_HOST_ROOT = API_HOST_ROOT_SINGLE + "/";
    private static final String API_SERVICE_ROOT = API_SERVICE_ROOT_SINGLE + "/";
    private static final String API_HOSTS_ROOT_SINGLE = "/biz/hosts";
    private static final String API_SERVICES_ROOT_SINGLE = "/biz/services";
    private static final String API_HOSTS_ROOT = API_HOSTS_ROOT_SINGLE + "/";
    private static final String API_SERVICES_ROOT = API_SERVICES_ROOT_SINGLE + "/";
    private static final String API_SET_IN_DOWNTIME_ROOT_SINGLE = "/biz/setindowntime";
    private static final String API_CLEAR_IN_DOWNTIME_ROOT_SINGLE = "/biz/clearindowntime";
    private static final String API_GET_IN_DOWNTIME_ROOT_SINGLE = "/biz/getindowntime";
    private static final String API_GET_AUTHORIZED_SERVICES_ROOT_SINGLE = "/biz/getauthorizedservices";

    private static final int MAX_POST_HOSTS_BATCH = 100;

    /**
     * Create a Biz REST Client for performing query and administrative operations
     * on the business objects in the Groundwork enterprise foundation database.
     * Deployment URL example:
     * <pre>http://server-name[:port]/foundation-webapp/api</pre>
     *
     * @param deploymentUrl the deployment root URL for all Rest operations
     */
    public BizClient(String deploymentUrl) {
        super(deploymentUrl);
    }

    /**
     * Create a Biz REST Client for performing Collage Business level operations
     * on the services in the Groundwork enterprise foundation database.
     * Deployment URL example:
     * <pre>http://server-name[:port]/foundation-webapp/api</pre>
     * <p>Supported Media Types</p>
     * <ul>application/xml {@link javax.ws.rs.core.MediaType#APPLICATION_XML_TYPE}</ul>
     * <ul>application/json {@link javax.ws.rs.core.MediaType#APPLICATION_JSON_TYPE}</ul>
     * <p></p>
     * @param deploymentUrl the deployment root URL for all Rest operations
     * @param mediaType a valid, supported internet media type (MIME) supported
     */
    public BizClient(String deploymentUrl, MediaType mediaType) {
        this(deploymentUrl);
        this.mediaType = mediaType;
    }

    /**
     * Create or update a host, and optionally add to a host group or a host category. On updates, sends events and
     * notifications when there is a status change
     *
     * @param host - name of the host to be created or updated, required
     * @param status - the new status for this host, must be a valid status, required
     * @param message - the textual message to write to this host, required
     * @param hostGroup - the name of one host group to join for this host, optional
     * @param hostCategory - the name of one host category to join for this host, optional
     * @param device - the device name for this host, optional, defaults to host name
     * @param appType - the type of application, optional, defaults to NAGIOS
     * @param agentId - the agent id of the hub or connector, optional
     * @return a DtoHost object after creation of depth full
     * @throws CollageRestException
     */
    public DtoHost createOrUpdateHost(String host, String status, String message, String hostGroup, String hostCategory,
                                      String device, String appType, String agentId)
            throws CollageRestException {
        Response.Status rs = Response.Status.SERVICE_UNAVAILABLE;
        ClientResponse<DtoOperationResults> response = null;
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                ClientRequest request = createClientRequest(build(API_HOST_ROOT_SINGLE));
                request.accept(mediaType);
                request.formParameter(BizFormParameters.PARAM_HOST, host);
                request.formParameter(BizFormParameters.PARAM_STATUS, status);
                request.formParameter(BizFormParameters.PARAM_MESSAGE, message);
                if (hostGroup != null)
                    request.formParameter(BizFormParameters.PARAM_HOST_GROUP, hostGroup);
                if (hostCategory != null)
                    request.formParameter(BizFormParameters.PARAM_HOST_CATEGORY, hostCategory);
                if (device != null)
                    request.formParameter(BizFormParameters.PARAM_DEVICE, device);
                if (appType != null)
                    request.formParameter(BizFormParameters.PARAM_APP_TYPE, appType);
                if (agentId != null)
                    request.formParameter(BizFormParameters.PARAM_AGENT_ID, agentId);
                response = request.post();
                if (response.getResponseStatus() == Response.Status.OK) {
                    return response.getEntity(DtoHost.class);
                }
                else if (response.getResponseStatus() == Response.Status.UNAUTHORIZED && retry < 1) {
                    log.info(RETRY_AUTH);
                    rs = response.getResponseStatus();
                    response.releaseConnection();
                    tokenSessionManager.removeToken(deploymentUrl);
                    continue;
                }
                // Special case for async
                if (response.getStatus() == TOO_MANY_REQUESTS) {
                    throw new CollageRestException(response.getEntity(String.class), response.getStatus());
                }
                else {
                    rs = response.getResponseStatus();
                }
                break;
            }
        }
        catch (Exception e) {
            if (e instanceof CollageRestException)
                throw (CollageRestException)e;
            throw new CollageRestException(e);
        }
        finally {
            if (response != null)
                response.releaseConnection();
        }
        if (rs == null)
            rs = Response.Status.SERVICE_UNAVAILABLE;
        throw new CollageRestException(String.format("Exception executing post to biz/host with status code of %d, reason: %s",
                rs.getStatusCode(), rs.getReasonPhrase()), rs.getStatusCode());
    }

    /**
     * Create or update a service, host, and optionally add host to a host/service group or host/service category.
     * On updates, sends events and notifications when there is a status change
     *
     * @param host - name of the host to be created or updated, required
     * @param service - name of the service to be created or updated, required
     * @param status - the new status for this host, must be a valid status, required
     * @param message - the textual message to write to this host, required
     * @param serviceGroup - the name of one service group to join for this service, optional
     * @param serviceCategory - the name of one service category to join for this service, optional
     * @param hostGroup - the name of one host group to join for this host, optional
     * @param hostCategory - the name of one host category to join for this host, optional
     * @param device - the device name for this host, optional, defaults to host name
     * @param appType - the type of application, optional, defaults to NAGIOS
     * @param agentId - the agent id of the hub or connector, optional
     * @return a DtoService object after full creation
     * @throws CollageRestException
     */
    public DtoService createOrUpdateService(String host, String service, String status, String message,
                                            String serviceGroup, String serviceCategory, String hostGroup,
                                            String hostCategory, String device, String appType, String agentId)
            throws CollageRestException {
        Response.Status rs = Response.Status.SERVICE_UNAVAILABLE;
        ClientResponse<DtoOperationResults> response = null;
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                ClientRequest request = createClientRequest(build(API_SERVICE_ROOT_SINGLE));
                request.accept(mediaType);
                request.formParameter(BizFormParameters.PARAM_HOST, host);
                request.formParameter(BizFormParameters.PARAM_SERVICE, service);
                request.formParameter(BizFormParameters.PARAM_STATUS, status);
                request.formParameter(BizFormParameters.PARAM_MESSAGE, message);
                if (serviceGroup != null)
                    request.formParameter(BizFormParameters.PARAM_SERVICE_GROUP, serviceGroup);
                if (serviceCategory != null)
                    request.formParameter(BizFormParameters.PARAM_SERVICE_CATEGORY, serviceCategory);
                if (hostGroup != null)
                    request.formParameter(BizFormParameters.PARAM_HOST_GROUP, hostGroup);
                if (hostCategory != null)
                    request.formParameter(BizFormParameters.PARAM_HOST_CATEGORY, hostCategory);
                if (device != null)
                    request.formParameter(BizFormParameters.PARAM_DEVICE, device);
                if (appType != null)
                request.formParameter(BizFormParameters.PARAM_APP_TYPE, appType);
                if (agentId != null)
                    request.formParameter(BizFormParameters.PARAM_AGENT_ID, agentId);
                response = request.post();
                if (response.getResponseStatus() == Response.Status.OK) {
                    return response.getEntity(DtoService.class);
                }
                else if (response.getResponseStatus() == Response.Status.UNAUTHORIZED && retry < 1) {
                    log.info(RETRY_AUTH);
                    rs = response.getResponseStatus();
                    response.releaseConnection();
                    tokenSessionManager.removeToken(deploymentUrl);
                    continue;
                }
                // Special case for async
                if (response.getStatus() == TOO_MANY_REQUESTS) {
                    throw new CollageRestException(response.getEntity(String.class), response.getStatus());
                }
                else {
                    rs = response.getResponseStatus();
                }
                break;
            }
        }
        catch (Exception e) {
            if (e instanceof CollageRestException)
                throw (CollageRestException)e;
            throw new CollageRestException(e);
        }
        finally {
            if (response != null)
                response.releaseConnection();
        }
        if (rs == null)
            rs = Response.Status.SERVICE_UNAVAILABLE;
        throw new CollageRestException(String.format("Exception executing post to biz/service with status code of %d, reason: %s",
                rs.getStatusCode(), rs.getReasonPhrase()), rs.getStatusCode());
    }

    /**
     * Administrative batch operations to add or update hosts with optional services and also process additional business
     * processes like event generation and performance notifications. {@link org.groundwork.rs.dto.DtoBizHost} is a list of
     * one or more {@link org.groundwork.rs.dto.DtoBizHost} objects. Each of these objects represent either a new host, or
     * a host to be updated, either with optional services as {@link org.groundwork.rs.dto.DtoBizService} objects that in
     * turn can be new or be updated. Any field that needs to be updated or added should be set on the DtoBizHost or
     * DtoBizService objects. The web service will determine if an update or insert is required by looking up the host's
     * primary key (the field <code>host</code>) from the provided DtoBizHost objects or the services' primary keys, (the
     * <code>host</code> and <code>service</code> fields), from the embedded DtoBizService objects.
     * <p>
     * The post operation is not transactional. If a list of ten hosts are passed in to be added, and if, for example, two
     * hosts fail to update, the other eight hosts will still be persisted. Each service is also processed individually.
     * The results for all post operations return a {@link DtoOperationResults} list of
     * {@link org.groundwork.rs.dto.DtoOperationResult}, holding the status (success, failure, warning) of each
     * sub-operation, see {@link org.groundwork.rs.dto.DtoOperationResult#getStatus()}. Each host and service add or update
     * will have its own returned result. The {@link org.groundwork.rs.dto.DtoOperationResult#getLocation()} method
     * provides the exact URL of the persisted host or service.
     * </p>
     * @param updates  a list of one or more {@link org.groundwork.rs.dto.DtoBizHost} objects. Optionally, these can have
     *                 embedded {@link org.groundwork.rs.dto.DtoBizService} objects. Each object will either be updated
     *                 or inserted based on existence of the host's <code>host</code> or service's <code>host</code> and
     *                 <code>service</code> primary keys.
     * @return a {@link DtoOperationResults} set of {@link org.groundwork.rs.dto.DtoOperationResult},
     *         holding the status of each host and service add or update operation.
     * @throws CollageRestException
     */
    public DtoOperationResults postHosts(DtoBizHostList updates) throws CollageRestException {
        // batch invoke large post hosts operations, (reported but dubious performance enhancement)
        if (updates.getHosts().size() > MAX_POST_HOSTS_BATCH) {
            DtoOperationResults batchResults = new DtoOperationResults();
            DtoBizHostList batchUpdates = new DtoBizHostList();
            for (int i = 0, limit = updates.size(); (i < limit); i++) {
                batchUpdates.add(updates.getHosts().get(i));
                if (batchUpdates.size() == MAX_POST_HOSTS_BATCH || i == limit-1) {
                    batchResults.merge(postHosts(batchUpdates));
                    batchUpdates.getHosts().clear();
                }
            }
            return batchResults;
        }
        // invoke post hosts
        Response.Status status = Response.Status.SERVICE_UNAVAILABLE;
        ClientResponse<DtoOperationResults> response = null;
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                ClientRequest request = createClientRequest(build(API_HOSTS_ROOT_SINGLE));
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
                // Special case for async
                if (response.getStatus() == TOO_MANY_REQUESTS) {
                    throw new CollageRestException(response.getEntity(String.class), response.getStatus());
                }
                else {
                    status = response.getResponseStatus();
                }
                break;
            }
        }
        catch (Exception e) {
            if (e instanceof CollageRestException)
                throw (CollageRestException)e;
            throw new CollageRestException(e);
        }
        finally {
            if (response != null)
                response.releaseConnection();
        }
        if (status == null)
            status = Response.Status.SERVICE_UNAVAILABLE;
        throw new CollageRestException(String.format("Exception executing post to hosts with status code of %d, reason: %s",
                status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }

    /**
     * Administrative batch operations to add or update Services and also process additional business processes like
     * event generation and performance notifications.
     * {@link org.groundwork.rs.dto.DtoBizServiceList} is a list of one or
     * more {@link org.groundwork.rs.dto.DtoService} objects. Each of these objects represent either a new service, or a service to be
     * updated. Any field that needs to be updated or added should be set on the DtoBizService object.
     * The web service will determine if an update or insert is required by looking up the
     * service's primary keys (the fields <code>description</code> and <code>hostName</code>) from the provided DtoBizService objects.
     * Both of these fields are required.
     * <p>
     * The post operation is not transactional. If a list of ten services are passed in to be added, and
     * if, for example, two services fail to update, the other eight services will still be persisted. The results for all
     * post operations return a {@link DtoOperationResults} list of {@link org.groundwork.rs.dto.DtoOperationResult}, holding
     * the status (success, failure, warning) of each sub-operation, see {@link org.groundwork.rs.dto.DtoOperationResult#getStatus()}.
     * The {@link org.groundwork.rs.dto.DtoOperationResult#getLocation()} method provides the exact URL of the persisted Service.
     * </p>
     * @param updates  a list of one or more {@link org.groundwork.rs.dto.DtoBizService} objects. Each object will either be updated
     *                 or inserted based on existence of the service's primary key <code>description</code> plus <code>hostName</code>.
     * @return a {@link DtoOperationResults} set of {@link org.groundwork.rs.dto.DtoOperationResult},
     *         holding the status of each operation
     * @throws CollageRestException
     */
    public DtoOperationResults postServices(DtoBizServiceList updates) throws CollageRestException {
        Response.Status status = Response.Status.SERVICE_UNAVAILABLE;
        ClientResponse<DtoOperationResults> response = null;
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                ClientRequest request = createClientRequest(build(API_SERVICES_ROOT_SINGLE));
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
                // Special case for async
                if (response.getStatus() == TOO_MANY_REQUESTS) {
                    throw new CollageRestException(response.getEntity(String.class), response.getStatus());
                }
                else {
                    status = response.getResponseStatus();
                }
                break;
            }
        }
        catch (Exception e) {
            if (e instanceof CollageRestException)
                throw (CollageRestException)e;
            throw new CollageRestException(e);
        }
        finally {
            if (response != null)
                response.releaseConnection();
        }
        if (status == null)
            status = Response.Status.SERVICE_UNAVAILABLE;
        throw new CollageRestException(String.format("Exception executing post to services with status code of %d, reason: %s",
                status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }

    /**
     * Increment the scheduled in downtime property for hosts and services selected by hosts, service descriptions,
     * host groups, and service groups. Together these select hosts and/or services that will have their downtime
     * properties set. Hosts and services can be specified as a list of names and descriptions or as a single wildcard
     * '*'. Host groups and service groups can be used to define the set of hosts and services when wildcards are used,
     * but their usage alone will not result in downtime properties being set. Whether hosts and/or services downtime is
     * set based on calling parameter conventions. Also generates LogMessage "events" to capture the downtime activity
     * per host and service for audit and SLA reporting. These events will have a monitor status of START_DOWNTIME or
     * IN_DOWNTIME depending on the initial downtime level of the corresponding host or service. Returns primary keys,
     * resulting downtime level, and entities in downtime metadata for the set of hosts and services that had their
     * downtime level property incremented.
     *
     * @param hostNames host names or '*' wildcard to put into downtime
     * @param serviceDescriptions service descriptions or '*' wildcard to put into downtime
     * @param hostGroupNames host group names to put into downtime
     * @param serviceGroupCategoryNames service group category names to put into downtime
     * @return list of host and services set in downtime
     * @throws CollageRestException
     */
    public DtoBizHostServiceInDowntimeList setInDowntime(List<String> hostNames, List<String> serviceDescriptions,
                                                         List<String> hostGroupNames,
                                                         List<String> serviceGroupCategoryNames) throws CollageRestException {
        // set in downtime by convention
        return setInDowntime(hostNames, serviceDescriptions, hostGroupNames, serviceGroupCategoryNames, false, false);
    }

    /**
     * Increment the scheduled in downtime property for hosts and services selected by hosts, service descriptions,
     * host groups, and service groups. Together these select hosts and/or services that will have their downtime
     * properties set. Hosts and services can be specified as a list of names and descriptions or as a single wildcard
     * '*'. Host groups and service groups can be used to define the set of hosts and services when wildcards are used,
     * but their usage alone will not result in downtime properties being set. Specifying a wildcard, empty, or null
     * host/service matching parameter are generally equivalent unless parameter conventions are used to infer setHosts
     * and setServices parameters, (enabled when both setHosts and setServices are false). Also generates LogMessage
     * "events" to capture the downtime activity per host and service for audit and SLA reporting. These events will
     * have a monitor status of START_DOWNTIME or IN_DOWNTIME depending on the initial downtime level of the
     * corresponding host or service. Returns primary keys, resulting downtime level, and entities in downtime metadata
     * for the set of hosts and services that had their downtime level property incremented.
     *
     * @param hostNames host names or '*' wildcard to put into downtime
     * @param serviceDescriptions service descriptions or '*' wildcard to put into downtime
     * @param hostGroupNames host group names to put into downtime
     * @param serviceGroupCategoryNames service group category names to put into downtime
     * @param setHosts set host downtime properties, (set false to use parameter conventions)
     * @param setServices set service downtime properties, (set false to use parameter conventions)
     * @return list of host and services set in downtime
     * @throws CollageRestException
     */
    public DtoBizHostServiceInDowntimeList setInDowntime(List<String> hostNames, List<String> serviceDescriptions,
                                                         List<String> hostGroupNames,
                                                         List<String> serviceGroupCategoryNames, boolean setHosts,
                                                         boolean setServices) throws CollageRestException {
        return setInDowntime(new DtoBizHostsAndServices(hostNames, serviceDescriptions, hostGroupNames,
                serviceGroupCategoryNames, setHosts, setServices));
    }

    /**
     * Increment the scheduled in downtime property for hosts and services selected by hosts, service descriptions,
     * host groups, and service groups. Together these select hosts and/or services that will have their downtime
     * properties set. Hosts and services can be specified as a list of names and descriptions or as a single wildcard
     * '*'. Host groups and service groups can be used to define the set of hosts and services when wildcards are used,
     * but their usage alone will not result in downtime properties being set. Specifying a wildcard, empty, or null
     * host/service matching parameter are generally equivalent unless parameter conventions are used to infer setHosts
     * and setServices parameters, (enabled when both setHosts and setServices are false). Also generates LogMessage
     * "events" to capture the downtime activity per host and service for audit and SLA reporting. These events will
     * have a monitor status of START_DOWNTIME or IN_DOWNTIME depending on the initial downtime level of the
     * corresponding host or service. Returns primary keys, resulting downtime level, and entities in downtime metadata
     * for the set of hosts and services that had their downtime level property incremented.
     *
     * @param hostsAndServices hosts, service descriptions, host groups, or service groups to put in downtime
     * @return list of host and services set in downtime
     * @throws CollageRestException
     */
    public DtoBizHostServiceInDowntimeList setInDowntime(DtoBizHostsAndServices hostsAndServices) throws CollageRestException {
        Response.Status status = Response.Status.SERVICE_UNAVAILABLE;
        ClientResponse<DtoBizHostServiceInDowntimeList> response = null;
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                String url = build(API_SET_IN_DOWNTIME_ROOT_SINGLE);
                ClientRequest request = createClientRequest(url);
                request.accept(mediaType);
                request.body(mediaType, hostsAndServices);
                response = request.post();
                if (response.getResponseStatus() == Response.Status.OK) {
                    return response.getEntity(DtoBizHostServiceInDowntimeList.class);
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
        throw new CollageRestException(String.format("Exception executing post to biz setindowntime with status code of %d, reason: %s", status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }

    /**
     * Decrement the scheduled in downtime property for specified hosts and services. These, along with metadata about
     * the entities in downtime, are specified by hosts and services in downtime returned from the setInDowntime method.
     * This method also generates LogMessage "events" to capture the downtime activity per host and service for audit
     * and SLA reporting. These events will have a monitor status of IN_DOWNTIME or END_DOWNTIME depending on the
     * resulting downtime level of the corresponding host or service. Returns primary keys and resulting downtime level
     * for the set of hosts and services that had their downtime level property decremented.
     *
     * @param hostsAndServicesInDowntime list of host and services set in downtime
     * @return list of host and services cleared
     * @throws CollageRestException
     */
    public DtoBizHostServiceInDowntimeList clearInDowntime(DtoBizHostServiceInDowntimeList hostsAndServicesInDowntime) throws CollageRestException {
        Response.Status status = Response.Status.SERVICE_UNAVAILABLE;
        ClientResponse<DtoBizHostServiceInDowntimeList> response = null;
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                String url = build(API_CLEAR_IN_DOWNTIME_ROOT_SINGLE);
                ClientRequest request = createClientRequest(url);
                request.accept(mediaType);
                request.body(mediaType, hostsAndServicesInDowntime);
                response = request.post();
                if (response.getResponseStatus() == Response.Status.OK) {
                    return response.getEntity(DtoBizHostServiceInDowntimeList.class);
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
        throw new CollageRestException(String.format("Exception executing post to biz clearindowntime with status code of %d, reason: %s", status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }

    /**
     * Get the scheduled in downtime property for specified hosts and services. Returns primary keys and downtime level
     * for the hosts and services.
     *
     * @param hostsAndServicesInDowntime list of host and services to query
     * @return list of host and services
     * @throws CollageRestException
     */
    public DtoBizHostServiceInDowntimeList getInDowntime(DtoBizHostServiceInDowntimeList hostsAndServicesInDowntime) throws CollageRestException {
        Response.Status status = Response.Status.SERVICE_UNAVAILABLE;
        ClientResponse<DtoBizHostServiceInDowntimeList> response = null;
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                String url = build(API_GET_IN_DOWNTIME_ROOT_SINGLE);
                ClientRequest request = createClientRequest(url);
                request.accept(mediaType);
                request.body(mediaType, hostsAndServicesInDowntime);
                response = request.post();
                if (response.getResponseStatus() == Response.Status.OK) {
                    return response.getEntity(DtoBizHostServiceInDowntimeList.class);
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
        throw new CollageRestException(String.format("Exception executing post to biz getindowntime with status code of %d, reason: %s", status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }

    /**
     * Returns full access authorized services.
     *
     * @return full access authorized services
     */
    public DtoBizAuthorizedServices getAuthorizedServices() {
        Response.Status status = Response.Status.SERVICE_UNAVAILABLE;
        ClientResponse<DtoBizAuthorizedServices> response = null;
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                String url = build(API_GET_AUTHORIZED_SERVICES_ROOT_SINGLE);
                ClientRequest request = createClientRequest(url);
                request.accept(mediaType);
                response = request.get();
                if (response.getResponseStatus() == Response.Status.OK) {
                    return response.getEntity(DtoBizAuthorizedServices.class);
                } else if (response.getResponseStatus() == Response.Status.NOT_FOUND) {
                    return null;
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
        throw new CollageRestException(String.format("Exception executing get to biz getauthorizedservices with status code of %d, reason: %s", status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }

    /**
     * Returns authorized services expanded from from authorized host groups and authorized service groups for a user.
     * Names of hosts fully accessible from host groups and a mapping of all service descriptions and their accessible
     * hosts are returned. Services access is determined from both the host groups, (all services per host), and service
     * groups as specified. A null return indicates full access.
     *
     * @param authorization authorized host and service groups
     * @return authorized services or null
     */
    public DtoBizAuthorizedServices getAuthorizedServices(DtoBizAuthorization authorization) {
        Response.Status status = Response.Status.SERVICE_UNAVAILABLE;
        ClientResponse<DtoBizAuthorizedServices> response = null;
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                String url = build(API_GET_AUTHORIZED_SERVICES_ROOT_SINGLE);
                ClientRequest request = createClientRequest(url);
                request.accept(mediaType);
                request.body(mediaType, authorization);
                response = request.post();
                if (response.getResponseStatus() == Response.Status.OK) {
                    return response.getEntity(DtoBizAuthorizedServices.class);
                } else if (response.getResponseStatus() == Response.Status.NOT_FOUND) {
                    return null;
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
        throw new CollageRestException(String.format("Exception executing post to biz getauthorizedservices with status code of %d, reason: %s", status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }
}
