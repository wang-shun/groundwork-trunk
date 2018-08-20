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
package org.jboss.portal.core.identity.services.metadata;

import java.util.List;
import java.util.Map;

import org.jboss.portal.identity.IdentityException;
import org.jboss.portal.identity.info.PropertyInfo;

/**
 * 
 * Core-Identity configuration class
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision$
 */
public class UIComponentConfiguration
{
   /** Identity property reference */
   private String propertyRef;

   /** UI reference name */
   private String name;

   /** List of validators */
   private List<String> validators;

   /** The converter */
   private String converter;

   /** Required flag */
   private boolean required;

   /** ReadOnly flag */
   private boolean readOnly;

   /** Property values */
   private Map<String, String> values;
   
   /** Reference for predefined values */
   private String predefinedMapValues;

   /** Identity PropertyInfo */
   private PropertyInfo propertyInfo;

   
   public String getPropertyRef()
   {
      return propertyRef;
   }

   public void setPropertyRef(String identifier) throws IdentityException
   {
      this.propertyRef = identifier;
   }

   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   // returns the first validator
   public String getValidator()
   {
      return (String) validators.get(0);
   }

   public List<String> getValidators()
   {
      return validators;
   }

   public void setValidators(List<String> validators)
   {
      this.validators = validators;
   }

   public boolean isRequired()
   {
      // If profile-info is mandatory always return true
      return this.propertyInfo.getUsage().equals("mandatory") ? true : required;
   }

   public void setRequired(boolean required)
   {
      this.required = required;
   }

   public boolean isReadOnly()
   {
      // If profile-info is read-only always return true
      return this.propertyInfo.getAccessMode().equals("read-only") ? true : readOnly;
   }

   public void setReadOnly(boolean readOnly)
   {
      this.readOnly = readOnly;
   }

   public String getConverter()
   {
      return converter;
   }

   public void setConverter(String converter)
   {
      this.converter = converter;
   }

   public Map<String, String> getValues()
   {
      return values;
   }

   public void setValues(Map<String, String> values)
   {
      this.values = values;
   }

   public PropertyInfo getPropertyInfo()
   {
      return propertyInfo;
   }

   public void setPropertyInfo(PropertyInfo propertyInfo)
   {
      this.propertyInfo = propertyInfo;
   }

   public String getPredefinedMapValues()
   {
      return predefinedMapValues;
   }
   
   public void setPredefinedMapValues(String predefinedMapValues)
   {
      this.predefinedMapValues = predefinedMapValues;
   }
   
   public Class getPropertyClass() throws ClassNotFoundException
   {
      // Returns the Class of the Property
      return Thread.currentThread().getContextClassLoader().loadClass(this.propertyInfo.getType());
   }
  
   public String toString()
   {
      StringBuilder builder = new StringBuilder();
      builder.append(getClass().getSimpleName());
      builder.append('@').append(Integer.toHexString(System.identityHashCode(this)));
      builder.append("{ name = ").append(name).append(',');
      builder.append(" reference = ").append(propertyRef).append('}');
      return builder.toString();
   }
}
