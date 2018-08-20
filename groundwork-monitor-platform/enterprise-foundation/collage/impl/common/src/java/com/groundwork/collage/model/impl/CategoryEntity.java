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

import com.groundwork.collage.model.Category;
import com.groundwork.collage.model.EntityType;


public class CategoryEntity implements Serializable,
		com.groundwork.collage.model.CategoryEntity {
	
	private static final long serialVersionUID = 1;
	
	private Integer		objectID;
	private Integer		categoryEntityID;	// Primary Key
	private EntityType	entityType;
	private Category	category;

	public CategoryEntity() {}
	
	public CategoryEntity(Integer objectID) {
		this.objectID = objectID;
	}
	/* (non-Javadoc)
	 * @see com.groundwork.collage.model.CategoryEntity#getObjectID()
	 */
	public Integer getObjectID() {
		return this.objectID;
	}

	/* (non-Javadoc)
	 * @see com.groundwork.collage.model.CategoryEntity#setObjectID(java.lang.Integer)
	 */
	public void setObjectID(Integer objectID) {
		this.objectID = objectID;

	}
	
	/* (non-Javadoc)
	 * @see com.groundwork.collage.model.CategoryEntity#getObjectID()
	 */
	public Integer getCategoryEntityID() {
		return this.categoryEntityID;
	}

	/* (non-Javadoc)
	 * @see com.groundwork.collage.model.CategoryEntity#setObjectID(java.lang.Integer)
	 */
	public void setCategoryEntityID(Integer categoryEntityID) {
		this.categoryEntityID = categoryEntityID;

	}

	/* (non-Javadoc)
	 * @see com.groundwork.collage.model.CategoryEntity#getEntityType()
	 */
	public EntityType getEntityType() {
		return this.entityType;
	}

	/* (non-Javadoc)
	 * @see com.groundwork.collage.model.CategoryEntity#setEntityType(com.groundwork.collage.model.EntityType)
	 */
	public void setEntityType(EntityType entityType) {
		this.entityType = entityType;

	}

	/* (non-Javadoc)
	 * @see com.groundwork.collage.model.CategoryEntity#getCategory()
	 */
	public Category getCategory() {
		return this.category;
	}

	/* (non-Javadoc)
	 * @see com.groundwork.collage.model.CategoryEntity#setCategory(com.groundwork.collage.model.Category)
	 */
	public void setCategory(Category category) {
		this.category = category;
	}

}
