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

package com.groundworkopensource.portal.identity.extendedui;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.hibernate.type.StandardBasicTypes;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Hibernate;


/**
 * Report Helper is database access class, that is used to retrieve and store
 * User extended UI attributes objects to and from jboss portal database.
 * 
 * @author nitin_jadhav
 * @version GWMON - 6.2
 */
public class ReportHelper {
    /**
     * QUERY
     */
    private static final String QUERY = "select roles.hg_list, roles.restrictionType from gw_ext_role_attributes roles, jbp_role_membership members, jbp_users users where users.jbp_uname=? and users.jbp_uid=members.jbp_uid and members.jbp_rid=roles.jbp_rid";

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
     * Returns Extended Role attributes form Database "jbossportal" by executing
     * SQL query for given user name.
     * 
     * @param userName
     * @return Object (String)
     * @throws HibernateException
     * @throws IOException
     */
    public static List getHostGroupsByUserName(String userName)
            throws HibernateException, IOException {
        Session session = getSessionFactory().openSession();
        try {
            Query query = session.createSQLQuery(QUERY). addScalar("hg_list",  StandardBasicTypes.STRING). addScalar("restrictionType",  StandardBasicTypes.STRING);
            query.setParameter(0, userName);
            return query.list();
        } catch (HibernateException he) {
            LOGGER.severe("Error while retriving records for role : "
                    + userName);
        } finally {
            session.flush();
            session.close();
        }
        return null;
    }

    /**
     * Pre-process query we get from report, to include/exclude selected
     * hostgroups.
     * 
     * @param dataSet
     * @param hostGroupsSet
     * @param userParam
     * @throws IOException
     * @throws ScriptException
     */
    public static String preProcessQuery(String _query, String userParam)
            throws IOException {
        // fetch role-based host groups!
        Object object = ReportHelper.getHostGroupsByUserName(userParam);

        Set<String> hostGroupsSet = new HashSet<String>();
        boolean unrestricted = false;

        String query = _query;

        if (object != null) {
            List list = (List) object;
            for (Object _hglist_object : list) {
                Object[] resultList = (Object[]) _hglist_object;

                // check the restriction type for a particular role. If its
                // N, then user should be given unrestricted access. If its
                // P, check the list of hostgroups.

                if (((String) resultList[1]).equals(NO_RESTRICTION)) {
                    // unrestricted access
                    unrestricted = true;
                    break;
                } else if (((String) resultList[1]).equals(PARTIAL_RESTRICTION)) {
                    // Partial access. make unified hostgroup list
                    if (resultList[0] != null) {
                        String[] hgArray = ((String) resultList[0])
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
     * Do user have access to atleast one host group?
     * 
     * @throws IOException
     * @throws HibernateException
     */
    public static boolean getUserHasHGAccess(String userName)
            throws IOException {
        Object object = getHostGroupsByUserName(userName);
        if (object != null) {
            List list = (List) object;
            for (Object hglistObject : list) {
                Object[] resultList = (Object[]) hglistObject;

                // check the restriction type for a particular role. If its
                // N, then user should be given unrestricted access. If its
                // P, check the list of host groups.

                if (((String) resultList[1]).equals(NO_RESTRICTION)) {
                    // unrestricted access
                    return true;
                } else if (((String) resultList[1]).equals(PARTIAL_RESTRICTION)) {
                    // Partial access.
                    if (resultList[0] != null
                            && ((String) resultList[0]).split(COMMA).length > 0) {
                        return true;
                    }
                }
            }
        }
        // no host group found in any of list => user do not have access to any
        // of host group
        return false;
    }

    /**
     * Does what it says- returns SessionFactory
     * 
     * @return
     * @throws IOException
     */
    public static SessionFactory getSessionFactory() throws IOException {
        try {
            return (SessionFactory) new InitialContext()
                    .lookup("java:/portal/PortletSessionFactory");
        } catch (NamingException badDatabaseName) {
            LOGGER.severe("Error in obtaining Session");
            throw (new IOException());
        }
    }
    
    /**
     * Checks if user is MSP user or not based on the restriction type
     * 
     * @throws IOException
     * @throws HibernateException
     */
    public static boolean isMSPUser(String userName)
            throws IOException {
        Object object = getHostGroupsByUserName(userName);
        if (object != null) {
            List list = (List) object;
            for (Object hglistObject : list) {
                Object[] resultList = (Object[]) hglistObject;

                // check the restriction type for a particular role. If its
                // N, then user should be given unrestricted access. If its
                // P, check the list of host groups.

                if (((String) resultList[1]).equals(NO_RESTRICTION)) {
                    // unrestricted access
                    return false;
                } else if (((String) resultList[1]).equals(PARTIAL_RESTRICTION)) {
                    return true;
                }
            }
        }
        // no host group found in any of list => user do not have access to any
        // of host group
        return false;
    }

}
