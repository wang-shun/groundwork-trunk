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
package org.jboss.portal.core.impl.model.portal;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.exception.ConstraintViolationException;
import org.jboss.logging.Logger;
import org.jboss.portal.core.model.portal.DuplicatePortalObjectException;
import org.jboss.portal.core.model.portal.PortalObjectId;
import org.jboss.portal.core.model.portal.PortalObjectPath;
import org.jboss.portal.jems.hibernate.ObjectContextualizer;
import org.jboss.portal.security.impl.JBossAuthorizationDomainRegistry;
import org.jboss.portal.security.spi.auth.PortalAuthorizationManagerFactory;

import javax.naming.InitialContext;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 12371 $
 */
public class PersistentPortalObjectContainer extends AbstractPortalObjectContainer
{

   /** The query for lookup when the path is null. */
   private static final String LOOKUP_QUERY_FOR_ROOT = "from ObjectNode where path=:path or path is null";

   /** The query for lookup when the path is not null. */
   private static final String LOOKUP_QUERY = "from ObjectNode where path=:path";

   /** . */
   private static Logger log = Logger.getLogger(PersistentPortalObjectContainer.class);

   /** . */
   protected SessionFactory sessionFactory;

   /** . */
   protected ContainerContext ctx;

   /** . */
   protected PortalAuthorizationManagerFactory portalAuthorizationManagerFactory;

   /** . */
   protected JBossAuthorizationDomainRegistry authorizationDomainRegistry;

   /** . */
   protected String sessionFactoryJNDIName;

   /** . */
   protected ObjectContextualizer contextualizer;

   /** . */
   protected ConcurrentHashMap cache;

   /** . */
   protected boolean cacheNaturalId;

   /** . */
   protected String rootName;

   public PersistentPortalObjectContainer()
   {
      ctx = new ContainerContext()
      {
         public void destroyChild(ObjectNode node)
         {
            Session session = sessionFactory.getCurrentSession();
            PortalObjectImpl object = node.getObject();
            session.delete(object);
            session.delete(node);
            session.flush();
         }

         public void createChild(ObjectNode node) throws DuplicatePortalObjectException
         {
            Session session = sessionFactory.getCurrentSession();
            try
            {
               session.save(node);
               session.save(node.getObject());
               session.flush();
            }
            catch (ConstraintViolationException e)
            {
               log.warn("The configured database is probably case-insensitive. " + e.getMessage());
               session.close();
               throw new DuplicatePortalObjectException();
            }
         }

         public void updated(ObjectNode node)
         {
            Session session = sessionFactory.getCurrentSession();
            session.flush();
         }
      };

      //
      contextualizer = new ObjectContextualizer(ctx);
      cache = new ConcurrentHashMap();
   }

   public String getRootName()
   {
      return rootName;
   }

   public void setRootName(String rootName)
   {
      this.rootName = rootName;
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

   public JBossAuthorizationDomainRegistry getAuthorizationDomainRegistry()
   {
      return authorizationDomainRegistry;
   }

   public void setAuthorizationDomainRegistry(JBossAuthorizationDomainRegistry authDomainRegistry)
   {
      this.authorizationDomainRegistry = authDomainRegistry;
   }

   public PortalAuthorizationManagerFactory getPortalAuthorizationManagerFactory()
   {
      return portalAuthorizationManagerFactory;
   }

   public void setPortalAuthorizationManagerFactory(PortalAuthorizationManagerFactory pamf)
   {
      this.portalAuthorizationManagerFactory = pamf;
   }

   public void setSessionFactoryJNDIName(String sessionFactoryJNDIName)
   {
      this.sessionFactoryJNDIName = sessionFactoryJNDIName;
   }

   public ContainerContext getContainerContext()
   {
      return this.ctx;
   }

   protected void startService() throws Exception
   {
      sessionFactory = (SessionFactory)new InitialContext().lookup(sessionFactoryJNDIName);

      //
      contextualizer.attach(sessionFactory);

      // Add ourself as the authorization domain
      if (authorizationDomainRegistry != null)
      {
         authorizationDomainRegistry.addDomain(this);
      }

      //
      super.startService();
   }

   protected void stopService() throws Exception
   {
      super.stopService();

      //
      if (authorizationDomainRegistry != null)
      {
         authorizationDomainRegistry.removeDomain(this);
      }

      //
      sessionFactory = null;
   }

   protected ContextImpl createRoot(String namespace) throws DuplicatePortalObjectException
   {
      log.debug("Detecting the existence of the portal object root context");
      Session session = sessionFactory.getCurrentSession();

      // Create root context if it does not exist
      ObjectNode root = getObjectNode(session, new PortalObjectId(namespace, PortalObjectPath.ROOT_PATH));

      //
      if (root == null)
      {
         // Bootstrap the root node
         log.debug("The root context of the object tree does not exist, about to create it");
         root = new ObjectNode(this.ctx, new PortalObjectId(namespace, PortalObjectPath.ROOT_PATH), namespace);
         session.save(root);

         //
         ContextImpl ctx = new ContextImpl();
         root.setObject(ctx);
         ctx.setObjectNode(root);
         session.save(ctx);

         //
         log.info("Created portal object root context for namespace " + namespace);

         //
         return ctx;
      }
      else
      {
         throw new DuplicatePortalObjectException("namespace " + namespace + " already exists");
      }
   }

   protected ObjectNode getObjectNode(PortalObjectId path)
   {
      return getObjectNode(sessionFactory.getCurrentSession(), path);
   }

   private ObjectNode getObjectNodeNoCache(Session session, PortalObjectId id)
   {
      Object result;

      //
      String queryString = LOOKUP_QUERY;

      // We need to lookup the root of the empty namespace with a special query in order to fix
      // oracle weird behavior with zero length strings considered as null value
      if (id.getPath().getLength() == 0 && id.getNamespace().length() == 0)
      {
         queryString = LOOKUP_QUERY_FOR_ROOT;
      }

      //
      Query query = session.createQuery(queryString);
      query.setParameter("path", id);

      // Unique result will return null if no object is found
      result = query.uniqueResult();

      return (ObjectNode)result;
   }

   private ObjectNode getObjectNode(Session session, PortalObjectId id)
   {
      // Get cached pk from natural id
      Long pk = cacheNaturalId ? (Long)cache.get(id) : null;

      //
      ObjectNode objectNode;

      //
      if (pk == null)
      {
         // No pk
         objectNode = getObjectNodeNoCache(session, id);
      }
      else
      {
         // Try lookup using the cached pk
         objectNode = (ObjectNode)session.get(ObjectNode.class, pk);

         // The pk may be invalid if the object has been recreted under the same path with a different pk
         if (objectNode == null)
         {
            // In that case we try a no cache
            objectNode = getObjectNodeNoCache(session, id);
         }
      }

      //
      if (cacheNaturalId)
      {
         if (objectNode != null)
         {
            cache.put(id, objectNode.getKey());
         }
         else
         {
            cache.remove(id);
         }
      }

      //
      return objectNode;
   }
}
