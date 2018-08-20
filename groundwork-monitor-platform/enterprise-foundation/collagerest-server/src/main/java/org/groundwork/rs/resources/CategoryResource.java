package org.groundwork.rs.resources;

import com.groundwork.collage.CollageAdminInfrastructure;
import com.groundwork.collage.CollageFactory;
import com.groundwork.collage.model.ApplicationType;
import com.groundwork.collage.model.Category;
import com.groundwork.collage.model.CategoryEntity;
import com.groundwork.collage.model.EntityType;
import com.groundwork.collage.query.QueryTranslation;
import com.groundwork.collage.query.QueryTranslator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.bs.category.CategoryService;
import org.groundwork.foundation.bs.metadata.MetadataService;
import org.groundwork.foundation.dao.SortCriteria;
import org.groundwork.rs.conversion.CategoryConverter;
import org.groundwork.rs.dto.DtoCategory;
import org.groundwork.rs.dto.DtoCategoryEntity;
import org.groundwork.rs.dto.DtoCategoryList;
import org.groundwork.rs.dto.DtoCategoryMemberUpdate;
import org.groundwork.rs.dto.DtoCategoryUpdate;
import org.groundwork.rs.dto.DtoCategoryUpdateList;
import org.groundwork.rs.dto.DtoDepthType;
import org.groundwork.rs.dto.DtoOperationResults;
import org.groundwork.rs.dto.DtoSortType;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Path("/categories")
public class CategoryResource extends AbstractResource {

    public static final String RESOURCE_PREFIX = "/categories/";
    protected static Log log = LogFactory.getLog(CategoryResource.class);

    @GET
    @Path("/{categoryName}/{entityType}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoCategory getCategory(@PathParam("categoryName") String categoryName,
                                   @PathParam("entityType") String entityType,
                                   @QueryParam("depth") @DefaultValue(DtoDepthType.DEFAULT) DtoDepthType.DtoDepthWrapper depthWrapper) {
        try {
            DtoDepthType depth = depthWrapper.getType();
            if (log.isDebugEnabled()) {
                log.debug(String.format("processing /GET on /categories/%s with depth: %s", categoryName, depth));
            }
            if (categoryName == null) {
                throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                        .entity("Category name is mandatory").build());
            }
            CategoryService categoryService =  CollageFactory.getInstance().getCategoryService();
            Category category = categoryService.getCategoryByName(categoryName, entityType);
            if (category == null) {
                String entityTypeMessage = (entityType == null) ? "" : "for entity type " + entityType;
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                        .entity(String.format("Category name [%s] was not found %s",
                                categoryName, entityTypeMessage)).build());
            }
            return CategoryConverter.convert(category, depth);
        } catch (Exception e) {
            if (e instanceof WebApplicationException)
                throw e;
            log.error(ResourceMessages.UNEXPECTED_EXCEPTION + e, e);
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
                    String.format("An error occurred processing request for category [%s].", categoryName)).build());
        }
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoCategoryList getCategories(@QueryParam("query") String query,
                                         @QueryParam("depth") @DefaultValue(DtoDepthType.DEFAULT) DtoDepthType.DtoDepthWrapper depthWrapper,
                                         @QueryParam("name") String name,
                                         @QueryParam("entityTypeName") String entityTypeName,
                                         @QueryParam("roots") @DefaultValue("false") boolean roots,
                                         @QueryParam("entityObjectId") Integer entityObjectId,
                                         @QueryParam("entityEntityTypeName") String entityEntityTypeName,
                                         @QueryParam("entityRoots") @DefaultValue("true") boolean entityRoots,
                                         @QueryParam("first") @DefaultValue("-1") int first,
                                         @QueryParam("count") @DefaultValue("-1") int count) {
        try {
            DtoDepthType depth = depthWrapper.getType();
            if (log.isDebugEnabled()) {
                if (query != null) {
                    log.debug(String.format("processing /GET on /categories with depth: %s, query: %s,  first: %d, count: %d",
                            depth, query, first, count));
                } else if (name != null) {
                    log.debug(String.format("processing /GET on /categories with depth: %s, name: %s, entityTypeName: %s",
                            depth, name, entityTypeName));
                    if (entityTypeName == null) {
                        throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                                .entity("Entity type name is mandatory").build());
                    }
                } else if (entityObjectId != null) {
                    log.debug(String.format("processing /GET on /categories with depth: %s, entityTypeName: %s, " +
                            "entityObjectId: %d, entityEntityTypeName: %s, entityRoots: %b", depth, entityTypeName,
                            entityObjectId, entityEntityTypeName, entityRoots));
                    if (entityTypeName == null) {
                        throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                                .entity("Entity type name is mandatory").build());
                    }
                    if (entityEntityTypeName == null) {
                        throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                                .entity("Entity entity type name is mandatory").build());
                    }
                } else if (entityTypeName != null) {
                    log.debug(String.format("processing /GET on /categories with depth: %s, entityTypeName: %s, roots: %b",
                            depth, entityTypeName, roots));
                } else {
                    log.debug(String.format("processing /GET on /categories with depth: %s, query: (none),  first: %d, count: %d",
                            depth, first, count));
                }
            }
            CategoryService categoryService =  CollageFactory.getInstance().getCategoryService();
            QueryTranslator queryTranslator = CollageFactory.getInstance().getQueryTranslator();
            MetadataService metadataService = CollageFactory.getInstance().getMetadataService();

            Collection<Category> categories = null;
            if (query != null) {
                QueryTranslation translation = queryTranslator.translate(query, QueryTranslator.CATEGORY_KEY);
                if (log.isDebugEnabled()) log.debug("hql = [" + translation.getHql() + "]");
                categories = categoryService.queryCategories(translation.getHql(), translation.getCountHql(), first, count).getResults();
                if (categories.isEmpty()) {
                    throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                            .entity(String.format("Categories not found for query criteria [%s]", query)).build());
                }
            } else if (name != null) {
                try {
                    Category hierarchyCategory = categoryService.getCategoryByName(name, entityTypeName);
                    categories = categoryService.getHierarchyCategories(hierarchyCategory);
                } catch (IllegalArgumentException iae) {
                    categories = Collections.EMPTY_LIST;
                }
                if (categories.isEmpty()) {
                    throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                            .entity(String.format("Categories not found for category [%s:%s]", name, entityTypeName)).build());
                }
            } else if (entityObjectId != null) {
                try {
                    EntityType entityType = metadataService.getEntityTypeByName(entityTypeName);
                    EntityType entityEntityType = metadataService.getEntityTypeByName(entityEntityTypeName);
                    if (entityRoots) {
                        categories = categoryService.getEntityRootCategoriesByObjectId(entityType, entityObjectId,
                                entityEntityType);
                    } else {
                        categories = categoryService.getEntityCategoriesByObjectId(entityType, entityObjectId,
                                entityEntityType);
                    }
                } catch (IllegalArgumentException iae) {
                    categories = Collections.EMPTY_LIST;
                }
                if (categories.isEmpty()) {
                    throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                            .entity(String.format("[%s] categories not found for entity [%d:%s]", entityTypeName,
                                    entityObjectId, entityEntityTypeName)).build());
                }
            } else if (entityTypeName != null) {
                try {
                    if (roots) {
                        categories = categoryService.getRootCategories(entityTypeName);
                    } else {
                        categories = categoryService.getCategoriesByEntityType(entityTypeName);
                    }
                } catch (IllegalArgumentException iae) {
                    categories = Collections.EMPTY_LIST;
                }
                if (categories.isEmpty()) {
                    throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                            .entity(String.format("Categories not found for entity type name [%s]", entityTypeName)).build());
                }
            } else {
                SortCriteria sortCriteria = createSortCriteria("name", DtoSortType.Ascending);
                categories = categoryService.getCategories(null, sortCriteria, first, count).getResults();
                if (categories.isEmpty()) {
                    throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                            .entity("Categories not found").build());
                }
            }

            List<DtoCategory> dtoCategories = new ArrayList<DtoCategory>();
            for (Category category : categories) {
                DtoCategory DtoCategory = CategoryConverter.convert(category, depth);
                dtoCategories.add(DtoCategory);
            }
            return new DtoCategoryList(dtoCategories);
        } catch (Exception e) {
            if (e instanceof WebApplicationException)
                throw e;
            log.error(ResourceMessages.UNEXPECTED_EXCEPTION + e, e);
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An error occurred processing request for categories.").build());
        }
    }

    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoOperationResults createCategories(DtoCategoryList dtoCategoryList) {
        if (log.isDebugEnabled()) {
            log.debug(String.format("processing /POST on /categories with %d categories", (dtoCategoryList == null) ? 0 : dtoCategoryList.size()));
        }
        if (dtoCategoryList == null) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Category list was not provided").build());
        }
        DtoOperationResults results = new DtoOperationResults("Category", DtoOperationResults.UPDATE);
        if (dtoCategoryList.size() == 0) {
            return results;
        }
        for (DtoCategory categoryUpdate : dtoCategoryList.getCategories()) {
            String entityTypeName = categoryUpdate.getEntityTypeName();
            if ((entityTypeName == null) && (categoryUpdate.getEntityType() != null)) {
                entityTypeName = categoryUpdate.getEntityType().getName();
            }
            if ((categoryUpdate.getName() == null) || (entityTypeName == null)) {
                results.fail("(unknown)", "Category name or entity type name not provided");
                continue;
            }
            String entity = categoryUpdate.getName() + ":" + entityTypeName;
            try {
                if (saveCategory(categoryUpdate, results)) {
                    results.success(entity, buildResourceLocator(uriInfo, RESOURCE_PREFIX, entity.replace(':', '/')));
                }
            }
            catch (Exception e) {
                results.fail(entity, e.getMessage());
            }
        }
        return results;
    }

    @PUT
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoOperationResults updateCategories(DtoCategoryUpdateList dtoCategoryUpdateList) {
        if (log.isDebugEnabled()) {
            log.debug(String.format("processing /PUT on /categories with %d category updates", (dtoCategoryUpdateList == null) ? 0 : dtoCategoryUpdateList.size()));
        }
        if (dtoCategoryUpdateList == null) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Category update list was not provided").build());
        }
        DtoOperationResults results = new DtoOperationResults("Category", DtoOperationResults.UPDATE);
        if (dtoCategoryUpdateList.size() == 0) {
            return results;
        }
        // access components
        CategoryService categoryService = CollageFactory.getInstance().getCategoryService();
        MetadataService metadataService = CollageFactory.getInstance().getMetadataService();
        CollageAdminInfrastructure admin = getAdminInfrastructureService();
        PlatformTransactionManager transactionManager = (PlatformTransactionManager)CollageFactory.getInstance().getAPIObject(CollageFactory.TRANSACTION_MANAGER);
        TransactionStatus transaction = null;
        boolean warnOnException = false;
        String entity = null;
        try {
            Set<Category> createdCategories = new HashSet<Category>();
            Set<Category> modifiedCategories = new HashSet<Category>();
            Set<Category> deletedCategories = new HashSet<Category>();
            // start transaction
            transaction = transactionManager.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRED));
            // process category updates
            for (DtoCategoryUpdate dtoCategoryUpdate : dtoCategoryUpdateList.getCategoryUpdates()) {
                warnOnException = false;
                entity = dtoCategoryUpdate.getCategoryName() + ":" + dtoCategoryUpdate.getEntityTypeName();
                // process category update
                CategoryService.Results updateResults;
                if (dtoCategoryUpdate.getDelete() != null) {
                    // delete category
                    warnOnException = true;
                    Category category = lookupUpdateCategory(categoryService, metadataService, dtoCategoryUpdate);
                    boolean childrenOnly = ((dtoCategoryUpdate.getChildrenOnly() != null) &&
                            dtoCategoryUpdate.getChildrenOnly());
                    CategoryService.Delete delete = CategoryService.Delete.valueOf(dtoCategoryUpdate.getDelete());
                    updateResults = categoryService.deleteCategory(category, childrenOnly, delete);
                    if (updateResults.deleted != null) {
                        // remove deleted category from any referencing categories
                        for (Category deletedCategory : updateResults.deleted) {
                            Collection<Category> referencingCategories = categoryService.deleteCategoryEntityByObjectID(
                                    category.getID(), category.getEntityType().getName());
                            if ((referencingCategories != null) && !referencingCategories.isEmpty()) {
                                for (Category referencingCategory : referencingCategories) {
                                    if (!updateResults.modified.contains(referencingCategories)) {
                                        updateResults.modified.add(referencingCategory);
                                    }
                                }
                            }
                        }
                    }
                    results.success(entity, "Category deleted, (" + delete.name() + ").");
                } else if (dtoCategoryUpdate.getCreate() != null) {
                    // create category
                    String name = dtoCategoryUpdate.getCategoryName();
                    EntityType entityType = lookupUpdateEntityType(metadataService, dtoCategoryUpdate);
                    String description = dtoCategoryUpdate.getDescription();
                    ApplicationType applicationType = lookupUpdateApplicationType(metadataService, dtoCategoryUpdate);
                    String agentId = dtoCategoryUpdate.getAgentId();
                    Category parent = lookupUpdateParent(categoryService, metadataService, dtoCategoryUpdate);
                    CategoryService.Create create = CategoryService.Create.valueOf(dtoCategoryUpdate.getCreate());
                    updateResults = categoryService.createAndSaveCategory(name, entityType, description, applicationType,
                            agentId, parent, create);
                    results.success(entity, buildResourceLocator(uriInfo, RESOURCE_PREFIX, entity.replace(':', '/')));
                } else if (dtoCategoryUpdate.getClone() != null) {
                    // clone category
                    Category category = lookupUpdateCategory(categoryService, metadataService, dtoCategoryUpdate);
                    String name = dtoCategoryUpdate.getCloneName();
                    CategoryService.Clone clone = CategoryService.Clone.valueOf(dtoCategoryUpdate.getClone());
                    updateResults = categoryService.cloneAndSaveCategory(category, name, clone);
                    results.success(entity, buildResourceLocator(uriInfo, RESOURCE_PREFIX, entity.replace(':', '/')));
                } else if (dtoCategoryUpdate.getModify() != null) {
                    // modify category
                    Category category = lookupUpdateCategory(categoryService, metadataService, dtoCategoryUpdate);
                    List<Category> otherCategories = lookupOtherCategories(categoryService, metadataService,
                            dtoCategoryUpdate);
                    CategoryService.Modify modify = CategoryService.Modify.valueOf(dtoCategoryUpdate.getModify());
                    updateResults = categoryService.modifyCategory(category, otherCategories, modify);
                    results.success(entity, buildResourceLocator(uriInfo, RESOURCE_PREFIX, entity.replace(':', '/')));
                } else {
                    throw new IllegalArgumentException("Category update delete, create, clone, or modify not specified");
                }
                if (updateResults.created != null) {
                    createdCategories.add(updateResults.created);
                }
                if (updateResults.modified != null) {
                    modifiedCategories.addAll(updateResults.modified);
                }
                if (updateResults.deleted != null) {
                    deletedCategories.addAll(updateResults.deleted);
                }
                entity = null;
            }
            // commit transaction
            transactionManager.commit(transaction);
            // send category notifications via admin: sending in created, modified,
            // and then removed order to make it easier to update remote models
            if (!createdCategories.isEmpty()) {
                admin.propagateCreatedCategories(createdCategories);
            }
            if (!modifiedCategories.isEmpty()) {
                admin.propagateModifiedCategories(modifiedCategories);
            }
            if (!deletedCategories.isEmpty()) {
                admin.propagateDeletedCategories(deletedCategories);
            }
        } catch (Exception e) {
            // rollback transaction
            if (transaction != null) {
                try {
                    transactionManager.rollback(transaction);
                } catch (Exception ignore) {
                }
            }
            // fail entity or request
            if (entity != null) {
                if (warnOnException) {
                    results.warn(entity, e.getMessage());
                } else {
                    results.fail(entity, e.getMessage());
                }
            } else {
                log.error(ResourceMessages.UNEXPECTED_EXCEPTION + e, e);
                throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("An error occurred processing request for category updates.").build());
            }
        }
        return results;
    }

    @PUT
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("/addmembers")
    public DtoOperationResults addMembersToCategory(DtoCategoryMemberUpdate dtoCategoryMemberUpdate) {
        if (log.isDebugEnabled()) {
            log.debug(String.format("processing /PUT add members on /categories for group (%s:%s)",
                    ((dtoCategoryMemberUpdate == null) ? null : dtoCategoryMemberUpdate.getName()),
                    ((dtoCategoryMemberUpdate == null) ? null : dtoCategoryMemberUpdate.getEntityTypeName())));
        }
        if (dtoCategoryMemberUpdate == null) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Category member update was not provided").build());
        }
        DtoOperationResults results = new DtoOperationResults("Category", DtoOperationResults.UPDATE);
        if ((dtoCategoryMemberUpdate.getName() == null) || (dtoCategoryMemberUpdate.getEntityTypeName() == null)) {
            results.fail("Category Update", "No category name or entity type name provided");
            return results;
        }
        String entity = dtoCategoryMemberUpdate.getName() + ":" + dtoCategoryMemberUpdate.getEntityTypeName();
        if ((dtoCategoryMemberUpdate.getEntities() == null) || dtoCategoryMemberUpdate.getEntities().isEmpty()) {
            results.fail(entity, "No category members to add provided");
            return results;
        }
        CollageAdminInfrastructure admin = getAdminInfrastructureService();
        CategoryService categoryService =  CollageFactory.getInstance().getCategoryService();
        MetadataService metadataService = CollageFactory.getInstance().getMetadataService();
        try {
            // lookup category
            Category category = categoryService.getCategoryByName(dtoCategoryMemberUpdate.getName(),
                    dtoCategoryMemberUpdate.getEntityTypeName());
            if (category == null) {
                results.fail(entity, "Category not found");
                return results;
            }
            // add members/category entities
            boolean added = false;
            for (DtoCategoryEntity dtoCategoryEntity : dtoCategoryMemberUpdate.getEntities()) {
                CategoryEntity categoryEntity = findEntity(category, dtoCategoryEntity);
                if (categoryEntity == null) {
                    EntityType entityEntityType = lookupCategoryEntityType(dtoCategoryEntity, metadataService);
                    if ((dtoCategoryEntity.getObjectID() == null) || (entityEntityType == null)) {
                        results.fail(entity, "Category entity object id or entity type missing or invalid");
                        return results;
                    }
                    categoryEntity = categoryService.createCategoryEntity();
                    categoryEntity.setObjectID(dtoCategoryEntity.getObjectID());
                    categoryEntity.setEntityType(entityEntityType);
                    categoryEntity.setCategory(category);
                    category.getCategoryEntities().add(categoryEntity);
                    added = true;
                }
            }
            if (!added) {
                results.fail(entity, "Category members not added");
                return results;
            }
            // save category via admin
            admin.saveCategory(category);
            results.success(entity, buildResourceLocator(uriInfo, RESOURCE_PREFIX, entity.replace(':', '/')));
        } catch (Exception e) {
            results.fail(entity, String.format("Failed to add members to category, error: %s", e.getMessage()));
        }
        return results;
    }

    @PUT
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("/deletemembers")
    public DtoOperationResults deleteMembersFromCategory(DtoCategoryMemberUpdate dtoCategoryMemberUpdate) {
        if (log.isDebugEnabled()) {
            log.debug(String.format("processing /PUT delete members on /categories for group (%s:%s)",
                    ((dtoCategoryMemberUpdate == null) ? null : dtoCategoryMemberUpdate.getName()),
                    ((dtoCategoryMemberUpdate == null) ? null : dtoCategoryMemberUpdate.getEntityTypeName())));
        }
        if (dtoCategoryMemberUpdate == null) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Category member update was not provided").build());
        }
        DtoOperationResults results = new DtoOperationResults("Category", DtoOperationResults.UPDATE);
        if ((dtoCategoryMemberUpdate.getName() == null) || (dtoCategoryMemberUpdate.getEntityTypeName() == null)) {
            results.fail("Category Update", "No category name or entity type name provided");
            return results;
        }
        String entity = dtoCategoryMemberUpdate.getName() + ":" + dtoCategoryMemberUpdate.getEntityTypeName();
        if ((dtoCategoryMemberUpdate.getEntities() == null) || dtoCategoryMemberUpdate.getEntities().isEmpty()) {
            results.fail("Category Update", "No members to delete provided");
            return results;
        }
        CollageAdminInfrastructure admin = getAdminInfrastructureService();
        CategoryService categoryService =  CollageFactory.getInstance().getCategoryService();
        try {
            // lookup category
            Category category = categoryService.getCategoryByName(dtoCategoryMemberUpdate.getName(),
                    dtoCategoryMemberUpdate.getEntityTypeName());
            if (category == null) {
                results.warn(entity, "Category not found");
                return results;
            }
            // remove members/category entities
            boolean removed = false;
            for (DtoCategoryEntity dtoCategoryEntity : dtoCategoryMemberUpdate.getEntities()) {
                CategoryEntity categoryEntity = findEntity(category, dtoCategoryEntity);
                if (categoryEntity != null) {
                    if (category.getCategoryEntities().remove(categoryEntity)) {
                        categoryService.deleteCategoryEntity(categoryEntity);
                        removed = true;
                    }
                }
            }
            if (!removed) {
                results.fail(entity, "Category members not removed");
                return results;
            }
            // save category via admin
            admin.saveCategory(category);
            results.success(entity, buildResourceLocator(uriInfo, RESOURCE_PREFIX, entity.replace(':', '/')));
        } catch (Exception e) {
            results.fail(entity, String.format("Failed to delete members from category, error: %s", e.getMessage()));
        }
        return results;
    }

    @DELETE
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public DtoOperationResults deleteCategoriesWithUpdate(DtoCategoryList dtoCategoryList) {
        if (log.isDebugEnabled()) {
            log.debug(String.format("processing /DELETE on /categories with %d categories", (dtoCategoryList == null) ? 0 : dtoCategoryList.size()));
        }
        if (dtoCategoryList == null) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Category list was not provided").build());
        }
        DtoOperationResults results = new DtoOperationResults("Category", DtoOperationResults.DELETE);
        if (dtoCategoryList.size() == 0) {
            return results;
        }
        CollageAdminInfrastructure admin = getAdminInfrastructureService();
        CategoryService categoryService =  CollageFactory.getInstance().getCategoryService();
        for (DtoCategory category : dtoCategoryList.getCategories()) {
            String entityTypeName = category.getEntityTypeName();
            if ((entityTypeName == null) && (category.getEntityType() != null)) {
                entityTypeName = category.getEntityType().getName();
            }
            if ((category.getName() == null) || (entityTypeName == null)) {
                results.fail("(unknown)", "Category name or entity type name not provided");
                continue;
            }
            String entity = category.getName() + ":" + entityTypeName;
            try {
                Category check = categoryService.getCategoryByName(category.getName(), entityTypeName);
                if (check == null) {
                    results.warn(entity, "Category not found, cannot delete.");
                } else {
                    // remove category
                    Category removed = admin.removeCategory(category.getName(), entityTypeName);
                    // remove category from referencing categories
                    admin.removeCategoryEntity(entityTypeName, removed.getID());
                    // remove category succeeded
                    results.success(entity, "Category deleted.");
                }
            }
            catch (Exception e) {
                log.error("Failed to remove category : " + e.getMessage(), e);
                results.fail(entity, e.toString());
            }
        }
        // 4. Return the results
        return results;
    }

    /**
     * Save category via the admin APIs.
     *
     * @param dtoCategory category to save
     * @param results returned operation results
     * @return saved flag
     */
    private boolean saveCategory(DtoCategory dtoCategory, DtoOperationResults results) {
        CategoryService categoryService =  CollageFactory.getInstance().getCategoryService();
        CollageAdminInfrastructure admin = getAdminInfrastructureService();
        MetadataService metadataService =  CollageFactory.getInstance().getMetadataService();
        String entityTypeName = dtoCategory.getEntityTypeName();
        if ((entityTypeName == null) && (dtoCategory.getEntityType() != null)) {
            entityTypeName = dtoCategory.getEntityType().getName();
        }
        if (entityTypeName == null) {
            results.fail( "Category Entity Type", "Category Entity Type Key not provided");
            return false;
        }
        Category category = categoryService.getCategoryByName(dtoCategory.getName(), entityTypeName);
        if (category != null) {
            // update
            if (dtoCategory.getDescription() != null)
                category.setDescription(dtoCategory.getDescription().isEmpty() ? null : dtoCategory.getDescription());
            if (dtoCategory.getAgentId() != null)
                category.setAgentId(dtoCategory.getAgentId());
            if (dtoCategory.getAppType() != null) {
                ApplicationType applicationType = metadataService.getApplicationTypeByName(dtoCategory.getAppType());
                if (applicationType != null) {
                    category.setApplicationType(applicationType);
                }
            }
            if (dtoCategory.isRoot() != null) {
                categoryService.modifyCategory(category, null, (dtoCategory.isRoot() ? CategoryService.Modify.ROOT :
                        CategoryService.Modify.UNROOT));
            }
        }
        else {
            // insert
            if (!validate(dtoCategory, results)) {
                return false;
            }
            EntityType entityType = lookupEntityType(dtoCategory, metadataService);
            if (entityType == null) {
                results.fail( "Category Entity Type", "Not Found Category Entity Type Key provided");
                return false;
            }
            ApplicationType applicationType = null;
            if (dtoCategory.getAppType() != null) {
                applicationType = metadataService.getApplicationTypeByName(dtoCategory.getAppType());
            }
            category = categoryService.createCategory(dtoCategory.getName(), dtoCategory.getDescription(),
                    entityType, applicationType, dtoCategory.getAgentId());
            if (dtoCategory.isRoot() != null) {
                category.setRoot(dtoCategory.isRoot());
            }
        }
        admin.saveCategory(category);

        // create category entities, (removes not supported)
        int count = 0;
        if (dtoCategory.getEntities() != null) {
            for (DtoCategoryEntity dtoEntity : dtoCategory.getEntities()) {
                CategoryEntity entity = findEntity(category, dtoEntity);
                if (entity == null) {
                    EntityType entityType = lookupCategoryEntityType(dtoEntity, metadataService);
                    if (entityType != null) {
                        entity = categoryService.createCategoryEntity();
                        entity.setObjectID(dtoEntity.getObjectID());
                        entity.setEntityType(entityType);
                        entity.setCategory(category);
                        category.getCategoryEntities().add(entity);
                        count++;
                    }
                }
            }
        }
        if (count > 0) {
            admin.saveCategory(category);
        }

        // add parents as specified for update, (removes not supported)
        Collection<Category> addParents = new ArrayList<Category>();
        if (dtoCategory.getParentNames() != null) {
            for (String parentName : dtoCategory.getParentNames()) {
                Category parent = findParent(category, parentName);
                if (parent == null) {
                    parent = categoryService.getCategoryByName(parentName, entityTypeName);
                    if (parent != null) {
                        addParents.add(parent);
                    }
                }
            }
        }
        if (dtoCategory.getParents() != null) {
            for (DtoCategory dtoParent : dtoCategory.getParents()) {
                Category parent = findParent(category, dtoParent);
                if (parent == null) {
                    parent = ((dtoParent.getName() != null) ?
                            categoryService.getCategoryByName(dtoParent.getName(), entityTypeName) :
                            categoryService.getCategoryById(dtoParent.getId()));
                    if (parent != null) {
                        addParents.add(parent);
                    }
                }
            }
        }
        if (!addParents.isEmpty()) {
            // add parents to category
            CategoryService.Results modifyResults = categoryService.modifyCategory(category, addParents,
                    CategoryService.Modify.ADD_PARENTS);
            // send category notifications via admin
            if ((modifyResults != null) && (modifyResults.modified != null) && !modifyResults.modified.isEmpty()) {
                admin.propagateModifiedCategories(modifyResults.modified);
            }
        }

        // add children as specified for update, (removes not supported)
        Collection<Category> addChildren = new ArrayList<Category>();
        if (dtoCategory.getChildNames() != null) {
            for (String childName : dtoCategory.getChildNames()) {
                Category child = findChild(category, childName);
                if (child == null) {
                    child = categoryService.getCategoryByName(childName, entityTypeName);
                    if (child != null) {
                        addChildren.add(child);
                    }
                }
            }
        }
        if (dtoCategory.getChildren() != null) {
            for (DtoCategory dtoChild : dtoCategory.getChildren()) {
                Category child = findChild(category, dtoChild);
                if (child == null) {
                    child = ((dtoChild.getName() != null) ?
                            categoryService.getCategoryByName(dtoChild.getName(), entityTypeName) :
                            categoryService.getCategoryById(dtoChild.getId()));
                    if (child != null) {
                        addChildren.add(child);
                    }
                }
            }
        }
        if (!addChildren.isEmpty()) {
            // add children to category
            CategoryService.Results modifyResults = categoryService.modifyCategory(category, addChildren,
                    CategoryService.Modify.ADD_CHILDREN);
            // send category notifications via admin
            if ((modifyResults != null) && (modifyResults.modified != null) && !modifyResults.modified.isEmpty()) {
                admin.propagateModifiedCategories(modifyResults.modified);
            }
        }
        return true;
    }


    /**
     * Lookup category entity type.
     *
     * @param dtoEntity category
     * @param metadataService metadata service
     * @return entity type
     */
    private EntityType lookupEntityType(DtoCategory dtoEntity, MetadataService metadataService) {
        EntityType entityType = null;
        if (dtoEntity.getEntityTypeName() != null) {
            entityType = metadataService.getEntityTypeByName(dtoEntity.getEntityTypeName());
        }
        else if (dtoEntity.getEntityType() != null && dtoEntity.getEntityType().getName() != null) {
            entityType = metadataService.getEntityTypeByName(dtoEntity.getEntityType().getName());
        }
        else {
            entityType =  metadataService.getEntityTypeById(dtoEntity.getEntityType().getId());
        }
        return entityType;
    }

    /**
     * Lookup category entity entity type.
     *
     * @param dtoEntity category entity
     * @param metadataService metadata service
     * @return entity type
     */
    private EntityType lookupCategoryEntityType(DtoCategoryEntity dtoEntity, MetadataService metadataService) {
        EntityType entityType = null;
        if (dtoEntity.getEntityTypeName() != null) {
            entityType = metadataService.getEntityTypeByName(dtoEntity.getEntityTypeName());
        }
        else if (dtoEntity.getEntityType() != null && dtoEntity.getEntityType().getName() != null) {
            entityType = metadataService.getEntityTypeByName(dtoEntity.getEntityType().getName());
        }
        else {
            int id = (dtoEntity.getEntityTypeId() == null) ? dtoEntity.getEntityType().getId() : dtoEntity.getEntityTypeId();
            entityType =  metadataService.getEntityTypeById(id);
        }
        return entityType;
    }

    /**
     * Find category entity.
     *
     * @param category category
     * @param dtoEntity category entity to find
     * @return category entity
     */
    private CategoryEntity findEntity(Category category, DtoCategoryEntity dtoEntity) {
        if (category.getCategoryEntities() != null) {
            for (CategoryEntity entity : category.getCategoryEntities()) {
                if ((entity.getObjectID() != null) && entity.getObjectID().equals(dtoEntity.getObjectID()) &&
                        (entity.getEntityType() != null)) {
                    String entityEntityTypeName = dtoEntity.getEntityTypeName();
                    if ((entityEntityTypeName == null) && (dtoEntity.getEntityType() != null)) {
                        entityEntityTypeName = dtoEntity.getEntityType().getName();
                    }
                    if (entityEntityTypeName != null) {
                        if (entityEntityTypeName.equals(entity.getEntityType().getName())) {
                            return entity;
                        }
                    } else {
                        Integer entityEntityTypeId = dtoEntity.getEntityTypeId();
                        if ((entityEntityTypeId == null) && (dtoEntity.getEntityType() != null)) {
                            entityEntityTypeId = dtoEntity.getEntityType().getId();
                        }
                        if (entityEntityTypeId != null) {
                            if (entityEntityTypeId.equals(entity.getEntityType().getEntityTypeId())) {
                                return entity;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Find category parent by name.
     *
     * @param category category
     * @param parentName parent name to find
     * @return parent category
     */
    private Category findParent(Category category, String parentName) {
        if (category.getParents() != null) {
            for (Category p : category.getParents()) {
                if (parentName.equals(p.getName())) {
                    return p;
                }
            }
        }
        return null;
    }

    /**
     * Find category parent.
     *
     * @param category category
     * @param parent parent category to find
     * @return parent category
     */
    private Category findParent(Category category, DtoCategory parent) {
        if (category.getParents() != null) {
            for (Category p : category.getParents()) {
                if (parent.getName() != null) {
                    if (parent.getName().equals(p.getName())) {
                        return p;
                    }
                }
                else {
                    if (parent.getId().equals(p.getID()))
                        return p;
                }
            }
        }
        return null;
    }

    /**
     * Find category child by name.
     *
     * @param category category
     * @param childName child name to find
     * @return child category
     */
    private Category findChild(Category category, String childName) {
        if (category.getChildren() != null) {
            for (Category c : category.getChildren()) {
                if (childName.equals(c.getName())) {
                    return c;
                }
            }
        }
        return null;
    }

    /**
     * Find category child.
     *
     * @param category category
     * @param child child category to find
     * @return child category
     */
    private Category findChild(Category category, DtoCategory child) {
        if (category.getChildren() != null) {
            for (Category c : category.getChildren()) {
                if (child.getName() != null) {
                    if (child.getName().equals(c.getName())) {
                        return c;
                    }
                } else {
                    if (child.getId().equals(c.getID())) {
                        return c;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Validate category update.
     *
     * @param categoryUpdate category update to validate
     * @param results returned operation results
     * @return valid flag
     */
    private boolean validate(DtoCategory categoryUpdate, DtoOperationResults results) {
        if (categoryUpdate.getEntityTypeName() == null) {
            if (categoryUpdate.getEntityType() == null) {
                results.fail("categoryEntityTypeKey Unknown", "No Category Entity Type Key provided");
                return false;
            }
            if (categoryUpdate.getEntityType().getId() == null && categoryUpdate.getEntityType().getName() == null) {
                results.fail("categoryEntityTypeKey Unknown", "No Category Entity Type Key provided");
                return false;
            }
        }
        if (categoryUpdate.getEntities() != null) {
            for (DtoCategoryEntity entity : categoryUpdate.getEntities()) {
                if (entity.getEntityTypeId() == null && entity.getEntityTypeName() == null) {
                    if (entity.getEntityType() == null) {
                        results.fail("categoryEntityEntityType Unknown", "No Category Entity Entity Type provided");
                        return false;
                    }
                    if (entity.getEntityType().getId() == null && entity.getEntityType().getName() == null) {
                        results.fail("categoryEntityEntityTypeKey Unknown", "No Category Entity Entity Type Key provided");
                        return false;
                    }
                }
            }
        }
        if (categoryUpdate.getParents() != null) {
            for (DtoCategory parent : categoryUpdate.getParents()) {
                if (parent.getName() == null && parent.getId() == null) {
                    results.fail("parent Unknown", "No Parent Category name provided");
                    return false;
                }
            }
        }
        if (categoryUpdate.getChildren() != null) {
            for (DtoCategory child : categoryUpdate.getChildren()) {
                if (child.getName() == null && child.getId() == null) {
                    results.fail("child Unknown", "No Child Category name provided");
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Lookup entity type from category update.
     *
     * @param metadataService metadata service
     * @param dtoCategoryUpdate category update
     * @return entity type or null
     */
    private static EntityType lookupUpdateEntityType(MetadataService metadataService, DtoCategoryUpdate dtoCategoryUpdate) {
        String entityTypeName = dtoCategoryUpdate.getEntityTypeName();
        if ((entityTypeName != null) && !entityTypeName.isEmpty()) {
            return metadataService.getEntityTypeByName(entityTypeName);
        }
        if ((dtoCategoryUpdate.getEntityType() != null) && (dtoCategoryUpdate.getEntityType().getId() != null)) {
            return metadataService.getEntityTypeById(dtoCategoryUpdate.getEntityType().getId());
        }
        return null;
    }

    /**
     * Lookup application type from category update.
     *
     * @param metadataService metadata service
     * @param dtoCategoryUpdate category update
     * @return application type or null
     */
    private static ApplicationType lookupUpdateApplicationType(MetadataService metadataService,
                                                               DtoCategoryUpdate dtoCategoryUpdate) {
        String appType = dtoCategoryUpdate.getAppType();
        if ((appType != null) && !appType.isEmpty()) {
            return metadataService.getApplicationTypeByName(appType);
        }
        if ((dtoCategoryUpdate.getApplicationType() != null) && (dtoCategoryUpdate.getApplicationType().getId() != null)) {
            return metadataService.getApplicationTypeById(dtoCategoryUpdate.getApplicationType().getId());
        }
        return null;
    }

    /**
     * Lookup category entity type name from category update.
     *
     * @param metadataService metadata service
     * @param dtoCategoryUpdate category update
     * @return category entity type name or null
     */
    private static String lookupUpdateEntityTypeName(MetadataService metadataService, DtoCategoryUpdate dtoCategoryUpdate) {
        String entityTypeName = dtoCategoryUpdate.getEntityTypeName();
        if ((entityTypeName != null) && !entityTypeName.isEmpty()) {
            return entityTypeName;
        }
        if ((dtoCategoryUpdate.getEntityType() != null) && (dtoCategoryUpdate.getEntityType().getId() != null)) {
            EntityType entityType = metadataService.getEntityTypeById(dtoCategoryUpdate.getEntityType().getId());
            if (entityType != null) {
                return entityType.getName();
            }
        }
        return null;
    }

    /**
     * Lookup category from category update.
     *
     * @param categoryService category service
     * @param metadataService metadata service
     * @param dtoCategoryUpdate category update
     * @return category or null
     */
    private static Category lookupUpdateCategory(CategoryService categoryService, MetadataService metadataService,
                                                 DtoCategoryUpdate dtoCategoryUpdate) {
        String entityTypeName = lookupUpdateEntityTypeName(metadataService, dtoCategoryUpdate);
        String categoryName = dtoCategoryUpdate.getCategoryName();
        if ((categoryName != null) && !categoryName.isEmpty() && (entityTypeName != null) && !entityTypeName.isEmpty()) {
            return categoryService.getCategoryByName(categoryName, entityTypeName);
        }
        if ((dtoCategoryUpdate.getCategory() != null) && (dtoCategoryUpdate.getCategory().getId() != null)) {
            return categoryService.getCategoryById(dtoCategoryUpdate.getCategory().getId());
        }
        return null;
    }

    /**
     * Lookup parent category from category update.
     *
     * @param categoryService category service
     * @param metadataService metadata service
     * @param dtoCategoryUpdate category update
     * @return parent category or null
     */
    private static Category lookupUpdateParent(CategoryService categoryService, MetadataService metadataService,
                                               DtoCategoryUpdate dtoCategoryUpdate) {
        String entityTypeName = lookupUpdateEntityTypeName(metadataService, dtoCategoryUpdate);
        String parentName = dtoCategoryUpdate.getParentName();
        if ((parentName != null) && !parentName.isEmpty() && (entityTypeName != null) && !entityTypeName.isEmpty()) {
            return categoryService.getCategoryByName(parentName, entityTypeName);
        }
        if ((dtoCategoryUpdate.getParent() != null) && (dtoCategoryUpdate.getParent().getId() != null)) {
            return categoryService.getCategoryById(dtoCategoryUpdate.getParent().getId());
        }
        return null;
    }

    /**
     * Lookup other categories from category update.
     *
     * @param categoryService category service
     * @param metadataService metadata service
     * @param dtoCategoryUpdate category update
     * @return other categories collection or null
     */
    private static List<Category> lookupOtherCategories(CategoryService categoryService, MetadataService metadataService,
                                                        DtoCategoryUpdate dtoCategoryUpdate) {
        String entityTypeName = lookupUpdateEntityTypeName(metadataService, dtoCategoryUpdate);
        List<String> otherCategoryNames = dtoCategoryUpdate.getOtherCategoryNames();
        if ((otherCategoryNames != null) && !otherCategoryNames.isEmpty() && (entityTypeName != null) &&
                !entityTypeName.isEmpty()) {
            List<Category> otherCategories = new ArrayList<Category>();
            for (String otherCategoryName : otherCategoryNames) {
                otherCategories.add(categoryService.getCategoryByName(otherCategoryName, entityTypeName));
            }
            return otherCategories;
        }
        if ((dtoCategoryUpdate.getOtherCategories() != null) && !dtoCategoryUpdate.getOtherCategories().isEmpty()) {
            List<Category> otherCategories = new ArrayList<Category>();
            for (DtoCategory otherCategory : dtoCategoryUpdate.getOtherCategories()) {
                if ((otherCategory.getName() != null) && !otherCategory.getName().isEmpty()) {
                    String otherCategoryEntityTypeName = entityTypeName;
                    if ((otherCategory.getEntityTypeName() != null) && !otherCategory.getEntityTypeName().isEmpty()) {
                        entityTypeName = otherCategory.getEntityTypeName();
                    } else if ((otherCategory.getEntityType() != null) &&
                            (otherCategory.getEntityType().getName() != null) &&
                            !otherCategory.getEntityType().getName().isEmpty()) {
                        entityTypeName = otherCategory.getEntityType().getName();
                    }
                    if ((entityTypeName != null) && !entityTypeName.isEmpty()) {
                        otherCategories.add(categoryService.getCategoryByName(otherCategory.getName(), entityTypeName));
                    }
                }
                if (otherCategory.getId() != null) {
                    otherCategories.add(categoryService.getCategoryById(otherCategory.getId()));
                }
                otherCategories.add(null);
            }
            return otherCategories;
        }
        return null;
    }
}
