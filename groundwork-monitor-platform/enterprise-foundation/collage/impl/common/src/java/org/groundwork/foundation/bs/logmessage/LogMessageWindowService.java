package org.groundwork.foundation.bs.logmessage;

import com.groundwork.collage.model.impl.StateTransition;
import org.groundwork.foundation.bs.exception.BusinessServiceException;

import java.util.List;

/**
 * LogMessageWindowService
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public interface LogMessageWindowService {

    String SERVICE = "org.groundwork.foundation.bs.logmessage.LogMessageWindowService";

    /**
     * Return window enabled configuration.
     *
     * @return window enabled
     */
    boolean isWindowEnabled();

    /**
     * Return window initialized status.
     *
     * @return window initialized
     */
    boolean isWindowInitialized();

    /**
     * Return whether date range is within window.
     *
     * @return in window
     */
    boolean isInWindow(String startDate, String endDate) throws BusinessServiceException;

    /**
     * Returns a List of StateTransition instances for the specified host for the date range provided or null
     * if the date range is outside the managed window.
     *
     * @param hostName host name
     * @param startDate start of date range
     * @param endDate end of date range
     * @return state transitions in date range
     * @throws BusinessServiceException
     */
    List<StateTransition> getHostStateTransitions(String hostName, String startDate, String endDate)
            throws BusinessServiceException;

    /**
     * Returns a List of StateTransition instances for the specified service for the date range provided or null
     * if the date range is outside the managed window.
     *
     * @param hostName service host name
     * @param serviceName service description
     * @param startDate state of date range
     * @param endDate end of date range
     * @return state transitions in date range
     * @throws BusinessServiceException
     */
    List<StateTransition> getServiceStateTransitions(String hostName, String serviceName, String startDate, String endDate)
            throws BusinessServiceException;
}
