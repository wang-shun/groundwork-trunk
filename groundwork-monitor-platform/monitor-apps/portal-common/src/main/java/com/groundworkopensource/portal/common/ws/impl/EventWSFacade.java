package com.groundworkopensource.portal.common.ws.impl;

import java.rmi.RemoteException;

import javax.xml.rpc.ServiceException;

import org.apache.log4j.Logger;
import org.groundwork.foundation.ws.api.WSEvent;
import org.groundwork.foundation.ws.api.WSFoundationException;
import org.groundwork.foundation.ws.model.impl.Filter;
import org.groundwork.foundation.ws.model.impl.Sort;
import org.groundwork.foundation.ws.model.impl.WSFoundationCollection;

import com.groundworkopensource.portal.common.CommonConstants;
import com.groundworkopensource.portal.common.exception.GWPortalException;
import com.groundworkopensource.portal.common.exception.WSDataUnavailableException;
import com.groundworkopensource.portal.common.ws.IEventWSFacade;

/**
 * This class provides implementation for IEventWSFacade interface which
 * interact with 'wsEvent' foundation web service.
 * 
 * @author shivangi_walvekar
 * 
 */
public class EventWSFacade implements IEventWSFacade {

    /**
     * SERVICE EXCEPTION MESSAGE
     */
    private static final String SERVICE_EXCEPTION_MESSAGE = "ServiceException while getting binding object for \"event\" web service";

    /**
     * REMOTE EXCEPTION MESSAGE
     */
    private static final String REMOTE_EXCEPTION_MESSAGE = "RemoteException while contacting \"event\" foundation web service";

    /**
     * WSFOUNDATION EXCEPTION MESSAGE
     */
    private static final String WSFOUNDATION_EXCEPTION_MESSAGE = "WSFoundationException while getting \"event\" web service  data";

    /**
     * logger
     */
    private static final Logger LOGGER = FoundationWSFacade.getLogger();

    /**
     * Returns Binding object for "event" web service
     * 
     * @return WSEvent Binding
     * @throws GWPortalException
     */
    private WSEvent getEventBinding() throws GWPortalException {
        // get the event binding object
        try {
            WSEvent eventBinding = WebServiceLocator.getInstance()
                    .eventLocator().getwsevent();
            if (null != eventBinding) {
                return eventBinding;
            }
        } catch (ServiceException sEx) {
            LOGGER.fatal(SERVICE_EXCEPTION_MESSAGE
                    + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + sEx);
        }
        throw new GWPortalException();
    }

    /**
     * (non-Javadoc)
     * 
     * @throws GWPortalException
     * @throws WSDataUnavailableException
     * 
     * @see com.groundworkopensource.portal.common.ws.IEventWSFacade#
     *      getHostStateTransitions (java.lang. String, java.lang. String,
     *      java.lang. String)
     */
    public WSFoundationCollection getHostStateTransitions(String hostName,
            String startDate, String endDate) throws GWPortalException,
            WSDataUnavailableException {
        WSFoundationCollection foundationCollection = null;
        // by default return null foundationCollection
        WSEvent eventBinding = getEventBinding();
        try {
            // LOGGER.debug("getting HostStateTransitions for host =" + hostName
            // + " startdate= " + startDate + " enddate= " + endDate);
            foundationCollection = eventBinding.getHostStateTransitions(
                    hostName, startDate, endDate);
            if (foundationCollection == null) {
                LOGGER.info("getting HostStateTransitions for host ="
                        + hostName + " startdate= " + startDate + " enddate= "
                        + endDate);
                throw new WSDataUnavailableException();
            }
        } catch (WSFoundationException fEx) {
            LOGGER.error(WSFOUNDATION_EXCEPTION_MESSAGE
                    + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + fEx);
            throw new WSDataUnavailableException();
        } catch (RemoteException rEx) {
            LOGGER.error(REMOTE_EXCEPTION_MESSAGE
                    + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + rEx);
            throw new WSDataUnavailableException();
        }
        return foundationCollection;
    }

    /**
     * (non-Javadoc)
     * 
     * @throws GWPortalException
     * @throws WSDataUnavailableException
     * 
     * @see com.groundworkopensource.portal.common.ws.IEventWSFacade#
     *      getServiceStateTransitions (java.lang. String, java.lang. String,
     *      java.lang. String, java.lang. String)
     */
    public WSFoundationCollection getServiceStateTransitions(String hostName,
            String serviceName, String startDate, String endDate)
            throws GWPortalException, WSDataUnavailableException {
        WSFoundationCollection foundationCollection = null;
        // by default return null foundationCollection
        WSEvent eventBinding = getEventBinding();
        try {
            // LOGGER.debug("getting getServiceStateTransitions for host ="
            // + hostName + "servicename= " + serviceName + " startdate= "
            // + startDate + " enddate= " + endDate);
            foundationCollection = eventBinding.getServiceStateTransitions(
                    hostName, serviceName, startDate, endDate);
            if (foundationCollection == null) {
                LOGGER
                        .info("Found null WSFoundationCollection when calling getServiceStateTransitions() for host ="
                                + hostName
                                + "servicename= "
                                + serviceName
                                + " startdate= "
                                + startDate
                                + " enddate= "
                                + endDate);
                throw new WSDataUnavailableException();
            }
        } catch (WSFoundationException fEx) {
            LOGGER.error(WSFOUNDATION_EXCEPTION_MESSAGE
                    + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + fEx);
            throw new WSDataUnavailableException();
        } catch (RemoteException rEx) {
            LOGGER.error(REMOTE_EXCEPTION_MESSAGE
                    + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + rEx);
            throw new WSDataUnavailableException();
        }
        return foundationCollection;
    }

    /**
     * (non-Javadoc)
     * 
     * @throws GWPortalException
     * @throws WSDataUnavailableException
     * 
     * @see com.groundworkopensource.portal.common.ws.IEventWSFacade#getEventsByCriteria(org.groundwork.foundation.ws.model.impl.Filter,
     *      org.groundwork.foundation.ws.model.impl.Sort, int, int)
     */
    public WSFoundationCollection getEventsByCriteria(Filter filter, Sort sort,
            int startIndex, int endIndex) throws GWPortalException,
            WSDataUnavailableException {
        WSFoundationCollection foundationCollection = null;
        WSEvent eventBinding = getEventBinding();
        try {
            foundationCollection = eventBinding.getEventsByCriteria(filter,
                    sort, startIndex, endIndex);
            if (foundationCollection == null) {
                LOGGER
                        .info("WSFoundationCollection is null in getEventsByCriteria() method");
                throw new WSDataUnavailableException();
            }
        } catch (WSFoundationException fEx) {
            LOGGER.error(WSFOUNDATION_EXCEPTION_MESSAGE
                    + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + fEx);
            throw new WSDataUnavailableException();
        } catch (RemoteException rEx) {
            LOGGER.error(REMOTE_EXCEPTION_MESSAGE
                    + CommonConstants.ACTUAL_EXCEPTION_MESSAGE + rEx);
            throw new WSDataUnavailableException();
        }
        return foundationCollection;

    }
}
