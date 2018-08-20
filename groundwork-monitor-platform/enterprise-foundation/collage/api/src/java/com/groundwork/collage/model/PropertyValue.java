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

import java.util.Date;

public interface PropertyValue 
{
	/**
	 * Convience method to return value depending on property type.
	 * @return
	 */
	public Object getValue();

	/**
	 * This property accepts values that are of the proper primitive wrapper
	 * (Date, Boolean, String, Integer, Long, Double), or are Strings and can
	 * be properly parsed by {@link #getPropertyType this.getPropertyType}
	 */
	public void setValue(Object o);

    public Integer getPropertyTypeId ();    
    public void setPropertyTypeId (Integer propertyTypeId);
    
    public String getName ();    
    public void setName (String name);
    
    public String getValueString ();
    public void setValueString (String val);
    
    public Date getValueDate ();
    public void setValueDate (Date val);
    
    public Boolean getValueBoolean ();
    public void setValueBoolean (Boolean val);

    public Integer getValueInteger ();
    public void setValueInteger (Integer val);
        
    public Long getValueLong ();
    public void setValueLong (Long val);
    
    public Double getValueDouble ();
    public void setValueDouble (Double val);
    
    public Date getCreatedOn ();
    public void setCreatedOn (Date createdOn);
    
    public Date getLastEditedOn ();
    public void setLastEditedOn (Date editedOn);

	/** 
	 * retrieves the PropertyType from cached metadata using the PropertyTypeId
	 * stored in the database
	 */
	public PropertyType getPropertyType();

	/** 
	 * stores the propertyType id of the propertyType passed
	 */
	public void setPropertyType(PropertyType propertyType);
}
