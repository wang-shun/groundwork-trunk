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

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import java.io.IOException;

/** @author <a href="theute@jboss.org">Thomas Heute</a> $Revision: 8786 $ */
public class IncludeTagTestCase
   extends JspTestCase
{
   public void test01() throws ServletException, IOException
   {
      RequestDispatcher rd = config.getServletContext().
         getRequestDispatcher("/WEB-INF/jsp/test/testInclude01.jsp");
      rd.forward(request, response);
   }

   public void end01(WebResponse webResponse)
   {
      assertEquals("BeginInclude:Include:EndInclude", webResponse.getText().replaceAll("[ \n\t]", ""));
   }

   public void test02() throws ServletException, IOException
   {
      RequestDispatcher rd = config.getServletContext().
         getRequestDispatcher("/WEB-INF/jsp/test/testInclude03.jsp");
      rd.forward(request, response);
   }

   public void end02(WebResponse webResponse)
   {
      assertEquals("Foo:BeginInclude:Include:EndInclude:Bar", webResponse.getText().replaceAll("[ \n\t]", ""));
   }


}
