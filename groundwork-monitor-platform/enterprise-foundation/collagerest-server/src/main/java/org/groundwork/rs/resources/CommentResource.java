package org.groundwork.rs.resources;

import com.groundwork.collage.CollageFactory;
import com.groundwork.collage.metrics.CollageTimer;
import com.groundwork.collage.model.Comment;
import com.groundwork.collage.model.Host;
import com.groundwork.collage.model.ServiceStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.bs.comment.CommentService;
import org.groundwork.foundation.bs.host.HostService;
import org.groundwork.foundation.bs.status.StatusService;
import org.groundwork.rs.dto.DtoComment;
import org.groundwork.rs.dto.DtoOperationResults;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/comments")
public class CommentResource extends AbstractResource {

    public static final String RESOURCE_PREFIX = "/comments/";
    protected static Log log = LogFactory.getLog(CommentResource.class);

    @POST
    @Path("/host/{hostid}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoOperationResults addHostComment(@PathParam("hostid") Integer hostId, DtoComment dtoComment) {
        if (hostId == null) throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("Host ID must not be null").build());
        if (dtoComment == null) throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("Comment not provided").build());
        CollageTimer timer = startMetricsTimer();
        DtoOperationResults results = new DtoOperationResults("Comment", DtoOperationResults.INSERT);
        HostService hostService = CollageFactory.getInstance().getHostService();
        Host host = hostService.getHostByHostId(hostId);
        if (host == null) throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("Host ID is invalid").build());
        CommentService commentService = CollageFactory.getInstance().getCommentService();
        Comment comment = commentService.createHostComment(hostId, dtoComment.getNotes(), dtoComment.getAuthor());
        host.addComment(comment);
        hostService.saveHost(host);
        stopMetricsTimer(timer);
        results.success(Integer.toString(comment.getCommentId()), "Host comment added");
        return results;
    }

    @POST
    @Path("/service/{serviceid}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoOperationResults addServiceComment(@PathParam("serviceid") Integer serviceId, DtoComment dtoComment) {
        if (serviceId == null) throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("Service ID must not be null").build());
        if (dtoComment == null) throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("Comment not provided").build());
        CollageTimer timer = startMetricsTimer();
        DtoOperationResults results = new DtoOperationResults("Comment", DtoOperationResults.INSERT);
        StatusService statusService = CollageFactory.getInstance().getStatusService();
        ServiceStatus service = statusService.getServiceById(serviceId);
        if (service == null) throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("Service ID is invalid").build());
        CommentService commentService =  CollageFactory.getInstance().getCommentService();
        Comment comment = commentService.createServiceComment(serviceId, dtoComment.getNotes(), dtoComment.getAuthor());
        service.addComment(comment);
        statusService.saveService(service);
        stopMetricsTimer(timer);
        results.success(Integer.toString(comment.getCommentId()), "Service comment added");
        return results;
    }

    @DELETE
    @Path("/host/{hostid}/{commentid}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoOperationResults deleteHostComment(@PathParam("hostid") Integer hostId, @PathParam("commentid") Integer id) {
        if (hostId == null) throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("Host ID must not be null").build());
        if (id == null) throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("Comment ID must not be null").build());
        CollageTimer timer = startMetricsTimer();
        DtoOperationResults results = new DtoOperationResults("Comment", DtoOperationResults.DELETE);
        HostService hostService = CollageFactory.getInstance().getHostService();
        Host host = hostService.getHostByHostId(hostId);
        if (host == null) throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("Host ID is invalid").build());
        CommentService commentService = CollageFactory.getInstance().getCommentService();
        Comment comment = commentService.get(id);
        if (comment == null) throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("Comment ID is invalid").build());
        host.removeComment(comment);
        commentService.delete(id);
        stopMetricsTimer(timer);
        results.success(id.toString(), "Comment deleted.");
        return results;
    }

    @DELETE
    @Path("/service/{serviceid}/{commentid}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoOperationResults deleteServiceComment(@PathParam("serviceid") Integer serviceId, @PathParam("commentid") Integer id) {
        if (serviceId == null) throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("Service ID must not be null").build());
        if (id == null) throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("Comment ID must not be null").build());
        CollageTimer timer = startMetricsTimer();
        DtoOperationResults results = new DtoOperationResults("Comment", DtoOperationResults.DELETE);
        StatusService statusService = CollageFactory.getInstance().getStatusService();
        ServiceStatus service = statusService.getServiceById(serviceId);
        if (service == null) throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("Service ID is invalid").build());
        CommentService commentService = CollageFactory.getInstance().getCommentService();
        Comment comment = commentService.get(id);
        if (comment == null) throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("Comment ID is invalid").build());
        service.removeComment(comment);
        commentService.delete(id);
        stopMetricsTimer(timer);
        results.success(id.toString(), "Comment deleted.");
        return results;
    }

}
