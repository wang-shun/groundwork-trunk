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
import org.groundwork.foundation.bs.EntityBusinessServiceImpl;
import org.groundwork.foundation.bs.exception.BusinessServiceException;
import org.groundwork.foundation.dao.FilterCriteria;
import org.groundwork.foundation.dao.FoundationDAO;
import org.groundwork.foundation.dao.FoundationQueryList;
import org.groundwork.foundation.dao.SortCriteria;

import java.util.Collection;

/**
 * AuditLogServiceImpl
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public class AuditLogServiceImpl extends EntityBusinessServiceImpl implements AuditLogService {

    /** Default sort criteria */
    private static final SortCriteria DEFAULT_SORT_CRITERIA;
    static {
        DEFAULT_SORT_CRITERIA = SortCriteria.desc(AuditLog.HP_TIMESTAMP);
        DEFAULT_SORT_CRITERIA.addSort(AuditLog.HP_ID, false);
    }

    /**
     * AuditLogServiceImpl FoundationDAO constructor.
     *
     * @param foundationDAO service Foundation DAO
     */
    public AuditLogServiceImpl(FoundationDAO foundationDAO) {
        super(foundationDAO, AuditLog.INTERFACE_NAME, AuditLog.COMPONENT_NAME);
    }

    @Override
    public FoundationQueryList getAuditLogs(FilterCriteria filterCriteria, SortCriteria sortCriteria, int firstResult, int maxResults) throws BusinessServiceException {
        sortCriteria = ((sortCriteria != null) ? sortCriteria : DEFAULT_SORT_CRITERIA);
        return query(filterCriteria, sortCriteria, firstResult, maxResults);
    }

    @Override
    public FoundationQueryList queryAuditLogs(String hqlQuery, String hqlCountQuery, int firstResult, int maxResults) throws BusinessServiceException {
        String testHqlQuery = hqlQuery.trim().toLowerCase();
        if (!(testHqlQuery.startsWith("from ") || testHqlQuery.startsWith("select "))) {
            throw new BusinessServiceException("Only AuditLogService HQL SELECT/FROM query supported");
        }
        String testHqlCountQuery = hqlCountQuery.trim().toLowerCase();
        if (!testHqlCountQuery.startsWith("select ") || !testHqlCountQuery.contains(" count(*) ")) {
            throw new BusinessServiceException("Only AuditLogService HQL SELECT count(*) query supported");
        }
        return _foundationDAO.queryWithPaging(hqlQuery, hqlCountQuery, firstResult, maxResults);
    }

    @Override
    public FoundationQueryList getHostAuditLogs(String hostName, int firstResult, int maxResults) throws BusinessServiceException {
        FilterCriteria filterCriteria = null;
        if (hostName != null) {
            filterCriteria = FilterCriteria.eq(AuditLog.HP_HOST_NAME, hostName);
        } else {
            filterCriteria = FilterCriteria.isNotNull(AuditLog.HP_HOST_NAME);
        }
        FilterCriteria notServiceDescriptionFilterCriteria = FilterCriteria.isNull(AuditLog.HP_SERVICE_DESCRIPTION);
        filterCriteria.and(notServiceDescriptionFilterCriteria);
        return query(filterCriteria, DEFAULT_SORT_CRITERIA, firstResult, maxResults);
    }

    @Override
    public FoundationQueryList getServiceAuditLogs(String hostName, String serviceDescription, int firstResult, int maxResults) throws BusinessServiceException {
        FilterCriteria filterCriteria = null;
        if (hostName != null) {
            filterCriteria = FilterCriteria.eq(AuditLog.HP_HOST_NAME, hostName);
        } else {
            filterCriteria = FilterCriteria.isNotNull(AuditLog.HP_HOST_NAME);
        }
        if (serviceDescription != null) {
            FilterCriteria serviceDescriptionFilterCriteria = FilterCriteria.eq(AuditLog.HP_SERVICE_DESCRIPTION, serviceDescription);
            filterCriteria.and(serviceDescriptionFilterCriteria);
        } else {
            FilterCriteria serviceDescriptionFilterCriteria = FilterCriteria.isNotNull(AuditLog.HP_SERVICE_DESCRIPTION);
            filterCriteria.and(serviceDescriptionFilterCriteria);
        }
        return query(filterCriteria, DEFAULT_SORT_CRITERIA, firstResult, maxResults);
    }

    @Override
    public FoundationQueryList getHostGroupAuditLogs(String hostGroupName, int firstResult, int maxResults) throws BusinessServiceException {
        FilterCriteria filterCriteria = null;
        if (hostGroupName != null) {
            filterCriteria = FilterCriteria.eq(AuditLog.HP_HOST_GROUP_NAME, hostGroupName);
        } else {
            filterCriteria = FilterCriteria.isNotNull(AuditLog.HP_HOST_GROUP_NAME);
        }
        return query(filterCriteria, DEFAULT_SORT_CRITERIA, firstResult, maxResults);
    }

    @Override
    public FoundationQueryList getServiceGroupAuditLogs(String serviceGroupName, int firstResult, int maxResults) throws BusinessServiceException {
        FilterCriteria filterCriteria = null;
        if (serviceGroupName != null) {
            filterCriteria = FilterCriteria.eq(AuditLog.HP_SERVICE_GROUP_NAME, serviceGroupName);
        } else {
            filterCriteria = FilterCriteria.isNotNull(AuditLog.HP_SERVICE_GROUP_NAME);
        }
        return query(filterCriteria, DEFAULT_SORT_CRITERIA, firstResult, maxResults);
    }

    @Override
    public AuditLog getAuditLogById(int auditLogId) throws BusinessServiceException {
        return (AuditLog)queryById(auditLogId);
    }

    @Override
    public AuditLog createAuditLog() throws BusinessServiceException {
        return (AuditLog)create();
    }

    @Override
    public void saveAuditLog(AuditLog auditLog) throws BusinessServiceException {
        save(auditLog);
    }

    @Override
    public void saveAuditLogs(Collection<AuditLog> auditLogs) throws BusinessServiceException {
        save(auditLogs);
    }

    @Override
    protected void delete(int objectId) throws BusinessServiceException {
        throw new BusinessServiceException("AuditLogService cannot be used to delete");
    }

    @Override
    protected void delete(int[] objectIds) throws BusinessServiceException {
        throw new BusinessServiceException("AuditLogService cannot be used to delete");
    }

    @Override
    protected void delete(String[] objectIds) throws BusinessServiceException {
        throw new BusinessServiceException("AuditLogService cannot be used to delete");
    }

    @Override
    protected void delete(Collection persistentObjects) throws BusinessServiceException {
        throw new BusinessServiceException("AuditLogService cannot be used to delete");
    }

    @Override
    protected void delete(Object persistentObject) throws BusinessServiceException {
        throw new BusinessServiceException("AuditLogService cannot be used to delete");
    }

    @Override
    protected void save(Object persistentObject) throws BusinessServiceException {
        // check persistent object type
        if (!(persistentObject instanceof AuditLog)) {
            throw new IllegalArgumentException("Non-null AuditLog instance expected");
        }
        // do not allow update
        if (((AuditLog)persistentObject).getAuditLogId() != null) {
            throw new BusinessServiceException("AuditLogService cannot be used to update");
        }
        // save new persistent object
        super.save(persistentObject);
    }

    @Override
    protected void save(Collection persistentObjects) throws BusinessServiceException {
        for (Object persistentObject : persistentObjects) {
            // check persistent object type
            if (!(persistentObject instanceof AuditLog)) {
                throw new IllegalArgumentException("Non-null AuditLog instance expected");
            }
            // do not allow update
            if (((AuditLog)persistentObject).getAuditLogId() != null) {
                throw new BusinessServiceException("AuditLogService cannot be used to update");
            }
        }
        // save new persistent objects
        super.save(persistentObjects);
    }
}
