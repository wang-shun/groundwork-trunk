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

/**
 * JMSDestinationInfo
 * POJO used to pass configuration of a JMS to the different initialize functions
 * 
 * @author roger.ruttimann@groundworkopensource.com
 *
 * Created: Apr 9, 2007
 */
public interface JMSDestinationInfo 
{
	public static final String DEFAULT_JNDI_FACTORY_CLASS = "org.jboss.naming.remote.client.InitialContextFactory";
	public static final String DEFAULT_JNDI_HOST = "localhost";
	public static final String DEFAULT_JNDI_PORT = "4447";
	public static final String DEFAULT_JNDI_ADMIN_USER = "admin";
	public static final String DEFAULT_JNDI_ADMIN_CREDENTIALS = "groundwork";
	String	getDestinationName();
	
	//** Server Context for JNDI lookup*/
	String getServerContext();	
	String getContextFactory();
	String getHost();	
	String getPort();
	
	String getAdminUser();
	String getAdminCredentials();
}
