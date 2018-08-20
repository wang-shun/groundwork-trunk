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
import org.jboss.portal.core.servlet.jsp.taglib.context.Context;
import org.jboss.portal.core.servlet.jsp.taglib.context.DelegateContext;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Test the out EL
 *
 * @author <a href="theute@jboss.org">Thomas Heute</a> $Revision: 8786 $
 */
public class OutELTestCase
   extends JspTestCase
{

   public void test01() throws ServletException, IOException
   {
      /**
       ServletRequest request = new HttpServletRequestImpl();
       ServletResponse response = new HttpServletResponseImpl();
       */

      PortalJsp jbossJsp = new PortalJsp()
      {
         public void _jspService(HttpServletRequest arg0, HttpServletResponse arg1) throws ServletException, IOException
         {
            assertEquals("value", PortalLib.out("key"));
         }
      };

      Context ctx = new DelegateContext();
      ctx.put("key", "value");
      request.setAttribute(PortalJsp.CTX_REQUEST, ctx);

      jbossJsp.service(request, response);
      jbossJsp.destroy();
   }


}
