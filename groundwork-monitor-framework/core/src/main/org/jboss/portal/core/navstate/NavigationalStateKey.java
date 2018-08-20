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
package org.jboss.portal.core.navstate;

import java.io.Externalizable;
import java.io.Serializable;

/**
 * A key for navigational state.
 *
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 8786 $
 */
public final class NavigationalStateKey implements Serializable
{

   /** The type. */
   private final Class type;

   /** The id. */
   private final Object id;

   /**
    * Construct a new navigational state key.
    *
    * @param type the type of state
    * @param id   the id
    * @throws IllegalArgumentException if any argument is null or the id argument does not implement either Serializable
    *                                  or Externalizable
    */
   public NavigationalStateKey(Class type, Object id)
   {
      if (type == null)
      {
         throw new IllegalArgumentException("No type provided");
      }
      if (id == null)
      {
         throw new IllegalArgumentException("No id provided");
      }
      if (id instanceof Serializable == false && id instanceof Externalizable == false)
      {
         throw new IllegalArgumentException("Id should implement Serializable or Externalizable");
      }
      this.type = type;
      this.id = id;
   }

   public Class getType()
   {
      return type;
   }

   public Object getId()
   {
      return id;
   }

   public boolean equals(Object obj)
   {
      if (obj == this)
      {
         return true;
      }
      if (obj instanceof NavigationalStateKey)
      {
         NavigationalStateKey that = (NavigationalStateKey)obj;
         return type.equals(that.type) && id.equals(that.id);
      }
      return false;
   }

   public int hashCode()
   {
      return type.hashCode() + id.hashCode();
   }
}
