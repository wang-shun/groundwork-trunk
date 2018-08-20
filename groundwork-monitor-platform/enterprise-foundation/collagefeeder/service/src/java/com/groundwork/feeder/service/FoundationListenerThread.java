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

/*Created on: Mar 30, 2006 */

package com.groundwork.feeder.service;

/**
 * FoundationListenerThread class the simply adds the uninitialize method
 * that needs to be implemented for any thread since stop() is 
 * unsafe and shouldn't be used.
 */

public class FoundationListenerThread extends Thread {
	
	
    	public void unInitialize()
    	{
    		
    	}
}
