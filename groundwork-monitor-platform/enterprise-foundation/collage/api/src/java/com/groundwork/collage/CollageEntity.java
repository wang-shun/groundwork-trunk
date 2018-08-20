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
package com.groundwork.collage;

import java.util.Hashtable;
import java.util.List;

public interface CollageEntity 
{
	public void setType(String type);
	public String getType();
	public boolean hasProperties();	
	public Hashtable<String, String> getProperties();
	public void setProperties(Hashtable<String, String> properties);	
	public void addProperty(String key, String value);
	public String getProperty(String key);	
	public String removeProperty (String key);
	
	public List<CollageEntity> getSubEntities ();
	public void addSubEntity(CollageEntity entity);	
}
