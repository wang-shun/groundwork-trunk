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
package org.jboss.portlet.util;

import java.util.Map;

/** @author <a href="theute@jboss.org">Thomas Heute </a> $Revision: 8786 $ */
public class Parameters
{

   private Map parameters;

   /** @param parameterMap  */
   public Parameters(Map parameterMap)
   {
      this.parameters = parameterMap;
   }

   public String getParameter(String name)
   {
      if (name == null)
      {
         return null;
      }
      String[] value = (String[])parameters.get(name);
      return value == null ? null : value[0];
   }

   public String get(String key, String def)
   {
      String value = getParameter(key);
      if (value != null)
      {
         return value;
      }
      else
      {
         return def;
      }
   }

   /**
    * Returns the value as a boolean
    *
    * @param key Key of the parameter
    * @param def Default value if value is different from true or false
    * @return boolean value for the string "true" or "false" (not sensitive to uppercase/lowercase, and leading/trailing
    *         spaces).
    */
   public boolean getBoolean(String key, boolean def)
   {

      String value = getParameter(key);
      if (value != null)
      {
         if ("true".equalsIgnoreCase(value.trim()))
         {
            return true;
         }
         else if ("false".equalsIgnoreCase(value.trim()))
         {
            return false;
         }
         else
         {
            return def;
         }
      }
      else
      {
         return def;
      }
   }

   public Boolean getBooleanObject(String key, boolean def)
   {
      Boolean bool = getBooleanObject(key);
      return (bool != null) ? bool : new Boolean(def);
   }

   public Boolean getBooleanObject(String key)
   {

      String value = getParameter(key);
      if (value != null)
      {
         if ("true".equalsIgnoreCase(value.trim()))
         {
            return Boolean.TRUE;
         }
         else if ("false".equalsIgnoreCase(value.trim()))
         {
            return Boolean.FALSE;
         }
         else
         {
            return null;
         }
      }
      else
      {
         return null;
      }
   }

   public byte[] getByteArray(String key, byte[] def)
   {
      String value = getParameter(key);
      byte[] returnValue = def;
      if (value != null)
      {
         returnValue = value.getBytes();
         if (returnValue == null)
         {
            returnValue = def;
         }
         return returnValue;
      }
      else
      {
         return def;
      }
   }

   public double getDouble(String key, double def)
   {
      String value = getParameter(key);
      double returnValue = def;
      if (value != null)
      {
         try
         {
            returnValue = Double.parseDouble(value);
            return returnValue;
         }
         catch (NumberFormatException e)
         {
            return def;
         }
      }
      else
      {
         return def;
      }
   }

   public Double getDoubleObject(String key, double defaultValue)
   {
      Double value = getDoubleObject(key);
      return value != null ? value : new Double(defaultValue);
   }

   public Double getDoubleObject(String key)
   {
      try
      {
         String value = getParameter(key);
         return (value != null) ? new Double(value) : null;
      }
      catch (NumberFormatException e)
      {
         return null;
      }
   }

   public float getFloat(String key, float def)
   {
      String value = getParameter(key);
      float returnValue = def;
      if (value != null)
      {
         try
         {
            returnValue = Float.parseFloat(value);
            return returnValue;
         }
         catch (NumberFormatException e)
         {
            return def;
         }
      }
      else
      {
         return def;
      }
   }

   public Float getFloatObject(String key, float defaultValue)
   {
      Float value = getFloatObject(key);
      return value != null ? value : new Float(defaultValue);
   }

   public Float getFloatObject(String key)
   {
      try
      {
         String value = getParameter(key);
         return (value != null) ? new Float(value) : null;
      }
      catch (NumberFormatException e)
      {
         return null;
      }
   }

   public short getShort(String key, short def)
   {
      String value = getParameter(key);
      short returnValue = def;
      if (value != null)
      {
         try
         {
            returnValue = Short.parseShort(value);
            return returnValue;
         }
         catch (NumberFormatException e)
         {
            return def;
         }
      }
      else
      {
         return def;
      }
   }

   public Short getShortObject(String key, short defaultValue)
   {
      Short value = getShortObject(key);
      return value != null ? value : new Short(defaultValue);
   }

   public Short getShortObject(String key)
   {
      try
      {
         String value = getParameter(key);
         return (value != null) ? new Short(value) : null;
      }
      catch (NumberFormatException e)
      {
         return null;
      }
   }

   public int getInt(String key, int def)
   {
      String value = getParameter(key);
      int returnValue = def;
      if (value != null)
      {
         try
         {
            returnValue = Integer.parseInt(value);
            return returnValue;
         }
         catch (NumberFormatException e)
         {
            return def;
         }
      }
      else
      {
         return def;
      }
   }

   public Integer getIntObject(String key, int defaultValue)
   {
      Integer value = getIntObject(key);
      return value != null ? value : new Integer(defaultValue);
   }

   public Integer getIntObject(String key)
   {
      try
      {
         String value = getParameter(key);
         return (value != null) ? new Integer(value) : null;
      }
      catch (NumberFormatException e)
      {
         return null;
      }
   }

   public long getLong(String key, long def)
   {
      String value = getParameter(key);
      long returnValue = def;
      if (value != null)
      {
         try
         {
            returnValue = Long.parseLong(value);
            return returnValue;
         }
         catch (NumberFormatException e)
         {
            return def;
         }
      }
      else
      {
         return def;

      }
   }

   public Long getLongObject(String key, long defaultValue)
   {
      Long value = getLongObject(key);
      return value != null ? value : new Long(defaultValue);
   }

   public Long getLongObject(String key)
   {
      try
      {
         String value = getParameter(key);
         return (value != null) ? new Long(value) : null;
      }
      catch (NumberFormatException e)
      {
         return null;
      }
   }

   public boolean getParameterExists(String param)
   {
      String result = getParameter(param);
      if ((result != null) && (result.length() != 0))
      {
         return true;
      }
      else
      {
         return false;
      }
   }
}