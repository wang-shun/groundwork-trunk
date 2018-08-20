package com.groundworkopensource.portal.statusviewer.bean;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.groundworkopensource.portal.model.ExtendedUIRole;
import com.groundworkopensource.portal.statusviewer.common.Constant;
import com.groundworkopensource.portal.statusviewer.common.PortletUtils;
import com.groundworkopensource.portal.model.CommonUtils;

/**
 * This bean maintains 'extended role' for user (for MSP extended role
 * functionality).
 * 
 * @author swapnil_gujrathi
 * 
 */
public class UserExtendedRoleBean {
    // /** logger. */
    // private static final Logger LOGGER = Logger
    // .getLogger(UserExtendedRoleBean.class.getName());

    /**
     * extended host group role list - if empty, display/show all Host Groups
     */
    private List<String> extRoleHostGroupList = new ArrayList<String>();

    /**
     * extended host group role list - if empty, display/show all Service Groups
     */
    private List<String> extRoleServiceGroupList = new ArrayList<String>();

    /**
     * Extended user role DashboardLinksDisabled
     */
    private boolean isDashboardLinksDisabled = true;

    /**
     * comma separated host group list,null if host group list is empty.
     */
    private String hostGroupListString;

    /**
     * comma separated service group list.null if service group list is empty
     */
    private String serviceGroupListString;
    /**
     * default Host Group
     */
    private String defaultHostGroup = null;

    /**
     * default Service Group
     */
    private String defaultServiceGroup = null;
    /**
     * RESTRICTED_KEYWORD to be used in HG and SG lists.
     */
    public static final String RESTRICTED_KEYWORD = "R#STR!CT#D";

    /**
     * UserRoleBean Constructor
     */
    public UserExtendedRoleBean() {
        this(PortletUtils.getExtendedRoleAttributes());

        hostGroupListString = getCommaSeparatedString(extRoleHostGroupList);
        serviceGroupListString = getCommaSeparatedString(extRoleServiceGroupList);
    }

    /**
     * @param extRoleAttributesList
     */
    public UserExtendedRoleBean(List<ExtendedUIRole> extRoleAttributesList) {
        /*
         * Loop through the List of extendedAttributes. Remember, a user can be
         * assigned to more than one role!
         */

        Set<String> hostGroupSet = new HashSet<String>();
        Set<String> serviceGroupSet = new HashSet<String>();
        if (extRoleAttributesList != null) {

            for (ExtendedUIRole extRole : extRoleAttributesList) {
                // If one of the role has dashboard links enabled, then user has
                // dashboard links enabled.
                if (!extRole.isDashboardLinksDisabled()) {
                    isDashboardLinksDisabled = false;
                    break;
                }
            }
            for (ExtendedUIRole extRole : extRoleAttributesList) {
                // compute extended host group and service group role list
                if (extRole.getRestrictionType().equals(
                        ExtendedUIRole.RESTRICTION_TYPE_NONE)) {
                    // No Restrictions - allow all HG and SG
                    extRoleHostGroupList.clear();
                    extRoleServiceGroupList.clear();
                    setDefaultHostGroup(null);
                    setDefaultServiceGroup(null);
                    return;
                } else {
                    // partial HG / SG selection
                    // Partial Host Group

                    // set default Host Group
                    if (null != extRole.getDefaultHostGroup()) {
                        setDefaultHostGroup(extRole.getDefaultHostGroup());
                    }

                    // set default Service Group
                    if (null != extRole.getDefaultServiceGroup()) {
                        setDefaultServiceGroup(extRole.getDefaultServiceGroup());
                    }

                    if (null == extRole.getHgList()) {
                        if (hostGroupSet.isEmpty()) {
                            hostGroupSet.add(RESTRICTED_KEYWORD);
                        }
                    } else {
                        if (hostGroupSet.contains(RESTRICTED_KEYWORD)) {
                            hostGroupSet.remove(RESTRICTED_KEYWORD);
                        }
                        hostGroupSet.addAll(CommonUtils.convert2HGList(extRole.getHgList()));
                    }

                    // Partial Service Group

                    if (null == extRole.getSgList()) {
                        if (serviceGroupSet.isEmpty()) {
                            serviceGroupSet.add(RESTRICTED_KEYWORD);
                        }
                    } else {
                        if (serviceGroupSet.contains(RESTRICTED_KEYWORD)) {
                            serviceGroupSet.remove(RESTRICTED_KEYWORD);
                        }
                        serviceGroupSet.addAll(CommonUtils.convert2SGList(extRole.getSgList()));
                    }

                }
            } // end for
        } // end if

        // convert set to list
        extRoleHostGroupList = new ArrayList<String>(hostGroupSet);
        extRoleServiceGroupList = new ArrayList<String>(serviceGroupSet);

        // if both contains RESTRICTED keyword, allow all (entire network)
        if (extRoleHostGroupList.contains(RESTRICTED_KEYWORD)
                && extRoleServiceGroupList.contains(RESTRICTED_KEYWORD)) {
            // No Restrictions - allow all HG and SG
            extRoleHostGroupList.clear();
            extRoleServiceGroupList.clear();
        }

        if (extRoleHostGroupList.contains(RESTRICTED_KEYWORD)
                || extRoleHostGroupList.isEmpty()) {
            setDefaultHostGroup(null);
        }
        if (extRoleServiceGroupList.contains(RESTRICTED_KEYWORD)
                || extRoleServiceGroupList.isEmpty()) {
            setDefaultServiceGroup(null);
        }
        // check if extended host group list does not contains default
        // host group then fist element in the list will be set as default
        // host group.
        if (getDefaultHostGroup() != null
                && !extRoleHostGroupList.contains(getDefaultHostGroup())) {
            if (!extRoleHostGroupList.isEmpty()) {
                setDefaultHostGroup(extRoleHostGroupList.get(0));
            }
        }
        // check if extended service group list does not contains default
        // service group then fist element in the list will be set as default
        // service group.
        if (getDefaultServiceGroup() != null
                && !extRoleServiceGroupList.contains(getDefaultServiceGroup())) {
            if (!extRoleServiceGroupList.isEmpty()) {
                setDefaultServiceGroup(extRoleServiceGroupList.get(0));
            }
        }
    }

    /**
     * Returns the isDashboardLinksDisabled.
     * 
     * @return the isDashboardLinksDisabled
     */
    public boolean isDashboardLinksDisabled() {
        return isDashboardLinksDisabled;
    }

    /**
     * Sets the isDashboardLinksDisabled.
     * 
     * @param isDashboardLinksDisabled
     *            the isDashboardLinksDisabled to set
     */
    public void setDashboardLinksDisabled(boolean isDashboardLinksDisabled) {
        this.isDashboardLinksDisabled = isDashboardLinksDisabled;
    }

    /**
     * Returns the extRoleHostGroupList.
     * 
     * @return the extRoleHostGroupList
     */
    public List<String> getExtRoleHostGroupList() {
        return extRoleHostGroupList;
    }

    /**
     * Sets the extRoleHostGroupList.
     * 
     * @param extRoleHostGroupList
     *            the extRoleHostGroupList to set
     */
    public void setExtRoleHostGroupList(List<String> extRoleHostGroupList) {
        this.extRoleHostGroupList = extRoleHostGroupList;
    }

    /**
     * Returns the extRoleServiceGroupList.
     * 
     * @return the extRoleServiceGroupList
     */
    public List<String> getExtRoleServiceGroupList() {
        return extRoleServiceGroupList;
    }

    /**
     * Sets the extRoleServiceGroupList.
     * 
     * @param extRoleServiceGroupList
     *            the extRoleServiceGroupList to set
     */
    public void setExtRoleServiceGroupList(List<String> extRoleServiceGroupList) {
        this.extRoleServiceGroupList = extRoleServiceGroupList;
    }

    /**
     * Sets the hostGroupListString.
     * 
     * @param hostGroupListString
     *            the hostGroupListString to set
     */
    public void setHostGroupListString(String hostGroupListString) {
        this.hostGroupListString = hostGroupListString;
    }

    /**
     * Returns the hostGroupListString.
     * 
     * @return the hostGroupListString
     */
    public String getHostGroupListString() {
        return hostGroupListString;
    }

    /**
     * Sets the serviceGroupListString.
     * 
     * @param serviceGroupListString
     *            the serviceGroupListString to set
     */
    public void setServiceGroupListString(String serviceGroupListString) {
        this.serviceGroupListString = serviceGroupListString;
    }

    /**
     * Returns the serviceGroupListString.
     * 
     * @return the serviceGroupListString
     */
    public String getServiceGroupListString() {
        return serviceGroupListString;
    }

    /**
     * get comma separated String from list.
     * 
     * @return comma Separated String
     */
    private String getCommaSeparatedString(List<String> list) {
        String commaSeparatedString = null;
        if (!list.isEmpty()) {
            StringBuffer commaSeparatedStringBuffer = new StringBuffer();
            for (Iterator<String> iterator = list.iterator(); iterator
                    .hasNext();) {
                commaSeparatedStringBuffer.append(iterator.next());
                commaSeparatedStringBuffer.append(Constant.COMMA);
            }
            if (commaSeparatedStringBuffer.length() > 0) {
                int lastIndexOf = commaSeparatedStringBuffer
                        .lastIndexOf(Constant.COMMA);
                if (lastIndexOf != -1) {
                    commaSeparatedString = commaSeparatedStringBuffer
                            .substring(0, lastIndexOf);

                }
            }
        }
        return commaSeparatedString;
    }

    /**
     * Sets the defaultHostGroup.
     * 
     * @param defaultHostGroup
     *            the defaultHostGroup to set
     */
    public void setDefaultHostGroup(String defaultHostGroup) {
        this.defaultHostGroup = defaultHostGroup;
    }

    /**
     * Returns the defaultHostGroup.
     * 
     * @return the defaultHostGroup
     */
    public String getDefaultHostGroup() {
        return defaultHostGroup;
    }

    /**
     * Sets the defaultServiceGroup.
     * 
     * @param defaultServiceGroup
     *            the defaultServiceGroup to set
     */
    public void setDefaultServiceGroup(String defaultServiceGroup) {
        this.defaultServiceGroup = defaultServiceGroup;
    }

    /**
     * Returns the defaultServiceGroup.
     * 
     * @return the defaultServiceGroup
     */
    public String getDefaultServiceGroup() {
        return defaultServiceGroup;
    }
}
