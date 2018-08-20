/*$Id: $
* Collage - The ultimate data integration framework.
*
* Copyright 2008 GroundWork Open Source, Inc. ("GroundWork")  
* All rights reserved. This program is free software; you can redistribute it
* and/or modify it under the terms of the GNU General Public License version 2
* as published by the Free Software Foundation.
*
* This program is distributed in the hope that it will be useful, but WITHOUT
* ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
* FOR A PARTICULAR PURPOSE.  See the GNU General Public License for 
* more details.
*
* You should have received a copy of the GNU General Public License along with
* this program; if not, write to the Free Software Foundation, Inc., 51 Franklin
* Street, Fifth Floor, Boston, MA 02110-1301, USA.
*
*/
package org.groundwork.foundation.jmx;

import java.io.IOException;

/**
 * 
 * @author Vong Tran (vong.tran@gmail.com)
 *
 */
public class JMXConfigurationException extends Exception {
	
	/**
	 * 
	 */
	public JMXConfigurationException(){
		super();
	}
	
	/**
	 * 
	 * @param msg
	 */
	public JMXConfigurationException(String msg){
		super(msg);
	}
	
	public JMXConfigurationException(String msg, Throwable cause){
		super(msg, cause);
	}
	
}
