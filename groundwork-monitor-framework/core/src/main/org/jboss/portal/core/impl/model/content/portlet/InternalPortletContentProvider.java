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
package org.jboss.portal.core.impl.model.content.portlet;

import org.jboss.portal.Mode;
import org.jboss.portal.common.i18n.LocalizedString;
import org.jboss.portal.common.i18n.ResourceBundleFactory;
import org.jboss.portal.common.i18n.ResourceBundleManager;
import org.jboss.portal.common.i18n.SimpleResourceBundleFactory;
import org.jboss.portal.common.util.EmptyResourceBundle;
import org.jboss.portal.core.impl.model.content.InternalContentProvider;
import org.jboss.portal.core.model.CustomizationManager;
import org.jboss.portal.core.model.content.Content;
import org.jboss.portal.core.model.content.ContentType;
import org.jboss.portal.core.model.content.spi.ContentProvider;
import org.jboss.portal.core.model.content.spi.handler.ContentHandler;
import org.jboss.portal.core.model.content.spi.handler.ContentState;
import org.jboss.portal.core.model.content.spi.portlet.ContentPortlet;
import org.jboss.portal.core.model.instance.Instance;
import org.jboss.portal.core.model.instance.InstanceContainer;
import org.jboss.portal.core.model.portal.Window;
import org.jboss.portal.core.model.portal.content.ContentRendererContext;
import org.jboss.portal.identity.User;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 9006 $
 */
public class InternalPortletContentProvider extends InternalContentProvider implements ContentHandler
{

   /** . */
   private InstanceContainer instanceContainer;

   /** . */
   private CustomizationManager customizationManager;

   /** . */
   private ResourceBundleManager bundleManager;

   /** . */
   private LocalizedString displayName;

   /** . */
   private LocalizedString description;

   public InstanceContainer getInstanceContainer()
   {
      return instanceContainer;
   }

   public void setInstanceContainer(InstanceContainer instanceContainer)
   {
      this.instanceContainer = instanceContainer;
   }

   public CustomizationManager getCustomizationManager()
   {
      return customizationManager;
   }

   public void setCustomizationManager(CustomizationManager customizationManager)
   {
      this.customizationManager = customizationManager;
   }

   public void start() throws Exception
   {
      ResourceBundleFactory factory = new SimpleResourceBundleFactory("conf/bundles/PortletContent", Thread.currentThread().getContextClassLoader());
      bundleManager = new ResourceBundleManager(EmptyResourceBundle.INSTANCE, factory);

      // displayName = new LocalizedString();

      //
      super.start();
   }


   public void stop()
   {
      super.stop();

      //
//      bundle = null;
   }

   protected ContentProvider createProvider()
   {
      return new ContentProvider()
      {
         public ContentType getContentType()
         {
            return getRegisteredContentType();
         }

         public LocalizedString getDisplayName()
         {
            return null;
         }

         public LocalizedString getDescription()
         {
            return null;
         }

         public ContentHandler getHandler()
         {
            return InternalPortletContentProvider.this;
         }

         public ContentPortlet getPortletInfo()
         {
            return new ContentPortlet()
            {
               public String getPortletName(Mode mode)
               {
                  if (EDIT_CONTENT_MODE.equals(mode))
                  {
                     return "PortletContentEditorInstance";
                  }
                  else
                  {
                     return null;
                  }
               }
            };
         }
      };
   }

   // ContentHandler implementation ************************************************************************************

   public Content newContent(String contextId, ContentState state)
   {
      return new PortletContent(this, contextId, state);
   }

   public void contentDestroyed(String contextId, ContentState state)
   {
      String instanceRef = state.getURI();

      // Do we have a related instance ?
      if (instanceRef != null)
      {
         Instance instance = instanceContainer.getDefinition(instanceRef);

         // Destroy related instance customization if possible
         if (instance != null)
         {
            instance.destroyCustomization(contextId);
         }
      }
   }

   public void contentCreated(String contextId, ContentState state)
   {
   }

   // ContentRenderer implementation ***********************************************************************************

   protected Instance getPortletInstance(ContentRendererContext rendererContext)
   {
      // The window
      Window window = rendererContext.getWindow();

      // We need the user id
      User user = rendererContext.getUser();

      // Get instance
      return customizationManager.getInstance(window, user);
   }

}
