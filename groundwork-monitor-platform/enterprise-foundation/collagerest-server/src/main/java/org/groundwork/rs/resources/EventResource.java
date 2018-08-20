package org.groundwork.rs.resources;

import com.groundwork.collage.CollageAdminInfrastructure;
import com.groundwork.collage.CollageFactory;
import com.groundwork.collage.HibernateProgrammaticTxnSupport;
import com.groundwork.collage.metrics.CollageTimer;
import com.groundwork.collage.model.Category;
import com.groundwork.collage.model.LogMessage;
import com.groundwork.collage.model.impl.StateTransition;
import com.groundwork.collage.query.QuerySubstitution;
import com.groundwork.collage.query.QueryTranslation;
import com.groundwork.collage.query.QueryTranslator;
import com.groundwork.collage.util.Nagios;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.bs.category.CategoryService;
import org.groundwork.foundation.bs.logmessage.LogMessageService;
import org.groundwork.foundation.dao.FilterCriteria;
import org.groundwork.foundation.dao.FoundationQueryList;
import org.groundwork.foundation.dao.SortCriteria;
import org.groundwork.rs.conversion.EventConverter;
import org.groundwork.rs.dto.*;
import org.hibernate.FlushMode;
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Path("/events")
public class EventResource extends AbstractResource {

    public static final String RESOURCE_PREFIX = "/events/";

    protected static Log log = LogFactory.getLog(EventResource.class);

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoEventList getEvents(@QueryParam("query") String query,
                             @QueryParam("first") @DefaultValue("-1") int first, @QueryParam("count") @DefaultValue("-1") int count) {
        CollageTimer timer = startMetricsTimer();
        try {
            if (log.isDebugEnabled()) {
                log.debug(String.format("processing /GET on /events query: %s, first: %d, count: %d",
                         (query == null) ? "(none)" : query,  first, count));
            }
            LogMessageService eventService =  CollageFactory.getInstance().getLogMessageService();
            QueryTranslator queryTranslator = CollageFactory.getInstance().getQueryTranslator();

            List<LogMessage> events = null;
            if (query != null) {
                QueryTranslation translation = queryTranslator.translate(query, QueryTranslator.EVENT_KEY);
                if (translation.hasSubstitutions()) {
                    for (QuerySubstitution sub : translation.getSubstitutions()) {
                        switch (sub.getSubstitutionType()) {
                            case CATEGORY_EQUAL:
                                String categoryIds = buildCategoryList(sub.getValue(), CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP);
                                if (categoryIds == null) {
                                    throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity(
                                      String.format("Categories not found for given event query [%s]", query)).build());
                                }
                                String newHql = queryTranslator.substitute(translation, sub, categoryIds);
                                translation.setHql(newHql);
                                break;
                            case CATEGORY_IN:
                                String ids = getServiceIdListByServiceGroups(sub.getValue());
                                if (ids.length() == 0) {
                                    throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity(
                                      String.format("Categories not found for given event query [%s]", query)).build());
                                }
                                String inHql = queryTranslator.substitute(translation, sub, ids);
                                translation.setHql(inHql);
                                break;
                        }
                    }
                }
                if (log.isDebugEnabled()) log.debug("hql = [" + translation.getHql() + "]");
                events = eventService.queryEvents(translation.getHql(), translation.getCountHql(), first, count).getResults();
            }
            else {
                // TODO: disallow this kind of query too large of a result set
                //SortCriteria sortCriteria = createSortCriteria("hostName", DtoSortType.Ascending);
                events = eventService.getLogMessages(null, null, first, count).getResults();
            }
            if (events.isEmpty()) {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                        .entity(String.format("Events not found for given event query [%s]", query)).build());
            }

            List<DtoEvent> dtoEvents = new ArrayList<DtoEvent>();
            for (LogMessage event : events) {
                DtoEvent dtoEvent = EventConverter.convert(event);
                dtoEvents.add(dtoEvent);
            }
            return new DtoEventList(dtoEvents);
        }
        catch (Exception e) {
            if (e instanceof WebApplicationException)
                throw (WebApplicationException)e;
            log.error(ResourceMessages.UNEXPECTED_EXCEPTION + e, e);
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An error occurred processing request for events.").build());
        }
        finally {
            stopMetricsTimer(timer);
        }
    }

    @GET
    @Path("/{eventIds}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoEventList getEventsByIds(@PathParam("eventIds") String eventIds,
                                   @QueryParam("first") @DefaultValue("-1") int first, @QueryParam("count") @DefaultValue("-1") int count) {
        CollageTimer timer = startMetricsTimer();
        try {
            if (log.isDebugEnabled()) {
                log.debug(String.format("processing /GET on /eventIds/%s ", eventIds));
            }
            if (eventIds == null) {
                throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                        .entity("Event Ids is mandatory").build());
            }
            Integer ids[] = null;
            try {
                ids = parseIdArray(eventIds);
            }
            catch (Exception e) {
                String message = String.format("error converting event ids %s ", eventIds);
                log.debug(message);
                throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity(message).build());
            }

            LogMessageService eventService =  CollageFactory.getInstance().getLogMessageService();
            FilterCriteria filterCriteria = FilterCriteria.in(LogMessage.HP_ID, ids);
            SortCriteria sortCriteria = null;

            List<LogMessage> events = eventService.getLogMessages(filterCriteria, sortCriteria, first, count).getResults();
            if (events.isEmpty()) {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                        .entity(String.format("Events not found for given event list [%s]", eventIds)).build());
            }

            List<DtoEvent> dtoEvents = new ArrayList<DtoEvent>();
            for (LogMessage event : events) {
                DtoEvent dtoEvent = EventConverter.convert(event);
                dtoEvents.add(dtoEvent);
            }
            return new DtoEventList(dtoEvents);
        } catch (Exception e) {
            if (e instanceof WebApplicationException)
                throw (WebApplicationException)e;
            log.error(ResourceMessages.UNEXPECTED_EXCEPTION + e, e);
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An error occurred processing request for events.").build());
        }
        finally {
            stopMetricsTimer(timer);
        }
    }

    @PUT
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoOperationResults updateEventsByProperties(DtoEventPropertiesList eventsList) {
        CollageTimer timer = startMetricsTimer();
        if (log.isDebugEnabled()) {
            log.debug(String.format("processing /PUT on /events with %d events", (eventsList == null) ? 0 : eventsList.getEvents().size()));
        }
        if (eventsList == null) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Properties is mandatory").build());
        }
        DtoOperationResults results = new DtoOperationResults("Event", DtoOperationResults.UPDATE);
        if (eventsList.getEvents().size() == 0) {
            return results;
        }
        CollageAdminInfrastructure admin = getAdminInfrastructureService();
        for (DtoEventProperties entity : eventsList.getEvents()) {
            try {
                if (entity.getId() < 1) {
                    results.fail(String.valueOf(entity.getId()), "Invalid entity id " + entity.getId() + ", skipped");
                    continue;
                }
                Map<String, Object> eventProperties = EventConverter.convertEventMap(entity.getProperties());
                LogMessage logMessage = admin.updateLogMessageByID(entity.getId(), eventProperties);
                String id = logMessage.getLogMessageId().toString();
                results.success(id, buildResourceLocator(uriInfo, RESOURCE_PREFIX, id));
            }
            catch (Exception e) {
                log.error("Failed to create event: " + entity.getId(), e);
                results.fail(String.valueOf(entity.getId()), e.toString());
            }
        }
        stopMetricsTimer(timer);
        return results;
    }

    @PUT
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("/{eventIds}")
    public DtoOperationResults updateEventsStatus(@PathParam("eventIds") String eventIds,
                                                  @QueryParam("opStatus") String opStatus,
                                                  @QueryParam("updatedBy") String updatedBy,
                                                  @QueryParam("comments") String comments) {
        CollageTimer timer = startMetricsTimer();
        if (log.isDebugEnabled()) {
            log.debug(String.format("processing /PUT on /events with op-status = %s, updatedBy %s, comments for %s for %d event ids",
                    opStatus, updatedBy, comments, eventIds.length()));
        }

        if (eventIds == null) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Event Ids is mandatory").build());
        }
        if (opStatus == null) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity("opStatus is mandatory").build());
        }
        List<Integer> ids = null;
        try {
            ids = parseIds(eventIds);
        }
        catch (Exception e) {
            String message = String.format("error converting event ids %s ", eventIds);
            log.debug(message);
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity(message).build());
        }
        //LogMessageService eventService =  CollageFactory.getInstance().getLogMessageService();
        CollageAdminInfrastructure admin = getAdminInfrastructureService();

        boolean transactionFailed = false;
        try {
            admin.storeEventOperationalStatus(ids, opStatus, updatedBy, comments);
        }
        catch (Exception e) {
            log.error("Failed to create events transactionally, retrying sequentially. Exception is: " + e.getMessage(), e);
            if (ids.size() == 1) {
                DtoOperationResults results = new DtoOperationResults("Event", DtoOperationResults.UPDATE);
                results.fail(ids.get(0).toString(), e.toString());
                return results;
            }
            transactionFailed = true;
        }
        DtoOperationResults results = new DtoOperationResults("Event", DtoOperationResults.UPDATE);
        if (transactionFailed) {
            for (Integer id : ids) {
                try {
                    List<Integer> singleTransaction = new ArrayList<Integer>();
                    singleTransaction.add(id);
                    admin.storeEventOperationalStatus(singleTransaction, opStatus, updatedBy, comments);
                    results.success(id.toString(), buildResourceLocator(uriInfo, RESOURCE_PREFIX, id.toString()));
                }
                catch (Exception e) {
                    log.error("Failed to update single event id : " + e.getMessage(), e);
                    results.fail(id.toString(), e.toString());
                }
            }
        }
        else {
            for (Integer id : ids ) {
                results.success(id.toString(), buildResourceLocator(uriInfo, RESOURCE_PREFIX, id.toString()));
            }
        }
        stopMetricsTimer(timer);
        return results;
    }

    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoOperationResults upsertEvents(final DtoEventList dtoEvents) {
        CollageTimer timer = startMetricsTimer();
        if (log.isDebugEnabled()) {
            log.debug(String.format("processing /POST on /events with %d events", (dtoEvents == null) ? 0 : dtoEvents.size()));
        }
        if (dtoEvents == null) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Event list was not provided").build());
        }
        if (dtoEvents.size() == 0) {
            return new DtoOperationResults("Event", DtoOperationResults.UPDATE);
        }

        // try to upsert events in one transaction: disable Hibernate session flush
        // and retry individually if single transaction fails
        DtoOperationResults results = (DtoOperationResults)HibernateProgrammaticTxnSupport.executeInTxn(
                new HibernateProgrammaticTxnSupport.RunInTxnAdapter() {
                    @Override
                    public Object run() throws Exception {
                        // upsert events transactionally
                        DtoOperationResults results = new DtoOperationResults("Event", DtoOperationResults.UPDATE);
                        upsertEvents(dtoEvents, true, false, results);
                        return results;
                    }

                    @Override
                    public boolean failed(Object result) {
                        return (((DtoOperationResults)result).getFailed() > 0);
                    }

                    @Override
                    public HibernateProgrammaticTxnSupport.RunInTxnRetry retryNotification(Object result, Exception exception) {
                        if (HibernateOptimisticLockingFailureException.class.isInstance(exception)) {
                            // We check for this exception as it is very likely due to an event being deleted outside
                            // of hibernate's awareness (e.g., due to the archival process).  If this occurs, we retry
                            // with caching disabled.
                            return HibernateProgrammaticTxnSupport.RunInTxnRetry.RETRY;
                        }
                        DtoOperationResults operationResults = (DtoOperationResults)result;
                        if ((operationResults != null) && (operationResults.getCount() == 1) && (operationResults.getSuccessful() == 0) && (dtoEvents.size() == 1)) {
                            return HibernateProgrammaticTxnSupport.RunInTxnRetry.RETURN;
                        }
                        log.debug("Retrying upsert events: " + exception, exception);
                        return HibernateProgrammaticTxnSupport.RunInTxnRetry.RETRY;
                    }

                    @Override
                    public Object retry() throws Exception {
                        // retry upsert events individually
                        DtoOperationResults results = new DtoOperationResults("Event", DtoOperationResults.UPDATE);
                        upsertEvents(dtoEvents, false, true, results);
                        return results;
                    }
                }, FlushMode.COMMIT);
        stopMetricsTimer(timer);
        return results;
    }

    /**
     * Upsert events transaction. Updated log messages cache is required if invoked within
     * a single transaction with Hibernate session flushing disabled. This is required since
     * new and modified log messages will not be available by query within the transaction.
     *
     * @param dtoEvents events to upsert as log messages
     * @param abortOnFailure abort transaction on failure
     * @param results operation results
     */
    private void upsertEvents(DtoEventList dtoEvents, boolean abortOnFailure, boolean clearCache, DtoOperationResults results) {
        CollageAdminInfrastructure admin = getAdminInfrastructureService();
        for (DtoEvent dtoEvent : dtoEvents.getEvents()) {
            if (clearCache) admin.clearMessageCache(dtoEvent.getHost(), dtoEvent.getService());
            try {
                if (dtoEvent.getAppType() == null) {
                    dtoEvent.setAppType(DtoApplicationType.DEFAULT_APP_TYPE);
                }
                if (dtoEvent.getTextMessage() == null) {
                    dtoEvent.setTextMessage("");
                }
                Properties properties = createEventPropertiesFromDto(dtoEvent);
                LogMessage logMessage = admin.updateLogMessage(dtoEvent.getMonitorServer(), dtoEvent.getAppType(),
                        dtoEvent.getDevice(), dtoEvent.getSeverity(), dtoEvent.getTextMessage(), null, null, null,
                        properties);
                String entity = logMessage.getLogMessageId().toString();
                results.success(entity, buildResourceLocator(uriInfo, RESOURCE_PREFIX, entity));
            }
            catch (Exception e) {
                String entity = failedEventEntity(dtoEvent);
                log.error("Failed to create event: " + entity, e);
                results.fail(entity, e.toString());
                if (abortOnFailure) {
                    return;
                }
            }
        }
    }

    /**
     * Generate entity string for failed event result.
     *
     * @param dtoEvent failed event
     * @return event entity
     */
    private static String failedEventEntity(DtoEvent dtoEvent) {
        return String.format("device: %s, monitor: %s, appType: %s, sev: %s",
                (dtoEvent.getDevice() == null) ? "" : dtoEvent.getDevice(),
                (dtoEvent.getMonitorServer() == null) ? "" : dtoEvent.getMonitorServer(),
                dtoEvent.getAppType(),
                (dtoEvent.getSeverity() == null) ? "" : dtoEvent.getSeverity());
    }

    private Properties createEventPropertiesFromDto(DtoEvent dtoEvent) {
        Properties properties = Nagios.createLogMessageProps(dtoEvent.getHost(),
                dtoEvent.getMonitorStatus(), formatDate(dtoEvent.getReportDate()), formatDate(dtoEvent.getLastInsertDate()),
                dtoEvent.getComponent(), dtoEvent.getErrorType(), dtoEvent.getService(), dtoEvent.getLoggerName(),
                dtoEvent.getApplicationName(), formatDate(dtoEvent.getFirstInsertDate()), dtoEvent.getTextMessage());
        if (dtoEvent.getConsolidationName() != null) {
            properties.put(LogMessage.KEY_CONSOLIDATION, dtoEvent.getConsolidationName());
        }
        if (dtoEvent.getOperationStatus() != null) {
            properties.put(LogMessage.EP_OPERATION_STATUS_NAME, dtoEvent.getOperationStatus());
        }
        putAllProperties(dtoEvent, properties);
        return properties;
    }

    private Properties createDynamicPropertiesFromDto(DtoEvent dtoEvent) {
        Properties properties = new Properties();
        putAllProperties(dtoEvent, properties);
        return properties;
    }

    @DELETE
    @Path("/{eventIds}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response deleteEvents(@PathParam("eventIds") String eventIds) {
        CollageTimer timer = startMetricsTimer();
        if (eventIds == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Event Ids is mandatory").build();
        }
        List<Integer> ids = null;
        try {
            ids = parseIds(eventIds);
        }
        catch (Exception e) {
            String message = String.format("error converting event ids %s ", eventIds);
            log.debug(message);
            return Response.status(Response.Status.BAD_REQUEST).entity(message).build();
        }
        LogMessageService eventService =  CollageFactory.getInstance().getLogMessageService();
        CollageAdminInfrastructure admin = getAdminInfrastructureService();
        DtoOperationResults results = new DtoOperationResults("Event", DtoOperationResults.DELETE);
        for (Integer id : ids) {
            try {
                LogMessage check = eventService.getLogMessageById(id);
                if (check == null) {
                    results.warn(id.toString(), "Event not found, cannot delete.");
                }
                else {
                    admin.removeLogMessage(id);
                    results.success(id.toString(), buildResourceLocator(uriInfo, RESOURCE_PREFIX, id.toString()));
                }
            }
            catch (Exception e) {
                log.error("Failed to remove single event id : " + e.getMessage(), e);
                results.fail(id.toString(), e.toString());
            }
        }
        stopMetricsTimer(timer);

        Response response = Response.ok(results).build();
        stopMetricsTimer(timer);
        return response;
    }

    @DELETE
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoOperationResults deleteEventsWithEventUpdate(DtoEventList dtoEvents) {
        CollageTimer timer = startMetricsTimer();
        if (log.isDebugEnabled()) {
            log.debug(String.format("processing /DELETE on /events with %d events", (dtoEvents == null) ? 0 : dtoEvents.size()));
        }
        if (dtoEvents == null) {
            throw new WebApplicationException(
                    Response.status(Response.Status.BAD_REQUEST).entity("Event list was not provided").build());
        }
        DtoOperationResults results = new DtoOperationResults("Event", DtoOperationResults.DELETE);
        if (dtoEvents.size() == 0) {
            return results;
        }
        LogMessageService eventService =  CollageFactory.getInstance().getLogMessageService();
        CollageAdminInfrastructure admin = getAdminInfrastructureService();
        for (DtoEvent event : dtoEvents.getEvents()) {
            if (event.getId() == null) {
                results.fail("unknown event", "failed to find event id property");
                continue;
            }
            try {
                LogMessage check = eventService.getLogMessageById(event.getId());
                if (check == null) {
                    results.warn(event.getId().toString(), "Event not found, cannot delete.");
                }
                else {
                    admin.removeLogMessage(event.getId());
                    results.success(event.getId().toString(), buildResourceLocator(uriInfo, RESOURCE_PREFIX, event.getId().toString()));
                }
            } catch (Exception e) {
                log.error("Failed to remove event: " + e.getMessage(), e);
                results.fail(event.getId().toString(), e.toString());
            }
        }
        stopMetricsTimer(timer);
        return results;
    }

    protected String getServiceIdListByServiceGroups(String serviceGroupList) {
        FilterCriteria sgFilterCriteria = createInFilterCriteria(Category.HP_NAME, serviceGroupList);
        FoundationQueryList serviceGroups = getCategoryService().getCategories(sgFilterCriteria, null, -1, -1);
        StringBuffer ids = new StringBuffer();
        if (serviceGroups.size() > 0) {
            int count = 0;
            //ids.append("(");
            for (Object o : serviceGroups.getResults()) {
                Category category = (Category)o;
                if (count > 0) ids.append(",");
                ids.append(category.getCategoryId());
                count++;
            }
            //ids.append(")");
        }
        return ids.toString();
    }

    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("/ack")
    public DtoOperationResults acknowledgeEvents(DtoAcknowledgeList acks) {
        CollageTimer timer = startMetricsTimer();
        if (log.isDebugEnabled()) {
            log.debug(String.format("processing /POST on /events/ack"));
        }
        if (acks == null) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Event Ack post data was not provided").build());
        }
        DtoOperationResults results = new DtoOperationResults("Ack", DtoOperationResults.UPDATE);
        for (DtoAcknowledge ack : acks.getAcks()) {
            try {
                if (ack.getHost() == null) {
                    results.fail("unknown-host", "hostName is required");
                    continue;
                }
                CollageAdminInfrastructure admin = getAdminInfrastructureService();
                boolean acked = admin.acknowledgeEvent(ack.getAppType(), "ACKNOWLEDGE", ack.getHost(),
                        ack.getService(), ack.getAcknowledgedBy(), ack.getAcknowledgeComment());
                String entity = ack.getHost() +
                        ((ack.getService() == null) ? "" : ":" + ack.getService());
                if (acked)
                    results.success(entity, "acknowledged " + entity);
                else
                    results.warn(entity, "failed to find NAGIOS acknowledgable Event for criteria");
            }
            catch (Exception e) {
                String entity = ack.toString();
                log.error("Failed to ack event: " + ack.toString(), e);
                results.fail(entity, e.toString());
            }
        }
        stopMetricsTimer(timer);
        return results;
    }

    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("/unack")
    public DtoOperationResults unAcknowledgeEvents(DtoUnAcknowledgeList unacks) {
        CollageTimer timer = startMetricsTimer();
        if (log.isDebugEnabled()) {
            log.debug(String.format("processing /POST on /events/unack"));
        }
        if (unacks == null) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Event UnAck post data was not provided").build());
        }
        DtoOperationResults results = new DtoOperationResults("UnAck", DtoOperationResults.UPDATE);
        for (DtoUnAcknowledge unack : unacks.getUnacks()) {
            try {
                if (unack.getHost() == null) {
                    results.fail("unknown-host", "hostName is required");
                    continue;
                }
                CollageAdminInfrastructure admin = getAdminInfrastructureService();
                boolean acked = admin.acknowledgeEvent(unack.getAppType(), "UNACKNOWLEDGE", unack.getHost(), unack.getService(), "", "");
                String entity = unack.getHost() +
                        ((unack.getService() == null) ? "" : ":" + unack.getService());
                if (acked)
                    results.success(entity, "unacknowledged " + entity);
                else
                    results.warn(entity, "failed to find NAGIOS unacknowledgable Event for criteria");
            }
            catch (Exception e) {
                String entity = unack.toString();
                log.error("Failed to unack event: " + unack.toString(), e);
                results.fail(entity, e.toString());
            }
        }
        stopMetricsTimer(timer);
        return results;
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("/stateTransitions")
    public DtoStateTransitionList getStateTransitions(@QueryParam("hostName") String hostName,
                                                      @QueryParam("serviceName") String serviceName,
                                                      @QueryParam("startDate") String startDate,
                                                      @QueryParam("endDate") String endDate) {

        CollageTimer timer = startMetricsTimer();
        try {
            if (log.isDebugEnabled()) {
                log.debug(String.format("processing /GET on /events/stateTransitions hostName: %s, serviceName: %s, startDate: %s, endDate: %s",
                        hostName, serviceName == null ? "(none)" : serviceName, startDate, endDate));
            }
            LogMessageService eventService = CollageFactory.getInstance().getLogMessageService();
            DtoStateTransitionList dtoStateTransitionList = getHostOrServiceStateTransitions(eventService, hostName,
                    serviceName, startDate, endDate);
            if (dtoStateTransitionList.size() == 0) {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                        .entity("State transitions not found").build());
            }
            return dtoStateTransitionList;
        }
        catch (Exception e) {
            if (e instanceof WebApplicationException) {
                throw (WebApplicationException) e;
            }
            log.error(ResourceMessages.UNEXPECTED_EXCEPTION + e, e);
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An error occurred processing request for event state transitions.").build());
        }
        finally {
            stopMetricsTimer(timer);
        }
    }

    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("/stateTransitions")
    public DtoStateTransitionListList getStateTransitions(DtoServiceKeyList dtoHostAndServiceKeys,
                                                          @QueryParam("startDate") String startDate,
                                                          @QueryParam("endDate") String endDate) {

        CollageTimer timer = startMetricsTimer();
        try {
            if (log.isDebugEnabled()) {
                log.debug(String.format("processing /POST on /events/stateTransitions startDate: %s, endDate: %s",
                        startDate, endDate));
            }
            if (dtoHostAndServiceKeys == null || dtoHostAndServiceKeys.size() == 0) {
                throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                        .entity("Host and service keys post data was not provided").build());
            }
            LogMessageService eventService =  CollageFactory.getInstance().getLogMessageService();
            List<DtoStateTransitionList> dtoStateTransitionLists = new ArrayList<>();
            for (DtoServiceKey dtoHostOrServiceKey : dtoHostAndServiceKeys.getServiceKeys()) {
                DtoStateTransitionList dtoStateTransitionList = getHostOrServiceStateTransitions(eventService,
                        dtoHostOrServiceKey.getHost(), dtoHostOrServiceKey.getService(), startDate, endDate);
                if (dtoStateTransitionList.size() > 0) {
                    dtoStateTransitionLists.add(dtoStateTransitionList);
                }
            }
            if (dtoStateTransitionLists.isEmpty()) {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                        .entity("State transitions not found").build());
            }
            return new DtoStateTransitionListList(dtoStateTransitionLists);
        }
        catch (Exception e) {
            if (e instanceof WebApplicationException) {
                throw (WebApplicationException) e;
            }
            log.error(ResourceMessages.UNEXPECTED_EXCEPTION + e, e);
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An error occurred processing request for event state transitions.").build());
        }
        finally {
            stopMetricsTimer(timer);
        }
    }

    private static DtoStateTransitionList getHostOrServiceStateTransitions(LogMessageService eventService,
                                                                           String hostName, String serviceName,
                                                                           String startDate, String endDate) {
        List<StateTransition> stateTransitions;
        if (serviceName == null) {
            stateTransitions = eventService.getHostStateTransitions(hostName, startDate, endDate);
        } else {
            stateTransitions = eventService.getServiceStateTransitions(hostName, serviceName, startDate, endDate);
        }
        List<DtoStateTransition> dtoStateTransitions = new ArrayList<>();
        if (stateTransitions != null) {
            for (StateTransition stateTransition : stateTransitions) {
                dtoStateTransitions.add(new DtoStateTransition(
                        stateTransition.getHostName(),
                        stateTransition.getServiceDescription(),
                        stateTransition.getFromStatus() != null ?
                                new DtoMonitorStatus(stateTransition.getFromStatus().getMonitorStatusId(),
                                        stateTransition.getFromStatus().getName(), null) : null,
                        stateTransition.getFromTransitionDate(),
                        stateTransition.getToStatus() != null ?
                                new DtoMonitorStatus(stateTransition.getToStatus().getMonitorStatusId(),
                                        stateTransition.getToStatus().getName(), null) : null,
                        stateTransition.getToTransitionDate(),
                        stateTransition.getDurationInState()));
            }
        }
        return new DtoStateTransitionList(dtoStateTransitions);
    }
}
