/*
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2018  GroundWork Open Source Solutions info@groundworkopensource.com

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

package com.groundwork.core.security.ldap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;

/**
 * LDAPMapper
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public class LDAPMapper {

    public static final String LDAP_MAPPING_ENABLED_PROP = "core.security.ldap.mapping_enabled";
    public static final String LDAP_MAPPING_ENABLED_DEFAULT = "false";

    private static Log logger = LogFactory.getLog(LDAPMapper.class);

    public final static String GROUNDWORK_7_PORTAL_ADMIN_ROLE = "gw-portal-administrator";
    public final static String GROUNDWORK_7_ADMIN_ROLE = "gw-monitoring-administrator";
    public final static String GROUNDWORK_7_PORTAL_OPERATOR_ROLE = "gw-monitoring-operator";
    public final static String GROUNDWORK_7_PORTAL_USER_ROLE = "gw-portal-user";
    public final static Set<String> GROUNDWORK_7_ROLES = new HashSet<String>(Arrays.asList(
            GROUNDWORK_7_PORTAL_ADMIN_ROLE,
            GROUNDWORK_7_ADMIN_ROLE,
            GROUNDWORK_7_PORTAL_OPERATOR_ROLE,
            GROUNDWORK_7_PORTAL_USER_ROLE
    ));

    private LDAPAggregator ldapAggregator;
    private Properties ldapMappingDirectives;

    /**
     * Construct LDAP mapper with aggregator and mappings.
     *
     * @param ldapMappingDirectives LDAP mapping directives
     * @param ldapAggregator LDAP aggregator
     */
    public LDAPMapper(Properties ldapMappingDirectives, LDAPAggregator ldapAggregator) {
        this.ldapMappingDirectives = ldapMappingDirectives;
        this.ldapAggregator = ldapAggregator;
    }

    /**
     * Get mapped user roles from LDAP.
     *
     * @param username principal/username with optional domain prefix
     * @return mapped roles or empty array
     */
    public String[] selectRolesByUsername(String username) {
        // get roles for user
        Set<String> roles = new HashSet<String>(Arrays.asList(ldapAggregator.selectRolesByUsername(username, true)));
        // map user roles from LDAP
        mapRolesByRoleName(roles);
        // strip domain from role names
        for (String role : new ArrayList<String>(roles)) {
            roles.remove(role);
            roles.add(LDAPAggregator.stripDomain(role));
        }
        // map roles from mapping directives
        mapRolesFromDirectives(roles);
        // return roles array
        return roles.toArray(new String[roles.size()]);
    }

    /**
     * Recursively map role roles.
     *
     * @param roles set of roles names with domain prefix
     */
    private void mapRolesByRoleName(Set<String> roles) {
        // recursively map roles
        for (String role : new ArrayList<String>(roles)) {
            Set<String> roleRoles = selectRolesByRoleName(role);
            if (!roleRoles.isEmpty()) {
                roles.remove(role);
                roles.addAll(roleRoles);
            }
        }
    }

    /**
     * Get role roles from LDAP.
     *
     * @param roleName role name with domain prefix
     * @return mapped roles or empty set
     */
    private Set<String> selectRolesByRoleName(String roleName) {
        // get roles for role
        Set<String> roles = new HashSet<String>(Arrays.asList(ldapAggregator.selectRolesByRoleName(roleName, true)));
        // get mapped role roles from LDAP
        mapRolesByRoleName(roles);
        return roles;
    }

    /**
     * Map role names based on mapping directives.
     *
     * @param roles set of stripped role names
     */
    private void mapRolesFromDirectives(Set<String> roles) {
        // map roles from directives
        for (String role : new ArrayList<String>(roles)) {
            try {
                String mappedRole = mapRoleFromDirectives(role, true);
                if (!mappedRole.equals(role)) {
                    roles.remove(role);
                    roles.add(mappedRole);
                }
            } catch (IllegalStateException ise) {
                roles.remove(role);
            }
        }
    }

    /**
     * Map role name based on mapping directives.
     *
     * @param role role name
     * @param initialMap initial map in recursive mapping
     * @return mapped role
     * @throws IllegalStateException if role not mapped
     */
    private String mapRoleFromDirectives(String role, boolean initialMap) {
        // recursively map role from directives
        String mappedRole = ldapMappingDirectives.getProperty(role);
        if (mappedRole == null && initialMap) {
            throw new IllegalStateException("Role not mapped by directive: "+role);
        }
        if (mappedRole == null || mappedRole.isEmpty() || GROUNDWORK_7_ROLES.contains(mappedRole)) {
            return role;
        }
        return mapRoleFromDirectives(mappedRole, false);
    }
}
