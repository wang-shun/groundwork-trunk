package com.groundworkopensource.portal.statusviewer.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.Collection;
import java.text.SimpleDateFormat;
import com.groundworkopensource.portal.common.exception.WSDataUnavailableException;
import com.groundworkopensource.portal.common.exception.GWPortalException;
import com.groundworkopensource.portal.common.ws.IWSFacade;
import com.groundworkopensource.portal.common.ws.WebServiceFactory;
import com.groundworkopensource.portal.common.ws.WebServiceFactory.WebServiceType;
import org.groundwork.foundation.ws.model.impl.Category;
import org.groundwork.foundation.ws.model.impl.HostGroup;
import com.groundworkopensource.portal.common.FacesUtils;
import com.groundworkopensource.portal.model.CustomGroup;
import com.groundworkopensource.portal.model.CustomGroupElement;
import com.groundworkopensource.portal.model.EntityType;
import com.groundworkopensource.portal.statusviewer.handler.ReferenceTreeMetaModel;
import com.groundworkopensource.portal.statusviewer.common.NetworkMetaEntity;
import com.groundworkopensource.portal.statusviewer.common.Constant;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import javax.faces.application.Application;
import javax.faces.context.FacesContext;
import javax.faces.application.FacesMessage;
import org.apache.commons.lang.StringUtils;

import org.apache.log4j.Logger;
import javax.faces.model.SelectItem;

public class CustomGroupBean implements java.io.Serializable {

	private List<SelectItem> entityTypes;

	private DualList hostGroups;

	private DualList serviceGroups;

	private DualList customGroupSelectItems;

	private String message;

	private List<CustomGroup> customGroups = null;

	private static final String DATE_FORMAT = "MM/dd/yyyy hh:mm:ss a";

	/** . */
	private static final Logger log = Logger.getLogger(CustomGroupBean.class);

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

	public CustomGroupBean() {
		init();
	}

	/**
	 * Initialize all drop downs and customgroups
	 */
	private void init() {
		try {
			customGroups = new ArrayList<CustomGroup>(
					foundationWSFacade.findCustomGroups());
			populateChildrenNames();
		} catch (WSDataUnavailableException exc) {
			log.error(exc.getMessage());
		}
		populateBaseEntities();
		if (customGroups == null || customGroups.size() == 0)
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
					if (entityTypeId == 1) {
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
					if (entityTypeId == 2) {
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
					if (entityTypeId == 3) {
						CustomGroup group = referenceTreeModel
								.findCustomGroupById(elementId);
						elementName = group.getGroupName();
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
		} catch (WSDataUnavailableException exc) {
			log.error(exc.getMessage());
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

		} catch (WSDataUnavailableException exc) {
			log.error(exc.getMessage());
		} catch (GWPortalException exc) {
			log.error(exc.getMessage());
		} catch (Exception exc) {
			log.error(exc.getMessage());
		}// end try/catch

		hostGroups = new DualList();
		if (null != hostGroupArr) {
			for (int i = 0; i < hostGroupArr.length; i++) {
				SelectItem item = new SelectItem(
						hostGroupArr[i].getHostGroupID(),
						hostGroupArr[i].getName(), "", false);
				hostGroups.addAvailableItem(item);

			} // end for
		} // end if

		serviceGroups = new DualList();
		if (serviceGroupArr != null) {
			for (int i = 0; i < serviceGroupArr.length; i++) {
				SelectItem item = new SelectItem(
						serviceGroupArr[i].getCategoryId(),
						serviceGroupArr[i].getName(), "", false);
				serviceGroups.addAvailableItem(item);
			} // end for

		} // end if

		if (entityTypeList != null) {
			entityTypes = new ArrayList<SelectItem>();
			for (int i = 0; i < entityTypeList.size(); i++) {
				SelectItem item = new SelectItem(String.valueOf(entityTypeList
						.get(i).getEntityTypeId()), entityTypeList.get(i)
						.getEntityType(), "", false);
				entityTypes.add(item);
			} // end for
		} // end if

		customGroupSelectItems = new DualList();
		if (customGroups != null) {
			for (int i = 0; i < customGroups.size(); i++) {
				SelectItem item = new SelectItem(customGroups.get(i)
						.getGroupId(), customGroups.get(i).getGroupName(), "",
						false);
				customGroupSelectItems.addAvailableItem(item);
			} // end for
		} // end if
	}

	public List<CustomGroup> getCustomGroups() {
		return customGroups;
	}

	public void setCustomGroups(List<CustomGroup> customGroups) {
		this.customGroups = customGroups;
	}

	/**
	 * Deletes the custom groups
	 */
	public String deleteCustomGroup() {
		try {
			int selectCount = 0;
			for (CustomGroup group : customGroups) {
				if (group.isSelected()) {
					foundationWSFacade.removeCustomGroup(group.getGroupId());
					selectCount++;
				} // end if
			}
			if (selectCount == 0) {
				FacesMessage fm = new FacesMessage(
						"Select atleast one custom group to delete ");
				FacesContext.getCurrentInstance().addMessage("Invalid Input!",
						fm);
				return "invalidEditSelection";
			}
		} catch (Exception exc) {
			FacesMessage fm = new FacesMessage(
					"Cannot delete custom group. Selected custom group is being referenced by another custom group. Start deleting from the bottom most custom group!");
			FacesContext.getCurrentInstance().addMessage("Error!", fm);
			log.error(exc.getMessage());
		}
		init();
		return null;
	}

	/**
	 * Edit the custom groups
	 */
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
				FacesMessage fm = new FacesMessage(
						"Cannot edit multiple custom groups! Select one custom group to edit ");
				FacesContext.getCurrentInstance().addMessage("Invalid Input!",
						fm);
				return "invalidEditSelection";
			} else if (selectCount == 0) {
				FacesMessage fm = new FacesMessage(
						"Select atleast one custom group to edit ");
				FacesContext.getCurrentInstance().addMessage("Invalid Input!",
						fm);
				return "invalidEditSelection";
			} else {
				this.populateBaseEntities();
				FacesContext context = FacesContext.getCurrentInstance();
				CustomGroup bean = (CustomGroup) context.getApplication()
						.evaluateExpressionGet(context, "#{customGroup}",
								CustomGroup.class);
				bean.setGroupName(selectedGroup.getGroupName());
				bean.setEntityType(selectedGroup.getEntityType());
				for (CustomGroupElement child : selectedGroup.getElements()) {
					int childId = (int) child.getElementId();
					if (selectedGroup.getEntityType().getEntityTypeId() == 1) {
						NetworkMetaEntity hostGroup = referenceTreeModel
								.getHostGroupById((int) child.getElementId());
						if (hostGroup != null) {
							String childName = hostGroup.getName();
							SelectItem item = new SelectItem(childId,
									childName, "", false);
							hostGroups.adjustDualList(item);
						}
					}
					if (selectedGroup.getEntityType().getEntityTypeId() == 2) {
						NetworkMetaEntity serviceGroup = referenceTreeModel
								.getServiceGroupById(childId);
						String childName = serviceGroup.getName();
						if (serviceGroup != null) {
							SelectItem item = new SelectItem(childId,
									childName, "", false);
							serviceGroups.adjustDualList(item);
						}
					}

					if (selectedGroup.getEntityType().getEntityTypeId() == 3) {
						CustomGroup group = referenceTreeModel
								.findCustomGroupById(childId);
						SelectItem item = new SelectItem(childId,
								group.getGroupName(), "", false);
						customGroupSelectItems.adjustDualList(item);
					}
				}
				if (customGroupSelectItems != null) {
					List<SelectItem> uiElements = customGroupSelectItems.getLeftList();
					SelectItem markForDelete = null;
					log.debug("After marking for delete..");
					for (SelectItem item : uiElements) {
						if (item.getLabel().toString().equalsIgnoreCase(selectedGroup.getGroupName())) {
							log.debug("Marking item for delete from left pane");
							markForDelete = item;
							break;
						}
					}
					if (markForDelete != null) {
						uiElements.remove(markForDelete);
					}
				}
			}
			return "edit";

		} catch (Exception exc) {
			log.error(exc.getMessage());
		}

		return "edit";
	}

	public String cancel() {
		init();
		return "cancel";
	}

	/**
	 * Method to save the customgroup
	 */
	public String save() {
		return this.saveCustomGroup("S");
	}

	/**
	 * Method to edit and save the customgroup
	 */
	public String updateAndSave() {
		return this.updateCustomGroup("S");
	}

	/**
	 * Method to edit and save the customgroup
	 */
	public String updateAndPublish() {
		return this.updateCustomGroup("P");
	}

	/**
	 * Method to edit and save the customgroup
	 */
	public String updateCustomGroup(String state) {
		try {
			CustomGroup group = getCustomGroupFromUI();
			if (isValid(group, false)) {
				List<SelectItem> uiElements = null;
				if (group.getEntityType().getEntityTypeId() == 1)
					uiElements = hostGroups.getRightList();
				if (group.getEntityType().getEntityTypeId() == 2)
					uiElements = serviceGroups.getRightList();
				if (group.getEntityType().getEntityTypeId() == 3)
					uiElements = customGroupSelectItems.getRightList();
				Collection<Long> children = new ArrayList<Long>();
				for (SelectItem uiElement : uiElements) {
					if (group.getEntityType().getEntityTypeId() == 3) {
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
				String strChildren = StringUtils.join(children.iterator(), ",");
				String strParent = StringUtils.join(parents.iterator(), ",");
				String userName = FacesUtils.getLoggedInUser();
				foundationWSFacade.updateCustomGroup(group.getGroupName(),
						(int) group.getEntityType().getEntityTypeId(),
						strParent, state, userName, strChildren);
			} else
				return "updateFail";
		} catch (Exception exc) {
			log.error(exc.getMessage());
		}
		init();
		return "updateSuccess";
	}

	private boolean isValid(CustomGroup group, boolean isCreate) {
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
			FacesMessage fm = new FacesMessage("Please add atleast one child!");
			FacesContext.getCurrentInstance().addMessage("Invalid Input!", fm);
			return isValid;
		}
		if (children != null && group.getEntityType().getEntityTypeId() == 3) {
			CustomGroupElement firstElement = children.get(0);
			CustomGroup firstChild = referenceTreeModel
					.findCustomGroupById(firstElement.getElementId());
			String firstChildConcreteType = referenceTreeModel
					.checkConcreteEntityType(firstChild);
			for (CustomGroupElement child : children) {
				Long childId = child.getElementId();
				CustomGroup childGroup = referenceTreeModel
						.findCustomGroupById(childId);
				String concreteType = referenceTreeModel
						.checkConcreteEntityType(childGroup);
				if (concreteType != null
						&& !concreteType
								.equalsIgnoreCase(firstChildConcreteType)) {
					FacesMessage fm = new FacesMessage(
							"One or more child(ren) belong to a different entity type. You cannot mix HostGroups, ServiceGroups or CustomGroups!");
					FacesContext.getCurrentInstance().addMessage(
							"Invalid Input!", fm);
					return isValid;
				} // end if
			} // end for
		} // end if

		if (!isCreate)
			return true;
		init();
		for (CustomGroup customGroup : customGroups) {
			if (customGroup.getGroupName().equalsIgnoreCase(
					group.getGroupName())) {
				FacesMessage fm = new FacesMessage("Custom Group with "
						+ group.getGroupName()
						+ " already exists. Please enter new Group Name!");
				FacesContext.getCurrentInstance().addMessage("Invalid Input!",
						fm);
				return isValid;
			}
		}
		isValid = true;
		return isValid;
	}

	/**
	 * Helper to save the group
	 */
	private String saveCustomGroup(String state) {
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
				String strChildren = StringUtils.join(children.iterator(), ",");
				String strParent = StringUtils.join(parents.iterator(), ",");
				String userName = FacesUtils.getLoggedInUser();
				foundationWSFacade.createCustomGroup(group.getGroupName(),
						(int) group.getEntityType().getEntityTypeId(),
						strParent, state, userName, strChildren);
			} else
				return "saveFail";
		} catch (Exception exc) {
			log.error(exc.getMessage());
		}
		init();
		return "saveSuccess";
	}

	/**
	 * Publish
	 */
	public String publish() {
		return this.saveCustomGroup("P");
	}

	public List<SelectItem> getEntityTypes() {
		return entityTypes;
	}

	public void setEntityTypes(List<SelectItem> entityTypes) {
		this.entityTypes = entityTypes;
	}

	public DualList getHostGroups() {
		return hostGroups;
	}

	public void setHostGroups(DualList hostGroups) {
		this.hostGroups = hostGroups;
	}

	public DualList getServiceGroups() {
		return serviceGroups;
	}

	public void setServiceGroups(DualList serviceGroups) {
		this.serviceGroups = serviceGroups;
	}

	public DualList getCustomGroupSelectItems() {
		return customGroupSelectItems;
	}

	public void setCustomGroupSelectItems(DualList customGroupSelectItems) {
		this.customGroupSelectItems = customGroupSelectItems;
	}

	private CustomGroup getCustomGroupFromUI() {
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
			if (group.getEntityType().getEntityTypeId() == 1)
				uiElements = hostGroups.getRightList();
			if (group.getEntityType().getEntityTypeId() == 2)
				uiElements = serviceGroups.getRightList();
			if (group.getEntityType().getEntityTypeId() == 3)
				uiElements = customGroupSelectItems.getRightList();
			List<CustomGroupElement> children = new ArrayList<CustomGroupElement>();
			for (SelectItem uiElement : uiElements) {
				if (group.getEntityType().getEntityTypeId() == 3) {
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

	public void setMessage(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	/**
	 * Helper to get the custom group
	 */
	private Long getCustomGroupIdByName(String group) {
		for (CustomGroup custom : customGroups) {
			if (group.equalsIgnoreCase(custom.getGroupName())) {
				return custom.getGroupId();
			}
		}
		return new Long(0);
	}
}
