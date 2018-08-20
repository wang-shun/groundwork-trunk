/*
 * Copyright (C) 2009 GroundWork Open Source, Inc. (GroundWork) All rights
 * reserved. Use is subject to GroundWork commercial license terms.
 */

package com.groundworkopensource.webapp.console;

import java.util.HashMap;

/**
 * The Class ConsoleORMappingUtil.
 */
public class ConsoleORMappingUtil {

    /** The map. */
    private static HashMap<String, String> map = null;

    /**
     * Find or mapping.
     * 
     * @param key
     *            the key
     * 
     * @return the mapping string against they key received.
     */
    public static String findORMapping(String key) {
        if (map == null) {
            map = new HashMap<String, String>();
            map.put("reportDate", "reportDate");
            map.put("msgCount", "msgCount");
            /*
             * changing mapping for "device" from "identification" to
             * "displayName" in order to resolve JIRA GWMON-8226
             */
            map.put("device", "device.displayName");
            map.put("host", "hostStatus");
            map.put("serviceDescription", "serviceStatus.serviceDescription");
            map.put("monitorStatus", "monitorStatus.monitorStatusId");
            map.put("severity", "severity.name");
            map.put("applicationType", "applicationType.name");
            map.put("textMessage", "textMessage");
            map.put("lastInsertDate", "lastInsertDate");
            map.put("firstInsertDate", "firstInsertDate");
            map.put("operationStatus", "operationStatus.name");
        }
        return map.get(key);
    }

    /**
     * Protected Constructor - Rationale: Instantiating utility classes does not
     * make sense. Hence the constructors should either be private or (if you
     * want to allow sub-classing) protected. <br>
     * 
     * Refer to "HideUtilityClassConstructor" section in
     * http://checkstyle.sourceforge.net/config_design.html.
     */
    protected ConsoleORMappingUtil() {
        // prevents calls from subclass
        throw new UnsupportedOperationException();
    }
}
