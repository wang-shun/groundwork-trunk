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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.IOException;

import java.util.Iterator;
import java.util.Map;
import java.util.List;
import java.util.StringTokenizer;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.groundwork.collage.model.Action;
import com.groundwork.collage.model.ActionProperty;
import com.groundwork.collage.model.ActionParameter;
import com.groundwork.collage.model.impl.ActionReturn;

public class ShellScriptAction extends FoundationActionImpl
{
	// Action Properties
	private static final String	PROP_SCRIPT = "Script";
	private static final String	PROP_ENV_VARS = "EnvironmentVariables";

	// Error Codes
	public static final String CODE_UNDEFINED_SCRIPT_ERROR = "UNDEFINED_SCRIPT_PROPERTY";
	public static final String CODE_SCRIPT_ERROR = "ERROR_EXECUTING_SCRIPT";

	// Environment Variable delimiter
	private static final String DELIMITER = ",";


	private String script = null;
	private String[] envVars = null;
	private String[] cmdArr = null;

	/** Enable Logging **/
	protected static Log log = LogFactory.getLog(ShellScriptAction.class);

	/**
	 * Initialize action and insure all properties and parameters are provided.
	 */
	public boolean initialize(Action action, Map<String, String> parameters)
	{
		if (super.initialize(action, parameters) == false)
			return false;

		ActionProperty scriptProperty = action.getActionProperty(PROP_SCRIPT);
		if (scriptProperty == null)
		{
			actionReturn = new ActionReturn(action.getActionId(),
									CODE_UNDEFINED_SCRIPT_ERROR,
									"Action property \"" + PROP_SCRIPT + "\" is not defined for action \"" + action.getName() + "\".");
			return false;
		}

		script = scriptProperty.getValue();
		if (script == null || script.length() == 0)
		{
			actionReturn = new ActionReturn(action.getActionId(),
					CODE_UNDEFINED_SCRIPT_ERROR,
					"Action property \"" + PROP_SCRIPT + "\" is missing value for action \"" + action.getName() + "\".");

			return false;
		}

		ActionProperty envVarActionProperty = action.getActionProperty(PROP_ENV_VARS);
		if (envVarActionProperty != null && envVarActionProperty.getValue() != null)
		{
			StringTokenizer tokenizer = new StringTokenizer(envVarActionProperty.getValue(), DELIMITER);
			int count = tokenizer.countTokens();
			if (count > 0)
			{
				int index = 0;
				envVars = new String[count];

				while (tokenizer.hasMoreTokens())
				{
					envVars[index++] = tokenizer.nextToken();
				}
			}
		}



		// Prepare the command line arguments
		List actionParams = null;
		if (action != null )
		{
			actionParams = action.getActionParameters();
		} // end if



		if (parameters !=null &&  actionParams!= null)
		{
			cmdArr = new String[actionParams.size() + 1]; // + 1 for the script name
			cmdArr[0] = script;
			for (int i = 0; i < actionParams.size(); i ++) {
				ActionParameter actionParam = (ActionParameter) actionParams.get(i);
				String paramName = actionParam.getName();
				cmdArr[i+1] = parameters.get(paramName);
				// Having some element of cmdArr[] be null will cause a NullPointerException
				// when we try to pass the array to runtime.exec(), so we may as well check
				// for that here and issue a far more informative error message.
				if (cmdArr[i+1] == null) {
					actionReturn = new ActionReturn(action.getActionId(),
						CODE_UNDEFINED_SCRIPT_ERROR,
						"Action property \"" + paramName + "\" is null for action \"" + action.getName() + "\"." );
					return false;
				}
			}
		} // end if

		return true;
	}

	public ActionReturn call() throws Exception
	{
		// Error occurred during initialization
		if (actionReturn != null)
			return actionReturn;

		BufferedWriter outCommand = null;
		BufferedReader is = null;

		try {
			Runtime runtime = Runtime.getRuntime();
			// Arul


			log.debug("Script=" + script);

			// Execute shell script and read from its input stream
			Process childProcess = runtime.exec(cmdArr, envVars);

			// Wait for child to finish - This may be an issue if we block a long time
			int exitCode = childProcess.waitFor();

			is = new BufferedReader(new InputStreamReader(childProcess.getInputStream()));

			StringBuilder sb = new StringBuilder(256);
			// Need to read by char to preserve new line.
			int c;
			while ((c = is.read()) != -1) {
				char ch = (char) c;
				sb.append(ch);
			}

			String actionReturnCode = null;
			if (exitCode == 0)
				actionReturnCode = ActionReturn.CODE_SUCCESS;
			else
				actionReturnCode = ActionReturn.CODE_INTERNAL_ERROR;
			// Return exit code and output from script
			return new ActionReturn(action.getActionId(), actionReturnCode, sb.toString());
		}

		catch (InterruptedException ie)
		{
			log.error("Interrupted script - " + script);
                        log.error("Error executing script (interrupted) - " + script +" -  " + ie.toString());
                        String host = "localhost";
                        String logMessage = "<GENERICLOG ApplicationType='SYSTEM' MonitorServerName='localhost' Device='127.0.0.1' Severity='CRITICAL' MonitorStatus='CRITICAL' TextMessage='Following script has been interrupted. Script: "+script.substring(1)+"' />";
                        postLogMessage (host, logMessage);

			return new ActionReturn(action.getActionId(),
					CODE_SCRIPT_ERROR,
					"Error executing script (interrupted) - " + script + " - " + ie.toString());
                }
		catch (Exception e)
		{
			log.error("Error executing script - " + script +" -  " + e.toString());
		        String host = "localhost";
		        String logMessage = "<GENERICLOG ApplicationType='SYSTEM' MonitorServerName='localhost' Device='127.0.0.1' Severity='CRITICAL' MonitorStatus='CRITICAL' TextMessage='Following script has been interrupted. Script: "+script.substring(1)+"' />";
		        postLogMessage (host, logMessage);
		        return new ActionReturn(action.getActionId(),
					CODE_SCRIPT_ERROR,
					"Error executing script - " + script + " - " + e.toString());
		}
		finally {
			if (outCommand != null)
			{
				try {
					outCommand.close();
				} catch (Exception e) {}
			}

			if (is != null)
			{
				try {
					is.close();
				} catch (Exception e) {}
			}
		}
	}




	public void postLogMessage (String host, String logMessage)
	{
    		// Create a socket with a timeout
    		try
		{
        		InetAddress addr = InetAddress.getByName(host);
        		int port = 4913;
        		SocketAddress sockaddr = new InetSocketAddress(addr, port);

        		// Create an unbound socket
        		Socket socket = new Socket();

        		// This method will block no more than timeoutMs.
        		// If the timeout occurs, SocketTimeoutException is thrown.
        		int timeoutMs = 2000;   // 2 seconds
        		socket.connect(sockaddr, timeoutMs);

        		BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        		wr.write(logMessage);
        		wr.flush();
    		}
		catch (UnknownHostException e) {System.out.println("Error: UnkownHost "+e.toString());}
		catch (SocketTimeoutException e) {System.out.println("Error: SocketTimeout "+e.toString());}
		catch (IOException e) {System.out.println("Error: IOException "+e.toString());}
	}



}
