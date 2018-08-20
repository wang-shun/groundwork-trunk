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
package org.jboss.portal.core.identity.services.workflow;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.jboss.portal.core.identity.services.metadata.CoreIdentityConfigurationException;

/**
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision$
 */
public interface RegistrationService
{

   /**
    * Start the registration process for a new user 
    * 
    * @param portalURL the portal url, used for sending the validation email
    * @param username the user name
    * @param password the users password
    * @param profileMap the users profile map
    * @param roles the users roles
    * @param locale the users locale
    * @param adminFlag if this service uses the admin approval workflow
    * @throws CoreIdentityConfigurationException
    * @return a status string
    */
   String registerUser(String url, String username, String password, Map<String, Object> profileMap, List<String> roles, Locale locale, boolean adminFlag) throws CoreIdentityConfigurationException;
   
   /**
    * Approve or reject a registration
    * 
    * @param id
    * @param approve
    * @return
    */
   String approve(String id, boolean approve)  throws CoreIdentityConfigurationException;
  
   /**
    * Get pending users
    * 
    * @param nodeName
    * @return
    */
   List<UserContainer> getPendingUsers(String nodeName) throws CoreIdentityConfigurationException;
   
   /**
    * Check for a existing username in the jBPM context 
    * 
    * @param username
    * @return
    */
   boolean checkUsername(String username) throws CoreIdentityConfigurationException;
   
   /**
    * Get pending users count
    * 
    * @return
    */
   int getPendingCount() throws CoreIdentityConfigurationException;
}

