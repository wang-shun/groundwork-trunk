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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.groundwork.collage.model.ApplicationType;
import com.groundwork.collage.model.EntityType;
import com.groundwork.collage.model.PropertyType;

public class ApplicationEntityProperty implements Serializable, com.groundwork.collage.model.ApplicationEntityProperty
{
    private static final long serialVersionUID = 1;
    
    private Integer _applicationEntityPropertyId = null;
    
	private ApplicationType _appType = null;
	private EntityType _entityType = null;
	private PropertyType _propertyType = null;
	private Integer _sortOrder = 999;

	/**
	 * Default Constructor
	 *
	 */
	public ApplicationEntityProperty ()
	{		
	}
	
	public ApplicationEntityProperty (ApplicationType appType, EntityType entityType, PropertyType propertyType)
	{
		_appType = appType;
		_entityType = entityType;
		_propertyType = propertyType;
	}
	
	public Integer getApplicationEntityPropertyId ()
	{
		return _applicationEntityPropertyId;
	}
	
	public void setApplicationEntityPropertyId (Integer id)
	{
		_applicationEntityPropertyId = id;
	}
	
	public ApplicationType getApplicationType()
	{	
		return _appType;
	}

	public void setApplicationType(ApplicationType appType)
	{	
		_appType = appType;
	}
	
	public EntityType getEntityType()
	{
		return _entityType;
	}
	
	public void setEntityType(EntityType entityType)
	{	
		_entityType = entityType;
	}

	public PropertyType getPropertyType()
	{
		return _propertyType;
	}
	
	public void setPropertyType(PropertyType propertyType)
	{	
		_propertyType = propertyType;
	}

	public Integer getSortOrder()
	{
		return _sortOrder;
	}
	
	public void setSortOrder(Integer sortOrder)
	{	
		_sortOrder = sortOrder;
	}

	/** appends applTypeId and name */
	public String toString() 
	{
		return new ToStringBuilder(this)
			.append("id", getApplicationEntityPropertyId())
			.append("app type", getApplicationType())
			.append("entity type", getEntityType())
			.append("property type", getPropertyType())
			.append("sort order", getSortOrder())
			.toString();
	}

	/** 
	 * two instances are equal if their names are equals - 
	 * this is a draconian concept of equality which stresses that the name
	 * of an ApplicationType should be unique across the application
	 */
	public boolean equals(Object other) 
	{
		if ( (this == other ) ) return true;
		if ( !(other instanceof ApplicationEntityProperty) ) return false;
		ApplicationEntityProperty castOther = (ApplicationEntityProperty) other;
		
		return new EqualsBuilder()
			.append(this.getApplicationEntityPropertyId(), castOther.getApplicationEntityPropertyId())
			.append(this.getApplicationType(), castOther.getApplicationType())
			.append(this.getEntityType(), castOther.getEntityType())
			.append(this.getPropertyType(), castOther.getPropertyType())
			.append(this.getSortOrder(), castOther.getSortOrder())
			.isEquals();
	}

	public int hashCode() 
	{
		return new HashCodeBuilder()
			.append(this.getApplicationEntityPropertyId())
			.append(this.getApplicationType())
			.append(this.getEntityType())
			.append(this.getPropertyType())
			.append(this.getSortOrder())
			.toHashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(com.groundwork.collage.model.ApplicationEntityProperty o)
	{
		if (o == null)
			throw new IllegalArgumentException("Unable to compare null ApplicationEntityProperty");
		
		PropertyType comparePropType = o.getPropertyType();
		
		if (_propertyType == null && comparePropType == null)
			return 0;
		
		if (_propertyType == null)
			return -1;
		
		if (comparePropType == null)
			return 1;
		
		// Compare ignoring case
		return _propertyType.getName().compareToIgnoreCase(comparePropType.getName());
	}	
}
