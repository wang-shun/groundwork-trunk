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
package org.groundwork.foundation.bs.performancedata;

import com.groundwork.collage.model.LogPerformanceData;
import com.groundwork.collage.model.PerformanceDataLabel;
import org.groundwork.foundation.bs.BusinessService;
import org.groundwork.foundation.bs.exception.BusinessServiceException;
import org.groundwork.foundation.dao.FilterCriteria;
import org.groundwork.foundation.dao.FoundationQueryList;
import org.groundwork.foundation.dao.SortCriteria;

import java.util.Collection;


/**
 * Interface for accessing PerformanceData related information stored in Foundation.
 * This Business service retrieves data from or related to PerformanceData table.
 * 
 * @author rruttimann@groundworkopensource.com
 * 
 * Created: Jan 9, 2007
 *
 */
public interface PerformanceDataService extends BusinessService {
	
	/**
	 * Generic get driven by Filter criterias
	 * @param filter
	 * @param sort
	 * @param firstResult
	 * @param maxResults
	 * @return FoundationQueryList
	 * @throws BusinessServiceException
	 */
	FoundationQueryList getPerformanceData(FilterCriteria filter, SortCriteria sort, int firstResult, int maxResults ) throws BusinessServiceException;

	/**
	 * Query for all Performance data.
	 * @param hostName
	 * @param serviceDescription
	 * @param performanceDataLabel
	 * @param firstResult if 0 returns all values otherwise start from this index 
	 * @param maxResults number of results returned starting on index defined by firstResult
	 * @return FoundationQueryList
	 * @throws BusinessServiceException
	 */
	FoundationQueryList getPerformanceData(String hostName, String serviceDescription, String performanceDataLabel, int firstResult, int maxResults ) throws BusinessServiceException;
	
	/**
	 * Query for Performance entries for a given Date Range
	 * @param hostName
	 * @param serviceDescription
	 * @param performanceDataLabel
	 * @param startDate
	 * @param endDate
	 * @return FoundationQueryList
	 * @throws BusinessServiceException
	 */
	FoundationQueryList getPerformanceData(String hostName, String serviceDescription, String performanceDataLabel,String startDate, String endDate, int firstResult, int maxResults) throws BusinessServiceException;
	/**
	 * Performance Data can be persisted for a given Host/service combination. Multiple entries can be
	 * created for the same host/service by defining distinguished Performance Data Label entries.
	 * Performance data will be consolidated for a day (recalculating average) and updating min/max entries
	 * Note: Create doesn't persist object. Object will be persisted
	 * @param hostName
	 * @param serviceDescription
	 * @param performanceDataLabel
	 * @param performanceValue
	 * @param checkDate
	 * @throws BusinessServiceException
	 */
	void createOrUpdatePerformanceData(String hostName, String serviceDescription, String performanceDataLabel, double performanceValue, String checkDate) throws BusinessServiceException;

	void createOrUpdatePerformanceData(String hostName, String serviceDescription, String performanceDataLabel, double performanceValue, String checkDate, String rollup) throws BusinessServiceException;
	
	/**
	 * Standard create method for a Performance Data entry
	 * Note: neds to be persisted with save call
	 * @throws BusinessServiceException
	 */
	LogPerformanceData createPerformanceData() throws BusinessServiceException;
	/**
	 * Maintenance method for deleting performance data in a specific date range for a host/service/label combination
	 * @param hostName
	 * @param serviceDescription
	 * @param performanceDataLabel
	 * @param startDate
	 * @param endDate
	 * @throws BusinessServiceException
	 */
	void deletePerformanceData(String hostName, String serviceDescription, String performanceDataLabel, String startDate, String endDate) throws BusinessServiceException;
	
	/**
	 * Standard method for deliting an Object from the persistent store
	 * @param logPerformanceData
	 * @throws BusinessServiceException
	 */
	void deletePerformanceData(LogPerformanceData logPerformanceData)throws BusinessServiceException;
	void deletePerformanceData(String hostName, String serviceName, String performanceName);

	void deleteLabel(String performanceName);

	/**
	 * Standard methods for persisting LogPerformanceData objects
	 * @param performanceData
	 * @throws BusinessServiceException
	 */
	void savePerformanceData(LogPerformanceData performanceData) throws BusinessServiceException;
	
	void savePerformanceData(Collection<LogPerformanceData> collectionPerformanceData ) throws BusinessServiceException;
	
	
	public void updatePerformanceDataLabelEntry(Integer performanceDataLabelId, String serviceDisplayName, String metricLabel, String unit);
	public PerformanceDataLabel createPerformanceDataLabelEntry(String performanceName);
	
	public String getPerformanceDataLabel();

	PerformanceDataLabel lookupPerformanceDataLabel(String performanceName);
	LogPerformanceData lookupPerformanceData(String hostName, String serviceName, String performanceName);

}
