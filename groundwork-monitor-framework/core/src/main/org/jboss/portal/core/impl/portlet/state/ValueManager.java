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
package org.jboss.portal.core.impl.portlet.state;

import org.jboss.portal.common.value.BooleanValue;
import org.jboss.portal.common.value.FloatValue;
import org.jboss.portal.common.value.IntegerValue;
import org.jboss.portal.common.value.StringValue;
import org.jboss.portal.common.value.Value;

/**
 * A manager handling type conversion. For now we handle only basic types : java.lang.String, integer and boolean.
 *
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 8786 $
 */
public class ValueManager
{

   /** Represents a string type. */
   public static final int TYPE_STRING = 1;

   /** Represents a integer type. */
   public static final int TYPE_INTEGER = 2;

   /** Represents a boolean type. */
   public static final int TYPE_BOOLEAN = 3;

   /** Represents a boolean type. */
   public static final int TYPE_FLOAT = 4;

   public static Value toValue(TypedStringArray tsa)
   {
      switch (tsa.getType())
      {
         case TYPE_INTEGER:
            return new IntegerValue(tsa.getStrings());
         case TYPE_BOOLEAN:
            return new BooleanValue(tsa.getStrings());
         case TYPE_FLOAT:
            return new FloatValue(tsa.getStrings());
         default:
            return new StringValue(tsa.getStrings());
      }
   }

   public static TypedStringArray toTypedStringArray(Value value)
   {
      if (value.isInstanceOf(Integer.class))
      {
         return new TypedStringArray(TYPE_INTEGER, value.asStringArray());
      }
      else if (value.isInstanceOf(Boolean.class))
      {
         return new TypedStringArray(TYPE_BOOLEAN, value.asStringArray());
      }
      else if (value.isInstanceOf(Float.class))
      {
         return new TypedStringArray(TYPE_FLOAT, value.asStringArray());
      }
      else
      {
         return new TypedStringArray(TYPE_STRING, value.asStringArray());
      }
   }
}
