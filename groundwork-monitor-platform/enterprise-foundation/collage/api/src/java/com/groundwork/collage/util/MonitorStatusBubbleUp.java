/*
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2015  GroundWork Open Source Solutions info@groundworkopensource.com

 *     This program is free software; you can redistribute it and/or modify
 *     it under the terms of version 2 of the GNU General Public License
 *     as published by the Free Software Foundation.

 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.

 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.groundwork.collage.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * MonitorStatusBubbleUp
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public class MonitorStatusBubbleUp {

    public static final String UNSCHEDULED_DOWN = "UNSCHEDULED DOWN";
    public static final String SCHEDULED_DOWN = "SCHEDULED DOWN";
    public static final String UNREACHABLE = "UNREACHABLE";
    public static final String UNSCHEDULED_CRITICAL = "UNSCHEDULED CRITICAL";
    public static final String CRITICAL = "CRITICAL";
    public static final String WARNING = "WARNING";
    public static final String WARNING_HOST = "WARNING_HOST";
    public static final String WARNING_SERVICE = "WARNING_SERVICE";
    public static final String PENDING = "PENDING";
    public static final String PENDING_HOST = "PENDING_HOST";
    public static final String PENDING_SERVICE = "PENDING_SERVICE";
    public static final String SCHEDULED_CRITICAL = "SCHEDULED CRITICAL";
    public static final String UNKNOWN = "UNKNOWN";
    public static final String OK = "OK";
    public static final String UP = "UP";
    public static final String DOWN = "DOWN";
    public static final String SUSPENDED = "SUSPENDED";

    /**
     * Host monitor status dictionary ordered most to least critical.
     */
    public static final List<String> HOST_MONITOR_STATUS_DICTIONARY = Arrays.asList(new String[]{
            UNSCHEDULED_DOWN,
            WARNING,
            UNREACHABLE,
            SCHEDULED_DOWN,
            PENDING,
            UP
    });

    /**
     * Service monitor status dictionary ordered most to least critical.
     */
    public static final List<String> SERVICE_MONITOR_STATUS_DICTIONARY = Arrays.asList(new String[]{
			UNSCHEDULED_CRITICAL,
            WARNING,
            PENDING,
            SCHEDULED_CRITICAL,
            UNKNOWN,
            OK
    });

    /**
     * Host alias monitor status map.
     */
    public static final Map<String,String> HOST_ALIAS_MONITOR_STATUS_MAP = new HashMap<String,String>();
    static {
        HOST_ALIAS_MONITOR_STATUS_MAP.put(WARNING_HOST, WARNING);
        HOST_ALIAS_MONITOR_STATUS_MAP.put(PENDING_HOST, PENDING);
        HOST_ALIAS_MONITOR_STATUS_MAP.put(DOWN, UNSCHEDULED_DOWN);
    }

    /**
     * Service alias monitor status map.
     */
    public static final Map<String,String> SERVICE_ALIAS_MONITOR_STATUS_MAP = new HashMap<String,String>();
    static {
        SERVICE_ALIAS_MONITOR_STATUS_MAP.put(WARNING_SERVICE, WARNING);
        SERVICE_ALIAS_MONITOR_STATUS_MAP.put(PENDING_SERVICE, PENDING);
        SERVICE_ALIAS_MONITOR_STATUS_MAP.put(CRITICAL, UNSCHEDULED_CRITICAL);
    }

    /**
     * Service to host monitor status dictionary translation.
     */
    public static final Map<String,String> SERVICE_TO_HOST_MONITOR_STATUS_TRANSLATOR = new HashMap<String,String>();
    static {
        SERVICE_TO_HOST_MONITOR_STATUS_TRANSLATOR.put(UNSCHEDULED_CRITICAL, UNSCHEDULED_DOWN);
        SERVICE_TO_HOST_MONITOR_STATUS_TRANSLATOR.put(WARNING, WARNING);
        SERVICE_TO_HOST_MONITOR_STATUS_TRANSLATOR.put(PENDING, PENDING);
        SERVICE_TO_HOST_MONITOR_STATUS_TRANSLATOR.put(SCHEDULED_CRITICAL, SCHEDULED_DOWN);
        SERVICE_TO_HOST_MONITOR_STATUS_TRANSLATOR.put(UNKNOWN, UNREACHABLE);
        SERVICE_TO_HOST_MONITOR_STATUS_TRANSLATOR.put(OK, UP);
    }

    /**
     * Host to service monitor status dictionary translation.
     */
    public static final Map<String,String> HOST_TO_SERVICE_MONITOR_STATUS_TRANSLATOR = new HashMap<String,String>();
    static {
        for (Map.Entry<String,String> translation : SERVICE_TO_HOST_MONITOR_STATUS_TRANSLATOR.entrySet()) {
            HOST_TO_SERVICE_MONITOR_STATUS_TRANSLATOR.put(translation.getValue(), translation.getKey());
        }
    }

    /**
     * Host monitor status set that allows service bubble up computation.
     */
    public static final Set<String> HOST_BUBBLE_UP_MONITOR_STATUS_SET = new HashSet<String>();
    static {
        HOST_BUBBLE_UP_MONITOR_STATUS_SET.add(PENDING);
        HOST_BUBBLE_UP_MONITOR_STATUS_SET.add(UP);
    }

    /**
     * Host monitor status set that should not be bubbled up from services.
     */
    public static final Set<String> HOST_IGNORE_BUBBLE_UP_MONITOR_STATUS_SET = new HashSet<String>();
    static {
        HOST_IGNORE_BUBBLE_UP_MONITOR_STATUS_SET.add(PENDING);
    }

    /**
     * Monitor status extractor for bubble up computation.
     *
     * @param <T> type
     */
    public interface MonitorStatusExtractor<T> {
        String extractMonitorStatus(T obj);
    }

    /**
     * Compute host bubble up status from services.
     *
     * @param monitorStatus host monitor status
     * @param children services children
     * @param extractor services child monitor status extractor
     * @param <T> services child type
     * @return bubble up status
     */
    public static <T> String computeHostMonitorStatusBubbleUp(String monitorStatus, Collection<T> children,
                                                              MonitorStatusExtractor<T> extractor) {
        return computeMonitorStatusBubbleUp(monitorStatus, children, extractor,
                HOST_MONITOR_STATUS_DICTIONARY,
                HOST_ALIAS_MONITOR_STATUS_MAP,
                HOST_BUBBLE_UP_MONITOR_STATUS_SET,
                HOST_IGNORE_BUBBLE_UP_MONITOR_STATUS_SET,
                SERVICE_MONITOR_STATUS_DICTIONARY,
                SERVICE_ALIAS_MONITOR_STATUS_MAP,
                SERVICE_TO_HOST_MONITOR_STATUS_TRANSLATOR);
    }

    /**
     * Compute host group bubble up status from children.
     *
     * @param children host group children
     * @param extractor child monitor status extractor
     * @param <T> child type
     * @return bubble up status
     */
    public static <T> String computeHostGroupMonitorStatusBubbleUp(Collection<T> children,
                                                                   MonitorStatusExtractor<T> extractor) {
        return computeMonitorStatusBubbleUp(null, children, extractor,
                MonitorStatusBubbleUp.HOST_MONITOR_STATUS_DICTIONARY,
                MonitorStatusBubbleUp.HOST_ALIAS_MONITOR_STATUS_MAP);
    }

    /**
     * Compute service group bubble up status from children.
     *
     * @param children service group children
     * @param extractor child monitor status extractor
     * @param <T> child type
     * @return bubble up status
     */
    public static <T> String computeServiceGroupMonitorStatusBubbleUp(Collection<T> children,
                                                                      MonitorStatusExtractor<T> extractor) {
        return computeMonitorStatusBubbleUp(null, children, extractor,
                MonitorStatusBubbleUp.SERVICE_MONITOR_STATUS_DICTIONARY,
                MonitorStatusBubbleUp.SERVICE_ALIAS_MONITOR_STATUS_MAP);
    }

    /**
     * Compute bubble up status from children.
     *
     * @param monitorStatus initial monitor status
     * @param children children
     * @param extractor child monitor status extractor
     * @param dictionary monitor status dictionary
     * @param aliases monitor status alias
     * @param <T> child type
     * @return initial or bubble up status
     */
    public static <T> String computeMonitorStatusBubbleUp(String monitorStatus, Collection<T> children,
                                                          MonitorStatusExtractor<T> extractor, List<String> dictionary,
                                                          Map<String,String> aliases) {
        return computeMonitorStatusBubbleUp(monitorStatus, children, extractor, dictionary, aliases, null, null,
                dictionary, aliases, null);
    }

    /**
     * Compute bubble up status from children.
     *
     * @param monitorStatus initial monitor status
     * @param children children
     * @param extractor child monitor status extractor
     * @param dictionary monitor status dictionary
     * @param aliases monitor status alias
     * @param bubbleUpMonitorStatus initial monitor status set that allow bubble up computation
     * @param ignoreBubbleUpMonitorStatus bubble up monitor status set that must not replace the initial monitor status
     * @param childDictionary child monitor status dictionary
     * @param childAliases child monitor status alias
     * @param translator child to return monitor status translator
     * @param <T> child type
     * @return initial or bubble up status
     */
    public static <T> String computeMonitorStatusBubbleUp(String monitorStatus, Collection<T> children,
                                                          MonitorStatusExtractor<T> extractor, List<String> dictionary,
                                                          Map<String,String> aliases, Set<String> bubbleUpMonitorStatus,
                                                          Set<String> ignoreBubbleUpMonitorStatus,
                                                          List<String> childDictionary, Map<String,String> childAliases,
                                                          Map<String,String> translator) {

        String currentStatus = null;
        if (monitorStatus != null) {
            // lookup current status
            currentStatus = lookupMonitorStatus(monitorStatus, dictionary, aliases);
            if (currentStatus == null) {
                return monitorStatus;
            }

            // ensure current status should be bubbled up
            if ((bubbleUpMonitorStatus != null) && !bubbleUpMonitorStatus.contains(currentStatus)) {
                return currentStatus;
            }
        }

        // extract, lookup, and map child statuses
        if ((children == null) || children.isEmpty()) {
            return currentStatus;
        }
        Set<String> childStatuses = new HashSet<String>();
        for (T child : children) {
            String childStatus = extractor.extractMonitorStatus(child);
            // lookup child status
            childStatus = lookupMonitorStatus(childStatus, childDictionary, childAliases);
            if (childStatus != null) {
                childStatuses.add(childStatus);
            }
        }

        // bubble up child status
        int bubbleUpStatusIndex = Integer.MAX_VALUE;
        for (String childStatus : childStatuses) {
            int index = childDictionary.indexOf(childStatus);
            if ((index != -1) && (index < bubbleUpStatusIndex)) {
                bubbleUpStatusIndex = index;
            }
        }
        if (bubbleUpStatusIndex == Integer.MAX_VALUE) {
            return currentStatus;
        }
        String bubbleUpStatus = childDictionary.get(bubbleUpStatusIndex);

        // translate child monitor status
        if (translator != null) {
            bubbleUpStatus = translator.get(bubbleUpStatus);
            if (bubbleUpStatus == null) {
                return currentStatus;
            }
        }

        // ignore bubble up status
        if ((ignoreBubbleUpMonitorStatus != null) && ignoreBubbleUpMonitorStatus.contains(bubbleUpStatus)) {
            return currentStatus;
        }

        // return bubble up status
        return bubbleUpStatus;
    }

    /**
     * Lookup monitor status in dictionary and aliases.
     *
     * @param monitorStatus monitor status to lookup
     * @param dictionary status dictionary
     * @param aliases alias map
     * @return canonical monitor status or null
     */
    private static String lookupMonitorStatus(String monitorStatus, List<String> dictionary, Map<String,String> aliases) {
        if (monitorStatus != null) {
            // lookup
            for (String status : dictionary) {
                if (monitorStatus.equalsIgnoreCase(status)) {
                    return status;
                }
            }
            // map aliases
            if (aliases != null) {
                for (Map.Entry<String,String> alias : aliases.entrySet()) {
                    if (monitorStatus.equalsIgnoreCase(alias.getKey())) {
                        return alias.getValue();
                    }
                }
            }
        }
        return null;
    }
}
