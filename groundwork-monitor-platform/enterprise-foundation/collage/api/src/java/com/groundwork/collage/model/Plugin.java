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

import javax.servlet.http.HttpServletRequest;
import java.util.Date;


/**
 * Plugin
 * 
 * @version $Id: PLugin.java 17917 2010-08-25 23:00:48Z ashanmugam $
 */

public interface Plugin extends AttributeData {
	
	  /** the name that identifies this entity in the system: "PLUGIN" */
    static final String ENTITY_TYPE_CODE = "PLUGIN";

	/** Spring bean interface id */
	static final String INTERFACE_NAME = "com.groundwork.collage.model.Plugin";

	/** Hibernate component name that this entity service using */
	static final String COMPONENT_NAME = "com.groundwork.collage.model.impl.Plugin";
	
	static final String HP_ID = "pluginId";
	static final String HP_NAME = "name";
	static final String HP_DESCRIPTION = "description";

	/** Entity Property Constants */
	static final String EP_ID = "PluginId";
	static final String EP_NAME = "Name";
	static final String EP_DESCRIPTION = "Description";
	
	public Integer getPluginId();
	
	public String getName();

	public String getUrl();

	public void setUrl(String url);

	public String getExternalUrl(HttpServletRequest request);

	public String getDependencies();

	public void setDependencies(String dependencies);

	public Date getLastUpdateTimestamp();

	public void setLastUpdateTimestamp(Date lastUpdateDate);

	public PluginPlatform getPluginPlatform();

	public void setPluginPlatform(PluginPlatform pluginPlatform);
	
	public String getChecksum() ;

	public void setChecksum(String checksum) ;

	public String getLastUpdatedBy() ;

	public void setLastUpdatedBy(String lastUpdatedBy) ;

}
