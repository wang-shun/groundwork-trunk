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
import org.jboss.portal.common.junit.TransactionAssert;
import org.jboss.portal.core.model.content.ContentType;
import org.jboss.portal.core.model.portal.Context;
import org.jboss.portal.core.model.portal.DuplicatePortalObjectException;
import org.jboss.portal.core.model.portal.NoSuchPortalObjectException;
import org.jboss.portal.core.model.portal.Page;
import org.jboss.portal.core.model.portal.Portal;
import org.jboss.portal.core.model.portal.PortalObject;

import java.util.Collection;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class PortalObjectTestCase extends AbstractPortalObjectContainerTestCase
{
   private Context root;
   private Portal p_1;
   private Page p_1_1;
   private Page p_1_2;
   private static final int A_INDEX = 65;

   public static TestSuite suite() throws Exception
   {
      return AbstractPortalObjectContainerTestCase.suite(PortalObjectTestCase.class);
   }

   @Override
   public void setUp() throws Exception
   {
      super.setUp();

      TransactionAssert.beginTransaction();
      root = container.getContext("");
      p_1 = root.createPortal("1");
      p_1_1 = p_1.createPage("1");
      p_1_2 = p_1.createPage("2");
      p_1_1.createPage("11");
      p_1_1.createPage("12");
      p_1_1.createPage("13");
      p_1_1.createPage("14");
      p_1_1.createWindow("w1", ContentType.PORTLET, "foo1");
      p_1_1.createWindow("w2", ContentType.PORTLET, "foo2");
      p_1_1.createWindow("w3", ContentType.PORTLET, "foo3");
      p_1_2.createPage("21");
      p_1_2.createPage("22");
      p_1_2.createPage("23");
      p_1_2.createPage("24");
      TransactionAssert.commitTransaction();
   }

   public void testGetChildren() throws Exception
   {
      Collection<PortalObject> children = root.getChildren();
      assertEquals(1, children.size());

      children = p_1.getChildren();
      assertEquals(2, children.size());

      children = p_1_1.getChildren();
      assertEquals(7, children.size());

      children = p_1_2.getChildren();
      assertEquals(4, children.size());

      children = p_1_1.getChildren(PortalObject.PAGE_MASK);
      assertEquals(4, children.size());

      children = p_1_1.getChildren(PortalObject.WINDOW_MASK);
      assertEquals(3, children.size());
   }

   public void testGetChild()
   {
      PortalObject child = p_1_1.getChild("w1");
      assertNotNull(child);
      assertEquals("w1", child.getName());
      assertEquals(p_1_1, child.getParent());
   }

   public void testIterator() throws NoSuchPortalObjectException, DuplicatePortalObjectException
   {
      Portal portal;
      Page page;

      TransactionAssert.beginTransaction();
      root.destroyChild(p_1.getName());
      char nbChildren = 20;
      portal = root.createPortal("portal");
      page = portal.createPage("page");

      // create children, we need to convert the current index with Character.toString as default ordering results in
      // pa10 < pa2 and in test failure
      for (char i = A_INDEX; i < nbChildren + A_INDEX; i++)
      {
         // convert current index into a letter
         String letter = Character.toString(i);
         page.createPage("pa" + letter);
         page.createWindow("w" + letter, ContentType.PORTLET, "foo" + letter);
      }
      TransactionAssert.commitTransaction();

      Collection<PortalObject> children = page.getChildren(PortalObject.PAGE_MASK);
      assertNotNull(children);
      char i = A_INDEX;
      String name;
      for (PortalObject child : children)
      {
         name = child.getName();
         assertEquals("pa" + Character.toString(i++), name);
      }

      children = page.getChildren(PortalObject.WINDOW_MASK);
      assertNotNull(children);
      i = A_INDEX;
      for (PortalObject child : children)
      {
         name = child.getName();
         assertEquals("w" + Character.toString(i++), name);
      }
   }

   /*
   // This is commented out because this test is meant to test performance and takes a long time
   // TODO: Should be moved to a performance test suite
   public void testPerformanceToArray() throws NoSuchPortalObjectException, DuplicatePortalObjectException
   {
      Portal portal;
      Page page;

      long time = System.currentTimeMillis();
      TransactionAssert.beginTransaction();
      root.destroyChild(p_1.getName());
      portal = root.createPortal("portal");
      page = portal.createPage("page");
      int nbPages = 200;
      int nbWindows = 100;
      for(int j = 0; j < nbPages; j++)
      {
         page.createPage("page" + j);
      }
      for (int k = 0; k < nbWindows; k++)
      {
         page.createWindow("window" + k, ContentType.PORTLET, "foo" + k);
      }
      TransactionAssert.commitTransaction();
      System.out.println("Creation time = " + (System.currentTimeMillis() - time));

      time = System.currentTimeMillis();
      for (int i = 0; i < 100000; i++)
      {
         assertEquals(nbWindows, page.getChildren(PortalObject.WINDOW_MASK).toArray().length);
         assertEquals(nbPages, page.getChildren(PortalObject.PAGE_MASK).toArray().length);
      }
      System.out.println("Tests time = " + (System.currentTimeMillis() - time));
   }*/

   /*
   // This is commented out because this test is meant to test performance and takes a long time
   // TODO: Should be moved to a performance test suite
   public void testPerformanceGetChildren() throws DuplicatePortalObjectException, NoSuchPortalObjectException
   {
      Portal portal;
      Page page;

      long time = System.currentTimeMillis();
      TransactionAssert.beginTransaction();
      root.destroyChild(p_1.getName());
      int nbPortals = 5;
      int nbPages = 20;
      int nbWindows = 20;
      for(int i = 0; i < nbPortals; i++)
      {
         portal = root.createPortal("p" + i);
         for(int j = 0; j < nbPages; j++)
         {
            String name = "p" + i + "pa" + j;
            page = portal.createPage(name);
            page.createPage(name + "pa1");
            page.createPage(name + "pa2");
            for (int k = 0; k < nbWindows; k++)
            {
               page.createWindow(name + "w" + k, ContentType.PORTLET, "foo" + k);
            }
         }
      }
      TransactionAssert.commitTransaction();
      System.out.println("Creation time = " + (System.currentTimeMillis() - time));


      time = System.currentTimeMillis();
      int portalIndex = (int) (nbPortals * Math.random());
      assertEquals(nbPortals, root.getChildren().size());
      assertEquals(nbPortals, root.getChildren(PortalObject.PORTAL_MASK).size());
      assertEquals(nbPages, root.getChild("p" + portalIndex).getChildren().size());
      assertEquals(nbPages, root.getChild("p" + portalIndex).getChildren(PortalObject.PAGE_MASK).size());

      for (int i = 0; i < 1000000; i++)
      {
         portalIndex = (int) (nbPortals * Math.random());
         String portalName = "p" + portalIndex;
         PortalObject child = root.getChild(portalName);
         assertNotNull(child);
         int pageIndex = (int) (nbPages * Math.random());
         child = child.getChild(portalName + "pa" + pageIndex);
         assertNotNull(child);
         assertEquals(2 + nbWindows, child.getChildren().size());
         assertEquals(2, child.getChildren(PortalObject.PAGE_MASK).size());
         assertEquals(nbWindows, child.getChildren(PortalObject.WINDOW_MASK).size());
      }
      System.out.println("Tests time = " + (System.currentTimeMillis() - time));
   }*/
}
