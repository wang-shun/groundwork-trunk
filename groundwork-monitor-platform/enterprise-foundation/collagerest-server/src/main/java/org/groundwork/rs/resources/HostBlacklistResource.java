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

package org.groundwork.rs.resources;

import com.groundwork.collage.CollageFactory;
import com.groundwork.collage.model.HostBlacklist;
import com.groundwork.collage.query.QueryTranslation;
import com.groundwork.collage.query.QueryTranslator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.bs.hostblacklist.HostBlacklistService;
import org.groundwork.rs.conversion.HostBlacklistConverter;
import org.groundwork.rs.dto.DtoHostBlacklist;
import org.groundwork.rs.dto.DtoHostBlacklistList;
import org.groundwork.rs.dto.DtoOperationResults;

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

/**
 * HostBlacklistResource
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
@Path("/hostblacklists")
public class HostBlacklistResource extends AbstractResource {

    public static final String RESOURCE_PREFIX = "/hostblacklists/";

    private static Log log = LogFactory.getLog(HostBlacklistResource.class);

    @GET
    @Path("/{host_name}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoHostBlacklist getHostBlacklist(@PathParam("host_name") String hostName) {
        if (log.isDebugEnabled()) {
            log.debug(String.format("processing GET on /hostblacklists for %s", hostName));
        }
        if (hostName == null) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("HostBlacklist host name was not provided").build());
        }
        HostBlacklistService hostBlacklistService = CollageFactory.getInstance().getHostBlacklistService();
        try {
            HostBlacklist hostBlacklist = hostBlacklistService.getHostBlacklistByHostName(hostName);
            if (hostBlacklist == null) {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity(String.format("HostBlacklist host name [%s] was not found", hostName)).build());
            }
            return HostBlacklistConverter.convert(hostBlacklist);
        } catch (WebApplicationException wae) {
            throw wae;
        } catch (Exception e) {
            log.error(ResourceMessages.UNEXPECTED_EXCEPTION + e, e);
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(String.format("An error occurred processing request for HostBlacklist host name [%s]", hostName)).build());
        }
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoHostBlacklistList getHostBlacklists(@QueryParam("query") String query,
                                                  @QueryParam("first") @DefaultValue("-1") int first,
                                                  @QueryParam("count") @DefaultValue("-1") int count) {
        try {
            if (log.isDebugEnabled()) {
                log.debug(String.format("processing GET on /hostblacklists with query: %s, first: %d, count: %d", (query == null) ? "(none)" : query,  first, count));
            }
            HostBlacklistService hostBlacklistService = CollageFactory.getInstance().getHostBlacklistService();
            QueryTranslator queryTranslator = CollageFactory.getInstance().getQueryTranslator();
            List<HostBlacklist> hostBlacklists = null;
            if (query != null) {
                QueryTranslation translation = queryTranslator.translate(query, QueryTranslator.HOST_BLACKLIST_KEY);
                if (log.isDebugEnabled()) {
                    log.debug("hql = [" + translation.getHql() + "]");
                }
                hostBlacklists = hostBlacklistService.queryHostBlacklists(translation.getHql(), translation.getCountHql(), first, count).getResults();
            } else {
                hostBlacklists = hostBlacklistService.getHostBlacklists(null, null, first, count).getResults();
            }
            if (hostBlacklists.isEmpty()) {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity(String.format("HostBlacklists not found for query criteria [%s]", (query != null) ? query : "(all)")).build());
            }
            DtoHostBlacklistList dtoHostBlacklists = new DtoHostBlacklistList();
            for (HostBlacklist hostBlacklist : hostBlacklists) {
                dtoHostBlacklists.add(HostBlacklistConverter.convert(hostBlacklist));
            }
            return dtoHostBlacklists;
        } catch (WebApplicationException wae) {
            throw wae;
        } catch (Exception e) {
            log.error(ResourceMessages.UNEXPECTED_EXCEPTION + e, e);
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("An error occurred processing request for hostblacklists.").build());
        }
    }

    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoOperationResults createOrUpdateHostBlacklists(DtoHostBlacklistList dtoHostBlacklists) {
        if (log.isDebugEnabled()) {
            log.debug(String.format("processing POST on /hostblacklists with %d HostBlacklists", (dtoHostBlacklists == null) ? 0 : dtoHostBlacklists.size()));
        }
        if (dtoHostBlacklists == null) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("HostBlacklists list was not provided").build());
        }
        if (dtoHostBlacklists.size() == 0) {
            return new DtoOperationResults(HostBlacklist.ENTITY_TYPE_CODE, DtoOperationResults.INSERT);
        }
        // optimize create all in one transaction
        HostBlacklistService hostBlacklistService = CollageFactory.getInstance().getHostBlacklistService();
        boolean createAll = true;
        for (DtoHostBlacklist dtoHostBlacklist : dtoHostBlacklists.getHostBlacklists()) {
            if (dtoHostBlacklist.getHostBlacklistId() != null) {
                createAll = false;
                break;
            }
        }
        // attempt to convert and create all host blacklists in one transaction
        if (createAll) {
            DtoOperationResults results = new DtoOperationResults(HostBlacklist.ENTITY_TYPE_CODE, DtoOperationResults.INSERT);
            try {
                // convert host blacklists
                List<HostBlacklist> createHostBlacklists = new ArrayList<HostBlacklist>(dtoHostBlacklists.size());
                for (DtoHostBlacklist dtoHostBlacklist : dtoHostBlacklists.getHostBlacklists()) {
                    createHostBlacklists.add(hostBlacklistService.createHostBlacklist(dtoHostBlacklist.getHostName()));
                }
                // save host blacklists
                hostBlacklistService.saveHostBlacklists(createHostBlacklists);
                // add successes to results
                for (HostBlacklist hostBlacklist : createHostBlacklists) {
                    results.success(hostBlacklist.getHostName(), buildResourceLocator(uriInfo, RESOURCE_PREFIX, hostBlacklist.getHostName()));
                }
                return results;
            } catch (Exception e) {
                // if there is only one host blacklist that has failed to convert or
                // save, emit that result
                if (dtoHostBlacklists.size() == 1) {
                    // add failure to results
                    String message = "Failed to create HostBlacklist: " + e.getMessage();
                    results.fail(dtoHostBlacklists.getHostBlacklists().get(0).getHostName(), message);
                    log.error(message, e);
                    return results;
                }
            }
        }
        // process host blacklists one at a time in order to ensure that
        // the results are returned in order and that individual host
        // blacklists may be saved even if others fail
        DtoOperationResults results = new DtoOperationResults(HostBlacklist.ENTITY_TYPE_CODE, (createAll ? DtoOperationResults.INSERT : DtoOperationResults.UPDATE));
        for (DtoHostBlacklist dtoHostBlacklist : dtoHostBlacklists.getHostBlacklists()) {
            if (dtoHostBlacklist.getHostBlacklistId() == null) {
                // create host blacklist
                try {
                    // convert host blacklist
                    HostBlacklist hostBlacklist = hostBlacklistService.createHostBlacklist(dtoHostBlacklist.getHostName());
                    // save host blacklist
                    hostBlacklistService.saveHostBlacklist(hostBlacklist);
                    // add success to results
                    results.success(hostBlacklist.getHostName(), buildResourceLocator(uriInfo, RESOURCE_PREFIX, hostBlacklist.getHostName()));
                } catch (Exception e) {
                    // add failure to results
                    String message = "Failed to create HostBlacklist: " + e.getMessage();
                    results.fail(dtoHostBlacklist.getHostName(), message);
                    log.error(message, e);
                }
            } else {
                // update host blacklist
                try {
                    // load host blacklist
                    HostBlacklist hostBlacklist = hostBlacklistService.getHostBlacklistById(dtoHostBlacklist.getHostBlacklistId());
                    if (hostBlacklist != null) {
                        // update and save host blacklist
                        hostBlacklist.setHostName(dtoHostBlacklist.getHostName());
                        hostBlacklistService.saveHostBlacklist(hostBlacklist);
                        // add success to results
                        results.success(hostBlacklist.getHostBlacklistId().toString(), buildResourceLocator(uriInfo, RESOURCE_PREFIX, hostBlacklist.getHostName()));
                    } else {
                        results.fail(dtoHostBlacklist.getHostBlacklistId().toString(), "HostBlacklist not found to update");
                    }
                } catch (Exception e) {
                    // add failure to results
                    String message = "Failed to update HostBlacklist: " + e.getMessage();
                    results.fail(dtoHostBlacklist.getHostBlacklistId().toString(), message);
                    log.error(message, e);
                }
            }
        }
        return results;
    }

    @DELETE
    @Path("/{host_names}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoOperationResults deleteHostBlacklists(@PathParam("host_names") String hostNamesString) {
        if (log.isDebugEnabled()) {
            log.debug(String.format("processing DELETE on /hostblacklists for %s", hostNamesString));
        }
        List<String> hostNames = null;
        try {
            hostNames = parseNames(hostNamesString);
        } catch (Exception e) {
            String message = String.format("Error converting host names [%s]", hostNamesString);
            log.debug(message);
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity(message).build());
        }
        HostBlacklistService hostBlacklistService = CollageFactory.getInstance().getHostBlacklistService();
        DtoOperationResults results = new DtoOperationResults(HostBlacklist.ENTITY_TYPE_CODE, DtoOperationResults.DELETE);
        for (String hostName : hostNames) {
            try {
                // delete host blacklist
                if (hostBlacklistService.deleteHostBlacklistByHostName(hostName)) {
                    // add success to results
                    results.success(hostName, "HostBlacklist deleted");
                } else {
                    // add warning to results
                    results.warn(hostName, "HostBlacklist not found, cannot delete");
                }
            } catch (Exception e) {
                String message = "Failed to delete or clear HostBlacklist: " + e.getMessage();
                results.fail(hostName, message);
                log.error(message, e);
            }
        }
        return results;
    }

    @DELETE
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoOperationResults deleteHostBlacklists(DtoHostBlacklistList dtoHostBlacklists) {
        if (log.isDebugEnabled()) {
            log.debug(String.format("processing DELETE on /hostblacklists with %d HostBlacklists", ((dtoHostBlacklists == null) ? 0 : dtoHostBlacklists.size())));
        }
        if (dtoHostBlacklists == null) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("HostBlacklists list was not provided").build());
        }
        HostBlacklistService hostBlacklistService = CollageFactory.getInstance().getHostBlacklistService();
        DtoOperationResults results = new DtoOperationResults(HostBlacklist.ENTITY_TYPE_CODE, DtoOperationResults.DELETE);
        for (DtoHostBlacklist dtoHostBlacklist : dtoHostBlacklists.getHostBlacklists()) {
            try {
                if (dtoHostBlacklist.getHostBlacklistId() != null) {
                    // delete host blacklist
                    hostBlacklistService.deleteHostBlacklistById(dtoHostBlacklist.getHostBlacklistId());
                    // add success to results
                    results.success(dtoHostBlacklist.getHostBlacklistId().toString(), "HostBlacklist deleted");
                } else {
                    // delete host blacklist
                    if (hostBlacklistService.deleteHostBlacklistByHostName(dtoHostBlacklist.getHostName())) {
                        // add success to results
                        results.success(dtoHostBlacklist.getHostName(), "HostBlacklist deleted");
                    } else {
                        // add warning to results
                        results.warn(dtoHostBlacklist.getHostName(), "HostBlacklist not found, cannot delete");
                    }
                }
            } catch (Exception e) {
                String message = "Failed to delete or clear HostBlacklist: " + e.getMessage();
                results.fail(dtoHostBlacklist.getHostName(), message);
                log.error(message, e);
            }
        }
        return results;
    }
}
