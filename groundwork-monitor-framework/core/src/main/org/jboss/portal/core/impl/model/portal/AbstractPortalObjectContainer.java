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

import org.jboss.portal.common.util.ParameterValidation;
import org.jboss.portal.core.model.content.ContentType;
import org.jboss.portal.core.model.content.spi.ContentProvider;
import org.jboss.portal.core.model.content.spi.ContentProviderRegistry;
import org.jboss.portal.core.model.content.spi.handler.ContentHandler;
import org.jboss.portal.core.model.portal.Context;
import org.jboss.portal.core.model.portal.DuplicatePortalObjectException;
import org.jboss.portal.core.model.portal.PortalObject;
import org.jboss.portal.core.model.portal.PortalObjectContainer;
import org.jboss.portal.core.model.portal.PortalObjectId;
import org.jboss.portal.core.model.portal.PortalObjectPath;
import org.jboss.portal.core.model.portal.PortalObjectPermission;
import org.jboss.portal.jems.as.system.AbstractJBossService;
import org.jboss.portal.security.PortalPermission;
import org.jboss.portal.security.PortalPermissionCollection;
import org.jboss.portal.security.PortalSecurityException;
import org.jboss.portal.security.RoleSecurityBinding;
import org.jboss.portal.security.spi.provider.AuthorizationDomain;
import org.jboss.portal.security.spi.provider.DomainConfigurator;
import org.jboss.portal.security.spi.provider.PermissionFactory;
import org.jboss.portal.security.spi.provider.PermissionRepository;
import org.jboss.portal.security.spi.provider.SecurityConfigurationException;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 11795 $
 */
public abstract class AbstractPortalObjectContainer extends AbstractJBossService
   implements PortalObjectContainer, PermissionFactory, AuthorizationDomain, DomainConfigurator, PermissionRepository
{

   /** . */
   private ContentProviderRegistry contentProviderRegistry;

   protected AbstractPortalObjectContainer()
   {
   }

   public ContentProviderRegistry getContentProviderRegistry()
   {
      return contentProviderRegistry;
   }

   public void setContentProviderRegistry(ContentProviderRegistry contentProviderRegistry)
   {
      this.contentProviderRegistry = contentProviderRegistry;
   }

   // PortalObjectContainer implementation******************************************************************************

   public org.jboss.portal.core.model.portal.Context getContext()
   {
      return getContext("");
   }

   public PortalObject getObject(PortalObjectId id) throws IllegalArgumentException
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(id, "id");
      ObjectNode node = getObjectNode(id);
      return node == null ? null : node.getObject();
   }

   public <T extends PortalObject> T getObject(PortalObjectId id, Class<T> expectedType) throws IllegalArgumentException
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(expectedType, "expected type");
      PortalObject object = getObject(id);

      // only return the object if it matches the expected class
      if(expectedType.isInstance(object))
      {
         return expectedType.cast(object);
      }

      return null;
   }

   public Context getContext(String namespace)
   {
      if (namespace == null)
      {
         throw new IllegalArgumentException("No null namespace accepted");
      }
      PortalObjectId id = new PortalObjectId(namespace, PortalObjectPath.ROOT_PATH);
      ObjectNode node = getObjectNode(id);
      return node == null ? null : (Context)node.getObject();
   }

   public Context createContext(String namespace) throws DuplicatePortalObjectException
   {
      if (namespace == null)
      {
         throw new IllegalArgumentException("No null namespace accepted");
      }
      return createRoot(namespace);
   }

   public AuthorizationDomain getAuthorizationDomain()
   {
      return this;
   }

   protected abstract ContextImpl createRoot(String namespace) throws DuplicatePortalObjectException;

   // AuthorizationDomain implementation *******************************************************************************

   public String getType()
   {
      return PortalObjectPermission.PERMISSION_TYPE;
   }

   public DomainConfigurator getConfigurator()
   {
      return this;
   }

   public PermissionRepository getPermissionRepository()
   {
      return this;
   }

   public PermissionFactory getPermissionFactory()
   {
      return this;
   }

   // PermissionFactory implementation *********************************************************************************

   public PortalPermission createPermissionContainer(PortalPermissionCollection collection) throws PortalSecurityException
   {
      return new PortalObjectPermission(collection);
   }

   public PortalPermission createPermission(String uri, String action) throws PortalSecurityException
   {
      PortalObjectId id = PortalObjectId.parse(uri, PortalObjectPath.CANONICAL_FORMAT);
      return new PortalObjectPermission(id, action);
   }

   public PortalPermission createPermission(String uri, Collection actions) throws PortalSecurityException
   {
      PortalObjectId id = PortalObjectId.parse(uri, PortalObjectPath.CANONICAL_FORMAT);
      return new PortalObjectPermission(id, actions);
   }

   // PermissionRepository implementation ******************************************************************************

   public PortalPermission getPermission(String roleName, String uri) throws PortalSecurityException
   {
      PortalObjectId id = PortalObjectId.parse(uri, PortalObjectPath.CANONICAL_FORMAT);
      ObjectNode on = getObjectNode(id);
      if (on != null)
      {
         RoleSecurityBinding binding = on.getBinding(roleName);
         if (binding != null)
         {
            return createPermission(uri, binding.getActions());
         }
      }
      return null;
   }

   // DomainConfigurator implementation ********************************************************************************

   public void removeSecurityBindings(String uri) throws SecurityConfigurationException
   {
      PortalObjectId id = PortalObjectId.parse(uri, PortalObjectPath.CANONICAL_FORMAT);
      ObjectNode on = getObjectNode(id);
      if (on == null)
      {
         throw new SecurityConfigurationException("The object should exist prior its security is configured : fixme");
      }
      on.setBindings(new HashSet());
   }

   public void setSecurityBindings(String uri, Set securityBindings) throws SecurityConfigurationException
   {
      PortalObjectId id = PortalObjectId.parse(uri, PortalObjectPath.CANONICAL_FORMAT);
      ObjectNode on = getObjectNode(id);
      if (on == null)
      {
         throw new SecurityConfigurationException("The object should exist prior its security is configured : fixme");
      }
      on.setBindings(securityBindings);
   }

   public Set getSecurityBindings(String uri)
   {
      PortalObjectId id = PortalObjectId.parse(uri, PortalObjectPath.CANONICAL_FORMAT);
      ObjectNode on = getObjectNode(id);
      if (on != null)
      {
         return on.getBindings();
      }
      else
      {
         return null;
      }
   }

   // ******************************************************************************************************************

   /**
    * Must be subclasses to provide the access to a node.
    *
    * @param id the portal object path
    * @return a node or null if not found
    */
   protected abstract ObjectNode getObjectNode(PortalObjectId id);

   public class ContainerContext
   {
      /**
       */
      public PortalObjectContainer getContainer()
      {
         return AbstractPortalObjectContainer.this;
      }

      public ContentType getDefaultContentType()
      {
         return ContentType.PORTLET;
      }

      /**
       */
      public void destroyChild(ObjectNode node)
      {
      }

      /**
       * @throws DuplicatePortalObjectException 
       */
      public void createChild(ObjectNode node) throws DuplicatePortalObjectException
      {
      }

      /**
       */
      public void updated(ObjectNode node)
      {
      }

      /**
       */
      public ContentHandler getContentHandler(ContentType contentType)
      {
         ContentProvider contentProvider = contentProviderRegistry.getContentProvider(contentType);

         //
         if (contentProvider != null)
         {
            return contentProvider.getHandler();
         }
         else
         {
            return null;
         }
      }
   }
}
