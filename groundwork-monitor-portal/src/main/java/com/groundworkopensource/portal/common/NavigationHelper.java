/*
 * Copyright 2009 GroundWork Open Source, Inc. ("GroundWork") All rights
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

package com.groundworkopensource.portal.common;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.net.URLEncoder;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.groundwork.foundation.ws.impl.WSClientConfiguration;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import com.groundworkopensource.portal.model.CustomGroup;
import com.groundworkopensource.portal.model.CustomGroupList;
import com.groundworkopensource.portal.model.UserNavigation;
import com.groundworkopensource.portal.model.NavigationList;
import com.groundworkopensource.portal.common.ws.impl.WebServiceLocator;

/**
 * Navigation Helper is database access class, that is used to retrieve and
 * store Navigation History objects to and from jboss portal database.
 * 
 * @author nitin_jadhav
 * @version GWMON - 6.1.1
 */
public class NavigationHelper {

	/**
	 * /** Logger
	 */
	private static final Logger LOGGER = Logger
			.getLogger(NavigationHelper.class);

	private static final String USERNAVIGATION_PATH = "usertabpersistance/";

	/**
	 * Returns all Navigation History records for provided user Id.
	 * 
	 * 
	 * @param userId
	 * @param app_type
	 * @return List
	 * @throws IOException
	 */

	public NavigationHelper() {
	};

	@SuppressWarnings("unchecked")
	public List<UserNavigation> getHistoryRecords(String userId, String app_type)
			throws IOException {
		DefaultHttpClient httpClient = null;
		List<UserNavigation> list = null;
		HttpResponse response = null;
		// Take base endpoint and append with path & path param
		String USERNAVIGATION_ENDPOINT = WebServiceLocator.getInstance()
				.portalExtnRESTeasyURL() + USERNAVIGATION_PATH + "gethistory";
		try {
			httpClient = new DefaultHttpClient();
			String username = WSClientConfiguration.getProperty(WSClientConfiguration.WEBSERVICES_USERNAME);
            String password = WSClientConfiguration.getProperty(WSClientConfiguration.WEBSERVICES_PASSWORD);
			httpClient.getCredentialsProvider().setCredentials(
                    new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
                    new UsernamePasswordCredentials(username, password));
			StringBuilder builder = new StringBuilder();
			builder.append("userId");
			builder.append("=");
			builder.append(URLEncoder.encode(userId));
			builder.append("&");
			builder.append("app_type");
			builder.append("=");
			builder.append(URLEncoder.encode(app_type));
			HttpGet getRequest = new HttpGet(USERNAVIGATION_ENDPOINT + "?"
					+ builder.toString());
			getRequest.addHeader("accept", "application/xml");

			response = httpClient.execute(getRequest);
			if (response.getStatusLine().getStatusCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : "
						+ response.getStatusLine().getStatusCode());
			}
			JAXBContext context = JAXBContext.newInstance(NavigationList.class);
			Unmarshaller um = context.createUnmarshaller();
			NavigationList naviList = (NavigationList) um
					.unmarshal(new StreamSource(new InputStreamReader((response
							.getEntity().getContent()))));

			Collection<UserNavigation> naviCol = naviList.getList();
			if (naviCol != null) {
				list = new ArrayList<UserNavigation>(naviCol);
			} else {
				// empty list
				list = new ArrayList<UserNavigation>();
			}

		} catch (Exception exc) {
			LOGGER.error("HTTP error: "
					+ response.getStatusLine().getStatusCode() + ", "
					+ response.getStatusLine().getReasonPhrase());
			throw new IOException();
		} finally {
			if (httpClient != null)
				httpClient.getConnectionManager().shutdown();
		}
		return list;
	}

	/**
	 * This method is used to add single record to Navigation History database.<br>
	 * Note: record Id is not provided. Its auto generated by database.
	 * 
	 * @param userId
	 * @param nodeId
	 * @param nodeName
	 * @param nodeType
	 * @param parentInfo
	 * @param toolTip
	 * @param app_type
	 * @throws IOException
	 */
	public void addHistoryRecord(String userId, int nodeId, String nodeName,
			String nodeType, String parentInfo, String toolTip, String app_type)
			throws IOException {
		DefaultHttpClient httpClient = null;
		List<UserNavigation> list = null;
		HttpResponse response = null;
		// Take base endpoint and append with path & path param
		String USERNAVIGATION_ENDPOINT = WebServiceLocator.getInstance()
				.portalExtnRESTeasyURL()
				+ USERNAVIGATION_PATH
				+ "addwithoutlabel";
		try {
			httpClient = new DefaultHttpClient();
			String username = WSClientConfiguration.getProperty(WSClientConfiguration.WEBSERVICES_USERNAME);
            String password = WSClientConfiguration.getProperty(WSClientConfiguration.WEBSERVICES_PASSWORD);
			httpClient.getCredentialsProvider().setCredentials(
                    new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
                    new UsernamePasswordCredentials(username, password));
			StringBuilder builder = new StringBuilder();
			builder.append("userId");
			builder.append("=");
			builder.append(URLEncoder.encode(userId));
			builder.append("&");
			builder.append("nodeId");
			builder.append("=");
			builder.append(URLEncoder.encode(String.valueOf(nodeId)));
			builder.append("&");
			builder.append("nodeName");
			builder.append("=");
			builder.append(URLEncoder.encode(nodeName));
			builder.append("&");
			builder.append("nodeType");
			builder.append("=");
			builder.append(URLEncoder.encode(nodeType));
			builder.append("&");
			builder.append("parentInfo");
			builder.append("=");
			builder.append(URLEncoder.encode(parentInfo == null ? ""
					: parentInfo));
			builder.append("&");
			builder.append("toolTip");
			builder.append("=");
			builder.append(URLEncoder.encode(toolTip));
			builder.append("&");
			builder.append("app_type");
			builder.append("=");
			builder.append(URLEncoder.encode(app_type));
			HttpGet getRequest = new HttpGet(USERNAVIGATION_ENDPOINT + "?"
					+ builder.toString());
			getRequest.addHeader("accept", "application/xml");

			response = httpClient.execute(getRequest);
			if (response.getStatusLine().getStatusCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : "
						+ response.getStatusLine().getStatusCode());
			}

		} catch (Exception exc) {
			LOGGER.error("HTTP error: "
					+ response.getStatusLine().getStatusCode() + ", "
					+ response.getStatusLine().getReasonPhrase());
			throw new IOException();
		} finally {
			if (httpClient != null)
				httpClient.getConnectionManager().shutdown();
		}
	}

	/**
	 * This method is used to add single record to Navigation History database.<br>
	 * Note: record Id is not provided. Its auto generated by database.
	 * 
	 * @param userId
	 * @param nodeId
	 * @param nodeName
	 * @param nodeType
	 * @param parentInfo
	 * @param toolTip
	 * @param app_type
	 * @param nodeLabel
	 * @throws IOException
	 */
	public void addHistoryRecord(String userId, int nodeId, String nodeName,
			String nodeType, String parentInfo, String toolTip,
			String app_type, String nodeLabel) throws IOException {
		DefaultHttpClient httpClient = null;
		List<UserNavigation> list = null;
		HttpResponse response = null;
		// Take base endpoint and append with path & path param
		String USERNAVIGATION_ENDPOINT = WebServiceLocator.getInstance()
				.portalExtnRESTeasyURL()
				+ USERNAVIGATION_PATH
				+ "addwithlabel";
		try {
			httpClient = new DefaultHttpClient();
			String username = WSClientConfiguration.getProperty(WSClientConfiguration.WEBSERVICES_USERNAME);
            String password = WSClientConfiguration.getProperty(WSClientConfiguration.WEBSERVICES_PASSWORD);
			httpClient.getCredentialsProvider().setCredentials(
                    new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
                    new UsernamePasswordCredentials(username, password));
			StringBuilder builder = new StringBuilder();
			builder.append("userId");
			builder.append("=");
			builder.append(URLEncoder.encode(userId));
			builder.append("&");
			builder.append("nodeId");
			builder.append("=");
			builder.append(URLEncoder.encode(String.valueOf(nodeId)));
			builder.append("&");
			builder.append("nodeName");
			builder.append("=");
			builder.append(URLEncoder.encode(nodeName));
			builder.append("&");
			builder.append("nodeType");
			builder.append("=");
			builder.append(URLEncoder.encode(nodeType));
			builder.append("&");
			builder.append("parentInfo");
			builder.append("=");
			builder.append(URLEncoder.encode(parentInfo == null ? "" : parentInfo));
			builder.append("&");
			builder.append("toolTip");
			builder.append("=");
			builder.append(URLEncoder.encode(toolTip == null ? "" : toolTip));
			builder.append("&");
			builder.append("app_type");
			builder.append("=");
			builder.append(URLEncoder.encode(app_type));
			builder.append("&");
			builder.append("nodeLabel");
			builder.append("=");
			builder.append(URLEncoder.encode(nodeLabel));
			HttpGet getRequest = new HttpGet(USERNAVIGATION_ENDPOINT + "?"
					+ builder.toString());
			getRequest.addHeader("accept", "application/xml");

			response = httpClient.execute(getRequest);
			if (response.getStatusLine().getStatusCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : "
						+ response.getStatusLine().getStatusCode());
			}

		} catch (Exception exc) {
			LOGGER.error("HTTP error: "
					+ response.getStatusLine().getStatusCode() + ", "
					+ response.getStatusLine().getReasonPhrase());
			throw new IOException();
		} finally {
			if (httpClient != null)
				httpClient.getConnectionManager().shutdown();
		}
	}

	/**
	 * This method is used to delete a single record of a user identified by
	 * "userId" from Navigation History database.
	 * 
	 * @param userId
	 * @param nodeId
	 * @param nodeType
	 * @param app_type
	 * @throws IOException
	 */
	public void deleteHistoryRecord(String userId, int nodeId, String nodeType,
			String app_type) throws IOException {
		DefaultHttpClient httpClient = null;
		List<UserNavigation> list = null;
		HttpResponse response = null;
		// Take base endpoint and append with path & path param
		String USERNAVIGATION_ENDPOINT = WebServiceLocator.getInstance()
				.portalExtnRESTeasyURL()
				+ USERNAVIGATION_PATH
				+ "deletenodewithtype";
		try {
			httpClient = new DefaultHttpClient();
			String username = WSClientConfiguration.getProperty(WSClientConfiguration.WEBSERVICES_USERNAME);
            String password = WSClientConfiguration.getProperty(WSClientConfiguration.WEBSERVICES_PASSWORD);
			httpClient.getCredentialsProvider().setCredentials(
                    new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
                    new UsernamePasswordCredentials(username, password));
			StringBuilder builder = new StringBuilder();
			builder.append("userId");
			builder.append("=");
			builder.append(URLEncoder.encode(userId));
			builder.append("&");
			builder.append("nodeId");
			builder.append("=");
			builder.append(URLEncoder.encode(String.valueOf(nodeId)));
			builder.append("&");
			builder.append("nodeType");
			builder.append("=");
			builder.append(URLEncoder.encode(nodeType));
			builder.append("&");
			builder.append("app_type");
			builder.append("=");
			builder.append(URLEncoder.encode(app_type));
			HttpGet getRequest = new HttpGet(USERNAVIGATION_ENDPOINT + "?"
					+ builder.toString());
			getRequest.addHeader("accept", "application/xml");

			response = httpClient.execute(getRequest);
			if (response.getStatusLine().getStatusCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : "
						+ response.getStatusLine().getStatusCode());
			}

		} catch (Exception exc) {
			LOGGER.error("HTTP error: "
					+ response.getStatusLine().getStatusCode() + ", "
					+ response.getStatusLine().getReasonPhrase());
			throw new IOException();
		} finally {
			if (httpClient != null)
				httpClient.getConnectionManager().shutdown();
		}
	}

	/**
	 * This method is used to delete all records of a user identified by
	 * "userId" from Navigation History database.
	 * 
	 * @param userId
	 * @param app_type
	 * @throws IOException
	 */
	public void deleteAllHistoryRecords(String userId, String app_type)
			throws IOException {
		DefaultHttpClient httpClient = null;
		List<UserNavigation> list = null;
		HttpResponse response = null;
		// Take base endpoint and append with path & path param
		String USERNAVIGATION_ENDPOINT = WebServiceLocator.getInstance()
				.portalExtnRESTeasyURL() + USERNAVIGATION_PATH + "deleteall";
		try {
			httpClient = new DefaultHttpClient();
			String username = WSClientConfiguration.getProperty(WSClientConfiguration.WEBSERVICES_USERNAME);
            String password = WSClientConfiguration.getProperty(WSClientConfiguration.WEBSERVICES_PASSWORD);
			httpClient.getCredentialsProvider().setCredentials(
                    new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
                    new UsernamePasswordCredentials(username, password));
			StringBuilder builder = new StringBuilder();
			builder.append("userId");
			builder.append("=");
			builder.append(URLEncoder.encode(userId));
			builder.append("&");
			builder.append("app_type");
			builder.append("=");
			builder.append(URLEncoder.encode(app_type));
			HttpGet getRequest = new HttpGet(USERNAVIGATION_ENDPOINT + "?"
					+ builder.toString());
			getRequest.addHeader("accept", "application/xml");

			response = httpClient.execute(getRequest);
			if (response.getStatusLine().getStatusCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : "
						+ response.getStatusLine().getStatusCode());
			}

		} catch (Exception exc) {
			LOGGER.error("HTTP error: "
					+ response.getStatusLine().getStatusCode() + ", "
					+ response.getStatusLine().getReasonPhrase());
			throw new IOException();
		} finally {
			if (httpClient != null)
				httpClient.getConnectionManager().shutdown();
		}
	}

	/**
	 * Get Max node id
	 * 
	 * @param userid
	 * @param app_type
	 * @throws IOException
	 */
	public int getMaxNodeID(String userId, String app_type) throws IOException {
		DefaultHttpClient httpClient = null;
		HttpResponse response = null;
		// Take base endpoint and append with path & path param
		String USERNAVIGATION_ENDPOINT = WebServiceLocator.getInstance()
				.portalExtnRESTeasyURL() + USERNAVIGATION_PATH + "getmaxnodeid";
		try {
			httpClient = new DefaultHttpClient();
			String username = WSClientConfiguration.getProperty(WSClientConfiguration.WEBSERVICES_USERNAME);
            String password = WSClientConfiguration.getProperty(WSClientConfiguration.WEBSERVICES_PASSWORD);
			httpClient.getCredentialsProvider().setCredentials(
                    new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
                    new UsernamePasswordCredentials(username, password));
			StringBuilder builder = new StringBuilder();
			builder.append("userId");
			builder.append("=");
			builder.append(URLEncoder.encode(userId));
			builder.append("&");
			builder.append("app_type");
			builder.append("=");
			builder.append(URLEncoder.encode(app_type));
			HttpGet getRequest = new HttpGet(USERNAVIGATION_ENDPOINT + "?"
					+ builder.toString());
			getRequest.addHeader("accept", "text/plain");

			response = httpClient.execute(getRequest);
			if (response.getStatusLine().getStatusCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : "
						+ response.getStatusLine().getStatusCode());
			}
			return Integer.parseInt(EntityUtils.toString(response.getEntity()));
		} catch (Exception exc) {
			LOGGER.error("HTTP error: "
					+ response.getStatusLine().getStatusCode() + ", "
					+ response.getStatusLine().getReasonPhrase());
			throw new IOException();
		} finally {
			if (httpClient != null)
				httpClient.getConnectionManager().shutdown();
		}
	}

	/**
	 * 
	 * @param userId
	 * @param nodeId
	 * @param nodeName
	 * @param nodeType
	 * @param app_type
	 * @return boolean
	 * @throws IOException
	 */
	public boolean updateHistoryRecord(String userId, int nodeId,
			String nodeName, String nodeType, String app_type)
			throws IOException {
		DefaultHttpClient httpClient = null;
		boolean result = false;
		HttpResponse response = null;
		// Take base endpoint and append with path & path param
		String USERNAVIGATION_ENDPOINT = WebServiceLocator.getInstance()
				.portalExtnRESTeasyURL()
				+ USERNAVIGATION_PATH
				+ "updatewithoutlabel";
		try {
			httpClient = new DefaultHttpClient();
			String username = WSClientConfiguration.getProperty(WSClientConfiguration.WEBSERVICES_USERNAME);
            String password = WSClientConfiguration.getProperty(WSClientConfiguration.WEBSERVICES_PASSWORD);
			httpClient.getCredentialsProvider().setCredentials(
                    new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
                    new UsernamePasswordCredentials(username, password));
			StringBuilder builder = new StringBuilder();
			builder.append("userId");
			builder.append("=");
			builder.append(URLEncoder.encode(userId));
			builder.append("&");
			builder.append("nodeId");
			builder.append("=");
			builder.append(URLEncoder.encode(String.valueOf(nodeId)));
			builder.append("&");
			builder.append("nodeName");
			builder.append("=");
			builder.append(URLEncoder.encode(nodeName));
			builder.append("&");
			builder.append("nodeType");
			builder.append("=");
			builder.append(URLEncoder.encode(nodeType));
			builder.append("&");
			builder.append("app_type");
			builder.append("=");
			builder.append(URLEncoder.encode(app_type));
			HttpGet getRequest = new HttpGet(USERNAVIGATION_ENDPOINT + "?"
					+ builder.toString());
			getRequest.addHeader("accept", "application/xml");

			response = httpClient.execute(getRequest);
			if (response.getStatusLine().getStatusCode() != 200) {
				result = true;
			}

		} catch (Exception exc) {
			LOGGER.error("HTTP error: "
					+ response.getStatusLine().getStatusCode() + ", "
					+ response.getStatusLine().getReasonPhrase());
			throw new IOException();
		} finally {
			if (httpClient != null)
				httpClient.getConnectionManager().shutdown();
		}
		return result;
	}

	/**
	 * Update Node_Name and Node_type to Navigation History database.
	 * 
	 * @param userId
	 * @param nodeId
	 * @param nodeName
	 * @param nodeType
	 * @param app_type
	 * @param tabHistory
	 * @param nodeLabel
	 * @return boolean
	 * @throws IOException
	 */
	public boolean updateHistoryRecord(String userId, int nodeId,
			String nodeName, String nodeType, String app_type,
			String tabHistory, String nodeLabel) throws IOException {
		DefaultHttpClient httpClient = null;
		boolean result = false;
		HttpResponse response = null;
		// Take base endpoint and append with path & path param
		String USERNAVIGATION_ENDPOINT = WebServiceLocator.getInstance()
				.portalExtnRESTeasyURL()
				+ USERNAVIGATION_PATH
				+ "updatewithlabel";
		try {
			httpClient = new DefaultHttpClient();
			String username = WSClientConfiguration.getProperty(WSClientConfiguration.WEBSERVICES_USERNAME);
            String password = WSClientConfiguration.getProperty(WSClientConfiguration.WEBSERVICES_PASSWORD);
			httpClient.getCredentialsProvider().setCredentials(
                    new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
                    new UsernamePasswordCredentials(username, password));
			StringBuilder builder = new StringBuilder();
			builder.append("userId");
			builder.append("=");
			builder.append(URLEncoder.encode(userId));
			builder.append("&");
			builder.append("nodeId");
			builder.append("=");
			builder.append(URLEncoder.encode(String.valueOf(nodeId)));
			builder.append("&");
			builder.append("nodeName");
			builder.append("=");
			builder.append(URLEncoder.encode(nodeName));
			builder.append("&");
			builder.append("nodeType");
			builder.append("=");
			builder.append(URLEncoder.encode(nodeType));
			builder.append("&");
			builder.append("app_type");
			builder.append("=");
			builder.append(URLEncoder.encode(app_type));
			builder.append("&");
			builder.append("tabHistory");
			builder.append("=");
			builder.append(URLEncoder.encode(tabHistory == null ? ""
					: tabHistory));
			builder.append("&");
			builder.append("nodeLabel");
			builder.append("=");
			builder.append(URLEncoder.encode(nodeLabel));
			HttpGet getRequest = new HttpGet(USERNAVIGATION_ENDPOINT + "?"
					+ builder.toString());
			getRequest.addHeader("accept", "application/xml");

			response = httpClient.execute(getRequest);
			if (response.getStatusLine().getStatusCode() != 200) {
				result = true;
			}

		} catch (Exception exc) {
			LOGGER.error("HTTP error: "
					+ response.getStatusLine().getStatusCode() + ", "
					+ response.getStatusLine().getReasonPhrase());
			throw new IOException();
		} finally {
			if (httpClient != null)
				httpClient.getConnectionManager().shutdown();
		}
		return result;
	}

	/**
	 * Update tab history column
	 * 
	 * @param userId
	 * @param nodeId
	 * @param app_type
	 * @param tabHistory
	 * @return boolean
	 * @throws IOException
	 */
	public boolean updateTabHistoryRecord(String userId, int nodeId,
			String app_type, String tabHistory) throws IOException {
		DefaultHttpClient httpClient = null;
		boolean result = false;
		HttpResponse response = null;
		// Take base endpoint and append with path & path param
		String USERNAVIGATION_ENDPOINT = WebServiceLocator.getInstance()
				.portalExtnRESTeasyURL()
				+ USERNAVIGATION_PATH
				+ "updatetabhistoryfield";
		try {
			httpClient = new DefaultHttpClient();
			String username = WSClientConfiguration.getProperty(WSClientConfiguration.WEBSERVICES_USERNAME);
            String password = WSClientConfiguration.getProperty(WSClientConfiguration.WEBSERVICES_PASSWORD);
			httpClient.getCredentialsProvider().setCredentials(
                    new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
                    new UsernamePasswordCredentials(username, password));
			StringBuilder builder = new StringBuilder();
			builder.append("userId");
			builder.append("=");
			builder.append(URLEncoder.encode(userId));
			builder.append("&");
			builder.append("nodeId");
			builder.append("=");
			builder.append(URLEncoder.encode(String.valueOf(nodeId)));
			builder.append("&");
			builder.append("app_type");
			builder.append("=");
			builder.append(URLEncoder.encode(app_type));
			builder.append("&");
			builder.append("tabHistory");
			builder.append("=");
			builder.append(URLEncoder.encode(tabHistory == null ? ""
					: tabHistory));
			HttpGet getRequest = new HttpGet(USERNAVIGATION_ENDPOINT + "?"
					+ builder.toString());
			getRequest.addHeader("accept", "application/xml");

			response = httpClient.execute(getRequest);
			if (response.getStatusLine().getStatusCode() != 200) {
				result = true;
			}

		} catch (Exception exc) {
			LOGGER.error("HTTP error: "
					+ response.getStatusLine().getStatusCode() + ", "
					+ response.getStatusLine().getReasonPhrase());
			throw new IOException();
		} finally {
			if (httpClient != null)
				httpClient.getConnectionManager().shutdown();
		}
		return result;
	}

	/**
	 * Update Node label column
	 * 
	 * @param userId
	 * @param nodeId
	 * @param app_type
	 * @param nodeLabel
	 * @return boolean
	 * @throws IOException
	 */
	public boolean updateNodeLabelRecord(String userId, int nodeId,
			String app_type, String nodeLabel) throws IOException {
		DefaultHttpClient httpClient = null;
		boolean result = false;
		HttpResponse response = null;
		// Take base endpoint and append with path & path param
		String USERNAVIGATION_ENDPOINT = WebServiceLocator.getInstance()
				.portalExtnRESTeasyURL()
				+ USERNAVIGATION_PATH
				+ "updatenodelabel";
		try {
			httpClient = new DefaultHttpClient();
			String username = WSClientConfiguration.getProperty(WSClientConfiguration.WEBSERVICES_USERNAME);
            String password = WSClientConfiguration.getProperty(WSClientConfiguration.WEBSERVICES_PASSWORD);
			httpClient.getCredentialsProvider().setCredentials(
                    new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
                    new UsernamePasswordCredentials(username, password));
			StringBuilder builder = new StringBuilder();
			builder.append("userId");
			builder.append("=");
			builder.append(URLEncoder.encode(userId));
			builder.append("&");
			builder.append("nodeId");
			builder.append("=");
			builder.append(URLEncoder.encode(String.valueOf(nodeId)));
			builder.append("&");
			builder.append("app_type");
			builder.append("=");
			builder.append(URLEncoder.encode(app_type));
			builder.append("&");
			builder.append("nodeLabel");
			builder.append("=");
			builder.append(URLEncoder
					.encode(nodeLabel == null ? "" : nodeLabel));
			HttpGet getRequest = new HttpGet(USERNAVIGATION_ENDPOINT + "?"
					+ builder.toString());
			getRequest.addHeader("accept", "application/xml");

			response = httpClient.execute(getRequest);
			if (response.getStatusLine().getStatusCode() != 200) {
				result = true;
			}

		} catch (Exception exc) {
			LOGGER.error("HTTP error: "
					+ response.getStatusLine().getStatusCode() + ", "
					+ response.getStatusLine().getReasonPhrase());
			throw new IOException();
		} finally {
			if (httpClient != null)
				httpClient.getConnectionManager().shutdown();
		}
		return result;
	}

	/**
	 * This method is used to delete a single record of a user identified by
	 * "userId" from Navigation History database.
	 * 
	 * @param userId
	 * @param nodeId
	 * @param app_type
	 * @throws IOException
	 */
	public void deleteHistoryRecord(String userId, int nodeId, String app_type)
			throws IOException {
		DefaultHttpClient httpClient = null;
		List<UserNavigation> list = null;
		HttpResponse response = null;
		// Take base endpoint and append with path & path param
		String USERNAVIGATION_ENDPOINT = WebServiceLocator.getInstance()
				.portalExtnRESTeasyURL() + USERNAVIGATION_PATH + "deletenode";
		try {
			httpClient = new DefaultHttpClient();
			String username = WSClientConfiguration.getProperty(WSClientConfiguration.WEBSERVICES_USERNAME);
            String password = WSClientConfiguration.getProperty(WSClientConfiguration.WEBSERVICES_PASSWORD);
			httpClient.getCredentialsProvider().setCredentials(
                    new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
                    new UsernamePasswordCredentials(username, password));
			StringBuilder builder = new StringBuilder();
			builder.append("userId");
			builder.append("=");
			builder.append(URLEncoder.encode(userId));
			builder.append("&");
			builder.append("nodeId");
			builder.append("=");
			builder.append(URLEncoder.encode(String.valueOf(nodeId)));
			builder.append("&");
			builder.append("app_type");
			builder.append("=");
			builder.append(URLEncoder.encode(app_type));
			HttpGet getRequest = new HttpGet(USERNAVIGATION_ENDPOINT + "?"
					+ builder.toString());
			getRequest.addHeader("accept", "application/xml");

			response = httpClient.execute(getRequest);
			if (response.getStatusLine().getStatusCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : "
						+ response.getStatusLine().getStatusCode());
			}

		} catch (Exception exc) {
			LOGGER.error("HTTP error: "
					+ response.getStatusLine().getStatusCode() + ", "
					+ response.getStatusLine().getReasonPhrase());
			throw new IOException();
		} finally {
			if (httpClient != null)
				httpClient.getConnectionManager().shutdown();
		}
	}
}
