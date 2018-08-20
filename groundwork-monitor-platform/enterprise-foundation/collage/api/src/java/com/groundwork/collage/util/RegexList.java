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

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * RegexList
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public class RegexList extends AbstractList<Pattern> {

    /** Case-insensitive matching flag */
    private boolean caseInsenstive;

    /** Listener used to get initial and/or dynamic patterns */
    private RegexListListener listener;

    /** Patterns time-to-live, 0 for static patterns */
    private long ttl;

    /** Regular expression patterns */
    private volatile List<Pattern> patterns = new ArrayList<Pattern>();

    /** Dynamic patterns expiration, 0 for static patterns */
    private volatile long expiration;

    /** Background patterns updating flag */
    private volatile boolean updating;

    /**
     * Construct a RegexList using a fixed list of match pattern objects. Objects
     * can be {@link java.util.regex.Pattern} or any object that returns a
     * regular expression pattern string from {@link java.lang.Object#toString()}.
     *
     * @param patterns list of match pattern objects
     * @param caseInsensitive case-insensitive flag
     */
    public RegexList(List<Object> patterns, boolean caseInsensitive) {
        this.patterns = convertPatterns(patterns, caseInsensitive);
    }

    /**
     * Construct a RegexList using a {@link com.groundwork.collage.util.RegexListListener}
     * to get a fixed list of match pattern objects.
     *
     * @param listener the {@link com.groundwork.collage.util.RegexListListener}
     * @param caseInsensitive case-insensitive flag
     */
    public RegexList(RegexListListener listener, boolean caseInsensitive) {
        this(listener.getPatterns(caseInsensitive), caseInsensitive);
        this.listener = listener;
    }

    /**
     * Construct a RegexList using a {@link com.groundwork.collage.util.RegexListListener}
     * to get a dynamic list of match pattern objects with a specific time-to-live.
     * The TTL must be greater than zero to trigger behavior that invokes the listener
     * periodically.
     *
     * @param listener the {@link com.groundwork.collage.util.RegexListListener}
     * @param caseInsensitive case-insensitive flag
     * @param ttl time-to-live in milliseconds
     */
    public RegexList(RegexListListener listener, boolean caseInsensitive, long ttl) {
        this(listener.getPatterns(caseInsensitive), caseInsensitive);
        this.listener = listener;
        this.ttl = ttl;
        updateExpiration();
    }

    @Override
    public Pattern get(int index) {
        return patterns.get(index);
    }

    @Override
    public int size() {
        return patterns.size();
    }

    @Override
    public Pattern set(int index, Pattern element) {
        updateExpiration();
        return patterns.set(index, element);
    }

    @Override
    public boolean add(Pattern pattern) {
        updateExpiration();
        return patterns.add(pattern);
    }

    @Override
    public Pattern remove(int index) {
        updateExpiration();
        return patterns.remove(index);
    }

    /**
     * Match string against regular expression patterns. Initiates dynamic
     * match patterns background update if expired, but does not block on
     * result. A specified string matches if any current pattern matches the
     * entire string.
     *
     * @param string match string
     * @return match flag
     */
    public boolean match(String string) {
        // update patterns in background thread if expired
        if ((ttl > 0) && (listener != null) && !updating && (System.currentTimeMillis() > expiration)) {
            updating = true;
            updateExpiration();
            Thread updateThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        patterns = convertPatterns(listener.getPatterns(caseInsenstive), caseInsenstive);
                    } catch (Exception e) {
                        listener.exception(e);
                    }
                    updating = false;
                }
            }, "RegexListUpdateThread");
            updateThread.setDaemon(true);
            updateThread.start();
        }
        // match patterns
        if (patterns.isEmpty()) {
            return false;
        }
        for (Pattern pattern : patterns) {
            if (pattern.matcher(string).matches()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Update the expiration when pattern list changes.
     */
    private void updateExpiration() {
        if (ttl > 0) {
            expiration = System.currentTimeMillis()+ttl;
        }
    }

    /**
     * Convert match pattern objects to {@link java.util.regex.Pattern}. Non
     * {@link java.util.regex.Pattern} objects are converted using
     * {@link java.lang.Object#toString()} that is expected to return regular
     * expression pattern strings. If case insensitive patterns are specified,
     * {@link java.util.regex.Pattern} instances must be flagged with
     * {@link java.util.regex.Pattern#CASE_INSENSITIVE}.
     *
     * @param patterns list of match pattern objects
     * @param caseInsensitive case-insensitive flag
     * @return list of match {@link java.util.regex.Pattern}
     */
    private static final List<Pattern> convertPatterns(List<Object> patterns, boolean caseInsensitive) {
        List<Pattern> convertedPatterns = new ArrayList<Pattern>();
        for (Object pattern : patterns) {
            if (pattern instanceof Pattern) {
                if (!caseInsensitive || (caseInsensitive && (((Pattern)pattern).flags() & Pattern.CASE_INSENSITIVE) != 0)) {
                    convertedPatterns.add((Pattern) pattern);
                }
            } else {
                try {
                    convertedPatterns.add(Pattern.compile(pattern.toString(), (caseInsensitive ? Pattern.CASE_INSENSITIVE : 0)));
                } catch (Exception e) {
                }
            }
        }
        return convertedPatterns;
    }
}
