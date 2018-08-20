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

import com.groundwork.collage.CollageAdminInfrastructure;
import com.groundwork.collage.CollageFactory;
import com.groundwork.collage.model.HostIdentity;
import com.groundwork.collage.query.QueryTranslation;
import com.groundwork.collage.query.QueryTranslator;
import com.groundwork.collage.util.Autocomplete;
import com.groundwork.collage.util.AutocompleteName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.bs.hostidentity.HostIdentityService;
import org.groundwork.rs.async.AsyncRestProcessor;
import org.groundwork.rs.conversion.HostIdentityConverter;
import org.groundwork.rs.dto.DtoHostIdentity;
import org.groundwork.rs.dto.DtoHostIdentityList;
import org.groundwork.rs.dto.DtoName;
import org.groundwork.rs.dto.DtoNamesList;
import org.groundwork.rs.dto.DtoOperationResults;
import org.groundwork.rs.tasks.HostIdentityCreateOrUpdateTask;

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

/**
 * HostIdentityResource
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
@Path("/hostidentities")
public class HostIdentityResource extends AbstractResource {

    public static final String RESOURCE_PREFIX = "/hostidentities/";

    private static Log log = LogFactory.getLog(HostIdentityResource.class);

    @GET
    @Path("/{id_or_host_name}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoHostIdentity getHostIdentity(@PathParam("id_or_host_name") String idOrHostName) {
        if (log.isDebugEnabled()) {
            log.debug(String.format("processing GET on /hostidentities for %s", idOrHostName));
        }
        if (idOrHostName == null) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("HostIdentity id or host name was not provided").build());
        }
        HostIdentityService hostIdentityService = CollageFactory.getInstance().getHostIdentityService();
        try {
            HostIdentity hostIdentity = hostIdentityService.getHostIdentityByIdOrHostName(idOrHostName);
            if (hostIdentity == null) {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity(String.format("HostIdentity id or host name [%s] was not found", idOrHostName)).build());
            }
            return HostIdentityConverter.convert(hostIdentity);
        } catch (WebApplicationException wae) {
            throw wae;
        } catch (Exception e) {
            log.error(ResourceMessages.UNEXPECTED_EXCEPTION + e, e);
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(String.format("An error occurred processing request for HostIdentity id or host name [%s]", idOrHostName)).build());
        }
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoHostIdentityList getHostIdentities(@QueryParam("query") String query,
                                                 @QueryParam("first") @DefaultValue("-1") int first,
                                                 @QueryParam("count") @DefaultValue("-1") int count) {
        try {
            if (log.isDebugEnabled()) {
                log.debug(String.format("processing GET on /hostidentities with query: %s, first: %d, count: %d", (query == null) ? "(none)" : query,  first, count));
            }
            HostIdentityService hostIdentityService = CollageFactory.getInstance().getHostIdentityService();
            QueryTranslator queryTranslator = CollageFactory.getInstance().getQueryTranslator();
            List<HostIdentity> hostIdentities = null;
            if (query != null) {
                QueryTranslation translation = queryTranslator.translate(query, QueryTranslator.HOST_IDENTITY_KEY);
                if (log.isDebugEnabled()) {
                    log.debug("hql = [" + translation.getHql() + "]");
                }
                hostIdentities = hostIdentityService.queryHostIdentities(translation.getHql(), translation.getCountHql(), first, count).getResults();
            } else {
                hostIdentities = hostIdentityService.getHostIdentities(null, null, first, count).getResults();
            }
            if (hostIdentities.isEmpty()) {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity(String.format("HostIdentities not found for query criteria [%s]", (query != null) ? query : "(all)")).build());
            }
            DtoHostIdentityList dtoHostIdentities = new DtoHostIdentityList();
            for (HostIdentity hostIdentity : hostIdentities) {
                dtoHostIdentities.add(HostIdentityConverter.convert(hostIdentity));
            }
            return dtoHostIdentities;
        } catch (WebApplicationException wae) {
            throw wae;
        } catch (Exception e) {
            log.error(ResourceMessages.UNEXPECTED_EXCEPTION + e, e);
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("An error occurred processing request for hostidentities.").build());
        }
    }

    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoOperationResults createOrUpdateHostIdentities(DtoHostIdentityList dtoHostIdentities,
                                                            @QueryParam("async") @DefaultValue("false") boolean async) {
        if (log.isDebugEnabled()) {
            log.debug(String.format("processing POST on /hostidentites with %d HostIdentities", (dtoHostIdentities == null) ? 0 : dtoHostIdentities.size()));
        }
        if (dtoHostIdentities == null) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("HostIdentities list was not provided").build());
        }
        if (async) {
            long start = System.currentTimeMillis();
            AsyncRestProcessor processor = AsyncRestProcessor.factory();
            HostIdentityCreateOrUpdateTask task = new HostIdentityCreateOrUpdateTask("HostIdentity creation job", dtoHostIdentities, buildResourceLocatorTemplate(uriInfo, RESOURCE_PREFIX));
            try {
                processor.submitJob(task);
                DtoOperationResults results = new DtoOperationResults("HostIdentity Async", DtoOperationResults.INSERT);
                results.success(task.getTaskId(), "Job " + task.getTaskId() + " submitted");
                if (log.isInfoEnabled()) {
                    log.info("--- HostIdentity Async job submitted in " + (System.currentTimeMillis() - start) + " ms");
                }
                return results;
            }
            catch (RejectedExecutionException e) {
                log.error(e.getMessage(), e);
                throw new WebApplicationException(Response.status(TOO_MANY_REQUESTS).entity("HostIdentity Async Processor is overloaded rejecting call: " + e.getMessage()).build());
            }
        }
        // execute createOrUpdateHostIdentities synchronously
        HostIdentityCreateOrUpdateTask task = new HostIdentityCreateOrUpdateTask("inline HostIdentity creation job", dtoHostIdentities, buildResourceLocatorTemplate(uriInfo, RESOURCE_PREFIX));
        return task.createOrUpdateHostIdentities();
    }

    @DELETE
    @Path("/{id_or_host_names}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoOperationResults deleteHostIdentities(@PathParam("id_or_host_names") String idOrHostNamesString,
                                                    @QueryParam("clear") @DefaultValue("false") boolean clear) {
        if (log.isDebugEnabled()) {
            log.debug(String.format("processing DELETE on /hostidentities for %s, clear is %b", idOrHostNamesString, clear));
        }
        List<String> idOrHostNames = null;
        try {
            idOrHostNames = parseNames(idOrHostNamesString);
        } catch (Exception e) {
            String message = String.format("Error converting ids or host names [%s]", idOrHostNamesString);
            log.debug(message);
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity(message).build());
        }
        HostIdentityService hostIdentityService = CollageFactory.getInstance().getHostIdentityService();
        CollageAdminInfrastructure admin = getAdminInfrastructureService();
        DtoOperationResults results = new DtoOperationResults(HostIdentity.ENTITY_TYPE_CODE,  (clear ? DtoOperationResults.CLEAR : DtoOperationResults.DELETE));
        for (String idOrHostName : idOrHostNames) {
            try {
                if (clear) {
                    // clear host identity
                    HostIdentity hostIdentity = hostIdentityService.getHostIdentityByIdOrHostName(idOrHostName);
                    if (hostIdentity != null) {
                        // removing host identity host names: reset host message status
                        if ((hostIdentity.getHost() != null) && (hostIdentity.getHostNames().size() > 1)) {
                            admin.resetHostStatusMessage(hostIdentity.getHost().getHostName());
                        }
                        // clear host names and add success to results
                        hostIdentityService.removeAllHostNamesFromHostIdentity(idOrHostName);
                        results.success(idOrHostName, "HostIdentity cleared");
                    } else {
                        results.fail(idOrHostName, "HostIdentity not found, cannot delete");
                    }
                } else {
                    // delete host identity
                    HostIdentity hostIdentity = hostIdentityService.getHostIdentityByIdOrHostName(idOrHostName);
                    if (hostIdentity != null) {
                        // removing host identity host names: reset host message status
                        if ((hostIdentity.getHost() != null) && (hostIdentity.getHostNames().size() > 1)) {
                            admin.resetHostStatusMessage(hostIdentity.getHost().getHostName());
                        }
                        // delete host identity and add success to results
                        hostIdentityService.deleteHostIdentity(hostIdentity);
                        results.success(idOrHostName, "HostIdentity deleted");
                    } else {
                        // add warning to results
                        results.warn(idOrHostName, "HostIdentity not found, cannot delete");
                    }
                }
            } catch (Exception e) {
                String message = "Failed to delete or clear HostIdentity: " + e.getMessage();
                results.fail(idOrHostName, message);
                log.error(message, e);
            }
        }
        return results;
    }

    @DELETE
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoOperationResults deleteHostIdentities(DtoHostIdentityList dtoHostIdentities,
                                                    @QueryParam("clear") @DefaultValue("false") boolean clear) {
        if (log.isDebugEnabled()) {
            log.debug(String.format("processing DELETE on /hostidentites with %d HostIdentities, clear is %b", ((dtoHostIdentities == null) ? 0 : dtoHostIdentities.size()), clear));
        }
        if (dtoHostIdentities == null) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("HostIdentities list was not provided").build());
        }
        HostIdentityService hostIdentityService = CollageFactory.getInstance().getHostIdentityService();
        CollageAdminInfrastructure admin = getAdminInfrastructureService();
        DtoOperationResults results = new DtoOperationResults(HostIdentity.ENTITY_TYPE_CODE, (clear ? DtoOperationResults.CLEAR : DtoOperationResults.DELETE));
        for (DtoHostIdentity dtoHostIdentity : dtoHostIdentities.getHostIdentities()) {
            try {
                String idOrHostName = dtoHostIdentity.getHostName();
                if (dtoHostIdentity.getHostIdentityId() != null) {
                    idOrHostName = dtoHostIdentity.getHostIdentityId().toString();
                }
                if (clear) {
                    // clear host identity
                    HostIdentity hostIdentity = hostIdentityService.getHostIdentityByIdOrHostName(idOrHostName);
                    if (hostIdentity != null) {
                        // removing host identity host names: reset host message status
                        if ((hostIdentity.getHost() != null) && (hostIdentity.getHostNames().size() > 1)) {
                            admin.resetHostStatusMessage(hostIdentity.getHost().getHostName());
                        }
                        // clear host names and add success to results
                        hostIdentityService.removeAllHostNamesFromHostIdentity(idOrHostName);
                        results.success(idOrHostName, "HostIdentity cleared");
                    } else {
                        results.warn(idOrHostName, "HostIdentity not found, cannot clear");
                    }
                } else {
                    // delete host identity
                    HostIdentity hostIdentity = hostIdentityService.getHostIdentityByIdOrHostName(idOrHostName);
                    if (hostIdentity != null) {
                        // removing host identity host names: reset host message status
                        if ((hostIdentity.getHost() != null) && (hostIdentity.getHostNames().size() > 1)) {
                            admin.resetHostStatusMessage(hostIdentity.getHost().getHostName());
                        }
                        // delete host identity and add success to results
                        hostIdentityService.deleteHostIdentity(hostIdentity);
                        results.success(idOrHostName, "HostIdentity deleted");
                    } else {
                        // add warning to results
                        results.warn(idOrHostName, "HostIdentity not found, cannot delete");
                    }
                }
            } catch (Exception e) {
                String message = "Failed to delete or clear HostIdentity: " + e.getMessage();
                results.fail(dtoHostIdentity.getHostName(), message);
                log.error(message, e);
            }
        }
        return results;
    }

    @GET
    @Path("/autocomplete/{prefix}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoNamesList autocomplete(@PathParam("prefix") String prefix, @QueryParam("limit") @DefaultValue("10") int limit) {
        if (log.isDebugEnabled()) {
            log.debug(String.format("processing /GET on /autocomplete/%s with limit %d", prefix, limit));
        }
        try {
            Autocomplete hostIdentityAutocompleteService = CollageFactory.getInstance().getHostIdentityAutocompleteService();
            List<AutocompleteName> names = hostIdentityAutocompleteService.autocomplete(prefix, limit);
            if (names.isEmpty()) {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity(String.format("Host identity names not found for prefix [%s]", prefix)).build());
            }
            List<DtoName> dtoNames = new ArrayList<DtoName>();
            for (AutocompleteName name : names) {
                dtoNames.add(new DtoName(name.getName(), name.getCanonicalName()));
            }
            return new DtoNamesList(dtoNames);
        } catch (WebApplicationException wae) {
            throw wae;
        } catch (Exception e) {
            log.error(ResourceMessages.UNEXPECTED_EXCEPTION + e, e);
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(String.format("An error occurred processing request for autocomplete [%s].", prefix)).build());
        }
    }
}
