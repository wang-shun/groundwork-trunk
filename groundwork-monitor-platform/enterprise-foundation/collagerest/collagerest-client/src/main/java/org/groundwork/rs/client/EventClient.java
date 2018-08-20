package org.groundwork.rs.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.rs.dto.DtoAcknowledgeList;
import org.groundwork.rs.dto.DtoEvent;
import org.groundwork.rs.dto.DtoEventList;
import org.groundwork.rs.dto.DtoEventPropertiesList;
import org.groundwork.rs.dto.DtoOperationResults;
import org.groundwork.rs.dto.DtoServiceKey;
import org.groundwork.rs.dto.DtoServiceKeyList;
import org.groundwork.rs.dto.DtoStateTransition;
import org.groundwork.rs.dto.DtoStateTransitionList;
import org.groundwork.rs.dto.DtoStateTransitionListList;
import org.groundwork.rs.dto.DtoUnAcknowledgeList;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.util.GenericType;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class EventClient extends BaseRestClient {

    protected static Log log = LogFactory.getLog(EventClient.class);
    private static final String API_ROOT_SINGLE = "/events";
    private static final String API_ROOT = API_ROOT_SINGLE + "/";

    public EventClient(String deploymentUrl) {
        super(deploymentUrl);
    }

    public EventClient(String deploymentUrl, MediaType mediaType) {
        this(deploymentUrl);
        this.mediaType = mediaType;
    }

    public DtoEvent lookup(String eventId) throws CollageRestException {
        List<String> ids = new LinkedList<String>();
        ids.add(eventId);
        List<DtoEvent> events = lookup(ids);
        if (events != null && events.size() > 0)
            return events.get(0);
        return null;
    }

    public List<DtoEvent> lookup(List<String> eventIdList) throws CollageRestException {
        Response.Status status = Response.Status.SERVICE_UNAVAILABLE;
        String eventIds = "";
        ClientResponse<DtoEventList> response = null;
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                eventIds = makeCommaSeparatedParamFromList(eventIdList);
                String url = buildUrlWithPath(API_ROOT, eventIds);
                ClientRequest request = createClientRequest(url);
                request.accept(mediaType);
                response = request.get(new GenericType<DtoEventList>(){});
                if (response.getResponseStatus() == Response.Status.OK) {
                    DtoEventList events = response.getEntity(new GenericType<DtoEventList>(){});
                    return events.getEvents();
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
        throw new CollageRestException(String.format("Exception executing lookup events (%s) with status code of %d, reason: %s",
                eventIds, status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }
    
    public List<DtoEvent> query(String query) throws CollageRestException {
        return query(query, -1, -1);
    }

    public List<DtoEvent> query(String query, int first, int count) throws CollageRestException {
        Response.Status status = Response.Status.SERVICE_UNAVAILABLE;
        ClientResponse<DtoEventList> response = null;
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                String url = buildEncodedQuery(API_ROOT, query, null, first, count);
                ClientRequest request = createClientRequest(url);
                request.accept(mediaType);
                response = request.get(new GenericType<DtoEventList>(){});
                if (response.getResponseStatus() == Response.Status.OK) {
                    DtoEventList events = response.getEntity(new GenericType<DtoEventList>(){});
                    return events.getEvents();
                }
                else if (response.getResponseStatus() == Response.Status.NOT_FOUND) {
                    // return an empty list for not found exception
                    return new ArrayList<DtoEvent>();
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
        throw new CollageRestException(String.format("Exception executing query events (%s) with status code of %d, reason: %s",
                query, status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }

    public List<DtoEvent> list(int first, int count) throws CollageRestException {
        return query(null, first, count);
    }

    public List<DtoEvent> list() throws CollageRestException {
        return query(null, -1, -1);
    }

    public DtoOperationResults post(DtoEventList updates) throws CollageRestException {
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
        throw new CollageRestException(String.format("Exception executing post to events with status code of %d, reason: %s",
                status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }

    public DtoOperationResults delete(List<String> eventIdsList) throws CollageRestException {
        if (eventIdsList.size() > 50) {
            DtoEventList deletes = new DtoEventList();
            for (String id : eventIdsList) {
                deletes.add(new DtoEvent(Integer.parseInt(id)));
            }
            return delete(deletes);
        }
        ClientResponse<DtoOperationResults> response = null;
        Response.Status status = Response.Status.SERVICE_UNAVAILABLE;
        String eventIds = "";
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                eventIds = makeCommaSeparatedParamFromList(eventIdsList);
                ClientRequest request = createClientRequest(buildUrlWithPath(API_ROOT, eventIds));
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
        throw new CollageRestException(String.format("Exception executing delete to events (%s) with status code of %d, reason: %s",
                eventIds, status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }

    /**
     * Administrative batch operations to delete events. {@link org.groundwork.rs.dto.DtoEventList} is a list of one or
     * more {@link org.groundwork.rs.dto.DtoEvent} objects. Each of these objects should only have the {@link org.groundwork.rs.dto.DtoEvent#setId(Integer)}
     * identification field set. The <code>identification</code> field is used to delete each event by primary key.
     * All other fields will be ignored.
     * <p>
     * The delete operation is not transactional. If a list of five events are passed in to be deleted, and
     * if, for example, two events fail to delete, the other three events will still be deleted. The results for all
     * delete operations return a {@link DtoOperationResults} list of {@link org.groundwork.rs.dto.DtoOperationResult}, , holding
     * the status (success, failure, warning) of each sub-operation, see {@link org.groundwork.rs.dto.DtoOperationResult#getStatus()}.
     * </p>
     * @param deletes   a list of one or more {@link org.groundwork.rs.dto.DtoEvent} objects. Only the
     *                  event's <code>identification</code> primary key will be considered when deleting.
     * @return a {@link DtoOperationResults} set of {@link org.groundwork.rs.dto.DtoOperationResult}, holding the status of each event operation
     * @throws CollageRestException
     */
    public DtoOperationResults delete(DtoEventList deletes) throws CollageRestException {
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
        throw new CollageRestException(String.format("Exception executing delete to events with status code of %d, reason: %s",
                status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }

    public DtoOperationResults delete(String eventId) throws CollageRestException {
        List<String> ids = new LinkedList<String>();
        ids.add(eventId);
        return delete(ids);
    }

    public DtoOperationResults update(DtoEventPropertiesList updates) throws CollageRestException {
        ClientResponse<DtoOperationResults> response = null;
        Response.Status status = Response.Status.SERVICE_UNAVAILABLE;
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                ClientRequest request = createClientRequest(build(API_ROOT_SINGLE));
                request.accept(mediaType);
                request.body(mediaType, updates);
                response = request.put();
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
        throw new CollageRestException(String.format("Exception executing put to events with status code of %d, reason: %s",
                status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());

    }

    public DtoOperationResults update(List<String> eventIdsList, String opStatus, String updatedBy, String comments) throws CollageRestException {
        Response.Status status = Response.Status.SERVICE_UNAVAILABLE;
        String eventIds = "";
        ClientResponse<DtoOperationResults> response = null;
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                eventIds = makeCommaSeparatedParamFromList(eventIdsList);
                String[] names = {"opStatus", "updatedBy", "comments"};
                String[] values = {opStatus, updatedBy, comments};
                ClientRequest request = createClientRequest(buildUrlWithPathAndQueryParams(API_ROOT, eventIds, buildEncodedQueryParams(names, values)));
                request.accept(mediaType);
                request.body(mediaType, "");
                response = request.put();
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
        throw new CollageRestException(String.format("Exception executing delete to events (%s) with status code of %d, reason: %s",
                eventIds, status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }

    public DtoOperationResults acknowledge(DtoAcknowledgeList acks) throws CollageRestException {
        ClientResponse<DtoOperationResults> response = null;
        Response.Status status = Response.Status.SERVICE_UNAVAILABLE;
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                String path = buildUrlWithPath(API_ROOT, "ack");
                ClientRequest request = createClientRequest(path);
                request.accept(mediaType);
                request.body(mediaType, acks);
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
        throw new CollageRestException(String.format("Exception executing ack to events with status code of %d, reason: %s",
                status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }

    public DtoOperationResults unacknowledge(DtoUnAcknowledgeList unacks) throws CollageRestException {
        ClientResponse<DtoOperationResults> response = null;
        Response.Status status = Response.Status.SERVICE_UNAVAILABLE;
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                String path = buildUrlWithPath(API_ROOT, "unack");
                ClientRequest request = createClientRequest(path);
                request.accept(mediaType);
                request.body(mediaType, unacks);
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
        throw new CollageRestException(String.format("Exception executing unack to events with status code of %d, reason: %s",
                status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }

    public List<DtoStateTransition> getStateTransitions(String hostName, String serviceName, String startDate,
                                                        String endDate) {
        ClientResponse<DtoStateTransitionList> response = null;
        Response.Status status = Response.Status.SERVICE_UNAVAILABLE;
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                String[] names, values;
                if (serviceName != null) {
                    names = new String[]{"hostName", "serviceName", "startDate", "endDate"};
                    values = new String[]{hostName, serviceName, startDate, endDate};
                } else {
                    names = new String[]{"hostName", "startDate", "endDate"};
                    values = new String[]{hostName, startDate, endDate};
                }
                String url = buildUrlWithPathAndQueryParams(API_ROOT, "stateTransitions",
                        buildEncodedQueryParams(names, values));
                ClientRequest request = createClientRequest(url);
                request.accept(mediaType);
                response = request.get(new GenericType<DtoStateTransitionList>(){});
                if (response.getResponseStatus() == Response.Status.OK) {
                    DtoStateTransitionList dtoList = response.getEntity(new GenericType<DtoStateTransitionList>(){});
                    return dtoList.getStateTransitions();
                } else if (response.getResponseStatus() == Response.Status.NOT_FOUND) {
                    return Collections.EMPTY_LIST;
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
        }
        catch (Exception e) {
            throw new CollageRestException(e);
        }
        finally {
            if (response != null) {
                response.releaseConnection();
            }
        }
        if (status == null) {
            status = Response.Status.SERVICE_UNAVAILABLE;
        }
        throw new CollageRestException(
                String.format("Exception executing stateTransitions with status code of %d, reason: %s",
                        status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }

    public Map<DtoServiceKey, List<DtoStateTransition>> getStateTransitions(List<DtoServiceKey> hostAndServiceNames,
                                                                            String startDate, String endDate) {
        ClientResponse<DtoStateTransitionListList> response = null;
        Response.Status status = Response.Status.SERVICE_UNAVAILABLE;
        try {
            for (int retry = 0; retry < RETRIES; retry++) {
                String[] names = new String[]{"startDate", "endDate"};
                String[] values = new String[]{startDate, endDate};
                String url = buildUrlWithPathAndQueryParams(API_ROOT, "stateTransitions",
                        buildEncodedQueryParams(names, values));
                ClientRequest request = createClientRequest(url);
                request.accept(mediaType);
                request.body(mediaType, new DtoServiceKeyList(hostAndServiceNames));
                response = request.post();
                if (response.getResponseStatus() == Response.Status.OK) {
                    Map<DtoServiceKey, List<DtoStateTransition>> dtoStateTransitionListsMap = new HashMap<>();
                    DtoStateTransitionListList dtoStateTransitionListList =
                            response.getEntity(new GenericType<DtoStateTransitionListList>(){});
                    for (DtoStateTransitionList dtoList : dtoStateTransitionListList.getStateTransitionLists()) {
                        List<DtoStateTransition> dtoStateTransitionList = dtoList.getStateTransitions();
                        if (!dtoStateTransitionList.isEmpty()) {
                            DtoStateTransition dtoStateTransition = dtoStateTransitionList.get(0);
                            dtoStateTransitionListsMap.put(new DtoServiceKey(dtoStateTransition.getServiceName(),
                                    dtoStateTransition.getHostName()), dtoStateTransitionList);
                        }
                    }
                    return dtoStateTransitionListsMap;
                } else if (response.getResponseStatus() == Response.Status.NOT_FOUND) {
                    return Collections.EMPTY_MAP;
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
        }
        catch (Exception e) {
            throw new CollageRestException(e);
        }
        finally {
            if (response != null) {
                response.releaseConnection();
            }
        }
        if (status == null) {
            status = Response.Status.SERVICE_UNAVAILABLE;
        }
        throw new CollageRestException(
                String.format("Exception executing stateTransitions with status code of %d, reason: %s",
                        status.getStatusCode(), status.getReasonPhrase()), status.getStatusCode());
    }
}
