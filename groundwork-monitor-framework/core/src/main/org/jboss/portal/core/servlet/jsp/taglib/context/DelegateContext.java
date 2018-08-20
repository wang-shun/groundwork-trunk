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
package org.jboss.portal.core.servlet.jsp.taglib.context;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet </a>
 * @version $Revision: 8786 $
 */
public class DelegateContext
   implements Context
{
   /** context's children */
   private ChildrenStrategy children;

   /** template name/value pairs for substitution */
   private Map delegate;

   /** creates a new DelegateContext object. */
   public DelegateContext()
   {
      this(new HashMap(), new HashMap());
   }

   /**
    * creates a new DelegateContext object.
    *
    * @param delegate name/value pairs for "root" data
    * @param children name/value pairs for nested or loop data
    */
   public DelegateContext(Map delegate, Map children)
   {
      this.delegate = delegate;
      this.children = new ChildrenStrategy(children);
   }

   /**
    * create a new "root" data context and name/value pairs to be used as nested or loop data
    *
    * @param children name/value pairs for nested or loop data
    * @return context to continue adding template data too
    */
   public static final DelegateContext createWithChildren(Map children)
   {
      return new DelegateContext(new HashMap(), children);
   }

   /**
    * create a new data context with already existing name/value pairs
    *
    * @param values existing name/value pair map
    * @return context to continue adding template data too
    */
   public static final DelegateContext createWithValues(Map values)
   {
      return new DelegateContext(values, new HashMap());
   }

   /**
    * add an existing data context into this context for use in template loops or nested template data.
    *
    * @param name variable prefix name
    * @param ctx  context to add
    */
   public void append(String name, Context ctx)
   {
      children.append(name, ctx);
   }

   /** @see org.jboss.portal.core.servlet.jsp.taglib.context.Context#childIterator(java.lang.String) */
   public Iterator childIterator(String name)
   {
      return children.childIterator(name);
   }

   /** @see org.jboss.portal.core.servlet.jsp.taglib.context.Context#get(java.lang.String) */
   public String get(String key)
   {
      return (String)delegate.get(key);
   }

   /**
    * create a new object to place data for use in template loops or nested template data. tpl var format: {
    * prefix.VAR_NAME }
    *
    * @param name variable prefix name
    * @return delegate context that will contain the loop or nested data
    */
   public DelegateContext next(String name)
   {
      DelegateContext ctx = new DelegateContext();
      append(name, ctx);

      return ctx;
   }

   /** @see org.jboss.portal.core.servlet.jsp.taglib.context.Context#put(java.lang.String,java.lang.String) */
   public Context put(String key, String value)
   {
      delegate.put(key, value);

      return this;
   }

   public Context put(String key, Integer value)
   {
      put(key, value.toString());
      return this;
   }
}