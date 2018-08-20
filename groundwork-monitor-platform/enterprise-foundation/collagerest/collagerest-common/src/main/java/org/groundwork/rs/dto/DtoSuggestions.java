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

package org.groundwork.rs.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * DtoSuggestions
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
@XmlRootElement(name="suggestions")
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoSuggestions {

    @XmlAttribute
    private int count;
    @XmlElementWrapper(name="suggestions")
    @XmlElement(name="suggestion")
    private List<DtoSuggestion> suggestions = new ArrayList<DtoSuggestion>();

    /**
     * Default constructor.
     */
    public DtoSuggestions() {
    }

    /**
     * Full shallow copy constructor.
     *
     * @param count suggestions total count
     * @param suggestions suggestions list
     */
    public DtoSuggestions(int count, List<DtoSuggestion> suggestions) {
        this.count = count;
        this.suggestions.addAll(suggestions);
    }

    /**
     * toString Object protocol implementation.
     *
     * @return Suggestions as String
     */
    @Override
    public String toString() {
        return String.format(getClass().getName()+"@%x[count=%d,suggestions=%s]",
                System.identityHashCode(this), count, suggestions);
    }

    public int getCount() {
        return count;
    }

    public List<DtoSuggestion> getSuggestions() {
        return suggestions;
    }
}
