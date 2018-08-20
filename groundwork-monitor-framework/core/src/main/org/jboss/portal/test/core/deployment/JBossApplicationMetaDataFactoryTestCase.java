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
import org.jboss.portal.common.net.URLTools;
import org.jboss.portal.core.deployment.JBossApplicationMetaDataFactory;
import org.jboss.portal.core.metadata.portlet.HeaderContentMetaData;
import org.jboss.portal.core.metadata.portlet.JBossApplicationMetaData;
import org.jboss.portal.core.metadata.portlet.JBossPortletMetaData;
import org.jboss.portal.core.metadata.portlet.LinkElementMetaData;
import org.jboss.portal.core.metadata.portlet.NamedMetaElementMetaData;
import org.jboss.portal.core.metadata.portlet.PortletIconMetaData;
import org.jboss.portal.core.metadata.portlet.PortletInfoMetaData;
import org.jboss.portal.core.metadata.portlet.ScriptElementMetaData;
import org.jboss.xb.binding.Unmarshaller;
import org.jboss.xb.binding.UnmarshallerFactory;

import java.net.URL;
import java.util.List;

/**
 * @author <a href="mailto:chris.laprun@jboss.com?subject=org.jboss.portal.test.core.deployment.JBossApplicationMetaDataFactoryTestCase">Chris
 *         Laprun</a>
 * @version $Revision: 8786 $
 * @since 2.4
 */
public class JBossApplicationMetaDataFactoryTestCase extends TestCase
{
   public void testHeaderContentMetaData() throws Exception
   {
      URL jbossPortletXML = Thread.currentThread().getContextClassLoader().getResource("test/deployment/jboss-portlet.xml");
      assertTrue(URLTools.exists(jbossPortletXML));

      //
      JBossApplicationMetaDataFactory factory = new JBossApplicationMetaDataFactory();

      //
      Unmarshaller unmarshaller = UnmarshallerFactory.newInstance().newUnmarshaller();

      //
      Object o = unmarshaller.unmarshal(jbossPortletXML.openStream(), factory, null);
      assertNotNull(o);
      assertTrue(o instanceof JBossApplicationMetaData);
      JBossApplicationMetaData app = (JBossApplicationMetaData)o;

      //
      assertNotNull(app.getPortlets());
      assertEquals(4, app.getPortlets().size());

      //
      JBossPortletMetaData portlet = (JBossPortletMetaData)app.getPortlets().get("Portlet1");
      assertNotNull(portlet);
      assertEquals("Portlet1", portlet.getName());
      assertEquals(Boolean.TRUE, portlet.getRemotable());
      HeaderContentMetaData headerContent = portlet.getHeaderContent();
      assertNotNull(headerContent);

      List elements = headerContent.getElements();
      assertEquals(3, elements.size());

      LinkElementMetaData link = (LinkElementMetaData)elements.get(0);
      assertEquals("text/css", link.getTypeAttribute());
      assertEquals("stylesheet", link.getRelAttribute());
      assertEquals("screen", link.getMediaAttribute());
      assertEquals("test.css", link.getHrefAttribute());

      ScriptElementMetaData script = (ScriptElementMetaData)elements.get(1);
      assertEquals("text/javascript", script.getTypeAttribute());
      assertEquals("test.js", script.getSrcAttribute());

      NamedMetaElementMetaData meta = (NamedMetaElementMetaData)elements.get(2);
      assertEquals("description", meta.getNameAttribute());
      assertEquals("test content", meta.getContentAttribute());

      portlet = (JBossPortletMetaData)app.getPortlets().get("Portlet2");
      assertNotNull(portlet);
      assertEquals("Portlet2", portlet.getName());
      assertNull(portlet.getRemotable());
   }

   /** JBPORTAL-1621: "title" attribute of "link" tag */
   public void testHeaderContentMetaDataTitleLink() throws Exception
   {
      URL jbossPortletXML = Thread.currentThread().getContextClassLoader().getResource("test/deployment/jboss-portlet.xml");
      assertTrue(URLTools.exists(jbossPortletXML));

      //
      JBossApplicationMetaDataFactory factory = new JBossApplicationMetaDataFactory();

      //
      Unmarshaller unmarshaller = UnmarshallerFactory.newInstance().newUnmarshaller();

      //
      Object o = unmarshaller.unmarshal(jbossPortletXML.openStream(), factory, null);
      assertNotNull(o);
      assertTrue(o instanceof JBossApplicationMetaData);
      JBossApplicationMetaData app = (JBossApplicationMetaData)o;

      //
      JBossPortletMetaData portlet = (JBossPortletMetaData)app.getPortlets().get("Portlet3");
      assertNotNull(portlet);
      assertEquals("Portlet3", portlet.getName());

      HeaderContentMetaData headerContent = portlet.getHeaderContent();
      List elements = headerContent.getElements();

      LinkElementMetaData link = (LinkElementMetaData)elements.get(0);
      assertEquals("text/css", link.getTypeAttribute());
      assertEquals("stylesheet", link.getRelAttribute());
      assertEquals("screen", link.getMediaAttribute());
      assertEquals("test.css", link.getHrefAttribute());
      assertEquals("foo", link.getTitleAttribute());

   }

   public void testPortletInfoMetadata() throws Exception
   {
      URL jbossPortletXML = Thread.currentThread().getContextClassLoader().getResource("test/deployment/jboss-portlet.xml");
      assertTrue(URLTools.exists(jbossPortletXML));

      //
      JBossApplicationMetaDataFactory factory = new JBossApplicationMetaDataFactory();

      //
      Unmarshaller unmarshaller = UnmarshallerFactory.newInstance().newUnmarshaller();

      //
      Object o = unmarshaller.unmarshal(jbossPortletXML.openStream(), factory, null);
      assertNotNull(o);
      assertTrue(o instanceof JBossApplicationMetaData);
      JBossApplicationMetaData app = (JBossApplicationMetaData)o;

      //
      JBossPortletMetaData portlet = (JBossPortletMetaData)app.getPortlets().get("Portlet4");
      assertNotNull(portlet);
      assertEquals("Portlet4", portlet.getName());

      PortletInfoMetaData portletInfo = (PortletInfoMetaData)portlet.getPortletInfo();
      System.out.println(">>>" + portletInfo.getPortletIcon());
      assertEquals("/tmp/toto.png", portletInfo.getPortletIcon().getIconLocation(PortletIconMetaData.SMALL));

   }

}
