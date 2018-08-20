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

import com.groundworkopensource.portal.common.FacesUtils;
import com.groundworkopensource.portal.model.CustomGroup;
import com.groundworkopensource.portal.model.CustomGroupElement;
import com.groundworkopensource.portal.statusviewer.common.Constant;
import org.apache.log4j.Logger;

import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import javax.faces.application.Application;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * AbstractCustomGroupBean
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public abstract class AbstractCustomGroupBean implements DelegateCustomGroupBean {

    private static final Logger log = Logger.getLogger(AbstractCustomGroupBean.class);

    protected static final int HOST_GROUP_ENTITY_TYPE_ID = 1;
    protected static final int SERVICE_GROUP_ENTITY_TYPE_ID = 2;
    protected static final int CUSTOM_GROUP_ENTITY_TYPE_ID = 3;

    protected List<CustomGroup> customGroups = new ArrayList<CustomGroup>();

    protected DualList customGroupSelectItems = new DualList();

    protected List<SelectItem> entityTypes = new ArrayList<SelectItem>();

    protected DualList hostGroups = new DualList();

    protected String message = null;

    protected DualList serviceGroups = new DualList();

    @Override
    public String deleteCustomGroup() {
        try {
            int selectCount = 0;
            for (CustomGroup group : customGroups) {
                if (group.isSelected()) {
                    selectCount++;
                    // make sure custom group has no children
                    if ((group.getEntityType().getEntityTypeId() == CUSTOM_GROUP_ENTITY_TYPE_ID) &&
                            (group.getElements() != null) && !group.getElements().isEmpty()) {
                        FacesMessage fm = new FacesMessage("Cannot delete custom group. " +
                                "Selected custom group is being referenced by another custom group. " +
                                "Start deleting from the bottom most custom group!");
                        FacesContext.getCurrentInstance().addMessage("Error!", fm);
                        break;
                    }
                    deleteCustomGroup(group);
                } // end if
            }
            if (selectCount == 0) {
                FacesMessage fm = new FacesMessage("Select at least one custom group to delete.");
                FacesContext.getCurrentInstance().addMessage("Invalid Input!", fm);
                return "invalidEditSelection";
            }
        } catch (Exception exc) {
            FacesMessage fm = new FacesMessage("Cannot delete custom group.");
            FacesContext.getCurrentInstance().addMessage("Error!", fm);
            log.error(exc.getMessage(), exc);
        }
        refresh();
        return null;
    }

    /**
     * Delete custom group.
     *
     * @param group custom group to delete
     */
    protected abstract void deleteCustomGroup(CustomGroup group) throws Exception;

    /**
     * Refresh custom groups and base entities.
     */
    protected abstract void refresh();

    @Override
    public String editCustomGroup() {
        try {
            int selectCount = 0;
            CustomGroup selectedGroup = null;
            for (CustomGroup group : customGroups) {
                if (group.isSelected()) {
                    selectedGroup = group;
                    selectCount++;
                } // end if
            }
            if (selectCount > 1) {
                FacesMessage fm = new FacesMessage("Cannot edit multiple custom groups! " +
                        "Select one custom group to edit.");
                FacesContext.getCurrentInstance().addMessage("Invalid Input!", fm);
                return "invalidEditSelection";
            } else if (selectCount == 0) {
                FacesMessage fm = new FacesMessage("Select at least one custom group to edit.");
                FacesContext.getCurrentInstance().addMessage("Invalid Input!", fm);
                return "invalidEditSelection";
            } else {
                refreshBaseEntities();
                FacesContext context = FacesContext.getCurrentInstance();
                CustomGroup bean = (CustomGroup) context.getApplication()
                        .evaluateExpressionGet(context, "#{customGroup}",
                                CustomGroup.class);
                bean.setGroupName(selectedGroup.getGroupName());
                bean.setEntityType(selectedGroup.getEntityType());
                for (CustomGroupElement child : selectedGroup.getElements()) {
                    int childId = (int) child.getElementId();
                    if (selectedGroup.getEntityType().getEntityTypeId() == HOST_GROUP_ENTITY_TYPE_ID) {
                        String childName = lookupHostGroupName(childId);
                        if (childName != null) {
                            SelectItem item = new SelectItem(childId,
                                    childName, "", false);
                            hostGroups.adjustDualList(item);
                        }
                    }
                    if (selectedGroup.getEntityType().getEntityTypeId() == SERVICE_GROUP_ENTITY_TYPE_ID) {
                        String childName = lookupServiceGroupName(childId);
                        if (childName != null) {
                            SelectItem item = new SelectItem(childId,
                                    childName, "", false);
                            serviceGroups.adjustDualList(item);
                        }
                    }

                    if (selectedGroup.getEntityType().getEntityTypeId() == CUSTOM_GROUP_ENTITY_TYPE_ID) {
                        CustomGroup group = findCustomGroupById(childId);
                        if (group != null) {
                            SelectItem item = new SelectItem(childId,
                                    group.getGroupName(), "", false);
                            customGroupSelectItems.adjustDualList(item);
                        }
                    }
                }
                List<SelectItem> uiElements = customGroupSelectItems.getLeftList();
                SelectItem markForDelete = null;
                for (SelectItem item : uiElements) {
                    if (item.getLabel().toString().equalsIgnoreCase(selectedGroup.getGroupName())) {
                        markForDelete = item;
                        break;
                    }
                }
                if (markForDelete != null) {
                    uiElements.remove(markForDelete);
                }
            }
        } catch (Exception exc) {
            log.error(exc.getMessage(), exc);
        }
        return "edit";
    }

    /**
     * Refresh entity types, host groups, and service groups.
     */
    protected abstract void refreshBaseEntities();

    /**
     * Lookup host group name from id.
     *
     * @param id host group id
     * @return host group name
     */
    protected abstract String lookupHostGroupName(int id);

    /**
     * Lookup service group name from id.
     *
     * @param id service group id
     * @return service group name
     */
    protected abstract String lookupServiceGroupName(int id);

    @Override
    public String cancel() {
        refresh();
        return "cancel";
    }

    @Override
    public String updateAndSave() {
        return updateCustomGroup("S");
    }

    @Override
    public String updateAndPublish() {
        return updateCustomGroup("P");
    }

    /**
     * Update custom group from UI state action.
     *
     * @param state custom group state
     * @return action
     */
    private String updateCustomGroup(String state) {
        String action = "updateSuccess";
        try {
            CustomGroup group = getCustomGroupFromUI();
            if (isValid(group, false)) {
                List<SelectItem> uiElements = null;
                if (group.getEntityType().getEntityTypeId() == HOST_GROUP_ENTITY_TYPE_ID)
                    uiElements = hostGroups.getRightList();
                if (group.getEntityType().getEntityTypeId() == SERVICE_GROUP_ENTITY_TYPE_ID)
                    uiElements = serviceGroups.getRightList();
                if (group.getEntityType().getEntityTypeId() == CUSTOM_GROUP_ENTITY_TYPE_ID)
                    uiElements = customGroupSelectItems.getRightList();
                Collection<Long> children = new ArrayList<Long>();
                for (SelectItem uiElement : uiElements) {
                    if (group.getEntityType().getEntityTypeId() == CUSTOM_GROUP_ENTITY_TYPE_ID) {
                        for (CustomGroup dbGroups : customGroups) {
                            if (dbGroups.getGroupId() == ((Long)uiElement.getValue()).longValue())
                                children.add(dbGroups.getGroupId());
                        }// end if
                    } else
                        children.add(((Integer) uiElement.getValue())
                                .longValue());
                } // end for

                // Parents cannot be added at this time. This means a
                // customgroup
                // cannot be inserted between.
                // List<String> uiParents = group.getSelectedParents();
                Collection<Long> parents = new ArrayList<Long>();
				/*
				 * for (String parentId : uiParents) {
				 * parents.add(Long.parseLong(parentId)); }
				 */
                String userName = FacesUtils.getLoggedInUser();
                // update custom group and refresh custom groups
                saveCustomGroup(group, parents, children, userName, state, false);
            } else {
                // message set by isValid()
                action = "updateFail";
            }
        } catch (Exception exc) {
            FacesMessage fm = new FacesMessage("Unable to update custom group as edited");
            FacesContext.getCurrentInstance().addMessage("Invalid Input!", fm);
            action = "updateFail";
            log.error(exc.getMessage(), exc);
        }
        return action;
    }

    @Override
    public String save() {
        return saveCustomGroup("S");
    }

    @Override
    public String publish() {
        return saveCustomGroup("P");
    }

    /**
     * Save custom group from UI state action.
     *
     * @param state custom group state
     * @return action
     */
    private String saveCustomGroup(String state) {
        String action = "saveSuccess";
        try {
            CustomGroup group = getCustomGroupFromUI();
            if (isValid(group, true)) {
                List<CustomGroupElement> elements = group.getElements();
                Collection<Long> children = new ArrayList<Long>();
                for (CustomGroupElement element : elements) {
                    children.add(element.getElementId());
                }
                // Parents cannot be added at this time. This means a
                // customgroup
                // cannot be inserted between.
                // List<String> uiParents = group.getSelectedParents();
                Collection<Long> parents = new ArrayList<Long>();
				/*
				 * for (String parentId : uiParents) {
				 * parents.add(Long.parseLong(parentId)); }
				 */
                String userName = FacesUtils.getLoggedInUser();
                // create custom group and refresh custom groups
                saveCustomGroup(group, parents, children, userName, state, true);
            } else {
                // message set by isValid()
                action = "saveFail";
            }
        } catch (Exception exc) {
            FacesMessage fm = new FacesMessage("Unable to create custom group as entered");
            FacesContext.getCurrentInstance().addMessage("Invalid Input!", fm);
            action = "saveFail";
            log.error(exc.getMessage(), exc);
        }
        return action;
    }

    /**
     * Save custom group and refresh custom groups and base entities.
     *
     * @param group custom group
     * @param parents parent ids list
     * @param children child ids list
     * @param userName updating user name
     * @param state custom group state
     * @param isCreate creating flag
     */
    protected abstract void saveCustomGroup(CustomGroup group, Collection<Long> parents, Collection<Long> children,
                                            String userName, String state, boolean isCreate) throws Exception;

    /**
     * Validate custom group.
     *
     * @param group custom group to validate
     * @param isCreate creating flag
     * @return valid flag
     */
    protected boolean isValid(CustomGroup group, boolean isCreate) {
        boolean isValid = false;
        if (group.getGroupName() == null || group.getGroupName().length() == 0) {
            FacesMessage fm = new FacesMessage("GroupName cannot be Empty!");
            FacesContext.getCurrentInstance().addMessage("Invalid Input!", fm);
            return isValid;
        }
        // Validation for different entity types in the children
        List<CustomGroupElement> children = group.getElements();

        // Validation for atleast one child..
        if (children == null || children.size() <= 0) {
            FacesMessage fm = new FacesMessage("Please add at least one child!");
            FacesContext.getCurrentInstance().addMessage("Invalid Input!", fm);
            return isValid;
        }
        if (children != null && group.getEntityType().getEntityTypeId() == CUSTOM_GROUP_ENTITY_TYPE_ID) {
            CustomGroupElement firstElement = children.get(0);
            CustomGroup firstChild = findCustomGroupById(firstElement.getElementId());
            String firstChildConcreteType = checkConcreteEntityType(firstChild);
            for (CustomGroupElement child : children) {
                Long childId = child.getElementId();
                CustomGroup childGroup = findCustomGroupById(childId);
                String concreteType = checkConcreteEntityType(childGroup);
                if ((concreteType != null) && !concreteType.equalsIgnoreCase(firstChildConcreteType)) {
                    FacesMessage fm = new FacesMessage("One or more children belong to a different entity type. " +
                            "You cannot mix HostGroups, ServiceGroups or CustomGroups!");
                    FacesContext.getCurrentInstance().addMessage("Invalid Input!", fm);
                    return isValid;
                } // end if
            } // end for
        } // end if

        if (!isCreate)
            return true;
        refresh();
        for (CustomGroup customGroup : customGroups) {
            if (customGroup.getGroupName().equalsIgnoreCase(
                    group.getGroupName())) {
                FacesMessage fm = new FacesMessage("Custom Group with " + group.getGroupName() +
                        " already exists. Please enter new Group Name!");
                FacesContext.getCurrentInstance().addMessage("Invalid Input!", fm);
                return isValid;
            }
        }
        isValid = true;
        return isValid;
    }

    /**
     * Get custom group from UI state.
     *
     * @return custom group
     */
    protected CustomGroup getCustomGroupFromUI() {
        String beanName = "customGroup";
        FacesContext facesContext = FacesContext.getCurrentInstance();
        Application app = facesContext.getApplication();
        ExpressionFactory ef = app.getExpressionFactory();
        ValueExpression valExp = ef.createValueExpression(FacesContext
                        .getCurrentInstance().getELContext(), "#{" + beanName + "}",
                Object.class);
        Object resultObj = valExp.getValue(facesContext.getELContext());
        if (resultObj != null) {
            CustomGroup group = (CustomGroup) resultObj;
            List<SelectItem> uiElements = null;
            if (group.getEntityType().getEntityTypeId() == HOST_GROUP_ENTITY_TYPE_ID)
                uiElements = hostGroups.getRightList();
            if (group.getEntityType().getEntityTypeId() == SERVICE_GROUP_ENTITY_TYPE_ID)
                uiElements = serviceGroups.getRightList();
            if (group.getEntityType().getEntityTypeId() == CUSTOM_GROUP_ENTITY_TYPE_ID)
                uiElements = customGroupSelectItems.getRightList();
            List<CustomGroupElement> children = new ArrayList<CustomGroupElement>();
            for (SelectItem uiElement : uiElements) {
                if (group.getEntityType().getEntityTypeId() == CUSTOM_GROUP_ENTITY_TYPE_ID) {
                    for (CustomGroup dbGroups : customGroups) {
                        if (dbGroups.getGroupId() == ((Long)uiElement.getValue()).longValue()) {
                            CustomGroupElement element = new CustomGroupElement();
                            element.setElementId(dbGroups.getGroupId());
                            children.add(element);
                        }
                    }// end for
                } else {
                    CustomGroupElement element = new CustomGroupElement();
                    element.setElementId(((Integer) uiElement.getValue())
                            .longValue());
                    children.add(element);
                } // end if
            } // end for
            group.setElements(children);
            return group;
        }
        return null;
    }

    /**
     * Lookup custom group id by name.
     *
     * @param group custom group name
     * @return custom group id or 0
     */
    protected Long getCustomGroupIdByName(String group) {
        for (CustomGroup custom : customGroups) {
            if (group.equalsIgnoreCase(custom.getGroupName())) {
                return custom.getGroupId();
            }
        }
        return new Long(0);
    }

    /**
     * Lookup custom group by id.
     *
     * @param groupId custom group id
     * @return custom group or null
     */
    protected CustomGroup findCustomGroupById(long groupId) {
        for (CustomGroup customGroup : customGroups) {
            if (customGroup.getGroupId() == groupId) {
                return customGroup;
            }
        }
        return null;
    }

    /**
     * Recursively check concrete entity type.
     *
     * @param group custom group
     * @return entity type
     */
    protected String checkConcreteEntityType(CustomGroup group) {
        List<CustomGroupElement> elements = group.getElements();
        String entityType = group.getEntityType().getEntityType();
        if (entityType.equalsIgnoreCase(Constant.DB_CUSTOM_GROUP)) {
            for (CustomGroupElement element : elements) {
                CustomGroup nextLevel = findCustomGroupById(element.getElementId());
                entityType = nextLevel.getEntityType().getEntityType();
                if (entityType.equalsIgnoreCase(Constant.DB_CUSTOM_GROUP)) {
                    return checkConcreteEntityType(nextLevel);
                }
                return entityType;
            }
        }
        return entityType;
    }

    @Override
    public List<CustomGroup> getCustomGroups() {
        return customGroups;
    }

    @Override
    public DualList getCustomGroupSelectItems() {
        return customGroupSelectItems;
    }

    @Override
    public List<SelectItem> getEntityTypes() {
        return entityTypes;
    }

    @Override
    public DualList getHostGroups() {
        return hostGroups;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public DualList getServiceGroups() {
        return serviceGroups;
    }
}
