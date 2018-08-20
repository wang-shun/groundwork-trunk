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

import com.groundwork.collage.CollageFactory;
import com.groundwork.collage.exception.CollageException;
import com.groundwork.collage.metrics.CollageMetrics;
import com.groundwork.collage.metrics.CollageTimer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.metadata.ClassMetadata;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FoundationDAOImpl extends HibernateDaoSupport implements FoundationDAO {
    /**
     * Enable log4j
     */
    protected Log log = LogFactory.getLog(this.getClass());

    private static final int OBJECT_NOT_FOUND_RETRIES = 1;

    /*************************************************************************/
	/* Constructors */
    /*************************************************************************/

    /*************************************************************************/
	/* Public Methods */
    /*************************************************************************/

    private CollageMetrics collageMetrics;

    private CollageMetrics getCollageMetrics() {
        if (collageMetrics == null) {
            collageMetrics = CollageFactory.getInstance().getCollageMetrics();
        }
        return collageMetrics;
    }

    public CollageTimer startMetricsTimer(String methodName) {
        CollageMetrics collageMetrics = getCollageMetrics();
        return (collageMetrics == null ? null : collageMetrics.startTimer("FoundationDAOImpl", methodName));
    }

    public void stopMetricsTimer(CollageTimer timer) {
        CollageMetrics collageMetrics = getCollageMetrics();
        if (collageMetrics != null) collageMetrics.stopTimer(timer);
    }

    /**
     * Save or update the persistent object specified.
     *
     * @param persistentObject
     * @throws CollageException
     */
    public void save(Object persistentObject) throws CollageException {
        CollageTimer timer = startMetricsTimer("saveObject");
        try {
            if (persistentObject == null) {
                throw new IllegalArgumentException("FoundationDAO.save(Object object) - Invalid null persistentObject parameter.");
            }

            try {
                this.getHibernateTemplate().saveOrUpdate(persistentObject);
            } catch (Exception e) {
                throw new CollageException(e);
            }
        } finally {
            stopMetricsTimer(timer);
        }
    }

    /**
     * Save or update the persistent objects provided in collection.
     *
     * @param objects
     * @throws CollageException
     */
    public void save(Collection objects) throws CollageException {
        CollageTimer timer = startMetricsTimer("saveObjects");
        try {
            if (objects == null) {
                throw new IllegalArgumentException("FoundationDAO.save(Collection objects) - Invalid null objects parameter.");
            }

            try {
                this.getHibernateTemplate().saveOrUpdateAll(objects);
            } catch (Exception e) {
                throw new CollageException(e);
            }
        } finally {
            stopMetricsTimer(timer);
        }
    }

    /**
     * Deletes specified persistent object
     *
     * @param persistentObject
     * @throws CollageException
     */
    public void delete(Object persistentObject) throws CollageException {
        CollageTimer timer = startMetricsTimer("deleteObject");
        try {
            if (persistentObject == null) {
                throw new IllegalArgumentException("FoundationDAO.delete(Object persistentObject) - Invalid null persistentObject parameter.");
            }

            try {
                this.getHibernateTemplate().delete(persistentObject);
            } catch (Exception e) {
                throw new CollageException(e);
            }
        } finally {
            stopMetricsTimer(timer);
        }
    }

    /**
     * Convenience method to delete a collection of persistent objects
     *
     * @param persistentObjects
     */
    public void delete(Collection persistentObjects) throws CollageException {
        CollageTimer timer = startMetricsTimer("deleteObjects");
        try {
            if (persistentObjects == null) {
                throw new IllegalArgumentException("FoundationDAO.delete(Collection objects) - Invalid null objects parameter.");
            }

            try {
                this.getHibernateTemplate().deleteAll(persistentObjects);
            } catch (Exception e) {
                throw new CollageException(e);
            }
        } finally {
            stopMetricsTimer(timer);
        }
    }

    /**
     * Delete specified entity with the specified id.
     *
     * @param entityName
     * @param id
     * @throws CollageException
     */
    public void delete(String entityName, int id) throws CollageException {
        try {
            Object obj = this.getSession().get(entityName, new Integer(id));

            if (obj != null) {
                delete(obj);
            }
        } catch (Exception e) {
            throw new CollageException(e);
        }
    }

    /**
     * Delete specified entity with the specified id.
     *
     * @param entityName
     * @param id
     * @throws CollageException
     */
    public void deleteByUUID(String entityName, UUID id) throws CollageException {
        try {
            Object obj = this.getSession().get(entityName, id);

            if (obj != null) {
                delete(obj);
            }
        } catch (Exception e) {
            throw new CollageException(e);
        }
    }

    /**
     * Delete the specified entity with the specified id
     *
     * @param persistentClass
     * @param id
     * @throws CollageException
     */
    public void delete(Class persistentClass, int id) throws CollageException {
        try {
            Object obj = this.getSession().get(persistentClass, new Integer(id));

            if (obj != null) {
                delete(obj);
            }
        } catch (Exception e) {
            throw new CollageException(e);
        }
    }

    /**
     * Delete the specified entity with the specified id
     *
     * @param persistentClass
     * @param id
     * @throws CollageException
     */
    public void deleteByUUID(Class persistentClass, UUID id) throws CollageException {
        try {
            Object obj = this.getSession().get(persistentClass, id);

            if (obj != null) {
                delete(obj);
            }
        } catch (Exception e) {
            throw new CollageException(e);
        }
    }

    /**
     * Delete the specified entities with the ids specified.
     *
     * @param entityName
     * @param ids
     * @throws CollageException
     */
    public void delete(String entityName, Collection<Integer> ids) throws CollageException {
        CollageTimer timer = startMetricsTimer("deleteEntityCollection<Integer>");
        try {
            Criteria criteria = this.getSession().createCriteria(entityName);

            // Add id criterion
            criteria.add(buildIdCriterion(ids));

            // Delete list returned in criteria
            this.getHibernateTemplate().deleteAll(criteria.list());
        } catch (Exception e) {
            throw new CollageException(e);
        } finally {
            stopMetricsTimer(timer);
        }
    }

    /**
     * Delete the specified entities with the ids specified.
     *
     * @param entityName
     * @param ids
     * @throws CollageException
     */
    public void deleteByUUID(String entityName, Collection<UUID> ids) throws CollageException {
        CollageTimer timer = startMetricsTimer("deleteEntityCollection<UUID>");
        try {
            Criteria criteria = this.getSession().createCriteria(entityName);

            // Add id criterion
            criteria.add(buildUUIDCriterion(ids));

            // Delete list returned in criteria
            this.getHibernateTemplate().deleteAll(criteria.list());
        } catch (Exception e) {
            throw new CollageException(e);
        } finally {
            stopMetricsTimer(timer);
        }
    }

    /**
     * Delete the specified entities with the ids specified.
     *
     * @param persistentClass
     * @param ids
     * @throws CollageException
     */
    public void delete(Class persistentClass, Collection<Integer> ids) throws CollageException {
        CollageTimer timer = startMetricsTimer("deleteClassCollection<Integer>");
        try {
            Criteria criteria = this.getSession().createCriteria(persistentClass);

            // Add id criterion
            criteria.add(buildIdCriterion(ids));

            // Delete list returned in criteria
            this.getHibernateTemplate().deleteAll(criteria.list());
        } catch (Exception e) {
            throw new CollageException(e);
        } finally {
            stopMetricsTimer(timer);
        }
    }

    /**
     * Delete the specified entities with the ids specified.
     *
     * @param persistentClass
     * @param ids
     * @throws CollageException
     */
    public void deleteByUUID(Class persistentClass, Collection<UUID> ids) throws CollageException {
        CollageTimer timer = startMetricsTimer("deleteClassCollection<UUID>");
        try {
            Criteria criteria = this.getSession().createCriteria(persistentClass);

            // Add id criterion
            criteria.add(buildUUIDCriterion(ids));

            // Delete list returned in criteria
            this.getHibernateTemplate().deleteAll(criteria.list());
        } catch (Exception e) {
            throw new CollageException(e);
        } finally {
            stopMetricsTimer(timer);
        }
    }

    /**
     * Query the specified entity with the specified id
     *
     * @param persistentClass
     * @param id
     * @return
     * @throws CollageException
     */
    public Object queryById(Class persistentClass, int id) throws CollageException {
        CollageTimer timer = startMetricsTimer("queryClassById");
        try {
            return this.getSession().get(persistentClass, new Integer(id));
        } catch (Exception e) {
            throw new CollageException(e);
        } finally {
            stopMetricsTimer(timer);
        }
    }

    /**
     * Query the specified entity with the specified id
     *
     * @param persistentClass
     * @param id
     * @return
     * @throws CollageException
     */
    public Object queryByUUID(Class persistentClass, UUID id) throws CollageException {
        CollageTimer timer = startMetricsTimer("queryClassByUUID");
        try {
            return this.getSession().get(persistentClass, id);
        } catch (Exception e) {
            throw new CollageException(e);
        } finally {
            stopMetricsTimer(timer);
        }
    }

    /**
     * Query the specified entity with the specified id
     *
     * @param entityName
     * @param id
     * @return
     * @throws CollageException
     */
    public Object queryById(String entityName, int id) throws CollageException {
        CollageTimer timer = startMetricsTimer("queryEntityById");
        try {
            return this.getSession().get(entityName, new Integer(id));
        } catch (Exception e) {
            throw new CollageException(e);
        } finally {
            stopMetricsTimer(timer);
        }
    }

    /**
     * Query the specified entity with the specified id
     *
     * @param entityName
     * @param id
     * @return
     * @throws CollageException
     */
    public Object queryByUUID(String entityName, UUID id) throws CollageException {
        CollageTimer timer = startMetricsTimer("queryEntityByUUID");
        try {
            return this.getSession().get(entityName, id);
        } catch (Exception e) {
            throw new CollageException(e);
        } finally {
            stopMetricsTimer(timer);
        }
    }

    /**
     * Query the specified entities with the specified ids
     *
     * @param persistentClass
     * @param ids
     * @param sortCriteria
     * @return
     * @throws CollageException
     */
    public List queryById(Class persistentClass, Collection<Integer> ids, SortCriteria sortCriteria) throws CollageException {
        CollageTimer timer = startMetricsTimer("queryClassByIds");
        try {
            Criteria criteria = this.getSession().createCriteria(persistentClass);

            // Add id criterion
            criteria.add(buildIdCriterion(ids));

            if (sortCriteria != null) {
                List<Order> sortList = sortCriteria.getSortList();
                Iterator<Order> it = sortList.iterator();
                while (it.hasNext()) {
                    criteria = criteria.addOrder(it.next());
                }

                // Add sort criteria property names and use to create Criteria aliases
                updateAliases(criteria, sortCriteria.getCriteriaAliases());
            }

            return criteria.list();
        } catch (Exception e) {
            throw new CollageException(e);
        } finally {
            stopMetricsTimer(timer);
        }
    }

    /**
     * Query the specified entities with the specified ids
     *
     * @param persistentClass
     * @param ids
     * @param sortCriteria
     * @return
     * @throws CollageException
     */
    public List queryByUUID(Class persistentClass, Collection<UUID> ids, SortCriteria sortCriteria) throws CollageException {
        CollageTimer timer = startMetricsTimer("queryClassByUUIDs");
        try {
            Criteria criteria = this.getSession().createCriteria(persistentClass);

            // Add id criterion
            criteria.add(buildUUIDCriterion(ids));

            if (sortCriteria != null) {
                List<Order> sortList = sortCriteria.getSortList();
                Iterator<Order> it = sortList.iterator();
                while (it.hasNext()) {
                    criteria = criteria.addOrder(it.next());
                }

                // Add sort criteria property names and use to create Criteria aliases
                updateAliases(criteria, sortCriteria.getCriteriaAliases());
            }

            return criteria.list();
        } catch (Exception e) {
            throw new CollageException(e);
        } finally {
            stopMetricsTimer(timer);
        }
    }

    /**
     * Query the specified entities with the specified ids
     *
     * @param entityName
     * @param ids
     * @param sortCriteria
     * @return
     * @throws CollageException
     */
    public List queryById(String entityName, Collection<Integer> ids, SortCriteria sortCriteria) throws CollageException {
        CollageTimer timer = startMetricsTimer("queryEntityByIds");
        try {
            Criteria criteria = this.getSession().createCriteria(entityName);

            // Add id criterion
            criteria.add(buildIdCriterion(ids));

            if (sortCriteria != null) {
                List<Order> sortList = sortCriteria.getSortList();
                Iterator<Order> it = sortList.iterator();
                while (it.hasNext()) {
                    criteria = criteria.addOrder(it.next());
                }

                // Add sort criteria property names and use to create Criteria aliases
                updateAliases(criteria, sortCriteria.getCriteriaAliases());
            }

            return criteria.list();
        } catch (Exception e) {
            throw new CollageException(e);
        } finally {
            stopMetricsTimer(timer);
        }
    }

    /**
     * Query the specified entities with the specified ids
     *
     * @param entityName
     * @param ids
     * @param sortCriteria
     * @return
     * @throws CollageException
     */
    public List queryByUUID(String entityName, Collection<UUID> ids, SortCriteria sortCriteria) throws CollageException {
        CollageTimer timer = startMetricsTimer("queryEntityByUUIDs");
        try {
            Criteria criteria = this.getSession().createCriteria(entityName);

            // Add id criterion
            criteria.add(buildUUIDCriterion(ids));

            if (sortCriteria != null) {
                List<Order> sortList = sortCriteria.getSortList();
                Iterator<Order> it = sortList.iterator();
                while (it.hasNext()) {
                    criteria = criteria.addOrder(it.next());
                }

                // Add sort criteria property names and use to create Criteria aliases
                updateAliases(criteria, sortCriteria.getCriteriaAliases());
            }

            return criteria.list();
        } catch (Exception e) {
            throw new CollageException(e);
        } finally {
            stopMetricsTimer(timer);
        }
    }

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
            int maxResults) throws CollageException {
        try {
            return performQuery(entityName, filterCriteria, sortCriteria, projectionCriteria, firstResult, maxResults);
        } catch (CollageException ce) {
            throw ce;
        } catch (Exception e) {
            throw new CollageException(e);
        }
    }

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
                                     int maxResults) throws CollageException {
        if (persistentClass == null)
            throw new IllegalArgumentException("Invalid null Class parameter.");

        try {
            return performQuery(persistentClass.getName(), filterCriteria, sortCriteria, projectionCriteria, firstResult, maxResults);
        } catch (CollageException ce) {
            throw ce;
        } catch (Exception e) {
            throw new CollageException(e);
        }
    }

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
            SortCriteria sortCriteria)
            throws CollageException {
        try {
            FoundationQueryList fql = performQuery(entityName,
                    filterCriteria,
                    sortCriteria,
                    null,
                    -1,
                    -1);

            return fql.getResults();
        } catch (CollageException ce) {
            throw ce;
        } catch (Exception e) {
            throw new CollageException(e);
        }
    }

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
                      SortCriteria sortCriteria)
            throws CollageException {
        if (persistentClass == null)
            throw new IllegalArgumentException("Invalid null Class parameter.");

        try {
            FoundationQueryList fql = performQuery(persistentClass.getName(),
                    filterCriteria,
                    sortCriteria,
                    null,
                    -1,
                    -1);

            return fql.getResults();
        } catch (CollageException ce) {
            throw ce;
        } catch (Exception e) {
            throw new CollageException(e);
        }
    }

    /**
     * Performs specified hql query with named parameters.
     */
    public List query(final String hqlQuery, final Map<String,Object> parameters) throws CollageException {
        return query(hqlQuery, parameters, -1, OBJECT_NOT_FOUND_RETRIES);
    }

    public List queryLimit(final String hqlQuery, final Map<String,Object> parameters, final int maxResults) throws CollageException {
        return query(hqlQuery, parameters, maxResults, OBJECT_NOT_FOUND_RETRIES);
    }

    private List query(final String hqlQuery, final Map<String,Object> parameters, final int maxResults, final int retry) throws CollageException {
        try {
            Query query = this.getSession().createQuery(hqlQuery);

            if (parameters != null && !parameters.isEmpty()) {
                for (Map.Entry<String,Object> parameter : parameters.entrySet()) {
                    Object parameterValue = parameter.getValue();
                    if (parameterValue instanceof Collection) {
                        query.setParameterList(parameter.getKey(), (Collection)parameterValue);
                    } else if (parameterValue instanceof Object[]) {
                        query.setParameterList(parameter.getKey(), (Object[])parameterValue);
                    } else {
                        query.setParameter(parameter.getKey(), parameterValue);
                    }
                }
            }
            if (maxResults > -1) {
                query.setMaxResults(maxResults);
            }

            return query.list();
        } catch (ObjectNotFoundException onfe) {
            if (retry > 0) {
                // retry on object not found exceptions: happens when
                // objects are deleted out from under queries
                return query(hqlQuery, parameters, maxResults, retry-1);
            }
            throw onfe;
        }
    }

    /**
     * Performs specified hql query with parameters.
     */
    public List query(final String hqlQuery, final Object[] parameters) throws CollageException {
        return query(hqlQuery, parameters, -1, OBJECT_NOT_FOUND_RETRIES);
    }

    public List queryLimit(final String hqlQuery, final Object[] parameters, final int maxResults) throws CollageException {
        return query(hqlQuery, parameters, maxResults, OBJECT_NOT_FOUND_RETRIES);
    }

    private List query(final String hqlQuery, final Object[] parameters, final int maxResults, final int retry) throws CollageException {
        try {
            Query query = this.getSession().createQuery(hqlQuery);

            if (parameters != null && parameters.length != 0) {
                for (int i = 0; i < parameters.length; i++) {
                    query.setParameter(i, parameters[i]);
                }
            }
            if (maxResults > -1) {
                query.setMaxResults(maxResults);
            }

            return query.list();
        } catch (ObjectNotFoundException onfe) {
            if (retry > 0) {
                // retry on object not found exceptions: happens when
                // objects are deleted out from under queries
                return query(hqlQuery, parameters, maxResults, retry-1);
            }
            throw onfe;
        }
    }

    /**
     * Performs specified hql query with parameter.
     */
    public List query(final String hqlQuery, final Object parameter) throws CollageException {
        return query(hqlQuery, parameter, -1, OBJECT_NOT_FOUND_RETRIES);
    }

    public List queryLimit(final String hqlQuery, final Object parameter, final int maxResults) throws CollageException {
        return query(hqlQuery, parameter, maxResults, OBJECT_NOT_FOUND_RETRIES);
    }

    private List query(final String hqlQuery, final Object parameter, final int maxResults, final int retry) throws CollageException {
        try {
            Query query = this.getSession().createQuery(hqlQuery);

            if (parameter != null)
                query.setParameter(0, parameter);
            if (maxResults > -1) {
                query.setMaxResults(maxResults);
            }

            return query.list();
        } catch (ObjectNotFoundException onfe) {
            if (retry > 0) {
                // retry on object not found exceptions: happens when
                // objects are deleted out from under queries
                return query(hqlQuery, parameter, maxResults, retry-1);
            }
            throw onfe;
        }
    }

    /**
     * Performs specified hql query.
     */
    public List query(final String hqlQuery) throws CollageException {
        return query(hqlQuery, -1, OBJECT_NOT_FOUND_RETRIES);
    }

    public List queryLimit(final String hqlQuery, final int maxResults) throws CollageException {
        return query(hqlQuery, maxResults, OBJECT_NOT_FOUND_RETRIES);
    }

    private List query(final String hqlQuery, final int maxResults, final int retry) throws CollageException {
        try {
            Query query = this.getSession().createQuery(hqlQuery);

            if (maxResults > -1) {
                query.setMaxResults(maxResults);
            }

            return query.list();
        } catch (ObjectNotFoundException onfe) {
            if (retry > 0) {
                // retry on object not found exceptions: happens when
                // objects are deleted out from under queries
                return query(hqlQuery, maxResults, retry-1);
            }
            throw onfe;
        }
    }

    /**
     * Performs specified sql query with named parameters.
     */
    public List sqlQuery(final String sqlQuery, final Map<String,Object> parameters) throws CollageException {
        return sqlQuery(sqlQuery, parameters, OBJECT_NOT_FOUND_RETRIES);
    }

    private List sqlQuery(final String sqlQuery, final Map<String,Object> parameters, final int retry) throws CollageException {
        CollageTimer timer = startMetricsTimer("sqlQueryMap");
        try {
            Query query = this.getSession().createSQLQuery(sqlQuery);

            // Set parameters
            if (parameters != null && !parameters.isEmpty()) {
                for (Map.Entry<String,Object> parameter : parameters.entrySet()) {
                    Object parameterValue = parameter.getValue();
                    if (parameterValue instanceof Collection) {
                        query.setParameterList(parameter.getKey(), (Collection)parameterValue);
                    } else if (parameterValue instanceof Object[]) {
                        query.setParameterList(parameter.getKey(), (Object[])parameterValue);
                    } else {
                        query.setParameter(parameter.getKey(), parameterValue);
                    }
                }
            }

            return query.list();
        } catch (ObjectNotFoundException onfe) {
            if (retry > 0) {
                // retry on object not found exceptions: happens when
                // objects are deleted out from under queries
                return sqlQuery(sqlQuery, parameters, retry-1);
            }
            throw onfe;
        } finally {
            stopMetricsTimer(timer);
        }
    }

    /**
     * Performs specified sql query with parameters.
     */
    public List sqlQuery(final String sqlQuery, final Object[] parameters) throws CollageException {
        return sqlQuery(sqlQuery, parameters, OBJECT_NOT_FOUND_RETRIES);
    }

    private List sqlQuery(final String sqlQuery, final Object[] parameters, final int retry) throws CollageException {
        CollageTimer timer = startMetricsTimer("sqlQueryArray");
        try {
            Query query = this.getSession().createSQLQuery(sqlQuery);

            // Set parameters
            if (parameters != null && parameters.length != 0) {
                for (int i = 0; i < parameters.length; i++) {
                    query.setParameter(i, parameters[i]);
                }
            }

            return query.list();
        } catch (ObjectNotFoundException onfe) {
            if (retry > 0) {
                // retry on object not found exceptions: happens when
                // objects are deleted out from under queries
                return sqlQuery(sqlQuery, parameters, retry - 1);
            }
            throw onfe;
        } finally {
            stopMetricsTimer(timer);
        }
    }

    /**
     * Performs specified sql query with parameter.
     */
    public List sqlQuery(final String sqlQuery, final Object parameter) throws CollageException {
        return sqlQuery(sqlQuery, parameter, OBJECT_NOT_FOUND_RETRIES);
    }

    private List sqlQuery(final String sqlQuery, final Object parameter, final int retry) throws CollageException {
        CollageTimer timer = startMetricsTimer("sqlQueryObject");
        try {
            Query query = this.getSession().createSQLQuery(sqlQuery);

            if (parameter != null)
                query.setParameter(0, parameter);

            return query.list();
        } catch (ObjectNotFoundException onfe) {
            if (retry > 0) {
                // retry on object not found exceptions: happens when
                // objects are deleted out from under queries
                return sqlQuery(sqlQuery, parameter, retry-1);
            }
            throw onfe;
        } finally {
            stopMetricsTimer(timer);
        }
    }

    /**
     * Performs specified sql query.
     */
    public List sqlQuery(final String sqlQuery) throws CollageException {
        return sqlQuery(sqlQuery, OBJECT_NOT_FOUND_RETRIES);
    }

    private List sqlQuery(final String sqlQuery, final int retry) throws CollageException {
        CollageTimer timer = startMetricsTimer("sqlQuery");
        try {
            Query query = this.getSession().createSQLQuery(sqlQuery);
            return query.list();
        } catch (ObjectNotFoundException onfe) {
            if (retry > 0) {
                // retry on object not found exceptions: happens when
                // objects are deleted out from under queries
                return sqlQuery(sqlQuery, retry-1);
            }
            throw onfe;
        } finally {
            stopMetricsTimer(timer);
        }
    }

    /**
     * Perform count query based on filter criteria returning scalar value
     *
     * @param persistentClass
     * @param filterCriteria
     * @return
     * @throws CollageException
     */
    public int queryCount(Class persistentClass, FilterCriteria filterCriteria)
            throws CollageException {
        if (persistentClass == null)
            throw new IllegalArgumentException("Invalid null Class parameter.");

        return performCountQuery(persistentClass.getName(), filterCriteria);
    }

    /**
     * Perform count query based on filter criteria returning scalar value
     *
     * @param entityName
     * @param filterCriteria
     * @return
     * @throws CollageException
     */
    public int queryCount(String entityName, FilterCriteria filterCriteria)
            throws CollageException {
        return performCountQuery(entityName, filterCriteria);
    }

    /**
     * Removes persistent object from
     *
     * @param persistentObject
     */
    public void evict(Object persistentObject) throws CollageException {
        CollageTimer timer = startMetricsTimer("evict");
        try {
            if (persistentObject == null)
                return;

            if (log.isDebugEnabled())
                log.debug("attempting to evict " + persistentObject + "...");

            try {
                getHibernateTemplate().evict(persistentObject);
            } catch (DataAccessException e) {
                String msg = "Unable to evict object '" + persistentObject;
                log.error(msg, e);
                throw new CollageException(e);
            }

            if (log.isDebugEnabled())
                log.debug("successfully evicted " + persistentObject);
        } finally {
            stopMetricsTimer(timer);
        }
    }

    /* Execute any database operations that may be pending */
    public void flush() throws CollageException {
        CollageTimer timer = startMetricsTimer("flush");
        if (log.isDebugEnabled()) log.debug("attempting to flush hibernate session...");

        try {
            getHibernateTemplate().flush();
        } catch (DataAccessException e) {
            String msg = "Unable to flush hibernate session";
            log.error(msg, e);
            throw new CollageException(e);
        } finally {
            stopMetricsTimer(timer);
        }

        if (log.isDebugEnabled()) log.debug("successfully flushed hibernate session");
    }

    /*************************************************************************/
	/* Private Methods */

    /**
     * *********************************************************************
     */

    private int performCountQuery(String entityName, FilterCriteria filterCriteria)
            throws CollageException {
        try {
            Criteria criteria = this.getSession().createCriteria(entityName);

            Collection<CriteriaAlias> aliases = new HashSet<CriteriaAlias>(5);

            // Apply filterCriteria
            if (filterCriteria != null) {
                criteria = criteria.add(filterCriteria.getCriterion());

                // Add filter criteria property names to be used later to create Criteria aliases
                aliases.addAll(filterCriteria.getCriteriaAliases());
            }

            // Perform distinct count on id if available for queries which result in
            // cartesian products.
            ClassMetadata metadata = this.getSessionFactory().getClassMetadata(entityName);
            String idPropertyName = metadata.getIdentifierPropertyName();
            if (idPropertyName != null) {
                criteria.setProjection(Projections.countDistinct(idPropertyName));
            } else {
                criteria.setProjection(Projections.rowCount());
            }

            // Apply Aliases based on property names in filter criteria
            updateAliases(criteria, aliases);

            // Perform query - Note:  count projections return Integers and not Longs
            Integer count = (Integer) criteria.uniqueResult();

            return count.intValue();
        } catch (Exception e) {
            throw new CollageException("Error occurred perfoming count query.", e);
        }
    }

    /**
     * Method is used to perform a "simple" query - A query with no joins.
     * NOTE:  ProjectionCriteria is not currently supported.
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
    private FoundationQueryList performSimpleQuery(
            String entityName,
            FilterCriteria filterCriteria,
            SortCriteria sortCriteria,
            ProjectionCriteria projectionCriteria,
            int firstResult,
            int maxResults) throws CollageException {
        String[] entityParts = entityName.split("\\.");
        String entityDescription = entityParts[entityParts.length - 1];
        CollageTimer timer = startMetricsTimer("performSimpleQuery-" + entityDescription);
        Session session = this.getSession();

        Criteria criteria = session.createCriteria(entityName);

        // Apply filterCriteria
        if (filterCriteria != null) {
            if (filterCriteria.getCriteriaAliases() != null && filterCriteria.getCriteriaAliases().size() > 0)
                throw new CollageException("Unable to perform simple query - FilterCriteria defined is complex");

            criteria = criteria.add(filterCriteria.getCriterion());
        }

        // Apply Sort Criteria
        if (sortCriteria != null) {
            if (sortCriteria.getCriteriaAliases() != null && sortCriteria.getCriteriaAliases().size() > 0)
                throw new CollageException("Unable to perform simple query - SortCriteria defined is complex");

            List<Order> sortList = sortCriteria.getSortList();
            Iterator<Order> it = sortList.iterator();
            while (it.hasNext()) {
                criteria = criteria.addOrder(it.next());
            }
        }

        int totalCount = -1;

        // Pagination
        if (firstResult > 0 || maxResults > 0) {
            totalCount = queryTotalCount(entityName, filterCriteria);

            if (firstResult < 1)
                firstResult = 0;

            // Just return an empty result set if total count is less than 1 OR
            // if the first index is outside the total count
            if (totalCount < 0 || firstResult >= totalCount) {
                stopMetricsTimer(timer);
                return new FoundationQueryList(new ArrayList(0), 0);
            }
            // If total count - the first index is more then max results we don't have to set max results
            else if (maxResults > 0 && (maxResults <= (totalCount - firstResult))) {
                criteria.setMaxResults(maxResults);
            }

            if (firstResult > 0)
                criteria.setFirstResult(firstResult);
        }

        FoundationQueryList results = new FoundationQueryList(criteria.list(), totalCount);
        stopMetricsTimer(timer);
        return results;
    }

    /**
     * Perform query with the specified criteria.
     * NOTE:  ProjectionCriteria is not currently supported.
     *
     * @param entityName
     * @param filterCriteria
     * @param sortCriteria
     * @param projectionCriteria,
     * @param firstResult
     * @param maxResults
     * @return
     * @throws CollageException
     */
    private FoundationQueryList performQuery(
            String entityName,
            FilterCriteria filterCriteria,
            SortCriteria sortCriteria,
            ProjectionCriteria projectionCriteria,
            int firstResult,
            int maxResults) throws CollageException {

        Collection<CriteriaAlias> filterAliases = (filterCriteria == null) ? null : filterCriteria.getCriteriaAliases();
        Collection<CriteriaAlias> sortAliases = (sortCriteria == null) ? null : sortCriteria.getCriteriaAliases();

        // First determine if we can do a simple query. If there are no
        // aliases then we can do a simple query
        // since there are no joins
        if ((filterAliases == null || filterAliases.size() == 0)
                && (sortAliases == null || sortAliases.size() == 0)) {
            return performSimpleQuery(entityName, filterCriteria,
                    sortCriteria, projectionCriteria, firstResult,
                    maxResults);
        }

        String[] entityParts = entityName.split("\\.");
        String entityDescription = entityParts[entityParts.length - 1];
        CollageTimer timer = startMetricsTimer("performQuery-" + entityDescription);

        try {
            ClassMetadata metadata = this.getSessionFactory().getClassMetadata(
                    entityName);
            String idPropertyName = metadata.getIdentifierPropertyName();
            if (idPropertyName == null) {
                log.warn("FoundationDAO does not currently support complex queries on entities with no identifier property - EntityName: "
                        + entityName);
                return null;
            }

            Session session = this.getSession();
            Criteria criteria = session.createCriteria(entityName);

            // Apply filterCriteria and sort criteria
            if (filterCriteria != null) {
                criteria = criteria.add(filterCriteria.getCriterion());

                // Add filter criteria property names to be used later to create
                // Criteria aliases
                updateAliases(criteria, filterAliases);
            }

            // Pagination Query - Because there may be a cartesian product
            // caused by joins
            // of one-to-many and the ResultTransformer does not occur until
            // after the query
            // is performed. Subsequently, returning incorrect data.
            // We must select distinct on the query entity's id in order to get
            // the proper "page" of results. With the ids, we then perform a
            // "select in"
            // to get the entity data.
            // So there is a total of 3 queries being performed:
            // 1) A query to get total count
            // 2) A query to get a page of entity ids
            // 3) A query to get the entity data restricted to the ids
            // TODO: This is a known issue and according to Hibernate JIRA
            // HB-520, it may
            // be addressed in the future with a Root Projection type
            // We may also want to investigate the ability to determine if a
            // cartesian product
            // will occur and if not then do a "simple" paging query.
            criteria.setProjection(Projections.countDistinct(idPropertyName));

            /*****************************************************************************
             * /* QUERY 1: Total Count - Take into account filter /
             *****************************************************************************/
            log.debug("In query 1....");
            Integer count = (Integer) criteria.uniqueResult();
            int totalCount = count.intValue();

            boolean bPaging = false;
            if (firstResult < 1)
                firstResult = 0;

            // Just return an empty result set if the first index is outside the
            // total count
            if (totalCount < 1 || (firstResult >= totalCount)) {
                return new FoundationQueryList(new ArrayList(0), 0);
            }
            // If total count - the first index is more then max results we
            // don't have to set max results
            else if (maxResults > 0
                    && (maxResults <= (totalCount - firstResult))) {
                criteria.setMaxResults(maxResults);
                bPaging = true;
            }

            // Apply first result if necessary
            if (firstResult > 0) {
                criteria.setFirstResult(firstResult);
                bPaging = true;
            }

            // clear total count projection and Get distinct ids - if we are
            // paging then sort
            // does have to be applied
            criteria.setProjection(null);
            // criteria.setProjection(Projections.distinct(Projections.property(idPropertyName)));
            // Apply sort if we are paging otherwise sort only needs to be
            // applied in Query 3
            // with the final result set
            // Initialize projection length here for later use
            int projLength = 0;
            if (bPaging == true && sortCriteria != null) {
                List<Order> sortList = sortCriteria.getSortList();
                Iterator<Order> it = sortList.iterator();
                while (it.hasNext()) {
                    criteria = criteria.addOrder(it.next());
                }

                // Since orderby field need to be in the select clause, we need
                // to add distinct to all projectionlist in the
                // sort criteria.
                ProjectionList projectList = sortCriteria.getProjectionList();
                projectList.add(Projections.property(idPropertyName));
                criteria.setProjection(Projections.distinct(projectList));
                projLength = projectList.getLength();

                if (sortAliases != null && sortAliases.size() > 0) {
                    // Only add aliases that have not been added by the filter
                    // criteria
                    if (filterAliases == null || filterAliases.size() == 0) {
                        updateAliases(criteria, sortAliases);
                    } else {
                        // Copy sort aliases
                        Collection<CriteriaAlias> aliases = new HashSet<CriteriaAlias>(
                                sortAliases.size());
                        aliases.addAll(sortAliases);

                        // remove filter aliases to avoid duplicates
                        aliases.removeAll(filterAliases);

                        // update aliases with the sort aliases that have not
                        // been added by the
                        // filter criteria
                        updateAliases(criteria, aliases);
                    }
                }
            } else {
                criteria.setProjection(Projections.property(idPropertyName));
                projLength = 1; // since I am setting only one projections here
            } // end if

            /*****************************************************************************
             * /* QUERY 2: Entity Id query taking into account paging and
             * filtering /
             *****************************************************************************/
            // Since additional projection is added to query 1, we need to
            // iterate and find the ids
            log.debug("In query 2....");
            List<Object> ids = new ArrayList<Object>();
            List rows = criteria.list();

            log.debug("In query 2 after casting...." + rows);
            log.debug("ProjectionList size=" + projLength);
            if (rows == null || rows.size() == 0) {
                return new FoundationQueryList(new ArrayList(0), 0);
            }
            for (Object obj : rows) {
                if (obj instanceof Object[]) {
                    Object[] objArr = (Object[]) obj;
                    ids.add(objArr[projLength - 1]);
                } else {
                    ids.add(obj);
                }
                log.debug("In query 2 object type...." + obj.getClass());
            }
            // If the ids list is empty, then return empty list
            if (ids != null && ids.size() == 0) {
                return new FoundationQueryList(new ArrayList(0), 0);
            }

            /*****************************************************************************
             * /* QUERY 3: Entity Query were ID is in id list from Query 2 Only
             * apply sort - paging and filter have been applied with Query 2 /
             *****************************************************************************/
            log.debug("In query 3....");
            // Reset criteria but just add sort - filter has been applied in the
            // id query
            criteria = session.createCriteria(entityName);

            // Apply Sort Criteria
            if (sortCriteria != null) {
                List<Order> sortList = sortCriteria.getSortList();
                Iterator<Order> it = sortList.iterator();
                while (it.hasNext()) {
                    criteria = criteria.addOrder(it.next());
                }

                if (sortAliases != null && sortAliases.size() > 0) {
                    // Apply Aliases based on property names in criteria
                    updateAliases(criteria, sortAliases);

                    // Set Root Transformer to be distinct for queries which
                    // return a Cartesian product.
                    criteria = criteria
                            .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
                }
            }
            log.debug("***************Before adding in query 3*************");
            // Now add the id's into the restriction
            criteria.add(Restrictions.in(idPropertyName, ids));
            log.debug("***************Before returning*************");
            return new FoundationQueryList(criteria.list(), totalCount);
        } catch (Exception e) {
            throw new CollageException(e);
        } finally {
            stopMetricsTimer(timer);
        }
    }

    /**
     * Return row count of specified filter criteria
     *
     * @param entityName
     * @param filterCriteria
     * @return
     * @throws CollageException
     */
    private int queryTotalCount(String entityName, FilterCriteria filterCriteria) throws CollageException {
        try {
            Criteria criteria = this.getSession().createCriteria(entityName);

            // Apply filterCriteria
            if (filterCriteria != null) {
                criteria = criteria.add(filterCriteria.getCriterion());

                // Apply Aliases based on property names in filter criteria
                updateAliases(criteria, filterCriteria.getCriteriaAliases());
            }

            ///////////////////////////////////////////////////////////////////////////
            // Note:  We are using the id property to get a distinct count.
            // This is necessary to get the proper count when joining a one to many relationship
            // Since a cartesian product of rows is returned using the rowcount projection
            // will yield an incorrect count of the root entity.
            // TODO:  This is a temporary workaround until a proper solution can
            // be implemented.
            ///////////////////////////////////////////////////////////////////////////

            ClassMetadata metadata = this.getSessionFactory().getClassMetadata(entityName);

            String idPropertyName = metadata.getIdentifierPropertyName();
            if (idPropertyName != null) {
                criteria.setProjection(Projections.countDistinct(idPropertyName));
            } else {
                criteria.setProjection(Projections.rowCount());
            }

            Integer count = (Integer) criteria.uniqueResult();

            return count.intValue();
        } catch (Exception e) {
            throw new CollageException(e);
        }
    }

    /**
     * Builds criterion for a collection of ids
     *
     * @param ids
     * @return
     */
    private Criterion buildIdCriterion(Collection<Integer> ids) {
        if (ids == null || ids.size() == 0) {
            throw new IllegalArgumentException("FoundationDAO.buildIdCriteria() - Invalid null id collection parameter.");
        }

        Criterion criterion = null;
        Criterion newCriterion = null;

        Iterator<Integer> it = ids.iterator();
        while (it.hasNext()) {
            newCriterion = Restrictions.idEq(it.next());

            if (criterion != null) {
                criterion = Restrictions.or(criterion, newCriterion);
            } else {
                criterion = newCriterion;
            }
        }

        return criterion;
    }

    /**
     * Builds criterion for a collection of ids
     *
     * @param ids
     * @return
     */
    private Criterion buildUUIDCriterion(Collection<UUID> ids) {
        if (ids == null || ids.size() == 0) {
            throw new IllegalArgumentException("FoundationDAO.buildIdCriteria() - Invalid null id collection parameter.");
        }

        Criterion criterion = null;
        Criterion newCriterion = null;

        Iterator<UUID> it = ids.iterator();
        while (it.hasNext()) {
            newCriterion = Restrictions.idEq(it.next());

            if (criterion != null) {
                criterion = Restrictions.or(criterion, newCriterion);
            } else {
                criterion = newCriterion;
            }
        }

        return criterion;
    }

    /**
     * Creates criteria aliases for the aliases identified.
     *
     * @param criteria
     * @param aliases
     */
    private void updateAliases(Criteria criteria, Collection<CriteriaAlias> aliases) {
        if (aliases == null || aliases.size() == 0)
            return;

        if (log.isDebugEnabled() == true)
            log.debug("updateAliases() - aliases  " + aliases);

        CriteriaAlias alias = null;
        Iterator<CriteriaAlias> it = aliases.iterator();
        while (it.hasNext()) {
            alias = it.next();

            // NOTE:  We are doing an left join based on association set up in mappings
            criteria.createAlias(alias.getAssociationPath(),
                    alias.getAlias(),
                    CriteriaSpecification.LEFT_JOIN);

            if (log.isDebugEnabled() == true)
                log.debug("Hibernate Criteria Alias created - Association: " + alias);
        }
    }

    public FoundationQueryList queryWithPaging(final String hqlQuery, final String countHql, final int firstResult,
                                               final int maxResults) throws CollageException {
        return queryWithPaging(hqlQuery, countHql, firstResult, maxResults, OBJECT_NOT_FOUND_RETRIES);
    }

    private FoundationQueryList queryWithPaging(final String hqlQuery, final String countHql, final int firstResult,
                                                final int maxResults, final int retry) throws CollageException
    {
        CollageTimer timer = startMetricsTimer("queryWithPaging");
        try {
            Session session = this.getSession();
            Query query = session.createQuery(hqlQuery);
            String[] aliases = query.getReturnAliases();
            long totalCount = -1;
            // Pagination
            if (firstResult > 0 || maxResults > 0) {
                Query countQuery = session.createQuery(countHql);
                totalCount = (Long) countQuery.list().get(0);
                int first = Math.max(firstResult, 0);
                int max = Math.max(maxResults, 0);
                // Just return an empty result set if total count is less than 1 OR
                // if the first index is outside the total count
                if (totalCount < 0 || first >= totalCount) {
                    return new FoundationQueryList(new ArrayList(0), 0);
                }
                // If total count - the first index is more then max results we don't have to set max results
                else if (max > 0 && (max <= (totalCount - first))) {
                    query.setMaxResults(max);
                }
                if (first > 0)
                    query.setFirstResult(first);
            }
            if ((aliases != null) && (aliases.length > 1)) {
                List<Object> result = new ArrayList();
                List initialResult = query.list();
                for (Object o : initialResult) {
                    Object[] oa = (Object[]) o;
                    result.add(oa[0]);
                }
                return new FoundationQueryList(result, (int) totalCount);
            }
            return new FoundationQueryList(query.list(), (int) totalCount);
        } catch (ObjectNotFoundException onfe) {
            if (retry > 0) {
                // retry on object not found exceptions: happens when
                // objects are deleted out from under queries
                return queryWithPaging(hqlQuery, countHql, firstResult, maxResults, retry-1);
            }
            throw onfe;
        } finally {
            stopMetricsTimer(timer);
        }
    }
}
