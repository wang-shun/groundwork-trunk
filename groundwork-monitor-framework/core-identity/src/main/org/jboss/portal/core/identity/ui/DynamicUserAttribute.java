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
package org.jboss.portal.core.identity.ui;

import java.util.HashMap;

import javax.faces.context.FacesContext;

import org.jboss.portal.core.identity.ui.common.IdentityUserBean;
import org.jboss.portal.faces.el.PropertyValue;
import org.jboss.portal.faces.el.dynamic.DynamicBean;

/**
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision$
 */
public class DynamicUserAttribute implements DynamicBean
{

   /** The username */
   private String username;

   /** The managed user bean */
   private IdentityUserBean identityUserBean;
   
   /** The hash map contains all the user attributes */
   private HashMap map;
   
   public DynamicUserAttribute()
   {
      this.map = new HashMap();
      FacesContext ctx = FacesContext.getCurrentInstance();
      this.identityUserBean = (IdentityUserBean) ctx.getApplication().createValueBinding(("#{identityusermgr}"))
      .getValue(ctx);
   }

   public DynamicUserAttribute(String username)
   {
      this();
      this.username = username;
   }

   public Class getType(Object propertyName) throws IllegalArgumentException
   {
      try
      {
         return identityUserBean.getPropertyType((String) propertyName);
      }
      catch (Exception e)
      {
         e.printStackTrace();
         throw new IllegalArgumentException("Property not found.");
      }
   }

   public PropertyValue getValue(Object propertyName)
   {
      Object propertyValue = map.get((String) propertyName);

      // Trying to fetch user property
      if (propertyValue == null)
      {
         try
         {
            propertyValue = identityUserBean.getUserProperty(this.username, (String) propertyName);
         }
         catch (Exception e)
         {
            // ok on user register
         }
      }
      
      return new PropertyValue(identityUserBean.getLocalizedValue((String)propertyName, propertyValue));
   }

   public boolean setValue(Object propertyName, Object value) throws IllegalArgumentException
   {
      if (value != null && value instanceof String && ((String)value).trim().length() == 0)
         map.put((String) propertyName, null);
      else
         map.put((String)propertyName, value);
      return true;
   }

   public HashMap getProfileAttributes()
   {
      return map;
   }
}
