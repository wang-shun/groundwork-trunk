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

public class StatisticProperty implements Serializable
{
    private java.lang.String name;

    private long count;

    public StatisticProperty() {
    }

    public StatisticProperty(
           java.lang.String name,
           long count) {
           this.name = name;
           this.count = count;
    }


    /**
     * Gets the name value for this StatisticProperty.
     * 
     * @return name
     */
    public java.lang.String getName() {
        return name;
    }


    /**
     * Sets the name value for this StatisticProperty.
     * 
     * @param name
     */
    public void setName(java.lang.String name) {
        this.name = name;
    }


    /**
     * Gets the count value for this StatisticProperty.
     * 
     * @return count
     */
    public long getCount() {
        return count;
    }


    /**
     * Sets the count value for this StatisticProperty.
     * 
     * @param count
     */
    public void setCount(long count) {
        this.count = count;
    }
    
	public String toString ()
	{
		StringBuilder sb = new StringBuilder(32);
		
		sb.append("Name: ");
		sb.append(name);
		sb.append(", Count: ");
		sb.append(count);

		return sb.toString();
	}    
}
