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

import org.groundwork.foundation.jms.JMSServerInfo;

/**
 * @author rogerrut
 *
 * Created: Apr 5, 2007
 */
public class JMSServerInfoImpl implements JMSServerInfo 
{
	private int		serverId;
	private String	persistencePath;
	
	private String password;
	private String server;
	private String userId;
	
	private	String serverContext;
	
	/* Admin info to connect to JMS server */
	private String	adminUser;
	private String	adminPassword;
	private int		adminPort;
	
	/* JNDI configuration */
	private String contextFactory = null;
	private String host = null;
	private String port = null;
	
	 private String jndiFactoryURLPkgs = null;

	public String getJNDIFactoryURLPkgs() {
		return jndiFactoryURLPkgs;
	}

	/**
	 * Constructor
	 * @param jndiContextFactory
	 * @param host
	 * @param port
	 * @param serverName
	 * @param serverContext
	 * @param serverId
	 * @param persistencePath
	 * @param adminUser
	 * @param adminPassword
	 * @param adminPort
	 */
	public JMSServerInfoImpl(String jndiContextFactory, 
			 				 String jndiHost, 
			 				 String jndiPort,
			 				 String serverName, 
							 String serverContext, 
							 int serverId, 
							 String persistencePath, 
							 String adminUser, 
							 String adminPassword, 
							 int adminPort,
							 String jndiFactoryURLPkgs)
	{
		if (jndiContextFactory == null || jndiContextFactory.length() == 0)
			throw new IllegalArgumentException("Invalid null / empty jndiContextFactory parameter.");

		if (jndiHost == null || jndiHost.length() == 0)
			throw new IllegalArgumentException("Invalid null / empty jndiHost parameter.");
		
		if (jndiPort == null || jndiPort.length() == 0)
			throw new IllegalArgumentException("Invalid null / empty jndiPort parameter.");
		
		if (serverName == null || serverName.length() == 0)
			throw new IllegalArgumentException("Invalid null / empty serverName parameter.");
		
		if (serverContext == null || serverContext.length() == 0)
			throw new IllegalArgumentException("Invalid null / empty serverContext parameter.");
				
		if (persistencePath == null || persistencePath.length() == 0)
			throw new IllegalArgumentException("Invalid null / empty persistencePath parameter.");
		
		if (adminUser == null || adminUser.length() == 0)
			throw new IllegalArgumentException("Invalid null / empty adminUser parameter.");		
		
		this.contextFactory 	= jndiContextFactory;
		this.host				= jndiHost; 
		this.port 				= jndiPort;	
		this.server				= serverName;
		this.serverContext		= serverContext;
		this.serverId			= serverId;
		this.persistencePath	= persistencePath;
		this.adminUser			= adminUser;
		this.adminPassword		= adminPassword;
		this.adminPort			= adminPort;	
		this.jndiFactoryURLPkgs = jndiFactoryURLPkgs;
	}
	
	/* (non-Javadoc)
	 * @see org.groundwork.foundation.jms.JMSServerInfo#getPasswword()
	 */
	public String getPasswword() {
		return this.password;
	}

	/* (non-Javadoc)
	 * @see org.groundwork.foundation.jms.JMSServerInfo#getServerName()
	 */
	public String getServerName() {
		return this.server;
	}

	/* (non-Javadoc)
	 * @see org.groundwork.foundation.jms.JMSServerInfo#getUserID()
	 */
	public String getUserId() {
		return this.userId;
	}

	/* (non-Javadoc)
	 * @see org.groundwork.foundation.jms.JMSServerInfo#setPassword(java.lang.String)
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/* (non-Javadoc)
	 * @see org.groundwork.foundation.jms.JMSServerInfo#setServerName(java.lang.String)
	 */
	public void setServerName(String serverName) {
		this.server = serverName;
	}

	/* (non-Javadoc)
	 * @see org.groundwork.foundation.jms.JMSServerInfo#setUserID(java.lang.String)
	 */
	public void setUserId(String userID) {
		this.userId= userID;
	}

	/**
	 * @return the persstencePath
	 */
	public String getPersistencePath() {
		return persistencePath;
	}

	/**
	 * @param persstencePath the persstencePath to set
	 */
	public void setPersistencePath(String persistencePath) {
		this.persistencePath = persistencePath;
	}

	/**
	 * @return the serverId
	 */
	public int getServerId() {
		return serverId;
	}

	/**
	 * @param serverId the serverId to set
	 */
	public void setServerId(int serverId) {
		this.serverId = serverId;
	}

	/**
	 * @return the serverContext
	 */
	public String getServerContext() {
		return serverContext;
	}

	/**
	 * @param serverContext the serverContext to set
	 */
	public void setServerContext(String serverContext) {
		this.serverContext = serverContext;
	}

	/**
	 * @return the adminPassword
	 */
	public String getAdminPassword() {
		return adminPassword;
	}

	/**
	 * @param adminPassword the adminPassword to set
	 */
	public void setAdminPassword(String adminPassword) {
		this.adminPassword = adminPassword;
	}

	/**
	 * @return the adminPort
	 */
	public int getAdminPort() {
		return adminPort;
	}

	/**
	 * @param adminPort the adminPort to set
	 */
	public void setAdminPort(int adminPort) {
		this.adminPort = adminPort;
	}

	/**
	 * @return the adminUser
	 */
	public String getAdminUser() {
		return adminUser;
	}

	/**
	 * @param adminUser the adminUser to set
	 */
	public void setAdminUser(String adminUser) {
		this.adminUser = adminUser;
	}

	public String getContextFactory()
	{
		return contextFactory;
	}

	public String getHost()
	{
		return host;
	}
	
	/* (non-Javadoc)
	 * @see org.groundwork.foundation.jms.JMSServerInfo#getPort()
	 */
	public String getPort() {
		return this.port;
	}
	
}
