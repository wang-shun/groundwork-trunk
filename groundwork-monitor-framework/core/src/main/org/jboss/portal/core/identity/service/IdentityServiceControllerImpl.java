/*
* JBoss, a division of Red Hat
* Copyright 2006, Red Hat Middleware, LLC, and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* This is free software; you can redistribute it and/or modify it
* under the terms of the GNU Lesser General Public License as
* published by the Free Software Foundation; either version 2.1 of
* the License, or (at your option) any later version.
*
* This software is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
* Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with this software; if not, write to the Free
* Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
* 02110-1301 USA, or see the FSF site: http://www.fsf.org.
*/
package org.jboss.portal.core.identity.service;

import org.jboss.beans.metadata.plugins.AbstractBeanMetaData;
import org.jboss.kernel.Kernel;
import org.jboss.kernel.plugins.bootstrap.basic.BasicBootstrap;
import org.jboss.kernel.spi.dependency.KernelControllerContext;
import org.jboss.portal.identity.IdentityContext;
import org.jboss.portal.identity.IdentityContextImpl;
import org.jboss.portal.identity.IdentityException;
import org.jboss.portal.identity.IdentityServiceController;
import org.jboss.portal.identity.ServiceJNDIBinder;
import org.jboss.portal.identity.UserModule;
import org.jboss.portal.identity.UserProfileModule;
import org.jboss.portal.identity.DelegatingUserProfileModuleImpl;
import org.jboss.portal.identity.RoleModule;
import org.jboss.portal.identity.ldap.LDAPUserModule;
import org.jboss.portal.identity.ldap.LDAPUserProfileModule;
import org.jboss.portal.identity.ldap.LDAPRoleModule;
import org.jboss.portal.identity.boot.IdentityServiceLoader;
import org.jboss.portal.identity.event.IdentityEvent;
import org.jboss.portal.identity.event.IdentityEventBroadcaster;
import org.jboss.portal.identity.metadata.service.IdentityServicesMetaData;
import org.jboss.portal.identity.metadata.service.ModuleServiceMetaData;
import org.jboss.portal.identity.metadata.config.ModuleMetaData;
import org.jboss.portal.identity.service.IdentityConfigurationService;
import org.jboss.portal.identity.service.UserProfileModuleService;
import org.jboss.portal.jems.as.JNDI;
import org.jboss.portal.jems.as.system.AbstractJBossService;
import org.jboss.portal.jems.as.system.JBossServiceModelMBean;
import org.jboss.portal.core.identity.cache.CachedLDAPUserModuleWrapper;
import org.jboss.portal.core.identity.cache.IdentityCacheService;
import org.jboss.portal.core.identity.cache.CachedUserProfileModuleWrapper;
import org.jboss.portal.core.identity.cache.CachedLDAPRoleModuleWrapper;

import javax.management.ObjectName;
import java.util.List;

/**
 * @author <a href="mailto:boleslaw dot dawidowicz at jboss.org">Boleslaw Dawidowicz</a>
 * @version $Revision: 10817 $
 */
public class IdentityServiceControllerImpl extends AbstractJBossService implements IdentityServiceController
{

   private static final org.jboss.logging.Logger log = org.jboss.logging.Logger.getLogger(IdentityServiceControllerImpl.class);

   private String jndiName;

   protected JNDI.Binding jndiBinding;

   private IdentityContext identityContext;

   private boolean registerMBeans = true;

   private String configFile;

   private String defaultConfigFile;

   private IdentityEventBroadcaster identityEventBroadcaster;

   private boolean ldapIdentityCache = true;

   /** . */
   protected Kernel kernel;


   protected void startService() throws Exception
   {
      if (jndiName != null)
      {
         jndiBinding = new JNDI.Binding(jndiName, this);
         jndiBinding.bind();
      }

      //initialize microcontainer stuff
      try
      {
         BasicBootstrap bootstrap = new BasicBootstrap();
         bootstrap.run();
         kernel = bootstrap.getKernel();
      }
      catch (Exception e)
      {
         throw new IdentityException(e);
      }

      IdentityServicesMetaData servicesMetaData = new IdentityServicesMetaData(defaultConfigFile, configFile);

      identityContext = bootstrapIdentityContext();

      // IdentityEventBroadcaster
      IdentityEventBroadcaster broadcaster = identityEventBroadcaster;
      if (broadcaster == null)
      {
         broadcaster = new IdentityEventBroadcaster()
         {
            public void fireEvent(IdentityEvent event)
            {
               // Noop
            }
         };
      }

      //
      try
      {
         identityContext.register(broadcaster, IdentityContext.TYPE_IDENTITY_EVENT_BROADCASTER);
      }
      catch (Throwable throwable)
      {
         throw new IdentityException("Unable to install IdentityEventBroadcaster", throwable);
      }

      //process the list of modules, instantiate them, configure them, tide them
      try
      {

         //inject configuration service
         IdentityConfigurationService configuration = servicesMetaData.getConfigurationService();
         configuration.setIdentityContext(identityContext);
         //TODO:set proper jndiName and serviceName and JNDI binder
         configuration.start();

         IdentityServiceLoader serviceLoader = new IdentityServiceLoader(identityContext, kernel, registerMBeans)
         {

            protected void registerMBean(String serviceName, Object serviceObject) throws Exception
            {
               JBossServiceModelMBean mbean = new JBossServiceModelMBean(serviceObject);
               getServer().registerMBean(mbean, new ObjectName(serviceName));
            }

            protected void unregisterMBean(String serviceName) throws Exception
            {
               ObjectName on = new ObjectName(serviceName);
               if (getServer().isRegistered(on))
               {
                  getServer().unregisterMBean(on);
               }
            }

            protected ServiceJNDIBinder getServiceJNDIBinder() throws Exception
            {
               return new SimpleServiceJNDIBinder();
            }
         };

         // process datasources and modules
         serviceLoader.bootstrapDatasource(servicesMetaData.getDatasourceServices().getDatasourcesList());

         serviceLoader.bootstrapModules(servicesMetaData.getModuleServices().getModulesList());

         UserModule userModule = (UserModule)identityContext.getObject(IdentityContext.TYPE_USER_MODULE);

         RoleModule roleModule = (RoleModule)identityContext.getObject(IdentityContext.TYPE_ROLE_MODULE);

         UserProfileModule userProfileModule = (UserProfileModule)identityContext.getObject(IdentityContext.TYPE_USER_PROFILE_MODULE);


         // For performance reasons we inject a wrapper around some identity modules to cache the calls. This is optional
         // and apply only to LDAP implementation of modules. Cache is request scoped and invalidated in server interceptor
         // IdentityCacheInterceptor

         if (isLdapIdentityCache())
         {
            ServiceJNDIBinder binder = new SimpleServiceJNDIBinder();

            IdentityCacheService cacheService = new IdentityCacheService();

            binder.bind(IdentityCacheService.JNDI_NAME, cacheService);

            List modules = servicesMetaData.getModuleServices().getModulesList();

            if (userModule instanceof LDAPUserModule)
            {
               LDAPUserModule ldapUserModule = (LDAPUserModule)userModule;

               // Unregister in IdentityContext

               identityContext.unregister(IdentityContext.TYPE_USER_MODULE);

               // Unregister in JNDI

               binder.unbind(ldapUserModule.getJNDIName());

               // Un/egister mbean

               String serviceName = null;

               // Discover serviceName for this module type

               for (Object moduleData : modules)
               {
                  ModuleServiceMetaData moduleService = (ModuleServiceMetaData)moduleData;
                  ModuleMetaData module = moduleService.getModuleData();

                  if (module.getType().equals(ldapUserModule.getModuleType()))
                  {
                     serviceName = module.getServiceName();
                     break;
                  }
               }

               // If we have the service name then follow with registration

               if (serviceName != null)
               {
                  // Unregister

                  ObjectName on = new ObjectName(serviceName);
                  if (getServer().isRegistered(on))
                  {
                     getServer().unregisterMBean(on);
                  }
               }


               CachedLDAPUserModuleWrapper userModuleWrapper = new CachedLDAPUserModuleWrapper((LDAPUserModule)userModule, cacheService);

               // Register wrapper
               identityContext.register(userModuleWrapper, ldapUserModule.getModuleType());
               binder.bind(ldapUserModule.getJNDIName(), userModuleWrapper);

               if (serviceName != null)
               {
                  // Register

                  JBossServiceModelMBean mbean = new JBossServiceModelMBean(userModuleWrapper);
                  getServer().registerMBean(mbean, new ObjectName(serviceName));
               }

            }

            if (roleModule instanceof LDAPRoleModule)
            {
               LDAPRoleModule ldapRoleModule = (LDAPRoleModule)roleModule;

               // Unregister in IdentityContext

               identityContext.unregister(IdentityContext.TYPE_ROLE_MODULE);

               // Unregister in JNDI

               binder.unbind(ldapRoleModule.getJNDIName());

               // Un/egister mbean

               String serviceName = null;

               // Discover serviceName for this module type

               for (Object moduleData : modules)
               {
                  ModuleServiceMetaData moduleService = (ModuleServiceMetaData)moduleData;
                  ModuleMetaData module = moduleService.getModuleData();

                  if (module.getType().equals(ldapRoleModule.getModuleType()))
                  {
                     serviceName = module.getServiceName();
                     break;
                  }
               }

               // If we have the service name then follow with registration

               if (serviceName != null)
               {
                  // Unregister

                  ObjectName on = new ObjectName(serviceName);
                  if (getServer().isRegistered(on))
                  {
                     getServer().unregisterMBean(on);
                  }
               }


               CachedLDAPRoleModuleWrapper roleModuleWrapper = new CachedLDAPRoleModuleWrapper((LDAPRoleModule)roleModule, cacheService);

               // Register wrapper
               identityContext.register(roleModuleWrapper, ldapRoleModule.getModuleType());
               binder.bind(ldapRoleModule.getJNDIName(), roleModuleWrapper);

               if (serviceName != null)
               {
                  // Register

                  JBossServiceModelMBean mbean = new JBossServiceModelMBean(roleModuleWrapper);
                  getServer().registerMBean(mbean, new ObjectName(serviceName));
               }

            }

            if (userProfileModule instanceof LDAPUserProfileModule ||
               userProfileModule instanceof DelegatingUserProfileModuleImpl)
            {
               UserProfileModuleService profileModuleService = (UserProfileModuleService)userProfileModule;

               // Unregister in IdentityContext

               identityContext.unregister(IdentityContext.TYPE_USER_PROFILE_MODULE);

               // Unregister in JNDI

               binder.unbind(profileModuleService.getJNDIName());

               // Un/egister mbean

               String serviceName = null;

               // Discover serviceName for this module type

               for (Object moduleData : modules)
               {
                  ModuleServiceMetaData moduleService = (ModuleServiceMetaData)moduleData;
                  ModuleMetaData module = moduleService.getModuleData();

                  if (module.getType().equals(profileModuleService.getModuleType()))
                  {
                     serviceName = module.getServiceName();
                     break;
                  }
               }

               // If we have the service name then follow with registration

               if (serviceName != null)
               {
                  // Unregister

                  ObjectName on = new ObjectName(serviceName);
                  if (getServer().isRegistered(on))
                  {
                     getServer().unregisterMBean(on);
                  }
               }

               CachedUserProfileModuleWrapper userProfileModuleWrapper = new CachedUserProfileModuleWrapper(userProfileModule, cacheService);

               // Register wrapper

               identityContext.register(userProfileModuleWrapper, profileModuleService.getModuleType());
               binder.bind(profileModuleService.getJNDIName(), userProfileModuleWrapper);

               if (serviceName != null)
               {
                  // Register

                  JBossServiceModelMBean mbean = new JBossServiceModelMBean(userProfileModuleWrapper);
                  getServer().registerMBean(mbean, new ObjectName(serviceName));
               }

            }
         }

      }
      catch (Throwable e)
      {
         throw new IdentityException("Cannot initiate identity modules: ", e);
      }
   }


   protected void stopService() throws Exception
   {
      if (jndiBinding != null)
      {
         jndiBinding.unbind();
         jndiBinding = null;
      }

   }

   private IdentityContext bootstrapIdentityContext() throws Exception
   {
      KernelControllerContext identityKernelContext;
      try
      {
         AbstractBeanMetaData contextBMD = new AbstractBeanMetaData(
            "portal:identity=IdentityContext",
            IdentityContextImpl.class.getName());
         //beans.add(contextBMD);
         identityKernelContext = kernel.getController().install(contextBMD);
         return (IdentityContext)identityKernelContext.getTarget();

      }
      catch (Throwable throwable)
      {
         throw new IdentityException("Unable to install IdentityContext", throwable);
      }

   }


   public IdentityContext getIdentityContext()
   {
      return identityContext;
   }

   public String getConfigFile()
   {
      return configFile;
   }

   public void setConfigFile(String configFile)
   {
      this.configFile = configFile;
   }

   public String getDefaultConfigFile()
   {
      return defaultConfigFile;
   }

   public void setDefaultConfigFile(String defaultConfigFile)
   {
      this.defaultConfigFile = defaultConfigFile;
   }

   public String getJndiName()
   {
      return jndiName;
   }

   public void setJndiName(String jndiName)
   {
      this.jndiName = jndiName;
   }

   public JNDI.Binding getJndiBinding()
   {
      return jndiBinding;
   }

   public void setJndiBinding(JNDI.Binding jndiBinding)
   {
      this.jndiBinding = jndiBinding;
   }

   public boolean isRegisterMBeans()
   {
      return registerMBeans;
   }

   public void setRegisterMBeans(boolean registerMBeans)
   {
      this.registerMBeans = registerMBeans;
   }

   public IdentityEventBroadcaster getIdentityEventBroadcaster()
   {
      return identityEventBroadcaster;
   }

   public void setIdentityEventBroadcaster(IdentityEventBroadcaster identityEventBroadcaster)
   {
      this.identityEventBroadcaster = identityEventBroadcaster;
   }

   public boolean isLdapIdentityCache()
   {
      return ldapIdentityCache;
   }

   public void setLdapIdentityCache(boolean ldapIdentityCache)
   {
      this.ldapIdentityCache = ldapIdentityCache;
   }
}
