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
package org.groundwork.foundation.bs;

import com.groundwork.collage.CollageFactory;
import com.groundwork.collage.metrics.CollageMetrics;
import com.groundwork.collage.metrics.CollageTimer;
import org.groundwork.foundation.bs.exception.BusinessServiceException;
import org.groundwork.foundation.bs.foundationsession.FoundationSession;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

public class BusinessServiceImpl extends HibernateDaoSupport implements BusinessService 
{
	// Foundation Session
	private FoundationSession _session = null;	
	
	/*************************************************************************/
	/* Constructors */
	/*************************************************************************/	
	
	/**
	 * Default Constructor for Spring Factory
	 */
	public BusinessServiceImpl ()
	{	
		 super(); 
	}
	
	public BusinessServiceImpl (FoundationSession session)
	{
		super(); 
		setFoundationSession(session);
	}
	
	/*************************************************************************/
	/* Public Methods */
	/*************************************************************************/
	
	/* (non-Javadoc)
	 * @see org.groundwork.foundation.bs.BusinessService#initialize(java.util.Properties)
	 */
	public void initialize() throws BusinessServiceException
	{
	}

	/* (non-Javadoc)
	 * @see org.groundwork.foundation.bs.BusinessService#notify(org.groundwork.foundation.bs.ServiceNotify)
	 */
	public void notify(ServiceNotify notify) throws BusinessServiceException
	{
	}

	/* (non-Javadoc)
	 * @see org.groundwork.foundation.bs.BusinessService#uninitialize()
	 */
	public void uninitialize() throws BusinessServiceException
	{
	}
	
	public FoundationSession getFoundationSession ()
	{
		return _session;
	}
	
	public void setFoundationSession (FoundationSession session)
	{
		if (session == null)
			throw new IllegalArgumentException("Invalid null FoundationSession parameter.");

		_session = session;		
	}	
	
	/*************************************************************************/
	/* Protected Methods */
	/*************************************************************************/	
	protected boolean isInitialized ()
	{
		if (_session == null)
			return false;
		
		return true;
	}

	private CollageMetrics collageMetrics = null;

	private CollageMetrics getCollageMetrics() {
		if (collageMetrics == null) {
			collageMetrics = CollageFactory.getInstance().getCollageMetrics();
		}
		return collageMetrics;
	}

	public CollageTimer startMetricsTimer(String className, String methodName) {
		CollageMetrics collageMetrics = getCollageMetrics();
	    return (collageMetrics == null ? null : collageMetrics.startTimer(className, methodName));
	}

	public void stopMetricsTimer(CollageTimer timer) {
		CollageMetrics collageMetrics = getCollageMetrics();
		if (collageMetrics != null) getCollageMetrics().stopTimer(timer);
	}


}
