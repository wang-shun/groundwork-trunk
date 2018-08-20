/*
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2014  GroundWork Open Source Solutions info@groundworkopensource.com

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

package com.groundwork.collage.query;

import java.util.Map;

/**
 * AuditLogQueryTranslationInfo
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public class AuditLogQueryTranslatorInfo extends AbstractQueryTranslatorInfo implements QueryTranslatorInfo {

    /**
     * Default constructor with AuditLog query info.
     */
    public AuditLogQueryTranslatorInfo() {
        super("AuditLog", "select distinct a", " from AuditLog a ", "select distinct count(*) from AuditLog a ", null, null);
    }

    @Override
    protected void createBasicProperties(Map properties) {
        properties.put("id".toLowerCase(), "auditLogId");
        properties.put("host".toLowerCase(), "hostName");
        properties.put("service".toLowerCase(), "serviceDescription");
        properties.put("user".toLowerCase(), "username");
        properties.put("hostGroup".toLowerCase(), "hostGroupName");
        properties.put("serviceGroup".toLowerCase(), "serviceGroupName");
    }
}
