/*
 * Copyright (C) 2009 GroundWork Open Source, Inc. (GroundWork) All rights
 * reserved. Use is subject to GroundWork commercial license terms.
 */

package com.groundworkopensource.webapp.console;

import java.util.Map;
import java.util.Set;

import javax.faces.event.ActionEvent;

import com.icesoft.faces.context.effects.Effect;
import com.icesoft.faces.context.effects.Fade;
import com.icesoft.faces.context.effects.Highlight;
import org.groundwork.foundation.ws.model.impl.ActionReturn;

/**
 * <p>
 * The PopupBean class is the backing bean that manages the Popup Panel state.
 * </p>
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
	private HostDetailBean host = null;
	private String appType;
	private boolean showModalInputPanel = false;
	private String inputText;
	private Map<String, Object> dynaPropMap = null;
	private Set<String> dynaPropKeys = null;
	private boolean showDynamicProps = false;
	
	private boolean showEventTile = false;

	private String buttonValue = "Submit";

	public Set<String> getDynaPropKeys() {
		if (dynaPropMap != null)
			return dynaPropMap.keySet();
		else
			return dynaPropKeys;
	}

	private int[] messageIds = null;
	private int actionId = 0;

	public int[] getMessageIds() {
		return messageIds;
	}

	public void setMessageIds(int[] messageIds) {
		this.messageIds = messageIds;
	}

	public int getActionId() {
		return actionId;
	}

	public void setActionId(int actionId) {
		this.actionId = actionId;
	}

	public void setInputText(String inputText) {
		this.inputText = inputText;
	}

	public String getInputText() {
		return this.inputText;
	}

	public boolean isShowModalInputPanel() {
		return showModalInputPanel;
	}

	public void setShowModalInputPanel(boolean showModalInputPanel) {
		this.showModalInputPanel = showModalInputPanel;
	}

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

	public void closeDynamicPropPopup(ActionEvent e) {

		showDynamicProps = false;
		this.dynaPropKeys = null;
		this.dynaPropMap = null;
	}

	public void closeModalPopup(ActionEvent e) {
		showModalPanel = false;

	}
	
	public void closeEventTilePopup(ActionEvent e) {
		showEventTile = false;

	}

	public void closeInputModalPopup(ActionEvent e) {
		if (buttonValue.equalsIgnoreCase("Submit")) {
			TabsetBean tabset = ConsoleHelper.getTabSetBean();
			Tab tab = tabset.getTabs().get(tabset.getTabIndex());
			ActionBean action = tab.getActionBean();
			ActionReturn actionReturn = action.performAction(messageIds,
					actionId, appType, inputText);
			if (!ConsoleConstants.ACTION_RETURN_SUCCESS.equals(actionReturn
					.getReturnCode())
					&& !ConsoleConstants.ACTION_RETURN__HTTP_OK
							.equalsIgnoreCase(actionReturn.getReturnCode())) {
				// showModalPanel = true;
				// showDraggablePanel = false;
				// showModalInputPanel = false;
				// inputText = null;
				buttonValue = "OK";
				title = "Error";
				message = "Error occurred: " + actionReturn.getReturnCode()
						+ " : " + actionReturn.getReturnValue();
				return;
			} // end if
		}
		showModalInputPanel = false;
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

	/**
	 * Returns whether links to SV are enabled.
	 * 
	 * @return the linksEnabled
	 */
	public Boolean getLinksEnabled() {
		return ConsoleHelper.isLinksEnabled()
				&& appType.equalsIgnoreCase(ConsoleConstants.APP_TYPE_NAGIOS);
	}

	/**
	 * Sets the appType.
	 * 
	 * @param appType
	 *            the appType to set
	 */
	public void setAppType(String appType) {
		this.appType = appType;
	}

	/**
	 * Returns the appType.
	 * 
	 * @return the appType
	 */
	public String getAppType() {
		return appType;
	}

	public Map<String, Object> getDynaPropMap() {
		return dynaPropMap;
	}

	public void setDynaPropMap(Map<String, Object> dynaPropMap) {
		this.dynaPropMap = dynaPropMap;
	}

	public boolean isShowDynamicProps() {
		return showDynamicProps;
	}

	public void setShowDynamicProps(boolean showDynamicProps) {
		this.showDynamicProps = showDynamicProps;
	}

	/**
	 * Sets the buttonValue.
	 * 
	 * @param buttonValue
	 *            the buttonValue to set
	 */
	public void setButtonValue(String buttonValue) {
		this.buttonValue = buttonValue;
	}

	/**
	 * Returns the buttonValue.
	 * 
	 * @return the buttonValue
	 */
	public String getButtonValue() {
		return buttonValue;
	}
	
	public boolean isShowEventTile() {
		return showEventTile;
	}

	public void setShowEventTile(boolean showEventTile) {
		this.showEventTile = showEventTile;
	}

}