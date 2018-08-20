/**
 * Copyright (C) 2009 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.exoplatform.organization.webui.component;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Collection;
import java.util.StringTokenizer;

import javax.ws.rs.core.MediaType;

import com.groundworkopensource.portal.model.*;
import org.apache.log4j.Logger;
import org.exoplatform.commons.serialization.api.annotations.Serialized;
import org.exoplatform.services.organization.MembershipType;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.webui.form.input.UICheckBoxInput;
import org.exoplatform.webui.form.validator.MandatoryValidator;
import org.exoplatform.webui.form.validator.NameValidator;
import org.exoplatform.webui.form.validator.SpecialCharacterValidator;
import org.exoplatform.webui.form.validator.StringLengthValidator;
import org.groundwork.foundation.ws.model.impl.Category;
import org.groundwork.foundation.ws.model.impl.HostGroup;

import com.groundworkopensource.portal.common.exception.GWPortalException;
import com.groundworkopensource.portal.common.exception.WSDataUnavailableException;
import com.groundworkopensource.portal.common.ws.IWSFacade;
import com.groundworkopensource.portal.common.ws.WebServiceFactory;
import com.groundworkopensource.portal.common.ws.WebServiceFactory.WebServiceType;
import org.groundwork.rs.client.ExtendedRoleClient;

@ComponentConfig(lifecycle = UIFormLifecycle.class, template = "system:/groovy/webui/form/UIFormWithTitle.gtmpl", events = {
        @EventConfig(listeners = UIMembershipTypeForm.SaveActionListener.class),
        @EventConfig(listeners = UIMembershipTypeForm.ResetActionListener.class, phase = Phase.DECODE),
        @EventConfig(listeners = UIMembershipTypeForm.FilterDefaultHGEntityActionListener.class),
        @EventConfig(listeners = UIMembershipTypeForm.ShowRestrictionActionListener.class),
        @EventConfig(listeners = UIMembershipTypeForm.FilterDefaultSGEntityActionListener.class) })
@Serialized
public class UIMembershipTypeForm extends UIForm {

    /** The logger. */
    private static final Logger logger = Logger
            .getLogger(UIMembershipTypeForm.class);

    private static String MEMBERSHIP_TYPE_NAME = "name",
            DESCRIPTION = "description",
            DASHBOARD_LINKS_DISABLED = "roledashboardLinksDisabled",
            ENABLE_ACTIONS = "enableActions",
            RESTRICTION_TYPE = "restrictionType", HOSTGROUP = "hostgroup",
            SERVICEGROUP = "servicegroup", DEFAULT_HG = "defaultHG",
            DEFAULT_SG = "defaultSG", NO_RESTRICTIONS = "N",
            PARTIAL_RESTRICTIONS = "P";

    private String membershipTypeName;

    private HostGroup[] hostGroups = null;

    private Category[] serviceGroups = null;

    private boolean renderRestrictionComponents = false;

    private final static String[] ACTIONS = { "Save", "Reset" };

    private long extRoleId = 0;

    public UIMembershipTypeForm() throws Exception {
        addUIFormInput(new UIFormStringInput(MEMBERSHIP_TYPE_NAME,
                MEMBERSHIP_TYPE_NAME, null).setReadOnly(false)
                .addValidator(MandatoryValidator.class)
                .addValidator(StringLengthValidator.class, 3, 255)
                .addValidator(NameValidator.class)
                .addValidator(SpecialCharacterValidator.class));

        addUIFormInput(new UIFormTextAreaInput(DESCRIPTION, DESCRIPTION, null));
        addUIFormInput(new UICheckBoxInput(DASHBOARD_LINKS_DISABLED,
                DASHBOARD_LINKS_DISABLED, false));
        addUIFormInput(new UICheckBoxInput(ENABLE_ACTIONS, ENABLE_ACTIONS, true));
        this.makeApplicationPermCheckboxes();
        UICheckBoxInput restrictionType = new UICheckBoxInput(RESTRICTION_TYPE,
                RESTRICTION_TYPE, true);
        restrictionType.setOnChange("ShowRestriction");
        addUIFormInput(restrictionType);

        // populate host groups and service groups
        List<SelectItemOption<String>> hostgroupOptionsMulti = this
                .makeHostGroupOptions();
        List<SelectItemOption<String>> servicegroupOptionsMulti = this
                .makeServiceGroupOptions();
        List<SelectItemOption<String>> hostgroupOptionsSingle = new ArrayList<SelectItemOption<String>>();

        List<SelectItemOption<String>> servicegroupOptionsSingle = new ArrayList<SelectItemOption<String>>();
        UIFormSelectBox hgMultiSelectBox = new UIFormSelectBox(HOSTGROUP, null,
                hostgroupOptionsMulti);
        hgMultiSelectBox.setId(HOSTGROUP);
        hgMultiSelectBox.setSize(10);
        hgMultiSelectBox.setMultiple(true);
        hgMultiSelectBox.setOnChange("FilterDefaultHGEntity");
        hgMultiSelectBox.setRendered(renderRestrictionComponents);
        addUIFormInput(hgMultiSelectBox);

        UIFormSelectBox hgSingleSelectBox = new UIFormSelectBox(DEFAULT_HG,
                DEFAULT_HG, hostgroupOptionsSingle);
        hgSingleSelectBox.setId(DEFAULT_HG);
        hgSingleSelectBox.setRendered(renderRestrictionComponents);
        addUIFormInput(hgSingleSelectBox);

        // Show the dropdown only atleast one service group is available.
        if (servicegroupOptionsMulti != null
                && servicegroupOptionsMulti.size() > 0) {
            UIFormSelectBox sgMultiSelectBox = new UIFormSelectBox(
                    SERVICEGROUP, null, servicegroupOptionsMulti);
            sgMultiSelectBox.setId(SERVICEGROUP);
            sgMultiSelectBox.setSize(10);
            sgMultiSelectBox.setMultiple(true);
            sgMultiSelectBox.setOnChange("FilterDefaultSGEntity");
            sgMultiSelectBox.setRendered(renderRestrictionComponents);
            addUIFormInput(sgMultiSelectBox);

            UIFormSelectBox sgSingleSelectBox = new UIFormSelectBox(DEFAULT_SG,
                    DEFAULT_SG, servicegroupOptionsSingle);
            sgSingleSelectBox.setId(DEFAULT_SG);
            sgSingleSelectBox.setRendered(renderRestrictionComponents);
            addUIFormInput(sgSingleSelectBox);
        } // end if

        setActions(ACTIONS);
    }

    /**
     * Makes select options for groundwork host groups
     *
     * @return
     */
    private List<SelectItemOption<String>> makeHostGroupOptions() {
        List<SelectItemOption<String>> hostgroupOptions = new ArrayList<SelectItemOption<String>>();
        IWSFacade foundationWSFacade = new WebServiceFactory()
                .getWebServiceInstance(WebServiceType.FOUNDATION_WEBSERVICE);
        try {
            if (hostGroups == null)
                hostGroups = foundationWSFacade.getAllHostGroups();
        } catch (WSDataUnavailableException exc) {
            logger.error(exc.getMessage());
        } catch (GWPortalException exc) {
            logger.error(exc.getMessage());
        } // end try/catch
        for (HostGroup hostgroup : hostGroups) {
            SelectItemOption<String> option = new SelectItemOption<String>(
                    hostgroup.getName());
            hostgroupOptions.add(option);
        }// end for
        return hostgroupOptions;
    }

    /**
     * Makes select options for groundwork host groups
     *
     * @return
     */
    private List<SelectItemOption<String>> makeEntityOptions(List<String> list) {
        List<SelectItemOption<String>> entityOptions = new ArrayList<SelectItemOption<String>>();
        for (String entity : list) {
            SelectItemOption<String> option = new SelectItemOption<String>(
                    entity, entity);
            entityOptions.add(option);
        }// end for
        return entityOptions;
    }

    /**
     * Makes select options for groundwork service groups
     *
     * @return
     */
    private List<SelectItemOption<String>> makeServiceGroupOptions() {
        List<SelectItemOption<String>> servicegroupOptions = new ArrayList<SelectItemOption<String>>();
        IWSFacade foundationWSFacade = new WebServiceFactory()
                .getWebServiceInstance(WebServiceType.FOUNDATION_WEBSERVICE);
        try {
            if (serviceGroups == null)
                serviceGroups = foundationWSFacade.getAllServiceGroups();
        } catch (WSDataUnavailableException exc) {
            logger.error(exc.getMessage());
        } catch (GWPortalException exc) {
            logger.error(exc.getMessage());
        } // end try/catch

        if (serviceGroups == null) { // No service Groups defined
            return servicegroupOptions; // empty list
        }

		/* Load list of Service Groups */
        for (Category category : serviceGroups) {
            SelectItemOption<String> option = new SelectItemOption<String>(
                    category.getName());
            servicegroupOptions.add(option);
        } // end for

        return servicegroupOptions;
    }

    /**
     * Creates app perm check boxes
     */
    private void makeApplicationPermCheckboxes() {
        List<ExtendedUIResource> uiResourceList = this.getResources().getList();
        for (ExtendedUIResource uiResource : uiResourceList) {
            addUIFormInput(new UICheckBoxInput(uiResource.getResourceName(), uiResource.getResourceName(), true));
        }// end for
    }

    private void resetApplicationPermCheckboxes() {
        List<ExtendedUIResource> uiResourceList = this.getResources().getList();
        for (ExtendedUIResource uiResource : uiResourceList) {
            this.getUICheckBoxInput(uiResource.getResourceName()).setValue(
                    true);
        }// end for
    }


    public void setMembershipType(MembershipType membershipType)
            throws Exception {
        if (membershipType == null) {
            membershipTypeName = null;
            getUIStringInput(MEMBERSHIP_TYPE_NAME).setReadOnly(false);
            return;
        } else {
            membershipTypeName = membershipType.getName();
            getUIStringInput(MEMBERSHIP_TYPE_NAME).setReadOnly(true);
        }
        // Dont use autobinding option here as MembershipType object
        // invokeGetBindingBean(membershipType);
        this.getUIStringInput(MEMBERSHIP_TYPE_NAME)
                .setValue(membershipTypeName);
        this.getUIFormTextAreaInput(DESCRIPTION).setValue(
                membershipType.getDescription());
        ExtendedUIRole extRole = this.findExtendedRole(membershipTypeName);
        this.setExtRoleId(extRole.getId());
        ExtendedUIRolePermissionList permList = extRole.getRolePermissions();
        Collection<ExtendedUIRolePermission> permCol = permList.getRolePermissions();
        for (ExtendedUIRolePermission permission : permCol) {
            this.getUICheckBoxInput(permission.getResource()).setValue(
                    permission.getAction().equals("allow") ? true : false);
        }
        this.getUICheckBoxInput(DASHBOARD_LINKS_DISABLED).setValue(
                extRole.isDashboardLinksDisabled());
        this.getUICheckBoxInput(ENABLE_ACTIONS).setValue(
                extRole.isActionsEnabled());
        this.getUICheckBoxInput(RESTRICTION_TYPE)
                .setValue(
                        extRole.getRestrictionType().equalsIgnoreCase(
                                NO_RESTRICTIONS) ? true : false);

        if (extRole.getRestrictionType().equalsIgnoreCase(PARTIAL_RESTRICTIONS)) {
            List<String> selectedHG = new ArrayList<String>();
            if (extRole.getHgList() != null) {
                StringTokenizer stknHG = new StringTokenizer(
                        extRole.getHgList(), ",");
                while (stknHG.hasMoreTokens()) {
                    selectedHG.add(stknHG.nextToken());
                }
            }
            List<String> selectedSG = new ArrayList<String>();
            if (extRole.getSgList() != null) {
                StringTokenizer stknSG = new StringTokenizer(
                        extRole.getSgList(), ",");
                while (stknSG.hasMoreTokens()) {
                    selectedSG.add(stknSG.nextToken());
                } // end while
            } // end if
            this.getUIFormSelectBox(HOSTGROUP).setRendered(true);
            this.getUIFormSelectBox(HOSTGROUP).setSelectedValues(
                    selectedHG.toArray(new String[selectedHG.size()]));
            this.getUIFormSelectBox(DEFAULT_HG).setRendered(true);
            this.getUIFormSelectBox(DEFAULT_HG).setOptions(
                    this.makeEntityOptions(selectedHG));
            String[] defaultselectedHG = new String[0];
            if (extRole.getDefaultHostGroup() != null) {
                defaultselectedHG = new String[]{extRole.getDefaultHostGroup()};
            }
            this.getUIFormSelectBox(DEFAULT_HG).setSelectedValues(
                    defaultselectedHG);
            if (this.getUIFormSelectBox(SERVICEGROUP) != null) {
                this.getUIFormSelectBox(SERVICEGROUP).setRendered(true);
                this.getUIFormSelectBox(SERVICEGROUP).setSelectedValues(
                        selectedSG.toArray(new String[selectedSG.size()]));
                this.getUIFormSelectBox(DEFAULT_SG).setRendered(true);
                this.getUIFormSelectBox(DEFAULT_SG).setOptions(
                        this.makeEntityOptions(selectedSG));
                String[] defaultselectedSG = new String[0];
                if (extRole.getDefaultServiceGroup() != null) {
                    defaultselectedSG = new String[]{extRole.getDefaultServiceGroup()};
                }
                this.getUIFormSelectBox(DEFAULT_SG).setSelectedValues(
                        defaultselectedSG);
            }
        } else {
            this.getUIFormSelectBox(HOSTGROUP).setRendered(false);
            this.getUIFormSelectBox(DEFAULT_HG).setRendered(false);
            if (this.getUIFormSelectBox(SERVICEGROUP) != null) {
                this.getUIFormSelectBox(SERVICEGROUP).setRendered(false);
                this.getUIFormSelectBox(DEFAULT_SG).setRendered(false);
            }
        }
    }

    public String getMembershipTypeName() {
        return membershipTypeName;
    }

    public ExtendedUIRolePermissionList preparePermList(UIMembershipTypeForm uiForm) {
        ExtendedUIResourceList resourceList = uiForm.getResources();
        List<ExtendedUIRolePermission> uiPermList = new ArrayList<>();
        for (ExtendedUIResource resource : resourceList.getList()) {
            ExtendedUIRolePermission permission = new ExtendedUIRolePermission();
            permission.setResource(resource.getResourceName());
            permission.setAction(uiForm
                    .getUICheckBoxInput(resource.getResourceName()).getValue() ? "allow" : "deny");
            uiPermList.add(permission);
        }
        ExtendedUIRolePermissionList permList = new ExtendedUIRolePermissionList();
        permList.setRolePermissions(uiPermList);
        return permList;
    }

    public static class SaveActionListener extends
            EventListener<UIMembershipTypeForm> {
        public void execute(Event<UIMembershipTypeForm> event) throws Exception {
            UIMembershipTypeForm uiForm = event.getSource();
            UIMembershipManagement uiMembershipManagement = uiForm.getParent();
            OrganizationService service = uiForm
                    .getApplicationComponent(OrganizationService.class);
            String msTypeName = uiForm.getUIStringInput(MEMBERSHIP_TYPE_NAME)
                    .getValue();

            MembershipType mt = service.getMembershipTypeHandler()
                    .findMembershipType(msTypeName);

            boolean dashboardLinksDisabled = uiForm.getUICheckBoxInput(
                    DASHBOARD_LINKS_DISABLED).getValue();
            boolean actionsEnabled = uiForm.getUICheckBoxInput(ENABLE_ACTIONS)
                    .getValue();
            boolean noRestrictions = uiForm
                    .getUICheckBoxInput(RESTRICTION_TYPE).getValue();
            String[] selectedHGs = uiForm.getUIFormSelectBox(HOSTGROUP)
                    .getSelectedValues();
            StringBuffer selectedServicegroupsBuf = null;
            if (uiForm.getUIFormSelectBox(SERVICEGROUP) != null) {
                selectedServicegroupsBuf = new StringBuffer();
                String[] selectedSGs = uiForm.getUIFormSelectBox(SERVICEGROUP)
                        .getSelectedValues();
                for (String selectedSG : selectedSGs) {
                    logger.info("Selected ServiceGroups ===>" + selectedSG);
                    selectedServicegroupsBuf.append(selectedSG);
                    selectedServicegroupsBuf.append(",");
                } // end for
            }
            String selectedDefaultHG = uiForm.getUIFormSelectBox(DEFAULT_HG)
                    .getValue();

            String selectedDefaultSG = null;
            if (uiForm.getUIFormSelectBox(DEFAULT_SG) != null) {
                selectedDefaultSG = uiForm.getUIFormSelectBox(DEFAULT_SG)
                        .getValue();
            }
            StringBuffer selectedHostgroupsBuf = new StringBuffer();
            for (String selectedHG : selectedHGs) {
                logger.info("Selected HostGroups ===>" + selectedHG);
                selectedHostgroupsBuf.append(selectedHG);
                selectedHostgroupsBuf.append(",");
            }

            logger.info("Default HostGroup ===>" + selectedDefaultHG);
            logger.info("Default ServiceGroup ===>" + selectedDefaultSG);
            String selectedHostgroups = null;
            String selectedServicegroups = null;
            if (noRestrictions) {
                selectedDefaultHG = null;
                selectedDefaultSG = null;
            } else {
                if (selectedHostgroupsBuf.length() > 0) {
                    selectedHostgroups = selectedHostgroupsBuf.substring(0,
                        selectedHostgroupsBuf.length() - 1);
                }
                if ((selectedServicegroupsBuf != null) &&
                    (selectedServicegroupsBuf.length() > 0)) {
                    selectedServicegroups = selectedServicegroupsBuf.substring(0,
                        selectedServicegroupsBuf.length() - 1);
                }
            }

            if (uiForm.getMembershipTypeName() == null) {
                // For create new membershipType case
                if (mt != null) {
                    UIApplication uiApp = event.getRequestContext()
                            .getUIApplication();
                    uiApp.addMessage(new ApplicationMessage(
                            "UIMembershipTypeForm.msg.SameName", null));
                    return;
                }
                mt = service.getMembershipTypeHandler()
                        .createMembershipTypeInstance();
                // uiForm.invokeSetBindingBean(mt);
                // Dont use autobinding option here as MembershipType object
                // does not have any extended role attributes.
                String membershipName = uiForm.getUIStringInput(
                        MEMBERSHIP_TYPE_NAME).getValue();
                mt.setName(membershipName);
                mt.setDescription(uiForm.getUIFormTextAreaInput(DESCRIPTION)
                        .getValue());

                service.getMembershipTypeHandler().createMembershipType(mt,
                        true);


                ExtendedUIRolePermissionList permList = uiForm.preparePermList(uiForm);
                permList.setRolePermissions(permList.getRolePermissions());

                uiForm.createExtendedRole(membershipName,
                        dashboardLinksDisabled, selectedHostgroups,
                        selectedServicegroups,
                        (noRestrictions ? NO_RESTRICTIONS
                                : PARTIAL_RESTRICTIONS), selectedDefaultHG,
                        selectedDefaultSG, actionsEnabled,permList);
                uiMembershipManagement.addOptions(mt);
            } else {
                // For edit a membershipType case
                if (mt == null) {
                    UIApplication uiApp = event.getRequestContext()
                            .getUIApplication();
                    uiApp.addMessage(new ApplicationMessage(
                            "UIMembershipTypeForm.msg.MembershipNotExist",
                            new String[] { msTypeName }));
                } else {
                    // uiForm.invokeSetBindingBean(mt);
                    // Dont use autobinding option here as MembershipType object
                    // does not have any extended role attributes.
                    String membershipName = uiForm.getUIStringInput(
                            MEMBERSHIP_TYPE_NAME).getValue();
                    mt.setName(membershipName);
                    mt.setDescription(uiForm
                            .getUIFormTextAreaInput(DESCRIPTION).getValue());
                    service.getMembershipTypeHandler().saveMembershipType(mt,
                            true);
                    ExtendedUIRolePermissionList permList = uiForm.preparePermList(uiForm);
                    permList.setRolePermissions(permList.getRolePermissions());
                    uiForm.updateExtendedRole(uiForm.getExtRoleId(),
                            membershipName, dashboardLinksDisabled,
                            selectedHostgroups, selectedServicegroups,
                            (noRestrictions ? NO_RESTRICTIONS
                                    : PARTIAL_RESTRICTIONS), selectedDefaultHG,
                            selectedDefaultSG, actionsEnabled,permList);
                }
            }

            uiMembershipManagement.getChild(UIListMembershipType.class)
                    .loadData();
            uiForm.getUIStringInput(MEMBERSHIP_TYPE_NAME).setReadOnly(false);
            uiForm.setMembershipType(null);
            uiForm.reset();
            uiForm.getUIStringInput(MEMBERSHIP_TYPE_NAME).setReadOnly(false);
            uiForm.getUICheckBoxInput(ENABLE_ACTIONS).setChecked(true);
            uiForm.getUICheckBoxInput(RESTRICTION_TYPE).setChecked(true);
            uiForm.setMembershipType(null);
            uiForm.resetApplicationPermCheckboxes();
            UIApplication uiApp = event.getRequestContext()
                    .getUIApplication();
            uiApp.addMessage(new ApplicationMessage(
                    "UIMembershipTypeForm.msg.AddSuccess",
                    new String[] { msTypeName }));
        }
    }

    public static class ResetActionListener extends
            EventListener<UIMembershipTypeForm> {
        public void execute(Event<UIMembershipTypeForm> event) throws Exception {
            UIMembershipTypeForm uiForm = event.getSource();
            uiForm.reset();
            uiForm.getUIStringInput(MEMBERSHIP_TYPE_NAME).setReadOnly(false);
            uiForm.getUICheckBoxInput(ENABLE_ACTIONS).setChecked(true);
            uiForm.getUICheckBoxInput(RESTRICTION_TYPE).setChecked(true);
            uiForm.setMembershipType(null);
            uiForm.resetApplicationPermCheckboxes();

        }
    }

    public static class ShowRestrictionActionListener extends
            EventListener<UIMembershipTypeForm> {
        public void execute(Event<UIMembershipTypeForm> event) throws Exception {
            UIMembershipTypeForm uiForm = event.getSource();
            logger.info("In ShowRestrictionActionListener");
            if (!uiForm.getUICheckBoxInput(RESTRICTION_TYPE).isChecked()) {
                uiForm.getUIFormSelectBox(HOSTGROUP).setRendered(true);
                uiForm.getUIFormSelectBox(DEFAULT_HG).setRendered(true);
                if (uiForm.getUIFormSelectBox(SERVICEGROUP) != null) {
                    uiForm.getUIFormSelectBox(SERVICEGROUP).setRendered(true);
                    uiForm.getUIFormSelectBox(DEFAULT_SG).setRendered(true);
                } // end if
            } else {
                uiForm.getUIFormSelectBox(HOSTGROUP).setRendered(false);
                uiForm.getUIFormSelectBox(DEFAULT_HG).setRendered(false);
                if (uiForm.getUIFormSelectBox(SERVICEGROUP) != null) {
                    uiForm.getUIFormSelectBox(SERVICEGROUP).setRendered(false);
                    uiForm.getUIFormSelectBox(DEFAULT_SG).setRendered(false);
                }
            } // end if
        }
    }

    public static class FilterDefaultHGEntityActionListener extends
            EventListener<UIMembershipTypeForm> {
        public void execute(Event<UIMembershipTypeForm> event) throws Exception {
            logger.info("In FilterDefaultHGEntityActionListener");
            UIMembershipTypeForm uiForm = event.getSource();
            String[] selectedHGs = uiForm.getUIFormSelectBox(HOSTGROUP)
                    .getSelectedValues();
            UIFormSelectBox defaultHG = uiForm.getUIFormSelectBox(DEFAULT_HG);
            List<SelectItemOption<String>> hostgroupOptionsSingle = new ArrayList<SelectItemOption<String>>();
            for (String selectedHG : selectedHGs) {
                // logger.info("Selected HostGroups ===>" + selectedHG);
                hostgroupOptionsSingle.add(new SelectItemOption(selectedHG,
                        selectedHG));
            }
            defaultHG.setOptions(hostgroupOptionsSingle);
        }
    }

    public static class FilterDefaultSGEntityActionListener extends
            EventListener<UIMembershipTypeForm> {
        public void execute(Event<UIMembershipTypeForm> event) throws Exception {
            logger.info("In FilterDefaultSGEntityActionListener");
            UIMembershipTypeForm uiForm = event.getSource();
            String[] selectedSGs = uiForm.getUIFormSelectBox(SERVICEGROUP)
                    .getSelectedValues();
            UIFormSelectBox defaultSG = uiForm.getUIFormSelectBox(DEFAULT_SG);
            List<SelectItemOption<String>> hostgroupOptionsSingle = new ArrayList<SelectItemOption<String>>();
            for (String selectedSG : selectedSGs) {
                // logger.info("Selected HostGroups ===>" + selectedHG);
                hostgroupOptionsSingle.add(new SelectItemOption(selectedSG,
                        selectedSG));
            }
            defaultSG.setOptions(hostgroupOptionsSingle);
        }
    }

    /**
     * Helper for create
     *
     */
    public void createExtendedRole(String roleName,
                                   boolean isDashboardLinksDisabled, String hgList, String sgList,
                                   String restrictionType, String defaultHostgroup,
                                   String defaultServicegroup, boolean actionsEnabled, ExtendedUIRolePermissionList permList) {
        ExtendedRoleClient client = new ExtendedRoleClient(RESTInfo.instance().portal_rest_url, MediaType.APPLICATION_XML_TYPE);
        ExtendedUIRole uiRole = new ExtendedUIRole();
        uiRole.setRoleName(roleName);
        uiRole.setDashboardLinksDisabled(isDashboardLinksDisabled);
        uiRole.setHgList(hgList);
        uiRole.setSgList(sgList);
        uiRole.setRestrictionType(restrictionType);
        uiRole.setDefaultHostGroup(defaultHostgroup);
        uiRole.setDefaultServiceGroup(defaultServicegroup);
        uiRole.setActionsEnabled(actionsEnabled);
        uiRole.setRolePermissions(permList);
        client.createRole(uiRole);

    }

    /**
     * Helper for create
     *
     */
    public void updateExtendedRole(long extRoleId, String roleName,
                                   boolean isDashboardLinksDisabled, String hgList, String sgList,
                                   String restrictionType, String defaultHostgroup,
                                   String defaultServicegroup, boolean actionsEnabled,ExtendedUIRolePermissionList permList) {
        ExtendedRoleClient client = new ExtendedRoleClient(RESTInfo.instance().portal_rest_url, MediaType.APPLICATION_XML_TYPE);
        ExtendedUIRole uiRole = new ExtendedUIRole();
        uiRole.setId(extRoleId);
        uiRole.setRoleName(roleName);
        uiRole.setDashboardLinksDisabled(isDashboardLinksDisabled);
        uiRole.setHgList(hgList);
        uiRole.setSgList(sgList);
        uiRole.setRestrictionType(restrictionType);
        uiRole.setDefaultHostGroup(defaultHostgroup);
        uiRole.setDefaultServiceGroup(defaultServicegroup);
        uiRole.setActionsEnabled(actionsEnabled);
        uiRole.setRolePermissions(permList);
        client.updateRole(uiRole);

    }

    /**
     * Helper for create
     *
     */
    public ExtendedUIRole findExtendedRole(String roleName) {
        ExtendedRoleClient client = new ExtendedRoleClient(RESTInfo.instance().portal_rest_url, MediaType.APPLICATION_XML_TYPE);
        return client.findRoleByName(roleName);
    }

    /**
     * Helper for find
     *
     */
    public ExtendedUIResourceList getResources() {
        ExtendedRoleClient client = new ExtendedRoleClient(RESTInfo.instance().portal_rest_url, MediaType.APPLICATION_XML_TYPE);
        return client.getResources();
    }

    public long getExtRoleId() {
        return extRoleId;
    }

    public void setExtRoleId(long extRoleId) {
        this.extRoleId = extRoleId;
    }

}
