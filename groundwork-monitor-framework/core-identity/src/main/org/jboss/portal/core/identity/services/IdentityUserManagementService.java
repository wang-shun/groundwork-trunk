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
package org.jboss.portal.core.identity.services;

import java.util.List;
import java.util.Map;

import org.jboss.portal.identity.IdentityException;

/**
 * The IdentityUserManagementService interface
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision$
 */
public interface IdentityUserManagementService
{
   /**
    * @param username the user name
    * @param password the users password
    * @param profileMap the users profile map
    * @param roles the users roles
    * @throws IdentityException
    * @throws IllegalArgumentException
    */
   void createUser(String username, String password, Map<String, Object> profileMap, List<String> roles) throws IdentityException;
   
   /**
    * @param username the user name
    * @return the users email address
    * @throws IdentityException
    * @throws IllegalArgumentException if the user name is null 
    */
   String getCurrentEmail(String username) throws IdentityException;
   
   /**
    * @param username the user name
    * @param email the users new email address
    * @throws IdentityException
    * @throws IllegalArgumentException
    */
   void updateEmail(String username, String email) throws IdentityException;
   
}

