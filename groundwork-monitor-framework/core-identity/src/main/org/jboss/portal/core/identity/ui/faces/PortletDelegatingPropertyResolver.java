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
package org.jboss.portal.core.identity.ui.faces;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.faces.context.FacesContext;
import javax.faces.el.EvaluationException;
import javax.faces.el.PropertyNotFoundException;
import javax.faces.el.PropertyResolver;
import javax.faces.model.SelectItem;
import javax.portlet.PortletPreferences;
import javax.portlet.ReadOnlyException;

import org.jboss.logging.Logger;
import org.jboss.portal.common.reflect.Modifier;
import org.jboss.portal.core.identity.services.IdentityConstants;
import org.jboss.portal.core.identity.services.metadata.UIComponentConfiguration;
import org.jboss.portal.core.identity.ui.faces.components.StaticValues;
import org.jboss.portal.faces.el.PropertyValue;
import org.jboss.portal.faces.el.decorator.BeanDecorator;
import org.jboss.portal.faces.el.dynamic.DynamicBean;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision$
 */
public class PortletDelegatingPropertyResolver extends PropertyResolver
{
   
   /** . */
   private PropertyResolver delegate;
   
   /** . */
   private volatile Map decoratorMap = new HashMap();
   
   /** .*/
   private static final Logger log = Logger.getLogger(PortletDelegatingPropertyResolver.class);
   
   public PortletDelegatingPropertyResolver(PropertyResolver delegate)
   {
      this.delegate = delegate;
   }

   public Class getType(Object base, int index) throws EvaluationException, PropertyNotFoundException
   {
      if ( base instanceof PortletPreferences)
      {
         throw new PropertyNotFoundException("PortletPreferences can not be accessed via an index");
      } else {
         return delegate.getType(base, index);
      }
   }

   public Class getType(Object base, Object property) throws EvaluationException, PropertyNotFoundException
   {
      if (base instanceof PortletPreferences)
      {
         return String.class;
      }
      else 
         // See if the object can handle itself the property
         if (base instanceof DynamicBean)
         {
            DynamicBean dynamicBean = (DynamicBean)base;
            Class type = dynamicBean.getType(property);
            if (type != null)
            {
               return type;
            }
         }

         //
         GetTypeBeanAction beanAction = new GetTypeBeanAction(base, property);
         if (resolveAction(base.getClass(), beanAction))
         {
            return beanAction.type;
         }

         //
         if (property instanceof String)
         {
            try
            {
               Field f = base.getClass().getField((String)property);
               if (Modifier.isReadableProperty(f))
               {
                  return f.getType();
               }
            }
            catch (NoSuchFieldException ignore)
            {
            }
         }
         
         //
         if (delegate != null)
         {
            return delegate.getType(base, property);
         }

         //
         throw createPNFE(base, property);
   }

   public Object getValue(Object base, int index) throws EvaluationException, PropertyNotFoundException
   {
      if (base instanceof PortletPreferences)
      {
         throw new PropertyNotFoundException("PortletPreferences can not be accessed via an index");
      }
      else
      {
         return delegate.getValue(base, index);
      }
   }

   public Object getValue(Object base, Object property) throws EvaluationException, PropertyNotFoundException
   {
      if ( base instanceof UIComponentConfiguration && ((String)property).equals("values"))
      {
         List list = new ArrayList();
         FacesContext ctx = FacesContext.getCurrentInstance();
         UIComponentConfiguration uiComponent = (UIComponentConfiguration) base;
         // if not required add a empty SelectItem
         if(! uiComponent.isRequired())
            list.add(new SelectItem(""));
        
         if (IdentityConstants.COMPONENT_VALUE_LOCALE.equals(uiComponent.getPredefinedMapValues()))
         {
            list.addAll(StaticValues.getLocale(ctx));
            return list;
         }
         else if (IdentityConstants.COMPONENT_VALUE_THEME.equals(uiComponent.getPredefinedMapValues()))
         {
            list.addAll(StaticValues.getTheme(ctx));
            return list;
         }
         else if (IdentityConstants.COMPONENT_VALUE_TIMEZONE.equals(uiComponent.getPredefinedMapValues()))
         {
            list.addAll(StaticValues.getTimezone());
            return list;
         }
         else {
            // building dynamic value list
            Iterator i = uiComponent.getValues().keySet().iterator();
            
            while(i.hasNext())
            {
               String key = (String) i.next();
               String value = (String) uiComponent.getValues().get(key); 
               ResourceBundle bundle = ResourceBundle.getBundle("conf.bundles.Identity", ctx.getViewRoot().getLocale());
               try 
               {
                  value = bundle.getString(IdentityConstants.DYNAMIC_VALUE_PREFIX + key.toUpperCase());
               }
               catch (Exception e)
               {
                  // just take the default
               }
               list.add(new SelectItem(key, value ));
            }
            return list;
         }
      }
      if ( base instanceof PortletPreferences)
      {
         PortletPreferences preferences = (PortletPreferences) base;
         return (preferences.getValue((String) property, null));
      }
      // See if the object can handle itself the property
      if (base instanceof DynamicBean)
      {
         DynamicBean dynamicBean = (DynamicBean)base;
         PropertyValue value = dynamicBean.getValue(property);
         if (value != null)
         {
            return value.getObject();
         }
      }

      //
      GetValueBeanAction beanAction = new GetValueBeanAction(base, property);
      if (resolveAction(base.getClass(), beanAction))
      {
         return beanAction.value.getObject();
      }

      
      //
      if (property instanceof String)
      {
         try
         {
            Field f = base.getClass().getField((String)property);
            if (Modifier.isReadableProperty(f))
            {
               return f.get(base);
            }
         }
         catch (NoSuchFieldException ignore)
         {
         }
         catch (IllegalAccessException e)
         {
            log.error("Was not able to read the field " + property + " of object " + base + " with class " + base.getClass().getName());
         }
      }
      
     //
      if (delegate != null)
      {
         return delegate.getValue(base, property);
      }

      //
      throw createPNFE(base, property);
   }

   public boolean isReadOnly(Object base, int index) throws EvaluationException, PropertyNotFoundException
   {
      if( base instanceof PortletPreferences)
      {
         throw new PropertyNotFoundException("PortletPreferences can not be accessed via an index");
      }
      else 
      {
         return delegate.isReadOnly(base, index);
      }
   }

   public boolean isReadOnly(Object base, Object property) throws EvaluationException, PropertyNotFoundException
   {
      if ( base instanceof PortletPreferences)
      {
         PortletPreferences preferences = (PortletPreferences) base;
         return preferences.isReadOnly((String) property);
      }
      else
      {
         return delegate.isReadOnly(base, property);
      }
   }

   public void setValue(Object base, int index, Object value) throws EvaluationException, PropertyNotFoundException
   {
      if (base instanceof PortletPreferences)
      {
         throw new PropertyNotFoundException("PortletPreferces can not be accessed via an index");
      }
      else
      {
         delegate.setValue(base, index, value);
      }

   }

   public void setValue(Object base, Object property, Object value) throws EvaluationException, PropertyNotFoundException
   {
      if (base instanceof PortletPreferences)
      {
         // TODO handle string array
         PortletPreferences preferences = (PortletPreferences) base;
         try
         {
            preferences.setValue((String) property, (String) value);
         }
         catch (ReadOnlyException e)
         {
            log.error("PortletPreference "+ (String) property +" read only", e);
         }
      }
      // See if the object can handle itself the property
      if (base instanceof DynamicBean)
      {
         DynamicBean dynamic = (DynamicBean)base;
         if (dynamic.setValue(property, value))
         {
            return;
         }
      }

      //
      SetValueBeanAction beanAction = new SetValueBeanAction(base, property, value);
      if (resolveAction(base.getClass(), beanAction))
      {
         return;
      }

      //
      if (property instanceof String)
      {
         try
         {
            Field f = base.getClass().getField((String)property);
            if (Modifier.isWritableProperty(f))
            {
               f.set(base, value);
               return;
            }
         }
         catch (NoSuchFieldException ignore)
         {
         }
         catch (IllegalAccessException e)
         {
            log.error("Was not able to write the field " + property + " of object " + base + " with class " + base.getClass().getName());
         }
      }

      //
      if (delegate != null)
      {
         delegate.setValue(base, property, value);
         return;
      }

      //
      throw createPNFE(base, property);

   }

   public final synchronized void registerDecorator(Class clazz, BeanDecorator decorator)
   {
      if (clazz == null)
      {
         throw new IllegalArgumentException();
      }
      if (decorator == null)
      {
         throw new IllegalArgumentException();
      }
      Map copy = new HashMap(decoratorMap);
      copy.put(clazz.getName(), decorator);
      decoratorMap = copy;
      log.debug("Added bean decorator " + clazz.getName() + " in resolver map");
   }

   private static interface BeanAction
   {
      boolean execute(BeanDecorator decorator);
   }

   private abstract static class AbstractBeanAction implements BeanAction
   {

      /** . */
      protected final Object base;

      /** . */
      protected final Object property;

      public AbstractBeanAction(Object base, Object property)
      {
         this.base = base;
         this.property = property;
      }
   }

   private static class GetTypeBeanAction extends AbstractBeanAction
   {

      /** . */
      private Class type;

      public GetTypeBeanAction(Object base, Object property)
      {
         super(base, property);
      }

      public boolean execute(BeanDecorator decorator)
      {
         type = decorator.getType(base, property);
         return type != null;
      }
   }

   private static class GetValueBeanAction extends AbstractBeanAction
   {

      /** . */
      private PropertyValue value;

      public GetValueBeanAction(Object base, Object property)
      {
         super(base, property);
      }

      public boolean execute(BeanDecorator decorator)
      {
         value = decorator.getValue(base, property);
         return value != null;
      }
   }

   private static class SetValueBeanAction extends AbstractBeanAction
   {

      /** . */
      private Object value;

      public SetValueBeanAction(Object base, Object property, Object value)
      {
         super(base, property);
         this.value = value;
      }

      public boolean execute(BeanDecorator decorator)
      {
         return decorator.setValue(base, property, value);
      }
   }

   private boolean resolveAction(Class clazz, BeanAction action)
   {
      BeanDecorator decorator = (BeanDecorator)decoratorMap.get(clazz.getName());
      if (decorator != null)
      {
         if (action.execute(decorator))
         {
            return true;
         }
      }
      Class[] itfs = clazz.getInterfaces();
      for (int i = 0; i < itfs.length; i++)
      {
         Class itf = clazz.getInterfaces()[i];
         if (resolveAction(itf, action))
         {
            return true;
         }
      }
      Class superClass = clazz.getSuperclass();
      if (superClass != null)
      {
         if (resolveAction(superClass, action))
         {
            return true;
         }
      }

      return false;
   }

   private PropertyNotFoundException createPNFE(Object base, int index)
   {
      return createPNFE(base, "[" + index + "]");
   }

   private PropertyNotFoundException createPNFE(Object base, Object propertyName)
   {
      return new PropertyNotFoundException("Property " + propertyName + " on object " + base + " was not found");
   }
}