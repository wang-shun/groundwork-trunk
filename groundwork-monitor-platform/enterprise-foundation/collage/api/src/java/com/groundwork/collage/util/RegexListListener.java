/*
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2014  GroundWork Open Source Solutions info@groundworkopensource.com

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

import java.util.List;

/**
 * RegexListListener
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public interface RegexListListener {

    /**
     * Get current list of match pattern objects to be used by RegexList. Objects
     * can be {@link java.util.regex.Pattern} or any object that returns a
     * regular expression pattern string from {@link java.lang.Object#toString()}.
     * If case insensitive patterns are specified, {@link java.util.regex.Pattern}
     * instances must be flagged with {@link java.util.regex.Pattern#CASE_INSENSITIVE}.
     *
     * @param caseInsensitive case-insensitive flag
     * @return list of match pattern objects
     */
    List<Object> getPatterns(boolean caseInsensitive);

    /**
     * Notification of exception thrown while invoking or processing patterns
     * returned by {@link #getPatterns(boolean)}.
     *
     * @param e thrown exception
     */
    void exception(Exception e);
}
