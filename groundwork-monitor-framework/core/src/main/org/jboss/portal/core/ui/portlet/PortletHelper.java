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
package org.jboss.portal.core.ui.portlet;

import org.jboss.logging.Logger;
import org.jboss.portal.core.servlet.jsp.PortalJsp;
import org.jboss.portlet.JBossActionRequest;
import org.jboss.portlet.JBossActionResponse;
import org.jboss.portlet.JBossPortlet;

import java.util.Locale;
import java.util.ResourceBundle;

/** @author <a href="theute@jboss.org">Thomas Heute </a> $Revision: 8786 $ */
public class PortletHelper
{
   private Logger log = Logger.getLogger(PortalJsp.class);

   private JBossPortlet portlet;

   public PortletHelper(JBossPortlet portlet)
   {
      this.portlet = portlet;
   }

   public void setRenderParameter(JBossActionResponse resp, String key, String value)
   {
      if (value != null)
      {
         resp.setRenderParameter(key, value);
      }
   }

   public void setI18nRenderParameter(JBossActionRequest req, JBossActionResponse resp, String key, String value)
   {
      if (value != null)
      {
         Locale locale = req.getLocale();
         ResourceBundle bundle = portlet.getResourceBundle(locale);
         try
         {
            resp.setRenderParameter(key, bundle.getString(value));
         }
         catch (Exception e)
         {
            log.error("Cannot find language key: " + key);
            resp.setRenderParameter(key, value);
         }
      }
   }
}