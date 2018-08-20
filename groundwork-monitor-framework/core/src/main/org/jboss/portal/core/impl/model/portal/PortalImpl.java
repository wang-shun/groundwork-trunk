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
package org.jboss.portal.core.impl.model.portal;

import org.jboss.portal.core.model.portal.DuplicatePortalObjectException;
import org.jboss.portal.core.model.portal.Page;
import org.jboss.portal.core.model.portal.Portal;
import org.jboss.portal.core.model.portal.PortalObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 9134 $
 */
public class PortalImpl extends PortalObjectImpl implements Portal
{

   // Persistent state
   protected Set windowStates;
   protected Set modes;

   public PortalImpl()
   {
      this(true);
   }

   public PortalImpl(boolean hibernate)
   {
      super(hibernate);
      windowStates = hibernate ? null : new HashSet();
      modes = hibernate ? null : new HashSet();
   }

   public Set getModes()
   {
      return modes;
   }

   public void setModes(Set modes)
   {
      this.modes = modes;
   }

   public Set getWindowStates()
   {
      return windowStates;
   }

   public void setWindowStates(Set windowStates)
   {
      this.windowStates = windowStates;
   }

   public Set getSupportedWindowStates()
   {
      return windowStates;
   }

   public Set getSupportedModes()
   {
      return modes;
   }

   public Page getPage(String name)
   {
      PortalObject child = getChild(name);
      if (child instanceof Page)
      {
         return (Page)child;
      }
      return null;
   }

   public Page createPage(String name) throws DuplicatePortalObjectException
   {
      PageImpl page = new PageImpl(false);
      addChild(name, page);
      return page;
   }

   public int getType()
   {
      return PortalObject.TYPE_PORTAL;
   }

   public Page getDefaultPage()
   {
      PortalObject child = getDefaultChild();
      if (child instanceof Page)
      {
         return (Page)child;
      }
      if (child != null)
      {
         log.warn("Default child is not a page " + child);
      }
      return null;
   }

   protected PortalObjectImpl cloneObject()
   {
      PortalImpl clone = new PortalImpl();
      clone.setWindowStates(new HashSet(getWindowStates()));
      clone.setModes(new HashSet(getModes()));
      clone.setDeclaredPropertyMap(new HashMap(getDeclaredPropertyMap()));
      clone.setListener(getListener());
      clone.setDisplayName(getDisplayName());
      return clone;
   }
}
