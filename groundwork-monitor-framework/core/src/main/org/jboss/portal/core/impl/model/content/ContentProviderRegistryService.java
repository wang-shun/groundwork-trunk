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
package org.jboss.portal.core.impl.model.content;

import org.jboss.portal.common.util.CopyOnWriteRegistry;
import org.jboss.portal.core.model.content.ContentType;
import org.jboss.portal.core.model.content.spi.ContentProvider;
import org.jboss.portal.core.model.portal.content.ContentRenderer;

import java.util.Collection;

/**
 * A simple registry to track the instance that provides content facilities.
 *
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 8786 $
 */
public class ContentProviderRegistryService implements InternalContentProviderRegistry
{

   /** . */
   private static volatile ContentProviderRegistryService instance;

   public static ContentProviderRegistryService getInstance()
   {
      return instance;
   }

   /** . */
   private final CopyOnWriteRegistry registry = new CopyOnWriteRegistry();

   public void registerContentProvider(InternalContentProvider contentProvider)
   {
      registry.register(contentProvider.registeredContentType, contentProvider);
   }

   public void unregisterContentProvider(ContentType contentType)
   {
      registry.unregister(contentType);
   }

   public void start()
   {
      synchronized (ContentProviderRegistryService.class)
      {
         if (instance != null)
         {
            throw new IllegalStateException("An instance of the content type registry service already exists");
         }
         instance = this;
      }
   }

   public void stop()
   {
      synchronized (ContentProviderRegistryService.class)
      {
         instance = null;
      }
   }

   //

   public Collection getContentTypes()
   {
      return registry.getKeys();
   }

   public ContentProvider getContentProvider(ContentType contentType)
   {
      InternalContentProvider acp = (InternalContentProvider)registry.getRegistration(contentType);
      return acp != null ? acp.contentProvider : null;
   }

   public ContentRenderer getRenderer(ContentType contentType)
   {
      return (InternalContentProvider)registry.getRegistration(contentType);
   }
}
