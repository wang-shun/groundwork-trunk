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
package org.jboss.portal.core.controller;

import org.jboss.portal.common.servlet.BufferingRequestWrapper;
import org.jboss.portal.common.servlet.BufferingResponseWrapper;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import java.io.IOException;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 8786 $
 */
public class ControllerRequestDispatcher
{

   private final RequestDispatcher dispatcher;
   private final BufferingRequestWrapper req;
   private final BufferingResponseWrapper resp;

   public ControllerRequestDispatcher(RequestDispatcher dispatcher, BufferingRequestWrapper req, BufferingResponseWrapper resp)
   {
      this.dispatcher = dispatcher;
      this.req = req;
      this.resp = resp;
   }

   public String getMarkup()
   {
      return resp.getContent();
   }

   public void setAttribute(String name, Object value)
   {
      req.setAttribute(name, value);
   }

   public Object getAttribute(String name)
   {
      return req.getAttribute(name);
   }

   public void include()
   {
      try
      {
         dispatcher.include(req, resp);
      }
      catch (ServletException e)
      {
         e.printStackTrace();
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
   }

}
