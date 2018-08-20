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

package org.groundwork.foundation.bs.hostblacklist;

import com.groundwork.collage.model.HostBlacklist;
import com.groundwork.collage.util.RegexList;
import com.groundwork.collage.util.RegexListListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.bs.EntityBusinessServiceImpl;
import org.groundwork.foundation.bs.exception.BusinessServiceException;
import org.groundwork.foundation.dao.FilterCriteria;
import org.groundwork.foundation.dao.FoundationDAO;
import org.groundwork.foundation.dao.FoundationQueryList;
import org.groundwork.foundation.dao.SortCriteria;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * HostBlacklistServiceImpl
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public class HostBlacklistServiceImpl extends EntityBusinessServiceImpl implements HostBlacklistService, RegexListListener {

    private static Log log = LogFactory.getLog(HostBlacklistServiceImpl.class);

    /** Default sort criteria */
    private static final SortCriteria DEFAULT_SORT_CRITERIA = SortCriteria.asc(HostBlacklist.HP_HOST_NAME);

    /** RegexList blacklist host names timeout */
    private static final long BLACKLIST_REGEXLIST_TIMEOUT = 300000;

    /** RegexList used to match blacklist host names */
    private RegexList blacklistRegexList;

    /**
     * HostBlacklistServiceImpl FoundationDAO constructor.
     *
     * @param foundationDAO service Foundation DAO
     */
    public HostBlacklistServiceImpl(FoundationDAO foundationDAO) {
        super(foundationDAO, HostBlacklist.INTERFACE_NAME, HostBlacklist.COMPONENT_NAME);
    }

    @Override
    public FoundationQueryList getHostBlacklists(FilterCriteria filterCriteria, SortCriteria sortCriteria, int firstResult, int maxResults) throws BusinessServiceException {
        sortCriteria = ((sortCriteria != null) ? sortCriteria : DEFAULT_SORT_CRITERIA);
        return query(filterCriteria, sortCriteria, firstResult, maxResults);
    }

    @Override
    public Collection<String> getHostNames() throws BusinessServiceException {
        return _foundationDAO.query("select hb." + HostBlacklist.HP_HOST_NAME + " from HostBlacklist hb");
    }

    @Override
    public boolean matchHostNameAgainstHostNames(String hostName) {
        // safely allocate blacklist RegexList if necessary
        if (blacklistRegexList == null) {
            synchronized (this) {
                if (blacklistRegexList == null) {
                    blacklistRegexList = new RegexList(this, true, BLACKLIST_REGEXLIST_TIMEOUT);
                }
            }
        }
        // match host name against blacklist RegexList
        return blacklistRegexList.match(hostName);
    }

    @Override
    public FoundationQueryList queryHostBlacklists(String hqlQuery, String hqlCountQuery, int firstResult, int maxResults) throws BusinessServiceException {
        String testHqlQuery = hqlQuery.trim().toLowerCase();
        if (!(testHqlQuery.startsWith("from ") || testHqlQuery.startsWith("select "))) {
            throw new BusinessServiceException("Only HostBlacklistService HQL SELECT/FROM query supported");
        }
        String testHqlCountQuery = hqlCountQuery.trim().toLowerCase();
        if (!testHqlCountQuery.startsWith("select ") || !testHqlCountQuery.contains(" count(*) ")) {
            throw new BusinessServiceException("Only HostBlacklistService HQL SELECT count(*) query supported");
        }
        return _foundationDAO.queryWithPaging(hqlQuery, hqlCountQuery, firstResult, maxResults);
    }

    @Override
    public HostBlacklist getHostBlacklistByHostName(String hostName) throws BusinessServiceException {
        if ((hostName == null) || (hostName.length() == 0)) {
            return null;
        }
        // query by host name
        FilterCriteria filterCriteria = FilterCriteria.eq(HostBlacklist.HP_HOST_NAME, hostName);
        FoundationQueryList results = query(filterCriteria, null, -1, -1);
        return (((results != null) && (results.size() > 0)) ? (HostBlacklist)results.get(0) : null);
    }

    @Override
    public HostBlacklist getHostBlacklistById(int id) throws BusinessServiceException {
        // query by id
        return (HostBlacklist)queryById(id);
    }

    @Override
    public Collection<HostBlacklist> getHostBlacklistsByHostNames(Collection<String> hostNames) throws BusinessServiceException {
        if ((hostNames == null) || hostNames.isEmpty()) {
            return Collections.EMPTY_LIST;
        }
        // query by host names
        FilterCriteria filterCriteria = null;
        for (String hostName : hostNames) {
            FilterCriteria hostNameFilterCriteria = FilterCriteria.eq(HostBlacklist.HP_HOST_NAME, hostName);
            if (filterCriteria == null) {
                filterCriteria = hostNameFilterCriteria;
            } else {
                filterCriteria.or(hostNameFilterCriteria);
            }
        }
        FoundationQueryList results = query(filterCriteria, DEFAULT_SORT_CRITERIA, -1, -1);
        return (((results != null) && (results.size() > 0)) ? (Collection<HostBlacklist>)results.getResults() : Collections.EMPTY_LIST);
    }

    @Override
    public HostBlacklist createHostBlacklist(String hostName) throws BusinessServiceException {
        // create and construct HostBlacklist instance
        com.groundwork.collage.model.impl.HostBlacklist hostBlacklist = (com.groundwork.collage.model.impl.HostBlacklist)create();
        hostBlacklist.setHostName(hostName);
        return hostBlacklist;
    }

    @Override
    public void saveHostBlacklist(HostBlacklist hostBlacklist) throws BusinessServiceException {
        save(hostBlacklist);
    }

    @Override
    public void saveHostBlacklists(List<HostBlacklist> hostBlacklists) throws BusinessServiceException {
        save(hostBlacklists);
    }

    @Override
    public void deleteHostBlacklistById(int id) throws BusinessServiceException {
        delete(id);
    }

    @Override
    public boolean deleteHostBlacklistByHostName(String hostName) throws BusinessServiceException {
        HostBlacklist hostBlacklist = getHostBlacklistByHostName(hostName);
        if (hostBlacklist == null) {
            return false;
        }
        deleteHostBlacklist(hostBlacklist);
        return true;
    }

    @Override
    public void deleteHostBlacklist(HostBlacklist hostBlacklist) throws BusinessServiceException {
        delete(hostBlacklist);
    }

    @Override
    public void deleteHostBlacklists(List<HostBlacklist> hostBlacklists) throws BusinessServiceException {
        delete(hostBlacklists);
    }

    @Override
    public List<Object> getPatterns(boolean caseInsensitive) {
        return _foundationDAO.query("select hb." + HostBlacklist.HP_HOST_NAME + " from HostBlacklist hb");
    }

    @Override
    public void exception(Exception e) {
        log.error("Error getting blacklist RegexList patterns: "+e, e);
    }
}
