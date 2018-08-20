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

import com.groundwork.collage.model.PropertyType;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/** @author Hibernate CodeGenerator */
public class Severity extends PropertyExtensibleAbstract implements Serializable, com.groundwork.collage.model.Severity {

	private static final long serialVersionUID = 1;
	
	/* Built-In (Non-dynamic) Properties - The Properties which have a description (hibernate property)
	 * defined are properties that can be used to query the entity.
	 * We distinquish between "filterable" properties and properties that are returned in 
	 * property maps.
	 */	
	private static final PropertyType PROP_ID = 
		new com.groundwork.collage.model.impl.PropertyType(				
								EP_ID,
								HP_ID, // Description is hibernate property name
								PropertyType.DataType.INTEGER, 
								Severity.ENTITY_TYPE_CODE,
								true);		
	
	private static final PropertyType PROP_NAME = 
		new com.groundwork.collage.model.impl.PropertyType(				
								EP_NAME,
								HP_NAME, // Description is hibernate property name
								PropertyType.DataType.STRING, 
								Severity.ENTITY_TYPE_CODE,
								true);	

	private static final PropertyType PROP_DESCRIPTION = 
		new com.groundwork.collage.model.impl.PropertyType(				
								EP_DESCRIPTION,
								HP_DESCRIPTION, // Description is hibernate property name
								PropertyType.DataType.STRING, 
								Severity.ENTITY_TYPE_CODE,
								true);	
	
    /** Built-In property list - For now its static - Once app type properties are supported we may have
     * to have an instance variable **/
	private static List<PropertyType> BUILT_IN_PROPERTIES = null;
    
    /** identifier field */
    private Integer severityId;

    /** persistent field */
    private String name;

    /** nullable persistent field */
    private String description;

    /** full constructor */
    public Severity(Integer severityId, String name, String description)
    {
        this.severityId = severityId;
        this.name = name;
        this.description = description;
    }

    /** default constructor */
    public Severity() {
    }

    /** minimal constructor */
    public Severity(Integer severityId, String name)
    {
        this.severityId = severityId;
        this.name = name;
    }

    public Integer getSeverityId() {
        return this.severityId;
    }

    public void setSeverityId(Integer severityId) {
        this.severityId = severityId;
    }

    public Integer getID ()
	{
		return getSeverityId();
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

    public String toString() {
        return new ToStringBuilder(this)
            .append("severityId", getSeverityId())
            .append("value", getName())
            .toString();
    }

    /**
     * Two Severitys are equal if they share the same name and numeric Id 
     */
    public boolean equals(Object other) 
    {
        if ( (this == other ) ) return true;
        if ( !(other instanceof Severity) ) return false;
        Severity castOther = (Severity) other;
        return new EqualsBuilder()
            .append(this.getSeverityId(), castOther.getSeverityId())
            .append(this.getName(), castOther.getName())
            .isEquals();
    }

    public int hashCode() 
    {
        return new HashCodeBuilder()
            .append(getSeverityId())
            .append(getName())
            .toHashCode();
    }
    
    /** 
     * This method overrides the property by the same name in
     * PropertyExtensibleAbstract to make it possible to get the value of one
     * of the named property getters 
     *
     * @throws IllegalArgumentException 
     *
     *  if unable to find PropertyType with the key provided, or if the key
     *  does not corresponding to one of the declared get/sets
     */
    public Object getProperty(String key) throws IllegalArgumentException
    {
    	if (key == null || key.length() == 0)
    	{
    		throw new IllegalArgumentException("Invalid null / empty property key.");
    	}
    	
        if (key.equalsIgnoreCase(EP_ID)) {
            return this.getID();
        }
        else if (key.equalsIgnoreCase(EP_NAME)) {
            return this.getName();
        }
        else if (key.equalsIgnoreCase(EP_DESCRIPTION)) {
            return this.getDescription();
        } 	
        else {
            return super.getProperty(key);
        }
    }       

	/* (non-Javadoc)
	 * @see com.groundwork.collage.model.impl.PropertyExtensibleAbstract#getBuiltInProperties()
	 */
	@Override
	public List<PropertyType> getBuiltInProperties()
	{
		if (BUILT_IN_PROPERTIES != null)
			return BUILT_IN_PROPERTIES;
			
		BUILT_IN_PROPERTIES = new ArrayList<PropertyType>(3);
		
		// Add non-volatile properties		
		BUILT_IN_PROPERTIES.add(PROP_ID);
		BUILT_IN_PROPERTIES.add(PROP_NAME);
		BUILT_IN_PROPERTIES.add(PROP_DESCRIPTION);		
		
		return BUILT_IN_PROPERTIES;
	}

	/* (non-Javadoc)
	 * @see com.groundwork.collage.model.impl.PropertyExtensibleAbstract#getEntityTypeCode()
	 */
	@Override
	public String getEntityTypeCode()
	{
		return ENTITY_TYPE_CODE;
	}

	/* (non-Javadoc)
	 * @see com.groundwork.collage.model.impl.PropertyExtensibleAbstract#getPropertyValueInstance(java.lang.String, java.lang.Object)
	 */
	@Override
	public PropertyValue getPropertyValueInstance(String name, Object value)
	{
		return new EntityPropertyValue(severityId, getEntityTypeId(), name, value);
	}

	/* (non-Javadoc)
	 * @see com.groundwork.collage.model.PropertyExtensible#getComponentProperties()
	 */
	public List<PropertyType> getComponentProperties()
	{
		// Filterable properties are the same as the built-in properties
		return getBuiltInProperties();
	}    
}
