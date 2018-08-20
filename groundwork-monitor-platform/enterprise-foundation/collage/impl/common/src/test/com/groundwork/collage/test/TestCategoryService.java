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
package com.groundwork.collage.test;


import com.groundwork.collage.model.ApplicationType;
import com.groundwork.collage.model.Category;
import com.groundwork.collage.model.CategoryEntity;
import com.groundwork.collage.model.EntityType;
import com.groundwork.collage.util.Autocomplete;
import com.groundwork.collage.util.AutocompleteName;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.groundwork.foundation.bs.category.CategoryService;
import org.groundwork.foundation.bs.metadata.MetadataService;
import org.groundwork.foundation.dao.FoundationQueryList;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author rdandridge
 *
 */
public class TestCategoryService extends AbstractTestCaseWithTransactionSupport 
{
    /* the following constants should reflect the state of test data */
    protected static final String AGENT_007 = "agent007";
	public static final String CATEGORY_1       = "All Infrastructure";
	public static final String CATEGORY_2       = "Email";

    private CategoryService categoryService = null;
    private MetadataService metadataService = null;
    private Autocomplete customGroupAutocompleteService = null;
    private Autocomplete serviceGroupAutocompleteService = null;

	public TestCategoryService(String x) {
		super(x);
	}

	/** define the tests to be run in this class */
	public static Test suite()
	{
		TestSuite suite = new TestSuite();

		executeScript(false, "testdata/monitor-data.sql");

		// run all tests
		//suite = new TestSuite(TestCategoryService.class);

		// or a subset thereoff
		suite.addTest(new TestCategoryService("testGetCategories"));
		suite.addTest(new TestCategoryService("testCreateCategories"));
		suite.addTest(new TestCategoryService("testNewCategoryFields"));
        suite.addTest(new TestCategoryService("testCategoryHierarchies"));
        suite.addTest(new TestCategoryService("testCategoryHierarchiesEdit"));
        suite.addTest(new TestCategoryService("testAutocomplete"));

		return suite;
	}

    public void setUp() throws Exception
    {
        super.setUp();
		
		// Retrieve category business service
		categoryService = collage.getCategoryService();
        assertNotNull(categoryService);
        metadataService = collage.getMetadataService();
        assertNotNull(metadataService);
        customGroupAutocompleteService = collage.getCustomGroupAutocompleteService();
        assertNotNull(customGroupAutocompleteService);
        serviceGroupAutocompleteService = collage.getServiceGroupAutocompleteService();
        assertNotNull(serviceGroupAutocompleteService);
	}
	
	public void testGetCategories()
	{
		startTime();
		FoundationQueryList categories = categoryService.getCategories(null, null, -1, -1);
		outputElapsedTime("categoryService.getCategories(null, null)");
		assertNotNull(categories);
		assertEquals(categories.size(), 7);
	}
	
	public void testCreateCategories()
	{
        EntityType et1 = metadataService.getEntityTypeByName("HOSTGROUP");
		Category newCategory1 = categoryService.createCategory(CATEGORY_1, "Description " + CATEGORY_1,et1);
		assertNotNull(newCategory1);
		assertTrue("Created Category "   + CATEGORY_1, newCategory1 instanceof Category);
		assertEquals("Category name for  " + CATEGORY_1, CATEGORY_1, newCategory1.getName());

        EntityType et2 = metadataService.getEntityTypeByName("TYPE_RULE");
		Category newCategory2 = categoryService.createCategory(CATEGORY_2, "Description " + CATEGORY_2,et2);
		assertNotNull(newCategory2);
		assertTrue("Created Category "   + CATEGORY_2, newCategory2 instanceof Category);
		assertEquals("Category name for  " + CATEGORY_2, CATEGORY_2, newCategory2.getName());
	}

    public void testNewCategoryFields()
    {
        ApplicationType applicationType = metadataService.getApplicationTypeByName(ApplicationType.SYSTEM_APPLICATION_TYPE_NAME);
        assertNotNull(applicationType);
        EntityType entityType = metadataService.getEntityTypeByName(CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP);
        assertNotNull(entityType);
        Category newCategory = categoryService.createCategory(CATEGORY_2, "Description " + CATEGORY_2, entityType, applicationType, AGENT_007);
        assertNotNull(newCategory);
        categoryService.saveCategory(newCategory);

        Category category = categoryService.getCategoryByName(CATEGORY_2, CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP);
        assert category != null;
        assert category.getAgentId().equals(AGENT_007);
        assert category.getName().equals(CATEGORY_2);
        assert category.getDescription().equals("Description " + CATEGORY_2);
        assert category.getEntityType().getName().equals(CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP);
        assert category.getApplicationType().getName().equals(ApplicationType.SYSTEM_APPLICATION_TYPE_NAME);

        categoryService.deleteCategoryByName(CATEGORY_2, CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP);
        category = categoryService.getCategoryByName(CATEGORY_2, CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP);
        assert category == null;
    }

    public void testCategoryHierarchies() {
        // create hierarchy
        beginTransaction();
        try {
            EntityType entityType = metadataService.getEntityTypeByName(CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP);
            assertNotNull(entityType);
            EntityType entityEntityType = metadataService.getEntityTypeByName(CategoryService.ENTITY_TYPE_CODE_SERVICESTATUS);
            assertNotNull(entityEntityType);
            // create category hierarchy
            CategoryService.Results results = categoryService.createAndSaveCategory("TEST-A", entityType, null, null, null,
                    null, CategoryService.Create.AS_ROOT);
            assertNotNull(results);
            assertEquals(results.entityType, entityType);
            Category testACategory = assertCategory("TEST-A", results.created);
            assertCategoryCollection(new String[]{}, results.modified);
            results = categoryService.createAndSaveCategory("TEST-B", entityType, null, null, null, testACategory,
                    CategoryService.Create.AS_CHILD);
            assertNotNull(results);
            assertEquals(results.entityType, entityType);
            Category testBCategory = assertCategory("TEST-B", results.created);
            assertCategoryCollection(new String[]{"TEST-A"}, results.modified);
            results = categoryService.createAndSaveCategory("TEST-C", entityType, null, null, null, testBCategory,
                    CategoryService.Create.AS_CHILD);
            assertNotNull(results);
            assertEquals(results.entityType, entityType);
            Category testCCategory = assertCategory("TEST-C", results.created);
            assertCategoryCollection(new String[]{"TEST-B"}, results.modified);
            CategoryEntity testCCategoryEntity = categoryService.createCategoryEntity();
            testCCategoryEntity.setEntityType(entityEntityType);
            testCCategoryEntity.setObjectID(1);
            testCCategoryEntity.setCategory(testCCategory);
            testCCategory.setCategoryEntities(new HashSet<CategoryEntity>());
            testCCategory.getCategoryEntities().add(testCCategoryEntity);
            commitTransaction();
        } finally {
            try {
                rollbackTransaction();
            } catch (Exception e) {
            }
        }
        // validate hierarchy
        beginTransaction();
        try {
            EntityType entityType = metadataService.getEntityTypeByName(CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP);
            assertNotNull(entityType);
            EntityType entityEntityType = metadataService.getEntityTypeByName(CategoryService.ENTITY_TYPE_CODE_SERVICESTATUS);
            assertNotNull(entityEntityType);
            // validate category hierarchy structure
            Category testACategory = categoryService.getCategoryByName("TEST-A", CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP);
            assertNotNull(testACategory);
            assertCategoryCollection(new String[]{}, testACategory.getAncestors());
            assertCategoryCollection(new String[]{}, testACategory.getParents());
            assertCategoryCollection(new String[]{"TEST-B"}, testACategory.getChildren());
            assertEquals(Boolean.TRUE, testACategory.isRoot());
            Category testBCategory = categoryService.getCategoryByName("TEST-B", CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP);
            assertNotNull(testBCategory);
            assertCategoryCollection(new String[]{"TEST-A"}, testBCategory.getAncestors());
            assertCategoryCollection(new String[]{"TEST-A"}, testBCategory.getParents());
            assertCategoryCollection(new String[]{"TEST-C"}, testBCategory.getChildren());
            assertEquals(Boolean.FALSE, testBCategory.isRoot());
            Category testCCategory = categoryService.getCategoryByName("TEST-C", CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP);
            assertNotNull(testCCategory);
            assertCategoryCollection(new String[]{"TEST-A", "TEST-B"}, testCCategory.getAncestors());
            assertCategoryCollection(new String[]{"TEST-B"}, testCCategory.getParents());
            assertCategoryCollection(new String[]{}, testCCategory.getChildren());
            assertEquals(Boolean.FALSE, testCCategory.isRoot());
            assertNotNull(testCCategory.getCategoryEntities());
            assertEquals(1, testCCategory.getCategoryEntities().size());
            CategoryEntity testCCategoryEntity = testCCategory.getCategoryEntities().iterator().next();
            assertEquals(entityEntityType, testCCategoryEntity.getEntityType());
            assertEquals(new Integer(1), testCCategoryEntity.getObjectID());
            // query category hierarchies
            Collection<Category> hierarchyCategories = categoryService.getHierarchyCategories(testACategory);
            assertEquals(3, hierarchyCategories.size());
            assertCategoryCollection(new String[]{"TEST-A", "TEST-B", "TEST-C"}, hierarchyCategories);
            hierarchyCategories = categoryService.getHierarchyCategories(testBCategory);
            assertEquals(2, hierarchyCategories.size());
            assertCategoryCollection(new String[]{"TEST-B", "TEST-C"}, hierarchyCategories);
            hierarchyCategories = categoryService.getHierarchyCategories(testCCategory);
            assertEquals(1, hierarchyCategories.size());
            assertCategoryCollection(new String[]{"TEST-C"}, hierarchyCategories);
            // query entity category hierarchies categories
            Collection<Category> categories = categoryService.getEntityCategoriesByObjectId(entityType, 1,
                    entityEntityType);
            assertCategoryCollection(new String[]{"TEST-C"}, categories);
            // query entity category hierarchies root categories
            Collection<Category> rootCategories = categoryService.getEntityRootCategoriesByObjectId(entityType, 1,
                    entityEntityType);
            assertCategoryCollection(new String[]{"TEST-A"}, rootCategories);
            // query category hierarchies root categories
            rootCategories = categoryService.getRootCategories(entityType.getName());
            assertCategoryCollection(new String[]{"TEST-A", "SG1", "SG2"}, rootCategories);
        } finally {
            rollbackTransaction();
        }
        // cleanup hierarchy
        beginTransaction();
        try {
            Category testACategory = categoryService.getCategoryByName("TEST-A", CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP);
            assertNotNull(testACategory);
            CategoryService.Results results = categoryService.deleteCategory(testACategory, false, CategoryService.Delete.CASCADE_ALL);
            assertNotNull(results);
            assertEquals(results.entityType, testACategory.getEntityType());
            assertCategoryCollection(new String[]{"TEST-A", "TEST-B", "TEST-C"}, results.deleted);
            assertCategoryCollection(new String[]{}, results.modified);
            commitTransaction();
        } finally {
            try {
                rollbackTransaction();
            } catch (Exception e) {
            }
        }
        // validate cleanup
        beginTransaction();
        try {
            Category testACategory = categoryService.getCategoryByName("TEST-A", CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP);
            assertNull(testACategory);
            Category testBCategory = categoryService.getCategoryByName("TEST-B", CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP);
            assertNull(testBCategory);
            Category testCCategory = categoryService.getCategoryByName("TEST-C", CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP);
            assertNull(testCCategory);
        } finally {
            rollbackTransaction();
        }
    }

    public void testCategoryHierarchiesEdit() {
        // create/modify hierarchies
        beginTransaction();
        try {
            EntityType entityType = metadataService.getEntityTypeByName(CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP);
            assertNotNull(entityType);
            // create category hierarchy
            CategoryService.Results results = categoryService.createAndSaveCategory("TEST-A", entityType, null, null, null,
                    null, CategoryService.Create.AS_ROOT);
            assertNotNull(results);
            assertEquals(results.entityType, entityType);
            Category testACategory = assertCategory("TEST-A", results.created);
            assertCategoryCollection(new String[]{}, results.modified);
            results = categoryService.createAndSaveCategory("TEST-B", entityType, null, null, null, testACategory,
                    CategoryService.Create.AS_CHILD);
            assertNotNull(results);
            assertEquals(results.entityType, entityType);
            assertCategory("TEST-B", results.created);
            Map<String,Category> testCategories = assertCategoryCollection(new String[]{"TEST-A"}, results.modified);
            testACategory = testCategories.get("TEST-A");
            results = categoryService.createAndSaveCategory("TEST-C", entityType, null, null, null, testACategory,
                    CategoryService.Create.AS_CHILD_WITH_PARENT_CHILDREN);
            assertNotNull(results);
            assertEquals(results.entityType, entityType);
            Category testCCategory = assertCategory("TEST-C", results.created);
            testCategories = assertCategoryCollection(new String[]{"TEST-A", "TEST-B"}, results.modified);
            Category testBCategory = testCategories.get("TEST-B");
            results = categoryService.cloneAndSaveCategory(testBCategory, "TEST-D", CategoryService.Clone.AS_ROOT);
            assertNotNull(results);
            assertEquals(results.entityType, entityType);
            Category testDCategory = assertCategory("TEST-D", results.created);
            assertCategoryCollection(new String[]{}, results.modified);
            results = categoryService.cloneAndSaveCategory(testCCategory, "TEST-E",
                    CategoryService.Clone.WITH_PARENTS_AND_CHILDREN);
            assertNotNull(results);
            assertEquals(results.entityType, entityType);
            assertCategory("TEST-E", results.created);
            testCategories = assertCategoryCollection(new String[]{"TEST-A", "TEST-B"}, results.modified);
            testBCategory = testCategories.get("TEST-B");
            results = categoryService.cloneAndSaveCategory(testBCategory, "TEST-F",
                    CategoryService.Clone.AS_LEAF_WITH_PARENTS);
            assertNotNull(results);
            assertEquals(results.entityType, entityType);
            assertCategory("TEST-F", results.created);
            testCategories = assertCategoryCollection(new String[]{"TEST-C", "TEST-E"}, results.modified);
            testCCategory = testCategories.get("TEST-C");
            Category testECategory = testCategories.get("TEST-E");
            results = categoryService.cloneAndSaveCategory(testCCategory, "TEST-G",
                    CategoryService.Clone.AS_ROOT_WITH_CHILDREN);
            assertNotNull(results);
            assertEquals(results.entityType, entityType);
            Category testGCategory = assertCategory("TEST-G", results.created);
            testCategories = assertCategoryCollection(new String[]{"TEST-B", "TEST-F"}, results.modified);
            Category testFCategory = testCategories.get("TEST-F");
            results = categoryService.modifyCategory(testFCategory,
                    Arrays.asList(new Category[]{testECategory, testGCategory}), CategoryService.Modify.REMOVE_PARENTS);
            assertNotNull(results);
            assertEquals(results.entityType, entityType);
            assertCategoryCollection(new String[]{"TEST-E", "TEST-F", "TEST-G"}, results.modified);
            results = categoryService.modifyCategory(testCCategory, Arrays.asList(new Category[]{testDCategory}),
                    CategoryService.Modify.ADD_CHILD);
            assertNotNull(results);
            assertEquals(results.entityType, entityType);
            assertCategoryCollection(new String[]{"TEST-B", "TEST-C", "TEST-F", "TEST-D"}, results.modified);
            results = categoryService.createAndSaveCategory("TEST-H", entityType, null, null, null, null,
                    CategoryService.Create.AS_ROOT);
            assertNotNull(results);
            assertEquals(results.entityType, entityType);
            Category testHCategory = assertCategory("TEST-H", results.created);
            assertCategoryCollection(new String[]{}, results.modified);
            results = categoryService.createAndSaveCategory("TEST-I", entityType, null, null, null, testHCategory,
                    CategoryService.Create.AS_CHILD);
            assertNotNull(results);
            assertEquals(results.entityType, entityType);
            Category testICategory = assertCategory("TEST-I", results.created);
            assertCategoryCollection(new String[]{"TEST-H"}, results.modified);
            results = categoryService.createAndSaveCategory("TEST-J", entityType, null, null, null, testICategory,
                    CategoryService.Create.AS_CHILD);
            assertNotNull(results);
            assertEquals(results.entityType, entityType);
            Category testJCategory = assertCategory("TEST-J", results.created);
            assertCategoryCollection(new String[]{"TEST-I"}, results.modified);
            results = categoryService.modifyCategory(testJCategory, null, CategoryService.Modify.ROOT);
            assertNotNull(results);
            assertEquals(results.entityType, entityType);
            assertCategoryCollection(new String[]{"TEST-J"}, results.modified);
            results = categoryService.createAndSaveCategory("TEST-K", entityType, null, null, null, null,
                    CategoryService.Create.AS_ROOT);
            assertNotNull(results);
            assertEquals(results.entityType, entityType);
            Category testKCategory = assertCategory("TEST-K", results.created);
            assertCategoryCollection(new String[]{}, results.modified);
            results = categoryService.createAndSaveCategory("TEST-L", entityType, null, null, null, testKCategory,
                    CategoryService.Create.AS_CHILD);
            assertNotNull(results);
            assertEquals(results.entityType, entityType);
            Category testLCategory = assertCategory("TEST-L", results.created);
            testCategories = assertCategoryCollection(new String[]{"TEST-K"}, results.modified);
            testKCategory = testCategories.get("TEST-K");
            results = categoryService.createAndSaveCategory("TEST-M", entityType, null, null, null, testKCategory,
                    CategoryService.Create.AS_CHILD);
            assertNotNull(results);
            assertEquals(results.entityType, entityType);
            Category testMCategory = assertCategory("TEST-M", results.created);
            testCategories = assertCategoryCollection(new String[]{"TEST-K"}, results.modified);
            testKCategory = testCategories.get("TEST-K");
            results = categoryService.createAndSaveCategory("TEST-N", entityType, null, null, null, testKCategory,
                    CategoryService.Create.AS_CHILD);
            assertNotNull(results);
            assertEquals(results.entityType, entityType);
            Category testNCategory = assertCategory("TEST-N", results.created);
            testCategories = assertCategoryCollection(new String[]{"TEST-K"}, results.modified);
            testKCategory = testCategories.get("TEST-K");
            results = categoryService.modifyCategory(testKCategory, Arrays.asList(new Category[]{testMCategory}),
                    CategoryService.Modify.REMOVE_CHILDREN);
            assertNotNull(results);
            assertEquals(results.entityType, entityType);
            testCategories = assertCategoryCollection(new String[]{"TEST-K", "TEST-M"}, results.modified);
            testMCategory = testCategories.get("TEST-M");
            results = categoryService.modifyCategory(testNCategory, null, CategoryService.Modify.ROOT);
            assertNotNull(results);
            assertEquals(results.entityType, entityType);
            testCategories = assertCategoryCollection(new String[]{"TEST-N"}, results.modified);
            testNCategory = testCategories.get("TEST-N");
            results = categoryService.modifyCategory(testNCategory, Arrays.asList(new Category[]{testMCategory}),
                    CategoryService.Modify.MOVE_CHILD);
            assertNotNull(results);
            assertEquals(results.entityType, entityType);
            testCategories = assertCategoryCollection(new String[]{"TEST-K", "TEST-M", "TEST-N"}, results.modified);
            testNCategory = testCategories.get("TEST-N");
            results = categoryService.modifyCategory(testLCategory, Arrays.asList(new Category[]{testNCategory}),
                    CategoryService.Modify.SWAP_PARENTS);
            assertNotNull(results);
            assertEquals(results.entityType, entityType);
            testCategories = assertCategoryCollection(new String[]{"TEST-K", "TEST-L", "TEST-M", "TEST-N"},
                    results.modified);
            testLCategory = testCategories.get("TEST-L");
            testNCategory = testCategories.get("TEST-N");
            results = categoryService.modifyCategory(testLCategory, null, CategoryService.Modify.ROOT_REMOVE_PARENTS);
            assertNotNull(results);
            assertEquals(results.entityType, entityType);
            testCategories = assertCategoryCollection(new String[]{"TEST-L", "TEST-M"}, results.modified);
            testLCategory = testCategories.get("TEST-L");
            testMCategory = testCategories.get("TEST-M");
            results = categoryService.modifyCategory(testNCategory, null, CategoryService.Modify.UNROOT);
            assertNotNull(results);
            assertEquals(results.entityType, entityType);
            assertCategoryCollection(new String[]{"TEST-N"}, results.modified);
            results = categoryService.createAndSaveCategory("TEST-O", entityType, null, null, null, testMCategory,
                    CategoryService.Create.AS_CHILD);
            assertNotNull(results);
            assertEquals(results.entityType, entityType);
            assertCategory("TEST-O", results.created);
            testCategories = assertCategoryCollection(new String[]{"TEST-M"}, results.modified);
            testMCategory = testCategories.get("TEST-M");
            results = categoryService.modifyCategory(testMCategory, Arrays.asList(new Category[]{testLCategory}),
                    CategoryService.Modify.MOVE_PARENT);
            assertNotNull(results);
            assertEquals(results.entityType, entityType);
            testCategories = assertCategoryCollection(new String[]{"TEST-L", "TEST-M", "TEST-O"}, results.modified);
            testMCategory = testCategories.get("TEST-M");
            Category testOCategory = testCategories.get("TEST-O");
            results = categoryService.modifyCategory(testMCategory, Arrays.asList(new Category[]{testOCategory}),
                    CategoryService.Modify.ADD_CHILDREN_UNROOT);
            assertNotNull(results);
            assertEquals(results.entityType, entityType);
            assertCategoryCollection(new String[]{"TEST-M", "TEST-O"}, results.modified);
            commitTransaction();
        } finally {
            try {
                rollbackTransaction();
            } catch (Exception e) {
            }
        }
        // validate hierarchies
        beginTransaction();
        try {
            EntityType entityType = metadataService.getEntityTypeByName(CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP);
            assertNotNull(entityType);
            // validate category hierarchy structure
            Category testACategory = categoryService.getCategoryByName("TEST-A", CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP);
            assertNotNull(testACategory);
            assertCategoryCollection(new String[]{}, testACategory.getAncestors());
            assertCategoryCollection(new String[]{}, testACategory.getParents());
            assertCategoryCollection(new String[]{"TEST-C", "TEST-E"}, testACategory.getChildren());
            assertEquals(Boolean.TRUE, testACategory.isRoot());
            Category testBCategory = categoryService.getCategoryByName("TEST-B", CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP);
            assertNotNull(testBCategory);
            assertCategoryCollection(new String[]{"TEST-A", "TEST-C", "TEST-D", "TEST-E", "TEST-G"}, testBCategory.getAncestors());
            assertCategoryCollection(new String[]{"TEST-C", "TEST-E", "TEST-G"}, testBCategory.getParents());
            assertCategoryCollection(new String[]{}, testBCategory.getChildren());
            assertEquals(Boolean.FALSE, testBCategory.isRoot());
            Category testCCategory = categoryService.getCategoryByName("TEST-C", CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP);
            assertNotNull(testCCategory);
            assertCategoryCollection(new String[]{"TEST-A", "TEST-D"}, testCCategory.getAncestors());
            assertCategoryCollection(new String[]{"TEST-A", "TEST-D"}, testCCategory.getParents());
            assertCategoryCollection(new String[]{"TEST-B", "TEST-F"}, testCCategory.getChildren());
            assertEquals(Boolean.FALSE, testCCategory.isRoot());
            Category testDCategory = categoryService.getCategoryByName("TEST-D", CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP);
            assertNotNull(testDCategory);
            assertCategoryCollection(new String[]{}, testDCategory.getAncestors());
            assertCategoryCollection(new String[]{}, testDCategory.getParents());
            assertCategoryCollection(new String[]{"TEST-C"}, testDCategory.getChildren());
            assertEquals(Boolean.TRUE, testDCategory.isRoot());
            Category testECategory = categoryService.getCategoryByName("TEST-E", CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP);
            assertNotNull(testECategory);
            assertCategoryCollection(new String[]{"TEST-A"}, testECategory.getAncestors());
            assertCategoryCollection(new String[]{"TEST-A"}, testECategory.getParents());
            assertCategoryCollection(new String[]{"TEST-B"}, testECategory.getChildren());
            assertEquals(Boolean.FALSE, testECategory.isRoot());
            Category testFCategory = categoryService.getCategoryByName("TEST-F", CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP);
            assertNotNull(testFCategory);
            assertCategoryCollection(new String[]{"TEST-A", "TEST-C", "TEST-D"}, testFCategory.getAncestors());
            assertCategoryCollection(new String[]{"TEST-C"}, testFCategory.getParents());
            assertCategoryCollection(new String[]{}, testFCategory.getChildren());
            assertEquals(Boolean.FALSE, testFCategory.isRoot());
            Category testGCategory = categoryService.getCategoryByName("TEST-G", CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP);
            assertNotNull(testGCategory);
            assertCategoryCollection(new String[]{}, testGCategory.getAncestors());
            assertCategoryCollection(new String[]{}, testGCategory.getParents());
            assertCategoryCollection(new String[]{"TEST-B"}, testGCategory.getChildren());
            assertEquals(Boolean.TRUE, testGCategory.isRoot());
            Category testHCategory = categoryService.getCategoryByName("TEST-H", CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP);
            assertNotNull(testHCategory);
            assertCategoryCollection(new String[]{}, testHCategory.getAncestors());
            assertCategoryCollection(new String[]{}, testHCategory.getParents());
            assertCategoryCollection(new String[]{"TEST-I"}, testHCategory.getChildren());
            assertEquals(Boolean.TRUE, testHCategory.isRoot());
            Category testICategory = categoryService.getCategoryByName("TEST-I", CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP);
            assertNotNull(testICategory);
            assertCategoryCollection(new String[]{"TEST-H"}, testICategory.getAncestors());
            assertCategoryCollection(new String[]{"TEST-H"}, testICategory.getParents());
            assertCategoryCollection(new String[]{"TEST-J"}, testICategory.getChildren());
            assertEquals(Boolean.FALSE, testICategory.isRoot());
            Category testJCategory = categoryService.getCategoryByName("TEST-J", CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP);
            assertNotNull(testJCategory);
            assertCategoryCollection(new String[]{"TEST-H", "TEST-I"}, testJCategory.getAncestors());
            assertCategoryCollection(new String[]{"TEST-I"}, testJCategory.getParents());
            assertCategoryCollection(new String[]{}, testJCategory.getChildren());
            assertEquals(Boolean.TRUE, testJCategory.isRoot());
            Category testKCategory = categoryService.getCategoryByName("TEST-K", CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP);
            assertNotNull(testKCategory);
            assertCategoryCollection(new String[]{}, testKCategory.getAncestors());
            assertCategoryCollection(new String[]{}, testKCategory.getParents());
            assertCategoryCollection(new String[]{"TEST-N"}, testKCategory.getChildren());
            assertEquals(Boolean.TRUE, testKCategory.isRoot());
            Category testLCategory = categoryService.getCategoryByName("TEST-L", CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP);
            assertNotNull(testLCategory);
            assertCategoryCollection(new String[]{"TEST-M"}, testLCategory.getAncestors());
            assertCategoryCollection(new String[]{"TEST-M"}, testLCategory.getParents());
            assertCategoryCollection(new String[]{}, testLCategory.getChildren());
            assertEquals(Boolean.TRUE, testLCategory.isRoot());
            Category testMCategory = categoryService.getCategoryByName("TEST-M", CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP);
            assertNotNull(testMCategory);
            assertCategoryCollection(new String[]{}, testMCategory.getAncestors());
            assertCategoryCollection(new String[]{}, testMCategory.getParents());
            assertCategoryCollection(new String[]{"TEST-L", "TEST-O"}, testMCategory.getChildren());
            assertEquals(Boolean.TRUE, testMCategory.isRoot());
            Category testNCategory = categoryService.getCategoryByName("TEST-N", CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP);
            assertNotNull(testNCategory);
            assertCategoryCollection(new String[]{"TEST-K"}, testNCategory.getAncestors());
            assertCategoryCollection(new String[]{"TEST-K"}, testNCategory.getParents());
            assertCategoryCollection(new String[]{}, testNCategory.getChildren());
            assertEquals(Boolean.FALSE, testNCategory.isRoot());
            Category testOCategory = categoryService.getCategoryByName("TEST-O", CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP);
            assertNotNull(testOCategory);
            assertCategoryCollection(new String[]{"TEST-M"}, testOCategory.getAncestors());
            assertCategoryCollection(new String[]{"TEST-M"}, testOCategory.getParents());
            assertCategoryCollection(new String[]{}, testOCategory.getChildren());
            assertEquals(Boolean.FALSE, testOCategory.isRoot());
        } finally {
            rollbackTransaction();
        }
        // delete hierarchies
        beginTransaction();
        try {
            Category testDCategory = categoryService.getCategoryByName("TEST-D", CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP);
            assertNotNull(testDCategory);
            CategoryService.Results results = categoryService.deleteCategory(testDCategory, false, CategoryService.Delete.CASCADE);
            assertNotNull(results);
            assertEquals(results.entityType, testDCategory.getEntityType());
            assertCategoryCollection(new String[]{"TEST-D"}, results.deleted);
            assertCategoryCollection(new String[]{"TEST-B", "TEST-C", "TEST-F"}, results.modified);
            Category testACategory = categoryService.getCategoryByName("TEST-A", CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP);
            assertNotNull(testACategory);
            results = categoryService.deleteCategory(testACategory, false, CategoryService.Delete.CASCADE);
            assertNotNull(results);
            assertEquals(results.entityType, testACategory.getEntityType());
            assertCategoryCollection(new String[]{"TEST-A", "TEST-C", "TEST-E", "TEST-F"}, results.deleted);
            assertCategoryCollection(new String[]{"TEST-B"}, results.modified);
            Category testGCategory = categoryService.getCategoryByName("TEST-G", CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP);
            assertNotNull(testGCategory);
            results = categoryService.deleteCategory(testGCategory, false, CategoryService.Delete.ORPHAN_CHILDREN_AS_ROOTS);
            assertNotNull(results);
            assertEquals(results.entityType, testGCategory.getEntityType());
            assertCategoryCollection(new String[]{"TEST-G"}, results.deleted);
            assertCategoryCollection(new String[]{"TEST-B"}, results.modified);
            Category testBCategory = categoryService.getCategoryByName("TEST-B", CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP);
            assertNotNull(testBCategory);
            results = categoryService.deleteCategory(testBCategory, false, CategoryService.Delete.LEAF_ONLY);
            assertNotNull(results);
            assertEquals(results.entityType, testBCategory.getEntityType());
            assertCategoryCollection(new String[]{"TEST-B"}, results.deleted);
            assertCategoryCollection(new String[]{}, results.modified);
            Category testICategory = categoryService.getCategoryByName("TEST-I", CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP);
            assertNotNull(testICategory);
            results = categoryService.deleteCategory(testICategory, false, CategoryService.Delete.ADD_CHILDREN_TO_PARENTS);
            assertNotNull(results);
            assertEquals(results.entityType, testICategory.getEntityType());
            assertCategoryCollection(new String[]{"TEST-I"}, results.deleted);
            assertCategoryCollection(new String[]{"TEST-H", "TEST-J"}, results.modified);
            Category testHCategory = categoryService.getCategoryByName("TEST-H", CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP);
            assertNotNull(testHCategory);
            results = categoryService.deleteCategory(testHCategory, false, CategoryService.Delete.CASCADE);
            assertNotNull(results);
            assertEquals(results.entityType, testHCategory.getEntityType());
            assertCategoryCollection(new String[]{"TEST-H"}, results.deleted);
            assertCategoryCollection(new String[]{"TEST-J"}, results.modified);
            Category testJCategory = categoryService.getCategoryByName("TEST-J", CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP);
            assertNotNull(testJCategory);
            results = categoryService.deleteCategory(testJCategory, false, CategoryService.Delete.LEAF_ONLY);
            assertNotNull(results);
            assertEquals(results.entityType, testJCategory.getEntityType());
            assertCategoryCollection(new String[]{"TEST-J"}, results.deleted);
            assertCategoryCollection(new String[]{}, results.modified);
            Category testKCategory = categoryService.getCategoryByName("TEST-K", CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP);
            assertNotNull(testKCategory);
            results = categoryService.deleteCategory(testKCategory, false, CategoryService.Delete.CASCADE);
            assertNotNull(results);
            assertEquals(results.entityType, testKCategory.getEntityType());
            assertCategoryCollection(new String[]{"TEST-K", "TEST-N"}, results.deleted);
            assertCategoryCollection(new String[]{}, results.modified);
            Category testMCategory = categoryService.getCategoryByName("TEST-M", CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP);
            assertNotNull(testMCategory);
            results = categoryService.deleteCategory(testMCategory, false, CategoryService.Delete.CASCADE_ALL);
            assertNotNull(results);
            assertEquals(results.entityType, testMCategory.getEntityType());
            assertCategoryCollection(new String[]{"TEST-L", "TEST-M", "TEST-O"}, results.deleted);
            assertCategoryCollection(new String[]{}, results.modified);
            commitTransaction();
        } finally {
            try {
                rollbackTransaction();
            } catch (Exception e) {
            }
        }
        // validate delete
        beginTransaction();
        try {
            Category testACategory = categoryService.getCategoryByName("TEST-A", CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP);
            assertNull(testACategory);
            Category testBCategory = categoryService.getCategoryByName("TEST-B", CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP);
            assertNull(testBCategory);
            Category testCCategory = categoryService.getCategoryByName("TEST-C", CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP);
            assertNull(testCCategory);
            Category testDCategory = categoryService.getCategoryByName("TEST-D", CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP);
            assertNull(testDCategory);
            Category testECategory = categoryService.getCategoryByName("TEST-E", CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP);
            assertNull(testECategory);
            Category testFCategory = categoryService.getCategoryByName("TEST-F", CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP);
            assertNull(testFCategory);
            Category testGCategory = categoryService.getCategoryByName("TEST-G", CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP);
            assertNull(testGCategory);
            Category testHCategory = categoryService.getCategoryByName("TEST-H", CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP);
            assertNull(testHCategory);
            Category testICategory = categoryService.getCategoryByName("TEST-I", CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP);
            assertNull(testICategory);
            Category testJCategory = categoryService.getCategoryByName("TEST-J", CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP);
            assertNull(testJCategory);
            Category testKCategory = categoryService.getCategoryByName("TEST-K", CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP);
            assertNull(testKCategory);
            Category testLCategory = categoryService.getCategoryByName("TEST-L", CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP);
            assertNull(testLCategory);
            Category testMCategory = categoryService.getCategoryByName("TEST-M", CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP);
            assertNull(testMCategory);
            Category testNCategory = categoryService.getCategoryByName("TEST-N", CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP);
            assertNull(testNCategory);
            Category testOCategory = categoryService.getCategoryByName("TEST-O", CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP);
            assertNull(testOCategory);
        } finally {
            rollbackTransaction();
        }
    }

    /**
     * Assert single named category.
     *
     * @param assertCategoryName category name
     * @param category category to test
     * @return valid category
     */
    private Category assertCategory(String assertCategoryName, Category category) {
        if (assertCategoryName == null) {
            assertNull(category);
        } else {
            assertNotNull(category);
            assertEquals(assertCategoryName, category.getName());
        }
        return category;
    }

    /**
     * Assert category collection consists of categories with given names.
     *
     * @param assertCategoriesNames category names
     * @param categories category collection to test
     * @return map of categories in collection by name
     */
    private Map<String,Category> assertCategoryCollection(String [] assertCategoriesNames, Collection<Category> categories) {
        Map<String,Category> returnCategoryMap = new HashMap<String,Category>();
        if ((assertCategoriesNames == null) || (assertCategoriesNames.length == 0)) {
            assertTrue((categories == null) || categories.isEmpty());
        } else {
            assertTrue((categories != null) && !categories.isEmpty());
            Set<String> assertCategoriesNamesSet = new HashSet<String>(Arrays.asList(assertCategoriesNames));
            Set<String> categoriesNamesSet = new HashSet<String>();
            for (Category category : categories) {
                categoriesNamesSet.add(category.getName());
                returnCategoryMap.put(category.getName(), category);
            }
            assertEquals(categories.size(), categoriesNamesSet.size());
            assertEquals(assertCategoriesNamesSet, categoriesNamesSet);
        }
        return returnCategoryMap;
    }

    public void testAutocomplete() throws Exception {
        // wait for initial load
        Thread.sleep(250);
        // test autocomplete names
        List<AutocompleteName> names = customGroupAutocompleteService.autocomplete("cg");
        assertNotNull(names);
        assertEquals(3, names.size());
        assertEquals("CG1", names.get(0).getName());
        assertEquals("CG2", names.get(1).getName());
        assertEquals("CG3", names.get(2).getName());
        names = serviceGroupAutocompleteService.autocomplete("sg");
        assertNotNull(names);
        assertEquals(2, names.size());
        assertEquals("SG1", names.get(0).getName());
        assertEquals("SG2", names.get(1).getName());
        // create custom and service group
        try {
            EntityType customGroupEntityType = metadataService.getEntityTypeByName(CategoryService.ENTITY_TYPE_CODE_CUSTOMGROUP);
            Category customGroup = categoryService.createCategory("CG2-2", null, customGroupEntityType);
            categoryService.saveCategory(customGroup);
            EntityType serviceGroupEntityType = metadataService.getEntityTypeByName(CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP);
            Category serviceGroup = categoryService.createCategory("SG2-2", null, serviceGroupEntityType);
            categoryService.saveCategory(serviceGroup);
            // wait for refresh and validate names
            Thread.sleep(250);
            names = customGroupAutocompleteService.autocomplete("CG2");
            assertNotNull(names);
            assertEquals(2, names.size());
            assertEquals("CG2", names.get(0).getName());
            assertEquals("CG2-2", names.get(1).getName());
            names = serviceGroupAutocompleteService.autocomplete("SG2");
            assertNotNull(names);
            assertEquals(2, names.size());
            assertEquals("SG2", names.get(0).getName());
            assertEquals("SG2-2", names.get(1).getName());
        } finally {
            // cleanup test objects
            categoryService.deleteCategoryByName("CG2-2", CategoryService.ENTITY_TYPE_CODE_CUSTOMGROUP);
            categoryService.deleteCategoryByName("SG2-2", CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP);
        }
        // wait for refresh and validate names
        Thread.sleep(250);
        names = customGroupAutocompleteService.autocomplete("cg");
        assertNotNull(names);
        assertEquals(3, names.size());
        assertEquals("CG1", names.get(0).getName());
        assertEquals("CG2", names.get(1).getName());
        assertEquals("CG3", names.get(2).getName());
        names = serviceGroupAutocompleteService.autocomplete("sg");
        assertNotNull(names);
        assertEquals(2, names.size());
        assertEquals("SG1", names.get(0).getName());
        assertEquals("SG2", names.get(1).getName());
    }
}
