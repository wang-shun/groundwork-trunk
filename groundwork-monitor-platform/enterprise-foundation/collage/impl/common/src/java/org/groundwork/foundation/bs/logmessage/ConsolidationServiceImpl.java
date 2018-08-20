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
package org.groundwork.foundation.bs.logmessage;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.groundwork.collage.model.ConsolidationCriteria;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.bs.EntityBusinessServiceImpl;
import org.groundwork.foundation.bs.exception.BusinessServiceException;
import org.groundwork.foundation.dao.FilterCriteria;
import org.groundwork.foundation.dao.FoundationDAO;
import org.groundwork.foundation.dao.FoundationQueryList;
import org.groundwork.foundation.dao.SortCriteria;
import org.springframework.dao.DataAccessException;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Consolidation Service Implementation Class
 * 
 */
public class ConsolidationServiceImpl extends EntityBusinessServiceImpl
		implements ConsolidationService {
	/** Enable Logging **/
	protected static Log log = LogFactory
			.getLog(ConsolidationServiceImpl.class);

	private static final String ERROR_CONSOLIDATIONCRITERIA_CREATE = "Insertion failed for ConsolidationCriteria  x4 [";
	private static final String ERROR_CONSOLIDATIONCRITERIA_LOOKUP_FAILED_2 = "]";
	private static final String ERROR_THROWS_EXCEPTION = "] Systems throws exception: ";

	protected ConsolidationServiceImpl(FoundationDAO foundationDAO) {
		super(foundationDAO, ConsolidationCriteria.INTERFACE_NAME,
				ConsolidationCriteria.COMPONENT_NAME);
	}

	private final LoadingCache<String, Integer> ids = CacheBuilder.newBuilder()
			.expireAfterWrite(5, TimeUnit.MINUTES)
			.build(new CacheLoader<String, Integer>() {
				public Integer load(@SuppressWarnings("NullableProblems") String name) {
					List results = _foundationDAO.sqlQuery("select " + ConsolidationCriteria.HP_ID + " from consolidationcriteria"
							+ " where " + ConsolidationCriteria.HP_NAME + " = '" + name.replaceAll("'", "''") + "'");
					return (results == null || results.size() == 0) ? null : (Integer) results.get(0);
				}
			});


	public void clearIdsCache() {
		ids.invalidateAll();
	}

	private Integer getConsolidationIdByName(String name) {
		try {
			return ids.get(name);
		} catch (ExecutionException | CacheLoader.InvalidCacheLoadException e) {
			return null;
		}
	}

	public ConsolidationCriteria createConsolidationCriteria(String name,
			String criteria) {
		ConsolidationCriteria consolidation = (ConsolidationCriteria)this.create();
		consolidation.setCriteria(criteria);
		consolidation.setName(name);
		return consolidation;
	}

	public void deleteConsolidationCriteriaById(int consolidationCriteriaId) {
		this.delete(consolidationCriteriaId);
		// invalidate in id cache
		for (Map.Entry<String, Integer> id: ids.asMap().entrySet()) {
			if (id.getValue() == consolidationCriteriaId) {
				ids.invalidate(id.getKey());
				break;
			}
		}
	}

	public void deleteConsolidationCriteriaByName(String name) {
		ConsolidationCriteria criteria = this
				.getConsolidationCriteriaByName(name);
		this.delete(criteria);
		// invalidate in id cache
		ids.invalidate(criteria.getName());
	}
	
	public void deleteAll() {
		List<ConsolidationCriteria> ccl = _foundationDAO.query(
				"com.groundwork.collage.model.impl.ConsolidationCriteria",
				null, null);
		this.delete(ccl);
		// invalidate in id cache
		for (ConsolidationCriteria criteria : ccl) {
			ids.invalidate(criteria.getName());
		}
	}

	public Collection<ConsolidationCriteria> getConsolidationCriterias(
			FilterCriteria filter, SortCriteria sort) {
		List<ConsolidationCriteria> results = this.query(filter, sort);
		return results;
	}

	public ConsolidationCriteria getConsolidationCriteriaById( int consolidationCriteriaID) {
		return (ConsolidationCriteria) queryById(consolidationCriteriaID);
	}

	public ConsolidationCriteria getConsolidationCriteriaByName(String name) {
	    Integer id = getConsolidationIdByName(name);
		if (id == null) {
			return null;
		}
		ConsolidationCriteria consolidationCriteria = getConsolidationCriteriaById(id);
		if (consolidationCriteria != null && !consolidationCriteria.getName().equals(name)) {
			ids.invalidate(name);
			return null;
		}
		return consolidationCriteria;
	}

	public int getConsolidationHash(Map properties, String consolidationName,
			String listToExclude) throws BusinessServiceException {
		try {
			StringBuilder infoMsg = null;
			StringBuilder hashString = new StringBuilder();

			ConsolidationCriteria consolidationCriteria = this
					.getConsolidationCriteriaByName(consolidationName);
			if (consolidationCriteria == null) {
				infoMsg = new StringBuilder("ConsolidationCriteria [");
				log.warn(infoMsg.append(consolidationName).append(
						"] doesn't exist.").toString());
				return 0;
			}

			String consolidationCriteriaString = consolidationCriteria
					.getCriteria();
			StringTokenizer tokenizer = new StringTokenizer(
					consolidationCriteriaString, ";");
			String token;
			String tokenValue;

			while (tokenizer.hasMoreTokens()) {
				token = tokenizer.nextToken();

				if (listToExclude.indexOf(token) == -1) {
					// lookup the property value for the string
					tokenValue = (String) properties.get(token);
					if (tokenValue != null && tokenValue.length() > 0) {
						log.info("Consolidation: property [" + token
								+ "] with value [" + tokenValue + "]");
						hashString.append(tokenValue);
					}
				}

			}

			// Got all property values added -- Calculate hash and return
			return hashString.toString().hashCode();

		} catch (Exception e) {
			throw new BusinessServiceException(
					"Exception while calculating Hash for Consolidation", e);
		}
	}

	public void saveConsolidationCriteria(String name, String criteria) {
		if (name == null || name.length() < 1) {
			throw new IllegalArgumentException(
					"Criteria name must be provided.");
		}
		if (criteria == null || criteria.length() < 1) {
			throw new IllegalArgumentException(
					"Criteria criteria string must be provided.");
		}

		ConsolidationCriteria newCriteria = this.createConsolidationCriteria(
				name, criteria);
		this.save(newCriteria);
		// add to id cache
		ids.put(name, newCriteria.getConsolidationCriteriaId());
	}

	public void saveConsolidationCriteria(ConsolidationCriteria criteria) {
		if (criteria == null) {
			throw new IllegalArgumentException(
					"A valid ConsolidationCriteria must be provided.");
		}

		this.save(criteria);
		// add to id cache
		ids.put(criteria.getName(), criteria.getConsolidationCriteriaId());
	}

	public String getConsolidationCriterias() {
		String rows = "";
		ConsolidationCriteria consolidationCriteria;
		try {
			// obj.setPerformanceName(performanceDataLabel);

			/* Get Service Status Object */
			List ccl = _foundationDAO.query(
					"com.groundwork.collage.model.impl.ConsolidationCriteria",
					null, null);

			if (ccl != null && ccl.size() > 0) {
				Iterator it = ccl.iterator();
				while (it.hasNext()) {
					consolidationCriteria = (ConsolidationCriteria) it.next();
					Integer consolidationCriteriaID = consolidationCriteria
							.getConsolidationCriteriaId();
					String r = consolidationCriteriaID.toString();
					String name = consolidationCriteria.getName();
					String criteria = consolidationCriteria.getCriteria();
					rows = rows
							+ tableRow(r, consolidationCriteriaID.toString(),
									name, criteria);
				}
			} else
				consolidationCriteria = (ConsolidationCriteria) ccl.get(0);
		} catch (DataAccessException e) {
			rows = "<tr><td>DataAccessException when trying to retrieve all ConsolidationCriteria.</td></tr>";
		} catch (Exception e) {
			rows = "<tr><td>DataAccessException when trying to retrieve all ConsolidationCriteria.</td></tr>";
		}
		return tableHeading() + rows + tableFooting();

	}

	public String tableHeading() {
		String style = "<style type=\"text/css\"> table.sf{ text-align: center;font-family: Verdana;font-weight: normal;font-size: 11px;color: #404040;width: 620px;border: 1px #6699CC solid;border-collapse: collapse;border-spacing: 0px; white-space: nowrap;}";
		style = style + "td.pe{border-bottom: 2px solid #6699CC;}";
		style = style
				+ "td.pf{ border-bottom: 2px solid #6699CC;border-left: 1px solid #6699CC;background-color: #BEC8D1;text-align: left;text-indent: 5px;font-family: Verdana;font-weight: bold;font-size: 11px;color: #404040; white-space: nowrap;}";
		style = style
				+ "td.ph{ border-bottom: 2px solid #6699CC;border-left: 1px solid #6699CC;background-color: white;text-align: left;text-indent: 5px;font-family: Verdana;font-size: 9px;color: #404040; white-space: nowrap;}";
		style = style
				+ "input.pi{border:0px none;width:90%;height:90%;background-color:	#FFFFE0;font-family: Verdana;font-size: 10px;color: #404040;}";
		style = style + "td.pj{border-left: 1px solid #6699CC;}</style>";
		return style
				+ "<table class=\"sf\"><tr><td class=\"pe\"><b>Name</b></td><td class=\"pe\"><b>Criteria</b></td><td><b>Update Data</b></td></tr>";
	}

	public String tableRow(String r, String consolidationCriteriaID,
			String name, String criteria) {
		return "<tr><td class=\"ph\" width=\"25%\"><input name=\"name"+"."+r+"\" value=\""+name+"\"  class=\"pi\"></td><td  class=\"ph\" width=\"35%\"><input name=\"criteria"+"."+r+"\" value=\""+criteria+"\" class=\"pi\"></td><td align=\"middle\" width=\"5%\" style=\"border-left: 1px solid #6699CC;\"><input name=\"consolidationCriteriaID\" value=\""+consolidationCriteriaID+"\" type=\"checkbox\"  ></td></tr>";
	}

	public String tableFooting() {
		return "<tr><td colspan=\"3\"><center><input TYPE=\"submit\" NAME=\"cmd\" VALUE=\"Update Data\"></center></td></tr></table>";
	}

	public void updateConsolidationCriteriaEntry(
			Integer consolidationCriteriaId, String name, String criteria) {
		log.debug("updateConsolidationCriteriaEntry ConsolidationCriteriaId ["
				+ consolidationCriteriaId + "] Name[" + name
				+ "] crieria[" + criteria + "]");
		ConsolidationCriteria consolidationCriteria = null;
		try {
			FilterCriteria ccFilter = FilterCriteria.eq(
					"consolidationCriteriaId", consolidationCriteriaId);
			List ccl = _foundationDAO.query(
					"com.groundwork.collage.model.impl.ConsolidationCriteria",
					ccFilter, null);

			if (ccl == null || ccl.size() == 0) {

				StringBuilder sb = new StringBuilder(
						ERROR_CONSOLIDATIONCRITERIA_CREATE);
				sb.append(consolidationCriteriaId).append(
						ERROR_CONSOLIDATIONCRITERIA_LOOKUP_FAILED_2).append(
						ERROR_THROWS_EXCEPTION);

				throw new BusinessServiceException(sb.toString());

			} else
				consolidationCriteria = (ConsolidationCriteria) ccl.get(0);
			consolidationCriteria.setName(name);
			consolidationCriteria.setCriteria(criteria);
			_foundationDAO.save(consolidationCriteria);
		} catch (Exception e) {
			log.error("ConsolidationCriteriaId [" + consolidationCriteriaId
					+ "] was not saved!");
		}
	}

    public FoundationQueryList query(String hql, String hqlCount, int firstResult, int maxResults) {
        FoundationQueryList list = _foundationDAO.queryWithPaging(hql, hqlCount, firstResult, maxResults);
        return list;
    }
}