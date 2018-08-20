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
import org.jboss.portal.common.transaction.TransactionManagerProvider;
import org.jboss.portal.core.model.content.spi.ContentProviderRegistry;
import org.jboss.portal.core.model.portal.PortalObjectContainer;
import org.jboss.portal.core.controller.coordination.CoordinationConfigurator;
import org.jboss.portal.server.deployment.PortalWebApp;
import org.jboss.portal.server.deployment.jboss.AbstractDeploymentFactory;
import org.jboss.portal.server.deployment.jboss.Deployment;
import org.xml.sax.EntityResolver;

import javax.management.MBeanServer;
import javax.transaction.TransactionManager;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @author <a href="mailto:boleslaw dot dawidowicz at redhat anotherdot com">Boleslaw Dawidowicz</a>
 * @version $Revision: 11492 $
 */
public class ObjectDeploymentFactory extends AbstractDeploymentFactory
{

   protected static final Pattern URL_PATTERN = Pattern.compile(".*-object\\.xml");

   /** . */
   protected URL setupURL;

   /** . */
   protected PortalObjectContainer portalObjectContainer;

   /** . */
   protected ContentProviderRegistry contentProviderRegistry;

   /** . */
   protected EntityResolver portalObjectEntityResolver;

   /** . */
   protected CoordinationConfigurator coordinationConfigurator;

   public boolean acceptFile(URL url)
   {
      String urlAsFile = url.getFile();
      Matcher matcher = URL_PATTERN.matcher(urlAsFile);
      return matcher.matches();
   }

   public Deployment newInstance(URL url, PortalWebApp pwa, MBeanServer mbeanServer) throws DeploymentException
   {
      try
      {
         TransactionManager tm = TransactionManagerProvider.JBOSS_PROVIDER.getTransactionManager();
         return new ObjectDeployment(url, mbeanServer, tm, pwa, this);
      }
      catch (Exception e)
      {
         throw new DeploymentException(e);
      }
   }

   public EntityResolver getPortalObjectEntityResolver()
   {
      return portalObjectEntityResolver;
   }

   public void setPortalObjectEntityResolver(EntityResolver portalObjectEntityResolver)
   {
      this.portalObjectEntityResolver = portalObjectEntityResolver;
   }

   public PortalObjectContainer getPortalObjectContainer()
   {
      return portalObjectContainer;
   }

   public void setPortalObjectContainer(PortalObjectContainer portalObjectContainer)
   {
      this.portalObjectContainer = portalObjectContainer;
   }

   public ContentProviderRegistry getContentProviderRegistry()
   {
      return contentProviderRegistry;
   }

   public void setContentProviderRegistry(ContentProviderRegistry contentProviderRegistry)
   {
      this.contentProviderRegistry = contentProviderRegistry;
   }

   public CoordinationConfigurator getCoordinationConfigurator()
   {
      return coordinationConfigurator;
   }

   public void setCoordinationConfigurator(CoordinationConfigurator coordinationConfigurator)
   {
      this.coordinationConfigurator = coordinationConfigurator;
   }
}
