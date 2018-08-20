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
package org.jboss.portal.core.model.portal.command;

import org.jboss.portal.core.controller.AccessDeniedException;
import org.jboss.portal.core.controller.ControllerCommand;
import org.jboss.portal.core.controller.NoSuchResourceException;
import org.jboss.portal.core.controller.SecurityException;
import org.jboss.portal.core.model.portal.PortalObject;
import org.jboss.portal.core.model.portal.PortalObjectId;
import org.jboss.portal.core.model.portal.PortalObjectPermission;
import org.jboss.portal.security.spi.auth.PortalAuthorizationManager;

// HACK: JBoss Portal's dashboard functionality is hardcoded based on
// whether the portal context's namespace is "dashboard" or not.  This
// version of the PortalObjectCommand extends dashboard functionality to the
// Groundwork Monitor Status Viewer.

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 10553 $
 */
public abstract class PortalObjectCommand extends ControllerCommand
{

   /** . */
   protected final PortalObjectId targetId;

   /** . */
   protected PortalObject target;

   /** . */
   protected boolean dashboard;
   
   protected boolean statusView;

   protected PortalObjectCommand(PortalObjectId targetId)
   {
      if (targetId == null)
      {
         throw new IllegalArgumentException();
      }
      this.targetId = targetId;
      this.dashboard = "dashboard".equals(targetId.getNamespace());
      // TODO: Remove hardcoding of Status View namespace
      statusView = "status".equals(targetId.getNamespace());
   }

   public final PortalObjectId getTargetId()
   {
      return targetId;
   }

   public void acquireResources() throws NoSuchResourceException
   {
      // Get portal object
      target = context.getController().getPortalObjectContainer().getObject(targetId);

      //
      if (target == null)
      {
         throw new NoSuchResourceException(targetId.toString());
      }
   }

   /**
    * Enforce the security on this command using the provided portal authorization manager.
    *
    * @param pam the portal authorization manager
    * @throws org.jboss.portal.core.controller.SecurityException
    *          if the access is not granted
    */
   public void enforceSecurity(PortalAuthorizationManager pam) throws SecurityException
   {
      PortalObject target = getTarget();
      PortalObjectId id = target.getId();
      PortalObjectPermission perm = new PortalObjectPermission(id, PortalObjectPermission.VIEW_MASK);
      if (!pam.checkPermission(perm))
      {
         throw new AccessDeniedException(id.toString(), "View permission not granted");
      }
   }

   /**
    * Return the target portal object of this command.
    *
    * @return the target portal object
    */
   public final PortalObject getTarget()
   {
      return target;
   }

   /**
    * Return true if the command is in a dashboard context.
    *
    * @return
    */
   public boolean isDashboard()
   {
      return dashboard;
   }
   
   /**
    * Return true if the command is in the Groundwork Status Viewer context.
    */
   public boolean isStatusView()
   {
       return statusView;
   }
}
