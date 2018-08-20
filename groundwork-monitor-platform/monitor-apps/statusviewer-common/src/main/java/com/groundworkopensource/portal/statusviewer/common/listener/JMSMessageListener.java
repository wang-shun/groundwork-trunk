package com.groundworkopensource.portal.statusviewer.common.listener;

import com.groundworkopensource.portal.statusviewer.bean.OnDemandServerPush;
import org.apache.log4j.Logger;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

/**
 * Listens for message from topic
 * 
 */
public class JMSMessageListener implements MessageListener {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger
            .getLogger(JMSMessageListener.class.getName());

    /**
     * Constructor
     */
    public JMSMessageListener() {

    }

    /**
     * Callback onMessage() - that should be implemented by all classes want to
     * have JMS Push functionality.
     * 
     * @param msg
     */
    public void onMessage(Message msg) {
        String xmlMessage = this.parseMessage(msg);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("+++ Processing Message in SV: " + xmlMessage);
        }
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
            if (xmlMessage
                    .startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?><NagiosPerformanceInfo>")) {
                this.processRefresh("performance.topic.name", xmlMessage);
            } // end if
            // Acknowledge the message since we have client acknowledge mode.
            try {
            	msg.acknowledge();
            }
            catch(JMSException jmse) {
            	LOGGER.error(jmse.getMessage());
            }
        } // end if
    }

    /**
     * Parses the message and gets the XML out
     * 
     * @param msg
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
                LOGGER.debug("Error occured while parsing JMS message: "
                        + e.getMessage());
            }
        }
        if (xmlMessage == null) {
            LOGGER.warn("Error occured: received null XML Message.");
        }
        return xmlMessage;
    }

    /**
     * Process the refresh
     * 
     * @param topicName
     * @param xmlMessage
     */
    private void processRefresh(String topicName, String xmlMessage) {
        // Get beans from the resource manager and call the refresh
        // method.
        for (OnDemandServerPush managedBean : OnDemandServerPush.getBeans()) {
            String listenToTopic = managedBean.getListenToTopic();
            if (listenToTopic != null
                    && listenToTopic.equalsIgnoreCase(topicName)) {
                managedBean.setXMLMessage(xmlMessage);
                JMSMessageProcessor.processMessage(managedBean);
            } // end if
        } // end for
    }
}
