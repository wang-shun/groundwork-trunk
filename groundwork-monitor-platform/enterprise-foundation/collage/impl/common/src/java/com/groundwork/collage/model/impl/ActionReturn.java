package com.groundwork.collage.model.impl;

public class ActionReturn 
{
	public static final String CODE_INTERNAL_ERROR = "INTERNAL_ERROR";
	public static final String CODE_NOTHING_RETURNED = "NO_RETURN";
	public static final String CODE_SUCCESS = "SUCCESS";
	
	private int actionId = -1;
	private String returnCode = null;
	private String returnValue = null;
	
	public ActionReturn (int actionId, String returnCode, String returnValue)
	{		
		this.actionId = actionId;
		this.returnCode = returnCode;
		this.returnValue = returnValue;
	}
	
	public int getActionId ()
	{
		return actionId;
	}
	
	public String getReturnCode() {
		return returnCode;
	}
	
	public String getReturnValue() {
		return returnValue;
	}
}
