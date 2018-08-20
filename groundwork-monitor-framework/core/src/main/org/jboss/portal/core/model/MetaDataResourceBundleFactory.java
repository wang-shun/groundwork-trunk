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

import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.jboss.logging.Logger;
import org.jboss.portal.common.i18n.ResourceBundleFactory;
import org.jboss.portal.common.i18n.ResourceBundleManager;
import org.jboss.portal.common.util.EmptyResourceBundle;

/**
 * @author <a href="mailto:theute@jboss.org">Thomas Heute</a>
 * @version $Revision$
 */
public class MetaDataResourceBundleFactory implements ResourceBundleFactory
{

   private Logger logger = Logger.getLogger(MetaDataResourceBundleFactory.class);
   
   public static ResourceBundleManager createResourceBundleManager(ClassLoader classLoader, List supportedLocales, String baseName)
   {
      if (classLoader == null)
      {
         throw new IllegalArgumentException("Need a non null classloader");
      }
      if (supportedLocales == null)
      {
         throw new IllegalArgumentException("Supported locales cannot be null");
      }

      // Create factory
      MetaDataResourceBundleFactory factory = new MetaDataResourceBundleFactory(classLoader, baseName);

      // Create manager
      ResourceBundleManager manager = new ResourceBundleManager(EmptyResourceBundle.INSTANCE, factory);

      // Preload declared locales
      for (Iterator i = supportedLocales.iterator();i.hasNext();)
      {
         Locale locale = (Locale)i.next();
         manager.getResourceBundle(locale);
      }

      //
      return manager;
   }
   
   private ClassLoader classLoader;
   private String baseName;
   
   public MetaDataResourceBundleFactory(ClassLoader classLoader, String baseName)
   {
      this.classLoader = classLoader;
      this.baseName = baseName;
   }
   
   public ResourceBundle getBundle(Locale locale) throws IllegalArgumentException
   {
      if (locale == null)
      {
         throw new IllegalArgumentException("Locale cannot be null");
      }
      
      try
      {
         return ResourceBundle.getBundle(baseName, locale, classLoader);
      }
      catch (MissingResourceException e)
      {
         logger.warn("Could not find resource bundle: " + baseName + " for locale: " + locale);
      }
      return EmptyResourceBundle.INSTANCE;
   }

}

