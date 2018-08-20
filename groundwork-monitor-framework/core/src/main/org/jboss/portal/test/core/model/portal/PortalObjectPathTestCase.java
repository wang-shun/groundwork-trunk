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
package org.jboss.portal.test.core.model.portal;

import junit.framework.TestCase;
import org.jboss.portal.common.junit.ExtendedAssert;
import org.jboss.portal.common.util.Tools;
import org.jboss.portal.core.model.portal.PortalObjectPath;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 8786 $
 */
public class PortalObjectPathTestCase extends TestCase
{

   public void testIAE()
   {
      try
      {
         PortalObjectPath.parse("/", null);
         fail();
      }
      catch (IllegalArgumentException e)
      {
      }
      try
      {
         PortalObjectPath.parse(null, PortalObjectPath.CANONICAL_FORMAT);
         fail();
      }
      catch (IllegalArgumentException e)
      {
      }
      try
      {
         PortalObjectPath.parse(null, PortalObjectPath.LEGACY_FORMAT);
         fail();
      }
      catch (IllegalArgumentException e)
      {
      }
      try
      {
         new PortalObjectPath((String[])null);
         fail();
      }
      catch (IllegalArgumentException e)
      {
      }
      try
      {
         new PortalObjectPath((PortalObjectPath)null);
         fail();
      }
      catch (IllegalArgumentException e)
      {
      }
      try
      {
         new PortalObjectPath(null, PortalObjectPath.CANONICAL_FORMAT);
         fail();
      }
      catch (IllegalArgumentException e)
      {
      }
      try
      {
         new PortalObjectPath(null, PortalObjectPath.LEGACY_FORMAT);
         fail();
      }
      catch (IllegalArgumentException e)
      {
      }
      try
      {
         new PortalObjectPath(new String[0]).toString(null);
         fail();
      }
      catch (IllegalArgumentException e)
      {
      }
   }

   public void testFormatIAE()
   {
      testFormatIAE(PortalObjectPath.CANONICAL_FORMAT);
      testFormatIAE(PortalObjectPath.LEGACY_FORMAT);
   }

   private void testFormatIAE(PortalObjectPath.Format format)
   {
      try
      {
         format.toString(null, 0, 0);
         fail();
      }
      catch (IllegalArgumentException expected)
      {
      }
      try
      {
         format.toString((PortalObjectPath)null);
         fail();
      }
      catch (IllegalArgumentException expected)
      {
      }
      try
      {
         format.toString(new String[]{"a", null, "b"}, 0, 3);
         fail();
      }
      catch (IllegalArgumentException expected)
      {
      }
   }

   public void testCanonicalFormat()
   {
      assertEquals(new PortalObjectPath(new String[]{}), PortalObjectPath.parse("/", PortalObjectPath.CANONICAL_FORMAT));
      assertEquals(new PortalObjectPath(new String[]{"a"}), PortalObjectPath.parse("/a", PortalObjectPath.CANONICAL_FORMAT));
      assertEquals(new PortalObjectPath(new String[]{"a", "b"}), PortalObjectPath.parse("/a/b", PortalObjectPath.CANONICAL_FORMAT));

      String a = "a.b";
      String b = "c";
      PortalObjectPath pop1 = new PortalObjectPath(new String[]{a, b});
      PortalObjectPath pop2 = PortalObjectPath.parse(new PortalObjectPath(new String[]{a, b}).toString(PortalObjectPath.CANONICAL_FORMAT), PortalObjectPath.CANONICAL_FORMAT);
      assertEquals(pop1.toString(PortalObjectPath.CANONICAL_FORMAT), pop2.toString(PortalObjectPath.CANONICAL_FORMAT));

      a = ".a.b";
      b = "c";
      pop1 = new PortalObjectPath(new String[]{a, b});
      pop2 = PortalObjectPath.parse(new PortalObjectPath(new String[]{a, b}).toString(PortalObjectPath.CANONICAL_FORMAT), PortalObjectPath.CANONICAL_FORMAT);
      assertEquals(pop1.toString(PortalObjectPath.CANONICAL_FORMAT), pop2.toString(PortalObjectPath.CANONICAL_FORMAT));

      a = "\\.a.b/";
      b = "\\c";
      pop1 = new PortalObjectPath(new String[]{a, b});
      pop2 = PortalObjectPath.parse(new PortalObjectPath(new String[]{a, b}).toString(PortalObjectPath.CANONICAL_FORMAT), PortalObjectPath.CANONICAL_FORMAT);
      assertEquals(pop1.toString(PortalObjectPath.CANONICAL_FORMAT), pop2.toString(PortalObjectPath.CANONICAL_FORMAT));

      a = "/";
      pop1 = new PortalObjectPath(new String[]{a});
      pop2 = PortalObjectPath.parse(new PortalObjectPath(new String[]{a}).toString(PortalObjectPath.CANONICAL_FORMAT), PortalObjectPath.CANONICAL_FORMAT);
      assertEquals(pop1.toString(PortalObjectPath.CANONICAL_FORMAT), pop2.toString(PortalObjectPath.CANONICAL_FORMAT));

      //
      try
      {
         PortalObjectPath.parse("", PortalObjectPath.CANONICAL_FORMAT);
         fail("Was expecting an IAE");
      }
      catch (IllegalArgumentException expected)
      {
      }
      try
      {
         PortalObjectPath.parse("a", PortalObjectPath.CANONICAL_FORMAT);
         fail("Was expecting an IAE");
      }
      catch (IllegalArgumentException expected)
      {
      }
   }

   public void testLegacyFormat()
   {
      assertEquals(new PortalObjectPath(new String[]{}), PortalObjectPath.parse("", PortalObjectPath.LEGACY_FORMAT));
      assertEquals(new PortalObjectPath(new String[]{"a"}), PortalObjectPath.parse("a", PortalObjectPath.LEGACY_FORMAT));
      assertEquals(new PortalObjectPath(new String[]{"a", "b"}), PortalObjectPath.parse("a.b", PortalObjectPath.LEGACY_FORMAT));

      String a = "a.b";
      String b = "c";
      PortalObjectPath pop1 = new PortalObjectPath(new String[]{a, b});
      PortalObjectPath pop2 = PortalObjectPath.parse(new PortalObjectPath(new String[]{a, b}).toString(PortalObjectPath.LEGACY_FORMAT), PortalObjectPath.LEGACY_FORMAT);
      assertEquals(pop1.toString(PortalObjectPath.LEGACY_FORMAT), pop2.toString(PortalObjectPath.LEGACY_FORMAT));

      a = ".a.b";
      b = "c";
      pop1 = new PortalObjectPath(new String[]{a, b});
      pop2 = PortalObjectPath.parse(new PortalObjectPath(new String[]{a, b}).toString(PortalObjectPath.LEGACY_FORMAT), PortalObjectPath.LEGACY_FORMAT);
      assertEquals(pop1.toString(PortalObjectPath.LEGACY_FORMAT), pop2.toString(PortalObjectPath.LEGACY_FORMAT));

      a = "\\.a.b/";
      b = "\\c";
      pop1 = new PortalObjectPath(new String[]{a, b});
      pop2 = PortalObjectPath.parse(new PortalObjectPath(new String[]{a, b}).toString(PortalObjectPath.LEGACY_FORMAT), PortalObjectPath.LEGACY_FORMAT);
      assertEquals(pop1.toString(PortalObjectPath.LEGACY_FORMAT), pop2.toString(PortalObjectPath.LEGACY_FORMAT));
   }

   public void testEquals()
   {
      assertTrue(new PortalObjectPath(new String[]{}).equals(new PortalObjectPath(new String[]{})));
      assertFalse(new PortalObjectPath(new String[]{}).equals(new PortalObjectPath(new String[]{"a"})));
      assertFalse(new PortalObjectPath(new String[]{}).equals(new PortalObjectPath(new String[]{"a", "b"})));
      assertFalse(new PortalObjectPath(new String[]{"a"}).equals(new PortalObjectPath(new String[]{})));
      assertTrue(new PortalObjectPath(new String[]{"a"}).equals(new PortalObjectPath(new String[]{"a"})));
      assertFalse(new PortalObjectPath(new String[]{"a"}).equals(new PortalObjectPath(new String[]{"a", "b"})));
      assertFalse(new PortalObjectPath(new String[]{"a", "b"}).equals(new PortalObjectPath(new String[]{})));
      assertFalse(new PortalObjectPath(new String[]{"a", "b"}).equals(new PortalObjectPath(new String[]{"a"})));
      assertTrue(new PortalObjectPath(new String[]{"a", "b"}).equals(new PortalObjectPath(new String[]{"a", "b"})));
   }

   public void testComparable()
   {
      assertTrue(new PortalObjectPath(new String[]{"a"}).compareTo(new PortalObjectPath(new String[]{"a"})) == 0);
      assertTrue(new PortalObjectPath(new String[]{"a"}).compareTo(new PortalObjectPath(new String[]{"b"})) < 0);
      assertTrue(new PortalObjectPath(new String[]{"b"}).compareTo(new PortalObjectPath(new String[]{"a"})) > 0);
      assertTrue(new PortalObjectPath(new String[]{"a"}).compareTo(new PortalObjectPath(new String[]{"a", "b"})) > 0);
      assertTrue(new PortalObjectPath(new String[]{"a", "b"}).compareTo(new PortalObjectPath(new String[]{"a"})) < 0);
   }

   public void testIterator()
   {
      ExtendedAssert.assertEquals(new Object[]{}, Tools.toArray(new PortalObjectPath(new String[]{}).names()));
      ExtendedAssert.assertEquals(new Object[]{"a"}, Tools.toArray(new PortalObjectPath(new String[]{"a"}).names()));
      ExtendedAssert.assertEquals(new Object[]{"a", "b"}, Tools.toArray(new PortalObjectPath(new String[]{"a", "b"}).names()));
   }

   public void testGetChild()
   {
      assertEquals(new PortalObjectPath(new String[]{"a"}), new PortalObjectPath(new String[]{}).getChild("a"));
      assertEquals(new PortalObjectPath(new String[]{"a", "b"}), new PortalObjectPath(new String[]{"a"}).getChild("b"));
   }

   public void testGetParent()
   {
      assertEquals(null, new PortalObjectPath(new String[]{}).getParent());
      assertEquals(new PortalObjectPath(new String[]{}), new PortalObjectPath(new String[]{"a"}).getParent());
      assertEquals(new PortalObjectPath(new String[]{"a"}), new PortalObjectPath(new String[]{"a", "b"}).getParent());
      assertEquals(new PortalObjectPath(new String[]{"a", "b"}), new PortalObjectPath(new String[]{"a", "b", "c"}).getParent());
   }
}
