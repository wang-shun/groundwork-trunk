package org.groundwork.rs.resources;

import com.groundwork.collage.CollageAdminInfrastructure;
import com.groundwork.collage.CollageAdminInfrastructureUtils;
import com.groundwork.collage.CollageFactory;
import com.groundwork.collage.metrics.CollageTimer;
import com.groundwork.collage.model.ServiceStatus;
import com.groundwork.collage.query.QueryTranslation;
import com.groundwork.collage.query.QueryTranslator;
import com.groundwork.collage.util.Autocomplete;
import com.groundwork.collage.util.AutocompleteName;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.bs.hostidentity.HostIdentityService;
import org.groundwork.foundation.bs.status.StatusService;
import org.groundwork.foundation.dao.SortCriteria;
import org.groundwork.rs.async.AsyncRestProcessor;
import org.groundwork.rs.conversion.ServiceConverter;
import org.groundwork.rs.dto.DtoDepthType;
import org.groundwork.rs.dto.DtoName;
import org.groundwork.rs.dto.DtoNamesList;
import org.groundwork.rs.dto.DtoOperationResults;
import org.groundwork.rs.dto.DtoService;
import org.groundwork.rs.dto.DtoServiceList;
import org.groundwork.rs.dto.DtoSortType;
import org.groundwork.rs.tasks.ServiceCreateTask;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

@Path("/services")
public class ServiceResource extends AbstractResource {

    public static final String RESOURCE_PREFIX = "/services/";
    protected static Log log = LogFactory.getLog(ServiceResource.class);

    @GET
    @Path("/{serviceName}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoService getService(@PathParam("serviceName") String serviceName,
                                 @QueryParam("hostName") String hostName,
                                 @QueryParam("depth") @DefaultValue(DtoDepthType.DEFAULT) DtoDepthType.DtoDepthWrapper depthWrapper) {
        CollageTimer timer = startMetricsTimer();
        try {
            if (log.isDebugEnabled()) {
                log.debug(String.format("processing /GET on /services/%s with host: %s", serviceName, hostName));
            }

            // Attempt to process the serviceName as a serviceStatusId if it is numeric
            if (StringUtils.isNumeric(serviceName)) {
                int serviceStatusId = Integer.valueOf(serviceName);
                StatusService statusService = CollageFactory.getInstance().getStatusService();
                ServiceStatus serviceStatus = statusService.getServiceById(serviceStatusId);
                if (serviceStatus != null) {
                    return ServiceConverter.convert(serviceStatus, DtoDepthType.Deep);
                }
                if (hostName == null) {
                    throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                            .entity(String.format("Service id [%d] not found", serviceStatusId)).build());
                }
            }

            if (serviceName == null) {
                throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                        .entity("Service name is mandatory").build());
            }
            if (hostName == null) {
                throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                        .entity("Host name is mandatory").build());
            }
            HostIdentityService hostIdentityService = CollageFactory.getInstance().getHostIdentityService();
            ServiceStatus serviceStatus = hostIdentityService.getServiceByDescriptionAndHostIdOrHostName(serviceName, hostName);
            if (serviceStatus == null) {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                        .entity(String.format("Service/Host names [%s/%s] not found", serviceName, hostName)).build());
            }
            return ServiceConverter.convert(serviceStatus, depthWrapper.getType());
        } catch (Exception e) {
            if (e instanceof WebApplicationException)
                throw e;
            log.error(ResourceMessages.UNEXPECTED_EXCEPTION + e, e);
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(String.format("An error occurred processing request for service/host [%s/%s].",
                            serviceName, hostName)).build());
        }
        finally {
            stopMetricsTimer(timer);
        }
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoServiceList getServices(@QueryParam("query") String query,
                                      @QueryParam("hostName") String hostName,
                                      @QueryParam("first") @DefaultValue("-1") int first,
                                      @QueryParam("count") @DefaultValue("-1") int count,
                                      @QueryParam("depth") @DefaultValue(DtoDepthType.DEFAULT) DtoDepthType.DtoDepthWrapper depthWrapper) {
        CollageTimer timer = startMetricsTimer();
        try {
            if (log.isDebugEnabled()) {
                log.debug(String.format("processing /GET on /services query: %s,  first: %d, count: %d",
                         (query == null) ? "(none)" : query,  first, count));
            }
            StatusService statusService = CollageFactory.getInstance().getStatusService();
            QueryTranslator queryTranslator = CollageFactory.getInstance().getQueryTranslator();

            if (query != null && hostName != null) {
                throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                        .entity("Badly formed request. Both query and hostName parameters not allowed").build());
            }
            List<ServiceStatus> serviceStatuses = null;
            long begin = System.currentTimeMillis();
            if (query != null) {
                QueryTranslation translation = queryTranslator.translate(query, QueryTranslator.SERVICE_KEY);
                if (log.isDebugEnabled()) log.debug("hql = [" + translation.getHql() + "]");
                serviceStatuses = statusService.queryServiceStatus(translation.getHql(), translation.getCountHql(), first, count).getResults();
            }
            else if (hostName != null) {
                String hostQuery = "hostName = '" + hostName + "'";
                QueryTranslation translation = queryTranslator.translate(hostQuery, QueryTranslator.SERVICE_KEY);
                if (log.isDebugEnabled()) log.debug("hostName hql = [" + translation.getHql() + "]");
                serviceStatuses = statusService.queryServiceStatus(translation.getHql(), translation.getCountHql(), first, count).getResults();
            }
            else {
                SortCriteria sortCriteria = createSortCriteria("serviceDescription", DtoSortType.Ascending);
                serviceStatuses = statusService.getServices(null, sortCriteria, first, count).getResults();
            }
            if (log.isDebugEnabled()) {
                log.debug("service resource query time: " + (System.currentTimeMillis() - begin));
            }
            if (serviceStatuses.isEmpty()) {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                        .entity(String.format("Service statuses not found for query criteria [%s]",
                                (query != null) ? query : "(all)")).build());
            }
            begin = System.currentTimeMillis();
            List<DtoService> dtoServices= new ArrayList<DtoService>();
            for (ServiceStatus serviceStatus : serviceStatuses) {
                DtoService DtoService = ServiceConverter.convert(serviceStatus, depthWrapper.getType());
                dtoServices.add(DtoService);
            }
            DtoServiceList result = new DtoServiceList(dtoServices);
            if (log.isDebugEnabled()) {
                log.debug("service resource conversion time: " + (System.currentTimeMillis() - begin));
            }
            return result;
        } catch (Exception e) {
            if (e instanceof WebApplicationException)
                throw e;
            log.error(ResourceMessages.UNEXPECTED_EXCEPTION + e, e);
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An error occurred processing request for service statuses.").build());
        }
        finally {
            stopMetricsTimer(timer);
        }
    }

    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoOperationResults createServices(DtoServiceList dtoServices,
                                              @QueryParam("merge") @DefaultValue("true") boolean merge,
                                              @QueryParam("async") @DefaultValue("false") boolean async) {
        CollageTimer timer = startMetricsTimer();
        if (log.isDebugEnabled()) {
            log.debug(String.format("processing /POST on /services with %d services (async %b) (merge %b)", (dtoServices == null) ? 0 : dtoServices.size(), async, merge));
        }
        if (dtoServices == null) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Service Status list was not provided").build());
        }

        if (async) {
            long start = System.currentTimeMillis();
            AsyncRestProcessor processor = AsyncRestProcessor.factory();
            ServiceCreateTask task = new ServiceCreateTask("services creation job", dtoServices, merge,
                    buildResourceLocatorTemplate(uriInfo, RESOURCE_PREFIX));
            try {
                processor.submitJob(task);
                DtoOperationResults results = new DtoOperationResults("Service Async", DtoOperationResults.UPDATE);
                results.success(task.getTaskId(), "Job " + task.getTaskId() + " submitted");
                if (log.isInfoEnabled()) {
                    log.info("--- Service Async job submitted in " + (System.currentTimeMillis() - start) + " ms");
                }
                return results;
            }
            catch (RejectedExecutionException e) {
                log.error(e.getMessage(), e);
                throw new WebApplicationException(Response.status(TOO_MANY_REQUESTS).entity("Service Async Processor is overloaded rejecting call: " + e.getMessage()).build());
            } finally {
                stopMetricsTimer(timer);
            }

        }
        // execute createServices synchronously
        ServiceCreateTask task = new ServiceCreateTask("inline services creation job", dtoServices, merge,
                buildResourceLocatorTemplate(uriInfo, RESOURCE_PREFIX));
        DtoOperationResults results = task.createServices();
        stopMetricsTimer(timer);
        return results;
    }

    @DELETE
    @Path("/{serviceNames}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoOperationResults deleteServices(@PathParam("serviceNames") String serviceNames,
                                   @QueryParam("hostName") String hostName) {
        CollageTimer timer = startMetricsTimer();
        if (log.isDebugEnabled()) {
            log.debug(String.format("processing /DELETE on /services/%s with host: %s", serviceNames, hostName));
        }
        if (serviceNames == null) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Service name is mandatory").build());
        }
        if (hostName == null) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Host name is mandatory").build());
        }
        List<String> names = null;
        try {
            names = parseNames(serviceNames);
        }
        catch (Exception e) {
            String message = String.format("error converting service names %s ", serviceNames);
            log.debug(message);
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity(message).build());
        }
        HostIdentityService hostIdentityService = CollageFactory.getInstance().getHostIdentityService();
        CollageAdminInfrastructure admin = getAdminInfrastructureService();
        DtoOperationResults results = new DtoOperationResults("ServiceStatus", DtoOperationResults.DELETE);
        for (String name : names) {
            String entity = hostName + ":" + name;
            try {
                // remove service
                if (CollageAdminInfrastructureUtils.removeService(hostName, name, hostIdentityService, admin)) {
                    results.success(entity, "Service deleted.");
                } else {
                    results.warn(entity, "Service not found, cannot delete.");
                }
            }
            catch (Exception e) {
                log.error(String.format("Failed to remove service: %s. %s", entity, e.getMessage()), e);
                results.fail(entity, e.toString());
            }
        }
        stopMetricsTimer(timer);
        return results;
    }


    @DELETE
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoOperationResults deleteServicesWithUpdate(DtoServiceList dtoServiceList) {
        CollageTimer timer = startMetricsTimer();
        if (log.isDebugEnabled()) {
            log.debug(String.format("processing /DELETE on /services with %d services", (dtoServiceList == null) ? 0 : dtoServiceList.size()));
        }
        if (dtoServiceList == null) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                            .entity("Service list was not provided").build());
        }
        DtoOperationResults results = new DtoOperationResults("ServiceStatus", DtoOperationResults.DELETE);
        if (dtoServiceList.size() == 0) {
            return results;
        }
        HostIdentityService hostIdentityService = CollageFactory.getInstance().getHostIdentityService();
        CollageAdminInfrastructure admin = getAdminInfrastructureService();
        for (DtoService service : dtoServiceList.getServices()) {
            if (service.getHostName() == null) {
                results.fail("unknown host", "failed to find hostname property");
                continue;
            }
            if (service.getDescription() == null) {
                results.fail("unknown service description", "failed to find service description property");
                continue;
            }
            String entity = service.getHostName() + ":" + service.getDescription();
            try {
                // remove service
                if (CollageAdminInfrastructureUtils.removeService(service.getHostName(), service.getDescription(),
                        hostIdentityService, admin)) {
                    results.success(entity, "Service deleted.");
                } else {
                    results.warn(entity, "Service not found, cannot delete.");
                }
            } catch (Exception e) {
                log.error(String.format("Failed to remove service: %s. %s", entity, e.getMessage()), e);
                results.fail(entity, e.toString());
            }
        }
        stopMetricsTimer(timer);
        // 4. Return the results
        return results;
    }

    @GET
    @Path("/autocomplete/{prefix}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoNamesList autocomplete(@PathParam("prefix") String prefix, @QueryParam("limit") @DefaultValue("10") int limit) {
        CollageTimer timer = startMetricsTimer();
        if (log.isDebugEnabled()) {
            log.debug(String.format("processing /GET on /autocomplete/%s with limit %d", prefix, limit));
        }
        try {
            Autocomplete statusAutocompleteService = CollageFactory.getInstance().getStatusAutocompleteService();
            List<AutocompleteName> names = statusAutocompleteService.autocomplete(prefix, limit);
            if (names.isEmpty()) {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity(String.format("Service status descriptions not found for prefix [%s]", prefix)).build());
            }
            List<DtoName> dtoNames = new ArrayList<DtoName>();
            for (AutocompleteName name : names) {
                dtoNames.add(new DtoName(name.getName()));
            }
            return new DtoNamesList(dtoNames);
        } catch (WebApplicationException wae) {
            throw wae;
        } catch (Exception e) {
            log.error(ResourceMessages.UNEXPECTED_EXCEPTION + e, e);
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(String.format("An error occurred processing request for autocomplete [%s].", prefix)).build());
        } finally {
            stopMetricsTimer(timer);
        }
    }
}
