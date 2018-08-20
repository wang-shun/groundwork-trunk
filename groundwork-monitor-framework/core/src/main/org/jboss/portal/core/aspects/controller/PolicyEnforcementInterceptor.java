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
package org.jboss.portal.core.aspects.controller;


import org.jboss.logging.Logger;
import org.jboss.portal.common.invocation.InvocationException;
import org.jboss.portal.core.controller.ControllerCommand;
import org.jboss.portal.core.controller.ControllerInterceptor;
import org.jboss.portal.core.controller.ControllerResponse;
import org.jboss.portal.core.controller.SecurityException;
import org.jboss.portal.core.controller.command.response.SecurityErrorResponse;
import org.jboss.portal.security.PortalSecurityException;
import org.jboss.portal.security.spi.auth.PortalAuthorizationManager;
import org.jboss.portal.security.spi.auth.PortalAuthorizationManagerFactory;

/**
 * This aspect enforces security policy for all commands. <p/> <p>Portal resources should only be accessible to
 * individuals that are entitled to do so. This interceptor makes sure that the requested resource is available to the
 * requesting subject, by utilizing the configured JACC Policy.</p>
 *
 * @author <a href="mailto:mholzner@novell.com>Martin Holzner</a>
 * @author julien@jboss.org
 * @version $LastChangedRevision: 8786 $, $LastChangedDate: 2007-10-27 21:14:48 -0400 (Sat, 27 Oct 2007) $
 */
public final class PolicyEnforcementInterceptor extends ControllerInterceptor
{

   /** Our logger. */
   private static Logger log = Logger.getLogger(PolicyEnforcementInterceptor.class);

   /** Trace . */
   protected boolean isTrace = log.isTraceEnabled();

   /**
    * @param cmd
    * @throws org.jboss.portal.common.invocation.InvocationException
    *
    */
   public ControllerResponse invoke(ControllerCommand cmd) throws Exception, InvocationException
   {
      try
      {
         PortalAuthorizationManagerFactory pamf = cmd.getControllerContext().getController().getPortalAuthorizationManagerFactory();
         PortalAuthorizationManager pam = pamf.getManager();
         cmd.enforceSecurity(pam);
      }
      catch (PortalSecurityException e)
      {
         return new SecurityErrorResponse(e, SecurityErrorResponse.NOT_AUTHORIZED, true);
      }
      catch (SecurityException e)
      {
         return new SecurityErrorResponse(e, SecurityErrorResponse.NOT_AUTHORIZED, false);
      }

      //
      return (ControllerResponse)cmd.invokeNext();
   }
}