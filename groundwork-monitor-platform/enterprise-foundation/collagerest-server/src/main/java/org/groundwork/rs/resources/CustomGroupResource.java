/*
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2015  GroundWork Open Source Solutions info@groundworkopensource.com

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
import com.groundwork.collage.model.*;
import com.groundwork.collage.query.QueryTranslation;
import com.groundwork.collage.query.QueryTranslator;
import com.groundwork.collage.util.Autocomplete;
import com.groundwork.collage.util.AutocompleteName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.bs.category.CategoryService;
import org.groundwork.foundation.bs.hostgroup.HostGroupService;
import org.groundwork.foundation.bs.metadata.MetadataService;
import org.groundwork.foundation.dao.FilterCriteria;
import org.groundwork.foundation.dao.SortCriteria;
import org.groundwork.rs.conversion.CustomGroupConverter;
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

@Path("/customgroups")
public class CustomGroupResource extends AbstractResource {

    public static final String RESOURCE_PREFIX = "/customgroups/";
    protected static final String CUSTOM_GROUP_UPDATE_MESSAGE = "Custom Group Update";
    protected static final String CUSTOM_GROUP_DELETE_MESSAGE = "Custom Group Delete";

    private static final Pattern ORDER_BY_PATTERN = Pattern.compile("\\b\\s*order\\s+by\\s", Pattern.CASE_INSENSITIVE);

    protected static Log log = LogFactory.getLog(CustomGroupResource.class);

    /**
     * Get custom group by name.
     *
     * @param customGroupName custom group name
     * @return custom group
     */
    @GET
    @Path("/{customGroup_name}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoCustomGroup getCustomGroupByName(@PathParam("customGroup_name") String customGroupName,
                                               @QueryParam("depth") @DefaultValue(DtoDepthType.DEFAULT) DtoDepthType.DtoDepthWrapper depthWrapper) {
        try {
            DtoDepthType depth = depthWrapper.getType();
            if (log.isDebugEnabled()) {
                log.debug(String.format("processing /GET on /customgroups/%s with depth: %s", customGroupName, depth));
            }
            if (customGroupName == null) {
                throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("Custom Group name is mandatory").build());
            }
            CategoryService categoryService = CollageFactory.getInstance().getCategoryService();
            Category category = categoryService.getCategoryByName(customGroupName, CategoryService.ENTITY_TYPE_CODE_CUSTOMGROUP);
            if (category == null) {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                        .entity(String.format("Custom Group name [%s] was not found", customGroupName)).build());
            }
            return CustomGroupConverter.convert(category, depth);
        } catch (Exception e) {
            if (e instanceof WebApplicationException)
                throw e;
            log.error(ResourceMessages.UNEXPECTED_EXCEPTION + e, e);
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(String.format("An error occurred processing request for customGroup [%s].", customGroupName)).build());
        }
    }

    /**
     * Query for custom groups.
     *
     * @param appType application type name filter
     * @param agentId agent id filter
     * @param query query string
     * @param first index of first result
     * @param count limit of results to return
     * @return list of custom groups
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoCustomGroupList getCustomGroups(@QueryParam("appType") String appType,
                                              @QueryParam("agentId") String agentId,
                                              @QueryParam("query") String query,
                                              @QueryParam("first") @DefaultValue("-1") int first,
                                              @QueryParam("count") @DefaultValue("-1") int count,
                                              @QueryParam("depth") @DefaultValue(DtoDepthType.DEFAULT) DtoDepthType.DtoDepthWrapper depthWrapper) {
        DtoDepthType depth = depthWrapper.getType();
        if (log.isDebugEnabled()) {
            log.debug(String.format("processing /GET on /customgroups with appType: %s, agentId: %s, query: %s, first: %d, count: %d, and depth: %s",
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
                queryBuilder.append(CategoryService.ENTITY_TYPE_CODE_CUSTOMGROUP);
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
                        CategoryService.ENTITY_TYPE_CODE_CUSTOMGROUP);
                categories = categoryService.getCategories(entityTypeFilter, sortCriteria, first, count).getResults();
            }

            if (categories.isEmpty()) {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                        .entity(String.format("Custom Groups not found for query criteria [%s]",
                                (queryBuilder != null) ? queryBuilder : "(all)")).build());
            }
            List<DtoCustomGroup> dtoCustomGroups = new ArrayList<>();
            for (Category category : categories) {
                dtoCustomGroups.add(CustomGroupConverter.convert(category, depth));
            }
            return new DtoCustomGroupList(dtoCustomGroups);
        } catch (Exception e) {
            if (e instanceof WebApplicationException)
                throw e;
            log.error(ResourceMessages.UNEXPECTED_EXCEPTION + e, e);
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An error occurred processing request for customGroups.").build());
        }
    }

    /**
     * Delete custom groups.
     *
     * @param customGroupList custom groups to delete
     * @return operation results
     */
    @DELETE
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoOperationResults deleteCustomGroups(DtoCustomGroupUpdateList customGroupList) {
        if (log.isDebugEnabled()) {
            log.debug(String.format("processing /DELETE on /customgroups with %d customgroups",
                    (customGroupList == null) ? 0 : customGroupList.size()));
        }
        if (customGroupList == null) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Custom Group list was not provided").build());
        }
        DtoOperationResults results = new DtoOperationResults("CustomGroup", DtoOperationResults.DELETE);
        if (customGroupList.size() == 0) {
            return results;
        }
        CollageAdminInfrastructure admin = getAdminInfrastructureService();
        CategoryService categoryService = CollageFactory.getInstance().getCategoryService();
        for (DtoCustomGroupUpdate group : customGroupList.getCustomGroups()) {
            String customGroup = group.getName();
            try {
                Category check = categoryService.getCategoryByName(customGroup, CategoryService.ENTITY_TYPE_CODE_CUSTOMGROUP);
                if (check == null) {
                    results.warn(CUSTOM_GROUP_DELETE_MESSAGE,
                            String.format("Custom Group %s not found, cannot delete.", customGroup));
                } else {
                    // remove custom group via admin
                    admin.removeCategory(group.getName(), CategoryService.ENTITY_TYPE_CODE_CUSTOMGROUP);
                    // remove custom group succeeded
                    results.success(customGroup, String.format("Custom Group %s deleted.", customGroup));
                }
            } catch (Exception e) {
                String message = String.format("Failed to remove category: %s. Error: %s", customGroup, e.getMessage());
                log.error(message, e);
                results.fail(CUSTOM_GROUP_DELETE_MESSAGE, message);
            }

        }
        return results;
    }

    /**
     * If the CustomGroup doesn't exist it would be created and all host and service groups would be added.
     * If the CustomGroup already exist the existing host and service group references would be removed and the new groups added.
     * <p/>
     *
     * @param customGroupList contains one or more custom groups, and list of groups to be added
     * @return a operation results set with detailed information regarding results of operations
     */
    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoOperationResults createOrUpdateCustomGroup(DtoCustomGroupUpdateList customGroupList) {
        if (log.isDebugEnabled()) {
            log.debug(String.format("processing /POST on /customGroups with %d customGroups",
                    (customGroupList == null) ? 0 : customGroupList.size()));
        }
        if (customGroupList == null) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Custom Group was not provided").build());
        }
        DtoOperationResults results = new DtoOperationResults("CustomGroup", DtoOperationResults.UPDATE);
        if (customGroupList.size() == 0) {
            return results;
        }
        for (DtoCustomGroupUpdate groupUpdate : customGroupList.getCustomGroups()) {
            if (groupUpdate.getName() == null) {
                results.fail(CUSTOM_GROUP_UPDATE_MESSAGE, "No Custom Group Name provided");
                continue;
            }
            try {
                if (saveCustomGroup(groupUpdate, results)) {
                    results.success(groupUpdate.getName(), buildResourceLocator(uriInfo, RESOURCE_PREFIX, groupUpdate.getName()));
                }
            } catch (Exception e) {
                results.fail(CUSTOM_GROUP_UPDATE_MESSAGE,
                        String.format("Failed to update Custom Group %s, error: %s",
                                groupUpdate.getName(), e.getMessage()));
            }
        }
        return results;
    }

    /**
     * Add one or more host or service groups (members) to a custom group
     * The list of groups would be merged with the existing groups for the custom group.
     * If the custom group doesn't exist an error would occur.
     *
     * @param customGroup contains the name of the custom group, and list of groups to be added
     * @return a operation results set with detailed information regarding results of operations
     */
    @PUT
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("/addmembers")
    public DtoOperationResults addMembersToCustomGroup(DtoCustomGroupMemberUpdate customGroup) {
        if (log.isDebugEnabled()) {
            log.debug(String.format("processing /PUT add members on /customGroups for group %s",
                    (customGroup == null) ? "(none)" : customGroup.getName()));
        }
        if (customGroup == null) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Custom Group was not provided").build());
        }
        DtoOperationResults results = new DtoOperationResults("CustomGroup", DtoOperationResults.UPDATE);
        if (customGroup.getName() == null) {
            results.fail(CUSTOM_GROUP_UPDATE_MESSAGE, "No Custom Group Name provided");
            return results;
        }
        try {
            if (addMembers(customGroup, results)) {
                results.success(customGroup.getName(), buildResourceLocator(uriInfo, RESOURCE_PREFIX, customGroup.getName()));
            }
        } catch (Exception e) {
            results.fail(CUSTOM_GROUP_UPDATE_MESSAGE,
                    String.format("Failed to add members to Custom Group %s, error: %s",
                            customGroup.getName(), e.getMessage()));
        }
        return results;
    }

    /**
     * Delete one or more host or service groups (members) from a custom group
     * The List of groups would be removed from the custom group.
     * If the custom group or any of the groups doesn't exist an error will be returned.
     *
     * @param customGroup contains the name of the custom group, and list of groups to be added
     * @return a operation results set with detailed information regarding results of operations
     */
    @PUT
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("/deletemembers")
    public DtoOperationResults deleteMembersFromCustomGroup(DtoCustomGroupMemberUpdate customGroup) {
        if (log.isDebugEnabled()) {
            log.debug(String.format("processing /PUT delete members on /customGroups for group %s",
                    (customGroup == null) ? "(none)" : customGroup.getName()));
        }
        if (customGroup == null) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Custom Group was not provided").build());
        }
        DtoOperationResults results = new DtoOperationResults("CustomGroup", DtoOperationResults.UPDATE);
        if (customGroup.getName() == null) {
            results.fail(CUSTOM_GROUP_UPDATE_MESSAGE, "No Custom Group Name provided");
            return results;
        }
        try {
            if (deleteMembers(customGroup, results)) {
                results.success(customGroup.getName(), buildResourceLocator(uriInfo, RESOURCE_PREFIX, customGroup.getName()));
            }
        } catch (Exception e) {
            results.fail(CUSTOM_GROUP_UPDATE_MESSAGE,
                    String.format("Failed to delete members to Custom Group %s, error: %s",
                            customGroup.getName(), e.getMessage()));
        }
        return results;
    }

    /**
     * Save custom group. Returned results are set on failure.
     *
     * @param dtoCustomGroup custom group to save
     * @param results returned results.
     * @return success status
     */
    private boolean saveCustomGroup(DtoCustomGroupUpdate dtoCustomGroup, DtoOperationResults results) {
        CategoryService categoryService = CollageFactory.getInstance().getCategoryService();
        HostGroupService hostGroupService = CollageFactory.getInstance().getHostGroupService();
        CollageAdminInfrastructure admin = getAdminInfrastructureService();
        MetadataService metadataService = CollageFactory.getInstance().getMetadataService();
        EntityType entityType = metadataService.getEntityTypeByName(CategoryService.ENTITY_TYPE_CODE_CUSTOMGROUP);
        if (entityType == null) {
            results.fail(CUSTOM_GROUP_UPDATE_MESSAGE, "Custom Group Standard Entity Type Not Found");
            return false;
        }
        Category category = categoryService.getCategoryByName(dtoCustomGroup.getName(), CategoryService.ENTITY_TYPE_CODE_CUSTOMGROUP);
        if (category != null) {
            // update
            if (dtoCustomGroup.getDescription() != null)
                category.setDescription(dtoCustomGroup.getDescription().isEmpty() ? null : dtoCustomGroup.getDescription());
            if (dtoCustomGroup.getAppType() != null) {
                // changing app type
                ApplicationType applicationType = metadataService.getApplicationTypeByName(dtoCustomGroup.getAppType());
                if (applicationType != null) {
                    category.setApplicationType(applicationType);
                } else {
                    results.fail(CUSTOM_GROUP_UPDATE_MESSAGE,
                            String.format("Failed to find app type %s for custom group %s",
                                    dtoCustomGroup.getAppType(), dtoCustomGroup.getName()));
                    return false;
                }
            }
            if (dtoCustomGroup.getAgentId() != null)
                category.setAgentId(dtoCustomGroup.getAgentId());
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
                category = categoryService.getCategoryByName(dtoCustomGroup.getName(), CategoryService.ENTITY_TYPE_CODE_CUSTOMGROUP);
            }
        } else {
            // insert
            ApplicationType applicationType = null;
            if (dtoCustomGroup.getAppType() != null) {
                applicationType = metadataService.getApplicationTypeByName(dtoCustomGroup.getAppType());
                if (applicationType == null) {
                    results.fail(CUSTOM_GROUP_UPDATE_MESSAGE,
                            String.format("Failed to find app type %s for custom group %s",
                                    dtoCustomGroup.getAppType(), dtoCustomGroup.getName()));
                    return false;
                }
            }
            category = categoryService.createCategory(dtoCustomGroup.getName(), dtoCustomGroup.getDescription(),
                    entityType, applicationType, dtoCustomGroup.getAgentId());
        }
        // host groups
        if (dtoCustomGroup.getHostGroupNames() != null) {
            EntityType hostGroupEntityType = metadataService.getEntityTypeByName(CategoryService.ENTITY_TYPE_CODE_HOSTGROUP);
            if (hostGroupEntityType == null) {
                results.fail(CUSTOM_GROUP_UPDATE_MESSAGE, "Host Group Standard Entity Type Not Found");
                return false;
            }
            for (String hostGroupName : dtoCustomGroup.getHostGroupNames()) {
                HostGroup hostGroup = hostGroupService.getHostGroupByName(hostGroupName);
                if (hostGroup == null) {
                    results.fail(CUSTOM_GROUP_UPDATE_MESSAGE,
                            String.format("Failed to find host group %s for custom group %s",
                                    hostGroupName, dtoCustomGroup.getName()));
                    return false;
                }
                CategoryEntity entity = categoryService.createCategoryEntity();
                entity.setCategory(category);
                entity.setEntityType(hostGroupEntityType);
                entity.setObjectID(hostGroup.getHostGroupId());
                category.getCategoryEntities().add(entity);
            }
        }
        // service groups
        if (dtoCustomGroup.getServiceGroupNames() != null) {
            EntityType serviceGroupEntityType = metadataService.getEntityTypeByName(CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP);
            if (serviceGroupEntityType == null) {
                results.fail(CUSTOM_GROUP_UPDATE_MESSAGE, "Service Group Standard Entity Type Not Found");
                return false;
            }
            for (String serviceGroupName : dtoCustomGroup.getServiceGroupNames()) {
                Category serviceGroup = categoryService.getCategoryByName(serviceGroupName, CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP);
                if (serviceGroup == null) {
                    results.fail(CUSTOM_GROUP_UPDATE_MESSAGE,
                            String.format("Failed to find service group %s for custom group %s",
                                    serviceGroupName, dtoCustomGroup.getName()));
                    return false;
                }
                CategoryEntity entity = categoryService.createCategoryEntity();
                entity.setCategory(category);
                entity.setEntityType(serviceGroupEntityType);
                entity.setObjectID(serviceGroup.getCategoryId());
                category.getCategoryEntities().add(entity);
            }
        }
        // save custom group
        admin.saveCategory(category);
        return true;
    }

    /**
     * Add host and service group category entities. Returned results are set on failure.
     *
     * @param dtoCustomGroup custom group to update
     * @param results returned results.
     * @return success status
     */
    private boolean addMembers(DtoCustomGroupMemberUpdate dtoCustomGroup, DtoOperationResults results) {
        CategoryService categoryService = CollageFactory.getInstance().getCategoryService();
        HostGroupService hostGroupService = CollageFactory.getInstance().getHostGroupService();
        CollageAdminInfrastructure admin = getAdminInfrastructureService();
        MetadataService metadataService = CollageFactory.getInstance().getMetadataService();
        Category category = categoryService.getCategoryByName(dtoCustomGroup.getName(), CategoryService.ENTITY_TYPE_CODE_CUSTOMGROUP);
        if (category == null) {
            results.fail(CUSTOM_GROUP_UPDATE_MESSAGE,
                    String.format("Custom Group %s was not found, cannot update", dtoCustomGroup.getName()));
            return false;
        }
        if ((((dtoCustomGroup.getHostGroupNames()) == null) || dtoCustomGroup.getHostGroupNames().isEmpty()) &&
                (((dtoCustomGroup.getServiceGroupNames()) == null) || dtoCustomGroup.getServiceGroupNames().isEmpty())) {
            results.fail(CUSTOM_GROUP_UPDATE_MESSAGE,
                    String.format("No group members provided for Custom Group %s, cannot update", dtoCustomGroup.getName()));
            return false;
        }
        // host groups
        if (dtoCustomGroup.getHostGroupNames() != null) {
            EntityType hostGroupEntityType = metadataService.getEntityTypeByName(CategoryService.ENTITY_TYPE_CODE_HOSTGROUP);
            if (hostGroupEntityType == null) {
                results.fail(CUSTOM_GROUP_UPDATE_MESSAGE, "Host Group Standard Entity Type Not Found");
                return false;
            }
            for (String hostGroupName : dtoCustomGroup.getHostGroupNames()) {
                HostGroup hostGroup = hostGroupService.getHostGroupByName(hostGroupName);
                if (hostGroup == null) {
                    results.fail(CUSTOM_GROUP_UPDATE_MESSAGE,
                            String.format("Failed to find host group %s for custom group %s",
                                    hostGroupName, dtoCustomGroup.getName()));
                    return false;
                }
                if (findEntity(category, hostGroup.getHostGroupId(), CategoryService.ENTITY_TYPE_CODE_HOSTGROUP) == null) {
                    CategoryEntity entity = categoryService.createCategoryEntity();
                    entity.setCategory(category);
                    entity.setEntityType(hostGroupEntityType);
                    entity.setObjectID(hostGroup.getHostGroupId());
                    category.getCategoryEntities().add(entity);
                }
            }
        }
        // service groups
        if (dtoCustomGroup.getServiceGroupNames() != null) {
            EntityType serviceGroupEntityType = metadataService.getEntityTypeByName(CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP);
            if (serviceGroupEntityType == null) {
                results.fail(CUSTOM_GROUP_UPDATE_MESSAGE, "Service Group Standard Entity Type Not Found");
                return false;
            }
            for (String serviceGroupName : dtoCustomGroup.getServiceGroupNames()) {
                Category serviceGroup = categoryService.getCategoryByName(serviceGroupName, CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP);
                if (serviceGroup == null) {
                    results.fail(CUSTOM_GROUP_UPDATE_MESSAGE,
                            String.format("Failed to find service group %s for custom group %s",
                                    serviceGroupName, dtoCustomGroup.getName()));
                    return false;
                }
                if (findEntity(category, serviceGroup.getCategoryId(), CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP) == null) {
                    CategoryEntity entity = categoryService.createCategoryEntity();
                    entity.setCategory(category);
                    entity.setEntityType(serviceGroupEntityType);
                    entity.setObjectID(serviceGroup.getCategoryId());
                    category.getCategoryEntities().add(entity);
                }
            }
        }
        // save custom group
        admin.saveCategory(category);
        return true;
    }

    /**
     * Delete host and service group category entities. Returned results are set on failure.
     *
     * @param dtoCustomGroup custom group to update
     * @param results returned results.
     * @return success status
     */
    private boolean deleteMembers(DtoCustomGroupMemberUpdate dtoCustomGroup, DtoOperationResults results) {
        CategoryService categoryService = CollageFactory.getInstance().getCategoryService();
        HostGroupService hostGroupService = CollageFactory.getInstance().getHostGroupService();
        CollageAdminInfrastructure admin = getAdminInfrastructureService();
        MetadataService metadataService = CollageFactory.getInstance().getMetadataService();
        Category category = categoryService.getCategoryByName(dtoCustomGroup.getName(), CategoryService.ENTITY_TYPE_CODE_CUSTOMGROUP);
        if (category == null) {
            results.fail(CUSTOM_GROUP_UPDATE_MESSAGE,
                    String.format("Custom Group %s was not found, cannot update", dtoCustomGroup.getName()));
            return false;
        }
        if ((((dtoCustomGroup.getHostGroupNames()) == null) || dtoCustomGroup.getHostGroupNames().isEmpty()) &&
                (((dtoCustomGroup.getServiceGroupNames()) == null) || dtoCustomGroup.getServiceGroupNames().isEmpty())) {
            results.fail(CUSTOM_GROUP_UPDATE_MESSAGE,
                    String.format("No group members provided for Custom Group %s, cannot delete", dtoCustomGroup.getName()));
            return false;
        }
        // host groups
        if (dtoCustomGroup.getHostGroupNames() != null) {
            for (String hostGroupName : dtoCustomGroup.getHostGroupNames()) {
                HostGroup hostGroup = hostGroupService.getHostGroupByName(hostGroupName);
                if (hostGroup == null) {
                    results.fail(CUSTOM_GROUP_UPDATE_MESSAGE,
                            String.format("Failed to find host group %s for custom group %s",
                                    hostGroupName, dtoCustomGroup.getName()));
                    return false;
                }
                CategoryEntity categoryEntity = findEntity(category, hostGroup.getHostGroupId(),
                        CategoryService.ENTITY_TYPE_CODE_HOSTGROUP);
                if (categoryEntity == null) {
                    results.warn(CUSTOM_GROUP_UPDATE_MESSAGE,
                            String.format("Failed to find host group %s for custom group %s to delete",
                                    hostGroupName, dtoCustomGroup.getName()));
                    return false;
                }
                category.getCategoryEntities().remove(categoryEntity);
                categoryService.deleteCategoryEntity(categoryEntity);
            }
        }
        // service groups
        if (dtoCustomGroup.getServiceGroupNames() != null) {
            for (String serviceGroupName : dtoCustomGroup.getServiceGroupNames()) {
                Category serviceGroup = categoryService.getCategoryByName(serviceGroupName, CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP);
                if (serviceGroup == null) {
                    results.fail(CUSTOM_GROUP_UPDATE_MESSAGE,
                            String.format("Failed to find service group %s for custom group %s",
                                    serviceGroupName, dtoCustomGroup.getName()));
                    return false;
                }
                CategoryEntity categoryEntity = findEntity(category, serviceGroup.getCategoryId(),
                        CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP);
                if (categoryEntity == null) {
                    results.warn(CUSTOM_GROUP_UPDATE_MESSAGE,
                            String.format("Failed to find service group %s for custom group %s to delete",
                                    serviceGroupName, dtoCustomGroup.getName()));
                    return false;
                }
                category.getCategoryEntities().remove(categoryEntity);
                categoryService.deleteCategoryEntity(categoryEntity);
            }
        }
        // save custom group
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
        if (log.isDebugEnabled()) {
            log.debug(String.format("processing /GET on /autocomplete/%s with limit %d", prefix, limit));
        }
        try {
            Autocomplete customGroupAutocompleteService = CollageFactory.getInstance().getCustomGroupAutocompleteService();
            List<AutocompleteName> names = customGroupAutocompleteService.autocomplete(prefix, limit);
            if (names.isEmpty()) {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity(String.format("Custom group names not found for prefix [%s]", prefix)).build());
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
        }
    }
}

