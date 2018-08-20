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
package org.groundwork.foundation.jms;

public interface JMSServerInfo {
	/**
	 * Setters/getters for ServerInfo structure
	 * @return
	 */
	String	getServerName();
	void	setServerName(String serverName);
		
	String	getUserId();
	void	setUserId(String userId);
	
	String	getPasswword();
	void	setPassword(String password);
	
	String	getPersistencePath();
	void	setPersistencePath(String persistencePath);
	
	int		getServerId();
	void 	setServerId(int serverId);
	
	String 	getContextFactory();	
	String 	getHost();
	String	getPort();
	
	/** Server Context (Server , port) to be used for JNDI binding*/
	String	getServerContext();
	void	setServerContext(String serverContext);
	
	/** Admin User name and password */
	String 	getAdminUser();
	void	setAdminUser(String adminUser);
	
	String	getAdminPassword();
	void	setAdminPassword(String AdminPasword);
	
	int		getAdminPort();
	void	setAdminPort(int adminPort);	
	
	String getJNDIFactoryURLPkgs();
}
