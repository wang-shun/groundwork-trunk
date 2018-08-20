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
import org.jboss.portal.core.servlet.jsp.taglib.context.DelegateContext;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import java.io.IOException;

/** @author <a href="theute@jboss.org">Thomas Heute </a> $Revision: 8786 $ */
public class IfTagTestCase
   extends ServletTestCase

{
   /**
    * If condition is not verified
    *
    * @throws ServletException
    * @throws IOException
    */
   public void testIfFalse() throws ServletException, IOException
   {
      RequestDispatcher rd = config.getServletContext().getRequestDispatcher("/WEB-INF/jsp/test/testIf01.jsp");
      rd.forward(request, response);
   }

   public void endIfFalse(WebResponse webResponse)
   {
      assertEquals("", webResponse.getText().trim());
   }

   /**
    * If condition is verified
    *
    * @throws ServletException
    * @throws IOException
    */
   public void testIfTrue() throws ServletException, IOException
   {
      RequestDispatcher rd = config.getServletContext().getRequestDispatcher("/WEB-INF/jsp/test/testIf01.jsp");
      DelegateContext context = new DelegateContext();
      context.next("IfCond");

      // todo : fixme
      //PortletRequest req = new ActionRequestImpl(null, null, null, null, null, null, request);
      //req.setAttribute(PortalJsp.CTX_REQUEST, context);
      //request.setAttribute("javax.portlet.request", req);

      //rd.forward(request, response);
   }

   public void endIfTrue(WebResponse webResponse)
   {
      assertEquals("Some text", webResponse.getText().trim());
   }

}