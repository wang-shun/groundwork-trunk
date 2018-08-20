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

package com.groundworkopensource.portal.statusviewer.handler;

import java.util.Iterator;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;

/**
 * Custom Message Listener class for defining custom messages to appear on UI
 * instead of default messages with component ID's.
 * 
 * JSF life-cycle includes six phases and phase events are fired during the
 * start and end of each phase. We can capture phase events by defining a Phase
 * Listener class.
 * 
 * 
 * @author mridu_narang
 * 
 */

public class CustomMessageListener implements PhaseListener {

    /**
     * Serial Version UID
     */
    private static final long serialVersionUID = 1L;

    /**
     * Field Reference Attribute
     */
    private static final String FIELD_REFERENCE = "fieldRef";

    /**
     * (non-Javadoc)
     * 
     * @see javax.faces.event.PhaseListener#afterPhase(javax.faces.event.PhaseEvent)
     */
    public void afterPhase(PhaseEvent event) {
        // after phase implementation
    }

    /**
     * (non-Javadoc)
     * 
     * @see javax.faces.event.PhaseListener#beforePhase(javax.faces.event.PhaseEvent)
     */
    public void beforePhase(PhaseEvent event) {

        // Get the Faces Context
        FacesContext facesContext = event.getFacesContext();

        // Get the root of the UIComponent
        UIViewRoot root = facesContext.getViewRoot();

        // Iterator to iterate through the client ID's
        Iterator<String> iterator = facesContext.getClientIdsWithMessages();

        while (iterator.hasNext()) {

            // Retrieve the client ID
            String clientId = iterator.next();

            // Retrieve the UI component
            UIComponent component = root.findComponent(clientId);
            
            if (component != null) {

	            // Get attribute for which re-defining reference
	            String fieldRef = (String) component.getAttributes().get(
	                    FIELD_REFERENCE);
	
	            // Setting the faces message
	            if (fieldRef != null) {
	
	                // Return an Iterator over the FacesMessages
	                Iterator<FacesMessage> facesIterator = facesContext
	                        .getMessages(clientId);
	
	                // Updating with custom message detail
	                while (facesIterator.hasNext()) {
	                    FacesMessage facesMessage = facesIterator.next();
	                    facesMessage.setDetail(fieldRef + " : "
	                            + facesMessage.getDetail());
	                }
	            }
            }
        }

    }

    /**
     * (non-Javadoc)
     * 
     * @see javax.faces.event.PhaseListener#getPhaseId()
     */
    public PhaseId getPhaseId() {
        return PhaseId.RENDER_RESPONSE;
    }

}
