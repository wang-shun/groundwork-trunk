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

package com.groundwork.feeder.service;

import java.util.List;

/**
 * PerfDataWriter
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public interface PerfDataWriter {

    static final int MAX_LABEL_LENGTH = 19;

    /**
     * Write perf data messages. Messages are assumed to be in the following
     * tab delimited format:
     *
     * serverTime TAB serverName TAB serviceName TAB TAB label = value ; warning ; critical
     *
     * Null or missing values are replaced with empty strings. The label field
     * is limited to 19 characters, (can be the last MAX_LABEL_LENGTH characters
     * of the serviceName). Exceptions thrown by this method will be logged w/o
     * stack trace, but otherwise ignored.
     *
     * @param messageList messages
     * @param appType messages application type
     */
    void writeMessages(List<String> messageList, String appType);
}
