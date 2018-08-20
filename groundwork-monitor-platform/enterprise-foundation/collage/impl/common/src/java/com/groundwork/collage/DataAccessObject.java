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

package com.groundwork.collage;

import java.io.Serializable;
import java.util.Collection;

import com.groundwork.collage.exception.CollageException;

/**
 * 
 * 
 * @author <a href="mailto:philippe.paravicini@eCommerceStudio.com>Philippe Paravicini</a>
 */
public interface DataAccessObject
{
    /** Public Enumeration Defining available conversions from hibernate data objects **/
    public enum DAOConvertType { WebService };
    
	/**
	 * Updates a persistent object into the database, optionally causing the
	 * persistence layer to flush all pending SQL statements, and/or evicting the
	 * updated object from cache, after the object is updated.
	 * <p>
	 * This method signature mirrors closely the operation of Hibernate and may
	 * not be the best choice for a general DAO interface; it may have to be
	 * revisited if we ever decide on a different persistence layer
	 * implementation.
	 * </p>
	 * 
	 * @param persistentObject 
	 *	 the object instance to be updated in the database
	 *
	 * @param doFlush 
	 *	 flush the underlying session and cause all pending SQL statements to be
	 *	 executed, after the object is updated
	 *
	 * @param doEvict
	 *	 evict this instance from the underlying Session cache to force a re-read
	 *	 from the database the next time that the object is retrieved
	 */
	public void update(Object persistentObject, boolean doFlush, boolean doEvict)
			throws CollageException;


	/**
	 * Updates an object into the database; makes no guarantees as to whether
	 * this may happen immediately, and as to whether any caches will be updated
	 */
	public void update(Object persistentObject) throws CollageException;


	/** 
	 * saves a newly created object into the database 
	 *
	 * @return the object after it was persisted
	 */
	public Serializable save(Object persistentObject) throws CollageException;


	/**
	 * saves or updates an object into the database, where the object may or
	 * may not be a an object that has been previously persisted;
	 * optionally causing the persistence layer to flush all pending SQL
	 * statements, and/or evicting the updated object from cache, after the
	 * object is updated.
	 * <p>
	 * This method signature mirrors closely the operation of Hibernate and may
	 * not be the best choice for a general DAO interface; it may have to be
	 * revisited if we ever decide on a different persistence layer
	 * implementation.
	 * </p>
	 * 
	 * @param persistentObject 
	 *	 the object instance to be updated in the database
	 *
	 * @param doFlush 
	 *	 flush the underlying session and cause all pending SQL statements to be
	 *	 executed, after the object is updated
	 *
	 * @param doEvict
	 *	 evict this instance from the underlying Session cache to force a re-read
	 *	 from the database the next time that the object is retrieved
	 */
	public void saveOrUpdate(Object persistentObject, boolean doFlush, boolean doEvict)
			throws CollageException;


	/** 
	 * saves or updates an object into the database, where the object may or
	 * may not be a an object that has been previously persisted
	 */
	public void saveOrUpdate(Object persistentObject) throws CollageException;


	/**
	 * Deletes a persistent object from the database, optionally causing the
	 * persistence layer to flush all pending SQL statements, after the object is
	 * deleted
	 * 
	 * @param persistentObject 
	 *	 the object instance to be updated in the database
	 *
	 * @param doFlush 
	 *	 flush the underlying session and cause all pending SQL statements to be
	 *	 executed, after the object is deleted
	 */
	public void delete(Object persistentObject, boolean doFlush) throws CollageException;

	/**
	 * Deletes an object from the database; makes no guarantees as to whether
	 * this may happen immediately.
	 */
	public void delete(Object persistentObject) throws CollageException;


	/**
	 * Evicts a persistent object from a cache in which it may have been placed
	 * by the persistence layer.
	 * <p>
	 * This method signature mirrors closely the operation of Hibernate and may
	 * not be the best choice for a general DAO interface; it may have to be
	 * revisited if we ever decide on a different persistence layer
	 * implementation.
	 * </p>
	 */
	public void evict(final Object persistentObject) throws CollageException;


	/** Execute any database operations that may be pending */
	public void flush() throws CollageException;
    
    /**
     * Each DAO must implement conversion between hibernate and data object classes.  
     * For now, we only provide conversion to web service data objects.
     * @param convertType
     * @param hibernateCollection
     * @return
     */    
    public Object[] convert (DAOConvertType convertType, Collection hibernateCollection);
    
    /**
     * Each DAO must implement conversion between hibernate and data object classes. 
     * The deep flag is added to determine whether a deep retrievel should occur (e.g. host groups => Hosts) 
     * For now, we only provide conversion to web service data objects.
     * @param convertType
     * @param hibernateCollection
     * @param deep
     * @return
     */    
    public Object[] convert (DAOConvertType convertType, Collection hibernateCollection, boolean deep);
}
