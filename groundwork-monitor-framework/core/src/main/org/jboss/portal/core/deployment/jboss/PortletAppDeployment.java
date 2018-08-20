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
package org.jboss.portal.core.deployment.jboss;

import org.jboss.deployment.DeploymentException;
import org.jboss.mx.util.MBeanProxyExt;
import org.jboss.portal.common.io.IOTools;
import org.jboss.portal.common.net.URLNavigator;
import org.jboss.portal.common.net.URLVisitor;
import org.jboss.portal.common.xml.NullEntityResolver;
import org.jboss.portal.common.xml.XMLTools;
import org.jboss.portal.core.metadata.ServiceMetaData;
import org.jboss.portal.core.metadata.portlet.JBossApplicationMetaData;
import org.jboss.portal.core.model.instance.DuplicateInstanceException;
import org.jboss.portal.core.model.instance.Instance;
import org.jboss.portal.core.model.instance.metadata.InstanceMetaData;
import org.jboss.portal.portlet.InvalidPortletIdException;
import org.jboss.portal.portlet.NoSuchPortletException;
import org.jboss.portal.portlet.PortletInvokerException;
import org.jboss.portal.portlet.container.managed.ManagedObjectRegistryEventListener;
import org.jboss.portal.portlet.impl.metadata.portlet.PortletPreferenceMetaData;
import org.jboss.portal.portlet.impl.metadata.portlet.PortletPreferencesMetaData;
import org.jboss.portal.portlet.state.PropertyChange;
import org.jboss.portal.portlet.state.PropertyMap;
import org.jboss.portal.security.RoleSecurityBinding;
import org.jboss.portal.security.SecurityConstants;
import org.jboss.portal.security.metadata.SecurityConstraintsMetaData;
import org.jboss.portal.security.spi.provider.AuthorizationDomain;
import org.jboss.portal.security.spi.provider.DomainConfigurator;
import org.jboss.portal.security.spi.provider.SecurityConfigurationException;
import org.jboss.portal.server.deployment.PortalWebApp;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.EntityResolver;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.xml.parsers.DocumentBuilder;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 11152 $
 */
public class PortletAppDeployment extends org.jboss.portal.portlet.deployment.jboss.PortletAppDeployment
{

   /** . */
   public static final int OVERWRITE_IF_EXISTS = 0;

   /** . */
   public static final int KEEP_IF_EXISTS = 1;

   /** . */
   private PortletAppDeploymentFactory factory;

   public PortletAppDeployment(URL url, PortalWebApp pwa, ManagedObjectRegistryEventListener listener, MBeanServer mbeanServer, PortletAppDeploymentFactory factory)
   {
      super(url, pwa, listener, mbeanServer, factory);
      this.factory = factory;
   }

   public void start() throws DeploymentException
   {
      // Inject services if needed
      injectServices();

      //
      super.start();

      // Build instances objects related to the portlet life cycle
      if (factory.getCreateInstances())
      {
         buildInstances();
      }
   }

   /** Inject service proxies into the context of the web application if it is needed. */
   protected void injectServices()
   {
      if (jbossAppMD instanceof JBossApplicationMetaData)
      {
         JBossApplicationMetaData jBossApplicationMetaData = ((JBossApplicationMetaData)jbossAppMD);
         for (Iterator<ServiceMetaData> i = jBossApplicationMetaData.getServices().values().iterator(); i.hasNext();)
         {
            ServiceMetaData serviceMD = (ServiceMetaData)i.next();

            //
            String serviceName = serviceMD.getName();
            String serviceClass = serviceMD.getClassName();
            String serviceRef = serviceMD.getRef();
            log.debug("Trying to inject service '" + serviceName + "' (ref: '" + serviceRef + "' class: " + serviceClass +
                    ") in the servlet context of " + pwa.getURL());

            //
            if (serviceRef.startsWith(":"))
            {
               log.debug("Detecting a relative service reference " + serviceRef + " prepending it with " + factory.getConfig().getDomain());
               serviceRef = factory.getConfig().getDomain() + serviceRef;
            }

            //
            try
            {
               Class proxyClass = pwa.getClassLoader().loadClass(serviceClass);
               ObjectName objectName = ObjectName.getInstance(serviceRef);
               Object proxy = MBeanProxyExt.create(proxyClass, objectName, mbeanServer, true);
               pwa.getServletContext().setAttribute(serviceName, proxy);
            }
            catch (Exception e)
            {
               log.error("Was not able to create service proxy", e);
            }
         }
      }
   }

   private void buildInstances()
   {
      // Read portlet-instances.xml
      try
      {
         // All the meta data gathered
         final ArrayList metaDataCtxs = new ArrayList();

         // Parse instances from legacy -object.xml
         log.debug("Looking for the WEB-INF path of " + pwa.getId());
         final String webInfPath = pwa.getServletContext().getRealPath("/WEB-INF");
         if (webInfPath != null)
         {
            File webInfFile = new File(webInfPath);
            URL webInfURL = webInfFile.toURL();
            URLNavigator.visit(webInfURL, new URLVisitor()
            {
               public void startDir(URL url, String name)
               {
               }

               public void endDir(URL url, String name)
               {
               }

               public void file(URL url, String name)
               {
                  boolean logged = false;
                  if (name.endsWith("-object.xml"))
                  {
                     InputStream in = null;
                     try
                     {
                        in = IOTools.safeBufferedWrapper(url.openStream());
                        DocumentBuilder builder = XMLTools.getDocumentBuilderFactory().newDocumentBuilder();
                        EntityResolver entityResolver = factory.getPortalObjectEntityResolver();
                        if (entityResolver == null)
                        {
                           log.debug("Coult not obtain entity resolver for " + url);
                           entityResolver = new NullEntityResolver();
                        }
                        else
                        {
                           log.debug("Obtained entity resolver " + entityResolver + " for " + url);
                        }
                        builder.setEntityResolver(entityResolver);
                        Document doc = builder.parse(in);
                        Element deploymentsElt = doc.getDocumentElement();
                        for (Iterator i = XMLTools.getChildrenIterator(deploymentsElt, "deployment"); i.hasNext();)
                        {
                           Element deploymentElt = (Element)i.next();

                           //
                           Element instanceElt = XMLTools.getUniqueChild(deploymentElt, "instance", false);

                           //
                           if (instanceElt != null)
                           {
                              if (!logged)
                              {
                                 log.debug("Found -object.xml containing instances, you need to convert and move them to the file " + webInfPath + "/portlet-instances.xml");
                                 logged = true;
                              }

                              //
                              InstanceMetaData metaData = InstanceMetaData.buildLegacyMetaData(instanceElt, pwa.getId());

                              //
                              if (metaData != null)
                              {
                                 InstanceMetaDataContext metaDataCtx = new InstanceMetaDataContext(metaData, KEEP_IF_EXISTS);
                                 metaDataCtxs.add(metaDataCtx);
                              }
                           }
                        }
                     }
                     catch (Exception e)
                     {
                        e.printStackTrace();
                     }
                     finally
                     {
                        IOTools.safeClose(in);
                     }
                  }
               }
            });
         }
         else
         {
            log.debug("No real path found");
         }

         // Output legacy file on the console
         if (metaDataCtxs.size() > 0)
         {
            DocumentBuilder builder = XMLTools.getDocumentBuilderFactory().newDocumentBuilder();
            Document doc = builder.newDocument();
            Element deployments = (Element)doc.appendChild(doc.createElement("deployments"));
            for (int i = 0; i < metaDataCtxs.size(); i++)
            {
               InstanceMetaDataContext metaDataCtx = (InstanceMetaDataContext)metaDataCtxs.get(i);
               InstanceMetaData instanceMD = metaDataCtx.metaData;
               Element deploymentElt = (Element)deployments.appendChild(doc.createElement("deployment"));
               Element instanceElt = (Element)deploymentElt.appendChild(doc.createElement("instance"));
               Element instanceIdElt = (Element)instanceElt.appendChild(doc.createElement("instance-id"));
               instanceIdElt.appendChild(doc.createTextNode(instanceMD.getId()));
               Element portletRefElt = (Element)instanceElt.appendChild(doc.createElement("portlet-ref"));
               portletRefElt.appendChild(doc.createTextNode(instanceMD.getPortletRef()));
            }
            String migratedContent = XMLTools.toString(doc, false, true, true, "utf-8");
            log.info("These instances have been found in -object.xml, you should put them in the file " + webInfPath + "/portlet-instances.xml");
            log.info(migratedContent);
         }

         // Get instances from portlet-instances.xml
         InputStream in = null;
         try
         {
            in = IOTools.safeBufferedWrapper(pwa.getServletContext().getResourceAsStream("/WEB-INF/portlet-instances.xml"));
            if (in != null)
            {
               DocumentBuilder builder = XMLTools.getDocumentBuilderFactory().newDocumentBuilder();
               EntityResolver entityResolver = factory.getPortletInstancesEntityResolver();
               if (entityResolver == null)
               {
                  log.debug("Coult not obtain entity resolver for portlet-instances.xml");
                  entityResolver = new NullEntityResolver();
               }
               else
               {
                  log.debug("Obtained entity resolver " + entityResolver + " for portlet-instances.xml");
               }
               builder.setEntityResolver(entityResolver);
               Document doc = builder.parse(in);

               //
               for (Iterator i = XMLTools.getChildrenIterator(doc.getDocumentElement(), "deployment"); i.hasNext();)
               {
                  Element deploymentElt = (Element)i.next();

                  //
                  Element instanceElt = XMLTools.getUniqueChild(deploymentElt, "instance", true);

                  //
                  InstanceMetaData metaData = InstanceMetaData.buildMetaData(instanceElt, pwa);

                  //
                  Element ifExistsElt = XMLTools.getUniqueChild(deploymentElt, "if-exists", false);
                  int ifExists = KEEP_IF_EXISTS;
                  if (ifExistsElt != null)
                  {
                     String tmp = XMLTools.asString(ifExistsElt);
                     if ("overwrite".equals(tmp))
                     {
                        ifExists = OVERWRITE_IF_EXISTS;
                     }
                     else if ("keep".equals(tmp))
                     {
                        ifExists = KEEP_IF_EXISTS;
                     }
                  }

                  //
                  InstanceMetaDataContext metaDataCtx = new InstanceMetaDataContext(metaData, ifExists);

                  //
                  metaDataCtxs.add(metaDataCtx);
               }
            }
         }
         finally
         {
            IOTools.safeClose(in);
         }

         // Create instances when we have
         if (metaDataCtxs.size() > 0)
         {
            createInstances(metaDataCtxs);
         }
      }
      catch (Exception e)
      {
         log.error("Error when creating instances", e);
      }
   }

   private void createInstances(List metaDataCtxs) throws Exception
   {
      // Create all the instances when possible
      for (int i = 0; i < metaDataCtxs.size(); i++)
      {
         InstanceMetaDataContext metaDataCtx = (InstanceMetaDataContext)metaDataCtxs.get(i);
         //
         try
         {
            handleInstance(metaDataCtx);
         }
         catch (NoSuchPortletException e)
         {
            String msg = "Failed to create instance " + metaDataCtx.metaData.getId() + " of portlet " + e.getPortletId() +
                    " because portlet " + e.getPortletId() + " is not available";
            log.warn(msg);
            log.debug(msg, e);
         }
         catch (InvalidPortletIdException e)
         {
            String msg = "Failed to create instance " + metaDataCtx.metaData.getId() + " of portlet " + e.getPortletId() +
                    " because portlet id " + e.getPortletId() + " is invalid";
            log.warn(msg);
            log.debug(msg, e);
         }
         catch (PortletInvokerException e)
         {
            String msg = "Failed to create instance " + metaDataCtx.metaData.getId() + " of portlet";
            log.warn(msg);
            log.debug(msg, e);
         }
         catch (DuplicateInstanceException e)
         {
            String msg = "Instance " + metaDataCtx.metaData.getId() + " already exists";
            log.warn(msg);
            log.debug(msg, e);
         }
         catch (SecurityConfigurationException e)
         {
            String msg = "Cannot configure security of instance " + metaDataCtx.metaData.getId();
            log.warn(msg);
            log.debug(msg, e);
         }
      }
   }

   /** Requires a transaction to execute, setup in aop configuration. */
   private void handleInstance(InstanceMetaDataContext metaDataCtx) throws PortletInvokerException, SecurityConfigurationException, DuplicateInstanceException
   {
      InstanceMetaData metaData = metaDataCtx.metaData;
      Instance instance = factory.getInstanceContainer().getDefinition(metaData.getId());
      if (instance == null)
      {
         createInstance(metaData);
      }
      else if (metaDataCtx.ifExists == OVERWRITE_IF_EXISTS)
      {
         log.debug("Reconfiguring instance " + metaData.getId() + " that already exists");
         configureInstance(instance, metaData);
      }
      else
      {
         log.debug("Instance " + metaData.getId() + " exists");
      }
   }

   private void configureInstance(Instance instance, InstanceMetaData metaData) throws PortletInvokerException, SecurityConfigurationException
   {
      List<PropertyChange> changes = new ArrayList<PropertyChange>();

      // Reset all preferences that are not overridden
      PortletPreferencesMetaData preferencesMetaData = metaData.getPreferences();
      PropertyMap propertyMap = instance.getProperties();
      if (propertyMap != null && propertyMap.size() > 0)
      {
         for (String key : propertyMap.keySet())
         {
            if (preferencesMetaData == null || preferencesMetaData.getPortletPreferences() == null || preferencesMetaData.getPortletPreferences().get(key) == null)
            {
               changes.add(PropertyChange.newReset(key));
            }
         }
      }

      // Configure preferences only if needed
      if (preferencesMetaData != null && preferencesMetaData.getPortletPreferences() != null && preferencesMetaData.getPortletPreferences().size() > 0)
      {
         for (PortletPreferenceMetaData preference : preferencesMetaData.getPortletPreferences().values())
         {
            List<String> preferenceValues = preference.getValue();
            changes.add(PropertyChange.newUpdate(preference.getName(), preferenceValues.toArray(new String[preferenceValues.size()])));
         }
      }

      if (changes.size() > 0)
      {
         instance.setProperties(changes.toArray(new PropertyChange[changes.size()]));
      }

      // Configure security
      SecurityConstraintsMetaData securityConstraints = metaData.getSecurityConstraints();
      if (securityConstraints == null)
      {
         securityConstraints = new SecurityConstraintsMetaData();
         securityConstraints.getConstraints().add(new RoleSecurityBinding("view", SecurityConstants.UNCHECKED_ROLE_NAME));
      }
      AuthorizationDomain authDomain = instance.getContainer().getAuthorizationDomain();
      DomainConfigurator domainConfigurator = authDomain.getConfigurator();
      domainConfigurator.setSecurityBindings(instance.getId(), securityConstraints.getConstraints());
   }

   private void createInstance(InstanceMetaData metaData) throws PortletInvokerException, DuplicateInstanceException, SecurityConfigurationException
   {
      log.debug("Creating portlet instance " + metaData.getId());

      // Resolve the portlet ref
      metaData.setPortletRef(resolvePortletRef(metaData.getPortletRef()));

      // Create the instance
      Instance instance = factory.getInstanceContainer().createDefinition(metaData);

      // Configure
      configureInstance(instance, metaData);
   }


   private String resolvePortletRef(String ref)
   {
      return "local." + getAppId() + "." + ref;
   }

   private class InstanceMetaDataContext
   {
      /** . */
      private InstanceMetaData metaData;

      /** . */
      private int ifExists;

      public InstanceMetaDataContext(InstanceMetaData metaData, int ifExists)
      {
         this.metaData = metaData;
         this.ifExists = ifExists;
      }
   }
}
