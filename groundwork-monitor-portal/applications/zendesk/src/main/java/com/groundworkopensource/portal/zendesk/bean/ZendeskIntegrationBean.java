/*
 * 
 * Copyright 2007 GroundWork Open Source, Inc. ("GroundWork") All rights
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

package com.groundworkopensource.portal.zendesk.bean;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.log4j.Logger;


/**
 * @author nitin_jadhav
 * 
 */
public class ZendeskIntegrationBean {
	private String zenURL;
	private String userId = null;
	private String token = null;
	private String zendeskLoginUrl = null;

	/**
	 * logger
	 */
	Logger LOGGER = Logger.getLogger(this.getClass().getName());

	public ZendeskIntegrationBean(String userId, String token,
			String zendeskLoginUrl) {
		this.userId = userId;
		this.token = token;
		this.zendeskLoginUrl = zendeskLoginUrl;
	}

	/**
	 * Sets the zenURL.
	 * 
	 * @param zenURL
	 *            the zenURL to set
	 */
	public void setZenURL(String zenURL) {
		this.zenURL = zenURL;
	}

	/**
	 * Returns the zenURL.
	 * 
	 * @return the zenURL
	 */
	public String getZenURL() {
		GetMethod authget = null;
		try {
			StringBuffer completeURL = new StringBuffer();
			/*
			 * String userId = FacesUtils.getPreference("useridPref"); String
			 * token = FacesUtils.getPreference("zendeskToken"); String
			 * zendeskUrl = FacesUtils.getPreference("zendeskUrlPref");
			 */
			HttpClient client = new HttpClient();
			authget = new GetMethod(zendeskLoginUrl);
			client.executeMethod(authget);
			String queryString = authget.getQueryString();
			if (queryString != null) {
				Map paramMap = ZendeskIntegrationBean.getQueryMap(queryString);
				completeURL.append("https://");
				completeURL.append(authget.getHostConfiguration().getHost());
				completeURL.append("/access/remote/?");
				completeURL.append("name=");
				completeURL.append(userId);
				completeURL.append("&");
				completeURL.append("email=");
				completeURL.append(userId);
				completeURL.append("&");
				completeURL.append("timestamp=");
				String timestamp = (String) paramMap.get("timestamp");
				completeURL.append(timestamp);
				completeURL.append("&");
				completeURL.append("return_to=");
				completeURL.append(zendeskLoginUrl);
				completeURL.append("&");
				completeURL.append("hash=");
				StringBuffer hashbuffer = new StringBuffer();
				hashbuffer.append(userId);
				hashbuffer.append(userId);
				hashbuffer.append(token);
				hashbuffer.append(timestamp);
				String hashValue = md5hash(hashbuffer.toString());
				completeURL.append(hashValue);
				zenURL = completeURL.toString();
			} // end if
			LOGGER.info("zen url -> " + zenURL);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Error in reading user preferences");
		} finally {
			if (authget != null) {
				authget.releaseConnection();
			}
		}
		// LOGGER.error("zen url -> " + zenURL);
		return zenURL;
	}

	/**
	 * Gets the query map from the query string.
	 */
	public static Map<String, String> getQueryMap(String query) {
		String[] params = query.split("&");
		Map<String, String> map = new HashMap<String, String>();
		for (String param : params) {
			String name = param.split("=")[0];
			String value = param.split("=")[1];
			map.put(name, value);
		}
		return map;
	}

	/**
	 * MD5Hash algorithm for the buffer
	 */
	private String md5hash(String buffer) {
		StringBuffer hexString = new StringBuffer();
		byte[] defaultBytes = buffer.getBytes();
		try {
			MessageDigest algorithm = MessageDigest.getInstance("MD5");
			algorithm.reset();
			algorithm.update(defaultBytes);
			byte messageDigest[] = algorithm.digest();

			for (int i = 0; i < messageDigest.length; i++) {
				String hex = Integer.toHexString(0xFF & messageDigest[i]);
				if (hex.length() == 1) {
					hexString.append('0');
				}
				hexString.append(hex);
			}
		} catch (NoSuchAlgorithmException nsae) {
			LOGGER.error(nsae.getMessage());
		}
		return hexString.toString();
	}

}
