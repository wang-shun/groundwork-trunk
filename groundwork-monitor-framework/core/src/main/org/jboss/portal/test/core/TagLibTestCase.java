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
import org.apache.cactus.WebResponse;
import org.jboss.portal.core.servlet.jsp.PortalJsp;
import org.jboss.portal.core.servlet.jsp.taglib.context.DelegateContext;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import java.io.IOException;

/** @author <a href="theute@jboss.org">Thomas Heute</a> $Revision: 8786 $ */
public class TagLibTestCase
   extends JspTestCase
{
   public void test01() throws ServletException, IOException
   {
      RequestDispatcher rd = config.getServletContext().
         getRequestDispatcher("/WEB-INF/jsp/test/testTaglib01.jsp");
      rd.forward(request, response);
   }

   public void end01(WebResponse webResponse)
   {
      assertEquals(":::", webResponse.getText().replaceAll("[ \n\t]", ""));
   }

   public void test02() throws ServletException, IOException
   {
      RequestDispatcher rd = config.getServletContext().
         getRequestDispatcher("/WEB-INF/jsp/test/testTaglib01.jsp");
      DelegateContext context = new DelegateContext();
      context.put("foo", "FOO");
      context.put("bar", "BAR");
      request.setAttribute(PortalJsp.CTX_REQUEST, context);

      rd.forward(request, response);
   }

   public void end02(WebResponse webResponse)
   {
      assertEquals("FOO:FOO:BAR:BAR", webResponse.getText().replaceAll("[ \n\t]", ""));
   }

   public void test03() throws ServletException, IOException
   {
      RequestDispatcher rd = config.getServletContext().
         getRequestDispatcher("/WEB-INF/jsp/test/testTaglib01.jsp");
      DelegateContext context = new DelegateContext();
      context.put("foo", "FOO");
      context.put("bar", "BAR");
      context.next("row1");
      request.setAttribute(PortalJsp.CTX_REQUEST, context);

      rd.forward(request, response);
   }

   public void end03(WebResponse webResponse)
   {
      assertEquals("FOO:FOO::BAR:BAR", webResponse.getText().replaceAll("[ \n\t]", ""));
   }

   public void test04() throws ServletException, IOException
   {
      RequestDispatcher rd = config.getServletContext().
         getRequestDispatcher("/WEB-INF/jsp/test/testTaglib01.jsp");
      DelegateContext context = new DelegateContext();
      context.put("foo", "FOO");
      context.put("bar", "BAR");
      DelegateContext row1 = context.next("row1");
      row1.put("value1", "VALUE");
      request.setAttribute(PortalJsp.CTX_REQUEST, context);

      rd.forward(request, response);
   }

   public void end04(WebResponse webResponse)
   {
      assertEquals("FOO:FOO:VALUE:BAR:BAR", webResponse.getText().replaceAll("[ \n\t]", ""));
   }
}
