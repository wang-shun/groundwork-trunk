package com.groundworkopensource.portal.common.ws.impl;

import java.io.InputStreamReader;
import java.io.StringReader;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.rpc.ServiceException;
import javax.xml.transform.stream.StreamSource;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.groundwork.foundation.ws.impl.WSClientConfiguration;
import org.apache.log4j.Logger;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;

import com.groundworkopensource.portal.common.exception.WSDataUnavailableException;
import com.groundworkopensource.portal.model.CustomGroup;
import com.groundworkopensource.portal.model.CustomGroupList;
import com.groundworkopensource.portal.common.ws.ICustomGroupWSFacade;
import com.groundworkopensource.portal.model.EntityType;
import com.groundworkopensource.portal.model.EntityTypeList;

/**
 * This class provides methods to interact with "CustomGroup" from JBOSS REST
 * service.
 * 
 * @author Arul
 * 
 */

public class CustomGroupWSFacade implements ICustomGroupWSFacade {

	/**
	 * logger
	 */
	private static final Logger LOGGER = FoundationWSFacade.getLogger();

	/**
	 * returns all available custom groups
	 * 
	 * @return Collection<CustomGroup>
	 * @throws WSDataUnavailableException
	 */

	public Collection<CustomGroup> findCustomGroups()
			throws WSDataUnavailableException {
		DefaultHttpClient httpClient = null;
		Collection<CustomGroup> groups = null;
		HttpResponse response = null;
		// Take base endpoint and append with path & path param
		String CUSTOMGROUPS_ENDPOINT = WebServiceLocator.getInstance()
				.portalExtnRESTeasyURL() + "customgroup/findcustomgroups";
		try {
			httpClient = new DefaultHttpClient();
			String username = WSClientConfiguration.getProperty(WSClientConfiguration.WEBSERVICES_USERNAME);
            String password = WSClientConfiguration.getProperty(WSClientConfiguration.WEBSERVICES_PASSWORD);
			httpClient.getCredentialsProvider().setCredentials(
                    new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
                    new UsernamePasswordCredentials(username, password));
			HttpGet getRequest = new HttpGet(CUSTOMGROUPS_ENDPOINT);
			getRequest.addHeader("accept", "application/xml");
			response = httpClient.execute(getRequest);
			if (response.getStatusLine().getStatusCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : "
						+ response.getStatusLine().getStatusCode());
			}
			JAXBContext context = JAXBContext
					.newInstance(CustomGroupList.class);
			Unmarshaller um = context.createUnmarshaller();
			CustomGroupList groupList = (CustomGroupList) um
					.unmarshal(new StreamSource(new InputStreamReader((response
							.getEntity().getContent()))));
			groups = groupList.getList();
			if (groups == null)
				groups = new ArrayList<CustomGroup>();

		} catch (Exception exc) {
			LOGGER.error("HTTP error: "
					+ response.getStatusLine().getStatusCode() + ", "
					+ response.getStatusLine().getReasonPhrase());
			throw new WSDataUnavailableException();
		} finally {
			if (httpClient != null)
				httpClient.getConnectionManager().shutdown();
		}
		return groups;
	}

	/**
	 * Create customgroup.
	 * 
	 * @throws WSDataUnavailableException
	 * @throws GWPortalException
	 */
	public void createCustomGroup(String groupName, int entityTypeId,
			String parents, String groupState, String createdBy, String children)
			throws WSDataUnavailableException {
		// Take base endpoint and append with path & path param
		String CUSTOMGROUPS_ENDPOINT = WebServiceLocator.getInstance()
				.portalExtnRESTeasyURL() + "customgroup/createCustomGroup";
		this.saveCustomGroup(groupName, entityTypeId, parents, groupState,
				createdBy, children, CUSTOMGROUPS_ENDPOINT);
	}

	/**
	 * Helper for create and update
	 */
	private void saveCustomGroup(String groupName, int entityTypeId,
			String parents, String groupState, String createdBy,
			String children, String endPoint) throws WSDataUnavailableException {
		DefaultHttpClient httpClient = null;
		HttpResponse response = null;
		try {
			httpClient = new DefaultHttpClient();
			String username = WSClientConfiguration.getProperty(WSClientConfiguration.WEBSERVICES_USERNAME);
            String password = WSClientConfiguration.getProperty(WSClientConfiguration.WEBSERVICES_PASSWORD);
			httpClient.getCredentialsProvider().setCredentials(
                    new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
                    new UsernamePasswordCredentials(username, password));
			HttpPost postRequest = new HttpPost(endPoint);
			postRequest.addHeader("accept", "application/xml");
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(6);
			nameValuePairs.add(new BasicNameValuePair("groupName", groupName));
			nameValuePairs.add(new BasicNameValuePair("entityTypeId", String
					.valueOf(entityTypeId)));
			nameValuePairs.add(new BasicNameValuePair("parents", parents));
			nameValuePairs
					.add(new BasicNameValuePair("groupState", groupState));
			nameValuePairs.add(new BasicNameValuePair("createdBy", createdBy));
			nameValuePairs.add(new BasicNameValuePair("children", children));
			postRequest.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			response = httpClient.execute(postRequest);
			if (response.getStatusLine().getStatusCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : "
						+ response.getStatusLine().getStatusCode());
			}
		} catch (Exception exc) {
			LOGGER.error("HTTP error: "
					+ response.getStatusLine().getStatusCode() + ", "
					+ response.getStatusLine().getReasonPhrase());
			throw new WSDataUnavailableException();
		} finally {
			if (httpClient != null)
				httpClient.getConnectionManager().shutdown();
		}

	}

	/**
	 * Update customgroup.
	 * 
	 * @throws WSDataUnavailableException
	 * @throws GWPortalException
	 */
	public void updateCustomGroup(String groupName, int entityTypeId,
			String parents, String groupState, String createdBy, String children)
			throws WSDataUnavailableException {
		// Take base endpoint and append with path & path param
		String CUSTOMGROUPS_ENDPOINT = WebServiceLocator.getInstance()
				.portalExtnRESTeasyURL() + "customgroup/updateCustomGroup";
		this.saveCustomGroup(groupName, entityTypeId, parents, groupState,
				createdBy, children, CUSTOMGROUPS_ENDPOINT);
	}

	/**
	 * Remove customgroup.
	 * 
	 * @throws WSDataUnavailableException
	 * @throws GWPortalException
	 */
	public void removeCustomGroup(Long groupid)
			throws WSDataUnavailableException {
		DefaultHttpClient httpClient = null;
		HttpResponse response = null;
		// Take base endpoint and append with path & path param
		String CUSTOMGROUPS_ENDPOINT = WebServiceLocator.getInstance()
				.portalExtnRESTeasyURL() + "customgroup/removeCustomGroup";
		try {
			httpClient = new DefaultHttpClient();
			String username = WSClientConfiguration.getProperty(WSClientConfiguration.WEBSERVICES_USERNAME);
            String password = WSClientConfiguration.getProperty(WSClientConfiguration.WEBSERVICES_PASSWORD);
			httpClient.getCredentialsProvider().setCredentials(
                    new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
                    new UsernamePasswordCredentials(username, password));
			HttpPost postRequest = new HttpPost(CUSTOMGROUPS_ENDPOINT);
			postRequest.addHeader("accept", "application/xml");
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
			nameValuePairs.add(new BasicNameValuePair("groupId", String
					.valueOf(groupid)));
			postRequest.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			response = httpClient.execute(postRequest);
			if (response.getStatusLine().getStatusCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : "
						+ response.getStatusLine().getStatusCode());
			}
		} catch (Exception exc) {
			LOGGER.error("HTTP error: "
					+ response.getStatusLine().getStatusCode() + ", "
					+ response.getStatusLine().getReasonPhrase());
			throw new WSDataUnavailableException();
		} finally {
			if (httpClient != null)
				httpClient.getConnectionManager().shutdown();
		}
	}

	/**
	 * Removes orphaned children just in case monarch delete hostgroups or
	 * servie groups.
	 * 
	 * @throws WSDataUnavailableException
	 * @throws GWPortalException
	 */
	public void removeOrphanedChildren(Long elementId, int entityTypeId)
			throws WSDataUnavailableException {
		DefaultHttpClient httpClient = null;
		HttpResponse response = null;
		// Take base endpoint and append with path & path param
		String CUSTOMGROUPS_ENDPOINT = WebServiceLocator.getInstance()
				.portalExtnRESTeasyURL() + "customgroup/removeOrphanedChildren";
		try {
			httpClient = new DefaultHttpClient();
			String username = WSClientConfiguration.getProperty(WSClientConfiguration.WEBSERVICES_USERNAME);
            String password = WSClientConfiguration.getProperty(WSClientConfiguration.WEBSERVICES_PASSWORD);
			httpClient.getCredentialsProvider().setCredentials(
                    new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
                    new UsernamePasswordCredentials(username, password));
			HttpPost postRequest = new HttpPost(CUSTOMGROUPS_ENDPOINT);
			postRequest.addHeader("accept", "application/xml");
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
			nameValuePairs.add(new BasicNameValuePair("elementId", String
					.valueOf(elementId)));
			nameValuePairs.add(new BasicNameValuePair("entityTypeId", String
					.valueOf(entityTypeId)));
			postRequest.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			response = httpClient.execute(postRequest);
			if (response.getStatusLine().getStatusCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : "
						+ response.getStatusLine().getStatusCode());
			}
		} catch (Exception exc) {
			LOGGER.error("HTTP error: "
					+ response.getStatusLine().getStatusCode() + ", "
					+ response.getStatusLine().getReasonPhrase());
			throw new WSDataUnavailableException();
		} finally {
			if (httpClient != null)
				httpClient.getConnectionManager().shutdown();
		}
	}

	/**
	 * Gets all entitytypes.
	 * 
	 * @throws WSDataUnavailableException
	 * @throws GWPortalException
	 */
	public Collection<EntityType> findEntityTypes()
			throws WSDataUnavailableException {
		DefaultHttpClient httpClient = null;
		Collection<EntityType> entityTypes = null;
		HttpResponse response = null;
		// Take base endpoint and append with path & path param
		String CUSTOMGROUPS_ENDPOINT = WebServiceLocator.getInstance()
				.portalExtnRESTeasyURL() + "customgroup/findEntityTypes";
		try {
			httpClient = new DefaultHttpClient();
			String username = WSClientConfiguration.getProperty(WSClientConfiguration.WEBSERVICES_USERNAME);
            String password = WSClientConfiguration.getProperty(WSClientConfiguration.WEBSERVICES_PASSWORD);
			httpClient.getCredentialsProvider().setCredentials(
                    new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
                    new UsernamePasswordCredentials(username, password));
			HttpGet getRequest = new HttpGet(CUSTOMGROUPS_ENDPOINT);
			getRequest.addHeader("accept", "application/xml");
			response = httpClient.execute(getRequest);
			if (response.getStatusLine().getStatusCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : "
						+ response.getStatusLine().getStatusCode());
			}
			JAXBContext context = JAXBContext.newInstance(EntityTypeList.class);
			Unmarshaller um = context.createUnmarshaller();
			EntityTypeList entityTypeList = (EntityTypeList) um
					.unmarshal(new StreamSource(new InputStreamReader((response
							.getEntity().getContent()))));
			entityTypes = entityTypeList.getList();
			if (entityTypes == null)
				entityTypes = new ArrayList<EntityType>();

		} catch (Exception exc) {
			LOGGER.error("HTTP error: "
					+ response.getStatusLine().getStatusCode() + ", "
					+ response.getStatusLine().getReasonPhrase());
			throw new WSDataUnavailableException();
		} finally {
			if (httpClient != null)
				httpClient.getConnectionManager().shutdown();
		}
		return entityTypes;
	}

}
