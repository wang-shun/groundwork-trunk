/*
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2007  GroundWork Open Source Solutions info@groundworkopensource.com

 *	 This program is free software; you can redistribute it and/or modify
 *	 it under the terms of version 2 of the GNU General Public License
 *	 as published by the Free Software Foundation.

 *	 This program is distributed in the hope that it will be useful,
 *	 but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	 GNU General Public License for more details.

 *	 You should have received a copy of the GNU General Public License
 *	 along with this program; if not, write to the Free Software
 *	 Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.groundwork.foundation.ws.impl;

import java.rmi.RemoteException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.ws.api.WSFoundationException;
import org.groundwork.foundation.ws.api.WSRRD;
import org.groundwork.foundation.ws.model.impl.WSFoundationCollection;
import org.groundwork.foundation.ws.model.impl.ExceptionType;

import com.groundwork.collage.CollageFactory;

public class RRDSoapBindingImpl implements WSRRD {
	protected static Log log = LogFactory.getLog(RRDSoapBindingImpl.class);

	public WSFoundationCollection getGraph(String hostName, String serviceName,
			long startDate, long endDate, String applicationType, int graphWidth)
			throws java.rmi.RemoteException, WSFoundationException {

		// get the WSStatistics api object.
		CollageFactory factory = CollageFactory.getInstance();
		WSRRD rrd = (WSRRD) factory.getAPIObject("WSRRD");

		// check the event object, if getting it failed, bail out now.
		if (rrd == null) {
			throw new WSFoundationException(
					"Unable to create WSStatistics instance",
					ExceptionType.SYSTEM);
		}
		// all is well, call our implementation.
		else {
			return rrd.getGraph(hostName, serviceName, startDate,
					endDate, applicationType,graphWidth);
		}
	}

}
