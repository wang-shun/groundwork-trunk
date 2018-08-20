package org.groundwork.rs.client;

import org.groundwork.rs.dto.DtoCategory;
import org.groundwork.rs.dto.DtoCategoryEntity;
import org.groundwork.rs.dto.DtoCategoryList;
import org.groundwork.rs.dto.DtoCategoryMemberUpdate;
import org.groundwork.rs.dto.DtoCategoryUpdate;
import org.groundwork.rs.dto.DtoCategoryUpdateList;
import org.groundwork.rs.dto.DtoDepthType;
import org.groundwork.rs.dto.DtoEntityType;
import org.groundwork.rs.dto.DtoOperationResult;
import org.groundwork.rs.dto.DtoOperationResults;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.fail;
import static org.junit.Assert.*;

public class CategoryClientTest extends AbstractClientTest {

    private static final String SERVICE_GROUP_ENTITY_TYPE = "SERVICE_GROUP";
    private static final String CUSTOM_GROUP_ENTITY_TYPE = "CUSTOM_GROUP";
    private static final String HOST_GROUP_ENTITY_TYPE = "HOSTGROUP";
    private static final String SERVICE_STATUS_ENTITY_TYPE = "SERVICE_STATUS";
    private static final int SERVICE_STATUS_ENTITY_TYPE_ID = 2;
    private static final String CATEGORY_ENTITY_TYPE = "CATEGORY";
    private static final int CATEGORY_ENTITY_TYPE_ID = 8;

    private static final String CATEGORY_400 = "category-400";
    private static final String CATEGORY_401 = "category-401";

    @Test
    public void testCategoryLookupShallow() throws Exception {
        if (serverDown) return;
        CategoryClient client = new CategoryClient(getDeploymentURL());
        DtoCategory category = client.lookup("web-svr", SERVICE_GROUP_ENTITY_TYPE);
        assertNotNull(category);
        assertEquals("web-svr", category.getName());
        assertEquals(SERVICE_GROUP_ENTITY_TYPE, category.getEntityTypeName());
        category = client.lookup("web-bad", SERVICE_GROUP_ENTITY_TYPE);
        assertNull(category);
        category = client.lookup("SG1", SERVICE_GROUP_ENTITY_TYPE);
        assertEquals("SG1", category.getName());
        assertEquals(SERVICE_GROUP_ENTITY_TYPE, category.getEntityTypeName());
    }

    @Test
    public void testCategoryLookupDeep() throws Exception {
        if (serverDown) return;
        CategoryClient client = new CategoryClient(getDeploymentURL());
        DtoCategory category = client.lookup("web-svr", SERVICE_GROUP_ENTITY_TYPE, DtoDepthType.Deep);
        assertNotNull(category);
        assertEquals("web-svr", category.getName());
        assertEquals(8, category.getEntities().size());
        category = client.lookup("web-bad", SERVICE_GROUP_ENTITY_TYPE, DtoDepthType.Deep);
        assertNull(category);
        category = client.lookup("SG1", SERVICE_GROUP_ENTITY_TYPE, DtoDepthType.Deep);
        assertEquals("SG1", category.getName());
        assertEquals(2, category.getEntities().size());
    }

    @Test
    public void testList() throws Exception {
        if (serverDown) return;
        CategoryClient client = new CategoryClient(getDeploymentURL());
        List<DtoCategory> categories = client.list(DtoDepthType.Deep);
        assertEquals(5, categories.size());
        for (DtoCategory category : categories) {
            String name = category.getName();
            if (name.equals("SG1") || name.equals("web-svr")) {
                assertEquals("com.groundwork.collage.model.impl.ServiceGroup", category.getEntityType().getDescription());
                assertEquals(SERVICE_GROUP_ENTITY_TYPE, category.getEntityType().getName());
                assertEquals(true, category.getEntityType().isLogicalEntity());
                assertEquals(false, category.getEntityType().isApplicationTypeSupported());
                if (name.equals("SG1")) {
                    assertNotNull(category.getEntities());
                    assertEquals(2, category.getEntities().size());
                    Set<Integer> objectIDs = new HashSet<Integer>(Arrays.asList(new Integer[]{44, 3}));
                    for (DtoCategoryEntity categoryEntity : category.getEntities()) {
                        assertTrue(objectIDs.contains(categoryEntity.getObjectID()));
                        assertEquals(SERVICE_STATUS_ENTITY_TYPE, categoryEntity.getEntityTypeName());
                    }
                } else {
                    assertNotNull(category.getEntities());
                    assertEquals(8, category.getEntities().size());
                    Set<Integer> objectIDs = new HashSet<Integer>(Arrays.asList(new Integer[]{47, 36, 31, 51, 44, 26, 67, 22}));
                    for (DtoCategoryEntity categoryEntity : category.getEntities()) {
                        assertTrue(objectIDs.contains(categoryEntity.getObjectID()));
                        assertEquals(SERVICE_STATUS_ENTITY_TYPE, categoryEntity.getEntityTypeName());
                    }
                }
            } else if (name.startsWith("CG")) {
                assertEquals("com.groundwork.collage.model.impl.CustomGroup", category.getEntityType().getDescription());
                assertEquals(CUSTOM_GROUP_ENTITY_TYPE, category.getEntityType().getName());
                assertEquals(true, category.getEntityType().isLogicalEntity());
                assertEquals(false, category.getEntityType().isApplicationTypeSupported());
                if (name.equals("CG1")) {
                    assertTrue(category.isRoot());
                    assertNull(category.getParents());
                    assertNull(category.getParentNames());
                    assertNotNull(category.getChildren());
                    assertEquals(2, category.getChildren().size());
                    Set<Integer> childIDs = new HashSet<Integer>(Arrays.asList(new Integer[]{4, 5}));
                    for (DtoCategory categoryChild : category.getChildren()) {
                        assertTrue(childIDs.contains(categoryChild.getId()));
                    }
                    assertNotNull(category.getChildNames());
                    assertEquals(2, category.getChildNames().size());
                    assertTrue(category.getChildNames().contains("CG2"));
                    assertTrue(category.getChildNames().contains("CG3"));
                    assertNull(category.getEntities());
                } else if (name.equals("CG2")) {
                    assertFalse(category.isRoot());
                    assertNotNull(category.getParents());
                    assertEquals(1, category.getParents().size());
                    Set<Integer> parentIDs = new HashSet<Integer>(Arrays.asList(new Integer[]{3}));
                    for (DtoCategory categoryParent : category.getParents()) {
                        assertTrue(parentIDs.contains(categoryParent.getId()));
                    }
                    assertNotNull(category.getParentNames());
                    assertEquals(1, category.getParentNames().size());
                    assertTrue(category.getParentNames().contains("CG1"));
                    assertNull(category.getChildren());
                    assertNull(category.getChildNames());
                    assertNotNull(category.getEntities());
                    assertEquals(1, category.getEntities().size());
                    Set<Integer> objectIDs = new HashSet<Integer>(Arrays.asList(new Integer[]{1}));
                    for (DtoCategoryEntity categoryEntity : category.getEntities()) {
                        assertTrue(objectIDs.contains(categoryEntity.getObjectID()));
                        assertEquals(HOST_GROUP_ENTITY_TYPE, categoryEntity.getEntityTypeName());
                    }
                } else if (name.equals("CG3")) {
                    assertFalse(category.isRoot());
                    assertNotNull(category.getParents());
                    assertEquals(1, category.getParents().size());
                    Set<Integer> parentIDs = new HashSet<Integer>(Arrays.asList(new Integer[]{3}));
                    for (DtoCategory categoryParent : category.getParents()) {
                        assertTrue(parentIDs.contains(categoryParent.getId()));
                    }
                    assertNotNull(category.getParentNames());
                    assertEquals(1, category.getParentNames().size());
                    assertTrue(category.getParentNames().contains("CG1"));
                    assertNull(category.getChildren());
                    assertNull(category.getChildNames());
                    assertNotNull(category.getEntities());
                    assertEquals(1, category.getEntities().size());
                    Set<Integer> objectIDs = new HashSet<Integer>(Arrays.asList(new Integer[]{2}));
                    for (DtoCategoryEntity categoryEntity : category.getEntities()) {
                        assertTrue(objectIDs.contains(categoryEntity.getObjectID()));
                        assertEquals(SERVICE_GROUP_ENTITY_TYPE, categoryEntity.getEntityTypeName());
                    }
                } else {
                    fail("Unexpected custom category name: "+name);
                }
            } else {
                fail("Unexpected category name: "+name);
            }
        }
    }

    @Test
    public void testQueries() throws Exception {
        if (serverDown) return;
        CategoryClient client = new CategoryClient(getDeploymentURL());
        List<DtoCategory> categories = client.query("entityType.name = '"+SERVICE_GROUP_ENTITY_TYPE+"'", DtoDepthType.Deep);
        assertEquals(2, categories.size());

        categories = client.query("c.categoryEntities.objectID = 22", DtoDepthType.Deep);
        assertEquals(1, categories.size());

        categories = client.query("name = 'web-svr'", DtoDepthType.Deep);
        assertEquals(1, categories.size());

        categories = client.query("name = 'not-found'", DtoDepthType.Deep);
        assertEquals(0, categories.size());

        categories = client.query("c.categoryEntities.objectID = 44", DtoDepthType.Deep);
        assertEquals(2, categories.size());
    }

    @Test
    public void testCreateAndDeleteCategories() throws Exception {
        if (serverDown) return;
        CategoryClient client = new CategoryClient(getDeploymentURL());
        DtoCategoryList updates = buildCategoryUpdate(false, false);
        DtoOperationResults results = client.post(updates);
        Assert.assertEquals(2, results.getCount().intValue());
        for (DtoOperationResult result : results.getResults()) {
            assert(result.getStatus().equals(DtoOperationResult.SUCCESS));
        }
        DtoCategory category = client.lookup(CATEGORY_400, SERVICE_GROUP_ENTITY_TYPE, DtoDepthType.Deep);
        Assert.assertNotNull(category);
        assertCategoryWritten(category);

        category = client.lookup(CATEGORY_400, SERVICE_GROUP_ENTITY_TYPE, DtoDepthType.Deep);
        Assert.assertNotNull(category);
        assertCategoryWritten(category);

        // reset data for next test
        client.delete(CATEGORY_400, SERVICE_GROUP_ENTITY_TYPE);
        client.delete(CATEGORY_401, SERVICE_GROUP_ENTITY_TYPE);

        // test its deleted
        category = client.lookup(CATEGORY_400, SERVICE_GROUP_ENTITY_TYPE);
        assertNull(category);
        category = client.lookup(CATEGORY_401, SERVICE_GROUP_ENTITY_TYPE);
        assertNull(category);

        // test warning for missing delete
        DtoOperationResults deleteResults = client.delete("NotACategory", SERVICE_GROUP_ENTITY_TYPE);
        assertNotNull(deleteResults);
        assertEquals(new Integer(1), deleteResults.getWarning());
    }

    @Test
    public void testCreateAndDeleteCategoriesWithEntities() throws Exception {
        if (serverDown) return;
        // allocate client
        CategoryClient client = new CategoryClient(getDeploymentURL());

        // run test using JSON and XML
        client.setMediaType(MediaType.APPLICATION_JSON_TYPE);
        testCreateAndDeleteCategoriesWithEntities(client);
        client.setMediaType(MediaType.APPLICATION_XML_TYPE);
        testCreateAndDeleteCategoriesWithEntities(client);
    }

    public void testCreateAndDeleteCategoriesWithEntities(CategoryClient client) throws Exception {
        // create with entities
        DtoCategoryList updates = buildCategoryUpdate(true, false);
        DtoOperationResults results = client.post(updates);
        Assert.assertEquals(2, results.getCount().intValue());
        for (DtoOperationResult result : results.getResults()) {
            assert(result.getStatus().equals(DtoOperationResult.SUCCESS));
        }

        // assert categories with entities
        DtoCategory category = client.lookup(CATEGORY_400, SERVICE_GROUP_ENTITY_TYPE, DtoDepthType.Deep);
        Assert.assertNotNull(category);
        assertCategoryWritten(category);
        // assert Category Entities
        assertEquals(1, category.getEntities().size());
        DtoCategoryEntity entity = category.getEntities().get(0);
        assertEquals(1024, entity.getObjectID().intValue());
        assertEquals(SERVICE_STATUS_ENTITY_TYPE_ID, entity.getEntityTypeId().intValue());
        assertEquals(SERVICE_STATUS_ENTITY_TYPE, entity.getEntityTypeName());
        category = client.lookup(CATEGORY_401, SERVICE_GROUP_ENTITY_TYPE, DtoDepthType.Deep);
        Assert.assertNotNull(category);
        assertCategoryWritten(category);
        assertEquals(2, category.getEntities().size());
        entity = category.getEntities().get(0);
        if (entity.getObjectID() == 1025) {
            assertCategories1(entity);
            assertCategories2(category.getEntities().get(1));
        } else {
            assertCategories2(entity);
            assertCategories1(category.getEntities().get(1));
        }

        // add member/entities
        DtoCategoryMemberUpdate memberUpdate = new DtoCategoryMemberUpdate();
        memberUpdate.setName(CATEGORY_400);
        memberUpdate.setEntityTypeName(SERVICE_GROUP_ENTITY_TYPE);
        DtoCategoryEntity categoryEntityMember = new DtoCategoryEntity();
        categoryEntityMember.setObjectID(2048);
        categoryEntityMember.setEntityTypeName(SERVICE_STATUS_ENTITY_TYPE);
        memberUpdate.addEntity(categoryEntityMember);
        results = client.addMembers(memberUpdate);
        Assert.assertEquals(1, results.getSuccessful().intValue());

        // assert member/entities
        category = client.lookup(CATEGORY_400, SERVICE_GROUP_ENTITY_TYPE, DtoDepthType.Deep);
        assertEquals(2, category.getEntities().size());

        // delete member/entities
        results = client.deleteMembers(memberUpdate);
        Assert.assertEquals(1, results.getSuccessful().intValue());

        // assert member/entities
        category = client.lookup(CATEGORY_400, SERVICE_GROUP_ENTITY_TYPE, DtoDepthType.Deep);
        assertEquals(1, category.getEntities().size());

        // reset data for next test
        client.delete(CATEGORY_400, SERVICE_GROUP_ENTITY_TYPE);
        client.delete(CATEGORY_401, SERVICE_GROUP_ENTITY_TYPE);

        // test its deleted
        category = client.lookup(CATEGORY_400, SERVICE_GROUP_ENTITY_TYPE);
        assertNull(category);
        category = client.lookup(CATEGORY_401, SERVICE_GROUP_ENTITY_TYPE);
        assertNull(category);
    }

    private void assertCategories1(DtoCategoryEntity entity) {
        assertEquals(1025, entity.getObjectID().intValue());
        assertEquals(SERVICE_STATUS_ENTITY_TYPE_ID, entity.getEntityTypeId().intValue());
        assertEquals(SERVICE_STATUS_ENTITY_TYPE, entity.getEntityTypeName());
    }

    private void assertCategories2(DtoCategoryEntity entity) {
        assertEquals(1026, entity.getObjectID().intValue());
        assertEquals(CATEGORY_ENTITY_TYPE_ID, entity.getEntityTypeId().intValue());
        assertEquals(CATEGORY_ENTITY_TYPE, entity.getEntityTypeName());
    }


    private DtoCategoryList buildCategoryUpdate(boolean withEntities, boolean withParents) throws Exception {
        DtoCategoryList categories = new DtoCategoryList();
        DtoCategory cat400 = new DtoCategory();
        cat400.setName(CATEGORY_400);
        cat400.setDescription("Category 400 description");
        cat400.setAppType("NAGIOS");
        cat400.setAgentId("007");
        DtoEntityType entityType = new DtoEntityType();
        entityType.setName(SERVICE_GROUP_ENTITY_TYPE);
        cat400.setEntityType(entityType);
        if (withParents) {
            DtoCategory sg1 = new DtoCategory();
            sg1.setName("SG1");
            cat400.addParent(sg1);
        }
        if (withEntities) {
            DtoCategoryEntity entity = new DtoCategoryEntity();
            entity.setObjectID(1024);
            //entity.setCategory(cat400);
            DtoEntityType et = new DtoEntityType();
            et.setName(SERVICE_STATUS_ENTITY_TYPE);
            entity.setEntityType(et);
            cat400.addEntity(entity);
        }
        categories.add(cat400);
        DtoCategory cat401 = new DtoCategory();
        cat401.setName(CATEGORY_401);
        cat401.setDescription("Category 401 description");
        entityType = new DtoEntityType();
        entityType.setName(SERVICE_GROUP_ENTITY_TYPE);
        cat401.setAppType("SYSTEM");
        cat401.setAgentId("008");
        cat401.setEntityType(entityType);
        if (withParents) {
            DtoCategory sg1 = new DtoCategory();
            sg1.setName("SG1");
            cat401.addParent(sg1);
            DtoCategory sg2 = new DtoCategory();
            sg2.setName("web-svr");
            cat401.addParent(sg2);
        }
        if (withEntities) {
            DtoCategoryEntity entity = new DtoCategoryEntity();
            entity.setObjectID(1025);
            //entity.setCategory(cat401);
            DtoEntityType et = new DtoEntityType();
            et.setName(SERVICE_STATUS_ENTITY_TYPE);
            entity.setEntityType(et);
            cat401.addEntity(entity);
            entity = new DtoCategoryEntity();
            entity.setObjectID(1026);
            //entity.setCategory(cat401);
            entity.setEntityTypeId(CATEGORY_ENTITY_TYPE_ID);
            cat401.addEntity(entity);
        }
        categories.add(cat401);
        return categories;
    }

    private void assertCategoryWritten(DtoCategory category) {
        assertNotNull(category.getName());
        if (category.getName().equals(CATEGORY_400)) {
            assertEquals(CATEGORY_400, category.getName());
            assertEquals("Category 400 description", category.getDescription());
            assertEquals(SERVICE_GROUP_ENTITY_TYPE, category.getEntityType().getName());
            assertEquals("NAGIOS", category.getAppType());
            assertEquals("007", category.getAgentId());
        }
        else if (category.getName().equals(CATEGORY_401)) {
            assertEquals(CATEGORY_401, category.getName());
            assertEquals("Category 401 description", category.getDescription());
            assertEquals(SERVICE_GROUP_ENTITY_TYPE, category.getEntityType().getName());
            assertEquals("SYSTEM", category.getAppType());
            assertEquals("008", category.getAgentId());
        }
        else {
            fail("category name " + category.getName() + " not valid");
        }
    }

    @Test
    public void testCategoryHierarchy() {
        if (serverDown) return;
        // allocate client
        CategoryClient client = new CategoryClient(getDeploymentURL());

        // run test using JSON and XML
        client.setMediaType(MediaType.APPLICATION_JSON_TYPE);
        testCategoryHierarchy(client);
        client.setMediaType(MediaType.APPLICATION_XML_TYPE);
        testCategoryHierarchy(client);
    }

    private void testCategoryHierarchy(CategoryClient client) {
        // create category hierarchies
        DtoCategoryUpdateList updates = new DtoCategoryUpdateList();
        DtoCategoryUpdate update = new DtoCategoryUpdate();
        update.setCategoryName("TEST-A");
        update.setEntityTypeName(SERVICE_GROUP_ENTITY_TYPE);
        update.setCreate(CategoryClient.CREATE_AS_ROOT);
        updates.getCategoryUpdates().add(update);
        update = new DtoCategoryUpdate();
        update.setCategoryName("TEST-B");
        update.setEntityTypeName(SERVICE_GROUP_ENTITY_TYPE);
        update.setParentName("TEST-A");
        update.setCreate(CategoryClient.CREATE_AS_CHILD);
        updates.getCategoryUpdates().add(update);
        update = new DtoCategoryUpdate();
        update.setCategoryName("TEST-C");
        update.setEntityTypeName(SERVICE_GROUP_ENTITY_TYPE);
        update.setParentName("TEST-B");
        update.setCreate(CategoryClient.CREATE_AS_CHILD);
        updates.getCategoryUpdates().add(update);
        update = new DtoCategoryUpdate();
        update.setCategoryName("TEST-C");
        update.setEntityTypeName(SERVICE_GROUP_ENTITY_TYPE);
        update.setClone(CategoryClient.CLONE_AS_LEAF_WITH_PARENTS);
        update.setCloneName("TEST-D");
        updates.getCategoryUpdates().add(update);
        update = new DtoCategoryUpdate();
        update.setCategoryName("TEST-D");
        update.setEntityTypeName(SERVICE_GROUP_ENTITY_TYPE);
        update.setModify(CategoryClient.MODIFY_ROOT);
        updates.getCategoryUpdates().add(update);
        update = new DtoCategoryUpdate();
        update.setCategoryName("TEST-D");
        update.setEntityTypeName(SERVICE_GROUP_ENTITY_TYPE);
        update.setModify(CategoryClient.MODIFY_ADD_CHILD);
        update.setOtherCategoryNames(Arrays.asList(new String[]{"TEST-A"}));
        updates.getCategoryUpdates().add(update);
        update = new DtoCategoryUpdate();
        update.setCategoryName("TEST-A");
        update.setEntityTypeName(SERVICE_GROUP_ENTITY_TYPE);
        update.setModify(CategoryClient.MODIFY_REMOVE_CHILDREN);
        update.setOtherCategoryNames(Arrays.asList(new String[]{"TEST-D"}));
        updates.getCategoryUpdates().add(update);
        DtoOperationResults results = client.update(updates);
        assertNotNull(results);
        assertEquals(new Integer(7), results.getSuccessful());
        assertEquals(7, results.getResults().size());
        assertEquals("TEST-A:" + SERVICE_GROUP_ENTITY_TYPE, results.getResults().get(0).getEntity());
        assertEquals("TEST-B:" + SERVICE_GROUP_ENTITY_TYPE, results.getResults().get(1).getEntity());
        assertEquals("TEST-C:" + SERVICE_GROUP_ENTITY_TYPE, results.getResults().get(2).getEntity());
        assertEquals("TEST-C:" + SERVICE_GROUP_ENTITY_TYPE, results.getResults().get(3).getEntity());
        assertEquals("TEST-D:" + SERVICE_GROUP_ENTITY_TYPE, results.getResults().get(4).getEntity());
        assertEquals("TEST-D:" + SERVICE_GROUP_ENTITY_TYPE, results.getResults().get(5).getEntity());
        assertEquals("TEST-A:" + SERVICE_GROUP_ENTITY_TYPE, results.getResults().get(6).getEntity());
        DtoCategoryList updateCategories = new DtoCategoryList();
        DtoCategory updateCategory = new DtoCategory();
        updateCategory.setName("TEST-C");
        updateCategory.setEntityTypeName(SERVICE_GROUP_ENTITY_TYPE);
        DtoCategoryEntity updateCategoryEntity = new DtoCategoryEntity();
        updateCategoryEntity.setObjectID(1024);
        updateCategoryEntity.setEntityTypeName(SERVICE_STATUS_ENTITY_TYPE);
        updateCategory.addEntity(updateCategoryEntity);
        updateCategories.add(updateCategory);
        results = client.post(updateCategories);
        assertNotNull(results);
        assertEquals(new Integer(1), results.getSuccessful());
        assertEquals(1, results.getResults().size());
        assertEquals("TEST-C:" + SERVICE_GROUP_ENTITY_TYPE, results.getResults().get(0).getEntity());
        // query categories
        List<DtoCategory> entityType = client.entityType(SERVICE_GROUP_ENTITY_TYPE, false);
        assertCategories(new String[]{"TEST-A", "TEST-B", "TEST-C", "TEST-D"}, entityType);
        List<DtoCategory> entityTypeRoots = client.entityType(SERVICE_GROUP_ENTITY_TYPE, true);
        assertCategories(new String[]{"TEST-A", "TEST-D"}, entityTypeRoots);
        assertNotCategories(new String[]{"TEST-B", "TEST-C"}, entityTypeRoots);
        // query category hierarchy
        List<DtoCategory> hierarchy = client.hierarchy("TEST-A", SERVICE_GROUP_ENTITY_TYPE);
        assertNotNull(hierarchy);
        assertEquals(4, hierarchy.size());
        DtoCategory hierarchyACategory = hierarchy.get(0);
        assertEquals("TEST-A", hierarchyACategory.getName());
        assertTrue((hierarchyACategory.getParentNames() == null) || hierarchyACategory.getParentNames().isEmpty());
        assertNotNull(hierarchyACategory.getChildNames());
        assertEquals(1, hierarchyACategory.getChildNames().size());
        assertTrue(hierarchyACategory.getChildNames().contains("TEST-B"));
        assertTrue((hierarchyACategory.isRoot() != null) && hierarchyACategory.isRoot());
        DtoCategory hierarchyBCategory = hierarchy.get(1);
        assertEquals("TEST-B", hierarchyBCategory.getName());
        assertNotNull(hierarchyBCategory.getParentNames());
        assertEquals(1, hierarchyBCategory.getParentNames().size());
        assertTrue(hierarchyBCategory.getParentNames().contains("TEST-A"));
        assertNotNull(hierarchyBCategory.getChildNames());
        assertEquals(2, hierarchyBCategory.getChildNames().size());
        assertTrue(hierarchyBCategory.getChildNames().contains("TEST-C"));
        assertTrue(hierarchyBCategory.getChildNames().contains("TEST-D"));
        assertTrue((hierarchyBCategory.isRoot() != null) && !hierarchyBCategory.isRoot());
        DtoCategory hierarchyCCategory = hierarchy.get(2);
        assertEquals("TEST-C", hierarchyCCategory.getName());
        assertNotNull(hierarchyCCategory.getParentNames());
        assertEquals(1, hierarchyCCategory.getParentNames().size());
        assertTrue(hierarchyCCategory.getParentNames().contains("TEST-B"));
        assertTrue((hierarchyCCategory.getChildNames() == null) || hierarchyCCategory.getChildNames().isEmpty());
        assertTrue((hierarchyCCategory.isRoot() != null) && !hierarchyCCategory.isRoot());
        DtoCategory hierarchyDCategory = hierarchy.get(3);
        assertEquals("TEST-D", hierarchyDCategory.getName());
        assertNotNull(hierarchyDCategory.getParentNames());
        assertEquals(1, hierarchyDCategory.getParentNames().size());
        assertTrue(hierarchyDCategory.getParentNames().contains("TEST-B"));
        assertTrue((hierarchyDCategory.getChildNames() == null) || hierarchyDCategory.getChildNames().isEmpty());
        assertTrue((hierarchyDCategory.isRoot() != null) && hierarchyDCategory.isRoot());
        // query categories by entity
        List<DtoCategory> entityRoots = client.entityRoots(SERVICE_GROUP_ENTITY_TYPE, 1024, SERVICE_STATUS_ENTITY_TYPE);
        assertNotNull(entityRoots);
        assertEquals(1, entityRoots.size());
        assertEquals("TEST-A", entityRoots.get(0).getName());
        List<DtoCategory> entity = client.entity(SERVICE_GROUP_ENTITY_TYPE, 1024, SERVICE_STATUS_ENTITY_TYPE);
        assertNotNull(entity);
        assertEquals(1, entity.size());
        assertEquals("TEST-C", entity.get(0).getName());
        // validate deep category access
        DtoCategory deepACategory = client.lookup("TEST-A", SERVICE_GROUP_ENTITY_TYPE, DtoDepthType.Deep);
        assertTrue((deepACategory.getParents() == null) || deepACategory.getParents().isEmpty());
        assertNotNull(deepACategory.getChildren());
        assertEquals(1, deepACategory.getChildren().size());
        assertTrue((deepACategory.isRoot() != null) && deepACategory.isRoot());
        DtoCategory deepBCategory = deepACategory.getChildren().get(0);
        assertEquals("TEST-B", deepBCategory.getName());
        assertNull(deepBCategory.getParents());
        assertNull(deepBCategory.getChildren());
        assertTrue((deepBCategory.isRoot() != null) && !deepBCategory.isRoot());
        // validate full category access
        DtoCategory fullACategory = client.lookup("TEST-A", SERVICE_GROUP_ENTITY_TYPE, DtoDepthType.Full);
        assertTrue((fullACategory.getParents() == null) || fullACategory.getParents().isEmpty());
        assertNotNull(fullACategory.getChildren());
        assertEquals(1, fullACategory.getChildren().size());
        assertTrue((fullACategory.isRoot() != null) && fullACategory.isRoot());
        DtoCategory fullBCategory = fullACategory.getChildren().get(0);
        assertEquals("TEST-B", fullBCategory.getName());
        assertNotNull(fullBCategory.getParents());
        assertEquals(1, fullBCategory.getParents().size());
        assertEquals("TEST-A", fullBCategory.getParents().get(0).getName());
        assertNull(fullBCategory.getParents().get(0).getParents());
        assertNull(fullBCategory.getParents().get(0).getChildren());
        assertNotNull(fullBCategory.getChildren());
        assertEquals(2, fullBCategory.getChildren().size());
        assertTrue((fullBCategory.isRoot() != null) && !fullBCategory.isRoot());
        DtoCategory fullCCategory = null;
        DtoCategory fullDCategory = null;
        for (DtoCategory child : fullBCategory.getChildren()) {
            if (child.getName().equals("TEST-C")) {
                fullCCategory = child;
            } else if (child.getName().equals("TEST-D")) {
                fullDCategory = child;
            }
        }
        assertNotNull(fullCCategory);
        assertNotNull(fullDCategory);
        assertNotNull(fullCCategory.getParents());
        assertEquals(1, fullCCategory.getParents().size());
        assertEquals("TEST-B", fullCCategory.getParents().get(0).getName());
        assertNull(fullCCategory.getParents().get(0).getParents());
        assertNull(fullCCategory.getParents().get(0).getChildren());
        assertTrue((fullCCategory.getChildren() == null) || fullCCategory.getChildren().isEmpty());
        assertTrue((fullCCategory.isRoot() != null) && !fullCCategory.isRoot());
        assertNotNull(fullDCategory.getParents());
        assertEquals(1, fullDCategory.getParents().size());
        assertEquals("TEST-B", fullDCategory.getParents().get(0).getName());
        assertNull(fullDCategory.getParents().get(0).getParents());
        assertNull(fullDCategory.getParents().get(0).getChildren());
        assertTrue((fullDCategory.getChildren() == null) || fullDCategory.getChildren().isEmpty());
        assertTrue((fullDCategory.isRoot() != null) && fullDCategory.isRoot());
        // delete hierarchies
        updates = new DtoCategoryUpdateList();
        update = new DtoCategoryUpdate();
        update.setCategoryName("TEST-A");
        update.setEntityTypeName(SERVICE_GROUP_ENTITY_TYPE);
        update.setDelete(CategoryClient.DELETE_CASCADE_ALL);
        updates.getCategoryUpdates().add(update);
        results = client.update(updates);
        assertNotNull(results);
        assertEquals(new Integer(1), results.getSuccessful());
        assertEquals(1, results.getResults().size());
        assertEquals("TEST-A:" + SERVICE_GROUP_ENTITY_TYPE, results.getResults().get(0).getEntity());
        assertEquals("Category deleted, (CASCADE_ALL).", results.getResults().get(0).getMessage());
        updates = new DtoCategoryUpdateList();
        update = new DtoCategoryUpdate();
        // verify hierarchy delete
        DtoCategory deletedACategory = client.lookup("TEST-A", SERVICE_GROUP_ENTITY_TYPE);
        assertNull(deletedACategory);
        DtoCategory deletedBCategory = client.lookup("TEST-B", SERVICE_GROUP_ENTITY_TYPE);
        assertNull(deletedBCategory);
        DtoCategory deletedCCategory = client.lookup("TEST-C", SERVICE_GROUP_ENTITY_TYPE);
        assertNull(deletedCCategory);
        DtoCategory deletedDCategory = client.lookup("TEST-D", SERVICE_GROUP_ENTITY_TYPE);
        assertNull(deletedDCategory);
    }

    private static void assertCategories(String [] categoryNames, Collection<DtoCategory> categories) {
        if ((categoryNames == null) || (categoryNames.length == 0)) {
            assertTrue((categories == null) || categories.isEmpty());
        } else {
            Collection<String> categoryNamesCollection = Arrays.asList(categoryNames);
            assertTrue((categories != null) && !categories.isEmpty());
            Set<String> foundCategoryNames = new HashSet<String>();
            for (DtoCategory category : categories) {
                if (categoryNamesCollection.contains(category.getName())) {
                    foundCategoryNames.add(category.getName());
                }
            }
            assertEquals(categoryNamesCollection.size(), foundCategoryNames.size());
        }
    }

    private static void assertNotCategories(String [] categoryNames, Collection<DtoCategory> categories) {
        if ((categoryNames != null) && (categoryNames.length > 0) && (categories != null) && !categories.isEmpty()) {
            Collection<String> categoryNamesCollection = Arrays.asList(categoryNames);
            for (DtoCategory category : categories) {
                assertFalse(categoryNamesCollection.contains(category.getName()));
            }
        }
    }

    @Test
    public void testCategoryDeleteFromCategory() throws Exception {
        if (serverDown) return;
        // allocate client
        CategoryClient client = new CategoryClient(getDeploymentURL());

        // create categories to delete and category with categories to delete
        DtoCategoryList categories = new DtoCategoryList();
        DtoCategory category0 = new DtoCategory();
        category0.setName("test-category-delete-from-category-0");
        category0.setEntityTypeName(SERVICE_GROUP_ENTITY_TYPE);
        categories.add(category0);
        DtoCategory category1 = new DtoCategory();
        category1.setName("test-category-delete-from-category-1");
        category1.setEntityTypeName(SERVICE_GROUP_ENTITY_TYPE);
        categories.add(category1);
        DtoOperationResults results = client.post(categories);
        assertNotNull(results);
        assertEquals(new Integer(2), results.getSuccessful());
        category0 = client.lookup("test-category-delete-from-category-0", SERVICE_GROUP_ENTITY_TYPE);
        assertNotNull(category0);
        category1 = client.lookup("test-category-delete-from-category-1", SERVICE_GROUP_ENTITY_TYPE);
        assertNotNull(category1);
        categories = new DtoCategoryList();
        DtoCategory category = new DtoCategory();
        category.setName("test-category-delete-from-category");
        category.setEntityTypeName(SERVICE_GROUP_ENTITY_TYPE);
        DtoCategoryEntity categoryEntity = new DtoCategoryEntity();
        categoryEntity.setObjectID(category0.getId());
        categoryEntity.setEntityTypeName(SERVICE_GROUP_ENTITY_TYPE);
        category.addEntity(categoryEntity);
        categoryEntity = new DtoCategoryEntity();
        categoryEntity.setObjectID(category1.getId());
        categoryEntity.setEntityTypeName(SERVICE_GROUP_ENTITY_TYPE);
        category.addEntity(categoryEntity);
        categories.add(category);
        results = client.post(categories);
        assertNotNull(results);
        assertEquals(new Integer(1), results.getSuccessful());
        category = client.lookup("test-category-delete-from-category", SERVICE_GROUP_ENTITY_TYPE, DtoDepthType.Deep);
        assertNotNull(category);
        assertNotNull(category.getEntities());
        assertEquals(2, category.getEntities().size());

        // delete categories
        results = client.delete("test-category-delete-from-category-0", SERVICE_GROUP_ENTITY_TYPE);
        assertNotNull(results);
        assertEquals(new Integer(1), results.getSuccessful());
        category0 = client.lookup("test-category-delete-from-category-0", SERVICE_GROUP_ENTITY_TYPE);
        assertNull(category0);
        DtoCategoryUpdateList categoryUpdates = new DtoCategoryUpdateList();
        DtoCategoryUpdate categoryUpdate = new DtoCategoryUpdate();
        categoryUpdate.setDelete(CategoryClient.DELETE_LEAF_ONLY);
        categoryUpdate.setCategoryName("test-category-delete-from-category-1");
        categoryUpdate.setEntityTypeName(SERVICE_GROUP_ENTITY_TYPE);
        categoryUpdates.add(categoryUpdate);
        results = client.update(categoryUpdates);
        assertNotNull(results);
        assertEquals(new Integer(1), results.getSuccessful());
        category1 = client.lookup("test-category-delete-from-category-1", SERVICE_GROUP_ENTITY_TYPE);
        assertNull(category1);

        // verify categories deleted from category
        category = client.lookup("test-category-delete-from-category", SERVICE_GROUP_ENTITY_TYPE, DtoDepthType.Deep);
        assertNotNull(category);
        assertTrue((category.getEntities() == null) || category.getEntities().isEmpty());

        // cleanup
        results = client.delete("test-category-delete-from-category", SERVICE_GROUP_ENTITY_TYPE);
        assertNotNull(results);
        assertEquals(new Integer(1), results.getSuccessful());
        category = client.lookup("test-category-delete-from-category", SERVICE_GROUP_ENTITY_TYPE);
        assertNull(category);
    }
}
