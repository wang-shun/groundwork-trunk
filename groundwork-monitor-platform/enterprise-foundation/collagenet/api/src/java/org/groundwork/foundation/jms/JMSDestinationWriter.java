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

import javax.jms.TextMessage;

public interface JMSDestinationWriter  {
	/** Initialize Queue Writer */
	void initialize(JMSDestinationInfo queueInfo) throws FoundationJMSException;
	void unInitialize() throws FoundationJMSException;

    /**
     * First uninitialize, then initialize
     * This method does not throw exceptions and is meant for retry situations.
     * @return true if successfully initialized, false if failed initialization
     */
    boolean reInitialize(JMSDestinationInfo queueInfo);

	/** Writes a message to the message destination (queue or topic). Commit each write*/
	TextMessage writeDestination(String msg) throws FoundationJMSException;

    TextMessage writeMessageWithProperty(String msg, String propName, String propValue) throws FoundationJMSException;

	/**
     *
	 * Commits any pending messages in the current session to the queue.
	 * Call write queue prior to commit.
	 * @throws FoundationJMSException
	 */
	void commit() throws FoundationJMSException;
}
