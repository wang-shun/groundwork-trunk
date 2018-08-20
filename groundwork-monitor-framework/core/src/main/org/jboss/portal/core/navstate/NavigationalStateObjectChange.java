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

/**
 * The change of the navigational state of a single object. If both old and new values are not null, it denotes an
 * update. Whenever the old value is null, a creation occured and conversely if the new value is null, a destruction
 * occured.
 *
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 8786 $
 */
public class NavigationalStateObjectChange extends NavigationalStateChange
{

   /** . */
   public static final int UPDATE = 0;

   /** . */
   public static final int CREATE = 1;

   /** . */
   public static final int DESTROY = 2;

   /** . */
   private final Object oldValue;

   /** . */
   private final Object newValue;

   /** . */
   private NavigationalStateKey key;

   private NavigationalStateObjectChange(NavigationalStateKey key, Object oldValue, Object newValue)
   {
      if (key == null)
      {
         throw new IllegalArgumentException("No key provided");
      }
      this.key = key;
      this.oldValue = oldValue;
      this.newValue = newValue;
   }

   public int getType()
   {
      return oldValue == null ? CREATE : (newValue == null ? DESTROY : UPDATE);
   }

   public NavigationalStateKey getKey()
   {
      return key;
   }

   public Object getOldValue()
   {
      return oldValue;
   }

   public Object getNewValue()
   {
      return newValue;
   }

   public static NavigationalStateObjectChange newUpdate(NavigationalStateKey key, Object oldValue, Object newValue)
   {
      if (oldValue == null)
      {
         throw new IllegalArgumentException("No old value provided");
      }
      if (newValue == null)
      {
         throw new IllegalArgumentException("No new value provided");
      }
      return new NavigationalStateObjectChange(key, oldValue, newValue);
   }

   public static NavigationalStateObjectChange newCreate(NavigationalStateKey key, Object newValue)
   {
      if (newValue == null)
      {
         throw new IllegalArgumentException("No new value provided");
      }
      return new NavigationalStateObjectChange(key, null, newValue);
   }

   public static NavigationalStateObjectChange newDestroy(NavigationalStateKey key, Object oldValue)
   {
      if (oldValue == null)
      {
         throw new IllegalArgumentException("No old value provided");
      }
      return new NavigationalStateObjectChange(key, oldValue, null);
   }
}
