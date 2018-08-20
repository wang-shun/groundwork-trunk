package com.groundworkopensource.portal.statusviewer.bean;

import com.groundworkopensource.portal.common.FacesUtils;
import com.groundworkopensource.portal.common.exception.WSDataUnavailableException;
import com.groundworkopensource.portal.common.ws.IWSFacade;
import com.groundworkopensource.portal.common.ws.WebServiceFactory;
import com.groundworkopensource.portal.common.ws.WebServiceFactory.WebServiceType;
import com.groundworkopensource.portal.model.CustomGroup;
import com.groundworkopensource.portal.model.CustomGroupElement;
import com.groundworkopensource.portal.model.EntityType;
import com.groundworkopensource.portal.statusviewer.common.Constant;
import com.groundworkopensource.portal.statusviewer.common.NetworkMetaEntity;
import com.groundworkopensource.portal.statusviewer.handler.ReferenceTreeMetaModel;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.groundwork.foundation.ws.model.impl.Category;
import org.groundwork.foundation.ws.model.impl.HostGroup;

import javax.faces.model.SelectItem;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PortalCustomGroupBean extends AbstractCustomGroupBean implements java.io.Serializable {

	/** . */
	private static final Logger log = Logger.getLogger(PortalCustomGroupBean.class);

	/**
	 * ReferenceTreeMetaModel instance
	 * <p>
	 * !!!!!!!!!!! IMP !!!!!!!!!! : Please do not remove below declaration of
	 * referenceTreeModel.
	 */
	private ReferenceTreeMetaModel referenceTreeModel = (ReferenceTreeMetaModel) FacesUtils
			.getManagedBean(Constant.REFERENCE_TREE);

	private IWSFacade foundationWSFacade = new WebServiceFactory()
			.getWebServiceInstance(WebServiceType.FOUNDATION_WEBSERVICE);

	public PortalCustomGroupBean() {
		init();
	}

	/**
	 * Initialize all drop downs and customgroups
	 */
	private void init() {
		try {
            customGroups.clear();
			customGroups.addAll(foundationWSFacade.findCustomGroups());
			populateChildrenNames();
		} catch (WSDataUnavailableException exc) {
			log.error(exc.getMessage());
		}
		populateBaseEntities();
		if (customGroups.size() == 0)
			message = "No CustomGroups Available!";
	}

	/**
	 * helper to populate child and parent names as it is not returned by rest api
	 */
	private void populateChildrenNames() {
		try {
			for (CustomGroup uiCustomGroup : customGroups) {
				// Now populate children
				Collection<CustomGroupElement> children = uiCustomGroup
						.getElements();

				for (CustomGroupElement child : children) {
					long entityTypeId = child.getEntityTypeId();
					long elementId = child.getElementId();
					String elementName = null;
					if (entityTypeId == HOST_GROUP_ENTITY_TYPE_ID) {
						NetworkMetaEntity hostGroup = referenceTreeModel
								.getHostGroupById((int) elementId);
						// If you dont find the hostgroup, then it is deleted in
						// monarch
						if (hostGroup != null)
							elementName = hostGroup.getName();
						else
							foundationWSFacade.removeOrphanedChildren(
									elementId, (int) entityTypeId);
					}
					if (entityTypeId == SERVICE_GROUP_ENTITY_TYPE_ID) {
						elementName = null;
						NetworkMetaEntity serviceGroup = referenceTreeModel
								.getServiceGroupById((int) elementId);
						// If you dont find the servicegroup, then it is deleted
						// in monarch
						if (serviceGroup != null)
							elementName = serviceGroup.getName();
						else
							foundationWSFacade.removeOrphanedChildren(
									elementId, (int) entityTypeId);
					}
					if (entityTypeId == CUSTOM_GROUP_ENTITY_TYPE_ID) {
						CustomGroup group = findCustomGroupById(elementId);
                        if (group != null) {
                            elementName = group.getGroupName();
                        }
					}
					if (elementName != null)
						child.setElementName(elementName);

				}

				// uiCustomGroup.setElements(uiElements);

				// Now populate parents
				Collection<CustomGroup> parents = uiCustomGroup.getParents();

				for (CustomGroup parent : parents) {

					List<CustomGroup> uiParents_level_2 = new ArrayList<CustomGroup>();
					for (CustomGroup hibParent_level_2 : parent.getParents()) {
						CustomGroup uiParent_level_2 = new CustomGroup();
						uiParent_level_2.setGroupName(hibParent_level_2
								.getGroupName());
						uiParents_level_2.add(uiParent_level_2);
					}
					parent.setParents(uiParents_level_2);
					// parent.setGroupName(parent.getGroupName());
					// parent.setGroupId(hibParent.getGroupId());
					// uiParents.add(uiParent);
				}

				// uiCustomGroup.setParents(uiParents);
			} // end if
		} catch (Exception exc) {
			log.error(exc.getMessage(), exc);
		}
	}

	/**
	 * Private helper method for populating hostgroups and service groups
	 */
	private void populateBaseEntities() {
		HostGroup[] hostGroupArr = null;
		Category[] serviceGroupArr = null;
		List<EntityType> entityTypeList = null;
		try {
			hostGroupArr = foundationWSFacade.getAllHostGroups();
			serviceGroupArr = foundationWSFacade.getAllServiceGroups();
			entityTypeList = new ArrayList<EntityType>(
					foundationWSFacade.findEntityTypes());
		} catch (Exception exc) {
			log.error(exc.getMessage(), exc);
		}// end try/catch

        hostGroups.clear();
		if (null != hostGroupArr) {
			for (int i = 0; i < hostGroupArr.length; i++) {
				SelectItem item = new SelectItem(
						hostGroupArr[i].getHostGroupID(),
						hostGroupArr[i].getName(), "", false);
				hostGroups.addAvailableItem(item);

			} // end for
		} // end if

        serviceGroups.clear();
		if (serviceGroupArr != null) {
			for (int i = 0; i < serviceGroupArr.length; i++) {
				SelectItem item = new SelectItem(
						serviceGroupArr[i].getCategoryId(),
						serviceGroupArr[i].getName(), "", false);
				serviceGroups.addAvailableItem(item);
			} // end for

		} // end if

        entityTypes.clear();
		if (entityTypeList != null) {
			for (int i = 0; i < entityTypeList.size(); i++) {
				SelectItem item = new SelectItem(String.valueOf(entityTypeList
						.get(i).getEntityTypeId()), entityTypeList.get(i)
						.getEntityType(), "", false);
				entityTypes.add(item);
			} // end for
		} // end if

        customGroupSelectItems.clear();
        for (int i = 0; i < customGroups.size(); i++) {
            SelectItem item = new SelectItem(customGroups.get(i)
                    .getGroupId(), customGroups.get(i).getGroupName(), "",
                    false);
            customGroupSelectItems.addAvailableItem(item);
        } // end for
	}

    @Override
    protected void deleteCustomGroup(CustomGroup group) throws Exception {
        // delete custom group
        foundationWSFacade.removeCustomGroup(group.getGroupId());
        // notify RTMM
        Set<Long> dependentIds = getCustomGroupDependents(group, null, null);
        referenceTreeModel.removeCustomGroup(group.getGroupId(), dependentIds);
    }

    @Override
    protected void refresh() {
        // repopulate custom groups and base entities
        init();
    }

    @Override
    protected void refreshBaseEntities() {
        // repopulate base entities
        populateBaseEntities();
    }

    @Override
    protected String lookupHostGroupName(int id) {
        // get host group name from RTMM
        NetworkMetaEntity hostGroup = referenceTreeModel.getHostGroupById(id);
        return ((hostGroup != null) ? hostGroup.getName() : null);
    }

    @Override
    protected String lookupServiceGroupName(int id) {
        // get service group name from RTMM
        NetworkMetaEntity serviceGroup = referenceTreeModel.getServiceGroupById(id);
        return ((serviceGroup != null) ? serviceGroup.getName() : null);
    }

    @Override
    protected void saveCustomGroup(CustomGroup group, Collection<Long> parents, Collection<Long> children,
                                   String userName, String state, boolean isCreate) throws Exception {
        // update custom group
        String strChildren = StringUtils.join(children.iterator(), ",");
        String strParent = StringUtils.join(parents.iterator(), ",");
        if (isCreate) {
            foundationWSFacade.createCustomGroup(group.getGroupName(),
                    (int) group.getEntityType().getEntityTypeId(),
                    strParent, state, userName, strChildren);
        } else {
            foundationWSFacade.updateCustomGroup(group.getGroupName(),
                    (int) group.getEntityType().getEntityTypeId(),
                    strParent, state, userName, strChildren);
        }
        // refresh custom groups
        init();
        // notify RTMM
        Long updatedCustomGroupId = getCustomGroupIdByName(group.getGroupName());
        if (updatedCustomGroupId.longValue() != 0L) {
            Set<Long> dependentIds = getCustomGroupDependents(group, parents, children);
            referenceTreeModel.updateCustomGroup(updatedCustomGroupId, dependentIds);
        }
    }

    /**
     * Get set of dependent ids, (parents and children), for a custom group.
     *
     * @param group custom group
     * @param parents new parents ids
     * @param children new children ids
     * @return set of dependent ids
     */
    private Set<Long> getCustomGroupDependents(CustomGroup group, Collection<Long> parents, Collection<Long> children) {
        Set<Long> dependentIds = new HashSet<Long>();
        // add custom group parents
        if ((group.getParents() != null) && !group.getParents().isEmpty()) {
            for (CustomGroup parent : group.getParents()) {
                dependentIds.add(parent.getGroupId());
            }
        }
        // add custom group new parents
        if ((parents != null) && !parents.isEmpty()) {
            dependentIds.addAll(parents);
        }
        // add custom group children if custom group type
        if ((group.getEntityType().getEntityTypeId() == CUSTOM_GROUP_ENTITY_TYPE_ID) &&
                (group.getElements() != null) && !group.getElements().isEmpty()) {
            for (CustomGroupElement customGroupElement : group.getElements()) {
                dependentIds.add(customGroupElement.getElementId());
            }
            // add custom group new children
            if ((children != null) && !children.isEmpty()) {
                dependentIds.addAll(children);
            }
        }
        return dependentIds;
    }
}
