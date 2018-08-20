
package com.groundworkopensource.portal.common.ws.impl.test;

/**
 * Constant files for test package.
 * 
 * @author swapnil_gujrathi
 * 
 */
public class TestConstants {
    /**
     * Protected Constructor - Rationale: Instantiating utility classes does not
     * make sense. Hence the constructors should either be private or (if you
     * want to allow sub-classing) protected. <br>
     * 
     * Refer to "HideUtilityClassConstructor" section in
     * http://checkstyle.sourceforge.net/config_design.html.
     */
    protected TestConstants() {
        // prevents calls from subclass
        throw new UnsupportedOperationException();
    }

    /**
     * EMPTY_STRING - ""
     */
    public static final String EMPTY_STRING = "";

    /**
     * COMMA - ","
     */
    public static final String COMMA = ",";
    /**
     * host group ID
     */
    public static final String HOST_HOST_GROUPS_HOST_GROUP_ID = "host.hostGroups.hostGroupId";
    /**
     * Nagios
     */
    public static final String NAGIOS = "NAGIOS";
    /**
     * ok monitor status
     */
    public static final String OK = "OK";
    /**
     * service string property
     */
    public static final String MONITOR_STATUS_NAME = "monitorStatus.name";
    /**
     * Up monitor status
     */
    public static final String UP = "Up";
    /**
     * host name string property
     */
    public static final String HOSTS_HOST_STATUS_HOST_MONITOR_STATUS_NAME = "hosts.hostStatus.hostMonitorStatus.name";
    /**
     * StatisticsWSFacade instance.
     */

}
