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
package org.jboss.portlet.content;

import org.apache.log4j.Logger;
import org.jboss.mx.util.MBeanProxy;
import org.jboss.mx.util.MBeanServerLocator;
import org.jboss.portal.Mode;
import org.jboss.portal.core.impl.model.content.InternalContentProviderRegistry;
import org.jboss.portal.core.impl.model.content.generic.InternalGenericContentProvider;
import org.jboss.portal.core.model.content.ContentType;
import org.jboss.portal.core.model.content.spi.portlet.ContentPortlet;
import org.jboss.portal.core.model.instance.InstanceContainer;
import org.jboss.portal.jems.as.system.JBossServiceModelMBean;
import org.jboss.portal.security.spi.auth.PortalAuthorizationManagerFactory;

import javax.management.Attribute;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Provide registration of a content type with a portlet instance. This listener can be used in war files to register
 * content driven portlet.
 *
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @author <a href="mailto:theute@jboss.org">Thomas Heute</a>
 * @version $Revision: 8786 $
 */
public class ContentTypeRegistration implements ServletContextListener
{

   /** . */
   private static final Logger log = Logger.getLogger(ContentTypeRegistration.class);

   /** . */
   private ContentType contentType;

   public void contextInitialized(ServletContextEvent event)
   {
      ServletContext ctx = event.getServletContext();

      //
      String tmp = ctx.getInitParameter("org.jboss.portal.content_type");
      if (tmp == null)
      {
         log.warn("The content type of the content registration is not defined, please define the init parameter org.jboss.portal.content_type in web.xml");
         return;
      }
      contentType = ContentType.create(tmp);

      //
      final String portletInstanceName = event.getServletContext().getInitParameter("org.jboss.portal.portlet_instance");
      if (portletInstanceName == null)
      {
         log.warn("The portlet instance name of the content registration is not defined, please define the init parameter org.jboss.portal.portlet_instance in web.xml");
         return;
      }

      ContentPortlet contentPortlet = new ContentPortlet()
      {
         public String getPortletName(Mode mode)
         {
            return portletInstanceName;
         }
      };

      //
      log.debug("About to register content type " + contentType + " with portlet instance " + portletInstanceName);

      InternalGenericContentProvider provider = new InternalGenericContentProvider();
      provider.setContentType(contentType.toString());

      provider.setDecorateContent(true);
      provider.setContentPortletInfo(contentPortlet);

      try
      {
         String name = "portal:service=ContentRenderer,type=" + portletInstanceName;
         ObjectName objectName = new ObjectName(name);
         MBeanServer mbeanServer = MBeanServerLocator.locateJBoss();
         JBossServiceModelMBean mbean = new JBossServiceModelMBean(provider);

         // Register the mbean
         mbeanServer.registerMBean(mbean, objectName);

         //
         ObjectName scObjectName = new ObjectName("jboss.system:service=ServiceController");
         mbeanServer.invoke(scObjectName, "create", new ObjectName[]{objectName}, new String[]{"javax.management.ObjectName"});

         // Get proxy on Content Provide Registry
         ObjectName contentProviderRegistryON = new ObjectName("portal:service=ContentProviderRegistry");
         Object registry = MBeanProxy.get(InternalContentProviderRegistry.class, contentProviderRegistryON, mbeanServer);
         mbean.setAttribute(new Attribute("Registry", registry));

         // Get proxy on Instance Container
         ObjectName instanceContainerON = new ObjectName("portal:container=Instance");
         Object container = MBeanProxy.get(InstanceContainer.class, instanceContainerON, mbeanServer);
         mbean.setAttribute(new Attribute("InstanceContainer", container));

         // Get proxy on Portal Authorization Manager Factory
         ObjectName pamfON = new ObjectName("portal:service=PortalAuthorizationManagerFactory");
         Object factory = MBeanProxy.get(PortalAuthorizationManagerFactory.class, pamfON, mbeanServer);
         mbean.setAttribute(new Attribute("PortalAuthorizationManagerFactory", factory));

         // Set dependencies
         List dependencies = new ArrayList();
         dependencies.add(contentProviderRegistryON);
         dependencies.add(instanceContainerON);
         dependencies.add(pamfON);
         mbeanServer.invoke(scObjectName, "register", new Object[]{objectName, dependencies}, new String[]{"javax.management.ObjectName", "java.util.Collection"});

         // Start the mbean
         mbeanServer.invoke(scObjectName, "start", new ObjectName[]{objectName}, new String[]{"javax.management.ObjectName"});

         log.debug("Registered InternalGenericContentProvider with name:" + name);
      }
      catch (Exception e)
      {
         log.warn("Couldn't perform ContentProvider registration", e);
         return;
      }
      log.debug("Registered content type " + contentType + " with portlet instance " + portletInstanceName);
   }

   public void contextDestroyed(ServletContextEvent event)
   {
      if (contentType != null)
      {
         log.debug("About to unregister content type " + contentType);

         //
         final String portletInstanceName = event.getServletContext().getInitParameter("org.jboss.portal.portlet_instance");
         String name = "portal:service=ContentRenderer,type=" + portletInstanceName;
         try
         {
            ObjectName objectName = new ObjectName(name);
            ObjectName scObjectName = new ObjectName("jboss.system:service=ServiceController");

            // Stop the mbean
            MBeanServer mbeanServer = MBeanServerLocator.locateJBoss();
            mbeanServer.invoke(scObjectName, "stop", new ObjectName[]{objectName}, new String[]{"javax.management.ObjectName"});
            mbeanServer.invoke(scObjectName, "remove", new Object[]{objectName}, new String[]{"javax.management.ObjectName"});
         }
         catch (Exception e)
         {
            log.warn("Couldn't perform ContentProvider registration", e);
            return;
         }

         log.debug("Content type " + contentType + " has been unregistered");
      }
   }
}
