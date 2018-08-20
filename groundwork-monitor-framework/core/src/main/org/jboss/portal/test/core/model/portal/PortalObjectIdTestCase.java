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
import org.jboss.portal.core.model.portal.PortalObjectId;
import org.jboss.portal.core.model.portal.PortalObjectPath;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 8786 $
 */
public class PortalObjectIdTestCase extends TestCase
{

   public void testIAE()
   {
      try
      {
         PortalObjectId.parse("/", null);
         fail();
      }
      catch (IllegalArgumentException e)
      {
      }
      try
      {
         PortalObjectId.parse(null, PortalObjectPath.CANONICAL_FORMAT);
         fail();
      }
      catch (IllegalArgumentException e)
      {
      }
      try
      {
         PortalObjectId.parse("", "/", null);
         fail();
      }
      catch (IllegalArgumentException e)
      {
      }
      try
      {
         PortalObjectId.parse("", null, PortalObjectPath.CANONICAL_FORMAT);
         fail();
      }
      catch (IllegalArgumentException e)
      {
      }
      try
      {
         PortalObjectId.parse(null, "/", PortalObjectPath.CANONICAL_FORMAT);
         fail();
      }
      catch (IllegalArgumentException e)
      {
      }
      try
      {
         PortalObjectId.toString(null, PortalObjectPath.ROOT_PATH, PortalObjectPath.CANONICAL_FORMAT);
         fail();
      }
      catch (IllegalArgumentException e)
      {
      }
      try
      {
         PortalObjectId.toString("", null, PortalObjectPath.CANONICAL_FORMAT);
         fail();
      }
      catch (IllegalArgumentException e)
      {
      }
      try
      {
         PortalObjectId.toString("", PortalObjectPath.ROOT_PATH, null);
         fail();
      }
      catch (IllegalArgumentException e)
      {
      }
      PortalObjectId id = new PortalObjectId("", PortalObjectPath.ROOT_PATH);
      try
      {
         id.toString(null);
         fail();
      }
      catch (IllegalArgumentException e)
      {
      }
      try
      {
         new PortalObjectId(null, PortalObjectPath.ROOT_PATH);
         fail();
      }
      catch (IllegalArgumentException e)
      {
      }
      try
      {
         new PortalObjectId("", null);
         fail();
      }
      catch (IllegalArgumentException e)
      {
      }
   }

   public void testParseCanonicalFormat()
   {
      assertCanonicalFormatEquals(new PortalObjectId("", new PortalObjectPath(new String[]{})), "/");
      assertCanonicalFormatEquals(new PortalObjectId("", new PortalObjectPath(new String[]{"a"})), "/a");
      assertCanonicalFormatEquals(new PortalObjectId("", new PortalObjectPath(new String[]{"a", "b"})), "/a/b");

      //
      assertCanonicalFormatEquals(new PortalObjectId("ns", new PortalObjectPath(new String[]{})), "ns:/");
      assertCanonicalFormatEquals(new PortalObjectId("ns", new PortalObjectPath(new String[]{"a"})), "ns:/a");
      assertCanonicalFormatEquals(new PortalObjectId("ns", new PortalObjectPath(new String[]{"a", "b"})), "ns:/a/b");

      //
      assertCanonicalFormatParse(new PortalObjectId("", new PortalObjectPath(new String[]{})), ":/");
      assertCanonicalFormatParse(new PortalObjectId("", new PortalObjectPath(new String[]{"a"})), ":/a");
      assertCanonicalFormatParse(new PortalObjectId("", new PortalObjectPath(new String[]{"a", "b"})), ":/a/b");
   }

   private void assertCanonicalFormatParse(PortalObjectId id, String string)
   {
      assertEquals(id, PortalObjectId.parse(string, PortalObjectPath.CANONICAL_FORMAT));
   }

   private void assertCanonicalFormatEquals(PortalObjectId id, String string)
   {
      assertEquals(id, PortalObjectId.parse(string, PortalObjectPath.CANONICAL_FORMAT));
      assertEquals(id.toString(PortalObjectPath.CANONICAL_FORMAT), string);
   }

   public void testParseLegacyFormat()
   {
      assertLegacyFormatEquals(new PortalObjectId("", new PortalObjectPath(new String[]{})), "");
      assertLegacyFormatEquals(new PortalObjectId("", new PortalObjectPath(new String[]{"a"})), "a");
      assertLegacyFormatEquals(new PortalObjectId("", new PortalObjectPath(new String[]{"a", "b"})), "a.b");

      //
      assertLegacyFormatEquals(new PortalObjectId("ns", new PortalObjectPath(new String[]{})), "ns:");
      assertLegacyFormatEquals(new PortalObjectId("ns", new PortalObjectPath(new String[]{"a"})), "ns:a");
      assertLegacyFormatEquals(new PortalObjectId("ns", new PortalObjectPath(new String[]{"a", "b"})), "ns:a.b");

      //
      assertLegacyFormatParse(new PortalObjectId("", new PortalObjectPath(new String[]{})), ":");
      assertLegacyFormatParse(new PortalObjectId("", new PortalObjectPath(new String[]{"a"})), ":a");
      assertLegacyFormatParse(new PortalObjectId("", new PortalObjectPath(new String[]{"a", "b"})), ":a.b");
   }

   private void assertLegacyFormatParse(PortalObjectId id, String string)
   {
      assertEquals(id, PortalObjectId.parse(string, PortalObjectPath.LEGACY_FORMAT));
   }

   private void assertLegacyFormatEquals(PortalObjectId id, String string)
   {
      assertEquals(id, PortalObjectId.parse(string, PortalObjectPath.LEGACY_FORMAT));
      assertEquals(id.toString(PortalObjectPath.LEGACY_FORMAT), string);
   }
}
