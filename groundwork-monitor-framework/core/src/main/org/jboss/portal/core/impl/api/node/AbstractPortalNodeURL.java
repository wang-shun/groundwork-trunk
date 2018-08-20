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
package org.jboss.portal.core.impl.api.node;

import org.jboss.portal.api.node.PortalNodeURL;
import org.jboss.portal.core.controller.ControllerContext;
import org.jboss.portal.core.model.portal.PortalObjectId;
import org.jboss.portal.portlet.ParametersStateString;
import org.jboss.portal.server.request.URLContext;
import org.jboss.portal.common.util.ParameterMap;

import java.util.Map;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 11549 $
 */
public class AbstractPortalNodeURL implements PortalNodeURL
{

   /** . */
   protected PortalObjectId id;

   /** . */
   protected ControllerContext controllerContext;

   /** . */
   protected Map<String, String[]> parameters;

   /** . */
   protected Boolean wantSecure;

   /** . */
   protected Boolean wantAuthenticated;

   /** . */
   protected boolean relative;

   /** . */
   protected URLContext urlContext;

   public AbstractPortalNodeURL(PortalObjectId id, ControllerContext controllerContext)
   {
      this.id = id;
      this.controllerContext = controllerContext;
      this.relative = true;
   }

   public void setParameter(String name, String value)
   {
      setParameter(name, new String[]{value});
   }

   public void setParameter(String name, String[] values)
   {
      if (parameters == null)
      {
         parameters = new ParameterMap();
      }
      parameters.put(name, values);
   }

   public void setAuthenticated(Boolean authenticated)
   {
      this.wantAuthenticated = authenticated;

      //
      this.urlContext = null;
   }

   public void setSecure(Boolean secure)
   {
      this.wantSecure = secure;

      //
      this.urlContext = null;
   }

   public void setRelative(boolean relative)
   {
      this.relative = relative;
   }

   protected URLContext getURLContext()
   {
      if (urlContext == null)
      {
         URLContext tmp = controllerContext.getServerInvocation().getServerContext().getURLContext();

         //
         if (wantSecure != null)
         {
            if (wantSecure.booleanValue())
            {
               tmp = tmp.asSecured();
            }
            else
            {
               tmp = tmp.asNonSecured();
            }
         }

         //
         if (wantAuthenticated != null)
         {
            if (wantAuthenticated.booleanValue())
            {
               tmp = tmp.asAuthenticated();
            }
            else
            {
               tmp = tmp.asNonAuthenticated();
            }
         }

         //
         urlContext = tmp;
      }

      //
      return urlContext;
   }
}
