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
package org.jboss.portal.test.core.model.content;

import org.jboss.portal.common.i18n.LocalizedString;
import org.jboss.portal.core.model.content.ContentType;
import org.jboss.portal.core.model.content.spi.ContentProvider;
import org.jboss.portal.core.model.content.spi.handler.ContentHandler;
import org.jboss.portal.core.model.content.spi.portlet.ContentPortlet;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 8786 $
 */
public class SimpleContentProvider implements ContentProvider
{

   /** . */
   private final ContentType contentType;

   /** . */
   private final SimpleContentHandler handler;

   /** . */
   private SimpleContentProviderRegistry registry;

   public SimpleContentProvider(ContentType contentType)
   {
      this.contentType = contentType;
      this.handler = new SimpleContentHandler();
   }

   public SimpleContentProvider(String contentTypeValue)
   {
      this.contentType = ContentType.create(contentTypeValue);
      this.handler = new SimpleContentHandler();
   }

   public SimpleContentProviderRegistry getRegistry()
   {
      return registry;
   }

   public void setRegistry(SimpleContentProviderRegistry registry)
   {
      this.registry = registry;
   }

   public ContentType getContentType()
   {
      return contentType;
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
      return handler;
   }

   public ContentPortlet getPortletInfo()
   {
      return null;
   }

   public void start()
   {
      registry.addContentProvider(this);
   }

   public void stop()
   {
      registry.removeContentProvider(contentType);
   }
}
