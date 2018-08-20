package com.groundwork.collage.model.impl;

import java.util.Map;

public class ActionPerform 
{
	private int actionId = -1;
	private Map<String, String> parameters = null;
	
	public ActionPerform (int actionId, Map<String, String>parameters)
	{
		this.actionId = actionId;
		this.parameters = parameters;
	}

	public int getActionId() {
		return actionId;
	}

	public Map<String, String> getParameters() {
		return parameters;
	}
}
