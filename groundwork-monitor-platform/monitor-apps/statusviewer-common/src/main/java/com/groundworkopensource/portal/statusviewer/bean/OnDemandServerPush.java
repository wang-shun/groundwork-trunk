package com.groundworkopensource.portal.statusviewer.bean;

import java.io.Serializable;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.groundworkopensource.portal.common.FacesUtils;
import com.groundworkopensource.portal.statusviewer.common.Constant;
import com.groundworkopensource.portal.statusviewer.common.listener.JMSTopicConnection;
import com.icesoft.faces.async.render.RenderManager;
import com.icesoft.faces.async.render.Renderable;
import com.icesoft.faces.async.render.SessionRenderer;
import com.icesoft.faces.webapp.xmlhttp.FatalRenderingException;
import com.icesoft.faces.webapp.xmlhttp.PersistentFacesState;
import com.icesoft.faces.webapp.xmlhttp.RenderingException;

/**
 * ServerPush - a class that should be extended by all want to have JMS Push
 * functionality. Threading is required in this class for parallel processing
 * for the push.
 * 
 */
public abstract class OnDemandServerPush implements Serializable, Runnable,
        Renderable {

    /**
     * MINUTE_IN_MILLIS
     */
    private static final int MINUTE_IN_MILLIS = 60000;

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger
            .getLogger(OnDemandServerPush.class.getName());

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * Active server push beans.
     */
    private static final ConcurrentHashMap<String, OnDemandServerPush> BEANS = new ConcurrentHashMap<String, OnDemandServerPush>();

    // /**
    // * requestId
    // */
    // protected String requestId = null;

    /**
     * Group Render Name.
     */
    protected String groupRenderName = getClass().getName()
            + Integer.toString(hashCode());

    /**
     * ICEFaces rendering state.
     */
    protected PersistentFacesState renderState = PersistentFacesState
            .getInstance();

    /**
     * Topic Name.
     */
    // protected String PROP_TOPIC_NAME = "topic.name";
    private String xmlMessage = null;

    /**
     * listen To this Topic
     */
    private String listenToTopic = "topic.name";

    /**
     * The last time this bean was refreshed.
     */
    private long lastRefreshTime = -1;

    /**
     * The last time an error was received when the view associated with this
     * bean was rendered.
     */
    private long lastErrorTime = -1;

    /**
     * The number of consecutive unsuccessful render attempts on the view
     * associated with this bean.
     */
    private int renderErrors = 0;

    /**
     * Instance of ManagedBean - entitySubscriber.
     */
    protected JMSTopicConnection jmsConnection = null;

    /**
     * Returns the listenToTopic.
     * 
     * @return the listenToTopic
     */
    public String getListenToTopic() {
        return listenToTopic;
    }

    /**
     * Sets the listenToTopic.
     * 
     * @param listenToTopic
     *            the listenToTopic to set
     */
    public void setListenToTopic(String listenToTopic) {
        this.listenToTopic = listenToTopic;
    }

    /**
     * Default Constructor
     */
    public OnDemandServerPush() {
        initialize();
    }

    /**
     * One argument constructor taking listenToTopic as parameter. This bean
     * will listen to 'topic'..
     * 
     * @param listenToTopic
     */
    public OnDemandServerPush(String listenToTopic) {
        this.listenToTopic = listenToTopic;
        initialize();
    }

    /**
     * Get an iterator over all of the currently active server push beans.
     * 
     * @return all of the currently active server push beans.
     */
    public static final Collection<OnDemandServerPush> getBeans() {
        return BEANS.values();
    }

    /**
     * Initializes the Server Push. Adds the bean to the resource manager.
     * 
     * @param topicPropertyName
     */
    private void initialize() {
    	Object obj = FacesUtils.getServletContext().getAttribute("jmsTopicConnection");
    	if (obj != null) {
    		jmsConnection = (JMSTopicConnection)obj;
    	}
    	else {
    		jmsConnection = (JMSTopicConnection) FacesUtils
    	            .getManagedBean("jmsTopicConnection");
    	}
        BEANS.put(groupRenderName, this);
        RenderManager.getInstance().getOnDemandRenderer(groupRenderName).add(
                this);
        LOGGER.debug("Creation of  [" + groupRenderName + "]");
    }

    /**
     * Run method for the thread
     */
    public void run() {
        lastRefreshTime = System.currentTimeMillis();
        LOGGER.debug("Refreshing bean: " + groupRenderName);

        // If we are still refreshing this bean more than a minute after the
        // last error was received, assume that the last render was a success.
        if (lastRefreshTime - lastErrorTime > MINUTE_IN_MILLIS) {
            LOGGER.debug("Resetting error counter to 0 on " + groupRenderName);
            this.renderErrors = 0;
        }

        refresh(xmlMessage);
        LOGGER.info("Time taken to refresh " + groupRenderName + " is "
                + (System.currentTimeMillis() - lastRefreshTime) + " ms");
    }

    /**
     * Abstract method refresh() - that should be implemented by all classes
     * want to have JMS Push functionality.
     * 
     * @param xmlMessage
     */
    public abstract void refresh(String xmlMessage);

    /**
     * Sets the XML Message
     * 
     * @param xmlMessage
     */
    public void setXMLMessage(String xmlMessage) {
        this.xmlMessage = xmlMessage;
    }

    /**
     * finalize method.
     */
    @Override
    protected void finalize() {
        LOGGER.debug("Finalize called on [" + groupRenderName + "]");
    }

    /**
     * 
     */
    public void free() {
        if (groupRenderName != null) {
            LOGGER.debug("free() called on [" + groupRenderName + "]");
            BEANS.remove(groupRenderName);
            SessionRenderer.removeCurrentSession(groupRenderName);
        }
    }

    /**
     * (non-Javadoc)
     * 
     * @see com.icesoft.faces.async.render.Renderable#getState()
     */
    public PersistentFacesState getState() {
        return renderState;
    }

    /**
     * (non-Javadoc)
     * 
     * @see com.icesoft.faces.async.render.Renderable#renderingException(com.icesoft
     *      .faces.webapp.xmlhttp.RenderingException)
     */
    public void renderingException(RenderingException exception) {
        LOGGER.debug("Received exception while rendering view for bean "
                + groupRenderName + ": \"" + exception.getMessage()
                + "\" (consecutive errors: " + ++renderErrors + ")");
        lastErrorTime = System.currentTimeMillis();

        // If we have received a fatal exception, or more than 3 consecutive
        // render errors bean, kill it
        if (exception instanceof FatalRenderingException
                || renderErrors > Constant.THREE) {
            LOGGER.debug("Cleaning up bean " + groupRenderName);
            free();
        }
    }
}
