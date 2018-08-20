package org.groundwork.rs.resources;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.rs.async.AsyncRestProcessor;
import org.groundwork.rs.auth.AuthAccessInfo;
import org.groundwork.rs.auth.AuthService;
import org.groundwork.rs.dto.DtoAsyncSettings;
import org.groundwork.rs.dto.DtoOperationResults;
import org.groundwork.rs.dto.DtoToken;
import org.groundwork.rs.dto.DtoTokensList;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

@Path("/settings")
public class SettingsResource {

    protected static Log log = LogFactory.getLog(SettingsResource.class);

    @GET
    @Path("/async")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoAsyncSettings getAsyncSettings() {
        try {
            if (log.isDebugEnabled()) {
                log.debug("processing /GET on /settings/async");
            }
            return AsyncRestProcessor.factory().getSettings();
        } catch (Exception e) {
            if (e instanceof WebApplicationException)
                throw e;
            log.error(ResourceMessages.UNEXPECTED_EXCEPTION + e, e);
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
                    ("An error occurred processing request for async settings.")).build());
        } finally {
        }
    }


//    TODO: http://jira/browse/GWMON-12908
//    A Rest API for listing authentication tokens. Authentication tokens can be depleted.
//    This API is to be used internally in the future. It will be disabled until we add authorization to the Rest APIs.
//    @GET
//    @Path("/tokens")
//    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoTokensList getTokenSettings() {
        try {
            if (log.isDebugEnabled()) {
                log.debug("processing /GET on /settings/tokens");
            }
            DtoTokensList tokens = new DtoTokensList();
            Map<String,AuthAccessInfo> result = AuthService.getInstance().listAccessTokens();
            for (Map.Entry<String,AuthAccessInfo> entry : result.entrySet()) {
                tokens.add(new DtoToken(entry.getKey(), entry.getValue().getAppName()));
            }
            return tokens;
        } catch (Exception e) {
            if (e instanceof WebApplicationException)
                throw e;
            log.error(ResourceMessages.UNEXPECTED_EXCEPTION + e, e);
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
                    ("An error occurred processing request for tokens settings.")).build());
        } finally {
        }
    }

    @PUT
    @Path("/async")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoOperationResults updateAsyncSettings(DtoAsyncSettings settings) {
        if (log.isDebugEnabled()) {
            log.debug("processing /PUT on /settings/async");
        }
        if (settings == null) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Host list was not provided").build());
        }
        boolean wasSet = AsyncRestProcessor.factory().setSettings(settings);
        DtoOperationResults results = new DtoOperationResults("AsyncSettings", DtoOperationResults.UPDATE);
        if (wasSet)
            results.success("AsyncSettings", "successfully updated settings");
        else
            results.fail("AsyncSettings", "async processor busy, could not set");
        return results;
    }

}
