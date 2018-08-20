package com.groundworkopensource.portal.common.ws;

import org.groundwork.foundation.ws.model.impl.Filter;
import org.groundwork.foundation.ws.model.impl.Sort;
import org.groundwork.foundation.ws.model.impl.WSFoundationCollection;

import com.groundworkopensource.portal.common.exception.GWPortalException;
import com.groundworkopensource.portal.common.exception.WSDataUnavailableException;

/**
 * Interface defining methods for "event" web service
 * 
 * @author shivangi_walvekar
 * 
 */
public interface IEventWSFacade {
    /**
     * This method returns host state transitions data for the host-name within
     * the given date range (start-date to end-date).
     * 
     * @param hostName
     * @param startDate
     * @param endDate
     * @return WSFoundationCollection containing state transitions datas
     * @throws GWPortalException
     * @throws WSDataUnavailableException
     */
    WSFoundationCollection getHostStateTransitions(String hostName,
            String startDate, String endDate) throws GWPortalException,
            WSDataUnavailableException;

    /**
     * @param hostName
     * @param serviceName
     * @param startDate
     * @param endDate
     * @return WSFoundationCollection containing state transitions data
     * @throws GWPortalException
     * @throws WSDataUnavailableException
     */
    WSFoundationCollection getServiceStateTransitions(String hostName,
            String serviceName, String startDate, String endDate)
            throws GWPortalException, WSDataUnavailableException;

    /**
     * returns log Message array depending on filter applied.
     * 
     * @param filter
     * @param sort
     * @param startIndex
     * @param endIndex
     * @return WSFoundationCollection
     * @throws GWPortalException
     * @throws WSDataUnavailableException
     */
    WSFoundationCollection getEventsByCriteria(Filter filter, Sort sort,
            int startIndex, int endIndex) throws GWPortalException,
            WSDataUnavailableException;
}
