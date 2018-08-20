/*
 * 
 * Copyright 2007 GroundWork Open Source, Inc. ("GroundWork") All rights
 * reserved. This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package com.groundworkopensource.portal.common.ws;

import com.groundworkopensource.portal.common.ws.impl.FoundationWSFacade;

/**
 * Factory class for getting an instance of Web Service (currently - Foundation
 * Web Service)
 * 
 * @author swapnil_gujrathi
 */

public class WebServiceFactory {

	/**
	 * enum for defining Web Service type. Currently there is only "Foundation"
	 * web service. If one more gets added, please add it into this enum.
	 */
	public static enum WebServiceType {
		/**
		 * Foundation Web-service
		 */
		FOUNDATION_WEBSERVICE
	}

	/**
	 * Returns instance of WebService as per WebServiceType
	 * 
	 * @param webServiceType
	 * @return WebService Instance
	 */
	public IWSFacade getWebServiceInstance(final WebServiceType webServiceType) {
		// as currently only "foundation" web service is available, default is
		// also returning instance of foundation web service
		switch (webServiceType) {
		case FOUNDATION_WEBSERVICE:
			return new FoundationWSFacade();

		default:
			return new FoundationWSFacade();
		}
	}
}
