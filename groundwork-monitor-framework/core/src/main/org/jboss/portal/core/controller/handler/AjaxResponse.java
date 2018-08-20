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
package org.jboss.portal.core.controller.handler;

import org.jboss.portal.server.ServerInvocationContext;
import org.jboss.portal.theme.impl.render.dynamic.DynaResponse;
import org.jboss.portal.theme.impl.render.dynamic.JSONMarshaller;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 8786 $
 */
public class AjaxResponse extends HandlerResponse
{

   /** . */
   private static final JSONMarshaller marshaller = new JSONMarshaller();

   /** The response to the client. */
   private final DynaResponse dynaResponse;

   public AjaxResponse(DynaResponse dynaResponse)
   {
      this.dynaResponse = dynaResponse;
   }

   public void sendResponse(ServerInvocationContext ctx) throws IOException, ServletException
   {
      HttpServletResponse resp = ctx.getClientResponse();
      resp.setContentType("text/html");
      marshaller.write(dynaResponse, resp.getWriter());
   }
}
