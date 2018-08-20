/*
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2007  GroundWork Open Source Solutions info@groundworkopensource.com

 *     This program is free software; you can redistribute it and/or modify
 *     it under the terms of version 2 of the GNU General Public License
 *     as published by the Free Software Foundation.

 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.

 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.groundwork.foundation.bs.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.bs.logmessage.LogMessageService;

import com.groundwork.collage.CollageFactory;
import com.groundwork.collage.model.Action;
import com.groundwork.collage.model.ActionProperty;
import com.groundwork.collage.model.impl.ActionReturn;

public class UpdateOperationStatusAction extends FoundationActionImpl
{
	private static final String COMMA = ",";
	
	// Error Codes
	private static final String CODE_MISSING_PARAMETERS = "MISSING_PARAMETERS";
	private static final String CODE_UNDEFINED_OPERATION_STATUS_ERROR = "UNDEFINED_OPSTATUS_PROPERTY";
	private static final String CODE_MISSING_LOG_MESSAGE_IDS_ERROR = "MISSING_LOG_MESSAGE_IDS_PARAMETER";
	private static final String CODE_EXECUTING_UPDATE_ERROR = "UNABLE_TO_UPDATE_LOG_MESSAGES";
	
	// Required Action Properties
	private static final String	PROP_OPERATION_STATUS = "OperationStatus";
	
	// Action Parameter Keys
	private static final String PARAM_LOG_MESSAGE_IDS = "LogMessageIds";
	private static final String PARAM_OPERATION_STATUS = "OperationStatus";
	
	private static final String PARAM_LOG_UPDATED_BY = "user";
	private static final String PARAM_COMMENTS = "user_comment";
	
	private String opStatus = null;
	List<Integer> idList = null;
	private String updatedBy = null;
	private String comments = null;
			
	/** Enable Logging **/
	protected static Log log = LogFactory.getLog(UpdateOperationStatusAction.class);

	/**
	 * Initialize action and insure all properties and parameters are provided.
	 * We also set the actionReturn member if an error occurs during initialization
	 * instead of throwing an exception
	 */
	public boolean initialize(Action action, Map<String, String> parameters)
	{
		if (super.initialize(action, parameters) == false)
			return false;		
			
		ActionProperty actionProperty = action.getActionProperty(PROP_OPERATION_STATUS);
		if (actionProperty == null)		
		{
			actionReturn = new ActionReturn(action.getActionId(), 
					CODE_UNDEFINED_OPERATION_STATUS_ERROR, 
									"Action property not defined - " + PROP_OPERATION_STATUS);
			return false;
		}
				
		opStatus = actionProperty.getValue();
		if (opStatus == null || opStatus.length() == 0)
		{
			actionReturn = new ActionReturn(action.getActionId(), 
					CODE_UNDEFINED_OPERATION_STATUS_ERROR, 
					"Action property missing value - " + PROP_OPERATION_STATUS);
			
			return false;
		}
		
		// Extract required parameters
		if (parameters == null || parameters.size() == 0)
		{
			actionReturn = new ActionReturn(action.getActionId(), 
					CODE_MISSING_PARAMETERS, 
					"Missing required parameters.");
			return false;		
		}
		
		String val = this.parameters.get(PARAM_LOG_MESSAGE_IDS);
		if (val == null || val.length() == 0)
		{
			actionReturn = new ActionReturn(action.getActionId(), 
					CODE_MISSING_LOG_MESSAGE_IDS_ERROR,  
					"Missing required parameters.");				
			return false;
		}
		
		// Convert comma-separated ids to int array
		String strId = null;
		StringTokenizer tokenizer = new StringTokenizer(val, COMMA); 			
		idList = new ArrayList<Integer>(tokenizer.countTokens());
		
		while (tokenizer.hasMoreTokens())
		{
			strId = tokenizer.nextToken();
			try {
				idList.add(new Integer(strId));
			}
			catch (Exception e)
			{
				log.warn("UpdateOperationStatusAction - Invalid log message id - " + strId);
			}
		}
							
		// See if caller passed in operation status
		String operationStatus = this.parameters.get(PARAM_OPERATION_STATUS);
		
		// If operation Status is provided, we use the one passed in instead of the property
		if (operationStatus != null && operationStatus.length() > 0)
			this.opStatus = operationStatus;
		
		
		// See if caller passed in operation status
		String updatedBy = this.parameters.get(PARAM_LOG_UPDATED_BY);
		
		// If updatedBy is provided, we use the one passed in instead of the property
		if (updatedBy != null && updatedBy.length() > 0)
			this.updatedBy = updatedBy;
		
		
		// See if caller passed in operation status
		String comments = this.parameters.get(PARAM_COMMENTS);
		
		// If operation Status is provided, we use the one passed in instead of the property
		if (comments != null && comments.length() > 0)
			this.comments = comments;

		return true;
	}

	public ActionReturn call() throws Exception 
	{
		// Error occurred during initialization
		if (actionReturn != null)
			return actionReturn;
		
		try {		
			CollageFactory factory = CollageFactory.getInstance();
			
			LogMessageService logMsgService = factory.getLogMessageService();				
			int numUpdated = 0;
            HashMap<String, Object> prop = null;

            prop = new HashMap<String, Object>();
            if (comments== null)
                comments="";
            prop.put("Comments",comments);
            prop.put("AcknowledgedBy",updatedBy);
            numUpdated = logMsgService.updateLogMessageOperationStatus(this.idList, this.opStatus, prop);
			return new ActionReturn(action.getActionId(), 
					ActionReturn.CODE_SUCCESS,  
					"Successfully updated " + numUpdated + " log message(s) operation status to " + this.opStatus);	
			
		}
		catch (Exception e)
		{
			log.error("Error occurred updating log messages.", e);
			return new ActionReturn(action.getActionId(), 
									CODE_EXECUTING_UPDATE_ERROR,  
									"Error occurred updating log messages - " + e.toString());			
		}	
	}
}
