/*
* Collage - The ultimate data integration framework.
*
* Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")  
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

package org.groundwork.foundation.jms;

/**
 * @author rogerrut
 *
 * Created: Apr 5, 2007
 */
public interface JMSServer {
	void initialize(JMSServerInfo serverInfo) throws FoundationJMSException;
	void unInitialize() throws FoundationJMSException;
	
	void restartJMSServer(JMSServerInfo serverInfo) throws FoundationJMSException;
	
	void addQueue(JMSDestinationInfo queueInfo) throws FoundationJMSException;
	void removeQueue(JMSDestinationInfo queueInfo) throws FoundationJMSException;
	
	void addTopic(JMSDestinationInfo topicInfo) throws FoundationJMSException;
	void removeTopic(JMSDestinationInfo topicInfo) throws FoundationJMSException;
}
