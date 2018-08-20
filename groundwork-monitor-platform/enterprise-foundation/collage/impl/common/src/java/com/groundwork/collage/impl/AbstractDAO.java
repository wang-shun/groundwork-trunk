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

package com.groundwork.collage.impl;

import java.io.Serializable;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import com.groundwork.collage.CollageAccessor;
import com.groundwork.collage.CollageFactory;
import com.groundwork.collage.DataAccessObject;
import com.groundwork.collage.exception.CollageException;

/**
 * Base class for all Data Access classes implemented using Hibernate and
 * String declarative transactions.
 * 
 * @author	<a href="mailto:philippe.paravicini@eCommerceStudio.com">Philippe Paravicini</a>
 * @version cvs: $Id: AbstractDAO.java 8692 2007-10-15 20:49:04Z glee $
 *
 */
public abstract class AbstractDAO extends HibernateDaoSupport implements DataAccessObject 
{
	// Common String constants
	protected final static String SINGLE_QUOTE = "'";
	protected final static String PERCENT = "%";
	protected final static String EMPTY_STRING = "";
	protected final static String APP_TYPE_NAGIOS = "NAGIOS";
	protected final static String ALL = "ALL";
	
	protected Log log = LogFactory.getLog(this.getClass());

	private CollageAccessor collage;
    
	/** returns a factory for Collage model entities */
	public CollageAccessor getCollage()
	{
		return CollageFactory.getInstance();
	}	

	/** plain vanilla constructor, merely calls parent constructor */
	public AbstractDAO() { super(); }

	/*
	 * Updates a persistent object into the database, optionally causing the
	 * persistence layer to flush all pending SQL statements, and/or evicting the
	 * updated object from cache, after the object is updated.
	 * 
	 * This method signature mirrors closely the operation of Hibernate and may
	 * not be the best choice for a general DAO interface; it may have to be
	 * revisited if we ever decide on a different persistence layer
	 * implementation.
	 * 
	 * 
	 * @param persistentObject 
	 *   the object instance to be updated in the database
	 *
	 * @param doFlush 
	 *   flush the underlying session and cause all pending SQL statements to be
	 *   executed
	 *
	 * @param doEvict
	 *   evict this instance from the underlying Session cache to force a re-read
	 *   from the database
	 */
	public void update(Object persistentObject, boolean doFlush, boolean doEvict) 
		throws CollageException 
	{
		if (log.isDebugEnabled()) log.debug("attempting to update " + persistentObject);
		try { 
			getHibernateTemplate().update(persistentObject);
			if (doFlush) this.flush();
			if (doEvict) this.evict(persistentObject);
		}
		catch (DataAccessException e) {
			String msg = "Unable to update object '" + persistentObject + "' - flush: " + doFlush + " - evict: " + doEvict;
			log.error(msg, e);
			throw new CollageException(e);
		}
		if (log.isInfoEnabled()) log.info("successfully updated " + persistentObject + "' - flush: " + doFlush + " - evict: " + doEvict);
	}


	/*
	 * Updates an object into the database; makes no guarantees as to whether
	 * this may happen immediately, and as to whether any caches will be updated
	 */
	public void update(Object persistentObject) throws CollageException {
		this.update(persistentObject, false, false);
	}


	/* 
	 * saves a newly created object into the database 
	 *
	 * @return the object after it was persisted
	 */
	public Serializable save(Object persistentObject) throws CollageException {
		if (log.isDebugEnabled()) log.debug("attempting to save " + persistentObject);
		Serializable o = null;
		try { 
			o = getHibernateTemplate().save(persistentObject);
		}
		catch (DataAccessException e) {
			String msg = "Unable to save object '" + persistentObject;
			log.error(msg, e);
			throw new CollageException(e);
		}
		if (log.isInfoEnabled()) log.info("successfully saved " + persistentObject);
		return o;
	}


	/* 
	 * saves or updates an object into the database, which may be a newly
	 * created object 
	 */
	public void saveOrUpdate(Object persistentObject, boolean doFlush, boolean doEvict) throws CollageException {
		if (log.isDebugEnabled()) log.debug("attempting to save or update " + persistentObject + " - flush: " + doFlush + " evict: " + doEvict);
		Serializable o = null;
		try { 
			getHibernateTemplate().saveOrUpdate(persistentObject);
			if (doFlush) this.flush();
			if (doEvict) this.evict(persistentObject);
		}
		catch (DataAccessException e) {
			String msg = "Unable to saveOrUpdate object '" + persistentObject + "' - flush: " + doFlush + " - evict: " + doEvict;
			log.error(msg, e);
			throw new CollageException(e);
		}
		if (log.isInfoEnabled()) log.info("successfully saved or updated " + persistentObject + "' - flush: " + doFlush + " - evict: " + doEvict);
	}


	/* 
	 * saves or updates an object into the database, which may be a newly
	 * created object 
	 */
	public void saveOrUpdate(Object persistentObject) throws CollageException {
		this.saveOrUpdate(persistentObject, false, false);
	}

	/*
	 * Deletes a persistent object from the database, optionally causing the
	 * persistence layer to flush all pending SQL statements after the object is
	 * deleted
	 * 
	 * @param persistentObject 
	 *   the object instance to be updated in the database
	 *
	 * @param doFlush 
	 *   flush the underlying session and cause all pending SQL statements to be
	 *   executed after the object is deleted
	 */
	public void delete(Object persistentObject, boolean doFlush) throws CollageException 
	{
		if (log.isDebugEnabled()) log.debug("attempting to delete " + persistentObject + " - doFlush: " + doFlush);
		try { 
			getHibernateTemplate().delete(persistentObject);
			if (doFlush) this.flush();
		}
		catch (DataAccessException e) {
			String msg = "Unable to delete object '" + persistentObject + "' - doFlush: " + doFlush;
			log.error(msg, e);
			throw new CollageException(e);
		}
		if (log.isInfoEnabled()) log.info("successfully deleted " + persistentObject + " - doFlush: " + doFlush);
	}


	/*
	 * Deletes an object from the database; makes no guarantees as to whether
	 * this may happen immediately.
	 */
	public void delete(Object persistentObject) throws CollageException {
	  this.delete(persistentObject, false);
	}


	/*
	 * Evicts a persistent object from a cache in which it may have been placed
	 * by the persistence layer.
	 * 
	 * This method signature mirrors closely the operation of Hibernate and may
	 * not be the best choice for a general DAO interface; it may have to be
	 * revisited if we ever decide on a different persistence layer
	 * implementation.
	 * 
	 */
	public void evict(final Object persistentObject) throws CollageException 
	{
		if (log.isDebugEnabled()) log.debug("attempting to evict " + persistentObject + "...");
		try { 
			getHibernateTemplate().evict(persistentObject);
		}
		catch (DataAccessException e) {
			String msg = "Unable to evict object '" + persistentObject;
			log.error(msg, e);
			throw new CollageException(e);
		}
		if (log.isDebugEnabled()) log.debug("successfully evicted " + persistentObject);
	}


	/* Execute any database operations that may be pending */
	public void flush() throws CollageException 
	{
		if (log.isDebugEnabled()) log.debug("attempting to flush hibernate session...");
		try { 
			getHibernateTemplate().flush();
		}
		catch (DataAccessException e) {
			String msg = "Unable to flush hibernate session";
			log.error(msg, e);
			throw new CollageException(e);
		}
		if (log.isDebugEnabled()) log.debug("successfully flushed hibernate session");
	}


	/** 
	 * converts the array provided into a string that can be passed to the 'IN'
	 * clause of a SQL statement matching against a text column; that is, a
	 * comma-separated list of strings within single quotes, enclosed in
	 * parentheses, for example:<br/> ('a','b','c'); 
     * returns an empty set of parentheses "()" if the array is
	 * null or empty
	 */
	public static String arrayToInClauseForTextColumn(String[] value)
	{
		final char openParen = '(';
		final char closeParen = ')';
		
		StringBuilder out = new StringBuilder();
		out.append(openParen);

		if (value != null) 
		{
			int length = value.length;
			int secondFromEnd = length - 1;
			
			char singleQuote = '\'';
			char comma = ',';
			
			for (int i=0; i < length ; i++)
			{
				out.append(singleQuote);
				out.append(value[i]);
				out.append(singleQuote);
				
				if ( i < secondFromEnd) out.append(comma);
			}
		}
		
		out.append(closeParen);
		
		return out.toString();
	}
    
    /**
     * Each DAO must implement conversion between hibernate and data object classes.  
     * For now, we only provide conversion to web service data objects.
     * @param convertType
     * @param hibernateCollection
     * @return
     */
    public final Object[] convert (DAOConvertType convertType, Collection hibernateCollection)
    {
        if (hibernateCollection == null || hibernateCollection.isEmpty() == true)
        {
            return null;
        }
        
        switch (convertType)
        {
            case WebService:
                return convertToWebServiceObjects(hibernateCollection);
            default:
                return null;
        }
    }
    
    /**
     * Each DAO must implement conversion between hibernate and data object classes.  
     * For now, we only provide conversion to web service data objects.
     * 
     * The deep flag is added to determine whether a deep retrievel should occur (e.g. host groups => Hosts)
     * @param convertType
     * @param hibernateCollection
     * @param deep
     * @return
     */
    public final Object[] convert (DAOConvertType convertType, Collection hibernateCollection, boolean deep)
    {
        if (hibernateCollection == null || hibernateCollection.isEmpty() == true)
        {
            return null;
        }
        
        switch (convertType)
        {
            case WebService:
                return convertToWebServiceObjects(hibernateCollection, deep);
            default:
                return null;
        }
    }    
    
    /**
     * DAO classes should implement this method to convert hibernate object into
     * web service objects.
     * 
     * Note:  If a new conversion is need we should update the convert () and add
     * a new abstract method to do the conversion.
     * 
     * @param hibernateCollection
     * @return
     */
    protected Object[] convertToWebServiceObjects(Collection hibernateCollection)
    {
        throw new UnsupportedOperationException("Method Not Implemented.");
    }
    
    /**
     * DAO classes should implement this method to convert hibernate object into
     * web service objects.
     * 
     * Note:  If a new conversion is need we should update the convert () and add
     * a new abstract method to do the conversion.
     * 
     * @param hibernateCollection
     * @param deep
     * @return
     */
    protected Object[] convertToWebServiceObjects(Collection hibernateCollection, boolean deep)
    {
        throw new UnsupportedOperationException("Method Not Implemented.");
    }    
}
