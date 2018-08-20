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

package org.groundwork.rs.conversion;

import com.groundwork.collage.model.AuditLog;
import org.groundwork.rs.dto.DtoAuditLog;

/**
 * AuditLogConverter
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public class AuditLogConverter {

    /**
     * Convert AuditLog model to DtoAuditLog instance.
     *
     * @param auditLog AuditLog model instance
     * @return new DtoAuditLog instance
     */
    public final static DtoAuditLog convert(AuditLog auditLog) {
        DtoAuditLog dtoAuditLog = new DtoAuditLog(
                auditLog.getAuditLogId(),
                auditLog.getSubsystem(),
                ((auditLog.getAction() != null) ? auditLog.getAction().name() : null),
                auditLog.getDescription(),
                auditLog.getUsername(),
                auditLog.getTimestamp());
        dtoAuditLog.setHostName(auditLog.getHostName());
        dtoAuditLog.setServiceDescription(auditLog.getServiceDescription());
        dtoAuditLog.setHostGroupName(auditLog.getHostGroupName());
        dtoAuditLog.setServiceGroupName(auditLog.getServiceGroupName());
        return dtoAuditLog;
    }
}
