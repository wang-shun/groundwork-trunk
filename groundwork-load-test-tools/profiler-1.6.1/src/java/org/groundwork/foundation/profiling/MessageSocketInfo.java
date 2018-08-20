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
package org.groundwork.foundation.profiling;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import org.groundwork.foundation.profiling.exceptions.ProfilerException;

public class MessageSocketInfo 
{
	private static final String DEFAULT_SERVER = "localhost";
	private static final int DEFAULT_PORT = 4913;
	
	// Server name or IP where to post messages
	private String _server = DEFAULT_SERVER;
	
	// Port where to post messages
	private int _port = DEFAULT_PORT;
	
	public MessageSocketInfo (String server, int port)
	{
		if (server != null && server.length() > 0)
		{
			_server = server;
		}
		
		if (port > 0)
		{
			_port = port;
		}
	}
	
	public String getServer ()
	{
		return _server;
	}
	
	public int getPort ()
	{
		return _port;
	}	

	public String toString()
	{
		StringBuilder sb = new StringBuilder(32);
		sb.append("Server: ");
		sb.append(_server);
		sb.append(", ");
		
		sb.append("Port: ");
		sb.append(_port);
		
		return sb.toString();
	}	
	
	/**
	 * Create socket with information MessageSocketInfo parameter.
	 * @param messageSocketInfo
	 * @return
	 * @throws ProfilerException
	 */
	public Socket createSocket() throws ProfilerException
	{	
		InetAddress address = null;
		try {
		    address = InetAddress.getByName(this.getServer());
		} 
		catch(UnknownHostException e) 
		{
			throw new ProfilerException("Socket host not found - " + this, e);
		}

		Socket socket = null;
		try {
		    socket = new Socket(address, this.getPort());		   		    
		} catch(IOException e) {
			throw new ProfilerException("Error occurred creating message socket - " + this, e);
		}	
		
		return socket;
	}	
}

