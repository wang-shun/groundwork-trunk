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

import java.util.Properties;
import com.groundwork.collage.exception.CollageException;
import com.groundwork.collage.model.PropertyType;

/**
 * Retrieves instances of Collage API components.
 * 
 * @author <a href=String mailto:dtaylor@itgroundwork.comString >David Sean Taylor</a>
 * @version $Id: CollageAccessor.java 6397 2007-04-02 21:27:40Z glee $
 * 
 */
public interface CollageAccessor 
{
    /**
     * Gets an instance of an API component.  
     * 
     * @param interfaceName  Name of the component to get.  If the component is 
     * not found, null is returned.
     * @return Instance of component requested.
     * @throws CollageException
     */
    public Object getAPIObject(String interfaceName) throws CollageException;	

    /** returns a Properties with general Foundation configuration values */
    public Properties getFoundationProperties();

    /** 
     * When updating the state of various entities, if the system encounters
     * any {@link PropertyType} names that have not been defined in the
     * metadata, this flag indicates whether the system should automatically
     * create and assign the PropertyType; this flag is false by default,
     * unless explicitly enabled in the configuration; in other words, by
     * default the system will complain if it encounters a PropertyType that
     * has not been properly defined
     */
    boolean isAutoCreateUnknownProperties();
    
    /**
     * load a given spring assembly into an existing Bean factory or creates
     * a new factory if it doesn't exists.
     * @param assemblyPath	Path to spring assembly. Must be in classpath usually in tha package META-INF directory
     * @throws CollageException
     */
    void loadSpringAssembly(String assemblyPath) throws CollageException;
    
    /**
     * Retrieves a QueryObjectWrapper object from an internal query object store
     * @param sessionObjectID
     * @return QueryObjectWrapper that encapsulates a hibernateSession
     */
    QueryObjectWrapper getQuerySessionObjectByID(int sessionObjectID);
    
    /**
     * Adds a given object to the Query Object store
     * @param sessionObject QueryObjectWrapper that encapsulates a hibernateSession
     * @return
     */
    int setQuerySessionObject(QueryObjectWrapper sessionObject);
    
    /**
     * remove query session from map
     * @param SessionObjectID
     */
    void removeQuerySessionObject(int SessionObjectID);
    
}
