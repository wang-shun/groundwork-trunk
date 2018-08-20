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

/**
 * Suggestion
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public class Suggestion implements Comparable<Suggestion> {

    private String name;
    private SuggestionEntityType entityType;

    /**
     * Suggestion constructor.
     *
     * @param name suggestion name
     * @param entityType suggestion entity type
     */
    public Suggestion(String name, SuggestionEntityType entityType) {
        this.name = name;
        this.entityType = entityType;
    }

    @Override
    public int hashCode() {
        return name.toLowerCase().hashCode() | entityType.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        } else if (other instanceof Suggestion) {
            Suggestion otherSuggestion = (Suggestion) other;
            return (name.equalsIgnoreCase(otherSuggestion.name) && entityType.equals(otherSuggestion.entityType));
        } else {
            throw new IllegalArgumentException("Suggestion instance required.");
        }
    }

    @Override
    public int compareTo(Suggestion other) {
        if (other == this) {
            return 0;
        } else if (other != null) {
            int compare = name.toLowerCase().compareTo(other.name.toLowerCase());
            return ((compare != 0) ? compare : entityType.compareTo(other.entityType));
        } else {
            throw new IllegalArgumentException("Suggestion instance required.");
        }
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).
                append("name", name).
                append("entityType", entityType).
                toString();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SuggestionEntityType getEntityType() {
        return entityType;
    }

    public void setEntityType(SuggestionEntityType entityType) {
        this.entityType = entityType;
    }
}
