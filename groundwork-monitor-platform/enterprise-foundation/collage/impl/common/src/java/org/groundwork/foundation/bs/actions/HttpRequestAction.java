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

import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.groundwork.collage.model.Action;
import com.groundwork.collage.model.ActionProperty;
import com.groundwork.collage.model.impl.ActionParameter;
import com.groundwork.collage.model.impl.ActionReturn;

public class HttpRequestAction extends FoundationActionImpl {
	// Action Properties
	private static final String PROP_URL = "URL";
	private static final String PROP_READ_TIMEOUT = "ReadTimeout";
	private static final String PROP_CONNECTION_TIMEOUT = "ConnectionTimeout";

	// Error Codes
	public static final String CODE_UNDEFINED_URL_ERROR = "UNDEFINED_URL_PROPERTY";
	public static final String CODE_UNDEFINED_SCRIPT_ERROR = "UNDEFINED_SCRIPT_PROPERTY";
	public static final String CODE_SCRIPT_ERROR = "ERORR_EXECUTING_SCRIPT";

	private String httpRequest = null;
	private int readTimeout = 0;
	private int connectionTimeout = 0;
	private StringBuffer data = null;
	private static final String PARAM_DELIMITER = "&";
	private static final String ENCODING = "UTF-8";
	/** Enable Logging * */
	protected static Log log = LogFactory.getLog(HttpRequestAction.class);

	/**
	 * Initialize action and insure all properties and parameters are provided.
	 */
	public boolean initialize(Action action, Map<String, String> parameters) {
		if (super.initialize(action, parameters) == false)
			return false;

		ActionProperty urlProperty = action.getActionProperty(PROP_URL);
		if (urlProperty == null) {
			actionReturn = new ActionReturn(action.getActionId(),
					CODE_UNDEFINED_URL_ERROR, "Action property not defined - "
							+ PROP_URL);
			return false;
		}

		httpRequest = urlProperty.getValue();
		if (httpRequest == null || httpRequest.length() == 0) {
			actionReturn = new ActionReturn(action.getActionId(),
					CODE_UNDEFINED_URL_ERROR,
					"Action property missing value - " + PROP_URL);
			return false;
		}

		ActionProperty readTimeoutProperty = action
				.getActionProperty(PROP_READ_TIMEOUT);
		if (readTimeoutProperty != null) {
			String val = readTimeoutProperty.getValue();
			if (val != null && val.length() > 0) {
				try {
					readTimeout = Integer.parseInt(val);
				} catch (Exception e) {
				}
			}
		}

		ActionProperty connTimeoutProperty = action
				.getActionProperty(PROP_CONNECTION_TIMEOUT);
		if (connTimeoutProperty != null) {
			String val = connTimeoutProperty.getValue();
			if (val != null && val.length() > 0) {
				try {
					connectionTimeout = Integer.parseInt(val);
				} catch (Exception e) {
				}
			}
		}

		// Prepare the command line arguments
		List actionParams = null;
		if (action != null) {
			actionParams = action.getActionParameters();

		} // end if

		if (parameters != null && actionParams != null) {
			data = new StringBuffer();
			Iterator<ActionParameter> iter = actionParams.iterator();
			while (iter.hasNext())

			{
				ActionParameter actionParam = iter.next();
				try {
					String paramName = actionParam.getName();
					data.append(URLEncoder.encode(paramName, ENCODING));
					data.append("=");
					data.append(URLEncoder.encode(parameters.get(paramName),
							ENCODING));
					data.append(PARAM_DELIMITER);
				} catch (Exception exc) {
					log.error("Error occurred while encoding the URL"
							+ exc.getMessage());
				} // end if
			} // end while
		} // end if

		return true;
	}

	public ActionReturn call() throws Exception {
		// Error occurred during initialization
		if (actionReturn != null)
			return actionReturn;

		HttpURLConnection connection = null;
		InputStream inputStream = null;
		OutputStreamWriter wr = null;

		try {

			URL url = new URL(httpRequest);

			connection = (HttpURLConnection) url.openConnection();

			connection.setReadTimeout(readTimeout);
			connection.setConnectTimeout(connectionTimeout);
			// String data = URLEncoder.encode("user", "UTF-8") + "=" +
			// URLEncoder.encode("test-arul", "UTF-8");
			// data += "&" + URLEncoder.encode("LogMessageIds", "UTF-8") + "=" +
			// URLEncoder.encode("test_value2", "UTF-8");
			if (data != null) {
				String queryString = data.substring(0, data.length());
				connection.setDoOutput(true);
				wr = new OutputStreamWriter(connection.getOutputStream());
				wr.write(queryString);
				wr.flush();
				inputStream = connection.getInputStream();
			} else {
				log.error("Error sending http request - No Data found");

			} // end if

			// Read all bytes
			StringBuilder sb = new StringBuilder(2048);
			byte[] ba = new byte[2048];

			while ((inputStream.read(ba, 0, 2048)) > 0) {
				sb.append(ba);
			}

			// Return response code and response
			return new ActionReturn(action.getActionId(), Integer
					.toString(connection.getResponseCode()), sb.toString());

		} catch (Exception e) {
			log.error("Error sending http request - " + httpRequest, e);

			return new ActionReturn(action.getActionId(), CODE_SCRIPT_ERROR,
					"Error sending http request - " + httpRequest + " - "
							+ e.toString());
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (Exception e) // Suppress exception
				{
				}
			}
			if (wr != null) {
				try {
					wr.close();
				} catch (Exception e) // Suppress exception
				{
				}
			}

			if (connection != null) {
				connection.disconnect();
			}
		}
	}
}
