package com.groundworkopensource.webapp.console;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.apache.log4j.Logger;

/**
 * The listener interface for receiving JMSMessage events. The class that is
 * interested in processing a JMSMessage event implements this interface, and
 * the object created with that class is registered with a component using the
 * component's <code>addJMSMessageListener<code> method. When
 * the JMSMessage event occurs, that object's appropriate
 * method is invoked.
 * 
 * @see JMSMessageEvent
 */
public class JMSMessageListener implements MessageListener {

    /** Logger. */
    private static final Logger LOGGER = Logger
            .getLogger(JMSMessageListener.class.getName());

    /**
     * Instantiates a new jMS message listener.
     */
    public JMSMessageListener() {

    }

    /**
     * Callback onMessage() - that should be implemented by all classes want to
     * have JMS Push functionality.
     * 
     * @param msg
     *            the msg
     */
    public void onMessage(Message msg) {
        String xmlMessage = this.parseMessage(msg);
        if (xmlMessage != null) {
            if (xmlMessage.startsWith("<EVENT>")) {
                this.processRefresh("event.topic.name", xmlMessage);
            } // end if
            if (xmlMessage.startsWith("<AGGREGATE>")) {
                this.processRefresh("topic.name", xmlMessage);
            } // end if
            if (xmlMessage.startsWith("<TREEVIEWUPDATES>")) {
                this.processRefresh("ui.events.topic.name", xmlMessage);
            } // end if
            
            try {
            	msg.acknowledge();
            }
            catch(JMSException jmse) {
            	LOGGER.error(jmse.getMessage());
            }
        } // end if
    }

    /**
     * Parses the message and gets the XML out.
     * 
     * @param msg
     *            the msg
     * 
     * @return the string
     */
    private String parseMessage(Message msg) {
        String xmlMessage = null;
        if (msg != null) {

            /* Convert message to TextMessage format */
            TextMessage textMessage = (TextMessage) msg;
            try {
                /* Get XML in text format from textMessage */
                xmlMessage = textMessage.getText();
            } catch (JMSException e) {
                LOGGER.error("onMessage(): " + e.getMessage());
            }
        }
        if (xmlMessage == null) {
            LOGGER.warn("onMessage(): Received null XML Message.");
        }
        return xmlMessage;
    }

    /**
     * Process the refresh.
     * 
     * @param topicName
     *            the topic name
     * @param xmlMessage
     *            the xml message
     */
    private void processRefresh(String topicName, String xmlMessage) {
        // Get beans from the resource manager and call the refresh
        // method.
        for (ServerPush managedBean : ServerPush.getBeans()) {
            String listenToTopic = managedBean.getListenToTopic();
            if (listenToTopic != null
                    && listenToTopic.equalsIgnoreCase(topicName)) {
                managedBean.setXMLMessage(xmlMessage);
                JMSMessageProcessor.processMessage(managedBean);
            } // end if
        } // end for
    }
}
