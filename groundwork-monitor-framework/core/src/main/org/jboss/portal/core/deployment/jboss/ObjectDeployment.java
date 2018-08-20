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
import org.jboss.logging.Logger;
import org.jboss.portal.common.io.IOTools;
import org.jboss.portal.common.xml.NullEntityResolver;
import org.jboss.portal.common.xml.XMLTools;
import org.jboss.portal.core.controller.coordination.CoordinationConfigurator;
import org.jboss.portal.core.model.content.ContentType;
import org.jboss.portal.core.model.content.spi.ContentProvider;
import org.jboss.portal.core.model.content.spi.handler.ContentHandler;
import org.jboss.portal.core.model.portal.PortalObject;
import org.jboss.portal.core.model.portal.PortalObjectContainer;
import org.jboss.portal.core.model.portal.PortalObjectId;
import org.jboss.portal.core.model.portal.PortalObjectPath;
import org.jboss.portal.core.model.portal.metadata.BuildContext;
import org.jboss.portal.core.model.portal.metadata.PortalObjectMetaData;
import org.jboss.portal.server.deployment.PortalWebApp;
import org.jboss.portal.server.deployment.jboss.Deployment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.EntityResolver;

import javax.management.MBeanServer;
import javax.transaction.TransactionManager;
import javax.xml.parsers.DocumentBuilder;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @author <a href="mailto:boleslaw dot dawidowicz at redhat anotherdot com">Boleslaw Dawidowicz</a>
 * @version $Revision: 11767 $
 */
public class ObjectDeployment extends Deployment
{

   /** . */
   public static final int OVERWRITE_IF_EXISTS = 0;

   /** . */
   public static final int KEEP_IF_EXISTS = 1;

   /** The logger. */
   protected static final Logger log = Logger.getLogger(ObjectDeployment.class);

   /** . */
   protected boolean isTrace = log.isTraceEnabled();

   /** . */
   protected TransactionManager tm;

   /** The factory that created this deployment. */
   protected ObjectDeploymentFactory factory;

   /** What we have deployed. */
   protected ArrayList<Unit> units;

   public ObjectDeployment(URL url,
                           MBeanServer mbeanServer,
                           TransactionManager tm,
                           PortalWebApp pwa,
                           ObjectDeploymentFactory factory)
   {
      super(url, pwa, mbeanServer);
      this.tm = tm;
      this.factory = factory;
   }

   public void start() throws DeploymentException
   {
      try
      {
         doStart();
      }
      catch (Exception e)
      {
         // And rethrow the exception for the deployer
         throw new DeploymentException(e);
      }
   }

   protected void doStart() throws Exception
   {
      InputStream in = null;
      try
      {
         // Load xml document
         log.debug("Loading portal metadata from " + url);
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

         // Build the list of deployment units
         List<Element> deploymentElts = XMLTools.getChildren(deploymentsElt, "deployment");
         units = new ArrayList<Unit>(deploymentElts.size());
         for (Element deploymentElt : deploymentElts)
         {
            Unit unit = new Unit();

            //
            Element parentRefElt = XMLTools.getUniqueChild(deploymentElt, "parent-ref", false);
            unit.parentRef = parentRefElt == null ? null : PortalObjectId.parse(XMLTools.asString(parentRefElt), PortalObjectPath.LEGACY_FORMAT);

            //
            Element ifExistsElt = XMLTools.getUniqueChild(deploymentElt, "if-exists", false);
            unit.ifExists = KEEP_IF_EXISTS;
            if (ifExistsElt != null)
            {
               String ifExists = XMLTools.asString(ifExistsElt);
               if ("overwrite".equals(ifExists))
               {
                  unit.ifExists = OVERWRITE_IF_EXISTS;
               }
               else if ("keep".equals(ifExists))
               {
                  unit.ifExists = KEEP_IF_EXISTS;
               }
            }

            // The object to create
            PortalObjectMetaData metaData = null;

            //
            Element metaDataElt = XMLTools.getUniqueChild(deploymentElt, "portal", false);
            if (metaDataElt == null)
            {
               metaDataElt = XMLTools.getUniqueChild(deploymentElt, "page", false);
               if (metaDataElt == null)
               {
                  metaDataElt = XMLTools.getUniqueChild(deploymentElt, "window", false);
                  if (metaDataElt == null)
                  {
                     metaDataElt = XMLTools.getUniqueChild(deploymentElt, "context", false);
                  }
               }
            }
            if (metaDataElt != null)
            {
               metaData = PortalObjectMetaData.buildMetaData(factory.contentProviderRegistry, metaDataElt);
            }
            else
            {
               log.debug("Instances element in -object.xml is not supported anymore");
            }

            //
            if (metaData != null)
            {
               unit.metaData = metaData;
               units.add(unit);
            }
         }

         // Resolve against ref in the same deployment
         BuildContext portalObjectBuildContext = new BuildContext()
         {
            public PortalObjectContainer getContainer()
            {
               return factory.portalObjectContainer;
            }

            public ContentHandler getContentHandler(ContentType contentType)
            {
               ContentProvider contentProvider = factory.contentProviderRegistry.getContentProvider(contentType);
               return contentProvider != null ? contentProvider.getHandler() : null;
            }
            
            public PortalWebApp getPortalWebApp()
            {
               return pwa;
            }

            public CoordinationConfigurator getCoordinationConfigurator()
            {
               return factory.getCoordinationConfigurator();
            }
         };

         // Create all objects
         for (Unit unit : units)
         {
            if (isTrace)
            {
               log.trace(unit);
            }
            if (unit.metaData instanceof PortalObjectMetaData)
            {

               PortalObjectContainer portalObjectContainer = factory.getPortalObjectContainer();
               PortalObjectMetaData portalObjectMD = (PortalObjectMetaData)unit.metaData;

               if (unit.parentRef != null)
               {
                  log.debug("Checking existence of parent portal object '" + unit.parentRef + "'");
                  Object o = portalObjectContainer.getObject(unit.parentRef);
                  if (o instanceof PortalObject)
                  {
                     PortalObject parent = (PortalObject)o;
                     boolean create = true;
                     if (parent.getChild(portalObjectMD.getName()) != null)
                     {
                        switch (unit.ifExists)
                        {
                           case OVERWRITE_IF_EXISTS:
                              parent.destroyChild(portalObjectMD.getName());
                              break;
                           case KEEP_IF_EXISTS:
                              create = false;
                              break;
                        }
                     }
                     if (create)
                     {
                        log.debug("Building portal object");
                        PortalObject po = portalObjectMD.create(portalObjectBuildContext, parent);
                        unit.ref = po.getId();
                     }
                  }
                  else if (o == null)
                  {
                     log.warn("Cannot create portal object " + unit.metaData + " because the parent '" + unit.parentRef + "' that the deployment descriptor references does not exist");
                  }
               }
               else
               {
                  if (portalObjectContainer.getContext(portalObjectMD.getName()) == null)
                  {
                     log.debug("Building portal object");
                     PortalObject po = portalObjectMD.create(portalObjectBuildContext, null);
                     unit.ref = po.getId();
                  }
               }
            }
         }
      }
      finally
      {
         IOTools.safeClose(in);
      }
   }

   public void stop() throws DeploymentException
   {
   }

   /**
    * Return factory
    *
    * @return @see ObjectDeploymentFactory
    */
   public ObjectDeploymentFactory getFactory()
   {
      return factory;
   }

   /** A unit of deployment in the deployment descriptor. */
   private static class Unit
   {
      /** The strategy to use when the root object already exists. */
      protected int ifExists;

      /** The parent ref. */
      protected PortalObjectId parentRef;

      /** Meta data of the deployed portal object. */
      protected Object metaData;

      /** The handle of the deployed object if not null. */
      protected PortalObjectId ref;

      public String toString()
      {
         StringBuffer buffer = new StringBuffer("Unit[::ifExists=" + ifExists);
         buffer.append(":parentRef=").append(parentRef);
         buffer.append(":Metadata=").append(metaData).append(":ref=").append(ref).append("]");
         return buffer.toString();
      }
   }

}
