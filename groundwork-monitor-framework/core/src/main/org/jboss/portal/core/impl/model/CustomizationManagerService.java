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
package org.jboss.portal.core.impl.model;

import org.jboss.logging.Logger;
import org.jboss.portal.core.impl.model.content.portlet.PortletContent;
import org.jboss.portal.core.model.CustomizationManager;
import org.jboss.portal.core.model.content.Content;
import org.jboss.portal.core.model.instance.Instance;
import org.jboss.portal.core.model.instance.InstanceContainer;
import org.jboss.portal.core.model.portal.Context;
import org.jboss.portal.core.model.portal.DuplicatePortalObjectException;
import org.jboss.portal.core.model.portal.NoSuchPortalObjectException;
import org.jboss.portal.core.model.portal.Portal;
import org.jboss.portal.core.model.portal.PortalObject;
import org.jboss.portal.core.model.portal.PortalObjectContainer;
import org.jboss.portal.core.model.portal.PortalObjectId;
import org.jboss.portal.core.model.portal.PortalObjectPath;
import org.jboss.portal.core.model.portal.PortalObjectPermission;
import org.jboss.portal.core.model.portal.Window;
import org.jboss.portal.identity.RoleModule;
import org.jboss.portal.identity.User;
import org.jboss.portal.identity.UserModule;
import org.jboss.portal.jems.as.system.AbstractJBossService;
import org.jboss.portal.security.spi.auth.PortalAuthorizationManager;
import org.jboss.portal.security.spi.auth.PortalAuthorizationManagerFactory;

import java.util.Iterator;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 11752 $
 */
public class CustomizationManagerService extends AbstractJBossService implements CustomizationManager
{

   /** . */
   private static final PortalObjectId TEMPLATE_ID = PortalObjectId.parse("/template", PortalObjectPath.CANONICAL_FORMAT);

   /** . */
   private static final Logger log = Logger.getLogger(CustomizationManager.class);

   /** . */
   private String dashboardContextId;

   /** . */
   private InstanceContainer instanceContainer;

   /** . */
   private PortalAuthorizationManagerFactory pamf;

   /** . */
   private UserModule userModule;

   /** . */
   private RoleModule roleModule;

   /** . */
   private PortalObjectContainer portalObjectContainer;

   public PortalObjectContainer getPortalObjectContainer()
   {
      return portalObjectContainer;
   }

   public void setPortalObjectContainer(PortalObjectContainer portalObjectContainer)
   {
      this.portalObjectContainer = portalObjectContainer;
   }

   public String getDashboardContextId()
   {
      return dashboardContextId;
   }

   public void setDashboardContextId(String dashboardContextId)
   {
      this.dashboardContextId = dashboardContextId;
   }

   public InstanceContainer getInstanceContainer()
   {
      return instanceContainer;
   }

   public void setInstanceContainer(InstanceContainer instanceContainer)
   {
      this.instanceContainer = instanceContainer;
   }

   public PortalAuthorizationManagerFactory getPortalAuthorizationManagerFactory()
   {
      return pamf;
   }

   public void setPortalAuthorizationManagerFactory(PortalAuthorizationManagerFactory portalAuthorizationManagerFactory)
   {
      this.pamf = portalAuthorizationManagerFactory;
   }

   public UserModule getUserModule()
   {
      return userModule;
   }

   public void setUserModule(UserModule userModule)
   {
      this.userModule = userModule;
   }

   public RoleModule getRoleModule()
   {
      return roleModule;
   }

   public void setRoleModule(RoleModule roleModule)
   {
      this.roleModule = roleModule;
   }

   protected void createService() throws Exception
   {
      super.createService();
   }


   protected void destroyService() throws Exception
   {
      super.destroyService();
   }

   public Instance getInstance(Window window) throws IllegalArgumentException
   {
      return getInstance(window, null);
   }

   public Instance getInstance(Window window, User user) throws IllegalArgumentException
   {
      if (window == null)
      {
         throw new IllegalArgumentException("No window provided");
      }

      //
      Content content = window.getContent();

      //
      String instanceId = ((PortletContent)content).getInstanceRef();
      if (instanceId == null)
      {
         return null;
      }

      // Get the instance
      Instance instance = instanceContainer.getDefinition(instanceId);
      if (instance != null)
      {
         // If we are in the context of an existing user we get a customization for that user
         if (user != null)
         {
            String userId = getUserId(user);

            // And if it is in a dashboard context we get the per window customization
            if (isDashboard(window, user))
            {
               // That's how we manufacture dash board keys
               String dashboardId = window.getId().toString();

               //
               instance = instance.getCustomization(dashboardId);
            }
            else
            {
               instance = instance.getCustomization(userId);
            }
         }
      }

      //
      return instance;
   }

   /**
    * Return true if the portal object is in a dashboard context for the current authenticated user.
    *
    * @param object
    * @return
    */
   public boolean isDashboard(PortalObject object, User user)
   {
      if (object == null)
      {
         throw new IllegalArgumentException("No null object");
      }

      // todo
      // We should test that it is the same than the request user
      // as for now we can only test permission for the currently
      // authenticated user

      //
      try
      {
         PortalAuthorizationManager pam = pamf.getManager();
         PortalObjectPermission perm = new PortalObjectPermission(object.getId(), PortalObjectPermission.DASHBOARD_MASK);
         return pam.checkPermission(perm);
      }
      catch (Exception e)
      {
         log.error("Cannot check dashboard for", e);
         return false;
      }
//      if (object == null)
//      {
//         throw new IllegalArgumentException("No null object");
//      }
//
//      //
//      PortalObjectId objectId = object.getName();
//
//      //
//      if (user != null && "dashboard".equals(objectId.getNamespace()))
//      {
//         String userName = user.getUserName();
//         PortalObjectPath objectPath = objectId.getPath();
//         Iterator i = objectPath.names();
//
//         //
//         if (i.hasNext())
//         {
//            // Skip empty context name
//            i.next();
//
//            // Check dashboard id is equals to user name
//            if (i.hasNext())
//            {
//               String dashboardId = (String)i.next();
//               return userName.equals(dashboardId);
//            }
//         }
//      }
//
//      //
//      return false;
   }

   public Portal getDashboard(User user) throws IllegalArgumentException
   {
      //
      Portal dashboardPortal = null;

      if (user != null)
      {
         String userId = getUserId(user);

         //
         try
         {
            Context dashboardContext = portalObjectContainer.getContext(dashboardContextId);

            //
            dashboardPortal = dashboardContext.getPortal(userId);

            // Create if not exist
            if (dashboardPortal == null)
            {
               Portal templatePortal = (Portal)portalObjectContainer.getObject(TEMPLATE_ID);

               // Copy the template portal
               dashboardPortal = (Portal)templatePortal.copy(dashboardContext, userId, false);
               copy(templatePortal, dashboardContext.getChild(userId));

            }
         }
         catch (DuplicatePortalObjectException e)
         {
            log.error("", e);
         }
      }

      //
      return dashboardPortal;
   }

   private void copy(PortalObject from, PortalObject to)
   {
      PortalAuthorizationManager pam = pamf.getManager();
      Iterator it = from.getChildren().iterator();
      {
         while (it.hasNext())
         {
            PortalObject portalObject = (PortalObject)it.next();
            try
            {
               PortalObjectPermission perm = new PortalObjectPermission(portalObject.getId(), PortalObjectPermission.VIEW_MASK);
               if (pam.checkPermission(perm))
               {
                  portalObject.copy(to, portalObject.getName(), false);
               }
               copy(portalObject, to.getChild(portalObject.getName()));
            }
            catch (IllegalArgumentException e)
            {
               e.printStackTrace();
            }
            catch (DuplicatePortalObjectException e)
            {
               e.printStackTrace();
            }
         }
      }

   }

   /** Destroys the user dashboard if any */
   public void destroyDashboard(String userId)
   {
      try
      {
         Context dashboardContext = (Context)portalObjectContainer.getContext(dashboardContextId);
         // Check that the user has a dashboard
         if (dashboardContext.getChild(userId) != null)
         {
            dashboardContext.destroyChild(userId);
         }
      }
      catch (NoSuchPortalObjectException e)
      {
         e.printStackTrace();
      }
   }

   private String getUserId(User user)
   {
      return user.getUserName();
   }
}
