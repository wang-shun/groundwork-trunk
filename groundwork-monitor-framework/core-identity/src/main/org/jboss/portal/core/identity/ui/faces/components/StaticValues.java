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
package org.jboss.portal.core.identity.ui.faces.components;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.portlet.PortletContext;

import org.jboss.portal.common.i18n.LocaleManager;
import org.jboss.portal.core.identity.ui.UserPortletConstants;
import org.jboss.portal.theme.PortalTheme;
import org.jboss.portal.theme.ThemeInfo;
import org.jboss.portal.theme.ThemeService;

/**
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision$
 */
public class StaticValues
{
   /**
    * Generates a list of time zones 
    * 
    * @return List <SelectItem>
    */
   public static List<SelectItem> getTimezone()
   {
      List<SelectItem> list = new ArrayList<SelectItem>();
      for (int i = 0; i < UserPortletConstants.TIME_ZONE_OFFSETS.length; i++)
      {
         if (UserPortletConstants.TIME_ZONE_OFFSETS[i] != null)
         {
            list.add(new SelectItem(""+ i, UserPortletConstants.TIME_ZONE_OFFSETS[i]));
         }
      }
      return list;
   }

   /**
    * Generates a list of available themes
    * 
    * @param FacesContext
    * @return List <SelectItem> 
    */
   public static List<SelectItem> getTheme(FacesContext ctx)
   {
      List<SelectItem> list = new ArrayList<SelectItem>();
      PortletContext pctx = (PortletContext) ctx.getExternalContext().getContext();
      ThemeService themeService = (ThemeService) pctx.getAttribute("ThemeService");
      for (Iterator<PortalTheme> i = themeService.getThemes().iterator(); i.hasNext();)
      {
         PortalTheme theme = i.next();
         ThemeInfo info = theme.getThemeInfo();
         list.add(new SelectItem(info.getRegistrationId().toString(), info.getAppId() + "." + info.getName()));
      }
      return list;
   }
   
   /**
    * Generates a list of locale
    * 
    * @param FacesContext
    * @return List <SelectItem>
    */
   public static List<SelectItem> getLocale(FacesContext ctx)
   {
      List<SelectItem> list = new ArrayList<SelectItem>();
      Locale currentLocale = ctx.getViewRoot().getLocale();
      ArrayList<Locale> locales = new ArrayList<Locale>(LocaleManager.getLocales());
      Collections.sort(locales, new LocaleComparator());
      
      for (Iterator<Locale> i = locales.iterator(); i.hasNext();)
      {
         Locale locale = i.next();
         list.add(new SelectItem(locale.toString(), locale.getDisplayName(currentLocale)));
      }
      return list;
   }
   
   private static class LocaleComparator implements Comparator<Locale>
   {

      public int compare(Locale arg0, Locale arg1)
      {
         Locale locale1 = arg0;
         Locale locale2 = arg1;
         int compare = locale1.getDisplayLanguage().compareTo(locale2.getDisplayLanguage());
         if (compare == 0)
         {
            compare = locale1.getDisplayCountry().compareTo(locale2.getDisplayCountry());
         }
         return compare;
      }
   }
}

