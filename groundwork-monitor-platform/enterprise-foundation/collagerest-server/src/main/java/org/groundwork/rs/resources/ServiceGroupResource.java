package org.groundwork.rs.resources;

import com.groundwork.collage.CollageAdminInfrastructure;
import com.groundwork.collage.CollageAdminInfrastructureUtils;
import com.groundwork.collage.CollageFactory;
import com.groundwork.collage.metrics.CollageTimer;
import com.groundwork.collage.model.ApplicationType;
import com.groundwork.collage.model.Category;
import com.groundwork.collage.model.CategoryEntity;
import com.groundwork.collage.model.EntityType;
import com.groundwork.collage.model.ServiceStatus;
import com.groundwork.collage.query.QueryTranslation;
import com.groundwork.collage.query.QueryTranslator;
import com.groundwork.collage.util.Autocomplete;
import com.groundwork.collage.util.AutocompleteName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.bs.category.CategoryService;
import org.groundwork.foundation.bs.hostidentity.HostIdentityService;
import org.groundwork.foundation.bs.metadata.MetadataService;
import org.groundwork.foundation.bs.status.StatusService;
import org.groundwork.foundation.dao.FilterCriteria;
import org.groundwork.foundation.dao.SortCriteria;
import org.groundwork.rs.conversion.ServiceGroupConverter;
import org.groundwork.rs.dto.*;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Path("/servicegroups")
public class ServiceGroupResource extends AbstractResource {

    public static final String RESOURCE_PREFIX = "/servicegroups/";
    protected static final String SERVICE_GROUP_UPDATE_MESSAGE = "Service Group Update";
    protected static final String SERVICE_GROUP_DELETE_MESSAGE = "Service Group Delete";

    private static final Pattern ORDER_BY_PATTERN = Pattern.compile("\\b\\s*order\\s+by\\s", Pattern.CASE_INSENSITIVE);

    protected static Log log = LogFactory.getLog(ServiceGroupResource.class);

    @GET
    @Path("/{serviceGroup_name}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoServiceGroup getServiceGroupByName(@PathParam("serviceGroup_name") String serviceGroupName,
                                                 @QueryParam("depth") @DefaultValue(DtoDepthType.DEFAULT) DtoDepthType.DtoDepthWrapper depthWrapper) {
        CollageTimer timer = startMetricsTimer();
        try {
            DtoDepthType depth = depthWrapper.getType();
            if (log.isDebugEnabled()) {
                log.debug(String.format("processing /GET on /servicegroups/%s with depth: %s", serviceGroupName, depth));
            }
            if (serviceGroupName == null) {
                throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("Service Group name is mandatory").build());
            }
            CategoryService categoryService = CollageFactory.getInstance().getCategoryService();
            Category category = categoryService.getCategoryByName(serviceGroupName, CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP);
            if (category == null) {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                        .entity(String.format("Service Group name [%s] was not found", serviceGroupName)).build());
            }
            List<ServiceStatus> serviceStatuses = null;
            if ((category.getCategoryEntities() != null) && !category.getCategoryEntities().isEmpty()) {
                StatusService statusService = CollageFactory.getInstance().getStatusService();
                serviceStatuses = statusService.getServicesByCategoryId(category.getCategoryId());
            }
            return ServiceGroupConverter.convert(category, serviceStatuses, depth);
        } catch (Exception e) {
            if (e instanceof WebApplicationException)
                throw e;
            log.error(ResourceMessages.UNEXPECTED_EXCEPTION + e, e);
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(String.format("An error occurred processing request for serviceGroup [%s].", serviceGroupName)).build());
        } finally {
            stopMetricsTimer(timer);
        }
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoServiceGroupList getServiceGroups(@QueryParam("appType") String appType,
                                                @QueryParam("agentId") String agentId,
                                                @QueryParam("query") String query,
                                                @QueryParam("first") @DefaultValue("-1") int first,
                                                @QueryParam("count") @DefaultValue("-1") int count,
                                                @QueryParam("depth") @DefaultValue(DtoDepthType.DEFAULT) DtoDepthType.DtoDepthWrapper depthWrapper) {
        CollageTimer timer = startMetricsTimer();
        DtoDepthType depth = depthWrapper.getType();
        if (log.isDebugEnabled()) {
            log.debug(String.format("processing /GET on /servicegroups with appType: %s, agentId: %s, query: %s, first: %d, count: %d, and depth: %s",
                    (appType == null) ? "" : appType, (agentId == null) ? "" : agentId, (query == null) ? "" : query, first, count, depth));
        }
        CategoryService categoryService = CollageFactory.getInstance().getCategoryService();
        QueryTranslator queryTranslator = CollageFactory.getInstance().getQueryTranslator();
        List<Category> categories = null;
        try {
            StringBuilder queryBuilder = null;
            if (!isEmpty(query) || !isEmpty(appType) || !isEmpty(agentId)) {
                String orderBy = "";
                if (!isEmpty(query)) {
                    Matcher orderByMatcher = ORDER_BY_PATTERN.matcher(query);
                    if (orderByMatcher.find()) {
                        int orderByIndex = orderByMatcher.start();
                        orderBy = query.substring(orderByIndex);
                        query = query.substring(0, orderByIndex);
                        if (!Character.isWhitespace(orderBy.charAt(0))) {
                            orderBy = " "+orderBy;
                        }
                    }
                }
                queryBuilder = new StringBuilder(!isEmpty(query) ? "("+query+") and " : "");
                queryBuilder.append("entityType.name = '");
                queryBuilder.append(CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP);
                queryBuilder.append("'");
                if (!isEmpty(appType)) {
                    queryBuilder.append(" and ");
                    queryBuilder.append("appType = '");
                    queryBuilder.append(appType);
                    queryBuilder.append("'");
                }
                if (!isEmpty(agentId)) {
                    queryBuilder.append(" and ");
                    queryBuilder.append("agentId = '");
                    queryBuilder.append(agentId);
                    queryBuilder.append("'");
                }
                queryBuilder.append(orderBy);
                QueryTranslation translation = queryTranslator.translate(queryBuilder.toString(), QueryTranslator.CATEGORY_KEY);
                if (log.isDebugEnabled()) log.debug("hql = [" + translation.getHql() + "]");
                categories = categoryService.queryCategories(translation.getHql(), translation.getCountHql(), first, count).getResults();
            } else {
                SortCriteria sortCriteria = createSortCriteria(Category.HP_NAME, DtoSortType.Ascending);
                FilterCriteria entityTypeFilter = FilterCriteria.eq(Category.HP_ENTITY_TYPE_NAME,
                        CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP);
                categories = categoryService.getCategories(entityTypeFilter, sortCriteria, first, count).getResults();
            }

            if (categories.isEmpty()) {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                        .entity(String.format("Service Groups not found for query criteria [%s]",
                                (queryBuilder != null) ? queryBuilder : "(all)")).build());
            }
            List<DtoServiceGroup> dtoServiceGroups = new ArrayList<DtoServiceGroup>();
            StatusService statusService = CollageFactory.getInstance().getStatusService();
            for (Category category : categories) {
                List<ServiceStatus> serviceStatuses = null;
                if ((category.getCategoryEntities() != null) && !category.getCategoryEntities().isEmpty()) {
                    serviceStatuses = statusService.getServicesByCategoryId(category.getCategoryId());
                }
                dtoServiceGroups.add(ServiceGroupConverter.convert(category, serviceStatuses, depth));
            }
            return new DtoServiceGroupList(dtoServiceGroups);
        } catch (Exception e) {
            if (e instanceof WebApplicationException)
                throw e;
            log.error(ResourceMessages.UNEXPECTED_EXCEPTION + e, e);
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An error occurred processing request for serviceGroups.").build());
        } finally {
            stopMetricsTimer(timer);
        }
    }

    @DELETE
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoOperationResults deleteServiceGroups(DtoServiceGroupUpdateList serviceGroupList) {
        CollageTimer timer = startMetricsTimer();
        if (log.isDebugEnabled()) {
            log.debug(String.format("processing /DELETE on /servicegroups with %d servicegroups",
                    (serviceGroupList == null) ? 0 : serviceGroupList.size()));
        }
        if (serviceGroupList == null) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Service Group list was not provided").build());
        }
        DtoOperationResults results = new DtoOperationResults("ServiceGroup", DtoOperationResults.DELETE);
        if (serviceGroupList.size() == 0) {
            return results;
        }
        CollageAdminInfrastructure admin = getAdminInfrastructureService();
        CategoryService categoryService = CollageFactory.getInstance().getCategoryService();
        for (DtoServiceGroupUpdate group : serviceGroupList.getServiceGroups()) {
            String serviceGroup = group.getName();
            try {
                Category check = categoryService.getCategoryByName(serviceGroup, CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP);
                if (check == null) {
                    results.warn(SERVICE_GROUP_DELETE_MESSAGE,
                            String.format("Service Group %s not found, cannot delete.", serviceGroup));
                } else {
                    // remove service group
                    if (CollageAdminInfrastructureUtils.removeServiceGroup(group.getName(), admin)) {
                        results.success(serviceGroup, String.format("Service Group %s deleted.", serviceGroup));
                    } else {
                        results.warn(serviceGroup, "Service group not found, cannot delete.");
                    }
                }
            } catch (Exception e) {
                String message = String.format("Failed to remove category: %s. Error: %s", serviceGroup, e.getMessage());
                log.error(message, e);
                results.fail(SERVICE_GROUP_DELETE_MESSAGE, message);
            }
        }
        stopMetricsTimer(timer);
        return results;
    }

    /**
     * If the ServiceGroup doesn't exist it would be created and all Services would be added.
     * If the ServiceGroup already exist the existing service references would be removed and the ListOfServices added.
     * This call would refresh the services for that service group
     * <p/>
     *
     * @param serviceGroupList contains one or more service groups, and list of services to be added
     * @return a operation results set with detailed information regarding results of operations
     */
    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoOperationResults createOrUpdateServiceGroup(DtoServiceGroupUpdateList serviceGroupList) {
        CollageTimer timer = startMetricsTimer();
        if (log.isDebugEnabled()) {
            log.debug(String.format("processing /POST on /serviceGroups with %d serviceGroups",
                    (serviceGroupList == null) ? 0 : serviceGroupList.size()));
        }
        if (serviceGroupList == null) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Service Group was not provided").build());
        }
        DtoOperationResults results = new DtoOperationResults("ServiceGroup", DtoOperationResults.UPDATE);
        if (serviceGroupList.size() == 0) {
            return results;
        }
        for (DtoServiceGroupUpdate groupUpdate : serviceGroupList.getServiceGroups()) {
            if (groupUpdate.getName() == null) {
                results.fail(SERVICE_GROUP_UPDATE_MESSAGE, "No Service Group Name provided");
                continue;
            }
            try {
                if (saveServiceGroup(groupUpdate, results)) {
                    results.success(groupUpdate.getName(), buildResourceLocator(uriInfo, RESOURCE_PREFIX, groupUpdate.getName()));
                }
            } catch (Exception e) {
                results.fail(SERVICE_GROUP_UPDATE_MESSAGE,
                        String.format("Failed to update Service Group %s, error: %s",
                                groupUpdate.getName(), e.getMessage()));
            }
        }
        stopMetricsTimer(timer);
        return results;
    }

    /**
     * Add one or more services (members) to a service group
     * The list of services would be merged with the existing services for the service group.
     * If the service group doesn't exist and error would occur.
     *
     * @param serviceGroup contains the name of the service group, and list of services to be added
     * @return a operation results set with detailed information regarding results of operations
     */
    @PUT
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("/addmembers")
    public DtoOperationResults addMembersToServiceGroup(DtoServiceGroupMemberUpdate serviceGroup) {
        CollageTimer timer = startMetricsTimer();
        if (log.isDebugEnabled()) {
            log.debug(String.format("processing /PUT add members on /serviceGroups for group %s",
                    (serviceGroup == null) ? "(none)" : serviceGroup.getName()));
        }
        if (serviceGroup == null) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Service Group was not provided").build());
        }
        DtoOperationResults results = new DtoOperationResults("ServiceGroup", DtoOperationResults.UPDATE);
        if (serviceGroup.getName() == null) {
            results.fail(SERVICE_GROUP_UPDATE_MESSAGE, "No Service Group Name provided");
            return results;
        }
        try {
            if (addMembers(serviceGroup, results)) {
                results.success(serviceGroup.getName(), buildResourceLocator(uriInfo, RESOURCE_PREFIX, serviceGroup.getName()));
            }
        } catch (Exception e) {
            results.fail(SERVICE_GROUP_UPDATE_MESSAGE,
                    String.format("Failed to add members to Service Group %s, error: %s",
                            serviceGroup.getName(), e.getMessage()));
        }
        stopMetricsTimer(timer);
        return results;
    }

    /**
     * Delete one or more services (members) from a service group
     * The List of services would be removed from the service group.
     * If the service group or any of the services doesn't exist and error will be returned.
     *
     * @param serviceGroup contains the name of the service group, and list of services to be added
     * @return a operation results set with detailed information regarding results of operations
     */
    @PUT
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("/deletemembers")
    public DtoOperationResults deleteMembersFromServiceGroup(DtoServiceGroupMemberUpdate serviceGroup) {
        CollageTimer timer = startMetricsTimer();
        if (log.isDebugEnabled()) {
            log.debug(String.format("processing /PUT delete members on /serviceGroups for group %s",
                    (serviceGroup == null) ? "(none)" : serviceGroup.getName()));
        }
        if (serviceGroup == null) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Service Group was not provided").build());
        }
        DtoOperationResults results = new DtoOperationResults("ServiceGroup", DtoOperationResults.UPDATE);
        if (serviceGroup.getName() == null) {
            results.fail(SERVICE_GROUP_UPDATE_MESSAGE, "No Service Group Name provided");
            return results;
        }
        try {
            if (deleteMembers(serviceGroup, results)) {
                results.success(serviceGroup.getName(), buildResourceLocator(uriInfo, RESOURCE_PREFIX, serviceGroup.getName()));
            }
        } catch (Exception e) {
            results.fail(SERVICE_GROUP_UPDATE_MESSAGE,
                    String.format("Failed to delete members to Service Group %s, error: %s",
                            serviceGroup.getName(), e.getMessage()));
        }
        stopMetricsTimer(timer);
        return results;
    }

    private boolean saveServiceGroup(DtoServiceGroupUpdate dtoServiceGroup, DtoOperationResults results) {
        CategoryService categoryService = CollageFactory.getInstance().getCategoryService();
        HostIdentityService hostIdentityService = CollageFactory.getInstance().getHostIdentityService();
        CollageAdminInfrastructure admin = getAdminInfrastructureService();
        MetadataService metadataService = CollageFactory.getInstance().getMetadataService();
        EntityType entityType = metadataService.getEntityTypeByName(CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP);
        if (entityType == null) {
            results.fail(SERVICE_GROUP_UPDATE_MESSAGE, "Service Group Standard Entity Type Not Found");
            return false;
        }
        EntityType serviceEntityType = metadataService.getEntityTypeByName(CategoryService.ENTITY_TYPE_CODE_SERVICESTATUS);
        if (serviceEntityType == null) {
            results.fail(SERVICE_GROUP_UPDATE_MESSAGE, "Service Status Standard Entity Type Not Found");
            return false;
        }
        Category category = categoryService.getCategoryByName(dtoServiceGroup.getName(), CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP);
        if (category != null) {
            // update
            if (dtoServiceGroup.getDescription() != null)
                category.setDescription(dtoServiceGroup.getDescription().isEmpty() ? null : dtoServiceGroup.getDescription());
            if (dtoServiceGroup.getAppType() != null) {
                // changing app type
                ApplicationType applicationType = metadataService.getApplicationTypeByName(dtoServiceGroup.getAppType());
                if (applicationType != null) {
                    category.setApplicationType(applicationType);
                } else {
                    results.fail(SERVICE_GROUP_UPDATE_MESSAGE,
                            String.format("Failed to find app type %s for service group %s",
                                    dtoServiceGroup.getAppType(), dtoServiceGroup.getName()));
                    return false;
                }
            }
            if (dtoServiceGroup.getAgentId() != null)
                category.setAgentId(dtoServiceGroup.getAgentId());
            // reset entities (services)
            Collection<CategoryEntity> entities = category.getCategoryEntities();
            if (entities != null) {
                List<CategoryEntity> copy = new ArrayList<>();
                copy.addAll(entities);
                for (CategoryEntity entity : copy) {
                    category.getCategoryEntities().remove(entity);
                }
                admin.saveCategory(category);
                for (CategoryEntity entity : copy) {
                    categoryService.deleteCategoryEntity(entity);
                }
                category = categoryService.getCategoryByName(dtoServiceGroup.getName(), CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP);
            }
        } else {
            // insert
            ApplicationType applicationType = null;
            if (dtoServiceGroup.getAppType() != null) {
                applicationType = metadataService.getApplicationTypeByName(dtoServiceGroup.getAppType());
                if (applicationType == null) {
                    results.fail(SERVICE_GROUP_UPDATE_MESSAGE,
                            String.format("Failed to find app type %s for service group %s",
                                    dtoServiceGroup.getAppType(), dtoServiceGroup.getName()));
                    return false;
                }
            }
            category = categoryService.createCategory(dtoServiceGroup.getName(), dtoServiceGroup.getDescription(),
                    entityType, applicationType, dtoServiceGroup.getAgentId());
        }
        // services
        if (dtoServiceGroup.getServices() != null) {
            for (DtoServiceKey service : dtoServiceGroup.getServices()) {
                ServiceStatus serviceStatus = hostIdentityService.getServiceByDescriptionAndHostIdOrHostName(service.getService(), service.getHost());
                if (serviceStatus == null) {
                    results.fail(SERVICE_GROUP_UPDATE_MESSAGE,
                            String.format("Failed to find service:host %s:%s for service group %s",
                                    service.getService(), service.getHost(), dtoServiceGroup.getName()));
                    return false;
                }
                CategoryEntity entity = categoryService.createCategoryEntity();
                entity.setCategory(category);
                entity.setEntityType(serviceEntityType);
                entity.setObjectID(serviceStatus.getServiceStatusId());
                category.getCategoryEntities().add(entity);
            }
        }
        admin.saveCategory(category);
        return true;
    }

    private boolean addMembers(DtoServiceGroupMemberUpdate dtoServiceGroup, DtoOperationResults results) {
        CategoryService categoryService = CollageFactory.getInstance().getCategoryService();
        HostIdentityService hostIdentityService = CollageFactory.getInstance().getHostIdentityService();
        CollageAdminInfrastructure admin = getAdminInfrastructureService();
        MetadataService metadataService = CollageFactory.getInstance().getMetadataService();
        EntityType serviceEntityType = metadataService.getEntityTypeByName(CategoryService.ENTITY_TYPE_CODE_SERVICESTATUS);
        if (serviceEntityType == null) {
            results.fail(SERVICE_GROUP_UPDATE_MESSAGE, "Service Status Standard Entity Type Not Found");
            return false;
        }
        Category category = categoryService.getCategoryByName(dtoServiceGroup.getName(), CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP);
        if (category == null) {
            results.fail(SERVICE_GROUP_UPDATE_MESSAGE,
                    String.format("Service Group %s was not found, cannot update", dtoServiceGroup.getName()));
            return false;
        }
        if (dtoServiceGroup.getServices() == null || dtoServiceGroup.getServices().size() == 0) {
            results.fail(SERVICE_GROUP_UPDATE_MESSAGE,
                    String.format("No Service members provided for Service Group %s, cannot update", dtoServiceGroup.getName()));
            return false;

        }
        for (DtoServiceKey service : dtoServiceGroup.getServices()) {
            ServiceStatus serviceStatus = hostIdentityService.getServiceByDescriptionAndHostIdOrHostName(service.getService(), service.getHost());
            if (serviceStatus == null) {
                results.fail(SERVICE_GROUP_UPDATE_MESSAGE,
                        String.format("Failed to find service:host %s:%s for service group %s",
                                service.getService(), service.getHost(), dtoServiceGroup.getName()));
                return false;
            }
            CategoryEntity entity = categoryService.createCategoryEntity();
            entity.setCategory(category);
            entity.setEntityType(serviceEntityType);
            entity.setObjectID(serviceStatus.getServiceStatusId());
            if (findEntity(category, serviceStatus.getServiceStatusId(), CategoryService.ENTITY_TYPE_CODE_SERVICESTATUS) == null) {
                category.getCategoryEntities().add(entity);
            }
        }
        admin.saveCategory(category);
        return true;
    }

    private boolean deleteMembers(DtoServiceGroupMemberUpdate dtoServiceGroup, DtoOperationResults results) {
        CategoryService categoryService = CollageFactory.getInstance().getCategoryService();
        HostIdentityService hostIdentityService = CollageFactory.getInstance().getHostIdentityService();
        CollageAdminInfrastructure admin = getAdminInfrastructureService();
        Category category = categoryService.getCategoryByName(dtoServiceGroup.getName(), CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP);
        if (category == null) {
            results.fail(SERVICE_GROUP_UPDATE_MESSAGE,
                    String.format("Service Group %s was not found, cannot update", dtoServiceGroup.getName()));
            return false;
        }
        if (dtoServiceGroup.getServices() == null || dtoServiceGroup.getServices().size() == 0) {
            results.fail(SERVICE_GROUP_UPDATE_MESSAGE,
                    String.format("No Service members provided for Service Group %s, cannot delete", dtoServiceGroup.getName()));
            return false;

        }
        for (DtoServiceKey service : dtoServiceGroup.getServices()) {
            ServiceStatus serviceStatus = hostIdentityService.getServiceByDescriptionAndHostIdOrHostName(service.getService(), service.getHost());
            if (serviceStatus == null) {
                results.fail(SERVICE_GROUP_UPDATE_MESSAGE,
                        String.format("Failed to find service:host %s:%s for service group %s",
                                service.getService(), service.getHost(), dtoServiceGroup.getName()));
                return false;
            }
            CategoryEntity entity = findEntity(category, serviceStatus.getServiceStatusId(), CategoryService.ENTITY_TYPE_CODE_SERVICESTATUS);
            if (entity == null) {
                results.warn(SERVICE_GROUP_UPDATE_MESSAGE,
                        String.format("Failed to find service:host %s:%s for service group %s to delete",
                                service.getService(), service.getHost(), dtoServiceGroup.getName()));
                return false;

            }
            category.getCategoryEntities().remove(entity);
            categoryService.deleteCategoryEntity(entity);
        }
        admin.saveCategory(category);
        return true;
    }

    /**
     * Find category entity in category.
     *
     * @param category category
     * @param id category entity object id
     * @param entityTypeName category entity type name
     * @return category entity
     */
    private CategoryEntity findEntity(Category category, Integer id, String entityTypeName) {
        if (category.getCategoryEntities() != null) {
            for (CategoryEntity entity : category.getCategoryEntities()) {
                if (entity.getObjectID() != null && entity.getObjectID().equals(id) &&
                        (entity.getEntityType() != null) && (entity.getEntityType().getName().equals(entityTypeName))) {
                    return entity;
                }
            }
        }
        return null;
    }

    @GET
    @Path("/autocomplete/{prefix}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoNamesList autocomplete(@PathParam("prefix") String prefix, @QueryParam("limit") @DefaultValue("10") int limit) {
        CollageTimer timer = startMetricsTimer();
        if (log.isDebugEnabled()) {
            log.debug(String.format("processing /GET on /autocomplete/%s with limit %d", prefix, limit));
        }
        try {
            Autocomplete serviceGroupAutocompleteService = CollageFactory.getInstance().getServiceGroupAutocompleteService();
            List<AutocompleteName> names = serviceGroupAutocompleteService.autocomplete(prefix, limit);
            if (names.isEmpty()) {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity(String.format("Service group names not found for prefix [%s]", prefix)).build());
            }
            List<DtoName> dtoNames = new ArrayList<DtoName>();
            for (AutocompleteName name : names) {
                dtoNames.add(new DtoName(name.getName()));
            }
            return new DtoNamesList(dtoNames);
        } catch (WebApplicationException wae) {
            throw wae;
        } catch (Exception e) {
            log.error(ResourceMessages.UNEXPECTED_EXCEPTION + e, e);
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(String.format("An error occurred processing request for autocomplete [%s].", prefix)).build());
        } finally {
            stopMetricsTimer(timer);
        }

    }
}

