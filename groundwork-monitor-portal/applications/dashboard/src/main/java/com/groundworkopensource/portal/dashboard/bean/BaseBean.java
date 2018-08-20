/*
 *  Copyright (C) 2009 GroundWork Open Source, Inc. (GroundWork)
 *  All rights reserved. This program is free software; you can redistribute
 *  it and/or modify it under the terms of the GNU General Public License
 *  version 2 as published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package com.groundworkopensource.portal.dashboard.bean;

import java.io.Serializable;

import com.groundworkopensource.portal.common.ws.IWSFacade;
import com.groundworkopensource.portal.common.ws.WebServiceFactory;
import com.groundworkopensource.portal.common.ws.WebServiceFactory.WebServiceType;

/**
 * @author rashmi_tambe
 * 
 */
public class BaseBean implements Serializable {

    /**
     * Determines if a de-serialized file is compatible with this class.
     * 
     * Maintainers must change this value if and only if the new version of this
     * class is not compatible with old versions. See Sun docs for <a
     * href=http://java.sun.com/products/jdk/1.1/docs/guide
     * /serialization/spec/version.doc.html> details. </a>
     * 
     * Not necessary to include in first version of the class, but included here
     * as a reminder of its importance.
     */
    private static final long serialVersionUID = 7526472295622776147L;

    /**
     * foundationWSFacade
     */
    protected IWSFacade foundationWSFacade;

    /**
     * initialize IWSFacade instance.
     */
    public BaseBean() {
        // Instance of WebServiceFactory.
        WebServiceFactory webServiceFactory = new WebServiceFactory();
        // get Foundation WebService from factory.
        foundationWSFacade = webServiceFactory
                .getWebServiceInstance(WebServiceType.FOUNDATION_WEBSERVICE);

    }
}
