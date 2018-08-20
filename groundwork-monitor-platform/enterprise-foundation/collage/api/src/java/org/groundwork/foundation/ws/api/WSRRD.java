/*
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2009  GroundWork Open Source Solutions info@groundworkopensource.com

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
package org.groundwork.foundation.ws.api;

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.groundwork.foundation.ws.model.impl.WSFoundationCollection;



/**
 * End point to get the RRD graph
 * @author arul
 *
 */
public interface WSRRD extends Remote {

	/**
	 * Gets the RRD graph for the supplied parameters
	 * @param hostName
	 * @param serviceName
	 * @param startDate
	 * @param endDate
	 * @param applicationType
	 * @return
	 * @throws WSFoundationException
	 * @throws RemoteException
	 */
	public WSFoundationCollection getGraph(String hostName, String serviceName,
			long startDate, long endDate, String applicationType, int graphWidth) throws WSFoundationException,
			RemoteException;

}
