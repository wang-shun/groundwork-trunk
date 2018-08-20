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
package org.jboss.portal.core.impl.api.session;

import org.jboss.portal.api.session.PortalSession;

import javax.servlet.http.HttpSession;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 8786 $
 */
public class PortalSessionImpl implements PortalSession
{

   /** . */
   private HttpSession session;

   public PortalSessionImpl(HttpSession session)
   {
      if (session == null)
      {
         throw new IllegalArgumentException();
      }
      this.session = session;
   }

   public String getId()
   {
      return session.getId();
   }

   public Object getAttribute(String name)
   {
      if (name == null)
      {
         throw new IllegalArgumentException();
      }
      return session.getAttribute("api." + name);
   }

   public void setAttribute(String name, Object attribute)
   {
      if (name == null)
      {
         throw new IllegalArgumentException();
      }
      session.setAttribute("api." + name, attribute);
   }

   public void removeAttribute(String name)
   {
      if (name == null)
      {
         throw new IllegalArgumentException();
      }
      session.removeAttribute("api." + name);
   }
}
