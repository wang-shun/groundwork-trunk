/*
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2007  GroundWork Open Source Solutions info@groundworkopensource.com

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

package com.groundwork.collage.model.impl;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;
 

/**
 * 
 * ConsolidationCriteriaImpl
 * @author <a href="mailto:rruttimann@itgroundwork.com"> Roger Ruttimann</a>
 * @version $Id: ConsolidationCriteria.java 7205 2007-07-05 20:15:48Z rruttimann $
 */
public class ConsolidationCriteria implements Serializable, com.groundwork.collage.model.ConsolidationCriteria 
{
    private static final long serialVersionUID = 1;

    /** identifier field */
    private Integer consolidationCriteriaId;

    /** persistent field */
    private String name;

    /** persistent field */
    private String criteria;

    /** full constructor */
    public ConsolidationCriteria(Integer consolidationCriteriaId, String name,
            String criteria) {
        this.consolidationCriteriaId = consolidationCriteriaId;
        this.name = name;
        this.criteria = criteria;
    }

    /** default constructor */
    public ConsolidationCriteria() {
    }

    public Integer getConsolidationCriteriaId() {
        return this.consolidationCriteriaId;
    }

    public void setConsolidationCriteriaId(Integer consolidationCriteriaId) {
        this.consolidationCriteriaId = consolidationCriteriaId;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCriteria() {
        return this.criteria;
    }

    public void setCriteria(String criteria) {
        this.criteria = criteria;
    }

    public String toString() {
        return new ToStringBuilder(this).append("consolidationCriteriaId",
                getConsolidationCriteriaId()).toString();
    }

}
