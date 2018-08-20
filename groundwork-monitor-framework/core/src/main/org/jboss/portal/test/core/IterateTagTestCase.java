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

import org.apache.cactus.ServletTestCase;
import org.apache.cactus.WebResponse;
import org.jboss.portal.core.servlet.jsp.PortalJsp;
import org.jboss.portal.core.servlet.jsp.taglib.context.DelegateContext;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import java.io.IOException;

/** @author <a href="theute@jboss.org">Thomas Heute</a> $Revision: 8786 $ */
public class IterateTagTestCase
   extends ServletTestCase


{
   /**
    * If condition is not verified
    *
    * @throws ServletException
    * @throws IOException
    */
   public void testIterateNone() throws ServletException, IOException
   {
      RequestDispatcher rd = config.getServletContext().
         getRequestDispatcher("/WEB-INF/jsp/test/testIterate01.jsp");
      rd.forward(request, response);
   }

   public void endIterateNone(WebResponse webResponse)
   {
      assertEquals("", webResponse.getText().trim());
   }

   /**
    * If condition is verified
    *
    * @throws ServletException
    * @throws IOException
    */
   public void testIterateOnce() throws ServletException, IOException
   {
      RequestDispatcher rd = config.getServletContext().
         getRequestDispatcher("/WEB-INF/jsp/test/testIterate01.jsp");
      DelegateContext context = new DelegateContext();
      context.next("row");
      request.setAttribute(PortalJsp.CTX_REQUEST, context);
      rd.forward(request, response);
   }

   public void endIterateOnce(WebResponse webResponse)
   {
      assertEquals("Some text", webResponse.getText().trim());
   }

   /**
    * If condition is verified
    *
    * @throws ServletException
    * @throws IOException
    */
   public void testIterateFive() throws ServletException, IOException
   {
      RequestDispatcher rd = config.getServletContext().
         getRequestDispatcher("/WEB-INF/jsp/test/testIterate01.jsp");
      DelegateContext context = new DelegateContext();
      context.next("row");
      context.next("row");
      context.next("row");
      context.next("row");
      context.next("row");
      request.setAttribute(PortalJsp.CTX_REQUEST, context);
      rd.forward(request, response);
   }

   public void endIterateFive(WebResponse webResponse)
   {
      assertEquals("Some textSome textSome textSome textSome text", webResponse.getText().trim());
   }

   /**
    * If condition is verified
    *
    * @throws ServletException
    * @throws IOException
    */
   public void testDoubleIterate() throws ServletException, IOException
   {
      RequestDispatcher rd = config.getServletContext().
         getRequestDispatcher("/WEB-INF/jsp/test/testIterate02.jsp");
      DelegateContext context = new DelegateContext();
      DelegateContext row1 = context.next("row");
      DelegateContext row2 = context.next("row");
      DelegateContext col1 = row2.next("col");
      DelegateContext col2 = row2.next("col");
      request.setAttribute(PortalJsp.CTX_REQUEST, context);
      rd.forward(request, response);
   }

   public void endDoubleIterate(WebResponse webResponse)
   {
      assertEquals("ACABBC", webResponse.getText().trim());
   }

   /**
    * If condition is verified
    *
    * @throws ServletException
    * @throws IOException
    */
   public void testTripleIterate() throws ServletException, IOException
   {
      RequestDispatcher rd = config.getServletContext().
         getRequestDispatcher("/WEB-INF/jsp/test/testIterate03.jsp");
      DelegateContext context = new DelegateContext();
      DelegateContext row1 = context.next("row");
      DelegateContext row2 = context.next("row");
      DelegateContext col1 = row2.next("col");
      DelegateContext col2 = row2.next("col");
      DelegateContext foo1 = col2.next("foo");
      request.setAttribute(PortalJsp.CTX_REQUEST, context);
      rd.forward(request, response);
   }

   public void endTripleIterate(WebResponse webResponse)
   {
      assertEquals("ADABBCD", webResponse.getText().trim());
   }
}
