/*
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2007  GroundWork Open Source Solutions info@groundworkopensource.com

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
package org.groundwork.foundation.bs.hostgroup;

import com.groundwork.collage.metrics.CollageTimer;
import com.groundwork.collage.model.Category;
import com.groundwork.collage.model.HostGroup;
import com.groundwork.collage.util.Autocomplete;
import com.groundwork.collage.util.AutocompleteName;
import com.groundwork.collage.util.AutocompleteNames;
import com.groundwork.collage.util.AutocompleteNamesIterator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.bs.EntityBusinessServiceImpl;
import org.groundwork.foundation.bs.category.CategoryService;
import org.groundwork.foundation.bs.exception.BusinessServiceException;
import org.groundwork.foundation.dao.FilterCriteria;
import org.groundwork.foundation.dao.FoundationDAO;
import org.groundwork.foundation.dao.FoundationQueryList;
import org.groundwork.foundation.dao.SortCriteria;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * LogMessage Service Implementation Class
 *
 */
public class HostGroupServiceImpl extends EntityBusinessServiceImpl implements HostGroupService, AutocompleteNames
{
	/** Enable Logging **/
	protected static Log log = LogFactory.getLog(HostGroupServiceImpl.class);
		
	/** Default Sort Criteria */
	private static final SortCriteria DEFAULT_SORT_CRITERIA = SortCriteria.asc(HostGroup.HP_NAME);

    private Autocomplete hostGroupAutocompleteService = null;

	protected HostGroupServiceImpl(FoundationDAO foundationDAO) {
		super(foundationDAO, HostGroup.INTERFACE_NAME, HostGroup.COMPONENT_NAME);
	}

	public FoundationQueryList getHostGroups(FilterCriteria filter, SortCriteria sortCriteria, int firstResult, int maxResults) 
	throws BusinessServiceException {
		CollageTimer timer = startMetricsTimer();
		if (sortCriteria == null)
			sortCriteria = DEFAULT_SORT_CRITERIA;
		
		FoundationQueryList results = this.query(filter, sortCriteria, firstResult, maxResults);
		stopMetricsTimer(timer);
		return results;
	}

    public List<HostGroup> getHostGroupsByCategoryId(int categoryId) throws BusinessServiceException {
        // validate query
		CollageTimer timer = startMetricsTimer();
        if (categoryId <= 0) {
            throw new IllegalArgumentException("Category id invalid.");
        }
        // query for host group entities in category
        List<HostGroup> results = _foundationDAO.query("select hg from HostGroup hg, CategoryEntity ce where " +
                "ce." + Category.HP_ENTITIES_CATEGORY_ID.substring(Category.HP_ENTITIES.length()+1) + " = ? and " +
                "ce." + Category.HP_ENTITIES_ID.substring(Category.HP_ENTITIES.length()+1) + " = hg." + HostGroup.HP_ID + " and " +
                "ce." + Category.HP_ENTITIES_TYPE_NAME.substring(Category.HP_ENTITIES.length()+1) + " = '" + CategoryService.ENTITY_TYPE_CODE_HOSTGROUP + "' " +
                "order by hg." + HostGroup.HP_NAME + " asc", categoryId);
		stopMetricsTimer(timer);
		return results;
    }

	public HostGroup getHostGroupById(int hgId) throws BusinessServiceException {
		CollageTimer timer = startMetricsTimer();
		if (hgId < 1)
			throw new IllegalArgumentException("A valid hostgroup id must be provided.");
		
		HostGroup results = (HostGroup)this.queryById(hgId);
		stopMetricsTimer(timer);
		return results;
	}

	public HostGroup getHostGroupByName(String hgName) throws BusinessServiceException {
		CollageTimer timer = startMetricsTimer();
		if (hgName == null || hgName.length()==0)
			throw new IllegalArgumentException("A valid hostgroup name must be provided.");
		
		FilterCriteria hostGroupFilter = FilterCriteria.eq(HostGroup.HP_NAME, hgName);
		FoundationQueryList results = this.getHostGroups(hostGroupFilter, null, -1, -1);
		if (results == null || results.size()==0)
			return null;

		stopMetricsTimer(timer);
		return (HostGroup)results.get(0);
	}

	public HostGroup createHostGroup() throws BusinessServiceException {
		CollageTimer timer = startMetricsTimer();
		HostGroup hostGroup = (HostGroup)this.create();
		stopMetricsTimer(timer);
		return hostGroup;
	}

	public HostGroup createHostGroup(String hostgroupName) throws BusinessServiceException {
		CollageTimer timer = startMetricsTimer();
		HostGroup hostGroup = (HostGroup)this.create();
		hostGroup.setName(hostgroupName);
		stopMetricsTimer(timer);
		return hostGroup;
	}

	public void deleteHostGroup(HostGroup hostGroup) throws BusinessServiceException {
		CollageTimer timer = startMetricsTimer();
		this.delete(hostGroup);
        // refresh autocomplete since name deleted
        refreshAutocompleteOnTransactionCommit();
		stopMetricsTimer(timer);
	}

	public void deleteHostGroup(Collection<HostGroup> hostGroups) throws BusinessServiceException {
		CollageTimer timer = startMetricsTimer();
		if (hostGroups == null || hostGroups.size()==0)
			throw new IllegalArgumentException("HostGroup list cannot be null or empty.");
		
		Iterator<HostGroup> hgIt = hostGroups.iterator();
		while (hgIt.hasNext())
		{
			HostGroup hg = hgIt.next();
			if (hg != null) {
                this.delete(hg);
                // refresh autocomplete since name deleted
                refreshAutocompleteOnTransactionCommit();
            } else {
                log.warn("HostGroup to delete doesn't exist. HostGroup list contains invalid HostGroup objects");
            }
		}
		stopMetricsTimer(timer);
	}

	public void deleteHostGroupById(int hostGroupId) throws BusinessServiceException {
		CollageTimer timer = startMetricsTimer();
		this.delete(hostGroupId);
        // refresh autocomplete since name deleted
        refreshAutocompleteOnTransactionCommit();
        stopMetricsTimer(timer);
	}

	public void deleteHostGroupByName(String hostGroupName) throws BusinessServiceException {
		CollageTimer timer = startMetricsTimer();
		HostGroup hostGroup = this.getHostGroupByName(hostGroupName);
		// Make sure HostGroup exists before calling persistence method
		if (hostGroup != null) {
            this.delete(hostGroup);
            // refresh autocomplete since name deleted
            refreshAutocompleteOnTransactionCommit();
        } else {
            log.warn("HostGroup to delete doesn't exist. Not found:" + hostGroupName);
        }
        stopMetricsTimer(timer);
	}

	// TODO: move to HostService
	public long getHostGroupHostCount(String hostGroupName) throws BusinessServiceException {
		CollageTimer timer = startMetricsTimer();
		if (hostGroupName == null || hostGroupName.length()==0)
		{
			throw new IllegalArgumentException("A valid hostgroup name must be provided.");
		}
		
		FilterCriteria hostgroupNameFilter = FilterCriteria.eq("hostGroups.name", hostGroupName);
		int count = _foundationDAO.queryCount("com.groundwork.collage.model.impl.Host", 
				 hostgroupNameFilter);  
		log.debug("Number of Hosts for the host group" + hostGroupName + "is " + count);
		stopMetricsTimer(timer);
		return count;
	}

	// TODO: move to StatusService
	public int getHostGroupServiceCount(String hostGroupName) throws BusinessServiceException {
		CollageTimer timer = startMetricsTimer();
		if (hostGroupName == null || hostGroupName.length() == 0)
			throw new IllegalArgumentException("A valid hostgroup name must be provided.");
		
		FilterCriteria hostgroupNameFilter = FilterCriteria.eq("host.hostGroups.name", hostGroupName);
				
		int results = _foundationDAO.queryCount("com.groundwork.collage.model.impl.ServiceStatus",
										 hostgroupNameFilter);
		stopMetricsTimer(timer);
		return results;
	}

	public void saveHostGroup(HostGroup hostGroup) throws BusinessServiceException {
		CollageTimer timer = startMetricsTimer();
		this.save(hostGroup);
        // refresh autocomplete since name changed
        refreshAutocompleteOnTransactionCommit();
		stopMetricsTimer(timer);
	}

	public void saveHostGroup(Collection<HostGroup> hostGroups) throws BusinessServiceException {
		CollageTimer timer = startMetricsTimer();
		if (hostGroups == null || hostGroups.size()==0)
			throw new IllegalArgumentException("HostGroup list cannot be null or empty.");
		
		Iterator<HostGroup> hgIt = hostGroups.iterator();
		while (hgIt.hasNext())
		{
			HostGroup hg = hgIt.next();
			this.save(hg);
		}
        // refresh autocomplete since names changed
        refreshAutocompleteOnTransactionCommit();
		stopMetricsTimer(timer);
	}

    public FoundationQueryList queryHostGroups(String hql, String hqlCount, int firstResult, int maxResults) {
		CollageTimer timer = startMetricsTimer();
        FoundationQueryList list = _foundationDAO.queryWithPaging(hql, hqlCount, firstResult, maxResults);
		stopMetricsTimer(timer);
        return list;
    }

    @Override
    public Iterator<AutocompleteName> openNamesIterator(String namesEntityType) {
        return new AutocompleteNamesIterator(getSessionFactory(), "select name from hostgroup");
    }

    @Override
    public void closeNamesIterator(Iterator<AutocompleteName> iterator) {
        ((AutocompleteNamesIterator)iterator).close();
    }

    /**
     * Set autocomplete service, (normally Spring injected).
     *
     * @param hostGroupAutocompleteService autocomplete service
     */
    public void setHostGroupAutocompleteService(Autocomplete hostGroupAutocompleteService) {
        this.hostGroupAutocompleteService = hostGroupAutocompleteService;
    }

    /**
     * Transaction synchronization to refresh autocomplete on commit.
     */
    private TransactionSynchronization transactionCommitCallback = new TransactionSynchronizationAdapter() {
        @Override
        public void afterCommit() {
            if (hostGroupAutocompleteService != null) {
                hostGroupAutocompleteService.refresh();
            }
        }
    };

    /**
     * Register transaction synchronization to invoke autocomplete
     * refresh on commit of current transaction.
     */
    private void refreshAutocompleteOnTransactionCommit() {
        try {
            List<TransactionSynchronization> synchronizations = TransactionSynchronizationManager.getSynchronizations();
            if (!synchronizations.contains(transactionCommitCallback)) {
                TransactionSynchronizationManager.registerSynchronization(transactionCommitCallback);
            }
        } catch (IllegalStateException ise) {
        }
    }
}
	
