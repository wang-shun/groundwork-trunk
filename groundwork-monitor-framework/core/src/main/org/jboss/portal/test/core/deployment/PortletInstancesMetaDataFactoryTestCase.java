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

package org.jboss.portal.test.core.deployment;

import junit.framework.TestCase;
import org.jboss.portal.common.i18n.LocalizedString;
import org.jboss.portal.common.io.IOTools;
import org.jboss.portal.common.net.URLTools;
import org.jboss.portal.common.xml.NullEntityResolver;
import org.jboss.portal.common.xml.XMLTools;
import org.jboss.portal.core.deployment.jboss.PortletAppDeploymentFactory;
import org.jboss.portal.core.model.instance.metadata.InstanceMetaData;
//import org.jboss.portal.portlet.impl.jsr168.info.Utils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.EntityResolver;

import javax.xml.parsers.DocumentBuilder;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class PortletInstancesMetaDataFactoryTestCase extends TestCase
{

   public void testLocalizedDisplayNameMetadata() throws Exception
   {
      // Get instances from portlet-instances.xml
      InputStream in = null;
      List instancesMetadata = new ArrayList();
      try
      {

         URL jbossPortletXML = Thread.currentThread().getContextClassLoader().getResource("test/deployment/jboss-portlet.xml");
         assertTrue(URLTools.exists(jbossPortletXML));

         in = Thread.currentThread().getContextClassLoader().getResourceAsStream("test/deployment/portlet-instances.xml");
//         in = IOTools.safeBufferedWrapper(in);


         assertNotNull(in);

         PortletAppDeploymentFactory factory = new PortletAppDeploymentFactory();

         if (in != null)
         {
            DocumentBuilder builder = XMLTools.getDocumentBuilderFactory().newDocumentBuilder();
            EntityResolver entityResolver = factory.getPortletInstancesEntityResolver();
            if (entityResolver == null)
            {
               entityResolver = new NullEntityResolver();
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
               InstanceMetaData metaData = InstanceMetaData.buildMetaData(instanceElt, null);
               instancesMetadata.add(metaData);
            }
         }
      }
      finally
      {
         IOTools.safeClose(in);
      }

      assertEquals(4, instancesMetadata.size());

      InstanceMetaData instanceMD = (InstanceMetaData)instancesMetadata.get(0);
      assertEquals("UserPortletInstance", instanceMD.getId());
      LocalizedString lString = instanceMD.getDisplayName();
      assertEquals("Mon instance de User portlet", lString.getString(Locale.FRENCH, false));
      assertEquals("My User portlet instance", lString.getString(Locale.ENGLISH, false));
      assertEquals("My User portlet instance", lString.getDefaultString());
      instanceMD = (InstanceMetaData)instancesMetadata.get(1);
      assertEquals("RolePortletInstance", instanceMD.getId());
      instanceMD = (InstanceMetaData)instancesMetadata.get(2);
      assertEquals("CatalogPortletInstance", instanceMD.getId());
      instanceMD = (InstanceMetaData)instancesMetadata.get(3);
      assertEquals("PortletContentEditorInstance", instanceMD.getId());

   }
}
