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

import org.groundwork.foundation.ws.model.impl.RRDGraph;

import com.groundworkopensource.portal.common.exception.WSDataUnavailableException;

/**
 * This class provides methods to interact with "WSRRD" foundation web service.
 * 
 * @author manish_kjain
 * 
 */
public interface IWSRRDFacade {

    /**
     * return RRDGraph object array.
     * 
     * @param hostName
     * @param serviceName
     * @param startTime
     * @param endTime
     * @param applicationType
     * @param width
     * @return RRDGraph[]
     * @throws WSDataUnavailableException
     */
    RRDGraph[] getRrdGraph(String hostName, String serviceName, long startTime,
            long endTime, String applicationType, int width)
            throws WSDataUnavailableException;

}
