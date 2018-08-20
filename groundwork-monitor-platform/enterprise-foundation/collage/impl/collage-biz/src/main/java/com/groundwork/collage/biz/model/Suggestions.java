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

package com.groundwork.collage.biz.model;

import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Suggestions
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public class Suggestions {

    private int count;
    private List<Suggestion> suggestions;

    /**
     * Suggestions constructor.
     *
     * @param count suggestions count
     */
    public Suggestions(int count) {
        this.count = count;
        this.suggestions = new ArrayList<Suggestion>();
    }

    /**
     * Suggestions constructor.
     *
     * @param count suggestions count
     * @param suggestions suggestions results
     */
    public Suggestions(int count, List<Suggestion> suggestions) {
        this.count = count;
        this.suggestions = suggestions;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).
                append("count", count).
                append("suggestions", suggestions).
                toString();
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<Suggestion> getSuggestions() {
        return suggestions;
    }
}
