package com.groundworkopensource.portal.statusviewer.common;

import java.util.HashMap;

/**
 * 
 * @author manish_kjain
 * 
 */
public class EventTableMappingUtil {

    /**
     * HashMap
     */
    private static HashMap<String, String> sortKeyMap = null;

    /**
     * Protected Constructor - Rationale: Instantiating utility classes does not
     * make sense. Hence the constructors should either be private or (if you
     * want to allow sub-classing) protected. <br>
     * 
     * Refer to "HideUtilityClassConstructor" section in
     * http://checkstyle.sourceforge.net/config_design.html.
     */
    protected EventTableMappingUtil() {
        // prevents calls from subclass
        throw new UnsupportedOperationException();
    }

    /**
     * @param key
     * @return String
     */
    public static synchronized String findORMapping(String key) {
        if (sortKeyMap == null) {
            sortKeyMap = new HashMap<String, String>();
            sortKeyMap.put(Constant.REPORT_DATE, Constant.REPORT_DATE);
            sortKeyMap.put(Constant.MSG_COUNT, Constant.MSG_COUNT);
            sortKeyMap.put(Constant.DEVICE, Constant.DEVICE_DISPLAY_NAME);
            sortKeyMap.put(Constant.EVENT_STATUS_BEAN,
                    Constant.MONITOR_STATUS_NAME);
            sortKeyMap.put(Constant.SEVERITY, Constant.SEVERITY_NAME);
            sortKeyMap.put(Constant.APPLICATION_TYPE,
                    Constant.APPLICATION_TYPE_NAME);
            sortKeyMap.put(Constant.TEXT_MESSAGE, Constant.TEXT_MESSAGE);
            sortKeyMap
                    .put(Constant.LAST_INSERT_DATE, Constant.LAST_INSERT_DATE);
            sortKeyMap.put(Constant.FIRST_INSERT_DATE,
                    Constant.FIRST_INSERT_DATE);
        }
        return sortKeyMap.get(key);
    }
}
