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
package org.jboss.portal.core.impl.model.content.generic;

import org.jboss.portal.common.util.CopyOnWriteRegistry;
import org.jboss.portal.core.impl.model.content.InternalContentProviderRegistry;
import org.jboss.portal.core.model.CustomizationManager;
import org.jboss.portal.core.model.content.ContentType;
import org.jboss.portal.core.model.content.spi.portlet.ContentPortlet;
import org.jboss.portal.core.model.instance.InstanceContainer;
import org.jboss.portal.security.spi.auth.PortalAuthorizationManagerFactory;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 8786 $
 */
public class GenericContentProviderRegistry
{

   /** . */
   private static GenericContentProviderRegistry instance;

   public static GenericContentProviderRegistry getInstance()
   {
      return instance;
   }

   /** . */
   private InstanceContainer instanceContainer;

   /** . */
   private CustomizationManager customizationManager;

   /** . */
   private InternalContentProviderRegistry registry;

   /** . */
   private CopyOnWriteRegistry registrations = new CopyOnWriteRegistry();

   /** . */
   private PortalAuthorizationManagerFactory pamf;

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

   public InternalContentProviderRegistry getRegistry()
   {
      return registry;
   }

   public void setRegistry(InternalContentProviderRegistry registry)
   {
      this.registry = registry;
   }

   public PortalAuthorizationManagerFactory getPortalAuthorizationManagerFactory()
   {
      return pamf;
   }

   public void setPortalAuthorizationManagerFactory(PortalAuthorizationManagerFactory portalAuthorizationManagerFactory)
   {
      this.pamf = portalAuthorizationManagerFactory;
   }

   public void register(
      ContentType contentType,
      ContentPortlet contentPortlet)
   {
      InternalGenericContentProvider provider = new InternalGenericContentProvider();
      provider.setContentType(contentType.toString());
      provider.setInstanceContainer(instanceContainer);
      provider.setDecorateContent(true);
      provider.setContentPortletInfo(contentPortlet);
      provider.setRegistry(registry);
      provider.setPortalAuthorizationManagerFactory(pamf);

      // Keep track
      registrations.register(contentType, provider);

      //
      try
      {
         provider.start();
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   public void unregister(ContentType contentType)
   {
      InternalGenericContentProvider provider = (InternalGenericContentProvider)registrations.unregister(contentType);
      if (provider != null)
      {
         provider.stop();
      }
   }

   public void start() throws Exception
   {
      synchronized (GenericContentProviderRegistry.class)
      {
         if (instance != null)
         {
            throw new IllegalStateException("Already an existing instance of the generic content provider registry");
         }
         instance = this;
      }
   }

   public void stop() throws Exception
   {
      synchronized (GenericContentProviderRegistry.class)
      {
         instance = null;
      }
   }
}
