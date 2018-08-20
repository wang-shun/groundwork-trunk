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
package org.jboss.portal.core.controller.portlet;

import org.jboss.portal.common.invocation.AttributeResolver;
import org.jboss.portal.common.invocation.Scope;
import org.jboss.portal.common.util.Tools;
import org.jboss.portal.core.controller.ControllerContext;
import org.jboss.portal.identity.User;
import org.jboss.portal.portlet.spi.UserContext;
import org.jboss.portal.server.ServerInvocation;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 11127 $
 */
public class ControllerUserContext implements UserContext
{

   /** . */
   private final ServerInvocation invocation;

   /** . */
   private final User user;

   /** . */
   private final Map<String, String> profile;
   
   /** . */
   private AttributeResolver principalAttributeResolver;

   public ControllerUserContext(ControllerContext controllerContext)
   {
      this.invocation = controllerContext.getServerInvocation();
      this.user = controllerContext.getUser();
      this.profile = controllerContext.getUserProfile();
      this.principalAttributeResolver = controllerContext.getAttributeResolver(Scope.PRINCIPAL_SCOPE);
   }

   public String getId()
   {
      return user != null ? user.getUserName() : null;
   }

   public Map<String, String> getInformations()
   {
      return profile;
   }

   public User getUser()
   {
      return user;
   }

   public Locale getLocale()
   {
      return invocation.getRequest().getLocale();
   }

   public List<Locale> getLocales()
   {
      return Tools.toList(invocation.getRequest().getLocales());
   }
   
   public Object getAttribute(String arg0)
   {
      return principalAttributeResolver.getAttribute(arg0);
   }

   public void setAttribute(String arg0, Object arg1)
   {
      principalAttributeResolver.setAttribute(arg0, arg1);
   }

}
