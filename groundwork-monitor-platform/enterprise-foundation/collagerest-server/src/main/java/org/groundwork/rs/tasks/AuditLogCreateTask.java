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

package org.groundwork.rs.tasks;

import com.groundwork.collage.CollageFactory;
import com.groundwork.collage.model.AuditLog;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.bs.auditlog.AuditLogService;
import org.groundwork.rs.async.RestTransaction;
import org.groundwork.rs.dto.DtoAuditLog;
import org.groundwork.rs.dto.DtoAuditLogList;
import org.groundwork.rs.dto.DtoOperationResults;

import java.util.ArrayList;
import java.util.List;

/**
 * AuditLogCreateTask
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public class AuditLogCreateTask extends AbstractRestTask implements RestRequestTask {

    private static Log log = LogFactory.getLog(AuditLogCreateTask.class);

    /** AuditLogs to create */
    private final DtoAuditLogList dtoAuditLogs;

    /**
     * Create AuditLogs task constructor.
     *
     * @param name task name
     * @param dtoAuditLogs AuditLogs to create
     */
    public AuditLogCreateTask(String name, DtoAuditLogList dtoAuditLogs) {
        super(name, null);
        this.dtoAuditLogs = dtoAuditLogs;
    }

    @Override
    public RestRequestResult call() throws Exception {
        RestTransaction session = new RestTransaction();
        session.startTransaction();
        DtoOperationResults results = createAuditLogs();
        session.releaseSession();
        return new RestRequestResult(results, this, true, 0, false);
    }

    /**
     * Create task AuditLogs.
     *
     * @return operation results
     */
    public DtoOperationResults createAuditLogs() {
        // setup results
        DtoOperationResults results = new DtoOperationResults(AuditLog.ENTITY_TYPE_CODE, DtoOperationResults.INSERT);
        if (dtoAuditLogs.size() == 0) {
            return results;
        }
        // attempt to convert and save all audit logs in one transaction
        AuditLogService auditLogService = CollageFactory.getInstance().getAuditLogService();
        try {
            // convert audit logs
            List<AuditLog> auditLogs = new ArrayList<AuditLog>(dtoAuditLogs.size());
            for (DtoAuditLog dtoAuditLog : dtoAuditLogs.getAuditLogs()) {
                auditLogs.add(convertToAuditLog(auditLogService, dtoAuditLog));
            }
            // save audit logs
            auditLogService.saveAuditLogs(auditLogs);
            // add successes to results
            for (AuditLog auditLog : auditLogs) {
                results.success(auditLog.toString(), "AuditLog saved");
            }
        } catch (Exception e) {
            // if there is only one audit log that has failed to convert or
            // save, emit that result
            if (dtoAuditLogs.size() == 1) {
                // add failure to results
                String message = "Failed to save AuditLog: " + e.getMessage();
                results.fail(dtoAuditLogs.getAuditLogs().get(0).toString(), message);
                log.error(message, e);
            } else {
                // retry conversion and saving of audit logs one at a time in
                // order to ensure that the results are returned in order and
                // that individual audit logs may save
                for (DtoAuditLog dtoAuditLog : dtoAuditLogs.getAuditLogs()) {
                    try {
                        // convert audit log
                        AuditLog auditLog = convertToAuditLog(auditLogService, dtoAuditLog);
                        // save audit log
                        auditLogService.saveAuditLog(auditLog);
                        // add success to results
                        results.success(auditLog.toString(), "AuditLog saved");
                    } catch (Exception e2) {
                        // add failure to results
                        String message = "Failed to save AuditLog: " + e2.getMessage();
                        results.fail(dtoAuditLog.toString(), message);
                        log.error(message, e2);
                    }
                }
            }
        }
        return results;
    }

    private static AuditLog convertToAuditLog(AuditLogService auditLogService, DtoAuditLog dtoAuditLog) {
        AuditLog auditLog = auditLogService.createAuditLog();
        auditLog.setSubsystem(dtoAuditLog.getSubsystem());
        auditLog.setAction(AuditLog.Action.valueOf(dtoAuditLog.getAction()));
        auditLog.setDescription(dtoAuditLog.getDescription().isEmpty() ? null : dtoAuditLog.getDescription());
        auditLog.setUsername(dtoAuditLog.getUsername());
        auditLog.setHostName(dtoAuditLog.getHostName());
        auditLog.setServiceDescription(dtoAuditLog.getServiceDescription());
        auditLog.setHostGroupName(dtoAuditLog.getHostGroupName());
        auditLog.setServiceGroupName(dtoAuditLog.getServiceGroupName());
        return auditLog;
    }
}
