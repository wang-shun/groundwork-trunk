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
import com.groundwork.collage.model.AuditLog;
import com.groundwork.collage.query.QueryTranslation;
import com.groundwork.collage.query.QueryTranslator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.bs.auditlog.AuditLogService;
import org.groundwork.rs.async.AsyncRestProcessor;
import org.groundwork.rs.conversion.AuditLogConverter;
import org.groundwork.rs.dto.DtoAuditLogList;
import org.groundwork.rs.dto.DtoOperationResults;
import org.groundwork.rs.tasks.AuditLogCreateTask;

import javax.ws.rs.Consumes;
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
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

/**
 * AuditLogResource
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
@Path("/auditlogs")
public class AuditLogResource extends AbstractResource {

    private static Log log = LogFactory.getLog(AuditLogResource.class);

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoAuditLogList getAuditLogs(@QueryParam("query") String query,
                                        @QueryParam("first") @DefaultValue("-1") int first,
                                        @QueryParam("count") @DefaultValue("-1") int count) {
        try {
            if (log.isDebugEnabled()) {
                log.debug(String.format("processing GET on /auditlogs with query: %s, first: %d, count: %d", (query == null) ? "(none)" : query,  first, count));
            }
            AuditLogService auditLogService = CollageFactory.getInstance().getAuditLogService();
            QueryTranslator queryTranslator = CollageFactory.getInstance().getQueryTranslator();
            List<AuditLog> auditLogs = null;
            if (query != null) {
                QueryTranslation translation = queryTranslator.translate(query, QueryTranslator.AUDIT_LOG_KEY);
                if (log.isDebugEnabled()) {
                    log.debug("hql = [" + translation.getHql() + "]");
                }
                auditLogs = auditLogService.queryAuditLogs(translation.getHql(), translation.getCountHql(), first, count).getResults();
            } else {
                auditLogs = auditLogService.getAuditLogs(null, null, first, count).getResults();
            }
            if (auditLogs.isEmpty()) {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity(String.format("AuditLogs not found for query criteria [%s]", (query != null) ? query : "(all)")).build());
            }
            DtoAuditLogList dtoAuditLogs = new DtoAuditLogList();
            for (AuditLog auditLog : auditLogs) {
                dtoAuditLogs.add(AuditLogConverter.convert(auditLog));
            }
            return dtoAuditLogs;
        } catch (WebApplicationException wae) {
            throw wae;
        } catch (Exception e) {
            log.error(ResourceMessages.UNEXPECTED_EXCEPTION + e, e);
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("An error occurred processing request for auditlogs.").build());
        }
    }

    @GET
    @Path("/{host_name}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoAuditLogList getHostAuditLogs(@PathParam("host_name") String hostName,
                                            @QueryParam("first") @DefaultValue("-1") int first,
                                            @QueryParam("count") @DefaultValue("-1") int count) {
        try {
            if (log.isDebugEnabled()) {
                log.debug(String.format("processing GET on /auditlogs/%s with first: %d, count: %d", hostName, first, count));
            }
            AuditLogService auditLogService = CollageFactory.getInstance().getAuditLogService();
            List<AuditLog> auditLogs = auditLogService.getHostAuditLogs(hostName, first, count).getResults();
            if (auditLogs.isEmpty()) {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity(String.format("AuditLogs not found for host name %s", hostName)).build());
            }
            DtoAuditLogList dtoAuditLogs = new DtoAuditLogList();
            for (AuditLog auditLog : auditLogs) {
                dtoAuditLogs.add(AuditLogConverter.convert(auditLog));
            }
            return dtoAuditLogs;
        } catch (WebApplicationException wae) {
            throw wae;
        } catch (Exception e) {
            log.error(ResourceMessages.UNEXPECTED_EXCEPTION + e, e);
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("An error occurred processing request for auditlogs.").build());
        }
    }

    @GET
    @Path("/{host_name}/{service_description}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoAuditLogList getHostAndServiceAuditLogs(@PathParam("host_name") String hostName,
                                                      @PathParam("service_description") String serviceDescription,
                                                      @QueryParam("first") @DefaultValue("-1") int first,
                                                      @QueryParam("count") @DefaultValue("-1") int count) {
        try {
            if (log.isDebugEnabled()) {
                log.debug(String.format("processing GET on /auditlogs/%s/%s with first: %d, count: %d", hostName, serviceDescription, first, count));
            }
            AuditLogService auditLogService = CollageFactory.getInstance().getAuditLogService();
            List<AuditLog> auditLogs = auditLogService.getServiceAuditLogs(hostName, serviceDescription, first, count).getResults();
            if (auditLogs.isEmpty()) {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity(String.format("AuditLogs not found for host name '%s' and service description '%s'", hostName, serviceDescription)).build());
            }
            DtoAuditLogList dtoAuditLogs = new DtoAuditLogList();
            for (AuditLog auditLog : auditLogs) {
                dtoAuditLogs.add(AuditLogConverter.convert(auditLog));
            }
            return dtoAuditLogs;
        } catch (WebApplicationException wae) {
            throw wae;
        } catch (Exception e) {
            log.error(ResourceMessages.UNEXPECTED_EXCEPTION + e, e);
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("An error occurred processing request for auditlogs.").build());
        }
    }

    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoOperationResults createAuditLogs(DtoAuditLogList dtoAuditLogs,
                                               @QueryParam("async") @DefaultValue("true") boolean async) {
        if (log.isDebugEnabled()) {
            log.debug(String.format("processing POST on /auditlogs with %d AuditLogs", (dtoAuditLogs == null) ? 0 : dtoAuditLogs.size()));
        }
        if (dtoAuditLogs == null) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("AuditLogs list was not provided").build());
        }
        if (async) {
            long start = System.currentTimeMillis();
            AsyncRestProcessor processor = AsyncRestProcessor.factory();
            AuditLogCreateTask task = new AuditLogCreateTask("AuditLog creation job", dtoAuditLogs);
            try {
                processor.submitJob(task);
                DtoOperationResults results = new DtoOperationResults("AuditLog Async", DtoOperationResults.INSERT);
                results.success(task.getTaskId(), "Job " + task.getTaskId() + " submitted");
                if (log.isInfoEnabled()) {
                    log.info("--- AuditLog Async job submitted in " + (System.currentTimeMillis() - start) + " ms");
                }
                return results;
            }
            catch (RejectedExecutionException e) {
                log.error(e.getMessage(), e);
                throw new WebApplicationException(Response.status(TOO_MANY_REQUESTS).entity("AuditLog Async Processor is overloaded rejecting call: " + e.getMessage()).build());
            }
        }
        // execute createAuditLogs synchronously
        AuditLogCreateTask task = new AuditLogCreateTask("inline AuditLog creation job", dtoAuditLogs);
        return task.createAuditLogs();
    }
}
