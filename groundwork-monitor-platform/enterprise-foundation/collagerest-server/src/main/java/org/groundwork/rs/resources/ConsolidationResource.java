package org.groundwork.rs.resources;

import com.groundwork.collage.CollageAdminInfrastructure;
import com.groundwork.collage.CollageFactory;
import com.groundwork.collage.model.ConsolidationCriteria;
import com.groundwork.collage.query.QueryTranslation;
import com.groundwork.collage.query.QueryTranslator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.bs.logmessage.ConsolidationService;
import org.groundwork.foundation.dao.SortCriteria;
import org.groundwork.rs.dto.DtoConsolidation;
import org.groundwork.rs.dto.DtoConsolidationList;
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
import java.util.List;

@Path("/consolidations")
public class ConsolidationResource extends AbstractResource {

    public static final String RESOURCE_PREFIX = "/consolidations/";
    protected static Log log = LogFactory.getLog(ConsolidationResource.class);

    @GET
    @Path("/{consolidationName}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoConsolidation getConsolidation(@PathParam("consolidationName") String consolidationName) {
        try {
            if (log.isDebugEnabled()) {
                log.debug(String.format("processing /GET on /consolidations/%s", consolidationName));
            }
            if (consolidationName == null) {
                throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                        .entity("Consolidation name is mandatory").build());
            }
            ConsolidationService consolidationService =  CollageFactory.getInstance().getConsolidationService();
            ConsolidationCriteria consolidation = consolidationService.getConsolidationCriteriaByName(consolidationName);
            if (consolidation == null) {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                        .entity(String.format("Consolidation name [%s] was not found", consolidationName)).build());
            }
            return new DtoConsolidation(consolidation.getConsolidationCriteriaId(), consolidation.getName(), consolidation.getCriteria());
        } catch (Exception e) {
            if (e instanceof WebApplicationException)
                throw e;
            log.error(ResourceMessages.UNEXPECTED_EXCEPTION + e, e);
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
                    String.format("An error occurred processing request for consolidation [%s].", consolidationName)).build());
        }
        finally {
        }
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoConsolidationList getConsolidations(@QueryParam("query") String query,
                                    @QueryParam("first") @DefaultValue("-1") int first, @QueryParam("count") @DefaultValue("-1") int count) {
        try {
            if (log.isDebugEnabled()) {
                log.debug(String.format("processing /GET on /consolidations with  query: %s,  first: %d, count: %d",
                                       (query == null) ? "(none)" : query,  first, count));
            }
            ConsolidationService consolidationService =  CollageFactory.getInstance().getConsolidationService();
            QueryTranslator queryTranslator = CollageFactory.getInstance().getQueryTranslator();
            List<ConsolidationCriteria> consolidations  = null;
            if (query != null) {
                QueryTranslation translation = queryTranslator.translate(query, QueryTranslator.CONSOLIDATION_TYPE_KEY);
                if (log.isDebugEnabled()) log.debug("hql = [" + translation.getHql() + "]");
                consolidations = consolidationService.query(translation.getHql(), translation.getCountHql(), first, count).getResults();
            }
            else {
                SortCriteria sortCriteria = createSortCriteria("name", DtoSortType.Ascending);
                consolidations = new ArrayList(consolidationService.getConsolidationCriterias(null, sortCriteria));
            }

            if (consolidations.isEmpty()) {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                        .entity(String.format("Consolidations not found for query criteria [%s]",
                                (query != null) ? query : "(all)")).build());
            }
            List<DtoConsolidation> dtoConsolidations = new ArrayList<DtoConsolidation>();
            for (ConsolidationCriteria consolidation : consolidations) {
                dtoConsolidations.add(new DtoConsolidation(consolidation.getConsolidationCriteriaId(), consolidation.getName(), consolidation.getCriteria()));
            }
            return new DtoConsolidationList(dtoConsolidations);
        } catch (Exception e) {
            if (e instanceof WebApplicationException)
                throw e;
            log.error(ResourceMessages.UNEXPECTED_EXCEPTION + e, e);
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An error occurred processing request for consolidations.").build());
        }
        finally {
        }
    }

    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoOperationResults createConsolidations(DtoConsolidationList dtoConsolidations) {
        if (log.isDebugEnabled()) {
            log.debug(String.format("processing /POST on /consolidations with %d consolidations",
                    (dtoConsolidations == null) ? 0 : dtoConsolidations.size()));
        }
        if (dtoConsolidations == null) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Consolidation list was not provided").build());
        }
        DtoOperationResults results = new DtoOperationResults("Consolidation", DtoOperationResults.UPDATE);
        if (dtoConsolidations.size() == 0) {
            return results;
        }
        CollageAdminInfrastructure admin = getAdminInfrastructureService();
        for (DtoConsolidation dto : dtoConsolidations.getConsolidations()) {
            if (dto.getName() == null) {
                results.fail("consolidationName Unknown", "No Consolidation name provided");
                continue;
            }
            try {
                admin.addOrUpdateConsolidationCriteria(dto.getName(), dto.getCriteria());
                results.success(dto.getName(), buildResourceLocator(uriInfo, RESOURCE_PREFIX, dto.getName()));
            }
            catch (Exception e) {
                log.error(String.format("Failed to save consolidation: %s. %s", dto.getName(), e.getMessage()), e);
                results.fail(dto.getName(), e.getMessage());
            }
        }
        return results;
    }

    @DELETE
    @Path("/{consolidationNames}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoOperationResults removeHostGroup(@PathParam("consolidationNames") String consolidationNames) {
        if (log.isDebugEnabled()) {
            log.debug(String.format("processing /DELETE on /consolidations for %s", consolidationNames));
        }
        List<String> names = null;
        try {
            names = parseNames(consolidationNames);
        }
        catch (Exception e) {
            String message = String.format("error converting consolidation names %s ", consolidationNames);
            log.debug(message);
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity(message).build());
        }
        DtoOperationResults results = new DtoOperationResults("HostGroup", DtoOperationResults.DELETE);
        CollageAdminInfrastructure admin = getAdminInfrastructureService();
        for (String name : names) {
            try {
                if (admin.removeConsolidationCriteria(name)) {
                    results.success(name, "Consolidation deleted.");
                } else {
                    results.warn(name, "Consolidation not found, cannot delete.");
                }
            }
            catch (Exception e) {
                log.error(String.format("Failed to remove consolidation: %s. %s", name, e.getMessage()), e);
                results.fail(name, e.toString());
            }
        }
        return results;
    }

}
