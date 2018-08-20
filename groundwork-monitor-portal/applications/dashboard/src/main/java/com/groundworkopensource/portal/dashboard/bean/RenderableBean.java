/*
 *  Copyright (C) 2009 GroundWork Open Source, Inc. (GroundWork)
 *  All rights reserved. This program is free software; you can redistribute
 *  it and/or modify it under the terms of the GNU General Public License
 *  version 2 as published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package com.groundworkopensource.portal.dashboard.bean;

import java.io.Serializable;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.apache.log4j.Logger;

import com.groundworkopensource.portal.common.ws.Constants;
import com.icesoft.faces.async.render.IntervalRenderer;
import com.icesoft.faces.async.render.RenderManager;
import com.icesoft.faces.async.render.Renderable;
import com.icesoft.faces.webapp.xmlhttp.FatalRenderingException;
import com.icesoft.faces.webapp.xmlhttp.PersistentFacesState;
import com.icesoft.faces.webapp.xmlhttp.RenderingException;

/**
 * <p>
 * This class represents a base bean implementing IntervalRenderer. All interval
 * rendering beans in the application should extend this base bean. The
 * rendering interval is set in web.xml using a context-parameter,
 * "renderer.interval" which is set to 30000 milli-sec by default. A bean can
 * set its own interval by using setRenderInterval() API.
 * </p>
 * 
 * <p>
 * Note: A Renderable bean has to register a renderManager in faces-config.xml
 * as shown below: <code>
 * 
 * <managed-bean>
        <description>
            Network statistics bean
        </description>
        <managed-bean-name>netstat</managed-bean-name>
        <managed-bean-class>
            com.groundworkopensource.portal.dashboard.bean.NetworkStatistics
        </managed-bean-class>
        <managed-bean-scope>request</managed-bean-scope>
        <managed-property>
            <property-name>renderManager</property-name>
            <value>#{renderManager}</value>
        </managed-property>
    </managed-bean>  
    
 * </code>
 * </p>
 * 
 * @author rashmi_tambe
 * 
 */
@SuppressWarnings("unchecked")
public class RenderableBean implements Renderable, Serializable {

    /**
     * CLOCK INTERVAL RENDERER
     */
    private static final String CLOCK_INTERVAL_RENDERER = "clock";

    /**
     * DEFAULT INTERVAL
     */
    private static final long DEFAULT_INTERVAL = 10000L;

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 7526472295622776147L;

    /**
     * logger
     */
    private static Logger logger = Logger.getLogger(NetworkStatistics.class
            .getName());

    /**
     * Time interval, in milliseconds, between renders.
     */
    private static Long renderInterval;

    /**
     * The state associated with the current user that can be used for
     * server-initiated render calls.
     */
    private PersistentFacesState state;

    /**
     * A named render group that can be shared by all TimeZoneBeans for
     * server-initiated render calls. Setting the interval determines the
     * frequency of the render call.
     */
    // TODO shall we declare this transient? => NO - its not working then
    private IntervalRenderer clock;

    /**
     * @return the renderInterval
     */
    public long getRenderInterval() {
        return renderInterval;
    }

    static {
        // get the rendering interval value from web.xml.
        FacesContext fc = FacesContext.getCurrentInstance();
        Map initParameterMap = fc.getExternalContext().getInitParameterMap();

        // if web.xml does not have it, default to 10000 milli-sec.
        String interval = (String) initParameterMap
                .get(Constants.RENDER_INTERVAL_KEY);
        if (interval != null) {
            renderInterval = Long.valueOf(interval);
        } else {
            renderInterval = DEFAULT_INTERVAL;
        }
        logger.debug("Retrieved renderInterval = " + renderInterval);
    }

    /**
     * Default Constructor
     */
    public RenderableBean() {
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
            logger.error("Failed to cleanup a clock bean", failedCleanup);
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
}
