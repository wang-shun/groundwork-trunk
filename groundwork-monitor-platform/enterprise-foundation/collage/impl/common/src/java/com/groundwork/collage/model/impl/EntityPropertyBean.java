/*
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2007  GroundWork Open Source Solutions info@groundworkopensource.com

 *	 This program is free software; you can redistribute it and/or modify
 *	 it under the terms of version 2 of the GNU General Public License
 *	 as published by the Free Software Foundation.

 *	 This program is distributed in the hope that it will be useful,
 *	 but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	 GNU General Public License for more details.

 *	 You should have received a copy of the GNU General Public License
 *	 along with this program; if not, write to the Free Software
 *	 Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package com.groundwork.collage.model.impl;

import com.groundwork.collage.model.ApplicationType;
import com.groundwork.collage.model.EntityType;
import com.groundwork.collage.model.PropertyType;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Represents the assignment of a specific {@link PropertyType} to an {@link
 * EntityType} (Host, ServiceStatus, etc...) in the context of an {@link ApplicationType} 
 * (NAGIOS, SYSLOG, JMX...) - this class is an artifact used to map the unique
 * ternary relationship between ApplicationType, EntityType and PropertyType;
 * it is not part of the published the object model, but is an implementation
 * artifact that is used from within the ApplicationType implementation to
 * store/persist the relationship to a ternary join table using hibernate
 *
 * @author <a href="mailto:philippe.paravicini@eCommerceStudio.com">Philippe Paravicini</a>
 */
public class EntityPropertyBean implements Comparable
{
	private EntityType      entityType;
	private PropertyType    propertyType;
	private int             sortOrder = 999;
	
	/** plain vanilla empty constructor */
	public EntityPropertyBean() {}

	public EntityPropertyBean(EntityType entityType, PropertyType propertyType)
	{
		this.entityType      = entityType;
		this.propertyType    = propertyType;
	}


	public EntityType getEntityType() {
		return this.entityType;
	}

	public void setEntityType(EntityType entityType) {
		this.entityType = entityType;
	}


	public PropertyType getPropertyType() {
		return this.propertyType;
	}

	public void setPropertyType(PropertyType propertyType) {
		this.propertyType = propertyType;
	}


	public int getSortOrder() {
		return this.sortOrder;
	}

	public void setSortOrder(int sortOrder) {
		this.sortOrder = sortOrder;
	}

	public String toString() 
	{
		return new ToStringBuilder(this)
			.append("ent", getEntityType())
			.append("prop", getPropertyType())
			.toString();
	}

	/** 
	 * returns true if the ApplicationType, EntityType, and PropertyType are all
	 * equal
	 */
	public boolean equals(Object other) 
	{
		if ( (this == other ) ) return true;
		if ( !(other instanceof EntityPropertyBean) ) return false;
		EntityPropertyBean castOther = (EntityPropertyBean) other;
		return new EqualsBuilder()
			.append(this.getEntityType(),   castOther.getEntityType())
			.append(this.getPropertyType(), castOther.getPropertyType())
			.isEquals();
	}

	public int hashCode() 
	{
		return new HashCodeBuilder()
			.append(getEntityType())
			.append(getPropertyType())
			.toHashCode();
	}

    @Override
    public int compareTo(Object o) {
        EntityPropertyBean other = (EntityPropertyBean)o;
        String myKey = this.getEntityType().getName() + this.getPropertyType().getName();
        String otherKey = other.getEntityType().getName() + other.getPropertyType().getName();
        return myKey.compareTo(otherKey);
    }
}
