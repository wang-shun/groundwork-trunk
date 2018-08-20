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

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.jboss.portal.identity.User;

/**
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision$
 */
public class UserContainer implements Serializable
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 7703409026217713647L;
   
   /** The jBPM process id */
   private String processId;
   
   /** The identity userName */
   private String username;

   /** The password */
   private String password;

   /** The profile Map */
   private Map<String, Object> profileMap;

   /** The assigned roles */
   private List<String> roles;
   
   /** Is selected */
   private boolean selected = false;

   /** The current node */
   private String currentNode;
   
   public UserContainer(User user)
   {
      this.username = user.getUserName();
   }

   public UserContainer(String username, String password, Map<String, Object> profileMap, List<String> roles)
   {
      this.username = username;
      this.password = password;
      this.profileMap = profileMap;
      this.roles = roles;
   }

   public String getUsername()
   {
      return username;
   }

   public void setUsername(String username)
   {
      this.username = username;
   }

   public String getPassword()
   {
      return password;
   }

   public void setPassword(String password)
   {
      this.password = password;
   }

   public String getProcessId()
   {
      return processId;
   }

   public void setProcessId(String processId)
   {
      this.processId = processId;
   }

   public Map<String, Object> getProfileMap()
   {
      return profileMap;
   }

   public void setProfileMap(Map<String, Object> profileMap)
   {
      this.profileMap = profileMap;
   }

   public List<String> getRoles()
   {
      return roles;
   }

   public void setRoles(List<String> roles)
   {
      this.roles = roles;
   }
   
   public boolean isSelected()
   {
      return selected;
   }
   
   public void setSelected(boolean selected)
   {
      this.selected = selected;
   }
   
   public String getCurrentNode()
   {
      return currentNode;
   }

   public void setCurrentNode(String currentNode)
   {
      this.currentNode = currentNode;
   }
   
   public String getEmail()
   {
      return (String) this.profileMap.get(User.INFO_USER_EMAIL_REAL);
   }
   
   public String getRegistrationDate(){
      Object obj = this.profileMap.get(User.INFO_USER_REGISTRATION_DATE);
      return obj != null ? obj.toString() : null;
   }
}
