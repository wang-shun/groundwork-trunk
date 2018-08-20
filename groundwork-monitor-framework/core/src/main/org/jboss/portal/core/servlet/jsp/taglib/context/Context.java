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

import java.util.Collections;
import java.util.Iterator;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet </a>
 * @version $Revision: 8786 $
 */
public interface Context
{
   /** null context */
   Context NULL_CONTEXT = new AbstractContext()
   {
      final Iterator it = Collections.EMPTY_LIST.iterator();

      public Iterator childIterator(String name)
      {
         return it;
      }

      public String get(String key)
      {
         return "";
      }

      public Context put(String key, String value)
      {
         return this;
      }

      public Context put(String key, Integer value)
      {
         return this;
      }
   };

   /**
    * get the template data from the context
    *
    * @param key template variable name
    * @return template value
    */
   public String get(String key);

   /**
    * add data to be rendered in the template through variable substitution
    *
    * @param key   template variable name
    * @param value value to render in template
    * @return context to place data into
    */
   public Context put(String key, String value);

   /**
    * add data to be rendered in the template through variable substitution
    *
    * @param key   template variable name
    * @param value value to render in template
    * @return context to place data into
    */
   public Context put(String key, Integer value);

   /**
    * get an iterator for the nested/loop data contexts
    *
    * @param name template variable prefix name
    * @return iterator for the children
    */
   Iterator childIterator(String name);
}