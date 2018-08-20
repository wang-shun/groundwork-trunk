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

package org.groundwork.rs.client;

import com.groundworkopensource.portal.model.CustomGroup;
import com.groundworkopensource.portal.model.CustomGroupElement;
import com.groundworkopensource.portal.model.CustomGroupList;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.rs.dto.DtoCategoryEntity;
import org.groundwork.rs.dto.DtoCategoryMemberUpdate;
import org.groundwork.rs.dto.DtoCategoryUpdate;
import org.groundwork.rs.dto.DtoCategoryUpdateList;
import org.groundwork.rs.dto.DtoCustomGroup;
import org.groundwork.rs.dto.DtoOperationResults;

import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * PortalCustomGroupMigration
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public class PortalCustomGroupMigration {

    protected static Log log = LogFactory.getLog(PortalCustomGroupMigration.class);

    public static final String DB_HOST_GROUP = "HostGroup";
    public static final String DB_SERVICE_GROUP = "ServiceGroup";
    public static final String DB_CUSTOM_GROUP = "CustomGroup";

    /**
     * Migration required check.
     *
     * @param portalDeploymentUrl portal deployment url
     * @param deploymentUrl foundation deployment url
     * @return migration required
     */
    public static boolean required(String portalDeploymentUrl, String deploymentUrl) {
        // get clients
        PortalCustomGroupClient portalCustomGroupClient = new PortalCustomGroupClient(portalDeploymentUrl);
        CustomGroupClient customGroupClient = new CustomGroupClient(deploymentUrl, MediaType.APPLICATION_JSON_TYPE);

        // get portal custom groups and custom groups
        CustomGroupList portalCustomGroups = portalCustomGroupClient.findCustomGroups();
        List<DtoCustomGroup> customGroups = customGroupClient.list();

        // migration required if portal custom groups exist without custom groups
        return ((portalCustomGroups != null) && (portalCustomGroups.getList() != null) &&
                !portalCustomGroups.getList().isEmpty() && customGroups.isEmpty());
    }

    /**
     * Migrate portal custom groups to custom group categories. Empty
     * portal custom group hierarchies and orphaned host or service group
     * references are filtered. All portal custom groups are removed after
     * migration is complete.
     *
     * @param portalDeploymentUrl portal deployment url
     * @param deploymentUrl foundation deployment url
     * @param dryrun flag to suppress permanent changes
     * @return migration successful
     */
    public static boolean perform(String portalDeploymentUrl, String deploymentUrl, boolean dryrun) {
        // get clients
        PortalCustomGroupClient portalCustomGroupClient = new PortalCustomGroupClient(portalDeploymentUrl);
        CategoryClient categoryClient = new CategoryClient(deploymentUrl, MediaType.APPLICATION_JSON_TYPE);
        CustomGroupClient customGroupClient = new CustomGroupClient(deploymentUrl, MediaType.APPLICATION_JSON_TYPE);
        HostGroupClient hostGroupClient = new HostGroupClient(deploymentUrl, MediaType.APPLICATION_JSON_TYPE);
        ServiceGroupClient serviceGroupClient = new ServiceGroupClient(deploymentUrl, MediaType.APPLICATION_JSON_TYPE);

        // get portal custom groups and custom groups
        CustomGroupList portalCustomGroups = portalCustomGroupClient.findCustomGroups();
        List<DtoCustomGroup> customGroups = customGroupClient.list();

        // validate migration required
        if ((portalCustomGroups == null) || (portalCustomGroups.getList() == null) ||
                portalCustomGroups.getList().isEmpty() || !customGroups.isEmpty()) {
            return false;
        }

        // start migration
        log.info("Starting portal custom group migration...");

        // create map of portal custom groups
        Map<Long,CustomGroup> portalCustomGroupsMap = new HashMap<Long,CustomGroup>();
        Set<Long> rootPortalCustomGroupIds = new HashSet<Long>();
        for (CustomGroup portalCustomGroup : portalCustomGroups.getList()) {
            // map portal custom groups
            portalCustomGroupsMap.put(portalCustomGroup.getGroupId(), portalCustomGroup);
            // determine root portal custom groups
            if (portalCustomGroup.getGroupState().equalsIgnoreCase("P") &&
                    ((portalCustomGroup.getParents() == null) || portalCustomGroup.getParents().isEmpty())) {
                rootPortalCustomGroupIds.add(portalCustomGroup.getGroupId());
            }
        }

        // validate portal custom groups in multiple passes bubbling valid state
        // up through the hierarchy from the host group and service group leaves;
        // validates host and service group existence, pruning invalid groups
        Set<Long> validPortalCustomGroupIds = new HashSet<Long>();
        Set<Long> invalidPortalCustomGroupIds = new HashSet<Long>();
        for (;;) {
            boolean validatedAnyPortalCustomGroup = false;
            for (CustomGroup portalCustomGroup : portalCustomGroups.getList()) {
                // validate portal custom groups that are not valid or invalid
                String portalCustomGroupName = portalCustomGroup.getGroupName();
                long portalCustomGroupId = portalCustomGroup.getGroupId();
                if (!validPortalCustomGroupIds.contains(portalCustomGroupId) &&
                        !invalidPortalCustomGroupIds.contains(portalCustomGroupId)) {
                    // validate portal custom group published state
                    if (!portalCustomGroup.getGroupState().equalsIgnoreCase("P")) {
                        invalidPortalCustomGroupIds.add(portalCustomGroupId);
                        validatedAnyPortalCustomGroup = true;
                        log.info("Skipping migration of unpublished portal custom group: " + portalCustomGroupName);
                        continue;
                    }
                    // validate portal custom group elements
                    String entityTypeName = portalCustomGroup.getEntityType().getEntityType();
                    Iterator<CustomGroupElement> elementIter = portalCustomGroup.getElements().iterator();
                    boolean elementsKnownValid = true;
                    if (entityTypeName.equalsIgnoreCase(DB_HOST_GROUP)) {
                        // validate and prune invalid host groups
                        for (; elementIter.hasNext(); ) {
                            long hostGroupId = elementIter.next().getElementId();
                            if (hostGroupClient.query("id = " + hostGroupId).isEmpty()) {
                                elementIter.remove();
                            }
                        }
                    } else if (entityTypeName.equalsIgnoreCase(DB_SERVICE_GROUP)) {
                        // validate and prune invalid service groups
                        for (; elementIter.hasNext(); ) {
                            long serviceGroupId = elementIter.next().getElementId();
                            if (serviceGroupClient.query("id = " + serviceGroupId).isEmpty()) {
                                elementIter.remove();
                            }
                        }
                    } else if (entityTypeName.equalsIgnoreCase(DB_CUSTOM_GROUP)) {
                        // validate and prune invalid child portal custom groups
                        for (; elementIter.hasNext(); ) {
                            long childPortalCustomGroupId = elementIter.next().getElementId();
                            if (!portalCustomGroupsMap.containsKey(childPortalCustomGroupId) ||
                                    invalidPortalCustomGroupIds.contains(childPortalCustomGroupId)) {
                                elementIter.remove();
                            } else if (!validPortalCustomGroupIds.contains(childPortalCustomGroupId)) {
                                elementsKnownValid = false;
                                break;
                            }
                        }
                    }
                    // if portal custom group elements have been validated, mark
                    // portal custom group valid or invalid
                    if (elementsKnownValid) {
                        if (!portalCustomGroup.getElements().isEmpty()) {
                            validPortalCustomGroupIds.add(portalCustomGroupId);
                            log.info("Migration portal custom group candidate: " + portalCustomGroupName);
                        } else {
                            invalidPortalCustomGroupIds.add(portalCustomGroupId);
                            log.info("Skipping migration of empty portal custom group: " + portalCustomGroupName);
                        }
                        validatedAnyPortalCustomGroup = true;
                    }
                }
            }
            // continue unless no portal custom groups validated in this pass
            if (!validatedAnyPortalCustomGroup) {
                break;
            }
        }

        // perform portal custom group migration
        boolean migrationFailed = true;
        try {
            // migrate from valid root portal custom groups
            for (Long rootPortalCustomGroupId : rootPortalCustomGroupIds) {
                if (validPortalCustomGroupIds.contains(rootPortalCustomGroupId)) {
                    // get valid root portal custom group
                    CustomGroup rootPortalCustomGroup = portalCustomGroupsMap.get(rootPortalCustomGroupId);
                    // migrate to root custom group category
                    createRootCustomGroup(rootPortalCustomGroup, portalCustomGroupsMap, categoryClient);
                }
            }
            migrationFailed = false;

            if (!dryrun) {
                // unlink all portal custom groups
                for (CustomGroup unlinkPortalCustomGroup : portalCustomGroups.getList()) {
                    if (portalCustomGroupClient.updateCustomGroup(unlinkPortalCustomGroup.getGroupName(),
                            (int) unlinkPortalCustomGroup.getEntityType().getEntityTypeId(), null,
                            unlinkPortalCustomGroup.getGroupState(), unlinkPortalCustomGroup.getCreatedBy(), null) == null) {
                        throw new RuntimeException("Unable to unlink portal custom group: " + unlinkPortalCustomGroup.getGroupName());
                    }
                }
                // remove all portal custom groups
                for (CustomGroup removePortalCustomGroup : portalCustomGroups.getList()) {
                    if (portalCustomGroupClient.removeCustomGroup(Long.toString(removePortalCustomGroup.getGroupId())) == null) {
                        throw new RuntimeException("Unable to remove portal custom group: " + removePortalCustomGroup.getGroupName());
                    }
                }
                // verify all portal custom groups removed
                portalCustomGroups = portalCustomGroupClient.findCustomGroups();
                if ((portalCustomGroups.getList() != null) && !portalCustomGroups.getList().isEmpty()) {
                    throw new RuntimeException("Unable to remove all portal custom groups");
                }
            }

            // migrated successfully
            log.info("Portal custom group migration complete.");
            return true;
        } finally {
            // remove all custom groups for dryrun or migration failed cleanup
            if (dryrun || migrationFailed) {
                try {
                    customGroups = customGroupClient.list();
                    if (!customGroups.isEmpty()) {
                        List<String> allCustomGroupNames = new ArrayList<String>();
                        for (DtoCustomGroup customGroup : customGroups) {
                            allCustomGroupNames.add(customGroup.getName());
                        }
                        DtoOperationResults results = customGroupClient.delete(allCustomGroupNames);
                        if (results.getSuccessful() != allCustomGroupNames.size()) {
                            throw new RuntimeException("Cleanup delete failed.");
                        }
                    }
                    log.info("Portal custom group migration dryrun or failure cleanup complete.");
                } catch (Exception e) {
                    String message = "Unable to remove all custom groups for dryrun or failure cleanup";
                    if (dryrun) {
                        throw new RuntimeException(message, e);
                    } else {
                        log.error(message + ": " + e, e);
                    }
                }
            }
        }
    }

    /**
     * Migrate valid portal custom group and all children recursively to
     * a new custom group category hierarchy. Assumes that all child custom
     * groups, host groups, and service groups are valid. Custom group
     * categories are created with names matching the original portal
     * custom groups.
     *
     * @param rootPortalCustomGroup root portal custom group
     * @param portalCustomGroupsMap map of portal custom groups
     * @param categoryClient category client used create custom groups
     */
    private static void createRootCustomGroup(CustomGroup rootPortalCustomGroup,
                                              Map<Long,CustomGroup> portalCustomGroupsMap,
                                              CategoryClient categoryClient) {
        createCustomGroup(rootPortalCustomGroup, null, portalCustomGroupsMap, categoryClient);
    }

    /**
     * Migrate valid portal custom group and all children recursively to
     * a new custom group category hierarchy. Assumes that all child custom
     * groups, host groups, and service groups are valid. Custom group
     * categories are created with names matching the original portal
     * custom groups.
     *
     * @param portalCustomGroup portal custom group
     * @param parentPortalCustomGroup parent portal custom group or null for root
     * @param portalCustomGroupsMap map of portal custom groups
     * @param categoryClient category client used create custom groups
     */
    private static void createCustomGroup(CustomGroup portalCustomGroup, CustomGroup parentPortalCustomGroup,
                                          Map<Long,CustomGroup> portalCustomGroupsMap, CategoryClient categoryClient) {
        String portalCustomGroupName = portalCustomGroup.getGroupName();
        // check to see if category already exists
        if (categoryClient.lookup(portalCustomGroupName, CategoryClient.ENTITY_TYPE_CODE_CUSTOMGROUP) != null) {
            // merge existing category with created parent
            if (parentPortalCustomGroup != null) {
                DtoCategoryUpdateList categoryUpdateList = new DtoCategoryUpdateList();
                DtoCategoryUpdate categoryUpdate = new DtoCategoryUpdate();
                categoryUpdate.setModify(CategoryClient.MODIFY_ADD_CHILD);
                categoryUpdate.setCategoryName(portalCustomGroupName);
                categoryUpdate.setEntityTypeName(CategoryClient.ENTITY_TYPE_CODE_CUSTOMGROUP);
                categoryUpdate.setOtherCategoryNames(Arrays.asList(new String[]{parentPortalCustomGroup.getGroupName()}));
                categoryUpdateList.add(categoryUpdate);
                DtoOperationResults results = categoryClient.update(categoryUpdateList);
                if ((results == null) || (results.getSuccessful() != 1)) {
                    throw new RuntimeException("Unable to merge custom group category: " + portalCustomGroupName);
                }
                log.info("Migrated portal custom group: " + portalCustomGroupName + ", (merged)");
            }
        } else {
            // create custom group category from portal custom group
            DtoCategoryUpdateList categoryUpdateList = new DtoCategoryUpdateList();
            DtoCategoryUpdate categoryUpdate = new DtoCategoryUpdate();
            if (parentPortalCustomGroup == null) {
                categoryUpdate.setCreate(CategoryClient.CREATE_AS_ROOT);
            } else {
                categoryUpdate.setCreate(CategoryClient.CREATE_AS_CHILD);
                categoryUpdate.setParentName(parentPortalCustomGroup.getGroupName());
            }
            categoryUpdate.setCategoryName(portalCustomGroupName);
            categoryUpdate.setEntityTypeName(CategoryClient.ENTITY_TYPE_CODE_CUSTOMGROUP);
            categoryUpdateList.add(categoryUpdate);
            DtoOperationResults results = categoryClient.update(categoryUpdateList);
            if ((results == null) || (results.getSuccessful() != 1)) {
                throw new RuntimeException("Unable to create custom group category: " + portalCustomGroupName);
            }
            log.info("Migrated portal custom group: " + portalCustomGroupName +
                    ((parentPortalCustomGroup == null) ? ", (as root)" : ""));
            // populate custom group category members or children
            String entityTypeName = portalCustomGroup.getEntityType().getEntityType();
            if (entityTypeName.equalsIgnoreCase(DB_HOST_GROUP)) {
                // host group members
                addGroupMembersToCustomGroup(portalCustomGroup, CategoryClient.ENTITY_TYPE_CODE_HOSTGROUP, categoryClient);
            } else if (entityTypeName.equalsIgnoreCase(DB_SERVICE_GROUP)) {
                // service group members
                addGroupMembersToCustomGroup(portalCustomGroup, CategoryClient.ENTITY_TYPE_CODE_SERVICEGROUP, categoryClient);
            } else if (entityTypeName.equalsIgnoreCase(DB_CUSTOM_GROUP)) {
                // create custom group children
                for (CustomGroupElement portalCustomGroupElement : portalCustomGroup.getElements()) {
                    CustomGroup childPortalCustomGroup = portalCustomGroupsMap.get(portalCustomGroupElement.getElementId());
                    createCustomGroup(childPortalCustomGroup, portalCustomGroup, portalCustomGroupsMap, categoryClient);
                }
            }
        }
    }

    /**
     * Add portal custom group host and service group elements to migrated
     * custom group categories. Assumes referenced host and service groups
     * are valid and custom group categories have been created with names
     * matching the original portal custom groups.
     *
     * @param portalCustomGroup portal custom group
     * @param groupMemberEntityTypeName host or service group entity type name
     * @param categoryClient category client used create custom groups
     */
    private static void addGroupMembersToCustomGroup(CustomGroup portalCustomGroup, String groupMemberEntityTypeName,
                                                     CategoryClient categoryClient) {
        String portalCustomGroupName = portalCustomGroup.getGroupName();
        // add custom group category members
        DtoCategoryMemberUpdate categoryMemberUpdate = new DtoCategoryMemberUpdate();
        categoryMemberUpdate.setName(portalCustomGroupName);
        categoryMemberUpdate.setEntityTypeName(CategoryClient.ENTITY_TYPE_CODE_CUSTOMGROUP);
        for (CustomGroupElement groupMemberElement : portalCustomGroup.getElements()) {
            DtoCategoryEntity categoryEntity = new DtoCategoryEntity();
            categoryEntity.setObjectID((int) groupMemberElement.getElementId());
            categoryEntity.setEntityTypeName(groupMemberEntityTypeName);
            categoryMemberUpdate.addEntity(categoryEntity);
        }
        DtoOperationResults results = categoryClient.addMembers(categoryMemberUpdate);
        if ((results == null) || (results.getSuccessful() != 1)) {
            throw new RuntimeException("Unable to create custom group category members: "+portalCustomGroupName);
        }
        log.info("Populated migrated portal custom group: "+portalCustomGroupName+", members: "+groupMemberEntityTypeName);
    }
}
