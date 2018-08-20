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

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.jboss.portal.jems.as.system.AbstractJBossService;
import org.jboss.portal.jems.hibernate.ObjectContextualizer;
import org.jboss.portal.portlet.state.InvalidStateIdException;
import org.jboss.portal.portlet.state.NoSuchStateException;
import org.jboss.portal.portlet.state.PropertyMap;
import org.jboss.portal.portlet.state.SimplePropertyMap;
import org.jboss.portal.portlet.state.producer.PortletStateContext;
import org.jboss.portal.portlet.state.producer.PortletStatePersistenceManager;
import org.jboss.portal.registration.Consumer;
import org.jboss.portal.registration.ConsumerGroup;
import org.jboss.portal.registration.DuplicateRegistrationException;
import org.jboss.portal.registration.NoSuchRegistrationException;
import org.jboss.portal.registration.Registration;
import org.jboss.portal.registration.RegistrationException;
import org.jboss.portal.registration.RegistrationLocal;
import org.jboss.portal.registration.RegistrationPersistenceManager;
import org.jboss.portal.registration.RegistrationStatus;

import javax.naming.InitialContext;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 11068 $
 */
public class PersistentPortletStatePersistenceManager extends AbstractJBossService implements PortletStatePersistenceManager, RegistrationPersistenceManager
{

   /** . */
   protected SessionFactory sessionFactory;

   /** . */
   protected String sessionFactoryJNDIName;

   /** . */
   protected ObjectContextualizer contextualizer;

   public String getSessionFactoryJNDIName()
   {
      return sessionFactoryJNDIName;
   }

   public void setSessionFactoryJNDIName(String sessionFactoryJNDIName)
   {
      this.sessionFactoryJNDIName = sessionFactoryJNDIName;
   }

   public PortletStateContext loadState(String id) throws InvalidStateIdException, NoSuchStateException
   {
      Session session = getCurrentSession();
      return loadState(session, id);
   }

   public String createState(String portletId, PropertyMap propertyMap)
   {
      if (portletId == null)
      {
         throw new IllegalArgumentException("id cannot be null");
      }
      if (propertyMap == null)
      {
         throw new IllegalArgumentException("No null value map accepted");
      }

      //
      Session session = getCurrentSession();

      // Create the persistent state
      PersistentPortletState context = new PersistentPortletState(portletId, propertyMap);
      session.persist(context);

      // Create relationship with registration if it exists
      PersistentRegistration registration = (PersistentRegistration)RegistrationLocal.getRegistration();
      if (registration != null)
      {
         registration.getRelatedPortletStates().add(context);
         context.setRelatedRegistration(registration);
      }

      //
      session.flush();

      //
      return context.getId();
   }

   public String cloneState(String stateId, PropertyMap propertyMap) throws InvalidStateIdException, NoSuchStateException
   {
      if (stateId == null)
      {
         throw new IllegalArgumentException("id cannot be null");
      }
      if (propertyMap == null)
      {
         throw new IllegalArgumentException("value map cannot be null");
      }

      //
      Session session = getCurrentSession();
      PersistentPortletState parentContext = loadState(session, stateId);

      // Create the persistent state
      PersistentPortletState context = new PersistentPortletState(parentContext.getPortletId(), propertyMap);
      session.persist(context);

      // Make the association
      context.setParent(parentContext);
      parentContext.getChildren().add(context);
      session.update(parentContext);

      // Create relationship with registration if it exists
      PersistentRegistration registration = (PersistentRegistration)RegistrationLocal.getRegistration();
      if (registration != null)
      {
         registration.getRelatedPortletStates().add(context);
         context.setRelatedRegistration(registration);
      }

      //
      session.flush();

      //
      return context.getId();
   }

   public String cloneState(String stateId) throws IllegalArgumentException, NoSuchStateException, InvalidStateIdException
   {
      if (stateId == null)
      {
         throw new IllegalArgumentException("id cannot be null");
      }

      //
      Session session = getCurrentSession();
      PersistentPortletState parentContext = loadState(session, stateId);

      // Create the persistent state
      PersistentPortletState context = new PersistentPortletState(parentContext.getPortletId(), new SimplePropertyMap(parentContext.getState().getProperties()));
      session.persist(context);

      // Make the association
      context.setParent(parentContext);
      parentContext.getChildren().add(context);
      session.update(parentContext);

      // Create relationship with registration if it exists
      PersistentRegistration registration = (PersistentRegistration)RegistrationLocal.getRegistration();
      if (registration != null)
      {
         registration.getRelatedPortletStates().add(context);
         context.setRelatedRegistration(registration);
      }

      //
      session.flush();

      //
      return context.getId();
   }

   public void updateState(String stateId, PropertyMap propertyMap) throws InvalidStateIdException, NoSuchStateException
   {
      Session session = getCurrentSession();
      PersistentPortletState context = loadState(session, stateId);

      //
      context.entries.clear();
      for (Iterator i = propertyMap.keySet().iterator(); i.hasNext();)
      {
         String key = (String)i.next();
         List<String> value = propertyMap.getProperty(key);
         PersistentPortletStateEntry entry = new PersistentPortletStateEntry(key, value);
         context.entries.put(key, entry);
      }

      //
      session.update(context);
   }

   public void destroyState(String stateId) throws InvalidStateIdException, NoSuchStateException
   {
      if (stateId == null)
      {
         throw new IllegalArgumentException("No null state id accepted");
      }

      //
      Session session = getCurrentSession();
      PersistentPortletState context = loadState(session, stateId);

      // Efficiently set the children parent to null
      String update = "update PersistentPortletState p set p.parent=NULL where p.parent=:parent";
      Query query = session.createQuery(update).setLong("parent", context.getKey().longValue());
      query.executeUpdate();

      // Destroy any relationship with registration
      PersistentRegistration registration = context.getRelatedRegistration();
      if (registration != null)
      {
         registration.getRelatedPortletStates().remove(context);
         context.setRelatedRegistration(null);
      }

      // Delete the state
      session.delete(context);
      session.flush();
   }

   protected void startService() throws Exception
   {
      sessionFactory = (SessionFactory)new InitialContext().lookup(sessionFactoryJNDIName);
      contextualizer = new ObjectContextualizer(this);
      contextualizer.attach(sessionFactory);
   }

   protected void stopService() throws Exception
   {
      contextualizer = null;
      sessionFactory = null;
   }

   protected Session getCurrentSession()
   {
      return sessionFactory.getCurrentSession();
   }

   private PersistentPortletState loadState(Session session, String stateId) throws NoSuchStateException, InvalidStateIdException
   {
      if (stateId == null)
      {
         throw new IllegalArgumentException("id cannot be null");
      }

      try
      {
         Long key = new Long(stateId);
         PersistentPortletState context = (PersistentPortletState)session.get(PersistentPortletState.class, key);

         //
         if (context == null)
         {
            throw new NoSuchStateException(stateId);
         }

         //
         return context;
      }
      catch (NumberFormatException e)
      {
         throw new InvalidStateIdException(e, stateId);
      }
   }

   // RegistrationPersistenceManager ***********************************************************************************


   public Consumer createConsumer(String consumerId, String consumerName) throws RegistrationException
   {
      if (consumerId == null)
      {
         throw new IllegalArgumentException("No null consumer id accepted");
      }
      if (consumerName == null)
      {
         throw new IllegalArgumentException("No null consumer name accepted");
      }

      // Get hibernate session
      Session session = getCurrentSession();

      //
      PersistentConsumer consumer = new PersistentConsumer(consumerId, consumerName);
      session.persist(consumer);

      //
      return consumer;
   }

   public ConsumerGroup getConsumerGroup(String name) throws RegistrationException
   {
      // Get hibernate session
      Session session = getCurrentSession();

      //
      return findGroupByName(session, name);
   }

   public ConsumerGroup createConsumerGroup(String name) throws RegistrationException
   {
      if (name == null)
      {
         throw new IllegalArgumentException("No null name accepted");
      }

      // Get hibernate session
      Session session = getCurrentSession();
      PersistentConsumerGroup group = null;

      // Detect duplicate
      if (findGroupByName(session, name) != null)
      {
         throw new DuplicateRegistrationException("Group " + name + " already exists");
      }

      // Create and persist
      group = new PersistentConsumerGroup(this, name);
      session.persist(group);

      //
      return group;
   }

   public void removeConsumerGroup(String name) throws RegistrationException
   {
      // Get hibernate session
      Session session = getCurrentSession();

      //
      PersistentConsumerGroup group = getGroupByName(session, name);

      //
      session.delete(group);
   }

   public void removeConsumer(String consumerId) throws RegistrationException
   {
      // Get hibernate session
      Session session = getCurrentSession();

      //
      PersistentConsumer consumer = getConsumerById(session, consumerId);

      //
      session.delete(consumer);
   }

   public void removeRegistration(String registrationId) throws RegistrationException
   {
      if (registrationId == null)
      {
         throw new IllegalArgumentException("No null registration id accepted");
      }

      // Get hibernate session
      Session session = getCurrentSession();

      //
      PersistentRegistration registration = getRegistrationById(session, registrationId);

      // Get related consumer
      PersistentConsumer consumer = registration.getRelatedConsumer();

      // Destroy relationship
      consumer.getRelatedRegistrations().remove(registration);
      registration.setRelatedConsumer(null);

      // Delete the registration
      session.delete(registration);
      session.flush();
   }

   public Consumer getConsumerById(String consumerId) throws RegistrationException
   {
      // Get hibernate session
      Session session = getCurrentSession();

      //
      return findConsumerById(session, consumerId);
   }

   public Registration addRegistrationFor(String consumerId, Map registrationProperties) throws RegistrationException
   {
      if (registrationProperties == null)
      {
         throw new IllegalArgumentException("No null registration properties accepted");
      }

      // Get hibernate session
      Session session = getCurrentSession();

      // Perform lookup
      PersistentConsumer consumer = getConsumerById(session, consumerId);

      // Create and persist registration and build relationship
      PersistentRegistration registration = new PersistentRegistration(registrationProperties, RegistrationStatus.PENDING);
      registration.setRelatedConsumer(consumer);
      consumer.getRelatedRegistrations().add(registration);
      session.persist(registration);
      session.saveOrUpdate(consumer);

      //
      return registration;
   }

   public Registration getRegistration(String registrationId)
   {
      // Get hibernate session
      Session session = getCurrentSession();

      //
      return findRegistrationById(session, registrationId);
   }

   public Consumer addConsumerToGroupNamed(String consumerId, String groupName) throws RegistrationException
   {
      Consumer consumer = getConsumerById(consumerId);

      // Build relationship
      ConsumerGroup group = getConsumerGroup(groupName);
      consumer.setGroup(group);

      //
      return consumer;
   }

   public Collection getConsumerGroups()
   {
      Session session = getCurrentSession();
      Query query = session.createQuery("from PersistentConsumerGroup");
      return query.list();
   }

   public Collection getConsumers()
   {
      Session session = getCurrentSession();
      Query query = session.createQuery("from PersistentConsumer");
      return query.list();
   }

   public Collection getRegistrations()
   {
      Session session = getCurrentSession();
      Query query = session.createQuery("from PersistentRegistration");
      return query.list();
   }

   private PersistentRegistration getRegistrationById(Session session, String registrationId) throws IllegalArgumentException, NoSuchRegistrationException
   {
      PersistentRegistration registration = findRegistrationById(session, registrationId);

      //
      if (registration == null)
      {
         throw new NoSuchRegistrationException("Cant find a consumer with the id " + registrationId);
      }

      //
      return registration;
   }

   private PersistentRegistration findRegistrationById(Session session, String registrationId) throws IllegalArgumentException
   {
      if (registrationId == null)
      {
         throw new IllegalArgumentException("No null consumer id accepted");
      }

      try
      {
         // Parse the key
         Long key = new Long(registrationId);

         // Perform lookup
         return (PersistentRegistration)session.get(PersistentRegistration.class, key);
      }
      catch (NumberFormatException e)
      {
         throw new IllegalArgumentException("Bad registration id format " + registrationId);
      }
   }

   private PersistentConsumer getConsumerById(Session session, String consumerId) throws IllegalArgumentException, NoSuchRegistrationException
   {
      PersistentConsumer consumer = findConsumerById(session, consumerId);

      //
      if (consumer == null)
      {
         throw new NoSuchRegistrationException("Cant find a consumer with the id " + consumerId);
      }

      //
      return consumer;
   }

   private PersistentConsumer findConsumerById(Session session, String consumerId) throws IllegalArgumentException
   {
      if (consumerId == null)
      {
         throw new IllegalArgumentException("No null consumer id accepted");
      }

      //
      Query query = session.createQuery("from PersistentConsumer where persistentId=:consumerId");
      query.setString("consumerId", consumerId);
      return (PersistentConsumer)query.uniqueResult();
   }

   private PersistentConsumerGroup getGroupByName(Session session, String groupName) throws IllegalArgumentException, NoSuchRegistrationException
   {
      PersistentConsumerGroup group = findGroupByName(session, groupName);

      //
      if (group == null)
      {
         throw new NoSuchRegistrationException("Cant find a group with the name " + groupName);
      }

      //
      return group;
   }

   private PersistentConsumerGroup findGroupByName(Session session, String groupName) throws IllegalArgumentException
   {
      if (groupName == null)
      {
         throw new IllegalArgumentException("No null group name accepted");
      }

      //
      Query query = session.createQuery("from PersistentConsumerGroup where persistentName=:groupName");
      query.setString("groupName", groupName);
      return (PersistentConsumerGroup)query.uniqueResult();
   }
}
