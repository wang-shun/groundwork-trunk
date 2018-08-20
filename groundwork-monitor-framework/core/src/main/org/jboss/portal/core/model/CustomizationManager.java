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
package org.jboss.portal.core.model;

import org.jboss.portal.core.model.instance.Instance;
import org.jboss.portal.core.model.portal.Portal;
import org.jboss.portal.core.model.portal.PortalObject;
import org.jboss.portal.core.model.portal.Window;
import org.jboss.portal.identity.User;

/**
 * Integration logic between portal objects, instances and users.
 *
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 8786 $
 */
public interface CustomizationManager
{
   /**
    * Returns a top level named portlet instance.
    *
    * @param window the window of the portlet instance
    * @return the target instance or null if it cannot be found
    * @throws IllegalArgumentException if the window is null
    */
   Instance getInstance(Window window) throws IllegalArgumentException;

   /**
    * Returns a contextualized portlet instance for the specified user id. If the window is in the context of a
    * dashboard then the portlet instance is further customized for that specific window.
    *
    * @param window the window of the portlet instance
    * @param user   the user that can be null
    * @return the target instance or null if it cannot be found
    * @throws IllegalArgumentException if the window is null
    */
   Instance getInstance(Window window, User user) throws IllegalArgumentException;

   /**
    * Returns the dashboard of a specific user.
    *
    * @param user
    * @return
    * @throws IllegalArgumentException
    */
   Portal getDashboard(User user) throws IllegalArgumentException;

   /**
    * Returns true if the portal object is in a dashboard context for the specified user.
    *
    * @param object
    * @return
    */
   boolean isDashboard(PortalObject object, User user);

   /**
    * Destroys the dashboard of a specified user.
    *
    * @param userId
    */
   void destroyDashboard(String userId);
}
