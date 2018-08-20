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
package org.jboss.portal.core.identity.ui.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.Comparator;
import java.util.Collections;

import javax.faces.context.FacesContext;

import org.jboss.logging.Logger;
import org.jboss.portal.core.identity.services.IdentityConstants;
import org.jboss.portal.core.identity.services.metadata.UIComponentConfiguration;
import org.jboss.portal.core.identity.ui.IdentityUIUser;
import org.jboss.portal.core.aspects.server.UserInterceptor;
import org.jboss.portal.core.controller.ControllerContext;
import org.jboss.portal.identity.IdentityException;
import org.jboss.portal.identity.NoSuchUserException;
import org.jboss.portal.identity.User;
import org.jboss.portal.identity.UserModule;
import org.jboss.portal.identity.UserProfileModule;
import org.jboss.portal.server.ServerInvocation;
import org.jboss.portlet.JBossRenderRequest;
import org.jboss.portlet.JBossActionRequest;

/**
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision$
 */
public class IdentityUserBean
{

   /** The identity user module */
   private UserModule userModule;

   /** The user profile module */
   private UserProfileModule userProfileModule;

   /** The core-identity meta data service */
   private MetaDataServiceBean metaDataService;

   /** the logger */
   private static final Logger log = Logger.getLogger(IdentityUserBean.class);

   public UserModule getUserModule()
   {
      return userModule;
   }

   public void setUserModule(UserModule userModule)
   {
      this.userModule = userModule;
   }

   public UserProfileModule getUserProfileModule()
   {
      return userProfileModule;
   }

   public void setUserProfileModule(UserProfileModule userProfileModule)
   {
      this.userProfileModule = userProfileModule;
   }

   public MetaDataServiceBean getMetaDataService()
   {
      return metaDataService;
   }

   public void setMetaDataService(MetaDataServiceBean metaDataService)
   {
      this.metaDataService = metaDataService;
   }

   /*
    * converts the dynamic attribute Map to a map concerning the identity service - UserProfileModule
    */
   public Map<String, Object> getProfileMap(Map<String, Object> attributeMap)
   {
      Map<String, Object> profileMap = new HashMap<String, Object>();

      for(String key : attributeMap.keySet())
      {
         Object value = attributeMap.get(key);
         UIComponentConfiguration uiComponent = (UIComponentConfiguration) this.metaDataService.getValue(key).getObject();

         if ( uiComponent != null)
         {
            profileMap.put(uiComponent.getPropertyRef(), value);
         }
      }
      return profileMap;
   }

   public void updateProfile(User user, Map<String, Object> attributeMap)
   {
      Map<String, Object> profileMap = this.getProfileMap(attributeMap);

      for(String key : profileMap.keySet())
      {
         Object value = profileMap.get(key);
         try
         {
            this.userProfileModule.setProperty(user, key, value);
         }
         catch (Exception e)
         {
            log.error("updateProfile failed", e);
         }
      }
   }

   public User findUserByUserName(String username) throws IllegalArgumentException, NoSuchUserException,
         IdentityException
   {
      return userModule.findUserByUserName(username);
   }

   public List<IdentityUIUser> findUsersFilteredByUserName(String filter, int offset, int limit)
               throws IllegalArgumentException, IdentityException
   {
      Set<User> users = new HashSet<User>();
      List<IdentityUIUser> list = new ArrayList<IdentityUIUser>();

      users = userModule.findUsersFilteredByUserName(filter, offset, limit);

      for(User user : users)
      {
         IdentityUIUser uiUser =new IdentityUIUser(user.getUserName());
         list.add(uiUser);
      }                            
      Collections.sort(list, new IdentityUIUserComparator());

      return list;
   }

   public void updatePassword(String username, String password)
               throws IllegalArgumentException, IdentityException, IdentityException
   {
      User user = userModule.findUserByUserName(username);
      user.updatePassword(password);
   }

   public Object getLocalizedValue(String propertyName, Object value)
   {
      UIComponentConfiguration uiComponent =  (UIComponentConfiguration) this.metaDataService.getValue(propertyName).getObject();
      if( uiComponent.getValues() != null
            && value instanceof String
            && uiComponent.getValues().size() > 0)
      {
         try
         {
            ResourceBundle bundle = ResourceBundle.getBundle("conf.bundles.Identity", FacesContext.getCurrentInstance().getViewRoot().getLocale());
            return bundle.getString(IdentityConstants.DYNAMIC_VALUE_PREFIX + ((String)value).toUpperCase());
         }
         catch (Exception e)
         {
            return value;
         }
      }
      return value;
   }

   public Class getPropertyType(String propertyName) throws IdentityException, ClassNotFoundException
   {
      UIComponentConfiguration uiComponent = (UIComponentConfiguration) this.metaDataService.getValue(propertyName).getObject();
      return uiComponent.getPropertyClass();
   }

   public Object getUserProperty(String username, String propertyName) throws IllegalArgumentException, NoSuchUserException, IdentityException
   {

      UIComponentConfiguration uiComponent = (UIComponentConfiguration) this.metaDataService.getValue(propertyName).getObject();

      Map profile = null;

      // Uncomment this to use the cached profile (for now its not invalidated on write)
      //profile = getCachedUserProfile();

      if (profile == null)
      {

         // This is to intercept calls to display current user profile and decrease number of calls to identity modules
         // Needs to be done in better way
         User user = getCurrentUser();
         if (user == null || !user.getUserName().equals(username))
         {
            user = this.findUserByUserName(username);
         }

         return this.userProfileModule.getProperty(user, uiComponent.getPropertyRef());
      }

      return profile.get(uiComponent.getPropertyRef());

   }

   public User getCurrentUser()
   {
      Object request = FacesContext.getCurrentInstance().getExternalContext().getRequest();

      ControllerContext context = null;

      if (request instanceof JBossRenderRequest)
      {
         JBossRenderRequest renderRequest = (JBossRenderRequest)request;
         context  = renderRequest.getControllerContext();
      }
      else if (request instanceof JBossActionRequest)
      {
         JBossActionRequest actionRequest = (JBossActionRequest)request;
         context  = actionRequest.getControllerContext();
      }

      if (context != null)
      {
         Object user = context.getAttribute(ServerInvocation.PRINCIPAL_SCOPE, UserInterceptor.USER_KEY);
         if (user instanceof User)
         {
            return (User)user;
         }
      }

      return null;

   }

   public Map getCachedUserProfile()
   {
      Object request = FacesContext.getCurrentInstance().getExternalContext().getRequest();

      ControllerContext context = null;

      if (request instanceof JBossRenderRequest)
      {
         JBossRenderRequest renderRequest = (JBossRenderRequest)request;
         context  = renderRequest.getControllerContext();
      }
      else if (request instanceof JBossActionRequest)
      {
         JBossActionRequest actionRequest = (JBossActionRequest)request;
         context  = actionRequest.getControllerContext();
      }

      if (context != null)
      {
         Object profile = context.getAttribute(ServerInvocation.PRINCIPAL_SCOPE, UserInterceptor.PROFILE_KEY);
         if (profile instanceof Map)
         {
            return (Map)profile;
         }
      }

      return null;

   }

   protected class IdentityUIUserComparator implements Comparator
   {


      public int compare(Object o1, Object o2)
      {
         try
         {
            IdentityUIUser u1 = (IdentityUIUser)o1;
            IdentityUIUser u2 = (IdentityUIUser)o2;

            return u1.getUsername().compareToIgnoreCase(u2.getUsername());
         }
         catch(Throwable e)
         {
            //none
         }
         return 0;
      }
   }
}
