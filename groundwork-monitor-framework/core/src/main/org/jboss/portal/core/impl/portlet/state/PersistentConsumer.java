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

import org.jboss.portal.registration.Consumer;
import org.jboss.portal.registration.ConsumerCapabilities;
import org.jboss.portal.registration.ConsumerGroup;
import org.jboss.portal.registration.DuplicateRegistrationException;
import org.jboss.portal.registration.RegistrationException;
import org.jboss.portal.registration.RegistrationStatus;
import org.jboss.portal.registration.impl.ConsumerCapabilitiesImpl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 8786 $
 */
public class PersistentConsumer implements Consumer
{

   // Persistent fields

   private Long key;
   private String persistentId;
   private String persistentName;
   private RegistrationStatus persistentStatus;
   private String persistentAgent;

   // Relationships

   private Set relatedRegistrations;
   private PersistentConsumerGroup relatedGroup;

   // Runtime state

   private ConsumerCapabilities capabilities = new ConsumerCapabilitiesImpl();

   /**
    * Manager constructor.
    *
    * @param id
    * @param name
    */
   public PersistentConsumer(String id, String name)
   {
      this.key = null;
      this.persistentId = id;
      this.persistentName = name;
      this.persistentStatus = RegistrationStatus.PENDING;
      this.relatedRegistrations = new HashSet();
      this.relatedGroup = null;
      this.persistentAgent = null;
   }

   /** Hibernate constructor. */
   PersistentConsumer()
   {
      this.key = null;
      this.persistentId = null;
      this.persistentName = null;
      this.persistentStatus = null;
      this.relatedRegistrations = null;
      this.relatedGroup = null;
      this.persistentAgent = null;
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

   public String getPersistentId()
   {
      return persistentId;
   }

   public void setPersistentId(String persistentId)
   {
      this.persistentId = persistentId;
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

   public String getPersistentAgent()
   {
      return persistentAgent;
   }

   public void setPersistentAgent(String persistentAgent)
   {
      this.persistentAgent = persistentAgent;
   }

   public Set getRelatedRegistrations()
   {
      return relatedRegistrations;
   }

   public void setRelatedRegistrations(Set relatedRegistrations)
   {
      this.relatedRegistrations = relatedRegistrations;
   }

   public PersistentConsumerGroup getRelatedGroup()
   {
      return relatedGroup;
   }

   public void setRelatedGroup(PersistentConsumerGroup relatedGroup)
   {
      this.relatedGroup = relatedGroup;
   }

   //

   public String getName()
   {
      return persistentName;
   }

   public RegistrationStatus getStatus()
   {
      return persistentStatus;
   }

   public void setStatus(RegistrationStatus status)
   {
      this.persistentStatus = status;
   }

   public Collection getRegistrations() throws RegistrationException
   {
      return Collections.unmodifiableSet(relatedRegistrations);
   }

   public ConsumerGroup getGroup()
   {
      return relatedGroup;
   }

   public String getId()
   {
      return persistentId;
   }

   public ConsumerCapabilities getCapabilities()
   {
      return capabilities;
   }

   public void setCapabilities(ConsumerCapabilities capabilities)
   {
      this.capabilities = capabilities;
   }

   public void setGroup(ConsumerGroup group) throws RegistrationException, DuplicateRegistrationException
   {
      if (this.relatedGroup != null)
      {
         this.relatedGroup.removeConsumer(this);
      }

      //
      if (group != null)
      {
         group.addConsumer(this);
      }
   }

   public String getConsumerAgent()
   {
      return persistentAgent;
   }

   public void setConsumerAgent(String consumerAgent) throws IllegalArgumentException, IllegalStateException
   {
      this.persistentAgent = consumerAgent;
   }
}
