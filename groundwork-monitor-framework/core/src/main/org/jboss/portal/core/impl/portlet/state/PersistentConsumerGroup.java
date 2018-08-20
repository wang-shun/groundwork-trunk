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

import org.hibernate.Session;
import org.jboss.portal.jems.hibernate.ContextObject;
import org.jboss.portal.registration.Consumer;
import org.jboss.portal.registration.ConsumerGroup;
import org.jboss.portal.registration.RegistrationException;
import org.jboss.portal.registration.RegistrationStatus;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 8786 $
 */
public class PersistentConsumerGroup implements ConsumerGroup, ContextObject
{

   // Persistent fields

   private Long key;
   private String persistentName;
   private RegistrationStatus persistentStatus;

   // Relationships

   private Map relatedConsumers;

   // Context

   private PersistentPortletStatePersistenceManager context;

   public PersistentConsumerGroup(PersistentPortletStatePersistenceManager context, String name)
   {
      this.key = null;
      this.persistentName = name;
      this.relatedConsumers = new HashMap();
      this.persistentStatus = RegistrationStatus.PENDING;
      this.context = context;
   }

   /** Hibernate constructor. */
   PersistentConsumerGroup()
   {
      this.key = null;
      this.persistentName = null;
      this.persistentStatus = null;
      this.relatedConsumers = null;
   }

   // ContextObject

   public void setContext(Object context)
   {
      this.context = (PersistentPortletStatePersistenceManager)context;
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

   public String getPersistentName()
   {
      return persistentName;
   }

   public void setPersistentName(String persistentName)
   {
      this.persistentName = persistentName;
   }

   public RegistrationStatus getPersistentStatus()
   {
      return persistentStatus;
   }

   public void setPersistentStatus(RegistrationStatus persistentStatus)
   {
      this.persistentStatus = persistentStatus;
   }

   public Map getRelatedConsumers()
   {
      return relatedConsumers;
   }

   public void setRelatedConsumers(Map relatedConsumers)
   {
      this.relatedConsumers = relatedConsumers;
   }

   //

   public String getName()
   {
      return persistentName;
   }

   public Collection getConsumers() throws RegistrationException
   {
      return Collections.unmodifiableCollection(relatedConsumers.values());
   }

   public Consumer getConsumer(String consumerId) throws IllegalArgumentException, RegistrationException
   {
      if (consumerId == null)
      {
         throw new IllegalArgumentException("No null consumer id accepted");
      }

      //
      return (Consumer)relatedConsumers.get(consumerId);
   }

   public void addConsumer(Consumer consumer) throws RegistrationException
   {
      PersistentConsumer pconsumer = validateImplementation(consumer);

      //
      if (relatedConsumers.containsKey(consumer.getId()))
      {
         throw new IllegalArgumentException("Consumer already attached " + consumer.getId());
      }

      //
      Session session = context.getCurrentSession();

      // Create the relationship
      relatedConsumers.put(consumer.getId(), pconsumer);
      pconsumer.setRelatedGroup(this);
      session.saveOrUpdate(this);
      session.saveOrUpdate(consumer);
   }

   public void removeConsumer(Consumer consumer) throws RegistrationException
   {
      validateImplementation(consumer);

      //
      if (relatedConsumers.remove(consumer.getId()) == null)
      {
         throw new IllegalArgumentException();
      }
   }

   public boolean contains(Consumer consumer)
   {
      validateImplementation(consumer);
      return relatedConsumers.containsKey(consumer.getId());
   }

   private PersistentConsumer validateImplementation(Consumer consumer) throws IllegalArgumentException
   {
      if (consumer == null)
      {
         throw new IllegalArgumentException();
      }
      if (consumer instanceof PersistentConsumer)
      {
         return (PersistentConsumer)consumer;
      }
      else
      {
         throw new IllegalArgumentException();
      }
   }

   public boolean isEmpty()
   {
      return relatedConsumers.isEmpty();
   }

   public RegistrationStatus getStatus()
   {
      return persistentStatus;
   }

   public void setStatus(RegistrationStatus status)
   {
      this.persistentStatus = status;
   }
}
