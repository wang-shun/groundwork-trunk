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
package org.jboss.portal.core.servlet.jsp;

import org.jboss.logging.Logger;
import org.jboss.portal.core.servlet.jsp.taglib.context.Context;
import org.jboss.portal.core.servlet.jsp.taglib.context.NamedContext;

import javax.portlet.PortletRequest;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.HttpJspPage;
import java.io.IOException;
import java.util.LinkedList;

/**
 * Any JSP page using the JBoss library should extend that class. It is done by adding the following line at the top of
 * the JSP <%@ page language="java" extends="org.jboss.portal.core.servlet.jsp.PortalJsp" %>
 *
 * @author <a href="theute@jboss.org">Thomas Heute </a> $Revision: 8786 $
 */
public abstract class PortalJsp
   implements HttpJspPage
{

   /** Key for context attribute in the portlet request. */
   public static final String CTX_REQUEST = "org.jboss.portal.core.context";

   /** HttpServletRequest so that it can be accessed in expression language static methods */
   public static final ThreadLocal request = new ThreadLocal();

   /** Stack of context, needed by expression language static methods */
   public static final ThreadLocal contextStack = new ThreadLocal();

   /** To log JSP related information */
   public static Logger logger = Logger.getLogger(PortalJsp.class);

   /** . */
   private ServletConfig config;

   /**
    * HttpJspPage implementation
    *
    * @see javax.servlet.jsp.HttpJspPage#_jspService(javax.servlet.http.HttpServletRequest,
    *      javax.servlet.http.HttpServletResponse)
    */
   abstract public void _jspService(HttpServletRequest arg0, HttpServletResponse arg1) throws ServletException,
      IOException;

   /**
    * HttpJspPage implementation
    *
    * @see javax.servlet.jsp.JspPage#jspInit()
    */
   public void jspInit()
   {
   }

   /**
    * HttpJspPage implementation
    *
    * @see javax.servlet.jsp.JspPage#jspDestroy()
    */
   public void jspDestroy()
   {
   }

   /**
    * HttpJspPage implementation
    *
    * @see javax.servlet.Servlet#init(javax.servlet.ServletConfig)
    */
   public void init(ServletConfig config) throws ServletException
   {
      this.config = config;
      jspInit();
   }

   /**
    * HttpJspPage implementation
    *
    * @see javax.servlet.Servlet#getServletConfig()
    */
   public ServletConfig getServletConfig()
   {
      return config;
   }

   /**
    * HttpJspPage implementation
    *
    * @see javax.servlet.Servlet#service(javax.servlet.ServletRequest,javax.servlet.ServletResponse)
    */
   public void service(ServletRequest request, ServletResponse response) throws ServletException, IOException
   {
      // Get HttpServlet* from Servlet*
      HttpServletRequest httpRequest = (HttpServletRequest)request;
      HttpServletResponse httpResponse = (HttpServletResponse)response;

      // Keep the request and contextStack
      Object formerRequest = PortalJsp.request.get();
      Object formerContextStack = PortalJsp.contextStack.get();

      // Initialize the ThreadLocal for contextMap
      PortletRequest req = (PortletRequest)request.getAttribute("javax.portlet.request");
      Object obj = req.getAttribute(CTX_REQUEST);

      LinkedList stack = new LinkedList();
      if (obj != null)
      {
         Context ctx = (Context)obj;
         NamedContext namedCtx = new NamedContext("", ctx);
         stack.addLast(namedCtx);
      }
      try
      {
         PortalJsp.request.set(httpRequest);
         PortalJsp.contextStack.set(stack);
         _jspService(httpRequest, httpResponse);
      }
      finally
      {
         PortalJsp.request.set(formerRequest);
         PortalJsp.contextStack.set(formerContextStack);
      }

   }

   /**
    * HttpJspPage implementation
    *
    * @see javax.servlet.Servlet#getServletInfo()
    */
   public String getServletInfo()
   {
      return "JBoss JSP superclass";
   }

   /**
    * HttpJspPage implementation
    *
    * @see javax.servlet.Servlet#destroy()
    */
   public void destroy()
   {
      jspDestroy();
   }

   /** For Jasper which seems to want a log method in the generated JSP. */
   protected final void log(String msg)
   {
      logger.debug(msg);
   }

}