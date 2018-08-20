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
package org.jboss.portal.core.servlet.jsp.taglib;

import org.jboss.portal.core.servlet.jsp.PortalJsp;
import org.jboss.portal.core.servlet.jsp.taglib.context.Context;
import org.jboss.portal.core.servlet.jsp.taglib.context.NamedContext;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import java.util.LinkedList;

/**
 * Inclusion tag. Used to include a JSP page to another page.
 *
 * @author <a href="theute@jboss.org">Thomas Heute</a> $Revision: 8786 $
 */
public class IncludeTag
   extends TagSupport
{

   /** The serialVersionUID */
   private static final long serialVersionUID = -6033201557205419883L;

   /** page attribute to the tag */
   private String page;

   /** Stack of context before changing it for the inclusion */
   private LinkedList formerContextStack;

   private Context formerContext;

   /**
    * Set the page attribute. THe filename can be relative or absolute (if starts with a /)
    *
    * @param filename
    */
   public void setPage(String filename)
   {
      this.page = filename;
   }

   public int doStartTag() throws JspException
   {

      // Save the former context stack
      formerContextStack = (LinkedList)PortalJsp.contextStack.get();
      formerContext = (Context)pageContext.getRequest().getAttribute(PortalJsp.CTX_REQUEST);

      LinkedList list = (LinkedList)PortalJsp.contextStack.get();
      LinkedList stack = new LinkedList();
      if (!list.isEmpty())
      {
         // Change the context attribute to the new context
         NamedContext ctx = (NamedContext)(list).getLast();
         pageContext.getRequest().setAttribute(PortalJsp.CTX_REQUEST, ctx.getContext());

         // Change the context stack to the new context
         stack.addLast(new NamedContext("", ctx.getContext()));
      }

      // Include the JSP page
      try
      {
         pageContext.include(page);
         PortalJsp.contextStack.set(stack);
      }
      catch (Exception e)
      {
         PortalJsp.logger.error("Cannot include page: " + page, e);
      }
      finally
      {
         // Put back to the original state
         PortalJsp.contextStack.set(formerContextStack);
         pageContext.getRequest().setAttribute(PortalJsp.CTX_REQUEST, formerContext);
      }
      return SKIP_BODY;
   }

}
