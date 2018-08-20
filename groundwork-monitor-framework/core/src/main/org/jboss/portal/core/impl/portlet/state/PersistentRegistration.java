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

import org.jboss.portal.common.NotYetImplemented;
import org.jboss.portal.common.util.LazyMap;
import org.jboss.portal.common.util.MapAccessor;
import org.jboss.portal.common.util.TypedMap;
import org.jboss.portal.registration.Consumer;
import org.jboss.portal.registration.Registration;
import org.jboss.portal.registration.RegistrationStatus;

import javax.xml.namespace.QName;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 10228 $
 */
public class PersistentRegistration implements Registration
{

   // Persistent fields

   private Long key;
   private String persistentHandle;
   private RegistrationStatus persistentStatus;
   private Map<String, Object> persistentProperties;

   // Relationships

   private PersistentConsumer relatedConsumer;
   private Set relatedPortletStates;

   // Wrapper

   private Properties properties;

   /** Hibernate constructor. */
   PersistentRegistration()
   {
      this.key = null;
      this.persistentHandle = null;
      this.relatedConsumer = null;
      this.persistentStatus = null;
      this.persistentProperties = new HashMap<String, Object>();
      this.properties = new Properties();
      this.relatedPortletStates = null;
   }

   public PersistentRegistration(Map<String, Object> properties, RegistrationStatus status)
   {
      this.key = null;
      this.persistentHandle = null;
      this.relatedConsumer = null;
      this.persistentStatus = status;
      this.persistentProperties = properties;
      this.properties = new Properties();
      this.relatedPortletStates = new HashSet();

      // Perform properties validation
      this.properties.validate();
   }

   // Hibernate

   public Long getKey()
   {
      return key;
   }

   public void setKey(Long key)
   {
      this.key = key;
   }

   public Set getRelatedPortletStates()
   {
      return relatedPortletStates;
   }

   public void setRelatedPortletStates(Set relatedPortletStates)
   {
      this.relatedPortletStates = relatedPortletStates;
   }

   public String getPersistentHandle()
   {
      return persistentHandle;
   }

   public void setPersistentHandle(String persistentHandle)
   {
      this.persistentHandle = persistentHandle;
   }

   public PersistentConsumer getRelatedConsumer()
   {
      return relatedConsumer;
   }

   public void setRelatedConsumer(PersistentConsumer relatedConsumer)
   {
      this.relatedConsumer = relatedConsumer;
   }

   public RegistrationStatus getPersistentStatus()
   {
      return persistentStatus;
   }

   public void setPersistentStatus(RegistrationStatus persistentStatus)
   {
      this.persistentStatus = persistentStatus;
   }

   public Map<String, Object> getPersistentProperties()
   {
      return persistentProperties;
   }

   public void setPersistentProperties(Map<String, Object> persistentProperties)
   {
      this.persistentProperties = persistentProperties;
   }

   //

   public String getId()
   {
      if (key == null)
      {
         // If that ever happens it is a bug
         throw new IllegalStateException("Transient registration object");
      }
      return key.toString();
   }

   public void setRegistrationHandle(String handle)
   {
      this.persistentHandle = handle;
   }

   public String getRegistrationHandle()
   {
      return persistentHandle;
   }

   public Consumer getConsumer()
   {
      return relatedConsumer;
   }

   public Map<String, Object> getProperties()
   {
      return Collections.unmodifiableMap(properties);
   }

   public void setPropertyValueFor(QName propertyName, Object value)
   {
      properties.setProperty(propertyName, value);
   }

   public void setPropertyValueFor(String propertyName, Object value)
   {
      properties.setProperty(propertyName, value);
   }

   public void removeProperty(QName propertyName)
   {
      properties.removeProperty(propertyName);
   }

   public void removeProperty(String propertyName)
   {
      properties.removeProperty(propertyName);
   }

   public Object getPropertyValueFor(QName propertyName)
   {
      return properties.getProperty(propertyName);
   }

   public Object getPropertyValueFor(String propertyName)
   {
      return properties.getProperty(propertyName);
   }

   public void updateProperties(Map registrationProperties)
   {
      properties.replace(registrationProperties);
   }

   public boolean hasEqualProperties(Registration registration)
   {
      if (registration != null)
      {
         PersistentRegistration preg = (PersistentRegistration)registration;

         //
         return hasEqualProperties(preg.persistentProperties);
      }

      //
      return false;
   }

   public boolean hasEqualProperties(Map properties)
   {
      return this.properties.equals(properties);
   }

   public RegistrationStatus getStatus()
   {
      return persistentStatus;
   }

   public void setStatus(RegistrationStatus status)
   {
      this.persistentStatus = status;
   }

   public void clearAssociatedState()
   {
      throw new NotYetImplemented();
   }

   // ***********

   private static final TypedMap.Converter KEY_CONVERTER = new TypedMap.Converter()
   {
      protected Object getInternal(Object o) throws IllegalArgumentException, ClassCastException
      {
         if (o instanceof QName == false)
         {
            throw new ClassCastException();
         }
         return o;
      }
      protected Object getExternal(Object o)
      {
         return o;
      }
      protected boolean equals(Object o, Object o1)
      {
         return o.equals(o1);
      }
   };

   private static final TypedMap.Converter VALUE_CONVERTER = new TypedMap.Converter()
   {
      protected Object getInternal(Object o) throws IllegalArgumentException, ClassCastException
      {
         if (o instanceof String == false)
         {
            throw new ClassCastException();
         }
         return o;
      }
      protected Object getExternal(Object o)
      {
         return o;
      }
      protected boolean equals(Object o, Object o1)
      {
         return o.equals(o1);
      }
   };
   
   private  LazyMap<String, Object> lazyMap = new LazyMap<String, Object>(new MapAccessor<String, Object>()
   {
      public Map<String, Object> getMap(boolean writable)
      {
         return persistentProperties;
      }
      
   });

   /** Implement registration properties semantics, mostly validation and equality. */
   public class Properties extends TypedMap
   {

      public Properties()
      {
         super(lazyMap, KEY_CONVERTER, VALUE_CONVERTER);
      }

      public void setProperty(String propertyName, Object value)
      {
         if (propertyName == null)
         {
            throw new IllegalArgumentException("No null property name accepted");
         }

         //
         setProperty(new QName(propertyName), value);
      }

      public void setProperty(QName propertyName, Object value)
      {
         if (propertyName == null)
         {
            throw new IllegalArgumentException("No null property name accepted");
         }

         //
         put(propertyName, value);
      }

      public void removeProperty(QName propertyName)
      {
         if (propertyName == null)
         {
            throw new IllegalArgumentException("No null property name accepted");
         }

         //
         remove(propertyName);
      }

      public void removeProperty(String propertyName)
      {
         if (propertyName == null)
         {
            throw new IllegalArgumentException("No null property name accepted");
         }

         //
         removeProperty(new QName(propertyName));
      }

      public Object getProperty(QName propertyName)
      {
         if (propertyName == null)
         {
            throw new IllegalArgumentException("No null property name accepted");
         }

         //
         return get(propertyName);
      }

      public Object getProperty(String propertyName)
      {
         if (propertyName == null)
         {
            throw new IllegalArgumentException("No null property name accepted");
         }

         //
         return getProperty(new QName(propertyName));
      }
   }
}
