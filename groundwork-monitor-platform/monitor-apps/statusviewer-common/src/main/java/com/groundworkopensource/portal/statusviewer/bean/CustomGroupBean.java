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

package com.groundworkopensource.portal.statusviewer.bean;

import com.groundworkopensource.portal.model.CustomGroup;
import com.groundworkopensource.portal.model.CustomGroupElement;
import com.groundworkopensource.portal.model.EntityType;
import com.groundworkopensource.portal.statusviewer.common.Constant;
import org.apache.log4j.Logger;
import org.groundwork.foundation.ws.impl.WSClientConfiguration;
import org.groundwork.rs.client.CategoryClient;
import org.groundwork.rs.client.CustomGroupClient;
import org.groundwork.rs.client.HostGroupClient;
import org.groundwork.rs.client.ServiceGroupClient;
import org.groundwork.rs.dto.DtoCategoryUpdate;
import org.groundwork.rs.dto.DtoCategoryUpdateList;
import org.groundwork.rs.dto.DtoCustomGroup;
import org.groundwork.rs.dto.DtoCustomGroupMemberUpdate;
import org.groundwork.rs.dto.DtoCustomGroupUpdate;
import org.groundwork.rs.dto.DtoCustomGroupUpdateList;
import org.groundwork.rs.dto.DtoHostGroup;
import org.groundwork.rs.dto.DtoOperationResults;
import org.groundwork.rs.dto.DtoServiceGroup;

import javax.faces.model.SelectItem;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * CustomGroupBean
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public class CustomGroupBean extends AbstractCustomGroupBean {

    private static final Logger log = Logger.getLogger(CustomGroupBean.class);

    private static final String HOST_GROUP_ENTITY_TYPE_NAME = "HostGroup";
    private static final String SERVICE_GROUP_ENTITY_TYPE_NAME = "ServiceGroup";
    private static final String CUSTOM_GROUP_ENTITY_TYPE_NAME = "CustomGroup";

    private String deploymentUrl;

    private Map<Integer,String> hostGroupNames = new HashMap<Integer,String>();

    private Map<Integer,String> serviceGroupNames = new HashMap<Integer,String>();

    private Map<Integer,String> customGroupNames = new HashMap<Integer,String>();

    public CustomGroupBean() {
        // lookup REST client deployment configuration
        this.deploymentUrl = WSClientConfiguration.getProperty(WSClientConfiguration.FOUNDATION_REST_ENDPOINT);
        // refresh custom groups and base entities
        refresh();
    }

    @Override
    protected void deleteCustomGroup(CustomGroup group) throws Exception {
        // delete custom group
        CategoryClient categoryClient = new CategoryClient(deploymentUrl, MediaType.APPLICATION_JSON_TYPE);
        DtoCategoryUpdateList dtoCategoryUpdates = new DtoCategoryUpdateList();
        DtoCategoryUpdate dtoCategoryUpdate = new DtoCategoryUpdate();
        dtoCategoryUpdate.setDelete(CategoryClient.DELETE_LEAF_ONLY);
        dtoCategoryUpdate.setCategoryName(group.getGroupName());
        dtoCategoryUpdate.setEntityTypeName(CategoryClient.ENTITY_TYPE_CODE_CUSTOMGROUP);
        dtoCategoryUpdates.add(dtoCategoryUpdate);
        DtoOperationResults results = categoryClient.update(dtoCategoryUpdates);
        if ((results == null) || (results.getSuccessful() != 1)) {
            throw new RuntimeException("Custom group " + group.getGroupName() + " delete failed" +
                    getFailureMessage(results));
        }
    }

    @Override
    protected void refresh() {
        // refresh custom groups
        customGroups.clear();
        customGroupNames.clear();
        CustomGroupClient customGroupClient = new CustomGroupClient(deploymentUrl, MediaType.APPLICATION_JSON_TYPE);
        List<DtoCustomGroup> dtoCustomGroups = customGroupClient.list();
        Map<Integer,CustomGroup> customGroupsMap = new HashMap<Integer,CustomGroup>();
        for (DtoCustomGroup dtoCustomGroup : dtoCustomGroups) {
            CustomGroup customGroup = new CustomGroup();
            customGroup.setGroupId(dtoCustomGroup.getId());
            customGroup.setGroupName(dtoCustomGroup.getName());
            customGroup.setGroupState("P");
            customGroup.setEntityType(new EntityType());
            customGroup.setElements(new ArrayList<CustomGroupElement>());
            if ((dtoCustomGroup.getHostGroups() != null) && !dtoCustomGroup.getHostGroups().isEmpty()) {
                customGroup.getEntityType().setEntityType(Constant.DB_HOST_GROUP);
                customGroup.getEntityType().setEntityTypeId(HOST_GROUP_ENTITY_TYPE_ID);
                for (DtoHostGroup dtoHostGroup : dtoCustomGroup.getHostGroups()) {
                    CustomGroupElement customGroupElement = new CustomGroupElement();
                    customGroupElement.setElementId(dtoHostGroup.getId());
                    customGroupElement.setElementName(dtoHostGroup.getName());
                    customGroup.getElements().add(customGroupElement);
                }
            } else if ((dtoCustomGroup.getServiceGroups() != null) && !dtoCustomGroup.getServiceGroups().isEmpty()) {
                customGroup.getEntityType().setEntityType(Constant.DB_SERVICE_GROUP);
                customGroup.getEntityType().setEntityTypeId(SERVICE_GROUP_ENTITY_TYPE_ID);
                for (DtoServiceGroup dtoServiceGroup : dtoCustomGroup.getServiceGroups()) {
                    CustomGroupElement customGroupElement = new CustomGroupElement();
                    customGroupElement.setElementId(dtoServiceGroup.getId());
                    customGroupElement.setElementName(dtoServiceGroup.getName());
                    customGroup.getElements().add(customGroupElement);
                }
            } else {
                customGroup.getEntityType().setEntityType(Constant.DB_CUSTOM_GROUP);
                customGroup.getEntityType().setEntityTypeId(CUSTOM_GROUP_ENTITY_TYPE_ID);
                if ((dtoCustomGroup.getChildren() != null) && !dtoCustomGroup.getChildren().isEmpty()) {
                    for (DtoCustomGroup dtoChildCustomGroup : dtoCustomGroup.getChildren()) {
                        CustomGroupElement customGroupElement = new CustomGroupElement();
                        customGroupElement.setElementId(dtoChildCustomGroup.getId());
                        customGroupElement.setElementName(dtoChildCustomGroup.getName());
                        customGroup.getElements().add(customGroupElement);
                    }
                }
            }
            customGroups.add(customGroup);
            customGroupNames.put((int)customGroup.getGroupId(), customGroup.getGroupName());
            customGroupsMap.put((int)customGroup.getGroupId(), customGroup);
        }
        // set custom group parents
        for (CustomGroup customGroup : customGroups) {
            if ((customGroup.getEntityType().getEntityTypeId() == CUSTOM_GROUP_ENTITY_TYPE_ID) &&
                    !customGroup.getElements().isEmpty()) {
                for (CustomGroupElement customGroupElement : customGroup.getElements()) {
                    CustomGroup childCustomGroup = customGroupsMap.get((int)customGroupElement.getElementId());
                    if (childCustomGroup != null) {
                        if (childCustomGroup.getParents() == null) {
                            childCustomGroup.setParents(new ArrayList<CustomGroup>());
                        }
                        childCustomGroup.getParents().add(customGroup);
                    }
                }
            }
        }
        // refresh host groups, service groups, entity types, and custom groups base entities
        refreshBaseEntities();
        // set message if no custom groups available
        if (customGroups.isEmpty()) {
            message = "No CustomGroups Available!";
        }
    }

    @Override
    protected void refreshBaseEntities() {
        // refresh host groups base entities
        hostGroups.clear();
        hostGroupNames.clear();
        HostGroupClient hostGroupClient = new HostGroupClient(deploymentUrl, MediaType.APPLICATION_JSON_TYPE);
        List<DtoHostGroup> dtoHostGroups = hostGroupClient.list();
        for (DtoHostGroup dtoHostGroup : dtoHostGroups) {
            SelectItem hostGroup = new SelectItem(dtoHostGroup.getId(), dtoHostGroup.getName(), "", false);
            hostGroups.addAvailableItem(hostGroup);
            hostGroupNames.put(dtoHostGroup.getId(), dtoHostGroup.getName());
        }
        // refresh host groups base entities
        serviceGroups.clear();
        serviceGroupNames.clear();
        ServiceGroupClient serviceGroupClient = new ServiceGroupClient(deploymentUrl, MediaType.APPLICATION_JSON_TYPE);
        List<DtoServiceGroup> dtoServiceGroups = serviceGroupClient.list();
        for (DtoServiceGroup dtoServiceGroup : dtoServiceGroups) {
            SelectItem serviceGroup = new SelectItem(dtoServiceGroup.getId(), dtoServiceGroup.getName(), "", false);
            serviceGroups.addAvailableItem(serviceGroup);
            serviceGroupNames.put(dtoServiceGroup.getId(), dtoServiceGroup.getName());
        }
        // refresh custom groups entity types base entities, (static hard coded)
        if (entityTypes.isEmpty()) {
            entityTypes.add(new SelectItem(HOST_GROUP_ENTITY_TYPE_ID, HOST_GROUP_ENTITY_TYPE_NAME, "", false));
            entityTypes.add(new SelectItem(SERVICE_GROUP_ENTITY_TYPE_ID, SERVICE_GROUP_ENTITY_TYPE_NAME, "", false));
            entityTypes.add(new SelectItem(CUSTOM_GROUP_ENTITY_TYPE_ID, CUSTOM_GROUP_ENTITY_TYPE_NAME, "", false));
        }
        // refresh custom groups base entities
        customGroupSelectItems.clear();
        for (CustomGroup customGroup : customGroups) {
            SelectItem customGroupSelectItem = new SelectItem(customGroup.getGroupId(), customGroup.getGroupName(), "",
                    false);
            customGroupSelectItems.addAvailableItem(customGroupSelectItem);
        }
    }

    @Override
    protected String lookupHostGroupName(int id) {
        return hostGroupNames.get(id);
    }

    @Override
    protected String lookupServiceGroupName(int id) {
        return serviceGroupNames.get(id);
    }

    @Override
    protected void saveCustomGroup(CustomGroup group, Collection<Long> parents, Collection<Long> children,
                                   String userName, String state, boolean isCreate) throws Exception {
        // lookup or create custom group
        CustomGroupClient customGroupClient = new CustomGroupClient(deploymentUrl, MediaType.APPLICATION_JSON_TYPE);
        DtoCustomGroup dtoCustomGroup = customGroupClient.lookup(group.getGroupName());
        if (dtoCustomGroup == null) {
            if (isCreate) {
                // create new custom group
                DtoCustomGroupUpdateList dtoCustomGroupUpdateList = new DtoCustomGroupUpdateList();
                DtoCustomGroupUpdate dtoCustomGroupUpdate = new DtoCustomGroupUpdate();
                dtoCustomGroupUpdate.setName(group.getGroupName());
                dtoCustomGroupUpdateList.add(dtoCustomGroupUpdate);
                DtoOperationResults results = customGroupClient.post(dtoCustomGroupUpdateList);
                if ((results == null) || (results.getSuccessful() != 1)) {
                    throw new RuntimeException("Custom group " + group.getGroupName() +
                            " create failed" + getFailureMessage(results));
                }
                dtoCustomGroup = customGroupClient.lookup(group.getGroupName());
                if (dtoCustomGroup == null) {
                    throw new RuntimeException("Custom group " + group.getGroupName() + " not found");
                }
            } else {
                throw new RuntimeException("Custom group " + group.getGroupName() + " not found");
            }
        } else if (isCreate) {
            throw new RuntimeException("Duplicate custom group " + group.getGroupName() + " found");
        }

        // make best effort to modify custom group
        Exception exception = null;
        try {
            // update custom group host groups
            Set<Integer> hostGroupIds = new HashSet<Integer>();
            if ((dtoCustomGroup.getHostGroups() != null) && !dtoCustomGroup.getHostGroups().isEmpty()) {
                for (DtoHostGroup dtoHostGroup : dtoCustomGroup.getHostGroups()) {
                    hostGroupIds.add(dtoHostGroup.getId());
                }
            }
            Set<Integer> saveHostGroupIds = new HashSet<Integer>();
            if ((group.getEntityType().getEntityTypeId() == HOST_GROUP_ENTITY_TYPE_ID) &&
                    (children != null) && !children.isEmpty()) {
                for (Long childId : children) {
                    saveHostGroupIds.add(childId.intValue());
                }
            }
            Set<Integer> hostGroupIdsToDelete = new HashSet<Integer>(hostGroupIds);
            hostGroupIdsToDelete.removeAll(saveHostGroupIds);
            if (!hostGroupIdsToDelete.isEmpty()) {
                // get host group names to delete from ids
                DtoCustomGroupMemberUpdate dtoCustomGroupMemberUpdate = new DtoCustomGroupMemberUpdate();
                dtoCustomGroupMemberUpdate.setName(dtoCustomGroup.getName());
                for (Integer hostGroupIdToDelete : hostGroupIdsToDelete) {
                    String hostGroupNameToDelete = hostGroupNames.get(hostGroupIdToDelete);
                    if (hostGroupNameToDelete != null) {
                        dtoCustomGroupMemberUpdate.addHostGroupNames(hostGroupNameToDelete);
                    }
                }
                // delete host groups
                if (dtoCustomGroupMemberUpdate.getHostGroupNames() != null) {
                    DtoOperationResults results = customGroupClient.deleteMembers(dtoCustomGroupMemberUpdate);
                    if ((results == null) || (results.getSuccessful() != 1)) {
                        exception = new RuntimeException("Custom group " + group.getGroupName() +
                                " delete members failed" + getFailureMessage(results));
                    }
                }
            }
            Set<Integer> hostGroupIdsToAdd = new HashSet<Integer>(saveHostGroupIds);
            hostGroupIdsToAdd.removeAll(hostGroupIds);
            if (!hostGroupIdsToAdd.isEmpty()) {
                // get host group names to add from ids
                DtoCustomGroupMemberUpdate dtoCustomGroupMemberUpdate = new DtoCustomGroupMemberUpdate();
                dtoCustomGroupMemberUpdate.setName(dtoCustomGroup.getName());
                for (Integer hostGroupIdToAdd : hostGroupIdsToAdd) {
                    String hostGroupNameToAdd = hostGroupNames.get(hostGroupIdToAdd);
                    if (hostGroupNameToAdd != null) {
                        dtoCustomGroupMemberUpdate.addHostGroupNames(hostGroupNameToAdd);
                    }
                }
                // add host groups
                if (dtoCustomGroupMemberUpdate.getHostGroupNames() != null) {
                    DtoOperationResults results = customGroupClient.addMembers(dtoCustomGroupMemberUpdate);
                    if ((results == null) || (results.getSuccessful() != 1)) {
                        exception = new RuntimeException("Custom group " + group.getGroupName() +
                                " add members failed" + getFailureMessage(results));
                    }
                }
            }

            // update custom group service groups
            Set<Integer> serviceGroupIds = new HashSet<Integer>();
            if ((dtoCustomGroup.getServiceGroups() != null) && !dtoCustomGroup.getServiceGroups().isEmpty()) {
                for (DtoServiceGroup dtoServiceGroup : dtoCustomGroup.getServiceGroups()) {
                    serviceGroupIds.add(dtoServiceGroup.getId());
                }
            }
            Set<Integer> saveServiceGroupIds = new HashSet<Integer>();
            if ((group.getEntityType().getEntityTypeId() == SERVICE_GROUP_ENTITY_TYPE_ID) &&
                    (children != null) && !children.isEmpty()) {
                for (Long childId : children) {
                    saveServiceGroupIds.add(childId.intValue());
                }
            }
            Set<Integer> serviceGroupIdsToDelete = new HashSet<Integer>(serviceGroupIds);
            serviceGroupIdsToDelete.removeAll(saveServiceGroupIds);
            if (!serviceGroupIdsToDelete.isEmpty()) {
                // get service group names to delete from ids
                DtoCustomGroupMemberUpdate dtoCustomGroupMemberUpdate = new DtoCustomGroupMemberUpdate();
                dtoCustomGroupMemberUpdate.setName(dtoCustomGroup.getName());
                for (Integer serviceGroupIdToDelete : serviceGroupIdsToDelete) {
                    String serviceGroupNameToDelete = serviceGroupNames.get(serviceGroupIdToDelete);
                    if (serviceGroupNameToDelete != null) {
                        dtoCustomGroupMemberUpdate.addServiceGroupNames(serviceGroupNameToDelete);
                    }
                }
                // delete service groups
                if (dtoCustomGroupMemberUpdate.getServiceGroupNames() != null) {
                    DtoOperationResults results = customGroupClient.deleteMembers(dtoCustomGroupMemberUpdate);
                    if ((results == null) || (results.getSuccessful() != 1)) {
                        exception = new RuntimeException("Custom group " + group.getGroupName() +
                                " delete members failed" + getFailureMessage(results));
                    }
                }
            }
            Set<Integer> serviceGroupIdsToAdd = new HashSet<Integer>(saveServiceGroupIds);
            serviceGroupIdsToAdd.removeAll(serviceGroupIds);
            if (!serviceGroupIdsToAdd.isEmpty()) {
                // get service group names to add from ids
                DtoCustomGroupMemberUpdate dtoCustomGroupMemberUpdate = new DtoCustomGroupMemberUpdate();
                dtoCustomGroupMemberUpdate.setName(dtoCustomGroup.getName());
                for (Integer serviceGroupIdToAdd : serviceGroupIdsToAdd) {
                    String serviceGroupNameToAdd = serviceGroupNames.get(serviceGroupIdToAdd);
                    if (serviceGroupNameToAdd != null) {
                        dtoCustomGroupMemberUpdate.addServiceGroupNames(serviceGroupNameToAdd);
                    }
                }
                // add service groups
                if (dtoCustomGroupMemberUpdate.getServiceGroupNames() != null) {
                    DtoOperationResults results = customGroupClient.addMembers(dtoCustomGroupMemberUpdate);
                    if ((results == null) || (results.getSuccessful() != 1)) {
                        exception = new RuntimeException("Custom group " + group.getGroupName() +
                                " add members failed" + getFailureMessage(results));
                    }
                }
            }

            // update custom group custom groups children
            Set<Integer> customGroupIds = new HashSet<Integer>();
            if ((dtoCustomGroup.getChildren() != null) && !dtoCustomGroup.getChildren().isEmpty()) {
                for (DtoCustomGroup dtoChildCustomGroup : dtoCustomGroup.getChildren()) {
                    customGroupIds.add(dtoChildCustomGroup.getId());
                }
            }
            Set<Integer> saveCustomGroupIds = new HashSet<Integer>();
            if ((group.getEntityType().getEntityTypeId() == CUSTOM_GROUP_ENTITY_TYPE_ID) &&
                    (children != null) && !children.isEmpty()) {
                for (Long childId : children) {
                    saveCustomGroupIds.add(childId.intValue());
                }
            }
            Set<Integer> customGroupIdsToAdd = new HashSet<Integer>(saveCustomGroupIds);
            customGroupIdsToAdd.removeAll(customGroupIds);
            if (!customGroupIdsToAdd.isEmpty()) {
                // get custom group names to add from ids
                List<String> customGroupNamesToAdd = new ArrayList<String>();
                for (Integer customGroupIdToAdd : customGroupIdsToAdd) {
                    String customGroupNameToAdd = customGroupNames.get(customGroupIdToAdd);
                    if (customGroupNameToAdd != null) {
                        customGroupNamesToAdd.add(customGroupNameToAdd);
                    }
                }
                // add custom group children
                if (!customGroupNamesToAdd.isEmpty()) {
                    CategoryClient categoryClient = new CategoryClient(deploymentUrl, MediaType.APPLICATION_JSON_TYPE);
                    DtoCategoryUpdateList dtoCategoryUpdates = new DtoCategoryUpdateList();
                    DtoCategoryUpdate dtoCategoryUpdate = new DtoCategoryUpdate();
                    dtoCategoryUpdate.setModify(CategoryClient.MODIFY_ADD_CHILDREN_UNROOT);
                    dtoCategoryUpdate.setCategoryName(dtoCustomGroup.getName());
                    dtoCategoryUpdate.setEntityTypeName(CategoryClient.ENTITY_TYPE_CODE_CUSTOMGROUP);
                    dtoCategoryUpdate.setOtherCategoryNames(customGroupNamesToAdd);
                    dtoCategoryUpdates.add(dtoCategoryUpdate);
                    DtoOperationResults results = categoryClient.update(dtoCategoryUpdates);
                    if ((results == null) || (results.getSuccessful() != 1)) {
                        exception = new RuntimeException("Custom group " + group.getGroupName() +
                                " modify add children failed" + getFailureMessage(results));
                    }
                }
            }
            Set<Integer> customGroupIdsToDelete = new HashSet<Integer>(customGroupIds);
            customGroupIdsToDelete.removeAll(saveCustomGroupIds);
            if (!customGroupIdsToDelete.isEmpty()) {
                // get custom group names to delete from ids
                List<String> customGroupNamesToDelete = new ArrayList<String>();
                for (Integer customGroupIdToDelete : customGroupIdsToDelete) {
                    String customGroupNameToDelete = customGroupNames.get(customGroupIdToDelete);
                    if (customGroupNameToDelete != null) {
                        customGroupNamesToDelete.add(customGroupNameToDelete);
                    }
                }
                // delete custom group children
                if (!customGroupNamesToDelete.isEmpty()) {
                    CategoryClient categoryClient = new CategoryClient(deploymentUrl, MediaType.APPLICATION_JSON_TYPE);
                    DtoCategoryUpdateList dtoCategoryUpdates = new DtoCategoryUpdateList();
                    DtoCategoryUpdate dtoCategoryUpdate = new DtoCategoryUpdate();
                    dtoCategoryUpdate.setModify(CategoryClient.MODIFY_REMOVE_CHILDREN);
                    dtoCategoryUpdate.setCategoryName(dtoCustomGroup.getName());
                    dtoCategoryUpdate.setEntityTypeName(CategoryClient.ENTITY_TYPE_CODE_CUSTOMGROUP);
                    dtoCategoryUpdate.setOtherCategoryNames(customGroupNamesToDelete);
                    dtoCategoryUpdates.add(dtoCategoryUpdate);
                    DtoOperationResults results = categoryClient.update(dtoCategoryUpdates);
                    if ((results == null) || (results.getSuccessful() != 1)) {
                        exception = new RuntimeException("Custom group " + group.getGroupName() +
                                " modify remove children failed" + getFailureMessage(results));
                    }
                }
            }
        } catch (Exception e) {
            exception = e;
        }

        // cleanup created custom group silently on error
        if (isCreate && (exception != null)) {
            try {
                CategoryClient categoryClient = new CategoryClient(deploymentUrl, MediaType.APPLICATION_JSON_TYPE);
                DtoCategoryUpdateList dtoCategoryUpdates = new DtoCategoryUpdateList();
                DtoCategoryUpdate dtoCategoryUpdate = new DtoCategoryUpdate();
                dtoCategoryUpdate.setDelete(CategoryClient.DELETE_ORPHAN_CHILDREN_AS_ROOTS);
                dtoCategoryUpdate.setCategoryName(group.getGroupName());
                dtoCategoryUpdate.setEntityTypeName(CategoryClient.ENTITY_TYPE_CODE_CUSTOMGROUP);
                dtoCategoryUpdates.add(dtoCategoryUpdate);
                categoryClient.update(dtoCategoryUpdates);
            } catch (Exception e) {
            }
        }

        // refresh custom groups
        refresh();

        // throw exception on error
        if (exception != null) {
            throw exception;
        }
    }

    /**
     * Get message from single failed result.
     *
     * @param results results to extract message from
     * @return message or empty string
     */
    private String getFailureMessage(DtoOperationResults results) {
        if ((results.getFailed() != null) && (results.getFailed() == 1) &&
                (results.getResults() != null) && (results.getResults().size() == 1)) {
            return ", [" + results.getResults().get(0).getMessage() +"]";
        }
        return "";
    }
}
