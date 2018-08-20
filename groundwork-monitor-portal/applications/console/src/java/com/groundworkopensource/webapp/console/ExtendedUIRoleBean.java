/*
 * 
 * Copyright 2007 GroundWork Open Source, Inc. ("GroundWork") All rights
 * reserved. This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package com.groundworkopensource.webapp.console;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import com.groundworkopensource.portal.model.CommonUtils;

import com.groundworkopensource.portal.model.ExtendedUIRole;

/**
 * @author manish_kjain
 * 
 */
public class ExtendedUIRoleBean {

    /**
     * Extended user role DashboardLinksDisabled
     */
    private boolean isDashboardLinksDisabled = true;
    /**
     * extended host group role list
     */
    private List<String> hostGroupList = new ArrayList<String>();

    /**
     * extended host group role list
     */
    private List<String> serviceGroupList = new ArrayList<String>();
    /**
     * comma separated host group list,empty string if host group list is empty.
     */
    private String hostGroupListString = ConsoleConstants.EMPTY_STRING;

    /**
     * comma separated service group list.empty string if service group list is
     * empty
     */
    private String serviceGroupListString = ConsoleConstants.EMPTY_STRING;

    /**
     * RESTRICTED_KEYWORD to be used in HG and SG lists.
     */
    public static final String RESTRICTED_KEYWORD = "R#STR!CT#D";

    /**
     * Constructor
     */
    public ExtendedUIRoleBean() {
        // getting user role based host group and service group list
        List<ExtendedUIRole> extRoleList = ConsoleHelper
                .getExtendedRoleAttributes();
        // Loop thru the List of extendedAttributes. Remember, a user can be
        // assigned to more than one role!
        Set<String> hostGroupSet = new HashSet<String>();
        Set<String> serviceGroupSet = new HashSet<String>();
        if (extRoleList != null) {

            for (ExtendedUIRole extRole : extRoleList) {
                // If one of the role has dashboard links enabled, then user has
                // dashboard links enabled.
                if (!extRole.isDashboardLinksDisabled()) {
                    isDashboardLinksDisabled = false;
                }

                // compute extended host group and service group role list
                if (extRole.getRestrictionType().equals(
                        ExtendedUIRole.RESTRICTION_TYPE_NONE)) {
                    // No Restrictions - allow all HG and SG
                    hostGroupList.clear();
                    serviceGroupList.clear();

                    return;
                } else {
                    // partial HG / SG selection
                    // Partial Host Group

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
        hostGroupList = new ArrayList<String>(hostGroupSet);
        serviceGroupList = new ArrayList<String>(serviceGroupSet);

        if (hostGroupList.contains(RESTRICTED_KEYWORD)
                && serviceGroupList.contains(RESTRICTED_KEYWORD)) {
            // No Restrictions - allow all HG and SG
            hostGroupList.clear();
            serviceGroupList.clear();
        }
        hostGroupListString = getCommaSeparatedString(hostGroupList);
        serviceGroupListString = getCommaSeparatedString(serviceGroupList);

    }

    /**
     * get comma separated String from list.
     * 
     * @return comma Separated String
     */
    private String getCommaSeparatedString(List<String> list) {
        String commaSeparatedString = ConsoleConstants.EMPTY_STRING;
        if (list.isEmpty() || list.contains(RESTRICTED_KEYWORD)) {
            return commaSeparatedString;
        }

        StringBuffer commaSeparatedStringBuffer = new StringBuffer();
        for (Iterator<String> iterator = list.iterator(); iterator.hasNext();) {
            commaSeparatedStringBuffer.append(iterator.next());
            commaSeparatedStringBuffer.append(ConsoleConstants.COMMA);
        }
        if (commaSeparatedStringBuffer.length() > 0) {
            int lastIndexOf = commaSeparatedStringBuffer
                    .lastIndexOf(ConsoleConstants.COMMA);
            if (lastIndexOf != -1) {
                commaSeparatedString = commaSeparatedStringBuffer.substring(0,
                        lastIndexOf);

            }
        }

        return commaSeparatedString;
    }

    /**
     * Sets the hostGroupList.
     * 
     * @param hostGroupList
     *            the hostGroupList to set
     */
    public void setHostGroupList(List<String> hostGroupList) {
        this.hostGroupList = hostGroupList;

    }

    /**
     * Returns the hostGroupList.
     * 
     * @return the hostGroupList
     */
    public List<String> getHostGroupList() {
        return hostGroupList;
    }

    /**
     * Sets the serviceGroupList.
     * 
     * @param serviceGroupList
     *            the serviceGroupList to set
     */
    public void setServiceGroupList(List<String> serviceGroupList) {
        this.serviceGroupList = serviceGroupList;
    }

    /**
     * Returns the serviceGroupList.
     * 
     * @return the serviceGroupList
     */
    public List<String> getServiceGroupList() {
        return serviceGroupList;
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
     * Sets the isDashboardLinksDisabled.
     * 
     * @param isDashboardLinksDisabled
     *            the isDashboardLinksDisabled to set
     */
    public void setDashboardLinksDisabled(boolean isDashboardLinksDisabled) {
        this.isDashboardLinksDisabled = isDashboardLinksDisabled;
    }

    /**
     * Returns the isDashboardLinksDisabled.
     * 
     * @return the isDashboardLinksDisabled
     */
    public boolean isDashboardLinksDisabled() {
        return isDashboardLinksDisabled;
    }

}
