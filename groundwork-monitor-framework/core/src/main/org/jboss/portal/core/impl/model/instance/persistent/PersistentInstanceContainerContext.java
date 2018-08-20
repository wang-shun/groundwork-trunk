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
package org.jboss.portal.core.impl.model.instance.persistent;

import EDU.oswego.cs.dl.util.concurrent.ConcurrentReaderHashMap;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.exception.ConstraintViolationException;
import org.jboss.portal.common.util.Tools;
import org.jboss.portal.core.impl.model.instance.AbstractInstance;
import org.jboss.portal.core.impl.model.instance.AbstractInstanceCustomization;
import org.jboss.portal.core.impl.model.instance.AbstractInstanceDefinition;
import org.jboss.portal.core.impl.model.instance.InstanceContainerImpl;
import org.jboss.portal.core.impl.model.instance.JBossInstanceContainerContext;
import org.jboss.portal.core.model.instance.DuplicateInstanceException;
import org.jboss.portal.core.model.instance.InstanceDefinition;
import org.jboss.portal.core.model.instance.InstancePermission;
import org.jboss.portal.core.model.instance.metadata.InstanceMetaData;
import org.jboss.portal.jems.hibernate.ObjectContextualizer;
import org.jboss.portal.portlet.PortletContext;
import org.jboss.portal.security.PortalSecurityException;
import org.jboss.portal.security.RoleSecurityBinding;
import org.jboss.portal.security.spi.auth.PortalAuthorizationManager;
import org.jboss.portal.security.spi.auth.PortalAuthorizationManagerFactory;

import javax.naming.InitialContext;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 10684 $
 */
public class PersistentInstanceContainerContext implements JBossInstanceContainerContext
{

   /** . */
   private static final Logger log = Logger.getLogger(PersistentInstanceContainerContext.class);

   /** . */
   private static final String BY_INSTANCE_ID_QUERY = "from " + Tools.getShortNameOf(PersistentInstanceDefinition.class) + " where instanceId=:instanceId";

   /** . */
   private static final String FROM_INSTANCE_DEFINITION_IMPL = "from " + Tools.getShortNameOf(PersistentInstanceDefinition.class);

   /** . */
   protected SessionFactory sessionFactory;

   /** . */
   protected String sessionFactoryJNDIName;

   /** . */
   protected ObjectContextualizer contextualizer;

   /** . */
   protected ConcurrentReaderHashMap cache;

   /** . */
   protected boolean cacheNaturalId;

   /** . */
   private InstanceContainerImpl container;

   public PersistentInstanceContainerContext()
   {
      this.contextualizer = new ObjectContextualizer(this);
      this.cache = new ConcurrentReaderHashMap();
   }

   public InstanceContainerImpl getContainer()
   {
      return container;
   }

   public void setContainer(InstanceContainerImpl container)
   {
      this.container = container;
   }

   public void flushNaturalIdCache()
   {
      cache.clear();
   }

   public int getNaturalIdCacheSize()
   {
      return cache.size();
   }

   public boolean getCacheNaturalId()
   {
      return cacheNaturalId;
   }

   public void setCacheNaturalId(boolean cacheNaturalId)
   {
      this.cacheNaturalId = cacheNaturalId;
   }

   public String getSessionFactoryJNDIName()
   {
      return sessionFactoryJNDIName;
   }

   public void setSessionFactoryJNDIName(String sessionFactoryJNDIName)
   {
      this.sessionFactoryJNDIName = sessionFactoryJNDIName;
   }

   public void start() throws Exception
   {
      sessionFactory = (SessionFactory)new InitialContext().lookup(sessionFactoryJNDIName);

      //
      contextualizer.attach(sessionFactory);
   }

   public void stop() throws Exception
   {
      sessionFactory = null;
   }

   public Collection<InstanceDefinition> getInstanceDefinitions()
   {
      Session session = sessionFactory.getCurrentSession();

      //
      return session.createQuery(FROM_INSTANCE_DEFINITION_IMPL).list();
   }

   public AbstractInstanceCustomization newInstanceCustomization(AbstractInstanceDefinition def, String id, PortletContext portletContext)
   {
      return new PersistentInstanceCustomization((PersistentInstanceDefinition)def, id, portletContext);
   }

   public AbstractInstanceDefinition newInstanceDefinition(String id, String portletRef)
   {
      return new PersistentInstanceDefinition(this, id, portletRef);
   }

   public AbstractInstanceDefinition newInstanceDefinition(InstanceMetaData instanceMetaData)
   {
      return new PersistentInstanceDefinition(this, instanceMetaData);
   }

   public AbstractInstanceDefinition getInstanceDefinition(String id)
   {
      // Get cached pk from natural id
      Long pk = cacheNaturalId ? (Long)cache.get(id) : null;

      //
      PersistentInstanceDefinition instance;

      //
      Session session = sessionFactory.getCurrentSession();

      //
      if (pk == null)
      {
         // No pk
         instance = lookupNoCache(session, id);
      }
      else
      {
         // Try lookup using the cached pk
         instance = (PersistentInstanceDefinition)session.get(PersistentInstanceDefinition.class, pk);

         // The pk may be invalid if the instance has been recreted under the same path with a different pk
         if (instance == null)
         {
            // In that case we try a no cache
            instance = lookupNoCache(session, id);
         }
      }

      //
      if (cacheNaturalId)
      {
         if (instance != null)
         {
            cache.put(id, instance.getKey());
         }
         else
         {
            cache.remove(id);
         }
      }

      //
      return instance;
   }

   public AbstractInstanceCustomization getCustomization(AbstractInstanceDefinition instanceDef, String customizationId)
   {
      PersistentInstanceDefinition _instanceDef = (PersistentInstanceDefinition)instanceDef;
      return (PersistentInstanceCustomization)_instanceDef.relatedCustomizations.get(customizationId);
   }

   private PersistentInstanceDefinition lookupNoCache(Session session, String id)
   {
      Query q = session.createQuery(BY_INSTANCE_ID_QUERY);
      q.setString("instanceId", id);
      return (PersistentInstanceDefinition)q.uniqueResult();
   }

   public void createInstanceDefinition(AbstractInstanceDefinition instanceDef) throws DuplicateInstanceException
   {
      String id = instanceDef.getId();

      //
      if (getInstanceDefinition(id) != null)
      {
         throw new DuplicateInstanceException("An instance with id " + id + " already exist");
      }

      //
      try
      {
         Session session = sessionFactory.getCurrentSession();
         session.persist(instanceDef);
      }
      catch (ConstraintViolationException e)
      {
         // May raise a constraint violation exception if it is has been inserted between the lookup
         // and the insert and the isolation level is not serializable
         throw new DuplicateInstanceException("An instance with id " + id + " already exist");
      }
   }

   public void createInstanceCustomizaton(AbstractInstanceCustomization customization)
   {
      createInstanceCustomizaton((PersistentInstanceCustomization)customization);
   }

   private void createInstanceCustomizaton(PersistentInstanceCustomization customization)
   {
      Session session = sessionFactory.getCurrentSession();

      // Persist in db
      session.persist(customization);

      // Get owner that will become the related definition
      PersistentInstanceDefinition relatedDefinition = customization.owner;

      // Create one to many assoication
      relatedDefinition.relatedCustomizations.put(customization.customizationId, customization);
      customization.relatedDefinition = relatedDefinition;

      // Update state
      session.update(customization.relatedDefinition);

      // Mark state as persistent
      customization.persistent = true;
   }

   private void updateInstance(AbstractInstance instanceDef)
   {
      Session session = sessionFactory.getCurrentSession();
      session.update(instanceDef);
   }

   public void updateInstance(AbstractInstance instance, PortletContext portletContext, boolean mutable)
   {
      PersistentInstanceDefinition _instance = (PersistentInstanceDefinition)instance;

      //
      _instance.setPortletRef(portletContext.getId());
      _instance.setState(portletContext.getState());
      _instance.setMutable(mutable);

      //
      updateInstance(_instance);
   }


   public void updateInstance(AbstractInstance instance, PortletContext portletContext)
   {
      instance.setPortletRef(portletContext.getId());
      instance.setState(portletContext.getState());

      //
      updateInstance(instance);
   }

   public void updateInstanceDefinition(AbstractInstanceDefinition def, Set securityBindings)
   {
      PersistentInstanceDefinition _def = (PersistentInstanceDefinition)def;

      //
      for (Iterator i = _def.getRelatedSecurityBindings().values().iterator(); i.hasNext();)
      {
         PersistentRoleSecurityBinding isc = (PersistentRoleSecurityBinding)i.next();

         // Break association
         i.remove();
         isc.setInstance(null);
      }

      for (Iterator i = securityBindings.iterator(); i.hasNext();)
      {
         RoleSecurityBinding sc = (RoleSecurityBinding)i.next();

         //
         PersistentRoleSecurityBinding isc = new PersistentRoleSecurityBinding(sc.getActions(), sc.getRoleName());

         // Create association
         isc.setInstance(_def);
         _def.getRelatedSecurityBindings().put(sc.getRoleName(), isc);
      }
   }

   public void destroyInstanceDefinition(AbstractInstanceDefinition instanceDef)
   {
      destroyInstanceDefinition((PersistentInstanceDefinition)instanceDef);
   }

   private void destroyInstanceDefinition(PersistentInstanceDefinition instanceDef)
   {
      Session session = sessionFactory.getCurrentSession();

      // Destroy the user instances
      Collection customizations = instanceDef.getRelatedCustomizations().values();
      for (Iterator i = customizations.iterator(); i.hasNext();)
      {
         PersistentInstanceCustomization userInstance = (PersistentInstanceCustomization)i.next();
         i.remove();
         userInstance.relatedDefinition = null;
         session.delete(userInstance);
      }

      // Delete instance
      session.delete(instanceDef);

      //
      session.flush();
   }


   public void destroyInstanceCustomization(AbstractInstanceCustomization customization)
   {
      destroyInstanceCustomization((PersistentInstanceCustomization)customization);
   }

   private void destroyInstanceCustomization(PersistentInstanceCustomization customization)
   {
      Session session = sessionFactory.getCurrentSession();

      // Delete relationship
      customization.relatedDefinition.relatedCustomizations.remove(customization.getId());
      customization.relatedDefinition = null;

      // Delete customization
      session.delete(customization);

      //
      session.flush();
   }

   public boolean checkPermission(InstancePermission perm)
   {
      if (container.getPerformSecurityChecks())
      {
         boolean result = false;
         try
         {
            PortalAuthorizationManagerFactory pamf = container.getPortalAuthorizationManagerFactory();
            PortalAuthorizationManager manager = pamf.getManager();
            result = manager.checkPermission(perm);
         }
         catch (PortalSecurityException e)
         {
            log.error("Cannot check instance permission", e);
         }
         return result;
      }
      else
      {
         return true;
      }
   }
}
