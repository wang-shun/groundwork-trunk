/*
 *  Copyright (C) 2009 GroundWork Open Source, Inc. (GroundWork)
 *  All rights reserved. Use is subject to GroundWork commercial license terms.
 */

package com.groundworkopensource.webapp.console;

import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;

import com.icesoft.faces.component.ext.HtmlCommandLink;

public class FreezeTableBean {
	private boolean freeze;
	//public static final String FREEZE = "Pause Incoming Events";
	//public static final String UNFREEZE = "Resume Incoming Events";
	private String freezeButtonText;
	public static Logger logger = Logger.getLogger(FreezeTableBean.class
			.getName());

	private String pauseButtonImage = ConsoleConstants.IMG_PAUSE_EVENTS;

	public FreezeTableBean() {
		freezeButtonText = ResourceUtils.getLocalizedMessage(ConsoleConstants.I18N_CONSOLE_CONTENT_PAUSE_EVENTS);
	}

	public void toggleButton(ActionEvent event) {
		String linkText = (String) ((HtmlCommandLink) event.getComponent())
				.getValue();

		if (linkText.equalsIgnoreCase(ResourceUtils.getLocalizedMessage(ConsoleConstants.I18N_CONSOLE_CONTENT_PAUSE_EVENTS))) {

			freeze = true;

		} else {

			freeze = false;

		}
		freeze(freeze);
	}

	public void reset() {
		freeze = !freeze;
		freezeButtonText = freeze ? ResourceUtils.getLocalizedMessage(ConsoleConstants.I18N_CONSOLE_CONTENT_RESUME_EVENTS) : ResourceUtils.getLocalizedMessage(ConsoleConstants.I18N_CONSOLE_CONTENT_PAUSE_EVENTS);
		freeze(freeze);
	}

	public void freeze(boolean value) {

		logger.debug("Is window freezed =" + value);
		TabsetBean tabset = ConsoleHelper.getTabSetBean();
		if (value) {
			tabset.stopTopicConnection();
			this.pauseButtonImage = ConsoleConstants.IMG_PLAY_EVENTS;
			freezeButtonText = ResourceUtils.getLocalizedMessage(ConsoleConstants.I18N_CONSOLE_CONTENT_RESUME_EVENTS);
		} else {
			tabset.startTopicConnection();
			this.pauseButtonImage = ConsoleConstants.IMG_PAUSE_EVENTS;
			freezeButtonText = ResourceUtils.getLocalizedMessage(ConsoleConstants.I18N_CONSOLE_CONTENT_PAUSE_EVENTS);
		} // end if

	}

	public String getFreezeButtonText() {
		return freezeButtonText;
	}

	public void setFreezeButtonText(String freezeButtonText) {
		this.freezeButtonText = freezeButtonText;
	}

	public String getPauseButtonImage() {
		return pauseButtonImage;
	}

	public void setPauseButtonImage(String pauseButtonImage) {
		this.pauseButtonImage = pauseButtonImage;
	}

}
