package com.groundworkopensource.portal.statusviewer.bean;

import java.io.Serializable;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.groundworkopensource.portal.statusviewer.common.IPCHandlerConstants;
import com.groundworkopensource.portal.statusviewer.handler.StateController;
import com.icesoft.faces.async.render.IntervalRenderer;
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
public abstract class ServerPush implements Serializable, Renderable {

    /**
     * HALF_MINUTE
     */
    private static final int HALF_MINUTE = 30000;

    /** Logger. */
    private static final Logger LOGGER = Logger.getLogger(ServerPush.class
            .getName());

    /** serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** Active server push beans. */
    private static final ConcurrentHashMap<String, ServerPush> BEANS = new ConcurrentHashMap<String, ServerPush>();

    // /** requestId. */
    // protected String requestId = null;

    /** Group Render Name. */
    protected String groupRenderName = "entity";

    /** ICEFaces rendering state. */
    protected PersistentFacesState renderState = PersistentFacesState
            .getInstance();

    /** intervalRender. */
    private boolean intervalRender = true;

    /** intervalRenderer. */
    private IntervalRenderer intervalRenderer;

    /**
     * Default Constructor.
     */
    public ServerPush() {
        initialize();
    }

    /**
     * Constructor.
     * 
     * @param renderingInterval
     *            the rendering interval
     */
    public ServerPush(long renderingInterval) {
        initialize(renderingInterval);
    }

    /**
     * Constructor, if extended bean is in session scope.
     * 
     * @param sessionBean
     */
    public ServerPush(boolean sessionBean) {
        // If the bean is session-scoped, then it is possible for the
        // PersistentFacesState reference to change over the duration of the
        // session. In this case, the state should be retrieved from the
        // constructor.
        if (sessionBean) {
            renderState = PersistentFacesState.getInstance();
        }
        initialize();
    }
    
    /**
     * Constructor, if extended bean is in session scope.
     * 
     * @param sessionBean
     */
    public ServerPush(boolean sessionBean, long renderingInterval) {
        // If the bean is session-scoped, then it is possible for the
        // PersistentFacesState reference to change over the duration of the
        // session. In this case, the state should be retrieved from the
        // constructor.
        if (sessionBean) {
            renderState = PersistentFacesState.getInstance();
        }
        initialize(renderingInterval);
    }

    /**
     * One argument constructor taking listenToTopic as parameter. This bean
     * will listen to 'topic'..
     * 
     * @param listenToTopic
     *            the listen to topic
     */
    public ServerPush(String listenToTopic) {
        // this.listenToTopic = listenToTopic;
        initialize();
    }

    /**
     * Get an iterator over all of the currently active server push beans.
     * 
     * @return Collection of the currently active server push beans
     */
    public static final Collection<ServerPush> getBeans() {
        return BEANS.values();
    }

    /**
     * Initializes the Server Push.Adds the bean to the resource manager.
     */
    private void initialize() {
        this.groupRenderName = Integer.toString(this.hashCode());
        // beans.put(groupRenderName, this);
        // RenderManager.getInstance().getOnDemandRenderer(groupRenderName).add(
        // this);
        LOGGER.debug("initialize Inverval render for class "
                + this.getClass().getName());
        intervalRenderer = RenderManager.getInstance().getIntervalRenderer(
                groupRenderName);
        intervalRenderer.setInterval(HALF_MINUTE);
        intervalRenderer.setName(groupRenderName);
        intervalRenderer.add(this);
        intervalRenderer.requestRender();
        // Remove any session attributes for tree expansion
        StateController stateController = new StateController();
        stateController
                .deleteSessionAttribute(IPCHandlerConstants.SV_PATH_ATTRIBUTE);
        stateController
                .deleteSessionAttribute(IPCHandlerConstants.SV_NODE_TYPE_ATTRIBUTE);
        stateController
                .deleteSessionAttribute(IPCHandlerConstants.SV_TAB_PRESSED_ATTRIBUTE);
        LOGGER.debug("Creation of  [" + this.getClass().getName()
                + Integer.toString(this.hashCode()) + "]");
    }

    /**
     * Initializes the Server Push.Adds the bean to the resourcemanager.
     * 
     * @param renderingInterval
     *            the rendering interval
     */
    private void initialize(long renderingInterval) {
        this.groupRenderName = Integer.toString(this.hashCode());
        // beans.put(groupRenderName, this);
        // RenderManager.getInstance().getOnDemandRenderer(groupRenderName).add(
        // this);
        LOGGER.debug("initialize Inverval render for class "
                + this.getClass().getName());
        intervalRenderer = RenderManager.getInstance().getIntervalRenderer(
                groupRenderName);
        intervalRenderer.setInterval(renderingInterval);
        intervalRenderer.setName(groupRenderName);
        intervalRenderer.add(this);
        intervalRenderer.requestRender();
        LOGGER.debug("Creation of  [" + this.getClass().getName()
                + Integer.toString(this.hashCode()) + "]");
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
     * finalize method.
     */
    @Override
    protected void finalize() {
        LOGGER.debug("Finalize called on [" + this.getClass().getName()
                + Integer.toString(this.hashCode()) + "]");

    }

    /**
     * Free.
     */
    public void free() {
        if (groupRenderName != null) {
            LOGGER.debug("free() called on [" + this.getClass().getName()
                    + Integer.toString(this.hashCode()) + "]");
            // beans.remove(groupRenderName);
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
        setIntervalRender(true);
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
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Received exception while rendering view for bean "
                    + getClass().getName() + Integer.toString(hashCode())
                    + ": " + exception.getMessage());
        }
        if (exception instanceof FatalRenderingException) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Cleaning up bean " + getClass().getName()
                        + Integer.toString(hashCode()));
            }
            performCleanup();
            free();
        }
    }

    /**
     * Used to properly shut-off the ticking clock.
     * 
     * @return true if properly shut-off, false if not.
     */
    protected boolean performCleanup() {
        try {
            if (intervalRenderer != null) {
                intervalRenderer.remove(this);
                // whether or not this is necessary depends on how 'shutdown'
                // you want an empty renderer. If it's emptied often, the cost
                // of shutdown+startup is too great
                if (intervalRenderer.isEmpty()) {
                    intervalRenderer.dispose();
                }
                intervalRenderer = null;
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("performCleanup() is called for "
                            + getClass().getName()
                            + Integer.toString(hashCode()));
                }
            }

            return true;

        } catch (Exception failedCleanup) {
            LOGGER.error("Failed to cleanup a clock inside render bean ",
                    failedCleanup);
        }
        return false;
    }

    /**
     * Sets the intervalRender.
     * 
     * @param intervalRender
     *            the intervalRender to set
     */
    public void setIntervalRender(boolean intervalRender) {
        this.intervalRender = intervalRender;
    }

    /**
     * Returns the intervalRender.
     * 
     * @return the intervalRender
     */
    public boolean isIntervalRender() {
        return intervalRender;
    }
}
