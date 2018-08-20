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
package org.jboss.portal.core.model.instance;

import org.jboss.logging.Logger;
import org.jboss.portal.Mode;
import org.jboss.portal.common.invocation.InvocationException;
import org.jboss.portal.portlet.PortletInvokerException;
import org.jboss.portal.portlet.PortletInvokerInterceptor;
import org.jboss.portal.portlet.invocation.PortletInvocation;
import org.jboss.portal.portlet.invocation.response.InsufficientPrivilegesResponse;
import org.jboss.portal.portlet.invocation.response.PortletInvocationResponse;
import org.jboss.portal.security.PortalSecurityException;
import org.jboss.portal.security.spi.auth.PortalAuthorizationManager;
import org.jboss.portal.security.spi.auth.PortalAuthorizationManagerFactory;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 11068 $
 */
public class InstanceSecurityInterceptor extends PortletInvokerInterceptor
{

   /** . */
   private Logger log = Logger.getLogger(InstanceSecurityInterceptor.class);

   /** . */
   private boolean trace = log.isTraceEnabled();

   /** . */
   private PortalAuthorizationManagerFactory pamf;

   public PortalAuthorizationManagerFactory getPortalAuthorizationManagerFactory()
   {
      return pamf;
   }

   public void setPortalAuthorizationManagerFactory(PortalAuthorizationManagerFactory portalAuthorizationManagerFactory)
   {
      this.pamf = portalAuthorizationManagerFactory;
   }

   public PortletInvocationResponse invoke(PortletInvocation invocation) throws IllegalArgumentException, PortletInvokerException
   {
      try
      {
         // Compute the security mask
         int mask = InstancePermission.VIEW_MASK;
         Mode mode = invocation.getMode();
         if (Mode.ADMIN.equals(mode))
         {
            mask |= InstancePermission.ADMIN_MASK;
         }

         //
         String instanceid = (String)invocation.getAttribute(Instance.INSTANCE_ID_ATTRIBUTE);
         PortalAuthorizationManager pam = pamf.getManager();
         InstancePermission perm = new InstancePermission(instanceid, mask);
         boolean authorized = pam.checkPermission(perm);

         //
         //
         if (trace)
         {
            log.trace("Access granted=" + authorized + " for instance " + instanceid);
         }
         if (!authorized)
         {
            return new InsufficientPrivilegesResponse();
         }
         else
         {
            return super.invoke(invocation);
         }
      }
      catch (PortalSecurityException e)
      {
         throw new InvocationException(e);
      }
   }
}
