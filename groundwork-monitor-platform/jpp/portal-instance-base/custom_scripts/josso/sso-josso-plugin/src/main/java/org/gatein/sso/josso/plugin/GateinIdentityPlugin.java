/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2006, Red Hat Middleware, LLC, and individual                    *
 * contributors as indicated by the @authors tag. See the                     *
 * copyright.txt in the distribution for a full listing of                    *
 * individual contributors.                                                   *
 *                                                                            *
 * This is free software; you can redistribute it and/or modify it            *
 * under the terms of the GNU Lesser General Public License as                *
 * published by the Free Software Foundation; either version 2.1 of           *
 * the License, or (at your option) any later version.                        *
 *                                                                            *
 * This software is distributed in the hope that it will be useful,           *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU           *
 * Lesser General Public License for more details.                            *
 *                                                                            *
 * You should have received a copy of the GNU Lesser General Public           *
 * License along with this software; if not, write to the Free                *
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA         *
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.                   *
 ******************************************************************************/
package org.gatein.sso.josso.plugin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.josso.gateway.identity.exceptions.NoSuchUserException;
import org.josso.gateway.identity.exceptions.SSOIdentityException;
import org.josso.gateway.identity.service.BaseRole;
import org.josso.gateway.identity.service.BaseRoleImpl;
import org.josso.gateway.identity.service.BaseUser;
import org.josso.gateway.identity.service.BaseUserImpl;
import org.josso.gateway.identity.service.store.UserKey;
import org.josso.gateway.identity.service.store.IdentityStore;

import org.josso.auth.Credential;
import org.josso.auth.CredentialKey;
import org.josso.auth.CredentialProvider;
import org.josso.auth.scheme.AuthenticationScheme;
import org.josso.auth.BindableCredentialStore;
import org.josso.auth.exceptions.SSOAuthenticationException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;


/**
 * Identity plugin implementation for JOSSO 1
 *
 * @org.apache.xbean.XBean element="gatein-store"
 *
 * @author <a href="mailto:sshah@redhat.com">Sohil Shah</a>
 *
 */
public class GateinIdentityPlugin extends AbstractIdentityPlugin implements BindableCredentialStore, IdentityStore
{

	private AuthenticationScheme authenticationScheme = null;
	private static final Log log = LogFactory.getLog(AbstractIdentityPlugin.class);

	public void setAuthenticationScheme(AuthenticationScheme authenticationScheme) {
		this.authenticationScheme = authenticationScheme;
	}

	// ----------------IdentityStore implementation---------------------------------------
	public boolean userExists(UserKey userKey) throws SSOIdentityException {
		// TODO
		return true;
	}

	public BaseRole[] findRolesByUserKey(UserKey userKey) throws SSOIdentityException {
		BaseRole[] emptyRoles = {};

		if (userKey == null) return emptyRoles;

		// get URL content
        URL url;
		try {
			url = buildURL("/rest/sso/authcallback/roles/" + userKey.toString());
		} catch (MalformedURLException e) {
			log.error("Malformed URL", e);
			return emptyRoles;
		}

		String rolesString = this.httpGet(url);

		if (rolesString == null || rolesString.isEmpty()) return emptyRoles;

		List<BaseRole> roles = new ArrayList<BaseRole>();
		StringTokenizer rolesTokens = new StringTokenizer(rolesString, ",");

		while (rolesTokens.hasMoreTokens()) {
			roles.add(new BaseRoleImpl(rolesTokens.nextToken()));
		}
		return roles.toArray(new BaseRole[roles.size()]);
	}

	public BaseUser loadUser(UserKey userKey) throws NoSuchUserException,
			SSOIdentityException
	{
		BaseUser user = new BaseUserImpl();
		user.setName(userKey.toString());
		return user;
	}

	// ---------------CredentialStore implementation----------------------------------------------------------
	public Credential[] loadCredentials(CredentialKey credentialKey,
										CredentialProvider credentialProvider) throws SSOIdentityException {
	    // TODO
		return null;
	}

	public Credential[] loadCredentials(CredentialKey credentialKey) throws SSOIdentityException {
		// TODO
		return null;
	}

	public String loadUID(CredentialKey key, CredentialProvider cp) throws SSOIdentityException {
		// TODO
		return null;
	}

	public boolean bind(String username, String password) throws SSOAuthenticationException {
		try {
			return bindImpl(username, password);
		}
		catch(Exception e) {
			throw new SSOAuthenticationException(e);
		}
	}

	private String httpGet(URL url) {
		URLConnection conn;
		BufferedReader br = null;
		InputStreamReader reader = null;
		String inputLine = null;
		try {
			conn = url.openConnection();
			reader = new InputStreamReader(conn.getInputStream());
			br = new BufferedReader(reader);
			// Rest returns only one line output
			inputLine = br.readLine();
		} catch (IOException e) {
			log.error(e.getMessage());
		} finally {
			try {
				if (br != null)
					br.close();
				if (reader != null)
					reader.close();
			} catch (IOException e) {
				log.error(e.getMessage());
			}
		}
		return inputLine;

	}
}
