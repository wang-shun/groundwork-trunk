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
import org.groundwork.foundation.bs.BusinessService;
import org.groundwork.foundation.bs.exception.BusinessServiceException;
import org.groundwork.foundation.dao.FilterCriteria;
import org.groundwork.foundation.dao.FoundationQueryList;
import org.groundwork.foundation.dao.SortCriteria;

import java.util.Collection;
import java.util.List;

public interface CategoryService extends BusinessService
{
	public static final String ENTITY_TYPE_CODE_SERVICEGROUP = "SERVICE_GROUP";
	public static final String ENTITY_TYPE_CODE_SERVICESTATUS = "SERVICE_STATUS";
    public static final String ENTITY_TYPE_CODE_HOST = "HOST";
    public static final String ENTITY_TYPE_CODE_CUSTOMGROUP = "CUSTOM_GROUP";
    public static final String ENTITY_TYPE_CODE_HOSTGROUP = "HOSTGROUP";
    public static final String ENTITY_TYPE_CODE_SERVICECATEGORY = "SERVICE_CATEGORY";
    public static final String ENTITY_TYPE_CODE_HOSTCATEGORY = "HOST_CATEGORY";

	/**
     * Generic method to query for hosts by using criteria. Limit result
     * set via paging parameters.
	 *
	 * @param filter query filter
	 * @param sortCriteria sort criteria or null
     * @param firstResult index of first result or -1
     * @param maxResults maximum number of categories to return or -1
	 * @return categories query result
	 * @throws BusinessServiceException
	 */
	FoundationQueryList getCategories(FilterCriteria filter, SortCriteria sortCriteria, int firstResult, int maxResults) throws BusinessServiceException;

    /**
     * Get all categories belonging to a particular category.
     *
     * @param categoryId category id
     * @return category list
     * @throws BusinessServiceException
     */
    List<Category> getCategoriesByCategoryId(int categoryId, String entityTypeName) throws BusinessServiceException;

    /**
	 * Category lookup by name and entity type.
	 * 
	 * @param name category name
     * @param entityTypeName category entity type name
     * @return category or null
	 * @throws BusinessServiceException
	 */
	Category getCategoryByName(String name, String entityTypeName) throws BusinessServiceException;
	
	/**
	 * Category lookup by id.
     *
	 * @param categoryId category id
	 * @return category or null
	 * @throws BusinessServiceException
	 */
	Category getCategoryById(int categoryId) throws BusinessServiceException;
	
	/**
	 * Root category lookup by entity type.
     *
	 * @return categories collection
	 * @throws BusinessServiceException
	 */
	Collection<Category> getRootCategories(String entityTypeName) throws BusinessServiceException;
	
	/**
	 * Categories lookup by entity type.
     *
	 * @param entityTypeName
	 * @return categories collection
	 * @throws BusinessServiceException
	 */
    Collection<Category> getCategoriesByEntityType(String entityTypeName) throws BusinessServiceException;
	
	/**
	 * Delete category by name and entity type.
     *
	 * @param name category name
     * @param entityTypeName category entity type name
	 * @throws BusinessServiceException
	 */
	void deleteCategoryByName(String name, String entityTypeName) throws BusinessServiceException;
	
	/**
	 * Delete category by id.
     *
	 * @param categoryId category id
	 * @throws BusinessServiceException
	 */
	void deleteCategoryById(int categoryId) throws BusinessServiceException;

    /**
     * Delete category entity.
     *
     * @param categoryEntity delete category entity.
     * @throws BusinessServiceException
     */
	void deleteCategoryEntity(CategoryEntity categoryEntity) throws BusinessServiceException;

	/**
	 * Save category.
     *
	 * @param category category to save
	 * @throws BusinessServiceException
	 */
	void saveCategory(Category category) throws BusinessServiceException;
	
	/**
	 * Save categories.
     *
	 * @param categories categories collection to save
	 * @throws BusinessServiceException
	 */
	void saveCategories(Collection<Category> categories) throws BusinessServiceException;
	
	/**
	 * Save category entity.
	 * 
	 * @param categoryEntity category entity to save
	 * @throws BusinessServiceException
	 */
	void saveCategoryEntity(CategoryEntity categoryEntity)  throws BusinessServiceException;
	
	/**
	 * Create new category.
     *
	 * @param name category name
	 * @param description category description or null
     * @param entityType category entity type
	 * @return new unsaved category
	 * @throws BusinessServiceException
	 */
	Category createCategory(String name, String description, EntityType entityType) throws BusinessServiceException;

    /**
     * Create new category.
     *
     * @param name category name
     * @param description category description or null
     * @param entityType category entity type
     * @param applicationType category application type or null
     * @param agentId category agent id or null
     * @return new unsaved category
     * @throws BusinessServiceException
     */
    Category createCategory(String name, String description, EntityType entityType, ApplicationType applicationType,
                            String agentId) throws BusinessServiceException;

	/**
	 * Create new category entity.
     *
	 * @return new unsaved category entity
	 * @throws BusinessServiceException
	 */
	CategoryEntity createCategoryEntity() throws BusinessServiceException;	
	
	/**
	 * Find and delete all category entities for the given object id and entity type.
     * Returns the collection of effected categories.
     *
	 * @param objectID category entity object id
	 * @param entityTypeName category entity type name
     * @return categories collection
	 * @throws BusinessServiceException
	 */
	Collection<Category> deleteCategoryEntityByObjectID(int objectID, String entityTypeName) throws BusinessServiceException;

    /**
     * Query by HQL String, Limit result set via paging parameters.
     *
     * @param hql HQL query string
     * @param hqlCount HQL count query string
     * @param firstResult index of first result or -1
     * @param maxResults maximum number of categories to return or -1
     * @return categories query result
     * @return a list of host objects matching the query
     */
    FoundationQueryList queryCategories(String hql, String hqlCount, int firstResult, int maxResults);

    /**
     * Get category hierarchy members. Retrieves all children of specified category.
     * Specified category can be any hierarchy member, not just a root category.
     *
     * @param category root category for hierarchy
     * @return hierarchy categories
     */
    Collection<Category> getHierarchyCategories(Category category);

    /**
     * Get categories of specified entity type hierarchies that contain a member
     * category that references the specified entity.
     *
     * @param entityType category entity type
     * @param entityObjectID category entity object id
     * @param entityEntityType category entity entity type
     * @return entity categories
     */
    Collection<Category> getEntityCategoriesByObjectId(EntityType entityType, int entityObjectID,
                                                       EntityType entityEntityType);

    /**
     * Get root categories of specified entity type hierarchies that contain a member
     * category that references the specified entity.
     *
     * @param entityType category entity type
     * @param entityObjectID category entity object id
     * @param entityEntityType category entity entity type
     * @return root entity categories
     */
    Collection<Category> getEntityRootCategoriesByObjectId(EntityType entityType, int entityObjectID,
                                                           EntityType entityEntityType);

    /**
     * Category hierarchy management API results. Results include the entity type of
     * the hierarchy, a created category or null, a collection of the deleted categories
     * or null, and a collection of categories that have been modified or null.
     */
    static class Results {
        public EntityType entityType;
        public Category created;
        public Collection<Category> deleted;
        public Collection<Category> modified;
    }

    /**
     * Delete operation types:
     *
     * LEAF_ONLY - delete category only if leaf
     * CASCADE - delete category and orphaned children deeply
     * CASCADE_ALL - delete category and all children deeply even if shared in other hierarchies
     * ORPHAN_CHILDREN_AS_ROOTS - delete category making orphaned children root categories
     * ADD_CHILDREN_TO_PARENTS - delete category moving all children to parent categories
     */
    enum Delete {LEAF_ONLY, CASCADE, CASCADE_ALL, ORPHAN_CHILDREN_AS_ROOTS, ADD_CHILDREN_TO_PARENTS}

    /**
     * Delete category and/or category children. Deletion is modified by delete
     * operation type.
     *
     * @param category category instance to delete
     * @param childrenOnly children only flag
     * @param delete delete operation type
     * @return delete results
     * @throws BusinessServiceException
     */
    Results deleteCategory(Category category, boolean childrenOnly, Delete delete) throws BusinessServiceException;

    /**
     * Create operation types:
     *
     * AS_ROOT - create category as root category
     * AS_CHILD - create category as child
     * AS_CHILD_WITH_PARENT_CHILDREN - create and insert category taking parent's children
     */
    enum Create {AS_ROOT, AS_CHILD, AS_CHILD_WITH_PARENT_CHILDREN}

    /**
     * Create and save category as a root or child category. Creation is modified by
     * create operation type. Name and entity type must be unique. Parent category must
     * have a matching entity type.
     *
     * @param name category name
     * @param entityType category logical entity type
     * @param description category description or null
     * @param applicationType category application type or null
     * @param agentId category agent id or null
     * @param parent parent category if adding as a child
     * @param create create operation type
     * @return create results
     * @throws BusinessServiceException
     */
    Results createAndSaveCategory(String name, EntityType entityType, String description,
                                  ApplicationType applicationType, String agentId, Category parent, Create create)
            throws BusinessServiceException;

    /**
     * Clone operation type:
     *
     * AS_ROOT - clone category as root without parents or children
     * AS_ROOT_WITH_CHILDREN - clone category as root with children and no parents
     * AS_LEAF_WITH_PARENTS - clone category as leaf with parents and no children
     * WITH_PARENTS_AND_CHILDREN - clone category with parents and children
     */
    enum Clone {AS_ROOT, AS_ROOT_WITH_CHILDREN, AS_LEAF_WITH_PARENTS, WITH_PARENTS_AND_CHILDREN}

    /**
     * Clone and save category. Cloning is modified by clone operation type. Cloned
     * category name and entity type must be unique. The cloned category will have a
     * matching entity type.
     *
     * @param category category to clone
     * @param name cloned category name
     * @param clone clone operation type
     * @return clone results
     * @throws BusinessServiceException
     */
    Results cloneAndSaveCategory(Category category, String name, Clone clone) throws BusinessServiceException;

    /**
     * Modify operation type:
     *
     * ROOT - mark a category as a root category
     * ROOT_REMOVE_PARENTS - remove category parents and mark as a root category
     * UNROOT - unmark a category as root if category is also in another hierarchy
     * ADD_PARENTS - add parent categories to category, (ADD_CHILD synonym)
     * ADD_PARENTS_UNROOT - add parent categories to category and unroot category, (ADD_CHILD_UNROOT synonym)
     * ADD_CHILDREN - add child categories to category, (ADD_PARENT synonym)
     * ADD_CHILDREN_UNROOT - add child categories to category and unroot children, (ADD_PARENT_UNROOT synonym)
     * REMOVE_PARENTS - remove parent categories from category
     * REMOVE_CHILDREN - remove child categories from category
     * MOVE_CHILD - move category as child to parent categories
     * ADD_CHILD - add category as child to parent categories, (ADD_PARENTS synonym)
     * ADD_CHILD_UNROOT - add category as child to parent categories and unroot category, (ADD_PARENTS_UNROOT synonym)
     * MOVE_PARENT - move category as parent to child categories
     * ADD_PARENT - add category as parent to child categories, (ADD_CHILDREN synonym)
     * ADD_PARENT_UNROOT - add category as parent to child categories and unroot children, (ADD_CHILDREN_UNROOT synonym)
     * SWAP_PARENTS - swap parents with category and other categories
     */
    enum Modify {ROOT, ROOT_REMOVE_PARENTS, UNROOT, ADD_PARENTS, ADD_PARENTS_UNROOT, ADD_CHILDREN, ADD_CHILDREN_UNROOT,
        REMOVE_PARENTS, REMOVE_CHILDREN, MOVE_CHILD, ADD_CHILD, ADD_CHILD_UNROOT, MOVE_PARENT, ADD_PARENT,
        ADD_PARENT_UNROOT, SWAP_PARENTS}

    /**
     * Modify category in category hierarchy. Modification is specified with the modify
     * operation type. The category and other categories must have a matching entity type.
     *
     * @param category hierarchy category to modify
     * @param otherCategories other hierarchy categories to modify
     * @param modify modify operation type
     * @return modification results
     * @throws BusinessServiceException
     */
    Results modifyCategory(Category category, Collection<Category> otherCategories, Modify modify)
            throws BusinessServiceException;

    /**
     * Merge category hierarchies. Specified root categories and children are merged
     * with existing persistent categories by name and entity type. Categories
     * orphaned by the merge operation are deleted according to the specified orphan
     * delete operation type. Because the specified categories are generally loaded
     * from external sources, they are assumed to be transient. All categories within
     * a hierarchy must have matching entity types.
     *
     * @param rootCategories category hierarchies to merge
     * @param orphanDelete orphan delete operation type
     * @return merge results
     * @throws BusinessServiceException
     */
    Results mergeCategoryHierarchies(Collection<Category> rootCategories, Delete orphanDelete)
            throws BusinessServiceException;
}
