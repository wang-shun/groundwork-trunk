/**
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2006  GroundWork Open Source Solutions info@itgroundwork.com

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
package org.groundwork.foundation.profiling.exceptions;

public class UnknownWorkloadMessage extends ProfilerException 
{

	private static final String MSG_FORMAT = "Unrecognized message type [%1$s]";
	
	public UnknownWorkloadMessage(String messageName) {
		super(String.format(MSG_FORMAT, messageName));
	}

	public UnknownWorkloadMessage(String messageName, Throwable cause) 
	{
		super(String.format(MSG_FORMAT, messageName), cause);
	}
}
