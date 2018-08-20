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
package org.jboss.portal.core.impl.model.instance;

import org.jboss.portal.core.model.instance.DuplicateInstanceException;
import org.jboss.portal.core.model.instance.Instance;
import org.jboss.portal.core.model.instance.InstanceContainer;
import org.jboss.portal.core.model.instance.InstanceDefinition;
import org.jboss.portal.core.model.instance.InstancePermission;
import org.jboss.portal.core.model.instance.NoSuchInstanceException;
import org.jboss.portal.core.model.instance.metadata.InstanceMetaData;
import org.jboss.portal.jems.as.system.AbstractJBossService;
import org.jboss.portal.portlet.PortletContext;
import org.jboss.portal.portlet.PortletInvoker;
import org.jboss.portal.portlet.PortletInvokerException;
import org.jboss.portal.portlet.PortletInvokerInterceptor;
import org.jboss.portal.portlet.impl.invocation.PortletInterceptorStack;
import org.jboss.portal.portlet.impl.invocation.PortletInterceptorStackFactory;
import org.jboss.portal.portlet.invocation.PortletInvocation;
import org.jboss.portal.portlet.invocation.response.PortletInvocationResponse;
import org.jboss.portal.portlet.state.DestroyCloneFailure;
import org.jboss.portal.security.PortalPermission;
import org.jboss.portal.security.PortalPermissionCollection;
import org.jboss.portal.security.PortalSecurityException;
import org.jboss.portal.security.RoleSecurityBinding;
import org.jboss.portal.security.SecurityConstants;
import org.jboss.portal.security.impl.JBossAuthorizationDomainRegistry;
import org.jboss.portal.security.spi.auth.PortalAuthorizationManager;
import org.jboss.portal.security.spi.auth.PortalAuthorizationManagerFactory;
import org.jboss.portal.security.spi.provider.AuthorizationDomain;
import org.jboss.portal.security.spi.provider.DomainConfigurator;
import org.jboss.portal.security.spi.provider.PermissionFactory;
import org.jboss.portal.security.spi.provider.PermissionRepository;
import org.jboss.portal.security.spi.provider.SecurityConfigurationException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Instance Container that is persistent
 *
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @author Anil.Saldhana@jboss.org
 * @version $Revision: 12308 $
 */
public class InstanceContainerImpl extends AbstractJBossService
        implements InstanceContainer, AuthorizationDomain, DomainConfigurator, PermissionRepository, PermissionFactory
{


   /** . */
   protected PortletInterceptorStackFactory stackFactory;

   /** . */
   protected PortletInvoker portletInvoker;

   /** . */
   protected PortalAuthorizationManagerFactory portalAuthorizationManagerFactory;

   /** . */
   protected JBossAuthorizationDomainRegistry authorizationDomainRegistry;

   /** Used to bypass security checks for testing. */
   protected boolean performSecurityChecks;

   /** If true clone the portlet on an instance creation. */
   protected boolean cloneOnCreate;

   /** The container context. */
   protected JBossInstanceContainerContext containerContext;

   public InstanceContainerImpl()
   {
      performSecurityChecks = true;
      cloneOnCreate = false;
   }

   public PortletInterceptorStackFactory getStackFactory()
   {
      return stackFactory;
   }

   public void setStackFactory(PortletInterceptorStackFactory stackFactory)
   {
      this.stackFactory = stackFactory;
   }

   public PortletInvoker getPortletInvoker()
   {
      return portletInvoker;
   }

   public void setPortletInvoker(PortletInvoker portletInvoker)
   {
      this.portletInvoker = portletInvoker;
   }

   public JBossAuthorizationDomainRegistry getAuthorizationDomainRegistry()
   {
      return authorizationDomainRegistry;
   }

   public void setAuthorizationDomainRegistry(JBossAuthorizationDomainRegistry authorizationDomainRegistry)
   {
      this.authorizationDomainRegistry = authorizationDomainRegistry;
   }

   public PortalAuthorizationManagerFactory getPortalAuthorizationManagerFactory()
   {
      return portalAuthorizationManagerFactory;
   }

   public void setPortalAuthorizationManagerFactory(PortalAuthorizationManagerFactory portalAuthorizationManagerFactory)
   {
      this.portalAuthorizationManagerFactory = portalAuthorizationManagerFactory;
   }

   protected void startService() throws Exception
   {
      // Add ourself as the authorization domain
      if (authorizationDomainRegistry != null)
      {
         authorizationDomainRegistry.addDomain(this);
      }

      //
      containerContext.setContainer(this);
   }

   protected void stopService() throws Exception
   {
      //
      containerContext.setContainer(null);

      //
      if (authorizationDomainRegistry != null)
      {
         authorizationDomainRegistry.removeDomain(this);
      }
   }

   public PortalPermission createPermissionContainer(PortalPermissionCollection collection) throws PortalSecurityException
   {
      return new InstancePermission(collection);
   }

   public PortalPermission createPermission(String uri, String action) throws PortalSecurityException
   {
      return new InstancePermission(uri, action);
   }

   public PortalPermission createPermission(String uri, Collection actions) throws PortalSecurityException
   {
      return new InstancePermission(uri, actions);
   }

   public AuthorizationDomain getAuthorizationDomain()
   {
      return this;
   }

   public boolean getCloneOnCreate()
   {
      return cloneOnCreate;
   }

   public void setCloneOnCreate(boolean cloneOnCreate)
   {
      this.cloneOnCreate = cloneOnCreate;
   }

   public boolean getPerformSecurityChecks()
   {
      return performSecurityChecks;
   }

   public void setPerformSecurityChecks(boolean performSecurityChecks)
   {
      this.performSecurityChecks = performSecurityChecks;
   }

   public JBossInstanceContainerContext getContainerContext()
   {
      return containerContext;
   }

   public void setContainerContext(JBossInstanceContainerContext containerContext)
   {
      this.containerContext = containerContext;
   }

   public InstanceDefinition getDefinition(String id)
   {
      if (id == null)
      {
         throw new IllegalArgumentException("id cannot be null");
      }
      return containerContext.getInstanceDefinition(id);
   }

   public InstanceDefinition createDefinition(String id, String portletId) throws DuplicateInstanceException, IllegalArgumentException, PortletInvokerException
   {
      return internalCreateDefinition(id, portletId, cloneOnCreate);
   }

   public InstanceDefinition createDefinition(InstanceMetaData instanceMD) throws DuplicateInstanceException, IllegalArgumentException, PortletInvokerException
   {
      return internalCreateDefinition(instanceMD, cloneOnCreate);
   }

   public InstanceDefinition createDefinition(String id, String portletId, boolean clone) throws DuplicateInstanceException, PortletInvokerException
   {
      return internalCreateDefinition(id, portletId, clone);
   }

   private InstanceDefinition internalCreateDefinition(String id, String portletId, boolean clone) throws DuplicateInstanceException, PortletInvokerException
   {
      InstanceMetaData instanceMD = new InstanceMetaData();
      instanceMD.setId(id);
      instanceMD.setPortletRef(portletId);
      return internalCreateDefinition(instanceMD, clone);
   }

   private InstanceDefinition internalCreateDefinition(InstanceMetaData instanceMD, boolean clone) throws DuplicateInstanceException, PortletInvokerException
   {
      if (instanceMD == null)
      {
         throw new IllegalArgumentException("instanceMD cannot be null");
      }
      boolean debug = log.isDebugEnabled();

      //
      if (debug)
      {
         log.debug("Creating instance " + instanceMD.getId() + " of portlet " + instanceMD.getPortletRef());
      }

      // Create the portlet context we'll use
      PortletContext portletContext = PortletContext.createPortletContext(instanceMD.getPortletRef());

      // Check that the portlet exist before creating an instance of it
      portletInvoker.getPortlet(portletContext);

      //
      AbstractInstanceDefinition instance = containerContext.newInstanceDefinition(instanceMD);

      //
      containerContext.createInstanceDefinition(instance);

      // Clone the portlet if required
      if (clone)
      {
         // Clone the portlet state now and update the instance
         if (debug)
         {
            log.debug("Cloning instance " + instance.getId() + "/" + portletContext);
         }
         portletContext = portletInvoker.createClone(portletContext);
         if (debug)
         {
            log.debug("Instance " + instance.getId() + " succesfully cloned " + portletContext);
         }

         //
         containerContext.updateInstance(instance, portletContext, true);
      }

      //
      return instance;
   }

   public void destroyDefinition(String id) throws PortletInvokerException, NoSuchInstanceException
   {
      if (id == null)
      {
         throw new IllegalArgumentException("id cannot be null");
      }

      // Lookup instance
      AbstractInstanceDefinition definition = containerContext.getInstanceDefinition(id);
      if (definition == null)
      {
         throw new NoSuchInstanceException(id);
      }

      // Get customizations
      Collection customizations = definition.getCustomizations();

      // Collect portlet info to destroy for logging purpose
      StringBuffer destroyLog = new StringBuffer("About to destroy portlets for instance=").
              append(definition.getInstanceId()).
              append(" [");

      //
      List toDestroy = new ArrayList(customizations.size());
      for (Iterator i = customizations.iterator(); i.hasNext();)
      {
         AbstractInstanceCustomization customization = (AbstractInstanceCustomization)i.next();

         // Get the user portlet context
         PortletContext customizationPortletContext = customization.getPortletContext();

         // Add the portlet context
         toDestroy.add(customizationPortletContext);

         //
         destroyLog.append(customizationPortletContext);
         if (i.hasNext())
         {
            destroyLog.append(',');
         }
      }

      //
      if (definition.isModifiable())
      {
         // Destroy the state only if it is not a producer offered portlet
         PortletContext sharedPortletContext = definition.getPortletContext();
         toDestroy.add(sharedPortletContext);
         destroyLog.append(sharedPortletContext);
      }
      destroyLog.append(']');
      log.debug(destroyLog);

      // Perform destruction
      List failures = portletInvoker.destroyClones(toDestroy);

      // Log failures if any
      if (failures.size() > 0)
      {
         StringBuffer failureLog = new StringBuffer("Some portlet were not properly destroyed for instance=").
                 append(definition.getInstanceId()).
                 append(" [");
         for (Iterator i = failures.iterator(); i.hasNext();)
         {
            DestroyCloneFailure failure = (DestroyCloneFailure)i.next();
            failureLog.append(failure.getPortletId());
            if (i.hasNext())
            {
               failureLog.append(',');
            }
         }
         failureLog.append(']');
         log.debug(failureLog);
      }

      //
      containerContext.destroyInstanceDefinition(definition);
   }

   public Collection<InstanceDefinition> getDefinitions()
   {
      Collection<InstanceDefinition> list = containerContext.getInstanceDefinitions();

      // Filter the list
      if (performSecurityChecks)
      {
         PortalAuthorizationManager mgr = portalAuthorizationManagerFactory.getManager();

         //
         for (Iterator i = list.iterator(); i.hasNext();)
         {
            Instance instance = (Instance)i.next();
            InstancePermission perm = new InstancePermission(instance.getId(), InstancePermission.VIEW_ACTION);
            if (!mgr.checkPermission(perm))
            {
               i.remove();
            }
         }
      }

      //
      return list;
   }

   PortletInvocationResponse invoke(PortletInvocation invocation) throws PortletInvokerException
   {
      PortletInterceptorStack stack = stackFactory.getInterceptorStack();
      if (stack.getLength() != 0)
      {
         try
         {
            return stack.getInterceptor(0).invoke(invocation);
         }
         catch (Exception e)
         {
            if (e instanceof PortletInvokerException)
            {
               throw (PortletInvokerException)e;
            }
            else if (e instanceof RuntimeException)
            {
               throw (RuntimeException)e;
            }
            else
            {
               throw new PortletInvokerException(e);
            }
         }
      }

      return portletInvoker.invoke(invocation);
   }

   //**********************************************************************
   //   AuthorizationDomain Interface
   //**********************************************************************

   public String getType()
   {
      return InstancePermission.PERMISSION_TYPE;
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

   public Set getSecurityBindings(String uri)
   {
      AbstractInstanceDefinition instance = containerContext.getInstanceDefinition(uri);

      //
      if (instance != null)
      {
         return instance.getSecurityBindings();
      }

      //
      return null;
   }

   public void setSecurityBindings(String uri, Set securityBindings) throws SecurityConfigurationException
   {
      AbstractInstanceDefinition instanceDef = containerContext.getInstanceDefinition(uri);

      //
      if (instanceDef == null)
      {
         throw new SecurityConfigurationException("The object should exist prior its security is configured : fixme");
      }

      //
      Set tmp = new HashSet(securityBindings.size());
      for (Iterator i = securityBindings.iterator(); i.hasNext();)
      {
         RoleSecurityBinding sc = (RoleSecurityBinding)i.next();

         // Optimize
         if (sc.getActions().size() > 0)
         {
            tmp.add(sc);
         }
      }

      //
      containerContext.updateInstanceDefinition(instanceDef, tmp);
   }

   public void removeSecurityBindings(String uri) throws SecurityConfigurationException
   {
      setSecurityBindings(uri, Collections.EMPTY_SET);
   }

   public PortalPermission getPermission(String roleName, String uri) throws PortalSecurityException
   {
      Set set = getSecurityBindings(uri);
      if (set != null && !set.isEmpty())
      {
         for (Iterator i = set.iterator(); i.hasNext();)
         {
            RoleSecurityBinding sc = (RoleSecurityBinding)i.next();
            String constraintRoleName = sc.getRoleName();
            if (constraintRoleName.equals(roleName) || SecurityConstants.UNCHECKED_ROLE_NAME.equals(constraintRoleName))
            {
               return createPermission(uri, sc.getActions());
            }
         }
      }
      return null;
   }
}
