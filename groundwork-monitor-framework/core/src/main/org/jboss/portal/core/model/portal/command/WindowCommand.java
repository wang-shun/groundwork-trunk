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

import org.jboss.portal.core.controller.SecurityException;
import org.jboss.portal.core.model.portal.Page;
import org.jboss.portal.core.model.portal.PortalObjectId;
import org.jboss.portal.core.model.portal.Window;
import org.jboss.portal.security.spi.auth.PortalAuthorizationManager;

/**
 * A superclass for command that target a specific window.
 *
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 8786 $
 */
public abstract class WindowCommand extends PageCommand
{

   /** The window. */
   protected Window window;

   public WindowCommand(PortalObjectId windowId) throws IllegalArgumentException
   {
      super(windowId);
   }

   protected final Page initPage()
   {
      window = (Window)getTarget();

      //
      return window.getPage();
   }

   public final Window getWindow()
   {
      return window;
   }

   /**
    * We only enforce security at instance and component level.
    *
    * @param pam
    * @throws org.jboss.portal.core.controller.SecurityException
    *
    * @throws org.jboss.portal.security.PortalSecurityException
    *
    */
   public void enforceSecurity(PortalAuthorizationManager pam) throws SecurityException
   {
   }
}
