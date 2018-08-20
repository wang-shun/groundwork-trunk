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
package org.jboss.portal.core.servlet.jsp.taglib;

import org.jboss.portal.core.servlet.jsp.PortalJsp;
import org.jboss.portal.core.servlet.jsp.taglib.context.Context;
import org.jboss.portal.core.servlet.jsp.taglib.context.NamedContext;

import javax.portlet.PortletConfig;
import javax.servlet.http.HttpServletRequest;
import java.util.LinkedList;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Expression language static functions for the JBoss library.
 *
 * @author <a href="theute@jboss.org">Thomas Heute</a> $Revision: 8786 $
 */
public class PortalLib
{

   /**
    * Internationalize messages
    *
    * @param key Key of the message
    * @return The corresponding value in Resource.
    */
   public static String getMessage(String key)
   {
      ThreadLocal threadRequest = PortalJsp.request;
      HttpServletRequest request = (HttpServletRequest)(threadRequest.get());
      Locale locale = request.getLocale();
      PortletConfig portletConfig = (PortletConfig)request.getAttribute("javax.portlet.config");
      ResourceBundle resourceBundle = portletConfig.getResourceBundle(locale);
      PortalJsp.logger.debug("Use locale:" + locale);
      try
      {
         return resourceBundle.getString(key);
      }
      catch (MissingResourceException e)
      {
         PortalJsp.logger.error("No such resource key in resource file: " + key);
         return key;
      }
   }

   /**
    * Return a translated message for a context value
    *
    * @param key The context value
    * @return Translated text
    */
   public static String i18nOut(String key)
   {
      return getMessage(out(key));
   }


   /**
    * Print a value from the context
    *
    * @param key The context path to the value requested
    * @return The value defined in the context
    */
   public static String out(String key)
   {
      ThreadLocal contextStackLocal = PortalJsp.contextStack;
      LinkedList contextStack = (LinkedList)contextStackLocal.get();

      if (contextStack.isEmpty())
      {
         PortalJsp.logger.warn("No context has been defined when trying to access " + key);
         return "";
      }

      // Split by the dot
      String[] ctxNames = key.split("[.]");

      NamedContext tmp = null;
      int i = 0;

      // Check that the path is correct
      for (i = 0; i < ctxNames.length - 1; i++)
      {
         try
         {
            tmp = (NamedContext)contextStack.get(i + 1);
         }
         catch (IndexOutOfBoundsException e)
         {
            PortalJsp.logger.warn("The key you called: " + key + " is not valid, please check the key");
            return "";
         }
         if (!ctxNames[i].equals(tmp.getName()))
         {
            PortalJsp.logger.warn("The context you called: " + ctxNames[i] + " does not match " + tmp.getName());
            return "";
         }
      }

      if (contextStack.get(i) != null)
      {
         NamedContext ctx = (NamedContext)contextStack.get(i);
         return convert(((Context)ctx.getContext()).get(ctxNames[ctxNames.length - 1]));
      }
      else
      {
         PortalJsp.logger.warn("There is no such context for " + key);
         return "";
      }
   }

   private static String convert(String toto)
   {
      if (toto != null)
      {
         toto = toto.replaceAll("<", "&lt;");
         toto = toto.replaceAll(">", "&gt;");
      }
      return toto;
   }

}
