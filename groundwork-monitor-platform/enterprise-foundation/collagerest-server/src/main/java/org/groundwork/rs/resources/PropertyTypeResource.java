package org.groundwork.rs.resources;

import com.groundwork.collage.CollageFactory;
import com.groundwork.collage.model.PropertyType;
import com.groundwork.collage.query.QueryTranslation;
import com.groundwork.collage.query.QueryTranslator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.bs.metadata.MetadataService;
import org.groundwork.foundation.dao.SortCriteria;
import org.groundwork.rs.conversion.PropertyTypeConverter;
import org.groundwork.rs.dto.DtoOperationResults;
import org.groundwork.rs.dto.DtoPropertyType;
import org.groundwork.rs.dto.DtoPropertyTypeList;
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

@Path("/propertytypes")
public class PropertyTypeResource extends AbstractResource {

    public static final String RESOURCE_PREFIX = "/propertytypes/";
    protected static Log log = LogFactory.getLog(PropertyTypeResource.class);

    @GET
    @Path("/{propertyTypeName}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoPropertyType getPropertyType(@PathParam("propertyTypeName") String propertyTypeName) {
        try {
            if (log.isDebugEnabled()) {
                log.debug(String.format("processing /GET on /propertytypes/%s", propertyTypeName));
            }
            if (propertyTypeName == null) {
                throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                        .entity("PropertyType name is mandatory").build());
            }
            MetadataService metadataService =  CollageFactory.getInstance().getMetadataService();
            PropertyType propertyType = metadataService.getPropertyTypeByName(propertyTypeName);
            if (propertyType == null) {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                        .entity(String.format("PropertyType name [%s] was not found", propertyTypeName)).build());
            }
            return PropertyTypeConverter.convert(propertyType);
        } catch (Exception e) {
            if (e instanceof WebApplicationException)
                throw e;
            log.error(ResourceMessages.UNEXPECTED_EXCEPTION + e, e);
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
                    String.format("An error occurred processing request for property type [%s].", propertyTypeName)).build());
        }
        finally {
        }
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoPropertyTypeList getPropertyTypes(@QueryParam("query") String query,
                                    @QueryParam("first") @DefaultValue("-1") int first, @QueryParam("count") @DefaultValue("-1") int count) {
        try {
            if (log.isDebugEnabled()) {
                log.debug(String.format("processing /GET on /propertytypes with  query: %s,  first: %d, count: %d",
                                       (query == null) ? "(none)" : query,  first, count));
            }
            MetadataService metadataService =  CollageFactory.getInstance().getMetadataService();
            QueryTranslator queryTranslator = CollageFactory.getInstance().getQueryTranslator();
            List<PropertyType> propertyTypes  = null;
            if (query != null) {
                QueryTranslation translation = queryTranslator.translate(query, QueryTranslator.PROPERTY_TYPE_KEY);
                if (log.isDebugEnabled()) log.debug("hql = [" + translation.getHql() + "]");
                propertyTypes = metadataService.queryMetadata(translation.getHql(), translation.getCountHql(), first, count).getResults();
            }
            else {
                SortCriteria sortCriteria = createSortCriteria("name", DtoSortType.Ascending);
                propertyTypes = metadataService.getPropertyTypes(null, sortCriteria, first, count).getResults();
            }

            if (propertyTypes.isEmpty()) {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                        .entity(String.format("PropertyTypes not found for query criteria [%s]",
                                (query != null) ? query : "(all)")).build());
            }
            List<DtoPropertyType> dtoPropertyTypes = new ArrayList<DtoPropertyType>();
            for (PropertyType propertyType : propertyTypes) {
                DtoPropertyType dtoPropertyType = PropertyTypeConverter.convert(propertyType);
                dtoPropertyTypes.add(dtoPropertyType);
            }
            return new DtoPropertyTypeList(dtoPropertyTypes);
        } catch (Exception e) {
            if (e instanceof WebApplicationException)
                throw e;
            log.error(ResourceMessages.UNEXPECTED_EXCEPTION + e, e);
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An error occurred processing request for property types.").build());
        }
        finally {
        }
    }

    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoOperationResults createPropertyTypes(DtoPropertyTypeList dtoPropertyTypes) {
        if (log.isDebugEnabled()) {
            log.debug(String.format("processing /POST on /propertytypes with %d property types",
                    (dtoPropertyTypes == null) ? 0 : dtoPropertyTypes.size()));
        }
        if (dtoPropertyTypes == null) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Property Type list was not provided").build());
        }
        DtoOperationResults results = new DtoOperationResults("PropertyType", DtoOperationResults.UPDATE);
        if (dtoPropertyTypes.size() == 0) {
            return results;
        }
        MetadataService metadataService =  CollageFactory.getInstance().getMetadataService();
        for (DtoPropertyType dto : dtoPropertyTypes.getPropertyTypes()) {
            if (dto.getName() == null) {
                results.fail("propertyTypeName Unknown", "No PropertyType name provided");
                continue;
            }
            if (dto.getDataType() == null) {
                results.fail("propertyDataType Unknown", "No PropertyType dataType provided");
                continue;
            }
            try {
                metadataService.savePropertyType(dto.getName(), dto.getDescription(), dto.getDataType().name());
                results.success(dto.getName(), buildResourceLocator(uriInfo, RESOURCE_PREFIX, dto.getName()));
            }
            catch (Exception e) {
                log.error(String.format("Failed to save propertyType: %s. %s", dto.getName(), e.getMessage()), e);
                results.fail(dto.getName(), e.getMessage());
            }
        }
        return results;
    }

    @DELETE
    @Path("/{propertyTypeNames}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoOperationResults removeHostGroup(@PathParam("propertyTypeNames") String propertyTypeNames) {
        if (log.isDebugEnabled()) {
            log.debug(String.format("processing /DELETE on /propertytypes for %s", propertyTypeNames));
        }
        List<String> names = null;
        try {
            names = parseNames(propertyTypeNames);
        }
        catch (Exception e) {
            String message = String.format("error converting propertyType names %s ", propertyTypeNames);
            log.debug(message);
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity(message).build());
        }
        DtoOperationResults results = new DtoOperationResults("HostGroup", DtoOperationResults.DELETE);
        MetadataService metadataService =  CollageFactory.getInstance().getMetadataService();
        for (String name : names) {
            try {
                if (metadataService.deletePropertyTypeByName(name)) {
                    results.success(name, "Property Type deleted.");
                } else {
                    results.warn(name, "Property Type not found, cannot delete.");
                }
            }
            catch (Exception e) {
                log.error(String.format("Failed to remove propertyType: %s. %s", name, e.getMessage()), e);
                results.fail(name, e.toString());
            }
        }
        return results;
    }

}
