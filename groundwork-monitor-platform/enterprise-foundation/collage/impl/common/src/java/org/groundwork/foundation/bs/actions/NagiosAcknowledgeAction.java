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

import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.bs.logmessage.LogMessageService;
import org.groundwork.foundation.dao.FilterCriteria;
import org.groundwork.foundation.dao.FoundationQueryList;

import com.groundwork.collage.CollageFactory;
import com.groundwork.collage.model.Action;
import com.groundwork.collage.model.ActionProperty;
import com.groundwork.collage.model.HostStatus;
import com.groundwork.collage.model.LogMessage;
import com.groundwork.collage.model.ServiceStatus;
import com.groundwork.collage.model.impl.ActionReturn;

public class NagiosAcknowledgeAction extends FoundationActionImpl 
{
	private static final String DEFAULT_COMMENT = "No Comment";
	
	private static final String COMMA = ",";
	
	// Error Codes
	private static final String CODE_MISSING_PARAMETERS = "MISSING_PARAMETERS";
	private static final String CODE_MISSING_USERNAME_ERROR = "MISSING_USERNAME_PARAMETER";
	private static final String CODE_MISSING_LOG_MESSAGE_IDS_ERROR = "MISSING_LOG_MESSAGE_IDS_PARAMETER";
	private static final String CODE_ACKNOWLEDGE_ERROR = "UNABLE_TO_ACKNOWLEDG_LOG_MESSAGES";
	private static final String CODE_UNDEFINED_NAGIOS_CMD_FILE_ERROR = "MISSING_NAGIOS_CMD_FILE_PROPERTY";
	private static final String CODE_NO_LOG_MESSAGES_FOUND = "NO_LOG_MESSAGES_MATCH_IDS";

    private static final String ACK_PENDING = "Nagios ACK Pending";
	
	// Required Action Properties
	private static final String	PROP_NAGIOS_CMD_FILE = "NagiosCommandFile";
	private static final String	PROP_SEND_NOTIFICATION = "DefaultSendNotification";
	private static final String	PROP_PERSISTENT_COMMENT = "DefaultPersistentComment";
	private static final String	PROP_COMMENT = "DefaultComment";
	
	// Action Parameter Keys
	private static final String PARAM_LOG_MESSAGE_IDS = "LogMessageIds";
	private static final String PARAM_USER_NAME = "UserName";
	private static final String PARAM_SEND_NOTIFICATION = "SendNotification";
	private static final String PARAM_PERSISTENT_COMMENT = "PersistentComment";
	private static final String PARAM_COMMENT = "Comment";
	
	// Host Acknowledge - Parameters:  
	// 1=Second From Epoch
	// 2=Host Name
	// 3=Send Notification
	// 4=Persistent Comment
	// 5=User Name
	// 6=Comment	
	private static final String FORMAT_HOST_ACKNOWLEDE = "[%1$d] ACKNOWLEDGE_HOST_PROBLEM;%2$s;1;%3$d;%4$d;%5$s;%6$s\n";
	
	// Service Acknowledge - Parameters:  
	// 1=Second From Epoch
	// 2=Host Name
	// 3=Service Name
	// 4=Send Notification
	// 5=Persistent Comment
	// 6=User Name
	// 7=Comment		
	private static final String FORMAT_SERVICE_ACKNOWLEDE = "[%1$d] ACKNOWLEDGE_SVC_PROBLEM;%2$s;%3$s;1;%4$d;%5$d;%6$s;%7$s\n";
	private static final String FORMAT_ECHO_CMD = "echo \"%1$s\" > %2$s";

	// Execute command array - 3 item is populated during command execution
	private String[] command = {"sh", "-c", null};
	
	private String cmdFileName = null;
	private String userName = null;
	private boolean bSendNotification = true;
	private boolean bPersistentComment = true;
	private String comment = DEFAULT_COMMENT;
	private List<Integer> idList = null;
			
	/** Enable Logging **/
	protected static Log log = LogFactory.getLog(NagiosAcknowledgeAction.class);

	/**
	 * Initialize action and insure all properties and parameters are provided.
	 * We also set the actionReturn member if an error occurs during initialization
	 * instead of throwing an exception
	 */
	public boolean initialize(Action action, Map<String, String> parameters)
	{
		if (super.initialize(action, parameters) == false)
			return false;		
			
		//////////////////////////////////////////////////////////////////////
		// Action Properties
		//////////////////////////////////////////////////////////////////////
		
		// Nagios Command File (Named Piped) Location		
		ActionProperty actionProperty = action.getActionProperty(PROP_NAGIOS_CMD_FILE);
		if (actionProperty == null)		
		{
			actionReturn = new ActionReturn(action.getActionId(), 
					CODE_UNDEFINED_NAGIOS_CMD_FILE_ERROR, 
									"Action property not defined - " + PROP_NAGIOS_CMD_FILE);
			return false;
		}		
		
		cmdFileName = actionProperty.getValue();
		if (cmdFileName == null || cmdFileName.length() == 0)
		{
			actionReturn = new ActionReturn(action.getActionId(), 
					CODE_UNDEFINED_NAGIOS_CMD_FILE_ERROR, 
					"Action property missing value - " + PROP_NAGIOS_CMD_FILE);
			
			return false;
		}				
		
		// Send Notification Property
		actionProperty = action.getActionProperty(PROP_SEND_NOTIFICATION);
		if (actionProperty != null)
		{
			String val = actionProperty.getValue();
			if (val != null && val.length() > 0)
			{
				try {
					bSendNotification = Boolean.parseBoolean(val);
				}
				catch (Exception e)
				{
					log.warn("Invalid DefaultSendNotification property boolean value - " 
							+ val + " - Defaulting to " + bSendNotification);	
				}
			}
		}
		
		// Persistent Property
		actionProperty = action.getActionProperty(PROP_PERSISTENT_COMMENT);
		if (actionProperty != null)
		{
			String val = actionProperty.getValue();
			if (val != null && val.length() > 0)
			{
				try {
					bPersistentComment = Boolean.parseBoolean(val);
				}
				catch (Exception e)
				{
					log.warn("Invalid DefaultPersistentComment property boolean value - " 
							+ val + " - Defaulting to " + bPersistentComment);	
				}
			}

		}
		
		// Send Notification Property
		actionProperty = action.getActionProperty(PROP_COMMENT);
		if (actionProperty != null)
		{
			String val = actionProperty.getValue();
			if (val == null || val.length() == 0)
			{
				comment = "";
			}
			else {
				comment = val;
			}
		}				
		
		//////////////////////////////////////////////////////////////////////
		// Extract required parameters
		//////////////////////////////////////////////////////////////////////
		
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
					"Missing LogMessageIds parameter.");				
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
							
		// UserName parameter is required
		userName = this.parameters.get(PARAM_USER_NAME);		
		if (userName == null || userName.length() == 0)
		{
			actionReturn = new ActionReturn(action.getActionId(), 
					CODE_MISSING_USERNAME_ERROR, 
					"Missing UserName parameter.");
			return false;	
		}
		
		//////////////////////////////////////////////////////////////////////
		// Optional Parameters
		//////////////////////////////////////////////////////////////////////
		
		val = this.parameters.get(PARAM_SEND_NOTIFICATION);		
		if (val != null && val.length() > 0)
		{
			// Convert to boolean
			try {
				this.bSendNotification = Boolean.parseBoolean(val);
			} catch (Exception e)
			{
				log.warn("Invalid SendNotification parameter boolean value - " 
						+ val + " - Defaulting to " + bSendNotification);
			}
		}
		
		val = this.parameters.get(PARAM_PERSISTENT_COMMENT);		
		if (val != null && val.length() > 0)
		{
			// Convert to boolean
			try {
				this.bPersistentComment = Boolean.parseBoolean(val);
			} catch (Exception e)
			{
				log.warn("Invalid PersistentComment parameter boolean value - " 
						+ val + " - Defaulting to " + bPersistentComment);
			}
		}
		
		val = this.parameters.get(PARAM_COMMENT);		
		if (val != null && val.length() > 0)
		{
			comment = val;
		}

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
			
			// Retrieve log messages by ids
			FilterCriteria filter = FilterCriteria.in(LogMessage.HP_ID, idList);
					
			FoundationQueryList list = logMsgService.getLogMessages(filter, null, -1, -1);
			if (list == null || list.size() == 0)
			{
				return new ActionReturn(action.getActionId(), 
						CODE_NO_LOG_MESSAGES_FOUND,  
						"Warning no log messages match ids provided");	
			}
			
			// Go through each log message and write them to the nagios command file
			LogMessage logMsg = null;
			ServiceStatus serviceStatus = null;
			HostStatus hostStatus = null;
			StringBuilder sb = new StringBuilder(128);
			Iterator<LogMessage> it = (Iterator<LogMessage>)list.iterator();	
			int numAcknowledged = 0;
			long secondsSinceEpoch = (new Date().getTime()) / 1000;
			while (it.hasNext())
			{
				logMsg = it.next();
				
				hostStatus = logMsg.getHostStatus();
				if (hostStatus == null)
					continue;
				
				serviceStatus = logMsg.getServiceStatus();
											
				// Determine if the log message is related to a host or a service
				// If neither then it is not a NAGIOS log message and we ignore it
				if (serviceStatus == null)
				{					
					sb.append(
						String.format(FORMAT_HOST_ACKNOWLEDE,
								secondsSinceEpoch,
								hostStatus.getHostName(),
								(bSendNotification == true ? 1 : 0),
								(bPersistentComment == true ? 1 : 0),
								userName,
								comment));

					numAcknowledged++;
				}
				else 
				{
					sb.append(
							String.format(FORMAT_SERVICE_ACKNOWLEDE,
									secondsSinceEpoch,
									hostStatus.getHostName(),
									serviceStatus.getServiceDescription(),
									(bSendNotification == true ? 1 : 0),
									(bPersistentComment == true ? 1 : 0),
									userName,
									comment));

					numAcknowledged++;
				}
                // Set temperary pending comment
                logMsg.setProperty("AcknowledgeComment",ACK_PENDING);
                logMsgService.saveLogMessage(logMsg);
			}
			
			// Use shell echo command to write to Nagios command pipe
			if (sb.length() > 0)
			{
				Runtime runtime = Runtime.getRuntime();
								
				// Use system command to write to command pipe
				// NOTE:  FileOutputStream locks up when trying to use it to write to the pipe.
				// That's why we are using a system command to write to the pipe
				command[2] = String.format(FORMAT_ECHO_CMD, sb.toString(), cmdFileName);

				runtime.exec(command);
			}

			return new ActionReturn(action.getActionId(), 
					ActionReturn.CODE_SUCCESS,  
					"Successfully acknowledged " + numAcknowledged + " log message(s)");	
		}	
		catch (Exception e)
		{
			log.error("Error occurred updating log messages.", e);
			return new ActionReturn(action.getActionId(), 
									CODE_ACKNOWLEDGE_ERROR,  
									"Error occurred acknowledging log messages - " + e.toString());			
		}	
	}
}
