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
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Include a piece of code or not according to a condition
 *
 * @author <a href="theute@jboss.org">Thomas Heute</a> $Revision: 11068 $
 */
public class IfTag
   extends TagSupport
{
   /** The serialVersionUID */
   private static final long serialVersionUID = -7906376517169142721L;

   /** Context of the section */
   private String ctx;

   private Iterator children;

   private LinkedList contextStack;

   private boolean include = false;

   /** @param ctx The ctx to set. */
   public void setCtx(String ctx)
   {
      this.ctx = ctx;
   }


   public int doStartTag() throws JspException
   {
      contextStack = (LinkedList)PortalJsp.contextStack.get();

      // If no context has been set
      if (contextStack.isEmpty())
      {
         include = false;
         PortalJsp.logger.debug("No context has been found");
         return SKIP_BODY;
      }

      Context currentContext = ((NamedContext)contextStack.getLast()).getContext();
      children = currentContext.childIterator(ctx);

      if (children.hasNext())
      {
         include = true;
         contextStack.addLast(new NamedContext(ctx, (Context)children.next()));
         return EVAL_PAGE;
      }
      else
      {
         include = false;
         return SKIP_BODY;
      }
   }

   public int doEndTag()
   {
      if (include)
      {
         contextStack.removeLast();
      }
      return SKIP_BODY;
   }
}
