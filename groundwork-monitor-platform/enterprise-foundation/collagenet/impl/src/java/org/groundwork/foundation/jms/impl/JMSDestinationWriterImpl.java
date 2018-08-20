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

package org.groundwork.foundation.jms.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.jms.FoundationJMSException;
import org.groundwork.foundation.jms.JMSDestinationInfo;
import org.groundwork.foundation.jms.JMSDestinationWriter;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Hashtable;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author rogerrut
 * 
 * Created: Apr 10, 2007
 */
public class JMSDestinationWriterImpl implements JMSDestinationWriter {
	/** Enable log4j for JMSServer class */
	private Log log = LogFactory.getLog(this.getClass());

	private AtomicBoolean initialized = new AtomicBoolean(false);

	private ConnectionFactory cnxF = null;
	private Destination dest = null;
	private Connection cnx = null;
	private Session session = null;

	private MessageProducer messageProducer = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.groundwork.foundation.jms.JMSDestinationWriter#initialize(org.groundwork.foundation.jms.JMSDestinationInfo)
	 */
	public void initialize(JMSDestinationInfo destInfo)
			throws FoundationJMSException {
		if (destInfo == null)
			throw new FoundationJMSException(
					"JMSDestinationInfo can't be null. DestinationReader can't be initialized");

		// Connect to destination
		Context ictx = null;
		try {
			Hashtable<String, String> htJndiProperties = null;
			if (destInfo.getContextFactory() != null
					&& destInfo.getHost() != null
					&& destInfo.getPort() != null) {
				htJndiProperties = new Hashtable<String, String>(3);
				htJndiProperties.put("java.naming.factory.initial", destInfo.getContextFactory());
				htJndiProperties.put("java.naming.provider.url", "remote://" + destInfo.getHost()
						+ ":" + destInfo.getPort());
			}

			ictx = new InitialContext(htJndiProperties);
			cnxF = (ConnectionFactory) ictx
					.lookup(destInfo.getServerContext());
			dest = (Destination) ictx.lookup(destInfo.getDestinationName());

			if (dest == null) {
				throw new FoundationJMSException(
						"No destination object found. Destination Name["
								+ destInfo.getDestinationName() + "]");
			}

			cnx = cnxF.createConnection(destInfo.getAdminUser(), destInfo.getAdminCredentials());
			session = cnx.createSession(true, 0);
			messageProducer = session.createProducer(dest);
		} catch (NamingException ne) {
			throw new FoundationJMSException("Couldn't create Context "
					+ destInfo.getServerContext() + " and destination "
					+ destInfo.getDestinationName(), ne);
		} catch (JMSException jmse) {
			throw new FoundationJMSException(
					"No destination object found. Destination Name["
							+ destInfo.getDestinationName() + "]", jmse);
		} finally {
			if (ictx != null) {
				try {
					ictx.close();
				} catch (Exception e) {
					log.error(e);
				}
			}
		}

		/* success */
		this.initialized.set(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.groundwork.foundation.jms.JMSDestinationWriter#unInitialize()
	 */
	public void unInitialize() throws FoundationJMSException {
		this.initialized.set(false);
		try {
			if (this.messageProducer != null)
				this.messageProducer.close();
			
			if (this.session != null)
				this.session.close();
			
			if (this.cnx != null)
				this.cnx.close();
		} catch (JMSException je) {
			throw new FoundationJMSException("Failed to close DestinationWriter.", je);
		}
	}

    public boolean reInitialize(JMSDestinationInfo destInfo) {
        try {
            unInitialize();
        }
        catch (Exception e) {
            log.warn("Failed to un initialize " + destInfo.getDestinationName());
        }
        try {
            initialize(destInfo);
        }
        catch (Exception e) {
            log.error("Failed to initialize " + destInfo.getDestinationName(), e);
            return false;
        }
        return true;
    }

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.groundwork.foundation.jms.JMSDestinationWriter#writeDestination(javax.jms.TextMessage)
	 */
	public TextMessage writeDestination(String msg) throws FoundationJMSException {
		if (this.initialized.get() == false)
			throw new FoundationJMSException(
					"Call initialize() before calling any other method");

		try {
			TextMessage message = session.createTextMessage();
			message.setText(msg);
			messageProducer.send(message);
			return message;
		} catch (JMSException je) {
			throw new FoundationJMSException("Failed adding message [" + msg
					+ " to destination.", je);
		}
	}

    public TextMessage writeMessageWithProperty(String msg, String propName, String propValue) throws FoundationJMSException {
        if (this.initialized.get() == false)
            throw new FoundationJMSException(
                    "Call initialize() before calling any other method");

        try {
            TextMessage message = session.createTextMessage();
            message.setStringProperty(propName, propValue);
            message.setText(msg);
            messageProducer.send(message);
			return message;
        } catch (JMSException je) {
            throw new FoundationJMSException("Failed adding message [" + msg
                    + " to destination.", je);
        }
    }

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.groundwork.foundation.jms.JMSDestinationWriter#commit()
	 */
	public void commit() throws FoundationJMSException {
		if (this.initialized.get() == false)
			throw new FoundationJMSException(
					"Call initialize() before calling any other method");

		try {
			this.session.commit();
		} catch (JMSException je) {
            // javax.jms.IllegalStateException
			throw new FoundationJMSException("Failed commit messages.", je);
		}
	}

}
