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
package org.jboss.portal.core.impl.model.instance.persistent;

import org.jboss.portal.common.i18n.LocalizedString;
import org.jboss.portal.core.impl.model.instance.AbstractInstanceCustomization;
import org.jboss.portal.core.impl.model.instance.AbstractInstanceDefinition;
import org.jboss.portal.core.impl.model.instance.InstanceContainerContext;
import org.jboss.portal.portlet.PortletContext;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 9574 $
 */
class PersistentInstanceCustomization extends AbstractInstanceCustomization
{

   // Persistent fields

   protected Long key;
   protected PersistentInstanceDefinition relatedDefinition;
   protected String customizationId;
   protected LocalizedString displayName;

   // Runtime fields

   protected PersistentInstanceDefinition owner;
   protected boolean persistent;

   /** Used to build transient instances. */
   public PersistentInstanceCustomization(PersistentInstanceDefinition owner, String customizationId, PortletContext portletContext)
   {
      if (owner == null)
      {
         throw new IllegalArgumentException();
      }
      if (customizationId == null)
      {
         throw new IllegalArgumentException();
      }
      if (portletContext == null)
      {
         throw new IllegalArgumentException();
      }

      //
      this.owner = owner;
      this.persistent = false;

      //
      this.customizationId = customizationId;
      this.portletRef = portletContext.getId();
      this.state = portletContext.getState();
   }

   /** Used by hibernate. */
   public PersistentInstanceCustomization()
   {
      this.owner = null;
      this.persistent = true;
   }

   public Long getKey()
   {
      return key;
   }

   public void setKey(Long key)
   {
      this.key = key;
   }

   public String getId()
   {
      return owner.getInstanceId();
   }

   public LocalizedString getDisplayName()
   {
      return owner.getDisplayName();
   }

   public void setDisplayName(LocalizedString localizedString)
   {
      this.displayName = localizedString;
   }

   public String getCustomizationId()
   {
      return customizationId;
   }

   public void setCustomizationId(String customizationId)
   {
      this.customizationId = customizationId;
   }

   public AbstractInstanceDefinition getOwner()
   {
      return owner;
   }

   public PersistentInstanceDefinition getRelatedDefinition()
   {
      return relatedDefinition;
   }

   public void setRelatedDefinition(PersistentInstanceDefinition relatedDefinition)
   {
      this.relatedDefinition = relatedDefinition;
      this.owner = relatedDefinition;
   }

   protected boolean isPersistent()
   {
      return persistent;
   }

   protected InstanceContainerContext getContainerContext()
   {
      return owner.containerContext;
   }
}
