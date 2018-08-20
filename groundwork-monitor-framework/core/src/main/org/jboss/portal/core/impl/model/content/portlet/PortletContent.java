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

import org.jboss.portal.common.i18n.LocalizedString;
import org.jboss.portal.core.impl.model.content.AbstractContent;
import org.jboss.portal.core.model.content.spi.handler.ContentState;

import java.util.Locale;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 8786 $
 */
public class PortletContent extends AbstractContent
{

   /** . */
   private final String contextId;

   /** . */
   private final InternalPortletContentProvider provider;

   public PortletContent(InternalPortletContentProvider handler, String contextId, ContentState state)
   {
      super(state);
      this.contextId = contextId;
      this.provider = handler;
   }

   public LocalizedString getDisplayName()
   {
      return new LocalizedString(state.getURI() + " portlet", Locale.ENGLISH);
   }

   public void setURI(String uri)
   {
      String currentInstanceRef = state.getURI();

      //
      if (currentInstanceRef != null && currentInstanceRef.equals(uri) == false)
      {
         provider.contentDestroyed(contextId, state);
      }

      //
      state.setURI(uri);
   }

   public String getInstanceRef()
   {
      return getURI();
   }

   public void setInstanceRef(String instanceRef)
   {
      setURI(instanceRef);
   }
}
