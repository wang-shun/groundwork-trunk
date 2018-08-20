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

import com.groundwork.collage.exception.CollageException;

import java.util.Collection;
import java.util.Date;

/** 
 * Wrapper class around a Hibernate Session that can be submitted
 * for a delayed (asynchronous execution)
 * 
 * @author rruttimann@groundworkopensource.com
 *
 */
public interface QueryObjectWrapper {
	
	/**
	 * State that the Query object can be in: idle,prepared,running or canceled
	 * @author rogerrut
	 *
	 */
	public enum QUERY_STATUS {idle,prepared,running,canceled}; 
	
	/**
	 * Returns the date of which the Query was started
	 * @return
	 */
	public Date getDateQuerySubmitted();
	
	/**
	 * Returns the date of which the Query was started
	 * @return
	 */
	public Date getDateQueryStarted();
	
	/**
	 * Returns current status of Query object
	 * @return QUERY_STATUS of embedded session 
	 */
	public QUERY_STATUS	getStatus();
	
	/**
	 * Change the Status of the Query Object
	 * @param status
	 */
	public void	setStatus(QUERY_STATUS status);

	/**
	 *  Cancel the Query associated with that object
	 * @return status as a string of cancel operation
	 */
	public String cancelQuery();
	
	/**
	 * Executes the Query associated with that object
	 * @return the result collection from the Hibernate call or Throws a Collage Exception
	 */
	public Collection executeQuery() throws CollageException;
	
	/**
	 * returns the Object Identification string of the DAO in which the Query was created.
	 * The ID will be used to lookup the DAO in the spring assembly and then convert WS objects 
	 * @return String 
	 */
	public String getDAOSpringID();
	
	public void setDAOSpringID(String daoID);

}
