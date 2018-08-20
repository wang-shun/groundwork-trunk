/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2009, Red Hat Middleware, LLC, and individual                    *
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

package org.jboss.portal.test.core.model.portal;

import junit.framework.TestSuite;
import org.jboss.portal.Mode;
import org.jboss.portal.WindowState;
import org.jboss.portal.common.junit.TransactionAssert;
import org.jboss.portal.common.util.CollectionBuilder;
import org.jboss.portal.core.model.content.ContentType;
import org.jboss.portal.core.model.content.spi.handler.ContentState;
import org.jboss.portal.core.model.portal.DuplicatePortalObjectException;
import org.jboss.portal.core.model.portal.Page;
import org.jboss.portal.core.model.portal.Portal;
import org.jboss.portal.core.model.portal.PortalContainer;
import org.jboss.portal.core.model.portal.PortalObject;
import org.jboss.portal.core.model.portal.PortalObjectId;
import org.jboss.portal.core.model.portal.PortalObjectPath;
import org.jboss.portal.core.model.portal.Window;
import org.jboss.portal.test.core.model.content.SimpleContent;
import org.jboss.portal.test.framework.AbstractPortalTestCase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Portal Object Container Test Cases based on the microcontainer architecture
 *
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @author <a href="mailto:anil.saldhana@jboss.org">Anil Saldhana</a>
 * @version $Revision: 12653 $
 */
public class PortalObjectContainerTestCase extends AbstractPortalObjectContainerTestCase
{

   public static TestSuite suite() throws Exception
   {
      return AbstractPortalTestCase.suite(PortalObjectContainerTestCase.class);
   }

   /** todo same with a transaction wrapping the start method */
   public void testRootNodeCreation() throws Exception
   {
      TransactionAssert.beginTransaction();
      PortalObject root = container.getContext();
      assertNotNull(root);
      TransactionAssert.commitTransaction();
   }

   public void testDashboardNodeCreation() throws DuplicatePortalObjectException
   {
      TransactionAssert.beginTransaction();
      PortalObject root = container.getContext();
      PortalObject dashboard = container.createContext("dashboard");
      assertNotNull(dashboard);
      assertFalse(root.equals(dashboard));
      TransactionAssert.commitTransaction();
   }

   public void testRetrieveNonExistingObject()
   {
      TransactionAssert.beginTransaction();
      PortalObject foo = container.getObject(PortalObjectId.parse("/foo", PortalObjectPath.CANONICAL_FORMAT));
      assertNull(foo);
      TransactionAssert.commitTransaction();
   }

   public void testPropertyUpdateCascadeToDescendantsWhenTheyDoNotDeclareIt() throws Exception
   {
      TransactionAssert.beginTransaction();
      PortalContainer ctx = container.getContext();
      Portal n1 = ctx.createPortal("default");
      Page n2 = n1.createPage("default");
      Page n3 = n2.createPage("default");
      Map p1 = n1.getProperties();
      Map p2 = n2.getProperties();
      Map p3 = n3.getProperties();

      //
      n1.setDeclaredProperty("foo", "bar1");
      assertEquals("bar1", p1.get("foo"));
      assertEquals("bar1", p2.get("foo"));
      assertEquals("bar1", p3.get("foo"));

      //
      n2.setDeclaredProperty("foo", "bar2");
      assertEquals("bar1", p1.get("foo"));
      assertEquals("bar2", p2.get("foo"));
      assertEquals("bar2", p3.get("foo"));

      TransactionAssert.commitTransaction();
   }

   public void testRegisterObjectWithSameName() throws Exception
   {
      TransactionAssert.beginTransaction();
      PortalContainer ctx = container.getContext();
      Portal n1 = ctx.createPortal("default");
      n1.createPage("default");
      boolean duplicate = false;
      try
      {
         n1.createPage("default");
      }
      catch (DuplicatePortalObjectException e)
      {
         duplicate = true;
      }
      assertTrue(duplicate);

      TransactionAssert.commitTransaction();
   }

   public void testRegisterObjectWithSameNameCaseInsensitive() throws Exception
   {
      // Some database will create 2 pages, while some other will consider
      // both names as equal.
      TransactionAssert.beginTransaction();
      PortalContainer ctx = container.getContext();
      Portal n1 = ctx.createPortal("default");
      n1.createPage("default");

      TransactionAssert.commitTransaction();

      boolean rollback = false;

      TransactionAssert.beginTransaction();
      try
      {
         n1.createPage("dEfAuLt");
      }
      catch (DuplicatePortalObjectException e)
      {
         rollback = true;
         TransactionAssert.rollbackTransaction(false);
      }
      if (!rollback)
      {
         TransactionAssert.commitTransaction();
      }
   }

   public void testPropertyUpdateDoesNotCascadeToDescendantsWhenTheyDeclareIt() throws Exception
   {
      TransactionAssert.beginTransaction();
      PortalContainer ctx = container.getContext();
      Portal n1 = ctx.createPortal("default");
      Page n2 = n1.createPage("default");
      Page n3 = n2.createPage("default");
      Map p1 = n1.getProperties();
      Map p2 = n2.getProperties();
      Map p3 = n3.getProperties();

      //
      n2.setDeclaredProperty("foo", "bar1");
      assertEquals(null, p1.get("foo"));
      assertEquals("bar1", p2.get("foo"));
      assertEquals("bar1", p3.get("foo"));

      //
      n1.setDeclaredProperty("foo", "bar2");
      assertEquals("bar2", p1.get("foo"));
      assertEquals("bar1", p2.get("foo"));
      assertEquals("bar1", p3.get("foo"));

      //
      TransactionAssert.commitTransaction();
   }

   public void testOverridenPropertyRemovalPropagateParentValueToDescendants() throws Exception
   {
      TransactionAssert.beginTransaction();
      PortalContainer ctx = container.getContext();
      Portal n1 = ctx.createPortal("default");
      Page n2 = n1.createPage("default");
      Page n3 = n2.createPage("default");
      Map p1 = n1.getProperties();
      Map p2 = n2.getProperties();
      Map p3 = n3.getProperties();

      //
      n1.setDeclaredProperty("foo", "bar1");
      n2.setDeclaredProperty("foo", "bar2");
      assertEquals("bar1", p1.get("foo"));
      assertEquals("bar2", p2.get("foo"));
      assertEquals("bar2", p3.get("foo"));

      //
      n2.setDeclaredProperty("foo", null);
      assertEquals("bar1", p1.get("foo"));
      assertEquals("bar1", p2.get("foo"));
      assertEquals("bar1", p3.get("foo"));

      //
      TransactionAssert.commitTransaction();
   }

   /**
    * Regression test for JBPORTAL-1541...
    *
    * @throws DuplicatePortalObjectException
    */
   public void testSetDeclaredPropertyWithoutParent() throws DuplicatePortalObjectException
   {
      TransactionAssert.beginTransaction();
      PortalContainer ctx = container.createContext("test");
      ctx.setDeclaredProperty("name", null);
      TransactionAssert.commitTransaction();
   }

   public void testCRUD() throws Exception
   {
      TransactionAssert.beginTransaction();
      PortalContainer ctx = container.getContext();
      Portal portal = ctx.createPortal("default");
      PortalObjectId portalId = portal.getId();
      assertNotNull(portalId);
      assertNotNull(portal);
      portal.setDeclaredProperty("foo1", "bar1");
      portal.setDeclaredProperty("foo2", "bar2");
      assertEquals(ctx, portal.getParent());
      assertEquals(portal, ctx.getChild("default"));
      assertEquals("bar1", portal.getDeclaredProperty("foo1"));
      assertEquals("bar1", portal.getProperty("foo1"));
      assertEquals("bar2", portal.getDeclaredProperty("foo2"));
      assertEquals("bar2", portal.getProperty("foo2"));

      //
      Page page = portal.createPage("default");
      assertNotNull(page);
      PortalObjectId pageId = page.getId();
      assertNotNull(pageId);
      page.setDeclaredProperty("foo2", "bar2_");
      page.setDeclaredProperty("foo3", "bar3");
      assertEquals(portal, page.getParent());
      assertEquals(page, portal.getChild("default"));
      assertNull(page.getDeclaredProperty("foo1"));
      assertEquals("bar1", page.getProperty("foo1"));
      assertEquals("bar2_", page.getDeclaredProperty("foo2"));
      assertEquals("bar2_", page.getProperty("foo2"));
      assertEquals("bar3", page.getDeclaredProperty("foo3"));
      assertEquals("bar3", page.getProperty("foo3"));

      //
      Window window = page.createWindow("default", ContentType.PORTLET, "uri");
      assertNotNull(window);
      PortalObjectId windowId = window.getId();
      assertNotNull(windowId);
      assertEquals(page, window.getParent());
      assertEquals(window, page.getChild("default"));
      TransactionAssert.commitTransaction();

      //
      TransactionAssert.beginTransaction();
      PortalObject portalIdObject = container.getObject(portalId);
      assertNotNull(portalIdObject);
      assertTrue(portalIdObject instanceof Portal);
      assertEquals(portalId, portalIdObject.getId());
      PortalObject pageIdObject = container.getObject(pageId);
      assertNotNull(pageIdObject);
      assertTrue(pageIdObject instanceof Page);
      assertEquals(pageId, pageIdObject.getId());
      PortalObject windowIdObject = container.getObject(windowId);
      assertNotNull(windowIdObject);
      assertTrue(windowIdObject instanceof Window);
      assertEquals(windowId, windowIdObject.getId());
      TransactionAssert.commitTransaction();

      //
      TransactionAssert.beginTransaction();
      ctx = container.getContext();
      assertNotNull(ctx);
      portal = ctx.getPortal("default");
      assertNotNull(portal);
      assertEquals(portalId, portal.getId());
      page = portal.getPage("default");
      assertNotNull(page);
      assertEquals(pageId, page.getId());
      window = page.getWindow("default");
      assertNotNull(window);
      assertEquals(windowId, window.getId());
      TransactionAssert.commitTransaction();

      //
      TransactionAssert.beginTransaction();
      ctx = container.getContext();
      assertNotNull(ctx);
      ctx.destroyChild("default");
      portal = ctx.getPortal("default");
      assertNull(portal);
      TransactionAssert.commitTransaction();

      //
      TransactionAssert.beginTransaction();
      ctx = (PortalContainer)container.getContext();
      portal = ctx.getPortal("default");
      assertNull(portal);
      TransactionAssert.commitTransaction();
   }

   public void testGetChildren() throws Exception
   {
      TransactionAssert.beginTransaction();
      PortalContainer ctx = container.getContext();
      Portal portal_1 = ctx.createPortal("1");
      Page page_1_1 = portal_1.createPage("1");
      Page page_1_2 = portal_1.createPage("2");
      Page page_1_1_1 = page_1_1.createPage("1");
      Window window_1_1_2 = page_1_1.createWindow("2", ContentType.PORTLET, "uri");
      Page page_1_1_3 = page_1_1.createPage("3");
      Window window_1_1_4 = page_1_1.createWindow("4", ContentType.PORTLET, "uri");
      TransactionAssert.commitTransaction();

      //
      TransactionAssert.beginTransaction();
      PortalObject o_1 = container.getContext().getChild("1");
      PortalObject o_1_1 = o_1.getChild("1");
      PortalObject o_1_2 = o_1.getChild("2");
      PortalObject o_1_1_1 = o_1_1.getChild("1");
      PortalObject o_1_1_2 = o_1_1.getChild("2");
      PortalObject o_1_1_3 = o_1_1.getChild("3");
      PortalObject o_1_1_4 = o_1_1.getChild("4");
      List l1 = new ArrayList(o_1_1.getChildren());
      Collections.sort(l1, new PortalObjectComparator());
      assertEquals(CollectionBuilder.arrayList().add(o_1_1_1).add(o_1_1_2).add(o_1_1_3).add(o_1_1_4).get(), l1);
      List l2 = new ArrayList(o_1_1.getChildren(PortalObject.PAGE_MASK));
      Collections.sort(l2, new PortalObjectComparator());
      assertEquals(CollectionBuilder.arrayList().add(o_1_1_1).add(o_1_1_3).get(), l2);
      List l3 = new ArrayList(o_1_1.getChildren(PortalObject.WINDOW_MASK));
      Collections.sort(l3, new PortalObjectComparator());
      assertEquals(CollectionBuilder.arrayList().add(o_1_1_2).add(o_1_1_4).get(), l3);
      List l4 = new ArrayList(o_1_1.getChildren(0));
      Collections.sort(l4, new PortalObjectComparator());
      assertEquals(CollectionBuilder.arrayList().get(), l4);
      List l5 = new ArrayList(o_1_1.getChildren(PortalObject.PORTAL_MASK));
      Collections.sort(l5, new PortalObjectComparator());
      assertEquals(CollectionBuilder.arrayList().get(), l5);
      TransactionAssert.commitTransaction();
   }

   public void testRecreate() throws Exception
   {
      PortalObjectId defaultId = PortalObjectId.parse("/default", PortalObjectPath.CANONICAL_FORMAT);

      //
      TransactionAssert.beginTransaction();
      PortalContainer ctx = container.getContext();
      Portal portal = ctx.createPortal("default");
      assertNotNull(portal);

      //
      PortalObject object = container.getObject(defaultId);
      assertNotNull(object);

      //
      ctx.destroyChild("default");
      ctx.createPortal("default");

      //
      object = container.getObject(defaultId);
      assertNotNull(object);

      //
      TransactionAssert.commitTransaction();
   }

   /**
    * @throws Exception
    * @todo test contentState / get/set property
    */
   public void testCopy() throws Exception
   {
      PortalObjectId defaultId = PortalObjectId.parse("/portal", PortalObjectPath.CANONICAL_FORMAT);

      //
      TransactionAssert.beginTransaction();
      PortalContainer ctx = container.getContext();
      Portal portal = ctx.createPortal("portal");
      portal.setDeclaredProperty("portalname", "portalvalue");
      portal.getSupportedWindowStates().add(WindowState.NORMAL);
      portal.getSupportedModes().add(Mode.VIEW);
      Page page = portal.createPage("default");
      page.setDeclaredProperty("pagename", "pagevalue");
      Window window = page.createWindow("window", ContentType.PORTLET, "uri");
      window.setDeclaredProperty("windowname", "windowvalue");
      SimpleContent content = (SimpleContent)window.getContent();
      assertNotNull(content);
      ContentState contentState = content.getState();
      assertNotNull(contentState);
      contentState.setParameter("abc", "def");
      TransactionAssert.commitTransaction();

      //
      TransactionAssert.beginTransaction();
      ctx = container.getContext();
      portal = (Portal)container.getObject(defaultId);
      portal = (Portal)portal.copy(ctx, "copy", true);
      assertNotNull(portal);
      assertEquals("copy", portal.getName());
      assertEquals(Collections.singleton(WindowState.NORMAL), portal.getSupportedWindowStates());
      assertEquals(Collections.singleton(Mode.VIEW), portal.getSupportedModes());
      assertEquals("portalvalue", portal.getDeclaredProperty("portalname"));
      page = portal.getPage("default");
      assertNotNull(page);
      assertEquals("pagevalue", page.getDeclaredProperty("pagename"));
      window = page.getWindow("window");
      assertNotNull(window);
      assertEquals("windowvalue", window.getDeclaredProperty("windowname"));
      assertEquals(ContentType.PORTLET, window.getContentType());
      content = (SimpleContent)window.getContent();
      assertNotNull(content);
      contentState = content.getState();
      assertNotNull(contentState);
      assertEquals("def", contentState.getParameter("abc"));
      TransactionAssert.commitTransaction();
   }

   private static class PortalObjectComparator implements Comparator
   {
      public int compare(Object o1, Object o2)
      {
         PortalObject po1 = (PortalObject)o1;
         PortalObject po2 = (PortalObject)o2;
         return po1.getId().compareTo(po2.getId());
      }
   }
}
