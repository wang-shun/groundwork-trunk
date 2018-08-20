/**
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2007  GroundWork Open Source Solutions info@itgroundwork.com

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

import java.util.Collection;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.springframework.dao.DataAccessException;

import com.groundwork.collage.QueryObjectWrapper;
import com.groundwork.collage.exception.CollageException;

/**
 * Implementation of QueryObjectWrapper class
 * @author rruttimann@groundwworkopensource.com
 *
 */
public class QueryObjectWrapperImpl implements QueryObjectWrapper {
	
	// Private members
	private Session hibernateSession = null;
	private QUERY_STATUS currentState = QUERY_STATUS.idle;
	
	/** This variable is used to identify the DAO that created the query */
	private String daoId = "";
	
	/** Date when Qurey was submitted*/
	private Date dateQuerySubmitted = null; 
	
	/** Date when Query was started*/
	private Date dateQueryStarted = null;
	
	// Text message
	static final String QUERY_MSG	= "Cancel Quey ";
	static final String QUERY_STOPPED	= "success. Query was stopped.";
	static final String QUERY_CANCEL_FAIL	= "failed to cancel with an error: ";
	static final String QUERY_NOT_RUNNING	= "not possible since Query is not running.";
	static final String QUERY_NOT_EXIST	= "Query object for the given ID does not exist.";
	
	Log log = LogFactory.getLog(this.getClass());
	
	// Constructor
	public QueryObjectWrapperImpl()
	{
		this.currentState = QUERY_STATUS.idle;
		this.dateQuerySubmitted = new Date(System.currentTimeMillis());
	}
	
	public QueryObjectWrapperImpl(Session obj)
	{
		this.hibernateSession = obj;
		this.currentState = QUERY_STATUS.idle;
		this.dateQuerySubmitted = new Date(System.currentTimeMillis());
	}
	
	/* (non-Javadoc)
	 * @see com.groundwork.collage.QueryObjectWrapper#cancelQuery()
	 */
	public String cancelQuery() {
		StringBuilder msg = new StringBuilder(QUERY_MSG);
		
		if (currentState == QUERY_STATUS.running)
		{
			try
			{
				this.hibernateSession.cancelQuery();
				this.currentState = QUERY_STATUS.canceled;
				
				msg.append(QUERY_STOPPED);
				
			}
			catch(Exception e)
			{
				// Failed to cancel quey
				msg.append(QUERY_CANCEL_FAIL).append(e).append(" ");
			}
			
		}
		else
		{
			msg.append(QUERY_NOT_RUNNING);
		}
		
		return msg.toString();
	}

	/* (non-Javadoc)
	 * @see com.groundwork.collage.QueryObjectWrapper#executeQuery()
	 */
	public Collection executeQuery() throws CollageException {
		
		StringBuilder msg = null;
		
		// Execute the Hibaernate Query
		try
		{
			this.currentState = QUERY_STATUS.running;
			this.dateQueryStarted = new Date(System.currentTimeMillis());
			return this.executeQuery();
			
		}
		catch (DataAccessException de)
		{
			msg = new StringBuilder("Failed to execute Query. Exception generated: ");
			msg.append(de);
			
			log.error(msg,de);
			throw new CollageException(msg.toString(), de);
			
		}
	}

	/* (non-Javadoc)
	 * @see com.groundwork.collage.QueryObjectWrapper#getDateQueryStarted()
	 */
	public Date getDateQueryStarted() {
		return this.dateQueryStarted;
	}

	/* (non-Javadoc)
	 * @see com.groundwork.collage.QueryObjectWrapper#getDateQuerySubmitted()
	 */
	public Date getDateQuerySubmitted() {
		return this.dateQuerySubmitted;
	}

	/* (non-Javadoc)
	 * @see com.groundwork.collage.QueryObjectWrapper#getSessionObject()
	 */
	public Session getSessionObject() {
		return this.hibernateSession;
	}
	
	/* (non-Javadoc)
	 * @see com.groundwork.collage.QueryObjectWrapper#setSessionObject()
	 */
	public void setSessionObject(Session hibernateSession)
	{
		this.hibernateSession = hibernateSession;
	}

	/* (non-Javadoc)
	 * @see com.groundwork.collage.QueryObjectWrapper#getStatus()
	 */
	public QUERY_STATUS getStatus() {
		return this.currentState;
	}
	
	public void setStatus(QUERY_STATUS status)
	{
		this.currentState = status;
	}
	
	public String getDAOSpringID()
	{
		return this.daoId;
	}
	
	public void setDAOSpringID(String daoID)
	{
		this.daoId = daoID;
	}
}
