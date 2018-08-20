package com.groundworkopensource.portal.statusviewer.common;

import org.apache.log4j.Logger;

import com.icesoft.faces.async.render.OnDemandRenderer;
import com.icesoft.faces.async.render.RenderManager;
import com.icesoft.faces.async.render.Renderable;
import com.icesoft.faces.async.render.SessionRenderer;
import com.icesoft.faces.webapp.xmlhttp.FatalRenderingException;
import com.icesoft.faces.webapp.xmlhttp.PersistentFacesState;
import com.icesoft.faces.webapp.xmlhttp.RenderingException;

/*
 * 
 * Copyright 2007 GroundWork Open Source, Inc. ("GroundWork") All rights
 * reserved. This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

/**
 * The Class OnDemandRenderBean.
 * 
 * @author manish_kjain
 */
public class OnDemandRenderBean implements Renderable {

    /** Logger. */
    private static final Logger LOGGER = Logger
            .getLogger(OnDemandRenderBean.class.getName());

    /** serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** Group Render Name. */
    protected String groupRenderName = "navigation";

    /** ICEFaces rendering state. */
    protected PersistentFacesState renderState = PersistentFacesState
            .getInstance();

    /** The on demand renderer. */
    public OnDemandRenderer onDemandRenderer;

    // /** The render manager. */
    // private RenderManager renderManager;

    /**
     * Default Constructor.
     */
    public OnDemandRenderBean() {
        initialize();
    }

    /**
     * Initializes the Server Push.Adds the bean to the resourcemanager.
     */
    private void initialize() {
        this.groupRenderName = "navigation";

        onDemandRenderer = RenderManager.getInstance().getOnDemandRenderer(
                groupRenderName);
        onDemandRenderer.add(this);
        LOGGER.debug("Creation of  [" + this.getClass().getName()
                + Integer.toString(this.hashCode()) + "]");
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
        LOGGER.error("Received exception while rendering view for bean "
                + getClass().getName() + Integer.toString(hashCode()) + ": "
                + exception.getMessage());
        if (exception instanceof FatalRenderingException) {
            LOGGER.debug("Cleaning up bean " + getClass().getName()
                    + Integer.toString(hashCode()));
            free();
        }
    }

    // /**
    // * Sets the renderManager.
    // *
    // * @param renderManager
    // * the renderManager to set
    // */
    // public void setRenderManager(RenderManager renderManager) {
    // this.renderManager = renderManager;
    // this.groupRenderName = "navigation";
    // SessionRenderer.addCurrentSession(groupRenderName);
    // // onDemandRenderer =
    // // renderManager.getOnDemandRenderer(groupRenderName);
    // // onDemandRenderer.add(this);
    // }

}
