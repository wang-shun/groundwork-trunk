package org.groundwork.rs.resources;
/*
 * Collage - The ultimate monitoring data integration framework.
 *
 * Copyright (C) 2004-2014  GroundWork Open Source Solutions info@groundworkopensource.com
 *     This program is free software; you can redistribute it and/or modify
 *     it under the terms of version 2 of the GNU General Public License
 *     as published by the Free Software Foundation.
 *
 */

import com.groundwork.collage.CollageAdminInfrastructure;
import com.groundwork.collage.CollageAdminInfrastructureUtils;
import com.groundwork.collage.CollageFactory;
import com.groundwork.collage.impl.admin.CollageAdminImpl;
import com.groundwork.collage.metrics.CollageTimer;
import com.groundwork.collage.model.Host;
import com.groundwork.collage.model.HostGroup;
import com.groundwork.collage.query.QueryTranslation;
import com.groundwork.collage.query.QueryTranslator;
import com.groundwork.collage.util.Autocomplete;
import com.groundwork.collage.util.AutocompleteName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.bs.host.HostService;
import org.groundwork.foundation.bs.hostidentity.HostIdentityService;
import org.groundwork.foundation.dao.SortCriteria;
import org.groundwork.rs.async.AsyncRestProcessor;
import org.groundwork.rs.conversion.HostConverter;
import org.groundwork.rs.dto.DtoDepthType;
import org.groundwork.rs.dto.DtoHost;
import org.groundwork.rs.dto.DtoHostList;
import org.groundwork.rs.dto.DtoName;
import org.groundwork.rs.dto.DtoNamesList;
import org.groundwork.rs.dto.DtoOperationResults;
import org.groundwork.rs.dto.DtoSortType;
import org.groundwork.rs.tasks.HostCreateTask;

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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.RejectedExecutionException;


@Path("/hosts")
public class HostResource extends AbstractResource {
    public static final String RESOURCE_PREFIX = "/hosts/";
    protected static Log log = LogFactory.getLog(HostResource.class);

    @GET
    @Path("/{host_name}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoHost getHost(@PathParam("host_name") String hostName,
            @QueryParam("depth") @DefaultValue(DtoDepthType.DEFAULT) DtoDepthType.DtoDepthWrapper depthWrapper) {
        CollageTimer timer = startMetricsTimer();
        try {
            DtoDepthType depth = depthWrapper.getType();
            if (log.isDebugEnabled()) {
                log.debug(String.format("processing /GET on /hosts/%s with depth: %s", hostName, depth));
            }
            if (hostName == null) {
                throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("Host name is mandatory").build());
            }
            HostIdentityService hostIdentityService = CollageFactory.getInstance().getHostIdentityService();
            Host host = hostIdentityService.getHostByIdOrHostName(hostName);
            if (host == null) {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                        .entity(String.format("Host name [%s] was not found", hostName)).build());
            }
            return HostConverter.convert(host, depth);
        } catch (Exception e) {
            if (e instanceof WebApplicationException)
                throw e;
            log.error(ResourceMessages.UNEXPECTED_EXCEPTION + e, e);
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(String.format("An error occurred processing request for host [%s].", hostName)).build());
        }
        finally {
            stopMetricsTimer(timer);
        }
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoHostList getHosts(@QueryParam("query") String query,
                     @QueryParam("depth") @DefaultValue(DtoDepthType.DEFAULT) DtoDepthType.DtoDepthWrapper depthWrapper,
                     @QueryParam("first") @DefaultValue("-1") int first, @QueryParam("count") @DefaultValue("-1") int count) {
        CollageTimer timer = startMetricsTimer();
        try {
            DtoDepthType depth = depthWrapper.getType();
            if (log.isDebugEnabled()) {
                log.debug(String.format("processing /GET on /hosts with depth: %s, query: %s,  first: %d, count: %d",
                   depth, (query == null) ? "(none)" : query,  first, count));
            }
            HostService hostService =  CollageFactory.getInstance().getHostService();
            QueryTranslator queryTranslator = CollageFactory.getInstance().getQueryTranslator();

            List<Host> hosts = null;
            long begin = System.currentTimeMillis();
            if (query != null) {
                QueryTranslation translation = queryTranslator.translate(query, QueryTranslator.HOST_KEY);
                if (log.isDebugEnabled()) log.debug("hql = [" + translation.getHql() + "]");
                hosts = hostService.queryHosts(translation.getHql(), translation.getCountHql(), first, count).getResults();
            }
            else {
                SortCriteria sortCriteria = createSortCriteria("hostName", DtoSortType.Ascending);
                hosts = hostService.getHosts(null, sortCriteria, first, count).getResults();
            }
            if (log.isDebugEnabled()) {
                log.debug("hosts resource query time: " + (System.currentTimeMillis() - begin));
            }
            if (hosts.isEmpty()) {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                        .entity(String.format("Hosts not found for query criteria [%s]", (query != null) ? query : "(all)")).build());
            }
            begin = System.currentTimeMillis();
            List<DtoHost> dtoHosts = new ArrayList<DtoHost>();
            for (Host host : hosts) {
                DtoHost dtoHost = HostConverter.convert(host, depth);
                dtoHosts.add(dtoHost);
            }
            DtoHostList result = new DtoHostList(dtoHosts);
            if (log.isDebugEnabled()) {
                log.debug("host resource conversion time: " + (System.currentTimeMillis() - begin));
            }
            return result;
        } catch (Exception e) {
            if (e instanceof WebApplicationException)
                throw e;
            log.error(ResourceMessages.UNEXPECTED_EXCEPTION + e, e);
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An error occurred processing request for hosts.").build());
        }
        finally {
            stopMetricsTimer(timer);
        }
    }

    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoOperationResults upsertHosts(DtoHostList dtoHosts,
                                           @QueryParam("merge") @DefaultValue("true") boolean merge,
                                           @QueryParam("async") @DefaultValue("false") boolean async) {
        CollageTimer timer = startMetricsTimer();
        if (log.isDebugEnabled()) {
            log.debug(String.format("processing /POST on /hosts with %d hosts", (dtoHosts == null) ? 0 : dtoHosts.size()));
        }
        if (dtoHosts == null) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                        .entity("Host list was not provided").build());
        }

        if (async) {
            long start = System.currentTimeMillis();
            AsyncRestProcessor processor = AsyncRestProcessor.factory();
            HostCreateTask task = new HostCreateTask("hosts creation job", dtoHosts, merge,
                    buildResourceLocatorTemplate(uriInfo, RESOURCE_PREFIX));
            try {
                processor.submitJob(task);
                DtoOperationResults results = new DtoOperationResults("Host Async", DtoOperationResults.UPDATE);
                results.success(task.getTaskId(), "Job " + task.getTaskId() + " submitted");
                if (log.isInfoEnabled()) {
                    log.info("--- Host Async job submitted in " + (System.currentTimeMillis() - start) + " ms");
                }
                stopMetricsTimer(timer);
                return results;
            }
            catch (RejectedExecutionException e) {
                log.error(e.getMessage(), e);
                throw new WebApplicationException(Response.status(TOO_MANY_REQUESTS).entity("Host Async Processor is overloaded rejecting call: " + e.getMessage()).build());
            }
        }
        // execute upsertHosts synchronously
        HostCreateTask task = new HostCreateTask("inline host creation job", dtoHosts, merge,
                buildResourceLocatorTemplate(uriInfo, RESOURCE_PREFIX));
        DtoOperationResults results = task.upsertHosts();
        stopMetricsTimer(timer);
        return results;
    }

    @DELETE
    @Path("/{hostNames}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoOperationResults deleteHosts(@PathParam("hostNames") String hostNames) {
        CollageTimer timer = startMetricsTimer();
        if (log.isDebugEnabled()) {
            log.debug(String.format("processing /DELETE on /hosts for %s", hostNames));
        }
        List<String> names = null;
        try {
            names = parseNames(hostNames);
        }
        catch (Exception e) {
            String message = String.format("error converting host names %s ", hostNames);
            log.debug(message);
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity(message).build());
        }
        HostIdentityService hostIdentityService = CollageFactory.getInstance().getHostIdentityService();
        CollageAdminInfrastructure admin = getAdminInfrastructureService();
        DtoOperationResults results = new DtoOperationResults("Host", DtoOperationResults.DELETE);
        for (String hostName : names) {
            try {
                // remove host
                if (CollageAdminInfrastructureUtils.removeHost(hostName, hostIdentityService, admin)) {
                    results.success(hostName, "Host deleted.");
                } else {
                    results.warn(hostName, "Host not found, cannot delete.");
                }
            } catch (Exception e) {
                log.error(String.format("Failed to remove host: %s. %s", hostName, e.getMessage()), e);
                results.fail(hostName, e.toString());
            }
        }
        stopMetricsTimer(timer);
        return results;
    }

    @DELETE
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoOperationResults deleteHostsWithHostUpdate(DtoHostList dtoHosts) {
        CollageTimer timer = startMetricsTimer();
        if (log.isDebugEnabled()) {
            log.debug(String.format("processing /DELETE on /hosts with %d hosts", (dtoHosts == null) ? 0 : dtoHosts.size()));
        }
        if (dtoHosts == null) {
            throw new WebApplicationException(
                    Response.status(Response.Status.BAD_REQUEST).entity("Host list was not provided").build());
        }
        DtoOperationResults results = new DtoOperationResults("Host", DtoOperationResults.DELETE);
        if (dtoHosts.size() == 0) {
            return results;
        }
        HostIdentityService hostIdentityService = CollageFactory.getInstance().getHostIdentityService();
        CollageAdminInfrastructure admin = getAdminInfrastructureService();
        for (DtoHost host : dtoHosts.getHosts()) {
            if (host.getHostName() == null) {
                results.fail("unknown host", "failed to find hostname property");
                continue;
            }
            try {
                // remove host
                if (CollageAdminInfrastructureUtils.removeHost(host.getHostName(), hostIdentityService, admin)) {
                    results.success(host.getHostName(), "Host deleted.");
                } else {
                    results.warn(host.getHostName(), "Host not found, cannot delete.");
                }
            } catch (Exception e) {
                log.error("Failed to remove host: " + e.getMessage(), e);
                results.fail(host.getHostName(), e.toString());
            }
        }
        stopMetricsTimer(timer);
        // 4. Return the results
        return results;
    }

    @PUT
    @Path("/rename")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoHost rename(@QueryParam("oldHostName") String oldHostName,
                                      @QueryParam("newHostName") String newHostName,
                                      @QueryParam("description") String description,
                                      @QueryParam("deviceIdentification") String deviceIdentification)
    {
        CollageTimer timer = startMetricsTimer();
        if (oldHostName == null) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("Old Host name is mandatory").build());
        }
        if (newHostName == null) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("New Host name is mandatory").build());
        }
        if (log.isDebugEnabled()) {
            log.debug(String.format("processing /PUT on /hosts/rename with old hostname: %s, new hostname: %s", oldHostName, newHostName));
        }
        try {
            CollageAdminInfrastructure admin = getAdminInfrastructureService();
            Host host = admin.renameHost(oldHostName, newHostName, description, deviceIdentification);
            if (host == null) {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                        .entity(String.format("Existing Host name [%s] was not found", oldHostName)).build());
            }
            return HostConverter.convert(host, DtoDepthType.Shallow);
        } catch (Exception e) {
            if (e.getMessage().startsWith(CollageAdminImpl.DUPLICATE_ERROR)) {
                throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                        .entity(String.format("New Host name [%s] was not unique", newHostName)).build());
            }
            if (e instanceof WebApplicationException)
                throw e;
            log.error(ResourceMessages.UNEXPECTED_EXCEPTION + e, e);
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build());
        } finally {
            stopMetricsTimer(timer);
        }

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
            Autocomplete hostAutocompleteService = CollageFactory.getInstance().getHostAutocompleteService();
            List<AutocompleteName> names = hostAutocompleteService.autocomplete(prefix, limit);
            if (names.isEmpty()) {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity(String.format("Host names not found for prefix [%s]", prefix)).build());
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

    @GET
    @Path("/filter/hostgroups")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoHostList filterHostsByHostGroups(@QueryParam("hostGroupNames") String hostGroupNames,
                                               @QueryParam("depth") @DefaultValue(DtoDepthType.DEFAULT) DtoDepthType.DtoDepthWrapper depthWrapper)
    {
        CollageTimer timer = startMetricsTimer();
        try {
            DtoDepthType depth = depthWrapper.getType();
            if (log.isDebugEnabled()) {
                log.debug(String.format("processing /GET on /hosts/filter/hostgroups with hostgroups: %s, depth: %s",
                        hostGroupNames, depth));
            }
            if (hostGroupNames == null) {
                throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("HostGroup names is mandatory").build());
            }
            HostService hostService =  CollageFactory.getInstance().getHostService();
            List<String> names = null;
            Set<String> uniqueGroups = new HashSet<>();
            try {
                names = parseNames(hostGroupNames);
                for (String name : names) {
                    uniqueGroups.add(name);
                }
            }
            catch (Exception e) {
                String message = String.format("error converting host group names %s ", hostGroupNames);
                log.debug(message);
                throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity(message).build());
            }
            List<Host> hosts = null;
            SortCriteria sortCriteria = createSortCriteria("hostName", DtoSortType.Ascending);
            hosts = hostService.getHosts(null, sortCriteria, -1, -1).getResults();
            if (hosts.isEmpty()) {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                        .entity(String.format("Hosts not found for filter criteria [%s]", hostGroupNames)).build());
            }
            List<DtoHost> dtoHosts = new ArrayList<DtoHost>();
            for (Host host : hosts) {
                for (Object group : host.getHostGroups()) {
                    HostGroup hostGroup = (HostGroup)group;
                    if (uniqueGroups.contains(hostGroup.getName())) {
                        DtoHost dtoHost = HostConverter.convert(host, depth);
                        dtoHosts.add(dtoHost);
                        break;
                    }
                }
            }
            DtoHostList list = new DtoHostList(dtoHosts);;
            return list;
        } catch (Exception e) {
            if (e instanceof WebApplicationException)
                throw e;
            log.error(ResourceMessages.UNEXPECTED_EXCEPTION + e, e);
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An error occurred processing request for filter hosts.").build());
        }
        finally {
            stopMetricsTimer(timer);
        }
    }

}

