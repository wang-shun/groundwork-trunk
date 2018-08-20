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
package org.jboss.portal.test.core;

import org.apache.cactus.JspTestCase;
import org.jboss.portal.core.servlet.jsp.PortalJsp;
import org.jboss.portal.core.servlet.jsp.taglib.PortalLib;
import org.jboss.portal.portlet.impl.jsr168.PortletResourceBundleFactory;
///import org.jboss.portal.portlet.impl.jsr168.metadata.LanguagesMetaData;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/** @author <a href="theute@jboss.org">Thomas Heute</a> $Revision: 10228 $ */
public class I18nELTestCase
   extends JspTestCase
{
   private static final String RESOURCE_BUNDLE_NAME = "MyResourceBundle";

/*
   public void test01() throws ServletException, IOException
   {
      PortalJsp jbossJsp = new PortalJsp()
      {
         public void _jspService(HttpServletRequest arg0, HttpServletResponse arg1) throws ServletException, IOException
         {
            assertEquals("Test_fr", PortalLib.getMessage("test"));
         }
      };
      // Builder loader
      ResourceClassLoader loader = new ResourceClassLoader(Thread.currentThread().getContextClassLoader());
      loader.addResource(RESOURCE_BUNDLE_NAME + "_fr.properties", "javax.portlet.title=Title_fr\ntest=Test_fr\n");

      // Build the language metadata
      LanguagesMetaData.InfoMetaData infoMD = new LanguagesMetaData.InfoMetaData();
      infoMD.setTitle("Title");
      infoMD.setShortTitle("ShortTitle");
      LanguagesMetaData languagesMD = new LanguagesMetaData();
      languagesMD.setInfo(infoMD);
      languagesMD.setResourceBundle(RESOURCE_BUNDLE_NAME);

      // Build the bundle
      PortletResourceBundleFactory rbs = new PortletResourceBundleFactory(loader, languagesMD);

//      PortletConfig portletConfig = new PortletConfigImpl("testPortlet", null, null, rbs);
//      request.setAttribute("javax.portlet.config", portletConfig);

      HttpServletRequestWrapperImpl requestWrapper = new HttpServletRequestWrapperImpl(request);
      requestWrapper.setLocale(Locale.FRENCH);

      jbossJsp.service(requestWrapper, response);
      jbossJsp.destroy();
   }
*/
   private class ResourceClassLoader extends ClassLoader
   {

      private Map resources = new HashMap();

      public ResourceClassLoader(ClassLoader parent)
      {
         super(parent);
      }

      public void addResource(String name, String content)
      {
         if (name == null)
         {
            throw new IllegalArgumentException();
         }
         if (content == null)
         {
            throw new IllegalArgumentException();
         }
         resources.put(name, content.getBytes());
      }

      public InputStream getResourceAsStream(String name)
      {
         if (resources.containsKey(name))
         {
            return new ByteArrayInputStream((byte[])resources.get(name));
         }
         else
         {
            return super.getResourceAsStream(name);
         }
      }
   }

}
