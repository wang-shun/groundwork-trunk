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
package org.jboss.portal.core.impl.model.instance;

import org.jboss.portal.core.model.instance.Instance;
import org.jboss.portal.core.model.instance.InstanceCustomization;
import org.jboss.portal.core.model.instance.InstanceDefinition;
import org.jboss.portal.portlet.PortletContext;
import org.jboss.portal.portlet.StateEvent;
import org.jboss.portal.portlet.spi.InstanceContext;
import org.jboss.portal.portlet.state.AccessMode;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 10119 $
 */
public class InstanceContextImpl implements InstanceContext
{

   /** . */
   protected final Instance instance;

   /** . */
   protected AccessMode accessMode;

   /** . */
   protected PortletContext clonedContext;

   /** . */
   protected PortletContext modifiedContext;

   public InstanceContextImpl(Instance instance, AccessMode accessMode)
   {
      if (instance == null)
      {
         throw new IllegalArgumentException();
      }
      this.instance = instance;
      this.accessMode = accessMode;
   }

   public String getId()
   {
      if (instance instanceof InstanceDefinition)
      {
         return instance.getId();
      }
      else
      {
         InstanceCustomization cust = (InstanceCustomization)instance;
         InstanceDefinition def = cust.getDefinition();
         return def.getId() + "." + cust.getId();
      }
   }

   public AccessMode getAccessMode()
   {
      return accessMode;
   }

   public void onStateEvent(StateEvent event)
   {
      StateEvent.Type type = event.getType();
      if (StateEvent.Type.PORTLET_CLONED_EVENT.equals(type))
      {
         clonedContext = event.getPortletContext();
      }
      else if (StateEvent.Type.PORTLET_MODIFIED_EVENT.equals(type))
      {
         modifiedContext = event.getPortletContext();
      }
   }
}
