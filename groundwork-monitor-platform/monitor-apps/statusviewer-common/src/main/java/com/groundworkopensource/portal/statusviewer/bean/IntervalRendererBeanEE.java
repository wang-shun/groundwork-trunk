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

package com.groundworkopensource.portal.statusviewer.bean;

import java.io.Serializable;

import org.apache.log4j.Logger;

import com.icesoft.faces.async.render.IntervalRenderer;
import com.icesoft.faces.async.render.RenderManager;
import com.icesoft.faces.async.render.Renderable;
import com.icesoft.faces.async.render.SessionRenderer;
import com.icesoft.faces.webapp.xmlhttp.FatalRenderingException;
import com.icesoft.faces.webapp.xmlhttp.PersistentFacesState;
import com.icesoft.faces.webapp.xmlhttp.RenderingException;

/**
 * 
 * 
 * This class represents a base bean implementing IntervalRenderer. All interval
 * rendering beans in the application should extend this base bean. The
 * rendering interval is set from status-viewer.properties file,
 * "renderer.interval" which is set to 2000 milli-sec by default. A bean can set
 * its own interval by using setRenderInterval() API.
 * 
 * @author manish_kjain
 * 
 */
public class IntervalRendererBeanEE implements Renderable, Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -3082513593193127090L;

    /**
     * CLOCK INTERVAL RENDERER
     */
    private static final String CLOCK_INTERVAL_RENDERER = "clock";

    /**
     * logger
     */
    private static Logger logger = Logger
            .getLogger(IntervalRendererBeanEE.class.getName());

    /**
     * Time interval, in milliseconds, between renders.
     */
    private Long renderInterval;

    /**
     * The state associated with the current user that can be used for
     * server-initiated render calls.
     */
    private final PersistentFacesState state;

    /**
     * render boolean to set true on click on getState
     */
    private boolean intervalRender = true;

    /**
     * Default Time interval, in milliseconds, between renders.
     */
    private static final long DEFAULT_RENDER_TIME = 120000L;

    /**
     * A named render group that can be shared by all TimeZoneBeans for
     * server-initiated render calls. Setting the interval determines the
     * frequency of the render call.
     */
    // shall we declare this transient? => NO - its not working then
    private IntervalRenderer clock;

    /**
     * @return the renderInterval
     */
    public long getRenderInterval() {
        return renderInterval;
    }

    /**
     * Default Constructor
     */
    public IntervalRendererBeanEE() {
        // initialization related to interval renderer
        state = PersistentFacesState.getInstance();
    }

    /**
     * parameterized Constructor
     * 
     * @param intervalInSec
     */
    public IntervalRendererBeanEE(long intervalInSec) {
        if (intervalInSec == 0) {
            this.renderInterval = DEFAULT_RENDER_TIME;
            logger.info("Default Time interval is set :-" + renderInterval);
        }
        this.renderInterval = intervalInSec;
        // initialization related to interval renderer
        state = PersistentFacesState.getInstance();
    }

    /**
     * Used to create, setup, and start an IntervalRenderer from the passed
     * renderManager This is used in conjunction with faces-config.xml to allow
     * the same single render manager to be set in all Beans
     * 
     * @param renderManager
     *            RenderManager to get the IntervalRenderer from
     */
    public void setRenderManager(RenderManager renderManager) {
        clock = renderManager.getIntervalRenderer(CLOCK_INTERVAL_RENDERER);
        clock.setInterval(renderInterval);
        clock.setName(CLOCK_INTERVAL_RENDERER);
        clock.add(this);
        clock.requestRender();
    }

    /**
     * Gets the current instance of PersistentFacesState
     * 
     * @return PersistentFacesState state
     */
    public PersistentFacesState getState() {
        this.setIntervalRender(true);
        return state;
    }

    /**
     * Callback to inform us that there was an Exception while rendering.
     * Continue from a transientRenderingException but not from a
     * FatalRenderingException
     * 
     * @param renderingException
     *            render exception passed in frome framework.
     */
    public void renderingException(RenderingException renderingException) {

        if (logger.isDebugEnabled()) {
            logger.debug("Rendering exception: ", renderingException);
        }

        if (renderingException instanceof FatalRenderingException) {
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
            if (clock != null) {
                clock.remove(this);
                // whether or not this is necessary depends on how 'shutdown'
                // you want an empty renderer. If it's emptied often, the cost
                // of shutdown+startup is too great
                if (clock.isEmpty()) {
                    clock.dispose();
                }
                clock = null;
            }
            return true;
        } catch (Exception failedCleanup) {
            logger.error("Failed to cleanup a clock inside render bean ",
                    failedCleanup);
        }
        return false;
    }

    /**
     * Dispose callback called due to a view closing or session
     * invalidation/timeout
     * 
     * @throws Exception
     */
    public void dispose() throws Exception {
        performCleanup();
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

    /**
     * free - removes bean from current session.
     */
    public void free() {
        logger.info("Cleaning session : " + this.getClass().getName());
        SessionRenderer.removeCurrentSession(CLOCK_INTERVAL_RENDERER);
    }

}
