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

import java.net.URL;

import javax.management.MBeanServer;

import org.jboss.deployment.DeploymentException;
import org.jboss.portal.core.deployment.JBossApplicationMetaDataFactory;
import org.jboss.portal.core.model.instance.InstanceContainer;
import org.jboss.portal.server.deployment.PortalWebApp;
import org.jboss.portal.server.deployment.jboss.Deployment;
import org.xml.sax.EntityResolver;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 10228 $
 */
public class PortletAppDeploymentFactory extends org.jboss.portal.portlet.deployment.jboss.PortletAppDeploymentFactory
{

   /** . */
   protected InstanceContainer instanceContainer;

   /** . */
   protected boolean createInstances;

   /** . */
   protected EntityResolver portletInstancesEntityResolver;

   /** . */
   protected EntityResolver portalObjectEntityResolver;

   public Deployment newInstance(URL url, PortalWebApp pwa, MBeanServer mbeanServer) throws DeploymentException
   {
      return new PortletAppDeployment(url, pwa, bridgeToInvoker, mbeanServer, this);
   }

   public EntityResolver getPortalObjectEntityResolver()
   {
      return portalObjectEntityResolver;
   }

   public void setPortalObjectEntityResolver(EntityResolver portalObjectEntityResolver)
   {
      this.portalObjectEntityResolver = portalObjectEntityResolver;
   }

   public EntityResolver getPortletInstancesEntityResolver()
   {
      return portletInstancesEntityResolver;
   }

   public void setPortletInstancesEntityResolver(EntityResolver portletInstancesEntityResolver)
   {
      this.portletInstancesEntityResolver = portletInstancesEntityResolver;
   }

   public InstanceContainer getInstanceContainer()
   {
      return instanceContainer;
   }

   public void setInstanceContainer(InstanceContainer instanceContainer)
   {
      this.instanceContainer = instanceContainer;
   }

   /** Return a subclass that does more. */
   public org.jboss.portal.portlet.deployment.jboss.JBossApplicationMetaDataFactory createJBossApplicationMetaDataFactory()
   {
      return new JBossApplicationMetaDataFactory();
   }

   public void enableCreateInstances()
   {
      createInstances = true;
   }

   public void disableCreateInstances()
   {
      createInstances = false;
   }

   public boolean getCreateInstances()
   {
      return createInstances;
   }

   public void setCreateInstances(boolean createInstances)
   {
      this.createInstances = createInstances;
   }
}
