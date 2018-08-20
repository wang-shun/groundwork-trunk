/*
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2007  GroundWork Open Source Solutions info@groundworkopensource.com

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
package org.groundwork.foundation.bs.category;

import com.groundwork.collage.model.ApplicationType;
import com.groundwork.collage.model.Category;
import com.groundwork.collage.model.CategoryEntity;
import com.groundwork.collage.model.EntityType;
import com.groundwork.collage.util.Autocomplete;
import com.groundwork.collage.util.AutocompleteName;
import com.groundwork.collage.util.AutocompleteNames;
import com.groundwork.collage.util.AutocompleteNamesIterator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.bs.EntityBusinessServiceImpl;
import org.groundwork.foundation.bs.exception.BusinessServiceException;
import org.groundwork.foundation.bs.metadata.MetadataService;
import org.groundwork.foundation.dao.FilterCriteria;
import org.groundwork.foundation.dao.FoundationDAO;
import org.groundwork.foundation.dao.FoundationQueryList;
import org.groundwork.foundation.dao.SortCriteria;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * LogMessage Service Implementation Class
 * 
 */
public class CategoryServiceImpl extends EntityBusinessServiceImpl implements
        CategoryService, AutocompleteNames {

    /** Spring bean interface id */
    private static final String INTERFACE_NAME = "com.groundwork.collage.model.Category";
    private static final String INTERFACE_CATEGORY_ENTITY = "com.groundwork.collage.model.CategoryEntity";

    /** Hibernate component name that this entity service using */
    private static final String COMPONENT_NAME = "com.groundwork.collage.model.impl.Category";

    /** Default Sort Criteria */
    private static final SortCriteria DEFAULT_SORT_CRITERIA = SortCriteria.asc(Category.HP_NAME);

    /** Enable Logging **/
    private static Log log = LogFactory.getLog(CategoryServiceImpl.class);

    private MetadataService metadataService = null;
    private Autocomplete customGroupAutocompleteService = null;
    private Autocomplete serviceGroupAutocompleteService = null;

    public CategoryServiceImpl(FoundationDAO foundationDAO, MetadataService metadataService) {
        super(foundationDAO, INTERFACE_NAME, COMPONENT_NAME);
        this.metadataService = metadataService;
    }

    @Override
    public FoundationQueryList getCategories(FilterCriteria filter, SortCriteria sortCriteria, int firstResult,
                                             int maxResults) throws BusinessServiceException {

        return query(filter, ((sortCriteria != null) ? sortCriteria : DEFAULT_SORT_CRITERIA), firstResult, maxResults);
    }

    @Override
    public List<Category> getCategoriesByCategoryId(int categoryId, String entityTypeName) throws BusinessServiceException {
        // validate query
        if ((categoryId <= 0) || (entityTypeName == null) || entityTypeName.isEmpty()) {
            throw new IllegalArgumentException("Category id or entity type name invalid or missing.");
        }
        // query for category entities in category
        return _foundationDAO.query("select c from Category c, CategoryEntity ce where " +
                "ce." + Category.HP_ENTITIES_CATEGORY_ID.substring(Category.HP_ENTITIES.length() + 1) + " = ? and " +
                "ce." + Category.HP_ENTITIES_ID.substring(Category.HP_ENTITIES.length() + 1) + " = c." + Category.HP_ID + " and " +
                "ce." + Category.HP_ENTITIES_TYPE_NAME.substring(Category.HP_ENTITIES.length() + 1) + " = '" + entityTypeName + "' " +
                "order by c." + Category.HP_NAME + " asc", categoryId);
    }

    @Override
    public Category getCategoryByName(String name, String entityTypeName) throws BusinessServiceException {

        if ((name == null) || name.isEmpty() || (entityTypeName == null) || entityTypeName.isEmpty()) {
            throw new IllegalArgumentException("A valid category and entity type name must be provided.");
        }
        FilterCriteria filter = FilterCriteria.eq(Category.HP_NAME, name);
        filter.and(FilterCriteria.eq(Category.HP_ENTITY_TYPE_NAME, entityTypeName));
        List<Category> results = query(filter, null);
        return (((results != null) && !results.isEmpty()) ? results.get(0) : null);
    }

    @Override
    public Category getCategoryById(int categoryId) throws BusinessServiceException {

        if (categoryId < 1) {
            throw new IllegalArgumentException( "A valid categoryId must be provided.");
        }
        return (Category)queryById(categoryId);
    }

    @Override
    public Collection<Category> getCategoriesByEntityType(String entityTypeName) throws BusinessServiceException {

        if ((entityTypeName == null) || entityTypeName.isEmpty()) {
            throw new IllegalArgumentException("A valid entity type name must be provided.");
        }
        FilterCriteria filter = FilterCriteria.eq(Category.HP_ENTITY_TYPE_NAME, entityTypeName);
        Collection<Category> categories = query(filter, null);
        return categories;
    }

    @Override
    public Collection<Category> getRootCategories(String entityTypeName) throws BusinessServiceException {

        if ((entityTypeName == null) || entityTypeName.isEmpty()) {
            throw new IllegalArgumentException("A valid entity type name must be provided.");
        }
        FilterCriteria filter = FilterCriteria.eq(Category.HP_ROOT, Boolean.TRUE);
        filter.and(FilterCriteria.eq(Category.HP_ENTITY_TYPE_NAME, entityTypeName));
        Collection<Category> categories = query(filter, null);
        return categories;
    }

    @Override
    public void deleteCategoryByName(String name, String entityTypeName) throws BusinessServiceException {

        if ((name == null) || name.isEmpty() || (entityTypeName == null) || entityTypeName.isEmpty()) {
            throw new IllegalArgumentException("A valid category and entity type name must be provided.");
        }
        Category category = getCategoryByName(name, entityTypeName);
        delete(category);
        // refresh autocomplete since name deleted
        refreshAutocompleteOnTransactionCommit();
    }

    @Override
    public void deleteCategoryById(int categoryId) throws BusinessServiceException {

        if (categoryId < 1) {
            throw new IllegalArgumentException("Invalid categoryId.");
        }
        delete(categoryId);
        // refresh autocomplete since name deleted
        refreshAutocompleteOnTransactionCommit();
    }

    @Override
    public void deleteCategoryEntity(CategoryEntity categoryEntity) throws BusinessServiceException {

        if (categoryEntity == null) {
            throw new IllegalArgumentException("CategoryEntity must not be null.");
        }
        delete(categoryEntity);
        // refresh autocomplete since name deleted
        refreshAutocompleteOnTransactionCommit();
    }

    @Override
    public void saveCategory(Category category) throws BusinessServiceException {

        if (category == null) {
            throw new IllegalArgumentException("Category is null.");
        }
        save(category);
        // refresh autocomplete since name changed
        refreshAutocompleteOnTransactionCommit();
    }

    @Override
    public void saveCategories(Collection<Category> categories) throws BusinessServiceException {

        if ((categories == null) || categories.isEmpty()) {
            throw new IllegalArgumentException("No categories specified to save.");
        }
        save(categories);
        // refresh autocomplete since name changed
        refreshAutocompleteOnTransactionCommit();
    }

    @Override
    public void saveCategoryEntity(CategoryEntity categoryEntity) throws BusinessServiceException {

        if (categoryEntity == null) {
            throw new IllegalArgumentException("CategoryEntity is null.");
        }
        save(categoryEntity);
        // refresh autocomplete since name changed
        refreshAutocompleteOnTransactionCommit();
    }

    @Override
    public Category createCategory(String name, String description, EntityType entityType) throws BusinessServiceException {

        return createCategory(name, description, entityType, null, null);
    }

    @Override
    public Category createCategory(String name, String description, EntityType entityType,
                                   ApplicationType applicationType, String agentId) throws BusinessServiceException {

        if ((name == null) || name.isEmpty() || (entityType == null)) {
            throw new IllegalArgumentException("Category name must not be empty or null and entity type must be specified.");
        }
        Category category = (Category) create();
        category.setName(name);
        if ((description != null) && !description.isEmpty()) {
            category.setDescription(description);
        }
        category.setEntityType(entityType);
        if (applicationType != null) {
            category.setApplicationType(applicationType);
        }
        if ((agentId != null) && !agentId.isEmpty()) {
            category.setAgentId(agentId);
        }
        return category;
    }

    @Override
    public CategoryEntity createCategoryEntity() throws BusinessServiceException {

        return (CategoryEntity) create(INTERFACE_CATEGORY_ENTITY);
    }

    @Override
    public Collection<Category> deleteCategoryEntityByObjectID(int objectID, String entityTypeName)
            throws BusinessServiceException {

        if ((objectID < 0) || (entityTypeName == null) || entityTypeName.isEmpty()) {
            throw new BusinessServiceException("Invalid ObjectID or EntityType name");
        }
        // query for categories with specified category entity references
        FilterCriteria criteria = FilterCriteria.eq(Category.HP_ENTITIES_ID, new Integer(objectID));
        criteria.and(FilterCriteria.eq(Category.HP_ENTITIES_TYPE_NAME, entityTypeName));
        Collection<Category> categories = query(criteria, null);
        // remove and delete category entity references
        for (Category category : categories) {
            for (Iterator<CategoryEntity> categoryEntitiesIter = category.getCategoryEntities().iterator(); categoryEntitiesIter.hasNext();) {
                CategoryEntity categoryEntity = categoryEntitiesIter.next();
                if ((categoryEntity.getObjectID().intValue() == objectID) &&
                        categoryEntity.getEntityType().getName().equals(entityTypeName)) {
                    categoryEntitiesIter.remove();
                    delete(categoryEntity);
                }
            }
        }
        save(categories);
        // refresh autocomplete since names changed
        refreshAutocompleteOnTransactionCommit();
        // return effected categories
        return categories;
    }

    @Override
    public FoundationQueryList queryCategories(String hql, String hqlCount, int firstResult, int maxResults) {

        FoundationQueryList list = _foundationDAO.queryWithPaging(hql, hqlCount, firstResult, maxResults);
        return list;
    }

    @Override
    public Collection<Category> getHierarchyCategories(Category category) {
        // validate query
        if (category == null) {
            throw new IllegalArgumentException("Category argument missing");
        }
        // query deep for hierarchy children using ancestors
        return _foundationDAO.query("select c from Category c where " +
                "c = ? or " +
                "? in elements(c." + Category.HP_ANCESTORS + ") " +
                "order by c." + Category.HP_NAME + " asc", new Object[]{category, category});
    }

    @Override
    public Collection<Category> getEntityCategoriesByObjectId(EntityType entityType, int entityObjectID,
                                                              EntityType entityEntityType) {
        // validate query
        if ((entityType == null) || (entityObjectID <= 0) || (entityEntityType == null)) {
            throw new IllegalArgumentException("Entity type, entity object id, or entity entity type arguments missing");
        }
        // query for categories with entity reference
        return _foundationDAO.query("select c from Category c, CategoryEntity ce where " +
                "c." + Category.HP_ENTITY_TYPE + " = ? and " +
                "ce in elements(c." + Category.HP_ENTITIES + ") and " +
                "ce." + Category.HP_ENTITIES_ID.substring(Category.HP_ENTITIES.length()+1) + " = ? and " +
                "ce." + Category.HP_ENTITIES_TYPE.substring(Category.HP_ENTITIES.length()+1) + " = ? " +
                "order by c." + Category.HP_NAME + " asc", new Object[]{entityType, entityObjectID, entityEntityType});
    }

    @Override
    public Collection<Category> getEntityRootCategoriesByObjectId(EntityType entityType, int entityObjectID,
                                                                  EntityType entityEntityType) {
        // validate query
        if ((entityType == null) || (entityObjectID <= 0) || (entityEntityType == null)) {
            throw new IllegalArgumentException("Entity type, entity object id, or entity entity type arguments missing");
        }
        // query for root ancestors of categories with entity reference
        return _foundationDAO.query("select rc from Category rc, Category c, CategoryEntity ce where " +
                "c." + Category.HP_ENTITY_TYPE + " = ? and " +
                "ce in elements(c." + Category.HP_ENTITIES + ") and " +
                "ce." + Category.HP_ENTITIES_ID.substring(Category.HP_ENTITIES.length()+1) + " = ? and " +
                "ce." + Category.HP_ENTITIES_TYPE.substring(Category.HP_ENTITIES.length()+1) + " = ? and " +
                "(rc = c or rc in elements(c." + Category.HP_ANCESTORS + ")) and " +
                "rc." + Category.HP_ROOT + " = true " +
                "order by rc." + Category.HP_NAME + " asc", new Object[]{entityType, entityObjectID, entityEntityType});
    }

    @Override
    public Results deleteCategory(Category category, boolean childrenOnly, Delete delete)
            throws BusinessServiceException {
        // validate delete
        if ((category == null) || (delete == null)) {
            throw new IllegalArgumentException("Category or delete operation arguments missing");
        }
        switch (delete) {
            case LEAF_ONLY:
                if ((category.getChildren() != null) && !category.getChildren().isEmpty()) {
                    throw new BusinessServiceException("Attempt to delete category with children");
                }
                break;
        }
        // delete categories based on delete operation
        Set<Category> deletedCategories = new HashSet<Category>();
        deletedCategories.add(category);
        Set<Category> modifiedCategories = new HashSet<Category>();
        switch (delete) {
            case LEAF_ONLY:
                break;
            case CASCADE:
                cascadeCategories(category, false, deletedCategories);
                break;
            case CASCADE_ALL:
                cascadeCategories(category, true, deletedCategories);
                break;
            case ORPHAN_CHILDREN_AS_ROOTS:
                if ((category.getChildren() != null) && !category.getChildren().isEmpty()) {
                    for (Category child : category.getChildren()) {
                        if ((child.getParents() == null) || !child.getParents().contains(category)) {
                            throw new BusinessServiceException("Hierarchy integrity failure detected");
                        }
                        child.setRoot(Boolean.TRUE);
                    }
                    modifiedCategories.addAll(category.getChildren());
                }
                break;
            case ADD_CHILDREN_TO_PARENTS:
                if ((category.getChildren() != null) && !category.getChildren().isEmpty() &&
                        (category.getParents() != null) && !category.getParents().isEmpty()) {
                    for (Category child : category.getChildren()) {
                        if ((child.getParents() == null) || !child.getParents().contains(category)) {
                            throw new BusinessServiceException("Hierarchy integrity failure detected");
                        }
                        for (Category parent : category.getParents()) {
                            if ((parent.getChildren() == null) || !parent.getChildren().contains(category)) {
                                throw new BusinessServiceException("Hierarchy integrity failure detected");
                            }
                            child.getParents().add(parent);
                            parent.getChildren().add(child);
                        }
                    }
                    modifiedCategories.addAll(category.getChildren());
                    modifiedCategories.addAll(category.getParents());
                }
                break;
        }
        for (Category deletedCategory : deletedCategories) {
            unlinkCategory(deletedCategory, modifiedCategories);
        }
        modifiedCategories.removeAll(deletedCategories);
        // update ancestors on modified categories
        updateAncestors(modifiedCategories);
        // persist deleted and modified categories
        delete(deletedCategories);
        save(modifiedCategories);
        // refresh autocomplete since names deleted and/or changed
        refreshAutocompleteOnTransactionCommit();
        // return results
        Results results = new Results();
        results.entityType = category.getEntityType();
        results.deleted = deletedCategories;
        results.modified = modifiedCategories;
        return results;
    }

    /**
     * Unlink all category parents and children.
     *
     * @param category category to unlink
     * @param modifiedCategories returned set of modified categories
     * @throws BusinessServiceException
     */
    private static void unlinkCategory(Category category, Set<Category> modifiedCategories)
            throws BusinessServiceException {
        unlinkCategoryParents(category, null, modifiedCategories);
        unlinkCategoryChildren(category, null, modifiedCategories);
    }

    /**
     * Unlink category parents.
     *
     * @param category category to unlink
     * @param parents collection of parents to unlink or null for all
     * @param modifiedCategories returned set of modified categories
     * @return unlinked parents
     * @throws BusinessServiceException
     */
    private static Collection<Category> unlinkCategoryParents(Category category, Collection<Category> parents,
                                                              Set<Category> modifiedCategories)
            throws BusinessServiceException {
        if ((category.getParents() != null) && !category.getParents().isEmpty()) {
            Collection<Category> unlinkedParents = new ArrayList<Category>();
            if (parents == null) {
                parents = new ArrayList<Category>(category.getParents());
            }
            for (Category parent : parents) {
                if (category.getParents().remove(parent)) {
                    if ((parent.getChildren() == null) || !parent.getChildren().remove(category)) {
                        throw new BusinessServiceException("Hierarchy integrity failure detected");
                    }
                    unlinkedParents.add(parent);
                    modifiedCategories.add(parent);
                }
            }
            return unlinkedParents;
        }
        return Collections.EMPTY_LIST;
    }

    /**
     * Unlink category children.
     *
     * @param category category to unlink
     * @param children collection of children to unlink or null for all
     * @param modifiedCategories returned set of modified categories
     * @return unlinked children
     * @throws BusinessServiceException
     */
    private static Collection<Category> unlinkCategoryChildren(Category category, Collection<Category> children,
                                                               Set<Category> modifiedCategories)
            throws BusinessServiceException {
        if ((category.getChildren() != null) && !category.getChildren().isEmpty()) {
            Collection<Category> unlinkedChildren = new ArrayList<Category>();
            if (children == null) {
                children = new ArrayList<Category>(category.getChildren());
            }
            for (Category child : children) {
                if (category.getChildren().remove(child)) {
                    if ((child.getParents() == null) || !child.getParents().remove(category)) {
                        throw new BusinessServiceException("Hierarchy integrity failure detected");
                    }
                    unlinkedChildren.add(child);
                    modifiedCategories.add(child);
                }
            }
            return unlinkedChildren;
        }
        return Collections.EMPTY_LIST;
    }

    /**
     * Cascade category children. Optionally traverses shared, (categories
     * with multiple parents), and root categories.
     *
     * @param category category to cascade
     * @param cascadeAll traverse shared and root categories
     * @param categories returned cascaded categories collection
     * @throws BusinessServiceException
     */
    private static void cascadeCategories(Category category, boolean cascadeAll, Set<Category> categories)
            throws BusinessServiceException {
        // cascade traversing recursively depth first through children
        if ((category.getChildren() != null) && !category.getChildren().isEmpty()) {
            for (Category child : category.getChildren()) {
                if ((child.getParents() == null) || !child.getParents().contains(category)) {
                    throw new BusinessServiceException("Hierarchy integrity failure detected");
                }
                // cascade if forced or is not root and all parents in hierarchy
                if (cascadeAll || (((child.isRoot() == null) || !child.isRoot())) &&
                        ((child.getParents().size() == 1) || categories.containsAll(child.getParents()))) {
                    if (categories.add(child)) {
                        cascadeCategories(child, cascadeAll, categories);
                    }
                }
            }
        }
    }

    /**
     * Update ancestors of modified categories and their cascaded children.
     *
     * @param modifiedCategories initial and returned set of modified categories
     */
    private static void updateAncestors(Set<Category> modifiedCategories) {
        if (!modifiedCategories.isEmpty()) {
            // compute ancestor updates as full hierarchies from modified categories
            Set<Category> ancestorUpdates = new HashSet<Category>();
            for (Category modifiedCategory : modifiedCategories) {
                ancestorUpdates.add(modifiedCategory);
                cascadeCategories(modifiedCategory, true, ancestorUpdates);
            }
            // update all ancestors for ancestor update categories; requires
            // multiple passes as parents are updated through hierarchy, (ancestor
            // updates are not sorted in parent/ancestor order)
            do {
                boolean updated = false;
                for (Iterator<Category> ancestorUpdatesIter = ancestorUpdates.iterator(); ancestorUpdatesIter.hasNext(); ) {
                    // update category ancestors
                    Category ancestorUpdateCategory = ancestorUpdatesIter.next();
                    // determine if category parent's ancestors are updated
                    boolean parentsAncestorsUpdated = true;
                    if ((ancestorUpdateCategory.getParents() != null) && !ancestorUpdateCategory.getParents().isEmpty()) {
                        for (Category parent : ancestorUpdateCategory.getParents()) {
                            if (ancestorUpdates.contains(parent)) {
                                parentsAncestorsUpdated = false;
                                break;
                            }
                        }
                    }
                    if (!parentsAncestorsUpdated) {
                        continue;
                    }
                    // compute ancestors from parent's updated ancestors
                    Set<Category> ancestors = new HashSet<Category>();
                    if ((ancestorUpdateCategory.getParents() != null) && !ancestorUpdateCategory.getParents().isEmpty()) {
                        for (Category parent : ancestorUpdateCategory.getParents()) {
                            if ((parent.getAncestors() != null) && !parent.getAncestors().isEmpty()) {
                                ancestors.addAll(parent.getAncestors());
                            }
                        }
                        ancestors.addAll(ancestorUpdateCategory.getParents());
                    }
                    // update ancestors and root
                    boolean ancestorsModified = false;
                    if (!ancestors.isEmpty()) {
                        if (ancestorUpdateCategory.getAncestors() != null) {
                            if (!ancestorUpdateCategory.getAncestors().equals(ancestors)) {
                                setCollectionCategories(ancestorUpdateCategory.getAncestors(), ancestors);
                                ancestorsModified = true;
                            }
                        } else {
                            ancestorUpdateCategory.setAncestors(ancestors);
                            ancestorsModified = true;
                        }
                    } else {
                        if ((ancestorUpdateCategory.getAncestors() != null) &&
                                !ancestorUpdateCategory.getAncestors().isEmpty()) {
                            ancestorUpdateCategory.getAncestors().clear();
                            ancestorUpdateCategory.setRoot(Boolean.TRUE);
                            ancestorsModified = true;
                        }
                    }
                    if (ancestorsModified) {
                        modifiedCategories.add(ancestorUpdateCategory);
                    }
                    // updated
                    ancestorUpdatesIter.remove();
                    updated = true;
                }
                if (!updated) {
                    throw new BusinessServiceException("Hierarchy integrity failure detected");
                }
            } while (!ancestorUpdates.isEmpty());
        }
    }

    /**
     * Modify collection of categories to match specified categories.
     *
     * @param collection collection of categories to modify
     * @param categories categories to set
     */
    private static void setCollectionCategories(Collection<Category> collection, Collection<Category> categories) {
        for (Iterator<Category> collectionIter = collection.iterator(); collectionIter.hasNext();) {
            Category category = collectionIter.next();
            if (!categories.contains(category)) {
                collectionIter.remove();
            }
        }
        for (Category category : categories) {
            if (!collection.contains(category)) {
                collection.add(category);
            }
        }
    }

    @Override
    public Results createAndSaveCategory(String name, EntityType entityType, String description,
                                         ApplicationType applicationType, String agentId, Category parent, Create create)
            throws BusinessServiceException {
        // validate create
        if ((name == null) || (entityType == null) || (create == null)) {
            throw new IllegalArgumentException("Name, entity type, or create operation arguments missing");
        }
        switch (create) {
            case AS_CHILD:
            case AS_CHILD_WITH_PARENT_CHILDREN:
                if (parent == null) {
                    throw new IllegalArgumentException("Parent argument missing");
                }
                if (!entityType.equals(parent.getEntityType())) {
                    throw new IllegalArgumentException("Parent argument entity type mismatch");
                }
                break;
        }
        // create new category
        Category createdCategory = createCategory(name, description, entityType, applicationType, agentId);
        // create category based on create operation
        Set<Category> modifiedCategories = new HashSet<Category>();
        modifiedCategories.add(createdCategory);
        switch (create) {
            case AS_ROOT:
                createdCategory.setRoot(Boolean.TRUE);
                break;
            case AS_CHILD_WITH_PARENT_CHILDREN:
                linkCategoryChildren(createdCategory, parent.getChildren(), parent, modifiedCategories);
                unlinkCategoryChildren(parent, null, modifiedCategories);
            case AS_CHILD:
                createdCategory.setRoot(Boolean.FALSE);
                createdCategory.setParents(new HashSet<Category>());
                createdCategory.getParents().add(parent);
                if (parent.getChildren() == null) {
                    parent.setChildren(new HashSet<Category>());
                }
                parent.getChildren().add(createdCategory);
                modifiedCategories.add(parent);
                break;
        }
        // update ancestors on created and modified categories
        updateAncestors(modifiedCategories);
        // persist created and modified categories
        save(modifiedCategories);
        // refresh autocomplete since names changed
        refreshAutocompleteOnTransactionCommit();
        // return results
        modifiedCategories.remove(createdCategory);
        Results results = new Results();
        results.entityType = entityType;
        results.created = createdCategory;
        results.modified = modifiedCategories;
        return results;
    }

    /**
     * Link category children.
     *
     * @param category category to link
     * @param children children to link
     * @param assertParent assert children to link have parent or null
     * @param modifiedCategories returned set of modified categories
     * @throws BusinessServiceException
     */
    private void linkCategoryChildren(Category category, Collection<Category> children, Category assertParent,
                                      Set<Category> modifiedCategories) throws BusinessServiceException {
        if ((children != null) && !children.isEmpty()) {
            if (category.getChildren() != null) {
                category.getChildren().addAll(children);
            } else {
                category.setChildren(new HashSet<Category>(children));
            }
            for (Category child : children) {
                if (assertParent != null) {
                    if ((child.getParents() == null) || !child.getParents().contains(assertParent)) {
                        throw new BusinessServiceException("Hierarchy integrity failure detected");
                    }
                } else if (child.getParents() == null) {
                    child.setParents(new HashSet<Category>());
                }
                child.getParents().add(category);
                modifiedCategories.add(child);
            }
        }
    }

    /**
     * Link category parents.
     *
     * @param category category to link
     * @param parents parents to link
     * @param assertChild assert parents to link have child or null
     * @param modifiedCategories returned set of modified categories
     * @throws BusinessServiceException
     */
    private void linkCategoryParents(Category category, Collection<Category> parents, Category assertChild,
                                     Set<Category> modifiedCategories) throws BusinessServiceException {
        if ((parents != null) && !parents.isEmpty()) {
            if (category.getParents() != null) {
                category.getParents().addAll(parents);
            } else {
                category.setParents(new HashSet<Category>(parents));
            }
            for (Category parent : parents) {
                if (assertChild != null) {
                    if ((parent.getChildren() == null) || !parent.getChildren().contains(assertChild)) {
                        throw new BusinessServiceException("Hierarchy integrity failure detected");
                    }
                } else if (parent.getChildren() == null) {
                    parent.setChildren(new HashSet<Category>());
                }
                parent.getChildren().add(category);
                modifiedCategories.add(parent);
            }
        }
    }

    @Override
    public Results cloneAndSaveCategory(Category category, String name, Clone clone)
            throws BusinessServiceException {
        // validate clone
        if ((category == null) || (name == null) || (clone == null)) {
            throw new IllegalArgumentException("Category, name, or clone operation arguments missing");
        }
        // create cloned category
        Category clonedCategory = createCategory(name, category.getDescription(), category.getEntityType(),
                category.getApplicationType(), category.getAgentId());
        clonedCategory.setRoot(category.isRoot());
        // create category based on create operation
        Set<Category> modifiedCategories = new HashSet<Category>();
        modifiedCategories.add(clonedCategory);
        switch (clone) {
            case AS_ROOT:
                clonedCategory.setRoot(Boolean.TRUE);
                break;
            case AS_ROOT_WITH_CHILDREN:
                clonedCategory.setRoot(Boolean.TRUE);
                linkCategoryChildren(clonedCategory, category.getChildren(), category, modifiedCategories);
                break;
            case AS_LEAF_WITH_PARENTS:
                linkCategoryParents(clonedCategory, category.getParents(), category, modifiedCategories);
                break;
            case WITH_PARENTS_AND_CHILDREN:
                linkCategoryChildren(clonedCategory, category.getChildren(), category, modifiedCategories);
                linkCategoryParents(clonedCategory, category.getParents(), category, modifiedCategories);
                break;
        }
        // update ancestors on cloned and modified categories
        updateAncestors(modifiedCategories);
        // persist cloned and modified categories
        save(modifiedCategories);
        // refresh autocomplete since names changed
        refreshAutocompleteOnTransactionCommit();
        // return results
        modifiedCategories.remove(clonedCategory);
        Results results = new Results();
        results.entityType = category.getEntityType();
        results.created = clonedCategory;
        results.modified = modifiedCategories;
        return results;
    }

    @Override
    public Results modifyCategory(Category category, Collection<Category> otherCategories, Modify modify)
            throws BusinessServiceException {
        // validate modify
        if ((category == null) || (modify == null)) {
            throw new IllegalArgumentException("Category or modify operation arguments missing");
        }
        switch (modify) {
            case UNROOT:
                if ((category.isRoot() != null) && category.isRoot() &&
                        ((category.getParents() == null) || category.getParents().isEmpty())) {
                    throw new IllegalArgumentException("Root category without parents cannot be unrooted");
                }
                break;
            case REMOVE_PARENTS:
            case REMOVE_CHILDREN:
            case MOVE_CHILD:
            case ADD_CHILD:
            case MOVE_PARENT:
            case ADD_PARENT:
            case SWAP_PARENTS:
                if ((otherCategories == null) || otherCategories.isEmpty()) {
                    throw new IllegalArgumentException("Other categories argument missing or empty");
                }
                for (Category otherCategory : otherCategories) {
                    if (otherCategory == null) {
                        throw new IllegalArgumentException("Other category argument null");
                    }
                    if (!category.getEntityType().equals(otherCategory.getEntityType())) {
                        throw new IllegalArgumentException("Other category entity type mismatch");
                    }
                }
                break;
        }
        // modify category based on modify operation
        Set<Category> modifiedCategories = new HashSet<Category>();
        modifiedCategories.add(category);
        switch (modify) {
            case ROOT_REMOVE_PARENTS:
                unlinkCategoryParents(category, null, modifiedCategories);
            case ROOT:
                category.setRoot(Boolean.TRUE);
                break;
            case UNROOT:
                category.setRoot(Boolean.FALSE);
                break;
            case REMOVE_PARENTS:
                unlinkCategoryParents(category, otherCategories, modifiedCategories);
                if ((category.getParents() == null) || category.getParents().isEmpty()) {
                    category.setRoot(Boolean.TRUE);
                }
                break;
            case REMOVE_CHILDREN:
                for (Category removedChild : unlinkCategoryChildren(category, otherCategories, modifiedCategories)) {
                    if ((removedChild.getParents() == null) || removedChild.getParents().isEmpty()) {
                        removedChild.setRoot(Boolean.TRUE);
                    }
                }
                break;
            case MOVE_CHILD:
                unlinkCategoryParents(category, null, modifiedCategories);
            case ADD_PARENTS:
            case ADD_PARENTS_UNROOT:
            case ADD_CHILD:
            case ADD_CHILD_UNROOT:
                linkCategoryParents(category, otherCategories, null, modifiedCategories);
                validateAcyclicHierarchy(category, new HashSet<Category>());
                switch (modify) {
                    case MOVE_CHILD:
                    case ADD_PARENTS_UNROOT:
                    case ADD_CHILD_UNROOT:
                        category.setRoot(Boolean.FALSE);
                        break;
                }
                break;
            case MOVE_PARENT:
                for (Category removedChild : unlinkCategoryChildren(category, null, modifiedCategories)) {
                    if ((removedChild.getParents() == null) || removedChild.getParents().isEmpty()) {
                        removedChild.setRoot(Boolean.TRUE);
                    }
                }
            case ADD_CHILDREN:
            case ADD_CHILDREN_UNROOT:
            case ADD_PARENT:
            case ADD_PARENT_UNROOT:
                linkCategoryChildren(category, otherCategories, null, modifiedCategories);
                validateAcyclicHierarchy(category, new HashSet<Category>());
                switch (modify) {
                    case ADD_CHILDREN_UNROOT:
                    case ADD_PARENT_UNROOT:
                        for (Category addedChild : otherCategories) {
                            addedChild.setRoot(Boolean.FALSE);
                        }
                        break;
                }
                break;
            case SWAP_PARENTS:
                Collection<Category> parents = unlinkCategoryParents(category, null, modifiedCategories);
                Set<Category> otherParents = new HashSet<Category>();
                for (Category otherCategory : otherCategories) {
                    otherParents.addAll(unlinkCategoryParents(otherCategory, null, modifiedCategories));
                    linkCategoryParents(otherCategory, parents, null, modifiedCategories);
                    validateAcyclicHierarchy(otherCategory, new HashSet<Category>());
                    otherCategory.setRoot(category.isRoot());
                }
                linkCategoryParents(category, otherParents, null, modifiedCategories);
                validateAcyclicHierarchy(category, new HashSet<Category>());
                if ((category.getParents() == null) || category.getParents().isEmpty()) {
                    category.setRoot(Boolean.TRUE);
                } else {
                    category.setRoot(Boolean.FALSE);
                }
                break;
        }
        // update ancestors on modified categories
        updateAncestors(modifiedCategories);
        // persist cloned and modified categories
        save(modifiedCategories);
        // refresh autocomplete since names changed
        refreshAutocompleteOnTransactionCommit();
        // return results
        Results results = new Results();
        results.entityType = category.getEntityType();
        results.modified = modifiedCategories;
        return results;
    }

    /**
     * Validate acyclic hierarchy. Because parents and children links are
     * strictly reciprocal, validation need only traverse the hierarchy in
     * one direction to validate. Category hierarchies are directed
     * acyclic graphs, (i.e. DAGs), so no loops can be present.
     *
     * @param category
     * @param categories traversed categories in hierarchy
     * @throws BusinessServiceException
     */
    private void validateAcyclicHierarchy(Category category, Set<Category> categories) throws BusinessServiceException {
        // add traversing category: if category already traversed cycle is detected
        if (!categories.add(category)) {
            throw new BusinessServiceException("Modified category hierarchy includes a cycle");
        }
        // walk hierarchy in single direction, (cycles will be detected
        // traversing hierarchy in either child or parent direction)
        if ((category.getChildren() != null) && !category.getChildren().isEmpty()) {
            for (Category child : category.getChildren()) {
                validateAcyclicHierarchy(child, categories);
            }
        }
        // remove traversed category
        categories.remove(category);
    }

    @Override
    public Results mergeCategoryHierarchies(Collection<Category> rootCategories, Delete orphanDelete)
            throws BusinessServiceException {
        return null; // TODO: NYI
    }

    @Override
    public Iterator<AutocompleteName> openNamesIterator(String namesEntityType) {
        EntityType entityType = metadataService.getEntityTypeByName(namesEntityType);
        if (entityType == null) {
            throw new RuntimeException("Category names entity type not defined: "+namesEntityType);
        }
        return new AutocompleteNamesIterator(getSessionFactory(), "select name from category where entityTypeid = "+entityType.getEntityTypeId());
    }

    @Override
    public void closeNamesIterator(Iterator<AutocompleteName> iterator) {
        ((AutocompleteNamesIterator)iterator).close();
    }

    /**
     * Set autocomplete service, (normally Spring injected).
     *
     * @param customGroupAutocompleteService autocomplete service
     */
    public void setCustomGroupAutocompleteService(Autocomplete customGroupAutocompleteService) {
        this.customGroupAutocompleteService = customGroupAutocompleteService;
    }

    /**
     * Set autocomplete service, (normally Spring injected).
     *
     * @param serviceGroupAutocompleteService autocomplete service
     */
    public void setServiceGroupAutocompleteService(Autocomplete serviceGroupAutocompleteService) {
        this.serviceGroupAutocompleteService = serviceGroupAutocompleteService;
    }

    /**
     * Transaction synchronization to refresh autocomplete on commit.
     */
    private TransactionSynchronization transactionCommitCallback = new TransactionSynchronizationAdapter() {
        @Override
        public void afterCommit() {
            if (customGroupAutocompleteService != null) {
                customGroupAutocompleteService.refresh();
            }
            if (serviceGroupAutocompleteService != null) {
                serviceGroupAutocompleteService.refresh();
            }
        }
    };

    /**
     * Register transaction synchronization to invoke autocomplete
     * refresh on commit of current transaction.
     */
    private void refreshAutocompleteOnTransactionCommit() {
        try {
            List<TransactionSynchronization> synchronizations = TransactionSynchronizationManager.getSynchronizations();
            if (!synchronizations.contains(transactionCommitCallback)) {
                TransactionSynchronizationManager.registerSynchronization(transactionCommitCallback);
            }
        } catch (IllegalStateException ise) {
        }
    }
}
