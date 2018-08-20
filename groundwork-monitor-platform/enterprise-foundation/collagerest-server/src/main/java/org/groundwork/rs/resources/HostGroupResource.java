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
import com.groundwork.collage.metrics.CollageTimer;
import com.groundwork.collage.model.Host;
import com.groundwork.collage.model.HostGroup;
import com.groundwork.collage.query.QueryTranslation;
import com.groundwork.collage.query.QueryTranslator;
import com.groundwork.collage.util.Autocomplete;
import com.groundwork.collage.util.AutocompleteName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.bs.hostgroup.HostGroupService;
import org.groundwork.foundation.bs.hostidentity.HostIdentityService;
import org.groundwork.foundation.dao.SortCriteria;
import org.groundwork.rs.conversion.HostGroupConverter;
import org.groundwork.rs.dto.DtoDepthType;
import org.groundwork.rs.dto.DtoHost;
import org.groundwork.rs.dto.DtoHostGroup;
import org.groundwork.rs.dto.DtoHostGroupList;
import org.groundwork.rs.dto.DtoName;
import org.groundwork.rs.dto.DtoNamesList;
import org.groundwork.rs.dto.DtoOperationResults;
import org.groundwork.rs.dto.DtoSortType;

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
import java.util.LinkedList;
import java.util.List;


@Path("/hostgroups")
public class HostGroupResource extends AbstractResource  {

    public static final String RESOURCE_PREFIX = "/hostgroups/";

    protected static Log log = LogFactory.getLog(HostGroupResource.class);


    @GET
    @Path("/{hostGroup_name}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoHostGroup getHostGroup(@PathParam("hostGroup_name") String hostGroupName,
                            @QueryParam("depth") @DefaultValue(DtoDepthType.DEFAULT) DtoDepthType.DtoDepthWrapper depthWrapper) {
        CollageTimer timer = startMetricsTimer();
        try {
            DtoDepthType depth = depthWrapper.getType();
            if (log.isDebugEnabled()) {
                log.debug(String.format("processing /GET on /hostgroups/%s with depth: %s", hostGroupName, depth));
            }
            if (hostGroupName == null) {
                throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("Host Group name is mandatory").build());
            }
            HostGroupService hostGroupService =  CollageFactory.getInstance().getHostGroupService();
            HostGroup hostGroup = hostGroupService.getHostGroupByName(hostGroupName);
            if (hostGroup == null) {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                        .entity(String.format("Host Group name [%s] was not found", hostGroupName)).build());
            }
            return HostGroupConverter.convert(hostGroup, depth);
        } catch (Exception e) {
            if (e instanceof WebApplicationException)
                throw e;
            log.error(ResourceMessages.UNEXPECTED_EXCEPTION + e, e);
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(String.format("An error occurred processing request for hostGroup [%s].", hostGroupName)).build());
        }
        finally {
            stopMetricsTimer(timer);
        }

    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoHostGroupList getHostGroups(@QueryParam("query") String query,
                     @QueryParam("depth") @DefaultValue(DtoDepthType.DEFAULT) DtoDepthType.DtoDepthWrapper depthWrapper,
                     @QueryParam("first") @DefaultValue("-1") int first, @QueryParam("count") @DefaultValue("-1") int count) {
        CollageTimer timer = startMetricsTimer();
        try {
            DtoDepthType depth = depthWrapper.getType();
            if (log.isDebugEnabled()) {
                log.debug(String.format("processing /GET on /hostgroups with depth: %s, query: %s,  first: %d, count: %d",
                        depth, (query == null) ? "(none)" : query,  first, count));
            }
            HostGroupService hostGroupService =  CollageFactory.getInstance().getHostGroupService();
            QueryTranslator queryTranslator = CollageFactory.getInstance().getQueryTranslator();

            List<HostGroup> hostGroups = null;
            if (query != null) {
                QueryTranslation translation = queryTranslator.translate(query, QueryTranslator.HOSTGROUP_KEY);
                if (log.isDebugEnabled()) log.debug("hql = [" + translation.getHql() + "]");
                hostGroups = hostGroupService.queryHostGroups(translation.getHql(), translation.getCountHql(), first, count).getResults();
            }
            else {
                SortCriteria sortCriteria = createSortCriteria("name", DtoSortType.Ascending);
                hostGroups = hostGroupService.getHostGroups(null, sortCriteria, first, count).getResults();
            }
            if (hostGroups.isEmpty()) {
                throw new WebApplicationException(
                        Response.status(Response.Status.NOT_FOUND).entity(String.format("Host Groups not found for query criteria [%s]",
                                (query != null) ? query : "(all)")).build());
            }
            List<DtoHostGroup> dtoHostGroups = new ArrayList<DtoHostGroup>();
            for (HostGroup hostGroup : hostGroups) {
                DtoHostGroup dtoHostGroup = HostGroupConverter.convert(hostGroup, depth);
                dtoHostGroups.add(dtoHostGroup);
            }
            return new DtoHostGroupList(dtoHostGroups);
        } catch (Exception e) {
            if (e instanceof WebApplicationException)
                throw e;
            log.error(ResourceMessages.UNEXPECTED_EXCEPTION + e, e);
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An error occurred processing request for host groups.").build());
        }
        finally {
            stopMetricsTimer(timer);
        }
    }

    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoOperationResults createHostGroups(DtoHostGroupList dtoHostGroupList) {
        CollageTimer timer = startMetricsTimer();
        if (log.isDebugEnabled()) {
            log.debug(String.format("processing /POST on /hostGroups with %d hostGroups",
                    (dtoHostGroupList == null) ? 0 : dtoHostGroupList.size()));
        }
        if (dtoHostGroupList == null) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Host Group list was not provided").build());
        }
        DtoOperationResults results = new DtoOperationResults("HostGroup", DtoOperationResults.UPDATE);
        if (dtoHostGroupList.size() == 0) {
            return results;
        }
        CollageAdminInfrastructure admin = getAdminInfrastructureService();
        HostIdentityService hostIdentityService = CollageFactory.getInstance().getHostIdentityService();
        List<String> hostsNotFound = new LinkedList<String>();
        for (DtoHostGroup groupUpdate : dtoHostGroupList.getHostGroups()) {
            if (groupUpdate.getName() == null) {
                results.fail("hostGroupName Unknown", "No host Group Name provided");
                continue;
            }
            List<String> hosts = new LinkedList<String>();
            if (groupUpdate.getHosts() != null) {
                for (DtoHost dtoHost : groupUpdate.getHosts()) {
                    Host host = hostIdentityService.getHostByIdOrHostName(dtoHost.getHostName());
                    if (host == null) {
                        hostsNotFound.add(dtoHost.getHostName());
                    } else {
                        hosts.add(host.getHostName());
                    }
                }
            }
            try {
                admin.updateHostGroup(groupUpdate.getAppType(), groupUpdate.getName(), hosts,
                        groupUpdate.getAlias(), groupUpdate.getDescription(), groupUpdate.getAgentId());
                results.success(groupUpdate.getName(), buildResourceLocator(uriInfo, RESOURCE_PREFIX, groupUpdate.getName()));
            }
            catch (Exception e) {
                results.fail(groupUpdate.getName(), e.getMessage());
            }
        }
        addToNotFoundList(hostsNotFound, results, "HostGroup");
        stopMetricsTimer(timer);
        return results;
    }

    @DELETE
    @Path("/{hostGroupNames}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoOperationResults removeHostGroup(@PathParam("hostGroupNames") String hostGroupNames,
                                     @QueryParam("clear") @DefaultValue("false") String clear) {
        CollageTimer timer = startMetricsTimer();
        boolean isClear = (clear.equalsIgnoreCase("true"));
        if (log.isDebugEnabled()) {
            log.debug(String.format("processing /DELETE on /hostgroups for %s, clear is %s", hostGroupNames, clear));
        }
        List<String> names = null;
        try {
            names = parseNames(hostGroupNames);
        }
        catch (Exception e) {
            String message = String.format("error converting host group names %s ", hostGroupNames);
            log.debug(message);
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity(message).build());
        }
        DtoOperationResults results = new DtoOperationResults("HostGroup", (isClear ? DtoOperationResults.CLEAR : DtoOperationResults.DELETE));
        for (String hostGroupName : names) {
            executeDeleteTransaction(isClear, hostGroupName, results);
        }
        stopMetricsTimer(timer);
        return results;
    }

    @DELETE
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoOperationResults deleteHostGroups(DtoHostGroupList dtoHostGroupList,
                        @QueryParam("clear") @DefaultValue("false") String clear) {
        CollageTimer timer = startMetricsTimer();
        boolean isClear = (clear.equalsIgnoreCase("true"));
        if (log.isDebugEnabled()) {
            log.debug(String.format("processing /DELETE on /hostgroups with %d hostgroups and clear = %s",
                    (dtoHostGroupList == null) ? 0 : dtoHostGroupList.size(), clear));
        }
        if (dtoHostGroupList == null) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Host Group list was not provided").build());
        }
        DtoOperationResults results = new DtoOperationResults("HostGroup", (isClear ? DtoOperationResults.CLEAR : DtoOperationResults.DELETE));
        if (dtoHostGroupList.size() == 0) {
            return results;
        }
        for (DtoHostGroup group : dtoHostGroupList.getHostGroups()) {
            executeDeleteTransaction(isClear, group.getName(), results);
        }
        stopMetricsTimer(timer);
        return results;
    }


    protected void executeDeleteTransaction(boolean isClear, String hostGroupName, DtoOperationResults results) {
        CollageAdminInfrastructure admin = getAdminInfrastructureService();
        HostGroupService hostGroupService =  CollageFactory.getInstance().getHostGroupService();
        try {
            if (isClear) {
                HostGroup hostGroup = hostGroupService.getHostGroupByName(hostGroupName);
                if (hostGroup != null) {
                    List<String> hostNames = new LinkedList<String>();
                    for (Object hostObject : hostGroup.getHosts()) {
                        hostNames.add(((Host)hostObject).getHostName());
                    }
                    admin.removeHostsFromHostGroup(hostGroup.getName(), hostNames);
                    results.success(hostGroupName, "Host Group cleared.");
                }
                else {
                    results.fail(hostGroupName, "Host group not found");
                }
            }
            else {
                // remove host group
                if (CollageAdminInfrastructureUtils.removeHostGroup(hostGroupName, admin)) {
                    results.success(hostGroupName, "Host Group deleted.");
                } else {
                    results.warn(hostGroupName, "Host group not found, cannot delete.");
                }
            }
        }
        catch (Exception e) {
            log.error(String.format("Failed to remove hostGroup: %s. %s", hostGroupName, e.getMessage()), e);
            results.fail(hostGroupName, e.toString());
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
            Autocomplete hostGroupAutocompleteService = CollageFactory.getInstance().getHostGroupAutocompleteService();
            List<AutocompleteName> names = hostGroupAutocompleteService.autocomplete(prefix, limit);
            if (names.isEmpty()) {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity(String.format("Host group names not found for prefix [%s]", prefix)).build());
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
