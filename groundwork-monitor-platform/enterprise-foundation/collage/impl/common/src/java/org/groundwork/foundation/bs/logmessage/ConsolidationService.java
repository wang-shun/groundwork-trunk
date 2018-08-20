/*
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2007  GroundWork Open Source Solutions info@groundworkopensource.com

 *	 This program is free software; you can redistribute it and/or modify
 *	 it under the terms of version 2 of the GNU General Public License
 *	 as published by the Free Software Foundation.

 *	 This program is distributed in the hope that it will be useful,
 *	 but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	 GNU General Public License for more details.

 *	 You should have received a copy of the GNU General Public License
 *	 along with this program; if not, write to the Free Software
 *	 Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.groundwork.foundation.bs.logmessage;

import com.groundwork.collage.model.ConsolidationCriteria;
import org.groundwork.foundation.bs.BusinessService;
import org.groundwork.foundation.bs.exception.BusinessServiceException;
import org.groundwork.foundation.dao.FilterCriteria;
import org.groundwork.foundation.dao.FoundationQueryList;
import org.groundwork.foundation.dao.SortCriteria;

import java.util.Collection;
import java.util.Map;

public interface ConsolidationService extends BusinessService {
	/**
	 * Manage Consolidation criterias
	 * @param name
	 * @param criteria
	 */
	ConsolidationCriteria createConsolidationCriteria(String name, String criteria);
	
	/**
	 * 
	 * @param name
	 */
	void deleteConsolidationCriteriaByName(String name);
	
	/**
	 * 
	 * @param consolidationCriteriaId
	 */
	void deleteConsolidationCriteriaById(int consolidationCriteriaId);
	
	/**
	 * Create and Commit a ConsolidationCriteria
	 * @param name
	 * @param criteria
	 */
	void saveConsolidationCriteria(String name, String criteria);
	
	/**
	 * Commit a previously created ConsolidationCriteria to the db
	 * @param criteria
	 */
	void saveConsolidationCriteria(ConsolidationCriteria criteria);
	
	/**
	 * 
	 * @param name
	 * @return
	 */
	ConsolidationCriteria getConsolidationCriteriaByName(String name);
	
	/**
	 * 
	 * @param consolidationCriteriaID
	 * @return
	 */
	ConsolidationCriteria getConsolidationCriteriaById(int consolidationCriteriaID);
	
	/**
	 * Get a list of available consolidation criterias
	 * @return a list of ConsolidationCriterias
	 */
	Collection<ConsolidationCriteria> getConsolidationCriterias(FilterCriteria filter, SortCriteria sort);
	
	/**
	 * Method that generates an unique hash of all the values that should be
	 * considered for consolidation of messages. The list of properties which
	 * values should be included are defined in the consolidation criteria table
	 * 
	 * @param properties
	 *            List of properties name/value pairs
	 * @param consolidationName
	 * 			  Name of Consolidation
	 * @param listToExclude
	 *            List of comma separated properties that should not be included
	 *            into the hash calculation
	 * @return
	 */
	int getConsolidationHash(Map properties, String consolidationName, String listToExclude) throws BusinessServiceException;
	
	String getConsolidationCriterias();
	
	void updateConsolidationCriteriaEntry(Integer consolidationCriteriaId, String name, String criteria);
	
	/**
	 * Deletes all the consolidation criterias.
	 */
	void deleteAll();

    /**
     * Query by and HQL String, limit result set to paging parameters
     *
     * @param hql
     * @param hqlCount
     * @param firstResult
     * @param maxResults
     * @return a list of consolidation criteria objects matching the query
     */
    public FoundationQueryList query(String hql,  String hqlCount, int firstResult, int maxResults);
}