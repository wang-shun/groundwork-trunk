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

import java.util.Properties;

import org.groundwork.foundation.bs.exception.BusinessServiceException;
import org.groundwork.foundation.bs.foundationsession.FoundationSession;
import org.hibernate.SessionFactory;

public interface BusinessService
{
	public void initialize () throws BusinessServiceException;
	
	public void uninitialize () throws BusinessServiceException;
	
	public void notify(ServiceNotify notify) throws BusinessServiceException;
	
	public FoundationSession getFoundationSession ();
	
	public void setFoundationSession (FoundationSession session);
	
	public void setSessionFactory (SessionFactory factory);
}
