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
package org.jboss.portal.core.aspects.server;

import org.jboss.portal.common.i18n.LocaleFormat;
import org.jboss.portal.common.invocation.InvocationException;
import org.jboss.portal.common.util.ConversionException;
import org.jboss.portal.identity.User;
import org.jboss.portal.server.ServerInterceptor;
import org.jboss.portal.server.ServerInvocation;
import org.jboss.portal.server.ServerRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * If the user is authenticated and has a preferred locale then this one is chosen. Otherwhise the locale used is the
 * one determined by the incoming HttpServletRequest provided by the servlet container.
 * <p/>
 * todo add cookie or session based locales
 *
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 8786 $
 */
public class LocaleInterceptor extends ServerInterceptor
{
   protected void invoke(ServerInvocation invocation) throws Exception, InvocationException
   {
      // The locales
      List locales = new ArrayList(4);

      //
      Map profile = (Map)invocation.getAttribute(ServerInvocation.PRINCIPAL_SCOPE, UserInterceptor.PROFILE_KEY);

      //
      if (profile != null)
      {
         Locale locale = null;
         Object lc = profile.get(User.INFO_USER_LOCALE);
         if (lc instanceof Locale)
         {
            locale = (Locale)lc;
         }
         else if (lc != null)
         {
            try
            {
               locale = LocaleFormat.DEFAULT.getLocale(lc.toString());
            }
            catch (ConversionException e)
            {
               //just to hide failure
            }
         }

         //
         if (locale != null)
         {
            locales.add(locale);
         }
      }

      // Get the locale from the user agent.
      ServerRequest req = invocation.getRequest();
      locales.add(invocation.getServerContext().getClientRequest().getLocale());

      try
      {
         // Set the locale for the request
         Locale[] tmp = (Locale[])locales.toArray(new Locale[locales.size()]);
         req.setLocales(tmp);

         // Invokoe
         invocation.invokeNext();
      }
      finally
      {
         // Clear the locale
         req.setLocales(null);
      }
   }
}
