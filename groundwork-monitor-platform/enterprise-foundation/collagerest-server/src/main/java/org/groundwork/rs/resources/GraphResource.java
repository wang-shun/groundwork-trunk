package org.groundwork.rs.resources;

import com.groundwork.collage.CollageFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.bs.rrd.RRDService;
import org.groundwork.foundation.ws.model.impl.RRDGraph;
import org.groundwork.rs.dto.DtoGraph;
import org.groundwork.rs.dto.DtoGraphList;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.Date;

@Path("/graphs")
public class GraphResource {

    public static final String RESOURCE_PREFIX = "/graphs/";
    protected static Log log = LogFactory.getLog(GraphResource.class);

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoGraphList getGraphs(@QueryParam("applicationType") @DefaultValue("NAGIOS") String applicationType,
                                  @QueryParam("hostName") String hostName,
                                  @QueryParam("serviceName") String serviceName,
                                  @QueryParam("startDate") @DefaultValue("-1") long startDate,
                                  @QueryParam("endDate") @DefaultValue("-1") long endDate,
                                  @QueryParam("graphWidth") @DefaultValue("-1") int graphWidth) {
        try {
            graphWidth = (graphWidth <= 0) ? RRDService.DEFAULT_RRD_WIDTH : graphWidth;
            endDate = (endDate <= 0) ? new Date().getTime() : endDate;
            startDate = (startDate <= 0) ? (new Date().getTime() - (60 * 60 * 24)) : startDate;
            if (log.isDebugEnabled()) {
                log.debug(String.format("processing /GET on /graphs with host: %s, service: %s, appType: %s, startDate: %d, endDate %d, width: %d",
                        (hostName == null) ? "" : hostName,
                        (serviceName == null) ? "" : serviceName,
                        applicationType,
                        startDate,
                        endDate,
                        graphWidth));
            }
            if (hostName == null) {
                throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("Host name is mandatory").build());
            }
            RRDService graphService = CollageFactory.getInstance().getRRDService();
            Collection<RRDGraph> graphs = graphService.generateGraph(applicationType, hostName, serviceName, startDate, endDate, graphWidth);
            DtoGraphList dtoGraphs = new DtoGraphList();
            if (graphs != null) {
                for (RRDGraph graph : graphs) {
                    dtoGraphs.add(new DtoGraph(graph.getRrdLabel(), graph.getGraph()));
                }
            }
            return dtoGraphs;
        } catch (Exception e) {
            if (e instanceof WebApplicationException)
                throw e;
            log.error(ResourceMessages.UNEXPECTED_EXCEPTION + e, e);
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An error occurred processing request for graphs.").build());
        } finally {
        }
    }
}
