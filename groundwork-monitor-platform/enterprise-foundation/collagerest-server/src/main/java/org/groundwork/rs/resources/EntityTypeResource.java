package org.groundwork.rs.resources;

import com.groundwork.collage.CollageFactory;
import com.groundwork.collage.model.EntityType;
import com.groundwork.collage.query.QueryTranslation;
import com.groundwork.collage.query.QueryTranslator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.bs.metadata.MetadataService;
import org.groundwork.foundation.dao.SortCriteria;
import org.groundwork.rs.conversion.EntityTypeConverter;
import org.groundwork.rs.dto.DtoEntityType;
import org.groundwork.rs.dto.DtoEntityTypeList;
import org.groundwork.rs.dto.DtoSortType;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Path("/entitytypes")
public class EntityTypeResource extends AbstractResource {

    public static final String RESOURCE_PREFIX = "/entitytypes/";
    protected static Log log = LogFactory.getLog(EntityTypeResource.class);

    @GET
    @Path("/{entityTypeName}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoEntityType getEntityType(@PathParam("entityTypeName") String entityTypeName) {
        try {
            if (log.isDebugEnabled()) {
                log.debug(String.format("processing /GET on /entitytypes/%s", entityTypeName));
            }
            if (entityTypeName == null) {
                throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                        .entity("EntityType name is mandatory").build());
            }
            MetadataService metadataService =  CollageFactory.getInstance().getMetadataService();
            EntityType entityType = metadataService.getEntityTypeByName(entityTypeName);
            if (entityType == null) {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                        .entity(String.format("EntityType name [%s] was not found", entityTypeName)).build());
            }
            return EntityTypeConverter.convert(entityType);
        } catch (Exception e) {
            if (e instanceof WebApplicationException)
                throw e;
            log.error(ResourceMessages.UNEXPECTED_EXCEPTION + e, e);
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
                    String.format("An error occurred processing request for entity type [%s].", entityTypeName)).build());
        }
        finally {
        }
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoEntityTypeList getEntityTypes(@QueryParam("query") String query,
                                    @QueryParam("first") @DefaultValue("-1") int first, @QueryParam("count") @DefaultValue("-1") int count) {
        try {
            if (log.isDebugEnabled()) {
                log.debug(String.format("processing /GET on /entitytypes with  query: %s,  first: %d, count: %d",
                                       (query == null) ? "(none)" : query,  first, count));
            }
            MetadataService metadataService =  CollageFactory.getInstance().getMetadataService();
            QueryTranslator queryTranslator = CollageFactory.getInstance().getQueryTranslator();
            List<EntityType> entityTypes  = null;
            if (query != null) {
                QueryTranslation translation = queryTranslator.translate(query, QueryTranslator.ENTITY_TYPE_KEY);
                if (log.isDebugEnabled()) log.debug("hql = [" + translation.getHql() + "]");
                entityTypes = metadataService.queryMetadata(translation.getHql(), translation.getCountHql(), first, count).getResults();
            }
            else {
                SortCriteria sortCriteria = createSortCriteria("name", DtoSortType.Ascending);
                entityTypes = metadataService.getEntityTypes(null, sortCriteria, first, count).getResults();
            }

            if (entityTypes.isEmpty()) {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                        .entity(String.format("EntityTypes not found for query criteria [%s]",
                                (query != null) ? query : "(all)")).build());
            }
            List<DtoEntityType> dtoEntityTypes = new ArrayList<DtoEntityType>();
            for (EntityType entityType : entityTypes) {
                DtoEntityType dtoEntityType = EntityTypeConverter.convert(entityType);
                dtoEntityTypes.add(dtoEntityType);
            }
            return new DtoEntityTypeList(dtoEntityTypes);
        } catch (Exception e) {
            if (e instanceof WebApplicationException)
                throw e;
            log.error(ResourceMessages.UNEXPECTED_EXCEPTION + e, e);
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An error occurred processing request for entity types.").build());
        }
        finally {
        }
    }
}
