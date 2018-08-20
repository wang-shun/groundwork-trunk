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
package org.jboss.portal.core.util;

import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 8786 $
 */
public class ProxyValidator
{

   private static final Set acceptedClasses = new HashSet();

   static
   {
      acceptedClasses.add(String.class);
      acceptedClasses.add(int.class);
      acceptedClasses.add(boolean.class);
      acceptedClasses.add(String[].class);
      acceptedClasses.add(int[].class);
      acceptedClasses.add(boolean[].class);
   }

   public static final int METHOD_NOT_ACCESSOR = 0;

   public static final int GETTER_INVALID_NAME = 1;
   public static final int GETTER_DUPLICATE_NAME = 2;
   public static final int GETTER_INVALID_RETURN_TYPE = 3;
   public static final int GETTER_NO_ARGUMENT = 4;
   public static final int GETTER_TOO_MANY_ARGUMENTS = 5;
   public static final int GETTER_RETURN_TYPE_DOES_NOT_MATCH_ARGUMENT_TYPE = 6;

   public static final int SETTER_DUPLICATE_NAME = 7;
   public static final int SETTER_INVALID_NAME = 8;
   public static final int SETTER_NO_ARGUMENT = 9;
   public static final int SETTER_TOO_MANY_ARGUMENTS = 10;
   public static final int SETTER_RETURN_TYPE_IS_NOT_VOID = 11;
   public static final int SETTER_INVALID_ARGUMENT_TYPE = 12;

   // 0 == method
   // 1 == method name
   private static final String[] DESCRIPTIONS =
      {
         "Method {0} is not an accessor",
         "Name {1} is not valid",
         "Name {1} is duplicated",
         "Method {0} has an invalid return type",
         "Method {0} has no argument",
         "Method {0} has too many arguments",
         "Method {0} does not have a return type matching the argument type",
         "Name {1} is duplicated",
         "Name {1} is not valid",
         "Method {0} has no argument",
         "Method {0} has too many arguments",
         "Method {0} has return type which is not void",
         "Method {0} has an invalid argument type",
      };

   public static class Error
   {
      private int code;
      private Method method;
      private String desc;

      public Error(int code, Method method)
      {
         this.code = code;
         this.method = method;
         desc = MessageFormat.format(DESCRIPTIONS[code], new Object[]{method, method.getName()});
      }

      public int getCode()
      {
         return code;
      }

      public Method getMethod()
      {
         return method;
      }

      public String getDescription()
      {
         return desc;
      }

      public String toString()
      {
         return desc;
      }
   }

   public static Error[] validate(Class itf)
   {
      List errors = new ArrayList();
      Method[] methods = itf.getMethods();
      Set getters = new HashSet();
      Set setters = new HashSet();
      for (int i = 0; i < methods.length; i++)
      {
         Method method = methods[i];
         String methodName = method.getName();
         if (methodName.startsWith("get"))
         {
            //
            if (methodName.substring(3).length() == 0)
            {
               errors.add(new Error(GETTER_INVALID_NAME, method));
            }
            if (getters.contains(methodName.substring(3)))
            {
               errors.add(new Error(GETTER_DUPLICATE_NAME, method));
            }
            if (!acceptedClasses.contains(method.getReturnType()))
            {
               errors.add(new Error(GETTER_INVALID_RETURN_TYPE, method));
            }
            if (method.getParameterTypes().length == 0)
            {
               errors.add(new Error(GETTER_NO_ARGUMENT, method));
            }
            else if (method.getParameterTypes().length > 1)
            {
               errors.add(new Error(GETTER_TOO_MANY_ARGUMENTS, method));
            }
            else if (!method.getReturnType().equals(method.getParameterTypes()[0]))
            {
               errors.add(new Error(GETTER_RETURN_TYPE_DOES_NOT_MATCH_ARGUMENT_TYPE, method));
            }
            getters.add(methodName.substring(3));
         }
         else if (methodName.startsWith("set"))
         {
            if (method.getParameterTypes().length == 0)
            {
               errors.add(new Error(SETTER_NO_ARGUMENT, method));
            }
            else if (method.getParameterTypes().length > 1)
            {
               errors.add(new Error(SETTER_TOO_MANY_ARGUMENTS, method));
            }
            else if (!acceptedClasses.contains(method.getParameterTypes()[0]))
            {
               errors.add(new Error(SETTER_INVALID_ARGUMENT_TYPE, method));
            }
            if (methodName.substring(3).length() == 0)
            {
               errors.add(new Error(SETTER_INVALID_NAME, method));
            }
            if (setters.contains(methodName.substring(3)))
            {
               errors.add(new Error(SETTER_DUPLICATE_NAME, method));
            }
            if (!method.getReturnType().equals(void.class))
            {
               errors.add(new Error(SETTER_RETURN_TYPE_IS_NOT_VOID, method));
            }
            setters.add(methodName.substring(3));
         }
         else
         {
            // Invalid
            errors.add(new Error(METHOD_NOT_ACCESSOR, method));
         }
      }

      return (Error[])errors.toArray(new Error[errors.size()]);
   }
}
