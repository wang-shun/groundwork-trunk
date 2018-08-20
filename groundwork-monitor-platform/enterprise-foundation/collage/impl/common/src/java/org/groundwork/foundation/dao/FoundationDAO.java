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
package org.groundwork.foundation.dao;

import com.groundwork.collage.exception.CollageException;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface FoundationDAO 
{	
	/**
	 * Save or update the persistent object specified.
	 * @param persistentObject
	 * @throws CollageException
	 */
	public void save (Object persistentObject) throws CollageException;
	
	/**
	 * Save or update the persistent objects provided in collection.
	 * @param objects
	 * @throws CollageException
	 */
	public void save (Collection objects) throws CollageException;
	
	/**
	 * Deletes specified persistent object
	 * 
	 * @param persistentObject
	 * @throws CollageException
	 */
	public void delete (Object persistentObject) throws CollageException;
	
	/**
	 * Convenience method to delete a collection of persistent objects
	 * @param persistentObjects
	 */
	public void delete (Collection persistentObjects) throws CollageException;
	
	/**
	 * Delete specified entity with the specified id.
	 * 
	 * @param entityName
	 * @param id
	 * @throws CollageException
	 */
	public void delete (String entityName, int id) throws CollageException;

    /**
     * Delete specified entity with the specified id.
     *
     * @param entityName
     * @param id
     * @throws CollageException
     */
    public void deleteByUUID (String entityName, UUID id) throws CollageException;

    /**
	 * Delete the specified entity with the specified id
	 * 
	 * @param persistentClass
	 * @param id
	 * @throws CollageException
	 */
	public void delete (Class persistentClass, int id) throws CollageException;

    /**
     * Delete the specified entity with the specified id
     *
     * @param persistentClass
     * @param id
     * @throws CollageException
     */
    public void deleteByUUID (Class persistentClass, UUID id) throws CollageException;

    /**
	 * Delete the specified entities with the ids specified.
	 * 
	 * @param entityName
	 * @param ids
	 * @throws CollageException
	 */
	public void delete (String entityName, Collection<Integer> ids) throws CollageException;

    /**
     * Delete the specified entities with the ids specified.
     *
     * @param entityName
     * @param ids
     * @throws CollageException
     */
    public void deleteByUUID (String entityName, Collection<UUID> ids) throws CollageException;

    /**
	 * Delete the specified entities with the ids specified.
	 * 
	 * @param persistentClass
	 * @param ids
	 * @throws CollageException
	 */
	public void delete (Class persistentClass,  Collection<Integer> ids) throws CollageException;

    /**
     * Delete the specified entities with the ids specified.
     *
     * @param persistentClass
     * @param ids
     * @throws CollageException
     */
    public void deleteByUUID (Class persistentClass,  Collection<UUID> ids) throws CollageException;

    /**
	 * Query the specified entity with the specified id
	 * 
	 * @param persistentClass
	 * @param id
	 * @return
	 * @throws CollageException
	 */
	public Object queryById (Class persistentClass, int id) throws CollageException;

    /**
     * Query the specified entity with the specified id
     *
     * @param persistentClass
     * @param id
     * @return
     * @throws CollageException
     */
    public Object queryByUUID (Class persistentClass, UUID id) throws CollageException;

    /**
	 * Query the specified entity with the specified id
	 * 
	 * @param entityName
	 * @param id
	 * @return
	 * @throws CollageException
	 */
	public Object queryById (String entityName, int id) throws CollageException;

    /**
     * Query the specified entity with the specified id
     *
     * @param entityName
     * @param id
     * @return
     * @throws CollageException
     */
    public Object queryByUUID (String entityName, UUID id) throws CollageException;

    /**
	 * Query the specified entities with the specified ids
	 * 
	 * @param persistentClass
	 * @param ids
	 * @param sortCriteria
	 * @return
	 * @throws CollageException
	 */
	public List queryById (Class persistentClass, Collection<Integer> ids, SortCriteria sortCriteria) throws CollageException;

    /**
     * Query the specified entities with the specified ids
     *
     * @param persistentClass
     * @param ids
     * @param sortCriteria
     * @return
     * @throws CollageException
     */
    public List queryByUUID (Class persistentClass, Collection<UUID> ids, SortCriteria sortCriteria) throws CollageException;

    /**
	 * Query the specified entities with the specified ids
	 * 
	 * @param entityName
	 * @param ids
	 * @param sortCriteria
	 * @return
	 * @throws CollageException
	 */
	public List queryById (String entityName, Collection<Integer> ids, SortCriteria sortCriteria) throws CollageException;

    /**
     * Query the specified entities with the specified ids
     *
     * @param entityName
     * @param ids
     * @param sortCriteria
     * @return
     * @throws CollageException
     */
    public List queryByUUID (String entityName, Collection<UUID> ids, SortCriteria sortCriteria) throws CollageException;

    /**
	 * Perform query with the specified criteria.
	 * 
	 * @param entityName
	 * @param filterCriteria
	 * @param sortCriteria
	 * @param projectionCriteria
	 * @param firstResult
	 * @param maxResults
	 * @return
	 * @throws CollageException
	 */
	public FoundationQueryList query(
			String entityName, 
			FilterCriteria filterCriteria, 
			SortCriteria sortCriteria, 
			ProjectionCriteria projectionCriteria,
			int firstResult, 
			int maxResults) throws CollageException;
	
	/**
	 * Perform query with the specified criteria.
	 * 
	 * @param persistentClass
	 * @param filterCriteria
	 * @param sortCriteria
	 * @param projectionCriteria
	 * @param firstResult
	 * @param maxResults
	 * @return
	 * @throws CollageException
	 */
	public FoundationQueryList query(Class persistentClass,
				FilterCriteria filterCriteria, 
				SortCriteria sortCriteria, 
				ProjectionCriteria projectionCriteria,
				int firstResult, 
				int maxResults) throws CollageException;
	
	/**
	 * Perform query with the specified criteria.
	 * 
	 * @param entityName
	 * @param filterCriteria
	 * @param sortCriteria
	 * @return
	 * @throws CollageException
	 */
	public List query(
			String entityName, 
			FilterCriteria filterCriteria, 
			SortCriteria sortCriteria) throws CollageException;
	
	/**
	 * Perform query with the specified criteria.
	 * 
	 * @param persistentClass
	 * @param filterCriteria
	 * @param sortCriteria
	 * @return
	 * @throws CollageException
	 */
	public List query(Class persistentClass,
				FilterCriteria filterCriteria, 
				SortCriteria sortCriteria) throws CollageException;
	
	/**
	 * Performs HQL query.
	 * 
	 * @param hqlQuery - HQL Query String
	 * @return
	 * @throws CollageException
	 */
	public List query(String hqlQuery) throws CollageException;

	/**
	 * Performs HQL query.
	 *
	 * @param hqlQuery - HQL Query String
	 * @param maxResults
	 * @return
	 * @throws CollageException
	 */
	public List queryLimit(String hqlQuery, int maxResults) throws CollageException;

	/**
	 * Performs HQL query with the specified parameters.
	 * 
	 * @param hqlQuery - HQL Query String
	 * @param parameters
	 * @return
	 * @throws CollageException
	 */
	public List query(String hqlQuery, Object[] parameters) throws CollageException;

	/**
	 * Performs HQL query with the specified parameters.
	 *
	 * @param hqlQuery - HQL Query String
	 * @param parameters
	 * @param maxResults
	 * @return
	 * @throws CollageException
	 */
	public List queryLimit(String hqlQuery, Object[] parameters, int maxResults) throws CollageException;

	/**
	 * Performs HQL query with the specified parameter.
	 * 
	 * @param hqlQuery - HQL Query String
	 * @param parameter
	 * @return
	 * @throws CollageException
	 */
	public List query(String hqlQuery, Object parameter) throws CollageException;

	/**
	 * Performs HQL query with the specified parameter.
	 *
	 * @param hqlQuery - HQL Query String
	 * @param parameter
	 * @param maxResults
	 * @return
	 * @throws CollageException
	 */
	public List queryLimit(String hqlQuery, Object parameter, int maxResults) throws CollageException;

	/**
     * Performs HQL query with the specified named parameters.
     *
     * @param hqlQuery - HQL Query String
     * @param parameters
     * @return
     * @throws CollageException
     */
    public List query(String hqlQuery, Map<String,Object> parameters) throws CollageException;

	/**
	 * Performs HQL query with the specified named parameters.
	 *
	 * @param hqlQuery - HQL Query String
	 * @param parameters
	 * @param maxResults
	 * @return
	 * @throws CollageException
	 */
	public List queryLimit(String hqlQuery, Map<String,Object> parameters, int maxResults) throws CollageException;

	/**
	 * Performs SQL query.
	 * 
	 * @param sqlQuery - HQL Query String
	 * @return
	 * @throws CollageException
	 */
	public List sqlQuery(String sqlQuery) throws CollageException;
	
	/**
	 * Performs HQL query with the specified parameters.
	 * 
	 * @param sqlQuery - HQL Query String
	 * @param parameters
	 * @return
	 * @throws CollageException
	 */
	public List sqlQuery(String sqlQuery, Object[] parameters) throws CollageException;
	
	/**
	 * Performs HQL query with the specified parameter.
	 * 
	 * @param sqlQuery - HQL Query String
	 * @param parameter
	 * @return
	 * @throws CollageException
	 */
	public List sqlQuery(String sqlQuery, Object parameter) throws CollageException;

    /**
     * Performs HQL query with the specified named parameters.
     *
     * @param sqlQuery - HQL Query String
     * @param parameters
     * @return
     * @throws CollageException
     */
    public List sqlQuery(String sqlQuery, Map<String,Object> parameters) throws CollageException;

    /**
	 * Perform count query based on filter criteria returning scalar value
	 * 
	 * @param persistentClass
	 * @param filterCriteria
	 * @return
	 * @throws CollageException
	 */
	public int queryCount(Class persistentClass,
				FilterCriteria filterCriteria)
	throws CollageException;	
	
	/**
	 * Perform count query based on filter criteria returning scalar value
	 * 
	 * @param entityName
	 * @param filterCriteria
	 * @return
	 * @throws CollageException
	 */
	public int queryCount(String entityName, FilterCriteria filterCriteria)
		throws CollageException;
	
	/**
	 * Evicts a persistent object from a cache in which it may have been placed
	 * by the persistence layer.
	 * 
	 * This method signature mirrors closely the operation of Hibernate and may
	 * not be the best choice for a general DAO interface; it may have to be
	 * revisited if we ever decide on a different persistence layer
	 * implementation.
	 * @param persistentObject
	 * @throws CollageException
	 */
	public void evict (Object persistentObject) throws CollageException;	
	
	/**
	 * Execute any database operations that may be pending
	 * @throws CollageException
	 */
	public void flush ()  throws CollageException;


    /**
     * Query by and HQL String, limit result set to paging parameters
     *
     * @param hqlQuery
     * @param hqlCount
     * @param firstResult
     * @param maxResults
     * @return a list of entity objects matching the query
     */
    FoundationQueryList queryWithPaging(
            String hqlQuery, String hqlCount, int firstResult, int maxResults) throws CollageException;
}
