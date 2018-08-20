package org.groundwork.rs.resources;

import com.groundwork.collage.CollageFactory;
import com.groundwork.collage.model.ApplicationType;
import com.groundwork.collage.model.EntityType;
import com.groundwork.collage.model.PropertyType;
import com.groundwork.collage.query.QueryTranslation;
import com.groundwork.collage.query.QueryTranslator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.bs.metadata.MetadataService;
import org.groundwork.foundation.dao.SortCriteria;
import org.groundwork.rs.conversion.ApplicationTypeConverter;
import org.groundwork.rs.dto.DtoApplicationType;
import org.groundwork.rs.dto.DtoApplicationTypeList;
import org.groundwork.rs.dto.DtoDepthType;
import org.groundwork.rs.dto.DtoEntityProperty;
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

@Path("/applicationtypes")
public class ApplicationTypeResource extends AbstractResource {

    public static final String RESOURCE_PREFIX = "/applicationtypes/";
    protected static Log log = LogFactory.getLog(ApplicationTypeResource.class);

    @GET
    @Path("/{applicationTypeName}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoApplicationType getApplicationType(@PathParam("applicationTypeName") String applicationTypeName,
            @QueryParam("depth") @DefaultValue(DtoDepthType.DEFAULT) DtoDepthType.DtoDepthWrapper depthWrapper)
    {
        try {
            if (log.isDebugEnabled()) {
                log.debug(String.format("processing /GET on /applicationtypes/%s", applicationTypeName));
            }
            if (applicationTypeName == null) {
                throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                        .entity("ApplicationType name is mandatory").build());
            }
            MetadataService metadataService =  CollageFactory.getInstance().getMetadataService();
            ApplicationType applicationType = metadataService.getApplicationTypeByName(applicationTypeName);
            if (applicationType == null) {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                        .entity(String.format("ApplicationType name [%s] was not found", applicationTypeName)).build());
            }
            return ApplicationTypeConverter.convert(applicationType, depthWrapper.getType());
        } catch (Exception e) {
            if (e instanceof WebApplicationException)
                throw e;
            log.error(ResourceMessages.UNEXPECTED_EXCEPTION + e, e);
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
                    String.format("An error occurred processing request for application property [%s].", applicationTypeName)).build());
        }
        finally {
        }
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoApplicationTypeList getApplicationTypes(@QueryParam("query") String query,
                         @QueryParam("depth") @DefaultValue(DtoDepthType.DEFAULT) DtoDepthType.DtoDepthWrapper depthWrapper,
                         @QueryParam("first") @DefaultValue("-1") int first, @QueryParam("count") @DefaultValue("-1") int count) {
        try {
            if (log.isDebugEnabled()) {
                log.debug(String.format("processing /GET on /applicationTypes with  query: %s,  first: %d, count: %d",
                                       (query == null) ? "(none)" : query,  first, count));
            }
            MetadataService metadataService =  CollageFactory.getInstance().getMetadataService();
            QueryTranslator queryTranslator = CollageFactory.getInstance().getQueryTranslator();
            List<ApplicationType> applicationTypes  = null;
            if (query != null) {
                QueryTranslation translation = queryTranslator.translate(query, QueryTranslator.APPLICATION_TYPE_KEY);
                if (log.isDebugEnabled()) log.debug("hql = [" + translation.getHql() + "]");
                applicationTypes = metadataService.queryMetadata(translation.getHql(), translation.getCountHql(), first, count).getResults();
            }
            else {
                SortCriteria sortCriteria = createSortCriteria("name", DtoSortType.Ascending);
                applicationTypes = metadataService.getApplicationTypes(null, sortCriteria, first, count).getResults();
            }

            if (applicationTypes.isEmpty()) {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                        .entity(String.format("ApplicationTypes not found for query criteria [%s]",
                                (query != null) ? query : "(all)")).build());
            }
            List<DtoApplicationType> dtoApplicationTypes = new ArrayList<DtoApplicationType>();
            for (ApplicationType applicationType : applicationTypes) {
                DtoApplicationType dtoApplicationType = ApplicationTypeConverter.convert(applicationType, depthWrapper.getType());
                dtoApplicationTypes.add(dtoApplicationType);
            }
            return new DtoApplicationTypeList(dtoApplicationTypes);
        } catch (Exception e) {
            if (e instanceof WebApplicationException)
                throw e;
            log.error(ResourceMessages.UNEXPECTED_EXCEPTION + e, e);
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An error occurred processing request for applicationTypes.").build());
        }
        finally {
        }
    }

    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoOperationResults createApplicationTypes(DtoApplicationTypeList dtoApplicationTypes) {
        if (log.isDebugEnabled()) {
            log.debug(String.format("processing /POST on /applicationtypes with %d application types",
                    (dtoApplicationTypes == null) ? 0 : dtoApplicationTypes.size()));
        }
        if (dtoApplicationTypes == null) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Application Type list was not provided").build());
        }
        DtoOperationResults results = new DtoOperationResults("ApplicationType", DtoOperationResults.UPDATE);
        if (dtoApplicationTypes.size() == 0) {
            return results;
        }
        MetadataService metadataService =  CollageFactory.getInstance().getMetadataService();
        for (DtoApplicationType dto : dtoApplicationTypes.getApplicationTypes()) {
            if (dto.getName() == null) {
                results.fail("applicationTypeName Unknown", "No ApplicationType name provided");
                continue;
            }
            try {
                ApplicationType applicationType = metadataService.getApplicationTypeByName(dto.getName());
                if (applicationType == null) {
                    applicationType = metadataService.createApplicationType();
                }
                applicationType = convertApplicationTypeFromDto(applicationType, dto);
                metadataService.saveApplicationType(applicationType);
                results.success(dto.getName(), buildResourceLocator(uriInfo, RESOURCE_PREFIX, dto.getName()));
            }
            catch (Exception e) {
                log.error(String.format("Failed to save applicationType: %s. %s", dto.getName(), e.getMessage()), e);
                results.fail(dto.getName(), e.getMessage());
            }
        }
        return results;
    }

    @DELETE
    @Path("/{applicationTypeNames}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoOperationResults removeApplicationType(@PathParam("applicationTypeNames") String applicationTypeNames) {
        if (log.isDebugEnabled()) {
            log.debug(String.format("processing /DELETE on /applicationtypes for %s", applicationTypeNames));
        }
        List<String> names = null;
        try {
            names = parseNames(applicationTypeNames);
        }
        catch (Exception e) {
            String message = String.format("error converting applicationType names %s ", applicationTypeNames);
            log.debug(message);
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity(message).build());
        }
        DtoOperationResults results = new DtoOperationResults("ApplicationType", DtoOperationResults.DELETE);
        MetadataService metadataService =  CollageFactory.getInstance().getMetadataService();
        for (String name : names) {
            try {
                if (metadataService.deleteApplicationTypeByName(name)) {
                    results.success(name, "Application Type deleted.");
                } else {
                    results.warn(name, "Application Type not found, cannot delete.");
                }
            }
            catch (Exception e) {
                log.error(String.format("Failed to remove applicationType: %s. %s", name, e.getMessage()), e);
                results.fail(name, e.toString());
            }
        }
        return results;
    }

    public ApplicationType convertApplicationTypeFromDto(ApplicationType applicationType, DtoApplicationType dto) {

        if (dto.getProperties() != null) {
            for (String key : dto.getProperties().keySet()) {
                applicationType.setProperty(key, dto.getProperties().get(key));
            }
        }
        applicationType.setName(dto.getName());
        if (dto.getDisplayName() != null)
            applicationType.setDisplayName(dto.getDisplayName().isEmpty() ? null : dto.getDisplayName());
        if (dto.getDescription() != null)
            applicationType.setDescription(dto.getDescription().isEmpty() ? null : dto.getDescription());
        if (dto.getStateTransitionCriteria() != null)
            applicationType.setStateTransitionCriteria(dto.getStateTransitionCriteria());
        if (dto.getEntityProperties() != null) {
            for (DtoEntityProperty entityProperty : dto.getEntityProperties()) {
                EntityType entityType = lookupEntityType(entityProperty.getEntityType());
                PropertyType propertyType = lookupPropertyType(entityProperty.getPropertyType());
                entityType.setName(entityProperty.getEntityType());
                if (entityType != null && propertyType != null) {
                    applicationType.assignPropertyType(entityType, propertyType, entityProperty.getSortOrder());
                }
            }
        }
        return applicationType;
    }

    protected EntityType lookupEntityType(String entityTypeName) {
        MetadataService metadataService =  CollageFactory.getInstance().getMetadataService();
        return metadataService.getEntityTypeByName(entityTypeName);
    }

    protected PropertyType lookupPropertyType(String propertyTypeName) {
        MetadataService metadataService =  CollageFactory.getInstance().getMetadataService();
        return metadataService.getPropertyTypeByName(propertyTypeName);
    }

}
