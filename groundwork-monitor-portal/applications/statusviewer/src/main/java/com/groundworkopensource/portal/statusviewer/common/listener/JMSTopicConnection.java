/*
 * StatusViewer - The ultimate gwportal framework. Copyright (C) 2004-2009
 * GroundWork Open Source Solutions info@groundworkopensource.com
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of version 2 of the GNU General Public License as published by the
 * Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
 */
package com.groundworkopensource.portal.statusviewer.common.listener;

import com.groundworkopensource.portal.common.ApplicationType;
import com.groundworkopensource.portal.common.PropertyUtils;
import org.apache.log4j.Logger;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.Topic;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Properties;

/**
 * JMS Listener/Subscriber subscribes to the topic server.
 * 
 * @author Arul
 */
public class JMSTopicConnection {

    /** JMS connection. */
    private Connection connection = null;

    /** JNDI Context. */
    private Context jndi = null;

    // Constants for properties to be accessed from configuration file.
    /** factory initial. */
    private static final String PROP_FACTORY_INITIAL = "java.naming.factory.initial";

    /** Provider URL. */
    private static final String PROP_PROVIDER_URL = "java.naming.provider.url";


    /** connection factory. */
    private static final String PROP_CON_FACTORY = "topic.connection.factory";
    
    /**
     * PRINCIPAL.
     */
    private static final String PROP_PRINCIPAL = "java.naming.security.principal";
    
    /**
     * CREDENTIALS.
     */
    private static final String PROP_CREDENTIALS = "java.naming.security.credentials";

    /** Logger. */
    private static final Logger LOGGER = Logger
            .getLogger(JMSTopicConnection.class.getName());

    /**
     * Default Constructor.
     */
    public JMSTopicConnection() {
        initJMS();
    }

    /**
     * Initializes the JMS connections. Subscribes the datatableBean to the
     * topic
     */
    private void initJMS() {
        LOGGER.info("Init JMS .....factory Initial "
                + PropertyUtils.getProperty(ApplicationType.STATUS_VIEWER,
                        PROP_FACTORY_INITIAL));
        try {
        	String jmsUser = PropertyUtils.getProperty(
                    ApplicationType.STATUS_VIEWER, PROP_PRINCIPAL);
              
            String jmsPassword = PropertyUtils.getProperty(
                    ApplicationType.STATUS_VIEWER, PROP_CREDENTIALS);
        	// Obtain a JNDI connection
            Properties env = new Properties();
            // ... specify the JNDI properties specific to the vendor
            env.put(PROP_FACTORY_INITIAL, PropertyUtils.getProperty(
                    ApplicationType.STATUS_VIEWER, PROP_FACTORY_INITIAL));
            env.put(PROP_PROVIDER_URL, PropertyUtils.getProperty(
                    ApplicationType.STATUS_VIEWER, PROP_PROVIDER_URL));
            env.put(PROP_PRINCIPAL, PropertyUtils.getProperty(
                    ApplicationType.STATUS_VIEWER, PROP_PRINCIPAL));
            env.put(PROP_CREDENTIALS, PropertyUtils.getProperty(
                    ApplicationType.STATUS_VIEWER, PROP_CREDENTIALS));
            jndi = new InitialContext(env);

            // Look up a JMS connection factory
            ConnectionFactory conFactory = (ConnectionFactory) jndi
                    .lookup(PropertyUtils.getProperty(
                            ApplicationType.STATUS_VIEWER, PROP_CON_FACTORY));

            
          
            // Create a JMS connection
            connection = conFactory.createConnection(jmsUser,jmsPassword);
            connection.start();
            // Now create 3 listeners(one for entity and other for events)
            this.createListener("topic.name");
            this.createListener("event.topic.name");
            this.createListener("ui.events.topic.name");
            this.createListener("performance.topic.name");

            LOGGER.info("Subscribed to entity topic successfully..");

        } catch (Exception e) {
            LOGGER.error("Error occured while initializing the JMS connection-"
                    + e.getMessage());
        }
    }

    /**
     * Creates a listener for the supplied topic.
     * 
     * @param topicName
     *            the topic name
     * 
     * @throws NamingException
     *             the naming exception
     * @throws JMSException
     *             the JMS exception
     */
    private void createListener(String topicName) throws NamingException,
            JMSException {
        JMSMessageListener listener = new JMSMessageListener();
        Session session = connection.createSession(false,
                Session.CLIENT_ACKNOWLEDGE);

        // Look up a JMS topic
        Topic topic = (Topic) jndi.lookup(PropertyUtils.getProperty(
                ApplicationType.STATUS_VIEWER, topicName));
        // LOGGER
        // .error("!!!!!!!!!!!!!!!! successfully created listener for topic ["
        // + topicName + "] " + topic.getTopicName());

        // Message subscriber
        MessageConsumer subscriber = session.createConsumer(topic);
        // Set a JMS message listener
        subscriber.setMessageListener(listener);

    }

    /**
     * Gets the connection.
     * 
     * @return the connection
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * Gets the jndi.
     * 
     * @return the jndi
     */
    public Context getJndi() {
        return jndi;
    }

}
