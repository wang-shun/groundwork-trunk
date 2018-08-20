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

/*Created on: Feb 16, 2006 */

package com.groundwork.feeder.adapter;

import com.groundwork.collage.exception.CollageException;

public interface TopicServer {
	
	void initialize() throws CollageException;
	void initialize(String serverName, String topicName, int port)throws CollageException;
	
	void unInitialize()throws CollageException;
	
	// Setters/getters
	void 	setServerName(String serverName);
	String	getServerName();
	
	void 	setTopicName(String topicName);
	String	getTopicName();
	
	void 	setPort(int port);
	int		getPort();

	
}
