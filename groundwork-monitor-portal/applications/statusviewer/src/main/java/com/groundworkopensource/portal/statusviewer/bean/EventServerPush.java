package com.groundworkopensource.portal.statusviewer.bean;

/**
 * ServerPush - a class that should be extended by all want to have JMS Push
 * functionality. Threading is required in this class for parallel processing
 * for the push.
 * 
 */
public abstract class EventServerPush extends OnDemandServerPush {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    /**
     * Default Constructor
     */
    public EventServerPush() {
        super("event.topic.name");
    }

}
