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

package com.groundwork.collage.model;

/**
 * 
 * ConsolidationCriteriaInterface
 * @author <a href="mailto:rruttimann@itgroundwork.com"> Roger Ruttimann</a>
 * @version $Id: ConsolidationCriteria.java 7205 2007-07-05 20:15:48Z rruttimann $
 */
public interface ConsolidationCriteria 
{
	/** Spring bean interface id */
	static final String INTERFACE_NAME = "com.groundwork.collage.model.ConsolidationCriteria";
	
	/** Hibernate component name that this entity service using */
	static final String COMPONENT_NAME = "com.groundwork.collage.model.impl.ConsolidationCriteria";
	
	/** Hibernate Property Constants **/
	static final String HP_ID = "consolidationCriteriaId";
	static final String HP_NAME = "name";
		
    Integer getConsolidationCriteriaId();

    void setConsolidationCriteriaId(Integer consolidationCriteriaId);

    String getName();

    void setName(String name);

    String getCriteria();

    void setCriteria(String criteria);
}