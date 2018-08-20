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
package org.groundwork.foundation.ws.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.NodeList;

import javax.xml.rpc.Call;
import javax.xml.soap.SOAPHeaderElement;

/**
 * 
 * GWSoapBindingStub- Reads the ws_client.properties file and sets the
 * credentials for java based soap clients. If file not found, it examines the 
 * soap headers for the authentication info
 * 
 * @author <a href="mailto:ashanmugam@gwos.com"> Roger Ruttimann </a>
 * @version $Id: GWSoapBindingStub.java 17937 2010-11-22 15:51:43Z ashanmugam $
 */
public class GWSoapBindingStub extends org.apache.axis.client.Stub {

	public static final String TAG_HEADER_AUTHENTICATION = "Authentication";

	public static final String TAG_HEADER_USER = "User";

	public static final String TAG_HEADER_SECRET = "Secret";
	
	public static final String HEADER_PREFIX = "gwos";

	/** Use log4j */
	protected Log log = LogFactory.getLog(this.getClass());

	protected org.apache.axis.client.Call createCall()
			throws java.rmi.RemoteException {
		String user = null;
		String password = null;
		org.apache.axis.client.Call _call = null;
		try {
			_call = super._createCall();
            user = WSClientConfiguration.getProperty(WSClientConfiguration.WEBSERVICES_USERNAME);
            password = JasyptUtils.jasyptDecrypt(WSClientConfiguration.getProperty(WSClientConfiguration.WEBSERVICES_PASSWORD));
		} catch (java.lang.Throwable _t) {
			// If ws_client file is not found then try to get it from Soap
			// headers
			try {
				SOAPHeaderElement[] elements = this.getHeaders();
				// Examine the headers
				for (SOAPHeaderElement header : elements) {
					if (header.getElementName().getLocalName()
							.equalsIgnoreCase(TAG_HEADER_AUTHENTICATION)) {
						NodeList children = header.getChildNodes();
						for (int index = 0; index < children.getLength(); index++) {
							SOAPHeaderElement child = (SOAPHeaderElement) children
									.item(index);
							if (child.getElementName().getLocalName()
									.equalsIgnoreCase(TAG_HEADER_USER)) {
								user = child.getValue();
							} // end if
							if (child.getElementName().getLocalName()
									.equalsIgnoreCase(TAG_HEADER_SECRET)) {
								password = child.getValue();
							} // end if
						} // end for
					} // end if
				} // end for

			} catch (Exception e) {
				log.error(e.getMessage());
			}

			// Finally if you cannot get in these 2 ways, then throw the error
			if (user == null && password == null) {
				log.error("No Authentication info supplied! Neither properties file "
						+ WSClientConfiguration.getFoundationPropertyFileLocation()
						+ " found nor Authentication headers found in the Soap call!");
				throw new org.apache.axis.AxisFault(
						"Failure trying to get the Call object", _t);
			} // end if
		}
		_call.setProperty(Call.USERNAME_PROPERTY, user);
		_call.setProperty(Call.PASSWORD_PROPERTY, password);
		return _call;
	}

}
