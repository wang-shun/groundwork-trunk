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

import org.apache.log4j.Logger;
import org.jboss.portal.core.model.instance.InstanceCustomization;
import org.jboss.portal.portlet.PortletContext;
import org.jboss.portal.portlet.state.AccessMode;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 8786 $
 */
public abstract class AbstractInstanceCustomization extends AbstractInstance implements InstanceCustomization
{

   /** The logger. */
   private static final Logger log = Logger.getLogger(AbstractInstanceCustomization.class);

   protected abstract boolean isPersistent();

   protected AccessMode getAccessMode()
   {
      return isPersistent() ? AccessMode.READ_WRITE : AccessMode.CLONE_BEFORE_WRITE;
   }

   protected final boolean isMutable()
   {
      return false;
   }

   protected final void setMutable(boolean modifiable)
   {
      throw new IllegalStateException("Modifiable field is immutable");
   }

   protected final Logger getLogger()
   {
      return log;
   }

   protected final void cloned(PortletContext portletContext)
   {
      // Make it persistent
      getContainerContext().createInstanceCustomizaton(this);

      // Update state
      getContainerContext().updateInstance(this, portletContext);
   }

   protected final String getInstanceId()
   {
      return getOwner().getInstanceId();
   }

}
