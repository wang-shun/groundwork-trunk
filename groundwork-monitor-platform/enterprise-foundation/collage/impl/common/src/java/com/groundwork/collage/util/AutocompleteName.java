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

/**
 * AutocompleteName
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public class AutocompleteName implements Comparable<AutocompleteName> {

    private String name;
    private String lowerCaseName;
    private String canonicalName;

    /**
     * Construct autcomplete name.
     *
     * @param name autocomplete name
     */
    public AutocompleteName(String name) {
        if (name == null) {
            throw new NullPointerException("Name cannot be null");
        }
        this.name = name;
        this.lowerCaseName = name.toLowerCase();
    }

    /**
     * Construct autcomplete name for alias.
     *
     * @param name autocomplete alias name
     * @param canonicalName autocomplete canonical name
     */
    public AutocompleteName(String name, String canonicalName) {
        this(name);
        this.canonicalName = canonicalName;
    }

    /**
     * Comparable compareTo implementation: compare folded names.
     *
     * @param other compare to other
     * @return comparison
     */
    @Override
    public int compareTo(AutocompleteName other) {
        return lowerCaseName.compareTo(other.lowerCaseName);
    }

    /**
     * Object hashCode implementation: hash folded name.
     *
     * @return hash code
     */
    @Override
    public int hashCode() {
        return lowerCaseName.hashCode();
    }

    /**
     * Object equals implementation: test folded names.
     *
     * @return equals
     */
    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (other instanceof AutocompleteName) {
            return lowerCaseName.equals(((AutocompleteName) other).lowerCaseName);
        }
        return false;
    }

    public String getName() {
        return name;
    }

    public String getLowerCaseName() {
        return lowerCaseName;
    }

    public String getCanonicalName() {
        return canonicalName;
    }
}
