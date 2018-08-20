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

package org.groundwork.foundation.bs.auditlog;

import com.groundwork.collage.model.AuditLog;
import org.groundwork.foundation.bs.BusinessService;
import org.groundwork.foundation.bs.exception.BusinessServiceException;
import org.groundwork.foundation.dao.FilterCriteria;
import org.groundwork.foundation.dao.FoundationQueryList;
import org.groundwork.foundation.dao.SortCriteria;

import java.util.Collection;

/**
 * AuditLogService
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public interface AuditLogService extends BusinessService {

    /**
     * General query by criteria API for AuditLog instances. Results
     * are returned sorted by default sort order, (reverse timestamp
     * and id order).
     *
     * @param filterCriteria filter criteria
     * @param sortCriteria optional sort criteria or null
     * @param firstResult index of first result to return
     * @param maxResults number of results to return
     * @return AuditLog query results
     * @throws BusinessServiceException
     */
    FoundationQueryList getAuditLogs(FilterCriteria filterCriteria, SortCriteria sortCriteria, int firstResult, int maxResults) throws BusinessServiceException;

    /**
     * General query by HQL API for AuditLog instances.
     *
     * @param hqlQuery HQL query string
     * @param hqlCountQuery HQL count query string
     * @param firstResult index of first result to return
     * @param maxResults number of results to return
     * @return AuditLog query results
     * @throws BusinessServiceException
     */
    FoundationQueryList queryAuditLogs(String hqlQuery, String hqlCountQuery, int firstResult, int maxResults) throws BusinessServiceException;

    /**
     * Query API for host AuditLog instances. Results are returned
     * sorted by default sort order, (reverse timestamp and id order).
     *
     * @param hostName host name
     * @param firstResult index of first result to return
     * @param maxResults number of results to return
     * @return AuditLog query results
     * @throws BusinessServiceException
     */
    FoundationQueryList getHostAuditLogs(String hostName, int firstResult, int maxResults) throws BusinessServiceException;

    /**
     * Query API for service AuditLog instances. Results are returned
     * sorted by default sort order, (reverse timestamp and id order).
     *
     * @param hostName host name
     * @param serviceDescription service description or null
     * @param firstResult index of first result to return
     * @param maxResults number of results to return
     * @return AuditLog query results
     * @throws BusinessServiceException
     */
    FoundationQueryList getServiceAuditLogs(String hostName, String serviceDescription, int firstResult, int maxResults) throws BusinessServiceException;

    /**
     * Query API for host group AuditLog instances. Results are returned
     * sorted by default sort order, (reverse timestamp and id order).
     *
     * @param hostGroupName host group name
     * @param firstResult index of first result to return
     * @param maxResults number of results to return
     * @return AuditLog query results
     * @throws BusinessServiceException
     */
    FoundationQueryList getHostGroupAuditLogs(String hostGroupName, int firstResult, int maxResults) throws BusinessServiceException;

    /**
     * Query API for service group AuditLog instances. Results are returned
     * sorted by default sort order, (reverse timestamp and id order).
     *
     * @param serviceGroupName service group name
     * @param firstResult index of first result to return
     * @param maxResults number of results to return
     * @return AuditLog query results
     * @throws BusinessServiceException
     */
    FoundationQueryList getServiceGroupAuditLogs(String serviceGroupName, int firstResult, int maxResults) throws BusinessServiceException;

    /**
     * Query by id API for AuditLog instance.
     *
     * @param auditLogId AuditLog id
     * @return AuditLog query result
     * @throws BusinessServiceException
     */
    AuditLog getAuditLogById(int auditLogId) throws BusinessServiceException;

    /**
     * Create new AuditLog instance.
     *
     * @return new AuditLog instance
     * @throws BusinessServiceException
     */
    AuditLog createAuditLog() throws BusinessServiceException;

    /**
     * Save new AuditLog instance, (update forbidden).
     *
     * @param auditLog new AuditLog instance
     * @throws BusinessServiceException
     */
    void saveAuditLog(AuditLog auditLog) throws BusinessServiceException;

    /**
     * Save new AuditLog instances, (updates forbidden).
     *
     * @param auditLogs collection of new AuditLog instances
     * @throws BusinessServiceException
     */
    void saveAuditLogs(Collection<AuditLog> auditLogs) throws BusinessServiceException;
}
