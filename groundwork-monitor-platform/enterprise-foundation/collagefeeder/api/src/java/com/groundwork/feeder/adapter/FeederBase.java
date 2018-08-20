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

package com.groundwork.feeder.adapter;

import com.groundwork.feeder.adapter.impl.FoundationMessage;

/**
 * 
 * FeederBase
 * @author <a href="mailto:rruttimann@itgroundwork.com"> Roger Ruttimann</a>
 * @version $Id: FeederBase.java 7205 2007-07-05 20:15:48Z rruttimann $
 * 
 * All classes implementing an adapter for the collage database derive from this base class
 */
public interface FeederBase {
    
    String getName();
    
    /** process()
     * @param beanFactory factory for getting implementation for interfaces. The value might be null of now bean factory is used*/
    void	process(Object beanFactory, FoundationMessage message);
    
    void	initialize();
    void	uninitialize();

}
