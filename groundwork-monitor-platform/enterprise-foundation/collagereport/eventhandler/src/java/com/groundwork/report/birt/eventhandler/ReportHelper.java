/*
 * Copyright 2009 GroundWork Open Source, Inc. ("GroundWork") All rights
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

package com.groundwork.report.birt.eventhandler;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.groundwork.rs.client.ExtendedRoleClient;

import javax.ws.rs.core.MediaType;
import javax.servlet.http.HttpServletRequest;
import org.eclipse.birt.report.engine.api.script.IReportContext;
import javax.servlet.http.HttpSession;


/**
 * Report Helper is database access class, that is used to retrieve and store
 * User extended UI attributes objects to and from jboss portal database.
 *
 * @author nitin_jadhav
 * @version GWMON - 6.2
 */
public class ReportHelper {

    public static final String EXTENDED_ROLE_ATT_BIRT = "com.gwos.portal.ext_role_atts.BIRT";

    /**
     * LIST
     */
    private static final String LIST = "<list>";

    /**
     * PARTIAL_RESTRICTION
     */
    private static final String PARTIAL_RESTRICTION = "P";

    /**
     * NO_RESTRICTION
     */
    private static final String NO_RESTRICTION = "N";

    /**
     * Logger
     */
    private static final Logger LOGGER = Logger.getLogger(ReportHelper.class
            .getName());

    /**
     * QUOTE_COMMA
     */
    private static final String QUOTE_COMMA = "',";
    /**
     * SINGLE_QUOTE
     */
    private static final String SINGLE_QUOTE = "'";
    /**
     * COMMA
     */
    private static final String COMMA = ",";

    /**
     * Pre-process query we get from report, to include/exclude selected
     * hostgroups.
     * @param _query
     * @param reportContext
     * @return
     * @throws IOException
     */
    public static String preProcessQuery(String _query,
                                         IReportContext reportContext) throws IOException {
        com.groundworkopensource.portal.model.ExtendedRoleList extnRoleList = getExtendedRoleAttributes(reportContext);

        Set<String> hostGroupsSet = new HashSet<String>();
        boolean unrestricted = false;

        String query = _query;

        if (extnRoleList != null) {
            for (com.groundworkopensource.portal.model.ExtendedUIRole extnRole : extnRoleList.getList()) {

                // check the restriction type for a particular role. If its
                // N, then user should be given unrestricted access. If its
                // P, check the list of hostgroups.

                if (extnRole.getRestrictionType().equals(NO_RESTRICTION)) {
                    // unrestricted access
                    unrestricted = true;
                    break;
                } else if (extnRole.getRestrictionType().equals(
                        PARTIAL_RESTRICTION)) {
                    // Partial access. make unified hostgroup list
                    if (extnRole.getHgList() != null) {
                        String[] hgArray = ((String) extnRole.getHgList())
                                .split(COMMA);
                        for (String hg : hgArray) {
                            hostGroupsSet.add(hg);
                        }
                    }
                }
            }

            if (unrestricted) {
                // remove [where .. IN .. ] part from
                query = query.substring(0, query.indexOf('['))
                        + query.substring(query.indexOf(']') + 1);

            } else {
                // restricted, process hg list
                // remove [ and ]
                query = query.replace('[', ' ').replace(']', ' ');
                // make a string in the form of 'hg1','hg2','hg3' to include
                // in query.
                StringBuilder sbList = new StringBuilder();
                for (String hostGroup : hostGroupsSet) {
                    sbList.append(SINGLE_QUOTE + hostGroup + QUOTE_COMMA);
                }

                // remove last comma from above string
                String param = sbList.substring(0, sbList.length() - 1);

                // replace ? with above build string
                query = query.replace(LIST, param);
            }

        }

        // return new modified query to dataset
        return query;
    }

    /**
     * Gets the ExtendedRole Attributes
     *
     * @return List of Extended UI Roles
     */
    @SuppressWarnings("unchecked")
    public static com.groundworkopensource.portal.model.ExtendedRoleList getExtendedRoleAttributes(
            IReportContext reportContext) {
        HttpServletRequest request = (HttpServletRequest) reportContext
                .getHttpServletRequest();
        com.groundworkopensource.portal.model.ExtendedRoleList retObj = null;
        HttpSession session = request.getSession();
        Object roleBIRTObj = request.getSession().getAttribute(
                EXTENDED_ROLE_ATT_BIRT);
        if (roleBIRTObj != null) {
            retObj = (com.groundworkopensource.portal.model.ExtendedRoleList) roleBIRTObj;
        } else {
            try {
                if (request != null && request.getSession() != null) {
                    String userParam = request
                            .getParameter(HostByHostGroupDataSetEventHandler.USER);
                    retObj = fetchExtendedRole(userParam);
                    if (retObj != null) {
                        request.getSession().setAttribute(
                                EXTENDED_ROLE_ATT_BIRT, retObj);
                    } // end if
                } // end if
            } catch (Exception pce) {
                LOGGER.severe(pce.getMessage());
            } // end try/catch
        } // end if/else
        return retObj;
    }

    /**
     * Helper for create
     *
     */
    public static com.groundworkopensource.portal.model.ExtendedRoleList fetchExtendedRole(String userName) throws Exception{
        ExtendedRoleClient client = new ExtendedRoleClient(RESTInfo.instance().portal_rest_url,MediaType.APPLICATION_XML_TYPE);
        return client.findRolesByUser(userName);
    }


    /**
     * Checks if user is MSP user or not based on the restriction type
     *
     * @throws IOException
     */
    public static boolean isMSPUser(IReportContext reportContext)
            throws IOException {
        com.groundworkopensource.portal.model.ExtendedRoleList extnRoleList = getExtendedRoleAttributes(reportContext);
        if (extnRoleList != null) {
            for (com.groundworkopensource.portal.model.ExtendedUIRole extnRole : extnRoleList.getList()) {

                // check the restriction type for a particular role. If its
                // N, then user should be given unrestricted access. If its
                // P, check the list of host groups.

                if ((extnRole.getRestrictionType()).equals(NO_RESTRICTION))
                    // unrestricted access
                    return false;
                else
                    return true;
            }
        }
        // no host group found in any of list => user do not have access to any
        // of host group
        return false;
    }

}
