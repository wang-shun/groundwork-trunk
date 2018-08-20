package com.groundworkopensource.webapp.console;

/*
 * 
 * Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")  
 * All rights reserved. This program is free software; you can redistribute it
 * and/or modify it under the terms of the GNU General Public License version 2
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for 
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51 Franklin
 * Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 */
import com.icesoft.faces.context.effects.Effect;
import com.icesoft.faces.context.effects.Fade;
import com.icesoft.faces.context.effects.Highlight;

import javax.faces.event.ActionEvent;

/**
 * <p>The PopupBean class is the backing bean that manages the Popup Panel
 * state.</p>
 */
public class PopupBean {

    // icons used for draggable panel
    private String closePopupImage = "./images/popupPanel/popupclose.gif";

    // show or hide each popup panel
    private boolean showDraggablePanel = false;
    private boolean showModalPanel = false;
    private Effect statusFadeEffect;
    private Effect statusEffect;
    private String message = null;
    private String title = null;
    private HostDetailBean host=null;


    public boolean isShowDraggablePanel() {
        return showDraggablePanel;
    }

    public void setShowDraggablePanel(boolean showDraggablePanel) {
        this.showDraggablePanel = showDraggablePanel;
    }

    public boolean isShowModalPanel() {
        return showModalPanel;
    }

    public void setShowModalPanel(boolean showModalPanel) {
        this.showModalPanel = showModalPanel;
    }

    public void closeDraggablePopup(ActionEvent e) {

        showDraggablePanel = false;
    }

    public void closeModalPopup(ActionEvent e) {

        showModalPanel = false;
    }

    public void setClosePopupImage(String closePopupImage) {
        this.closePopupImage = closePopupImage;
    }

    public String getClosePopupImage() {
        return this.closePopupImage;
    }

    public String updateStatus() {
        if (statusEffect == null) {
            statusEffect = new Highlight("#AADDFF");
        }
        if (statusFadeEffect == null) {
            statusFadeEffect = new Fade(1.0f, 0.1f);
        }
        statusEffect.setFired(false);
        statusFadeEffect.setFired(false);
        return null;
    }

    public Effect getStatusFadeEffect() {
        return statusFadeEffect;
    }

    public void setStatusFadeEffect(Effect statusFadeEffect) {
        this.statusFadeEffect = statusFadeEffect;
    }

    public Effect getStatusEffect() {
        return statusEffect;
    }

    public void setStatusEffect(Effect statusEffect) {
        this.statusEffect = statusEffect;
    }

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public HostDetailBean getHost() {
		return host;
	}

	public void setHost(HostDetailBean host) {
		this.host = host;
	}
}