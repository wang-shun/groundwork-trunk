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

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.groundwork.collage.model.ApplicationType;
import com.groundwork.collage.model.PropertyExtensible;
import com.groundwork.collage.model.PropertyType;

/**
 * 
 * 
 * @author  <a href="mailto:philippe.paravicini@eCommerceStudio.com">Philippe Paravicini</a>
 * @version $Revision: 13403 $ - $Date: 2008-10-16 10:02:32 -0700 (Thu, 16 Oct 2008) $
 *
 */
public class EntityType implements com.groundwork.collage.model.EntityType, Cloneable
{
	Log log = LogFactory.getLog(this.getClass());

	private Integer entityTypeId;
	private String  name;
	private String  description;
	private Boolean isLogicalEntity;
	private Boolean applicationTypeSupported;
	
	private ApplicationType applicationType;
	private Map propertyTypes;


	public Integer getEntityTypeId() 
	{
		if (this.entityTypeId == null)
			this.entityTypeId = new Integer(-1);

		return this.entityTypeId;
	}

	public void setEntityTypeId(Integer entityTypeId) {
		this.entityTypeId = entityTypeId;
	}


	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}


	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

/*
	public ApplicationType getApplicationType() {
		return this.applicationType;
	}

	public void setApplicationType(ApplicationType applicationType) {
		this.applicationType = applicationType;
	}
*/

	public Map getPropertyTypes() {
		if (this.propertyTypes == null)
			this.propertyTypes = new TreeMap();

		return this.propertyTypes;
	}

	public PropertyType getPropertyType(String name) {
		return (PropertyType)this.getPropertyTypes().get(name);
	}

	public void mapPropertyType(PropertyType propertyType) 
	{
		if (propertyType != null) {
			this.getPropertyTypes().put(propertyType.getName(), propertyType);
		}
		else
			log.warn("attempting to add null PropertyType! - not added");
	}
	
	public List<PropertyType> getBuiltInProperties ()
	{
		try {
			Class cls = Class.forName(description);
		
			PropertyExtensible propExtensiable = (PropertyExtensible)cls.newInstance();
			
			return propExtensiable.getBuiltInProperties();
						
		}
		catch (ClassNotFoundException cnfe)
		{
			log.warn("Unable to retrieve built in properties for entity - " + name + ", class: " + description, cnfe);
		}
		catch (IllegalAccessException iae)
		{
			log.warn("Unable to instantiate entity class. No public constructor - " + description, iae);
		}
		catch (InstantiationException ie)
		{
			log.warn("Unable to instantiate entity class - " + description, ie);
		}
		
		return null;		
	}

	public List<PropertyType> getComponentProperties ()
	{
		try {
			Class cls = Class.forName(description);
		
			PropertyExtensible propExtensiable = (PropertyExtensible)cls.newInstance();
			
			return propExtensiable.getComponentProperties();						
		}
		catch (ClassNotFoundException cnfe)
		{
			log.warn("Unable to retrieve component properties for entity - " + name + ", class: " + description, cnfe);
		}
		catch (IllegalAccessException iae)
		{
			log.warn("Unable to instantiate entity class. No public constructor - " + description, iae);
		}
		catch (InstantiationException ie)
		{
			log.warn("Unable to instantiate entity class - " + description, ie);
		}
		
		return null;		
	}
	
	/*
	 * A logical entity is one that is not persisted (i.e. statistic entities)
	 * (non-Javadoc)
	 * @see com.groundwork.collage.model.EntityType#isLogicalEntity()
	 */
	public void setLogicalEntity (Boolean isLogicalEntity)
	{
		this.isLogicalEntity = isLogicalEntity;
	}
	
	public Boolean getLogicalEntity ()
	{
		return this.isLogicalEntity;
	}
	
	public String toString() 
	{
		return new ToStringBuilder(this)
			.append("id", getEntityTypeId())
			.append("name", getName())
			.append("props", getPropertyTypes())
			.toString();
	}

	public boolean equals(Object other) 
	{
		if ( (this == other ) ) return true;
		if ( !(other instanceof EntityType) ) return false;
		EntityType castOther = (EntityType) other;
		return new EqualsBuilder()
			.append(this.getName(), castOther.getName())
			.isEquals();
	}

	public int hashCode() 
	{
		return new HashCodeBuilder()
			.append(getName())
			.toHashCode();
	}

	public Object clone() throws CloneNotSupportedException { return super.clone(); }

	public Boolean getApplicationTypeSupported() {
		return applicationTypeSupported;
	}

	public void setApplicationTypeSupported(Boolean applicationTypeSupported) {
		this.applicationTypeSupported = applicationTypeSupported;
	}

}
