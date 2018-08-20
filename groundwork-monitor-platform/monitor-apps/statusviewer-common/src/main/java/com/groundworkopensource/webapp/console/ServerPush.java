package com.groundworkopensource.webapp.console;

import java.io.Serializable;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.groundworkopensource.portal.common.FacesUtils;
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
 */
public abstract class ServerPush implements Serializable, Runnable, Renderable {

    /** Logger. */
    private static final Logger LOGGER = Logger.getLogger(ServerPush.class
            .getName());

    /** serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** Active server push beans. */
    private static final ConcurrentHashMap<String, ServerPush> BEANS = new ConcurrentHashMap<String, ServerPush>();

    /** requestId. */
    protected String requestId = null;

    /** Group Render Name. */
    protected String groupRenderName = "entity";

    /** ICEFaces rendering state. */
    protected PersistentFacesState renderState = PersistentFacesState
            .getInstance();

    /** Topic Name. */
    // protected String PROP_TOPIC_NAME = "topic.name";
    private String xmlMessage = null;

    /** The listen to topic. */
    protected String listenToTopic = "topic.name";

    /** Instance of ManagedBean - entitySubscriber. */
    protected JMSTopicConnection jmsConnection = (JMSTopicConnection) FacesUtils
            .getManagedBean("jmsConsoleTopicConnection");

    /**
     * Gets the listen to topic.
     * 
     * @return the listen to topic
     */
    public String getListenToTopic() {
        return listenToTopic;
    }

    /**
     * Default Constructor.
     */
    public ServerPush() {
        initialize();
    }

    /**
     * One argument constructor taking listenToTopic as parameter. This bean
     * will listen to 'topic'..
     * 
     * @param listenToTopic
     *            the listen to topic
     */
    public ServerPush(String listenToTopic) {
        this.listenToTopic = listenToTopic;
        initialize();
    }

    /**
     * Get an iterator over all of the currently active server push beans.
     * 
     * @return Collection < ServerPush >
     */
    public static final Collection<ServerPush> getBeans() {
        return BEANS.values();
    }

    /**
     * Initializes the Server Push.Adds the bean to the resourcemanager.
     */
    private void initialize() {
        this.groupRenderName = Integer.toString(this.hashCode());
        BEANS.put(groupRenderName, this);
        RenderManager.getInstance().getOnDemandRenderer(groupRenderName).add(
                this);
        LOGGER.debug("Creation of  [" + this.getClass().getName()
                + Integer.toString(this.hashCode()) + "]");
    }

    /**
     * Run method for the thread.
     */
    public void run() {
        long startTime = System.currentTimeMillis();
        refresh(xmlMessage);
        LOGGER.debug("Time taken to refresh " + this + " is "
                + (System.currentTimeMillis() - startTime) + " ms");
    }

    /**
     * Abstract method refresh() - that should be implemented by all classes
     * want to have JMS Push functionality.
     * 
     * @param xmlMessage
     *            the xml message
     */
    public abstract void refresh(String xmlMessage);

    /**
     * Sets the XML Message.
     * 
     * @param xmlMessage
     *            the xml message
     */
    public void setXMLMessage(String xmlMessage) {
        this.xmlMessage = xmlMessage;
    }

    /**
     * finalize method.
     */
    @Override
    protected void finalize() {
        LOGGER.debug("Finalize called on [" + this.getClass().getName()
                + Integer.toString(this.hashCode()) + "]");
    }

    /**
     * free - removes bean from current session.
     */
    public void free() {
        if (groupRenderName != null) {
            LOGGER.debug("free() called on [" + this.getClass().getName()
                    + Integer.toString(this.hashCode()) + "]");
            BEANS.remove(groupRenderName);
            SessionRenderer.removeCurrentSession(groupRenderName);
        }
    }

    /**
     * (non-Javadoc).
     * 
     * @return the state
     * 
     * @see com.icesoft.faces.async.render.Renderable#getState()
     */
    public PersistentFacesState getState() {
        return renderState;
    }

    /**
     * (non-Javadoc).
     * 
     * @param exception
     *            the exception
     * 
     * @see com.icesoft.faces.async.render.Renderable#renderingException(com.icesoft
     *      .faces.webapp.xmlhttp.RenderingException)
     */
    public void renderingException(RenderingException exception) {
        LOGGER.debug("Received exception while rendering view for bean "
                + getClass().getName() + Integer.toString(hashCode()) + ": "
                + exception.getMessage());
        if (exception instanceof FatalRenderingException) {
            LOGGER.debug("Cleaning up bean " + getClass().getName()
                    + Integer.toString(hashCode()));
            free();
        }
    }
}
