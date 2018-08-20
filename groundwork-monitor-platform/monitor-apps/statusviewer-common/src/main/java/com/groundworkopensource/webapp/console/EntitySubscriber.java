/*
 *  Copyright (C) 2009 GroundWork Open Source, Inc. (GroundWork)
 *  All rights reserved. Use is subject to GroundWork commercial license terms.
 */

package com.groundworkopensource.webapp.console;

import java.util.Properties;

import javax.faces.context.FacesContext;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.Topic;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.log4j.Logger;

import com.groundworkopensource.portal.common.ApplicationType;
import com.groundworkopensource.portal.common.PropertyUtils;
import com.icesoft.faces.async.render.SessionRenderer;

/**
 * JMS Listener/Subscriber subscribes to the topic server.
 * 
 * @author Arul
 */
public abstract class EntitySubscriber implements MessageListener {
    /**
     * JMS connection.
     */
    private Connection connection = null;
    /**
     * Group Render Name.
     */
    protected String groupRenderName = "entity";
    // Constants for properties to be accessed from configuration file.
    /**
     * factory initial.
     */
    private static final String PROP_FACTORY_INITIAL = "java.naming.factory.initial";
    /**
     * Host.
     */
    private static final String PROP_FACTORY_HOST = "java.naming.factory.host";
    /**
     * Port.
     */
    private static final String PROP_FACTORY_PORT = "java.naming.factory.port";
    
    /**
     * PKGS.
     */
    private static final String PROP_FACTORY_PKGS = "java.naming.factory.url.pkgs";
    /**
     * connection factory.
     */
    private static final String PROP_CON_FACTORY = "context.factory";
    /**
     * Topic Name.
     */
    private static final String PROP_TOPIC_NAME = "topic.name.entity";
    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger
            .getLogger(EntitySubscriber.class.getName());

    /**
     * Default Constructor
     */
    public EntitySubscriber() {
        // 
        if (FacesContext.getCurrentInstance() != null) {
            initJMS();
            SessionRenderer.addCurrentSession(groupRenderName);
        } // end if

    }

    /**
     * Abstract method. All Handles extending this class should implement this
     * method.
     * 
     * @param arg0
     */
    public abstract void onMessage(Message arg0);

    /**
     * Initializes the JMS connections. Subscribes the datatableBean to the
     * topic
     */
    private void initJMS() {
        LOGGER.info("Init JMS..");
        LOGGER.info("factory Initial "
                + PropertyUtils.getProperty(ApplicationType.EVENT_CONSOLE,
                        PROP_FACTORY_INITIAL));
        try {
            // Obtain a JNDI connection
            Properties env = new Properties();
            // ... specify the JNDI properties specific to the vendor
            env.put(PROP_FACTORY_INITIAL, PropertyUtils.getProperty(
                    ApplicationType.EVENT_CONSOLE, PROP_FACTORY_INITIAL));
            env.put(PROP_FACTORY_HOST, PropertyUtils.getProperty(
                    ApplicationType.EVENT_CONSOLE, PROP_FACTORY_HOST));
            env.put(PROP_FACTORY_PORT, PropertyUtils.getProperty(
                    ApplicationType.EVENT_CONSOLE, PROP_FACTORY_PORT));
            env.put(PROP_FACTORY_PKGS, PropertyUtils.getProperty(
                    ApplicationType.EVENT_CONSOLE, PROP_FACTORY_PKGS));
            Context jndi = new InitialContext(env);

            // Look up a JMS connection factory
            ConnectionFactory conFactory = (ConnectionFactory) jndi
                    .lookup(PropertyUtils.getProperty(
                            ApplicationType.EVENT_CONSOLE, PROP_CON_FACTORY));

            // Create a JMS connection
            connection = conFactory.createConnection();

            // Create JMS session objects

            Session subSession = connection.createSession(false,
                    Session.CLIENT_ACKNOWLEDGE);

            // Look up a JMS topic
            Topic topic = (Topic) jndi.lookup(PropertyUtils.getProperty(
                    ApplicationType.EVENT_CONSOLE, PROP_TOPIC_NAME));

            // Message subscriber
            MessageConsumer subscriber = subSession.createConsumer(topic);

            // Set a JMS message listener
            subscriber.setMessageListener(this);

            connection.start();
            LOGGER.info("Subscribed to entity topic successfully..");

        } catch (Exception e) {
            LOGGER.error("Error occured while initializing the JMS connection-"
                    + e.getMessage());
        }
    }

}
