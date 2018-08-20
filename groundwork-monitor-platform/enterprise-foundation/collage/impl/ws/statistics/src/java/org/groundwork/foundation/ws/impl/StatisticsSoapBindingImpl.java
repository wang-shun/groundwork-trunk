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
import org.groundwork.foundation.ws.api.WSStatistics;
import org.groundwork.foundation.ws.model.impl.ExceptionType;
import org.groundwork.foundation.ws.model.impl.Filter;
import org.groundwork.foundation.ws.model.NagiosStatisticQueryType;
import org.groundwork.foundation.ws.model.StatisticQueryType;
import org.groundwork.foundation.ws.model.impl.WSFoundationCollection;

import com.groundwork.collage.CollageFactory;

public class StatisticsSoapBindingImpl implements WSStatistics
{
	 protected static Log log = LogFactory.getLog(StatisticsSoapBindingImpl.class);
    /* (non-Javadoc)
     * @see org.groundwork.foundation.ws.api.WSStatistics#getNagiosStatistics(java.lang.String, java.lang.String, java.lang.String)
     */
    public WSFoundationCollection getNagiosStatisticsByString(String type, String value) throws WSFoundationException, RemoteException {
        // get the WSStatistics api object.
        CollageFactory factory = CollageFactory.getInstance();
        WSStatistics statistics = (WSStatistics) factory.getAPIObject("WSStatistics");
        
        // check the event object, if getting it failed, bail out now.
        if (statistics == null) {
            throw new WSFoundationException("Unable to create WSStatistics instance", ExceptionType.SYSTEM);
        }
        // all is well, call our implementation.
        else {
           return statistics.getNagiosStatisticsByString(type, value);
        }
    }

    /* (non-Javadoc)
     * @see org.groundwork.foundation.ws.api.WSStatistics#getStatistics(java.lang.String, java.lang.String, java.lang.String)
     */
    public WSFoundationCollection getStatisticsByString(String type, String value, String applicationType) throws WSFoundationException, RemoteException {
        // get the WSStatistics api object.
        CollageFactory factory = CollageFactory.getInstance();
        WSStatistics statistics = (WSStatistics) factory.getAPIObject("WSStatistics");
        
        // check the event object, if getting it failed, bail out now.
        if (statistics == null) {
            throw new WSFoundationException("Unable to create WSStatistics instance", ExceptionType.SYSTEM);
        }
        // all is well, call our implementation.
        else {
           return statistics.getStatisticsByString(type, value, applicationType);
        }
    }
    
    public WSFoundationCollection getStatistics(StatisticQueryType statisticQueryType, java.lang.String value, java.lang.String applicationType) throws java.rmi.RemoteException, WSFoundationException {

        // get the WSStatistics api object.
        CollageFactory factory = CollageFactory.getInstance();
        WSStatistics statistics = (WSStatistics) factory.getAPIObject("WSStatistics");
        
        // check the event object, if getting it failed, bail out now.
        if (statistics == null) {
            throw new WSFoundationException("Unable to create WSStatistics instance", ExceptionType.SYSTEM);
        }
        // all is well, call our implementation.
        else {
           return statistics.getStatistics(statisticQueryType, value, applicationType);
        }
    }
    
    public WSFoundationCollection getGroupStatistics(StatisticQueryType statisticQueryType,Filter filter,  java.lang.String groupName, java.lang.String applicationType) throws java.rmi.RemoteException, WSFoundationException {

        // get the WSStatistics api object.
        CollageFactory factory = CollageFactory.getInstance();
        WSStatistics statistics = (WSStatistics) factory.getAPIObject("WSStatistics");
        
        // check the event object, if getting it failed, bail out now.
        if (statistics == null) {
            throw new WSFoundationException("Unable to create WSStatistics instance", ExceptionType.SYSTEM);
        }
        // all is well, call our implementation.
        else {
           return statistics.getGroupStatistics(statisticQueryType, filter, groupName, applicationType);
        }
    }

    public WSFoundationCollection getNagiosStatistics(NagiosStatisticQueryType nagiosStatisticsQueryType, java.lang.String value) throws java.rmi.RemoteException, WSFoundationException {

        // get the WSStatistics api object.
        CollageFactory factory = CollageFactory.getInstance();
        WSStatistics statistics = (WSStatistics) factory.getAPIObject("WSStatistics");
        
        // check the event object, if getting it failed, bail out now.
        if (statistics == null) {
            throw new WSFoundationException("Unable to create WSStatistics instance", ExceptionType.SYSTEM);
        }
        // all is well, call our implementation.
        else {
           return statistics.getNagiosStatistics(nagiosStatisticsQueryType, value);
        }
    }
    
    
        public double getHostAvailabilityForHostgroup(java.lang.String hostGroupName) throws java.rmi.RemoteException, WSFoundationException {
        	
            // get the WSStatistics api object.
            CollageFactory factory = CollageFactory.getInstance();
            WSStatistics statistics = (WSStatistics) factory.getAPIObject("WSStatistics");
            log.debug("StatisticsSoapBindingImpl.getHostAvailabilityForHostgroup  hostGroupName=["+hostGroupName+"] value =["+statistics.getHostAvailabilityForHostgroup(hostGroupName)+"]");
            // check the event object, if getting it failed, bail out now.
            if (statistics == null) {
                throw new WSFoundationException("Unable to create WSStatistics instance", ExceptionType.SYSTEM);
            }
            // all is well, call our implementation.
            else {
               return statistics.getHostAvailabilityForHostgroup(hostGroupName);
            }
        }
        
        public double getServiceAvailabilityForHostgroup(java.lang.String hostGroupName) throws java.rmi.RemoteException, WSFoundationException {
        	
            // get the WSStatistics api object.
            CollageFactory factory = CollageFactory.getInstance();
            WSStatistics statistics = (WSStatistics) factory.getAPIObject("WSStatistics");
            log.debug("StatisticsSoapBindingImpl.getServiceAvailabilityForHostgroup  hostGroupName=["+hostGroupName+"] value =["+statistics.getServiceAvailabilityForHostgroup(hostGroupName)+"]");
            // check the event object, if getting it failed, bail out now.
            if (statistics == null) {
                throw new WSFoundationException("Unable to create WSStatistics instance", ExceptionType.SYSTEM);
            }
            // all is well, call our implementation.
            else {
               return statistics.getServiceAvailabilityForHostgroup(hostGroupName);
            }
        }
        
        public double getServiceAvailabilityForServiceGroup(java.lang.String serviceGroupName) throws java.rmi.RemoteException, WSFoundationException {
        	
            // get the WSStatistics api object.
            CollageFactory factory = CollageFactory.getInstance();
            WSStatistics statistics = (WSStatistics) factory.getAPIObject("WSStatistics");
            log.debug("StatisticsSoapBindingImpl.getServiceAvailabilityForHostgroup  serviceGroupName=["+serviceGroupName+"] value =["+statistics.getServiceAvailabilityForServiceGroup(serviceGroupName)+"]");
            // check the event object, if getting it failed, bail out now.
            if (statistics == null) {
                throw new WSFoundationException("Unable to create WSStatistics instance", ExceptionType.SYSTEM);
            }
            // all is well, call our implementation.
            else {
               return statistics.getServiceAvailabilityForServiceGroup(serviceGroupName);
            }
        }
 
}
