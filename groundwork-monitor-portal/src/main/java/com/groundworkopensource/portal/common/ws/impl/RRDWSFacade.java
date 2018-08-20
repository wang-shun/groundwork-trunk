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

package com.groundworkopensource.portal.common.ws.impl;

import java.rmi.RemoteException;

import javax.xml.rpc.ServiceException;

import org.apache.log4j.Logger;
import org.groundwork.foundation.ws.api.WSFoundationException;
import org.groundwork.foundation.ws.api.WSRRD;
import org.groundwork.foundation.ws.model.impl.RRDGraph;
import org.groundwork.foundation.ws.model.impl.WSFoundationCollection;

import com.groundworkopensource.portal.common.CommonConstants;
import com.groundworkopensource.portal.common.exception.WSDataUnavailableException;
import com.groundworkopensource.portal.common.ws.IWSRRDFacade;

/**
 * @author manish_kjain
 * 
 */
public class RRDWSFacade implements IWSRRDFacade {

    /**
     * logger
     */
    private static final Logger LOGGER = FoundationWSFacade.getLogger();

    /**
     * return RRDGraph object array.
     * 
     * @param hostName
     * @param serviceName
     * @param startTime
     * @param endTime
     * @param applicationType
     * @param graphWidth
     * @return RRDGraph[]
     * @throws WSDataUnavailableException
     */
    public RRDGraph[] getRrdGraph(String hostName, String serviceName,
            long startTime, long endTime, String applicationType, int graphWidth)
            throws WSDataUnavailableException {
        RRDGraph[] rrdGraphs = new RRDGraph[] {};
        WSRRD rrdBinding = null;
        try {
            rrdBinding = WebServiceLocator.getInstance().rrdServiceLocator()
                    .getrrd();
        } catch (ServiceException sEx) {
            LOGGER
                    .fatal("ServiceException while getting binding object for \"WSRRD\" web service"
                            + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + sEx);
            return rrdGraphs;
        }
        if (rrdBinding == null) {
            LOGGER.error("rrdBinding is null in getRrdGraph() method ");
            return rrdGraphs;
        }
        try {
            WSFoundationCollection rrdCol = rrdBinding.getGraph(hostName,
                    serviceName, startTime, endTime, applicationType,
                    graphWidth);
            if (rrdCol == null) {
                // error occurred. throw exception
                throw new WSDataUnavailableException();
            }
            rrdGraphs = rrdCol.getRrdGraph();

        } catch (WSFoundationException fEx) {

            LOGGER.error("WSFoundationException  in getRrdGraph method:-"
                    + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + fEx);
            // error occurred. throw exception
            throw new WSDataUnavailableException();
        } catch (RemoteException rEx) {
            LOGGER.error("RemoteException  in getRrdGraph method:-"
                    + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + rEx);
            // error occurred. throw exception
            throw new WSDataUnavailableException();
        } catch (Exception ex) {
            LOGGER.error(" exception in getRrdGraph method:-" + ex);
            // error occurred. throw exception
            throw new WSDataUnavailableException();
        }
        return rrdGraphs;
    }
}
