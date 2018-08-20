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

import org.jboss.portal.common.util.TypedMap;
import org.jboss.portal.portlet.state.AbstractPropertyMap;
import org.jboss.portal.portlet.state.PropertyMap;
import org.jboss.portal.portlet.state.producer.PortletState;
import org.jboss.portal.portlet.state.producer.PortletStateContext;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 11068 $
 */
public class PersistentPortletState implements PortletStateContext
{

   /** The primary key. */
   protected Long key;

   /** The portlet id. */
   protected String portletId;

   /** The different entries. */
   protected Map entries;

   /** When the state has been created. */
   protected Date creationTime;

   /** When the state expires, a null value means there is no expiration date scheduled. */
   protected Date terminationTime;

   /** For now a registration id, later probably a one to many relationship with a registration entry. */
   protected PersistentRegistration relatedRegistration;

   /** The clones of this state. */
   protected Set children;

   /** The state that we cloned from. */
   protected PersistentPortletState parent;

   /** . */
   private PortletState ctx;

   public PersistentPortletState()
   {
      this.key = null;
      this.portletId = null;
      this.entries = null;
      this.creationTime = null;
      this.terminationTime = null;
      this.relatedRegistration = null;
      this.children = null;
      this.parent = null;
      this.ctx = null;
   }

   public PersistentPortletState(String portletId, PropertyMap propertyMap)
   {
      this.key = null;
      this.portletId = portletId;
      this.entries = new HashMap();
      this.creationTime = Calendar.getInstance().getTime();
      this.terminationTime = null;
      this.relatedRegistration = null;
      this.children = new HashSet();
      this.parent = null;
      this.ctx = null;

      //
      for (Iterator i = propertyMap.keySet().iterator(); i.hasNext();)
      {
         String key = (String)i.next();
         List<String> value = propertyMap.getProperty(key);
         entries.put(key, new PersistentPortletStateEntry(key, value));
      }
   }

   public Long getKey()
   {
      return key;
   }

   public void setKey(Long key)
   {
      this.key = key;
   }

   public String getId()
   {
      return key.toString();
   }

   public String getPortletId()
   {
      return portletId;
   }

   public void setPortletId(String portletId)
   {
      this.portletId = portletId;
   }

   public Map getEntries()
   {
      return entries;
   }

   public void setEntries(Map entries)
   {
      this.entries = entries;
   }

   public Date getCreationTime()
   {
      return creationTime;
   }

   public void setCreationTime(Date creationTime)
   {
      this.creationTime = creationTime;
   }

   public Date getTerminationTime()
   {
      return terminationTime;
   }

   public void setTerminationTime(Date terminationTime)
   {
      this.terminationTime = terminationTime;
   }

   public PersistentRegistration getRelatedRegistration()
   {
      return relatedRegistration;
   }

   public void setRelatedRegistration(PersistentRegistration relatedRegistration)
   {
      this.relatedRegistration = relatedRegistration;
   }

   public Set getChildren()
   {
      return children;
   }

   public void setChildren(Set children)
   {
      this.children = children;
   }

   public PersistentPortletState getParent()
   {
      return parent;
   }

   public void setParent(PersistentPortletState parent)
   {
      this.parent = parent;
   }

   //

   public PortletState getState()
   {
      if (ctx == null)
      {
         PropertyMap props = new AbstractPropertyMap(entries, KEY_CONVERTER, VALUE_CONVERTER);
         ctx = new PortletState(portletId, props);
      }
      return ctx;
   }

   private static final TypedMap.Converter KEY_CONVERTER = new TypedMap.Converter()
   {
      protected Object getInternal(Object o) throws IllegalArgumentException, ClassCastException
      {
         if (!(o instanceof String))
         {
            throw new ClassCastException("Was expecting an instanceof " + String.class.getName() + " but was " + o.getClass().getName());
         }
         return o;
      }
      protected Object getExternal(Object o)
      {
         if (!(o instanceof String))
         {
            throw new ClassCastException("Was expecting an instanceof " + String.class.getName() + " but was " + o.getClass().getName());
         }
         return o;
      }
      protected boolean equals(Object o, Object o1)
      {
         return o.equals(o1);
      }
   };

   private static TypedMap.Converter VALUE_CONVERTER = new TypedMap.Converter()
   {
      protected Object getInternal(Object o) throws IllegalArgumentException, ClassCastException
      {
         throw new IllegalArgumentException("Immutable");
      }
      protected Object getExternal(Object o)
      {
         return ((PersistentPortletStateEntry)o).getValue();
      }
      protected boolean equals(Object o, Object o1)
      {
         return o.equals(o1);
      }
   };

}
