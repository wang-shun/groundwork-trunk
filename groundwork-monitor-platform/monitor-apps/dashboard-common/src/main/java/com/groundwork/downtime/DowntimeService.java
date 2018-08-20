package com.groundwork.downtime;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface DowntimeService {

    /**
     * Login to Downtime service via JOSSO. The goal here is to piggyback on the Agent by passing a JOSSO JSESSIONID token,
     * which will in turn give back a JOSSO_JSESSIONID provided by the /nms-tools webapp JOSSO agent filter.
     * This is not working yet, so we are still logging in with hard-coded credentials
     *
     * @param groundworkServer the host in format scheme://host[:port] i.e. https://localhost
     * @param username
     * @param password
     * @return a Downtime context with required credentials and cookies to make Http client requests
     * @throws DowntimeException
     */
    DowntimeContext login(String groundworkServer, String username, String password) throws DowntimeException;

    /**
     * relogin on session time out
     *
     * @param context
     * @return
     * @throws DowntimeException
     */
    DowntimeContext relogin(DowntimeContext context) throws DowntimeException;

    /**
     * Logout of the JOSSO service. Used in tests primarily to manage JOSSO resources
     *
     * @param context
     * @throws DowntimeException
     */
    void logout(DowntimeContext context) throws DowntimeException;

    /**
     * List all downtimes
     *
     * @param context contains required credentials and cookies to make Http client requests
     * @return a list of 0..n downtime records
     * @throws DowntimeException
     */
    List<DtoDowntime> list(DowntimeContext context) throws DowntimeException;

    /**
     * Retrieve downtime maintenance windows for hosts:services by date:time range
     * Each element is uniquely identified by a host::service name
     * A list of downtime maintenance windows are returned for each element
     *
     * @param context
     * @param startRange
     * @param endRange
     * @return a list of downtime maintenance window transitiosn
     * @throws DowntimeException
     */
    Map<String, List<DowntimeMaintenanceWindow>> range(DowntimeContext context, Date startRange, Date endRange) throws DowntimeException;

    /**
     * Lookup a maintenance window in given map
     *
     * @param hostName
     * @param serviceName
     * @param maintenanceWindows
     * @return
     */
    List<DowntimeMaintenanceWindow> lookup(String hostName, String serviceName, Map<String,List<DowntimeMaintenanceWindow>> maintenanceWindows);

    boolean ping(DowntimeContext context);
}
