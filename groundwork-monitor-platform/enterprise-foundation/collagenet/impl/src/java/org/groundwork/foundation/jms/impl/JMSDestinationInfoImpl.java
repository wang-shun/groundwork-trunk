/*
* Collage - The ultimate data integration framework.
*
* Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")  
* All rights reserved. This program is free software; you can redistribute it
* and/or modify it under the terms of the GNU General Public License version 2
* as published by the Free Software Foundation.
*
* This program is distributed in the hope that it will be useful, but WITHOUT
* ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
* FOR A PARTICULAR PURPOSE.  See the GNU General Public License for 
* more details.
*
* You should have received a copy of the GNU General Public License along with
* this program; if not, write to the Free Software Foundation, Inc., 51 Franklin
* Street, Fifth Floor, Boston, MA 02110-1301, USA.
*
*/
/*
* Collage - The ultimate data integration framework.
*
* Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")  
* All rights reserved. This program is free software; you can redistribute it
* and/or modify it under the terms of the GNU General Public License version 2
* as published by the Free Software Foundation.
*
* This program is distributed in the hope that it will be useful, but WITHOUT
* ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
* FOR A PARTICULAR PURPOSE.  See the GNU General Public License for 
* more details.
*
* You should have received a copy of the GNU General Public License along with
* this program; if not, write to the Free Software Foundation, Inc., 51 Franklin
* Street, Fifth Floor, Boston, MA 02110-1301, USA.
*
*/

package org.groundwork.foundation.jms.impl;

import org.groundwork.foundation.jms.JMSDestinationInfo;

/**
 * @author rogerrut
 *
 * Created: Apr 9, 2007
 */
public class JMSDestinationInfoImpl implements JMSDestinationInfo 
{	
	/* Class attributes */
	private String contextFactory = null;
	private String host = null;
	private String port = null;
	private String destinationName = null;
	private	String serverContext = null;
	private String adminUser = null;
	private String adminCredentials = null;
	
	/**
	 * No default constructor
	 * @param serverContext
	 * @param queueName
	 */
	public JMSDestinationInfoImpl(
			String jndiContextFactory, 
			String jndiHost, 
			String jndiPort,
			String serverContext, 
			String destinationName,String adminUser, String adminCredentials)
	{
		if (jndiContextFactory == null || jndiContextFactory.length() == 0)
			throw new IllegalArgumentException("Invalid null / empty jndiContextFactory parameter.");

		if (jndiHost == null || jndiHost.length() == 0)
			throw new IllegalArgumentException("Invalid null / empty jndiHost parameter.");
		
		if (jndiPort == null || jndiPort.length() == 0)
			throw new IllegalArgumentException("Invalid null / empty jndiPort parameter.");
		
		if (serverContext == null || serverContext.length() == 0)
			throw new IllegalArgumentException("Invalid null / empty serverContext parameter.");
		
		if (destinationName == null || destinationName.length() == 0)
			throw new IllegalArgumentException("Invalid null / empty destinationName parameter.");
		
		this.contextFactory = jndiContextFactory;
		this.host = jndiHost;
		this.port = jndiPort;
		this.serverContext = serverContext;
		this.destinationName = destinationName;
		this.adminUser = adminUser;
		this.adminCredentials = adminCredentials;
	}
	
	/* (non-Javadoc)
	 * @see org.groundwork.foundation.jms.JMSQueueInfo#getQueueName()
	 */
	public String getDestinationName() {
		return this.destinationName;
	}

	/* (non-Javadoc)
	 * @see org.groundwork.foundation.jms.JMSQueueInfo#getServerContext()
	 */
	public String getServerContext() {
		return this.serverContext;
	}

	public String getContextFactory()
	{
		return contextFactory;
	}

	public String getHost()
	{
		return host;
	}

	public String getPort()
	{
		return port;
	}
	
	public String getAdminUser() {
		return adminUser;
	}
	
	public String getAdminCredentials() {
		return adminCredentials;
	}
}
